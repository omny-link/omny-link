/*******************************************************************************
 * Copyright 2018 Tim Stephenson and contributors
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
var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;
var DEFAULT_INACTIVE_STAGES = 'cold,complete,on hold,unqualified,waiting list';

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    contacts: [],
    entityPath: '/orders',
    stockItems: [],
    orders: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    saveObserver:false,
    server: $env.server,
    title: 'Orders',
    variant: 'order',
    customField: function(obj, name) {
      if (obj['customFields']==undefined) {
        return undefined;
      } else if (!Array.isArray(obj['customFields'])) {
        return obj.customFields[name];
      } else {
        var val;
        $.each(obj['customFields'], function(i,d) {
          if (d.name == name) val = d.value;
        });
        return val;
      }
    },
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
      return i18n.getAgeString(ractive.parseDate(timeString))
    },
    formatAccountId: function(contactId) {
      console.info('formatAccountId for contact: '+contactId);
      if (contactId == undefined) return;
      var acctId;
      $.each(Array.findBy('selfRef',contactId,ractive.get('contacts')).links, function(i,d) {
        if (d.rel == 'account') acctId = d.href.substring(d.href.lastIndexOf('/')+1);
      });
      return acctId;
    },
    formatAccountId: function(contactId) {
      console.info('formatContactId');
      if (contactId == undefined) return contactId;
      var contact = Array.findBy('selfRef','/contacts/'+contactId,ractive.get('contacts'));
      return contact == undefined ? 'n/a' : contact.accountName;
    },
    formatContactId: function(contactId) {
      console.info('formatContactId');
      if (contactId == undefined) return contactId;
      var contact = Array.findBy('selfRef','/contacts/'+contactId,ractive.get('contacts'));
      return contact == undefined ? 'n/a' : contact.fullName;
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
    formatDateTime: function(timeString) {
      if (timeString==undefined) return 'n/a';
      var dts = new Date(timeString).toLocaleString(navigator.languages);
      // remove secs
      if (dts.split(':').length>1) dts = dts.substring(0, dts.lastIndexOf(':'));
      return dts;
    },
    formatFavorite: function(obj) {
      if (obj['favorite']) return 'glyphicon-star';
      else return 'glyphicon-star-empty';
    },
    formatId: function(entity) {
      return ractive.id(entity);
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
      for (idx in order.orderItems) {
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
      for (var idx = 0 ; idx < order.orderItems.length ; idx++) {
        stockItemIds.push(order.orderItems[idx].stockItem.id);
      }
      var stockItemNames = '';
      for (var idx = 0 ; idx < stockItemIds.length ; idx++) {
        var tmp = Array.findBy('selfRef','/stock-items/' + stockItemIds[idx],ractive.get('stockItems'));
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
      if (email == undefined) return '';
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    haveCustomExtension: function(extName) {
      return Array.findBy('name',ractive.get('tenant.id')+extName,ractive.get('tenant.partials'))!=undefined;
    },
    helpUrl: '//omny.link/user-help/orders/#the_title',
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
        if (obj['contactName'] == undefined) {
          var contact = Array.findBy('selfRef','/contacts/'+obj.contactId,ractive.get('contacts'));
          if (contact != undefined) obj.contactName = contact.fullName;
        }
        for (var idx = 0 ; idx < search.length ; idx++) {
          var searchTerm = search[idx].toLowerCase();
          var match = ( (obj.selfRef!=undefined && obj.selfRef.indexOf(searchTerm)>=0)
              || (obj.localId!=undefined && searchTerm.indexOf(obj.localId)>=0)
              || (obj.name!=undefined && obj.name.toLowerCase().indexOf(searchTerm)>=0)
              || (searchTerm.startsWith('stage:') && obj.stage!=undefined && obj.stage.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(6))==0)
              || (obj.date!=undefined && searchTerm.startsWith('date>') && new Date(obj.date)>new Date(ractive.get('searchTerm').substring(8)))
              || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8)))
              || (searchTerm.startsWith('created>') && new Date(obj.created)>new Date(ractive.get('searchTerm').substring(8)))
              || (obj.date!=undefined && searchTerm.startsWith('date<') && new Date(obj.date)<new Date(ractive.get('searchTerm').substring(8)))
              || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
              || (searchTerm.startsWith('created<') && new Date(obj.created)<new Date(ractive.get('searchTerm').substring(8)))
              || (searchTerm.startsWith('contactid:') && ractive.get('searchTerm').substring(10).replace(/ /g,'')==''+obj.contactId)
              || (searchTerm.startsWith('contactid:') && ractive.get('searchTerm').substring(10).replace(/ /g,'').split(',').indexOf(''+obj.contactId)!=-1)
              || (obj.contactName!=undefined && obj.contactName.toLowerCase().indexOf(searchTerm)>=0)
              || (obj.stockItem!=undefined && obj.stockItem.name.toLowerCase().indexOf(searchTerm)>=0)
              || (searchTerm.startsWith('owner:') && obj.owner != undefined && obj.owner.indexOf(searchTerm.substring(6))!=-1)
              || (searchTerm.startsWith('active') && (obj.stage==undefined || obj.stage.length==0 || ractive.inactiveStages().indexOf(obj.stage.toLowerCase())==-1))
              || (searchTerm.startsWith('!active') && ractive.inactiveStages().indexOf(obj.stage.toLowerCase())!=-1)
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
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "orderListSect", "url": "/partials/order-list-sect.html"},
      { "name": "orderListTable", "url": "/partials/order-list-table.html"},
      { "name": "currentDocumentListSect", "url": "/webjars/supportservices/3.0.0/partials/current-document-list-sect.html"},
      { "name": "currentNoteListSect", "url": "/webjars/supportservices/3.0.0/partials/current-note-list-sect.html"},
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
    'loginSect': '',
    'orderListSect':'',
    'currentOrderSect': '',
    'poweredBy': '',
    'sidebar': '',
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
        owner: $auth.getClaim('sub'),
        stage: ractive.initialOrderStage(),
        tenantId: ractive.get('tenant.id'),
        type: ractive.get('variant')
    };
    ractive.select( order );
    ractive.initTags();
  },
  addOrderItem: function(order) {
    console.info('addOrderItem: '+ractive.id(order));
    var orderId = ractive.id(order);
    var tmp = ractive.get('itemPrototype')== undefined
        ? { orderId: orderId, customFields: {} }
        : ractive.get('itemPrototype');
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
        success: completeHandler = function(data) {
          ractive.set('saveObserver',true);
          ractive.set('current.ref',data.lastUsed);
          ractive.set('saveObserver',false);
        }
    });
  },
  delete: function (order) {
    var orderId = order['id']==undefined ? ractive.localId(order.selfRef) : order.id;
    console.info('delete '+orderId+'...');

    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/'+orderId,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteOrderItem: function(item,j) {
    console.log('deleteOrderItem ' + (item['id'] == undefined ? 'new item?' : item.id) +'...');
    if (item['id'] == undefined) {
      ractive.splice('current.orderItems', j, 1);
    } else {
      var orderId = ractive.get('current.id');
      $.ajax({
        url: ractive.getServer() + '/'+ractive.get('tenant.id')+'/orders/'+orderId+'/order-items/'+item.id,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.select(ractive.get('current'));
        }
      });
    }
    return false; // cancel bubbling to prevent edit as well as delete
  },
  edit: function (order) {
    console.info('edit: '+ractive.uri(order)+'...');

    ractive.set('currentIdx',ractive.get('orders').indexOf(order));
    ractive.select( order );
  },
  fetch: function() {
    console.info('fetch variant: '+ractive.get('variant'));
    ractive.set('saveObserver', false);
    var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/findByType/'+ractive.get('variant');
    $.ajax({
      dataType: "json",
      url: url,
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('orders', data);
        } else {
          ractive.merge('orders', data['_embedded'].orders);
        }
        ractive.initControls();
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.showSearchMatched();
        ractive.set('saveObserver', true);
      }
    });
    ractive.fetchContacts();
    ractive.fetchStockItems();
    //ractive.fetchStockCategories();
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
  fetchContacts: function () {
    console.info('fetchContacts...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/',
      crossDomain: true,
      success: function( data ) {
        ractive.set('saveObserver', false);
        ractive.set('contacts',data.map(function(obj) {
            obj.name = obj.fullName;
            return obj;
          })
        );
        ractive.addDataList({ name: 'contacts' }, ractive.get('contacts'));
        ractive.set('saveObserver', true);
      }
    });
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
  filter: function(filter) {
    console.info('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected')
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.showSearchMatched();
    $('input[type="search"]').blur();
  },
  hideResults: function() {
    $('#ordersTableToggle').addClass('glyphicon-triangle-right').removeClass('glyphicon-triangle-bottom');
    $('#ordersTable').slideUp();
    $('#currentSect').slideDown({ queue: true });
  },
  inactiveStages: function() {
    var inactiveStages = ractive.get('tenant.serviceLevel.inactiveStages')==undefined
        ? DEFAULT_INACTIVE_STAGES
        : ractive.get('tenant.serviceLevel.inactiveStages').join();
    return inactiveStages;
  },
  oninit: function() {
    console.info('oninit');
    this.on( 'filter', function ( event, filter ) {
      console.info('filter on '+JSON.stringify(event)+','+filter.idx);
      $('.omny-dropdown.dropdown-menu li').removeClass('selected');
      $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected');
      ractive.search(filter.value);
    });
    this.on( 'sortOrder', function ( event, column ) {
      console.info('sortOrder on '+column);
      $( "#ajax-loader" ).show();
      // if already sorted by this column reverse order
      if (this.get('sortOrderColumn')==column) this.set('sortOrderAsc', !this.get('sortOrderAsc'));
      this.set( 'sortOrderColumn', column );
      $( "#ajax-loader" ).hide();
    });
  },
  save: function () {
    console.info('save order: '+ractive.get('current.name')+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      if (tmp.stockItem!=undefined && tmp.stockItem.selfRef!=undefined && tmp.stockItem.id==undefined) tmp.stockItem.id = ractive.id(tmp.stockItem)
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
//      console.log('ready to save order'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/'+tmp.tenantId+'/orders/' : ractive.tenantUri(tmp),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) {
            ractive.set('current._links.self.href',location);
            ractive.set('current.selfRef', location);
            ractive.set('current.orderId', ractive.id(ractive.get('current')));
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
          ractive.showMessage((ractive.get('tenant.strings.'+ractive.get('variant'))==undefined
              ? ractive.get('variant') : ractive.get('tenant.strings.'+ractive.get('variant')))
              +' saved');
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
  saveFeedback: function() {
    console.info('saveFeedback: ...');
    ractive.set('saveObserver', false);
    if (document.getElementById('currentFeedbackForm') == undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentFeedbackForm').checkValidity()) {
      var tmp = ractive.get('current.feedback');
      tmp.orderId = ractive.get('current.id');
      tmp.tenantId = ractive.get('tenant.id');

      console.log('ready to save feedback' + JSON.stringify(tmp) + ' ...');
      $.ajax({
        url: tmp.id === undefined
            ? ractive.tenantUri(ractive.get('current')) + '/feedback'
            : ractive.tenantUri(ractive.get('current')) + '/feedback/'+tmp.id,
        type: tmp.id === undefined ? 'POST': 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('orders.'+ractive.get('currentOrderIdx')+'.feedback._links.self.href',location);
          switch (jqXHR.status) {
          case 201:
            var currentIdx = ractive.get('orders').push(ractive.get('current'))-1;
            break;
          case 204:
            break;
          }
          ractive.showMessage(ractive.get('tenant.strings.feedback')+' saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      var msg = 'Cannot save yet as feedback is invalid';
      console.warn(msg);
      $('#currentFeedbackForm :invalid').addClass('field-error');
      ractive.showMessage(msg);
      ractive.set('saveObserver', true);
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
      tmp.orderId = ractive.id(ractive.get('current'));
      tmp.tenantId = ractive.get('tenant.id');
      console.log('ready to save order item' + JSON.stringify(tmp) + ' ...');
      $.ajax({
        url: tmp.id === undefined
          ? ractive.getServer() + '/' + ractive.get('tenant.id') + '/orders/' + tmp.orderId + '/order-items'
         : ractive.getServer() + '/' + ractive.get('tenant.id') + '/orders/' + tmp.orderId + '/order-items/'+tmp.id,
        type: tmp.id === undefined ? 'POST': 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current.orderItems._links.self.href',location);
//          switch (jqXHR.status) {
//          case 201:
//
//            // TODO selecting the order works for refreshing the order items but messes up subsequent edits which are applied to the wrong item
//         // TODO cannot select directly as have no hateos links
//            //ractive.select(Array.findBy('selfRef', ractive.get('current.id'), ractive.get('orders')));
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
    ractive.set('saveObserver',false);
    // default owner to current user
    if (order['owner'] == undefined || order['owner'] == '') order.owner = $auth.getClaim('sub');
    if (ractive.uri(order) != undefined) {
      console.log('loading detail for '+ractive.uri(order));
      $.getJSON(ractive.tenantUri(order), function( data ) {
        console.log('found order '+data);
        ractive.set('saveObserver',false);
        if (data['id'] == undefined) data.id = ractive.id(data);
        if (data['contactId'] != undefined) data.contact = Array.findBy('selfRef', '/contacts/'+data.contactId, ractive.get('contacts'));
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        if (ractive.get('tenant.features.notesOnOrder')==true) ractive.sortChildren('notes','created',false);
        if (ractive.get('tenant.features.documentsOnOrder')==true) ractive.sortChildren('documents','created',false);
        if (ractive.get('currentOrderItemId')!=undefined) {
          ractive.toggleEditOrderItem(Array.findBy('id',ractive.get('currentOrderItemId'),ractive.get('current.orderItems')));
        }
        var obj = Array.findBy('type','contact',ractive.get('tenant.orderFields'));
        if (obj!=undefined) {
          ractive.updateContactId(ractive.get('current.customFields.'+obj.name), 'current.customFields.'+obj.name+'Id')
        }
        obj = Array.findBy('type','contact',ractive.get('tenant.purchaseOrderFields'));
        if (obj!=undefined) {
          ractive.updateContactId(ractive.get('current.customFields.'+obj.name), 'current.customFields.'+obj.name+'Id')
        }
        ractive.set('saveObserver',true);
      });
    } else {
      console.log('Skipping load as has no identifier.'+order.name);
      ractive.set('current', order);
      ractive.set('saveObserver',true);
    }
    ractive.hideResults();
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showResults: function() {
    $('#ordersTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#ordersTable').slideDown({ queue: true });
  },
  showSearchMatched: function() {
    ractive.set('searchMatched',$('#ordersTable tbody tr').length);
    if ($('#ordersTable tbody tr:visible').length==1) {
      var orderId = $('#ordersTable tbody tr:visible').data('href')
      var order = Array.findBy('id',orderId,ractive.get('orders'))
      ractive.edit( order );
    }
    $( "#ajax-loader" ).hide();
  },
  toggleEditOrderItem: function(orderItem, j) {
    console.info('editOrderItem '+(orderItem['id']==undefined ? 'new item?' : orderItem.id)+'...');

//    ractive.set('currentOrderItemId', orderItem.id);
    ractive.set('currentOrderItemIdx',ractive.get('current.orderItems').indexOf(orderItem));
//    ractive.set('currentOrderItem',Array.findBy('id',orderItem.id,ractive.get('current.orderItems')));

//    $('.currentOrderItemSect').hide();
    $($('.currentOrderItemSect')[j]).toggle();
//    ractive.selectOrderItem( orderItem );

  },
  toggleResults: function() {
    console.info('toggleResults');
    $('#ordersTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#ordersTable').slideToggle();
  },
  updateContactId: function(newVal, keypath) {
    console.info('updateContactId: '+newVal);
    if (newVal==undefined || newVal.length==0) return;
    var newContact = Array.findBy('fullName',newVal,ractive.get('contacts'));
    ractive.set('current.contact', newContact);
    if (keypath == undefined) keypath = 'current.contactId';
    ractive.set(keypath, ractive.id(newContact));
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
  if (params['v']!=undefined) {
    ractive.set('variant',decodeURIComponent(params['v']));
  }
  if (ractive.get('searchTerm')==undefined) {
    ractive.set('searchTerm','updated>'+ractive.daysAgo(7));
  }
  ractive.set('saveObserver', true);
});

ractive.observe('current.stockItem.id', function(newValue, oldValue, keypath) {
  console.info('stock item changed from '+oldValue+' to '+newValue);
  if (newValue != undefined) {
    timerId = setInterval(function() {
      if (ractive.get('stockItems').length==0) {
        ractive.showMessage('Still loading stock items, please wait...', 'alert-warning');
      } else {
        clearInterval(timerId);
        var stockItem = Array.findBy('selfRef','/stock-items/'+newValue,ractive.get('stockItems'));
        if (stockItem != undefined) {
          ractive.fetchStockCategory(stockItem.stockCategoryName);
        }
      }
    }, 5000);
  }
});

ractive.observe('variant', function(newValue, oldValue, keypath) {
  console.info('update variant');
  ractive.updateTitle();
});

ractive.observe('tenant.strings.orders', function(newValue, oldValue, keypath) {
  console.info('update title');
  ractive.updateTitle();
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete
// controls done that way save the oldValue
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  console.info('current prop change: '+newValue +','+oldValue+' '+keypath);
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
  console.info('current price change: '+newValue +','+oldValue);
  if (newValue!=undefined) ractive.set('current.tax', parseFloat(newValue*0.2));
});

ractive.on( 'filter', function ( event, filter ) {
  console.info('filter on '+JSON.stringify(event)+','+filter.idx);
  ractive.filter(filter);
});
ractive.on( 'sort', function ( event, column ) {
  console.info('sort on '+column);
  // if already sorted by this column reverse order
  if (this.get('sortColumn')==column) this.set('sortAsc', !this.get('sortAsc'));
  this.set( 'sortColumn', column );
});

