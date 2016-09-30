var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    metrics:[],
    title: 'Moving Window',
    username: localStorage['username'],
    formatDate: function(date) {
      if (date==undefined) return 'n/a';
      return date.toLocaleDateString(navigator.languages);
    },
    hash: function(email) {
      if (email == undefined) return '';
      //console.log('hash '+email+' = '+ractive.hash(email));
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    helpUrl: '//omny.link/user-help/window/#the_title',
    matchRole: function(role) {
      console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
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
      { "name": "metricListSect", "url": "/partials/metric-list-sect.html"},
      { "name": "navbar", "url": "/partials/metric-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    windowDays: 90
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);

    var format = d3.time.format("%Y-%m-%dT%H:%M:%S.%L%Z").parse;
    var url = ractive.getServer()+'/'+ractive.get('tenant.id')+'/metrics/?window='+ractive.get('windowDays');
    $.getJSON(url, function(json) {
      ractive.set('rawMetrics', json);
      var data = {
        labels: ['impressions', 'sessions', 'visitors', 'enquiries'],
        x: [],
        y1: [],
        y2: [],
        y3: [],
        y4: [],
      }
      
      var seen = [];
      for (i in json) {
        var d = json[i];
        if (data.labels.indexOf(d.name) != -1) {
          if (seen.indexOf(d.occurred) == -1) {
            seen.push(d.occurred);
            data['x'].push(format(d.occurred));
          }
          if (d.name=='impressions') data.y1.push(d.value);
          if (d.name=='sessions') data.y2.push(d.value);
          if (d.name=='visitors') data.y3.push(d.value);
          if (d.name=='enquiries') data.y4.push(d.value);
        }
      }
      ractive.set('metrics', data);
      mw.displayDataSet('#visualisation', data);
    });
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  }
});
ractive.observe('windowDays', function(newValue, oldValue, keypath) {
  if (newValue==undefined) ractive.set('title', 'Moving Window');
  else ractive.set('title', newValue + ' Day Moving Window');

  if (oldValue!=undefined && newValue!=oldValue) ractive.fetch();
});