package link.omny.catalog.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import link.omny.catalog.TestApplication;
import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.repositories.StockItemRepository;
import link.omny.catalog.web.StockCategoryController.ShortStockCategory;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tim Stephenson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebAppConfiguration
public class StockCategoryControllerTest {

    private static final String CATEGORY_BOREHAMWOOD = "Borehamwood";

    private static final String TENANT_ID = "test";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockCategoryRepository categoryRepo;

    @Autowired
    private StockItemRepository accountRepo;

    @Autowired
    private StockCategoryController categoryController;

    @Autowired
    private StockItemController itemController;

    private Long categoryId;

    private String itemUri;

    @After
    public void tearDown() {
        categoryController.delete(TENANT_ID, categoryId);
        // check clean
        List<ShortStockCategory> list = categoryController.listForTenant(
                TENANT_ID, null, null);
        assertEquals(0, list.size());
    }

    @Test
    public void testLifecycle() throws IOException {
        StockCategory category = createCategory();

        StockItem officeItem = createOfficeItem();
        addItemToCategory(category, officeItem.getSelfRef());

        StockItem warehouseItem = createWarehouseItem();
        addItemToCategory(category, warehouseItem.getSelfRef());

        findByLocation(category, officeItem, warehouseItem);

        StockCategory category2 = categoryRepo.findOne(categoryId);
        assertNotNull(category2.getCreated());
        // TODO h2 test db not supporting default?
        // assertNotNull(category2.getLastUpdated());

        findByName(category, officeItem, warehouseItem);
    }

    protected void findByLocation(StockCategory category, StockItem officeItem,
            StockItem warehouseItem)
            throws IOException {
        // Find all types
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, null, null, null, null);
        assertEquals(1, categoryResults.size());

        // check category
        assertEquals(category.getName(), categoryResults.get(0).getName());
        assertNotNull(category.getImages());
        assertEquals(StockCategory.DEFAULT_IMAGE_COUNT, category.getImages()
                .size());

        // check item
        assertNotNull(categoryResults.get(0).getStockItems());
        assertEquals(2, categoryResults.get(0).getStockItems().size());
        assertEquals(officeItem.getName(), categoryResults.get(0)
                .getStockItems()
                .get(0).getName());
        // item desc if null should default to category
        assertEquals(category.getDescription(), categoryResults.get(0)
                .getStockItems().get(0).getDescription());
        assertNotNull(category.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, categoryResults.get(0)
                .getStockItems().get(0).getImages().size());

        // Find just Office types
        categoryResults = categoryController.findByLocation(TENANT_ID, null,
                "Office", null, null);
        assertEquals(1, categoryResults.get(0).getStockItems().size());
    }

    protected void findByName(StockCategory category, StockItem officeItem,
            StockItem warehouseItem) throws IOException {
        ShortStockCategory categoryFound = categoryController.findByName(
                TENANT_ID, CATEGORY_BOREHAMWOOD, null);

        // check category
        assertEquals(category.getName(), categoryFound.getName());
        assertNotNull(category.getImages());
        assertEquals(StockCategory.DEFAULT_IMAGE_COUNT, category.getImages()
                .size());

        // check item
        assertNotNull(categoryFound.getStockItems());
        assertEquals(2, categoryFound.getStockItems().size());
        assertEquals(officeItem.getName(), categoryFound.getStockItems().get(0)
                .getName());
        // item desc if null should default to category
        assertEquals(category.getDescription(), categoryFound.getStockItems()
                .get(0).getDescription());
        assertNotNull(category.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, categoryFound
                .getStockItems().get(0).getImages().size());

        // Find just Office types
        categoryFound = categoryController.findByName(TENANT_ID, CATEGORY_BOREHAMWOOD,
                "Office");
        assertEquals(1, categoryFound.getStockItems().size());
    }

    protected void addItemToCategory(StockCategory category, String itemUri) {
        itemController.setStockCategory(TENANT_ID, itemUri, categoryId);
    }

    protected StockCategory createCategory() throws IOException {
        // Read JSON into category object
        StockCategory category = getCategory();

        // Store via REST API
        ResponseEntity<?> categoryResp = categoryController.create(TENANT_ID,
                category);

        // assert stored fields
        assertEquals(HttpStatus.CREATED, categoryResp.getStatusCode());

        // check REST headers
        List<String> locationHdrs = categoryResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        categoryId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        // assert derived fields
        assertEquals("", category.getVideoCode());
        assertNotNull(category.getImages());
        assertEquals(StockCategory.DEFAULT_IMAGE_COUNT, category.getImages()
                .size());

        return category;
    }

    protected StockItem createOfficeItem() throws IOException {
        StockItem item = getOfficeItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        // assert derived fields
        assertNotNull(item.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, item.getImages().size());

        return item;
    }

    protected StockItem createWarehouseItem() throws IOException {
        StockItem item = getWarehouseItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        // assert derived fields
        assertNotNull(item.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, item.getImages().size());

        return item;
    }

    private String getLocationUri(ResponseEntity<?> itemResp) {
        assertEquals(HttpStatus.CREATED, itemResp.getStatusCode());
        List<String> locationHdrs = itemResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        itemUri = locationHdrs.get(0);
        assertNotNull(itemUri);
        return itemUri;
    }

    protected StockCategory getCategory() throws IOException {
        String categoryJson = "{\"name\":\"Borehamwood\",\"description\":\"A very fine property\",\"postCode\":\"WD6 1RN\"}";

        StockCategory category = objectMapper.readValue(categoryJson,
                new TypeReference<StockCategory>() {
                });
        assertNotNull(category);
        assertEquals(CATEGORY_BOREHAMWOOD, category.getName());
        assertEquals("WD6 1RN", category.getPostCode());

        return category;
    }

    protected StockItem getOfficeItem() throws IOException {
        String itemJson = "{\"name\":\"trademark\",\"type\":\"Office\","
                + "\"tenantId\":\"" + TENANT_ID + "\","
                + "\"firstContact\":null,\"lastUpdated\":null,"
                + "\"customFields\":{}}";

        StockItem item = objectMapper.readValue(itemJson,
                new TypeReference<StockItem>() {
                });
        assertNotNull(item);

        return item;
    }

    protected StockItem getWarehouseItem() throws IOException {
        StockItem item = new StockItem("Warehouse name", "Warehouse");
        item.setTenantId(TENANT_ID);

        assertEquals("Warehouse name", item.getName());
        assertEquals("Warehouse", item.getType());
        return item;
    }
}
