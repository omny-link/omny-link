var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    intro: "Let's get started right away by introducing the Omny Bar icons:",
    title: 'Omny Link',
    title2: 'The next generation of contact management has arrived!',
    username: localStorage['username'],
    gravatar: function(email) {
      if (email == undefined) return '';
      return '<img class="img-rounded" style="width:36px" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36&d=https%3A%2F%2Fapi.omny.link%2F'+ractive.get('tenant.id')+'%2Fgravatars%2F'+ractive.hash(email)+'.png"/>'
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    help: '<p>The contact management page is the central hub from which to manage all your prospects, partners, customers; in fact every person or organisation you may ever need to talk to!</p>\
      <h2>Key concepts</h2>\
      <ul>\
        <li>\
          <h3 id="contactList">Your contact list</h3>\
          <p>This contains all of your contacts, by default only the active ones will be displayed. You can search and filter in any number of ways.</p>\
        </li>\
        <li>\
          <h3 id="currentContact">A one-page view of your contact</h3>\
          <p>Clicking on a row in the contact list will open it up to show the full details including:</p>\
          <ul>\
            <li>Contact details</li>\
            <li>Details of the organisation the contact belongs to</li>\
            <li>A list of activities performed by or in relation to this contact</li>\
            <li>A most-recent-first list of notes about your interactions with this contact</li>\
            <li>Links to any documents that relate to this contact</li>\
          <li>...</li>\
        </ul>\
      </ul>',
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
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
    ],
  },
  fetch: function () {
    console.info('fetch...');
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  sendMessage: function(msg) {
    console.log('sendMessage: '+msg.name);
    var type = (msg['pattern'] == 'inOut' || msg['pattern'] == 'outOnly') ? 'GET' : 'POST';
    var d = (msg['pattern'] == 'inOut') ? {query:msg['body']} : {json:msg['body']};
    console.log('d: '+d);
    //var d['businessDescription']=ractive.get('message.bizKey');
    return $.ajax({
      url: '/msg/'+ractive.get('tenant.id')+'/'+msg.name+'/',
      type: type,
      data: d,
      dataType: 'text',
      success: completeHandler = function(data) {
        console.log('Message received:'+data);
        if (msg['callback']!=undefined) msg.callback(data);
      },
    });
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
});

$(document).ready(function() {
  var statusCode = parseInt(getSearchParameters()['statusCode']);
  var msg = getSearchParameters()['msg'];
  if (statusCode!=undefined && !isNaN(statusCode)) {
    switch (statusCode) {
    case 401:
      msg = "You're not authorised to see that page";
      break;
    case 404:
      msg = "We can't find that page";
      break;
    case 500:
      msg = '';
      break;
    default:
      console.warn('statusCode: '+statusCode);
    }
    ractive.set('title2', 'Ooops! Something went wrong... '+msg);
    ractive.set('intro', 'Please continue by clicking one of the icons below:');
  }
});
