/*******************************************************************************
 * Copyright 2011-2018 Tim Stephenson and contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new BaseRactive({
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
    helpUrl: '//omny.link/user-help/events/#the_title',
    server: $env.server,
    stdPartials: [
      { "name": "eventCurrentSect", "url": "/partials/event-current-sect.html"},
      { "name": "eventListSect", "url": "/partials/event-list-sect.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/event-navbar.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    title: 'Event Stream'
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

