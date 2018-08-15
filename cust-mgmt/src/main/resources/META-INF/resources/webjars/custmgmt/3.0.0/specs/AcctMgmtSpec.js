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
describe("Account Management API", function() {
  var tenantId = 'acme';
  var $rh = new RestEntityHelper({
    server: window['$env'] == undefined ? 'http://localhost:8080' : $env.server,
    tenantId: tenantId
  });
  
  var originalTimeout;
  var accountsBefore = [];
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
  
  beforeEach(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  it("searches to take an initial baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/contacts/',  function(data, textStatus, jqXHR) {
      contactsBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
    $rh.getJSON('/'+tenantId+'/accounts/',  function(data, textStatus, jqXHR) {
      accountsBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
  });

  it("creates a new account", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/accounts/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(account),
      success: completeHandler = function(data, textStatus, jqXHR) {
        // NOTE this is URI not tenantUri
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/accounts\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        account.tenant = tenantId;
        account.links = [ { rel: 'self', href: location } ];
        done();
      }
    });
  });
  
  it("fetches updated accounts and checks the newly added one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/accounts/',  function( data ) {
      accounts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      
      expect(accounts.length).toEqual(accountsBefore.length+1);
      // returns /accounts/xxx rather than http://host/tenant/accounts/xxx
      expect($rh.uri(account).endsWith($rh.uri(accounts[0])));
      expect(accounts[0].name).toEqual(account.name);
      expect(accounts[0].email).toEqual(account.email);

      done();
    });
  });

  it("adds a note to the account", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(account)+'/notes',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(note),
      success: completeHandler = function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/accounts\/[0-9]*\/notes\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("adds a document link to the account", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(account)+'/documents',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(doc),
      success: completeHandler = function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/accounts\/[0-9]*\/documents\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("adds an activity to the account", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(account)+'/activities/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(activity),
      success: completeHandler = function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/.*\/accounts\/[0-9]*\/activities\/[0-9]*/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

// TODO not yet implemented
//  it("sets the stage of the account", function(done) {
//    $rh.ajax({
//      url: $rh.tenantUri(account)+'/stage/tested',
//      type: 'POST',
//      contentType: 'application/json',
//      data: JSON.stringify(activity),
//      success: completeHandler = function(data, textStatus, jqXHR) {
//        expect(jqXHR.status).toEqual(200);
//        done();
//      }
//    });
//  });
  
  it("updates the new account", function(done) {
    account.phone1 = '+44 7777 123456';
    account.phone2 = '+44 7777 123456';
    account.customFields.lastAccountingDate = '31/12/2017';
    $rh.ajax({
      url: $rh.tenantUri(account),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(account),
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });
  
  it("fetches updated accounts and checks the newly updated one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/accounts/',  function( data ) {
      accounts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      
      expect(accounts.length).toEqual(accountsBefore.length+1);
      expect($rh.uri(account)).toContain($rh.uri(accounts[0]));
      expect(accounts[0].phone1).toEqual(account.phone1);
      // phone2 not returned in summary

      done();
    });
  });
  
//  it("adds a contact to the new account", function(done) {
////    contact.accountId = $rh.localId(account);
//    $rh.ajax({
//      url: '/'+tenantId+'/contacts/',
//      type: 'POST',
//      contentType: 'application/json',
//      data: JSON.stringify(contact),
//      success: completeHandler = function(data, textStatus, jqXHR) {
//        // NOTE this is URI not tenantUri
//        var location = jqXHR.getResponseHeader('Location');
//        expect(location).toMatch(/\/contacts\/[0-9]/);
//        expect(jqXHR.status).toEqual(201);
//        contact.links = [ { rel: 'self', href: location } ];
//        done();
//      }
//    });
//  });
  
//  it("fetches contacts for account and checks the newly added one holds correct information", function(done) {
//    $rh.getJSON('/'+tenantId+'/contacts/findByAccountId?accountId='+$rh.localId(account),  function( data ) {
//      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
//      
//      expect(data.length).toEqual(1);
//      expect(data[0].firstName).toEqual(contact.firstName);
//
//      done();
//    });
//  });
  
  it("fetches complete account inc. child entities and check all fields are correct", function(done) {
    $rh.getJSON($rh.tenantUri(account),  function( data ) {

      console.log('  account: '+JSON.stringify(data));
      
      expect($rh.localId(data)).toEqual($rh.localId(account));
      expect(data.name).toEqual(account.name);
      expect(data.email).toEqual(account.email);
      expect(data.phone1).toEqual(account.phone1);
      expect(data.phone2).toEqual(account.phone2);
      expect(data.firstContact).toBeDefined();
      expect(data.customFields).toBeDefined();
      expect(data.customFields.lastAccountingDate).toEqual(account.customFields.lastAccountingDate);
      
      expect(data.activities.length).toEqual(1);
      expect(data.activities[0].type).toEqual(activity.type);
      expect(data.activities[0].content).toEqual(activity.content);
// Not yet implemented
//      expect(data.activities[1].type).toEqual('TRANSITION_TO_STAGE');
//      expect(data.activities[1].content).toEqual('From null to tested');
      
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

//  it("deletes the added contact", function(done) {
//    $rh.ajax({
//      url: $rh.tenantUri(contact),
//      type: 'DELETE',
//      contentType: 'application/json',
//      success: completeHandler = function(data, textStatus, jqXHR) {
//        expect(jqXHR.status).toEqual(204);
//        done();
//      }
//    });
//  });
  
  it("deletes the added account", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(account),
      type: 'DELETE',
      contentType: 'application/json',
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

//  it("checks the data is the same as the baseline", function(done) {
//    $rh.getJSON('/'+tenantId+'/contacts/',  function( data ) {
//      contacts = data;
//      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
//      
//      expect(contacts.length).toEqual(contactsBefore.length);
//    });
//    
//    $rh.getJSON('/'+tenantId+'/accounts/',  function(data, textStatus, jqXHR) {
//      accounts = data;
//      expect(jqXHR.status).toEqual(200);
//
//      expect(accounts.length).toEqual(accountsBefore.length);
//      done();
//    });
//  });
  
  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
