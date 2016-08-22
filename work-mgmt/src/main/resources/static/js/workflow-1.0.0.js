var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new AuthenticatedRactive({
  el: 'container',

  template: '#template',

  partials: {
  },

  data: {
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    events: [],
    helpUrl: 'http://omny.link/user-help/workflow/#the_title',
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
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    stepIndex: 0,
    title: 'Workflow Definer',
    username: localStorage['username']
  },
  deploy: function() {
    console.info('deploy');
    ractive.showStep('deploy');
  },
  fetch: function() {
    console.info('fetch');
  },
  fetchDecisions: function() {
    console.info('fetchDecisions');
    $.ajax({
      dataType: "json",
      url: '/'+ractive.get('tenant.id')+'/decision-models/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('decisions', data);
          ractive.set('saveObserver',true);
        }else{
          ractive.merge('decisions', data['_embedded'].decisions);
          ractive.set('saveObserver', true);
        }
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#decisionsTable tbody tr:visible').length);
      }
    });
  },
  initControls: function() { 
    console.info('initControls');
//    ractive.initAutoComplete();
//    ractive.initAutoNumeric();
//    ractive.initDatepicker();
  },
  oninit: function() {
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  showStep: function(name) {
    $('.wizard-step a[href="#'+name+'"]').click();
  }
});
