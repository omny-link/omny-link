var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new AuthenticatedRactive({
  el: 'container',
  template: '#template',
  data: {
    //server: 'http://api.knowprocess.com',
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    events: [],
    formatDateTime: function(timeString) {
//    console.log('formatDate: '+timeString);
      if (timeString==undefined) return '';
    return new Date(timeString).toLocaleString(navigator.languages);
    },
    stdPartials: [
      { "name": "eventCurrentSect", "url": "/partials/event-current-sect.html"},
      { "name": "eventListSect", "url": "/partials/event-list-sect.html"},
      { "name": "navbar", "url": "/partials/event-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    title: 'Event Stream',
    username: localStorage['username'],
  },
  simpleTodoFormExtension: function(x) { 
    console.log('simpleTodoFormExtension: '+JSON.stringify(x));    
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/events?limit=50', function( data ) {
      ractive.merge('events', data);
    });
  },
  oninit: function() {
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  select: function(event) {
    ractive.set('current', event);
//    ractive.set('saveObserver',false);
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/'+event.processInstanceId, function( data ) {
      console.log('found process instance '+JSON.stringify(data));
      event.processInstance = data;
      ractive.set('current',event);
      ractive.toggleResults();
      //      ractive.set('saveObserver',true);
    });
//    ractive.fetchUserNotes();
    $('#currentSect').slideDown();
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#eventsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#eventsTable').slideToggle();
  }
});

