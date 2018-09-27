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

var EDGE_LUMINANCE = 0.1;
var CENTER_LUMINANCE = 0.6;

fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    title: 'Funnel',
    accounts:[],
    contacts:[],
    orders:[],
    data: [],
    searchMatched: 0,
    searchTerm: '',
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    formatAccountId: function(contactId) {
      console.info('formatAccountId');
      if (contactId == undefined) return contactId;
      var contact = Array.findBy('selfRef','/contacts/'+contactId,ractive.get('contacts'));
      return contact == undefined ? 'n/a' : contact.accountName;
    },
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatAlertCount: function(alerts) {
      console.log('formatAlerts');
      if (typeof alerts == 'string') alerts = JSON.parse(alerts);

      return alerts == undefined ? 0 : Object.keys(alerts).length;
    },
    formatContactId: function(contactId) {
      console.info('formatContactId');
      if (contactId == undefined) return contactId;
      var contact = Array.findBy('selfRef','/contacts/'+contactId,ractive.get('contacts'));
      return contact == undefined ? 'n/a' : contact.fullName;
    },
    formatContent: function(content) {
      console.info('formatContent');
      content = content.replace(/\n/g,'<br/>');
      content = Autolinker.link(content);
      return content;
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      var d = new Date(timeString);
      // IE strikes again
      if (d == 'Invalid Date') d = parseDateIEPolyFill(timeString);
      return d.toLocaleDateString(navigator.languages);
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
    funnel: {
      options: {
        chart: {
          bottomPinch: 0,
          bottomWidth: 4 / 5,
          animate: 200,
          curve: {
            enabled: false,
          },
        },
        block : {
          dynamicHeight: true,
          fill: {
            type: 'gradient',
          },
          minHeight: 20,
          highlight: true,
        },
        label: {
          fontSize: '1em',
          format: '{l}: {f}'
        },
        events: {
          click: {
            block: function(d) {
              console.log('<' + d.label.raw + '> selected.');
              if (getSearchParameters()['o']!=undefined) {
                ractive.set('entityPath','/orders');
                if (ractive.get('orders').length==0) ractive.fetchOrders();
                if (ractive.get('contacts').length==0) ractive.fetchContacts();
                $('#ordersTable:hidden').slideDown();
              } else if (ractive.get('tenant.features.accountView')) {
                ractive.set('entityPath','/accounts');
                if (ractive.get('accounts').length==0) ractive.fetchAccounts();
                $('#accountsTable:hidden').slideDown();
              } else {
                ractive.set('entityPath','/contacts');
                if (ractive.get('contacts').length==0) ractive.fetchContacts();
                $('#contactsTable:hidden').slideDown();
              }
              ractive.search(ractive.get('searchTerm').replace(/ stage:[!\S]+/g,'')+' stage:'+d.label.raw.replace(/ /g,'_'));
            },
          },
        }
      }
    },
    hash: function(email) {
      if (email == undefined) return '';
      //console.log('hash '+email+' = '+ractive.hash(email));
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    helpUrl: '//omny-link.github.io/user-help/funnel/',
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
        var search = ractive.get('searchTerm').trim().split(' ');
        for (var idx = 0 ; idx < search.length ; idx++) {
          var searchTerm = search[idx].toLowerCase();
          var match = ( (obj.selfRef!=undefined && obj.selfRef.indexOf(searchTerm)>=0)
            || (obj.firstName!=undefined && obj.firstName.toLowerCase().indexOf(searchTerm)>=0)
            || (obj.lastName!=undefined && obj.lastName.toLowerCase().indexOf(searchTerm)>=0)
            || (searchTerm.indexOf('@')!=-1 && obj.email.toLowerCase().indexOf(searchTerm)>=0)
            || (obj.phone1!=undefined && obj.phone1.indexOf(searchTerm)>=0)
            || (obj.phone2!=undefined && obj.phone2.indexOf(searchTerm)>=0)
            || (obj.name!=undefined && obj.name.toLowerCase().indexOf(searchTerm)>=0)
            || (obj.accountName!=undefined && obj.accountName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
            || (searchTerm.startsWith('type:') && obj.accountType!=undefined && obj.accountType.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.toLowerCase().replace(/ /g,'_').substring(5))==0)
            || (searchTerm.startsWith('enquiry:') && obj.enquiryType!=undefined && obj.enquiryType.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.toLowerCase().replace(/ /g,'_').substring(8))==0)
            || (searchTerm.startsWith('stage:') && obj.stage!=undefined && obj.stage.toLowerCase().replace(/ /g,'_').indexOf(searchTerm.toLowerCase().replace(/ /g,'_').substring(6))==0)
            || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('created>') && new Date(obj.firstContact)>new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('created>') && new Date(obj.created)>new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('created<') && new Date(obj.firstContact)<new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('created<') && new Date(obj.created)<new Date(searchTerm.substring(8)))
            || (searchTerm.startsWith('#') && obj.tags.indexOf(searchTerm.substring(1))!=-1)
            || (searchTerm.startsWith('owner:') && obj.owner.indexOf(searchTerm.substring(6))!=-1)
            || (searchTerm.startsWith('active') && (obj.stage==undefined || obj.stage.length==0 || ractive.inactiveStages().indexOf(obj.stage.toLowerCase())==-1))
            || (searchTerm.startsWith('!active') && ractive.inactiveStages().indexOf(obj.stage.toLowerCase())!=-1)
          );
          // no match is definitive but matches may fail other terms (AND logic)
          if (!match) return false;
        }
        return true;
      }
    },
    server: $env.server,
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
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/funnel-navbar.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/webjars/supportservices/3.0.0/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "funnelSect", "url": "/partials/funnel-sect.html"},
      { "name": "accountListTable", "url": "/partials/account-list-table.html" },
      { "name": "contactListTable", "url": "/partials/contact-list-table.html"},
      { "name": "orderListTable", "url": "/partials/order-list-table.html"}
    ],
  },
  partials: {
    helpModal: '',
    navbar: '',
    loginSect: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    titleArea: '',
    funnelSect: '',
    accountListTable: '',
    contactListTable: '',
    orderListTable: ''
  },
  activeStages: function() {
    console.info('activeStages');
    var activeStages = [];
    var stages = ractive.get('stages');
    $.each(stages, function(i,d) {
      if (d['idx']>=0) activeStages.push(d.name);
    });
    return activeStages;
  },
  edit: function(obj) {
    ractive.openInNewWindow(obj);
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    ractive.initControls();
    if (getSearchParameters()['o']!=undefined) {
      var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/funnel';
      ractive.set('stages', ractive.get('orderStages'));
    } else if (ractive.get('tenant.features.accountView')) {
      var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/funnel/accounts';
      ractive.set('stages', ractive.get('accountStages'));
    } else {
      var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/funnel/contacts';
      ractive.set('stages', ractive.get('contactStages'));
    }
    $.ajax({
      dataType: "json",
      url: url,
      crossDomain: true,
      success: function( data ) {
        ractive.set('funnel.raw', data);
        if (ractive.get('stages')==undefined || ractive.get('stages').length==0) {
          console.warn('Stages not yet loaded, defer chart load');
          return;
        } else {
          ractive.renderChart();
        }
      }
    });
  },
  fetchAccounts: function () {
    console.info('fetchAccounts...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/accounts/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('accounts', data);
        } else {
          ractive.merge('accounts', data['_embedded'].accounts);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) $('.power-user').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#accountsTable tbody tr:visible').length);
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
        if (data['_embedded'] == undefined) {
          ractive.merge('contacts', data);
        } else {
          ractive.merge('contacts', data['_embedded'].contacts);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) $('.power-user').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#contactsTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchOrders: function () {
    console.info('fetchOrders...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/orders/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('orders', data);
        } else {
          ractive.merge('orders', data['_embedded'].accounts);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) $('.power-user').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#ordersTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }
    });
  },
  inactiveStages: function() {
    console.info('inactiveStages');
    var inactiveStages = [];
    $.each(ractive.get('stages'), function(i,d) {
      if (d['idx']<0) inactiveStages.push(d.name);
    });
    return inactiveStages;
  },
  openInNewWindow: function(obj) {
    console.info('openInNewWindow');
    if (getSearchParameters()['o']!=undefined) {
      window.open('/orders.html?q='+ractive.id(obj)+'&v='+obj.type);
    } else if (ractive.get('tenant.features.accountView')) {
      window.open('/accounts.html?q='+ractive.id(obj));
    } else {
      window.open('/contacts.html?q='+ractive.id(obj));
    }
  },
  renderChart: function() {
    console.info('renderChart');
    if (ractive.get('funnel.raw')==undefined) return;
    var data = ractive.get('funnel.raw');
    var funnelData = [ractive.activeStages().length];
    var activeStages = ractive.activeStages();
    var haveFunnel = false;
    $.each(activeStages, function(i,d) {
      //console.log(i+': '+d+' = '+data.stages[d]);
      var color = (i % 2 == 0 ? '#949699' : '#f9f9f9');
      funnelData[i] = [d,data.stages[d]==undefined ? 0 :data.stages[d], ColorLuminance(color,EDGE_LUMINANCE), ColorLuminance(color,CENTER_LUMINANCE)];
      if (!haveFunnel) haveFunnel = (data.stages[d]>0);
    });
    ractive.merge('funnel.data',funnelData);
    // TODO a bug? If all funnel data is 0 chart does not render
    if (haveFunnel) {
      var chart = new D3Funnel('#funnel');
      chart.draw(funnelData, ractive.get('funnel.options'));
    } else {
      ractive.showMessage('No records in your funnel, check stage field is set correctly.');
    }
  }
});

