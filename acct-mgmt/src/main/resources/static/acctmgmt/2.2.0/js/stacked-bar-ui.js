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
var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    title: 'Summary of latest returns',
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
    },
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
    server: $env.server,
    stdPartials: [
      { "name": "metricListSect", "url": "/partials/metric-list-sect.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/webjars/supportservices/2.2.0/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
  },
  partials: {
    metricListSect: '',
    loginSect: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    titleArea: ''
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

    this.loadStandardPartials(this.get('stdPartials'));
  }
});
