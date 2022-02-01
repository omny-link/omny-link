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
import { BaseRactive } from './base';
// import {$,jQuery} from 'jquery';

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    intro: "Let's get started right away by introducing the toolbar icons:",
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    gravatar: function(email) {
      return ractive.ractive(email);
    },
    hash: function(email) {
      return ractive.hash(email);
    },
    helpUrl: '//omny-link.github.io/user-help/#the_title',
    matchRole: function(role) {
      console.info('matchRole: '+role);
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.ractive"},
      { "name": "profileArea", "url": "/partials/profile-area.ractive"},
      { "name": "sidebar", "url": "/partials/sidebar.ractive"},
      { "name": "titleArea", "url": "/partials/title-area.ractive"},
    ],
  },
  partials: {
    'helpModal': '',
    'profileArea': '',
    'sidebar': '',
    'supportBar': '',
    'titleArea': '',
    'toolbar': ''
  },
});

$(window).on("load", function() {
// document.addEventListener("DOMContentLoaded", function(){
  ractive.initBase();
});

ractive.observe('$env', function(newValue, oldValue, keypath) {
  console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
  ractive.set('title', import.meta.env.VITE_APP_NAME);
  ractive.set('tagLine', import.meta.env.VITE_TAG_LINE);
});