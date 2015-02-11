var TRANSITION_DURATION = 500;
// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new Ractive({
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
    tasks: []
  },
  saveUser: function () {
    console.log('saveUser '+ractive.data.current+' ...');
    $('#currentSect').slideUp();
    $.ajax({
      url: '/tasks/'+(ractive.data.currentAction == 'CREATE' ? '' : ractive.data.current.id),
      type: ractive.data.currentAction == 'CREATE' ? 'POST' : 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(ractive.data.current),
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.fetch();
      },
      error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          alert("Bother: "+textStatus+':'+errorThrown);
      }
    });
  },
  edit: function(task) { 
    console.log('editUser '+task+' ...');
    $('.create-field').hide();
    ractive.set('current',task);
    ractive.select(task);
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON("/tasks/"+localStorage['username']+'/',  function( data ) {
      ractive.merge('tasks', data);
    });
  },
  fetchUserNotes: function () {
    console.log('fetchUserNotes...');
    $.getJSON("/tasks/"+ractive.data.current.id+'/notes',  function( data ) {
      ractive.merge('currentNotes', data);
  });
  },
  oninit: function() {
    this.fetch();
  },
  select: function(task) { 
    ractive.merge('current',task);
    ractive.fetchUserNotes();
    $('#currentSect').slideDown();
  },
});
