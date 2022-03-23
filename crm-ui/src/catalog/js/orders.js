/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributorss
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
const DEFAULT_INACTIVE_STAGES = 'cold,complete,on hold,unqualified,waiting list';

var ractive = new BaseRactive({
  cm: new CustMgmtClient({
    server: $env.server
  }),
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    contacts: [],
    entityName: 'order',
    entityPath: '/orders',
    stockItems: [],
    orders: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    saveObserver:false,
    server: $env.server,
    taxRate: 0.2,
    title: 'Orders',
    variant: 'order',
    daysAgo: function(noOfDays) {
      return ractive.daysAgo(noOfDays);
    },
    featureEnabled: function(feature) {
      //console.info('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    formatAge: function(timeString) {
      console.info('formatAge: '+timeString);
      return (timeString == "-1" || timeString==undefined) ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatAgeFromDate: function(timeString) {
      if (timeString==undefined) return;
      return i18n.getAgeString(ractive.parseDate(timeString));
    },
    formatContactAddress: function(contactId, selector) {
      console.info('formatContactAddress');
      if (contactId == undefined) return contactId;
      $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/'+contactId, function(contact) {
        $(selector).val(contact.address);
      });
    },
    formatContent: function(content) {
      console.info('formatContent:'+content);
      content = content.replace(/\n/g,'<br/>');
      content = ractive.autolinker().link(content);
      return content;
    },
    formatDate: function(timeString) {
      if (timeString == undefined || timeString.length==0) return 'n/a';
      var date = ractive.parseDate(timeString);
      if (date == 'Invalid Date') {
        return timeString;
      } else {
        return date.toLocaleDateString(navigator.languages);
      }
    },
    formatDate6Digit: function(timeString) {
      if (timeString == undefined || timeString.length==0) return 'n/a';
      var date = ractive.parseDate(timeString);
      if (date == 'Invalid Date') {
        return timeString;
      } else {
        return ''+(date.getFullYear()-2000)+
            (date.getMonth()<9 ? '0'+(date.getMonth()+1) : date.getMonth()+1+'')+
            (date.getDate()<10 ? '0'+(date.getDate()) : date.getDate()+'');
      }
    },
    formatDateTime: function(timeString) {
      if (timeString==undefined) return 'n/a';
      var dts = new Date(timeString).toLocaleString(navigator.languages);
      // remove secs
      if (dts.split(':').length>1) dts = dts.substring(0, dts.lastIndexOf(':'));
      return dts;
    },
    formatFavorite: function(obj) {
      if (obj.favorite) return 'glyphicon-star';
      else return 'glyphicon-star-empty';
    },
    formatId: function(entity) {
      return ractive.localId(entity);
    },
    formatJson: function(json) {
      //console.info('formatJson: '+json);
      try {
        var obj = JSON.parse(json);
        var html = '';
        $.each(Object.keys(obj), function(i,d) {
          html += (typeof obj[d] == 'object' ? '' : '<b>'+d+'</b>: '+obj[d]+'<br/>');
        });
        return html;
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    formatSumOrderItemField: function(order,fieldName) {
      var val=0;
      for (let idx in order.orderItems) {
        if (order.orderItems[idx].customFields[fieldName]==undefined) continue;
        var tmp = order.orderItems[idx].customFields[fieldName];
        if (tmp != undefined) {
          if (tmp!='-' && tmp!='n/a') val += parseInt(tmp);
        }
      }
      return val;
    },
    formatUniqOrderItemField: function(order,fieldName) {
      if (order == undefined || fieldName == undefined) return '';
      var val='';
      var items = order.orderItems.slice(); // avoid recursion by cloning
      for (var idx = 0 ; idx < items.length ; idx++) {
        if (items[idx].customFields[fieldName]==undefined) continue;
        var tmp = items[idx].customFields[fieldName];
        if (tmp != undefined && val.indexOf(tmp)==-1) {
          if (val.length > 0 && tmp!='-' && tmp!='n/a') val += ',';
          if (tmp!='-' && tmp!='n/a') val += tmp;
        }
      }
      return val;
    },
    formatStockItemIds: function(order) {
      if (order==undefined) return '';
      if (order.stockItemNames!=undefined) return order.stockItemNames;
      console.info('formatStockItemIds for order: '+ractive.uri(order));

      var stockItemIds = [];
      for (let idx = 0 ; idx < order.orderItems.length ; idx++) {
        stockItemIds.push(order.orderItems[idx].stockItem.id);
      }
      var stockItemNames = '';
      for (let idx = 0 ; idx < stockItemIds.length ; idx++) {
        var tmp = Array.findBy('id',stockItemIds[idx],ractive.get('stockItems'));
        if (tmp != undefined && stockItemNames.indexOf(tmp.name)==-1) {
          if (stockItemNames.length > 0) stockItemNames += ',';
          stockItemNames += tmp.name;
        }
      }
      return (order.stockItemNames = stockItemNames);
    },
    formatTags: function(tags) {
      var html = '';
      if (tags==undefined) return html;
      var tagArr = tags.split(',');
      $.each(tagArr, function(i,d) {
        html += '<span class="img-rounded" style="background-color:'+d+'">&nbsp;&nbsp;</span>';
      });
      return html;
    },
    gravatar: function(email) {
      return ractive.gravatar(email);
    },
    hash: function(email) {
      return ractive.hash(email);
    },
    haveCustomExtension: function(extName) {
      return Array.findBy('name',ractive.get('tenant.id')+extName,ractive.get('tenant.partials'))!=undefined;
    },
    helpUrl: '//omny-link.github.io/user-help/orders/#the_title',
    lessThan24hAgo: function(isoDateTime) {
      if (isoDateTime == undefined || (new Date().getTime()-new Date(isoDateTime).getTime()) < 1000*60*60*24) {
        return true;
      }
      return false;
    },
    matchRole: function(role) {
      //console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    matchSearch: function(obj) {
      if (ractive.get('searchTerm')==undefined || ractive.get('searchTerm').length==0) {
        return true;
      } else {
        var search = ractive.get('searchTerm').split(' ');
        if ('contactName' in obj) {
          var contact = Array.findBy('id',obj.contactId,ractive.get('contacts'));
          if (contact != undefined) obj.contactName = contact.fullName;
        }
        for (let idx = 0 ; idx < search.length ; idx++) {
          var searchTerm = search[idx].toLowerCase();
          var match = ( (obj.id != undefined && searchTerm.indexOf(obj.id)>=0) ||
              (obj.name!=undefined && obj.name.toLowerCase().indexOf(searchTerm)>=0) ||
              (obj.invoiceRef!=undefined && obj.invoiceRef.toLowerCase().indexOf(searchTerm)>=0) ||
              (searchTerm.startsWith('stage:') && obj.stage!=undefined && obj.stage.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(6))==0) ||
              (obj.date!=undefined && searchTerm.startsWith('date>') && new Date(obj.date)>new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('created>') && new Date(obj.created)>new Date(ractive.get('searchTerm').substring(8))) ||
              (obj.date!=undefined && searchTerm.startsWith('date<') && new Date(obj.date)<new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('created<') && new Date(obj.created)<new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('contactid:') && ractive.get('searchTerm').substring(10).replace(/ /g,'')==''+obj.contactId) ||
              (searchTerm.startsWith('contactid:') && ractive.get('searchTerm').substring(10).replace(/ /g,'').split(',').indexOf(''+obj.contactId)!=-1) ||
              (obj.contactName!=undefined && obj.contactName.toLowerCase().indexOf(searchTerm)>=0) ||
              (obj.stockItem!=undefined && obj.stockItem.name.toLowerCase().indexOf(searchTerm)>=0) ||
              (searchTerm.startsWith('owner:') && obj.owner != undefined && obj.owner.indexOf(searchTerm.substring(6))!=-1) ||
              (searchTerm.startsWith('active') && (obj.stage==undefined || obj.stage.length==0 || ractive.inactiveStages().indexOf(obj.stage.toLowerCase())==-1)) ||
              (searchTerm.startsWith('!active') && ractive.inactiveStages().indexOf(obj.stage.toLowerCase())!=-1)
            );
            // no match is definitive but match now may fail other terms (AND logic)
            if (!match) return false;
        }
        return true;
      }
    },
    localId: function(obj) {
      return ractive.localId(obj);
    },
    selectMultiple: [],
    sort: function (array, column, asc) {
      console.info('sort '+(asc ? 'ascending' : 'descending')+' on: '+column);
      array = array.slice(); // clone, so we don't modify the underlying data

      return array.sort( function ( a, b ) {
        if (b[column]==undefined || b[column]==null || b[column]=='') {
          return (a[column]==undefined || a[column]==null || a[column]=='') ? 0 : -1;
        } else if (asc) {
          return a[ column ] < b[ column ] ? -1 : 1;
        } else {
          return a[ column ] > b[ column ] ? -1 : 1;
        }
      });
    },
    sortAsc: false,
    sortColumn: 'lastUpdated',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc';
      else return 'hidden';
    },
    stdPartials: [
      { "name": "customActionModal", "url": "/partials/custom-action-modal.html"},
      { "name": "fieldExtension", "url": "/partials/field-extension.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/order-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "orderListSect", "url": "/partials/order-list-sect.html"},
      { "name": "orderListTable", "url": "/partials/order-list-table.html"},
      { "name": "currentDocumentListSect", "url": "/partials/current-document-list-sect.html"},
      { "name": "currentNoteListSect", "url": "/partials/current-note-list-sect.html"},
      { "name": "currentOrderSect", "url": "/partials/order-current-sect.html"},
      { "name": "currentOrderExtensionSect", "url": "/partials/order-extension.html"},
      { "name": "currentPurchaseOrderExtensionSect", "url": "/partials/purchase-order-extension.html"},
      { "name": "currentOrderTableSect", "url": "/partials/order-list-table.html" },
      { "name": "currentOrderItemListSect", "url": "/partials/order-item-list-sect.html"},
      { "name": "currentFeedbackSect", "url": "/partials/feedback-current-sect.html"},
      { "name": "currentOrderItemSect", "url": "/partials/order-item-current-sect.html"},
      { "name": "currentOrderItemExtensionSect", "url": "/partials/order-item-extension.html" }
    ],
    uniq: function(fieldName, arr) {
      return Array.uniq(fieldName, arr);
    }
  },
  partials: {
    'profileArea': '',
    'titleArea': '',
    'orderListSect':'',
    'currentOrderSect': '',
    'poweredBy': '',
    'sidebar': '',
    'toolbar': '',
    'helpModal': '',
    'customActionModal': '',
    'orderListTable': ''
  },
  add: function () {
    console.info('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var order = {
        name: ractive.get('tenant.strings.order') == undefined ? 'Order' : ractive.get('tenant.strings.order'),
        orderItems: [],
        owner: ractive.get('profile.username'),
        stage: ractive.initialOrderStage(),
        tenantId: ractive.get('tenant.id'),
        type: ractive.get('variant')
    };
    ractive.select( order );
    ractive.initTags();
  },
  addOrderItem: function(order) {
    console.info('addOrderItem: '+ractive.localId(order));
    var orderId = ractive.localId(order);
    var tmp = ractive.get('itemPrototype')== undefined ?
        { orderId: orderId, customFields: {} } :
        ractive.get('itemPrototype');
    ractive.set('currentOrderItemIdx',ractive.get('current.orderItems').length);
    ractive.push('current.orderItems', tmp);

    ractive.saveOrderItem();
    ractive.select(ractive.get('current'));
  },
  assignSequence: function(seqName) {
    console.info('assignSequence '+ractive.localId(ractive.get('current'))+'...');

    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/sequences/'+seqName,
        type: 'GET',
        success: function(data) {
          ractive.set('saveObserver',true);
          ractive.set('current.ref',data.lastUsed);
          ractive.set('saveObserver',false);
        }
    });
  },
  delete: function (order) {
    var orderId = 'id' in order ? order.id : ractive.localId(order);
    console.info('delete '+orderId+'...');

    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/'+orderId,
        type: 'DELETE',
        success: function() {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteOrderItem: function(item,j) {
    console.log('deleteOrderItem ' + ('id' in item ? item.id : 'new item?') +'...');
    if ('id' in item) {
      var orderId = ractive.get('current.id');
      $.ajax({
        url: ractive.getServer() + '/'+ractive.get('tenant.id')+'/orders/'+orderId+'/order-items/'+item.id,
        type: 'DELETE',
        success: function() {
          ractive.select(ractive.get('current'));
        }
      });
    } else {
      ractive.splice('current.orderItems', j, 1);
    }
    return false; // cancel bubbling to prevent edit as well as delete
  },
  edit: function (order) {
    console.info('edit: '+ractive.uri(order)+'...');

    ractive.set('currentIdx',ractive.get('orders').indexOf(order));
    ractive.select( order );
  },
  enhanceOrderWithContact: function(order) {
    if (ractive.get('contacts') !== undefined) {
      order.contact = Array.findBy('id',order.contactId,ractive.get('contacts'));
      ractive.update('current.contact');
    }
  },
  enhanceOrdersWithContact: function() {
    ractive.get('orders').map(function(obj) {
      if ('contactId' in obj) {
        obj.contact = Array.findBy('id',obj.contactId,ractive.get('contacts'));
      }
      return obj;
    });
  },
  enhanceOrderWithStockItems: function(order) {
    if (ractive.get('stockItems') !== undefined && order.orderItems !== undefined) {
      order.orderItems.map(function(d) {
        d.stockItem = Array.findBy('id',d.stockItemId,ractive.get('stockItems'));
      });
      ractive.update('current.orderItems');
    }
    if (ractive.get('stockItems') !== undefined && order.stockItemId !== undefined) {
      order.stockItem = Array.findBy('id',order.stockItemId,ractive.get('stockItems'));
      ractive.update('current.stockItem');
    }
  },
  fetch: function() {
    console.info('fetch variant: '+ractive.get('variant'));
    if (ractive.get('fetchInFlight')==true) {
      console.warn('skipping fetch as already running');
      return;
    } else {
      ractive.set('saveObserver', false);
      ractive.set('fetchInFlight', true);
      var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/findByType/'+ractive.get('variant');
      $.ajax({
        dataType: "json",
        url: url,
        crossDomain: true,
        success: function( data ) {
          ractive.set('fetchInFlight', false);
          if ('_embedded' in data) {
            data = data._embedded.orders;
          }
          ractive.merge('orders', data);
          ractive.initControls();
          if (ractive.hasRole('admin')) $('.admin').show();
          if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
          ractive.showSearchMatched();
          ractive.set('saveObserver', true);
        }
      });
      let params = getSearchParameters();
      if ('accountId' in params) ractive.fetchAccountContacts(params.accountId);
      else ractive.fetchContacts();
      ractive.fetchStockItems();
      //ractive.fetchStockCategories();
    }
  },
  fetchAccounts: function () {
    console.info('fetchAccounts...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/accounts/',
      crossDomain: true,
      success: function( data ) {
        ractive.set('saveObserver', false);
        ractive.set('accounts', data);
        ractive.addDataList({ name: 'accounts' }, ractive.get('accounts'));
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchAccountContacts: function(acctId) {
    console.info('fetchAccountContacts...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/findByAccountId?accountId='+acctId,
      crossDomain: true,
      success: function(data) {
        ractive.set('saveObserver', false);
        var contactData = jQuery.map(data, function(n) {
          return ({
            "id": ractive.localId(n),
            "name": n.fullName
          });
        });
        ractive.addDataList({ name: 'contacts' }, contactData);
        ractive.set('contacts', data);
        console.log('fetched ' + data.length + ' contacts for account');
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchContacts: function () {
    console.info('fetchContacts...');
    if (ractive.get('fetchContactsInFlight')==true) {
      console.warn('skipping fetchContacts as already running');
      return;
    } else {
      ractive.showActivityIndicator();
      ractive.set('saveObserver', false);
      ractive.set('fetchContactsInFlight', true);
      ractive.cm.fetchContacts(ractive.get('tenant.id'))
        .then(function(data) {
          ractive.showActivityIndicator();
          ractive.set('saveObserver', false);
          ractive.set('contacts', data.map(function(obj) {
            obj.name = obj.fullName;
            return obj;
          }));
          ractive.addDataList({ name: 'contacts' }, ractive.get('contacts'));
          ractive.set('fetchContactsInFlight', false);
          ractive.set('saveObserver', true);
          ractive.hideActivityIndicator();
        });
    }
  },
  fetchStockCategory: function(stockCategoryName) {
    if (stockCategoryName == undefined) return;
    console.info('fetchStockCategory...');
    $.ajax({
      dataType : "json",
      method: 'GET',
      url : ractive.getServer() + '/' + ractive.get('tenant.id') + '/stock-categories/findByName?name='+stockCategoryName,
      crossDomain : true,
      success : function(data) {
        ractive.set('saveObserver', false);
        ractive.set('current.stockCategory', data);
        ractive.set('saveObserver', true);
      }
    });
  },
  inactiveStages: function() {
    var inactiveStages = ractive.get('tenant.serviceLevel.inactiveStages')==undefined ?
        DEFAULT_INACTIVE_STAGES :
        ractive.get('tenant.serviceLevel.inactiveStages').join();
    return inactiveStages;
  },
  initEditor: function() {
    console.info('initEditor');
    if ('curDescription' in CKEDITOR.instances) {
      //
    } else {
      CKEDITOR.replace( 'curDescription', {
        height: 150,
        toolbarGroups: [
          { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
          { name: 'paragraph',   groups: [ 'list' ] }
        ]
      });
      CKEDITOR.instances.curDescription.on('blur', function(ev) {
        ractive.set('current.description', ev.editor.getData().replace(/ &amp; /g, ' and ').replace(/&amp;/g, ' and ').replace(/&[a-z]*;/, ''));
        ractive.save();
      });
    }
    CKEDITOR.instances.curDescription.setData(ractive.get('current.description'));
  },
  save: function () {
    console.info('save order: '+ractive.get('current.name')+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      if (tmp.stockItem!=undefined && ractive.localId(tmp.stockItem)!=undefined && tmp.stockItem.id==undefined) {
        tmp.stockItem.id = ractive.localId(tmp.stockItem);
      }
      if (tmp.contact!=undefined) {
        tmp.contactId = ractive.localId(tmp.contact);
        ractive.set('current.contactId', ractive.localId(tmp.contact));
      }
      delete tmp.contact;
      delete tmp.documents;
      delete tmp.notes;
      delete tmp.orderItems;
      // without time json will not reach endpoint
      if (tmp.date != null) tmp.date += 'T00:00:00';
      tmp.tenantId = ractive.get('tenant.id');
      tmp.price = parseFloat($('#curPrice').autoNumeric('get'));
      tmp.tax = parseFloat($('#curTax').autoNumeric('get'));
//      console.log('ready to save order'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/'+tmp.tenantId+'/orders/' : ractive.tenantUri(tmp),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) {
            ractive.set('current._links.self.href',location);
            ractive.set('current.orderId', ractive.localId(ractive.get('current')));
          }
          switch (jqXHR.status) {
          case 201:
            var currentIdx = ractive.get('orders').push(ractive.get('current'))-1;
            ractive.set('currentIdx',currentIdx);
            var seqName = ractive.get('tenant.strings.'+ractive.get('variant'));
            if (Array.findBy('name',seqName,ractive.get('tenant.sequences'))!=undefined) {
              ractive.assignSequence(seqName);
            }
            break;
          case 204:
//            ractive.splice('orders',ractive.get('currentIdx'),1,ractive.get('current'));
            break;
          }
          //ractive.fetch();
          ractive.showMessage((ractive.get('tenant.strings.'+ractive.get('variant'))==undefined ?
              ractive.get('variant') :
              ractive.get('tenant.strings.'+ractive.get('variant'))) + ' saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as order is invalid');
      $('#currentForm :invalid').addClass('field-error');
      var entityName = ractive.get('tenant.strings.order') == undefined ? 'order' : ractive.get('tenant.strings.order').toLowerCase();
      ractive.showMessage('Cannot save yet as '+entityName+' is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  saveOrderItem: function() {
    console.info('saveOrderItem: ...');
    ractive.set('saveObserver', false);
    if (ractive.get('currentOrderItemIdx')!= undefined) {
      ractive.set('current.orderItems.'+ractive.get('currentOrderItemIdx')+'.stockItem',
          Array.findBy('name', ractive.get('current.orderItems.'+ractive.get('currentOrderItemIdx')+'.stockItem.name'), ractive.get('stockItems')));
    }
    if (document.getElementById('currentOrderItemForm') == undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentOrderItemForm').checkValidity()) {
      var tmp = ractive.get('current.orderItems.'+ractive.get('currentOrderItemIdx'));
      if (tmp === undefined) return; // still loading
      tmp.orderId = ractive.localId(ractive.get('current'));
      tmp.tenantId = ractive.get('tenant.id');
      console.log('ready to save order item' + JSON.stringify(tmp) + ' ...');
      $.ajax({
        url: tmp.id === undefined ?
          ractive.getServer() + '/' + ractive.get('tenant.id') + '/orders/' + tmp.orderId + '/order-items' :
          ractive.getServer() + '/' + ractive.get('tenant.id') + '/orders/' + tmp.orderId + '/order-items/'+tmp.id,
        type: tmp.id === undefined ? 'POST': 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current.orderItems._links.self.href',location);
//          switch (jqXHR.status) {
//          case 201:
//
//            // TODO selecting the order works for refreshing the order items but messes up subsequent edits which are applied to the wrong item
//         // TODO cannot select directly as have no hateos links
//            //ractive.select(Array.findBy('id', ractive.get('current.id'), ractive.get('orders')));
////
//            var currentIdx = ractive.get('current.orderItems').push(ractive.get('current'))-1;
////            ractive.set('currentIdx',currentIdx);
//            break;
//          case 204:
////            ractive.splice('orders',ractive.get('currentIdx'),1,ractive.get('current'));
//            break;
//          }
          ractive.showMessage(ractive.get('tenant.strings.orderItem')+' saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      var msg = 'Cannot save yet as order item is invalid';
      console.warn(msg);
      $('#currentOrderItemForm :invalid').addClass('field-error');
      ractive.showMessage(msg);
      ractive.set('saveObserver', true);
    }
  },
  select: function(order) {
    console.info('select: '+JSON.stringify(order));
    ractive.showActivityIndicator();
    ractive.set('saveObserver', false);
    // default owner to current user
    if (!('owner' in order) || order.owner == '')
      order.owner = ractive.get('profile.username');
    if (ractive.uri(order) != undefined) {
      console.log('loading detail for '+ractive.uri(order));
      $.getJSON(ractive.tenantUri(order), function( data ) {
        console.log('found order '+data);
        ractive.showActivityIndicator();
        ractive.set('saveObserver', false);
        if (!('id' in data)) data.id = ractive.localId(data);
        if ('contactId' in data) ractive.enhanceOrderWithContact(data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        ractive.initEditor();
        if (ractive.get('tenant.features.notesOnOrder')==true) ractive.sortChildren('notes','created',false);
        if (ractive.get('tenant.features.documentsOnOrder')==true) ractive.sortChildren('documents','created',false);
        if (ractive.get('currentOrderItemId')!=undefined) {
          ractive.toggleEditOrderItem(Array.findBy('id',ractive.get('currentOrderItemId'),ractive.get('current.orderItems')));
        }
        var obj = Array.findBy('type','contact',ractive.get('tenant.orderFields'));
        if (obj!=undefined) {
          ractive.updateContactId(ractive.get('current.customFields.'+obj.name), 'current.customFields.'+obj.name+'Id');
        }
        obj = Array.findBy('type','contact',ractive.get('tenant.purchaseOrderFields'));
        if (obj!=undefined) {
          ractive.updateContactId(ractive.get('current.customFields.'+obj.name), 'current.customFields.'+obj.name+'Id');
        }
        ractive.enhanceOrdersWithContact();
        ractive.enhanceOrderWithStockItems(ractive.get('current'));
        ractive.set('saveObserver', true);
        ractive.hideActivityIndicator();
      });
    } else {
      console.log('Skipping load as has no identifier.'+order.name);
      ractive.set('current', order);
      ractive.set('saveObserver',true);
    }
    ractive.hideResults();
  },
  toggleEditOrderItem: function(orderItem, j) {
    console.info('editOrderItem '+('id' in orderItem ? orderItem.id : 'new item?')+'...');

//    ractive.set('currentOrderItemId', orderItem.id);
    ractive.set('currentOrderItemIdx',ractive.get('current.orderItems').indexOf(orderItem));
//    ractive.set('currentOrderItem',Array.findBy('id',orderItem.id,ractive.get('current.orderItems')));

//    $('.currentOrderItemSect').hide();
    $($('.currentOrderItemSect')[j]).toggle();
//    ractive.selectOrderItem( orderItem );

  },
  updateContactId: function(newVal, keypath) {
    console.info('updateContactId: '+newVal);
    if (newVal==undefined || newVal.length==0) return;
    var newContact = Array.findBy('fullName',newVal,ractive.get('contacts'));
    ractive.set('current.contact', newContact);
    if (keypath == undefined) keypath = 'current.contactId';
    ractive.set(keypath, ractive.localId(newContact));
  },
  updateStockItem: function(newVal) {
    console.info('updateStockItem: '+newVal);
    var newStockItem = Array.findBy('name',newVal,ractive.get('stockItems'));
    ractive.set('current.stockItem', newStockItem);
  },
  updateTitle: function() {
    if (ractive.get('tenant.strings.'+ractive.get('variant')+'s')==undefined) {
      ractive.set('title', ractive.get('variant'));
    } else {
      ractive.set('title', ractive.get('tenant.strings.'+ractive.get('variant')+'s'));
    }
  }
});

$(document).ready(function() {
  ractive.set('saveObserver', false);
  var params = getSearchParameters();
  if ('v' in params) {
    ractive.set('variant',decodeURIComponent(params.v));
  }
  if (ractive.get('searchTerm')==undefined) {
    ractive.set('searchTerm','updated>'+ractive.daysAgo(7));
  }
  ractive.set('saveObserver', true);
});

ractive.observe('contacts', function(newValue) {
  console.info('updated available contacts, now have: '+newValue.length);
  if (newValue !== undefined) {
    ractive.showActivityIndicator();
    ractive.enhanceOrdersWithContact();
    if (ractive.get('current')!==undefined) {
      ractive.enhanceOrderWithContact(ractive.get('current'));
    }
    ractive.hideActivityIndicator();
  }
});

ractive.observe('current.stockItem.id', function(newValue, oldValue, keypath) {
  console.info('current prop change: '+newValue +','+oldValue+' '+keypath);
  if (newValue != undefined) {
    let timerId = setInterval(function() {
      if (ractive.get('stockItems').length==0) {
        ractive.showMessage('Still loading stock items, please wait...', 'alert-warning');
      } else {
        clearInterval(timerId);
        var stockItem = Array.findBy('id','/stock-items/'+newValue,ractive.get('stockItems'));
        if (stockItem != undefined) {
          ractive.fetchStockCategory(stockItem.stockCategoryName);
        }
      }
    }, 5000);
  }
});

ractive.observe('stockItems', function(newValue) {
  console.info('updated available stockItems, now have: '+newValue.length);
  if (newValue !== undefined) {
    ractive.showActivityIndicator();
    if (ractive.get('current')!==undefined) {
      ractive.enhanceOrderWithStockItems(ractive.get('current'));
    }
    ractive.hideActivityIndicator();
  }
});

ractive.observe('variant', function(newValue, oldValue, keypath) {
  console.info('current prop change: '+newValue +','+oldValue+' '+keypath);
  ractive.updateTitle();
});

ractive.observe('tenant.strings.orders', function(newValue, oldValue, keypath) {
  console.info('current prop change: '+newValue +','+oldValue+' '+keypath);
  ractive.updateTitle();
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete
// controls done that way save the oldValue
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  var ignored = [ 'current.notes', 'current.documents' ];
  if (!ractive.get('saveObserver')) {
    console.debug('Skipped save of '+keypath+' because in middle of other operation');
  } else if (ractive.get('saveObserver') && keypath.indexOf('current.orderItems')!=-1) {
    ractive.saveOrderItem();
  } else if (ractive.get('saveObserver') && keypath.indexOf('current.feedback')!=-1) {
    ractive.saveFeedback();
  } else if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1 && keypath.indexOf('current.orderItems')==-1) {
    ractive.save();
  } else {
    console.warn('Skipped order save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

ractive.observe('current.price', function(newValue, oldValue, keypath) {
  if (!ractive.get('saveObserver')) {
    console.debug('Skipped calculation of tax on '+newValue+' because in middle of other operation');
  }
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  if (newValue!=undefined && newValue !== '') {
    ractive.set('current.tax', parseFloat(String(newValue).replaceAll(/,/g, ''))*ractive.get('taxRate'));
    ractive.set('current.totalPrice', parseFloat(String(newValue).replaceAll(/,/g, ''))*(1+ractive.get('taxRate')));
    $('.autoNumeric').autoNumeric('update');
  }
});


