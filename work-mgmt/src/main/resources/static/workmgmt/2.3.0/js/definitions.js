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
  parser: new DOMParser(),
  serializer: new XMLSerializer(),
  transformer: new XSLTProcessor(),
  partials: { simpleTodoFormExtension: function(x) {
    return 'HELLO'+x
  } },
  data: {
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    definitions: [],
    diagPanZoomHandlers: [],
    entityPath: '/process-definitions',
    showDetails: false,
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
    },
    formatDateTime: function(timeString) {
//    console.log('formatDate: '+timeString);
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleString(navigator.languages);
    },
    formatJson: function(json) {
      console.log('formatJson: '+json);
      try {
        var obj = json;
        if (typeof json == 'string') obj = JSON.parse(json);
        return ractive.json2Html(obj);
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    hasRole: function(role) {
      return ractive.hasRole(role);
    },
    helpUrl: '//omny.link/user-help/definitions/#the_title',
    renderBpmnIcon: function(activityType) {
      switch(activityType) {
      case 'boundaryError':
        return 'bpmn-icon-intermediate-event-catch-error';
      case 'boundarySignal':
        return 'bpmn-icon-intermediate-event-catch-signal';
      case 'boundaryTimer':
        return 'bpmn-icon-intermediate-event-catch-timer';
      case 'callActivity':
        return 'bpmn-icon-call-activity';
      case 'endEvent':
        return 'bpmn-icon-end-event-none';
      case 'exclusiveGateway':
        return 'bpmn-icon-gateway-xor';
      case 'inclusiveGateway':
        return 'bpmn-icon-gateway-or';
      case 'intermediateThrowEvent':
        return 'bpmn-icon-intermediate-event-none';
      case 'intermediateSignalThrow':
        return 'bpmn-icon-intermediate-event-throw-signal';
      case 'messageStartEvent':
        return 'bpmn-icon-start-event-message';
      case 'parallelGateway':
        return 'bpmn-icon-gateway-parallel';
      case 'receiveTask':
        return 'bpmn-icon-receive-task';
      case 'scriptTask':
        return 'bpmn-icon-script-task';
      case 'sendTask':
        return 'bpmn-icon-send-task';
      case 'serviceTask':
        return 'bpmn-icon-service-task';
      case 'startEvent':
        return 'bpmn-icon-start-event-none';
      case 'userTask':
        return 'bpmn-icon-user-task';
      default:
        console.error('Unable to render '+activityType);
      }
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
    matchSearch: function(obj) {
      var searchTerm = ractive.get('searchTerm');
      //console.info('matchSearch: '+searchTerm);
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.name!=undefined && obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.description!=undefined && obj.description.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.id.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (searchTerm.startsWith('category:') && obj.category!=undefined && obj.category.indexOf(ractive.get('searchTerm').substring(9))!=-1)
          || (searchTerm.startsWith('!executable') && (obj.deploymentId==undefined || obj.deploymentId==''))
          || (searchTerm.startsWith('executable') && obj.deploymentId!=undefined && obj.deploymentId!='')
          || (searchTerm.startsWith('version:latest') && ractive.isLatestVersion(obj))
        );
      }
    },
    searchTerm: 'version:latest',
    server: $env.server,
    sort: function (array, column, asc) {
      console.info('sort '+(asc ? 'ascending' : 'descending')+' on: '+column);
      array = array.slice(); // clone, so we don't modify the underlying data

      return array.sort( function ( a, b ) {
        if (b[column]==undefined || b[column]==null || b[column]=='') {
          return (a[column]==undefined || a[column]==null || a[column]=='') ? 0 : -1;
        } else if (asc) {
          return a[ column ] < b[ column ] ? -1 : 1;
        } else {
          return a[ column ] > b[ column ] ? -1 : 1;
        }
      });
    },
    sortAsc: false,
    sortColumn: 'startTime',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    sortInstanceAsc: false,
    sortInstanceColumn: 'startTime',
    sortedInstance: function(column) {
      console.info('sortedInstance');
      if (ractive.get('sortInstanceColumn') == column && ractive.get('sortInstanceAsc')) return 'sort-asc';
      else if (ractive.get('sortInstanceColumn') == column && !ractive.get('sortInstanceAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "defnCurrentSect", "url": "/partials/defn-current-sect.html"},
      { "name": "defnListSect", "url": "/partials/defn-list-sect.html"},
      { "name": "defnPropSect", "url": "/partials/defn-property-sect.html"},
      { "name": "designerSect", "url": "/partials/designer-sect.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "instanceListSect", "url": "/partials/instance-list-sect.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "navbar", "url": "/partials/defn-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/webjars/supportservices/2.2.0/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    title: 'Process Definitions'
  },
  partials: {
    defnCurrentSect: '',
    defnListSect: '',
    defnPropSect: '',
    designerSect: '',
    helpModal: '',
    instanceListSect: '',
    navbar: '',
    loginSect: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    titleArea: ''
  },
  simpleTodoFormExtension: function(x) {
    console.log('simpleTodoFormExtension: '+JSON.stringify(x));
  },
  activate: function(key) {
    console.log('activate: '+key);
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+key+'/activate',
      type: 'POST',
      contentType: 'application/json',
      success: completeHandler = function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Activated workflow "'+key+'"');
        ractive.showResults();
      },
    });
  },
  add: function () {
    console.log('add...');
    $('#upload').slideDown();
  },
  addDeploymentResource: function () {
    console.log('add...');
    //$('#upload fieldset').append($('#resourceControl').html());
    $("#resourceFile").click();
  },
  addProcess: function () {
    console.info('addProcess...');
    $('#designer').slideDown();
  },
  collapseAdd: function () {
    console.log('collapseAdd...');
    $('#upload').slideUp();
  },
  collapseDesigner: function () {
    console.log('collapseDesigner...');
    $('#designer').slideUp();
  },
  define: function(defn) {
    console.info('define');
    ractive.set('saveObserver',false);
    if (document.getElementById('defnForm')==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('defnForm').checkValidity()) {
      $.ajax({
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/',
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(defn),
          success: completeHandler = function(data) {
            console.log('  Success, received: '+data);
            ractive.set('defn', data);
            ractive.set('saveObserver',true);
          }
      });
    } else {
      console.warn('Unfortunately the process text is invalid');
      $('#defnForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  delete: function (deploymentId) {
    console.log('delete '+deploymentId+'...');
    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments/'+deploymentId,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteSelectedInstances: function() {
    console.info('deleteSelectedInstances');
    $.each(ractive.get('current.instances'), function(i,d) {
      if (d.selected) {
        console.log('delete:  '+d.processs);
        ractive.deleteInstance(d,i);
      }
    });
    ractive.fetchInstances();
  },
  deleteInstance: function (instance,idx) {
    console.info('deleteInstance '+instance.id+'...');
    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/'+instance.id,
        type: 'DELETE',
        success: completeHandler = function(data) {
          console.log('  Success, received: '+data);
          ractive.get('current.instances').splice(idx,1);
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deploy: function(formId) {
    console.info('deploy');
    ractive.set('saveObserver',false);
    if (document.getElementById(formId)==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById(formId).checkValidity()) {
      var defn = $('#'+formId).serializeArray();
      $.ajax({
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments/',
          type: 'POST',
          data: defn,
          success: completeHandler = function(data) {
            console.log('  Success, received: '+data);
            ractive.set('defn', data);
            ractive.set('saveObserver',true);
            ractive.collapseDesigner();
            ractive.showMessage('Deployed successfully');
          }
      });
    } else {
      console.warn('Unfortunately the process text is invalid');
      $('#defnForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  download: function() {
    console.info('download');
    $.ajax({
      headers: {
        "Accept": "application/xml"
      },
      url: ractive.tenantUri(ractive.get('current'))+'.bpmn',
      crossDomain: true,
      success: function( data ) {
        console.warn('response;'+data);
        var serializer = new XMLSerializer();
        var bpmn = serializer.serializeToString(data);
        ractive.downloadUri("data:application/xml," + encodeURIComponent(bpmn),ractive.get('current.id')+".bpmn");
      }
    });
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions', function( data ) {
      ractive.merge('definitions', data);
      ractive.set('searchMatched',$('#definitionsTable tbody tr:visible').length);
      if (ractive.get('searchMatched')==1) {
        var id = $($('#definitionsTable tbody tr:visible')[0]).data('id');
        ractive.select(Array.findBy('id', id, ractive.get('definitions')));
      }
    });
  },
  fetchBpmn: function() {
    console.log('fetch bpmn');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+ractive.get('current.id')+'.bpmn', function( data ) {
      console.log('found bpmn '+data);
      var bpmnDoc = ractive.parser.parseFromString(data, "text/xml");
      ractive.set('current.bpmn',bpmnDoc);
    });
  },
  fetchDiagrams: function(definition) {
    console.info('fetchDiagrams');
    $.each(ractive.get('current.diagramIds'), function(idx,d) {
      var diagId = d;
      console.log('  fetch diagram: '+diagId);
      $.get(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id+'/'+diagId+'.svg', function( data, textStatus, jqXHR ) {
        try {
          var diagDoc = $.parseXML(data);
          var diagId = diagDoc.getElementsByTagName('svg')[0].id;
          var diagName = diagDoc.getElementsByTagName('svg')[0].attributes['name'].value;
          console.log('found diagram: '+diagId);
          ractive.splice('current.diagrams',ractive.get('current.diagramIds').indexOf(diagId),0, { id: diagId, name: diagName, image: data });
          $('.event, .dataStoreReference, .flow, .gateway, .lane, .participant, .task').on('click',ractive.showSelection);
          $('[data-called-element]').attr('class',$('[data-called-element]').attr('class')+' clickable');
          $('[data-called-element]').click(function(ev) {
            console.log('drill down to call activity');
            console.log('selected: '+JSON.stringify($(ev.target)));
            console.log('selected: '+ev.target.attributes['data-called-element'].value);
            ractive.select(ractive.find(ev.target.attributes['data-called-element'].value));
            $('[data-toggle="tooltip"]').tooltip();
          });
          $('[data-ref]').click(function(ev) { ractive.showSelectedId($(ev.target).data('ref')); });
        } catch (e) {
          console.error('  e:'+e);
        }
      }, 'text');
    });
  },
  fetchInstances: function() {
    console.log('fetch instances');
    // collapse any visible instances so notice any new instances and to force refresh on re-open
    $('section.instanceSect[data-instance-id]').hide();
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+ractive.get('current.id')+'/instances?limit=20', function( data ) {
      console.log('found instances '+data.length);
      ractive.set('current.instances',data);
    });
  },
  fetchMore: function() {
    console.log('fetch more instances');
    if (ractive.get('current.instances') == undefined || ractive.get('current.instances').length>=ractive.get('current.instanceCount')) return;
    // collapse any visible instances so notice any new instances and to force refresh on re-open
    $('section.instanceSect[data-instance-id]').hide();
    var nextPage = Math.ceil(ractive.get('current.instances').length / 20);
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+ractive.get('current.id')+'/instances?limit=20&page='+nextPage, function( data ) {
      console.log('found instances '+data.length);
      ractive.push('current.instances',data);
    });
  },
  fetchIssues: function(definition) {
    console.log('fetch issues');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id+'/issues', function( data ) {
      console.log('found issues '+data.length);
      ractive.set('current.issues',data);
    });
  },
  fetchRenderer: function() {
    $.get(ractive.getServer()+'/xslt/bpmn2svg.xslt', function(data) {
      ractive.transformer.importStylesheet(data);
    });
  },
  find: function(defnKey) {
    console.info('find');
    var found, foundIdx = -1;
    $.each(ractive.get('definitions'), function(i,d) {
      if (d.key==defnKey && (found==undefined || d.id > found.id)) {
        found = d;
        foundIdx = i;
      }
    });
    return found;
  },
  hidePropertySect: function() {
    console.info('hidePropertySect');
    $('#propertySect').slideUp();
  },
  isLatestVersion: function(defn) {
    var defns = ractive.get('definitions');
    var latestVsn = 0;
    for(idx in defns) {
      if (defns[idx].key == defn.key && defns[idx].version>latestVsn) latestVsn = defns[idx].version;
    }
    return defn.version==latestVsn ? true : false;
  },
  json2Html: function(obj) {
    var html = '<ul class="field-json">';
    $.each(Object.keys(obj), function(i,d) {
      if (typeof obj[d] == 'object' && obj[d]['valueType'] != undefined && (obj[d]['string'] == undefined)) { // empty javax.json.JsonObject
        console.info('Supressing empty value '+d);
      } else if (typeof obj[d] == 'object' && obj[d]['valueType'] != undefined) { // populated javax.json.JsonObject
        html += '<li><label style="text-align:right;padding-right:10px">'+d.toLabel()+':</label><span class="col-scroll">'+obj[d]['string']+'</span>';
      } else if (typeof obj[d] == 'object') { // child object
        html += '<table class="table table-striped"><tr><th>' + d.toLabel() +'</th><td>'+ ractive.json2Html(obj[d])+'</td></tr></table>';
      } else {
        html += '<label>'+d.toLabel()+':</label><span class="col-scroll">'+obj[d]+'</span>';
      }
    });
    return html;
  } ,
  renderBpmn: function() {
    $.each(ractive.get('current.diagramIds'), function(idx,d) {
      var diagId = d;
      console.log('  fetch diagram: '+diagId);
      if (typeof ractive.get('current.bpmn') == 'string') {
        ractive.set('current.bpmn',ractive.parser.parseFromString(ractive.get('current.bpmn'), "text/xml"));
      }
      ractive.transformer.clearParameters();
      ractive.transformer.setParameter('http://www.omg.org/spec/BPMN/20100524/MODEL', 'diagramId', diagId);
      var result = ractive.transformer.transformToDocument(ractive.get('current.bpmn'));
      if (result == undefined) {
        ractive.showError("Unable to render image of BPMN");
      } else {
        var svgString = ractive.serializer.serializeToString(result.firstElementChild);
        var diagName = ractive.get('current.bpmn').querySelector('#'+diagId).getAttribute('name');
        ractive.splice('current.diagrams',ractive.get('current.diagramIds').indexOf(diagId),0, { id: diagId, name: diagName, image: svgString });
      }

      $('.event, .dataStoreReference, .flow, .gateway, .lane, .participant, .task').on('click',ractive.showSelection);
      $('[data-called-element]').attr('class',$('[data-called-element]').attr('class')+' clickable');
      $('[data-called-element]').click(function(ev) {
        console.log('drill down to call activity');
        console.log('selected: '+JSON.stringify($(ev.target)));
        console.log('selected: '+ev.target.attributes['data-called-element'].value);
        ractive.select(ractive.find(ev.target.attributes['data-called-element'].value));
        $('[data-toggle="tooltip"]').tooltip();
      });
      $('[data-ref]').click(function(ev) { ractive.showSelectedId($(ev.target).data('ref')); });
    });
  },
  select: function(definition) {
    ractive.set('current', definition);
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id, function( data ) {
      console.log('found definition '+JSON.stringify(data));
      ractive.set('saveObserver',false);
      data.diagrams = [];
      ractive.set('current',data);
      ractive.toggleResults();
      ractive.set('saveObserver',true);
      if (ractive.get('current').deploymentId==null) {
        ractive.fetchIssues(ractive.get('current'));
      } else {
        ractive.fetchInstances();
      }
      ractive.fetchBpmn();
      if (ractive.hasRole('admin')) $('.admin').show();
    });

    $('#currentSect').slideDown();
  },
  showIssues: function() {
    console.info('showIssues');
    $.each(ractive.get('current.issues'), function(i,d) {
      $('#'+d.modelRef+'Issue').attr('visibility','visible');312

      $('#'+d.modelRef+'IssueBG').attr('visibility','visible');
    });
  },
  showPropertySect: function() {
    console.info('showPropertySect');
    $('#propertySect').slideDown();
  },
  showResults: function() {
    console.log('showResults');
    $('#currentSect').slideUp();
    $('#definitionsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#definitionsTable').slideDown();
  },
  showSelection: function(ev) {
    ractive.showSelectedId(ev.target.id);
  },
  showSelectedId: function(id) {
    console.log('showSelectedId:'+id);
    // jQuery does not have SVG support for add/removeClass
    if ($('.selected').length>0)
        $('.selected').attr('class', $('.selected').attr('class').replace(/selected/,''))
    $('#'+id).attr('class',$('#'+id).attr('class')+' selected');

    if (typeof ractive.get('current.bpmn') == 'string') {
      ractive.set('current.bpmn',ractive.parser.parseFromString(ractive.get('current.bpmn'), "text/xml"));
    }

    ractive.set('selected',id);
    ractive.set('selectedBpmnObject', {
      id: id,
      element: ractive.get('current.bpmn').getElementById(id),
      name: $('#'+id).data('name'),
      type: $('#'+id).data('type')==undefined ? '' : $('#'+id).data('type').toLabel(),

      condition: $('#'+id).data('condition'),
      resource: $('#'+id).data('resource'),
      script: $('#'+id).data('script'),
      serviceType: $('#'+id).data('service-type'),
      timerCycle: $('#'+id).data('timer-cycle'),
      timerDate: $('#'+id).data('timer-date'),
      timerDuration: $('#'+id).data('timer-duration')
    });

    switch (ractive.get('selectedBpmnObject.serviceType')) {
    case 'com.knowprocess.resource.spi.RestDelete':
      ractive.set('selectedBpmnObject.serviceType', 'REST DELETE');
      break;
    case 'com.knowprocess.resource.spi.RestGet':
      ractive.set('selectedBpmnObject.serviceType', 'REST GET');
      break;
    case 'com.knowprocess.resource.spi.RestPost':
      ractive.set('selectedBpmnObject.serviceType', 'REST POST');
//        ractive.set('selectedBpmnObject.serviceTypeDetails', 'TODO');
      break;
    case 'com.knowprocess.resource.spi.RestPut':
      ractive.set('selectedBpmnObject.serviceType', 'REST PUT');
      break;
    case 'com.knowprocess.el.TemplateTask':
      ractive.set('selectedBpmnObject.serviceType', 'Merge template');
      break;
    case 'com.knowprocess.xslt.TransformTask':
      ractive.set('selectedBpmnObject.serviceType', 'XSLT');
      break;
    }
    // for service tasks and user tasks
    var fields = ractive.get('selectedBpmnObject.element').querySelectorAll('field, formProperty');
    var extDetails = '';
    for (idx in fields) {
      if (fields[idx].attributes!=undefined) {
        extDetails += '&nbsp;&nbsp;<em>'+fields[idx].attributes.getNamedItem('name').value.toLabel()+'</em>';
        // service tasks only
        if (fields[idx].attributes.getNamedItem('expression') != undefined) {
          extDetails += '<span>: '+fields[idx].attributes.getNamedItem('expression').value+'</span>';
        } else if (fields[idx].textContent != undefined && fields[idx].textContent.trim().length > 0){
          extDetails += '<span>: '+fields[idx].textContent.trim()+'</span>';
        }
        extDetails += '<br/>';
      }
    }
    // for call activities
    var intoActivity = ractive.get('selectedBpmnObject.element').querySelectorAll('in');
    if (intoActivity.length > 0) extDetails += '<br/><label>To activity</label><br/>';
    for (idx in intoActivity) {
      if (intoActivity[idx].attributes!=undefined) {
        extDetails += '&nbsp;&nbsp;<em>'+intoActivity[idx].attributes.getNamedItem('target').value.toLabel()+'</em>';
        if (intoActivity[idx].attributes.getNamedItem('source') != undefined && intoActivity[idx].attributes.getNamedItem('target').value != intoActivity[idx].attributes.getNamedItem('source').value) {
          extDetails += '<em>: '+intoActivity[idx].attributes.getNamedItem('source').value.toLabel()+'</em>';
        } else if (intoActivity[idx].attributes.getNamedItem('sourceExpression') != undefined) {
          extDetails += '<span>: '+intoActivity[idx].attributes.getNamedItem('sourceExpression').value.toLabel()+'</span>';
        }
        extDetails += '<br/>';
      }
    }
    var outOfActivity = ractive.get('selectedBpmnObject.element').querySelectorAll('out');
    if (outOfActivity.length > 0) extDetails += '<br/><label>From activity</label><br/>';
    for (idx in outOfActivity) {
      if (outOfActivity[idx].attributes!=undefined) {
        extDetails += '&nbsp;&nbsp;<em>'+outOfActivity[idx].attributes.getNamedItem('target').value.toLabel()+'</em>';
        if (outOfActivity[idx].attributes.getNamedItem('source') != undefined && outOfActivity[idx].attributes.getNamedItem('target').value != outOfActivity[idx].attributes.getNamedItem('source').value) {
          extDetails += '<em>: '+outOfActivity[idx].attributes.getNamedItem('source').value.toLabel()+'</em>';
        } else if (outOfActivity[idx].attributes.getNamedItem('sourceExpression') != undefined) {
          extDetails += '<span>: '+outOfActivity[idx].attributes.getNamedItem('sourceExpression').value.toLabel()+'</span>';
        }
        extDetails += '<br/>';
      }
    }
    ractive.set('selectedBpmnObject.extensionDetails', extDetails);
  },
  startInstance: function(id, label, bizKey) {
    console.log('startInstance: '+id+' for '+bizKey);
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        processDefinitionId: id,
        businessKey: bizKey
      }),
      success: completeHandler = function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetchInstances();
        ractive.showMessage('Started workflow "'+label+'" for '+bizKey);
      },
    });
  },
  suspend: function(id) {
    console.log('suspend: '+id);
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+id+'/suspend',
      type: 'POST',
      contentType: 'application/json',
      success: completeHandler = function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Suspended workflow "'+id+'"');
        ractive.showResults();
      },
    });
  },
  toggleAuditTrail: function(instance, idx) {
    console.log('toggleAuditTrail for: '+instance.id);
    if ($('section.instanceSect[data-instance-id="'+instance.id+'"]').is(':visible')) {
      $('section.instanceSect[data-instance-id="'+instance.id+'"]').hide();
    } else {
      $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/'+instance.id, function( data ) {
        console.log('found audit trail: '+data.auditTrail.length);
        data.auditTrail.sort(function(a,b) { return new Date(b.startTime)-new Date(a.startTime); });
        ractive.set('current.instances.'+idx+'.auditTrail',data.auditTrail);
        instance.auditTrail = data.auditTrail;

        console.log('found processVariables: '+data.processVariables.length);
        instance.processVariableNames = Object.keys(data.processVariables);
        instance.processVariables = data.processVariables;
        ractive.set('current.instances.'+idx,instance);
        $('.instanceSect[data-instance-id='+instance.id+']').slideDown();
      });
    }
    $('[data-instance-id="'+instance.id+'"] .glyphicon-eye-open,[data-instance-id="'+instance.id+'"] .glyphicon-eye-close')
        .toggleClass('glyphicon-eye-open glyphicon-eye-close');
  },
  toggleDetails: function() {
    console.info('toggleDetails');
    ractive.set('showDetails', !ractive.get('showDetails'));
    $('.details-control').toggleClass('glyphicon-eye-open').toggleClass('glyphicon-eye-close');
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#definitionsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#definitionsTable').slideToggle();
  },
  upload: function(formId) {
    console.log('upload, id: '+formId);
    var formElement = document.getElementById(formId);
    var formData = new FormData(formElement);
    return $.ajax({
        type: 'POST',
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
//          console.log('successfully uploaded definition');
          ractive.fetch();
          ractive.collapseAdd();
          ractive.showMessage('Deployed successfully');
        }
    });
  }
});

ractive.observe('current.bpmn', function(newValue, oldValue, keypath) {
  console.log('bpmn changed');
  if ((oldValue == undefined || oldValue.id == '') && newValue != undefined && newValue.id != '') {
    // old server side render works fine but is slower in desktop test
    // ractive.fetchDiagrams(ractive.get('current'));
    // client side render is faster but only shows current partial when all done, hence delay...
    setTimeout(function() { ractive.renderBpmn(ractive.get('current')) }, 500);
  }
});

ractive.on( 'sortInstances', function ( event, column ) {
  console.info('sortInstances on '+column);
  // if already sorted by this column reverse order
  if (this.get('sortInstanceColumn')==column) this.set('sortInstanceAsc', !this.get('sortInstanceAsc'));
  this.set( 'sortInstanceColumn', column );
});

$(document).ready(function() {
  ractive.fetchRenderer();
  ractive.initInfiniteScroll();
});
