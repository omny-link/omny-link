package link.omny.catalog.web;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import link.omny.catalog.model.GeoPoint;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.repositories.StockItemRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/**
 * REST web service for accessing stock items.
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/stock-categories")
public class StockCategoryController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(StockCategoryController.class);

    @Value("${omny.catalog.searchRadius:100}")
    private String searchRadius;

    private int iSearchRadius;

    @Autowired
    private StockCategoryRepository stockCategoryRepo;

    @Autowired
    private StockItemRepository stockItemRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeolocationService geo;

    public int getSearchRadius() {
        if (iSearchRadius == 0) {
            iSearchRadius = Integer.parseInt(searchRadius);
        }
        return iSearchRadius;
    }

    /**
     * Imports JSON representation of stockCategorys.
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
    public @ResponseBody Iterable<StockCategory> handleCsvFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading CSV stockCategorys for: %1$s",
                tenantId));

        throw new RuntimeException("Not yet implemented");

        // String content = new String(file.getBytes());
        // List<StockCategory> list = new CsvImporter().readStockCategorys(
        // new StringReader(
        // content), content.substring(0, content.indexOf('\n'))
        // .split(","));
        // LOGGER.info(String.format("  found %1$d stockCategorys",
        // list.size()));
        // for (StockCategory stockCategory : list) {
        // stockCategory.setTenantId(tenantId);
        // }
        //
        // Iterable<StockCategory> result = stockCategoryRepo.save(list);
        // LOGGER.info("  saved.");
        // return result;
    }

    /**
     * Return just the stockCategorys for a specific tenant.
     * 
     * @return stockCategorys for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockCategory> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List stockCategorys for tenant %1$s",
                tenantId));

        List<StockCategory> list;
        if (limit == null) {
            list = stockCategoryRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = stockCategoryRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s stockCategorys", list.size()));

        return wrap(list);
    }

    @RequestMapping(value = "/findByName", method = RequestMethod.GET)
    public @ResponseBody ShortStockCategory findByName(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("name") String name,
            @RequestParam(value = "type", required = false) String type)
            throws IOException {
        LOGGER.info(String.format("findByName %1$s, type %2$s for tenant %3$s",
                name, type, tenantId));

        StockCategory category = stockCategoryRepo.findByName(name, tenantId);
        if (category == null) {
            throw new EntityNotFoundException(String.format(
                    "No Stock Category with name %1$s", name));
        }

        if (type != null && type.trim().length() > 0) {
            filter(category, type);
        }

        ShortStockCategory shortStockCategory = wrap(category);
        return shortStockCategory;
    }

    /**
     * Return Stock Categories for a specific tenant within a given distance of
     * the search location.
     * 
     * @return stockCategories for that tenant.
     * @throws IOException
     *             If unable to contact the geo-coding service or it in turn
     *             throws an exception.
     */
    @RequestMapping(value = "/findByLocation", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockCategory> findByLocation(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit)
            throws IOException {
        LOGGER.info(String.format("List stockCategories for tenant %1$s",
                tenantId));

        List<StockCategory> list = new ArrayList<StockCategory>();
        GeoPoint qPoint = null;
        if (q != null && q.trim().length() > 0) {
            try {
                qPoint = geo.locate(q);
            } catch (UnknownHostException e) {
                LOGGER.error(String.format(
                        "Unable to geo locate '%1$s', will return unfiltered list", q));
                q = null;
            }
        }

        List<StockCategory> tmpList = null;
        if (type == null || type.trim().length() == 0) {
            tmpList = stockCategoryRepo.findAllForTenant(tenantId);
        } else {
            tmpList = stockCategoryRepo.findByTypeForTenant(tenantId, type);
        }
        for (StockCategory stockCategory : tmpList) {
            try {
                if (stockCategory.getLng() == 0.0d) {
                    stockCategory.setGeoPoint(geo.locate(stockCategory
                            .getPostCode()));
                    stockCategoryRepo.save(stockCategory);
                }

                if (matchQuery(q, qPoint, stockCategory)) {
                    // TODO On the face of it the SQL query does this already,
                    // but something is going wrong
                    filter(stockCategory, type);
                    list.add(stockCategory);
                }
            } catch (UnknownHostException e) {
                LOGGER.error(String
                        .format("Unable to geo locate '%1$s', will return unfiltered list",
                                stockCategory.getPostCode()));
                // TODO see above
                filter(stockCategory, type);
                list.add(stockCategory);
            } catch (Exception e) {
                LOGGER.error(String.format(
                        "Exception calculating distance of %1$s from %2$s",
                        stockCategory.getName(), q), e);
            }
        }
        Collections
                .sort(list,
                        (o1, o2) -> ((int) o1.getDistance())
                                - ((int) o2.getDistance()));

        LOGGER.info(String.format("Found %1$s stockCategories", list.size()));

        return wrap(list);
    }

    private void filter(final StockCategory stockCategory, final String type) {
        ArrayList<StockItem> filteredItems = new ArrayList<StockItem>();
        for (StockItem item : stockCategory.getStockItems()) {
            if (type == null || item.getType().equalsIgnoreCase(type)) {
                filteredItems.add(item);
            }
        }
        stockCategory.setStockItems(filteredItems);
    }

    private boolean matchQuery(String q, GeoPoint qPoint,
            StockCategory stockCategory) {
        return q == null
                || q.trim().length() == 0
                || geo.distance(qPoint, stockCategory) <= getSearchRadius();
    }

    /**
     * Create a new stock category.
     * 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody StockCategory stockCategory) {
        stockCategory.setTenantId(tenantId);
        stockCategoryRepo.save(stockCategory);

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", stockCategory.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/{id}").buildAndExpand(vars).toUri());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing stockCategory.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockCategoryId,
            @RequestBody StockCategory updatedStockCategory) {
        StockCategory stockCategory = stockCategoryRepo
                .findOne(stockCategoryId);

        BeanUtils.copyProperties(updatedStockCategory, stockCategory, "id",
                "item");
        stockCategory.setTenantId(tenantId);
        stockCategoryRepo.save(stockCategory);
    }

    /**
     * Delete an existing stockCategory.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockCategoryId) {
        stockCategoryRepo.delete(stockCategoryId);
    }

    private List<ShortStockCategory> wrap(List<StockCategory> list) {
        List<ShortStockCategory> resources = new ArrayList<ShortStockCategory>(
                list.size());
        for (StockCategory stockCategory : list) {
            resources.add(wrap(stockCategory));
        }
        return resources;
    }

    private ShortStockCategory wrap(StockCategory stockCategory) {
        ShortStockCategory resource = new ShortStockCategory();

        BeanUtils.copyProperties(stockCategory, resource);

        // Not set by BeanUtils due to diff type
        resource.setDistance(String.valueOf(Math.round(stockCategory
                .getDistance())));
        Link detail = linkTo(StockCategoryRepository.class,
                stockCategory.getId()).withSelfRel();
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
    public static class ShortStockCategory extends ResourceSupport {
        private String selfRef;
        private String name;
        private String description;
        private String address1;
        private String address2;
        private String town;
        private String cityOrCounty;
        private String postCode;
        private String country;
        private String distance;
        private List<StockItem> stockItems;
        private List<MediaResource> images;
        private String types;
        private String mapUrl;
        private String videoCode;
        private Date created;
        private Date lastUpdated;
    }
}
