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
describe("Contact management", function() {
  const tenantId = 'acme';
  const server = (typeof $env === 'undefined' || !$env) ? 'http://localhost:8082' : $env.server;
  const baseUrl = `${server}/${tenantId}`;
  const originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;

  const contact = {
      firstName: 'Fred',
      lastName: 'Flintstone',
      email: 'fred@slaterock.com',
      customFields: { 
        dateOfBirth: '01/01/1970'
      }
  };
  const account = {
      name: 'Bedrock Slate and Gravel'
  };
  const note = {
      author: 'boss@slaterock.com',
      content: 'Fred is a great worker'
  }
  const doc = {
      author: 'boss@slaterock.com',
      name: 'Annual review',
      url: 'http://slaterock.com/reviews/Fred.odt',
      favorite: true
  }
  const activity = {
      content: 'Annual review',
      type: 'TEST'
  }

  let accountsBefore = [];
  let contactsBefore = [];
  let contacts = [];

  function getIdFromLocation(location) {
    var match = location.match(/\/(\d+)(?:$|\/)/);
    return match ? match[1] : null;
  }
  function accountUri(account) {
    return `${baseUrl}/accounts/${account.id || getIdFromLocation((account.links && account.links[0] && account.links[0].href) || '')}`;
  }
  function contactUri(contact) {
    return `${baseUrl}/contacts/${contact.id || getIdFromLocation((contact.links && contact.links[0] && contact.links[0].href) || '')}`;
  }
  async function createTestContact(baseUrl, contact) {
    await fetch(`${baseUrl}/contacts/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contact)
    }).then(function (response) {
      if (response.status !== 201) fail(`Failed to create contact: ${response.status}`);
      const location = response.headers.get('Location');
      if (!location) fail('No Location header returned');
      contact.links = [{ rel: 'self', href: location }];
    }).catch(e => { fail(e); });
    console.info(`Created contact: ${contact.links[0].href}`);
  }


  beforeAll(async function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
        
    accountsBefore = await fetch(`${baseUrl}/accounts/`).then(r => r.json()).catch(e => { fail(e); });
    console.info(`Baseline accounts: ${accountsBefore.length}`);
    contactsBefore = await fetch(`${baseUrl}/contacts/`).then(r => r.json()).catch(e => { fail(e); });
    console.info(`Baseline contacts: ${contactsBefore.length}`);

    await createTestContact(baseUrl, contact);
  });

  it("fetches updated contacts and checks the newly added one is correct", function(done) {
    fetch(`${baseUrl}/contacts/`)
      .then(r => r.json())
      .then(function(data) {
        data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
        contacts = data;
        expect(contacts.length).toEqual(contactsBefore.length+1);
        // You may need to adjust the following checks if uri helpers are needed
        expect(contacts[0].firstName).toEqual(contact.firstName);
        expect(contacts[0].lastName).toEqual(contact.lastName);
        expect(contacts[0].fullName).toEqual(contact.firstName+ ' ' + contact.lastName);
        expect(contacts[0].email).toEqual(contact.email);
        expect(contacts[0].firstContact).not.toBeNull();
        expect(contacts[0].links).toBeDefined();
        expect(contacts[0].links[0].href).toMatch(/.*\/contacts\/[0-9]*/);
        expect(contacts[0].links.length).toEqual(1);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("adds a note to the contact", function(done) {
    fetch(`${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}/notes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(note)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/.*\/contacts\/[0-9]*\/notes\/[0-9]*/);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("adds a document link to the contact", function(done) {
    fetch(`${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}/documents`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(doc)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/.*\/contacts\/[0-9]*\/documents\/[0-9]*/);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("adds an activity to the contact", function(done) {
    fetch(`${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}/activities`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(activity)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/.*\/contacts\/[0-9]*\/activities\/[0-9]*/);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("sets the stage of the contact", function(done) {
    fetch(`${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}/stage/tested`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(activity)
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("updates the new contact", function(done) {
    contact.phone1 = '+44 7777 123456';
    contact.phone2 = '020 7123 4567';
    contact.customFields.dateOfBirth = '31/10/1969';
    fetch(`${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contact)
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches updated contacts and checks the newly updated one is correct", function(done) {
    fetch(`${baseUrl}/contacts/`)
      .then(r => r.json())
      .then(function(data) {
        contacts = data;
        data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
        expect(contacts.length).toEqual(contactsBefore.length+1);
        expect(contacts[0].phone1).toEqual(contact.phone1);
        expect(contacts[0].phone2).toEqual(contact.phone2);
        expect(contacts[0].firstOccurred || contacts[0].firstContact).not.toBeNull();
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("adds an account to the new contact", function(done) {
    fetch(`${baseUrl}/accounts/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(account)
    }).then(function(response) {
      expect(response.status).toEqual(201);
      var location = response.headers.get('Location');
      expect(location).toMatch(/\/accounts\/[0-9]/);
      account.links = [ { rel: 'self', href: location } ];
      var contactAccountLink = `${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}/account`;
      fetch(contactAccountLink, {
        method: 'PUT',
        headers: { 'Content-Type': 'text/uri-list' },
        body: location
      }).then(function(response) {
        expect(response.status).toEqual(204);
        done();
      }).catch(function(e) { fail(e); done(); });
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches updated contacts and checks the newly added one holds correct account information", function(done) {
    fetch(`${baseUrl}/contacts/`)
      .then(r => r.json())
      .then(function(data) {
        contacts = data;
        data.sort(function(a,b) { return new Date(b.firstContact)-new Date(a.firstContact); });
        expect(contacts.length).toEqual(contactsBefore.length+1);
        expect(contacts[0].firstName).toEqual(contact.firstName);
        expect(contacts[0].lastName).toEqual(contact.lastName);
        expect(contacts[0].email).toEqual(contact.email);
        expect(contacts[0].account).toBeDefined();
        expect(contacts[0].account.name).toEqual(account.name);
        expect(contacts[0].links[0].href).toMatch(/.*\/contacts\/[0-9]*/);
        expect(contacts[0].links[1].href).toMatch(/.*\/accounts\/[0-9]*/);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("fetches complete contact inc. child entities and check all fields are correct", function(done) {
    fetch(`${baseUrl}/contacts/${getIdFromLocation(contact.links[0].href)}`)
      .then(r => r.json())
      .then(function(data) {
        expect(data.firstName).toEqual(contact.firstName);
        expect(data.lastName).toEqual(contact.lastName);
        expect(data.email).toEqual(contact.email);
        expect(data.phone1).toEqual(contact.phone1);
        expect(data.phone2).toEqual(contact.phone2);
        expect(data.firstContact).toBeDefined();
        expect(data.customFields).toBeDefined();
        expect(data.customFields.dateOfBirth).toEqual(contact.customFields.dateOfBirth);
        expect(data.account.name).toEqual(account.name);
        expect(data.account.id).toBeDefined();
        expect(data.activities.length).toEqual(3);
        data.activities.sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
        expect(data.activities[0].type).toEqual('LINK_ACCOUNT_TO_CONTACT');
        expect(data.activities[0].content).toMatch(/Linked account [0-9]* to contact [0-9]*/);
        expect(data.activities[1].type).toEqual('TRANSITION_TO_STAGE');
        expect(data.activities[1].content).toEqual('From null to tested');
        expect(data.activities[2].type).toEqual(activity.type);
        expect(data.activities[2].content).toEqual(activity.content);
        expect(data.notes.length).toEqual(1);
        expect(data.notes[0].author).toEqual(note.author);
        expect(data.notes[0].name).toEqual(note.name);
        expect(data.documents.length).toEqual(1);
        expect(data.documents[0].author).toEqual(doc.author);
        expect(data.documents[0].name).toEqual(doc.name);
        expect(data.documents[0].url).toEqual(doc.url);
        expect(data.documents[0].confidential).toEqual(false);
        expect(data.documents[0].favorite).toEqual(doc.favorite);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  afterAll(async function() {
    if (contact.links && contact.links.length > 0) {
      await fetch(contactUri(contact), {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
      }).then(function (response) {
        if (response.status !== 204) {
          fail(`Failed to delete contact: ${response.status}`);
        } else { console.info(`successfully removed test contact ${contactUri(contact)}`) }
      }).catch(function (e) { fail(e); });
    }
    if (account.links && account.links.length > 0) {
      await fetch(accountUri(account), {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
      }).then(function (response) {
        if (response.status !== 204) {
          fail(`Failed to delete account: ${response.status}`);
        } else { console.info(`successfully removed test account ${accountUri(account)}`) }
      }).catch(function (e) { fail(e); });
    }

    const accountsAfter = await fetch(`${baseUrl}/accounts/`).then(r => r.json()).catch(e => { fail(e); });
    const contactsAfter = await fetch(`${baseUrl}/contacts/`).then(r => r.json()).catch(e => { fail(e); });
    expect(contactsAfter.length).toEqual(contactsBefore.length);
    expect(accountsAfter.length).toEqual(accountsBefore.length);

    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
