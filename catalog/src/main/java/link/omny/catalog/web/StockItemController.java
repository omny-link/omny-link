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
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

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

import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.internal.CatalogCsvImporter;
import link.omny.catalog.model.CustomStockItemField;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.repositories.MediaResourceRepository;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.repositories.StockItemRepository;
import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.custmgmt.internal.NullAwareBeanUtils;
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
     * @return stock items for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockItem> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return wrapShort(listForTenant(tenantId, page, limit));
    }

    /**
     * @return stock items for that tenant.
     */
    public @ResponseBody List<StockItem> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List stockItems for tenant %1$s", tenantId));

        List<StockItem> list;
        if (limit == null) {
            list = stockItemRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = stockItemRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s stockItems", list.size()));

        return list;
    }

    /**
     * @return stock items for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    public @ResponseBody ResponseEntity<String> listForTenantAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        StringBuilder sb = new StringBuilder()
                .append("id,stockCategoryId,name,description,size,sizeString,unit,price,"
                        + "tags,videoCode,status,offerStatus,"
                        + "offerTitle,offerDescription,offerCallToAction,"
                        + "offerUrl,tenantId,created,lastUpdated,");
        List<String> customFieldNames = stockItemRepo.findCustomFieldNames(tenantId);
        LOGGER.info("Found {} custom field names while exporting orders for {}: {}",
                customFieldNames.size(), tenantId, customFieldNames);
        for (String fieldName : customFieldNames) {
            sb.append(fieldName).append(",");
        }
        sb.append("\r\n");

        for (StockItem item : listForTenant(tenantId, page, limit)) {
            item.setCustomHeadings(customFieldNames);
            sb.append(item.toCsv()).append("\r\n");
        }
        LOGGER.info("Exporting CSV stock items for {} generated {} bytes",
                tenantId, sb.length());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(sb.length());
        return new ResponseEntity<String>(
                sb.toString(), httpHeaders, HttpStatus.OK);
    }

    /**
     * @return stockItems for the specified tenant and status.
     */
    @RequestMapping(value = "/findByStatus/{status}", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockItem> findByStatusForTenant(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("status") String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List stockItems for tenant %1$s", tenantId));

        List<StockItem> list;
        if (limit == null) {
            list = stockItemRepo.findByStatusForTenant(status.toLowerCase(),
                    tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = stockItemRepo.findPageByStatusForTenant(
                    status.toLowerCase(), tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s stockItems", list.size()));

        return wrapShort(list);
    }

    /**
     * Return just the matching stock item.
     *
     * @return stock item for that tenant with the matching id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional
    @JsonView(StockItemViews.Detailed.class)
    public @ResponseBody StockItem findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find stock item for id %1$s", id));

        StockItem item = stockItemRepo.findOne(Long.parseLong(id));
        if (item == null) {
            throw new BusinessEntityNotFoundException("Stock item", id);
        }
        // Ensure everything loaded, while still in transaction
        item.getStockCategory();
        LOGGER.info("Found item from category {} with {} custom fields",
                (item.getStockCategory() == null ? "n/a" : item.getStockCategory().getName()),
                item.getCustomFields().size());

        addLinks(tenantId, item);
        return item;
    }

    /**
     * Return just the stock items for a given stock category name.
     *
     * @return stockItems for that tenant and stock category.
     */
    @RequestMapping(value = "/findByStockCategoryName/{categoryName}", method = RequestMethod.GET)
    public @ResponseBody List<ShortStockItem> findByStockCategoryName(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("categoryName") String categoryName) {

        if (categoryName == null) {
            throw new IllegalArgumentException(
                    String.format("You must specify the category name to search for"));
        }

        return wrapShort(stockItemRepo.findAllForCategoryName(
                categoryName.toLowerCase(), tenantId));
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
        if (stockItem.getStockCategory() != null) {
            stockItem.setStockCategory(stockCategoryRepo.findByName(
                    stockItem.getStockCategory().getName(), tenantId));
        }
        for (CustomStockItemField field : stockItem.getCustomFields()) {
            field.setStockItem(stockItem);
        }
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
    @Transactional
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId,
            @RequestBody StockItem updatedStockItem) {
        StockItem stockItem = stockItemRepo.findOne(stockItemId);

        NullAwareBeanUtils.copyNonNullProperties(updatedStockItem, stockItem,
                "id", "tagsAsList", "stockCategory");
        // This is not a mechanism to update the category but only to link
        // item to a different category if necessary
        if (updatedStockItem.getStockCategory() == null) {
            stockItem.setStockCategory(null);
        } else if (updatedStockItem.getStockCategory().getId() != null
                && !updatedStockItem.getStockCategory().getId().equals(stockItem.getStockCategory().getId())) {
            // During creation stock cat name may be set whilst id is not
            stockItem.setStockCategory(stockCategoryRepo.findByName(updatedStockItem.getStockCategory().getName(), tenantId));
        }

        stockItem.setTenantId(tenantId);
        stockItemRepo.save(stockItem);
    }

    /**
     * Update a single custom field of the specified stockItem.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { "application/x-www-form-urlencoded" })
    public @ResponseBody void updateCustomField(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("fieldValue") String fieldVal) {
        StockItem stockItem = stockItemRepo.findOne(stockItemId);
        stockItem.addCustomField(new CustomStockItemField(fieldName, fieldVal));
        stockItem.setTenantId(tenantId);
        stockItemRepo.save(stockItem);
    }

    /**
     * Update a media resource to the specified item.
     */
    @RequestMapping(value = "/{stockItemId}/images/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void updateImage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @PathVariable("id") Long resourceId,
            @RequestBody MediaResource updatedResource) {
        MediaResource resource = mediaResourceRepo
                .findOne(resourceId);
        BeanUtils.copyProperties(updatedResource, resource, "id", "stockItem");
        mediaResourceRepo.save(resource);
    }

    /**
     * @return List of media resource for the specified stock item.
     */
    @JsonView(MediaResourceViews.Summary.class)
    @RequestMapping(value = "/{stockItemId}/images", method = RequestMethod.GET)
    public @ResponseBody List<MediaResource> listImages(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId) {
         List<MediaResource> resources = mediaResourceRepo.findByStockItemId(stockItemId);
         for (MediaResource resource : resources) {
             addLinks(tenantId, stockItemId, resource);
         }
         return resources;
    }

    /**
     * Add a media resource to the specified stockItem.
     */
    @RequestMapping(value = "/{stockItemId}/images", method = RequestMethod.POST)
    public @ResponseBody void addImage(
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

    /**
     * Add a document to the specified stockItem.
     */
    @RequestMapping(value = "/{stockItemId}/documents", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId, @RequestBody Document doc) {
         StockItem stockItem = stockItemRepo.findOne(stockItemId);
         stockItem.getDocuments().add(doc);
         stockItem.setLastUpdated(new Date());
         stockItemRepo.save(stockItem);
         doc = stockItem.getDocuments().get(stockItem.getDocuments().size()-1);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, stockItem.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a document to the specified stockItem.
     *      *
     * <p>This is just a convenience method, see {@link #addDocument(String, Long, Document)}
     * @return
     *
     * @return The document created.
     */
    @RequestMapping(value = "/{stockItemId}/documents", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Document addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @RequestParam("author") String author,
            @RequestParam("name") String name,
            @RequestParam("url") String url) {

        return addDocument(tenantId, stockItemId, new Document(author, name, url)).getBody();
    }

    /**
     * Add a note to the specified stockItem.
     * @return the created note.
     */
    @RequestMapping(value = "/{stockItemId}/notes", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId, @RequestBody Note note) {
        StockItem stockItem = stockItemRepo.findOne(stockItemId);
        stockItem.getNotes().add(note);
        stockItem.setLastUpdated(new Date());
        stockItemRepo.save(stockItem);
        note = stockItem.getNotes().get(stockItem.getNotes().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, stockItem.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified stockItem from its parts.
     *
     * <p>This is just a convenience method, see {@link #addNote(String, Long, Note)}
     *
     * @return The note created.
     */
    @RequestMapping(value = "/{stockItemId}/notes", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Note addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        return addNote(tenantId, stockItemId, new Note(author, content, favorite)).getBody();
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
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId) {
        stockItemRepo.delete(stockItemId);
    }

    /**
     * Delete a stock item's image.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{stockItemId}/images/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void deleteImage(@PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @PathVariable("id") Long imageId) {
        mediaResourceRepo.delete(imageId);
    }

    private List<ShortStockItem> wrapShort(List<StockItem> list) {
        List<ShortStockItem> resources = new ArrayList<ShortStockItem>(
                list.size());
        for (StockItem stockItem : list) {
            resources.add(wrapShort(stockItem));
        }
        return resources;
    }

    private ShortStockItem wrapShort(StockItem stockItem) {
        ShortStockItem resource = new ShortStockItem();
        BeanUtils.copyProperties(stockItem, resource, "stockCategory");

        if (stockItem.getStockCategory() != null) {
            resource.setStockCategoryName(stockItem.getStockCategory()
                    .getName());
        }

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
        private String description;
        private String size;
        private String sizeString;
        private String unit;
        private BigDecimal price;
        private String tags;
        private String tenantId;
        private String stockCategoryName;
        private String mapUrl;
        private String directionsByRoad;
        private String directionsByPublicTransport;
        private String directionsByAir;
        private String status;
        private Date created;
        private Date lastUpdated;
        private List<MediaResource> images;
    }

    private void addLinks(String tenantId, StockItem item) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/stock-items/%2$s",
                tenantId, item.getId())));
        item.setLinks(links);
    }

    private void addLinks(String tenantId, Long stockItemId, MediaResource resource) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/stock-items/%2$s/images/%3$s",
                tenantId, stockItemId, resource.getId())));
        resource.setLinks(links);
    }
}
