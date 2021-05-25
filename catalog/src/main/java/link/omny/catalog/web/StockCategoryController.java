/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.catalog.web;

import java.io.IOException;
import java.net.URI;
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

import link.omny.catalog.json.JsonCustomStockCategoryFieldDeserializer;
import link.omny.catalog.model.CustomStockCategoryField;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.model.api.ShortStockCategory;
import link.omny.catalog.model.api.ShortStockItem;
import link.omny.catalog.repositories.MediaResourceRepository;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
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
    private MediaResourceRepository mediaResourceRepo;

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
    public @ResponseBody List<? extends ShortStockCategory> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return wrapShort(listForTenant(tenantId, page, limit));
    }

    protected List<StockCategory> listForTenant(String tenantId,
            Integer page, Integer limit) {
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

        return list;
    }

    /**
     * @return stock categories for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    public @ResponseBody ResponseEntity<String> listForTenantAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        StringBuilder sb = new StringBuilder()
                .append("id,name,description,address1,address2,town,"
                        + "countyOrCity,postCode,country,"
                        + "lat,lng,tags,mapUrl,directionsByRoad,"
                        + "directionsByPublicTransport,directionsByAir,"
                        + "videoCode,status,productSheetUrl,offerStatus,"
                        + "offerTitle,offerDescription,offerCallToAction,"
                        + "offerUrl,tenantId,created,lastUpdated,");
        List<String> customFieldNames = stockCategoryRepo.findCustomFieldNames(tenantId);
        LOGGER.info("Found {} custom field names while exporting orders for {}: {}",
                customFieldNames.size(), tenantId, customFieldNames);
        for (String fieldName : customFieldNames) {
            sb.append(fieldName).append(",");
        }
        sb.append("\r\n");

        for (StockCategory order : listForTenant(tenantId, page, limit)) {
            order.setCustomHeadings(customFieldNames);
            sb.append(order.toCsv()).append("\r\n");
        }
        LOGGER.info("Exporting CSV stock categories for {} generated {} bytes",
                tenantId, sb.length());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(sb.length());
        return new ResponseEntity<String>(
                sb.toString(), httpHeaders, HttpStatus.OK);
    }

    /**
     * @return List of media resource for the specified stock category.
     */
    @JsonView(MediaResourceViews.Summary.class)
    @RequestMapping(value = "/{stockCategoryId}/images", method = RequestMethod.GET)
    public @ResponseBody List<MediaResource> listImages(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId) {
         List<MediaResource> resources = mediaResourceRepo.findByStockCategoryId(stockCategoryId);
         for (MediaResource resource : resources) {
             addLinks(tenantId, stockCategoryId, resource);
         }
         return resources;
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

        StockCategory category = stockCategoryRepo.findOne(Long.parseLong(id));
        addLinks(tenantId, category);
        return category;
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

        filter(category, expandTags(tag));

        return category;
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

        for (CustomStockCategoryField field : stockCategory.getCustomFields()) {
            field.setStockCategory(stockCategory);
        }
        for (StockItem item : stockCategory.getStockItems()) {
            item.setStockCategory(stockCategory);
        }

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
     * Add a document to the specified stockCategory.
     */
    @RequestMapping(value = "/{stockCategoryId}/documents", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId, @RequestBody Document doc) {
         StockCategory stockCategory = stockCategoryRepo.findOne(stockCategoryId);
         stockCategory.getDocuments().add(doc);
         stockCategory.setLastUpdated(new Date());
         stockCategoryRepo.save(stockCategory);
         doc = stockCategory.getDocuments().get(stockCategory.getDocuments().size()-1);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, stockCategory.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a document to the specified stockCategory.
     *      *
     * <p>This is just a convenience method, see {@link #addDocument(String, Long, Document)}
     * @return
     *
     * @return The document created.
     */
    @RequestMapping(value = "/{stockCategoryId}/documents", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Document addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId,
            @RequestParam("author") String author,
            @RequestParam("name") String name,
            @RequestParam("url") String url) {

        return addDocument(tenantId, stockCategoryId, new Document(author, name, url)).getBody();
    }

    /**
     * Add a note to the specified stockCategory.
     * @return the created note.
     */
    @RequestMapping(value = "/{stockCategoryId}/notes", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId, @RequestBody Note note) {
        StockCategory stockCategory = stockCategoryRepo.findOne(stockCategoryId);
        stockCategory.getNotes().add(note);
        stockCategory.setLastUpdated(new Date());
        stockCategoryRepo.save(stockCategory);
        note = stockCategory.getNotes().get(stockCategory.getNotes().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, stockCategory.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified stockCategory from its parts.
     *
     * <p>This is just a convenience method, see {@link #addNote(String, Long, Note)}
     *
     * @return The note created.
     */
    @RequestMapping(value = "/{stockCategoryId}/notes", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Note addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        return addNote(tenantId, stockCategoryId, new Note(author, content, favorite)).getBody();
    }

    /**
     * Add a media resource to the specified category.
     */
    @RequestMapping(value = "/{stockCategoryId}/images", method = RequestMethod.POST)
    public @ResponseBody void addImage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId,
            @RequestParam("author") String author,
            @RequestParam("url") String url) {
        addMediaResource(tenantId, stockCategoryId, new MediaResource(author, url));
    }

    /**
     * Add a media resource to the specified stock category.
     */
    public @ResponseBody void addMediaResource(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId,
            @RequestBody MediaResource mediaResource) {
        StockCategory stockCategory = stockCategoryRepo.findOne(stockCategoryId);
        mediaResource.setStockCategory(stockCategory);
        mediaResourceRepo.save(mediaResource);
        // necessary to force a save
        stockCategory.setLastUpdated(new Date());
        stockCategoryRepo.save(stockCategory);
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
     * Update a media resource to the specified category.
     */
    @RequestMapping(value = "/{stockCategoryId}/images/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void updateImage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId,
            @PathVariable("id") Long resourceId,
            @RequestBody MediaResource updatedResource) {
        MediaResource resource = mediaResourceRepo
                .findOne(resourceId);
        BeanUtils.copyProperties(updatedResource, resource, "id", "stockCategory");
        mediaResourceRepo.save(resource);
    }


    /**
     * Delete an existing stockCategory.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockCategoryId) {
        stockCategoryRepo.delete(stockCategoryId);
    }

    /**
     * Delete a stock category's image.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{stockCategoryId}/images/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void deleteImage(@PathVariable("tenantId") String tenantId,
            @PathVariable("stockCategoryId") Long stockCategoryId,
            @PathVariable("id") Long imageId) {
        mediaResourceRepo.delete(imageId);
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

    private void addLinks(String tenantId, StockCategory category) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/stock-categories/%2$s",
                tenantId, category.getId())));
        category.setLinks(links);
    }

    private void addLinks(String tenantId, Long stockCategoryId, MediaResource resource) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/stock-categories/%2$s/images/%3$s",
                tenantId, stockCategoryId, resource.getId())));
        resource.setLinks(links);
    }
}
