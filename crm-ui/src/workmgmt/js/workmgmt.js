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

  partials: {
    profileArea: '',
    titleArea: '',
    workListSect: '',
    workCurrentSect: '',
    sidebar: '',
    helpModal: '',
    customActionModal: '',
    supportBar: '',
    toolbar: '',
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
      $.getJSON(ractive.get('current.variables.contactId'), function (data) {
        ractive.set('current.variables.contact',data);
        $.each(Object.keys(data), function(i,d) {
          var idx = ractive.findFormProperty(d);
          if (idx != -1) ractive.set('current.formProperties['+idx+'].value',data[d]);
        });
      });
      return $('#linkTemplate').html();
    },
    memoCtrl: function(v) {
      return $('#memoTemplate').html();
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
        if (ractive.get('current.variables.contactId') != undefined) {
          $.getJSON(ractive.get('current.variables.contactId'), function (data) {
            ractive.set('current.variables.contact',data);
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
    formatDate: function(timeString) {
      console.log('formatDate: '+timeString);
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
      return new Date(timeString).toLocaleString(navigator.languages);
    },
    formatLabel: function(str) {
      return str.toLabel();
    },
    helpUrl: '//omny.link/user-help/work/#the_title',
    keys: function(obj) {
      return Object.keys(obj);
    },
    matchFilter: function(obj,i) {
      console.log("matchFilter: "+i+':'+obj+'('+JSON.stringify(obj.variables)+')');
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
      if (typeof formProp.id=='accountId') {
        if (formProp.value.substring(0,4)!='http') formProp.value = '/accounts.html?q='+formProp.value;
        return 'link';
      } else if (formProp.id=='contactId') {
        if (formProp.value.substring(0,4)!='http') formProp.value = '/contacts.html?q='+formProp.value;
        return 'link';
      } else if (typeof formProp.value == 'string' && formProp.id=='imageUrl') {
        return 'image';
      } else if (typeof formProp.value == 'string' && formProp.value.substring(0,1)=='{') {
        return 'json';
      } else if (typeof formProp.value == 'string' && formProp.value.substring(0,4)=='http') {
        return 'link';
      } else if (formProp.id == 'memoName') {
        return 'memo';
      } else if (formProp.id=='orderId') {
        if (formProp.value.substring(0,4)!='http') formProp.value = '/orders.html?q='+formProp.value;
        return 'link';
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
        return ( (obj.businessKey!=undefined && obj.businessKey.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.name!=undefined && obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.id.indexOf(searchTerm)>=0)
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
        if (array === undefined) return;
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
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc';
      else return 'hidden';
    },
    stdPartials: [
      { "name": "customActionModal", "url": "/partials/custom-action-modal.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
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
    switch (until) {
    case 'P1D':
      until = new Date(new Date().getTime() + (1000*60*60*24)).toISOString();
      break;
    case 'P7D':
      until = new Date(new Date().getTime() + (7*1000*60*60*24)).toISOString();
      break;
    case 'P14D':
      until = new Date(new Date().getTime() + (14*1000*60*60*24)).toISOString();
      break;
    case 'P30D':
      until = new Date(new Date().getTime() + (30*1000*60*60*24)).toISOString();
      break;
    }
    ractive.submitTaskVar('deferUntil',until,'local','date');
    ractive.showResults();
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
        url: ractive.getBpmServer()+'/flowable-rest/service/runtime/process-instances/'+piid,
        type: 'DELETE',
        contentType: 'application/json',
        crossDomain: true,
        headers: { Authorization: ractive.getBpmAuth() },
        success: function(data, textStatus, jqXHR) {
          console.log('data: '+data);
          if (jqXHR.status == 204) {
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
    if (ractive.get('fetch')==true) {
      console.info('  skip fetch as already in progress');
      return;
    }
    ractive.set('fetch', true);
    $.ajax({
      url: ractive.getBpmServer()+'/flowable-rest/service/query/tasks',
      type: 'POST',
       data: JSON.stringify({
        includeTaskLocalVariables: true,
        includeProcessVariables: true,
        involvedUser: ractive.get('profile.username'),
        start: 0,
        size: 500
      }),
      contentType: 'application/json',
      crossDomain: true,
      headers: { Authorization: ractive.getBpmAuth() },
      success: function(data, textStatus, jqXHR) {
        ractive.set('saveObserver', false);
        // flowable contains metadata as well, use data only
        data = data.data;
        console.log('fetched ' + data.length + ' tasks');

        data.forEach(function(t,i) {
          var data2 = {};
          t.variables.forEach(function(v,j) { data2[v['name']]= v['value']; });
          t.msg=data2['messageName'];
          t.processVarNames=Object.keys(data2);
          t.variables=data2;
        });

        if (jqXHR.status == 200) {
          ractive.showMessage('Fetched tasks successfully');
          ractive.merge('tasks', data);
          ractive.set('xTasks', ractive.get('tasks'));
          if (ractive.hasRole('admin')) $('.admin').show();
          ractive.showSearchMatched();
        }
        ractive.set('fetch', false);
        ractive.set('saveObserver',true);
      }
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.dropdown.dropdown-menu li').removeClass('selected')
    $('.dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
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
    if (task.variables.length==0 || task.variables['deferUntil']==undefined || new Date(task.variables['deferUntil']).getTime() <= new Date().getTime()) {
      return false;
    } else {
      return true;
    }
  },
  isDue: function(task) {
    if (task.dueDate!=undefined && new Date(task.dueDate).getTime() <= (new Date().getTime()+24*60*60*1000)) {
      return true;
    } else if (task.variables!=undefined && task.variables['deferUntil']!=undefined && new Date(task.variables['deferUntil']).getTime() <= (new Date().getTime()+24*60*60*1000)) {
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
    } else if (task.variables!=undefined && task.variables['deferUntil']!=undefined && new Date(task.variables['deferUntil']).getTime() <= new Date().getTime()) {
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
  reviseValidation: function() {
    console.log('reviseValidation');
    if (ractive.get('current.variables.stage')=='Cold') {
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
        success: function(data, textStatus, jqXHR) {
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
      var contact = ractive.get('current.variables.contact');
      contact.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: ractive.get('current.variables.contactId'),
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
    var n = { author:ractive.get('profile.username'), contact: ractive.get('current.variables.contactId'), content: $('#note').val()}
    var url = ractive.get('current.variables.contactId')+'/notes';
    url = url.replace('contacts/',ractive.get('tenant.id')+'/contacts/');
    console.log('  url:'+url);
    if (n.content.trim().length > 0) {
      $.ajax({
        url: url,
        type: 'POST',
        data: n,
        success: function(data) {
          console.log('response: '+ data);
          ractive.showMessage('Note saved successfully');
          $('#note').val(undefined);
        }
      });
    }
  },
  select: function(task) {
    ractive.set('saveObserver',false);
    $.ajax({
      url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+task.id,
      type: 'GET',
      contentType: 'application/json',
      crossDomain: true,
      headers: { Authorization: ractive.getBpmAuth() },
      success: function(data, textStatus, jqXHR) {
        console.log('found task '+JSON.stringify(data));
        // avoid getting 1 Jan 1970
        if (data.dueDate==undefined) data.dueDate='';
        else data.dueDate = new Date(data.dueDate).toISOString().substring(0,10);
        ractive.set('current', data);

        $.ajax({
          url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+task.id+'/variables?scope=global',
          type: 'GET',
          contentType: 'application/json',
          crossDomain: true,
          headers: { Authorization: ractive.getBpmAuth() },
          success: function(data, textStatus, jqXHR) {
            var data2 = {};
            data.forEach(function(d,i) { data2[d['name']]= d['value']; });
            ractive.set('current.msg',data2['messageName']);
            ractive.set('current.processVarNames',Object.keys(data2));
            ractive.set('current.variables',data2);
            // Remove previous initiators (one added with each select, possible ractive binding bug)
            $('.initiator span:not(:first)').remove();
            ractive.initiatorIcon(ractive.get('current.variables')["initiator"]);
          }
        });
        $.ajax({
          url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+task.id+'/variables?scope=local',
          type: 'GET',
          contentType: 'application/json',
          crossDomain: true,
          headers: { Authorization: ractive.getBpmAuth() },
          success: function(data, textStatus, jqXHR) {
            var data2 = {};
            data.forEach(function(d,i) { data2[d['name']]= d['value']; });
            ractive.set('current.msg',data2['messageName']);
            ractive.set('current.taskVarNames',Object.keys(data2));
            ractive.set('current.taskVariables',data2);
            ractive.set('current.variables',{
              ...ractive.get('current.variables'), ...ractive.get('current.taskVariables')
	    });
          }
        });
        $.ajax({
          url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+task.id+'/form',
          type: 'GET',
          contentType: 'application/json',
          crossDomain: true,
          headers: { Authorization: ractive.getBpmAuth() },
          success: function(data, textStatus, jqXHR) {
            ractive.set('current.formProperties',data.fields);
          }
        });
        console.log('found task '+JSON.stringify(data));
      }
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
    $('#tasksTableToggle').addClass('kp-icon-caret-down').removeClass('kp-icon-caret-right');
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
    $('#tasksTableToggle').addClass('kp-icon-caret-right').removeClass('kp-icon-caret-down');
    $('#tasksTable').slideUp();
    $('#currentSect').slideDown({ queue: true });
  },
  submitTask: function(action) {
    console.log('submitTask '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);

    $('#currentSect').hide();
    var t = ractive.get('current');
    t.action = action;
    t.tenantId = ractive.get('tenant.id');
    $.ajax({
      url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+t.id,
      type: 'POST',
      contentType: 'application/json',
      crossDomain: true,
      data: JSON.stringify(t),
      headers: { Authorization: ractive.getBpmAuth() },
      success: function(data, textStatus, jqXHR) {
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
  submitTaskVar: function(name,value,scope,type) {
    console.log('submitTaskVar '+ractive.get('current.id')+': '+name+'='+value+' '+scope+'...');
    ractive.set('saveObserver',false);

    var t = ractive.get('current');
    t.tenantId = ractive.get('tenant.id');
    if (ractive.get('current.processVarNames').includes(name) || ractive.get('current.taskVarNames').includes(name)) {
      // UPDATE
      $.ajax({
        url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+t.id+'/variables/'+name,
        type: 'PUT',
        contentType: 'application/json',
        crossDomain: true,
        data: JSON.stringify({
          name:name,
          value:value,
          scope:scope===undefined?'global':scope,
          type:type===undefined?'string':type
        }),
        headers: { Authorization: ractive.getBpmAuth() },
        success: function(data, textStatus, jqXHR) {
          ractive.set('saveObserver',true);
          ractive.fetch();
        }
      });
    } else {
      // CREATE
      $.ajax({
        url: ractive.getBpmServer()+'/flowable-rest/service/runtime/tasks/'+t.id+'/variables',
        type: 'POST',
        contentType: 'application/json',
        crossDomain: true,
        data: JSON.stringify([{
          name:name,
          value:value,
          scope:scope===undefined?'global':scope,
          type:type===undefined?'string':type
        }]),
        headers: { Authorization: ractive.getBpmAuth() },
        success: function(data, textStatus, jqXHR) {
          ractive.set('saveObserver',true);
        }
      });
    }
  },
  submitColdTask: function() {
    ractive.set('current.variables.stage','Cold');
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
    $('#tasksTableToggle').toggleClass('kp-icon-caret-down').toggleClass('kp-icon-caret-right');
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

