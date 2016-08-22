var ractive = new AuthenticatedRactive({
  el: 'container',
  template: '#template',
  data: {
    helpUrl: 'http://omny.link/user-help/users/#the_title',
    matchRole: function(role) {
      console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
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
    sortAsc: true,
    sortColumn: 'fullName',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/user-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "userCurrentSect", "url": "/partials/user-current-sect.html"},
      { "name": "userListSect", "url": "/partials/user-list-sect.html"}
    ],
    title: 'User Management',
    username: localStorage['username'],
    users: []
  },
  addGroup: function () {
    console.log('addGroup...');
    $('#curNewGroup').removeClass('hidden').slideDown();
  },
  addUserToGroup: function () {
    console.log('addUserToGroup...');
    var newGroup = $('#curNewGroup').val();
    $('#curNewGroup').slideUp();

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
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            alert("Bother: "+textStatus+':'+errorThrown);
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
  initControls: function() { 
    console.log('initControls');
    ractive.initAutoComplete();
  },
  oninit: function() {
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save '+ractive.get('current')+' ...');
    if (!document.getElementById('userForm').checkValidity()) { 
      ractive.showFormError('userForm','Please correct the highlighted fields');
      return ;
    }
    $('#curUsername').val($('#curUsername').val().toLowerCase());
    
    $.ajax({
      url: ractive.getServer()+'/users/'+(ractive.get('currentAction') == 'CREATE' ? '' : ractive.get('current.id')),
      type: ractive.get('currentAction') == 'CREATE' ? 'POST' : 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('current')),
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.showMessage('User has been saved successfully');
        ractive.fetch();
        ractive.set('currentAction', 'UPDATE');
      }
    });
  },
  select: function(user) {
    if (user!=undefined && user['id']!=undefined) {
      ractive.set('saveObserver',false);
      $.getJSON(ractive.getServer()+'/users/'+user.id, function( data ) {
        console.log('found user '+JSON.stringify(data));
        ractive.set('current', data);
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

