var ractive = new AuthenticatedRactive({
  el: 'container',
  template: '#template',
  data: {
    stdPartials: [
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
        url: '/users/'+ractive.get('current.id')+'/groups/'+newGroup,
        type: 'POST',
        contentType: 'application/json',
        success: completeHandler = function(data) {
          console.log('data: '+ data);
          ractive.merge('current.groups', {id:newGroup.toLowerCase(),name:newGroup});
        }
      });
  },
  addUser: function (user) {
    console.log('addUser '+user+' ...');
    ractive.set('currentAction', 'CREATE');
    ractive.set('current', { });
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
    $.getJSON("/users/",  function( data ) {
      ractive.merge('users', data);
    });
  },
  fetchUserGroups: function () {
    console.log('fetchUserGroups...');
    $.getJSON("/users/"+ractive.get('current.id')+'/groups',  function( data ) {
      ractive.merge('currentGroups', data);
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
    
    $.ajax({
      url: '/users/'+(ractive.get('currentAction') == 'CREATE' ? '' : ractive.get('current.id')),
      type: ractive.get('currentAction') == 'CREATE' ? 'POST' : 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('current')),
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.showMessage('User has been saved successfully');
        ractive.fetch();
        setTimeout(function() { $('#currentSect').slideUp(); }, EASING_DURATION*4);
      }
    });
  },
  select: function(user) {
    if (user!=undefined && user['id']!=undefined) {
      ractive.set('saveObserver',false);
      $.getJSON('/users/'+user.id, function( data ) {
        console.log('found user '+JSON.stringify(data));
        ractive.set('current', data);
        $('#userPwdForm input[type="password"]').on('blur', function(ev) {
          ractive.updatePassword();
        });
        ractive.set('saveObserver',true);
      });
    }
    ractive.toggleResults();
    $('#currentSect').slideDown();
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
    } else {
      document.getElementById('curPassword').setCustomValidity('');
    }
    if (!document.getElementById('userPwdForm').checkValidity()) { 
      ractive.showFormError('userForm','Please correct the highlighted fields');
      return ;
    }
    
    $.ajax({
      url: '/users/'+ractive.get('current.id')+'/reset-password',
      type: 'POST',
      data: { password: $('#curPassword').val(), password2:$('#curPassword2').val() },
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.showMessage('User password has been updated');
        ractive.fetch();
        setTimeout(function() { $('#currentSect').slideUp(); }, EASING_DURATION*4);
      }
    });
  }
});
