/*******************************************************************************
 * Copyright 2015-2018 Tim Stephenson and contributors
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
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    intro: "Let's get started right away by introducing the toolbar icons:",
    title: $env.appName,
    tagLine: $env.tagLine,
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    helpUrl: '//omny-link.github.io/user-help/',
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
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "toolbar", "url": "/partials/toolbar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
    ],
  },
  partials: {
    'helpModal': '',
    'loginSect': '',
    'profileArea': '',
    'sidebar': '',
    'supportBar': '',
    'titleArea': '',
    'toolbar': ''
  },
  fetch: function () {
    console.info('fetch...');
  },
  oninit: function() {
    console.log('oninit');
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

//$(document).ready(function() {
//  var statusCode = parseInt(getSearchParameters()['statusCode']);
//  var msg = decodeURIComponent(getSearchParameters()['msg']);
//  if (statusCode!=undefined && !isNaN(statusCode)) {
//    switch (statusCode) {
//    case 401:
//      msg = "You're not authorised to see that page";
//      break;
//    case 404:
//      msg = "We can't find that page";
//      break;
//    case 500:
//      msg = '';
//      break;
//    default:
//      console.warn('statusCode: '+statusCode);
//    }
//    ractive.set('tagLine', 'Ooops! Something went wrong... '+msg);
//    ractive.set('intro', 'Please continue by clicking one of the icons below:');
//  }
//});
