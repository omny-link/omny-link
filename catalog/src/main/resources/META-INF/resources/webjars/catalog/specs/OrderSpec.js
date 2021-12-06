/*******************************************************************************
 * Copyright 2015-2018 Tim Stephenson and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
describe("Product catalogue", function() {
  var tenantId = 'acme';
  var $rh = new RestEntityHelper({
    server: window['$env'] == undefined ? 'http://localhost:8080' : $env.server,
    tenantId: tenantId
  });

  var originalTimeout;
  var stockItemsBefore;
  var ordersBefore = [];
  var orders = [];
  var stockItem = {
      name: 'Widget A'
  };
  var order = {
      name: 'Order 123',
      date: '2017-01-31',
      price: '100',
      customFields: {
        specialInstructions: 'Signature required'
      }
  };
  var note = {
      author: 'sales@slaterock.com',
      content: 'No deliveries before 7am'
  }
  var doc = {
      author: 'info@slaterock.com',
      name: 'Delivery note for order 123',
      url: 'http://slaterock.com/delivery-notes/Order123.odt',
      favorite: true
  }
  var feedback = {
      description: 'Palette not well packed hence damage',
      type: 'driver',
      customFields: {
        refusalReason: '1 palette damaged'
      }
  }
  var complexOrder = {
      name: 'Order 456',
      date: '2017-02-01',
      stage: 'Draft',
      orderItems: [
        { price: 100, status: 'Draft' }
      ]
  };
  beforeEach(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
  });

  it("searches to take an initial baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/',  function(data, textStatus, jqXHR) {
      ordersBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
  });

  it("takes a baseline of available stock items", function(done) {
    $rh.getJSON('/'+tenantId+'/stock-items/',  function(data, textStatus, jqXHR) {
      stockItemsBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
  });

  it("creates a new stock item", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/stock-items/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(stockItem),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/stock-items\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        stockItem.links = [ { rel: 'self', href: location } ];
        order.stockItem = stockItem;
        complexOrder.orderItems[0].stockItem = stockItem;
        done();
      }
    });
  });

  it("creates a new order", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/orders/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(order),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/orders\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        order.links = [ { rel: 'self', href: location } ];
        done();
      }
    });
  });

  it("fetches updated orders and checks the newly added one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/',  function( data ) {
      orders = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });

      expect(orders.length).toEqual(ordersBefore.length+1);
      console.log('latest order: '+JSON.stringify(orders[0]));
      expect($rh.localId(orders[0])).toEqual($rh.localId(order));
      expect(orders[0].created).toBeDefined();
      expect(orders[0].name).toEqual(order.name);
      expect(orders[0].date).toEqual(order.date);
      expect(''+orders[0].price).toEqual(order.price);

      done();
    });
  });

  it("adds a note to the order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(order)+'/notes/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(note),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/orders\/[0-9]*\/notes\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("adds a document link to the order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(order)+'/documents/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(doc),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/orders\/[0-9]*\/documents\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("sets the stage of the order", function(done) {
    order.stage = 'dispatched';
    $rh.ajax({
      url: $rh.tenantUri(order)+'/stage/'+order.stage,
      type: 'POST',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        console.log('  check new stage: '+$rh.tenantUri(order));
        $rh.getJSON($rh.tenantUri(order), function( data ) {

          expect($rh.localId(data)).toEqual($rh.localId(order));
          expect(data.name).toEqual(order.name);
          expect(''+data.price).toEqual(order.price);
          expect(data.order).toEqual(order.order);
          expect(data.stage).toEqual('dispatched');
          expect(data.created).toBeDefined();
          expect(data.customFields).toBeDefined();
          expect(data.customFields.specialInstructions).toEqual(order.customFields.specialInstructions);

          done();
        });
      }
    });
  });

  it("updates the new order", function(done) {
    order.dueDate = '2017-02-15';
    order.owner = 'sales@slaterock.com';
    $rh.ajax({
      url: $rh.tenantUri(order),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(order),
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("provides feedback for the order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(order)+'/feedback/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(feedback),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/orders\/[0-9]*\/feedback\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("fetches updated orders and checks the newly updated one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/', function( data ) {
      orders = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });

      expect(orders.length).toEqual(ordersBefore.length+1);
      expect($rh.uri(order)).toContain($rh.uri(orders[0]));
      expect(orders[0].owner).toEqual(order.owner);
      expect(orders[0].dueDate).toEqual(order.dueDate);
      expect(orders[0].stage).toEqual(order.stage);

      done();
    });
  });

  it("fetches complete order inc. child entities and check all fields are correct", function(done) {
    console.log('tenantUri: '+$rh.tenantUri(order));
    $rh.getJSON($rh.tenantUri(order), function( data ) {

      expect($rh.localId(data)).toEqual($rh.localId(order));
      expect(data.name).toEqual(order.name);
      expect(data.date).toEqual(order.date);
      expect(''+data.price).toEqual(order.price);
      expect(data.dueDate).toEqual(order.dueDate);
      expect(data.order).toEqual(order.order);
      expect(data.stage).toEqual(order.stage);
      expect(data.created).toBeDefined();
      expect(data.lastUpdated).toBeGreaterThan(data.created);
      expect(data.customFields).toBeDefined();
      expect(data.customFields.specialInstructions).toEqual(order.customFields.specialInstructions);

      expect(data.notes.length).toEqual(1);
      expect(data.notes[0].author).toEqual(note.author);
      expect(data.notes[0].name).toEqual(note.name);

      expect(data.documents.length).toEqual(1);
      expect(data.documents[0].author).toEqual(doc.author);
      expect(data.documents[0].name).toEqual(doc.name);
      expect(data.documents[0].url).toEqual(doc.url);
      expect(data.documents[0].confidential).toEqual(false); // default value
      expect(data.documents[0].favorite).toEqual(doc.favorite); // defaults to false

      expect(data.feedback.type).toEqual(feedback.type);
      expect(data.feedback.description).toEqual(feedback.description);
      expect(data.created).toBeDefined();
      expect(data.feedback.customFields).toBeDefined();
      expect(data.feedback.customFields.refusalReason).toEqual(feedback.customFields.refusalReason);

      done();
    });
  });

  it("creates a new complex order (including order items)", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/orders/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(complexOrder),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/orders\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        complexOrder.links = [ { rel: 'self', href: location } ];
        done();
      }
    });
  });

  it("fetches the complex order inc. order items and check all fields are correct", function(done) {
    console.log('tenantUri: '+$rh.tenantUri(complexOrder));
    $rh.getJSON($rh.tenantUri(complexOrder), function( data ) {

      expect($rh.localId(data)).toEqual($rh.localId(complexOrder));
      expect(data.name).toEqual(complexOrder.name);
      complexOrder = data;

      done();
    });
  });

  it("updates the new complex order", function(done) {
    complexOrder.dueDate = '2017-02-16';
    complexOrder.owner = 'sales@slaterock.com';
    $rh.ajax({
      url: $rh.tenantUri(complexOrder),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(complexOrder),
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

    it("fetches the complex order inc. order items and check all fields are correct", function(done) {
    console.log('tenantUri: '+$rh.tenantUri(complexOrder));
    $rh.getJSON($rh.tenantUri(complexOrder), function( data ) {

      expect($rh.localId(data)).toEqual($rh.localId(complexOrder));
      expect(data.name).toEqual(complexOrder.name);
      expect(data.date).toEqual(complexOrder.date);
      expect(data.dueDate).toEqual(complexOrder.dueDate);
      expect(data.complexOrder).toEqual(complexOrder.complexOrder);
      expect(data.stage).toEqual(complexOrder.stage);
      expect(data.created).toBeDefined();
      expect(data.lastUpdated).toBeGreaterThan(data.created);

      expect(data.orderItems.length).toEqual(1);
      expect(data.orderItems.price).toEqual(complexOrder.orderItems.price);
      expect(data.orderItems.status).toEqual(complexOrder.orderItems.status);
      expect(data.orderItems.dueDate).toEqual(complexOrder.orderItems.dueDate);
      expect(data.orderItems.owner).toEqual(complexOrder.orderItems.owner);

      done();
    });
  });

  it("deletes the added order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(order),
      type: 'DELETE',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("deletes the added complex order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(complexOrder),
      type: 'DELETE',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("deletes the added stock item", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(stockItem),
      type: 'DELETE',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("checks the order data is the same as the baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/',  function( data ) {
      orders = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });

      expect(orders.length).toEqual(ordersBefore.length);
      done();
    });
  });

  it("checks the stock item data is the same as the baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/stock-items/',  function( data ) {
      stockItems = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });

      expect(stockItems.length).toEqual(stockItemsBefore.length);
      done();
    });
  });

  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
