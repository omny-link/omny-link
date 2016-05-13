package link.omny.catalog.web;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import link.omny.catalog.internal.CatalogCsvImporter;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.repositories.MediaResourceRepository;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.repositories.StockItemRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

/**
 * REST web service for accessing stock items.
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/stock-items")
public class StockItemController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(StockItemController.class);

    @Autowired
    private StockItemRepository stockItemRepo;

    @Autowired
    private StockCategoryRepository stockCategoryRepo;

    @Autowired
    private MediaResourceRepository mediaResourceRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Imports JSON representation of stockItems.
     *
     * <p>
     * This is a handy link: http://shancarter.github.io/mr-data-converter/
     *
     * @param file
     *            A file posted in a multi-part request
     * @return The meta data of the added model
     * @throws IOException
     *             If cannot parse the JSON.
     */
    @RequestMapping(value = "/uploadcsv", method = RequestMethod.POST)
    public @ResponseBody Iterable<StockItem> handleCsvFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading CSV stockItems for: %1$s",
                tenantId));

        String content = new String(file.getBytes());
        List<StockItem> list = new CatalogCsvImporter().readStockItems(
                new StringReader(content),
                content.substring(0, content.indexOf('\n')).split(","),
                tenantId);
        LOGGER.info(String.format("  found %1$d stockItems", list.size()));

        List<StockItem> tmp = new ArrayList<StockItem>();
        for (StockItem stockItem : list) {
            if (stockItem.getStockCategory() != null
                    && stockItem.getStockCategory().getName() != null) {
                stockItem.setStockCategory(stockCategoryRepo.findByName(
                        stockItem.getStockCategory().getName(), tenantId));
            } else {
                stockItem.setStockCategory(null);
            }
            tmp.add(stockItem);
        }

        Iterable<StockItem> result = stockItemRepo.save(tmp);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Return just the stockItems for a specific tenant.
     * 
     * @return stockItems for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockItem> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @AuthenticationPrincipal UserDetails activeUser,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List stockItems for tenant %1$s", tenantId));

        List<StockItem> list;
        if (limit == null) {
            // TODO unfortunately activeUser is null, prob some kind of class
            // cast error it seems
            // Use SecurityContextHolder as temporary fallback
            Authentication authentication = SecurityContextHolder.getContext()
                    .getAuthentication();

            for (GrantedAuthority a : authentication.getAuthorities()) {
                System.out.println("  " + a.getAuthority());
                System.out.println("  "
                        + a.getAuthority().equals("ROLE_editor"));
                System.out.println("  " + a.getAuthority().equals("editor"));
            }

            // if (authentication.getAuthorities().contains("ROLE_editor")) {
            list = stockItemRepo.findAllForTenant(tenantId);
            // } else {
            // list = stockItemRepo.findAllForTenantOwnedByUser(tenantId,
            // authentication.getName());
            // }
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = stockItemRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s stockItems", list.size()));

        return wrap(list);
    }

    /**
     * Return just the matching stock item.
     * 
     * @return stock item for that tenant with the matching id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody ShortStockItem findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find contact for id %1$s", id));

        return wrap(stockItemRepo.findOne(Long.parseLong(id)));
    }

    /**
     * Return just the matching stockItems (probably will be one in almost every
     * case).
     * 
     * @return stockItems for that tenant.
     */
    @RequestMapping(value = "/stock-category/{locationName}", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockItem> getForLocationName(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("locationName") String locationName) {

        return wrap(stockItemRepo
                .findAllForCategoryName(locationName, tenantId));
    }

    /**
     * Create a new stockItem.
     * 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody StockItem stockItem) {
        stockItem.setTenantId(tenantId);
        stockItemRepo.save(stockItem);

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", stockItem.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/{id}").buildAndExpand(vars).toUri());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing stockItem.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId,
            @RequestBody StockItem updatedStockItem) {
        StockItem stockItem = stockItemRepo.findOne(stockItemId);

        BeanUtils.copyProperties(updatedStockItem, stockItem, "id");
        stockItem.setTenantId(tenantId);
        stockItemRepo.save(stockItem);
    }

    /**
     * Add a media resource to the specified stockItem.
     */
    @RequestMapping(value = "/{stockItemId}/images", method = RequestMethod.POST)
    public @ResponseBody void addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @RequestParam("author") String author,
            @RequestParam("url") String url) {

        addMediaResource(tenantId, stockItemId, new MediaResource(author, url));
    }

    /**
     * Add a media resource to the specified stockItem.
     */
    public @ResponseBody void addMediaResource(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @RequestBody MediaResource mediaResource) {
        StockItem stockItem = stockItemRepo.findOne(stockItemId);
        mediaResource.setStockItem(stockItem);
        mediaResourceRepo.save(mediaResource);
        // necessary to force a save
        stockItem.setLastUpdated(new Date());
        stockItemRepo.save(stockItem);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{categoryId}/stockCategory", method = RequestMethod.PUT, consumes = { "application/json" })
    @Transactional
    public @ResponseBody void setStockCategory(
            @PathVariable("tenantId") String tenantId,
            @RequestBody String itemUri,
            @PathVariable("categoryId") Long categoryId) {
        LOGGER.info(String.format("Linking item %2$s to category %1$s",
                categoryId, itemUri));

        Long itemId = Long
                .parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1));

        stockItemRepo.setStockCategory(itemId, categoryId);
    }

    /**
     * Delete an existing stockItem.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId) {
        StockItem stockItem = stockItemRepo.findOne(stockItemId);

        stockItemRepo.delete(stockItem);
    }

    private List<ShortStockItem> wrap(List<StockItem> list) {
        List<ShortStockItem> resources = new ArrayList<ShortStockItem>(
                list.size());
        for (StockItem stockItem : list) {
            resources.add(wrap(stockItem));
        }
        return resources;
    }

    private ShortStockItem wrap(StockItem stockItem) {
        ShortStockItem resource = new ShortStockItem();
        BeanUtils.copyProperties(stockItem, resource);
        if (stockItem.getStockCategory() != null) {
            resource.setStockCategoryName(stockItem.getStockCategory()
                    .getName());
        }
        resource.setPrice(stockItem.getPriceString());
        resource.setMapUrl(stockItem.getStockCategory().getMapUrl());
        resource.setDirectionsByAir(stockItem.getStockCategory()
                .getDirectionsByAir());
        resource.setDirectionsByPublicTransport(stockItem.getStockCategory()
                .getDirectionsByPublicTransport());
        resource.setDirectionsByRoad(stockItem.getStockCategory()
                .getDirectionsByRoad());

        Link detail = linkTo(StockItemRepository.class, stockItem.getId())
                .withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    private Link linkTo(
            @SuppressWarnings("rawtypes") Class<? extends CrudRepository> clazz,
            Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortStockItem extends ResourceSupport {
        private String selfRef;
        private String name;
        private String stockCategoryName;
        private String type;
        private String price;
        private String mapUrl;
        private String directionsByRoad;
        private String directionsByPublicTransport;
        private String directionsByAir;
        private Date created;
        private Date lastUpdated;
        private List<MediaResource> images;
    }
}
