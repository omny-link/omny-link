/*******************************************************************************
 * Copyright 2015-2018 Tim Stephenson and contributors
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

var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    contacts: [],
    entityPath: '/stock-items',
    stockCategories: [],
    stockItems: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    saveObserver:false,
    server: $env.server,
    title: 'Stock Management',
    customField: function(obj, name) {
      if (obj['customFields']==undefined) {
        return undefined;
      } else if (!Array.isArray(obj['customFields'])) {
        return obj.customFields[name];
      } else {
        var val;
        $.each(obj['customFields'], function(i,d) {
          if (d.name == name) val = d.value;
        });
        return val;
      }
    },
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      if (timeString == "-1" || isNaN(timeString)) return 'n/a';
      else return i18n.getDurationString(timeString) + ' ago';
    },
    formatAgeFromDate: function(timeString) {
      if (timeString==undefined) return;
      return i18n.getAgeString(ractive.parseDate(timeString))
    },
    formatContent: function(content) {
      console.info('formatContent:'+content);
      content = content.replace(/\n/g,'<br/>');
      content = ractive.autolinker().link(content);
      return content;
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    formatDateTime: function(timeString) {
      if (timeString==undefined) return 'n/a';
      var dts = new Date(timeString).toLocaleString(navigator.languages);
      // remove secs
      if (dts.split(':').length>1) dts = dts.substring(0, dts.lastIndexOf(':'));
      return dts;
    },
    formatFavorite: function(obj) {
      if (obj['favorite']) return 'glyphicon-star';
      else return 'glyphicon-star-empty';
    },
    formatJson: function(json) {
      console.log('formatJson: '+json);
      try {
        var obj = json;
        if (typeof json == 'string') obj = JSON.parse(json);
        return ractive.json2Html(obj);
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    formatTags: function(tags) {
      var html = '';
      if (tags==undefined) return html;
      var tagArr = tags.split(',');
      $.each(tagArr, function(i,d) {
        html += '<span class="img-rounded" style="background-color:'+d+'">&nbsp;&nbsp;</span>';
      });
      return html;
    },
    gravatar: function(email) {
      if (email == undefined) return '';
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    haveCustomExtension: function(extName) {
      return Array.findBy('name',ractive.get('tenant.id')+extName,ractive.get('tenant.partials'))!=undefined;
    },
    helpUrl: '//omny-link.github.io/user-help/stock-items/',
    matchFilter: function(obj) {
      var filter = ractive.get('filter');
      //console.info('matchFilter: '+JSON.stringify(filter));
      if (filter==undefined) {
        return true;
      } else {
        try {
          if (filter.operator=='in') {
            var values = filter.value.toLowerCase().split(',');
            return values.indexOf(obj[filter.field].toLowerCase())!=-1;
          } else if (filter.operator=='!in') {
            var values = filter.value.toLowerCase().split(',');
            return values.indexOf(obj[filter.field].toLowerCase())==-1;
          } else {
            if (filter.operator==undefined) filter.operator='==';
            return eval("'"+filter.value.toLowerCase()+"'"+filter.operator+"'"+obj[filter.field].toLowerCase()+"'");
          }
        } catch (e) {
          //console.debug('Exception during filter, probably means record does not have a value for the filtered field');
          return true;
        }
      }
    },
    matchRole: function(role) {
      console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    matchSearch: function(obj) {
      var searchTerm = ractive.get('searchTerm');
      //console.info('matchSearch: '+searchTerm);
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.selfRef.indexOf(searchTerm.toLowerCase())>=0)
          || (obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.stockCategoryName!=undefined && obj.stockCategoryName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.status!=undefined && obj.status.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created>') && new Date(obj.firstStockItem)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created<') && new Date(obj.firstStockItem)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('#') && obj.tags.toLowerCase().indexOf(ractive.get('searchTerm').substring(1).toLowerCase())!=-1)
        );
      }
    },
    selectMultiple: [],
    sort: function (array, column, asc) {
      console.info('sort '+(asc ? 'ascending' : 'descending')+' on: '+column);
      array = array.slice(); // clone, so we don't modify the underlying data

      return array.sort( function ( a, b ) {
        if (b[column]==undefined || b[column]==null || b[column]=='') {
          return (a[column]==undefined || a[column]==null || a[column]=='') ? 0 : -1;
        } else if (asc) {
          return a[ column ] < b[ column ] ? -1 : 1;
        } else {
          return a[ column ] > b[ column ] ? -1 : 1;
        }
      });
    },
    sortAsc: false,
    sortColumn: 'lastUpdated',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc';
      else return 'hidden';
    },
    stdPartials: [
      { "name": "customActionModal", "url": "/partials/custom-action-modal.html"},
      { "name": "fieldExtension", "url": "/partials/field-extension.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/stock-item-navbar.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "stockItemListSect", "url": "/partials/stock-item-list-sect.html"},
      { "name": "currentDocumentListSect", "url": "/webjars/supportservices/3.0.0/partials/current-document-list-sect.html"},
      { "name": "currentNoteListSect", "url": "/webjars/supportservices/3.0.0/partials/current-note-list-sect.html"},
      { "name": "currentImageSect", "url": "/partials/image-current-sect.html"},
      { "name": "currentOfferSect", "url": "/partials/offer-current-sect.html"},
      { "name": "currentStockItemSect", "url": "/partials/stock-item-current-sect.html"},
      { "name": "currentStockItemExtensionSect", "url": "/partials/stock-item-extension.html"}
    ],
  },
  partials: {
    'profileArea': '',
    'titleArea': '',
    'loginSect': '',
    'stockItemListSect': '',
    'currentStockItemSect': '',
    'poweredBy': '',
    'sidebar': '',
    'helpModal': '',
    'customActionModal': '',
    'supportBar': ''
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var stockItem = {
        name: ractive.get('tenant.strings.stockItem') == undefined ? 'Stock Item' : ractive.get('tenant.strings.stockItem'),
        tenantId: ractive.get('tenant.id')
    };
    ractive.select( stockItem );
    ractive.initTags();
  },
  addImage: function (stockItem) {
    console.log('addImage '+stockItem+' ...');
    if (stockItem==undefined || stockItem == '') {
      ractive.showMessage('You must have created your '+ractive.get('tenant.strings.stockItem')+' before adding images');
      return;
    }
    ractive.set('current.image', {
      author: (window['$auth'] == undefined ? undefined : $auth.getClaim('sub')),
      stockItem: ractive.uri(stockItem),
      url: undefined
    });
    $('#imagesTable tr:nth-child(1)').slideDown();
  },
  delete: function (obj) {
    var url = ractive.tenantUri(obj);
    console.info('delete '+obj+'...');
    $.ajax({
        url: url,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.showResults();
//        },
//        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
//          console.error(textStatus+': '+errorThrown);
//          ractive.showError('Unable to delete '+obj.name+' at this time');
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  deleteImage: function (obj) {
    var url = ractive.tenantUri(obj);
    console.info('delete '+obj+'...');
    $.ajax({
        url: url,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetchImages();
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          console.error(textStatus+': '+errorThrown);
          ractive.showError('Unable to delete '+image.url+' at this time');
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  edit: function (stockItem) {
    console.log('edit'+stockItem+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('currentIdx',ractive.get('stockItems').indexOf(stockItem));
    ractive.select( stockItem );
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/stock-items/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('stockItems', data);
        } else {
          ractive.merge('stockItems', data['_embedded'].stockItems);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.showSearchMatched();
        ractive.set('saveObserver', true);
      }
    });
    ractive.fetchStockCategories();
  },
  fetchImages: function() {
    var showImages = ractive.get('tenant.features.stockItemImages');
    if (showImages == undefined || showImages != true) return;
    $.getJSON(ractive.tenantUri(ractive.get('current'))+'/images',  function( data ) {
      ractive.set('saveObserver',false);
      console.log('found '+data.length+' images');
      ractive.set('current.images', data);
      $.each(ractive.get('current.images'), function(i,d) {
        if (typeof d.created == 'string') d.created = Date.parse(d.created);
        d.age = new Date()-d.created;
      });
      // sort most recent first
      ractive.get('current.images').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      ractive.set('saveObserver',true);
    });
  },
  fetchStockCategories: function () {
    console.info('fetchStockCategories...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/stock-categories/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('stockCategories', data);
        } else {
          ractive.merge('stockCategories', data['_embedded'].stockCategories);
        }
        // set up stockCategory typeahead
        var stockCategories = jQuery.map( ractive.get('stockCategories'), function( n, i ) {
          return ( {  "id": ractive.id(n), "name": n.name } );
        });
        ractive.set('stockCategoriesDropDown',stockCategories);
        ractive.addDataList({ name: "stockCategories" }, stockCategories);
        ractive.set('saveObserver', true);
      }
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.dropdown.dropdown-menu li').removeClass('selected')
    $('.dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.set('searchMatched',$('#stockItemsTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  findEntity: function(entityList, name, value) {
    console.log('findEntity: '+entityList+','+name+', '+value);
    var c = undefined;
    $.each(ractive.get(entityList), function(i,d) {
      if (d[name]==value) {
        c = d;
      }
    });
    return c;
  },
  initAccessControl: function() {
    if (!ractive.hasRole('product_owner')) {
      $('input,select,textarea').attr('disabled','disabled').attr('readonly','readonly');
      ractive.showMessage('You do not have permission to edit this record, only Product Owners may do that');
    }
  },
  oninit: function() {
    console.log('oninit');
  },
  save: function () {
    console.log('save stockItem: '+ractive.get('current').name+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) {
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      // ensure stock cat id up to date, don't use observer as fires after save
      if (ractive.get('current.stockCategory.name')!=undefined) {
        var stockCat = Array.findBy('name',ractive.get('current.stockCategory.name'),ractive.get('stockCategories'));
        if (stockCat != undefined) {
          ractive.set('current.stockCategory.id',ractive.id(stockCat));
        }
      }
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      delete tmp.images;
      delete tmp.tagsAsList;
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save stockItem'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/'+tmp.tenantId+'/stock-items/' : ractive.tenantUri(tmp),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          switch (jqXHR.status) {
          case 201:
            var currentIdx = ractive.get('stockItems').push(ractive.get('current'))-1;
            ractive.set('currentIdx',currentIdx);
            break;
          case 204:
            ractive.splice('stockItems',ractive.get('currentIdx'),1,ractive.get('current'));
            break;
          }
          ractive.showMessage((ractive.get('tenant.strings.stockItem') == undefined ? 'Stock item' : ractive.get('tenant.strings.stockItem')) +' saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as stockItem is invalid');
      $('#currentForm :invalid').addClass('field-error');
      var entityName = ractive.get('tenant.strings.stockItem') == undefined ? 'stock item' : ractive.get('tenant.strings.stockItem').toLowerCase();
      ractive.showMessage('Cannot save yet as '+entityName+' is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  saveNewImage: function () {
    console.log('saveNewImage '+JSON.stringify(ractive.get('current.image'))+' ...');
    var n = ractive.get('current.image');
    n.url = $('#image').val();
    var url = ractive.tenantUri(ractive.get('current'))+'/images';
    if (n.url.trim().length > 0) {
      $('#imagesTable tr:nth-child(1)').slideUp();
      $.ajax({
        url: url,
        type: 'POST',
        data: n,
        success: completeHandler = function(data) {
          console.log('data: '+ data);
          ractive.showMessage('Image link saved successfully');
          if (tenant.features.stockItemImages) ractive.fetchImages();
          $('#image').val(undefined);
        }
      });
    }
  },
  saveImage: function (obj) {
    console.log('saveImage '+JSON.stringify(obj)+' ...');
    $.ajax({
      url: ractive.tenantUri(obj),
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(obj),
      success: completeHandler = function(data) {
        console.log('data: '+ data);
        ractive.showMessage('Image link saved successfully');
        ractive.fetchImages();
      }
    });
  },
  select: function(stockItem) {
    console.log('select: '+JSON.stringify(stockItem));
    ractive.set('saveObserver',false);
    if (stockItem.stockCategory == undefined || stockItem.stockCategory == '') stockItem.stockCategory = new Object();
    // default owner to current user
    if ((stockItem.owner == undefined || stockItem.owner == '') && window['$auth']!=undefined) stockItem.owner = $auth.getClaim('sub');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (stockItem._links == undefined && stockItem.links != undefined) {
	    stockItem._links = stockItem.links;
	    $.each(stockItem.links, function(i,d) {
        if (d.rel == 'self') stockItem._links.self = { href:d.href };
      });
	  }
	  if (stockItem._links != undefined) {
	    var url = ractive.tenantUri(stockItem);
	    if (url == undefined) {
	      ractive.showError('No stockItem selected, please check link');
	      return;
	    }
	    console.log('loading detail for '+url);
	    $.getJSON(url, function( data ) {
        console.log('found stockItem '+data);
        ractive.set('saveObserver',false);
        if (data['id'] == undefined) data.id = ractive.id(data);
        ractive.set('current', data);
        if ('tenant.features.stockItemImages') ractive.fetchImages();
        ractive.initControls();
        ractive.initTags(!ractive.hasRole('product_owner'));
        ractive.initAccessControl();
        ractive.set('saveObserver',true);
      });
    } else {
      console.log('Skipping load as no _links.'+stockItem.name);
      ractive.set('current', stockItem);
      ractive.set('saveObserver',true);
    }
	  ractive.toggleResults();
	  $('#currentSect').slideDown();
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showResults: function() {
    $('#stockItemsTableToggle').addClass('kp-icon-caret-down').removeClass('kp-icon-caret-right');
    $('#currentSect').slideUp();
    $('#stockItemsTable').slideDown({ queue: true });
  },
  showSearchMatched: function() {
    ractive.set('searchMatched',$('#stockItemsTable tbody tr').length);
    if ($('#stockItemsTable tbody tr:visible').length==1) {
      var stockItemId = $('#stockItemsTable tbody tr:visible').data('href')
      var stockItem = Array.findBy('selfRef',stockItemId,ractive.get('stockItems'))
      ractive.edit( stockItem );
    }
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#stockItemsTableToggle').toggleClass('kp-icon-caret-down').toggleClass('kp-icon-caret-right');
    $('#stockItemsTable').slideToggle();
  },
  /**
   * Inverse of editField.
   */
  updateField: function (selector, path) {
    var tmp = $(selector).text();
    console.log('updateField '+path+' to '+tmp);
    ractive.set(path,tmp);
    $(selector).css('border-width','0px').css('padding','0px');
  },
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#stockItemsTable tbody tr').length);
  }, 500);
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete
// controls done that way save the oldValue
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.documents','current.doc','current.images','current.image','current.notes','current.note'];
  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    ractive.save();
  } else {
    console.warn('Skipped stockItem save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

ractive.on( 'filter', function ( event, filter ) {
  console.info('filter on '+JSON.stringify(event)+','+filter.idx);
  ractive.filter(filter);
});
ractive.on( 'sort', function ( event, column ) {
  console.info('sort on '+column);
  // if already sorted by this column reverse order
  if (this.get('sortColumn')==column) this.set('sortAsc', !this.get('sortAsc'));
  this.set( 'sortColumn', column );
});

