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
    server: $env.server,
  }),
      el: 'container',
      lazy: true,
      template: '#template',
      data: {
        accounts: [],
        entityName: 'account',
        entityPath: '/accounts',
        contacts: [],
        orders: [],
        title: 'Account Management',
        index: function(a,b) { return index(a,b); }, // jshint ignore:line
        sum: function(fieldName, arr) {
           console.info('sum: '+fieldName);
           return arr.reduce(function (a,b) {
             return a + (isNaN(parseInt(ractive.index(b,fieldName))) ? 0 : parseInt(ractive.index(b,fieldName)));
           }, 0);
        },
        alerts: function(selector) {
          console.log('alerts for ' + selector);
          return $(selector + ' :invalid').length;
        },
        customField: function(obj, name) {
          if (!('customFields' in obj)) {
            return undefined;
          } else if (!Array.isArray(obj.customFields)) {
            return obj.customFields[name];
          } else {
            var val;
            $.each(obj.customFields, function(i, d) {
              if (d.name == name) {
                val = d.value;
              }
            });
            return val;
          }
        },
        daysAgo: function(noOfDays) {
          return new Date(new Date().setDate(new Date().getDate() - noOfDays)).toISOString().substring(0,10);
        },
        featureEnabled: function(feature) {
          console.log('featureEnabled: '+feature);
          if (feature==undefined || feature.length==0) return true;
          else return ractive.get('tenant.features.'+feature);
        },
        fields: [ "id", "country", "owner", "fullName", "title",
            "customFields", "tenantId", "lastUpdated", "firstName", "lastName",
            "tags", "source", "email", "postCode", "account", "uuid", "phone1",
            "stage", "doNotCall", "doNotEmail", "firstContact", "accountId",
            "phone2", "address1", "address2", "town", "countyOrCity",
            "postCode", "country", "enquiryType", "accountType", "medium",
            "campaign", "keyword", "emailConfirmed", "account.name",
            "account.businessWebsite", "account.companyNumber",
            "account.incorporationYear", "account.sic" ],
        fieldValidators: {
          "phone1": "^\\+?[0-9, \\-()]{0,15}$",
          "phone2": "^\\+?[0-9, \\-()]{0,15}$"
        },
        findDocName: function(docId) {
          console.info('findDocName: ' + docId);
        },
        formatAlertCount: function(alerts) {
          console.log('formatAlertCount');
          if (typeof alerts == 'string') alerts = JSON.parse(alerts);
          var val = 0;
          return alerts == undefined ? 0 : Object.keys(alerts).reduce(function (prev, cur) {
            return prev + parseInt(alerts[cur]);
          }, val);
        },
        formatAge: function(timeString) {
          console.info('formatAge: ' + timeString);
          if (timeString == "-1" || isNaN(timeString)) return 'n/a';
          else return i18n.getDurationString(timeString) + ' ago';
        },
        formatAgeFromDate: function(timeString) {
          if (timeString==undefined) return;
          return i18n.getAgeString(ractive.parseDate(timeString));
        },
        formatContactId: function(contactId) {
          console.info('formatContactId');
          if (contactId == undefined) return 'n/a';

          var contact = Array.findBy('id',contactId,ractive.get('current.contacts'));
          return contact == undefined ? 'n/a' : contact.fullName;
        },
        formatContent: function(content) {
          // console.info('formatContent');
          if (content == undefined)
            return '';
          content = content.replace(/\n/g, '<br/>');
          content = Autolinker.link(content);
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
          if (timeString == undefined)
            return 'n/a';
          return new Date(timeString).toLocaleString(navigator.languages);
        },
        formatDueDate: function(date) {
          var diff = Date.parse(date) - new Date().getTime();
          if (diff < 0)
            return 'alert-danger';
          else if (diff < (1000 * 60 * 60 * 24 * 30))
            return 'alert-warning';
          else
            return '';
        },
        formatFavorite: function(obj) {
          if (obj == undefined)
            return '';
          else if (obj.favorite)
            return 'glyphicon-star';
          else
            return 'glyphicon-star-empty';
        },
        formatJson: function(json) {
          console.log('formatJson: ' + json);
          try {
            var obj = JSON.parse(json);
            var html = '';
            $.each(Object.keys(obj), function(i, d) {
              html += (typeof obj[d] == 'object' ? '' : '<b>'+d+'</b>: '+obj[d]+'<br/>');
            });
            return html;
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
        formatParentLeiLink: function(obj) {
          if (obj.parentOrg==undefined || obj.parentOrg.length==0) return;
          else if (ractive.partials[ractive.get('tenant.id')+'ParentLeiLink'] != undefined) return ractive.get('tenant.id')+'ParentLeiLink';
          else return 'companyLeiLink';
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
          var val='';
          for (var idx = 0 ; idx < order.orderItems.length ; idx++) {
            if (order.orderItems[idx].customFields[fieldName]==undefined) continue;
            var tmp = order.orderItems[idx].customFields[fieldName];
            if (tmp != undefined && val.indexOf(tmp)==-1) {
              if (val.length > 0 && tmp!='-' && tmp!='n/a') val += ',';
              if (tmp!='-' && tmp!='n/a') val += tmp;
            }
          }
          return val;
        },
        formatStockItemIds: function(order) {
          console.info('formatStockItemIds');
          return ractive.getStockItemNames(order);
        },
        formatTags: function(tags) {
          var html = '';
          if (tags == undefined)
            return html;
          var tagArr = tags.split(',');
          $.each(tagArr, function(i, d) {
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
        haveStageReasons: function() {
          console.info('haveStageReasons?');
          if (ractive.get('current.stage') != 'Cold')
            return false;
          return ractive.get('tenant.typeaheadControls').filter(function(d) {
            return d.name == 'stageReasons';
          }).length > 0;
        },
        helpUrl: '//omny-link.github.io/user-help/accounts/#the_title',
        inactiveStages: function() {
          return ractive.inactiveStages();
        },
        lessThan24hAgo: function(isoDateTime) {
          if (isoDateTime == undefined ||
              (new Date().getTime() - new Date(isoDateTime).getTime()) < 1000 * 60 * 60 * 24) {
            return true;
          }
          return false;
        },
        localId: function(objOrUri) {
          return ractive.localId(objOrUri);
        },
        matchPage: function(pageName) {
          console.info('matchPage: ' + pageName);
          return document.location.href.indexOf(pageName) != -1;
        },
        matchRole: function(role) {
          if (role == undefined || ractive.hasRole(role)) {
            $('.' + role).show();
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
              var match = ( (obj.id != undefined && searchTerm.indexOf(obj.id)>=0) ||
                (obj.name != undefined && obj.name.toLowerCase().indexOf(searchTerm) >= 0) ||
                (obj.orgCode != undefined && obj.orgCode.toLowerCase().indexOf(searchTerm) >= 0) ||
                (obj.email != undefined && obj.email.toLowerCase().indexOf(searchTerm) >= 0) ||
                (obj.phone1 != undefined && obj.phone1.indexOf(searchTerm) >= 0) ||
                (obj.phone2 != undefined && obj.phone2.indexOf(searchTerm) >= 0) ||
                (obj.accountName != undefined && obj.accountName.toLowerCase().indexOf(searchTerm) >= 0) ||
                (obj.account!=undefined && obj.account.name!=undefined && obj.account.name.toLowerCase().indexOf(searchTerm)>=0) ||
                (searchTerm.startsWith('type:') && obj.accountType!=undefined && obj.accountType.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(5))==0) ||
                (searchTerm.startsWith('enquiry:') && obj.enquiryType!=undefined && obj.enquiryType.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(8))==0) ||
                (searchTerm.startsWith('stage:') && obj.stage!=undefined && obj.stage.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.replace(/ /g,'_').substring(6))==0) ||
                (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated) > new Date(searchTerm.substring(8))) ||
                (searchTerm.startsWith('created>') && new Date(obj.firstContact) > new Date(searchTerm.substring(8))) ||
                (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated) < new Date(searchTerm.substring(8))) ||
                (searchTerm.startsWith('created<') && new Date(obj.firstContact) < new Date(searchTerm.substring(8))) ||
                (searchTerm.startsWith('#') && obj.tags != undefined && obj.tags.toLowerCase().indexOf(searchTerm.substring(1)) != -1)) ||
                (searchTerm.startsWith('owner:') && obj.owner!=undefined && obj.owner.indexOf(searchTerm.substring(6))!=-1) ||
                (searchTerm.startsWith('active') && (obj.stage==undefined || obj.stage.length==0 || ractive.inactiveStages().indexOf(obj.stage.toLowerCase())==-1)) ||
                (searchTerm.startsWith('!active') && ractive.inactiveStages().indexOf(obj.stage.toLowerCase())!=-1)
              ;
              //no match is definitive but matches may fail other terms (AND logic)
              if (!match) return false;
            }
            return true;
          }
        },
        saveObserver: false,
        selectMultiple: [],
        server: $env.server,
        sort: function(array, column, asc) {
          return ractive.sortBy(array, column, asc);
        },
        sortAsc: false,
        sortColumn: 'lastUpdated',
        sorted: function(column) {
          console.info('sorted');
          if (ractive.get('sortColumn') == column && ractive.get('sortAsc'))
            return 'sort-asc';
          else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc'))
            return 'sort-desc';
          else
            return 'hidden';
        },
        sortContactAsc: false,
        sortContactColumn: 'created',
        sortedContact: function(column) {
          console.info('sortedContact');
          if (ractive.get('sortContactColumn') == column && ractive.get('sortContactAsc'))
            return 'sort-asc';
          else if (ractive.get('sortContactColumn') == column && !ractive.get('sortContactAsc'))
            return 'sort-desc';
          else
            return 'hidden';
        },
        sortOrderAsc: false,
        sortOrderColumn: 'created',
        sortedOrder: function(column) {
          console.info('sortedOrder');
          if (ractive.get('sortOrderColumn') == column && ractive.get('sortOrderAsc'))
            return 'sort-asc';
          else if (ractive.get('sortOrderColumn') == column && !ractive.get('sortOrderAsc'))
            return 'sort-desc';
          else
            return 'hidden';
        },
        stdPartials: [
          { "name": "activityCurrentSect", "url": "/partials/activity-current-sect.html"},
          { "name": "accountFinancials", "url": "/partials/account-financials.html" },
          { "name": "accountListSect", "url": "/partials/account-list-sect.html" },
          { "name": "accountListTable", "url": "/partials/account-list-table.html" },
          { "name": "companyLeiLink", "url": "/partials/lei-link-uk-company.html" },
          { "name": "currentAccountSect", "url": "/partials/account-current-sect.html" },
          { "name": "currentContactAccountSect", "url": "/partials/contact-current-account-sect.html" },
          { "name": "currentDocumentListSect", "url": "/partials/current-document-list-sect.html" },
          { "name": "currentNoteListSect", "url": "/partials/current-note-list-sect.html" },
          { "name": "currentOrderListSect", "url": "/partials/contact-current-order-list-sect.html" },
          { "name": "currentTaskListSect", "url": "/partials/task-list-sect.html" },
          { "name": "customActionModal", "url": "/partials/custom-action-modal.html" },
          { "name": "fieldExtension", "url": "/partials/field-extension.html"},
          { "name": "helpModal", "url": "/partials/help-modal.html" },
          { "name": "profileArea", "url": "/partials/profile-area.html" },
          { "name": "socialModal", "url": "/partials/social-modal.html" },
          { "name": "sidebar", "url": "/partials/sidebar.html" },
          { "name": "titleArea", "url": "/partials/title-area.html" },
          { "name": "navbar", "url": "/partials/account-navbar.html" },
          { "name": "supportBar", "url": "/partials/support-bar.html" }
        ],
        workPartials: [
          { "name": "currentTaskListSect", "url": "/partials/task-list-sect.html" },
          { "name": "instanceListSect", "url": "/partials/instance-list-sect.html" },
          { "name": "taskListTable", "url": "/partials/task-list-table.html" }
        ],
        uniq: function(fieldName, arr) {
          return Array.uniq(fieldName, arr);
        }
      },
      partials: {
        activityCurrentSect: '',
        accountListTable: '',
        accountListSect: '',
        currentAccountSect: '',
        currentOrderListSect: '',
        currentNoteListSect: '',
        customActionModal: '',
        helpModal: '',
        mergeModal: '',
        navbar: '',
        poweredBy: '',
        profileArea: '',
        sidebar: '',
        titleArea: '',
        supportBar: ''
      },
      add: function() {
        console.log('add...');
        $('h2.edit-form,h2.edit-field').hide();
        $('.create-form,create-field').show();
        var acct = {
          name: '',
          author: ractive.get('profile.username'),
          tenantId: ractive.get('tenant.id'),
          url: undefined
        };
        ractive.select(acct);
      },
      addContact: function() {
        console.log('addContact ');
        ractive.set('saveObserver', false);
        if (ractive.get('current.contacts')==undefined) ractive.set('current.contacts', []);

        var obj = {
          account: ractive.uri(ractive.get('current')),
          mainContact: ractive.get('current.contacts').length==0 ? true: false,
          owner: ractive.get('profile.username'),
          tenant: ractive.get('tenant.id')
        };
        ractive.get('current.contacts').splice(0, 0, obj);

        ractive.toggleEditContact(obj);

        ractive.set('saveObserver', true);
        if ($('#accountContactsSect div:visible').length == 0)
          $('#accountContactsSect .ol-collapse').click();
      },
      addOrder: function() {
        console.log('addOrder ...');
        if (ractive.get('current.contacts').length == 0) {
          ractive.showWarning('There are no contacts yet, please create one before trying to add an order');
          return;
        }
        ractive.set('saveObserver', false);

        var obj = {
          account: ractive.uri(ractive.get('current')),
          tenantId: ractive.get('tenant.id'),
          orderItems: [],
          stage: ractive.initialOrderStage()
        };
        ractive.get('orders').splice(0, 0, obj);
        ractive.toggleEditOrder(obj);

        ractive.set('saveObserver', true);
        if ($('#orderSect div:visible').length == 0) $('#orderSect .ol-collapse').click();
      },
      addOrderItem: function(orderId) {
        console.info('addOrderItem: '+orderId);
        var tmp = ractive.get('itemPrototype')== undefined ? {} : ractive.get('itemPrototype');
        tmp.orderId = ractive.localId(orderId);
        ractive.set('currentOrderIdx',ractive.get('orders').indexOf(Array.findBy('id',orderId,ractive.get('orders'))));
        ractive.push('orders.'+ractive.get('currentOrderIdx')+'.orderItems', tmp);
        ractive.set('currentOrderItemIdx',ractive.get('orders.'+ractive.get('currentOrderIdx')+'.orderItems').length-1);
        ractive.saveOrderItem();
      },
      addOrderItems: function(orderId, form) {
        var order = Array.findBy('id',orderId,ractive.get('orders'));
        ractive.set('currentOrderIdx', ractive.get('orders').indexOf(order));
        var label = 'Add order items';
        if (ractive.get('tenant.strings.addOrderItems')!=undefined) label = ractive.get('tenant.strings.addOrderItems');
        ractive.startCustomAction('AddOrderItems', label, order, form, label);
      },
      addServiceLevelAlerts: function() {
        if (ractive.get('current.stage') == undefined)
          return;
        $('#curStage').removeClass('alert-danger');
        $('#notes ~ .messages').remove();
        var msgs;
        if (ractive.get('tenant.serviceLevel.initialResponseThreshold') != 0) {
          if (ractive.initialAccountStage() == ractive.get('current.stage') &&
              new Date().getTime() - new Date(ractive.get('current.firstContact')).getTime() >
              (1000 * 60 * 60 * 24 * ractive.get('tenant.serviceLevel.initialResponseThreshold'))) {
            $('#curStage').addClass('alert-danger');
            msgs = 'An initial response is expected within ' +
                ractive.get('tenant.serviceLevel.initialResponseThreshold') +
                ' day(s) after which please update the stage.';
          }
        }
        if (ractive.get('inactiveStages')().indexOf(
            ractive.get('current.stage').toLowerCase()) == -1 &&
            ractive.get('tenant.serviceLevel.inactivityReminderThreshold') > 0) {
          if (new Date().getTime() - new Date(ractive.get('current.notes.0.created')).getTime() >
              (1000 * 60 * 60 * 24 * ractive.get('tenant.serviceLevel.inactivityReminderThreshold'))) {
            var inactivityMsg = 'An updated note is expected every ' +
                ractive.get('tenant.serviceLevel.inactivityReminderThreshold') +
                ' day(s) unless the lead is set inactive.';
            $('#notes').after(
                '<div class="messages alert-danger">' + inactivityMsg + '</div>');
            if (msgs != undefined)
              msgs += '<br/>';
            else
              msgs = '';
            msgs += inactivityMsg;
          }
        }
        if (msgs != undefined)
          ractive.showError(msgs);
      },
      applyAccessControl: function() {
        ractive.update('#currentSect'); // matchRole may return different now
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) {
          document.querySelectorAll('.power-user [readonly]').forEach(function(d) {
            d.removeAttribute('readonly');
          });
          document.querySelectorAll('.power-user [disabled]').forEach(function(d) {
            d.removeAttribute('disabled');
          });
        }
      },
      cloneOrder: function(order) {
        console.log('cloneOrder, id: '+ order.id+', name: '+order.name);
        var newOrder = JSON.parse(JSON.stringify(order));
        delete newOrder.created;
        delete newOrder.date;
        delete newOrder.id;
        delete newOrder.invoiceRef;
        delete newOrder.links;
        delete newOrder.selfRef;
        delete newOrder.lastUpdated;
        newOrder.stage = ractive.initialOrderStage();
        for (var idx = 0 ; idx < newOrder.orderItems.length ; idx++) {
          delete newOrder.orderItems[idx].created;
          delete newOrder.orderItems[idx].customFields.date;
          delete newOrder.orderItems[idx].id;
          delete newOrder.orderItems[idx].links;
          delete newOrder.orderItems[idx].orderItemId;
          delete newOrder.orderItems[idx].selfRef;
          delete newOrder.orderItems[idx].lastUpdated;
        }
        ractive.push('orders', newOrder);
        ractive.set('currentOrderIdx', ractive.get('orders').length-1);
        ractive.saveOrder();
      },
      edit: function(acct) {
        console.log('edit acct: ' + ractive.localId(acct) + '...');
        $('h2.edit-form,h2.edit-field').show();
        $('.create-form,create-field').hide();
        ractive.set('saveObserver', false);
        ractive.set('current.id', ractive.localId(acct));
        ractive.set('currentIdx', ractive.get(
            ractive.get('entityPath').substring(1)).indexOf(acct));
        ractive.select(acct);
      },
      editField: function(selector, path) {
        console.log('editField ' + path + '...');
        $(selector).css('border-width', '1px').css('ractive.toggleResults();padding',
            '5px 10px 5px 10px');
      },
  enhanceOrdersWithContact: function(orders) {
    orders.map(function(obj) {
      if ('contactId' in obj) {
        obj.contact = Array.findBy('id',obj.contactId,ractive.get('current.contacts'));
      }
      return obj;
    });
  },
      deleteAccount: function(obj) {
        console.log('delete ' + obj + '...');
        $.ajax({
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/accounts/' + ractive.id(obj),
          type: 'DELETE',
          success: function() {
            ractive.fetch();
            ractive.showResults();
          }
        });
        return false; // cancel bubbling to prevent edit as well as delete
      },
      deleteContact: function(obj) {
        console.log('delete ' + obj + '...');
        if (Array.findBy('contactId', ractive.localId(obj), ractive.get('orders'))==undefined) {
          $.ajax({
            url: ractive.tenantUri(obj),
            type: 'DELETE',
            contentType: 'application/json',
            success: function() {
              ractive.fetchAccountContacts();
            }
          });
        } else {
          ractive.showError('Cannot delete contact while it has '+(ractive.get('tenant.strings.orders') == null ? 'orders': ractive.get('tenant.strings.orders').toLowerCase()));
        }
        return false; // cancel bubbling to prevent edit as well as delete
      },
      deleteOrder: function(obj) {
        console.log('deleteOrder ' + ractive.localId(obj) + '...');
        $.ajax({
          url: ractive.getServer() + '/orders/' + ractive.localId(obj),
          type: 'DELETE',
          success: function() {
            ractive.fetchOrders(ractive.get('current'));
          }
        });
        return false; // cancel bubbling to prevent edit as well as delete
      },
      deleteOrderItem: function(item) {
        console.log('deleteOrderItem ' + item.orderId + ', '+ ractive.localId(item) +'...');
        $.ajax({
          url: ractive.getServer() + '/'+ractive.get('tenant.id')+'/orders/'+item.orderId+'/order-items/'+ractive.localId(item),
          type: 'DELETE',
          success: function() {
            ractive.fetchOrders(ractive.get('current'));
          }
        });
        return false; // cancel bubbling to prevent edit as well as delete
      },
      fetch: function() {
        console.info('fetch...');
        if (ractive.get('fetchPrimaryEntityList')==true) {
          console.warn('skipping fetch as already running');
          return;
        } else {
          ractive.set('saveObserver', false);
          ractive.set('fetchPrimaryEntityList', true);
          var entityName = ractive.get('entityPath').substring(1);
          $( "#ajax-loader" ).show();
          ractive.cm.fetchAccounts(ractive.get('tenant.id'))
            .then(function(data) {
              if ('_embedded' in data) {
                ractive.set(entityName, data._embedded[entityName]);
              } else {
                ractive.set(entityName, data);
              }
              if (ractive.hasRole('admin')) $('.admin').show();
              if (ractive.hasRole('power-user')) $('.power-user').show();
              if (ractive.fetchCallbacks != null) ractive.fetchCallbacks.fire();
              ractive.showSearchMatched();
              ractive.set('saveObserver', true);
              ractive.set('fetchPrimaryEntityList', false);
              $( "#ajax-loader" ).hide();
          });
        }
      },
      fetchAccountContacts: function() {
        console.info('fetchAccountContacts...');
        ractive.set('saveObserver', false);
        $.ajax({
          dataType: "json",
          url: ractive.getServer() + '/' + ractive.get('tenant.id') +
              '/contacts/findByAccountId?accountId=' +
              ractive.id(ractive.get('current')),
          crossDomain: true,
          success: function(data) {
            ractive.set('saveObserver', false);
            ractive.set('current.contacts', data);
            var contactData = jQuery.map(data, function(n) {
              return ({
                "id": ractive.id(n),
                "name": n.fullName
              });
            });
            ractive.set('contactsTypeahead', contactData);
            console.log('fetched ' + data.length + ' contacts for account');
            ractive.set('saveObserver', true);
            if (ractive.get('tenant.features.orders'))
              ractive.fetchOrders(ractive.get('current'));
          }
        });
      },
      fetchFeedback: function(order) {
        console.info('fetchFeedback...');
        if (ractive.get('tenant.features.orders')!=true) return;
        var orderId = ractive.id(order);

        ractive.set('saveObserver', false);
        $.ajax({
          dataType: "json",
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/'+orderId+'/feedback',
          crossDomain: true,
          success: function(data) {
            ractive.set('saveObserver', false);
            var orderIdx = ractive.get('orders').indexOf(Array.findBy('id','/orders/'+orderId,ractive.get('orders')));
            ractive.set('orders.'+orderIdx+'.feedback',data);
            ractive.set('saveObserver', true);
          },
          error: function(jqXHR) {
            console.debug("No feedback, that's ok, change status to 200");
            jqXHR.status = 200;
          }
        });
      },
      fetchOrders: function(account) {
        console.info('fetchOrders...');
        if (ractive.get('tenant.features.orders') != true)
          return;

        ractive.set('saveObserver', false);

        var contactIds = '';
        for (let idx in account.contacts) {
          if (typeof account.contacts[idx] != 'function') {
            if (contactIds.length>0) contactIds += ',';
            contactIds += ractive.localId(account.contacts[idx]);
          }
        }
        if (contactIds.length == 0) return; // no contacts means no orders
        ractive.set('current.contactIds',contactIds);

        $.ajax({
          dataType: "json",
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/findByContacts/'+contactIds,
          crossDomain: true,
          success: function(data) {
            if ('_embedded' in data) {
              data = data._embedded.orders;
            }
            ractive.set('saveObserver', false);
            try {
              console.log('fetched ' + data.length + ' orders');
              ractive.enhanceOrdersWithContact(data);

              // sort order items
              data.forEach(function(d) {
                let sortedItems = [];
                d.orderItems.forEach(function(e) {
                  console.debug('  {} has index {}', e.id, e.index);
                  if (e.index != undefined) {
                    sortedItems[e.index-1] = e;
                  } else {
                    console.warn(' cannot sort item {} without index, continuing', e.id);
                    sortedItems = d.orderItems;
                    return;
                  }
                });
                d.orderItems = sortedItems;
                d.stockItemNames = ractive.getStockItemNames(d);
              });

              ractive.set('orders', data);
              ractive.update('orders');
            } catch (e) {
              console.error(e);
            }
            ractive.set('saveObserver', true);
          }
        });
      },
      fetchProcessInstances: function(contactId) {
        console.info('fetchProcessInstances...');

        ractive.set('saveObserver', false);
        $.ajax({
          dataType: "json",
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+
              '/process-instances/findByVar/contactId/' + contactId,
          crossDomain: true,
          success: function(data) {
            ractive.set('saveObserver', false);
            ractive.set('current.instances', data);
            console.log('fetched ' + data.length + ' instances');
            for (let idx in data) {
              ractive.push('current.activities', {
                content: data[idx]['processDefinitionId'] + ': ' + data[idx]['businessKey'],// jshint ignore:line
                occurred: "2016-10-10",
                type: "workflow"
              });
            }
            ractive.sortChildren('instances', 'occurred', false);
            ractive.set('saveObserver', true);
          }
        });
      },
      /** @deprecated use findAny */
      find: function(contactId) {
        console.log('find: ' + contactId);
        var c;
        $.each(ractive.get('contacts'), function(i, d) {
          if (contactId.endsWith(ractive.id(d))) {
            c = d;
          }
        });
        return c;
      },
      findAny: function(id, arr) {
        if (arr == undefined)
          return null; // in case entity array not loaded yet
        console.log('findAny: ' + id);

        var c;
        $.each(arr, function(i, d) {
          if (d.selfRef != undefined && d.selfRef.endsWith(id)) {
            c = d;
          } else if (ractive.id(d).endsWith(id)) {
            c = d;
          }
        });
        return c;
      },
      findByFullName: function(entity) {
        return 'fullName' in entity && entity.fullName == ractive.get('fullName');
      },
      importComplete: function(imported, failed) {
        console.log('inferComplete');
        ractive.showMessage('Import complete added ' + imported + ' records ' + ' with ' + failed + ' failures');
        if (failed == 0) {
          ractive.fetch();
          $("#pasteSect").animate({
            width: 'toggle'
          }, ractive.EASING_DURATION * 2, function() {
            $("#contactsSect").slideDown(ractive.EASING_DURATION * 2);
          });
        }
      },
      inactiveStages: function() {
        return ractive.get('tenant.serviceLevel.inactiveStages') == undefined ?
            DEFAULT_INACTIVE_STAGES :
            ractive.get('tenant.serviceLevel.inactiveStages').join();
      },
      inferDomainName: function() {
        console.info('inferDomainName');
        var email = ractive.get('current.email');
        if (email == undefined)
          return false;
        var emailDomain = email.substring(email.indexOf('@') + 1);
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
          console.log('Assuming this is company-owned domain: ' + emailDomain);
          ractive.set('current.businessWebsite', 'http://' + emailDomain);
        }
        console.log('  emailDomain: ' + emailDomain);
      },
      mergeContacts: function() {
        console.info('mergeContacts');
        ractive.set('contact1', ractive.get('contacts[0]'));
        ractive.set('contact2', ractive.get('contacts[1]'));
        ractive.set('mergedContact', ractive.get('contacts[2]'));
        // alert('Sorry merge of contacts is not yet implemented');
        $('#mergeModal').modal({});
      },
      oninit: function() {
        console.log('oninit');
      },
      pasteInit: function() {
        ractive.set('pasteData', undefined);
        $("#contactsSect").slideUp(ractive.EASING_DURATION * 2, function() {
          $("#pasteSect").animate({
            width: 'toggle'
          }, ractive.EASING_DURATION * 2);
        });

        document.addEventListener('paste', function(e) {
          console.error('  ' + e.clipboardData.types);
          if (e.clipboardData.types.indexOf('text/plain') > -1) {
            ractive.pastePreview(e.clipboardData.getData('text/plain'));
            e.preventDefault(); // We are already handling the data from the
            // clipboard, we do not want it inserted into
            // the document
          }
        });
      },
      pasteDataToObjects: function() {
        console.info('pasteDataToObjects');

        var list = [];
        $.each(ractive.get('pasteData.rows'), function(i, d) {
          var obj = {};
          $.each(ractive.get('pasteData.headers'), function(j, e) {
            console.log('  ' + i + ':' + d);
            if (ractive.get('fields').indexOf(e) != -1) {
              if (e.indexOf('.') == -1) {
                obj[e] = d[j];
              } else {
                var orig = obj;
                var elements = e.split('.');
                for ( var idx in elements) {
                  if (idx == elements.length - 1) {
                    obj[elements[idx]] = d[j];
                  } else {
                    if (obj[elements[idx]] == undefined)
                      obj[elements[idx]] = {};
                    obj = obj[elements[idx]];
                  }
                }
                obj = orig;
              }
            }
          });
      if (!('firstName' in obj)) obj.firstName = 'Ann';
      if (!('lastName' in obj)) obj.lastName = 'Other';
      if (!('email' in obj)) obj.email = 'info@omny.link';
      if (!('enquiryType' in obj)) obj.enquiryType = 'User Import';

      obj.tenantId = ractive.get('tenant.id');
      if ('account' in obj) obj.account.tenantId = ractive.get('tenant.id');

          list.push(obj);
        });
        ractive.set('list', list);
        return list;
      },
      pasteImport: function() {
        console.info('pasteImport');

        var list = ractive.pasteDataToObjects();
        var imported = 0;
        var failed = 0;

        for (let idx in list) {
          ractive.sendMessage({
            name: "omny.importedContact",
            body: JSON.stringify(list[idx]),
            callback: function() { // jshint ignore:line
              console.log('  sendMessage callback...');
              imported++;
              if (imported + failed == list.length)
                ractive.importComplete(imported, failed);
            },
            pattern: "inOnly"
          }).fail(function() { // jshint ignore:line
            var msg = "Unable to import record " + idx;
            console.warn('msg:' + msg);
            failed++;
            if (imported + failed == list.length)
              ractive.importComplete(imported, failed);
          });
        }
      },
      pastePreview: function(data) {
        var rows = data.split("\n");

        for ( var y in rows) {
          var cells = rows[y].trim().split("\t");
          if (y == 0) {
            ractive.set('pasteData.headers', cells);
          } else {
            ractive.set('pasteData.rows.' + (y - 1), cells);
          }
        }

        ractive.pasteValidate();

        $("#pasteZone").animate({
          width: 'toggle'
        }, ractive.EASING_DURATION * 2);
      },
      pasteValidate: function() {
        var valid = true;
        $.each(ractive.get('pasteData.headers'), function(i, d) {
          console.log('  ' + i + ':' + d);
          if (d.indexOf('customFields') != -1) {
            console.debug('assume this field is ok:' + d);
          } else if (ractive.get('fields').indexOf(d) == -1) {
            $('#pastePreview th[data-name="' + d + '"] .glyphicon').show();
            valid = false;
          }

          var v = ractive.get('fieldValidators');
          $.each(ractive.get('pasteData.rows'), function(j, e) {
            console.log(j + ':' + e[i]);
            if (v[d] != undefined && e[i].search(v[d]) == -1) {
              $('#pastePreview tbody tr[data-row="'+j+'"] td[data-col="'+i+'"] .glyphicon').show();
            }
          });
        });
        if (!valid)
          ractive
              .showWarning('There are problems with the proposed import, please modify and try again');
      },
      save: function() {
        ractive.saveAccount();
      },
      saveAccount: function() {
        console.log('save account: ' + ractive.get('current').name + '...');
        ractive.set('saveObserver', false);
        var id = ractive.uri(ractive.get('current'));
        if (document.getElementById('currentForm') == undefined) {
          console.debug('still loading, safe to ignore');
        } else if (document.getElementById('currentForm').checkValidity()) {
          // cannot save contact and account in one (grrhh), this will clone...
          var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
          delete tmp.activities;
          delete tmp.contacts;
          delete tmp.contactIds;
          delete tmp.orders;
          delete tmp.notes;
          delete tmp.documents;
          delete tmp.tasks;
          tmp.tenantId = ractive.get('tenant.id');
          $.ajax({
            url: id === undefined ?
                ractive.getServer() + '/' + tmp.tenantId + ractive.get('entityPath') + '/':
                ractive.tenantUri(tmp),
            type: id === undefined ? 'POST': 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(tmp),
            success:  function(data, textStatus, jqXHR) {
              // console.log('data: '+ data);
              var entityName = ractive.get('entityPath').substring(1);
              var location = jqXHR.getResponseHeader('Location');
              ractive.set('saveObserver', false);
              if (location != undefined) {
                ractive.set('current._links.self.href', location);
                ractive.set('current.id', ractive.localId(location));
              }
              switch (jqXHR.status) {
              case 201:
                ractive.splice('accounts', 0, 0, ractive.get('current'));
                break;
              case 204:
                ractive.splice(entityName, ractive.get('currentIdx'), 1, ractive.get('current'));
                break;
              }
              ractive.showMessage('Account saved');
              ractive.addServiceLevelAlerts();
              ractive.set('saveObserver', true);
            }
          });
        } else {
          console.warn('Cannot save yet as account is invalid');
          $('#currentForm :invalid').addClass('field-error');
          ractive.showMessage('Cannot save yet as account is incomplete');
          ractive.set('saveObserver', true);
        }
      },
      saveContact: function() {
        console.log('save contact:...');
        ractive.set('saveObserver', false);
        if (document.getElementById('currentContactForm') == undefined) {
          console.debug('still loading, safe to ignore');
        } else if (document.getElementById('currentContactForm')
            .checkValidity()) {
          ractive.set('currentContact.tenantId', ractive.get('tenant.id'));

          var tmp = ractive.get('currentContact');
          if (typeof tmp == 'function') return ; // 10 Jul 17 Ractive extension
          delete tmp.account;
          delete tmp.activities;
          delete tmp.alertsAsList;
          delete tmp.documents;
          delete tmp.notes;
          delete tmp.orders;
          delete tmp.customFields; // NOTE cannot handle customFields
          tmp.accountId = ractive.id(ractive.get('current'));
          if (tmp.alerts!=undefined && Array.isArray(tmp.alerts)) tmp.alerts = tmp.alerts.join();
          if (tmp.emailOptIn == 'on') tmp.emailOptIn = true;
          else  tmp.emailOptIn = false;
          var id = ractive.uri(tmp) == undefined ? undefined: ractive.tenantUri(ractive.get('currentContact'), '/contacts');
          console.log('ready to save contact' + JSON.stringify(tmp) + ' ...');
          $.ajax({
            url: id === undefined ?
                ractive.getServer() + '/' + tmp.tenantId + '/contacts/' : id,
            type: id === undefined ? 'POST': 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(tmp),
            success:  function(data, textStatus, jqXHR) {
              // console.log('data: '+ data);
              var location = jqXHR.getResponseHeader('Location');
              ractive.set('saveObserver', false);
              if (location != undefined)
                ractive.set('currentContact._links.self.href', location);
              switch (jqXHR.status) {
              case 201:
                ractive.fetchAccountContacts();
                break;
              case 204:
                ractive.fetchAccountContacts();
                ractive.update('orders');
                break;
              }
              ractive.showMessage('Contact saved');
              ractive.set('saveObserver', true);
            }
          });
        } else {
          console.warn('Cannot save yet as contact is invalid');
          $('#currentOrderForm :invalid').addClass('field-error');
          ractive.showMessage('Cannot save yet as contact is incomplete');
          ractive.set('saveObserver', true);
        }
      },
      saveDependentAccount: function() {
        if (ractive.get('current.account') == undefined)
          return;
        if (ractive.uri(ractive.get('current')) == undefined) {
          ractive.showMessage('You must have created your contact before adding account details');
          return;
        }
        console.log('saveDependentAccount ' + ractive.get('current.account.name') + ' ...');
        var id = ractive.get('current.accountId');
        console.log(' id: ' + id);
        ractive.set('saveObserver', false);
        ractive.set('current.account.tenantId', ractive.get('tenant.id'));
        if (ractive.get('current.account.companyNumber') == '')
          ractive.set('current.account.companyNumber', undefined);
        if ($('#currentAccountForm:visible').length != 0 &&
            document.getElementById('currentAccountForm').checkValidity()) {
          $.ajax({
            url: ractive.getServer() + '/' + ractive.get('tenant.id') + '/accounts/' + (id == undefined ? '': id),
            type: id == undefined ? 'POST': 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(ractive.get('current.account')),
            success:  function(data, textStatus, jqXHR) {
              var location = jqXHR.getResponseHeader('Location');
              if (location != undefined)
                ractive.set('current.account.id', location.substring(location.lastIndexOf('/') + 1));
              var contactAccountLink = ractive.uri(ractive.get('current'));
              contactAccountLink += '/account';
              console.log(' attempt to link account: ' + location + ' to ' + contactAccountLink);
              if (jqXHR.status == 201) {
                ractive.saveAccountLink(contactAccountLink, location);
              } else if (jqXHR.status == 204) {
                ractive.set('saveObserver', false);
                var currentIdx = ractive.get('currentIdx');
                ractive.splice('contacts', currentIdx, 1, ractive
                    .get('current'));
                ractive.showMessage('Account updated');
                ractive.set('saveObserver', true);
              }
              ractive.set('contacts.' + ractive.get('currentIdx') + '.accountName',
                  ractive.get('current.account.name'));
            }
          });
        } else if ($('#currentAccountForm:visible').length != 0) {
          var msg = 'Cannot save yet as account is invalid';
          console.warn(msg);
          $('#currentAccountForm :invalid').addClass('field-error');
          ractive.showMessage(msg);
          ractive.set('saveObserver', true);
        }
      },
      saveAccountLink: function(contactAccountLink, location) {
        console.info('saveAccountLink: ' + contactAccountLink + ' to ' + location);
        $.ajax({
          url: contactAccountLink,
          type: 'PUT',
          contentType: 'text/uri-list',
          data: location,
          success:  function() {
            ractive.set('saveObserver', false);
            console.log('linked account: ' + location + ' to ' + contactAccountLink);
            ractive.select(ractive.get('current'));
            ractive.showMessage('Contact added to Account');
            ractive.set('saveObserver', true);
          }
        });
      },
      saveOrder: function() {
        console.log('save order: ...');
        ractive.set('saveObserver', false);
        if (document.getElementById('currentOrderForm') == undefined) {
          console.debug('still loading, safe to ignore');
        } else if (document.getElementById('currentOrderForm').checkValidity()) {
          // TODO redundant?
          ractive.set('orders.'+ractive.get('currentOrderIdx')+'.tenantId', ractive.get('tenant.id'));

          var tmp = ractive.get('orders.'+ractive.get('currentOrderIdx'));
          if (tmp.date == 'n/a') delete tmp.date;
          var contactName = tmp.contactName;
          if (contactName != undefined) {
            tmp.contactId = ractive.localId(Array.findBy('fullName', contactName,ractive.get('current.contacts')));
            delete tmp.contactName;
          }
          var id = ractive.uri(tmp) == undefined ? undefined: ractive.id(tmp);
          console.log('ready to save order' + JSON.stringify(tmp) + ' ...');
          $.ajax({
            // TODO cannot use tenantUri() here
            url: id === undefined ?
                ractive.getServer() + '/' + tmp.tenantId + '/orders/' :
                ractive.getServer() + '/' + tmp.tenantId + '/orders/' + id,
            type: id === undefined ? 'POST': 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(tmp),
            success:  function() {
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
          var id = ractive.localId(tmp);
          console.log('ready to save order item' + JSON.stringify(tmp) + ' ...');
          $.ajax({
            // TODO no use case for creating items (yet)
            url: id === undefined ?
               ractive.getServer() + '/' + tmp.tenantId + '/orders/' + tmp.orderId + '/order-items' :
               ractive.getServer() + '/' + tmp.tenantId + '/orders/' + tmp.orderId + id,
            type: id === undefined ? 'POST': 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(tmp),
            success:  function() {
              ractive.fetchOrders(ractive.get('current'));
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
      select: function(account) {
        console.log('select: ' + ractive.localId(account));
        ractive.set('saveObserver', false);
        // default owner to current user
        if (account.owner == undefined || account.owner == '')
          account.owner = ractive.get('profile.username');
        // adapt between Spring Hateos and Spring Data Rest
        if (account._links == undefined && account.links != undefined) {
          account._links = account.links;
          $.each(account.links, function(i, d) {
            if (d.rel == 'self')
              account._links.self = {
                href: d.href
              };
          });
        }
        if (account._links != undefined) {
          var url = ractive.uri(account); // includes getServer
          if (url == undefined) {
            ractive.showError('No account selected, please check link');
            return;
          }
          // console.log('loading detail for ' + url);
          $.getJSON(ractive.tenantUri(account), function(data) {
              console.log('found account ' + data);
              ractive.set('saveObserver', false);
              ractive.set('current', data);

              ractive.fetchStockItems();
              ractive.fetchAccountContacts();
              ractive.fetchTasks('accountId',ractive.id(ractive.get('current')));

              ractive.initControls();
              ractive.initTags();
              if (ractive.get('tenant.features.accountDescription')==true) ractive.initEditor();
              // who knows why this is needed, but it is, at least for
              // first time rendering
              $('.autoNumeric').autoNumeric('update', {});
              ractive.sortChildren('notes','created',false);
              ractive.sortChildren('documents','created',false);
              ractive.addServiceLevelAlerts();
              ractive.analyzeEmailActivity(ractive.get('current.activities'));
              ractive.applyAccessControl();
              ractive.set('saveObserver', true);
            });
        } else {
          console.log('Skipping load as no _links.' + account.lastName);
          ractive.set('current', account);
          ractive.set('saveObserver', true);
        }
        ractive.hideResults();
      },
      selectMultiple: function(account) {
        console.info('selectMultiple: ' + ractive.localId(account));
        if ($('tr[data-href="' + ractive.localId(account) + '"] input[type="checkbox"]').prop('checked')) {
          console.log('  checked: ' + $('tr[data-href="' + ractive.localId(account) + '"] input[type="checkbox"]').prop('checked'));
          ractive.push('selectMultiple', ractive.localId(account));
        } else {
          var idx = ractive.get('selectMultiple').indexOf(ractive.localId(account));
          console.log('  idx: ' + idx);
          ractive.splice('selectMultiple', idx, 1);
        }
        console.log('  selectMultiple: ' + ractive.get('selectMultiple'));

        // Dis/Enable merge buttons
        $('tr[data-href] .glyphicon-transfer').hide();
        if (ractive.get('selectMultiple').length == 2) {
          $.each(ractive.get('selectMultiple'), function(i, d) {
            $('tr[data-href="' + d + '"] .glyphicon-transfer').show();
          });
        }
      },
      sendMessage: function(msg) {
        console.log('sendMessage: ' + msg.name);
        var type = (msg.pattern == 'inOut' || msg.pattern == 'outOnly') ? 'GET' : 'POST';
        var d = (msg.pattern == 'inOut') ? { query: msg.body }: { json: msg.body };
        console.log('d: ' + d);
        // var d['businessDescription']=ractive.get('message.bizKey');
        return $.ajax({
          url: ractive.getServer() + '/msg/' + ractive.get('tenant.id') + '/' + msg.name + '/',
          type: type,
          data: d,
          dataType: 'text',
          success:  function(data) {
            console.log('Message received:' + data);
            if ('callback' in msg)
              msg.callback(data);
          },
        });
      },
      setMainContact: function(contactRef) {
        console.info('setMainContact: ' + contactRef);
        for (var idx = 0 ; idx < ractive.get('current.contacts').length ; idx++) {
          ractive.set('currentContact', ractive.get('current.contacts.' + idx));
          if (ractive.localId(ractive.get('current.contacts.' + idx)) != contactRef) {
            ractive.set('current.contacts.' + idx + '.mainContact', false);
            ractive.saveContact();
          } else {
            ractive.set('current.contacts.' + idx + '.mainContact', true);
            ractive.saveContact();
          }
        }
      },
      showAlertCounters: function() {
        // console.info('showAlertCounters');
        var alerts = {
          account: $('#currentAccount :invalid').length,
          activities: $('#activitySect :invalid').length,
          activityAnalysis: $('#activityAnalysisSect :invalid').length,
          budget: $('#budgetSect :invalid').length,
          connections: $('#connectionsSect :invalid').length,
          documents: $('#documentsTable .alert-danger').length,
          notes: $('#notesTable .alert-danger, #notesSect .messages.alert-danger').length,
        };
        ractive.set('alerts', alerts);
      },
      showContactSocial: function(obj, networkName, keypath) {
        ractive.set('currentContact',obj);
        ractive.showSocial(networkName, keypath);
      },
      toggleEditContact: function(obj) {
        console.info('toggleEditContact');
        ractive.set('currentContact', obj);
        var editing = $('[data-contact-id="' + ractive.localId(obj) + '"]').siblings()
            .children('a.glyphicon-pencil').hasClass('editing');
        // disable _all_ contacts
        $('[data-contact-id]').siblings().children('a.glyphicon-pencil')
            .removeClass('editing');
        $('[data-contact-id]').removeClass('editing');
        $('[data-contact-id="' + ractive.localId(obj) + '"] span').removeClass('hidden');
        $('[data-contact-id="' + ractive.localId(obj) + '"] input').addClass('hidden');

        if (editing) { // Change editable to display, inc. discard unsaved
          ractive.fetchAccountContacts();
          ractive.set('currentContact', undefined);
        } else if (ractive.uri(obj) == undefined) { // new contact
          // save now to avoid race condition leading to multiple records later
          ractive.saveContact();
        } else { // Change clicked contact to editable
          $('[data-contact-id="' + ractive.localId(obj) + '"]').siblings()
            .children('a.glyphicon-pencil').addClass('editing');
          $('[data-contact-id="' + ractive.localId(obj) + '"] span').addClass('hidden');
          $('[data-contact-id="' + ractive.localId(obj) + '"] input')
            .removeClass('hidden')
            .addClass('editing')
            .on('blur', function(ev) {
              ractive.set('saveObserver', false);
              ractive.set('currentContact.'+ev.target.name,ev.target.value);
              ractive.saveContact();
//              ractive.set('saveObserver', true);
            });
        }
      },
      toggleOrderSubEntity: function(obj, subEntity) {
        console.log('toggleOrderSubEntity(' + ractive.id(obj) + ', ' + subEntity + ')');
        $('[data-order-id="' + ractive.id(obj) + '"].' + subEntity).slideToggle();
        if (subEntity == 'feedback') ractive.fetchFeedback(obj);
      },
      /**
       * Inverse of editField.
       */
      updateField: function(selector, path) {
        var tmp = $(selector).text();
        console.log('updateField ' + path + ' to ' + tmp);
        ractive.set(path, tmp);
        $(selector).css('border-width', '0px').css('padding', '0px');
      },
      upload: function(formId) {
        console.log('upload:' + formId);
        ractive.showMessage('Uploading ...');

        var formElement = document.getElementById(formId);
        var formData = new FormData(formElement);
        var entity = $('#' + formId + ' .entity').val();
        var fileName = $('#' + formId + ' input[type="file"]').val();
        var fileExt = fileName.substring(fileName.lastIndexOf('.') + 1);
        return $.ajax({
          type: 'POST',
          url: ractive.tenantUri(entity)+'/upload'+fileExt.toLowerCase(),
          data: formData,
          cache: false,
          contentType: false,
          processData: false,
          success: function(response) {
            // console.log('successfully uploaded data');
            ractive.showMessage('Successfully uploaded ' + response.length + ' records');
          },
          error: function(jqXHR, textStatus, errorThrown) {
            console.error(textStatus+': '+errorThrown);
            ractive.showError('Something went wrong with that upload, please talk to your administrator');
          }
        });
      }
    });

ractive.observe('profile', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  if ((ractive.get('searchTerm') == undefined || ractive.get('searchTerm').length==0) && newValue!=undefined) {
    $('.dropdown.dropdown-menu li:nth-child(3)').addClass('selected');
    ractive.search('active owner:'+newValue.email);
  }
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete
// controls done that way save the oldValue
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  if (ractive.get('current') != undefined) ractive.showAlertCounters();
  var ignored = [ 'current.contacts', 'current.notes', 'current.documents' ];
  if (!ractive.get('saveObserver')) console.debug('Skipped save of '+keypath+' because in middle of other operation');
  else if (ractive.get('saveObserver') && ignored.indexOf(keypath) == -1 && keypath.startsWith('current.')) ractive.saveAccount();
  else {
     console.warn('Skipped save triggered by change to: ' + keypath);
     console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
     console.log(' saveObserver: '+ractive.get('saveObserver'));
  }
});

ractive.observe('current.contacts.*.twitter currentContact.twitter', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  if (ractive.get('saveObserver') && newValue != undefined && newValue != '') {
    ractive.saveContact();
  }
});

ractive.observe('current.stage', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  if (newValue == 'Cold' && ractive.get('current.stageDate') == undefined) {
    ractive.set('current.stageDate', new Date());
  }
});

ractive.observe('current.businessWebsite', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  if (newValue != undefined && newValue != '' &&
      !(newValue.startsWith('http') || newValue.startsWith('https'))) {
    ractive.set('current.businessWebsite', 'http://' + newValue);
  }
});

ractive.on('sortContact', function(event, column) {
  console.info('sortContact on ' + column);
  // if already sorted by this column reverse order
  if (this.get('sortContactColumn') == column)
    this.set('sortContactAsc', !this.get('sortContactAsc'));
  this.set('sortContactColumn', column);
});
ractive.on('sortOrder', function(event, column) {
  console.info('sortOrder on ' + column);
  // if already sorted by this column reverse order
  if (this.get('sortOrderColumn') == column)
    this.set('sortOrderAsc', !this.get('sortOrderAsc'));
  this.set('sortOrderColumn', column);
});

