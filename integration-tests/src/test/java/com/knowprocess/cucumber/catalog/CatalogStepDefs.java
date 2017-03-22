package com.knowprocess.cucumber.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.springframework.http.HttpStatus;

import com.knowprocess.cucumber.IntegrationTestSupport;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import link.omny.catalog.model.MediaResource;
import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;

public class CatalogStepDefs extends IntegrationTestSupport {

    private int stockItemCount;

    public CatalogStepDefs() {
        super();
        stockItemCount = Integer.parseInt(properties.getProperty("kp.app.stockItemCount"));
    }

    @Then("^the call took less than (\\d+)ms$")
    public void the_call_took_less_than_ms(int max) throws Throwable {
        assertTrue(latestTiming < max);
    }
    
    @When("^a list of stock categories is requested$")
    public void a_list_of_stock_categories_is_requested() throws Throwable {
        executeGet(String.format("/%1$s/stock-categories/", tenantId));
    }

    @Then("^a list of stock categories _summaries_ is returned$")
    public void a_list_of_stock_categories__summaries__is_returned()
            throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.OK);
        StockCategory[] categories = (StockCategory[]) latestResponse.parseArray(StockCategory.class);
        assertEquals(59, categories.length);
        assertEquals(0, categories[0].getStockItems().size());
        assertEquals(0, categories[0].getCustomFields().size());
    }
    
    @When("^a list of stock items is requested$")
    public void a_list_of_stock_items_is_requested() throws Throwable {
        executeGet(String.format("/%1$s/stock-items/", tenantId));
    }
    
    @Then("^a list of stock item _summaries_ is returned$")
    public void a_list_of_stock_item__summaries__is_returned() throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.OK);
        StockItem[] items = (StockItem[]) latestResponse.parseArray(StockItem.class);
        assertEquals(stockItemCount, items.length);
        assertEquals(0, items[0].getCustomFields().size());
    }
    
    @When("^a search is made for Corsham$")
    public void a_search_is_made_for_Corsham() throws Throwable {
        executeGet(String.format("/%1$s/stock-categories/findByLocation?q=Corsham", tenantId));
    }
    
    @Then("^a list of (\\d+) stock categories is returned with full details$")
    public void a_list_of_stock_categories_is_returned(int catCount) throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.OK);
        StockCategory[] categories = (StockCategory[]) latestResponse.parseArray(StockCategory.class);
        assertEquals(catCount, categories.length);
    }

    @Then("^the first category is ([\\w]*), which has the default of (\\d+) image URLs and contains (\\d+) items$")
    public void the_first_category_is_X_which_has_the_default_of_image_URLs_and_contains_items(
            String categoryName, int imageCount, int itemCount) throws Throwable {
        StockCategory cat = ((StockCategory[]) latestResponse.latestObject())[0];
        assertNotNull(cat);
        assertEquals(categoryName, cat.getName());
        assertStockCategoryFieldsPresent(cat);

        assertEquals(itemCount, cat.getStockItems().size());
        assertEquals(imageCount, cat.getImages().size());
        for (MediaResource resource : cat.getImages()) {
            // Starts from /images/melksham/1.jpg thru to 8.jpg
            assertNotNull(resource.getUrl());
        }
    }
    
    @Then("^the default of (\\d+) image URLs are included for the specified category$")
    public void the_default_of_image_URLs_are_included_for_specified_category(int imageCount) throws Throwable {
        StockCategory cat = ((StockCategory) latestResponse.latestObject());
        assertNotNull(cat);
        assertEquals(imageCount, cat.getImages().size());
        for (MediaResource resource : cat.getImages()) {
            assertNotNull(resource.getUrl());
        }
    }

    @Then("^(\\d+) units are included each with (\\d+) image urls, name, description, tags, and size$")
    public void units_are_included_each_with_image_urls(int unitCount, int imageCount) throws Throwable {
        StockCategory cat = ((StockCategory) latestResponse.latestObject());
        assertNotNull(cat);
        assertEquals(unitCount, cat.getStockItems().size());
        for (StockItem item : cat.getStockItems()) {
            assertNotNull(item);
            assertNotNull(item.getId());
            assertNotNull(item.getName());
            assertNotNull(item.getPrimeTag());
            assertNotNull(item.getSize());
            assertNotNull(item.getSizeString());
            assertNotNull(item.getCustomFields());
            assertEquals(imageCount, item.getImages().size());
            for (MediaResource resource : item.getImages()) {
                assertNotNull(resource.getUrl());
            }
        }
    }
    
    @When("^a search is made for an office near \"([^\"]*)\"$")
    public void a_search_is_made_for_an_office_near_post_code(String arg1) throws Throwable {
        executeGet(String.format("/%1$s/stock-categories/findByLocation?q=%2$s&&type=Office", tenantId, arg1));
    }
    
    @When("^a request is made for ([\\w]*)$")
    public void a_request_is_made_for_category(String town) throws Throwable {
        executeGet(String.format("/%1$s/stock-categories/findByName?name=%2$s", tenantId, town));
    }
    
    @When("^a request is made for item ([\\d+]*)$")
    public void a_request_is_made_for_item(int id) throws Throwable {
        executeGet(String.format("/%1$s/stock-items/%2$d", tenantId, id));
    }

    @Then("^Office \"([^\"]*)\" is returned including all details$")
    public void office_in_town_is_returned_including_all_details(String name) throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.OK);
        StockItem item = (StockItem) latestResponse.parseObject(StockItem.class);
        assertEquals(name, item.getName());
    }
    
    @Then("^the stock item's category ([\\w]*) is available$")
    public void the_stock_category_is_available(String category) throws Throwable {
        StockItem item = (StockItem) latestResponse.parseObject(StockItem.class);
        assertNotNull(item.getStockCategory());
        assertEquals(category, item.getStockCategory().getName());
        assertStockCategoryFieldsPresent(item.getStockCategory());
    }

    @When("^the item status is updated to \"([^\"]*)\"$")
    public void the_item_status_is_updated_to(String status) throws Throwable {
        StockItem item = (StockItem) latestResponse.latestObject();
        item.setStatus(status);
        executePut(String.format("/%1$s/stock-items/%1$d", item.getId()), item);
    }

    @Then("^success status is returned$")
    public void success_status_is_returned() throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.NO_CONTENT);
    }
    
    @Then("^category ([\\w]*) alone is returned including name, description, status, address1, postcode, tags & directions$")
    public void the_single_stock_category_is_returned_including_all_details(String town) throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.OK);
        StockCategory category = (StockCategory) latestResponse.parseObject(StockCategory.class);
        assertEquals(town, category.getName());
        assertStockCategoryFieldsPresent(category);
    }

    private void assertStockCategoryFieldsPresent(StockCategory category) {
        assertNotNull(category.getDescription());
        assertNotNull(category.getStatus());
        assertNotNull(category.getAddress1());
        assertNotNull(category.getPostCode());
        assertNotNull(category.getLat());
        assertNotNull(category.getLng());
        assertNotNull(category.getMapUrl());
        assertNotNull(category.getTags());
        assertNotNull(category.getDirectionsByAir());
        assertNotNull(category.getDirectionsByPublicTransport());
        assertNotNull(category.getDirectionsByRoad());
        assertNotNull(category.getCustomFields());

//        assertNotNull(category.getCustomFieldValue("leaseType"));
//        assertNotNull(category.getCustomFieldValue("licenceType"));
 //       assertNotNull(category.getCustomFieldValue("facilityTags"));
    }

}
