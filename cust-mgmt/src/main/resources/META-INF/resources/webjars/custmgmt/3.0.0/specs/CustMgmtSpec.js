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
describe("Contact Management API", function() {
  var tenantId = 'acme';
  var $rh = new RestEntityHelper({
    server: window['$env'] == undefined ? 'http://localhost:8080' : $env.server,
    tenantId: tenantId
  });

  var originalTimeout;
  var accountsBefore = [];
  var contactsBefore = [];
  var contacts = [];
  var contact = {
      firstName: 'Fred',
      lastName: 'Flintstone',
      email: 'fred@slaterock.com',
      customFields: {
        dateOfBirth: '01/01/1970'
      }
  };
  var account = {
      name: 'Bedrock Slate and Gravel'
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

  beforeEach(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  it("searches to take an initial baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/contacts/',  function(data, textStatus, jqXHR) {
      contactsBefore = data;
      expect(jqXHR.status).toEqual(200);

      $rh.getJSON('/'+tenantId+'/accounts/',  function(data, textStatus, jqXHR) {
        accountsBefore = data;
        expect(jqXHR.status).toEqual(200);
        done();
      });
    });

  });

  it("creates a new contact", function(done) {
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
        done();
      }
    });
  });

  it("fetches updated contacts and checks the newly added one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/contacts/',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });

      expect(contacts.length).toEqual(contactsBefore.length+1);
      //console.log('latest contact: '+JSON.stringify(contacts[0]));
      expect($rh.uri(contacts[0])).toEqual($rh.uri(contact));
      expect(contacts[0].firstName).toEqual(contact.firstName);
      expect(contacts[0].lastName).toEqual(contact.lastName);
      expect(contacts[0].email).toEqual(contact.email);

      done();
    });
  });

  it("adds a note to the contact", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(contact)+'/notes/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(note),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/contacts\/[0-9]*\/notes\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("adds a document link to the contact", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(contact)+'/documents/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(doc),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/contacts\/[0-9]*\/documents\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("adds an activity to the contact", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(contact)+'/activities/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(activity),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/contacts\/[0-9]*\/activities\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("sets the stage of the contact", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(contact)+'/stage/tested',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(activity),
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("updates the new contact", function(done) {
    contact.phone1 = '+44 7777 123456';
    contact.phone2 = '+44 7777 123456';
    contact.customFields.dateOfBirth = '31/12/1969';
    $rh.ajax({
      url: $rh.tenantUri(contact),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(contact),
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("fetches updated contacts and checks the newly updated one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/contacts/',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });

      expect(contacts.length).toEqual(contactsBefore.length+1);
      expect($rh.uri(contacts[0])).toEqual($rh.uri(contact));
      expect(contacts[0].phone1).toEqual(contact.phone1);
      // phone2 not returned in summary

      done();
    });
  });

  it("adds an account to the new contact", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/accounts/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(account),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/accounts\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        account.links = [ { rel: 'self', href: location } ];

        var contactAccountLink = $rh.uri(contact)+'/account';
        $rh.ajax({
          url: contactAccountLink,
          type: 'PUT',
          contentType: 'text/uri-list',
          data: location,
          success: function(data, textStatus, jqXHR) {
            console.log('linked account: '+location+' to '+contactAccountLink);
            done();
          }
        });
      }
    });
  });

  it("fetches updated contacts and checks the newly added one holds correct account information", function(done) {
    $rh.getJSON('/'+tenantId+'/contacts/',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });

      expect(contacts.length).toEqual(contactsBefore.length+1);
      //console.log('latest contact: '+JSON.stringify(contacts[0]));
      expect($rh.uri(contacts[0])).toEqual($rh.uri(contact));
      expect(contacts[0].firstName).toEqual(contact.firstName);
      expect(contacts[0].lastName).toEqual(contact.lastName);
      expect(contacts[0].email).toEqual(contact.email);
      expect(contacts[0].accountName).toEqual(account.name);

      done();
    });
  });

  it("fetches complete contact inc. child entities and check all fields are correct", function(done) {
    console.log('tenantUri: '+$rh.tenantUri(contact));
    $rh.getJSON($rh.tenantUri(contact),  function( data ) {

      expect($rh.localId(data)).toEqual($rh.localId(contact));
      expect(data.firstName).toEqual(contact.firstName);
      expect(data.lastName).toEqual(contact.lastName);
      expect(data.email).toEqual(contact.email);
      expect(data.phone1).toEqual(contact.phone1);
      expect(data.phone2).toEqual(contact.phone2);
      expect(data.stage).toEqual('tested');
      expect(data.firstContact).toBeDefined();
      expect(data.customFields).toBeDefined();
      expect(data.customFields.dateOfBirth).toEqual(contact.customFields.dateOfBirth);

      expect(data.account.name).toEqual(account.name);
      expect(data.account.firstContact).toBeDefined();

      expect(data.activities.length).toEqual(2);
      expect(data.activities[0].type).toEqual(activity.type);
      expect(data.activities[0].content).toEqual(activity.content);
      expect(data.activities[1].type).toEqual('TRANSITION_TO_STAGE');
      expect(data.activities[1].content).toEqual('From null to tested');

      expect(data.notes.length).toEqual(1);
      expect(data.notes[0].author).toEqual(note.author);
      expect(data.notes[0].name).toEqual(note.name);

      expect(data.documents.length).toEqual(1);
      expect(data.documents[0].author).toEqual(doc.author);
      expect(data.documents[0].name).toEqual(doc.name);
      expect(data.documents[0].url).toEqual(doc.url);
      expect(data.documents[0].confidential).toEqual(false); // default value
      expect(data.documents[0].favorite).toEqual(doc.favorite); // defaults to false

      done();
    });
  });

  it("deletes the added contact and account", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(contact),
      type: 'DELETE',
      contentType: 'application/json',
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("checks the data is the same as the baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/contacts/',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });

      expect(contacts.length).toEqual(contactsBefore.length);
    });

    $rh.getJSON('/'+tenantId+'/accounts/',  function(data, textStatus, jqXHR) {
      accounts = data;
      expect(jqXHR.status).toEqual(200);

      expect(accounts.length).toEqual(accountsBefore.length);
      done();
    });
  });

  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
