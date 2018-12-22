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
    title: 'Directory',
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
    formatSpouse: function(spouse) {
      if (spouse == undefined) return '';
      else return 'and '+spouse.substring(0, spouse.indexOf(' '));
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
    helpUrl: '//omny-link.github.io/user-help/contacts/',
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
      if (obj == undefined || obj.customFields['churchDirectory']!='true') return false;
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
    server: $env.server,
    localId: function(selfRef) {
      return ractive.localId(selfRef);
    },
    sort: function (array, column, asc) {
      if (array == undefined) return;
      console.info('sort array of '+(array == undefined ? 0 : array.length)+' items '+(asc ? 'ascending' : 'descending')+' on: '+column);
      ractive.showActivityIndicator();
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
    stdPartials: [
      { "name": "directoryListSect", "url": "/partials/directory-list-sect.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "toolbar", "url": "/partials/toolbar.html"},
      { "name": "socialModal", "url": "/partials/social-modal.html" },
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "navbar", "url": "/partials/directory-navbar.html"},
      { "name": "supportBar", "url": "/webjars/supportservices/3.0.0/partials/support-bar.html"}
    ],
  },
  partials: {
    activityCurrentSect: '',
    contactListSect: '',
    helpModal: '',
    navbar: '',
    profileArea: '',
    sidebar: '',
    titleArea: '',
    toolbar: '',
    supportBar: ''
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+ractive.get('entityPath')+'/?page=0&limit=300&returnFull=true',
//      url: ractive.getServer()+'/'+ractive.get('tenant.id')+ractive.get('entityPath')+'/filter/directory/true',
      crossDomain: true,
      success: function( data ) {
        ractive.set('saveObserver', false);
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) $('.power-user').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('contacts',data.map(function(obj) {
            obj.name = obj.fullName;
            var result = ractive.findSpouse(data, obj.customFields['spouse']);
            if (result != undefined) {
              obj.spouse = result.d;
              data.splice(result.i, 1);
            }
            return obj;
          })
        );
        ractive.showSearchMatched();
        ractive.set('saveObserver', true);
      }
    });
  },
  findSpouse: function(arr, fullName) {
    console.log('findSpouse: '+fullName);
    var result;
    $.each(arr, function(i,d) {
      if (d.fullName == fullName) return result = { i:i, d:d };
    });
    return result;
  },
  oninit: function() {
    console.log('oninit');
    this.on( 'filter', function ( event, filter ) {
      console.info('filter on '+JSON.stringify(event)+','+filter.idx);
      $('.dropdown.dropdown-menu li').removeClass('selected');
      $('.dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected');
      ractive.search(filter.value);
    });
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showSearchMatched: function() {
    ractive.set('searchMatched', Math.floor($('#directoryTable tbody tr').length/2));
  }
});

