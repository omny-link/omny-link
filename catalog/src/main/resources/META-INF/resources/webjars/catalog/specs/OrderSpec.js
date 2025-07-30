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
const tenantId = 'acme';
const server = (typeof $env === 'undefined' || !$env) ? 'http://localhost:8082' : $env.server;
const baseUrl = `${server}/${tenantId}`;
const originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;

async function fetchJson(url, options = {}) {
  const res = await fetch(url, options);
  const data = await res.json().catch(() => undefined);
  return { data, res };
}

async function fetchText(url, options = {}) {
  const res = await fetch(url, options);
  const data = await res.text();
  return { data, res };
}

describe("Product catalogue", function() {
  const contact = {
    firstName: 'Barney',
    lastName: 'Rubble',
    email: 'barney@slaterock.com',
    customFields: {
      dateOfBirth: '02/02/1968'
    }
  };
  const stockItem = {
    name: 'Widget A ' + new Date().getTime()
  };
  const order = {
    name: 'Order 123',
    type: 'order',
    date: '2017-01-31',
    price: '100',
    customFields: {
      specialInstructions: 'Signature required'
    }
  };
  const orderWithContact = {
    name: 'Order 789',
    type: 'order',
    date: '2017-01-31',
    price: '100',
    customFields: {
      specialInstructions: 'Signature required'
    }
  };
  const po = {
    name: 'Purchase Order 456',
    type: 'po',
    date: '2017-02-28',
    price: '100',
    customFields: {
      approver: 'Mr Slate'
    }
  };
  const note = {
    author: 'sales@slaterock.com',
    content: 'No deliveries before 7am'
  };
  const doc = {
    author: 'info@slaterock.com',
    name: 'Delivery note for order 123',
    url: 'http://slaterock.com/delivery-notes/Order123.odt',
    favorite: true
  };
  const feedback = {
    description: 'Palette not well packed hence damage',
    type: 'driver',
    customFields: {
      refusalReason: '1 palette damaged',
      refusalDate: '2017-02-04'
    }
  };
  let complexOrder = {
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
  const additionalOrderItem = {
    index: 3,
    price: 300,
    status: 'Draft',
    customFields: {
      specialInstructions: 'Christmas wrap'
    }
  };

  let ordersBefore = [];
  let purchaseOrdersBefore = [];
  let orders = [];
  let purchaseOrders = [];
  let stockItemsBefore;
  let stockItems;

  beforeEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
  });

  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });

  function getIdFromLocation(location) {
    return location.substring(location.lastIndexOf('/') + 1);
  }

  it("searches to take an initial baseline", async function() {
    const res1 = await fetch(`${baseUrl}/orders/`);
    expect(res1.status).toEqual(200);
    ordersBefore = await res1.json();
    const res2 = await fetch(`${baseUrl}/orders/findByType/po`);
    expect(res2.status).toEqual(200);
    purchaseOrdersBefore = await res2.json();
  });

  it("takes a baseline of available stock items", async function() {
    const res = await fetch(`${baseUrl}/stock-items/`);
    expect(res.status).toEqual(200);
    stockItemsBefore = await res.json();
  });

  it("creates a new stock item", async function() {
    const res = await fetch(`${baseUrl}/stock-items/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(stockItem)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/\/stock-items\/[0-9]/);
    expect(res.status).toEqual(201);
    stockItem.links = [{ rel: 'self', href: location }];
    order.stockItem = stockItem;
    complexOrder.orderItems[0].stockItem = stockItem;
    complexOrder.orderItems[1].stockItem = stockItem;
    const stockItemId = parseInt(location.substring(location.lastIndexOf('/') + 1));
    complexOrder.orderItems[0].stockItemId = stockItemId;
    complexOrder.orderItems[1].stockItemId = stockItemId;
  });

  it("creates a new order", async function() {
    const res = await fetch(`${baseUrl}/orders/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(order)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/\/orders\/[0-9]/);
    expect(res.status).toEqual(201);
    order.links = [{ rel: 'self', href: location }];
  });

  it("fetches updated orders and checks the newly added one is correct", async function() {
    const res = await fetch(`${baseUrl}/orders/`);
    orders = await res.json();
    orders.sort((a, b) => new Date(b.created) - new Date(a.created));
    expect(orders.length).toEqual(ordersBefore.length + 1);
    expect(orders[0].name).toEqual(order.name);
    expect(orders[0].type).toEqual(order.type);
    expect(orders[0].date).toEqual(order.date);
    expect('' + orders[0].price).toEqual(order.price);
  });

  it("creates a new purchase order", async function() {
    const res = await fetch(`${baseUrl}/orders/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(po)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/\/orders\/[0-9]/);
    expect(res.status).toEqual(201);
    po.links = [{ rel: 'self', href: location }];
  });

  it("fetches purchase orders by type and checks result", async function() {
    const res = await fetch(`${baseUrl}/orders/findByType/po`);
    purchaseOrders = await res.json();
    purchaseOrders.sort((a, b) => new Date(b.created) - new Date(a.created));
    expect(purchaseOrders.length).toEqual(purchaseOrdersBefore.length + 1);
    expect(purchaseOrders[0].name).toEqual(po.name);
    expect(purchaseOrders[0].type).toEqual(po.type);
    expect(purchaseOrders[0].date).toEqual(po.date);
    expect('' + purchaseOrders[0].price).toEqual(po.price);
  });

  it("adds a note to the order", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}/notes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(note)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/.*\/orders\/[0-9]*\/notes\/[0-9]*/);
    expect(res.status).toEqual(201);
  });

  it("adds a document link to the order", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}/documents`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(doc)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/.*\/orders\/[0-9]*\/documents\/[0-9]*/);
    expect(res.status).toEqual(201);
  });

  it("sets the stage of the order", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    order.stage = 'dispatched';
    const res = await fetch(`${baseUrl}/orders/${orderId}/stage/${order.stage}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(res.status).toEqual(204);
    const res2 = await fetch(`${baseUrl}/orders/${orderId}`);
    const data = await res2.json();
    expect(data.name).toEqual(order.name);
    expect('' + data.price).toEqual(order.price);
    expect(data.stage).toEqual('dispatched');
    expect(data.customFields.specialInstructions).toEqual(order.customFields.specialInstructions);
  });

  it("updates the new order", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    order.dueDate = '2017-02-15';
    order.owner = 'sales@slaterock.com';
    const res = await fetch(`${baseUrl}/orders/${orderId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(order)
    });
    expect(res.status).toEqual(204);
  });

  it("provides feedback for the order", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(feedback)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/.*\/orders\/[0-9]*\/feedback\/[0-9]*/);
    expect(res.status).toEqual(201);
  });

  it("fetches updated orders by type and checks the newly updated one is correct", async function() {
    const res = await fetch(`${baseUrl}/orders/findByType/order`);
    orders = await res.json();
    orders.sort((a, b) => new Date(b.created) - new Date(a.created));
    expect(orders.length).toEqual(ordersBefore.length + 1);
    expect(orders[0].owner).toEqual(order.owner);
    expect(orders[0].dueDate).toEqual(order.dueDate);
    expect(orders[0].stage).toEqual(order.stage);
  });

  it("fetches complete order inc. child entities and check all fields are correct", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}`);
    const data = await res.json();
    expect(data.name).toEqual(order.name);
    expect(data.date).toEqual(order.date);
    expect('' + data.price).toEqual(order.price);
    expect(data.dueDate).toEqual(order.dueDate);
    expect(data.stage).toEqual(order.stage);
    expect(data.customFields.specialInstructions).toEqual(order.customFields.specialInstructions);
    expect(data.notes.length).toBeGreaterThanOrEqual(1);
    expect(data.documents.length).toBeGreaterThanOrEqual(1);
    expect(data.feedback.type).toEqual(feedback.type);
    expect(data.feedback.description).toEqual(feedback.description);
    expect(data.feedback.customFields.refusalReason).toEqual(feedback.customFields.refusalReason);
    expect(data.feedback.customFields.refusalDate).toEqual(feedback.customFields.refusalDate);
  });

  it("fetches complete order inc. child entities BY CONTACT and check all fields are correct", async function() {
    // create contact
    const resContact = await fetch(`${baseUrl}/contacts/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contact)
    });
    const contactLocation = resContact.headers.get('Location');
    expect(contactLocation).toMatch(/\/contacts\/[0-9]/);
    expect(resContact.status).toEqual(201);
    contact.links = [{ rel: 'self', href: contactLocation }];
    // create order with contact
    orderWithContact.contactId = getIdFromLocation(contactLocation);
    const resOrder = await fetch(`${baseUrl}/orders/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderWithContact)
    });
    const orderLocation = resOrder.headers.get('Location');
    expect(orderLocation).toMatch(/\/orders\/[0-9]/);
    expect(resOrder.status).toEqual(201);
    orderWithContact.links = [{ rel: 'self', href: orderLocation }];
    // add feedback
    const resFeedback = await fetch(`${baseUrl}/orders/${getIdFromLocation(orderLocation)}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(feedback)
    });
    const feedbackLocation = resFeedback.headers.get('Location');
    expect(feedbackLocation).toMatch(/.*\/orders\/[0-9]*\/feedback\/[0-9]*/);
    expect(resFeedback.status).toEqual(201);
    // find order by contact
    const resFind = await fetch(`${baseUrl}/orders/findByContacts/${orderWithContact.contactId}`);
    let data = await resFind.json();
    expect(data.length).toEqual(1);
    data = data[0];
    expect(data.name).toEqual(orderWithContact.name);
    expect(data.date).toEqual(orderWithContact.date);
    expect('' + data.price).toEqual(orderWithContact.price);
    expect(data.customFields.specialInstructions).toEqual(orderWithContact.customFields.specialInstructions);
    expect(data.feedback.type).toEqual(feedback.type);
    expect(data.feedback.description).toEqual(feedback.description);
    expect(data.feedback.customFields.refusalReason).toEqual(feedback.customFields.refusalReason);
    expect(data.feedback.customFields.refusalDate).toEqual(feedback.customFields.refusalDate);
  });

  it("creates a new complex order (including order items)", async function() {
    const res = await fetch(`${baseUrl}/orders/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(complexOrder)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/\/orders\/[0-9]/);
    expect(res.status).toEqual(201);
    complexOrder.links = [{ rel: 'self', href: location }];
  });

  it("fetches the complex order inc. order items and check all fields are correct", async function() {
    const orderId = getIdFromLocation(complexOrder.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}`);
    const data = await res.json();
    expect(data.name).toEqual(complexOrder.name);
    expect(data.orderItems.length).toEqual(2);
    complexOrder = data;
  });

  it("updates the new complex order", async function() {
    const orderId = getIdFromLocation(complexOrder.links[0].href);
    complexOrder.dueDate = '2017-02-16';
    complexOrder.owner = 'sales@slaterock.com';
    const res = await fetch(`${baseUrl}/orders/${orderId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(complexOrder)
    });
    expect(res.status).toEqual(204);
  });

  it("adds an item to the complex order", async function() {
    const orderId = getIdFromLocation(complexOrder.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}/order-items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(additionalOrderItem)
    });
    const location = res.headers.get('Location');
    expect(location).toMatch(/\/order-items\/[0-9]/);
    expect(res.status).toEqual(201);
    additionalOrderItem.links = [{ rel: 'self', href: location }];
    additionalOrderItem.id = getIdFromLocation(location);
  });

  it("fetches the complex order inc. order items and check all fields are correct", async function() {
    const orderId = getIdFromLocation(complexOrder.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}`);
    const data = await res.json();
    expect(data.name).toEqual(complexOrder.name);
    expect(data.orderItems.length).toEqual(3);
  });

  it("deletes the last item of the complex order", async function() {
    const orderId = getIdFromLocation(complexOrder.links[0].href);
    const itemId = additionalOrderItem.id;
    const res = await fetch(`${baseUrl}/orders/${orderId}/order-items/${itemId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(res.status).toEqual(204);
  });

  it("deletes the added order", async function() {
    const orderId = getIdFromLocation(order.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(res.status).toEqual(204);
  });

  it("deletes the added purchase order", async function() {
    const poId = getIdFromLocation(po.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${poId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(res.status).toEqual(204);
  });

  it("deletes the added complex order", async function() {
    const orderId = getIdFromLocation(complexOrder.links[0].href);
    const res = await fetch(`${baseUrl}/orders/${orderId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(res.status).toEqual(204);
  });

  it("deletes the added order with its associated contact", async function() {
    const orderId = getIdFromLocation(orderWithContact.links[0].href);
    const resOrder = await fetch(`${baseUrl}/orders/${orderId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(resOrder.status).toEqual(204);
    const contactId = getIdFromLocation(contact.links[0].href);
    const resContact = await fetch(`${baseUrl}/contacts/${contactId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(resContact.status).toEqual(204);
  });

  it("deletes the added stock item", async function() {
    const stockItemId = getIdFromLocation(stockItem.links[0].href);
    const res = await fetch(`${baseUrl}/stock-items/${stockItemId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });
    expect(res.status).toEqual(204);
  });

  it("checks the order data is the same as the baseline", async function() {
    const res = await fetch(`${baseUrl}/orders/`);
    orders = await res.json();
    orders.sort((a, b) => new Date(b.created) - new Date(a.created));
    expect(orders.length).toEqual(ordersBefore.length);
  });

  it("checks the stock item data is the same as the baseline", async function() {
    const res = await fetch(`${baseUrl}/stock-items/`);
    stockItems = await res.json();
    stockItems.sort((a, b) => new Date(b.created) - new Date(a.created));
    expect(stockItems.length).toEqual(stockItemsBefore.length);
  });
});
