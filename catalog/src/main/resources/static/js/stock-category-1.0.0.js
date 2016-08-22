var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    csrfToken: getCookie(CSRF_COOKIE),
    entityPath: '/stock-categories',
    stockCategories: [],
    filter: {field: "stage", operator: "!in", value: "cold,complete"},
    //saveObserver:false,
    title: 'Stock Management',
    username: localStorage['username'],
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
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return (timeString==undefined || timeString == "-1") ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleDateString(navigator.languages);
    },
    formatJson: function(json) { 
      console.log('formatJson: '+json);
      try {
        var obj = JSON.parse(json);
        var html = '';
        $.each(Object.keys(obj), function(i,d) {
          html += (typeof obj[d] == 'object' ? '' : '<b>'+d+'</b>: '+obj[d]+'<br/>');
        });
        return html;
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
    helpUrl: 'http://omny.link/user-help/stock-categories/#the_title',
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
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.status!=undefined && obj.status.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created>') && new Date(obj.firstStockCategory)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created<') && new Date(obj.firstStockCategory)<new Date(ractive.get('searchTerm').substring(8)))
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
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/stock-category-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "stockCategoryListSect", "url": "/partials/stock-category-list-sect.html"},
      { "name": "currentImageSect", "url": "/partials/image-current-sect.html"},
      { "name": "currentStockCategorySect", "url": "/partials/stock-category-current-sect.html"},
    ],
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var stockCategory = { stockCategory: {}, author:ractive.get('username'), tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select( stockCategory );
    ractive.initTags();
  },
  delete: function (obj) {
    console.log('delete '+obj+'...');
    var url = obj.links != undefined
        ? obj.links.filter(function(d) { console.log('this:'+d);if (d['rel']=='self') return d;})[0].href
        : obj._links.self.href;
    $.ajax({
        url: url,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.toggleResults();
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          console.error('XX: '+errorThrown);
          ractive.handleError(jqXHR,textStatus,errorThrown);
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  edit: function (stockCategory) {
    console.log('edit'+stockCategory+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('currentIdx',ractive.get('stockCategories').indexOf(stockCategory));
    ractive.select( stockCategory );
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
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/stock-categories/?projection=complete',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('stockCategories', data);
        } else {
          ractive.merge('stockCategories', data['_embedded'].stockCategories);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#stockCategoriesTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }
    });
  },
  fetchStockCategories: function () {
    console.info('fetchStockCategories...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/stock-categories/',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('stockCategories', data);
        } else {
          ractive.merge('stockCategories', data['_embedded'].stockCategories);
        }
        // set up stockCategory typeahead
        var stockCategories = jQuery.map( ractive.get('stockCategories'), function( n, i ) {
          return ( {  "id": ractive.getId(n), "name": n.name } );
        });
        ractive.set('stockCategoriesDropDown',stockCategories);
        $('#curStockCategory').typeahead({
          items:'all',
          minLength:0,
          source:stockCategories,
//          updater:function(item) {
//            return item.id;
//          }
        });
//        $('#curCompanyName').on("click", function (ev) {
//          newEv = $.Event("keydown");
//          newEv.keyCode = newEv.which = 40;
//          $(ev.target).trigger(newEv);
//          return true;
//        });
        ractive.set('saveObserver', true);
      }
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected')
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.set('searchMatched',$('#stockCategoriesTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(stockCategoryId) { 
    console.log('find: '+stockCategoryId);
    var c; 
    $.each(ractive.get('stockCategories'), function(i,d) { 
      if (stockCategoryId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save stockCategory: '+ractive.get('current').lastName+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) { 
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      delete tmp.images;
      delete tmp.stockItems;
      if (id != undefined && tmp.stockCategory != undefined && Object.keys(tmp.stockCategory).length > 0 && tmp.stockCategory.id != undefined) {
        tmp.stockCategory = id.substring(0,id.indexOf('/',8))+'/stock-categories/'+tmp.stockCategory.id;  
      } else {
        delete tmp.stockCategory;
        delete tmp.stockCategoryId;
      }       
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save stockCategory'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? '/'+tmp.tenantId+'/stock-categories/' : ractive.tenantUri(tmp),
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
            ractive.set('current.fullName',ractive.get('current.firstName')+' '+ractive.get('current.lastName'));
            var currentIdx = ractive.get('stockCategories').push(ractive.get('current'))-1;
            ractive.set('currentIdx',currentIdx);
            break;
          case 204: 
            ractive.splice('stockCategories',ractive.get('currentIdx'),1,ractive.get('current'));
            break;
          }
          ractive.showMessage(ractive.get('tenant.strings.stockCategory')+' saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as stockCategory is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as '+ractive.get('tenant.strings.stockCategory')+' is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  select: function(stockCategory) {
    console.log('select: '+JSON.stringify(stockCategory));
    ractive.set('saveObserver',false);
    if (stockCategory.stockCategory == undefined || stockCategory.stockCategory == '') stockCategory.stockCategory = new Object();
    // default owner to current user
    if (stockCategory.owner == undefined || stockCategory.owner == '') stockCategory.owner = ractive.get('username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (stockCategory._links == undefined && stockCategory.links != undefined) { 
	    stockCategory._links = stockCategory.links;
	    $.each(stockCategory.links, function(i,d) { 
        if (d.rel == 'self') stockCategory._links.self = { href:d.href };
      });
	  }
	  if (stockCategory._links != undefined) {
	    var url = ractive.stripProjection(stockCategory._links.self.href);
	    if (url == undefined) {
	      ractive.showError('No stockCategory selected, please check link');
	      return;
	    }
	    console.log('loading detail for '+url);
	    $.getJSON(ractive.getServer()+url+'?projection=complete', function( data ) {
        console.log('found stockCategory '+data);
        if (data['id'] == undefined) data.id = ractive.id(data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
//        ractive.fetchNotes();
//        ractive.fetchDocs();
        ractive.set('saveObserver',true);
      });
    } else { 
      console.log('Skipping load as no _links.'+stockCategory.lastName);
      ractive.set('current', stockCategory);
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
    $('#stockCategoriesTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#stockCategoriesTable').slideDown({ queue: true });
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#stockCategoriesTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#stockCategoriesTable').slideToggle();
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
  upload: function (formId) {
    console.log('upload:'+formId);
    ractive.showMessage('Uploading ...');
  
    var formElement = document.getElementById(formId);
    var formData = new FormData(formElement);
    var entity = $('#'+formId+' .entity').val();
    var fileName = $('#'+formId+' input[type="file"]').val();
    var fileExt = fileName.substring(fileName.lastIndexOf('.')+1);
    return $.ajax({
        type: 'POST',
        url: '/'+ractive.get('tenant.id')+'/'+entity+'/upload'+fileExt.toLowerCase(),
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
  //        console.log('successfully uploaded data');
          ractive.showMessage('Successfully uploaded '+response.length+' records');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          ractive.handleError(jqXHR, textStatus, errorThrown);
        }
      });
  }
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#stockCategoriesTable tbody tr').length);
  }, 500);
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.documents','current.doc','current.notes','current.note'];
  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    ractive.save();
  } else { 
    console.warn  ('Skipped stockCategory save of '+keypath);
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

