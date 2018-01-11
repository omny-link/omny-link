/*******************************************************************************
 * Copyright 2018 Tim Stephenson and contributors
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
$ = jQuery;

var EASING_DURATION = 500;
fadeOutMessages = true;

// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new Ractive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',
  lazy: true,
  template: '#dashTemplate',
  data: {
    entities: [],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    }
  },
  fetchByProxy: function (resource,keypath) {
    console.log('fetchByProxy...');

    var d = {
      action: 'p_resource',
      resource: resource
    };
    return $.ajax({
      type: 'GET',
      url: '/wp-admin/admin-ajax.php',
      data: d,
      dataType: 'json',
      timeout: 30000,
      success: function(data, textStatus, jqxhr) {
        console.log('loaded list...');
        ractive.set(keypath, data);
        ractive.initAutoComplete();
      }
    });
  },
  handleError: function(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) {
    case 401:
    case 403:
      ractive.showError("Session expired, please login again");
      window.location.href='/login';
      break;
    default:
      ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
    }
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showError: function(msg) {
    this.showMessage(msg, 'bg-danger text-danger');
  },
  showFormError: function(formId, msg) {
    this.showError(msg);
    var selector = formId==undefined || formId=='' ? ':invalid' : '#'+formId+' :invalid';
    $(selector).addClass('field-error');
    $(selector)[0].focus();
  },
  showMessage: function(msg, additionalClass) {
    if (additionalClass == undefined) additionalClass = 'bg-info text-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages p').empty().append(msg).removeClass().addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='bg-danger text-danger') setTimeout(function() {
      $('#messages p').fadeOut();
    }, EASING_DURATION*10);
  }
});

$(document).ready(function() {
  ractive.fetchByProxy('/contacts/','contacts');
  ractive.fetchByProxy('/domain/?projection=complete','domain');
  ractive.fetchByProxy('/decision-models/','decisions');
  ractive.fetchByProxy('/tasks/','tasks');
});
