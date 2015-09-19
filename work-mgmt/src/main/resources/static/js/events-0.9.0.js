var EASING_DURATION = 500;
var fadeOutMessages = true;
// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new AuthenticatedRactive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',

  // We could pass in a string, but for the sake of convenience
  // we're passing the ID of the <script> tag above.
  template: '#template',

  // partial templates
  partials: { simpleTodoFormExtension: function(x) {
    return 'HELLO'+x
  } },

  // Here, we're passing in some initial data
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
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    username: localStorage['username'],
  },
  simpleTodoFormExtension: function(x) { 
    console.log('simpleTodoFormExtension: '+JSON.stringify(x));    
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON('/'+ractive.get('tenant.id')+'/events?limit=50', function( data ) {
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
    $.getJSON('/'+ractive.get('tenant.id')+'/process-instances/'+event.processInstanceId, function( data ) {
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

