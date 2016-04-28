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

        StockItem item = createItem();

        addItemToCategory(category, itemUri);

        searchCategory(category, item);

        StockCategory category2 = categoryRepo.findOne(categoryId);
        // TODO Unclear why but this is
        assertNotNull(category2.getCreated());
        assertNotNull(category2.getLastUpdated());
    }

    protected void searchCategory(StockCategory category, StockItem item)
            throws IOException {
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, null, null, null);
        assertEquals(1, categoryResults.size());

        // check category
        assertEquals(category.getName(), categoryResults.get(0).getName());

        // check item
        assertNotNull(categoryResults.get(0).getStockItems());
        assertEquals(1, categoryResults.get(0).getStockItems().size());
        assertEquals(item.getName(), categoryResults.get(0).getStockItems()
                .get(0).getName());
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
        assertNotNull(category.getImages());
        assertEquals(StockCategory.DEFAULT_IMAGE_COUNT, category.getImages()
                .size());

        return category;
    }

    protected StockItem createItem() throws IOException {
        StockItem item = getItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);
        assertEquals(HttpStatus.CREATED, itemResp.getStatusCode());
        List<String> locationHdrs = itemResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        itemUri = locationHdrs.get(0);
        assertNotNull(itemUri);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        return item;
    }

    protected StockCategory getCategory() throws IOException {
        String categoryJson = "{\"name\":\"Borehamwood\",\"postCode\":\"WD6 1RN\"}";

        StockCategory category = objectMapper.readValue(categoryJson,
                new TypeReference<StockCategory>() {
                });
        assertNotNull(category);
        assertEquals("Borehamwood", category.getName());
        assertEquals("WD6 1RN", category.getPostCode());

        return category;
    }

    protected StockItem getItem() throws IOException {
        String itemJson = "{\"name\":\"trademark\","
                + "\"companyNumber\":null,\"aliases\":null,"
                + "\"businessWebsite\":\"\",\"shortDesc\":null,"
                + "\"description\":\"test\",\"incorporationYear\":null,"
                + "\"noOfEmployees\":null,\"tenantId\":\"firmgains\","
                + "\"firstContact\":null,\"lastUpdated\":null,"
                + "\"customFields\":{}}";

        StockItem item = objectMapper.readValue(itemJson,
                new TypeReference<StockItem>() {
                });
        assertNotNull(item);

        return item;
    }
}
