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
              alert('<' + d.label.raw + '> selected.');
            },
          },
        }
      },
      data: []
    },
    hash: function(email) {
      if (email == undefined) return '';
      //console.log('hash '+email+' = '+ractive.hash(email));
      return '<img class="img-rounded" src="http://www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    stdPartials: [
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "contactFunnelSect", "url": "/partials/contact-funnel-sect.html"}
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
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
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

