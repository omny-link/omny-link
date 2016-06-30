var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    title: 'Summary of latest returns',
    username: localStorage['username'],
    hash: function(email) {
      if (email == undefined) return '';
      //console.log('hash '+email+' = '+ractive.hash(email));
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
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
    stdPartials: [
      { "name": "metricListSect", "url": "/partials/metric-list-sect.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);

//    d3.csv("data.csv", function(error, data) {
    d3.json("data/cohort.json", function(error, data) {
      if (error) throw error;
      else {
    	  data.sort(function(a, b) { return b['Category'] < a['Category']; });
    	  sb.displayDataSet("#visualisation", data, { yAxisPercentage: true });
      }
    });
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  }
});