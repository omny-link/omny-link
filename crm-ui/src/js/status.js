/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributorss
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
    accounts: [],
    entityName: 'tenant',
    tenants: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    saveObserver:false,
    title: 'Tenant Profile',
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
    },
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    formatJson: function(json) {
      console.log('formatJson: '+json);
      try {
        var obj = JSON.parse(json);
        var html = '';
        $.each(Object.keys(obj), function(i,d) {
          html += (typeof obj[d] == 'object' ? '' : '<b>'+d+'</b>: '+obj[d]+'<br/>');
        });
        return html;
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    formatTags: function(tags) {
      var html = '';
      if (tags==undefined) return html;
      var tagArr = tags.split(',');
      $.each(tagArr, function(i,d) {
        html += '<span class="img-rounded" style="background-color:'+d+'">&nbsp;&nbsp;</span>';
      });
      return html;
    },
    gravatar: function(email) {
      return ractive.gravatar(email);
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
    server: $env.server,
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "navbar", "url": "/partials/status-navbar.html"},
      { "name": "statusTable", "url": "/partials/status-table.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "currentTenantSect", "url": "/partials/status-current-sect.html"}
    ],
  },
  partials: {
    currentTenantSect: '',
    helpModal: '',
    instanceListSect: '',
    navbar: '',
    loginSect: '',
    profileArea: '',
    sidebar: '',
    statusTable: '',
    supportBar: '',
    titleArea: ''
  },
  createBot: function (force) {
    console.log('createBot...');
    $.ajax({
      url: ractive.getServer()+'/admin/tenants/'+ractive.get('tenant.id')+'/bot'+(force ? '/?force=true' : ''),
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('plan')),
      success: function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        $('#curBotInvalid,#curBotCreate').hide();
        ractive.showMessage('Created bot user');
        ractive.fetch();
      },
    });
  },
  deployProcess: function(process) {
    console.log('deployProcess...'+JSON.stringify(process));
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments/'+ractive.encode(process.url)+'/',
      type: 'POST',
      contentType: 'application/json',
      dataType: 'text',
      data: { deploymentName: 'From status page' },
      success: function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Latest version has been deployed');
      },
    });
  },
  deployTemplate: function(template) {
    console.log('deployTemplate...'+JSON.stringify(template));
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/',
      type: 'POST',
      contentType: 'application/json',
      dataType: 'text',
      data: JSON.stringify({
        name: template.ref,
        owner: ractive.get('profile.username'),
        richContent: 'TODO',
        shortContent: 'TODO',
        status: 'Draft',
        title: template.name
      }),
      success: function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Latest version has been deployed');
      },
    });
  },
  encode: function(url) {
    // for some reason 'properly' encoded url (using encodeURICOmponent) does not get routed to Spring controller
    return url.substring(0,url.indexOf('.bpmn')).replace(/\//g,'.');
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    //if (ractive.get('tenant.name'));
    ractive.set('current', ractive.get('tenant'));
    if (document.location.href.indexOf('status.html')!=-1) $('input[type="checkbox"]').attr('disabled','disabled');
    if (ractive.hasRole('admin')) $('.admin').show();
    if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
    ractive.showSearchMatched();
    ractive.set('saveObserver', true);
    if (ractive.FF.TENANT_SUMMARY) {
      $.getJSON(ractive.getServer()+'/admin/tenants/'+ractive.get('tenant.id'), function(data) {
        ractive.set('currentSummary', data);
      });
    }
  }
});

