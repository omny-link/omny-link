var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    accounts: [],
    entityPath: '/contacts',
    csrfToken: getCookie(CSRF_COOKIE),
    contacts: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete,on hold"},
    //saveObserver:false,
    title: 'Contact Management',
    username: localStorage['username'],
    age: function(timeString) {
      if (timeString==undefined) return;
      return i18n.getAgeString(new Date(timeString))
    },
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
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
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
    formatFavorite: function(obj) {
      if (obj['favorite']) return 'glyphicon-star';
      else return 'glyphicon-star-empty';
    },
    formatJson: function(json) { 
      console.log('formatJson: '+json);
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
      return '<img class="img-rounded" style="width:36px" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36&d=https%3A%2F%2Fapi.omny.link%2F'+ractive.get('tenant.id')+'%2Fgravatars%2F'+ractive.hash(email)+'.png"/>'
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    haveStageReasons: function() {
      console.info('haveStageReasons?');
      if (ractive.get('current.stage')!='Cold') return false;
      return ractive.get('tenant.typeaheadControls').filter(function(d) {
        return d.name=='stageReasons';
      }).length > 0;
    },
    help: '<p>The contact management page is the central hub from which to manage all your prospects, partners, customers; in fact every person or organisation you may ever need to talk to!</p>\
      <h2>Key concepts</h2>\
      <ul>\
        <li>\
          <h3 id="contactList">Your contact list</h3>\
          <p>This contains all of your contacts, by default only the active ones will be displayed. You can search and filter in any number of ways.</p>\
        </li>\
        <li>\
          <h3 id="currentContact">A one-page view of your contact</h3>\
          <p>Clicking on a row in the contact list will open it up to show the full details including:</p>\
          <ul>\
            <li>Contact details</li>\
            <li>Details of the organisation the contact belongs to</li>\
            <li>A list of activities performed by or in relation to this contact</li>\
            <li>A most-recent-first list of notes about your interactions with this contact</li>\
            <li>Links to any documents that relate to this contact</li>\
          <li>...</li>\
        </ul>\
      </ul>',
    lessThan24hAgo: function(isoDateTime) {
      if (isoDateTime == undefined || (new Date().getTime()-new Date(isoDateTime).getTime()) < 1000*60*60*24) {
        return true;
      }
      return false;
    },
    matchFilter: function(obj) {
      var filter = ractive.get('filter');
      //console.info('matchFilter: '+JSON.stringify(filter));
      if (filter==undefined) {
        return true;
      } else {
        try {
          if (filter.operator=='in') {
            var values = filter.value.toLowerCase().split(',');
            return values.indexOf(obj[filter.field].toLowerCase())!=-1;
          } else if (filter.operator=='!in') {
            var values = filter.value.toLowerCase().split(',');
            return values.indexOf(obj[filter.field].toLowerCase())==-1;
          } else {
            if (filter.operator==undefined) filter.operator='==';
            return eval("'"+filter.value.toLowerCase()+"'"+filter.operator+"'"+obj[filter.field].toLowerCase()+"'");
          }
        } catch (e) {
          //console.debug('Exception during filter, probably means record does not have a value for the filtered field');
          return true;
        }
      }
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
      var searchTerm = ractive.get('searchTerm');
      //console.info('matchSearch: '+searchTerm);
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.firstName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.lastName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.email.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.phone1!=undefined && obj.phone1.indexOf(searchTerm)>=0)
          || (obj.phone2!=undefined && obj.phone2.indexOf(searchTerm)>=0)
          || (obj.accountName!=undefined && obj.accountName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created>') && new Date(obj.firstContact)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created<') && new Date(obj.firstContact)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('#') && obj.tags.indexOf(ractive.get('searchTerm').substring(1))!=-1)
        );
      }
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
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "customActionModal", "url": "/partials/custom-action-modal.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "contactListSect", "url": "/partials/contact-list-sect.html"},
      { "name": "contactListTable", "url": "/partials/contact-list-table.html"},
      { "name": "mergeModal", "url": "/partials/contact-merge-sect.html"},
      { "name": "navbar", "url": "/partials/contact-navbar.html"},
      { "name": "currentContactSect", "url": "/partials/contact-current-sect.html"},
      { "name": "currentCompanyBackground", "url": "/partials/contact-company-sect.html"}
    ],
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var contact = { account: {}, author:ractive.get('username'), tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select( contact );
    ractive.initTags();
  },
  addDoc: function (contact) {
    console.log('addDoc '+contact+' ...');
    if (contact==undefined || contact == '') {
      ractive.showMessage('You must have created your contact before adding documents');
      return;
    }
    ractive.set('current.doc', { author:ractive.get('username'), contact: ractive.stripProjection(contact), url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
  },
  addNote: function (contact) {
    console.log('addNote '+contact+' ...');
    ractive.set('saveObserver', false);
    if (contact==undefined || contact == '') {
      ractive.showMessage('You must have created your contact before adding notes');
      return;
    }
    ractive.get('current.notes').splice(0, 0, { 
      author:ractive.get('username'), contact: ractive.uri(ractive.get('current')), content: '', favorite: true
    });
    ractive.set('saveObserver', true);
    //$('#notesTable tr:nth-child(1)').slideDown();
  },
  addSector: function () {
    console.log('addSector ...');
    ractive.showError('Not yet implemented');
    //$('#curPartnerSectors').append($('#sectorTemplate').html());
  },
  cancelNote: function() {
    console.info('cancelNote');
    ractive.get('current.notes').splice(0, 1);
  },
  edit: function (contact) {
    console.log('edit'+contact+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('currentIdx',ractive.get('contacts').indexOf(contact));
    ractive.select( contact );
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  /*followUp: function(contactId) { 
    console.log('followUp: '+JSON.stringify(contactId));
    $.ajax({
      type: 'GET',
      url: '/msg/firmgains/firmgains.followUp.json?query={"contactId":"'+contactId+'","tenantId":"'+ractive.get('tenant.id')+'"}&businessDescription=FollowUp',
      crossDomain: true,
      success: function( data ) {
        console.log('data: '+data);
      }
    });
  },*/
  /*formatJson: function(json) { 
    console.log('formatJson: '+json);
    var obj = JSON.parse(json);
    var html = '';
    $.each(Object.keys(obj), function(i,d) {
      html += (typeof obj[d] == 'object' ? '' : d+': '+obj[d]+'<br/>');
    });5
    //console.log('HTML: '+html);
    //ractive.set('current.html',html);
    return html;
//    $(selector).append(html);
  },*/
  delete: function (obj) {
    console.log('delete '+obj+'...');
    $.ajax({
        url: ractive.getServer()+ractive.uri(obj),
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/?projection=complete',
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
  fetchAccounts: function () {
    console.info('fetchAccounts...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/accounts/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('accounts', data);
        } else {
          ractive.merge('accounts', data['_embedded'].accounts);
        }
        // set up account typeahead
        var accData = jQuery.map( ractive.get('accounts'), function( n, i ) {
          return ( {  "id": ractive.getId(n), "name": n.name } );
        });
        ractive.set('accountsDropDown',accData);
//        $('#curAccountName').typeahead({
//          items:'all',
//          minLength:0,
//          source:accData,
////          updater:function(item) {
////            return item.id;
////          }
//        });
//        $('#curCompanyName').on("click", function (ev) {
//          newEv = $.Event("keydown");
//          newEv.keyCode = newEv.which = 40;
//          $(ev.target).trigger(newEv);
//          return true;
//        });
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchCompaniesHouseInfo: function() { 
    if (ractive.get('tenant.features.companyBackground')==undefined || ractive.get('tenant.features.companyBackground')==false) return;
    console.info('fetchCompaniesHouseInfo for '+ractive.get('current.account.companyNumber'));
    ractive.sendMessage({
      name:"omny.companyRecord",
      body:JSON.stringify({companyNumber:ractive.get('current.account.companyNumber')}),
      callback:function(results) {
        //results = JSON.parse(results);
        ractive.set('saveObserver',false);
        ractive.set('current.account.companiesHouseInfo',JSON.parse(results));
        // A bit of a hack required here, will resolve once can move to proper JSON API
        var o = ractive.get('current.account.companiesHouseInfo.companyOfficersHtml');
        if (o.indexOf('<h2 class="heading-medium total-appointments"')!=-1) { 
          ractive.set('current.account.companiesHouseInfo.companyOfficersHtml', o.substring(o.indexOf('<h2 class="heading-medium total-appointments"')));
        }
        var fh = ractive.get('current.account.companiesHouseInfo.companyFilingsHtml');
        if (fh.indexOf('<div class="js-hidden warning-overview" id="firefox-pdf-notice">')!=-1) { 
          ractive.set('current.account.companiesHouseInfo.companyFilingsHtml', fh.substring(fh.indexOf('<div class="js-hidden warning-overview" id="firefox-pdf-notice">')));
        }
        ractive.set('saveObserver',true);
        $('#fhTable').addClass('table-striped');
      },
      pattern:"inOut"
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected')
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.set('searchMatched',$('#contactsTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(contactId) { 
    console.log('find: '+contactId);
    var c; 
    $.each(ractive.get('contacts'), function(i,d) { 
      if (contactId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  hideResults: function() {
    $('#contactsTableToggle').removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-right');
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
  inferDomainName: function() {
    console.info('inferDomainName');
    var email = ractive.get('current.email');
    if (email==undefined) return false;
    var emailDomain = email.substring(email.indexOf('@')+1);
    switch (emailDomain) {
    case 'aol.com':
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
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  pasteInit: function() {
    ractive.set('pasteData',undefined);
    $("#contactsSect").slideUp(EASING_DURATION*2, function() {
      $("#pasteSect").animate({width:'toggle'},EASING_DURATION*2);
    });

    document.addEventListener('paste', function(e){
      console.error('  '+e.clipboardData.types);
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
        }
      });
      if (obj['firstName']==undefined) obj['firstName'] = 'Ann';
      if (obj['lastName']==undefined) obj['lastName'] = 'Other';
      if (obj['email']==undefined) obj['email'] = 'info@omny.link';
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
    var imported = 0;
    var failed = 0;

    for(var idx in list) {
      ractive.sendMessage({
        name:"omny.importedContact",
        body:JSON.stringify(list[idx]),
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
    }
  },
  pastePreview: function(data) {
    var rows = data.split("\n");

    for(var y in rows) {
      var cells = rows[y].trim().split("\t");
      if (y==0){
        ractive.set('pasteData.headers',cells);
      }else{
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
        if (v[d]!=undefined && e[i].search(v[d])==-1) {
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
      if (id != undefined && tmp.account != undefined && Object.keys(tmp.account).length > 0 && tmp.account.id != undefined) {
        tmp.account = id.substring(0,id.indexOf('/',8))+'/accounts/'+tmp.account.id;  
      } else {
        delete tmp.account;
        delete tmp.accountId;
      }       
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
            $.ajax({
              url: contactAccountLink,
              type: 'PUT',
              contentType: 'text/uri-list',
              data: location,
              success: completeHandler = function(data, textStatus, jqXHR) {
                ractive.set('saveObserver',false);
                console.log('linked account: '+location+' to '+contactAccountLink);
                ractive.merge('contacts', ractive.get('current'));
                ractive.showMessage('Account saved');
                ractive.set('saveObserver',true);
              }
            });
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
  searchCompaniesHouse: function() {
    if (ractive.get('tenant.features.companyBackground')==undefined || ractive.get('tenant.features.companyBackground')==false) return;
    console.info('searchCompaniesHouse');
    var q = ractive.get('current.account.name');
    ractive.sendMessage({
      name:"omny.companySearch",
      body:JSON.stringify({companyName:q}),
      callback:function(results) {
        console.log('  sendMessage callback...')
        results = JSON.parse(results);
        ractive.set('companiesHouseResults',results);
        var data = jQuery.map( results.items, function( n, i ) {
          return ( {  "id": n.company_number, "name": n.company_number +' '+n.title } );
        });
        $('#curCompanyNumber').typeahead({
          items:'all',
          minLength:0,
          source:data,
          updater:function(item) {
            return item.id;
          }
        });
        $('#curCompanyNumber').on("click", function (ev) {
          newEv = $.Event("keydown");
          newEv.keyCode = newEv.which = 40;
          $(ev.target).trigger(newEv);
          return true;
        });
        if (ractive.get('current.account.companyNumber')!=undefined) ractive.fetchCompaniesHouseInfo();
      },
      pattern:"inOut"
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      var msg = "Unable to lookup company data at the moment. Please try later.";
      console.warn('msg:'+msg);
      ractive.showMessage(msg,'alert-warning');
    });
  },
  select: function(contact) {
    console.log('select: '+JSON.stringify(contact));
    ractive.set('saveObserver',false);
    if (contact.account == undefined || contact.account == '') contact.account = new Object();
    // default owner to current user
    if (contact.owner == undefined || contact.owner == '') contact.owner = ractive.get('username');
    // adapt between Spring Hateos and Spring Data Rest
    if (contact._links == undefined && contact.links != undefined) {
      contact._links = contact.links;
      $.each(contact.links, function(i,d) {
        if (d.rel == 'self') contact._links.self = { href:d.href };
      });
    }
    if (contact._links != undefined) {
      var url = ractive.uri(contact); // includes getServer
      if (url == undefined) {
        ractive.showError('No contact selected, please check link');
        return;
      }
      console.log('loading detail for '+url);
      $.getJSON(url+'?projection=complete', function( data ) {
        console.log('found contact '+data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        ractive.sortChildren('notes','created',false);
        ractive.sortChildren('documents','created',false);
        if (ractive.get('current.account.companyNumber')!=undefined) ractive.fetchCompaniesHouseInfo();
        ractive.sortChildren('activities','occurred',false);
        if (ractive.get('current.account')!=undefined &&
            (ractive.get('current.account.businessWebsite')==undefined || ractive.get('current.account.businessWebsite')=='')) ractive.inferDomainName(); 
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
  showResults: function() {
    $('#contactsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#contactsTable').slideDown({ queue: true });
  },
  startCustomAction: function(key, label, contact, form) {
    console.log('startCustomAction: '+key+' for '+contact.id);
    var instanceToStart = {
        processDefinitionId: key,
        businessKey: contact.firstName+' '+contact.lastName,
        label: label,
        processVariables: { 
            contactId: ractive.getId(contact), 
            contactShortId: ractive.stripProjection(ractive.getId(contact).substring(ractive.getId(contact).indexOf('/contacts')+10)), 
            initiator: ractive.get('username'),
            tenantId: ractive.get('tenant.id')
        }
      };
    console.log(JSON.stringify(instanceToStart));
    // save what we know so far...
    ractive.set('instanceToStart',instanceToStart);
    if (form == undefined) {
      // ... and submit 
      ractive.submitCustomAction();
    } else {
      // ... or display form 
      $('#customActionModal').modal('show');
    }
  },
  submitCustomAction: function() {
    console.info('submitCustomAction');
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('instanceToStart')),
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.showMessage('Started workflow "'+ractive.get('instanceToStart.label')+'" for '+ractive.get('instanceToStart.businessKey'));
        $('#customActionModal').modal('hide');
        ractive.select(ractive.get('current'));// refresh
      },
    });
  },
  toggleAllNotes: function(btn) {
    console.info('toggleAllNotes');
    $('#notesTable tr.unfavorite').slideToggle();
    $(btn).toggleClass('glyphicon-star glyphicon-star-empty');
  },
  toggleFavorite: function(idx) {
    console.info('toggleFavorite: '+idx);
    ractive.set('current.notes.'+idx+'.favorite',!ractive.get('current.notes.'+idx+'.favorite'));
    var n = ractive.get('current.notes.'+idx);
    var url = ractive.uri(n)+'/favorite';
    url = url.replace(ractive.entityName(n),ractive.get('tenant.id')+'/'+ractive.entityName(n));

    $.ajax({
      url: url,
      type: 'POST',
      data: { favorite: n.favorite },
      success: completeHandler = function(data) {
        console.log('response: '+ data);
        ractive.showMessage('Note favorited');
      }
    });
  },
  toggleResults: function() {
    console.info('toggleResults');
    $('#contactsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
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
          ractive.handleError(jqXHR, textStatus, errorThrown);
        }
      });
  }
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#contactsTable tbody tr').length);
  }, 500);
});


// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.account.companiesHouseInfo','current.documents','current.doc','current.notes','current.note'];
  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    if (keypath=='current.account') ractive.saveAccount();
    else ractive.save();
  } else { 
    console.warn  ('Skipped contact save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

ractive.observe('current.stage', function(newValue, oldValue, keypath) {
  console.log('stage changing from '+oldValue+' to '+newValue);
  if (newValue=='Cold' && ractive.get('current.stageDate')==undefined) {
    ractive.set('current.stageDate',new Date());
    $('#curStageReason').typeahead({
      items:'all',
      minLength:0,
      source:ractive.get('stageReasons'),
      updater:function(item) {
        return item.id;
      }
    });
  }
});

ractive.observe('current.account.name', function(newValue, oldValue, keypath) {
  console.log('account name changing from '+oldValue+' to '+newValue);
  if (newValue!=undefined && newValue!='') {
    $('#curCompanyNumber').typeahead('destroy');
    ractive.set('current.account.companyNumber',undefined);
    ractive.searchCompaniesHouse();
  }
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

function parseDateIEPolyFill(timeString) {
  var start = timeString.substring(0,timeString.indexOf('.'));
  var offset;
  if (timeString.indexOf('-',timeString.indexOf('T'))!=-1) {
    offset = timeString.substr(timeString.indexOf('-',timeString.indexOf('T')),3)+':'+timeString.substr(timeString.indexOf('-',timeString.indexOf('T'))+3,2);
  } else if (timeString.indexOf('+')!=-1) {
    offset = timeString.substr(timeString.indexOf('+'),3)+':'+timeString.substr(timeString.indexOf('+')+3,2);
  }
  return new Date(Date.parse(start+offset));
}
