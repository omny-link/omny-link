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
describe("Process automation", function() {
  const tenantId = 'acme';
  const server = (typeof $env === 'undefined' || !$env) ? 'http://localhost:8082' : $env.server;
  const baseUrl = `${server}/${tenantId}`;
  const originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;

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

  var processCreds = 'rest-admin:secret';

  beforeAll(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 15000;
  });

  it("searches to take an initial baseline", async function() {
    const contactsRes = await fetch(`${server}/${tenantId}/contacts/`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    contactsBefore = await contactsRes.json();
    expect(contactsRes.status).toEqual(200);

    const accountsRes = await fetch(`${server}/${tenantId}/accounts/`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    accountsBefore = await accountsRes.json();
    expect(accountsRes.status).toEqual(200);
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

  it("fetches created contact and child entities and check all fields are correct", async function() {
    await new Promise(resolve => setTimeout(resolve, 5500));
    // should be complete by now
    const resp = await fetch(procInstContactOnly.server + '/query/historic-variable-instances', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Basic ' + btoa(processCreds),
        'Content-Type': 'application/json',
      },
      method: 'POST',
      mode: 'cors',
      body: JSON.stringify({ processInstanceId: procInstContactOnly.id })
    });
    expect(resp.status).toEqual(200);
    const respJson = await resp.json();
    const contactIdArr = respJson.data.filter(x => x.variable.name == 'contactId');
    expect(contactIdArr.length).toEqual(1);
    const contactUri = contactIdArr[0].variable.value;
    contactOnlyEnquiry.id = parseInt(contactUri.substring(contactUri.lastIndexOf('/') + 1));

    const acctIdArr = respJson.data.filter(x => x.variable.name == 'accountId');
    expect(acctIdArr.length).toEqual(1);
    const acctUri = acctIdArr[0].variable.value;
    expect(acctUri).toBeNull();

    const contactRes = await fetch(`${server}/${tenantId}/contacts/${contactOnlyEnquiry.id}`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    const data = await contactRes.json();
    expect(data.id).toEqual(contactOnlyEnquiry.id);
    expect(data.firstName).toEqual('Barney');
    expect(data.lastName).toEqual('Rubble');
    expect(data.email).toEqual(contactOnlyEnquiry.email);
    expect(data.source).toEqual(contactOnlyEnquiry.source);
    expect(data.firstContact).toBeDefined();
    expect(data.customFields).toBeDefined();
    expect(data.customFields.dateOfBirth).toEqual(contactOnlyEnquiry.customFields.dateOfBirth);
    expect(data.notes.length).toEqual(1);
    expect(data.notes[0].content).toEqual(contactAndAccountEnquiry.message.replace(/\n/g, '<br/>'));
    expect(data.activities.length).toEqual(1);
    data.activities.sort((a, b) => new Date(b.occurred) - new Date(a.occurred));
    expect(data.activities[0].type).toEqual('Enquiry');
    expect(data.activities[0].content).toEqual('See notes');
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

  it("fetches created contact, account and child entities and check all fields are correct", async function() {
    await new Promise(resolve => setTimeout(resolve, 5500));
    // should be complete by now
    const resp = await fetch(procInstContactAndAccount.server + '/query/historic-variable-instances', {
      headers: {
        'Accept': 'application/json',
        'Authorization': 'Basic ' + btoa(processCreds),
        'Content-Type': 'application/json',
      },
      method: 'POST',
      mode: 'cors',
      body: JSON.stringify({ processInstanceId: procInstContactAndAccount.id })
    });
    expect(resp.status).toEqual(200);
    const respJson = await resp.json();
    const contactIdArr = respJson.data.filter(x => x.variable.name == 'contactId');
    expect(contactIdArr.length).toEqual(1);
    const contactUri = contactIdArr[0].variable.value;
    contactAndAccountEnquiry.id = parseInt(contactUri.substring(contactUri.lastIndexOf('/') + 1));

    const acctIdArr = respJson.data.filter(x => x.variable.name == 'accountId');
    expect(acctIdArr.length).toEqual(1);
    const acctUri = acctIdArr[0].variable.value;
    contactAndAccountEnquiry.account.id = parseInt(acctUri.substring(acctUri.lastIndexOf('/') + 1));

    const contactRes = await fetch(`${server}/${tenantId}/contacts/${contactAndAccountEnquiry.id}`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    const data = await contactRes.json();
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
    data.activities.sort((a, b) => new Date(b.occurred) - new Date(a.occurred));
    expect(data.activities[0].type).toEqual('LINK_ACCOUNT_TO_CONTACT');
    expect(data.activities[0].content).toMatch(/Linked account [0-9]* to contact [0-9]*/);
    // note will be attached to account not contact
    const accountRes = await fetch(`${server}/${tenantId}/accounts/${contactAndAccountEnquiry.account.id}`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    const acctData = await accountRes.json();
    expect(acctData.id).toEqual(contactAndAccountEnquiry.account.id);
    expect(acctData.name).toEqual(contactAndAccountEnquiry.account.name);
    expect(acctData.notes.length).toEqual(1);
    expect(acctData.notes[0].content).toEqual(contactAndAccountEnquiry.message.replace(/\n/g, '<br/>'));
    expect(acctData.activities.length).toEqual(1);
    acctData.activities.sort((a, b) => new Date(b.occurred) - new Date(a.occurred));
    expect(acctData.activities[0].type).toEqual('Enquiry');
    expect(acctData.activities[0].content).toEqual('See notes');
  });

  it("deletes the added contact", async function() {
    const res = await fetch(`${server}/${tenantId}/contacts/${contactOnlyEnquiry.id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    expect(res.status).toEqual(204);
  });

  it("deletes the added contact and account", async function() {
    const resContact = await fetch(`${server}/${tenantId}/contacts/${contactAndAccountEnquiry.id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    expect(resContact.status).toEqual(204);
    const resAccount = await fetch(`${server}/${tenantId}/accounts/${contactAndAccountEnquiry.account.id}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    expect(resAccount.status).toEqual(204);
  });

  it("checks the data is the same as the baseline", async function() {
    const contactsRes = await fetch(`${server}/${tenantId}/contacts/`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    contacts = await contactsRes.json();
    contacts.sort((a, b) => new Date(b.firstContact) - new Date(a.firstContact));
    expect(contacts.length).toEqual(contactsBefore.length);

    const accountsRes = await fetch(`${server}/${tenantId}/accounts/`, {
      headers: { 'Accept': 'application/json', 'Authorization': 'Basic ' + btoa(processCreds) }
    });
    const accounts = await accountsRes.json();
    expect(accountsRes.status).toEqual(200);
    expect(accounts.length).toEqual(accountsBefore.length);
  });

  afterAll(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
