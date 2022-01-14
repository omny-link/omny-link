/*******************************************************************************
 * Copyright 2015-2021 Tim Stephenson and contributors
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
describe("Process automation", function() {
  var tenantId = 'acme';
  var server = window['$env'] == undefined ? 'http://localhost:8080' : $env.server;
  var $rh = new RestEntityHelper({
    server: server,
    tenantId: tenantId
  });

  var originalTimeout;
  var account = {
      name: 'Bedrock Slate and Gravel'
  };
  var accountsBefore = [];
  var activity = {
      content: 'New enquiry',
      type: 'TEST'
  }
  var contactsBefore = [];
  var contacts = [];
  var enquiry = {
      fullName: 'Barney Rubble',
      account: {
        name: account.name,
      },
      email: 'barney@slaterock.com',
      message: 'Hi,\nI need to hire an extra bronto-crane.\nDo you have one available?\nBarney',
      tenantId: tenantId,
      ip: "10.192.168.100",
      admin_email: "info@knowprocess.com",
      customFields: {
        dateOfBirth: '01/01/1970'
      }
  };
  var contact = {
      fullName: 'Barney Rubble',
      email: 'barney@slaterock.com',
      tenantId: tenantId,
      customFields: {
        dateOfBirth: '01/01/1970',
        ip: "10.192.168.100"
      }
  };
  var processCreds = 'rest-admin:g5LkZSpZXzF8V4FIyPVOey5y';

  beforeAll(function() {
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

  it("submits a new task", function(done) {
    var requestUrl = server+"/form/"+tenantId+"/SimpleToDo.action";
    fetch(requestUrl, {
      "credentials": "include",
      "headers": {
          "Accept": "*/*",
          "Content-Type": "application/x-www-form-urlencoded",
          "Authorization": "Basic "+btoa(processCreds)
      },
      "body": "businessKey=Add task to/for Cobblestone County High"
        + "&name=SimpleToDo 23380"
        + "&initiator=info@knowprocess.com"
        + "&tenantId="+tenantId+"&accountId=23380"
        + "&what=STUFF&when=2021-11-22&who=tim@knowprocess.com",
      "method": "POST",
      "mode": "cors"
    })
    .then(response => {
      console.log(response);
      expect(response.status).toEqual(201);
      expect(response.headers.get('Location')).toMatch(/.*\/runtime\/process-instances\/[-a-f0-9]{36}/);
      procInstTask = response.headers.get('Location').match(/[-a-f0-9]{36}/);
      done();
    });
  });

  it("submits a new enquiry message", function(done) {
    var requestUrl = server+"/msg/"+tenantId+"/kp.enquiry.json";
    fetch(requestUrl, {
      "headers": {
          "Accept": "*/*",
          "Authorization": "Basic "+btoa(processCreds),
          "Content-Type": "application/json"
      },
      "method": "POST",
      "mode": "cors",
      "body": JSON.stringify(enquiry)
    })
    .then(response => {
      console.log(response);
      expect(response.status).toEqual(201);
      expect(response.headers.get('Location')).toMatch(/.*\/runtime\/process-instances\/[-a-f0-9]{36}/);
      procInstUri = response.headers.get('Location');
      procInstMsg = response.headers.get('Location').match(/[-a-f0-9]{36}/);
      done();
    });
  });

  it("fetches complete contact inc. child entities and check all fields are correct", function(done) {
    setTimeout(function () {

      fetch(procInstUri+'/variables', {
        "headers": {
          "Accept": "application/json",
          "Authorization": "Basic "+btoa(processCreds)
        },
        "method": "GET",
        "mode": "cors"
      })
      .then(response => {
        console.log(response);
        expect(response.status).toEqual(200);
        return response.json();
      })
      .then(data => {
        console.log(data);

        const contactIdArr = data.filter(variable => variable.name == 'contactId');
        expect(contactIdArr.length).toEqual(1);
        contactUri = contactIdArr[0].value;
        console.log('  created contact: '+contactUri);
        contact.id = parseInt($rh.localId(contactUri));

        const acctIdArr = data.filter(variable => variable.name == 'accountId');
        expect(acctIdArr.length).toEqual(1);
        acctUri = acctIdArr[0].value;
        console.log('  created acct: '+acctUri);
        account.id = parseInt($rh.localId(acctUri));

        $rh.getJSON('/'+tenantId+'/contacts/'+contact.id, function( data ) {
          expect(data.id).toEqual(contact.id);
          expect(data.firstName).toEqual('Barney');
          expect(data.lastName).toEqual('Rubble');
          expect(data.email).toEqual(enquiry.email);
          expect(data.firstContact).toBeDefined();
          expect(data.customFields).toBeDefined();
          expect(data.customFields.dateOfBirth).toEqual(enquiry.customFields.dateOfBirth);

          expect(data.account.name).toEqual(enquiry.accountName);
          expect(data.account.id).toEqual(account.id);

//          expect(data.activities.length).toEqual(3);
//          data.activities.sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });

//          expect(data.activities[0].type).toEqual('LINK_ACCOUNT_TO_CONTACT');
//          expect(data.activities[0].content).toMatch(/Linked account [0-9]* to contact [0-9]*/);
//          expect(data.activities[1].type).toEqual('TRANSITION_TO_STAGE');
//          expect(data.activities[1].content).toEqual('From null to tested');
//          expect(data.activities[2].type).toEqual(activity.type);
//          expect(data.activities[2].content).toEqual(activity.content);

//          expect(data.notes.length).toEqual(1);
//          expect(data.notes[0].author).toEqual(note.author);
//          expect(data.notes[0].name).toEqual(note.name);

//          expect(data.documents.length).toEqual(1);
//          expect(data.documents[0].author).toEqual(doc.author);
//          expect(data.documents[0].name).toEqual(doc.name);
//          expect(data.documents[0].url).toEqual(doc.url);
//          expect(data.documents[0].confidential).toEqual(false); // default value
//          expect(data.documents[0].favorite).toEqual(doc.favorite); // defaults to false

          done();
        });
      });
    }, 5500);
  });

  it("deletes the added contact and account", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/contacts/'+contact.id,
      type: 'DELETE',
      contentType: 'application/json',
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        $rh.ajax({
          url: '/'+tenantId+'/accounts/'+account.id,
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

  afterAll(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
