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
    title: 'Tenant Dashboard',
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleDateString(navigator.languages);
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
      //console.info('matchSearch: '+searchTerm);
      var searchTerm = ractive.get('searchTerm');
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0) );
      }
    },
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
    sortAsc: true,
    sortColumn: 'shortId',
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
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "tenantListSect", "url": "/partials/tenant-list-sect.html"},
      { "name": "navbar", "url": "/partials/tenant-navbar.html"}
    ],
  },
  archive: function () {
    console.log('archive');
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/admin/process-instances/archive',
      crossDomain: true,
      success: function( data ) {
        ractive.set('tenants', data);
        if (ractive.hasRole('admin')) $('.admin').show();
        ractive.fetch();
        ractive.set('saveObserver', true);
      }
    });
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/admin/tenants',
      crossDomain: true,
      success: function( data ) {
        ractive.set('tenants', data);
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
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  }
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#tenantsTable tbody tr').length);
  }, 500);
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

