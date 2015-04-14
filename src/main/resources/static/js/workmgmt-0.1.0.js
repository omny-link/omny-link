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
  // partials: { question: question },

  // Here, we're passing in some initial data
  data: {
    //server: 'http://api.knowprocess.com',
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    formatDate: function(timeString) {
//      console.log('formatDate: '+timeString);
      if (timeString==undefined) return '';
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    tasks: [],
    username: localStorage['username'],
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
  oninit: function() {
    this.ajaxSetup();
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
    $('#currentSect').slideDown();
  },
  showError: function(msg) {
    this.showMessage(msg, 'bg-danger text-danger');
  },
  showFormError: function(formId, msg) {
    this.showError(msg);
    var selector = formId==undefined || formId=='' ? ':invalid' : '#'+formId+' :invalid';
    $(selector).addClass('field-error');
    $(selector)[0].focus();
  },
  showMessage: function(msg, additionalClass) {
    if (additionalClass == undefined) additionalClass = 'bg-info text-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages p').empty().append(msg).removeClass().addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='bg-danger text-danger') setTimeout(function() {
      $('#messages p').fadeOut();
    }, EASING_DURATION*10);
  },
  submitTask: function() {
    console.log('submitTask '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
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

ractive.observe('username', function(newValue, oldValue, keypath) {
  ractive.getProfile();
});

function getSearchParameters() {
  var prmstr = window.location.search == undefined ? null : window.location.search.substr(1);
  return prmstr != null && prmstr != "" ? transformToAssocArray(prmstr) : {};
}

function transformToAssocArray( prmstr ) {
  var params = {};
  var prmarr = prmstr.split("&");
  for ( var i = 0; i < prmarr.length; i++) {
      var tmparr = prmarr[i].split("=");
      params[tmparr[0]] = tmparr[1];
  }
  return params;
}
