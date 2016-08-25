function getSearchParameters() {
  var prmstr = window.location.search.substr(1);
  return prmstr != null && prmstr != "" ? transformToAssocArray(prmstr) : {};
}

function transformToAssocArray( prmstr ) {
  var params = {};
  var prmarr = prmstr.split("&");
  for ( var i = 0; i < prmarr.length; i++) {
      var tmparr = prmarr[i].split("=");
      params[tmparr[0]] = tmparr[1];
  }
  return params;
}

/**
 * @return the unique values in an array of _simple_ types.
 */
function uniq(a) {
    var seen = {};
    var out = [];
    var len = a.length;
    var j = 0;
    for(var i = 0; i < len; i++) {
         var item = a[i];
         if(seen[item] !== 1) {
               seen[item] = 1;
               out[j++] = item;
         }
    }
    return out;
}

var $auth = new AuthHelper(); 
function AuthHelper() {
  this.getProfile = function(username) {
    console.log('getProfile: '+username);
    if (username) {
      $.getJSON(ractive.getServer()+'/users/'+username, function(profile) {
        ractive.set('profile',profile);
        $('.profile-img').empty().append('<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(ractive.get('profile.email'))+'?s=34"/>');
        if ($auth.hasRole('super_admin')) $('.super-admin').show();
        $auth.loadTenantConfig(ractive.get('profile.tenant'));
      })
      .error(function(jqXHR, textStatus, errorThrown) {
        console.warn('Failed to get profile, will rely on default');
        switch (jqXHR.status) {
        case 401:
        case 403:
        case 405: /* Could also be a bug but in production we'll assume a timeout */
          $auth.redirectToLogin();
          break;
        default:
          ractive.set('profile',{tenant:'omny'});
          $auth.loadTenantConfig(ractive.get('tenant.id'));
        }
      });
    } else if (ractive.get('tenant') && $auth.isPublic(window.location.href)) {
      var tenant = ractive.get('tenant.id');
      console.warn('... page supplied default tenant:'+tenant);
      $auth.loadTenantConfig(ractive.get('tenant.id'));
    } else {
      $auth.redirectToLogin();
    }
  }
  this.hasRole = function(role) {
    if (ractive && ractive.get('profile')) {
      var hasRole = ractive.get('profile').groups.filter(function(g) {return g.id==role})
      return hasRole!=undefined && hasRole.length>0;
    }
    return false;
  }
  this.isPublic = function(url) {
    if (url.indexOf('public')==-1 && url.indexOf('login')==-1) return false;
    else return true;
  }
  this.loadTenantConfig = function(tenant) {
    if (tenant==undefined || tenant=='undefined') $auth.redirectToLogin();
    console.info('loadTenantConfig:'+tenant);
    $.getJSON(ractive.getServer()+'/tenants/'+tenant+'.json', function(response) {
      //console.log('... response: '+JSON.stringify(response));
      ractive.set('saveObserver', false);
      ractive.set('tenant', response);
      ractive.applyBranding();
      ractive.set('saveObserver', true);
      if (ractive.tenantCallbacks!=undefined) ractive.tenantCallbacks.fire();
    });
  }
  this.redirectToLogin = function() {
    ractive.showWarning('Please login to access this page');
    setTimeout(function() { window.location.href='/'; }, 1000);
  }
}

$(document).ready(function() {
  ractive.set('saveObserver',false);

  ractive.observe('username', function(newValue, oldValue, keypath) {
    if (ractive['getProfile'] != undefined) ractive.getProfile();
  });
  
  if (typeof ractive['fetch'] == 'function') {
    if (ractive.tenantCallbacks==undefined) ractive.tenantCallbacks = $.Callbacks();
    ractive.tenantCallbacks.add(function() {
      ractive.fetch();
    });
  }
  if (ractive.brandingCallbacks==undefined) ractive.brandingCallbacks = $.Callbacks();
  ractive.brandingCallbacks.add(function() {
    ractive.initControls();
  });
  
  var s = getSearchParameters()['s'];
  if (s!=undefined) ractive.set('searchTerm',s);

  var id = getSearchParameters()['id'];
  if (id!=undefined) {
    ractive.set('searchId',id);
    if (ractive.fetchCallbacks==undefined) ractive.fetchCallbacks = $.Callbacks();
    ractive.fetchCallbacks.add(function() {
      ractive.edit(ractive.find(ractive.get('searchId')));
    });
  }
  
  ractive.set('saveObserver', true);
});
