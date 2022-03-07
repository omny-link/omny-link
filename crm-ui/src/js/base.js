/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
var fadeOutMessages = true;

var BaseRactive = Ractive.extend({ // jshint ignore:line
  EASING_DURATION: 500,
  FF: {
    TENANT_SUMMARY: false
  },
  _autolinker: undefined,
  addDataList: function(d, data) {
    if (d.name == undefined) {
      console.warn('Data list with selector "'+d.ref+'" has no name, please add one to use as a datalist');
      return;
    }
    $('datalist#'+d.name).remove();
    $('body').append('<datalist id="'+d.name+'">');
    if (data == null) {
      console.error('No data for datalist: '+d.name+', please fix configuration');
    } else {
      ractive.set('saveObserver', false);
      ractive.set(d.name, data);
      ractive.set('saveObserver', true);
      $.each(data, function (i,e) {
        $('datalist#'+d.name).append('<option value="'+e.name+'">'+e.name+'</option>');
      });
    }
  },
  analyzeEmailActivity: function(activities) {
    if (activities == undefined || activities.length == 0) return;
    ractive.set('saveObserver', false);
    ractive.sortChildren('activities','occurred',false);
    if (activities.length > 0) {
      var count = 0;
      for (var idx in activities) {
        if (activities[idx].type == 'email') {
          count++;
          if (idx == 0) ractive.set('current.timeSinceEmail',(new Date().getTime() - activities[idx].occurred));
        }
      }
      ractive.set('current.emailsSent',count);
    }
    ractive.set('saveObserver', true);
  },
  applyAccessControl: function() {
    console.info('applyAccessControl');
    if (location.href.indexOf('public')==-1 && ractive.get('tenant.access.readonly')) {
      fadeOutMessages=false;
      ractive.showMessage('System is in read-only mode', 'alert-warning');
      $('input,select,textarea').attr('readonly','readonly').attr('disabled','disabled');
    }
  },
  applyBranding: function() {
    if (ractive.get('tenant')==undefined) return ;
    var tenant = ractive.get('tenant.id');
    if (tenant != undefined) {
      if (ractive.get('tenant.theme.cssUrl') !== undefined) {
        $('head').append('<link href="'+ractive.get('tenant.theme.cssUrl')+'" rel="stylesheet">');
      }
      if (ractive.get('tenant.theme.logoUrl')!=undefined) {
        $('.navbar-brand').empty().append('<img src="'+ractive.get('tenant.theme.logoUrl')+'" alt="logo"/>');
      }
      if (ractive.get('tenant.theme.iconUrl')!=undefined) {
          $('link[rel="icon"]').attr('href',ractive.get('tenant.theme.iconUrl'));
      }
      ractive.initContentEditable();// required here for the tenant switcher
      // tenant partial templates
      $.each(ractive.get('tenant').partials, function(i,d) {
        $.get(d.url, function(response){
//          console.log('partial '+d.url+' response: '+response);
          try {
            ractive.set('saveObserver', false);
            ractive.resetPartial(d.name,response);
          } catch (e) {
            console.error('Unable to reset partial '+d.name+': '+e);
          }
          ractive.set('saveObserver', true);
        });
      });
      ractive.applyAccessControl();
      if (ractive.brandingCallbacks!=undefined) ractive.brandingCallbacks.fire();
    }
  },
  daysAgo: function(noOfDays) {
    return new Date(new Date().setDate(new Date().getDate() - noOfDays)).toISOString().substring(0,10);
  },
  download: function(entityName) {
    console.info('download');
    $.ajax({
      headers: {
        "Accept": "text/csv"
      },
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/'+entityName+'/',
      crossDomain: true,
      success: function( data ) {
        console.info('CSV received, first record: '+data.substring(0,data.indexOf('\n')));
        var entityLabel = ractive.get('tenant.strings.'+entityName.toCamelCase())==undefined ? entityName : ractive.get('tenant.strings.'+entityName.toCamelCase());
        try {
          var blob = new Blob([data], {type : 'text/csv'});
          ractive.downloadUri(URL.createObjectURL(blob), entityLabel+".csv");
        } catch (e) {
          ractive.showError('Your browser does not support downloading large files, please try a more modern one.');
        }
      }
    });
  },
  downloadUri: function(uri, name) {
    ractive.set('uri', uri);
    var link = document.createElement("a");
    link.download = name;
    link.href = uri;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  },
  entityName: function(entity) {
    console.info('entityName');
    var id = ractive.uri(entity);
    var lastSlash = id.lastIndexOf('/');
    return id.substring(id.lastIndexOf('/', lastSlash-1)+1, lastSlash);
  },
  fetchConfig: function() {
    console.info('fetchConfig');
    $.getJSON('/configuration', function(data) {
      ractive.set('server',data.clientContext);
    });
  },
  fetchMemoTemplates: function() {
    console.info('fetchMemoTemplates...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType : "json",
      url : ractive.getServer() + '/' + ractive.get('tenant.id') + '/memos/',
      crossDomain : true,
      success : function(data) {
        if ('_embedded' in data) {
          data = data._embedded.memos;
        }
        ractive.set('memos', data);
        console.log('fetched ' + data.length + ' memos');
        // HTML5 style only
        $('datalist#memos').remove();
        $('body').append('<datalist id="memos">');
        $.each(ractive.get('memos'), function (i,d) {
          $('datalist#memos').append('<option value="'+d.name+'">'+d.name+'</option>');
        });

        ractive.set('saveObserver', true);
      }
    });
  },
  fetchStockCategories: function() {
    if (ractive.get('tenant.features.stockCategory')!=true) return;
    console.info('fetchCategories...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType : "json",
      url : ractive.getServer() + '/' + ractive.get('tenant.id') + '/stock-categories/',
      crossDomain : true,
      success : function(data) {
        if ('_embedded' in data) {
          data = data._embedded.stockCategories;
        }
        ractive.set('stockCategories', data);
        console.log('fetched ' + data.length + ' stock categories');
        // HTML5 style only
        $('datalist#stockCategories').remove();
        $('body').append('<datalist id="stockCategories">');
        $.each(ractive.get('stockCategories'), function (i,d) {
          $('datalist#stockCategories').append('<option value="'+d.name+'">'+d.name+'</option>');
        });

        ractive.set('saveObserver', true);
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
        if ('_embedded' in data) {
          data = data._embedded.stockItems;
        }
        ractive.set('stockItems', data);
        console.log('fetched ' + data.length + ' stock items');
        var stockItemData = jQuery.map(data, function(n) {
          return ({
            "id": ractive.id(n),
            "name": n.name
          });
        });
        ractive.set('stockItemsTypeahead', stockItemData);
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
  fetchTasks: function(varName, varVal) {
    console.info('fetchTasks...');
    if (ractive.get('profile')==undefined) {
      console.info(' not ready to fetch tasks');
      return;
    }
    if (ractive.get('fetchTasks')==true) {
      console.info('  skip fetch tasks as already in progress');
      return;
    }
    ractive.set('fetchTasks', true);
    ractive.set('saveObserver', false);
    $.ajax({
      contentType: 'application/json',
      dataType: "json",
      type: 'POST',
      url: ractive.getBpmServer() + '/flowable-rest/service/query/tasks',
      crossDomain: true,
      headers: { Authorization: ractive.getBpmAuth() },
      xhrFields: {
        withCredentials: true
      },
      data: JSON.stringify({
        "includeTaskLocalVariables": true,
        "includeProcessVariables": true,
        "processInstanceVariables": [ {
          "name": varName,
          "operation": "equals",
          "value": varVal
        } ]
      }),
      success: function(data) {
        ractive.set('saveObserver', false);
        // flowable contains metadata as well, use data only
        data = data.data;
        console.log('fetched ' + data.length + ' tasks');

        var data2 = {};
        data.forEach(function(t) {
          t.variables.forEach(function(v) { data2[v['name']]= v['value']; }); // jshint ignore:line
          t.businessKey=ractive.get('current.name') == undefined ? ractive.get('current.fullName') : ractive.get('current.name');
          if ('what' in data2) t.name=data2.what;
          if ('messageName' in data2) t.msg=data2.messageName;
          t.processVarNames=Object.keys(data2);
          t.variables=data2;
	});

        ractive.set('xTasks', data);
        ractive.set('current.tasks', data);
        if (data.length > 0) ractive.sortChildren('tasks', 'dueDate', false);
        ractive.set('fetchTasks', false);
        ractive.set('saveObserver', true);
        ractive.set('alerts.tasks', data.length);
      }
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected');
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected');
    ractive.search(filter.value);
  },
  getCookie: function(name) {
    //console.log('getCookie: '+name)
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  },
  getAttribute: function(attr) {
    return ractive.getProfile().attributes[attr];
  },
  getProfile: function() {
    var profile = localStorage.getItem('profile');
    if (profile == undefined) {
      alert('Unable to authenticate you at the moment, please try later');
      return;
    }
    profile = JSON.parse(profile);
    var username = profile.username;
    console.log('getProfile: '+username);
    if (username) {
      ractive.set('saveObserver', false);
      ractive.set('profile',profile);
      $('.profile-img').empty().append('<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(ractive.get('profile.email'))+'?s=34" title="'+ractive.get('profile.email')+'"/>');
      if (ractive.hasRole('super-admin')) $('.super-admin').show();
      ractive.set('saveObserver', true);
      return profile;
    }
  },
  getServer: function() {
    return ractive.get('server')==undefined ? '' : ractive.get('server');
  },
  getBpmAuth: function() {
    return 'Basic '+btoa(
        ractive.get('profile.username') + ':' +
        ractive.get('profile.attributes.token.0'));
  },
  getBpmServer: function() {
    return 'https://flowable.knowprocess.com';
  },
  getStockItemNames : function (order) {
    var stockItemIds = [];
    if (order == undefined ||
        (!ractive.get('tenant.features.orderItems') && 'stockItem' in order) ||
        (ractive.get('tenant.features.orderItems') && ('orderItems' in order || order.orderItems.length == 0)))
      return;
    else if (!ractive.get('tenant.features.orderItems')) {
      var id = ractive.id(order.stockItem);
      stockItemIds.push(id.substring(id.lastIndexOf('/') + 1));
    } else {
      for (var idx = 0 ; idx < order.orderItems.length ; idx++) {
        if ('stockItem' in order.orderItems[idx]) {
          stockItemIds.push(order.orderItems[idx].stockItem.id);
        } else if ('stockItemId' in order.orderItems[idx].customFields) {
          stockItemIds.push(order.orderItems[idx].customFields.stockItemId);
        } else {
          console.warn('cannot find stock item for order item '+idx);
        }
      }
    }
    var stockItemNames = [];
    stockItemIds = new Set(stockItemIds);
    stockItemIds.forEach(function(value) {
    var tmp = Array.findBy('id',
        '/stock-items/' + value, ractive.get('stockItems'));
      if (tmp != undefined) stockItemNames.push(tmp.name);
    });
    return stockItemNames.join();
  },
  gravatar: function(email) {
    if (email == undefined) return '';
    else return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+
        ractive.hash(email)+'?s=36&d='+
        encodeURIComponent(ractive.getBpmServer()+'/'+ractive.get('tenant.id')+'/gravatars/')+
        ractive.hash(email)+'.png"/>';
  },
  hash: function(email) {
    if (email==undefined) return;
    return md5(email.trim().toLowerCase());
  },
  hasRole: function(role) {
    // console.info('hasRole: ' + role);
    try {
      return (ractive.keycloak.hasRealmRole(role));
    } catch (e) {
      console.warn('No keycloak, using legacy authentication');
      return this.hasRoleLegacy(role);
    }
  },
  hasRoleLegacy: function(role) {
    var ractive = this;
    if (this && this.get('profile')) {
      var hasRole;
      if (ractive.get('profile.groups')!= undefined) {
        hasRole = ractive.get('profile.groups').filter(function(g) { return g.id==role; });
      }
      return hasRole!=undefined && hasRole.length>0;
    }
    return false;
  },
  hideMessage: function() {
    $('#messages, .messages').hide();
  },
  hideResults: function() {
    var entityName = ractive.get('entityName');
    $('#'+entityName+'sTableToggle').addClass('kp-icon-caret-right').removeClass('kp-icon-caret-down');
    $('#'+entityName+'sTable').slideUp();
    $('#currentSect').slideDown({ queue: true });
  },
  hideUpload: function () {
    console.log('hideUpload...');
    $('#upload').slideUp();
  },
  html2Pdf: function(selector, fileName) {
    const { jsPDF } = window.jspdf;
    var doc = new jsPDF('l', 'mm', [1200, 1210]);
    var pdfjs = document.querySelector(selector);
    doc.html(pdfjs, {
      callback: function(doc) {
        doc.save(fileName.endsWith('.pdf') ? fileName : fileName+'.pdf');
      },
      x: 10,
      y: 10
    });
  },
  // deprecated: use localId
  id: function(entity) {
    return ractive.localId(entity);
  },
  initAbout: function() {
    $('.powered-by-icon').empty().append('<img src="'+ractive.getServer()+'/images/icon/omny-link-icon.svg" alt="'+$env.appName+'">');
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    if (ractive.get('tenant.typeaheadControls')!=undefined && ractive.get('tenant.typeaheadControls').length>0) {
      $.each(ractive.get('tenant.typeaheadControls'), function(i,d) {
        //console.log('binding ' +d.url+' to typeahead control: '+d.selector);
        if (d.url==undefined) {
          ractive.addDataList(d,d.values);
        } else {
          var url = d.url;
          if (url.indexOf('//')==-1) url = ractive.getServer()+url;
          $.getJSON(url, function(data) {
            if (data == null || !Array.isArray(data)) {
              console.info('No values for datalist: '+d.name+', probably refreshing token');
            } else {
              d.values = data;
              ractive.addDataList(d,d.values);
            }
          })
          .fail(function(jqXHR) {
            if (jqXHR.status == 401) console.info('No values for datalist: '+d.name+', need to refresh token or login again');
            else console.error('No values for datalist: '+d.name+', please check configuration');
          });
        }
      });
    }
  },
  initAutoNumeric: function() {
    console.log('initAutoNumeric');
    document.querySelectorAll('.autoNumeric').forEach(function(d) {
      $(d).autoNumeric('destroy');
      $(d).autoNumeric('init');
    });
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
    ractive.initAbout();
    ractive.initAutoComplete();
    ractive.initAutoNumeric();
    ractive.initContentEditable();
    ractive.initShortKeys();
  },
  initInfiniteScroll: function() {
    $(window).scroll(function() { // when within 100px of bottom
      if($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
        if ('fetchMore' in ractive) ractive.fetchMore();
      }
    });
  },
  initShortKeys: function() {
    $( "body" ).keypress(function( event ) {
      if (document.activeElement.tagName.toLowerCase() == 'input' ||
          document.activeElement.tagName.toLowerCase() == 'select' ||
          document.activeElement.tagName.toLowerCase() == 'textarea') return;
      switch (event.which) { // ref http://keycode.info/
      case 13: // enter
        $(event.target).blur().focus();
        break;
      case 47: // forward slash on both Mac and Linux
      case 191: // forward slash (allegedly)
        $('.search').focus();
        event.preventDefault();
        break;
      case 63: // ?
        $('#helpModal').modal({});
        event.preventDefault();
        break;
      default:
        //console.log("No Handler for .keypress() called with: "+event.which);
      }
    });
  },
  initTags: function(readOnly) {
    console.info('initTags');
    ractive.set('saveObserver', false);
    $('[data-bind]').each(function(i,d) {
      $(d).val(ractive.get($(d).data('bind')));//.css('display','none');
    });
    if ($('input.tag-ctrl').length>0) {
      $('input.tag-ctrl').tagsinput('destroy');
      $('input.tag-ctrl').tagsinput({ });
      if (!readOnly) {
        $('input.tag-ctrl').on('itemAdded', function(event) {
          ractive.set($(event.target).data('bind'),$(event.target).val());
          ractive.save();
        });
        $('input.tag-ctrl').on('itemRemoved', function(event) {
          ractive.set($(event.target).data('bind'),$(event.target).val());
          ractive.save();
        });
      }
    }
    ractive.set('saveObserver', true);
  },
  initialAccountStage: function() {
    console.log('initialAccountStage');
    $.each(ractive.get('accountStages'), function(i,d) {
      if ('idx' in d && parseInt(d.idx)==0) return d.name;
    });
    return '';
  },
  initialContactStage: function() {
    console.log('initialContactStage');
    $.each(ractive.get('contactStages'), function(i,d) {
      if ('idx' in d && parseInt(d.idx)==0) return d.name;
    });
    return '';
  },
  initialOrderStage: function() {
    console.log('initialOrderStage');
    $.each(ractive.get('orderStages'), function(i,d) {
      if ('idx' in d && parseInt(d.idx)==0) return d.name;
    });
    return '';
  },
  initiatorIcon: function(initiator) {
    if (initiator==undefined || initiator=='anonymousUser') {
      $('.initiator-img').empty().append('<img class="img-rounded" src="' +
          (ractive.get('tenant.strings.botIcon') ? ractive.get('tenant.strings.botIcon') : ractive.getServer()+'/images/icon/omny-link-icon.svg')+
          '" width="34"/>');
    } else {
      $('.initiator-img').empty().append(ractive.gravatar(initiator));
    }
    ractive.set('saveObserver',true);
  },
  json2Html: function(obj) {
    var html = '<ul class="field-json">';
    $.each(Object.keys(obj), function(i,d) {
      if (typeof obj[d] == 'object' && 'valueType' in obj[d] && 'string' in obj[d]) { // empty javax.json.JsonObject
        console.info('Supressing empty value '+d);
      } else if (typeof obj[d] == 'object' && 'valueType' in obj[d]) { // populated javax.json.JsonObject
        html += '<li><label style="text-align:right;padding-right:10px">'+d.toLabel()+':</label><span class="col-scroll">'+obj[d].string+'</span>';
      } else if (typeof obj[d] == 'object') { // child object
        html += '<table class="table table-striped"><tr><th>' + d.toLabel() +'</th><td>'+ ractive.json2Html(obj[d])+'</td></tr></table>';
      } else {
        html += '<label>'+d.toLabel()+':</label><span class="col-scroll">'+obj[d]+'</span>';
      }
    });
    return html;
  },
  loadStandardPartial: function(name,url) {
    //console.log('loading...: '+d.name)
      $.get(url, function(response) {
        //console.log('... loaded: '+d.name)
        //console.log('response: '+response)
        if (ractive != undefined) {
          try {
            ractive.set('saveObserver', false);
            ractive.resetPartial(name,response);
            ractive.set('saveObserver', true);
          } catch (e) {
            console.warn('Unable to reset partial '+name+': '+e);
          }
        }
      });
    },
  loadStandardPartials: function(stdPartials) {
    console.info('loadStandardPartials');
    if (ractive == undefined || stdPartials == undefined) return;
    $.each(stdPartials, function(i,d) {
      ractive.loadStandardPartial(d.name, d.url);
    });
  },
  loadTenantConfig: function(tenant) {
    console.info('loadTenantConfig:'+tenant);
    $.getJSON('__CONFIG_SERVER_URL__/tenants/'+tenant+'/'+tenant+'.json', function(response) {
      // console.log('... response: '+JSON.stringify(response));
      ractive.set('saveObserver', false);
      ractive.set('tenant', response);
      if ('fetch' in ractive) ractive.fetch();
      ractive.applyBranding();
      ractive.get('tenant.typeaheadControls').forEach(function(d) {
        if ('name' in d && 'values' in d) ractive.set(d.name, d.values);
      });
      ractive.set('saveObserver', true);
      if (ractive.tenantCallbacks!=undefined) ractive.tenantCallbacks.fire();
    });
  },
  logout: function() {
    ractive.keycloak.logout();
  },
  parseDate: function(timeString) {
    var d = new Date(timeString);
    // IE strikes again
    if (d == 'Invalid Date') d = parseDateIEPolyFill(timeString);
    return d == 'Invalid Date' ? timeString : d;
  },
  rewrite: function(id) {
    console.info('rewrite:'+id);
    if (ractive.get('server')!=undefined && ractive.get('server')!='' && id.indexOf(ractive.get('server'))==-1) {
      //console.error('  rewrite is necessary');
      if (id.indexOf('://')==-1) {
        return ractive.getServer()+id;
      } else {
        return ractive.getServer()+id.substring(id.indexOf('/', id.indexOf('://')+4));
      }
    } else {
      return id;
    }
  },
  search: function(searchTerm) {
    $( "#ajax-loader" ).show();
    setTimeout(function() {
      ractive.set('searchTerm',searchTerm);
      ractive.set('searchMatched',$('.resultsSect .table tbody tr:visible').length);
      if ('showResults' in ractive) ractive.showResults();
      $( "#ajax-loader" ).hide();
    }, ractive.EASING_DURATION);
  },
  localId: function(uriOrObj) {
    if (uriOrObj == undefined) return;
    else if (typeof uriOrObj == 'object' && 'id' in uriOrObj) return ''+uriOrObj.id;
    else if (typeof uriOrObj == 'object') return ractive.localId(ractive.uri(uriOrObj));
    else return uriOrObj.substring(uriOrObj.lastIndexOf('/')+1);
  },
  showBanner: function(msg, additionalClass) {
    var banner = document.getElementById('olBanner');
    if (banner === null) {
      banner = document.createElement('div');
      banner.id="olBanner";
      var bannerContainer = document.createElement('div');
      bannerContainer.className = 'container';
      banner.appendChild(bannerContainer);
      document.querySelector('body').prepend(banner);
    }
    banner.className='alert '+additionalClass;
    banner.style = 'z-index:200;position:relative;';
    document.querySelector('#olBanner>.container').innerHTML = msg;
  },
  showCurrent: function() {
    $('#currentSect').slideDown({ queue: true });
  },
  showDisconnected: function(msg) {
    console.log('showDisconnected: '+msg);
    if ($('#connectivityMessages').length>0) {
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
    if (additionalClass == undefined) additionalClass = 'bg-info text-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages, .messages').empty().append(msg).removeClass().addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='bg-danger text-danger') setTimeout(function() {
      $('#messages, .messages').fadeOut();
    }, ractive.EASING_DURATION*10);
    else $('#messages, .messages').append('<span class="text-danger pull-right glyphicon glyphicon-remove" onclick="ractive.hideMessage()"></span>');
  },
  showReconnected: function() {
    console.log('showReconnected');
    $( "#ajax-loader" ).hide();
    if ($('#connectivityMessages:visible').length>0) {
      $('#connectivityMessages').remove();
      $('body').append('<div id="connectivityMessages" class="alert-info">Reconnected</div>').show();
      setTimeout(function() {
        $('#connectivityMessages').fadeOut();
      }, ractive.EASING_DURATION*10);
    }
  },
  showResults: function() {
    var entityName = ractive.get('entityName');
    $('#'+entityName+'sTableToggle').addClass('kp-icon-caret-down').removeClass('kp-icon-caret-right');
    $('#currentSect').slideUp();
    $('#'+entityName+'sTable').slideDown({ queue: true });
  },
  showSearchMatched: function() {
    var entityName = ractive.get('entityName');
    ractive.set('searchMatched',$('#'+entityName+'sTable tbody tr').length);
    if ($('#'+entityName+'sTable tbody tr:visible').length==1) {
      var id = $('#'+entityName+'sTable tbody tr:visible').data('href');
      var q = Array.findBy('id',id,ractive.get(entityName+'s'));
      ractive.select( q );
    }
  },
  showUpload: function () {
    console.log('showUpload...');
    $('#upload').slideDown();
  },
  showSocial: function(networkName, keypath) {
    console.log('showSocial: '+networkName);
    ractive.set('current.network', { name: networkName, keypath: keypath, value: ractive.get(keypath) });
    $('#socialModalSect').modal('show');
  },
  showWarning: function(msg) {
    this.showMessage(msg, 'alert-warning');
  },
  submitSocial: function(network) {
    console.log('submitSocial: '+network);
    ractive.set(network.keypath,ractive.get('current.network.value'));
    $('#socialModalSect').modal('hide');
  },
  sortBy: function (array, column, asc) {
    if (array === undefined || array.length === 0) return;
    console.info('sort array of '+(array == undefined ? 0 : array.length)+' items '+(asc ? 'ascending' : 'descending')+' on: '+column);
    array = array.slice(); // clone, so we don't modify the underlying data

    return array.sort( function ( a, b ) {
      if (b[column]==undefined || b[column]==null || b[column]=='') {
        return (a[column]==undefined || a[column]==null || a[column]=='') ? 0 : -1;
      } else if (asc) {
        return (''+a[ column ]).toLowerCase() < (''+b[ column ]).toLowerCase() ? -1 : 1;
      } else {
        return (''+a[ column ]).toLowerCase() > (''+b[ column ]).toLowerCase() ? -1 : 1;
      }
    });
  },
  sortChildren: function(childArray, sortBy, asc) {
    console.info('sortChildren');
    if (ractive.get('current.'+childArray)==undefined) return 0;
    ractive.sort('current.'+childArray, function(a,b) {
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
      processDefinitionKey: key,
      businessKey: businessKey == undefined ? label : businessKey,
      label: label,
      variables: {
        businessKey: businessKey == undefined ? label : businessKey,
        initiator: ractive.get('profile.username'),
        tenantId: ractive.get('tenant.id'),
      }
    };

    if (object != undefined) {
      var singularEntityName = ractive.entityName(object).toCamelCase().singular();
      instanceToStart.variables[singularEntityName+'Id'] = ractive.localId(ractive.uri(object));
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
      $('#submitCustomActionForm').off().on('click', ractive.submitCustomAction);
      $('#customActionModalSect').modal('show');
    }
  },
  submitCustomAction: function() {
    console.info('submitCustomAction');
    if (document.getElementById('customActionForm').checkValidity()) {
      if (ractive.get('instanceToStart.processDefinitionKeyOverride') != undefined) ractive.set('instanceToStart.processDefinitionKey', ractive.get('instanceToStart.processDefinitionKeyOverride'));
      var data = {
        processDefinitionKey: ractive.get('instanceToStart.processDefinitionKey'),
        businessKey: ractive.get('instanceToStart.label')+' to/for '+ractive.get('instanceToStart.businessKey'),
        name: ractive.get('instanceToStart.processDefinitionKey')+' '+ractive.localId(ractive.get('current')),
        variables: [ ],
        /*tenantId: ractive.get('tenant.id'),*/
        returnVariables: true
      };
      var varObj = ractive.get('instanceToStart.variables');
      // for compatibility with v2 extension forms
      Object.assign(varObj,ractive.get('instanceToStart.processVariables'));
      Object.keys(varObj).forEach(function(key) {
        var v = { name:key, value:varObj[key], scope:"global" };
        if (typeof varObj[key]=='object') v.type = 'json';
        data.variables.push(v);
      });
      $.ajax({
        url: ractive.getBpmServer()+'/flowable-rest/service/runtime/process-instances/',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        crossDomain: true,
        headers: { Authorization: ractive.getBpmAuth() },
        success: function(data, textStatus, jqXHR) {
          console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
          if (ractive.get('instanceToStart.label')==ractive.get('instanceToStart.businessKey')) {
            ractive.showMessage('Started workflow "'+ractive.get('instanceToStart.label')+'"');
          } else {
            ractive.showMessage('Started workflow "'+ractive.get('instanceToStart.label')+'" for '+ractive.get('instanceToStart.businessKey'));
          }
          $('#customActionModalSect').modal('hide');
          if (document.location.href.endsWith('contacts.html') ||
              document.location.href.endsWith('accounts.html')) {
            // refresh individual record
            setTimeout(ractive.select, ractive.EASING_DURATION*5, ractive.get('current'));
          } else {
            // refresh list
            setTimeout(ractive.fetch, ractive.EASING_DURATION*5);
          }
          // @deprecated use ractive's own callbacks
          if (ractive.customActionCallbacks!=undefined) ractive.customActionCallbacks.fire(jqXHR.getResponseHeader('Location'));
          ractive.fire('postCustomActionSubmit');
          // cleanup ready for next time
          $('#submitCustomActionForm').off('click');
        },
      });
    } else {
      ractive.showFormError('customActionForm','Please correct the highlighted fields');
    }
  },
  stripProjection: function(link) {
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
  tenantUri: function(entity, entityPath) {
    //console.log('tenantUri: '+entity);
    var uri = ractive.uri(entity);
    if (entityPath===undefined) entityPath = ractive.get('entityPath');
    if (uri != undefined && uri.indexOf(ractive.get('tenant.id')+'/')==-1) {
      uri = uri.replace(entityPath,'/'+ractive.get('tenant.id')+entityPath);
    }
    return uri;
  },
  toCsv: function(json, title, headings) {
    //If json is not an object then JSON.parse will parse the JSON string in an Object
    var arr = typeof json != 'object' ? JSON.parse(json) : json;

    var csv = '';

    // write title on first row
    csv += title + '\n\n';

    var row = '';
    if (headings === undefined || headings == true) {
      // extract label from json fields in array idx 0
      for (var idx in arr[0]) {
          row += idx + ',';
      }

      row = row.slice(0, -1); // strip trailing comma
      headings = row + '\n';
    }
    csv += headings + '\n';

    var propNames = headings.split(',');
    for (var i = 0; i < arr.length; i++) {
        row = '';

        for (var j = 0 ; j < propNames.length ; j++) {
          try {
            var val = eval('arr['+i+'].'+propNames[j]); // jshint ignore:line
            row += '"' + (val == undefined ? '' : val) + '",';
          } catch (err) {
            console.error('Fail to extract property '+propNames[j]+'from row '+i);
          }
        }

        row = row.slice(0, -1); // strip trailing comma
        csv += row + '\n';
    }

    if (csv == '') {
        alert("Invalid data");
        return;
    }

    //Generate a file name (<title with _ for spaces>-<timestamp>)
    var fileName = title.replace(/ /g,"_")+new Date().toISOString().substring(0,16).replace(/[T:]/g,'-');

    //Initialize file format you want csv or xls
    var uri = 'data:text/csv;charset=utf-8,' + escape(csv);

    // Now the little tricky part.
    // you can use either>> window.open(uri);
    // but this will not work in some browsers
    // or you will not get the correct file extension

    //this trick will generate a temp <a /> tag
    var link = document.createElement("a");
    link.href = uri;

    //set the visibility hidden so it will not effect on your web-layout
    link.style = "visibility:hidden";
    link.download = fileName + ".csv";

    //this part will append the anchor tag and remove it after automatic click
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  },
  toggleResults: function() {
    console.info('toggleResults');
    var entityName = ractive.get('entityName');
    $('#'+entityName+'sTableToggle').toggleClass('kp-icon-caret-down').toggleClass('kp-icon-caret-right');
    $('#'+entityName+'sTable').slideToggle();
  },
  toggleSection: function(sect) {
    console.info('toggleSection: '+$(sect).attr('id'));
    $('#'+$(sect).attr('id')+'>div').toggle();
    $('#'+$(sect).attr('id')+' .ol-collapse').toggleClass('kp-icon-caret-right').toggleClass('kp-icon-caret-down');
  },
  toggleSidebar: function() {
    console.info('toggleSidebar');
    $('.toolbar-left').toggle(ractive.EASING_DURATION);
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
        success: function() {
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
    try {
      if ('links' in entity) {
        $.each(entity.links, function(i,d) {
          if (d.rel == 'self') {
            uri = d.href;
          }
        });
      } else if ('_links' in entity) {
        uri = ractive.stripProjection(entity._links.self.href);
      } else if ('id' in entity) {
        uri = ractive.get('entityPath')+'/'+entity.id;
      }
      // work around for sub-dir running
      if (uri != undefined && uri.indexOf(ractive.getServer())==-1 && uri.indexOf('//')!=-1) {
        uri = ractive.getServer() + uri.substring(uri.indexOf('/', uri.indexOf('//')+2));
      } else if (uri != undefined && uri.indexOf('//')==-1) {
        uri = ractive.getServer()+uri;
      }
    } catch (e) {
      console.error('Cannot get URI of '+JSON.stringify(entity)+'. '+e);
    }
    ractive.set('saveObserver', saveObserver);
    return uri;
  }
});

$(document).ready(function() {
  try {
    ractive.keycloak = Keycloak('/keycloak.json');
    ractive.keycloak.init({ onLoad: 'login-required' })
    .then(function(authenticated) {
      console.info(authenticated ? 'authenticated' : 'not authenticated');
      if ('cm' in ractive && 'options' in ractive.cm) {
        ractive.cm.options.token = ractive.keycloak.token;
      }
      ractive.keycloak.loadUserProfile().then(function(profile) {
        localStorage.setItem('profile', JSON.stringify(profile));
        ractive.getProfile();
        ractive.set('tenant.id', ractive.getAttribute('tenantId'));
        ractive.loadTenantConfig(ractive.get('tenant.id'));
      }).catch(function(e) {
        console.error('Failed to load user profile: '+e);
      });
    }).catch(function(e) {
      console.error('failed to initialize: '+e);
    });
  } catch (e) {
    console.warn('no Keycloak, use legacy authentication');
  }

  ractive.set('saveObserver', false);
  //ajax loader
  if ($('#ajax-loader').length==0) $('body').append('<div id="ajax-loader"><img class="ajax-loader" src="/images/ajax-loader.gif" style="width:10%" alt="Loading..."/></div>');
  $( document ).ajaxStart(function() {
    $( "#ajax-loader" ).show();
  });
  $( document ).ajaxStop(function() {
    $( "#ajax-loader" ).hide();
  });

  ractive.loadStandardPartials(ractive.get('stdPartials'));
  ractive.loadStandardPartials(ractive.get('workPartials'));

  $( document ).ajaxComplete(function(event, jqXHR) {
    if (jqXHR.status > 0 && jqXHR < 400) ractive.showReconnected();
  });

  ractive.observe('tenant', function(newValue, oldValue, keypath) {
    console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
    if ((oldValue == undefined || oldValue.id == '') && newValue != undefined && newValue.id != '' && 'fetch' in ractive) {
      //ractive.fetch();
    }

    if (newValue!=undefined && newValue.accountFields!=undefined && newValue.accountFields.length>0) {
      for(let idx = 0 ; idx<newValue.accountFields.length ; idx++) {
        if (newValue.accountFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if ('fetchAccounts' in ractive) ractive.fetchAccounts();
        } else if (newValue.accountFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if ('fetchContacts' in ractive) ractive.fetchContacts();
        }
      }
    }
    if (newValue!=undefined && newValue.contactFields!=undefined && newValue.contactFields.length>0) {
      for(let idx = 0 ; idx<newValue.contactFields.length ; idx++) {
        if (newValue.contactFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if ('fetchAccounts' in ractive) ractive.fetchAccounts();
        } else if (newValue.contactFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if ('fetchContacts' in ractive) ractive.fetchContacts();
        }
      }
    }
    if (newValue!=undefined && newValue.orderFields!=undefined && newValue.orderFields.length>0) {
      for(let idx = 0 ; idx<newValue.orderFields.length ; idx++) {
        if (newValue.orderFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if ('fetchAccounts' in ractive) ractive.fetchAccounts();
        } else if (newValue.orderFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if ('fetchContacts' in ractive) ractive.fetchContacts();
        }
      }
    }
    if (newValue!=undefined && newValue.orderItemFields!=undefined && newValue.orderItemFields.length>0) {
      for(let idx = 0 ; idx<newValue.orderItemFields.length ; idx++) {
        if (newValue.orderItemFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if ('fetchAccounts' in ractive) ractive.fetchAccounts();
        } else if (newValue.orderItemFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if ('fetchContacts' in ractive) ractive.fetchContacts();
        }
      }
    }
  });

  ractive.on( 'filter', function ( event, filter ) {
    console.info('filter on '+JSON.stringify(filter)+','+filter.idx);
    let start = new Date().getTime();
    ractive.filter(filter);
    console.info('  took '+(new Date().getTime()-start)+'ms');
  });

  ractive.on('sort', function ( event, column ) {
    console.info('sort on '+column);
    // if already sorted by this column reverse order
    if (this.get('sortColumn')==column) this.set('sortAsc', !this.get('sortAsc'));
    this.set( 'sortColumn', column );
  });

  ractive.observe('title', function(newValue, oldValue, keypath) {
    console.log("'"+keypath+"' changing from '"+oldValue+"' to '"+newValue+"'");
    if (newValue!=undefined && newValue!='') {
      $('title').empty().append(newValue);
    }
  });

  ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
    console.log("'"+keypath+"' changed from '"+oldValue+"' to '"+newValue+"'");
    let start = new Date().getTime();
    if ('showResults' in ractive) {
      $( "#ajax-loader" ).show();
      ractive.showResults();
    }
    setTimeout(ractive.showSearchMatched, ractive.EASING_DURATION);
    console.info('  took '+(new Date().getTime()-start)+'ms');
  });

  var params = getSearchParameters();
  if ('searchTerm' in params) {
    ractive.set('searchTerm',decodeURIComponent(params.searchTerm));
  } else if ('q' in params) {
    ractive.set('searchTerm',decodeURIComponent(params.q));
  }
  if ('s' in params) {
    ractive.set('sortColumn',decodeURIComponent(params.s));
  }
  if ('asc' in params && params.asc=='true') {
    ractive.set('sortAsc',true);
  } else {
    ractive.set('sortAsc',false);
  }
  window.i18n = new I18nController();
  ractive.set('saveObserver', true);
});

function selectElementContents(el) {
  var range = document.createRange();
  range.selectNodeContents(el);
  var sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(range);
}

function getSearchParameter(key) { // jshint ignore:line
  var params = getSearchParameters();
  return key in params ? params.key : undefined;
}

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

/* Object extensions */

Array.prototype.clean = function(deleteValue) {
  for (var i = 0; i < this.length; i++) {
    if (this[i] == deleteValue) {
      this.splice(i, 1);
      i--;
    }
  }
  return this;
};

/**
 * @return The first array element whose 'k' field equals 'v'.
 */
Array.findBy = function(k,v,arr) {
  if (arr == undefined) return undefined;
  for (var idx = 0 ; idx < arr.length ; idx++) {
    if (arr[idx][k]==v) return arr[idx];
    else if ('selfRef'==k && arr[idx][k] != undefined && arr[idx][k].endsWith(v)) return arr[idx];
  }
};

/**
 * @return All  array elements whose 'k' field equals 'v'.
 */
Array.findAll = function(k,v,arr) {
  var retArr = [];
  for (var idx = 0 ; idx < arr.length ; idx++) {
    if (arr[idx][k]==v) retArr.push(arr[idx]);
    else if ('selfRef'==k && arr[idx][k] != undefined && arr[idx][k].endsWith(v)) return retArr.push(arr[idx]);
  }
  return retArr;
};

Array.uniq = function(fieldName, arr) {
  // console.info('uniq');
  if (arr == undefined) return undefined;
  var list = '';
  for (var idx = 0 ; idx < arr.length ; idx++) {
    if (index(arr[idx],fieldName) != undefined &&
        list.indexOf(index(arr[idx],fieldName)) == -1) {
      if (list != '') list += ',';
      list += index(arr[idx],fieldName);
    }
  }
  return list;
};

function index(obj, keypath, value) {
  if (typeof keypath == 'string')
      return index(obj,keypath.split('.'), value);
  else if (keypath.length==1 && value!==undefined)
      return obj[keypath[0]] = value; // jshint ignore:line
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

/************************************************************
 * MD5 (Message-Digest Algorithm) http://www.webtoolkit.info/
 ***********************************************************/
 function md5(string) {
  function RotateLeft(lValue, iShiftBits) {
  return (lValue<<iShiftBits) | (lValue>>>(32-iShiftBits));
  }

  function AddUnsigned(lX,lY) {
  var lX4,lY4,lX8,lY8,lResult;
  lX8 = (lX & 0x80000000);
  lY8 = (lY & 0x80000000);
  lX4 = (lX & 0x40000000);
  lY4 = (lY & 0x40000000);
  lResult = (lX & 0x3FFFFFFF)+(lY & 0x3FFFFFFF);
  if (lX4 & lY4) {
  return (lResult ^ 0x80000000 ^ lX8 ^ lY8);
  }
  if (lX4 | lY4) {
  if (lResult & 0x40000000) {
  return (lResult ^ 0xC0000000 ^ lX8 ^ lY8);
  } else {
  return (lResult ^ 0x40000000 ^ lX8 ^ lY8);
  }
  } else {
  return (lResult ^ lX8 ^ lY8);
  }
  }

  function F(x,y,z) { return (x & y) | ((~x) & z); }
  function G(x,y,z) { return (x & z) | (y & (~z)); }
  function H(x,y,z) { return (x ^ y ^ z); }
  function I(x,y,z) { return (y ^ (x | (~z))); }

  function FF(a,b,c,d,x,s,ac) {
  a = AddUnsigned(a, AddUnsigned(AddUnsigned(F(b, c, d), x), ac));
  return AddUnsigned(RotateLeft(a, s), b);
  }

  function GG(a,b,c,d,x,s,ac) {
  a = AddUnsigned(a, AddUnsigned(AddUnsigned(G(b, c, d), x), ac));
  return AddUnsigned(RotateLeft(a, s), b);
  }

  function HH(a,b,c,d,x,s,ac) {
  a = AddUnsigned(a, AddUnsigned(AddUnsigned(H(b, c, d), x), ac));
  return AddUnsigned(RotateLeft(a, s), b);
  }

  function II(a,b,c,d,x,s,ac) {
  a = AddUnsigned(a, AddUnsigned(AddUnsigned(I(b, c, d), x), ac));
  return AddUnsigned(RotateLeft(a, s), b);
  }

  function ConvertToWordArray(string) {
  var lWordCount;
  var lMessageLength = string.length;
  var lNumberOfWords_temp1=lMessageLength + 8;
  var lNumberOfWords_temp2=(lNumberOfWords_temp1-(lNumberOfWords_temp1 % 64))/64;
  var lNumberOfWords = (lNumberOfWords_temp2+1)*16;
  var lWordArray=Array(lNumberOfWords-1);
  var lBytePosition = 0;
  var lByteCount = 0;
  while ( lByteCount < lMessageLength ) {
  lWordCount = (lByteCount-(lByteCount % 4))/4;
  lBytePosition = (lByteCount % 4)*8;
  lWordArray[lWordCount] = (lWordArray[lWordCount] | (string.charCodeAt(lByteCount)<<lBytePosition));
  lByteCount++;
  }
  lWordCount = (lByteCount-(lByteCount % 4))/4;
  lBytePosition = (lByteCount % 4)*8;
  lWordArray[lWordCount] = lWordArray[lWordCount] | (0x80<<lBytePosition);
  lWordArray[lNumberOfWords-2] = lMessageLength<<3;
  lWordArray[lNumberOfWords-1] = lMessageLength>>>29;
  return lWordArray;
  }

  function WordToHex(lValue) {
  var WordToHexValue='',WordToHexValue_temp='',lByte,lCount;
  for (lCount = 0;lCount<=3;lCount++) {
  lByte = (lValue>>>(lCount*8)) & 255;
  WordToHexValue_temp = '0' + lByte.toString(16);
  WordToHexValue = WordToHexValue + WordToHexValue_temp.substr(WordToHexValue_temp.length-2,2);
  }
  return WordToHexValue;
  }

  function Utf8Encode(string) {
  string = string.replace(/\r\n/g,'\n');
  var utftext = '';

  for (var n = 0; n < string.length; n++) {
  var c = string.charCodeAt(n);

  if (c < 128) {
  utftext += String.fromCharCode(c);
  }
  else if((c > 127) && (c < 2048)) {
  utftext += String.fromCharCode((c >> 6) | 192);
  utftext += String.fromCharCode((c & 63) | 128);
  }
  else {
  utftext += String.fromCharCode((c >> 12) | 224);
  utftext += String.fromCharCode(((c >> 6) & 63) | 128);
  utftext += String.fromCharCode((c & 63) | 128);
  }
  }

  return utftext;
  }

  var x=Array();
  var k,AA,BB,CC,DD,a,b,c,d;
  var S11=7, S12=12, S13=17, S14=22;
  var S21=5, S22=9 , S23=14, S24=20;
  var S31=4, S32=11, S33=16, S34=23;
  var S41=6, S42=10, S43=15, S44=21;

  string = Utf8Encode(string);

  x = ConvertToWordArray(string);

  a = 0x67452301; b = 0xEFCDAB89; c = 0x98BADCFE; d = 0x10325476;

  for (k=0;k<x.length;k+=16) {
  AA=a; BB=b; CC=c; DD=d;
  a=FF(a,b,c,d,x[k+0], S11,0xD76AA478);
  d=FF(d,a,b,c,x[k+1], S12,0xE8C7B756);
  c=FF(c,d,a,b,x[k+2], S13,0x242070DB);
  b=FF(b,c,d,a,x[k+3], S14,0xC1BDCEEE);
  a=FF(a,b,c,d,x[k+4], S11,0xF57C0FAF);
  d=FF(d,a,b,c,x[k+5], S12,0x4787C62A);
  c=FF(c,d,a,b,x[k+6], S13,0xA8304613);
  b=FF(b,c,d,a,x[k+7], S14,0xFD469501);
  a=FF(a,b,c,d,x[k+8], S11,0x698098D8);
  d=FF(d,a,b,c,x[k+9], S12,0x8B44F7AF);
  c=FF(c,d,a,b,x[k+10],S13,0xFFFF5BB1);
  b=FF(b,c,d,a,x[k+11],S14,0x895CD7BE);
  a=FF(a,b,c,d,x[k+12],S11,0x6B901122);
  d=FF(d,a,b,c,x[k+13],S12,0xFD987193);
  c=FF(c,d,a,b,x[k+14],S13,0xA679438E);
  b=FF(b,c,d,a,x[k+15],S14,0x49B40821);
  a=GG(a,b,c,d,x[k+1], S21,0xF61E2562);
  d=GG(d,a,b,c,x[k+6], S22,0xC040B340);
  c=GG(c,d,a,b,x[k+11],S23,0x265E5A51);
  b=GG(b,c,d,a,x[k+0], S24,0xE9B6C7AA);
  a=GG(a,b,c,d,x[k+5], S21,0xD62F105D);
  d=GG(d,a,b,c,x[k+10],S22,0x2441453);
  c=GG(c,d,a,b,x[k+15],S23,0xD8A1E681);
  b=GG(b,c,d,a,x[k+4], S24,0xE7D3FBC8);
  a=GG(a,b,c,d,x[k+9], S21,0x21E1CDE6);
  d=GG(d,a,b,c,x[k+14],S22,0xC33707D6);
  c=GG(c,d,a,b,x[k+3], S23,0xF4D50D87);
  b=GG(b,c,d,a,x[k+8], S24,0x455A14ED);
  a=GG(a,b,c,d,x[k+13],S21,0xA9E3E905);
  d=GG(d,a,b,c,x[k+2], S22,0xFCEFA3F8);
  c=GG(c,d,a,b,x[k+7], S23,0x676F02D9);
  b=GG(b,c,d,a,x[k+12],S24,0x8D2A4C8A);
  a=HH(a,b,c,d,x[k+5], S31,0xFFFA3942);
  d=HH(d,a,b,c,x[k+8], S32,0x8771F681);
  c=HH(c,d,a,b,x[k+11],S33,0x6D9D6122);
  b=HH(b,c,d,a,x[k+14],S34,0xFDE5380C);
  a=HH(a,b,c,d,x[k+1], S31,0xA4BEEA44);
  d=HH(d,a,b,c,x[k+4], S32,0x4BDECFA9);
  c=HH(c,d,a,b,x[k+7], S33,0xF6BB4B60);
  b=HH(b,c,d,a,x[k+10],S34,0xBEBFBC70);
  a=HH(a,b,c,d,x[k+13],S31,0x289B7EC6);
  d=HH(d,a,b,c,x[k+0], S32,0xEAA127FA);
  c=HH(c,d,a,b,x[k+3], S33,0xD4EF3085);
  b=HH(b,c,d,a,x[k+6], S34,0x4881D05);
  a=HH(a,b,c,d,x[k+9], S31,0xD9D4D039);
  d=HH(d,a,b,c,x[k+12],S32,0xE6DB99E5);
  c=HH(c,d,a,b,x[k+15],S33,0x1FA27CF8);
  b=HH(b,c,d,a,x[k+2], S34,0xC4AC5665);
  a=II(a,b,c,d,x[k+0], S41,0xF4292244);
  d=II(d,a,b,c,x[k+7], S42,0x432AFF97);
  c=II(c,d,a,b,x[k+14],S43,0xAB9423A7);
  b=II(b,c,d,a,x[k+5], S44,0xFC93A039);
  a=II(a,b,c,d,x[k+12],S41,0x655B59C3);
  d=II(d,a,b,c,x[k+3], S42,0x8F0CCC92);
  c=II(c,d,a,b,x[k+10],S43,0xFFEFF47D);
  b=II(b,c,d,a,x[k+1], S44,0x85845DD1);
  a=II(a,b,c,d,x[k+8], S41,0x6FA87E4F);
  d=II(d,a,b,c,x[k+15],S42,0xFE2CE6E0);
  c=II(c,d,a,b,x[k+6], S43,0xA3014314);
  b=II(b,c,d,a,x[k+13],S44,0x4E0811A1);
  a=II(a,b,c,d,x[k+4], S41,0xF7537E82);
  d=II(d,a,b,c,x[k+11],S42,0xBD3AF235);
  c=II(c,d,a,b,x[k+2], S43,0x2AD7D2BB);
  b=II(b,c,d,a,x[k+9], S44,0xEB86D391);
  a=AddUnsigned(a,AA);
  b=AddUnsigned(b,BB);
  c=AddUnsigned(c,CC);
  d=AddUnsigned(d,DD);
  }

  var temp = WordToHex(a)+WordToHex(b)+WordToHex(c)+WordToHex(d);

  return temp.toLowerCase();
 }

