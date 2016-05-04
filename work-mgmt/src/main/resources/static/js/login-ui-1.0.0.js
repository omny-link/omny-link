var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    username: localStorage['username'],
  },
  fetch: function() {
    if (ractive.hasRole('admin')) $('.admin').show();
    if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
  },
  enter: function () {
    console.log('enter...');
    ractive.login();
  }
});
