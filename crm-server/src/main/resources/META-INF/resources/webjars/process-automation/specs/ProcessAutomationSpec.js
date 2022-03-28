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
  var contactsBefore = [];
  var contacts = [];
  var contactOnlyEnquiry = {
      fullName: 'Barney Rubble',
      email: 'barney@slaterock.com',
      message: 'Hi,\nI need to hire an extra bronto-crane.\nDo you have one available?\nBarney',
      tenantId: tenantId,
      ip: "10.192.168.100",
      admin_email: "info@knowprocess.com",
      source: "jasmine test harness",
      customFields: {
        dateOfBirth: '01/01/1970'
      }
  };
  var contactAndAccountEnquiry = {
      fullName: 'Barney Rubble',
      account: {
        name: account.name,
      },
      email: 'barney@slaterock.com',
      message: 'Hi,\nI need to hire an extra bronto-crane.\nDo you have one available?\nBarney',
      tenantId: tenantId,
      ip: "10.192.168.100",
      admin_email: "info@knowprocess.com",
      source: "jasmine test harness",
      customFields: {
        dateOfBirth: '01/01/1970'
      }
  };

  var processCreds = 'rest-admin:g5LkZSpZXzF8V4FIyPVOey5y';

  beforeAll(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 15000;
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

  it("submits a new enquiry message containing contact info only", function(done) {
    var requestUrl = server+"/msg/"+tenantId+"/kp.enquiry.json";
    fetch(requestUrl, {
      "headers": {
          "Accept": "*/*",
          "Authorization": "Basic "+btoa(processCreds),
          "Content-Type": "application/json"
      },
      "method": "POST",
      "mode": "cors",
      "body": JSON.stringify(contactOnlyEnquiry)
    })
    .then(response => {
      console.log(response);
      expect(response.status).toEqual(201);
      expect(response.headers.get('Location')).toMatch(/.*\/runtime\/process-instances\/[-a-f0-9]{36}/);
      const location = response.headers.get('Location');
      procInstContactOnly = {
        uri: location,
        id: location.substring(location.lastIndexOf('/')+1),
        server: location.substring(0,location.indexOf('/runtime')),
        msg: location.match(/[-a-f0-9]{36}/)
      }
      done();
    });
  });

  it("fetches created contact and child entities and check all fields are correct", function(done) {
    setTimeout(function () {
      // should be complete by now
      fetch(procInstContactOnly.server+'/query/historic-variable-instances', {
        "headers": {
          "Accept": "application/json",
          "Authorization": "Basic "+btoa(processCreds),
          "Content-Type": "application/json",
        },
        "method": "POST",
        "mode": "cors",
        "body": JSON.stringify({ "processInstanceId": procInstContactOnly.id })
      })
      .then(response => {
        console.log(response);
        expect(response.status).toEqual(200);
        return response.json();
      })
      .then(resp => {
        const contactIdArr = resp.data.filter(x => x.variable.name == 'contactId');
        expect(contactIdArr.length).toEqual(1);
        contactUri = contactIdArr[0].variable.value;
        console.log('  created contact: '+contactUri);
        contactOnlyEnquiry.id = parseInt($rh.localId(contactUri));

        const acctIdArr = resp.data.filter(x => x.variable.name == 'accountId');
        expect(acctIdArr.length).toEqual(1);
        acctUri = acctIdArr[0].variable.value;
        expect(acctIdArr[0].variable.value).toBeNull();;

        $rh.getJSON('/'+tenantId+'/contacts/'+contactOnlyEnquiry.id, function( data ) {
          expect(data.id).toEqual(contactOnlyEnquiry.id);
          expect(data.firstName).toEqual('Barney');
          expect(data.lastName).toEqual('Rubble');
          expect(data.email).toEqual(contactOnlyEnquiry.email);
          expect(data.source).toEqual(contactOnlyEnquiry.source);
          expect(data.firstContact).toBeDefined();
          expect(data.customFields).toBeDefined();
          expect(data.customFields.dateOfBirth).toEqual(contactOnlyEnquiry.customFields.dateOfBirth);

          expect(data.notes.length).toEqual(1);
          expect(data.notes[0].content).toEqual(contactAndAccountEnquiry.message.replace(/\n/g,'<br/>'));

          expect(data.activities.length).toEqual(1);
          data.activities.sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
          expect(data.activities[0].type).toEqual('Enquiry');
          expect(data.activities[0].content).toEqual('See notes');

          done();
        });
      });
    }, 5500);
  });

  it("submits a new enquiry message containing contact and account info", function(done) {
    var requestUrl = server+"/msg/"+tenantId+"/kp.enquiry.json";
    fetch(requestUrl, {
      "headers": {
          "Accept": "*/*",
          "Authorization": "Basic "+btoa(processCreds),
          "Content-Type": "application/json"
      },
      "method": "POST",
      "mode": "cors",
      "body": JSON.stringify(contactAndAccountEnquiry)
    })
    .then(response => {
      console.log(response);
      expect(response.status).toEqual(201);
      expect(response.headers.get('Location')).toMatch(/.*\/runtime\/process-instances\/[-a-f0-9]{36}/);
      const location = response.headers.get('Location');
      procInstContactAndAccount = {
        uri: location,
        id: location.substring(location.lastIndexOf('/')+1),
        server: location.substring(0,location.indexOf('/runtime')),
        msg: location.match(/[-a-f0-9]{36}/)
      }
      done();
    });
  });

  it("fetches created contact, account and child entities and check all fields are correct", function(done) {
    setTimeout(function () {
      // should be complete by now
      fetch(procInstContactAndAccount.server+'/query/historic-variable-instances', {
        "headers": {
          "Accept": "application/json",
          "Authorization": "Basic "+btoa(processCreds),
          "Content-Type": "application/json",
        },
        "method": "POST",
        "mode": "cors",
        "body": JSON.stringify({ "processInstanceId": procInstContactAndAccount.id })
      })
      .then(response => {
        console.log(response);
        expect(response.status).toEqual(200);
        return response.json();
      })
      .then(resp => {
        const contactIdArr = resp.data.filter(x => x.variable.name == 'contactId');
        expect(contactIdArr.length).toEqual(1);
        contactUri = contactIdArr[0].variable.value;
        console.log('  created contact: '+contactUri);
        contactAndAccountEnquiry.id = parseInt($rh.localId(contactUri));

        const acctIdArr = resp.data.filter(x => x.variable.name == 'accountId');
        expect(acctIdArr.length).toEqual(1);
        acctUri = acctIdArr[0].variable.value;
        console.log('  created acct: '+acctUri);
        contactAndAccountEnquiry.account.id = parseInt($rh.localId(acctUri));

        $rh.getJSON('/'+tenantId+'/contacts/'+contactAndAccountEnquiry.id, function( data ) {
          expect(data.id).toEqual(contactAndAccountEnquiry.id);
          expect(data.firstName).toEqual('Barney');
          expect(data.lastName).toEqual('Rubble');
          expect(data.email).toEqual(contactAndAccountEnquiry.email);
          expect(data.source).toEqual(contactAndAccountEnquiry.source);
          expect(data.firstContact).toBeDefined();
          expect(data.customFields).toBeDefined();
          expect(data.customFields.dateOfBirth).toEqual(contactAndAccountEnquiry.customFields.dateOfBirth);

          expect(data.account.name).toEqual(contactAndAccountEnquiry.account.name);
          expect(data.account.id).toEqual(contactAndAccountEnquiry.account.id);

          expect(data.activities.length).toEqual(1);
          data.activities.sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
          expect(data.activities[0].type).toEqual('LINK_ACCOUNT_TO_CONTACT');
          expect(data.activities[0].content).toMatch(/Linked account [0-9]* to contact [0-9]*/);

          // note will be attached to account not contact

          $rh.getJSON('/'+tenantId+'/accounts/'+contactAndAccountEnquiry.account.id, function( data ) {
            expect(data.id).toEqual(contactAndAccountEnquiry.account.id);
            expect(data.name).toEqual(contactAndAccountEnquiry.account.name);

            expect(data.notes.length).toEqual(1);
            expect(data.notes[0].content).toEqual(contactAndAccountEnquiry.message.replace(/\n/g,'<br/>'));

            expect(data.activities.length).toEqual(1);
            data.activities.sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
            expect(data.activities[0].type).toEqual('Enquiry');
            expect(data.activities[0].content).toEqual('See notes');

            done();
          });
        });
      });
    }, 5500);
  });

  it("deletes the added contact", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/contacts/'+contactOnlyEnquiry.id,
      type: 'DELETE',
      contentType: 'application/json',
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("deletes the added contact and account", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/contacts/'+contactAndAccountEnquiry.id,
      type: 'DELETE',
      contentType: 'application/json',
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        $rh.ajax({
          url: '/'+tenantId+'/accounts/'+contactAndAccountEnquiry.account.id,
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
