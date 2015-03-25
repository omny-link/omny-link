var TRANSITION_DURATION = 500;
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
          ractive.fetchUserGroups();
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
    ractive.select(ractive.get('current'));
  },
  saveUser: function () {
    console.log('saveUser '+ractive.get('current')+' ...');
    $('#currentSect').slideUp();
    $.ajax({
      url: '/users/'+(ractive.get('currentAction') == 'CREATE' ? '' : ractive.get('current.id')),
      type: ractive.get('currentAction') == 'CREATE' ? 'POST' : 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('current')),
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.fetch();
      },
      error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          alert("Bother: "+textStatus+':'+errorThrown);
      }
  });
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
  oninit: function() {
    this.ajaxSetup();
    this.fetch();
  },
  select: function(user) { 
    ractive.merge('current',user);
    ractive.fetchUserGroups();
    $('#currentSect').slideDown();
  },
});
