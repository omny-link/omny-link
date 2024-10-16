/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import link.omny.catalog.internal.CatalogCsvImporter;
import link.omny.catalog.model.CustomStockItemField;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.repositories.MediaResourceRepository;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.repositories.StockItemRepository;
import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.internal.NullAwareBeanUtils;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;

/**
 * REST web service for accessing stock items.
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/stock-items")
@Tag(name = "Stock item API")
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
    private MediaResourceController mediaResourceSvc;

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
    @Operation(hidden = true)
    public @ResponseBody Iterable<StockItem> handleCsvFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info("Uploading CSV stockItems for: {}", tenantId);

        String content = new String(file.getBytes());
        List<StockItem> list = new CatalogCsvImporter().readStockItems(
                new StringReader(content),
                content.substring(0, content.indexOf('\n')).split(","),
                tenantId);
        LOGGER.info("  found {} stockItems", list.size());

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

        Iterable<StockItem> result = stockItemRepo.saveAll(tmp);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * @return stock items for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    @Operation(summary = "Retrieves the stock items for a specific tenant.")
    @JsonView(StockItemViews.Summary.class)
    public @ResponseBody List<EntityModel<StockItem>> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return addLinks(tenantId, listForTenant(tenantId, page, limit));
    }

    /**
     * @return stock items for that tenant.
     */
    protected List<StockItem> listForTenant(String tenantId, Integer page,
            Integer limit) {
        LOGGER.info("List stockItems for tenant {}", tenantId);

        List<StockItem> list;
        if (limit == null) {
            list = stockItemRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = stockItemRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info("Found {} stockItems", list.size());
        return list;
    }

    /**
     * @return stock items for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    @Operation(summary = "Retrieves the stock items for a specific tenant.")
    @JsonView(StockItemViews.Detailed.class)
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
    @Operation(summary = "Return stock items with the specified status for the specified tenant.")
    public @ResponseBody List<EntityModel<StockItem>> findByStatusForTenant(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("status") String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("List stockItems for tenant {}", tenantId);

        List<StockItem> list;
        if (limit == null) {
            list = stockItemRepo.findByStatusForTenant(status.toLowerCase(),
                    tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = stockItemRepo.findPageByStatusForTenant(
                    status.toLowerCase(), tenantId, pageable);
        }
        LOGGER.info("Found {} stockItems", list.size());

        return addLinks(tenantId, list);
    }

    protected StockItem findById(final String tenantId, final Long id) {
        return stockItemRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        StockItem.class, id));
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
    @Operation(summary = "Return the specified stock item.")
    public @ResponseBody EntityModel<StockItem> findEntityById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.info("Find stock item for id {}", id);

        StockItem item = findById(tenantId, Long.parseLong(id));
        LOGGER.info("Found item from category {} with {} custom fields",
                (item.getStockCategory() == null ? "n/a" : item.getStockCategory().getName()),
                item.getCustomFields().size());

        return addLinks(tenantId, item);
    }

    /**
     * Return just the stock items for a given stock category name.
     *
     * @return stockItems for that tenant and stock category.
     */
    @RequestMapping(value = "/findByStockCategoryName/{categoryName}", method = RequestMethod.GET)
    @Operation(summary = "Return stock items in the named category for the specified tenant.")
    public @ResponseBody List<EntityModel<StockItem>> findByStockCategoryName(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("categoryName") String categoryName) {

        if (categoryName == null) {
            throw new IllegalArgumentException(
                    String.format("You must specify the category name to search for"));
        }

        return addLinks(tenantId, stockItemRepo.findAllForCategoryName(
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
    @Operation(summary = "Create a new stock item.")
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
    @Operation(summary = "Update an existing stock item.")
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId,
            @RequestBody StockItem updatedStockItem) {
        StockItem stockItem = findById(tenantId, stockItemId);

        NullAwareBeanUtils.copyNonNullProperties(updatedStockItem, stockItem,
                "id", "tagsAsList", "stockCategory");
        // Seems stock cat is always null under Spring Boot 2
        if (stockItem.getStockCategory() != null
                && updatedStockItem.getStockCategory().getId() != null
                && !updatedStockItem.getStockCategory().getId().equals(stockItem.getStockCategory().getId())) {
            // During creation stock cat name may be set whilst id is not
            stockItem.setStockCategory(stockCategoryRepo.findByName(updatedStockItem.getStockCategory().getName(), tenantId));
        }

        stockItem.setTenantId(tenantId);
        stockItem = stockItemRepo.save(stockItem);
    }

    /**
     * Update a single custom field of the specified stockItem.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { "application/x-www-form-urlencoded" })
    @Operation(summary = "Update a custom field of the specified stock item.")
    public @ResponseBody void updateCustomField(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("fieldValue") String fieldVal) {
        StockItem stockItem = findById(tenantId, stockItemId);
        stockItem.addCustomField(new CustomStockItemField(fieldName, fieldVal));
        stockItem.setTenantId(tenantId);
        stockItemRepo.save(stockItem);
    }

    /**
     * Update a media resource to the specified item.
     */
    @RequestMapping(value = "/{stockItemId}/images/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    @Operation(summary = "Update an image linked to the specified stock item.")
    public @ResponseBody void updateImage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @PathVariable("id") Long resourceId,
            @RequestBody MediaResource updatedResource) {
        MediaResource resource = mediaResourceRepo
                .findById(resourceId)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        StockItem.class, resourceId));
        BeanUtils.copyProperties(updatedResource, resource, "id", "stockItem");
        mediaResourceRepo.save(resource);
    }

    /**
     * @return List of media resource for the specified stock item.
     */
    @JsonView(MediaResourceViews.Summary.class)
    @RequestMapping(value = "/{stockItemId}/images", method = RequestMethod.GET)
    @Operation(summary = "Retrieves images for the specified stock item.")
    public @ResponseBody List<MediaResource> listImages(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId) {
         List<MediaResource> resources = mediaResourceRepo.findByStockItemId(stockItemId);
         for (MediaResource resource : resources) {
             mediaResourceSvc.addLinks(tenantId, resource);
         }
         return resources;
    }

    /**
     * Add a media resource to the specified stockItem.
     */
    @RequestMapping(value = "/{stockItemId}/images", method = RequestMethod.POST)
    @Operation(summary = "Add an image to the specified stock item.")
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
    public void addMediaResource(String tenantId, Long stockItemId,
            MediaResource mediaResource) {
        StockItem stockItem = findById(tenantId, stockItemId);
        mediaResource.setStockItem(stockItem);
        mediaResourceRepo.save(mediaResource);
        // necessary to force a save
        stockItem.setLastUpdated(new Date());
        stockItem= stockItemRepo.save(stockItem);
    }

    /**
     * Add a document to the specified stockItem.
     */
    @RequestMapping(value = "/{stockItemId}/documents", method = RequestMethod.POST)
    @Transactional
    @Operation(summary = "Add a document to the specified stock item.")
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId, @RequestBody Document doc) {
         StockItem stockItem = findById(tenantId, stockItemId);
         stockItem.getDocuments().add(doc);
         stockItem.setLastUpdated(new Date());
         stockItem = stockItemRepo.save(stockItem);
         doc = stockItem.getDocuments().stream()
                 .reduce((first, second) -> second).orElse(null);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, stockItem.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified stockItem.
     * @return the created note.
     */
    @RequestMapping(value = "/{stockItemId}/notes", method = RequestMethod.POST)
    @Transactional
    @Operation(summary = "Add a document to the specified stock category.")
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId, @RequestBody Note note) {
        StockItem stockItem = findById(tenantId, stockItemId);
        stockItem.getNotes().add(note);
        stockItem.setLastUpdated(new Date());
        stockItem = stockItemRepo.save(stockItem);
        note = stockItem.getNotes().stream()
                .reduce((first, second) -> second).orElse(null);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, stockItem.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{itemId}/stockCategory",
            method = RequestMethod.PUT, consumes = { "text/uri-list" })
    @Transactional
    @Operation(summary = "Sets the category for the specified stock item.")
    public @ResponseBody void setStockCategory(
            @PathVariable("tenantId") String tenantId,
            @RequestBody String categoryUri,
            @PathVariable("itemId") Long itemId) {
        LOGGER.info("Linking item {} to category {}", itemId, categoryUri);

        try {
            Long categoryId = Long.parseLong(
                    categoryUri.substring(categoryUri.lastIndexOf('/') + 1));
            stockItemRepo.setStockCategory(itemId, categoryId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("%1$s is not a valid stock category", categoryUri));
        }
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{categoryId}/stockCategory",
            method = RequestMethod.PUT, consumes = { "application/json" })
    @Transactional
    @Operation(hidden = true)
    @Deprecated
    public @ResponseBody void setStockCategoryLegacy(
            @PathVariable("tenantId") String tenantId,
            @RequestBody String itemUri,
            @PathVariable("categoryId") Long categoryId) {
        setStockCategory(tenantId, itemUri, categoryId);
    }

    /**
     * Delete an existing stockItem.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete the specified stock item.")
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long stockItemId) {
        stockItemRepo.deleteById(stockItemId);
    }

    /**
     * Delete a stock item's image.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{stockItemId}/images/{id}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete an image of the specified stock item.")
    public @ResponseBody void deleteImage(@PathVariable("tenantId") String tenantId,
            @PathVariable("stockItemId") Long stockItemId,
            @PathVariable("id") Long imageId) {
        mediaResourceRepo.deleteById(imageId);
    }

    protected List<EntityModel<StockItem>> addLinks(final String tenantId, final List<StockItem> list) {
        ArrayList<EntityModel<StockItem>> entities = new ArrayList<EntityModel<StockItem>>();
        for (StockItem stockItem : list) {
            entities.add(addLinks(tenantId, stockItem));
        }
        return entities;
    }

    protected EntityModel<StockItem> addLinks(final String tenantId, final StockItem stockItem) {
        EntityModel<StockItem> model = EntityModel.of(stockItem,
                linkTo(methodOn(StockItemController.class).findEntityById(tenantId, stockItem.getId().toString()))
                        .withSelfRel());
        if (stockItem.getStockCategory() != null) {
            model.add(linkTo(methodOn(StockCategoryController.class).findEntityById(
                    tenantId, String.valueOf(stockItem.getStockCategory().getId())))
                            .withRel("stock-category"));
        }
        return model;
    }
}
