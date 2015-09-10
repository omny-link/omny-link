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
    stdPartials: [
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
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
          ractive.get('current.groups').merge({id:newGroup.toLowerCase(),name:newGroup});
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            alert("Bother: "+textStatus+':'+errorThrown);
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
      },
      error: errorHandler = function(jqXHR, textStatus, errorThrown) {
        ractive.showError("Bother: "+textStatus+':'+errorThrown);
      }
    });
  },
  select: function(user) { 
    ractive.set('saveObserver',false);
    $.getJSON('/users/'+user.id, function( data ) {
      console.log('found user '+JSON.stringify(data));
      ractive.set('current', data);
      ractive.set('saveObserver',true);
    });
    ractive.toggleResults();
    $('#currentSect').slideDown();
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#usersTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#usersTable').slideToggle();
  }
});


