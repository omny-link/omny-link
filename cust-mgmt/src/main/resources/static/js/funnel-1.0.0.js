var EASING_DURATION = 500;

var EDGE_LUMINANCE = 0.1;
var CENTER_LUMINANCE = 0.6;

fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    title: 'Sales Funnel',
    username: localStorage['username'],
    contacts:[],
    filter: { field2: 'owner' },
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
              ractive.set('filter.field','stage');
              ractive.set('filter.value',d.label.raw);
              ractive.filter(ractive.get('filter'));
            },
          },
        }
      },
      data: []
    },
    hash: function(email) {
      if (email == undefined) return '';
      //console.log('hash '+email+' = '+ractive.hash(email));
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    matchFilter: function(obj) {
      var filter = ractive.get('filter');
      //console.info('matchFilter: '+JSON.stringify(filter));
      if (filter==undefined || obj[filter.field]==undefined) {
        return false;
      } else {
        try {
          if (filter.operator==undefined) filter.operator='==';
          var matchField = true;
          if (filter['value']!=undefined) matchField = eval("'"+filter.value.toLowerCase()+"'"+filter.operator+"'"+obj[filter.field].toLowerCase()+"'");
          var matchField2 = true;
          if (filter['value2']!=undefined&& filter.value2!='') matchField2 = eval("'"+filter.value2.toLowerCase()+"'"+filter.operator+"'"+obj[filter.field2].toLowerCase()+"'");
          return matchField && matchField2;
        } catch (e) {
          console.error('Exception during filter');
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
        return true;
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
    sortAsc: false,
    sortColumn: 'lastUpdated',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "contactFunnelSect", "url": "/partials/contact-funnel-sect.html"},
      { "name": "contactListTable", "url": "/partials/contact-list-table.html"}
    ],
  },
  activeStages: function() {
    console.info('activeStages');
    var activeStages = [];
    $.each(ractive.get('stages'), function(i,d) {
      if (d['idx']>=0) activeStages.push(d.name);
    });
    return activeStages;
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/analytics/funnel',
      crossDomain: true,
      success: function( data ) {
        ractive.set('funnel.raw', data);
        if (ractive.get('stages')==undefined || ractive.get('stages').length==0) {
          console.warn('Stages not yet loaded, defer chart load');
          return;
        } else {
          ractive.renderChart();
        }
        ractive.fetchContacts();
      }
    });

  },
  fetchContacts: function () {
    console.info('fetchContacts...');
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
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    ractive.set('searchMatched',$('#contactsTable tbody tr:visible').length);
    $('.col-actions .glyphicon-pencil').hide();
    $('.col-actions .glyphicon-new-window').show();
  },
  inactiveStages: function() {
    console.info('inactiveStages');
    var inactiveStages = [];
    $.each(ractive.get('stages'), function(i,d) {
      if (d['idx']<0) inactiveStages.push(d.name);
    });
    return inactiveStages;
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  openInNewWindow: function(obj) {
    console.info('openInNewWindow');
    window.open('/contacts.html?id='+ractive.uri(obj));
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

