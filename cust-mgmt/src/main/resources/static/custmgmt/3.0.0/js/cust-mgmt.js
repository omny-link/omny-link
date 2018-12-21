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
var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;
var DEFAULT_INACTIVE_STAGES = 'cold,complete,on hold,unqualified,waiting list';

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    accounts: [],
    entityPath: '/contacts',
    contacts: [],
    title: 'Contact Management',
    alerts: function(selector) {
      console.log('alerts for '+selector);
      return $(selector+' :invalid').length;
    },
    contactStages: [ "New" ],
    customField: function(obj, name) {
      if (obj['customFields']==undefined) {
        return undefined;
      } else if (!Array.isArray(obj['customFields'])) {
        return obj.customFields[name];
      } else {
        var val;
        ractive.set('saveObserver', false);
        $.each(obj['customFields'], function(i,d) {
          if (d.name == name) val = d.value;
        });
        ractive.set('saveObserver', true);
        return val;
      }
    },
    daysAgo: function(noOfDays) {
      return ractive.daysAgo(noOfDays);
    },
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    fields: ["id", "country", "owner", "fullName", "title", "customFields",
              "tenantId", "lastUpdated", "firstName", "lastName", "tags",
              "source", "email", "postCode", "account", "uuid", "phone1",
              "stage", "doNotCall", "doNotEmail", "firstContact", "accountId",
              "phone2", "address1", "address2", "town", "countyOrCity",
              "postCode", "country",
              "enquiryType", "accountType", "medium", "campaign", "keyword",
              "emailConfirmed", "account.name", "account.businessWebsite",
              "account.companyNumber", "account.incorporationYear",
              "account.sic"],
    fieldValidators: {
      "phone1": "^\\+?[0-9, \\-()]{0,15}$",
      "phone2": "^\\+?[0-9, \\-()]{0,15}$"
    },
    formatAge: function(millis) {
      console.info('formatAge: ' + millis);
      if (millis == "-1" || isNaN(millis)) return 'n/a';
      else return i18n.getDurationString(millis) + ' ago';
    },
    formatAgeFromDate: function(timeString) {
      if (timeString==undefined) return;
      return i18n.getAgeString(ractive.parseDate(timeString))
    },
    formatContactId: function(contactId) {
      console.info('formatContactId');
      if (contactId == undefined) return 'n/a';
      if (contactId.endsWith(ractive.get('current.selfRef'))) return ractive.get('current.fullName');

      var contact = Array.findBy('selfRef',contactId,ractive.get('contacts'));
      return contact == undefined ? 'n/a' : contact.fullName;
    },
    formatContent: function(content) {
      console.info('formatContent:'+content);
      content = content.replace(/\n/g,'<br/>');
      content = ractive.autolinker().link(content);
      return content;
    },
    formatDate: function(timeString) {
      if (timeString == undefined || timeString.length==0)
        return 'n/a';
      try {
        var d = ractive.parseDate(timeString);
        return d.toLocaleDateString(navigator.languages);
      } catch (e) {
        return timeString;
      }
    },
    formatDateTime: function(timeString) {
      if (timeString==undefined) return 'n/a';
      var dts = new Date(timeString).toLocaleString(navigator.languages);
      // remove secs
      if (dts.split(':').length>1) dts = dts.substring(0, dts.lastIndexOf(':'));
      return dts;
    },
    formatDueDate: function(date) {
      var diff = Date.parse(date)-new Date().getTime();
      if (diff < 0) return 'alert-danger';
      else if (diff < (1000*60*60*24*30)) return 'alert-warning';
      else return '';
    },
    formatFavorite: function(obj) {
      if (obj['favorite']) return 'glyphicon-star';
      else return 'glyphicon-star-empty';
    },
    formatJson: function(json) {
      console.log('formatJson: '+json);
      try {
        var obj = json;
        if (typeof json == 'string') obj = JSON.parse(json);
        return ractive.json2Html(obj);
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    formatLeiLink: function(obj) {
      if (obj.companyNumber==undefined || obj.companyNumber.length==0) return;
      else if (obj.accountType!=undefined && obj.accountType.length>0 && ractive.partials[obj.accountType.toCamelCase()+'LeiLink'] != undefined) return obj.accountType.toCamelCase()+'LeiLink';
      else if (ractive.partials[ractive.get('tenant.id')+'LeiLink'] != undefined) return ractive.get('tenant.id')+'LeiLink';
      else return 'companyLeiLink';
    },
    formatOrderVariant: function() {
      if (ractive.get('orders') == undefined) {
        var tmp = ractive.get('tenant.strings.orders');
        if (ractive.get('tenant.features.purchaseOrders')==true) tmp+= (' / '+ractive.get('tenant.strings.purchaseOrders'));
        return tmp;
      } else {
        var variants = $.unique(ractive.get('orders').map(function(el) { return el.type; } ));
        if (variants.indexOf('po')!=-1 && variants.indexOf('order')!=-1)
          return ractive.get('tenant.strings.orders')+' / '+ractive.get('tenant.strings.purchaseOrders');
        if (variants.indexOf('po')!=-1)
          return ractive.get('tenant.strings.purchaseOrders');
        else return ractive.get('tenant.strings.orders');
      }
    },
    formatStockItemIds: function(order) {
      // console.info('formatStockItemIds');
      var names = ractive.getStockItemNames(order);
      return names == undefined ? '' : names;
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
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    haveCustomExtension: function(extName) {
      return Array.findBy('name',ractive.get('tenant.id')+extName,ractive.get('tenant.partials'))!=undefined;
    },
    haveStageReasons: function() {
      console.info('haveStageReasons?');
      if (ractive.get('current.stage')!='Cold') return false;
      return ractive.get('tenant.typeaheadControls').filter(function(d) {
        return d.name=='stageReasons';
      }).length > 0;
    },
    helpUrl: '//omny-link.github.io/user-help/contacts/',
    inactiveStages: function() {
      return ractive.inactiveStages();
    },
    lessThan24hAgo: function(isoDateTime) {
      if (isoDateTime == undefined || (new Date().getTime()-new Date(isoDateTime).getTime()) < 1000*60*60*24) {
        return true;
      }
      return false;
    },
    matchPage: function(pageName) {
      console.info('matchPage: '+pageName);
      return document.location.href.indexOf(pageName)!=-1;
    },
    matchRole: function(role) {
      console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    matchSearch: function(obj) {
      //console.info('matchSearch: '+searchTerm);
      if (ractive.get('searchTerm')==undefined || ractive.get('searchTerm').length==0) {
        return true;
      } else {
        var search = ractive.get('searchTerm').split(' ');
        for (var idx = 0 ; idx < search.length ; idx++) {
          var searchTerm = search[idx].toLowerCase();
          var match = ( (obj.selfRef.indexOf(searchTerm)>=0)
            || (obj.firstName.toLowerCase().indexOf(searchTerm)>=0)
            || (obj.lastName.toLowerCase().indexOf(searchTerm)>=0)
            || (searchTerm.indexOf('@')!=-1 && obj.email.toLowerCase().indexOf(searchTerm)>=0)
            || (obj.phone1!=undefined && obj.phone1.indexOf(searchTerm)>=0)
            || (obj.phone2!=undefined && obj.phone2.indexOf(searchTerm)>=0)
            || (obj.accountName!=undefined && obj.accountName.toLowerCase().indexOf(searchTerm)>=0)
            || (searchTerm.startsWith('type:') && obj.accountType!=undefined && obj.accountType.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(5))==0)
            || (searchTerm.startsWith('enquiry:') && obj.enquiryType!=undefined && obj.enquiryType.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(8))==0)
            || (searchTerm.startsWith('stage:') && obj.stage!=undefined && obj.stage.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(6))==0)
            || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('created>') && new Date(obj.firstContact)>new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('created<') && new Date(obj.firstContact)<new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('#') && obj.tags.toLowerCase().indexOf(searchTerm.substring(1))!=-1)
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
    saveObserver: false,
    selectMultiple: [],
    server: $env.server,
    localId: function(selfRef) {
      return ractive.localId(selfRef);
    },
    sort: function (array, column, asc) {
      if (array == undefined) return;
      console.info('sort array of '+(array == undefined ? 0 : array.length)+' items '+(asc ? 'ascending' : 'descending')+' on: '+column);
      array = array.slice(); // clone, so we don't modify the underlying data

      return array.sort( function ( a, b ) {
        if (b[column]==undefined || b[column]==null || b[column]=='') {
          return (a[column]==undefined || a[column]==null || a[column]=='') ? 0 : -1;
        } else if (asc) {
          return (''+a[ column ]).toLowerCase() < (''+b[ column ]).toLowerCase() ? -1 : 1;
        } else {
          return (''+a[ column ]).toLowerCase() > (''+b[ column ]).toLowerCase() ? -1 : 1;
        }
      });
    },
    sortAsc: false,
    sortColumn: 'lastUpdated',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    sortOrderAsc: false,
    sortOrderColumn: 'created',
    sortedOrder: function(column) {
      console.info('sortedOrder');
      if (ractive.get('sortOrderColumn') == column && ractive.get('sortOrderAsc')) return 'sort-asc';
      else if (ractive.get('sortOrderColumn') == column && !ractive.get('sortOrderAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "activityCurrentSect", "url": "/partials/activity-current-sect.html"},
      { "name": "accountFinancials", "url": "/partials/account-financials.html"},
      { "name": "companyLeiLink", "url": "/partials/lei-link-uk-company.html" },
      { "name": "contactListSect", "url": "/partials/contact-list-sect.html"},
      { "name": "contactListTable", "url": "/partials/contact-list-table.html"},
      { "name": "currentAccountSect", "url": "/partials/account-current-sect.html"},
      { "name": "currentContactSect", "url": "/partials/contact-current-sect.html"},
      { "name": "currentContactExtensionSect", "url": "/partials/contact-extension.html"},
      { "name": "currentContactAccountSect", "url": "/partials/contact-current-account-sect.html"},
      { "name": "currentDocumentListSect", "url": "/webjars/supportservices/3.0.0/partials/current-document-list-sect.html"},
      { "name": "currentNoteListSect", "url": "/webjars/supportservices/3.0.0/partials/current-note-list-sect.html"},
      { "name": "currentOrderListSect", "url" : "/partials/contact-current-order-list-sect.html"},
      { "name": "customActionModal", "url": "/partials/custom-action-modal.html"},
      { "name": "fieldExtension", "url": "/partials/field-extension.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "toolbar", "url": "/partials/toolbar.html"},
      { "name": "socialModal", "url": "/partials/social-modal.html" },
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "navbar", "url": "/partials/contact-navbar.html"},
      { "name": "supportBar", "url": "/webjars/supportservices/3.0.0/partials/support-bar.html"}
    ],
    workPartials: [
      { "name": "currentTaskListSect", "url": "/partials/task-list-sect.html"},
      { "name": "instanceListSect", "url": "/partials/instance-list-sect.html"},
      { "name": "taskListTable", "url": "/partials/task-list-table.html"}
    ]
  },
  partials: {
    activityCurrentSect: '',
    contactListTable: '',
    contactListSect: '',
    currentContactExtensionSect: '',
    currentContactSect: '',
    currentOrderListSect: '',
    currentNoteListSect: '',
    customActionModal: '',
    helpModal: '',
    loginSect: '',
    mergeModal: '',
    navbar: '',
    poweredBy: '',
    profileArea: '',
    sidebar: '',
    titleArea: '',
    toolbar: '',
    supportBar: ''
  },
  addContact: function () {
    console.log('addContact...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    ractive.select({
      account: {},
      phone1:'', phone2:'',
      stage: ractive.initialContactStage(),
      tenantId: ractive.get('tenant.id'),
      url: undefined
    });
    ractive.fetchAccounts();
    ractive.initTags();
    ractive.showAlertCounters();
  },
  addSector: function () {
    console.log('addSector ...');
    ractive.showError('Not yet implemented');
    //$('#curPartnerSectors').append($('#sectorTemplate').html());
  },
  addServiceLevelAlerts: function () {
    if (ractive.get('current.stage')==undefined) return;
    $('#curStage').removeClass('alert-danger');
    $('#notes ~ .messages').remove();
    var msgs;
    if (ractive.get('tenant.serviceLevel.initialResponseThreshold')!=0) {
      if (ractive.initialContactStage()==ractive.get('current.stage') && new Date().getTime()-new Date(ractive.get('current.firstContact')).getTime()>(1000*60*60*24*ractive.get('tenant.serviceLevel.initialResponseThreshold'))) {
        $('#curStage').addClass('alert-danger');
        msgs = 'An initial response is expected within '+ractive.get('tenant.serviceLevel.initialResponseThreshold')+' day(s) after which please update the stage.';
      }
    }
    if (ractive.get('inactiveStages')().indexOf(ractive.get('current.stage').toLowerCase())==-1 && ractive.get('tenant.serviceLevel.inactivityReminderThreshold')>0) {
      if (new Date().getTime()-new Date(ractive.get('current.notes.0.created')).getTime()>(1000*60*60*24*ractive.get('tenant.serviceLevel.inactivityReminderThreshold'))) {
        var inactivityMsg = 'An updated note is expected every '+ractive.get('tenant.serviceLevel.inactivityReminderThreshold')+' day(s) unless the lead is set inactive.';
        $('#notes').after('<div class="messages alert-danger">'+inactivityMsg+'</div>');
        if (msgs != undefined) msgs += '<br/>'; else msgs = '';
        msgs += inactivityMsg;
      }
    }
    if (msgs != undefined) ractive.showError(msgs);
  },
  cloneOrder: function(order) {
    var newOrder = JSON.parse(JSON.stringify(order));
    delete newOrder.created;
    delete newOrder.date;
    delete newOrder.id;
    delete newOrder.invoiceRef;
    delete newOrder.links;
    newOrder.localId = undefined;
    delete newOrder.selfRef;
    delete newOrder.lastUpdated;
    newOrder.stage = ractive.initialOrderStage();
    for (var idx = 0 ; idx < newOrder.orderItems.length ; idx++) {
      delete newOrder.orderItems[idx].created;
      delete newOrder.orderItems[idx].customFields.date;
      delete newOrder.orderItems[idx].id;
      delete newOrder.orderItems[idx].links;
      //delete newOrder.orderItems[idx].localId;
      delete newOrder.orderItems[idx].orderItemId;
      delete newOrder.orderItems[idx].selfRef;
      delete newOrder.orderItems[idx].lastUpdated;
    }
    ractive.push('orders', newOrder);
    ractive.set('currentOrderIdx', ractive.get('orders').length-1);
    ractive.saveOrder();
  },
  delete: function (obj) {
    console.log('delete '+obj+'...');
    $.ajax({
        url: ractive.getServer()+'/contacts/'+ractive.id(obj),
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteOrder : function(obj) {
    console.log('deleteOrder ' + obj.selfRef + '...');
    $.ajax({
      url : ractive.getServer() + '/orders/' + ractive.id(obj),
      type : 'DELETE',
      success : completeHandler = function(data) {
        ractive.fetchOrders(ractive.get('current'));
      }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteOrderItem : function(order, item) {
    console.log('deleteOrderItem ' + order.selfRef + ', '+ item.selfRef +'...');
    $.ajax({
      url : ractive.getServer() + '/'+ractive.get('tenant.id')+'/orders/' + ractive.localId(order.selfRef)+ '/order-items/' + ractive.localId(item.selfRef),
      type : 'DELETE',
      success : completeHandler = function(data) {
        ractive.fetchOrders(ractive.get('current'));
      }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  edit: function (contact) {
    console.log('edit'+contact+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('saveObserver',false);
    ractive.set('currentIdx',ractive.get('contacts').indexOf(contact));
    ractive.select( contact );
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+ractive.get('entityPath')+'/',
      crossDomain: true,
      success: function( data ) {
        ractive.set('saveObserver', false);
        if (data['_embedded'] == undefined) {
          ractive.set('contacts', data);
        } else {
          ractive.set('contacts', data['_embedded'].contacts);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) $('.power-user').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        if (ractive.get('tenant.features.orders')) ractive.fetchStockItems();
        ractive.set('contacts',data.map(function(obj) {
            obj.name = obj.fullName;
            return obj;
          })
        );
        ractive.addDataList({ name: 'contacts' }, ractive.get('contacts'));
        ractive.showSearchMatched();
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchAccounts: function () {
    console.info('fetchAccounts...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/account-pairs/',
      crossDomain: true,
      success: function( data ) {
//        if (data['_embedded'] != undefined) {
//          data = data['_embedded'].accounts;
//        }
        ractive.set('saveObserver', false);
        console.log('fetched '+data.length+' accounts for typeahead');
//        ractive.set('accounts',data);
//        var accData = jQuery.map( data, function( n, i ) {
//          return ( {  /*"id": ractive.id(n),*/ "name": n.name } );
//        });
//        ractive.set('accountsTypeahead',data);
        ractive.addDataList({ name: "accounts" }, data);
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchOrders: function(contact) {
    console.info('fetchOrders...');
    if (ractive.get('tenant.features.orders')!=true) return;
    var contactId = ractive.id(contact);

    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/findByContact/'+contactId,
      crossDomain: true,
      success: function( data ) {
        ractive.set('saveObserver', false);
        ractive.set('orders',data);
        console.log('fetched '+data.length+' orders');
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchProcessInstances: function (contactId) {
    console.info('fetchProcessInstances...');

    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/findByVar/contactId/'+contactId,
      crossDomain: true,
      success: function( data ) {
        ractive.set('current.instances',data);
        console.log('fetched '+data.length+' instances');
        ractive.set('saveObserver', false);
        for (idx in data) {
          ractive.push('current.activities', {
            content: data[idx]['processDefinitionId']+': '+data[idx]['businessKey'],
            occurred: "2016-10-10",
            type: "workflow"
          });
        }
        ractive.sortChildren('instances','occurred',false);
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchTasks: function (contactId) {
    console.info('fetchTasks...');

    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/tasks/findByVar/contactId/'+contactId,
      crossDomain: true,
      success: function( data ) {
        console.log('fetched '+data.length+' tasks');
        ractive.set('saveObserver', false);
        ractive.set('xTasks',data);
        ractive.set('current.tasks',data);
        ractive.sortChildren('tasks','dueDate',false);
        ractive.set('saveObserver', true);
        ractive.set('alerts.tasks',data.length);
      }
    });
  },
  findContact: function(contactId) {
    console.log('findContact: '+contactId);
    var c;
    $.each(ractive.get('contacts'), function(i,d) {
      if (contactId.endsWith(ractive.id(d))) {
        c = d;
      }
    });
    return c;
  },
  hideResults: function() {
    $('#contactsTableToggle').removeClass('kp-icon-caret-down').addClass('kp-icon-caret-right');
    $('#contactsTable').slideUp();
    $('#currentSect').slideDown({ queue: true });
  },
  importComplete: function(imported, failed) {
    console.log('inferComplete');
    ractive.showMessage('Import complete added '+imported+' records '+' with '+failed+' failures');
    if (failed==0) {
      ractive.fetch();
      $("#pasteSect").animate({width:'toggle'},EASING_DURATION*2, function() {
        $("#contactsSect").slideDown(EASING_DURATION*2);
      });
    }
  },
  inactiveStages: function() {
    var inactiveStages = ractive.get('tenant.serviceLevel.inactiveStages')==undefined
        ? DEFAULT_INACTIVE_STAGES
        : ractive.get('tenant.serviceLevel.inactiveStages').join();
    return inactiveStages;
  },
  inferDomainName: function() {
    console.info('inferDomainName');
    var email = ractive.get('current.email');
    if (email==undefined) return false;
    var emailDomain = email.substring(email.indexOf('@')+1);
    switch (emailDomain) {
    case 'aol.com':
    case 'btinternet.com':
    case 'gmail.com':
    case 'googlemail.com':
    case 'hotmail.co.uk':
    case 'hotmail.com':
    case 'live.com':
    case 'mac.com':
    case 'outlook.com':
    case 'yahoo.com':
    case 'yahoo.co.uk':
      break;
    default:
      console.log('Assuming this is company-owned domain: '+emailDomain);
      ractive.set('current.account.businessWebsite','http://'+emailDomain);
    }
    console.log('  emailDomain: '+emailDomain);
  },
  mergeContacts: function() {
    console.info('mergeContacts');
    ractive.set('contact1',ractive.get('contacts[0]'));
    ractive.set('contact2',ractive.get('contacts[1]'));
    ractive.set('mergedContact',ractive.get('contacts[2]'));
//    alert('Sorry merge of contacts is not yet implemented');
    $('#mergeModal').modal({});
  },
  oninit: function() {
    console.log('oninit');
    this.on( 'filter', function ( event, filter ) {
      console.info('filter on '+JSON.stringify(event)+','+filter.idx);
      $('.dropdown.dropdown-menu li').removeClass('selected');
      $('.dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected');
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
  pasteInit: function() {
    ractive.set('pasteData',undefined);
    $("#contactsSect").slideUp(EASING_DURATION*2, function() {
      $("#pasteSect").animate({width:'toggle'},EASING_DURATION*2);
    });

    document.addEventListener('paste', function(e){
      console.debug('  '+e.clipboardData.types);
      if(e.clipboardData.types.indexOf('text/plain') > -1){
        ractive.pastePreview(e.clipboardData.getData('text/plain'));
        e.preventDefault(); // We are already handling the data from the clipboard, we do not want it inserted into the document
      }
    });
  },
  pasteDataToObjects: function() {
    console.info('pasteDataToObjects');

    var list = [];
    $.each(ractive.get('pasteData.rows'), function(i,d) {
      var obj = {};
      $.each(ractive.get('pasteData.headers'), function(j,e) {
        console.log('  '+i+':'+d);
        if (ractive.get('fields').indexOf(e)!=-1) {
          if (e.indexOf('.')==-1) {
            obj[e] = d[j];
          } else {
            var orig = obj;
            var elements = e.split('.');
            for (var idx in elements) {
              if (idx==elements.length-1) {
                obj[elements[idx]] = d[j];
              } else {
                if (obj[elements[idx]]==undefined) obj[elements[idx]] = {};
                obj = obj[elements[idx]];
              }
            }
            obj = orig;
          }
        } else if (e.startsWith('customFields.') && d[j]!=undefined && d[j].trim().length>0) {
          if (obj['customFields']==undefined) obj.customFields = {};
          obj.customFields[e.substring(e.indexOf('.')+1)] = d[j];
        }
      });
      if (obj['firstName']==undefined) obj['firstName'] = 'Ann';
      if (obj['lastName']==undefined) obj['lastName'] = 'Other';
      if (obj['email']==undefined) obj['email'] = 'info@example.com';
      if (obj['enquiryType']==undefined) obj['enquiryType'] = 'User Import';

      obj['tenantId'] = ractive.get('tenant.id');
      if (obj['account']!=undefined) obj.account['tenantId'] = ractive.get('tenant.id');

      list.push(obj);
    });
    ractive.set('list',list);
    return list;
  },
  pasteImport: function() {
    console.info('pasteImport');

    var list = ractive.pasteDataToObjects();
    var total = list.length+1;
    var currentIdx = 0;
    var imported = 0;
    var failed = 0;
    var requests = [];

    setInterval(function() {
      if(requests.length > 0) {
        var request = requests.pop();
        if(typeof request === "function") {
          ractive.showMessage('Importing '+(++currentIdx)+' of '+total+' records');
          request(JSON.stringify(list[currentIdx]));
        }
      } else {
        ractive.showMessage('Completed import; '+failed+' failed out of '+total+' attempted');
      }
    }, 3000);

    for(var idx in list) {
      requests.push(function (json) {
        ractive.sendMessage({
          name:"omny.importedContact",
          body:json,
          callback:function(results) {
            console.log('  sendMessage callback...')
            imported++;
            if (imported+failed==list.length) ractive.importComplete(imported, failed);
          },
          pattern:"inOnly"
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
          var msg = "Unable to import record "+idx;
          console.warn('msg:'+msg);
          failed++;
          if (imported+failed==list.length) ractive.importComplete(imported, failed);
        });
      });
    }
  },
  pastePreview: function(data) {
    var rows = data.split("\n");

    for(var y = 0 ; y < rows.length ; y++) {
      if (rows[y]==undefined || rows[y].trim().length==0) continue;
      var cells = rows[y].trim().split("\t");
      if (y==0) {
        ractive.set('pasteData.headers',cells);
      } else {
        ractive.set('pasteData.rows.'+(y-1),cells);
      }
    }

    ractive.pasteValidate();

    $("#pasteZone").animate({width:'toggle'},EASING_DURATION*2);
  },
  pasteValidate: function(data) {
    var valid = true;
    $.each(ractive.get('pasteData.headers'), function(i,d) {
      console.log('  '+i+':'+d);
      if (d.indexOf('customFields')!=-1) {
        console.debug('assume this field is ok:'+d);
      } else if (ractive.get('fields').indexOf(d)==-1) {
        $('#pastePreview th[data-name="'+d+'"] .glyphicon').show();
        valid = false;
      }

      var v = ractive.get('fieldValidators');
      $.each(ractive.get('pasteData.rows'), function(j,e) {
        console.log(j+':'+e[i]);
        if (v[d]!=undefined && e[i]!=undefined && e[i].search(v[d])==-1) {
          //console.error('gotcha!');
          $('#pastePreview tbody tr[data-row="'+j+'"] td[data-col="'+i+'"] .glyphicon').show();
        }
      });
    });
    if (!valid) ractive.showWarning('There are problems with the proposed import, please modify and try again');
  },
  save: function () {
    console.log('save contact: '+ractive.get('current').lastName+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      // cannot save contact and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      //console.log('account: '+JSON.stringify(tmp.account));
      delete tmp.fullName;
      delete tmp.notes;
      delete tmp.documents;
      delete tmp.tasks;
      if (tmp.alerts != undefined && Array.isArray(tmp.alerts)) tmp.alerts = tmp.alerts.join();
      // account save is handled separately
      delete tmp.account;
      delete tmp.accountId;
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save contact'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/'+tmp.tenantId+'/contacts/' : ractive.tenantUri(tmp),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          switch (jqXHR.status) {
          case 201:
            ractive.set('current.fullName',ractive.get('current.firstName')+' '+ractive.get('current.lastName'));
            var currentIdx = ractive.get('contacts').push(ractive.get('current'))-1;
            ractive.set('currentIdx',currentIdx);
            if (ractive.uri(ractive.get('current.account'))==undefined) ractive.saveAccount();
            break;
          case 204:
            ractive.splice('contacts',ractive.get('currentIdx'),1,ractive.get('current'));
            break;
          }
          ractive.showMessage('Contact saved');
          ractive.addServiceLevelAlerts();
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  saveAccount: function () {
    if (ractive.get('current.account')==undefined) return;
    if (ractive.uri(ractive.get('current'))==undefined) {
      ractive.showMessage('You must have created your contact before adding account details');
      return;
    }
    console.log('saveAccount '+ractive.get('current.account.name')+' ...');
    var id = ractive.get('current.accountId');
    console.log(' id: '+id);
    ractive.set('saveObserver',false);
    ractive.set('current.account.tenantId',ractive.get('tenant.id'));
    if (ractive.get('current.account.companyNumber')=='') ractive.set('current.account.companyNumber',undefined);
    if ($('#currentAccountForm:visible').length!=0 && document.getElementById('currentAccountForm').checkValidity()) {
      $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/accounts/'+(id == undefined ? '' : id),
        type: id == undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(ractive.get('current.account')),
        success: completeHandler = function(data, textStatus, jqXHR) {
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current.account.id',location.substring(location.lastIndexOf('/')+1));
          var contactAccountLink = ractive.uri(ractive.get('current'));
          contactAccountLink+='/account';
          console.log(' attempt to link account: '+location+' to '+contactAccountLink);
          if (jqXHR.status == 201) {
            ractive.saveAccountLink(contactAccountLink,location);
          } else if (jqXHR.status == 204) {
            ractive.set('saveObserver',false);
            var currentIdx =ractive.get('currentIdx');
            ractive.splice('contacts',currentIdx,1,ractive.get('current'));
            ractive.showMessage('Account updated');
            ractive.set('saveObserver',true);
          }
          ractive.set('contacts.'+ractive.get('currentIdx')+'.accountName',ractive.get('current.account.name'));
        }
      });
    } else if ($('#currentAccountForm:visible').length!=0) {
      var msg = 'Cannot save yet as account is invalid';
      console.warn(msg);
      $('#currentAccountForm :invalid').addClass('field-error');
      ractive.showMessage(msg);
      ractive.set('saveObserver',true);
    }
  },
  saveAccountLink: function(contactAccountLink, location) {
    console.info('saveAccountLink: '+contactAccountLink+' to '+location);
    $.ajax({
      url: contactAccountLink,
      type: 'PUT',
      contentType: 'text/uri-list',
      data: location,
      success: completeHandler = function(data, textStatus, jqXHR) {
        ractive.set('saveObserver',false);
        console.log('linked account: '+location+' to '+contactAccountLink);
        ractive.select(ractive.get('current'));
        ractive.showMessage('Contact added to Account');
        ractive.set('saveObserver',true);
      }
    });
  },
  saveOrder: function() {
    console.log('save order: ...');
    ractive.set('saveObserver', false);
    if (document.getElementById('currentOrderForm') == undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentOrderForm').checkValidity()) {
      var tmp = ractive.get('orders.'+ractive.get('currentOrderIdx'));
      if (tmp.date == 'n/a') delete tmp.date;
      var contactName = tmp.contactName;
      if (contactName != undefined) {
        tmp.contactId = ractive.localId(Array.findBy('fullName', contactName,ractive.get('current.contacts')).selfRef);
        delete tmp.contactName;
      }
      var id = ractive.uri(tmp) == undefined ? undefined : ractive.id(tmp);
      console.log('ready to save order' + JSON.stringify(tmp) + ' ...');
      $.ajax({
        // TODO cannot use tenantUri() here
        url: id === undefined
            ? ractive.getServer() + '/' + ractive.get('tenant.id') +'/orders/'
            : ractive.getServer() + '/' + ractive.get('tenant.id') + '/orders/' + id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: function(data, textStatus, jqXHR) {
          // console.log('data: '+ data);
          ractive.showMessage('Order saved');
          ractive.fetchOrders(ractive.get('current'));
        }
      });
    } else {
      console.warn('Cannot save yet as order is invalid');
      $('#currentOrderForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as order is incomplete');
      ractive.set('saveObserver', true);
    }
  },
  saveOrderItem: function() {
    console.log('saveOrderItem: ...');
    ractive.set('saveObserver', false);
    if (document.getElementById('currentOrderForm') == undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentOrderForm').checkValidity()) {
      var tmp = ractive.get('orders.'+ractive.get('currentOrderIdx')+'.orderItems.'+ractive.get('currentOrderItemIdx'));
      tmp.tenantId = ractive.get('tenant.id');
      var id = tmp.selfRef;
      console.log('ready to save order item' + JSON.stringify(tmp) + ' ...');
      $.ajax({
        // TODO no use case for creating items (yet)
        url : id === undefined
          ? ractive.getServer() + '/' + tmp.tenantId + '/orders/' + tmp.orderId + '/order-items'
          : ractive.getServer() + '/' + tmp.tenantId + '/orders/' + tmp.orderId + id,
        type : id === undefined ? 'POST' : 'PUT',
        contentType : 'application/json',
        data : JSON.stringify(tmp),
        success : completeHandler = function(data, textStatus, jqXHR) {
          // console.log('data: '+ data);
          ractive.showMessage('Order item saved');
        }
      });
    } else {
      var msg = 'Cannot save yet as order item is invalid';
      console.warn(msg);
      $('#currentOrderForm :invalid').addClass('field-error');
      ractive.showMessage(msg);
      ractive.set('saveObserver', true);
    }
  },
  select: function(contact) {
    console.log('select: '+JSON.stringify(contact));
    ractive.set('saveObserver',false);
//    if (ractive.get('tenant.features.account')
//        && (ractive.get('current.account')==undefined || ractive.get('current.account.name')=='')) {
//      // This is not viable as it causes the UI to hang once have a couple of
//      // thousand accounts. Also a very rare case that it's needed
//      ractive.fetchAccounts();
//    }
    if (contact.account == undefined || contact.account == '') contact.account = new Object();
    // default owner to current user
    if (window['$auth']!=undefined && (contact.owner == undefined || contact.owner == '')) contact.owner = $auth.getClaim('sub');
    // adapt between Spring Hateos and Spring Data Rest
    if (contact._links == undefined && contact.links != undefined) {
      contact._links = contact.links;
      $.each(contact.links, function(i,d) {
        if (d.rel == 'self') contact._links.self = { href:d.href };
      });
    }
    if (contact._links != undefined) {
      var url = ractive.tenantUri(contact); // includes getServer
      if (url == undefined) {
        ractive.showError('No contact selected, please check link');
        return;
      }
      console.log('loading detail for '+url);
      $.getJSON(ractive.tenantUri(contact), function( data ) {
        console.log('found contact '+data);
        ractive.set('saveObserver',false);
        ractive.set('current', data);

        if (ractive.get('tenant.features.orders')) {
          // TODO WIP
          //ractive.fetchStockItems();
          ractive.fetchOrders(ractive.get('current'));
        }
        if (ractive.get('tenant.features.workManagement'))
          ractive.fetchTasks(ractive.localId(ractive.get('current')));

        ractive.initControls();
        ractive.initTags();
        //ractive.fetchNotes();
        ractive.sortChildren('notes','created',false);
//        if (ractive.get('tenant.features.documents')) ractive.fetchDocs();
        ractive.addServiceLevelAlerts();
        ractive.sortChildren('documents','created',false);
        ractive.analyzeEmailActivity(ractive.get('current.activities'));
        if (ractive.get('tenant.features.account')==true
            && (ractive.get('current.account')==undefined || ractive.get('current.account.name')==undefined)) {
          ractive.fetchAccounts();
        } else if (ractive.get('current.account.businessWebsite')==undefined || ractive.get('current.account.businessWebsite')=='') {
          ractive.inferDomainName();
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        ractive.set('saveObserver',true);
      });
    } else {
      console.log('Skipping load as no _links.'+contact.lastName);
      ractive.set('current', contact);
      ractive.set('saveObserver',true);
    }
    ractive.hideResults();
  },
  selectMultiple: function(contact) {
    console.info('selectMultiple: '+contact.selfRef);
    if ($('tr[data-href="'+contact.selfRef+'"] input[type="checkbox"]').prop('checked')) {
      console.log('  checked: '+$('tr[data-href="'+contact.selfRef+'"] input[type="checkbox"]').prop('checked'));
      ractive.push('selectMultiple', contact.selfRef);
    } else {
      var idx = ractive.get('selectMultiple').indexOf(contact.selfRef);
      console.log('  idx: '+idx);
      ractive.splice('selectMultiple', idx, 1);
    }
    console.log('  selectMultiple: '+ractive.get('selectMultiple'));

    // Dis/Enable merge buttons
    $('tr[data-href] .glyphicon-transfer').hide()
    if (ractive.get('selectMultiple').length == 2) {
      $.each(ractive.get('selectMultiple'), function(i,d) {
        $('tr[data-href="'+d+'"] .glyphicon-transfer').show();
      });
    }
  },
  sendMessage: function(msg) {
    console.log('sendMessage: '+msg.name);
    var type = (msg['pattern'] == 'inOut' || msg['pattern'] == 'outOnly') ? 'GET' : 'POST';
    var d = (msg['pattern'] == 'inOut') ? {query:msg['body']} : {json:msg['body']};
    console.log('d: '+d);
    //var d['businessDescription']=ractive.get('message.bizKey');
    return $.ajax({
      url: ractive.getServer()+'/msg/'+ractive.get('tenant.id')+'/'+msg.name+'/',
      type: type,
      data: d,
      dataType: 'text',
      success: completeHandler = function(data) {
        console.log('Message received:'+data);
        if (msg['callback']!=undefined) msg.callback(data);
      },
    });
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showAlertCounters: function() {
    console.info('showAlertCounters');
    //$('.alert-counter').remove();
//    ractive.set('alerts',{});
    var alerts = {
      account:$('#currentAccount :invalid').length,
      activities:$('#activitySect :invalid').length,
      activityAnalysis:$('#activityAnalysisSect :invalid').length,
      budget:$('#budgetSect :invalid').length,
      connections:$('#connectionsSect :invalid').length,
      documents:$('#documentsTable .alert-danger').length,
      notes:$('#notesTable .alert-danger, #notesSect .messages.alert-danger').length,
    }
    ractive.set('alerts',alerts);
  },
  showResults: function() {
    $('#contactsTableToggle').addClass('kp-icon-caret-down').removeClass('kp-icon-caret-right');
    $('#currentSect').slideUp();
    $('#contactsTable').slideDown({ queue: true });
  },
  showSearchMatched: function() {
    ractive.set('searchMatched',$('#contactsTable tbody tr').length);
    if ($('#contactsTable tbody tr:visible').length==1) {
      var contactId = $('#contactsTable tbody tr:visible').data('href')
      var contact = Array.findBy('selfRef',contactId,ractive.get('contacts'))
      ractive.select( contact );
    }
  },
  toggleResults: function() {
    console.info('toggleResults');
    $('#contactsTableToggle').toggleClass('kp-icon-caret-down').toggleClass('kp-icon-caret-right');
    $('#contactsTable').slideToggle();
  },
  /**
   * Inverse of editField.
   */
  updateField: function (selector, path) {
    var tmp = $(selector).text();
    console.log('updateField '+path+' to '+tmp);
    ractive.set(path,tmp);
    $(selector).css('border-width','0px').css('padding','0px');
  },
  upload: function (formId) {
    console.log('upload:'+formId);
    ractive.showMessage('Uploading ...');

    var formElement = document.getElementById(formId);
    var formData = new FormData(formElement);
    var entity = $('#'+formId+' .entity').val();
    var fileName = $('#'+formId+' input[type="file"]').val();
    var fileExt = fileName.substring(fileName.lastIndexOf('.')+1);
    return $.ajax({
        type: 'POST',
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/'+entity+'/upload'+fileExt.toLowerCase(),
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
  //        console.log('successfully uploaded data');
          ractive.showMessage('Successfully uploaded '+response.length+' records');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          console.error(textStatus+': '+errorThrown);
          ractive.showError('Something went wrong with that upload, please talk to your administrator');
        }
      });
  }
});

$(document).ready(function() {
  ractive.set('saveObserver', false);

  ractive.observe('profile', function(newValue, oldValue, keypath) {
    console.log('profile changed');
    if ((ractive.get('searchTerm') == undefined || ractive.get('searchTerm').length==0) && newValue!=undefined) {
      $('.dropdown.dropdown-menu li:nth-child(3)').addClass('selected');
      ractive.search('active owner:'+newValue.id);
    }
  });

  ractive.observe('tenant.features.workManagement', function(newValue, oldValue, keypath) {
    if (newValue == true) ractive.loadStandardPartials(ractive.get('workPartials'));
  });

  // Save on model change
  // done this way rather than with on-* attributes because autocomplete
  // controls done that way save the oldValue
  ractive.observe('current.*', function(newValue, oldValue, keypath) {
    if (ractive.get('current')!=undefined) ractive.showAlertCounters();
    ignored = [ 'current.notes', 'current.documents' ];
    if (ractive.get('saveObserver') != true) {
      console.debug('Skipped save of '+keypath+' because in middle of other operation');
  //  } else if (newValue == '' && oldValue == undefined) {
  //    console.warn('Skipped contact save during load, should not have got here really');
    } else if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
      console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
      if (keypath=='current.account') ractive.saveAccount();
      else ractive.save();
    } else {
      console.warn('Skipped contact save of '+keypath);
      //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
      //console.log('  saveObserver: '+ractive.get('saveObserver'));
    }
  });

  ractive.observe('current.stage', function(newValue, oldValue, keypath) {
    console.log('stage changing from '+oldValue+' to '+newValue);
    if (newValue=='Cold' && ractive.get('current.stageDate')==undefined) {
      ractive.set('current.stageDate',new Date());
    }
  });
  ractive.set('saveObserver', true);
});

ractive.observe('current.account.businessWebsite', function(newValue, oldValue, keypath) {
  console.log('account businessWebsite changing from '+oldValue+' to '+newValue);
  if (newValue!=undefined && newValue!='' && !(newValue.startsWith('http') || newValue.startsWith('https'))) {
    ractive.set('current.account.businessWebsite','http://'+newValue);
  }
});

// Cannot work due to http://docs.ractivejs.org/0.5/observers#a-gotcha-to-be-aware-of
function significantDifference(newValue,oldValue) {
//  if (newValue=='') { console.log('new value is empty');newValue = null;}
//  if (oldValue=='') { console.log('oldvalue is empty');oldValue = null;}
  var newVal = JSON.stringify(newValue);
  var oldVal = JSON.stringify(oldValue);
  console.debug('sig diff between  '+newVal+' and '+oldVal+ ': '+(newVal!=oldVal));
  return newValue != oldValue;
}
