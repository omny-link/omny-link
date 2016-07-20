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
  addResource: function () {
    console.log('addResource...');
    //$('#upload fieldset').append($('#resourceControl').html());
    $("#file").click();
  },
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
    if (ractive.get('profile')==undefined) return ;
    var tenant = ractive.get('profile').tenant;
    if (tenant != undefined) {
      $('link[rel="icon"]').attr('href',$('link[rel="icon"]').attr('href').replace('omny',tenant));
      $('head').append('<link href="/css/'+tenant+'-1.0.0.css" rel="stylesheet">');
      $('.navbar-brand').empty().append('<img src="/images/'+tenant+'-logo.png" alt="logo"/>');
      // ajax loader 
      $( "#ajax-loader" ).remove();
      $('body').append('<div id="ajax-loader"><img class="ajax-loader" src="/images/omny-ajax-loader.gif" style="width:10%" alt="Loading..."/></div>');
      $( document ).ajaxStart(function() {
        $( "#ajax-loader" ).show();
      });
      $( document ).ajaxStop(function() {
        $( "#ajax-loader" ).hide();
      });
      ractive.initContentEditable();// required here for the tenant switcher
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
  entityName: function(entity) {
    console.info('entityName');
    var id = ractive.uri(entity);
    var lastSlash = id.lastIndexOf('/');
    return id.substring(id.lastIndexOf('/', lastSlash-1)+1, lastSlash);
  },
  fetchDocs: function() {
    $.getJSON(ractive.uri(ractive.get('current'))+'/documents',  function( data ) {
      if (data['_embedded'] != undefined) {
        console.log('found docs '+data);
        ractive.merge('current.documents', data['_embedded'].documents);
        // sort most recent first
        ractive.get('current.documents').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
      ractive.set('saveObserver',true);
    });
  },
  fetchNotes: function() {
    $.getJSON(ractive.uri(ractive.get('current'))+'/notes',  function( data ) {
      if (data['_embedded'] != undefined) {
        console.log('found notes '+data);
        ractive.merge('current.notes', data['_embedded'].notes);
        // sort most recent first
        ractive.get('current.notes').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
    });
  },
  getCookie: function(name) {
    //console.log('getCookie: '+name)
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  },
  /** @deprecated use uri() */
  getId: function(entity) {
    return ractive.uri(entity);
  },
  getProfile: function() {
    console.log('getProfile: '+this.get('username'));
    if ($auth.isPublic(window.location.href) && ractive.get('tenant')!=undefined) {
      var tenant = ractive.get('tenant.id');
      console.warn('... page supplied default tenant:'+tenant);
      $auth.loadTenantConfig(ractive.get('tenant.id'));
    } else if ($auth.isPublic(window.location.href)) {
      console.error('Page must supply default tenant if intended for public use');
    } else {
      $auth.getProfile(this.get('username'));
    }
  },
  getServer: function() {
    return ractive.get('server')==undefined ? '' : ractive.get('server');
  },
  handleError: function(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) {
    case 0:
      var msg = 'Unable to connect, retrying...';
      ractive.showMessage(msg, 'alert-warning');
      break; 
    case 400:
      var msg = jqXHR.responseJSON == null ? textStatus+': '+errorThrown : errorThrown+': '+jqXHR.responseJSON.message;
      ractive.showError(msg);
      break; 
    case 401:
    case 403: 
    case 405: /* Could also be a bug but in production we'll assume a timeout */ 
      ractive.showError("Session expired, please login again");
      window.location.href='/login';
      break; 
    case 404: 
      var path ='';
      if (jqXHR.responseJSON != undefined) {
        path = " '"+jqXHR.responseJSON.path+"'";
      }
      var msg = "That's odd, we can't find the page"+path+". Please let us know about this message";
      console.error('msg:'+msg);
      ractive.showError(msg);
      break;
    default: 
      var msg = "Bother! Something has gone wrong (code "+jqXHR.status+"): "+textStatus+':'+errorThrown;
      console.error('msg:'+msg);
      $( "#ajax-loader" ).hide();
      ractive.showError(msg);
    }
  },
  hash: function(email) {
    if (email==undefined) return email; 
    return hex_md5(email.trim().toLowerCase());
  },
  hasRole: function(role) {
    var ractive = this;
    if (this && this.get('profile')) {
      var hasRole = ractive.get('profile').groups.filter(function(g) {return g.id==role})
      return hasRole!=undefined && hasRole.length>0;
    }
    return false;
  },
  hideMessage: function() {
    $('#messages').hide();
  },
  hideUpload: function () {
    console.log('hideUpload...');
    $('#upload').slideUp();
  },
  id: function(entity) {
    // TODO switch to modular vsn
    console.log('id: '+entity);
    var id = ractive.uri(entity);
    return id.substring(id.lastIndexOf('/')+1);
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    if (ractive.get('tenant.typeaheadControls')!=undefined && ractive.get('tenant.typeaheadControls').length>0) {
      $.each(ractive.get('tenant.typeaheadControls'), function(i,d) {
        //console.log('binding ' +d.url+' to typeahead control: '+d.selector);
        $.get(ractive.getServer()+d.url, function(data){
          if (d.name!=undefined) ractive.set(d.name,data); 
          $(d.selector).typeahead({ items:'all',minLength:0,source:data });
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
  initContentEditable: function() {
    console.log('initContentEditable');
    $("[contenteditable]").focus(function() { 
      console.log('click '+this.id);
      selectElementContents(this);
    });
  },
  initControls: function() { 
    console.log('initControls');
    ractive.initAutoComplete();
    ractive.initAutoNumeric();
    ractive.initDatepicker();
    ractive.initContentEditable();
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
  initTags: function() {
    console.info('initTags');
    $('[data-bind]').each(function(i,d) {
      $(d).val(ractive.get($(d).data('bind'))).css('display','none');
    });

    if ($(".tag-ctrl").is(":ui-tagit")) $(".tag-ctrl").tagit('destroy');
    $(".tag-ctrl").tagit({
      placeholderText: "Comma separated tags",
      afterTagAdded: function(event, ui) {
        ractive.set($(event.target).data('bind'),$(event.target).val());
      },
      afterTagRemoved: function(event, ui) {
        ractive.set($(event.target).data('bind'),$(event.target).val());
      }
    });
  },
  loadStandardPartials: function(stdPartials) {
    console.info('loadStandardPartials');
    $.each(stdPartials, function(i,d) {
      //console.log('loading...: '+d.name)
      $.get(d.url, function(response){
        //console.log('... loaded: '+d.name)
        //console.log('response: '+response)
        if (ractive != undefined) {
          try {
            ractive.resetPartial(d.name,response);
          } catch (e) {
            console.error('Unable to reset partial '+d.name+': '+e);
          }
        }
      });
    });
  },
  login: function() {
    console.info('login');
    if (!document.forms['loginForm'].checkValidity()) {
      ractive.showMessage('Please provide both username and password');
      return false;
    }
    $('#username').val($('#username').val().toLowerCase());
    localStorage['username'] = $('#username').val();
    localStorage['password'] = $('#password').val();
    if (window.ua) {
      ua.login($('#username').val());
    }
    document.forms['loginForm'].submit();
  },
  logout: function() {
    delete localStorage['username'];
    delete localStorage['password'];
    document.cookie = this.CSRF_COOKIE+'=;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    // IE returns collection; Chrome and others the first
    if (document.forms['logoutForm'].length>1) document.forms['logoutForm'][0].submit();
    else document.forms['logoutForm'].submit();
  },
  saveDoc: function () {
    console.log('saveDoc '+JSON.stringify(ractive.get('current.doc'))+' ...');
    var n = ractive.get('current.doc');
    var url = ractive.uri(ractive.get('current'))+'/documents';
    url = url.replace(ractive.entityName(ractive.get('current')),ractive.get('tenant.id')+'/'+ractive.entityName(ractive.get('current')));
    if (n.url.trim().length > 0) {
      $('#docsTable tr:nth-child(1)').slideUp();
      $.ajax({
        /*url: '/documents',
        contentType: 'application/json',*/
        url: url,
        type: 'POST',
        data: n,
        success: completeHandler = function(data) {
          console.log('data: '+ data);
          ractive.showMessage('Document link saved successfully');
          ractive.fetchDocs();
          $('#doc').val(undefined);
        }
      });
    }
  },
  saveNote: function(n) {
    console.info('saveNote '+JSON.stringify(ractive.get('current.note'))+' ...');
    /// TODO this is temporary for backwards compatiblity with older workflow forms
    if (n == undefined) {
      n = ractive.get('current.note');
      n.content = $('#note').val();
    }
    n.contact = ractive.uri(ractive.get('current'));
    var url = ractive.uri(ractive.get('current'))+'/notes';
    url = url.replace(ractive.entityName(ractive.get('current')),ractive.get('tenant.id')+'/'+ractive.entityName(ractive.get('current')));
    console.log('  url:'+url);
    if (n.content != undefined && n.content.trim().length > 0) {
//      $('#notesTable tr:nth-child(1)').slideUp();
      $.ajax({
        url: url,
        type: 'POST',
        data: n,
        success: completeHandler = function(data) {
          console.log('response: '+ data);
          ractive.showMessage('Note saved successfully');
          ractive.fetchNotes();
          $('#note').val(undefined);
        }
      });
    }
  },
  showError: function(msg) {
    this.showMessage(msg, 'bg-danger text-danger');
  },
  showFormError: function(formId, msg) {
    this.showError(msg);
    var selector = formId==undefined || formId=='' ? ':invalid' : '#'+formId+' :invalid';
    $(selector).addClass('field-error');
    $(selector)[0].focus();
  },
  showMessage: function(msg, additionalClass) {
    console.log('showMessage: '+msg);
    if (additionalClass == undefined) additionalClass = 'bg-info text-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages').empty().append(msg).removeClass().addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='bg-danger text-danger') setTimeout(function() {
      $('#messages').fadeOut();
    }, EASING_DURATION*10);
    else $('#messages').append('<span class="text-danger pull-right glyphicon glyphicon-remove" onclick="ractive.hideMessage()"></span>');
  },
  showWarning: function(msg) {
    this.showMessage(msg, 'alert-warning');
  },
  showUpload: function () {
    console.log('showUpload...');
    $('#upload').slideDown();
  },
  sortChildren: function(childArray, sortBy, asc) {
    console.info('sortChildren');
    ractive.get('current.'+childArray).sort(function(a,b) {
      if (a[sortBy] > b[sortBy]) {
        return asc ? 1 : -1;
      }
      if (a[sortBy] < b[sortBy]) {
        return asc ? -1 : 1;
      }
      // a must be equal to b
      return 0;
    });
  },
  stripProjection: function(link) {
    // TODO switch to modularized version
    if (link==undefined) return;
    var idx = link.indexOf('{projection');
    if (idx==-1) {
      idx = link.indexOf('{?projection');
      if (idx==-1) {
        return link;
      } else {
        return link.substring(0,idx);
      }
    } else {
      return link.substring(0,idx);
    }
  },
  switchToTenant: function(tenant) {
    if (tenant==undefined || typeof tenant != 'string') {
      return false;
    }
    console.log('switchToTenant: '+tenant);
    $.ajax({
      method: 'PUT',
      url: ractive.getServer()+"/admin/tenant/"+ractive.get('username')+'/'+tenant,
      success: function() {
        window.location.reload();
      }
    })
  },
  tenantUri: function(entity) {
    console.log('tenantUri: '+entity);
    var uri = ractive.uri(entity);
    if (uri != undefined && uri.indexOf(ractive.get('tenant.id')+'/')==-1) {
      uri = uri.replace(ractive.get('entityPath'),'/'+ractive.get('tenant.id')+ractive.get('entityPath'));
    }
    return uri;
  },
  toggleSection: function(sect) {
    console.info('toggleSection: '+$(sect).attr('id'));
    $('#'+$(sect).attr('id')+'>div').toggle();
    $('#'+$(sect).attr('id')+' .ol-collapse').toggleClass('glyphicon-triangle-right').toggleClass('glyphicon-triangle-bottom');
  },
  toggleSidebar: function() {
    console.info('toggleSidebar');
    $('.omny-bar-left').toggle(EASING_DURATION);
  },
  upload: function(formId) {
    console.log('upload, id: '+formId);
    var formElement = document.getElementById(formId);
    var formData = new FormData(formElement);
    return $.ajax({
        type: 'POST',
        url: formElement.action,
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
          console.log('successfully uploaded resource');
          ractive.fetch();
          ractive.hideUpload();
        }
    });
  },
  uri: function(entity) {
    // TODO switch to use modularized version
    console.log('uri: '+entity);
    var uri;
    if (entity['links']!=undefined) {
      $.each(entity.links, function(i,d) {
        if (d.rel == 'self') {
          uri = d.href;
        }
      });
    } else if (entity['_links']!=undefined) {
      uri = ractive.stripProjection(entity._links.self.href);
    }
    // work around for sub-dir running
    if (uri != undefined && uri.indexOf(ractive.getServer())==-1 && uri.indexOf('//')!=-1) {
      uri = ractive.getServer() + uri.substring(uri.indexOf('/', uri.indexOf('//')+2));
    } else if (uri != undefined && uri.indexOf('//')==-1) {
      uri = ractive.getServer()+uri;
    }

    return uri;
  }
});

$( document ).ajaxError(function( event, request, settings ) {
  // retry indefinitely
//  if(!settings.secondExec) {
//    settings.secondExec = true;
    setTimeout(function() {
      $.ajax(settings); 
    }, EASING_DURATION*5);
//  }
});

$( document ).bind('keypress', function(e) {
  switch (e.keyCode) {
  case 13: // Enter
    if (window['ractive'] && ractive['enter']) ractive['enter']();
    break; 
  case 63:   // ?
    console.log('help requested');
    $('#helpModal').modal({});
    break;
  }
});


// TODO remove the redundancy of having this in AuthenticatedRactive and here
function getCookie(name) {
  //console.log('getCookie: '+name)
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

function selectElementContents(el) {
  var range = document.createRange();
  range.selectNodeContents(el);
  var sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(range);
}

/******************************** Polyfills **********************************/
// ref https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith
if (!String.prototype.endsWith) {
  String.prototype.endsWith = function(searchString, position) {
      var subjectString = this.toString();
      if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
        position = subjectString.length;
      }
      position -= searchString.length;
      var lastIndex = subjectString.indexOf(searchString, position);
      return lastIndex !== -1 && lastIndex === position;
  };
}