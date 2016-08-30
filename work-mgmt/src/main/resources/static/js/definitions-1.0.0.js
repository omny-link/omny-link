var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new AuthenticatedRactive({
  el: 'container',
  template: '#template',
  partials: { simpleTodoFormExtension: function(x) {
    return 'HELLO'+x
  } },
  data: {
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    definitions: [],
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
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
    helpUrl: 'http://omny.link/user-help/definitions/#the_title',
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
        );
      }
    },
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
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/defn-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    title: 'Process Definitions',
    username: localStorage['username'],
  },
  simpleTodoFormExtension: function(x) { 
    console.log('simpleTodoFormExtension: '+JSON.stringify(x));    
  },
  activate: function(key) {
    console.log('activate: '+key);
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+key+'/activate',
      type: 'GET',
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
  collapseAdd: function () {
    console.log('collapseAdd...');
    $('#upload').slideUp();
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
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions', function( data ) {
      ractive.merge('definitions', data);
    });
  },
  fetchImage: function(definition) {
    console.info('fetchImage');
    $.get(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id+'.svg', function( data ) {
      console.log('found image');
      ractive.set('current.image',data);
      $('.event').on('mouseover',ractive.showSelection);
      $('.gateway').on('mouseover',ractive.showSelection);
      $('.flow').on('mouseover',ractive.showSelection);
      $('.task').on('mouseover',ractive.showSelection);
      $('[data-called-element]').attr('class',$('[data-called-element]').attr('class')+' clickable');
      $('[data-called-element]').click(function(ev) {
        console.log('drill down to call activity');
        console.log('selected: '+JSON.stringify($(ev.target)));
        console.log('selected: '+ev.target.attributes['data-called-element'].value);
        ractive.select(ractive.find(ev.target.attributes['data-called-element'].value));
      });
    }, 'text');
  },
  fetchInstances: function() {
    console.log('fetch instances');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+ractive.get('current.id')+'/instances', function( data ) {
      console.log('found instances '+data.length);
      ractive.set('current.instances',data);
    });
  },
  fetchIssues: function(definition) {
    console.log('fetch issues');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id+'/issues', function( data ) {
      console.log('found issues '+data.length);
      ractive.set('current.issues',data);
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
  oninit: function() {
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  select: function(definition) {
    ractive.set('current', definition);
//    ractive.set('saveObserver',false);
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id, function( data ) {
      console.log('found definition '+JSON.stringify(data));
      ractive.set('current',data);
      ractive.toggleResults();
      ractive.fetchImage(ractive.get('current'));
      //      ractive.set('saveObserver',true);
      if (ractive.get('current').deploymentId==null) {
        ractive.fetchIssues(ractive.get('current'));
      } else {
        ractive.fetchInstances();
      }
      if (ractive.hasRole('admin')) $('.admin').show();
    });
    
    $('#currentSect').slideDown();
  },
  showIssues: function() {
    console.info('showIssues');
    $.each(ractive.get('current.issues'), function(i,d) {
      $('#'+d.modelRef+'Issue').attr('visibility','visible');
      $('#'+d.modelRef+'IssueBG').attr('visibility','visible');
    });
  },
  showPropertySect: function() {
    console.info('showPropertySect');
    $('#propertySect').slideDown();
//    if (ev.clientX > window.innerWidth/2) {
//      $('#propertySect').css('left',0);
//      $('#propertySect').css('right','auto');
//    } else {
//      $('#propertySect').css('left','auto');
//      $('#propertySect').css('right',0);
//    }
  },
  showResults: function() {
    console.log('showResults');
    $('#currentSect').slideUp();
    $('#definitionsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#definitionsTable').slideDown();
  },
  showSelection: function(ev) {
    console.log('showSelection:'+ev.target.id);
    // jQuery does not have SVG support for add/removeClass
    if ($('.selected').length>0)
        $('.selected').attr('class', $('.selected').attr('class').replace(/selected/,''))
    $('#'+ev.target.id).attr('class',$('#'+ev.target.id).attr('class')+' selected');
    ractive.set('selected',ev.target.id);
    ractive.set('selectedBpmnObject', { 
      id: ev.target.id, 
      name: $('#'+ev.target.id).data('name'), 
      type: $('#'+ev.target.id).data('type')==undefined ? '' : $('#'+ev.target.id).data('type').toLabel(), 

      calledElement: $('#'+ev.target.id).data('called-element'),
      condition: $('#'+ev.target.id).data('condition'),
      resource: $('#'+ev.target.id).data('resource'),
      script: $('#'+ev.target.id).data('script'),
      serviceType: $('#'+ev.target.id).data('service-type'),
      timerCycle: $('#'+ev.target.id).data('timer-cycle'),
      timerDate: $('#'+ev.target.id).data('timer-date'),
      timerDuration: $('#'+ev.target.id).data('timer-duration')
    });
  },
//  showUserTaskPropertySect: function(ev) {
//    console.info('showUserTaskPropertySect at x,y:'+ev.clientX+','+ev.clientY);
//    var side = ev.clientX > window.innerWidth/2 ? 'left' :'right';
//    console.log('  side: '+side);
////    'left': function() { return (ev.clientX > window.innerWidth/2) ? ev.clientX-450 : undefined },
////    'right': function() { return (ev.clientX > window.innerWidth/2) ? undefined : ev.clientX-450 },
//    $('#propertySect').css({
//      'display':'block',
//      /*'right':0,
//      'top':ev.clientY-100,
//      'width':'400px'*/
//    });
//    /*if (ev.clientX > window.innerWidth/2) {
//      $('#propertySect').css('left',0);
//      $('#propertySect').css('right','auto');
//    } else {
//      $('#propertySect').css('left','auto');
//      $('#propertySect').css('right',0);
//    }*/
//  },
  startInstance: function(key, label, bizKey) {
    console.log('startInstance: '+key+' for '+bizKey);
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        processDefinitionId: key,
        businessKey: bizKey
      }),
      success: completeHandler = function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetchInstances();
        ractive.showMessage('Started workflow "'+label+'" for '+bizKey);
      },
    });
  },
  suspend: function(key) {
    console.log('suspend: '+key);
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-definitions/'+key+'/suspend',
      type: 'GET',
      contentType: 'application/json',
      success: completeHandler = function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.fetch();
        ractive.showMessage('Suspended workflow "'+key+'"');
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
        }
    });
  }
});

ractive.on( 'sortInstances', function ( event, column ) {
  console.info('sortInstances on '+column);
  // if already sorted by this column reverse order
  if (this.get('sortInstanceColumn')==column) this.set('sortInstanceAsc', !this.get('sortInstanceAsc'));
  this.set( 'sortInstanceColumn', column );
});
