package link.omny.catalog.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import link.omny.catalog.TestApplication;
import link.omny.catalog.model.CustomStockItemField;
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
public class StockControllersTest {

    private static final String CATEGORY_BOREHAMWOOD = "Borehamwood";

    private static final String TENANT_ID = "omny";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockCategoryRepository categoryRepo;

    @Autowired
    private StockItemRepository itemRepo;

    @Autowired
    private StockCategoryController categoryController;

    @Autowired
    private StockItemController itemController;

    private Long categoryId;

    private String itemUri;

    private DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd");

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

        StockItem officeItem = createOfficeItem(category);
        addItemToCategory(category, officeItem.getSelfRef());

        StockItem warehouseItem = createWarehouseItem(category);
        addItemToCategory(category, warehouseItem.getSelfRef());

        StockItem businessUnitItem = createBusinessUnitItem(category);
        addItemToCategory(category, businessUnitItem.getSelfRef());

        StockItem unpublishedItem = createUnpublishedItem(category);
        addItemToCategory(category, unpublishedItem.getSelfRef());

        StockCategory category2 = categoryRepo.findOne(categoryId);
        assertNotNull(category2.getCreated());
        // TODO h2 test db not supporting default?
        // assertNotNull(category2.getLastUpdated());

        findAll(category, officeItem, warehouseItem);
        findNearReading(category, officeItem);
        findOffice(category, officeItem);
        findOfficeNearReading(category, officeItem);
        findOffersNearReading(category, officeItem);

        ShortStockCategory category3 = findByName(category, officeItem,
                warehouseItem);

        update(category3);
    }

    protected void update(ShortStockCategory category) {
        Long itemId = category.getStockItems().get(0).getStockItemId();
        StockItem stockItem = itemRepo.findOne(itemId);
        assertNotNull(stockItem.getId());

        GregorianCalendar cal = new GregorianCalendar();
        stockItem.addCustomField(new CustomStockItemField("tenancyEndDate",
                isoFormatter.format(cal.getTime())));

        itemController.update(TENANT_ID, stockItem.getId(), stockItem);

        StockItem stockItem2 = itemRepo.findOne(stockItem.getId());
        assertEquals(stockItem.getCustomFields().size(), stockItem2
                .getCustomFields().size());
    }

    protected void findAll(StockCategory category, StockItem officeItem,
            StockItem warehouseItem) throws IOException {
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, null, null, null, false, null, null);
        assertEquals(1, categoryResults.size());

        checkCategory(category, categoryResults.get(0));

        // check item
        assertNotNull(categoryResults.get(0).getStockItems());
        assertEquals(3, categoryResults.get(0).getStockItems().size());

        checkOfficeItem(category, officeItem, categoryResults.get(0));
    }

    protected void findOffice(StockCategory category, StockItem officeItem)
            throws IOException {
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, null, "Office", null, false, null,
                        null);
        assertEquals(2, categoryResults.get(0).getStockItems().size());

        checkCategory(category, categoryResults.get(0));
        checkOfficeItem(category, officeItem, categoryResults.get(0));
    }

    protected void findNearReading(StockCategory category, StockItem officeItem)
            throws IOException {
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, "Reading", null, null, false, null,
                        null);
        assertEquals(3, categoryResults.get(0).getStockItems().size());

        checkCategory(category, categoryResults.get(0));
        checkOfficeItem(category, officeItem, categoryResults.get(0));
    }

    protected void findOfficeNearReading(StockCategory category,
            StockItem officeItem) throws IOException {
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, "Reading", "Office", null, false,
                        null, null);
        assertEquals(2, categoryResults.get(0).getStockItems().size());

        checkOfficeItem(category, officeItem, categoryResults.get(0));
    }

    protected void findOffersNearReading(StockCategory category,
            StockItem officeItem) throws IOException {
        List<ShortStockCategory> categoryResults = categoryController
                .findByLocation(TENANT_ID, "Reading", null, null, true, null,
                        null);
        assertEquals(3, categoryResults.get(0).getStockItems().size());

        checkOfficeItem(category, officeItem, categoryResults.get(0));
    }

    protected ShortStockCategory findByName(StockCategory category,
            StockItem officeItem,
            StockItem warehouseItem) throws IOException {
        ShortStockCategory categoryFound = categoryController.findByName(
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
        assertEquals(officeItem.getName(), categoryFound.getStockItems().get(0)
                .getName());
        assertEquals("Published", officeItem.getStatus());

        // item desc if null should default to props file value
        assertEquals("A fantastic working space", categoryFound.getStockItems()
                .get(0).getDescription());
        assertNotNull(category.getImages());
        assertEquals(StockItem.DEFAULT_IMAGE_COUNT, categoryFound
                .getStockItems().get(0).getImages().size());

        // check custom fields (needs extra fetch as not all included in search)
        Long itemId = categoryFound.getStockItems().get(0).getStockItemId();
        StockItem stockItem = itemRepo.findOne(itemId);
        assertNotNull(stockItem.getId());
        assertEquals(officeItem.getCustomFields().size(), stockItem
                .getCustomFields().size());
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

    protected StockItem createOfficeItem(StockCategory category)
            throws IOException {
        StockItem item = getOfficeItem();
        ResponseEntity<?> itemResp = itemController.create(TENANT_ID, item);

        String itemUri = getLocationUri(itemResp);
        item.setId(Long.parseLong(itemUri.substring(itemUri.lastIndexOf('/') + 1)));

        assertEquals("trademark", item.getName());
        assertEquals("Office", item.getTags());
        assertEquals("vacant", item.getCustomFields().get(0).getName());
        assertEquals("true", item.getCustomFields().get(0).getValue());
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
        itemUri = locationHdrs.get(0);
        assertNotNull(itemUri);
        return itemUri;
    }

    protected StockCategory getCategory() throws IOException {
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
        assertEquals("vacant", item.getCustomFields().get(0).getName());
        assertEquals("true", item.getCustomFields().get(0).getValue());
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
