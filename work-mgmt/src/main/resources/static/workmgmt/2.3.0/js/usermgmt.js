var ractive = new BaseRactive({
  el: 'container',
  template: '#template',
  data: {
    haveCustomExtension: function(extName) {
      return Array.findBy('name',ractive.get('tenant.id')+extName,ractive.get('tenant.partials'))!=undefined;
    },
    helpUrl: '//omny.link/user-help/users/#the_title',
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
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
    sortAsc: true,
    sortColumn: 'fullName',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "currentUserExtensionSect", "url": "/partials/user-extension.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "navbar", "url": "/partials/user-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "userCurrentSect", "url": "/partials/user-current-sect.html"},
      { "name": "userListSect", "url": "/partials/user-list-sect.html"}
    ],
    tenant: { id: 'omny' },
    title: 'User Management',
    users: []
  },
  partials: {
    helpModal: '',
    loginSect: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    titleArea: '',
    userCurrentSect: '',
    userInfo: '<li class="form-group">'
      	     +'  <label class="col-md-4 col-sm-4">{{#tenant.strings[key]}}{{tenant.strings[key]}}{{else}}{{key.toLabel()}}{{/}}:</label>'
      	     +'  <input class="form-control" id="cur{{key.toLeadingCaps().replace(/-/g,\'\')}}"'
      	     +'      on-blur="saveUserInfo(this)" value="{{value}}">'
      	     +'  <p class="col-md-offset-4 col-sm-offset-4 help-block">{{tenant.strings[key+\'Hint\']}}</p>'
      	     +'</li>',
    userListSect: ''
  },
  addGroup: function () {
    console.log('addGroup...');
    $('#curNewGroup').removeClass('hidden').slideDown();
  },
  addUserToGroup: function(newGroup) {
    console.log('addUserToGroup: '+newGroup);

    $.ajax({
        url: ractive.getServer()+'/users/'+ractive.get('current.id')+'/groups/'+newGroup,
        type: 'POST',
        contentType: 'application/json',
        success: completeHandler = function() {
          ractive.get('current.groups').push( {id:newGroup.toLowerCase(),name:newGroup} );
        }
      });
  },
  addUser: function (user) {
    console.log('addUser '+user+' ...');
    ractive.set('currentAction', 'CREATE');
    ractive.set('current', { groups: [] });
    $('.create-field').show();
    $('.no-update-field').removeProp('readonly');
    ractive.select(ractive.get('current'));
  },
  delete: function (url) {
    console.log('delete '+url+'...');
    $.ajax({
      url: url,
      type: 'DELETE',
      success: completeHandler = function() {
        ractive.fetch();
      }
    });
  },
  deleteUserFromGroup: function(group) {
    console.log('deletUserFromGroup: '+group);

    $.ajax({
        url: ractive.getServer()+'/users/'+ractive.get('current.id')+'/groups/'+group,
        type: 'DELETE',
        contentType: 'application/json',
        success: completeHandler = function() {
//          ractive.get('current.groups').push( {id:newGroup.toLowerCase(),name:newGroup} );
        }
      });
  },
  edit: function(user) {
    console.log('editUser '+user+' ...');
    $('.create-field').hide();
    $('.no-update-field').prop('readonly','readonly');
    ractive.set('currentAction', 'EDIT');
    ractive.set('current',user);
    ractive.select(user);
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/users/',  function( data ) {
      ractive.merge('users', data);
      if (ractive.hasRole('admin')) $('.admin').show();
      ractive.showSearchMatched();
    });
  },
  fetchUserGroups: function () {
    console.log('fetchUserGroups...');
    $.getJSON(ractive.getServer()+"/users/"+ractive.get('current.id')+'/groups',  function( data ) {
      ractive.merge('currentGroups', data);
    });
  },
  hideResults: function() {
    console.log('hideResults');
    $('#usersTableToggle').removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-right');
    $('#usersTable').slideUp();
  },
  save: function () {
    console.log('save '+ractive.get('current')+' ...');
    if (!document.getElementById('userForm').checkValidity()) {
      ractive.showFormError('userForm','Please correct the highlighted fields');
      return ;
    }
    var tmp = ractive.get('current');
    delete tmp.authorities;
    $.ajax({
      url: ractive.getServer()+'/users/'+(ractive.get('currentAction') == 'CREATE' ? '' : ractive.get('current.id')),
      type: ractive.get('currentAction') == 'CREATE' ? 'POST' : 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(tmp),
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.set('saveObserver',false);
        ractive.showMessage('User has been saved successfully');
        ractive.fetch();
        ractive.showResults();
        ractive.set('currentAction', 'UPDATE');
        ractive.set('saveObserver',true);
      }
    });
  },
  saveUserInfo: function (info) {
    console.log('saveUserInfo '+info.key+' ...');
    if (!document.getElementById('userForm').checkValidity()) {
      ractive.showFormError('userForm','Please correct the highlighted fields');
      return ;
    }

    $.ajax({
      url: ractive.tenantUri(ractive.get('current'))+'/info/'+info.key,
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ key: info.key, value: info.value }),
      success: completeHandler = function() {
        ractive.showMessage('User info has been saved successfully');
      }
    });
  },
  select: function(user) {
    if (user!=undefined && user['id']!=undefined) {
      ractive.set('saveObserver',false);
      $.getJSON(ractive.getServer()+'/users/'+user.id, function( data ) {
        console.log('found user '+JSON.stringify(data));
        ractive.set('saveObserver',false);
        ractive.set('current', data);

        for (var idx = 0 ; idx < data.groups.length ; idx++) {
          $('#curGroups').append('<li>'+data.groups[idx].id+'</li>');
        }
        if ($("#curGroups").is(":ui-tagit")) $("#curGroups").tagit('destroy');
        $("#curGroups").tagit({
          placeholderText: "Comma separated roles",
          readOnly: ractive.hasRole('admin') ? false : true,
          afterTagAdded: function(event, ui) {
            if (ui.duringInitialization) return;
            else ractive.addUserToGroup(ui.tagLabel);
          },
          afterTagRemoved: function(event, ui) {
            ractive.deleteUserFromGroup(ui.tagLabel);
          }
        });

        $('#userPwdForm input[type="password"]').on('blur', function(ev) {
          ractive.updatePassword();
        });
        if (ractive.hasRole('admin')) $('.admin').show();
        ractive.set('saveObserver',true);
      });
    }
    ractive.hideResults();
    $('#currentSect').slideDown();
  },
  togglePwdFields: function() {
    console.info('togglePwdFields');
    if ($('#resetPwdBtn').text()=='Cancel') {
      $('#resetPwdBtn').empty().append('Reset Password');
      $('.pwdField').slideUp();
    } else {
      $('#resetPwdBtn').empty().append('Cancel');
      $('.pwdField').slideDown();
      $('.pwdField input')[0].focus();
      ractive.hideMessage();
    }
  },
  showResults: function() {
    console.log('showResults');
    $('#usersTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#usersTable').slideDown();
  },
  showSearchMatched: function() {
    ractive.set('searchMatched',$('#usersTable tbody tr').length);
    if ($('#usersTable tbody tr:visible').length==1) {
      var userId = $('#usersTable tbody tr:visible').data('href')
      var user = Array.findBy('id',ractive.localId(userId),ractive.get('users'))
      ractive.edit( user );
    }
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#usersTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#usersTable').slideToggle();
  },
  updatePassword: function () {
    console.log('updatePassword '+ractive.get('current')+' ...');
    if (document.getElementById('curPassword').value != document.getElementById('curPassword2').value) {
      document.getElementById('curPassword').setCustomValidity('Passwords must match.');
    } else if (document.getElementById('curPassword').value.trim().length <= 8 || document.getElementById('curPassword2').value.trim().length <= 8) {
      document.getElementById('curPassword').setCustomValidity('Passwords must be at least 8 characters.');
    } else {
      document.getElementById('curPassword').setCustomValidity('');
    }
    if (!document.getElementById('userPwdForm').checkValidity()) {
      ractive.showFormError('userForm','Please correct the highlighted fields');
      return ;
    }

    $('.pwdField').slideUp();
    $.ajax({
      url: ractive.getServer()+'/users/'+ractive.get('current.id')+'/reset-password',
      type: 'POST',
      data: { password: $('#curPassword').val(), password2:$('#curPassword2').val() },
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.showMessage('User password has been updated');
        ractive.fetch();
        setTimeout(function() { $('#currentSect').slideUp(); }, EASING_DURATION*4);
        ractive.showResults();
      }
    });
  }
});

ractive.observe('current.email', function(newValue, oldValue, keypath) {
  console.log('email changed from '+oldValue+' to '+newValue);
  if (newValue == undefined) return;
  ractive.set('current.email', newValue.toLowerCase().trim());
  newValue = newValue.toLowerCase().trim();
});
ractive.observe('current.id', function(newValue, oldValue, keypath) {
  console.log('id changed from '+oldValue+' to '+newValue);
  if (newValue == undefined) return;
  ractive.set('current.id', newValue.toLowerCase().trim());
  newValue = newValue.toLowerCase().trim();
});
