/*******************************************************************************
 * Copyright 2015-2021 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
function CustMgmtClient(o) { // jshint ignore:line
  const PAGE_SIZE = 1000;
  var me = {
    options: o
  };

  function commonHeaders() {
    return {
      "Accept": "application/json, text/javascript",
      "X-Requested-With": "XMLHttpRequest",
      "Authorization": "Bearer "+me.options.token
    };
  }

  me.deleteAccount = function(account) {
    return deleteEntity(account, 'account');
  };

  me.deleteContact = function(contact) {
    return deleteEntity(contact, 'contact');
  };

  function deleteEntity(entity, entityPath) {
    return fetch(uriFromEntity(entity, entityPath), {
      "headers": commonHeaders(),
      "body": JSON.stringify(entity),
      "method": "DELETE",
      "mode": "cors"
    });
  }

  me.fetchAccount = function(uri) {
    return fetchEntity(uri);
  };

  me.fetchAccounts = function(tenantId, page, limit) {
    return fetchEntities('account', tenantId, page, limit);
  };

  me.fetchContact = function(uri) {
    return fetchEntity(uri);
  };

  me.fetchContacts = async function(tenantId, page, limit) { // jshint ignore:line
    return fetchEntities('contact', tenantId, page, limit);
  };

  async function fetchEntities(entityPath, tenantId, page, limit) { // jshint ignore:line
    let entities = [];
    let keepGoing = true;
    page = page == undefined ? 0 : page;
    limit = limit == undefined ? PAGE_SIZE : limit;
    while (keepGoing) {
      const response = await fetch(me.options.server+'/' + (tenantId === undefined ? '' : tenantId + '/') + entityPath +'s/?page='+page+'&limit='+limit, {
        "headers": commonHeaders(),
        "method": "GET",
        "mode": "cors"
      });
      const data = await response.json();
      Array.prototype.push.apply(entities, data);
      page += 1;
      if (data.length < limit) {
        keepGoing = false;
        return entities;
      }
    }
  }

  function fetchEntity(uri) {
    return fetch(uri, {
      "headers": commonHeaders(),
      "method": "GET",
      "mode": "cors"
    });
  }

  me.saveAccount = function(account, tenantId) {
    return saveEntity(account, 'account', tenantId);
  };

  me.saveContact = function(contact, tenantId) {
    return saveEntity(contact, 'contact', tenantId);
  };

  function saveEntity(entity, entityPath, tenantId) {
    var headers = commonHeaders();
    headers["Content-Type"] = "application/json";
    let uri = uriFromEntity(entity, entityPath);
    return fetch(uri === undefined ? me.options.server + '/' + tenantId + '/'+ entityPath +'s/' : uri, {
      "headers": headers,
      "body": JSON.stringify(entity),
      "method": uri === undefined ? 'POST' : 'PUT',
      "mode": "cors"
    });
  }

  function uriFromEntity(entity, entityPath) {
    var uri;
    if ('links' in entity) {
      $.each(entity.links, function(i,d) {
        if (d.rel == 'self') {
          uri = d.href;
        }
      });
    } else if ('_links' in entity) {
      uri = entity._links.self.href;
    } else if ('id' in entity) {
      uri = entityPath+'/'+entity.id;
    }

    return uri;
  }

  return me;
}
