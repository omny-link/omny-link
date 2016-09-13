var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new AuthenticatedRactive({
  el: 'container',

  template: '#template',

  partials: {
    defaultCtrl: function() {
      return $('#defaultTemplate').html();
    },
    enumCtrl: function() {
      return $('#enumTemplate').html();
    },
    jsonCtrl: function(v) {
      return $('#jsonTemplate').html();
    },
    linkCtrl: function() {
      $.getJSON(ractive.get('current.processVariables.contactId'), function (data) { 
        ractive.set('current.processVariables.contact',data);
        $.each(Object.keys(data), function(i,d) {
          var idx = ractive.findFormProperty(d);
          if (idx != -1) ractive.set('current.formProperties['+idx+'].value',data[d]);          
        });
      });
      return $('#linkTemplate').html();
    },
    userForm: function() {
      console.log('renderUserForm');
      var form = ractive.get('current.formKey');
      // process migration issue...
      if (form == 'simpleTodo') form = '/partials/generic-form.html';
      if (form == '/partials/simpleTodoFormExtension.html') form = '/partials/generic-form.html';
      // catch all form
      if (form == undefined) form = '/partials/generic-form.html';
      $.get(form, function (partial) {
        if (ractive != undefined) ractive.resetPartial('userForm',partial);
        ractive.initControls();

        if (ractive.get('current.processVariables.contactId') != undefined) {
          $.getJSON(ractive.get('current.processVariables.contactId'), function (data) {
            ractive.set('current.processVariables.contact',data);
          });
        }
      });
      return '<div>Loading...</div>';
    },
    userPropertiesForm: function() {
      console.info('userPropertiesForm');
    }
  },

  data: {
    //server: 'http://api.knowprocess.com',
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    events: [],
    filter: 'deferred',
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
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    formatDateIfPresent: function(timeString) {
      if (timeString==undefined) return undefined;
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    formatDateTime: function(timeString) {
//    console.log('formatDate: '+timeString);
      if (timeString==undefined) return '';
      return new Date(timeString).toLocaleDatetimeString(navigator.languages);
    },
    formatLabel: function(str) {
      return str.toLabel();
    },
    helpUrl: 'http://omny.link/user-help/work/#the_title',
    keys: function(obj) {
      return Object.keys(obj);
    },
    matchFilter: function(obj) {
      console.log("matchFilter: "+obj+'('+JSON.stringify(obj.taskLocalVariables)+')');
      if (ractive.get('filter')==undefined) return true; 
      else if (ractive.get('filter').indexOf('deferred')!=-1 && ractive.isDeferred(obj)) return false;
      else return true;
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
    renderAs: function(formProp) {
      console.log('renderAs: '+formProp.name);
      if (typeof formProp.value == 'string' && formProp.value.substring(0,4)=='http') {
        return 'link';
      } else if (typeof formProp.value == 'string' && formProp.value.substring(0,1)=='{') {
        return 'json';
      } else if (formProp.type == 'enum') {
        return 'enum';
      } else {
        return 'default';
      }
    },
    matchSearch: function(obj) {
      var searchTerm = ractive.get('searchTerm');
      //console.info('matchSearch: '+searchTerm);
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.businessKey.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.name!=undefined && obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.taskDefinitionKey.toLabel().toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created>') && new Date(obj.firstContact)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created<') && new Date(obj.firstContact)<new Date(ractive.get('searchTerm').substring(8)))
        );
      }
    },
    renderPriority: function(task) {
      console.log('renderPriority');
      if (ractive.isOverdue(task)) return 'overdue';
      if (ractive.isDue(task)) return 'due';
      return '';
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
    sortColumn: 'createTime',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "customActionModal", "url": "/partials/custom-action-modal.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/work-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "workCurrentSect", "url": "/partials/work-current-sect.html"},
      { "name": "workListSect", "url": "/partials/work-list-sect.html"}
    ],
    tasks: [],
    title: 'Work Management',
    username: localStorage['username'],
  },
  collapseSendMessage: function() {
    console.log('collapseSendMessage...');
    $('#sendMessage').slideUp();
  },
  deferTask: function(until) {
    until = until == undefined ? 'PT24H' : until;
    console.log('deferTask until: '+until);
    ractive.submitTask('defer='+until);
  },
  edit: function(task) { 
    console.log('edit '+task+' ...');
    $('.create-field').hide();
    ractive.set('currentIdx',ractive.get('tasks').indexOf(task));
    ractive.set('current',task);
    ractive.select(task);
  },
  endInstance: function(piid) {
    console.log('endInstance...');
    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/'+piid,
        type: 'DELETE',
        contentType: 'application/json',
        success: completeHandler = function(data, textStatus, jqXHR) {
          console.log('data: '+data);
          if (jqXHR.status == 200) {
        	  ractive.fetch();
              ractive.showMessage('Ended successfully');
          }
          ractive.showResults();
          //ractive.set('saveObserver',true);
        }
      });
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/tasks/'+ractive.get('username')+'/', function( data ) {
      ractive.merge('tasks', data);
      if (ractive.hasRole('admin')) $('.admin').show();
      ractive.set('searchMatched',$('#tasksTable tbody tr:visible').length);
    });
  },
  fetchUserNotes: function () {
    console.log('fetchUserNotes...');
    $.getJSON(ractive.getServer()+'/tasks/'+ractive.get('current.id')+'/notes',  function( data ) {
      ractive.merge('currentNotes', data);
    });
  },
  filter: function(val) { 
    ractive.set('filter', val);
    ractive.set('searchMatched',$('#tasksTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  findFormProperty: function(name) { 
    console.log('findFormProperty: '+name);
    var rtn = -1;
    $.each(ractive.get('current.formProperties'), function(i,d) {
      if (d.id==name) { 
        console.log('found match ');
        rtn = i;
        return;
      }
    });
    return rtn;
  },
  isDeferred: function(task) {
    if (task.taskLocalVariables.length==0 || task.taskLocalVariables['deferUntil']==undefined || new Date(task.taskLocalVariables['deferUntil']).getTime() <= new Date().getTime()) { 
      return false;
    } else { 
      return true;
    }
  },
  isDue: function(task) {
    if (task.dueDate!=undefined && new Date(task.dueDate).getTime() <= (new Date().getTime()+24*60*60*1000)) { 
      return true;
    } else if (task.taskLocalVariables!=undefined && task.taskLocalVariables['deferUntil']!=undefined && new Date(task.taskLocalVariables['deferUntil']).getTime() <= (new Date().getTime()+24*60*60*1000)) {
      return true;
    } else { 
      return false;
    }
  },
  isOverdue: function(task) {
    if (task.dueDate!=undefined && new Date(task.dueDate).getTime() <= new Date().getTime()) { 
      return true;
    } else if (task.taskLocalVariables!=undefined && task.taskLocalVariables['deferUntil']!=undefined && new Date(task.taskLocalVariables['deferUntil']).getTime() <= new Date().getTime()) {
      return true;
    } else { 
      return false;
    }
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
  reviseValidation: function() {
    console.log('reviseValidation');
    if (ractive.get('current.processVariables.stage')=='Cold') {
      $(':required').removeAttr('required');
    }
  },
  save: function () {
    console.log('save '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
    var id = ractive.get('current').id
    
    if (document.getElementById('currentForm').checkValidity()) {
      var t = ractive.get('current');
      t.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: ractive.getServer()+'/task/'+id,
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
  saveContact: function() { 
    console.log('saveContact '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
    var id = ractive.get('current').id
    
    if (document.getElementById('currentForm').checkValidity()) {
      var contact = ractive.get('current.processVariables.contact');
      contact.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: ractive.get('current.processVariables.contactId'),
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(contact),
        success: function(data, textStatus, jqXHR) {
          console.log('saved contact: '+ jqXHR.status);
          ractive.showMessage('Contact saved successfully');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
    }
  },
  saveNote: function () {
    console.info('saveNote '+JSON.stringify(ractive.get('current.note'))+' ...');
    var n = { author:ractive.get('username'), contact: ractive.get('current.processVariables.contactId'), content: $('#note').val()}
    var url = ractive.get('current.processVariables.contactId')+'/notes';
    url = url.replace('contacts/',ractive.get('tenant.id')+'/contacts/');
    console.log('  url:'+url);
    if (n.content.trim().length > 0) { 
      $.ajax({
        url: url,
        type: 'POST',
        data: n,
        success: completeHandler = function(data) {
          console.log('response: '+ data);
          ractive.showMessage('Note saved successfully'); 
          $('#note').val(undefined);
        }
      });
    }
  },
  select: function(task) { 
    ractive.set('saveObserver',false);
    $.getJSON(ractive.getServer()+'/task/'+task.id+'/', function( data ) {
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

      // Remove previous initiators (one added with each select, possible ractive binding bug)
      $('.initiator span:not(:first)').remove();

      // set image for initiator
      if (ractive.get('current.processVariables')["initiator"]==undefined || ractive.get('current.processVariables')["initiator"]=='anonymousUser') {
        $('.initiator-img').empty().append('<img class="img-rounded" src="/images/icon/omny-icon.png" width="34"/>');
      } else {
        $('.initiator-img').empty().append('<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(ractive.get('current.processVariables')["initiator"])+'?s=34"/>');
      }
      ractive.set('saveObserver',true);
      // due date handling
      if ($('#curDueDate').datepicker()!=undefined) $('#curDueDate').datepicker('destroy');
      $('#curDueDate').datepicker('update',new Date(ractive.get('current.dueDate')));
      $('#curDueDate').datepicker().on('changeDate', function(e) {
        console.log('date changed:'+JSON.stringify(e));
        ractive.set('current.dueDate', $('#curDueDate').datepicker('getDate'));
        ractive.save();
      });
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
      url: ractive.getServer()+'/msg/'+ractive.get('tenant.id')+'/'+ractive.get('message.name')+'/',
      type: type,
      data: d,
      dataType: 'text',
      success: function(data) {
        ractive.showMessage('Message received.');
        if (ractive.get('message.pattern') == 'inOut' || ractive.get('message.pattern') == 'outOnly') {
          $('#response').slideDown();
          $('#responseBody').html(data);
          $('#cancel').empty().append('Close');
        } else {
          ractive.collapseSendMessage();
        }
        ractive.fetch();
      },
    });
  },
  showResults: function() {
    console.log('showResults');
    $('#tasksTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#tasksTable').slideDown({ queue: true });
  },
  startCustomAction: function(key, label, form) {
    console.log('startCustomAction: '+key+' using form '+form);
    var instanceToStart = {
        businessKey: label,
        processDefinitionId: key,
        label: label,
        processVariables: {
          initiator: ractive.get('username'),
          tenantId: ractive.get('tenant.id')
        }
      };
    console.log(JSON.stringify(instanceToStart));
    // save what we know so far...
    ractive.set('instanceToStart',instanceToStart);
    if (form == undefined) {
      // ... and submit
      ractive.submitCustomAction();
    } else {
      // ... or display form
      $('#customActionModal').modal('show');
    }
  },
  submitCustomAction: function(key, bizKey) {
    console.info('submitCustomAction');
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('instanceToStart')),
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        var msg = 'Started workflow "'+ractive.get('instanceToStart.label')+'"';
        if (ractive.get('instanceToStart.businessKey')!=undefined) msg+=(' for '+ractive.get('instanceToStart.businessKey'));
        ractive.showMessage(msg);
        $('#customActionModal').modal('hide');
      }
    });
  },
  submitTask: function(action) {
    console.log('submitTask '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
    
    var id = ractive.get('current').id;
    $('#currentSect').hide();
    var t = ractive.get('current');
    t.tenantId = ractive.get('tenant.id');
    $.ajax({
      url: ractive.getServer()+'/task/'+id+'?'+action,
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
  },
  submitColdTask: function() {
    ractive.set('current.processVariables.stage','Cold');
    ractive.submitTask('complete');
  },
  submitCompleteTask: function() {
    console.log('submitCompleteTask...');
    if (document.getElementById('currentForm').checkValidity()) {
      ractive.submitTask('complete');
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

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#contactsTable tbody tr').length);
  }, 500);
});
ractive.on( 'filter', function ( event, filter ) {
  console.info('filter on '+JSON.stringify(event)+','+filter.idx);
  ractive.filter(filter);
});
ractive.on( 'sort', function ( event, column ) {
  console.info('sort on '+column);
  // if already sorted by this column reverse order
  if (this.get('sortColumn')==column) this.set('sortAsc', !this.get('sortAsc'));
  this.set( 'sortColumn', column );
});
