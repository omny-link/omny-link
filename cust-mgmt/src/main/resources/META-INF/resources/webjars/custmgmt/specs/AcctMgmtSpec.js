/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
describe("Account management", function() {
  var tenantId = 'acme';
  var server = (typeof $env === 'undefined' || !$env) ? 'http://localhost:8080' : $env.server;
  var baseUrl = server + '/' + tenantId;
  
  var originalTimeout;
  var accountsBefore = [];
  var contactsBefore = [];
  var accounts = [];
  var contacts = [];
  var contact = {
      firstName: 'Fred',
      lastName: 'Flintstone',
      email: 'fred@slaterock.com'
  };
  var account = {
      name: 'Bedrock Slate and Gravel',
      email: 'info@slaterock.com',
      customFields: { 
        lastAccountingDate: '31/12/2017'
      }
  };
  var note = {
      author: 'boss@slaterock.com',
      content: 'Fred is a great worker'
  }
  var doc = {
      author: 'boss@slaterock.com',
      name: 'Annual review',
      url: 'http://slaterock.com/reviews/Fred.odt',
      favorite: true
  }
  var activity = {
      content: 'Annual review',
      type: 'test'
  }
  
  function getIdFromLocation(location) {
    var match = location.match(/\/(\d+)(?:$|\/)/);
    return match ? match[1] : null;
  }
  function accountUri(account) {
    return baseUrl + '/accounts/' + (account.id || getIdFromLocation(account.links && account.links[0] && account.links[0].href || ''));
  }
  function contactUri(contact) {
    return baseUrl + '/contacts/' + (contact.id || getIdFromLocation(contact.links && contact.links[0] && contact.links[0].href || ''));
  }
  
  beforeAll(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
  });

  it("searches to take an initial baseline", function(done) {
    Promise.all([
      fetch(baseUrl + '/contacts/').then(r => { contactsBefore = r.json ? r.json() : []; return r; }),
      fetch(baseUrl + '/accounts/').then(r => { accountsBefore = r.json ? r.json() : []; return r; })
    ]).then(function(responses) {
      expect(responses[0].status).toEqual(200);
      expect(responses[1].status).toEqual(200);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("creates a new account", function(done) {
    fetch(baseUrl + '/accounts/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(account)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/\/accounts\/[0-9]/);
      account.links = [ { rel: 'self', href: location } ];
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches updated accounts and checks the newly added one is correct", function(done) {
    fetch(baseUrl + '/accounts/')
      .then(r => r.json())
      .then(function(data) {
        accounts = data;
        data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
        expect(accounts.length).toEqual(accountsBefore.length+1);
        expect(accounts[0].name).toEqual(account.name);
        expect(accounts[0].email).toEqual(account.email);
        expect(accounts[0].firstContact).not.toBeNull();
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("adds a note to the account", function(done) {
    fetch(accountUri(account) + '/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(note)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/.*\/accounts\/[0-9]*\/notes\/[0-9]*/);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("adds a document link to the account", function(done) {
    fetch(accountUri(account) + '/documents', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(doc)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/.*\/accounts\/[0-9]*\/documents\/[0-9]*/);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("adds an activity to the account", function(done) {
    fetch(accountUri(account) + '/activities/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(activity)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/.*\/accounts\/[0-9]*\/activities\/[0-9]*/);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("sets the stage of the account", function(done) {
    fetch(accountUri(account) + '/stage/tested', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(activity)
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("updates the new account", function(done) {
    account.phone1 = '+44 7777 123456';
    account.phone2 = '020 7123 4567';
    account.customFields.parking = 'All private cars to be parked in bays and not obstructing Bronto-crane access';
    fetch(accountUri(account), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(account)
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches updated accounts and checks the newly updated one is correct", function(done) {
    fetch(baseUrl + '/accounts/')
      .then(r => r.json())
      .then(function(data) {
        accounts = data;
        data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
        expect(accounts.length).toEqual(accountsBefore.length+1);
        expect(accounts[0].phone1).toEqual(account.phone1);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("fetches just the updated account and checks it is correct", function (done) {
    fetch(accountUri(account))
      .then(r => r.json())
      .then(function(data) {
        expect(data.phone1).toEqual(account.phone1);
        expect(data.phone2).toEqual(account.phone2);
        expect(data.customFields.parking).toEqual(account.customFields.parking);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("adds a contact to the new account", function(done) {
    contact.accountId = getIdFromLocation(account.links[0].href);
    fetch(baseUrl + '/contacts/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contact)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/\/contacts\/[0-9]/);
      contact.links = [ { rel: 'self', href: location } ];
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches contacts for account and checks the newly added one holds correct information", function(done) {
    fetch(baseUrl + '/contacts/findByAccountId?accountId=' + getIdFromLocation(account.links[0].href))
      .then(r => r.json())
      .then(function(data) {
        data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
        expect(data.length).toEqual(1);
        expect(data[0].firstName).toEqual(contact.firstName);
        expect(data[0].firstContact).not.toBeNull();
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("fetches complete account inc. child entities and check all fields are correct", function(done) {
    fetch(accountUri(account))
      .then(r => r.json())
      .then(function(data) {
        expect(data.name).toEqual(account.name);
        expect(data.email).toEqual(account.email);
        expect(data.phone1).toEqual(account.phone1);
        expect(data.phone2).toEqual(account.phone2);
        expect(data.firstContact).not.toBeNull();
        expect(data.customFields).toBeDefined();
        expect(data.customFields.lastAccountingDate).toEqual(account.customFields.lastAccountingDate);
        expect(data.activities.length).toEqual(2);
        data.activities.sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
        expect(data.activities[0].type).toEqual('TRANSITION_TO_STAGE');
        expect(data.activities[0].content).toEqual('From null to tested');
        expect(data.activities[0].occurred).not.toBeNull();
        expect(data.activities[1].type).toEqual(activity.type);
        expect(data.activities[1].content).toEqual(activity.content);
        expect(data.activities[1].occurred).not.toBeNull();
        expect(data.notes.length).toEqual(1);
        expect(data.notes[0].author).toEqual(note.author);
        expect(data.documents.length).toEqual(1);
        expect(data.documents[0].author).toEqual(doc.author);
        expect(data.documents[0].name).toEqual(doc.name);
        expect(data.documents[0].url).toEqual(doc.url);
        expect(data.documents[0].confidential).toEqual(false);
        expect(data.documents[0].favorite).toEqual(doc.favorite);
        expect(data.documents[0].created).not.toBeNull();
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("deletes the added contact", function(done) {
    fetch(contactUri(contact), {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("deletes the added account", function(done) {
    fetch(accountUri(account), {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("checks the data is the same as the baseline", function(done) {
    Promise.all([
      fetch(baseUrl + '/contacts/').then(r => r.json()),
      fetch(baseUrl + '/accounts/').then(r => r.json())
    ]).then(function([contactsData, accountsData]) {
      contacts = contactsData;
      accounts = accountsData;
      contacts.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      expect(contacts.length).toEqual(contactsBefore.length);
      expect(accounts.length).toEqual(accountsBefore.length);
      done();
    }).catch(function(e) { fail(e); done(); });
  });
  
  afterAll(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
