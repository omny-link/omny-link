fadeOutMessages = false;
var ractive = new AuthenticatedRactive({
  el: 'container',
  template: '#template',
  data: {
    stdPartials: [
      { "name": "passwordSect", "url": "/partials/user-password-sect.html"}
    ],
    title: 'Change your password',
  },
  fetch: function () {
    console.log('fetch...');
    
    if (getSearchParameters()['uuid']==undefined) {
      ractive.showMessage('This reset code is invalid, please request another', 'alert-warning');
    } else {
      ractive.set('current.uuid', getSearchParameters()['uuid']);
      ractive.set('current.instanceId', getSearchParameters()['instanceId']);
      ractive.set('current.tenantId', getSearchParameters()['tenantId']);
    }
    
//    $.getJSON("/users/",  function( data ) {
//      ractive.merge('users', data);
//      if (ractive.hasRole('admin')) $('.admin').show();
//    });
  },
  initControls: function() { 
    console.log('initControls');
  },
  oninit: function() {
    this.loadStandardPartials(this.get('stdPartials'));
  },
  reset: function() {
    console.info('reset');
    if (document.getElementById('resetForm').checkValidity()) {
      $('#resetSect').slideUp();
      $('#loginSect').slideDown();
      var addr = $('#email').val();
      $.ajax({
        url: '/msg/omny/omny.resetPassword.json',
        type: 'POST',
        data: { json: JSON.stringify({ email: addr }) },
        dataType: 'text',
        success: function(data) {
          ractive.showMessage('An reset link has been sent to '+addr);
        },
      });
    } else {
      ractive.showError('Please enter the email address you registered with');
    }
  },
  updatePassword: function () {
    console.log('updatePassword '+ractive.get('current')+' ...');
    if (document.getElementById('curPassword').value != document.getElementById('curPassword2').value) {
      document.getElementById('curPassword').setCustomValidity('Passwords must match.');
    } else if (document.getElementById('curPassword').value.trim().length < 8 || document.getElementById('curPassword2').value.trim().length < 8) {
      document.getElementById('curPassword').setCustomValidity('Passwords must be at least 8 characters.');
    } else {
      document.getElementById('curPassword').setCustomValidity('');
    }
    if (!document.getElementById('userPwdForm').checkValidity()) { 
      ractive.showFormError('userForm','Please correct the highlighted fields');
      return ;
    }
    
    $('#userPwdForm').slideUp();
    
    $.ajax({
      url: '/'+ractive.get('current.tenantId')+'/messages/omny.resetPassword/'+ractive.get('current.instanceId'),
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(ractive.get('current')),
      dataType: 'text',
      success: function(data) {
        ractive.showMessage("Your password has been reset, we'll now redirect you to the login screen");
        setTimeout(function() { document.location.href = '/login' }, 2000);
      },
      error: function(jqXHR, textStatus, errorThrown) {
        var msg = "We can't find that reset code or perhaps it expired, please request another";
        console.error(jqXHR.status+' '+textStatus+':'+msg);
        $( "#ajax-loader" ).hide();
        ractive.showError(msg);
        $('#resetSect').slideDown();
      }
    });
  }
});
