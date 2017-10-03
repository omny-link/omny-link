var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new BaseRactive({
  el: 'container',
  template: '#template',
  partials: {
  },
  data: {
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    events: [],
    helpUrl: '//omny.link/user-help/workflow/#the_title',
    matchRole: function(role) {
      console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    server: $env.server,
    stdPartials: [
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    stepIndex: 0,
    title: 'Workflow Definer'
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
  },
  oninit: function() {

    this.loadStandardPartials(this.get('stdPartials'));
  },
  showStep: function(name) {
    $('.wizard-step a[href="#'+name+'"]').click();
  }
});
