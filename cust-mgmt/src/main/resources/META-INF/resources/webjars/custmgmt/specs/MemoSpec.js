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
describe("Memo API", function() {
  var tenantId = 'acme';
  var $rh = new RestEntityHelper({
    server: window['$env'] == undefined ? 'http://localhost:8080' : $env.server,
    tenantId: tenantId
  });

  var originalTimeout;
  var memosBefore = [];
  var memo = {
      "owner": "tim@knowprocess.com",
      "status": "Draft",
      "tenantId": tenantId,
      "name": "TEST",
      "title": "A test memo",
      "requiredVars": "",
      "richContent":"<p>The quick brown fox jumps over the lazy dog</p>"
  };
  
  beforeEach(function() {
    originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  it("searches to take an initial baseline", function(done) {
    $rh.getJSON('/'+tenantId+'/memos/',  function(data, textStatus, jqXHR) {
      memosBefore = data;
      expect(jqXHR.status).toEqual(200);
      done();
    });
  });

  it("creates a new memo", function(done) {
    $rh.ajax({
      url: '/'+tenantId+'/memos/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(memo),
      success: function(data, textStatus, jqXHR) {
        var location = jqXHR.getResponseHeader('Location');
        expect(location).toMatch(/\/memos\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        memo.links = [ { rel: 'self', href: location } ];
        done();
      }
    });
  });
  
  it("fetches updated memos and checks the newly added one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/memos/',  function( data ) {
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      memos = data;

      expect(memos.length).toEqual(memosBefore.length+1);
      expect($rh.uri(memos[0])).toEqual($rh.uri(memo));
      expect(memos[0].name).toEqual(memo.name);
      expect(memos[0].subject).toEqual(memo.subject);
      expect(memos[0].status).toEqual(memo.status);

      expect(memos[0].links).toBeDefined();
      expect(memos[0].links[0].href).toMatch(/.*\/memos\/[0-9]*/);
      expect(memos[0].links.length).toEqual(1);

      done();
    });
  });

  it("updates the new memo", function(done) {
    memo.title = 'UPDATE: '+memo.title;
    memo.status = 'Published';
    $rh.ajax({
      url: $rh.tenantUri(memo),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(memo),
      success: function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);
        done();
      }
    });
  });

  it("fetches updated memos and checks the newly updated one is correct", function(done) {
    $rh.getJSON('/'+tenantId+'/memos/', function( data ) {
      memos = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });

      expect(memos.length).toEqual(memosBefore.length+1);
      expect($rh.uri(memos[0])).toEqual($rh.uri(memo));
      expect(memos[0].title).toEqual(memo.title);
      expect(memos[0].status).toEqual('Published');

      expect(memos[0].links).toBeDefined();
      expect(memos[0].links[0].href).toMatch(/.*\/memos\/[0-9]*/);
      expect(memos[0].links.length).toEqual(1);

      done();
    });
  });

  it("clone the new memo", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(memo)+'/clone',
      type: 'POST',
      success: function(data, textStatus, jqXHR) {
        clonedUri = jqXHR.getResponseHeader('Location');
        expect(clonedUri).toMatch(/\/memos\/[0-9]/);
        expect(jqXHR.status).toEqual(201);
        done();
      }
    });
  });

  it("fetches cloned memo, check correctness and delete", function(done) {
    $rh.getJSON(clonedUri, function( data ) {
      expect($rh.uri(data)).toEqual(clonedUri);
      expect(data.name).toEqual(memo.name + 'Copy');
      expect(data.title).toEqual(memo.title);
      expect(data.status).toEqual('Draft');

      // TODO cannot understand why _links object instead of links array here
      expect(data._links).toBeDefined();
      expect(data._links.self.href).toMatch(/.*\/memos\/[0-9]*/);

      done();
    });
  });

  it("deletes the added memo", function(done) {
    $rh.ajax({
      url: $rh.tenantUri(memo),
      type: 'DELETE',
      contentType: 'application/json',
      success: completeHandler = function(data, textStatus, jqXHR) {
        expect(jqXHR.status).toEqual(204);

        $rh.ajax({
          url: clonedUri,
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
    $rh.getJSON('/'+tenantId+'/memos/',  function( data ) {
      memos = data;
      data.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      
      expect(memos.length).toEqual(memosBefore.length);
      done();
    });
  });
  
  afterEach(function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });
});
