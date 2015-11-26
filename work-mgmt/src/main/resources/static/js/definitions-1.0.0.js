var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new AuthenticatedRactive({
  el: 'container',
  template: '#template',
  partials: { simpleTodoFormExtension: function(x) {
    return 'HELLO'+x
  } },
  data: {
    //server: 'http://api.knowprocess.com',
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
    hasRole: function(role) {
      return ractive.hasRole(role);
    },
    stdPartials: [
      { "name": "defnCurrentSect", "url": "/partials/defn-current-sect.html"},
      { "name": "defnListSect", "url": "/partials/defn-list-sect.html"},
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
        url: '/'+ractive.get('tenant.id')+'/deployments/'+deploymentId,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteInstance: function (instance,idx) {
    console.log('deleteInstance '+instance.id+'...');
    $.ajax({
        url: '/'+ractive.get('tenant.id')+'/process-instances/'+instance.id,
        type: 'DELETE',
        success: completeHandler = function(data) {
          console.log('  Success, received: '+data);
          //ractive.fetchInstances();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON('/'+ractive.get('tenant.id')+'/process-definitions', function( data ) {
      ractive.merge('definitions', data);
    });
  },
  fetchInstances: function() {
    console.log('fetch instances');
    var definition = ractive.get('current');
    $.getJSON('/'+ractive.get('tenant.id')+'/process-definitions/'+definition.id+'/instances', function( data ) {
      console.log('found instances '+data.length);
      definition.instances = data;
      ractive.set('current',definition);
    });
  },
  oninit: function() {
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  select: function(definition) {
    ractive.set('current', definition);
//    ractive.set('saveObserver',false);
    $.getJSON('/'+ractive.get('tenant.id')+'/deployments/'+definition.deploymentId, function( data ) {
      console.log('found deployment '+JSON.stringify(data));
      definition.deployment = data;
      ractive.set('current',definition);
      ractive.toggleResults();
      //      ractive.set('saveObserver',true);
    });
    ractive.fetchInstances();
    $('#currentSect').slideDown();
  },
  showResults: function() {
    console.log('showResults');
    $('#currentSect').slideUp();
    $('#definitionsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#definitionsTable').slideDown();
  },
  startInstance: function(key, label, bizKey) {
    console.log('startInstance: '+key+' for '+bizKey);
    $.ajax({
      url: '/'+ractive.get('tenant.id')+'/process-instances/',
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
  toggleAuditTrail: function(instance, idx) {
    console.log('toggleAuditTrail for: '+instance.id);
    if ($('section[data-instanceid="'+instance.id+'"]').is(':visible')) {
      $('section[data-instanceid="'+instance.id+'"]').hide();
      $($('.btn-details')[idx]).empty().append('View Details');
    } else {
      $.getJSON('/'+ractive.get('tenant.id')+'/process-instances/'+instance.id, function( data ) {
        console.log('found audit trail: '+data.auditTrail.length);
        data.auditTrail.sort(function(a,b) { return new Date(b.startTime)-new Date(a.startTime); });
        var def = ractive.get('current');
        def.instances[idx].auditTrail=data.auditTrail;
        
        console.log('found processVariables: '+data.processVariables);
        def.instances[idx].processVariables=data.processVariables;
        def.instances[idx].processVariableNames=Object.keys(data.processVariables);
        
        ractive.set('current',def);
        $('[data-instanceId='+instance.id+']').slideDown();
        $($('.btn-details')[idx]).empty().append('Hide Details');
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
        url: '/'+ractive.get('tenant.id')+'/deployments',
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

