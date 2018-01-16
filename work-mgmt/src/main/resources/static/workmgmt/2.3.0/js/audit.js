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
  partials: {
    'profileArea':'',
    'titleArea':'',
    'loginSect':'',
    'instanceListSect':'',
    'defnPropSect':'',
    'sidebar':'',
    'helpModal':''
  },
  data: {
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    definitions: [],
    diagPanZoomHandlers: [],
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
        var obj = JSON.parse(json);
        var html = '<ul class="json">';
        $.each(Object.keys(obj), function(i,d) {
          if (typeof obj[d] == 'object' && obj[d]['string'] != undefined) {
            html += '<li><label>'+d.toLabel()+':</label><span>'+obj[d]['string']+'</span>';
          } else if (typeof obj[d] == 'object') {
            // currently ignore keys without value, may change that?
          } else {
            html += '<label>'+d+':</label><span>'+obj[d]+'</span>';
          }
        });
        return html;
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
    stdPartials: [
      { "name": "defnCurrentSect", "url": "/partials/defn-current-sect.html"},
      { "name": "defnPropSect", "url": "/partials/defn-property-sect.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "instanceListSect", "url": "/partials/instance-list-sect.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "navbar", "url": "/partials/defn-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    title: 'Audit Trail'
  },
  simpleTodoFormExtension: function(x) {
    console.log('simpleTodoFormExtension: '+JSON.stringify(x));
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
          ractive.splice('diagPanZoomHandlers', idx, 0, svgPanZoom('#'+diagId));

          var diagEl = document.querySelector('.bpmnDiagram'); // element to make resizable
          diagEl.addEventListener('mouseup', function(e) {
            $('svg').attr('width', ($(e.target).css('width')-50));
            $('svg').attr('height', ($(e.target).css('height')-50));
            ractive.get('diagPanZoomHandlers.0').resize(); // update SVG cached size and controls positions
            ractive.get('diagPanZoomHandlers.0').fit();
            ractive.get('diagPanZoomHandlers.0').center();
          }, false);
        } catch (e) {
          console.error('  e:'+e);
        }
      }, 'text');
    });
  },
  isLatestVersion: function(defn) {
    var defns = ractive.get('definitions');
    var latestVsn = 0;
    for(idx in defns) {
      if (defns[idx].key == defn.key && defns[idx].version>latestVsn) latestVsn = defns[idx].version;
    }
    return defn.version==latestVsn ? true : false;
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
  fetch: function() {
    var instanceId = parseInt(getSearchParameters()['instanceId']);
    console.log('fetch instance: '+instanceId);
    if (instanceId==undefined) {
      ractive.showError("You must specify the instanceId as a request parameter");
    } else {
      var idx = 0;
      $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/'+instanceId, function( data ) {
        console.log('found audit trail: '+data.auditTrail.length);
        data.auditTrail.sort(function(a,b) { return new Date(b.startTime)-new Date(a.startTime); });
        ractive.set('current.instances.'+idx, data);

        console.log('found processVariables: '+data.processVariables.length);
        data.processVariableNames = Object.keys(data.processVariables);

        // adapt section from the definition page defaults
        if (!$('#instancesTable').is(':visible')) {
          $('#currentInstanceListSect .ol-collapse').click();
          $('.instanceSect[data-instance-id='+instanceId+']').slideDown();
        }
        // TODO load definition and thence diagram?
        // ractive.fetchDefinition(data.processDefinitionId);
      });
    }
  }
});
