var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    accounts: [],
    csrfToken: getCookie(CSRF_COOKIE),
    tenants: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    //saveObserver:false,
    title: 'Tenant Profile',
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
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
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
    help: '<p>The tenant management page is the central hub from which to manage all your prospects, partners, customers; in fact every person or organisation you may ever need to talk to!</p>\
      <h2>Key concepts</h2>\
      <ul>\
        <li>\
          <h3 id="tenantList">Your tenant list</h3>\
          <p>This contains all of your tenants, by default only the active ones will be displayed. You can search and filter in any number of ways.</p>\
        </li>\
        <li>\
          <h3 id="currentTenant">A one-page view of your tenant</h3>\
          <p>Clicking on a row in the tenant list will open it up to show the full details including:</p>\
          <ul>\
            <li>Tenant details</li>\
            <li>Details of the organisation the tenant belongs to</li>\
            <li>A list of activities performed by or in relation to this tenant</li>\
            <li>A most-recent-first list of notes about your interactions with this tenant</li>\
            <li>Links to any documents that relate to this tenant</li>\
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
          || (searchTerm.startsWith('created>') && new Date(obj.firstTenant)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created<') && new Date(obj.firstTenant)<new Date(ractive.get('searchTerm').substring(8)))
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
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "statusTable", "url": "/partials/tenant-status-table.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "tenantListSect", "url": "/partials/tenant-list-sect.html"},
      { "name": "navbar", "url": "/partials/tenant-navbar.html"},
      { "name": "currentTenantSect", "url": "/partials/tenant-current-sect.html"}
    ],
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var tenant = { account: {}, author:ractive.get('username'), tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select( tenant );
    ractive.initTags();
  },
  addDoc: function (tenant) {
    console.log('addDoc '+tenant+' ...');
    if (tenant==undefined || tenant == '') {
      ractive.showMessage('You must have created your tenant before adding documents');
      return;
    }
    ractive.set('current.doc', { author:ractive.get('username'), tenant: ractive.stripProjection(tenant), url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
  },
  addNote: function (tenant) {
    console.log('addNote '+tenant+' ...');
    if (tenant==undefined || tenant == '') {
      ractive.showMessage('You must have created your tenant before adding notes');
      return;
    }
    ractive.set('current.note', { author:ractive.get('username'), tenant: ractive.stripProjection(tenant), content: undefined});
    $('#notesTable tr:nth-child(1)').slideDown();
  },
  addSector: function () {
    console.log('addSector ...');
    ractive.showError('Not yet implemented');
    //$('#curPartnerSectors').append($('#sectorTemplate').html());
  },
  edit: function (tenant) {
    console.log('edit'+tenant+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('currentIdx',ractive.get('tenants').indexOf(tenant));
    ractive.select( tenant );
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  /*followUp: function(tenantId) { 
    console.log('followUp: '+JSON.stringify(tenantId));
    $.ajax({
      type: 'GET',
      url: '/msg/firmgains/firmgains.followUp.json?query={"tenantId":"'+tenantId+'","tenantId":"'+ractive.get('tenant.id')+'"}&businessDescription=FollowUp',
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
      url: ractive.getServer()+'/tenants/'+ractive.get('tenant.id'),
      crossDomain: true,
      success: function( data ) {
//        if (data['_embedded'] == undefined) {
          ractive.set('current', data);
//        } else {
//          ractive.merge('tenants', data['_embedded'].tenants);
//        }
        // If this is status page allow no changes
        if (document.location.href.indexOf('status.html')!=-1) $('input[type="checkbox"]').attr('disabled','disabled');
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#tenantsTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected')
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.set('searchMatched',$('#tenantsTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(tenantId) { 
    console.log('find: '+tenantId);
    var c; 
    $.each(ractive.get('tenants'), function(i,d) { 
      if (tenantId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save tenant: '+ractive.get('current').lastName+'...');
    ractive.set('saveObserver',false);
    var id = ractive.getId(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) { 
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      // cannot save tenant and account in one (grrhh), this will clone...
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
//      console.log('ready to save tenant'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? '/tenants/' : id,
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
            var currentIdx = ractive.get('tenants').push(ractive.get('current'))-1;
            ractive.set('currentIdx',currentIdx);
            break;
          case 204: 
            ractive.splice('tenants',ractive.get('currentIdx'),1,ractive.get('current'));
            break;
          }
          ractive.showMessage('Tenant saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as tenant is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as tenant is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  saveAccount: function () {
    if (ractive.get('current.account')==undefined) return;
    console.log('saveAccount '+ractive.get('current.account.name')+' ...');
    var id = ractive.get('current.accountId');
    console.log(' id: '+id);
    ractive.set('saveObserver',false);
    ractive.set('current.account.tenantId',ractive.get('tenant.id'));
    if (ractive.get('current.account.companyNumber')=='') ractive.set('current.account.companyNumber',undefined); 
    if ($('#currentAccountForm:visible').length!=0 && document.getElementById('currentAccountForm').checkValidity()) { 
      $.ajax({
        url: id == undefined ? ractive.getServer()+'/'+ractive.get('tenant.id')+'/accounts/' : ractive.get('tenant.id')+'/accounts/'+id,
        type: id == undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(ractive.get('current.account')),
        success: completeHandler = function(data, textStatus, jqXHR) {
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current.account.id',location.substring(location.lastIndexOf('/')+1));
          var tenantAccountLink = ractive.stripProjection(ractive.get('current')._links.self.href);
          tenantAccountLink+='/account';
          console.log(' attempt to link account: '+location+' to '+tenantAccountLink);
          if (jqXHR.status == 201) { 
            $.ajax({
              url: tenantAccountLink,
              type: 'PUT',
              contentType: 'text/uri-list',
              data: location,
              success: completeHandler = function(data, textStatus, jqXHR) {
                ractive.set('saveObserver',false);
                console.log('linked account: '+location+' to '+tenantAccountLink);
                ractive.merge('tenants', ractive.get('current'));
                ractive.showMessage('Account saved');
                ractive.set('saveObserver',true);
              }
            });
          } else if (jqXHR.status == 204) {
            ractive.set('saveObserver',false);
            var currentIdx =ractive.get('currentIdx');
            ractive.splice('tenants',currentIdx,1,ractive.get('current'));
            ractive.showMessage('Account updated');
            ractive.set('saveObserver',true);
          }
          ractive.set('tenants.'+ractive.get('currentIdx')+'.accountName',ractive.get('current.account.name'));
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
  select: function(tenant) {
    console.log('select: '+JSON.stringify(tenant));
    ractive.set('saveObserver',false);
    if (tenant.account == undefined || tenant.account == '') tenant.account = new Object();
    // default owner to current user
    if (tenant.owner == undefined || tenant.owner == '') tenant.owner = ractive.get('username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (tenant._links == undefined && tenant.links != undefined) { 
	    tenant._links = tenant.links;
	    $.each(tenant.links, function(i,d) { 
        if (d.rel == 'self') tenant._links.self = { href:d.href };
      });
	  }
	  if (tenant._links != undefined) {
	    var url = ractive.stripProjection(tenant._links.self.href);
	    if (url == undefined) {
	      ractive.showError('No tenant selected, please check link');
	      return;
	    }
	    console.log('loading detail for '+url);
	    $.getJSON(ractive.getServer()+url+'?projection=complete', function( data ) {
        console.log('found tenant '+data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        ractive.fetchNotes();
        ractive.fetchDocs();
        // sort most recent first
        ractive.get('current.activities').sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
        if (ractive.get('current.account')!=undefined &&
            (ractive.get('current.account.businessWebsite')==undefined || ractive.get('current.account.businessWebsite')=='')) ractive.inferDomainName(); 
      });
    } else { 
      console.log('Skipping load as no _links.'+tenant.lastName);
      ractive.set('current', tenant);
      ractive.set('saveObserver',true);
    }
	  ractive.toggleResults();
	  $('#currentSect').slideDown();
  },
  selectMultiple: function(tenant) {
    console.info('selectMultiple: '+tenant.selfRef);
    if ($('tr[data-href="'+tenant.selfRef+'"] input[type="checkbox"]').prop('checked')) {
      console.log('  checked: '+$('tr[data-href="'+tenant.selfRef+'"] input[type="checkbox"]').prop('checked'));
      ractive.push('selectMultiple', tenant.selfRef);
    } else {
      var idx = ractive.get('selectMultiple').indexOf(tenant.selfRef);
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
    $('#tenantsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#tenantsTable').slideDown({ queue: true });
  },
  startCustomAction: function(key, label, tenant, form) {
    console.log('startCustomAction: '+key+' for '+tenant.id);
    var instanceToStart = {
        processDefinitionId: key,
        businessKey: tenant.firstName+' '+tenant.lastName,
        label: label,
        processVariables: { 
            tenantId: ractive.getId(tenant), 
            tenantShortId: ractive.stripProjection(ractive.getId(tenant).substring(ractive.getId(tenant).indexOf('/tenants')+10)), 
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
    $('#tenantsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#tenantsTable').slideToggle();
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
        url: '/'+ractive.get('tenant.id')+'/'+entity+'/upload'+fileExt.toLowerCase(),
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
    ractive.set('searchMatched',$('#tenantsTable tbody tr').length);
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
    console.warn  ('Skipped tenant save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
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

