/*******************************************************************************
 * Copyright 2011-2018 Tim Stephenson and contributors
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
var BaseRactive = Ractive.extend({
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
      for (idx in activities) {
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
      $('head').append('<link href="'+ractive.getServer()+'/css/'+tenant+'-1.0.0.css" rel="stylesheet">');
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
        ractive.downloadUri(encodeURI("data:text/csv;charset=utf-8,"+data), entityLabel+".csv");
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
    delete link;
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
        if (data['_embedded'] != undefined) {
          data = data['_embedded'].memos;
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
        if (data['_embedded'] != undefined) {
          data = data['_embedded'].stockCategories;
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
        if (data['_embedded'] != undefined) {
          data = data['_embedded'].stockItems;
        }
        ractive.set('stockItems', data);
        console.log('fetched ' + data.length + ' stock items');
        var stockItemData = jQuery.map(data, function(n, i) {
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
  getCookie: function(name) {
    //console.log('getCookie: '+name)
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
  },
  getProfile: function() {
    // TODO
  },
  getServer: function() {
    return ractive.get('server')==undefined ? '' : ractive.get('server');
  },
  getStockItemNames : function (order) {
    var stockItemIds = [];
    if (order == undefined
        || (!ractive.get('tenant.features.orderItems') && order['stockItem'] == undefined)
        || (ractive.get('tenant.features.orderItems') && (order['orderItems'] == undefined || order.orderItems.length == 0)))
      return;
    else if (!ractive.get('tenant.features.orderItems')) {
      var id = ractive.id(order.stockItem);
      stockItemIds.push(id.substring(id.lastIndexOf('/') + 1));
    } else {
      for (var idx = 0 ; idx < order.orderItems.length ; idx++) {
        if (order.orderItems[idx]['stockItem']!=undefined) {
          stockItemIds.push(order.orderItems[idx].stockItem.id);
        } else if (order.orderItems[idx].customFields['stockItemId']!=undefined) {
          stockItemIds.push(order.orderItems[idx].customFields['stockItemId']);
        } else {
          console.warn('cannot find stock item for order item '+idx);
        }
      }
    }
    var stockItemNames = [];
    stockItemIds = new Set(stockItemIds);
    stockItemIds.forEach(function(value) {
    var tmp = Array.findBy('selfRef',
        '/stock-items/' + value, ractive.get('stockItems'));
      if (tmp != undefined) stockItemNames.push(tmp.name);
    });
    return stockItemNames.join();
  },
  hash: function(email) {
    if (email==undefined) return;
    return hex_md5(email.trim().toLowerCase());
  },
  hasRole: function(role) {
    var ractive = this;
    if (this && this.get('profile')) {
      var hasRole;
      if (ractive.get('profile.groups')!= undefined) {
        hasRole = ractive.get('profile.groups').filter(function(g) {return g.id==role});
      }
      return hasRole!=undefined && hasRole.length>0;
    }
    return false;
  },
  hideLogin: function() {
    $('#loginSect').slideUp();
  },
  hideMessage: function() {
    $('#messages, .messages').hide();
  },
  hideUpload: function () {
    console.log('hideUpload...');
    $('#upload').slideUp();
  },
  // TODO why is this so slow?
  id: function(entity) {
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
          .fail(function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 401) console.info('No values for datalist: '+d.name+', need to refresh token or login again');
            else console.error('No values for datalist: '+d.name+', please check configuration');
          });
        }
      });
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
    ractive.initContentEditable();
    ractive.initShortKeys();
  },
  initInfiniteScroll: function() {
    $(window).scroll(function() { // when within 100px of bottom
      if($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
        if (ractive['fetchMore']!=undefined) ractive.fetchMore();
      }
    });
  },
  initShortKeys: function() {
    $( "body" ).keypress(function( event ) {
      if (event.target.tagName.toLowerCase() == 'input' || event.target.tagName.toLowerCase() == 'textarea') return;
      switch (event.which) { // ref http://keycode.info/
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
    if (typeof $(".tag-ctrl")['tagit'] != 'function') return;
    ractive.set('saveObserver', false);
    $('[data-bind]').each(function(i,d) {
      $(d).val(ractive.get($(d).data('bind'))).css('display','none');
    });

    if ($(".tag-ctrl").is(":ui-tagit")) $(".tag-ctrl").tagit('destroy');
    $(".tag-ctrl").tagit({
      allowSpaces: true,
      placeholderText: "Comma separated tags",
      readOnly: readOnly==true ? true : false,
      showAutocompleteOnFocus: true,
      afterTagAdded: function(event, ui) {
        ractive.set($(event.target).data('bind'),$(event.target).val());
      },
      afterTagRemoved: function(event, ui) {
        ractive.set($(event.target).data('bind'),$(event.target).val());
      }
    });
    ractive.set('saveObserver', true);
  },
  initialAccountStage: function() {
    console.log('initialAccountStage');
    var rtn = '';
    $.each(ractive.get('accountStages'), function(i,d) {
      if (parseInt(d['idx'])==0) rtn = d.name;
    });
    return rtn;
  },
  initialContactStage: function() {
    console.log('initialContactStage');
    var rtn = '';
    $.each(ractive.get('contactStages'), function(i,d) {
      if (parseInt(d['idx'])==0) rtn = d.name;
    });
    return rtn;
  },
  initialOrderStage: function() {
    console.log('initialOrderStage');
    var rtn = '';
    if (ractive.get('orderStages')==undefined) return rtn;
    $.each(ractive.get('orderStages'), function(i,d) {
      if (parseInt(d['idx'])==0) rtn = d.name;
    });
    return rtn;
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
      ractive.loadStandardPartial(d.name, ractive.getServer()+d.url);
    });
  },
  loadTenantConfig: function(tenant) {
    $.getJSON($env.tenantConfig, function(data) {
      ractive.set('tenant', data);
    });
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
      if (ractive['showResults'] != undefined) ractive.showResults();
      $( "#ajax-loader" ).hide();
    }, EASING_DURATION);
  },
  localId: function(uriOrObj) {
    if (uriOrObj == undefined) return;
    else if (typeof uriOrObj == 'object') return ractive.localId(ractive.uri(uriOrObj));
    else return uriOrObj.substring(uriOrObj.lastIndexOf('/')+1);
  },
  /** @deprecated use localId */
  shortId: function(uri) {
    return ractive.localId(uri);
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
  showLogin: function() {
    console.info('showLogin');
    $('#loginSect').slideDown();
  },
  showMessage: function(msg, additionalClass) {
    console.log('showMessage: '+msg);
    if (additionalClass == undefined) additionalClass = 'bg-info text-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages, .messages').empty().append(msg).removeClass().addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='bg-danger text-danger') setTimeout(function() {
      $('#messages, .messages').fadeOut();
    }, EASING_DURATION*10);
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
      }, EASING_DURATION*10);
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
  submitSocial: function(network) {
    console.log('submitSocial: '+network);
    ractive.set(network.keypath,ractive.get('current.network.value'));
    $('#socialModalSect').modal('hide');
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
    // TODO
  },
  submitCustomAction: function() {
    // TODO
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
  switchToTenant: function() {
    // TODO
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

    if (headings === undefined || headings == true) {
      // extract label from json fields in array idx 0
      var row = '';

      for (var idx = 0 ; idx < arr[0].length ; idx++) {
          row += idx + ',';
      }

      row = row.slice(0, -1); // strip trailing comma
      headings = row + '\n';
    }
    csv += headings + '\n';

    var propNames = headings.split(',');
    for (var i = 0; i < arr.length; i++) {
        var row = '';

        for (var j = 0 ; j < propNames.length ; j++) {
          try {
            var val = eval('arr['+i+'].'+propNames[j]);
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
    try {
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
    } catch (e) {
      console.error('Cannot get URI of '+JSON.stringify(entity)+'. '+e);
    }
    ractive.set('saveObserver', saveObserver);
    return uri;
  }
});

$(document).ready(function() {
  ractive.set('saveObserver', false);
  //ajax loader
  if ($('#ajax-loader').length==0) $('body').append('<div id="ajax-loader"><img class="ajax-loader" src="'+ractive.getServer()+'/images/ajax-loader.gif" style="width:10%" alt="Loading..."/></div>');
  $( document ).ajaxStart(function() {
    $( "#ajax-loader" ).show();
  });
  $( document ).ajaxStop(function() {
    $( "#ajax-loader" ).hide();
  });

  ractive.loadStandardPartials(ractive.get('stdPartials'));
  ractive.loadTenantConfig();

  $( document ).ajaxComplete(function( event, jqXHR, ajaxOptions ) {
    if (jqXHR.status > 0) ractive.showReconnected();
  });

  ractive.observe('tenant', function(newValue, oldValue, keypath) {
    console.log('tenant changed');
    if ((oldValue == undefined || oldValue.id == '') && newValue != undefined && newValue.id != '' && ractive['fetch'] != undefined) {
      ractive.fetch();
    }

    if (newValue!=undefined && newValue.accountFields!=undefined && newValue.accountFields.length>0) {
      for(var idx = 0 ; idx<newValue.accountFields.length ; idx++) {
        if (newValue.accountFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if (ractive['fetchAccounts']!=undefined) ractive.fetchAccounts();
        } else if (newValue.accountFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if (ractive['fetchContacts']!=undefined) ractive.fetchContacts();
        }
      }
    }
    if (newValue!=undefined && newValue.contactFields!=undefined && newValue.contactFields.length>0) {
      for(var idx = 0 ; idx<newValue.contactFields.length ; idx++) {
        if (newValue.contactFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if (ractive['fetchAccounts']!=undefined) ractive.fetchAccounts();
        } else if (newValue.contactFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if (ractive['fetchContacts']!=undefined) ractive.fetchContacts();
        }
      }
    }
    if (newValue!=undefined && newValue.orderFields!=undefined && newValue.orderFields.length>0) {
      for(var idx = 0 ; idx<newValue.orderFields.length ; idx++) {
        if (newValue.orderFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if (ractive['fetchAccounts']!=undefined) ractive.fetchAccounts();
        } else if (newValue.orderFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if (ractive['fetchContacts']!=undefined) ractive.fetchContacts();
        }
      }
    }
    if (newValue!=undefined && newValue.orderItemFields!=undefined && newValue.orderItemFields.length>0) {
      for(var idx = 0 ; idx<newValue.orderItemFields.length ; idx++) {
        if (newValue.orderItemFields[idx].type=='account' && $('datalist#accounts').length==0) {
          if (ractive['fetchAccounts']!=undefined) ractive.fetchAccounts();
        } else if (newValue.orderItemFields[idx].type=='contact' && $('datalist#contacts').length==0) {
          if (ractive['fetchContacts']!=undefined) ractive.fetchContacts();
        }
      }
    }
  });

  ractive.on('sort', function ( event, column ) {
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

  ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
    console.log('searchTerm changed');
    if (typeof ractive['showResults'] == 'function') {
      $( "#ajax-loader" ).show();
      ractive.showResults();
    }
    setTimeout(ractive.showSearchMatched, EASING_DURATION);
  });

  var params = getSearchParameters();
  if (params['searchTerm']!=undefined) {
    ractive.set('searchTerm',decodeURIComponent(params['searchTerm']));
  } else if (params['q']!=undefined) {
    ractive.set('searchTerm',decodeURIComponent(params['q']));
  }
  if (params['s']!=undefined) {
    ractive.set('sortColumn',decodeURIComponent(params['s']));
  }
  if (params['asc']!=undefined && params['asc']=='true') {
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
}
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
}
Array.uniq = function(fieldName, arr) {
  // console.info('uniq');
  if (arr == undefined) return undefined;
  list = '';
  for (var idx = 0 ; idx < arr.length ; idx++) {
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
