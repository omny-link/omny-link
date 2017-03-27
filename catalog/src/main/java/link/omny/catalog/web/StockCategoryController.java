package link.omny.catalog.web;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityNotFoundException;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

import link.omny.catalog.CatalogException;
import link.omny.catalog.json.JsonCustomStockCategoryFieldDeserializer;
import link.omny.catalog.model.CustomStockCategoryField;
import link.omny.catalog.model.GeoPoint;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.model.api.ShortStockCategory;
import link.omny.catalog.model.api.ShortStockItem;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for accessing stock items.
 * 
 * @author Tim Stephenson
 */
@RestController
@RequestMapping(value = "/{tenantId}/stock-categories")
public class StockCategoryController {

    private static final String PUBLISHED = "Published";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(StockCategoryController.class);

    @Value("${omny.catalog.searchRadius:100}")
    private String searchRadius;

    private int iSearchRadius;

    @Autowired
    private StockCategoryRepository stockCategoryRepo;

    @Autowired
    private GeoLocationService geo;

    public int getSearchRadius() {
        if (iSearchRadius == 0) {
            iSearchRadius = Integer.parseInt(searchRadius);
        }
        return iSearchRadius;
    }

    /**
     * Imports JSON representation of stock categories.
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
        LOGGER.info(String.format("Uploading CSV stockCategories for: %1$s",
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
     * Return just the stock categories for a specific tenant.
     * 
     * @return stockCategories for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<? extends ShortStockCategory> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List stockCategories for tenant %1$s",
                tenantId));

        List<StockCategory> list;
        if (limit == null) {
            list = stockCategoryRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = stockCategoryRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s stock categories", list.size()));

        return wrapShort(list);
    }

    /**
     * Return just the matching stock category.
     * 
     * @return stock category for that tenant with the matching id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional
    @JsonView(StockCategoryViews.Detailed.class)
    public @ResponseBody StockCategory findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find stock category for id %1$s", id));

        return stockCategoryRepo.findOne(Long.parseLong(id));
    }
    
    @RequestMapping(value = "/findByName", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    @JsonView(StockCategoryViews.Detailed.class)
    public @ResponseBody StockCategory findByName(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("name") String name,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "type", required = false) String type)
            throws IOException {
        // backwards compatibility
        if (type != null && tag == null) {
            tag = type;
        }
        LOGGER.info(String.format("findByName %1$s, tag %2$s for tenant %3$s",
                name, tag, tenantId));

        StockCategory category = stockCategoryRepo.findByName(name, tenantId);
        if (category == null) {
            throw new EntityNotFoundException(String.format(
                    "No Stock Category with name %1$s", name));
        }
        geocode(category);

        filter(category, expandTags(tag));

        return category;
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
    @Transactional(readOnly = true)
    public @ResponseBody List<ShortStockCategoryResource> findByLocation(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "offers", required = false) boolean offers,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit)
            throws IOException {
        // backwards compatibility
        if (type != null && tag == null) {
            tag = type;
        }
        LOGGER.info(String
                .format("List stockCategories for tenant: %1$s, q: %2$s, tag: %3$s, offers: %4$B",
                        tenantId, q, tag, offers));

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

        // TODO need to make this some kind of extension point
        List<String> tags = expandTags(tag);

        List<StockCategory> tmpList = findStockCategories(tenantId, offers);
        for (StockCategory stockCategory : tmpList) {
            // Capture tags now before filtering
            String allTagsAvail = stockCategory.getTags();
            try {
                geocode(stockCategory);

                if (matchQuery(q, qPoint, stockCategory)) {
                    filter(stockCategory, tags);
                    list.add(stockCategory);
                }
            } catch (CatalogException e) {
                LOGGER.error(String
                        .format("Unable to geo locate '%1$s', will return unfiltered list",
                                stockCategory.getPostCode()));
                filter(stockCategory, tags);
                list.add(stockCategory);
            } catch (Exception e) {
                LOGGER.error(String.format(
                        "Exception calculating distance of %1$s from %2$s",
                        stockCategory.getName(), q), e);
            }
            // set unfiltered tag list
            stockCategory.setTags(allTagsAvail);
        }
        Collections
                .sort(list,
                        (o1, o2) -> ((int) o1.getDistance())
                                - ((int) o2.getDistance()));

        LOGGER.info(String.format("Found %1$s stock categories", list.size()));
        return wrap(list);
    }

    protected void geocode(StockCategory stockCategory) {
        if (stockCategory.getLng() == null
                && stockCategory.getPostCode() != null) {
            try {
                stockCategory.setGeoPoint(geo.locate(stockCategory
                    .getPostCode()));
                stockCategoryRepo.save(stockCategory);
            } catch (IOException e) {

                String msg  = String
                        .format("Unable to geo locate '%1$s', details follow",
                                stockCategory.getPostCode());
                LOGGER.error(msg, e);
                throw new CatalogException(msg);
            }
        } else if (stockCategory.getLng() == null) {
            LOGGER.warn(String
                    .format("Skipping geo-coding because postcode of stock category %1$d is missing",
                            stockCategory.getId()));
        }
    }

    private List<StockCategory> findStockCategories(String tenantId,
            boolean offers) {
        if (offers) {
            return stockCategoryRepo.findByStatusAndOffersForTenant(tenantId,
                    PUBLISHED, PUBLISHED);
        } else {
            return stockCategoryRepo.findByStatusForTenant(tenantId,
                    PUBLISHED);
        }
    }

    private List<String> expandTags(String tag) {
        List<String> tags;
        if (tag == null) {
            return Collections.emptyList();
        } else if (tag.toLowerCase().contains("office")
                || tag.toLowerCase().contains("storage")
                || tag.toLowerCase().contains("workshop")) {
            tags = Arrays.asList("Business Unit", tag);
        } else {
            tags = Arrays.asList(tag);
        }
        return tags;
    }
    
    private void filter(final StockCategory stockCategory,
            final List<String> tags) {
        ArrayList<StockItem> filteredItems = new ArrayList<StockItem>();
        for (StockItem item : stockCategory.getStockItems()) {
            for (String tag : tags) {
                if (tag == null || (item.getTags().contains(tag)
                        && PUBLISHED.equalsIgnoreCase(item.getStatus()))) {
                    filteredItems.add(item);
                }
            }
            if ((tags == null || tags.size() == 0)
                    && PUBLISHED.equalsIgnoreCase(item.getStatus())) {
                filteredItems.add(item);
            }
        }
        stockCategory.setStockItems(filteredItems);
    }

    private boolean matchQuery(String q, GeoPoint qPoint,
            StockCategory stockCategory) {
        return q == null
                || q.trim().length() == 0
                || stockCategory.getGeoPoint() == null
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
        try {
            geocode(stockCategory);
        } catch (CatalogException e) {
            ; // already logged
        }

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

    private List<? extends ShortStockCategory> wrapShort(List<StockCategory> list) {
        List<ShortStockCategoryResource> resources = new ArrayList<ShortStockCategoryResource>(
                list.size());
        for (StockCategory stockCategory : list) {
            resources.add(wrapShort(stockCategory));
        }
        return resources;
    }

    private ShortStockCategoryResource wrapShort(StockCategory stockCategory) {
        ShortStockCategoryResource resource = new ShortStockCategoryResource();

        // NB exclude tags as otherwise stock items will be loaded too!
        BeanUtils.copyProperties(stockCategory, resource, "customFields", "stockItems", "tags");

        // Not set by BeanUtils due to diff type
        resource.setDistance(String.valueOf(Math.round(stockCategory
                .getDistance())));

        Link detail = linkTo(StockCategoryRepository.class,
                stockCategory.getId()).withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }
    
    private List<ShortStockCategoryResource> wrap(List<StockCategory> list) {
        List<ShortStockCategoryResource> resources = new ArrayList<ShortStockCategoryResource>(
                list.size());
        for (StockCategory stockCategory : list) {
            resources.add(wrap(stockCategory));
        }
        return resources;
    }

    private ShortStockCategoryResource wrap(StockCategory stockCategory) {
        ShortStockCategoryResource resource = new ShortStockCategoryResource();

        BeanUtils.copyProperties(stockCategory, resource);

        // Not set by BeanUtils due to diff type
        resource.setDistance(String.valueOf(Math.round(stockCategory
                .getDistance())));

        ArrayList<ShortStockItemResource> items = new ArrayList<ShortStockItemResource>();
        for (StockItem item : stockCategory.getStockItems()) {
            items.add(wrap(item));
        }
        resource.setStockItems(items);

        Link detail = linkTo(StockCategoryRepository.class,
                stockCategory.getId()).withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    private ShortStockItemResource wrap(StockItem item) {
        ShortStockItemResource resource = new ShortStockItemResource();

        BeanUtils.copyProperties(item, resource);
        resource.setStockItemId(item.getId());

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
    public static class ShortStockCategoryResource extends ResourceSupport implements ShortStockCategory {
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
        private List<? extends ShortStockItem> stockItems;
        @JsonDeserialize(using = JsonCustomStockCategoryFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomStockCategoryField> customFields;
        private List<MediaResource> images;
        private String tags;
        private String mapUrl;
        private Double lat;
        private Double lng;
        private String directionsByRoad;
        private String directionsByPublicTransport;
        private String directionsByAir;
        private String videoCode;
        private String productSheetUrl;
        private String status;
        private String offerStatus;
        private String offerTitle;
        private String offerDescription;
        private String offerCallToAction;
        private String offerUrl;
        private Date created;
        private Date lastUpdated;
        /**
         * Legacy support
         * @deprecated
         */
        @Override
        public String getCountyOrCity() {
            return cityOrCounty;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortStockItemResource extends ResourceSupport implements ShortStockItem {
        private Long stockItemId;
        private String selfRef;
        private String name;
        private String description;
        private String size;
        private String sizeString;
        private String unit;
        private String price;
        private String tags;
        private String status;
        private Date created;
        private Date lastUpdated;
        private String tenantId;
        private List<MediaResource> images;
    }
}
