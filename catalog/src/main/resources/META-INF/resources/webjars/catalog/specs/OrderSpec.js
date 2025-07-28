/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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
  var purchaseOrdersBefore = [];
  var orders = [];
  var purchaseOrders = [];
  var contact = {
      firstName: 'Barney',
      lastName: 'Rubble',
      email: 'barney@slaterock.com',
      customFields: {
        dateOfBirth: '02/02/1968'
      }
  };
  var stockItem = {
      name: 'Widget A '+new Date().getTime()
  };
  var order = {
      name: 'Order 123',
      type: 'order',
      date: '2017-01-31',
      price: '100',
      customFields: {
        specialInstructions: 'Signature required'
      }
  };
  var orderWithContact = {
      name: 'Order 789',
      type: 'order',
      date: '2017-01-31',
      price: '100',
      customFields: {
        specialInstructions: 'Signature required'
      }
  };
  var po = {
      name: 'Purchase Order 456',
      type: 'po',
      date: '2017-02-28',
      price: '100',
      customFields: {
        approver: 'Mr Slate'
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
        refusalReason: '1 palette damaged',
        refusalDate: '2017-02-04'
      }
  }
  var complexOrder = {
      name: 'Order 789',
      date: '2017-02-01',
      stage: 'Draft',
      orderItems: [
        {
          index: 1,
          price: 100,
          status: 'Draft',
          customFields: {
            controlledGoods: 'Proof of age, over 18 required'
          }
        },
        {
          index: 2,
          price: 200,
          status: 'Draft',
          customFields: {
            specialInstructions: 'Signature required'
          }
        }
      ]
  };
  var additionalOrderItem = {
    index: 3,
    price: 300,
    status: 'Draft',
    customFields: {
      specialInstructions: 'Christmas wrap'
    }
  }

  beforeEach(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
  });

  it("searches to take an initial baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/',  function(data, textStatus, jqXHR) {
      ordersBefore = data;
      expect(jqXHR.status).toEqual(200);

      $rh.getJSON('/'+tenantId+'/orders/findByType/po',  function(data, textStatus, jqXHR) {
        purchaseOrdersBefore = data;
        expect(jqXHR.status).toEqual(200);
        done();
      });
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
        // stockItem will be ignored like this, but check it does not get rejected
        complexOrder.orderItems[0].stockItem = stockItem;
        complexOrder.orderItems[1].stockItem = stockItem;
        // stock item id should be saved this way
        complexOrder.orderItems[0].stockItemId = parseInt(location.substring(location.lastIndexOf('/')+1));
        complexOrder.orderItems[1].stockItemId = parseInt(location.substring(location.lastIndexOf('/')+1));
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
      expect(orders[0].type).toEqual(order.type);
      expect(orders[0].date).toEqual(order.date);
      expect(''+orders[0].price).toEqual(order.price);

      done();
    });
  });

  it("creates a new purchase order", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/orders/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(po),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/orders\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        po.links = [ { rel: 'self', href: location } ];
        done();
      }
    });
  });

  it("fetches purchase orders by type and checks result", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/findByType/po',  function( data ) {
      purchaseOrders = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });

      expect(purchaseOrders.length).toEqual(purchaseOrdersBefore.length+1);
      console.log('latest po: '+JSON.stringify(purchaseOrders[0]));
      expect($rh.localId(purchaseOrders[0])).toEqual($rh.localId(po));
      expect(purchaseOrders[0].created).toBeDefined();
      expect(purchaseOrders[0].name).toEqual(po.name);
      expect(purchaseOrders[0].type).toEqual(po.type);
      expect(purchaseOrders[0].date).toEqual(po.date);
      expect(''+purchaseOrders[0].price).toEqual(po.price);

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

  it("fetches updated orders by type and checks the newly updated one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/orders/findByType/order', function( data ) {
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
      expect(data.feedback.customFields.refusalDate).toEqual(feedback.customFields.refusalDate);

      done();
    });
  });

  it("fetches complete order inc. child entities BY CONTACT and check all fields are correct", function(done) {
    // first we need a contact ...
    $rh.ajax({
      url: '/'+tenantId+'/contacts/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(contact),
      success: function(data, textStatus, jqXHR) {
        // NOTE this is URI not tenantUri
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/contacts\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        contact.links = [ { rel: 'self', href: location } ];

        // .. then an order ...
        orderWithContact.contactId = location.substring(location.lastIndexOf('/')+1);
        $rh.ajax({
          url: '/'+tenantId+'/orders/',
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(orderWithContact),
          success: function(data, textStatus, jqXHR) {
            var location = jqXHR.getResponseHeader('Location');
            expect(location).toMatch(/\/orders\/[0-9]/);
            expect(jqXHR.status).toEqual(201);
            orderWithContact.links = [ { rel: 'self', href: location } ];

            // ... and feedback ...
            $rh.ajax({
              url: $rh.tenantUri(orderWithContact)+'/feedback/',
              type: 'POST',
              contentType: 'application/json',
              data: JSON.stringify(feedback),
              success: function(data, textStatus, jqXHR) {
                var location = jqXHR.getResponseHeader('Location');
                expect(location).toMatch(/.*\/orders\/[0-9]*\/feedback\/[0-9]*/);
                expect(jqXHR.status).toEqual(201);

                // ... and now find order by contact
                console.log('tenantUri: '+$rh.tenantUri(orderWithContact)+', contactId: '+orderWithContact.contactId);
                console.log('tenantUri: '+$rh.localId(orderWithContact)+', contactId: '+orderWithContact.contactId);
                $rh.getJSON('/'+tenantId+'/orders/findByContacts/'+orderWithContact.contactId, function( data ) {

                  expect(data.length).toEqual(1);
                  data = data[0];

                  expect($rh.localId(data)).toEqual($rh.localId(orderWithContact));
                  expect(data.name).toEqual(orderWithContact.name);
                  expect(data.date).toEqual(orderWithContact.date);
                  expect(''+data.price).toEqual(orderWithContact.price);
                  expect(data.dueDate).toEqual(orderWithContact.dueDate);
                  expect(data.order).toEqual(orderWithContact.order);
                  expect(data.stage).toEqual(orderWithContact.stage);
                  expect(data.created).toBeDefined();
                  expect(data.customFields).toBeDefined();
                  expect(data.customFields.specialInstructions).toEqual(orderWithContact.customFields.specialInstructions);

                  expect(data.feedback.type).toEqual(feedback.type);
                  expect(data.feedback.description).toEqual(feedback.description);
                  expect(data.created).toBeDefined();
                  expect(data.feedback.customFields).toBeDefined();
                  expect(data.feedback.customFields.refusalReason).toEqual(feedback.customFields.refusalReason);
                  expect(data.feedback.customFields.refusalDate).toEqual(feedback.customFields.refusalDate);

                  done();
                });
              }
            });
          }
        });
      }
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

      expect(data.orderItems.length).toEqual(2);
      let sortedItems = [];
      data.orderItems.forEach(function(d,i) {
        if (d.index != undefined) {
          sortedItems[d.index-1] = d;
        } else {
          console.warn('  cannot sort item: '+d.id);
          sortedItems = data.orderItems;
          return;
        }
      });
      console.log('  sortedItems.length: '+sortedItems.length);
      data.orderItems = sortedItems;

      expect(data.orderItems[0].price).toEqual(complexOrder.orderItems[0].price);
      expect(data.orderItems[0].status).toEqual(complexOrder.orderItems[0].status);
      expect(data.orderItems[0].dueDate).toEqual(complexOrder.orderItems[0].dueDate);
      expect(data.orderItems[0].owner).toEqual(complexOrder.orderItems[0].owner);
      expect(data.orderItems[0].stockItemId).toBeDefined();
      expect(data.orderItems[0].stockItemId).toEqual(complexOrder.orderItems[0].stockItemId);
      expect(data.orderItems[0].customFields.controlledGoods).toEqual(complexOrder.orderItems[0].customFields.controlledGoods);

      expect(data.orderItems[1].price).toEqual(complexOrder.orderItems[1].price);
      expect(data.orderItems[1].status).toEqual(complexOrder.orderItems[1].status);
      expect(data.orderItems[1].dueDate).toEqual(complexOrder.orderItems[1].dueDate);
      expect(data.orderItems[1].owner).toEqual(complexOrder.orderItems[1].owner);
      expect(data.orderItems[1].stockItemId).toBeDefined();
      expect(data.orderItems[1].stockItemId).toEqual(complexOrder.orderItems[1].stockItemId);
      expect(data.orderItems[1].customFields.signatureRequired).toEqual(complexOrder.orderItems[1].customFields.signatureRequired);

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

  it("adds an item to the complex order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(complexOrder)+'/order-items',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(additionalOrderItem),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/order-items\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        additionalOrderItem.links = [ { rel: 'self', href: location } ];
        additionalOrderItem.id = location.substring(location.lastIndexOf('/'));
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

      expect(data.orderItems.length).toEqual(3);
      expect(data.orderItems[0].price).toEqual(complexOrder.orderItems[0].price);
      expect(data.orderItems[0].status).toEqual(complexOrder.orderItems[0].status);
      expect(data.orderItems[0].dueDate).toEqual(complexOrder.orderItems[0].dueDate);
      expect(data.orderItems[0].owner).toEqual(complexOrder.orderItems[0].owner);

      expect(data.orderItems[1].price).toEqual(complexOrder.orderItems[1].price);
      expect(data.orderItems[1].status).toEqual(complexOrder.orderItems[1].status);
      expect(data.orderItems[1].dueDate).toEqual(complexOrder.orderItems[1].dueDate);
      expect(data.orderItems[1].owner).toEqual(complexOrder.orderItems[1].owner);

      expect(data.orderItems[2].price).toEqual(additionalOrderItem.price);
      expect(data.orderItems[2].status).toEqual(additionalOrderItem.status);
      expect(data.orderItems[2].dueDate).toEqual(additionalOrderItem.dueDate);
      expect(data.orderItems[2].owner).toEqual(additionalOrderItem.owner);

      done();
    });
  });

  it("deletes the last item of the complex order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(complexOrder)+'/order-items/'+additionalOrderItem.id,
      type: 'DELETE',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
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

  it("deletes the added purchase order", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(po),
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

  it("deletes the added order with its associated contact", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(orderWithContact),
      type: 'DELETE',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        $rh.ajax({
          url: $rh.tenantUri(contact),
          type: 'DELETE',
          contentType: 'application/json',
          success: completeHandler = function(data, textStatus, jqXHR) {
            expect(jqXHR.status).toEqual(204);
            done();
          }
        });
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
