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
  },
  reset: function() {
    console.info('reset');
    if (document.getElementById('resetForm').checkValidity()) {
      $('#resetSect').slideUp();
      $('#loginSect').slideDown();
      var addr = $('#email').val();
      $.ajax({
        url: '/msg/omny/omny.passwordResetRequest.json',
        type: 'POST',
        data: { json: JSON.stringify({ email: addr, tenantId: 'omny' }) },
        dataType: 'text',
        success: function(data) {
          ractive.showMessage('An reset link has been sent to '+addr);
        },
      });
    } else {
      ractive.showError('Please enter the email address you registered with');
    }
  },
  showReset: function() {
    $('#loginSect').slideUp();
    $('#resetSect').slideDown();
  }
});
