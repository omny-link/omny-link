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
describe("Memo API", function() {
  const tenantId = 'acme';
  const server = (typeof $env === 'undefined' || !$env) ? 'http://localhost:8082' : $env.server;
  const baseUrl = `${server}/${tenantId}`;
  const originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
  const memo = {
      "owner": "tim@knowprocess.com",
      "status": "Draft",
      "tenantId": tenantId,
      "name": "TEST",
      "title": "A test memo",
      "requiredVars": "",
      "richContent":"<p>The quick brown fox jumps over the lazy dog</p>"
  };
  
  let memosBefore = [];
  let clonedUri = null;
  let memos = [];

  function getIdFromLocation(location) {
    var match = location.match(/\/(\d+)(?:$|\/)/);
    return match ? match[1] : null;
  }
  function memoUri(memo) {
    return `${baseUrl}/memos/${memo.id || getIdFromLocation((memo.links && memo.links[0] && memo.links[0].href) || '')}`;
  }
  async function createTestMemo(baseUrl, memo) {
    await fetch(`${baseUrl}/memos/`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(memo)
    }).then(function (response) {
      if (response.status !== 201) fail(`Failed to create memo: ${response.status}`);
      const location = response.headers.get('Location');
      if (!location) fail('No Location header returned');
      memo.links = [{ rel: 'self', href: location }];
    }).catch(e => { fail(e); });
    console.info(`Created memo: ${memo.links[0].href}`);
  }

  beforeAll(async function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
    // fetch baseline memos
    memosBefore = await fetch(`${baseUrl}/memos/`).then(r => r.json()).catch(e => { fail(e); });
    await createTestMemo(baseUrl, memo);
  });

  it("fetches updated memos and checks the newly added one is correct", function(done) {
    fetch(`${baseUrl}/memos/`)
      .then(r => r.json())
      .then(function(data) {
        data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
        memos = data;
        expect(memos.length).toEqual(memosBefore.length+1);
        // You may need to adjust the following checks if uri helpers are needed
        expect(memos[0].name).toEqual(memo.name);
        expect(memos[0].title).toEqual(memo.title);
        expect(memos[0].status).toEqual(memo.status);
        expect(memos[0].links).toBeDefined();
        expect(memos[0].links[0].href).toMatch(/.*\/memos\/[0-9]*/);
        expect(memos[0].links.length).toEqual(1);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("updates the new memo", function(done) {
    memo.title = 'UPDATE: '+memo.title;
    memo.status = 'Published';
    fetch(memoUri(memo), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(memo)
    }).then(function(response) {
      expect(response.status).toEqual(204);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches updated memos and checks the newly updated one is correct", function(done) {
    fetch(`${baseUrl}/memos/`)
      .then(r => r.json())
      .then(function(data) {
        memos = data;
        data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
        expect(memos.length).toEqual(memosBefore.length+1);
        expect(memos[0].title).toEqual(memo.title);
        expect(memos[0].status).toEqual('Published');
        expect(memos[0].links).toBeDefined();
        expect(memos[0].links[0].href).toMatch(/.*\/memos\/[0-9]*/);
        expect(memos[0].links.length).toEqual(1);
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  it("clone the new memo", function(done) {
    fetch(memoUri(memo) + '/clone', {
      method: 'POST'
    }).then(function(response) {
      clonedUri = response.headers.get('Location');
      console.info(`Cloned memo URI: ${clonedUri}`);
      expect(clonedUri).toMatch(/\/memos\/[0-9]/);
      expect(response.status).toEqual(201);
      done();
    }).catch(function(e) { fail(e); done(); });
  });

  it("fetches cloned memo, check correctness and delete", function(done) {
    fetch(clonedUri)
      .then(r => r.json())
      .then(function(data) {
        expect(data._links).toBeDefined();
        expect(data._links.self.href).toMatch(/.*\/memos\/[0-9]*/);
        expect(data.name).toEqual(memo.name + 'Copy');
        expect(data.title).toEqual(memo.title);
        expect(data.status).toEqual('Draft');
        done();
      }).catch(function(e) { fail(e); done(); });
  });

  afterAll(async function() {
    if (memo.links && memo.links.length > 0) {
      await fetch(memoUri(memo), {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
      }).then(function (response) {
        if (response.status !== 204) {
          fail(`Failed to delete memo: ${response.status}`);
        } else { console.info(`successfully removed test memo ${memoUri(memo)}`) }
      }).catch(function (e) { fail(e); });
    }
    if (clonedUri) {
      await fetch(clonedUri, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
      }).then(function (response) {
        if (response.status !== 204) {
          fail(`Failed to delete cloned memo: ${response.status}`);
        } else { console.info(`successfully removed cloned memo ${clonedUri}`) }
      }).catch(function (e) { fail(e); });
    }

    const memosAfter = await fetch(`${baseUrl}/memos/`).then(r => r.json()).catch(e => { fail(e); });
    expect(memosAfter.length).toEqual(memosBefore.length);

    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
