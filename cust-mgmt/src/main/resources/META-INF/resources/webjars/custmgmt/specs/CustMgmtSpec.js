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
describe("Contact management", function() {
  var tenantId = 'acme';
  var $rh = new RestEntityHelper({
    server: window['$env'] == undefined ? 'http://localhost:8080' : $env.server,
    tenantId: tenantId
  });

  var originalTimeout;
  var tenantId = 'client1';
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
  
  beforeEach(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
    
    $re = new RestEntityHelper();
  });

  it("searches to take an initial baseline", function(done) {
    $.getJSON('/'+tenantId+'/contacts/?projection=complete',  function(data, textStatus, jqXHR) {
      contactsBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
    $.getJSON('/'+tenantId+'/accounts/?projection=complete',  function(data, textStatus, jqXHR) {
      accountsBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
  });

  it("saves a new contact", function(done) {
    $.ajax({
      url: '/'+tenantId+'/contacts/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(contact),
      success: completeHandler = function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/contacts\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        contact.links = [ { rel: 'self', href: location } ];
        done();
      }
    });
  });
  
  it("fetches updated contacts and checks the newly added one is correct", function(done) {
    $.getJSON('/'+tenantId+'/contacts/?projection=complete',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      
      expect(contacts.length).toEqual(contactsBefore.length+1);
      //console.log('latest contact: '+JSON.stringify(contacts[0]));
      expect($re.id(contacts[0])).toEqual($re.id(contact));
      expect(contacts[0].firstName).toEqual(contact.firstName);
      expect(contacts[0].lastName).toEqual(contact.lastName);
      expect(contacts[0].email).toEqual(contact.email);

      done();
    });
  });

  it("updates the new contact", function(done) {
    contact.phone1 = '+44 77777 123456';
    contact.phone2 = '+44 20 7123 4567';
    contact.customFields.dateOfBirth = '31/12/1969';
    $.ajax({
      url: '/'+tenantId+'/contacts/'+$re.id(contact),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(contact),
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });
  
  it("fetches updated contacts and checks the newly added one is correct", function(done) {
    $.getJSON('/'+tenantId+'/contacts/?projection=complete',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      
      expect(contacts.length).toEqual(contactsBefore.length+1);
      expect($re.id(contacts[0])).toEqual($re.id(contact));
      expect(contacts[0].phone1).toEqual(contact.phone1);
      expect(contacts[0].phone2).toEqual(contact.phone2);

      done();
    });
  });
  
  it("adds an account to the new contact", function(done) {
    account.contact = [ $re.uri(contact) ];
    $.ajax({
      url: '/'+tenantId+'/accounts/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(account),
      success: completeHandler = function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/accounts\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        account.links = [ { rel: 'self', href: location } ];
        
        var contactAccountLink = $re.uri(contact)+'/account';
        $.ajax({
          url: contactAccountLink,
          type: 'PUT',
          contentType: 'text/uri-list',
          data: location,
          success: completeHandler = function(data, textStatus, jqXHR) {
            console.log('linked account: '+location+' to '+contactAccountLink);
            done();
          }
        });
      }
    });
  });
  
  it("fetches updated contacts and checks the newly added one holds correct account information", function(done) {
    $.getJSON('/'+tenantId+'/contacts/?projection=complete',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      
      expect(contacts.length).toEqual(contactsBefore.length+1);
      //console.log('latest contact: '+JSON.stringify(contacts[0]));
      expect($re.id(contacts[0])).toEqual($re.id(contact));
      expect(contacts[0].firstName).toEqual(contact.firstName);
      expect(contacts[0].lastName).toEqual(contact.lastName);
      expect(contacts[0].email).toEqual(contact.email);
      expect(contacts[0].account.name).toEqual(account.name);

      done();
    });
  });

<<<<<<< HEAD:cust-mgmt/src/main/resources/static/tests/spec/CustMgmtSpec.js
  it("fetches complete contact inc. account and check correct", function(done) {
    $.getJSON('/contacts/'+$re.id(contact)+'?projection=complete',  function( data ) {
=======
  it("fetches complete contact inc. child entities and check all fields are correct", function(done) {
	var uri = $rh.tenantUri(contact);
    console.log('tenantUri: '+uri);
    expect(uri).toBeDefined();
    $rh.getJSON(uri,  function( data ) {
>>>>>>> 49f3efbb (upgrade to spring boot 2.4.3 to api test passing):cust-mgmt/src/main/resources/META-INF/resources/webjars/custmgmt/3.0.0/specs/CustMgmtSpec.js

      console.log('  contact: '+JSON.stringify(data));
      
      expect($re.id(data)).toEqual($re.id(contact));
      expect(data.firstName).toEqual(contact.firstName);
      expect(data.lastName).toEqual(contact.lastName);
      expect(data.email).toEqual(contact.email);
      expect(data.phone1).toEqual(contact.phone1);
      expect(data.phone2).toEqual(contact.phone2);
      expect(data.firstContact).toBeDefined();
      expect(data.customFields).toBeDefined();
      expect(data.customFields.dateOfBirth).toEqual(contact.customFields.dateOfBirth);
      
      expect(data.account.name).toEqual(account.name);
      expect(data.account.firstContact).toBeDefined();
<<<<<<< HEAD:cust-mgmt/src/main/resources/static/tests/spec/CustMgmtSpec.js
      
=======

      expect(data.activities.length).toEqual(3);
      expect(data.activities[0].type).toEqual(activity.type);
      expect(data.activities[0].content).toEqual(activity.content);
      expect(data.activities[1].type).toEqual('TRANSITION_TO_STAGE');
      expect(data.activities[1].content).toEqual('From null to tested');
      expect(data.activities[2].type).toEqual('LINK_ACCOUNT_TO_CONTACT');
      expect(data.activities[2].content).toMatch(/Linked account [0-9]* to contact [0-9]*/);

      expect(data.notes.length).toEqual(1);
      expect(data.notes[0].author).toEqual(note.author);
      expect(data.notes[0].name).toEqual(note.name);

      expect(data.documents.length).toEqual(1);
      expect(data.documents[0].author).toEqual(doc.author);
      expect(data.documents[0].name).toEqual(doc.name);
      expect(data.documents[0].url).toEqual(doc.url);
      expect(data.documents[0].confidential).toEqual(false); // default value
      expect(data.documents[0].favorite).toEqual(doc.favorite); // defaults to false

>>>>>>> 49f3efbb (upgrade to spring boot 2.4.3 to api test passing):cust-mgmt/src/main/resources/META-INF/resources/webjars/custmgmt/3.0.0/specs/CustMgmtSpec.js
      done();
    });
  });

  it("deletes the added contact and account", function(done) {
    $.ajax({
      url: '/'+tenantId+'/contacts/'+$re.id(contact),
      type: 'DELETE',
      contentType: 'application/json',
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });
  
  it("checks the data is the same as the baseline", function(done) {
    $.getJSON('/'+tenantId+'/contacts/?projection=complete',  function( data ) {
      contacts = data;
      data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
      
      expect(contacts.length).toEqual(contactsBefore.length);
    });
    
    $.getJSON('/'+tenantId+'/accounts/?projection=complete',  function(data, textStatus, jqXHR) {
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
