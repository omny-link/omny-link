/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
var EASING_DURATION = 500;

var fadeOutMessages = true;
var ractive = new BaseRactive({
  el: 'container',
  template: '#template',
  modeler: new BpmnModeler(),
  parser: new DOMParser(),
  serializer: new XMLSerializer(),
  partials: { simpleTodoFormExtension: function(x) {
    return 'HELLO'+x
  } },
  data: {
    bpmnUndoStack: [],
    bpmnRedoStack: [],
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
      // console.log('formatDate: '+timeString);
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
    isVisual: function(bpmnObject) {
      switch (bpmnObject.localName) {
      case 'dataObject':
      case 'dataStore':
      case 'error':
      case 'escalation':
      case 'message':
      case 'resource':
      case 'signal':
      case 'message':
        return false;
      default:
        return true;
      }
    },
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
      // console.info('matchSearch: '+searchTerm);
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
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc';
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
      { "name": "procCurrentSect", "url": "/partials/proc-current-sect.html"},
      { "name": "procListSect", "url": "/partials/proc-list-sect.html"},
      { "name": "designerSect", "url": "/partials/proc-designer-sect.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "instanceListSect", "url": "/partials/instance-list-sect.html"},
      { "name": "navbar", "url": "/partials/proc-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    tenantUri: function(obj) {
      return ractive.tenantUri(obj);
    },
    title: 'Process Definitions'
  },
  partials: {
    procCurrentSect: '',
    procListSect: '',
    procPropSect: '',
    designerSect: '',
    helpModal: '',
    instanceListSect: '',
    navbar: '',
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
      success: function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Activated workflow "'+key+'"');
        ractive.showResults();
      },
    });
  },
  add: function () {
    console.log('add...');
    $('#upload').slideToggle();
  },
  addBpmnClickHandlers: function() {
    $('.event, .dataInput, .dataOutput, .dataInputAssociation, .dataOutputAssociation, .dataObjectReference, .dataStoreReference, .flow, .gateway, .lane, .messageFlow, .participant, .sequenceFlow, .task').on('click',ractive.showSelection);
    $('[data-called-element]').attr('class',$('[data-called-element]').attr('class')+' clickable draggable');
    $('[data-called-element]').click(function(ev) {
      console.log('drill down to call activity');
      console.log('selected: '+JSON.stringify($(ev.target)));
      console.log('selected: '+ev.target.attributes['data-called-element'].value);
      ractive.select(ractive.find(ev.target.attributes['data-called-element'].value));
      $('[data-toggle="tooltip"]').tooltip();
    });
    $('[data-ref]').click(function(ev) { ractive.showSelectedId($(ev.target).data('ref')); });
  },
  addBpmnDataLists: function(bpmn) {
    // data objects
    $('datalist#dataObjects').remove();
    $('body').append('<datalist id="dataObjects">');
    var dataObjects = bpmn.querySelectorAll('dataObject');
    if (dataObjects == null) {
      console.error('No data for datalist: dataObjects, please create one');
    } else {
      $.each(dataObjects, function (i,e) {
        var name = e.getAttribute('name') == null ? e.id : e.getAttribute('name');
        $('datalist#dataObjects').append('<option value="'+e.id+'">'+name+'</option>');
      });
    }
    // resources
    $('datalist#resources').remove();
    $('body').append('<datalist id="resources">');
    var resources = bpmn.querySelectorAll('resource');
    if (resources == null) {
      console.error('No data for datalist: resources, please create one');
    } else {
      $.each(resources, function (i,e) {
        var name = e.getAttribute('name') == null ? e.id : e.getAttribute('name');
        $('datalist#resources').append('<option value="'+e.id+'">'+name+'</option>');
      });
    }
  },
  addBpmnDataAssociation: function(type) {
    console.info('addBpmnDataAssociation: '+type);
    var ids = ractive.get('selectedIds').split(',');
    var bpmn = ractive.get('current.bpmn');

    bpmn = ractive.modeler.addDataAssociation(bpmn, ids);

    ractive.nextBpmn(bpmn);
  },
  addBpmnFlowLink: function(type) {
    console.info('addBpmnFlowLink: '+type);
    var ids = ractive.get('selectedIds').split(',');
    var bpmn = ractive.get('current.bpmn');

    bpmn = ractive.modeler.addFlowLink(bpmn, ids, type);

    ractive.nextBpmn(bpmn);
  },
  addBpmnWaypoint: function(selected, idx) {
    console.log('addBpmnWaypoint to '+selected+' after '+idx);
    var bpmn = ractive.modeler.addWaypoint(ractive.get('current.bpmn'), selected, idx);
    ractive.nextBpmn(bpmn);
    ractive.showSelectedId(selected);
  },
  addDeploymentResourceFile: function() {
    console.log('addDeploymentResourceFile...');
    $("#resourceFile").click();
  },
  addDeploymentResourceUrl: function() {
    console.log('addDeploymentResourceUrl...');
    $("#resourceUrl").show();
  },
  addStarterDeploymentResource: function(name) {
    console.log('addStarterDeploymentResource...');
    ractive.uploadFromUrl(ractive.getServer()+'/public/processes/'+name+'.bpmn', true);
  },
  addProcess: function () {
    console.info('addProcess...');
    $('#designer').slideToggle();
  },
  addProcessObject: function(type, options) {
    console.info('addProcessObject...');
    var bpmn = ractive.getDocument('current.bpmn');
    var upstreamId = ractive.get('selected');

    var result = ractive.modeler.addProcessObject(bpmn, type, upstreamId, options);

    // save???

    if (Array.isArray(ractive.get('current.'+type+'s'))) ractive.push('current.'+type+'s', result.obj);
    ractive.nextBpmn(result.bpmn);
    $('#'+result.obj.id).click();
  },
  alignBpmn: function(strategy) {
    console.info('alignBpmn');
    var ids = ractive.get('selectedIds').split(',');
    var bpmn = ractive.get('current.bpmn');

    bpmn = ractive.modeler.alignBpmn(bpmn, ids, strategy);

    ractive.set('selectedIds', undefined);
    ractive.nextBpmn(bpmn);
  },
  collapseAdd: function () {
    console.log('collapseAdd...');
    $("#resourceFile").val('');
    $("#resourceUrl").hide().val('');
    $('#upload').slideUp();
  },
  collapseDesigner: function () {
    console.log('collapseDesigner...');
    $('#designer').slideUp();
  },
  define: function(defn) {
    console.info('define');
    ractive.set('saveObserver',false);
    if (document.getElementById('procForm')==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('procForm').checkValidity()) {
      $.ajax({
          url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/',
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(defn),
          success: function(data) {
            console.log('  Success, received: '+data);
            ractive.set('proc', data);
            ractive.set('saveObserver',true);
          }
      });
    } else {
      console.warn('Unfortunately the process text is invalid');
      $('#defnForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as process text is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  delete: function (deploymentId) {
    console.log('delete '+deploymentId+'...');
    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments/'+deploymentId,
        type: 'DELETE',
        success: function(data) {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteBpmn: function() {
    console.log('deleteBpmn...');
    var ids = ractive.get('selectedIds').split(',');
    var bpmn = ractive.get('current.bpmn');

    bpmn = ractive.modeler.deleteObjects(bpmn, ids);

    ractive.nextBpmn(bpmn);
  },
  deleteBpmnWaypoint: function(idx) {
    console.log('deleteBpmnWaypoint...');
    var bpmn = ractive.get('current.bpmn');

    bpmn = ractive.modeler.deleteWaypoint(bpmn, ractive.get('selected'), idx);

    ractive.nextBpmn(bpmn);
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
        success: function(data) {
          console.log('  Success, received: '+data);
          ractive.get('current.instances').splice(idx,1);
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deploy: function(defn) {
    console.info('deploy');
    ractive.set('saveObserver',false);

    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/deployments/',
        type: 'POST',
        data: defn,
        success: function(data, textStatus, jqXHR) {
          console.log('  Success, received: '+data);
          ractive.set('defn', data);
          ractive.set('saveObserver',true);
          ractive.collapseDesigner();
          ractive.showMessage('Deployed successfully');
          ractive.fetch();
        }
    });
  },
  deployForm: function(formId) {
    console.info('deployForm');
    ractive.set('saveObserver',false);
    if (document.getElementById(formId)==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById(formId).checkValidity()) {
      ractive.deploy($('#'+formId).serializeArray());
    } else {
      console.warn('Unfortunately the process text is invalid');
      $('#defnForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  deployModeler: function() {
    console.info('deployModeler');
    ractive.set('saveObserver',false);

    var defn = [
      { "name":"name","value": ractive.get('current.name') },
      { "name":"bpmn","value": ractive.serializer.serializeToString(ractive.get('current.bpmn')) },
      { "name":"tenantId","value": ractive.get('tenant.id') }
    ]

    ractive.deploy(defn);
    ractive.showResults();
    ractive.fetch();
  },
  download: function() {
    console.info('download');
    if (ractive.get('current.bpmn') == undefined) {
      // TODO obsolete?
      $.ajax({
        headers: {
          "Accept": "application/xml"
        },
        url: ractive.tenantUri(ractive.get('current'))+'.bpmn',
        crossDomain: true,
        success: function( data ) {
          console.warn('fetched bpmn: '+data);
          var bpmn = ractive.serializer.serializeToString(data);
          ractive.downloadUri("data:application/xml," + encodeURIComponent(bpmn),ractive.get('current.id')+".bpmn");
        }
      });
    } else {
      var data = ractive.get('current.bpmn');
      console.warn('local bpmn: '+data);
      var serializer = new XMLSerializer();
      var bpmn = serializer.serializeToString(data);
      ractive.downloadUri("data:application/xml," + encodeURIComponent(bpmn),
          ractive.get('current.name') == undefined ? ractive.get('current.id')+'.bpmn' : ractive.get('current.name')+'.bpmn');
    }
  },
  dragEndOrClick: function(ev) {
    console.log('dragEndOrClick');
    var coord = ractive.getMousePosition(ev);
    var curPos = ractive.get('curPos');
    if (ractive.get('draggedBpmnObject') && Math.abs(curPos.x - coord.x) > 10
        && Math.abs(curPos.y - coord.y) > 10) {
      dragEnd(ev);
    } else {
      // click(ev);
      // editText(ev);
    }
  },
  dragEnd: function(ev) {
    console.log('dragEnd');
    ractive.renderBpmn(ractive.get('current.bpmn'));
// ractive.get('selectedBpmnObject') = null;
  },
  drag: function(ev) {
    if (ractive.get('draggedBpmnObject')) {
      console.log('drag: '+ractive.get('draggedBpmnObject').id);
      ev.preventDefault();
      var coord = ractive.getMousePosition(ev);
      var dragOffset = ractive.get('dragOffset');
      // update view
      if (ev.target.nextElementSibling != undefined
          && ev.target.nextElementSibling.tagName == 'text') {
        var dx = ractive.get('draggedBpmnObject').getAttributeNS(null, "x")
            - ev.target.nextElementSibling.getAttributeNS(null, "x");
        var dy = ractive.get('draggedBpmnObject').getAttributeNS(null, "y")
            - ev.target.nextElementSibling.getAttributeNS(null, "y");
        ev.target.nextElementSibling.setAttributeNS(null, "x", coord.x -dx - dragOffset.x);
        ev.target.nextElementSibling.setAttributeNS(null, "y", coord.y -dy - dragOffset.y);
      }
      ractive.get('draggedBpmnObject').setAttributeNS(null, "x", coord.x - dragOffset.x);
      ractive.get('draggedBpmnObject').setAttributeNS(null, "y", coord.y - dragOffset.y);
      // update model
      var modelEl = me.modelDom.querySelector('[ID="'+ev.target.getAttribute('id')+'"] Bounds');
      if (modelEl != undefined) {
        console.log('  model '+ev.target.getAttribute('id')+' updating '
            +modelEl.getAttribute('x')+','+modelEl.getAttribute('y')
            +' to '+coord.x+','+coord.y);
        modelEl.setAttribute('x', coord.x - me.dragOffset.x);
        modelEl.setAttribute('y', coord.y - me.dragOffset.y);
        console.log('  model '+ev.target.getAttribute('id')+' updated to: '
            +modelEl.getAttribute('x')+','+modelEl.getAttribute('y')
            +' '+coord.x+','+coord.y);
      }
    }
  },
  dragStart: function(ev) {
    console.log('dragStart');
    ractive.set('curPos', ractive.getMousePosition(ev));
    if (ev.target.classList.contains('draggable')) {
      ractive.set('draggedBpmnObject', ev.target);


      var dragOffset = ractive.get('curPos');
      dragOffset.x -= parseFloat(ractive.get('draggedBpmnObject').getAttributeNS(null, "x"));
      dragOffset.y -= parseFloat(ractive.get('draggedBpmnObject').getAttributeNS(null, "y"));
      ractive.set('dragOffset', dragOffset);
    }
  },
  enumerateNonVisualObjects: function(bpmn, type) {
    var values = Array.from(bpmn.querySelectorAll(type), function(obj) {
      return { id: obj.id, name: obj.getAttribute('name') };
    });
    ractive.merge('current.'+type+'s', values);
  },
  fetch: function() {
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
    $.getJSON(ractive.tenantUri(ractive.get('current'))+'.bpmn', function( data ) {
      console.log('found bpmn '+data);
      var bpmnDoc = ractive.parser.parseFromString(data, "text/xml");
      ractive.nextBpmn(bpmnDoc);
    });
  },
  fetchInstances: function() {
    console.log('fetch instances');
    // collapse any visible instances so notice any new instances and to force
    // refresh on re-open
    $('section.instanceSect[data-instance-id]').hide();
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+ractive.get('current.id')+'/instances?limit=20', function( data ) {
      console.log('found instances '+data.length);
      ractive.set('current.instances',data);
    });
  },
  fetchMore: function() {
    console.log('fetch more instances');
    if (ractive.get('current.instances') == undefined || ractive.get('current.instances').length>=ractive.get('current.instanceCount')) return;
    // collapse any visible instances so notice any new instances and to force
    // refresh on re-open
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
      ractive.modeler.transformer.importStylesheet(data);
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
  getDocument: function(key) {
    if (typeof ractive.get(key) == 'string') {
      ractive.set(key,ractive.parser.parseFromString(ractive.get(key), "text/xml"));
    }
    return ractive.get(key);
  },
  // translate screen co-ords to SVG co-ords
  getMousePosition: function(ev) {
    if (ev.touches) { ev = ev.touches[0]; }
    var CTM = document.querySelector('#'+ev.target.id).ownerSVGElement.getScreenCTM();
// var CTM = me.svgContainer.firstElementChild.getScreenCTM();
    return {
      x: (ev.clientX - CTM.e) / CTM.a,
      y: (ev.clientY - CTM.f) / CTM.d
    };
  },
  hideBpmnProperties: function() {
    $('.bpmnProperties').hide();
  },
  hideIssues: function() {
    console.info('hideIssues');
    var issues = ractive.get('current.issues');
    if (issues == undefined) return;
    $.each(issues, function(i,d) {
      $('#'+d.modelRef+'Issue,#'+d.modelRef+'IssueBG').attr('visibility','hidden');
    });
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
  makeDraggable: function(svg) {
    svg.addEventListener('mousedown', ractive.dragStart);
    svg.addEventListener('mousemove', ractive.drag);
    svg.addEventListener('mouseup',  ractive.dragEndOrClick);
    svg.addEventListener('mouseleave',  ractive.dragEnd);

    // At least under chrome causes drop to 'jump' (last drag wildly offset)
    // svg.addEventListener('dragstart', startDrag);
    // svg.addEventListener('drag', drag);
    // svg.addEventListener('dragend', dragEnd);
    // svg.addEventListener('drop', dragEnd);

    svg.addEventListener('touchstart', ractive.dragStart);
    svg.addEventListener('touchmove', ractive.drag);
    svg.addEventListener('touchend',  ractive.dragEndOrClick);
    svg.addEventListener('touchleave',  ractive.dragEnd);
    svg.addEventListener('touchcancel',  ractive.dragEnd);
  },
  nextBpmn: function(bpmn) {
    console.info('nextBpmn');
    if (ractive.get('current.bpmn') != undefined) ractive.push('bpmnUndoStack', ractive.get('current.bpmn'));
    if (ractive.get('current.deploymentId') != undefined) {
      ractive.set('current.deployment', undefined);
      ractive.set('current.deploymentId', undefined);
      ractive.set('current.id', bpmn.querySelector('definitions').id);
      ractive.set('current.category', bpmn.querySelector('definitions').getAttribute('targetNamespace'));
    }
    ractive.set('current.bpmn', bpmn);
    ractive.set('bpmnRedoStack', []);
    ractive.renderBpmn(bpmn);
  },
  redoBpmn: function() {
    console.info('redoBpmn');
    if (ractive.get('bpmnRedoStack').length > 0) {
      ractive.push('bpmnUndoStack', ractive.get('current.bpmn'));
      var bpmn = ractive.pop('bpmnRedoStack');
      ractive.set('current.bpmn', bpmn);
      ractive.renderBpmn(bpmn);
    }
  },
  renderBpmn: function(bpmn) {
    var diags = bpmn.querySelectorAll('BPMNDiagram');
    ractive.set('current.diagramIds', []);
    $.each(diags, function(idx, d) {
      var diagId = d.id;
      console.log('  render diagram: '+diagId);
      ractive.push('current.diagramIds', diagId);

      var svgString = ractive.modeler.render(bpmn, diagId);

      var diagDoc = $.parseXML(svgString);
      var diagName = diagDoc.getElementsByTagName('svg')[0].attributes['name'].value;
      var pos = ractive.get('current.diagramIds').indexOf(diagId);
      ractive.set('current.diagrams.'+pos, { id: diagId, name: diagName, image: svgString });

      // ractive.makeDraggable(ractive.getDocument(svgString).firstElementChild);
      ractive.addBpmnDataLists(bpmn);
      ractive.addBpmnClickHandlers();
      ractive.enumerateNonVisualObjects(bpmn, 'dataObject');
      ractive.enumerateNonVisualObjects(bpmn, 'dataStore');
      ractive.enumerateNonVisualObjects(bpmn, 'error');
      ractive.enumerateNonVisualObjects(bpmn, 'escalation');
      ractive.enumerateNonVisualObjects(bpmn, 'message');
      ractive.enumerateNonVisualObjects(bpmn, 'resource');
      ractive.enumerateNonVisualObjects(bpmn, 'signal');
    });
  },
  reverseBpmnFlow: function() {
    console.info('reverseBpmnFlow');
    var bpmn = ractive.getDocument('current.bpmn');
    var flowDI = bpmn.querySelector('BPMNEdge[bpmnElement="'+ractive.get('selected')+'"]');
  },
  select: function(definition) {
    ractive.set('current', definition);
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id, function( data ) {
      console.log('found definition '+JSON.stringify(data));
      ractive.set('saveObserver',false);
      data.diagrams = [];
      data.dataObjects = [];
      data.dataStores = [];
      data.errors = [];
      data.escalations = [];
      data.messages = [];
      data.resources = [];
      data.signals = [];
      ractive.set('current',data);
      ractive.toggleResults();
      ractive.set('saveObserver',true);
      if (ractive.get('current').deploymentId==null) {
        ractive.fetchIssues(ractive.get('current'));
      } else {
        ractive.fetchInstances();
      }
      ractive.renderBpmn(ractive.getDocument('current.bpmn'));
      if (ractive.hasRole('admin')) $('.admin').show();
    });

    $('#currentSect').slideDown();
  },
  showIssues: function() {
    console.info('showIssues');
    var issues = ractive.get('current.issues');
    if (issues == undefined) return;
    $.each(issues, function(i,d) {
      $('#'+d.modelRef+'Issue,#'+d.modelRef+'IssueBG').attr('visibility','visible');
    });
  },
  showPropertySect: function() {
    console.info('showPropertySect');
    $('#propertySect').slideDown();
  },
  showResults: function() {
    console.log('showResults');
    $('#currentSect').slideUp();
    $('#definitionsTableToggle').addClass('kp-icon-caret-down').removeClass('kp-icon-caret-right');
    $('#definitionsTable').slideDown();
  },
  showSelection: function(ev) {
    if (ev.ctrlKey || ev.shiftKey) {
      console.log('detected multi select');
      if (ractive.get('selectedIds')==undefined) ractive.set('selectedIds',ev.target.id);
      else if (ractive.get('selectedIds').indexOf(ev.target.id)!=-1) console.debug(ev.target.id+' already selected');
      else ractive.set('selectedIds',ractive.get('selectedIds')+','+ev.target.id);
      $('#'+ev.target.id).attr('class',$('#'+ev.target.id).attr('class')+' selected');
    } else {
      ractive.showSelectedId(ev.target.id);
    }
  },
  showSelectedId: function(id) {
    console.log('showSelectedId:'+id);
    // jQuery does not have SVG support for add/removeClass
    if ($('.selected').length>0)
        $('.selected').attr('class', $('.selected').attr('class').replace(/selected/,''))
    $('#'+id).attr('class',$('#'+id).attr('class')+' selected');

    if (typeof ractive.get('current.bpmn') == 'string') {
      ractive.nextBpmn(ractive.parser.parseFromString(ractive.get('current.bpmn'), "text/xml"));
    }

    ractive.set('selected',id);
    ractive.set('selectedIds',id);
    ractive.set('selectedBpmnObject', {
      id: id,
      element: ractive.get('current.bpmn').getElementById(id),
      diElement: (ractive.get('current.bpmn') === undefined ? undefined : ractive.get('current.bpmn').querySelector('[bpmnElement='+id+']')),
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

    $('.bpmnTypes select, .bpmnSubTypes select').hide();
    switch (ractive.get('selectedBpmnObject.element.localName')) {
    case 'boundaryEvent':
      $('#boundaryEventSubTypes').show();
      break;
    case 'endEvent':
      $('#endEventSubTypes').show();
      break;
    case 'exclusiveGateway': case 'inclusiveGateway': case 'multipleGateway':
    case 'parallelGateway': case 'complexGateway': case 'eventBasedGateway':
      $('#gatewaySubTypes').show();
      break;
    case 'intermediateEvent':
      $('#intermediateEventSubTypes').show();
      break;
    case 'startEvent':
      $('#startEventSubTypes').show();
      break;
    case 'task': case 'businessRuleTask': case 'manualTask': case 'receiveTask':
    case 'scriptTask': case 'sendTask': case 'serviceTask': case 'userTask':
      $('#taskTypes').show();
//        document.getElementById('taskSubTypes').);
      break;
    }

    switch (ractive.get('selectedBpmnObject.element.localName')) {
    case 'exclusiveGateway':
    case 'task': case 'businessRuleTask': case 'manualTask': case 'receiveTask':
    case 'scriptTask': case 'sendTask': case 'serviceTask': case 'userTask':
      $('#defaultSeqFlow').empty();
      var flows = ractive.get('current.bpmn').querySelectorAll('[sourceRef="'+id+'"]');
      for (var idx = 0 ; idx < flows.length ; idx++) {
        var selectedOption = ractive.get('current.bpmn').getElementById(id).getAttribute('default');
        $('#defaultSeqFlow').append('<option value="'+flows[idx].id+'"'
            + (flows[idx].id == selectedOption ? 'selected' : '') +'>'
            +(flows[idx].getAttribute('name') == undefined ? flows[idx].id : flows[idx].getAttribute('name'))
            +'</option>');
      }
      break;
    }

    switch (ractive.get('selectedBpmnObject.serviceType')) {
    case 'com.knowprocess.resource.spi.RestDelete':
      ractive.set('selectedBpmnObject.serviceType', 'REST DELETE');
      break;
    case 'com.knowprocess.resource.spi.RestGet':
      ractive.set('selectedBpmnObject.serviceType', 'REST GET');
      break;
    case 'com.knowprocess.resource.spi.RestPost':
      ractive.set('selectedBpmnObject.serviceType', 'REST POST');
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
    try {
      var fields = ractive.get('selectedBpmnObject.element').querySelectorAll('field, formProperty');
      var extDetails = '';
      for (idx in fields) {
        if (fields[idx].attributes!=undefined) {
          extDetails += '&nbsp;&nbsp;<em>'+fields[idx].attributes.getNamedItem('name').value.toLabel()+'</em>';
          // service tasks only
          if (fields[idx].attributes.getNamedItem('expression') != undefined) {
            extDetails += '<span>: '+fields[idx].attributes.getNamedItem('expression').value+'</span>';
          } else if (fields[idx].attributes.getNamedItem('stringValue') != undefined) {
            extDetails += '<span>: '+fields[idx].attributes.getNamedItem('stringValue').value+'</span>';
          } else if (fields[idx].textContent != undefined && fields[idx].textContent.trim().length > 0){
            extDetails += '<span>: '+fields[idx].textContent.trim()+'</span>';
          }
          extDetails += '<br/>';
        }
      }
    } catch (e) {
      console.warn('No formProperties found for task '+ractive.get('selectedBpmnObject.id'));
    }
    try {
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
    } catch (e) {
      console.warn('No input found for task '+ractive.get('selectedBpmnObject.id'));
    }
    try {
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
    } catch (e) {
      console.warn('No output found for task '+ractive.get('selectedBpmnObject.id'));
    }
    ractive.set('selectedBpmnObject.extensionDetails', extDetails);
    $('.bpmnProperties').show();
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
      success: function(data,textStatus,jqXHR) {
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
      success: function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Suspended workflow "'+id+'"');
        ractive.showResults();
      },
    });
  },
  tidyBpmn: function() {
    console.log('tidyBpmn...');
    bpmn = ractive.modeler.tidy(ractive.getDocument('current.bpmn'));
    ractive.nextBpmn(bpmn);
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
        ractive.initiatorIcon(/* always use bot icon */);
        $('.instanceSect[data-instance-id='+instance.id+']').slideDown();
      });
    }
    $('[data-instance-id="'+instance.id+'"] .kp-icon-eye,[data-instance-id="'+instance.id+'"] .kp-icon-eye-blocked')
        .toggleClass('kp-icon-eye kp-icon-eye-blocked');
  },
  toggleDebug: function() {
    console.info('toggleDebug');
    ractive.modeler.showBounds = true;
    ractive.renderBpmn(ractive.get('current.bpmn'));
  },
  toggleDetails: function() {
    console.info('toggleDetails');
    ractive.set('showDetails', !ractive.get('showDetails'));
    $('.details-control').toggleClass('kp-icon-eye').toggleClass('kp-icon-eye-blocked');
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#definitionsTableToggle').toggleClass('kp-icon-caret-down').toggleClass('kp-icon-caret-right');
    $('#definitionsTable').slideToggle();
  },
  transposeBpmn: function() {
    ractive.set('current.bpmn', ractive.modeler.reversePoolOrder(ractive.get('current.bpmn')));
    document.getElementById('Diagram-COLLABORATION_1').setAttribute('transform', 'translate(-250,-150) rotate(90)');
  },
  undoBpmn: function() {
    console.info('undoBpmn');
    if (ractive.get('bpmnUndoStack').length > 0) {
      ractive.push('bpmnRedoStack', ractive.get('current.bpmn'));
      var bpmn = ractive.pop('bpmnUndoStack');
      ractive.set('current.bpmn', bpmn);
    }
  },
  updateBpmnObject: function(id, key, value) {
    console.log('updateBpmnObject:'+id);
    var bpmn = ractive.getDocument('current.bpmn');

    bpmn = ractive.modeler.updateObject(bpmn, id, key, value);
    ractive.nextBpmn(bpmn);
  },
  setConditional: function(obj, isConditional) {
    console.log('setConditional on:'+obj.id);
    var bpmn = ractive.getDocument('current.bpmn');
    if (isConditional) {
      bpmn = ractive.modeler.updateCondition(bpmn, obj, isConditional);
    } else {
      bpmn = ractive.modeler.deleteCondition(bpmn, obj);
    }
    ractive.nextBpmn(bpmn);
    ractive.showSelectedId(obj.id);
  },
  updateCondition: function(obj, value) {
    var bpmn = ractive.getDocument('current.bpmn');
    bpmn = ractive.modeler.updateCondition(bpmn, obj, value);
    ractive.nextBpmn(bpmn);
    ractive.showSelectedId(obj.id);
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
// console.log('successfully uploaded definition');
          ractive.fetch();
          ractive.collapseAdd();
          ractive.showMessage('Deployed successfully');
        }
    });
  },
  uploadFromUrl: function(url, resetIds) {
    console.info('uploadFromUrl:'+url);
    if (url == undefined || url.trim().length<7) return;
    $.ajax({
      headers: {
        "Accept": "application/xml"
      },
      url: url,
      crossDomain: true,
      success: function( data ) {
        console.warn('fetched bpmn template');
        if (resetIds) ractive.modeler.resetIds(data);
        var defn = [
          { "name":"name","value": url },
          { "name":"bpmn","value": ractive.serializer.serializeToString(data) },
          { "name":"tenantId","value": ractive.get('tenant.id') }
        ]

        ractive.deploy(defn);
        ractive.fetch();
      },
      error: function(jqXHR, textStatus, errorThrown) {
        ractive.showError('Unable to download from URL: '+url);
      }
    });

    ractive.collapseAdd();
  }
});

ractive.observe('current.bpmn', function(newValue, oldValue, keypath) {
  console.log('bpmn changed');
  if (typeof newValue == 'string') {
    newValue = ractive.parser.parseFromString(newValue, "text/xml");
  }
  if (newValue != undefined) ractive.renderBpmn(newValue);
})

ractive.observe('showDetails', function(newValue, oldValue, keypath) {
  console.log('showDetails changed');
  if (newValue != undefined && newValue) {
    ractive.showIssues();
  } else {
    ractive.hideIssues();
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
