var CSRF_COOKIE = 'XSRF-TOKEN';
var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;


/**
 * Extends Ractive to handle authentication for RESTful clients connecting
 * to Spring servers.
 * 
 * Also offers some standard controls like Typeahead and a re-branding mechanism.
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
      headers: { 'X-CSRF-TOKEN': this.getCookie(CSRF_COOKIE) },
      error: this.handleError
    });
  },
  applyBranding: function() {
    var tenant = ractive.get('profile').tenant;
    if (tenant != undefined) {
      $('link[rel="icon"]').attr('href',$('link[rel="icon"]').attr('href').replace('omny',tenant));
      //$('link[rel="stylesheet"]').attr('href',$('link[rel="stylesheet"]').attr('href').replace('omny',tenant));
      $('head').append('<link href="css/'+tenant+'-0.5.0.css" rel="stylesheet">');
      $('.navbar-brand').empty().append('<img src="/images/'+tenant+'-logo.png" alt="logo"/>');
      // ajax loader 
      $('body').append('<div id="ajax-loader"><img class="ajax-loader" src="images/'+tenant+'-ajax-loader.gif" alt="Loading..."/></div>');
      $( document ).ajaxStart(function() {
        $( "#ajax-loader" ).show();
      });
      $( document ).ajaxStop(function() {
        $( "#ajax-loader" ).hide();
      });
      if (ractive.get('tenant.showPoweredBy')!=false) {
        // powered by 
        $('body').append('<div class="powered-by"><h1><span class="powered-by-text">powered by</span><img src="images/omny-greyscale-inline-logo.png" alt="powered by Omny Link"/></h1></div><p class="beta bg-warning pull-right">Beta!</p>');
      }
      // tenant partial templates
      $.each(ractive.get('tenant').partials, function(i,d) {
        $.get(d.url, function(response){
          //console.log('response: '+response)
          ractive.resetPartial(d.name,response);
        });
      });
      if (ractive.brandingCallbacks!=undefined) ractive.brandingCallbacks.fire();
    }
  },
  getCookie: function(name) {
    //console.log('getCookie: '+name)
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  },
  getProfile: function() {
    console.log('getProfile: '+this.get('username'));
    var ractive = this;
    if (this && this.get('username')) $.getJSON('/users/'+ractive.get('username'), function(profile) {
      ractive.set('profile',profile);
      if (ractive.hasRole('super_admin')) $('.admin').show();
      ractive.loadTenantConfig(ractive.get('profile.tenant'));
    });
    else this.showError('You are not logged in, some or all functionality will be unavailable.');
  },
  handleError: function(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) { 
    case 401:
    case 403: 
      this.showError("Session expired, please login again");
      window.location.href='/login';
      break; 
    default: 
      var msg = "Bother! Something has gone wrong: "+textStatus+':'+errorThrown;
      console.error('msg:'+msg);
      this.showError(msg);        
    }
  },
  hasRole: function(role) {
    var ractive = this;
    if (this && this.get('profile')) {
      var hasRole = ractive.get('profile').groups.filter(function(g) {return g.id==role})
      return hasRole!=undefined && hasRole.length>0;
    }
    return false;
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    if (ractive.get('tenant.typeaheadControls')!=undefined && ractive.get('tenant.typeaheadControls').length>0) {
      $.each(ractive.get('tenant.typeaheadControls'), function(i,d) {
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
    }
  },
  initAutoNumeric: function() { 
    if ($('.autoNumeric')!=undefined && $('.autoNumeric').length>0) {
      $('.autoNumeric').autoNumeric('init', {});
    }
  },
  initControls: function() { 
    console.log('initControls');
    ractive.initAutoComplete();
    ractive.initAutoNumeric();
    ractive.initDatepicker();
  },
  initDatepicker: function() {
    console.log('initDatepicker');
    if ($('.datepicker')!=undefined && $('.datepicker').length>0) {
      $('.datepicker').datepicker({
        format: "dd/mm/yyyy",
        autoclose: true,
        todayHighlight: true
      });
    }
  },
  loadTenantConfig: function(tenant) {
    console.log('loadTenantConfig:'+tenant);
    $.getJSON('/tenants/'+tenant+'.json', function(response) {
      console.log('... response: '+response);
      ractive.set('tenant', response);
      $.ajaxSetup({
        headers: {'X-Tenant': ractive.get('tenant.id')}
      });
      ractive.applyBranding();
      if (ractive.tenantCallbacks!=undefined) ractive.tenantCallbacks.fire(); 
    });
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

// TODO remove the redundancy of having this in AuthenticatedRactive and here
function getCookie(name) {
  //console.log('getCookie: '+name)
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

