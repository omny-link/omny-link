var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    accounts: [],
    tenants: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    title: 'Tenant Dashboard',
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
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
    helpUrl: '//omny.link/user-help/tenant/#the_title',
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
      //console.info('matchSearch: '+searchTerm);
      var searchTerm = ractive.get('searchTerm');
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0) );
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
    sortAsc: true,
    sortColumn: 'tenantId',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "tenantListSect", "url": "/partials/dash-list-sect.html"},
      { "name": "navbar", "url": "/partials/dash-navbar.html"}
    ],
  },
  partials: {
    helpModal: '',
    navbar: '',
    loginSect: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    tenantListSect: '',
    titleArea: ''
  },
  archive: function (tenantId) {
    console.log('archive: '+tenantId);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+tenantId+'/process-instances/archive',
      crossDomain: true,
      success: function( data ) {
        ractive.set('tenants', data);
        if (ractive.hasRole('admin')) $('.admin').show();
        ractive.fetch();
        ractive.set('saveObserver', true);
      }
    });
  },
  checkServiceLevels: function(tenantId) {
    console.info('checkServiceLevels:'+tenantId);
    $.ajax({
      url: ractive.getServer()+'/'+tenantId+'/messages/omny.decisionInvocation.json',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify( { "decisionName":"CheckServiceLevels" } ),
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.showMessage('Started workflow to check service levels of all contacts and raise alerts accordingly');
        setTimeout(function() { ractive.fetch(); }, EASING_DURATION*4)
      },
    });
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/admin/tenants/',
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
      if (tenantId.endsWith(ractive.id(d))) {
        c = d;
      }
    });
    return c;
  },
  oninit: function() {
    console.log('oninit');

    this.loadStandardPartials(this.get('stdPartials'));
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  }
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
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