ractive.observe('owner', function(newValue, oldValue, keypath) {
  console.log('owner changed from '+oldValue+' to '+newValue);
  if (newValue!=undefined && newValue!='') ractive.search(ractive.get('searchTerm').replace(/ owner:[!\S]+/g,'')+' owner:'+newValue);
});

ractive.observe('created', function(newValue, oldValue, keypath) {
  console.log('created changed from '+oldValue+' to '+newValue);
  if (newValue!=undefined) {
    var createdExpr;
    switch (newValue) {
    case 'Past 7 days':
      createdExpr = ' created>'+(new Date(new Date().getTime()-(7*24*60*60*1000)).toISOString());
      break;
    case 'This month':
      createdExpr = ' created>'+(new Date(new Date().getFullYear(),new Date().getMonth(),1).toISOString());
      break;
    case 'This year':
      createdExpr = ' created>'+(new Date(new Date().getFullYear(),0,1).toISOString());
      break;
    default:
      createdExpr = newValue;
    }
    ractive.search(ractive.get('searchTerm').replace(/ created[<>][!\S]+/g,'')+' '+createdExpr);
  }
});
ractive.observe('updated', function(newValue, oldValue, keypath) {
  console.log('updated changed from '+oldValue+' to '+newValue);
  if (newValue!=undefined) {
    var updatedExpr;
    switch (newValue) {
    case 'Past 7 days':
      updatedExpr = ' updated>'+(new Date(new Date().getTime()-(7*24*60*60*1000)).toISOString());
      break;
    case 'This month':
      updatedExpr = ' updated>'+(new Date(new Date().getFullYear(),new Date().getMonth(),1).toISOString());
      break;
    case 'This year':
      updatedExpr = ' updated>'+(new Date(new Date().getFullYear(),0,1).toISOString());
      break;
    default:
      updatedExpr = newValue;
    }
    ractive.search(ractive.get('searchTerm').replace(/ updated[<>][!\S]+/g,'')+' '+updatedExpr);
  }
});

ractive.observe('stages', function(newValue, oldValue, keypath) {
  console.log('stages loaded');
  if (newValue!=undefined && ractive.get('funnel')!=undefined) ractive.renderChart();
});

ractive.on( 'sort', function ( event, column ) {
  console.info('sort on '+column);
  // if already sorted by this column reverse order
  if (this.get('sortColumn')==column) this.set('sortAsc', !this.get('sortAsc'));
  this.set( 'sortColumn', column );
});

// FROM http://www.sitepoint.com/javascript-generate-lighter-darker-color/
function ColorLuminance(hex, lum) {

  // validate hex string
  hex = String(hex).replace(/[^0-9a-f]/gi, '');
  if (hex.length < 6) {
    hex = hex[0]+hex[0]+hex[1]+hex[1]+hex[2]+hex[2];
  }
  lum = lum || 0;

  // convert to decimal and change luminosity
  var rgb = "#", c, i;
  for (i = 0; i < 3; i++) {
    c = parseInt(hex.substr(i*2,2), 16);
    c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
    rgb += ("00"+c).substr(c.length);
  }

  return rgb;
}

