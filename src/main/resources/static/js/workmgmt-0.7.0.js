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
    return '<div class="clearfix"></div>'
           +'<ul style="padding-left:0">'
           +  '{{# keys}}'
           +    '<li>'
           +      '<label class="col-md-3">{{..toLabel()}}</label>'
           +      '<input id="cur{{.}}" readonly style="width: 60%" value="{{obj[.]}}">'
           +    '</li>'
           +  '{{/}}'
           +'</ul>';
  } },

  // Here, we're passing in some initial data
  data: {
    //server: 'http://api.knowprocess.com',
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    events: [],
    formatJson: function(formProp) { 
      console.log('formatJson: '+formProp);
      console.log('formatJson: '+formProp.name+','+formProp.value);
      var obj = JSON.parse(formProp);
      var html = '';
      $.each(Object.keys(obj), function(i,d) {
        html += '<label class="col-md-3">'+d+'</label>';
        html += '<input id="cur'+d+'" readonly value="'+obj[d]+'">';
      })
      console.log('HTML: '+html);
      ractive.set('current.html',html);
      $('#formatJson').append(html);
    },
    formatDate: function(timeString) {
//      console.log('formatDate: '+timeString);
      if (timeString==undefined) return '';
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    formatDateTime: function(timeString) {
//    console.log('formatDate: '+timeString);
      if (timeString==undefined) return '';
    return new Date(timeString).toLocaleDatetimeString(navigator.languages);
    },
    keys: function(obj) {
      return Object.keys(obj);
    },
    stdPartials: [
      /*{ "name": "navbar", "url": "/partials/simpleTodoFormExtension.html"}*/
    ],
    tasks: [],
    username: localStorage['username'],
  },
  simpleTodoFormExtension: function(x) { 
    console.log('simpleTodoFormExtension: '+JSON.stringify(x));    
  },
  collapseSendMessage: function() {
    console.log('collapseSendMessage...');
    $('#sendMessage').slideUp();
  },
  edit: function(task) { 
    console.log('edit '+task+' ...');
    $('.create-field').hide();
    ractive.set('currentIdx',ractive.get('tasks').indexOf(task));
    ractive.set('current',task);
    ractive.select(task);
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON('/'+ractive.get('tenant.id')+'/tasks/'+ractive.get('username')+'/', function( data ) {
      ractive.merge('tasks', data);
      if (ractive.hasRole('admin')) $('.admin').show();
    });
  },
  fetchUserNotes: function () {
    console.log('fetchUserNotes...');
    $.getJSON("/tasks/"+ractive.get('current.id')+'/notes',  function( data ) {
      ractive.merge('currentNotes', data);
    });
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    var typeaheads = ractive.get('tenant.typeaheadControls'); 
    if (typeaheads==undefined) return; 
    $.each(typeaheads, function(i,d) {
      console.log('binding ' +d.url+' to typeahead control: '+d.selector);
      $.get(d.url, function(data){
        $(d.selector).typeahead({ minLength:0,source:data });
        $(d.selector).on("click", function (ev) {
          newEv = $.Event("keydown");
          newEv.keyCode = newEv.which = 40;
          $(ev.target).trigger(newEv);
          return true;
       });
      },'json');
    });
  },
  initAutoNumeric: function() { 
    $('.autoNumeric').autoNumeric('init', {});
  },
  initControls: function() { 
    console.log('initControls');
    ractive.initAutoComplete();
    ractive.initAutoNumeric();
    ractive.initDatepicker();
  },
  initDatepicker: function() { 
    $('.datepicker').datepicker({
      format: "dd/mm/yyyy",
      autoclose: true,
      todayHighlight: true
    });
  },
  newMessage: function() {
    console.log('newMessage...');
    ractive.set('message', {tenant: ractive.get('tenant.id'),name: ractive.get('tenant.id')+'.messageName'});
    $('#sendMessage').slideDown();
  },
  oninit: function() {
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
    var id = ractive.get('current').id
    
    if (document.getElementById('currentForm').checkValidity()) {
      var t = ractive.get('current');
      t.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: '/task/'+id,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(t),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
//          var location = jqXHR.getResponseHeader('Location');
//          if (location != undefined) ractive.set('current._links.self.href',location);
//          if (jqXHR.status == 201) ractive.get('contacts').push(ractive.get('current'));
          if (jqXHR.status == 204) ractive.splice('tasks',ractive.get('currentIdx'),1,ractive.get('current'));
          ractive.showMessage('Task saved');
          ractive.set('saveObserver',true);
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
    }
  },
  select: function(task) { 
    ractive.set('saveObserver',false);
    $.getJSON('/task/'+task.id+'/', function( data ) {
      console.log('found task '+JSON.stringify(data));
      data.taskLocalVarNames = Object.keys(data.taskLocalVariables);
      data.processVarNames = Object.keys(data.processVariables);
      data.msg = data.processVariables[data.processVariables['messageName']];
      // Ensure freshest data available...
//      $.each(data.formProperties, function(i,d) {
//        console.log('have form prop: '+JSON.stringify(d));
//        if (d.value.startsWith('http://')) { // TODO should restrict to known servers?
//          console.log('fetching: '+d.value);
//          $.getJSON(d.value, function(response) {
//            console.log('... found '+response);
//            data.processVariables[d.id]=response;
//          });
//        }
//      });
      ractive.set('current', data);
      ractive.set('saveObserver',true);
    });
//    ractive.fetchUserNotes();
    ractive.toggleResults();
    $('#currentSect').slideDown();
  },
  sendMessage: function() {
    console.log('sendMessage: '+ractive.get('message.name'));
    var type = ((ractive.get('message.pattern') == 'inOut' || ractive.get('message.pattern') == 'outOnly') ? 'GET' : 'POST');
    var d = (ractive.get('message.pattern') == 'inOut' ? {query:ractive.get('message.body')} : {json:ractive.get('message.body')});
    console.log('d: '+d);
    //var d['businessDescription']=ractive.get('message.bizKey');
    $.ajax({
      url: '/msg/'+ractive.get('tenant.id')+'/'+ractive.get('message.name')+'/',
      type: type,
      data: d,
      dataType: 'text',
      success: completeHandler = function() {
        ractive.showMessage('Message received.');
        ractive.fetch();
        ractive.collapseSendMessage();
      },
    });
  },
  showResults: function() {
    console.log('showResults');
    $('#tasksTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#tasksTable').slideDown();
  },
  startSop: function(key, bizKey) {
    console.log('startSop: '+key+' for '+bizKey);
    $.ajax({
      url: '/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        processDefinitionId: key,
        businessKey: bizKey
      }),
      success: completeHandler = function(data) {
        console.log('response: '+ data);
        ractive.showMessage('Started: '+data.getId());
      },
    });
  },
  submitTask: function() {
    console.log('submitTask '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
    $('#currentSect').hide();
    var id = ractive.get('current').id
    if (document.getElementById('currentForm').checkValidity()) {
      var t = ractive.get('current');
      t.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: '/task/'+id+'?complete',
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(t),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
//          var location = jqXHR.getResponseHeader('Location');
//          if (location != undefined) ractive.set('current._links.self.href',location);
//          if (jqXHR.status == 201) ractive.get('contacts').push(ractive.get('current'));
          if (jqXHR.status <= 300) ractive.fetch();
          ractive.showMessage('Task submitted');
          ractive.showResults();
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
    }
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#tasksTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#tasksTable').slideToggle();
  }
});

//Save on model change
//done this way rather than with on-* attributes because autocomplete 
//controls done that way save the oldValue 
//ractive.observe('current.dueDate current.customFields.*', function(newValue, oldValue, keypath) {
//  ignored=[];
//  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
//    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
//    ractive.save();
//  } else { 
//   console.warn  ('Skipped contact save of '+keypath);
//   //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
//   //console.log('  saveObserver: '+ractive.get('saveObserver'));
//  }
//});
