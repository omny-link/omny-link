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
  createBot: function () {
    console.log('createBot...');
    $.ajax({
      url: ractive.getServer()+'/tenants/'+ractive.get('tenant.id')+'/bot',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('plan')),
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        $('#curBotInvalid,#curBotCreate').hide();
        ractive.showMessage('Created bot user');
        ractive.fetch();
      },
    });
  },
  deployProcess: function(process) {
    console.log('deployProcess...'+JSON.stringify(process));
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments/'+ractive.encode(process.url)+'/',
      type: 'POST',
      contentType: 'application/json',
      dataType: 'text',
      data: { deploymentName: 'From status page' },
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Latest version has been deployed');
      },
    });
  },
  encode: function(url) {
    // for some reason 'properly' encoded url (using encodeURICOmponent) does not get routed to Spring controller
    return url.substring(0,url.indexOf('.bpmn')).replace(/\//g,'.');
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
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  }
});

