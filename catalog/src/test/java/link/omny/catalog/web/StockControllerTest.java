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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.catalog.CatalogTestApplication;
import link.omny.catalog.model.CustomStockItemField;
import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.model.api.ShortStockCategory;
import link.omny.catalog.model.api.ShortStockItem;

/**
 * @author Tim Stephenson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CatalogTestApplication.class)
@WebAppConfiguration
public class StockControllerTest {

    private static final String CATEGORY_BOREHAMWOOD = "Borehamwood";

    private static final String TENANT_ID = "omny";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockCategoryController categoryController;

    @Autowired
    private StockItemController itemController;

    private Long categoryId;

    private String itemUri;

    private DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @AfterEach
    public void tearDown() {
        categoryController.delete(TENANT_ID, categoryId);
        // check clean
        List<StockCategory> list = (List<StockCategory>) categoryController.listForTenant(
                TENANT_ID, null, null);
        assertEquals(0, list.size());
    }

    @Test
    public void testLifecycle() throws IOException {
        StockCategory borehamwoodCat = createCategory();

        StockItem officeItem = createOfficeItem(borehamwoodCat);
        addItemToCategory(borehamwoodCat, getRef(officeItem));

        StockItem warehouseItem = createWarehouseItem(borehamwoodCat);
        addItemToCategory(borehamwoodCat, getRef(warehouseItem));

        StockItem businessUnitItem = createBusinessUnitItem(borehamwoodCat);
        addItemToCategory(borehamwoodCat, getRef(businessUnitItem));

        StockItem unpublishedItem = createUnpublishedItem(borehamwoodCat);
        addItemToCategory(borehamwoodCat, getRef(unpublishedItem));

        StockCategory category2 = categoryController.findById(TENANT_ID, categoryId);
        assertNotNull(category2.getCreated());

        ShortStockCategory category3 = findByName(borehamwoodCat, officeItem,
                warehouseItem);
        assertTrue(category3 instanceof StockCategory);
        update((StockCategory) category3);
    }

    private String getRef(StockItem item) {
        return methodOn(StockItemController.class)
                .findEntityById(TENANT_ID, item.getId().toString())
                .getLink("self").get().getHref();
    }

    protected void update(StockCategory category) {
        Long itemId = category.getStockItems().iterator().next().getId();
        StockItem stockItem = itemController.findEntityById(
                category.getTenantId(), itemId.toString()).getContent();
        assertNotNull(stockItem.getId());

        GregorianCalendar cal = new GregorianCalendar();
        // StockItem stockItem = new StockItem();
        stockItem.setId(itemId);
        stockItem.addCustomField(new CustomStockItemField("tenancyEndDate",
                isoFormatter.format(cal.getTime())));

        itemController.update(TENANT_ID, stockItem.getId(), stockItem);

        StockItem stockItem2 = itemController.findEntityById(
                category.getTenantId(), itemId.toString()).getContent();
        assertEquals(stockItem.getCustomFields().size(), stockItem2
                .getCustomFields().size());
    }

    protected void findAll(StockCategory category, StockItem officeItem,
            StockItem warehouseItem) throws IOException {
        List<StockCategory> categoryResults = categoryController
                .listForTenant(TENANT_ID, null, null);
        assertEquals(1, categoryResults.size());

        checkCategory(category, categoryResults.iterator().next());

        // check item
        assertNotNull(categoryResults.iterator().next().getStockItems());
        assertEquals(3, categoryResults.iterator().next().getStockItems().size());

        checkOfficeItem(category, officeItem, categoryResults.iterator().next());
    }

 // TODO fix removal of geocoder
    protected void findOffice(StockCategory category, StockItem officeItem)
            throws IOException {
        List<StockCategory> categoryResults = categoryController
                .listForTenant(TENANT_ID, null, null);
        // TODO need to filter for just type = Office
        assertEquals(2, categoryResults.iterator().next().getStockItems().size());

        checkCategory(category, categoryResults.iterator().next());
        checkOfficeItem(category, officeItem, categoryResults.iterator().next());
    }

    protected ShortStockCategory findByName(StockCategory category,
            StockItem officeItem, StockItem warehouseItem) throws IOException {
        StockCategory categoryFound = categoryController.findByName(
                TENANT_ID, CATEGORY_BOREHAMWOOD, null, null);

        checkCategory(category, categoryFound);

        // check item
        assertNotNull(categoryFound.getStockItems());
        assertEquals(3, categoryFound.getStockItems().size());

        checkOfficeItem(category, officeItem, categoryFound);

        // Find just Office tags
        categoryFound = categoryController.findByName(TENANT_ID,
                CATEGORY_BOREHAMWOOD, "Office", null);
        assertEquals(2, categoryFound.getStockItems().size());

        return categoryFound;
    }

    private void checkCategory(StockCategory category,
            ShortStockCategory categoryFound) {
        assertEquals(category.getName(), categoryFound.getName());
        assertEquals("Published", categoryFound.getStatus());
        assertEquals("Business unit,Office,Warehouse", categoryFound.getTags());
        assertNotNull(category.getImages());
        assertEquals(StockCategory.DEFAULT_IMAGE_COUNT, category.getImages()
                .size());
    }

    protected void checkOfficeItem(StockCategory category, StockItem officeItem,
            ShortStockCategory categoryFound) {
        assertEquals(officeItem.getName(), categoryFound.getStockItems().iterator().next()
                .getName());
        assertEquals("Published", officeItem.getStatus());

        assertNotNull(category.getImages());
        ShortStockItem foundItem = categoryFound.getStockItems().iterator().next();
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, ((StockItem) foundItem).getImages().size());
        Long itemId = ((StockItem) foundItem).getId();
        // check custom fields (needs extra fetch as not all included in search)
        StockItem stockItem = itemController.findEntityById(category.getTenantId(), itemId.toString()).getContent();
        assertNotNull(stockItem.getId());
        assertEquals(officeItem.getCustomFields().size(), stockItem
                .getCustomFields().size());
    }

    protected void addItemToCategory(StockCategory category, String itemUri) {
        itemController.setStockCategory(TENANT_ID, itemUri, categoryId);
    }

    protected StockCategory createCategory() throws IOException {
        // Read JSON into category object
        StockCategory category = getCategoryBorehamwood();

        // Store via REST API
        ResponseEntity<?> categoryResp = categoryController.create(TENANT_ID,
                category);

        // assert stored fields
        assertEquals(HttpStatus.CREATED, categoryResp.getStatusCode());

        // check REST headers
        List<String> locationHdrs = categoryResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.iterator().next());
        categoryId = Long.parseLong(locationHdrs.iterator().next().substring(
                locationHdrs.iterator().next().lastIndexOf('/') + 1));

        // assert derived fields
        assertEquals("", category.getVideoCode());
        assertNotNull(category.getImages());
        assertEquals(StockCategory.DEFAULT_IMAGE_COUNT, category.getImages()
                .size());

        return category;
    }

    protected StockItem createOfficeItem(StockCategory category)
            throws IOException {
        StockItem item = getOfficeItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        assertEquals("trademark", item.getName());
        assertEquals("Office", item.getTags());
        assertEquals(2, item.getCustomFields().size());
        assertEquals("true", item.getCustomFieldValue("vacant"));

        // assert derived fields
        item.setStockCategory(category);
        assertNotNull(item.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, item.getImages().size());

        return item;
    }

    protected StockItem createWarehouseItem(StockCategory category)
            throws IOException {
        StockItem item = getWarehouseItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        // assert derived fields
        item.setStockCategory(category);
        assertNotNull(item.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, item.getImages().size());

        return item;
    }

    protected StockItem createBusinessUnitItem(StockCategory category)
            throws IOException {
        StockItem item = getBusinessUnitItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        // assert derived fields
        item.setStockCategory(category);
        assertNotNull(item.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, item.getImages().size());

        return item;
    }

    protected StockItem createUnpublishedItem(StockCategory category)
            throws IOException {
        StockItem item = getUnpublishedItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        // assert derived fields
        item.setStockCategory(category);
        assertNotNull(item.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, item.getImages().size());

        return item;
    }

    private String getLocationUri(ResponseEntity<?> itemResp) {
        assertEquals(HttpStatus.CREATED, itemResp.getStatusCode());
        List<String> locationHdrs = itemResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        itemUri = locationHdrs.iterator().next();
        assertNotNull(itemUri);
        return itemUri;
    }

    protected StockCategory getCategoryBorehamwood() throws IOException {
        String categoryJson = "{\"name\":\"Borehamwood\","
                + "\"description\":\"A very fine property\","
                + "\"postCode\":\"WD6 1RN\"," 
                + "\"status\":\"Published\","
                + "\"tags\":\"Office\","
                + "\"offerStatus\":\"Published\","
                + "\"offerTitle\":\"Summer Special\","
                + "\"offerCallToAction\":\"Apply Now\","
                + "\"offerDescription\":\"It's great!\","
                + "\"offerUrl\":\"http://omny.link/offers\"}";

        StockCategory category = objectMapper.readValue(categoryJson,
                new TypeReference<StockCategory>() {
                });
        assertNotNull(category);
        assertEquals(CATEGORY_BOREHAMWOOD, category.getName());
        assertEquals("WD6 1RN", category.getPostCode());

        return category;
    }

    protected StockItem getOfficeItem() throws IOException {
        String itemJson = "{\"name\":\"trademark\",\"tags\":\"Office\","
                + "\"tenantId\":\"" + TENANT_ID + "\","
                + "\"firstContact\":null,\"lastUpdated\":null,"
                + "\"status\": \"Published\","
                + "\"customFields\":{ \"vacant\": true, \"publishWebsite\": true }}";

        StockItem item = objectMapper.readValue(itemJson,
                new TypeReference<StockItem>() {
                });
        assertNotNull(item);
        assertEquals("trademark", item.getName());
        assertEquals("Office", item.getTags());
        assertEquals("Published", item.getStatus());
        assertEquals(2, item.getCustomFields().size());
        assertEquals("true", item.getCustomFieldValue("vacant"));

        return item;
    }

    protected StockItem getWarehouseItem() throws IOException {
        StockItem item = new StockItem("Warehouse name", "Warehouse");
        item.setTenantId(TENANT_ID);
        item.setStatus("Published");
        item.addCustomField(new CustomStockItemField("publishWebsite",
                Boolean.TRUE.toString()));

        assertEquals("Warehouse name", item.getName());
        assertEquals("Warehouse", item.getTags());
        return item;
    }

    protected StockItem getBusinessUnitItem() throws IOException {
        StockItem item = new StockItem("Business unit name", "Business unit");
        item.setTenantId(TENANT_ID);
        item.setStatus("Published");
        item.addTag("Office");
        item.addCustomField(new CustomStockItemField("publishWebsite",
                Boolean.TRUE.toString()));

        assertEquals("Business unit name", item.getName());
        assertEquals(2, item.getTagsAsList().size());
        assertTrue(item.getTagsAsList().contains("Business unit"));
        assertTrue(item.getTagsAsList().contains("Office"));
        return item;
    }

    protected StockItem getUnpublishedItem() throws IOException {
        StockItem item = new StockItem("Unpublished unit name", "Gym");
        item.setTenantId(TENANT_ID);
        item.setStatus("Draft");

        assertEquals("Unpublished unit name", item.getName());
        assertEquals("Gym", item.getTags());
        return item;
    }
}
