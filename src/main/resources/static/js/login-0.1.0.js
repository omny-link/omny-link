var CSRF_COOKIE = 'XSRF-TOKEN';

/**
 * Extends Ractive to handle authentication for RESTful clients connecting
 * to Spring servers.
 *
 * Form names expected:
 *   loginForm
 *   logoutForm
 */
var AuthenticatedRactive = Ractive.extend({
  CSRF_TOKEN: 'XSRF-TOKEN',
  ajaxSetup: function() {
    console.log('ajaxSetup: '+this);
    $.ajaxSetup({
      username: localStorage['username'],
      password: localStorage['password'],
      headers: { 'X-CSRF-TOKEN': this.getCookie(CSRF_COOKIE) }
    });
  },
  getCookie: function(name) {
    //console.log('getCookie: '+name)
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  },
  getProfile: function() {
    console.log('getProfile: '+ractive.get('username'));
    if (ractive && ractive.data['username']) $.getJSON('/users/'+ractive.get('username'), function(profile) {
      ractive.set('profile',profile);
      if (ractive.hasRole('ADMIN')) $('.admin').show();
    });
    else console.debug('Not logged in, skipping profile');
  },
  hasRole: function(role) {
    if (ractive && ractive.get('profile'))
      return ractive.get('profile').groups.filter(function(g) {return g.id==role})!=undefined;
    return false;
  },
  login: function() {
    console.log('login');
    if (!document.forms['loginForm'].checkValidity()) {
      // TODO message
      return false;
    }
    localStorage['username'] = $('#username').val();
    localStorage['password'] = $('#password').val();
    document.forms['loginForm'].submit();
  },
  logout: function() {
    localStorage['username'] = null;
    localStorage['password'] = null;
    document.cookie = this.CSRF_COOKIE+'=;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    document.forms['logoutForm'].submit();
  }
});


function getCookie(name) {
  //console.log('getCookie: '+name)
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}
