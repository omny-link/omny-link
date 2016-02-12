var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    csrfToken: getCookie(CSRF_COOKIE),
    contacts: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    //saveObserver:false,
    title: 'Contact Management',
    username: localStorage['username'],
    age: function(timeString) {
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
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleDateString(navigator.languages);
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
      return '<img class="img-rounded" src="http://www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    haveStageReasons: function() {
      console.info('haveStageReasons?');
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
      { "name": "mergeModal", "url": "/partials/contact-merge-sect.html"},
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
    if (contact==undefined || contact == '') {
      ractive.showMessage('You must have created your contact before adding notes');
      return;
    }
    ractive.set('current.note', { author:ractive.get('username'), contact: ractive.stripProjection(contact), content: undefined});
    $('#notesTable tr:nth-child(1)').slideDown();
  },
  addSector: function () {
    console.log('addSector ...');
    ractive.showError('Not yet implemented');
    //$('#curPartnerSectors').append($('#sectorTemplate').html());
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
    var url = obj.links != undefined
        ? obj.links.filter(function(d) { console.log('this:'+d);if (d['rel']=='self') return d;})[0].href
        : obj._links.self.href;
    $.ajax({
        url: url,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.toggleResults();
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          console.error('XX: '+errorThrown);
          ractive.handleError(jqXHR,textStatus,errorThrown);
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
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#contactsTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchDocs: function() { 
    $.getJSON(ractive.getId(ractive.get('current'))+'/documents',  function( data ) {
      if (data['_embedded'] != undefined) {
        console.log('found docs '+data);
        ractive.merge('current.documents', data['_embedded'].documents);
        // sort most recent first
        ractive.get('current.documents').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
      ractive.set('saveObserver',true);
    });
  },
  fetchNotes: function() { 
    $.getJSON(ractive.getId(ractive.get('current'))+'/notes',  function( data ) {
      if (data['_embedded'] != undefined) {
        console.log('found notes '+data);
        ractive.merge('current.notes', data['_embedded'].notes);
        // sort most recent first
        ractive.get('current.notes').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
    });
  },
  fetchCompaniesHouseInfo: function() { 
    console.info('fetchCompaniesHouseInfo for '+ractive.get('current.account.companyNumber'));
    ractive.sendMessage({
      name:"omny.companyRecord",
      body:JSON.stringify({companyNumber:ractive.get('current.account.companyNumber')}),
      callback:function(results) {
        //results = JSON.parse(results);
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
  getId: function(contact) { 
    console.log('getId: '+contact);
    var uri; 
    if (contact['links']!=undefined) {
      $.each(contact.links, function(i,d) { 
        if (d.rel == 'self') { 
          uri = d.href;
        }
      });
    } else if (contact['_links']!=undefined) {
      uri = ractive.stripProjection(contact._links.self.href);
    } 
    return uri;
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
  save: function () {
    console.log('save contact: '+ractive.get('current').lastName+'...');
    ractive.set('saveObserver',false);
    var id = ractive.getId(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) { 
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      // cannot save contact and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      //console.log('account: '+JSON.stringify(tmp.account));
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
        url: id === undefined ? '/'+tmp.tenantId+'/contacts/' : id.replace(/contacts/,tmp.tenantId+'/contacts'),
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
            ractive.set('currentIdx',ractive.get('contacts').push(ractive.get('current'))-1);
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
    console.log('saveAccount '+ractive.get('current.account').name+' ...');
    if (ractive.get('current.account') == undefined) ractive.set('current.account',{});
    var id = ractive.get('current.accountId');
    console.log(' id: '+id);
    ractive.set('saveObserver',false);
    ractive.set('current.account.tenantId',ractive.get('tenant.id'));
    if (ractive.get('current.account.companyNumber')=='') ractive.set('current.account.companyNumber',undefined); 
    ractive.set('saveObserver',true);
    if ($('#currentAccountForm:visible').length!=0 && document.getElementById('currentAccountForm').checkValidity()) { 
      $.ajax({
        url: id == undefined ? ractive.getServer()+'/'+ractive.get('tenant.id')+'/accounts/' : ractive.get('tenant.id')+'/accounts/'+id,
        type: id == undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(ractive.get('current.account')),
        success: completeHandler = function(data, textStatus, jqXHR) {
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current.account.id',location.substring(location.lastIndexOf('/')+1));
          var contactAccountLink = ractive.stripProjection(ractive.get('current')._links.self.href);
          contactAccountLink+='/account';
          console.log(' attempt to link account: '+location+' to '+contactAccountLink);
          if (jqXHR.status == 201) { 
            $.ajax({
              url: contactAccountLink,
              type: 'PUT',
              contentType: 'text/uri-list',
              data: location,
              success: completeHandler = function(data, textStatus, jqXHR) {
                console.log('linked account: '+location+' to '+contactAccountLink);
                ractive.get('contacts').push(ractive.get('current'));
                ractive.showMessage('Account saved');
              }
            });
          } else if (jqXHR.status == 204) {
            ractive.splice('contacts',ractive.get('currentIdx'),1,ractive.get('current'));
            ractive.showMessage('Account updated');
          }
          //ractive.get('contacts')[ractive.get('currentIdx')].lastUpdated=new Date().toISOString();
        }
      });
    } else if ($('#currentAccountForm:visible').length!=0) {
      console.warn('Cannot save yet as account is invalid');
      $('#currentAccountForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as account is incomplete');
    }
  },
  saveDoc: function () {
    console.log('saveDoc '+JSON.stringify(ractive.get('current.doc'))+' ...');
    var n = ractive.get('current.doc');
    n.url = $('#doc').val();
    var url = ractive.getId(ractive.get('current'))+'/documents';
    url = url.replace('contacts/',ractive.get('tenant.id')+'/contacts/');
    if (n.url.trim().length > 0) { 
      $('#docsTable tr:nth-child(1)').slideUp();
      $.ajax({
        /*url: '/documents',
        contentType: 'application/json',*/
        url: url,
        type: 'POST',
        data: n,
        success: completeHandler = function(data) {
          console.log('data: '+ data);
          ractive.showMessage('Document link saved successfully');
          ractive.fetchDocs();
          $('#doc').val(undefined);
        }
      });
    } 
  },
  saveNote: function () {
    console.info('saveNote '+JSON.stringify(ractive.get('current.note'))+' ...');
    var n = ractive.get('current.note');
    n.content = $('#note').val();
    var url = ractive.getId(ractive.get('current'))+'/notes';
    url = url.replace('contacts/',ractive.get('tenant.id')+'/contacts/');
    console.log('  url:'+url);
    if (n.content.trim().length > 0) { 
      $('#notesTable tr:nth-child(1)').slideUp();
      $.ajax({
        /*url: '/notes',
        contentType: 'application/json',*/
        url: url,
        type: 'POST',
        data: n,
        success: completeHandler = function(data) {
          console.log('response: '+ data);
          ractive.showMessage('Note saved successfully'); 
          ractive.fetchNotes();
          $('#note').val(undefined);
        }
      });
    }
  },
  searchCompaniesHouse: function() {
    console.info('searchCompaniesHouse');
    var q = ractive.get('current.account.name');
    ractive.sendMessage({
      name:"omny.companySearch",
      body:JSON.stringify({companyName:q}),
      callback:function(results) {
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
	    var url = ractive.stripProjection(contact._links.self.href);
	    if (url == undefined) {
	      ractive.showError('No contact selected, please check link');
	      return;
	    }
	    console.log('loading detail for '+url);
	    $.getJSON(ractive.getServer()+url+'?projection=complete', function( data ) {
        console.log('found contact '+data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        ractive.fetchNotes();
        ractive.fetchDocs();
        if (ractive.get('current.account.companyNumber')!=undefined) ractive.fetchCompaniesHouseInfo();
        // sort most recent first
        ractive.get('current.activities').sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
      });
    } else { 
      console.log('Skipping load as no _links.'+contact.lastName);
      ractive.set('current', contact);
      ractive.set('saveObserver',true);
    }
	  ractive.toggleResults();
	  $('#currentSect').slideDown();
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
    $.ajax({
      url: '/msg/'+ractive.get('tenant.id')+'/'+msg.name+'/',
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
  stripProjection: function(link) {
    if (link==undefined) return;
    var idx = link.indexOf('{projection');
    if (idx==-1) { 
      idx = link.indexOf('{?projection');
      if (idx==-1) { 
        return link;
      } else {
        return link.substring(0,idx);
      }
    } else {
      return link.substring(0,idx);
    }
  },
  submitCustomAction: function() {
    console.info('submitCustomAction');
    $.ajax({
      url: '/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('instanceToStart')),
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.showMessage('Started workflow "'+ractive.get('instanceToStart.label')+'" for '+ractive.get('instanceToStart.businessKey'));
        $('#customActionModal').modal('hide');
      },
    });
  },
  toggleResults: function() {
    console.log('toggleResults');
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
    return $.ajax({
        type: 'POST',
        url: '/'+ractive.get('tenant.id')+'/'+entity+'/upload',
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
  ignored=['current.documents','current.doc','current.notes','current.note'];
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

ractive.observe('current.account.name', function(newValue, oldValue, keypath) {
  console.log('account name changing from '+oldValue+' to '+newValue);
  if (newValue!=undefined && newValue!='') {
    $('#curCompanyNumber').typeahead('destroy');
    ractive.set('current.account.companyNumber',undefined);
    ractive.searchCompaniesHouse();
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

