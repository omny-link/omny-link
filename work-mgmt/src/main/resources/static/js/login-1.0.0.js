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
  addDataList: function(d, data) {
    $('datalist#'+d.name).remove();
    $('body').append('<datalist id="'+d.name+'">');
    $.each(data, function (i,e) {
      $('datalist#'+d.name).append('<option value="'+e.name+'">'+e.name+'</option>');
    });
  },
  ajaxSetup: function() {
    console.log('ajaxSetup: '+this);
    $.ajaxSetup({
      username: localStorage['username'],
      password: localStorage['password'],
      headers: { 'X-CSRF-TOKEN': this.getCookie(CSRF_COOKIE) }
    });
  },
  applyBranding: function() {
    if (ractive.get('profile')==undefined) return ;
    var tenant = ractive.get('profile').tenant;
    if (tenant != undefined) {
      $('head').append('<link href="/css/'+tenant+'-1.0.0.css" rel="stylesheet">');
      if (ractive.get('tenant.theme.logoUrl')!=undefined) {
        $('.navbar-brand').empty().append('<img src="'+ractive.get('tenant.theme.logoUrl')+'" alt="logo"/>');
      }
      if (ractive.get('tenant.theme.iconUrl')!=undefined) {
          $('link[rel="icon"]').attr('href',ractive.get('tenant.theme.iconUrl'));
      }
      if (ractive.get('tenant.show.activityTracker') && ua!=undefined) ua.enabled = true;
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
//          console.log('partial '+d.url+' response: '+response);
          try {
            ractive.resetPartial(d.name,response);
          } catch (e) {
            console.error('Unable to reset partial '+d.name+': '+e);
          }
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
        //console.log('found docs '+data);
        if (ractive.get('current.documents')==undefined) ractive.set('current.documents', []);
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
        //console.log('found notes '+data);
        if (ractive.get('current.notes')==undefined) ractive.set('current.notes', []);
        ractive.merge('current.notes', data['_embedded'].notes);
        // sort most recent first
        ractive.get('current.notes').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
    });
  },
  fetchStockItems: function() {
    console.info('fetchStockItems...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType : "json",
      url : ractive.getServer() + '/' + ractive.get('tenant.id') + '/stock-items/',
      crossDomain : true,
      success : function(data) {
        if (data['_embedded'] != undefined) {
          data = data['_embedded'].stockItems;
        }
        ractive.set('stockItems', data);
        console.log('fetched ' + data.length + ' stock items');
        var stockItemData = jQuery.map(data, function(n, i) {
          return ({
            "id": ractive.getId(n),
            "name": n.name
          });
        });
        ractive.set('stockItemsTypeahead', stockItemData);
        if (ractive['initStockItemTypeahead']!= undefined) ractive.initStockItemTypeahead();

        // HTML5 style
        $('datalist#stockItems').remove();
        $('body').append('<datalist id="stockItems">');
        $.each(ractive.get('stockItems'), function (i,d) {
          $('datalist#stockItems').append('<option value="'+d.name+'">'+d.name+'</option>');
        });

        ractive.set('saveObserver', true);
      }
    });
  },
  getCookie: function(name) {
    //console.log('getCookie: '+name)
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  },
  getStockItemNames: function(order) {
    var stockItemIds = [];
    if (order == undefined 
        || (!ractive.get('tenant.show.orderItems') && order['stockItem'] == undefined) 
        ||  (ractive.get('tenant.show.orderItems') && (order['orderItems'] == undefined || order.orderItems.length==0))) return;
    else if (!ractive.get('tenant.show.orderItems')) stockItemIds.push(order.stockItem.selfRef.substring(order.stockItem.selfRef.lastIndexOf('/')+1));
    else {
      for (idx in order.orderItems) {
        stockItemIds.push(order.orderItems[idx].customFields['stockItemId']);
      }
    }
    var stockItemNames = '';
    stockItemIds = uniq(stockItemIds);
    for (idx in stockItemIds) {
      var tmp = Array.findBy('selfRef','/stock-items/' + stockItemIds[idx],ractive.get('stockItems'));
      if (tmp != undefined) {
        if (stockItemNames.length > 0)
          stockItemNames += ',';
        stockItemNames += tmp.name;
      }
    }
    return stockItemNames;
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
  getStageName: function(idx) {
    console.log('getStageName: '+idx);
    var rtn = '';
    $.each(ractive.get('stages'), function(i,d) {
      if (parseInt(d['idx'])==idx) rtn = d.name;
    });
    return rtn;
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
    $('#messages, .messages').hide();
  },
  hideUpload: function () {
    console.log('hideUpload...');
    $('#upload').slideUp();
  },
  id: function(entity) {
    // TODO switch to modular vsn
    //console.log('id: '+entity);
    var id = ractive.uri(entity);
    return id.substring(id.lastIndexOf('/')+1);
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    if (ractive.get('tenant.typeaheadControls')!=undefined && ractive.get('tenant.typeaheadControls').length>0) {
      $.each(ractive.get('tenant.typeaheadControls'), function(i,d) {
        //console.log('binding ' +d.url+' to typeahead control: '+d.selector);
        if (d.url==undefined) {
          ractive.initTypeaheadControl(d,d.values);
          ractive.addDataList(d,d.values);
        } else {
          $.get(ractive.getServer()+d.url, function(data) {
            ractive.initTypeaheadControl(d,data);
            ractive.addDataList(d,data);
          },'json');
        }
      });
    }
  },
  /**
   * @deprecated Use html 5 datalist going forward.
   */
  initTypeaheadControl: function(d, data) {
    if (d.name!=undefined) ractive.set(d.name,data);
    $(d.selector).typeahead({ items:'all',minLength:0,source:data });
    $(d.selector).on("click", function (ev) {
      newEv = $.Event("keydown");
      newEv.keyCode = newEv.which = 40;
      $(ev.target).trigger(newEv);
      return true;
    });
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
  loadStandardPartial: function(name,url) {
    //console.log('loading...: '+d.name)
      $.get(url, function(response){
        //console.log('... loaded: '+d.name)
        //console.log('response: '+response)
        if (ractive != undefined) {
          try {
            ractive.resetPartial(name,response);
          } catch (e) {
            console.error('Unable to reset partial '+name+': '+e);
          }
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
  parseDate: function(timeString) {
    var d = new Date(timeString);
    // IE strikes again
    if (d == 'Invalid Date') d = parseDateIEPolyFill(timeString);
    return d;
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
  shortId: function(uri) {
    return uri.substring(uri.lastIndexOf('/')+1);
  },
  showDisconnected: function(msg) {
    console.log('showDisconnected: '+msg);
    if ($('#connectivityMessages.alert-info').length>0) {
      ; // Due to ordering of methods, actually reconnected now
    } else {
      $('#connectivityMessages').remove();
      $('body').append('<div id="connectivityMessages" class="alert-warning">'+msg+'</div>').show();
    }
  },
  showError: function(msg) {
    this.showMessage(msg, 'alert-danger');
  },
  showFormError: function(formId, msg) {
    this.showError(msg);
    var selector = formId==undefined || formId=='' ? ':invalid' : '#'+formId+' :invalid';
    $(selector).addClass('field-error');
    $(selector)[0].focus();
  },
  showHelp: function() {
    console.info('showHelp');
    $('iframe.helpContent')
        .attr('src',ractive.get('helpUrl'))
        .prop('height', window.innerHeight*0.8);
    $('#helpModal').modal({});
  },
  showMessage: function(msg, additionalClass) {
    console.log('showMessage: '+msg);
    if (additionalClass == undefined) additionalClass = 'alert-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages, .messages').empty().append(msg).removeClass().addClass('messages').addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='alert-danger' && additionalClass!='alert-warning') {
      setTimeout(function() {
        $('#messages, .messages').fadeOut();
      }, EASING_DURATION*10);
    } else {
      $('#messages, .messages').append('<span class="text-danger pull-right glyphicon glyphicon-remove" onclick="ractive.hideMessage()"></span>');
    }
  },
  showReconnected: function() {
    console.log('showReconnected');
    if ($('#connectivityMessages:visible').length>0) {
      $('#connectivityMessages').remove();
      $('body').append('<div id="connectivityMessages" class="alert-info">Reconnected</div>').show();
      setTimeout(function() {
        $('#connectivityMessages').fadeOut();
      }, EASING_DURATION*10);
    }
  },
  showWarning: function(msg) {
    this.showMessage(msg, 'alert-warning');
  },
  showUpload: function () {
    console.log('showUpload...');
    $('#upload').slideDown();
  },
  showSocial: function(networkName, keypath) {
    console.log('showSocial: '+networkName);
    ractive.set('network', { name: networkName, keypath: keypath, value: ractive.get(keypath) });
    $('#socialModalSect').modal('show');
  },
  submitSocial: function(network) {
    console.log('submitSocial: '+network);
    ractive.set(network.keypath,ractive.get('network.value'));
    $('#socialModalSect').modal('hide');
  },
  sortChildren: function(childArray, sortBy, asc) {
    console.info('sortChildren');
    if (ractive.get('current.'+childArray)==undefined) return 0;
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
  startCustomAction: function(key, label, object, form, businessKey) {
    console.log('startCustomAction: '+key+(object == undefined ? '' : ' for '+object.id));
    var instanceToStart = {
        processDefinitionId: key,
        businessKey: businessKey == undefined ? label : businessKey,
        label: label,
        processVariables: {
          initiator: ractive.get('username'),
          tenantId: ractive.get('tenant.id')
        }
      };
    if (object != undefined) {
      var singularEntityName = ractive.entityName(object).toCamelCase().singular();
      instanceToStart.processVariables[singularEntityName+'Id'] = ractive.uri(object);
      instanceToStart.processVariables[singularEntityName+'ShortId'] = ractive.shortId(ractive.uri(object));
    }
    console.log(JSON.stringify(instanceToStart));
    // save what we know so far...
    ractive.set('instanceToStart',instanceToStart);
    ractive.initAutoComplete();
    if (form == undefined || !form) {
      // ... and submit
      ractive.submitCustomAction();
    } else {
      // ... or display form, override submit handler with $('#submitCustomActionForm').off('click').on('click',function)
      $('#submitCustomActionForm').on('click', ractive.submitCustomAction);
      $('#customActionModalSect').modal('show');
    }
  },
  submitCustomAction: function() {
    console.info('submitCustomAction');
    if (document.getElementById('customActionForm').checkValidity()) {
      $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(ractive.get('instanceToStart')),
        success: completeHandler = function(data, textStatus, jqXHR) {
          console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
          ractive.showMessage('Started workflow "'+ractive.get('instanceToStart.label')+'" for '+ractive.get('instanceToStart.businessKey'));
          $('#customActionModalSect').modal('hide');
          if (document.location.href.endsWith('contacts.html')) {
            ractive.select(ractive.get('current'));// refresh individual record
          } else {
            ractive.fetch(); // refresh list
          }
          if (ractive.customActionCallbacks!=undefined) ractive.customActionCallbacks.fire();
          // cleanup ready for next time
          $('#submitCustomActionForm').off('click');
        },
      });
    } else {
      ractive.showFormError('customActionForm','Please correct the highlighted fields');
    }
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
  tenantUri: function(entity, entityPath) {
    //console.log('tenantUri: '+entity);
    var uri = ractive.uri(entity);
    if (entityPath===undefined) entityPath = ractive.get('entityPath');
    if (uri != undefined && uri.indexOf(ractive.get('tenant.id')+'/')==-1) {
      uri = uri.replace(entityPath,'/'+ractive.get('tenant.id')+entityPath);
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
    //console.log('uri: '+entity);
    var saveObserver = ractive.get('saveObserver');
    ractive.set('saveObserver', false);
    var uri;
    if (entity['links']!=undefined) {
      $.each(entity.links, function(i,d) {
        if (d.rel == 'self') {
          uri = d.href;
        }
      });
    } else if (entity['_links']!=undefined) {
      uri = ractive.stripProjection(entity._links.self.href);
    } else if (entity['id']!=undefined) {
      uri = ractive.get('entityPath')+'/'+entity.id;
    }
    // work around for sub-dir running
    if (uri != undefined && uri.indexOf(ractive.getServer())==-1 && uri.indexOf('//')!=-1) {
      uri = ractive.getServer() + uri.substring(uri.indexOf('/', uri.indexOf('//')+2));
    } else if (uri != undefined && uri.indexOf('//')==-1) {
      uri = ractive.getServer()+uri;
    }

    ractive.set('saveObserver', saveObserver);
    return uri;
  }
});

$( document ).ajaxError(function( event, request, settings ) {
  switch (request.status) {
  case 0:
    // server unavailable, retry
    if (settings['retryIn']==undefined) settings.retryIn = 4000;
    else settings.retryIn = settings.retryIn * 2;
    var msg = 'Unable to connect, retrying in '+(settings.retryIn/1000)+' secs ...';
    ractive.showDisconnected(msg);
    setTimeout(function() {
      $.ajax(settings);
    }, settings.retryIn);
    break;
  case 200:
    break;
  case 400:
    var msg = request.responseJSON == null ? textStatus+': '+errorThrown : errorThrown+': '+request.responseJSON.message;
    ractive.showError(msg);
    break;
  case 401:
  case 403:
  case 405: /* Could also be a bug but in production we'll assume a timeout */
    ractive.showReconnected();
    ractive.showMessage("Session expired, please login again");
    window.location.href='/login';
    break;
  case 404:
    var path ='';
    if (request.responseJSON != undefined) {
      path = " '"+request.responseJSON.path+"'";
    }
    var msg = "That's odd, we can't find the page"+path+". Please let us know about this message";
    console.error('msg:'+msg);
    ractive.showError(msg);
    break;
  default:
    var msg = "Bother! Something has gone wrong (code "+request.status+"): "+textStatus+':'+errorThrown;
    console.error('msg:'+msg);
    $( "#ajax-loader" ).hide();
    ractive.showError(msg);
  }
});
$( document ).ajaxSuccess(function( event, request, settings ) {
  if (settings['retryIn']==undefined) ractive.showReconnected();
});

$( document ).bind('keypress', function(e) {
  switch (e.keyCode) {
  case 13: // Enter key
    if (window['ractive'] && ractive['enter']) ractive['enter']();
    break;
  case 63:   // ? key
    ractive.showHelp();
    break;
  }
});

$(document).ready(function() {
  ractive.on( 'sort', function ( event, column ) {
    console.info('sort on '+column);
    // if already sorted by this column reverse order
    if (this.get('sortColumn')==column) this.set('sortAsc', !this.get('sortAsc'));
    this.set( 'sortColumn', column );
  });

  ractive.observe('title', function(newValue, oldValue, keypath) {
    console.log('title changing from '+oldValue+' to '+newValue);
    if (newValue!=undefined && newValue!='') {
      $('title').empty().append(newValue);
    }
  });

  var params = getSearchParameters();
  if (params['searchTerm']!=undefined) {
    ractive.set('searchTerm',decodeURIComponent(params['searchTerm']));
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

/* Object extensions */

/**
 * @return The first array element whose 'k' field equals 'v'.
 */
Array.findBy = function(k,v,arr) {
  for (idx in arr) {
    if (arr[idx][k]==v) return arr[idx];
    else if ('selfRef'==k && arr[idx][k] != undefined && arr[idx][k].endsWith(v)) return arr[idx];
  }
}
/**
 * @return All  array elements whose 'k' field equals 'v'.
 */
Array.findAll = function(k,v,arr) {
  var retArr = [];
  for (idx in arr) {
    if (arr[idx][k]==v) retArr.push(arr[idx]);
    else if ('selfRef'==k && arr[idx][k] != undefined && arr[idx][k].endsWith(v)) return retArr.push(arr[idx]);
  }
  return retArr;
}
Array.uniq = function(fieldName, arr) {
  // console.info('uniq');
  list = '';
  for (idx in arr) {
    if (index(arr[idx],fieldName) != undefined
        && list.indexOf(index(arr[idx],fieldName)) == -1) {
      if (list != '')
        list += ','
      list += index(arr[idx],fieldName);
    }
  }
  return list;
}

function index(obj, keypath, value) {
  if (typeof keypath == 'string')
      return index(obj,keypath.split('.'), value);
  else if (keypath.length==1 && value!==undefined)
      return obj[keypath[0]] = value;
  else if (keypath.length==0)
      return obj;
  else
      return index(obj[keypath[0]],keypath.slice(1), value);
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

function parseDateIEPolyFill(timeString) {
  var start = timeString.substring(0,timeString.indexOf('.'));
  var offset;
  if (timeString.indexOf('-',timeString.indexOf('T'))!=-1) {
    offset = timeString.substr(timeString.indexOf('-',timeString.indexOf('T')),3)+':'+timeString.substr(timeString.indexOf('-',timeString.indexOf('T'))+3,2);
  } else if (timeString.indexOf('+')!=-1) {
    offset = timeString.substr(timeString.indexOf('+'),3)+':'+timeString.substr(timeString.indexOf('+')+3,2);
  }
  return new Date(Date.parse(start+offset));
}
