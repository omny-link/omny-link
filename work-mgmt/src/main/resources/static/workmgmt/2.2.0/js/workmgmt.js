var EASING_DURATION = 500;
var fadeOutMessages = true;
var ractive = new BaseRactive({
  el: 'container',

  template: '#template',

  partials: {
    profileArea: '',
    titleArea: '',
    loginSect: '',
    workListSect: '',
    workCurrentSect: '',
    sidebar: '',
    helpModal: '',
    customActionModal: '',
    supportBar: '',
    defaultCtrl: function() {
      return $('#defaultTemplate').html();
    },
    enumCtrl: function() {
      return $('#enumTemplate').html();
    },
    imageCtrl: function(v) {
      return $('#imageTemplate').html();
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

        // TODO Seems this doesn't work, race condition?
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
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    events: [],
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
    },
    filter: {idx:8, field: 'taskLocalVariables.deferUntil', func: 'isActive'},
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
    helpUrl: '//omny.link/user-help/work/#the_title',
    keys: function(obj) {
      return Object.keys(obj);
    },
    matchFilter: function(obj,i) {
      console.log("matchFilter: "+i+':'+obj+'('+JSON.stringify(obj.taskLocalVariables)+')');
      var f = ractive.get('filter');
      if (f.operator==undefined) f.operator='==';

      if (f.func=='isDueToday') return ractive.isDueToday(obj);
      else if (f.func=='isDueTodayTomorrow') return ractive.isDueTodayTomorrow(obj);
      else if (f.func=='isDueThisWeek') return ractive.isDueThisWeek(obj);
      else if (f.func=='isDueThisMonth') return ractive.isDueThisMonth(obj);
      else if (f.func=='isActive') return !ractive.isDeferred(obj);
      else if (f.func=='isDeferred') return ractive.isDeferred(obj);
      else return eval('obj["'+f.field+'"]'+f.operator+f.value);
    },
    matchPage: function(pageName) {
      console.info('matchPage: '+pageName);
      return document.location.href.indexOf(pageName)!=-1;
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
//      if (typeof formProp.value == 'string' && formProp.id=='imageUrl') {
//        return 'image';
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
          || (obj.processInstanceId.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
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
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "navbar", "url": "/partials/work-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "workCurrentSect", "url": "/partials/work-current-sect.html"},
      { "name": "workListSect", "url": "/partials/work-list-sect.html"},
      { "name": "workListTable", "url": "/partials/task-list-table.html"}
    ],
    tasks: [],
    title: 'Work Management'
  },
  collapseSendMessage: function() {
    console.log('collapseSendMessage...');
    $('#sendMessage').slideUp();
  },
  deferTask: function(until) {
    if (until == undefined) until = ractive.get('deferUntil') == undefined ? 'PT24H' : ractive.get('deferUntil');
    console.log('deferTask until: '+until);
    $('#remindBtn').dropdown('toggle');
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
              ractive.showMessage('Ended workflow successfully');
          }
          ractive.showResults();
          //ractive.set('saveObserver',true);
        }
      });
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/tasks/'+$auth.getClaim('sub')+'/', function( data ) {
      ractive.merge('tasks', data);
      ractive.set('xTasks', ractive.get('tasks'));
      if (ractive.hasRole('admin')) $('.admin').show();
      ractive.showSearchMatched();
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected')
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.showSearchMatched();
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
  isDueBefore: function(task, when) {
    if (when != undefined && task.dueDate!=undefined && new Date(task.dueDate).getTime() <= when.getTime()) {
      return true;
    } else {
      return false;
    }
  },
  isDueToday: function(task) {
    var deadline = new Date();
    deadline = new Date(deadline.setDate(deadline.getDate()+1)); // add 1 day
    deadline = new Date(deadline.setHours(18)); // set close of business to 18:00
    deadline = new Date(deadline.setMinutes(0));
    return ractive.isDueBefore(task, deadline);
  },
  isDueTodayTomorrow: function(task) {
    var deadline = new Date();
    deadline = new Date(deadline.setDate(deadline.getDate()+2)); // add 2 days
    deadline = new Date(deadline.setHours(18)); // set close of business to 18:00
    deadline = new Date(deadline.setMinutes(0));
    return ractive.isDueBefore(task, deadline);
  },
  isDueThisWeek: function(task) {
    var deadline = new Date();
    deadline = new Date(deadline.setDate(deadline.getDate()+(5-deadline.getDay()))); // set to Friday
    deadline = new Date(deadline.setHours(18)); // set close of business to 18:00
    deadline = new Date(deadline.setMinutes(0));
    return ractive.isDueBefore(task, deadline);
  },
  isDueThisMonth: function(task) {
    var now  = new Date();
    var deadline = new Date(now.getFullYear(), now.getMonth()+1, 0, 18, 0);
    return ractive.isDueBefore(task, deadline);
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
//    this.loadStandardPartials(this.get('stdPartials'));
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
          console.error(textStatus+': '+errorThrown);
          ractive.showError('So sorry, we can\'t save at this time, please refresh the page and try again');
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
    var n = { author:$auth.getClaim('sub'), contact: ractive.get('current.processVariables.contactId'), content: $('#note').val()}
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
      data.taskLocalVarNames = data['taskLocalVariables'] == undefined ? [] : Object.keys(data.taskLocalVariables);
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
      // avoid getting 1 Jan 1970
      if (data.dueDate==undefined) data.dueDate='';
      else data.dueDate = new Date(data.dueDate).toISOString().substring(0,10);
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
    });
    ractive.showTask();
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
  showSearchMatched: function() {
    ractive.set('searchMatched',$('#tasksTable tbody tr').length);
    if ($('#tasksTable tbody tr:visible').length==1) {
      var taskId = $('#tasksTable tbody tr:visible').data('href')
      var task = Array.findBy('id',ractive.localId(taskId),ractive.get('tasks'))
      ractive.select( task );
    }
  },
  showTask: function() {
    console.log('showResults');
    $('#tasksTableToggle').addClass('glyphicon-triangle-right').removeClass('glyphicon-triangle-bottom');
    $('#tasksTable').slideUp();
    $('#currentSect').slideDown({ queue: true });
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

