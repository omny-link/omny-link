package com.knowprocess.cucumber.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;

import com.knowprocess.cucumber.IntegrationTestSupport;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.Order;

public class OrderStepDefs extends IntegrationTestSupport {

    private static final CustomOrderField CUSTOM_FIELD_2 = new CustomOrderField("bar", "bar1");
    private static final CustomOrderField CUSTOM_FIELD_1 = new CustomOrderField("foo", "foo1");
    private static final String INVOICE_REF = "ABC-123";
    private String orderUri;
    private Order order;
    private BigDecimal orderPrice;
    private String orderName;
    private int customFieldCount = 2;

    @When("^a list of orders is requested$")
    public void a_list_of_orders_is_requested() throws Throwable {
        executeGet(String.format("/%1$s/orders/", tenantId));
    }

    @Then("^a list of (\\d+) order _summaries_ is returned$")
    public void a_list_of_order__summaries__is_returned(int orderCount) throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.OK);
        Order[] categories = (Order[]) latestResponse.parseArray(Order.class);
        assertEquals(orderCount, categories.length);
    }

    @When("^a list of orders is requested for contacts (\\d+) & (\\d+)$")
    public void a_list_of_orders_is_requested_for_contacts(int contactId1, int contactId2) throws Throwable {
        executeGet(String.format("/%1$s/orders/findByContacts/%2$d,%3$d", tenantId, contactId1, contactId2));
    }

    @When("^the order X is retrieved$")
    public void the_order_X_is_retrieved() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^a success response is returned identifying the order created$")
    public void a_success_response_is_returned_identifying_the_order_created() throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.CREATED);
        orderUri = latestResponse.location();
        assertTrue(orderUri.matches("http://\\w*:?\\d*/\\w*/orders/\\d+"));
    }

    @When("^an order for a \"([^\"]*)\" priced at \"([$£]?)([^\"]*)\" with 2 custom fields is submitted$")
    public void an_order_is_submitted(String name, String currency, String price) throws Throwable {
        Order order = new Order();
        orderName = name;
        order.setName(orderName);
        orderPrice = new BigDecimal(price);
        order.setPrice(orderPrice);
        order.addCustomField(CUSTOM_FIELD_1);
        order.addCustomField(CUSTOM_FIELD_2);
        executePost(String.format("/%1$s/orders/", tenantId), order);
    }

    @Then("^the order is retrievable by the provided identifier$")
    public void the_order_is_retrievable_by_the_provided_identifier() throws Throwable {
        executeGet(orderUri);
        latestResponse.statusCodeIs(HttpStatus.OK);
        order = (Order) latestResponse.parseObject(Order.class);
    }

    @Then("^the order includes expected price and custom fields$")
    public void the_order_includes_price_and_custom_fields() throws Throwable {
        assertNotNull(order);
        assertNotNull(order.getLocalId());
        assertEquals(orderName, order.getName());
        assertEquals(tenantId, order.getTenantId());
        assertEquals(orderPrice, order.getPrice());
        assertEquals(customFieldCount , order.getCustomFields().size());
        assertEquals(CUSTOM_FIELD_1.getValue(), order.getCustomFieldValue(CUSTOM_FIELD_1.getName()));
    }

    @When("^an invoice ref is specified for the order$")
    public void an_invoice_ref_is_specified_for_the_order() throws Throwable {
        order.setInvoiceRef(INVOICE_REF);
        executePut(orderUri, order);
        latestResponse.statusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Then("^retrieving the order shows the invoice ref to have been saved$")
    public void retrieving_the_order_shows_the_invoice_ref_to_have_been_saved() throws Throwable {
        executeGet(orderUri);
        latestResponse.statusCodeIs(HttpStatus.OK);
        order = (Order) latestResponse.parseObject(Order.class);
        assertNotNull(order);
        assertNotNull(order.getLocalId());
        assertEquals(INVOICE_REF, order.getInvoiceRef());
    }

    @When("^feedback is submitted for the order$")
    public void feedback_is_submitted_for_the_order() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the response identifies the newly created feedback$")
    public void the_response_identifies_the_newly_created_feedback() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the feedback is retrievable and includes creation date, content and custom field$")
    public void the_feedback_is_retrievable_and_includes_creation_date_content_and_custom_field() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^the order is deleted$")
    public void the_order_is_deleted() throws Throwable {
        executeDelete(orderUri);
    }

    @Then("^the order IS retrievable but marked deleted$")
    public void the_order_is_retrievable_but_deleted() throws Throwable {
        executeGet(orderUri);
        latestResponse.statusCodeIs(HttpStatus.OK);
        order = (Order) latestResponse.parseObject(Order.class);
        assertNotNull(order);
        assertNotNull(order.getLocalId());
        assertEquals("deleted", order.getStage());
    }

}
