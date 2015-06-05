var EASING_DURATION = 500;
var fadeOutMessages = true;
// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new AuthenticatedRactive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',

  // We could pass in a string, but for the sake of convenience
  // we're passing the ID of the <script> tag above.
  template: '#template',

  // partial templates
  partials: { simpleTodoFormExtension: function(x) {
    return 'HELLO'+x
  } },

  // Here, we're passing in some initial data
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
      if (timeString==undefined) return '';
      return new Date(timeString).toLocaleString(navigator.languages);
    },
    hasRole: function(role) {
      return ractive.hasRole(role);
    },
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
  showAuditTrail: function(instance, idx) {
    console.log('showAuditTrail for: '+instance.id);
    $.getJSON('/'+ractive.get('tenant.id')+'/process-instances/'+instance.id, function( data ) {
      console.log('found audit trail: '+data.auditTrail.length);
      data.auditTrail.sort(function(a,b) { return new Date(b.startTime)-new Date(a.startTime); });
      var def = ractive.get('current');
      def.instances[idx].auditTrail=data.auditTrail;
      ractive.set('current',def);
      $('[data-instanceId='+instance.id+']').slideDown();
    });
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

