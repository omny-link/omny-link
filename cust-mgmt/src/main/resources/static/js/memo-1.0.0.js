var EASING_DURATION = 500;
fadeOutMemos = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    memos: [],
    filter: undefined,
    //saveObserver:false,
    entityPath: '/memos',
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    chars: function(string) {
      console.info('chars: '+string);
      var len = string == undefined ? 0 : string.length;
      console.log('  returning: '+len);
      return len;
    },
    customField: function(obj, name) {
      if (obj['customFields']==undefined) {
        return undefined;
      } else if (!Array.isArray(obj['customFields'])) {
        return obj.customFields[name];
      } else {
        //console.error('customField 30');
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
      else return ractive.get('tenant.show.'+feature);
    },
    formatDate: function(timeString) {
      return new Date(timeString).toLocaleDateString(navigator.languages).replace('Invalid Date','n/a').replace('01/01/1970','n/a');
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
    hash: function(email) {
      if (email == undefined) return '';
      console.log('hash '+email+' = '+ractive.hash(email));
      return '<img class="img-rounded" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    helpUrl: '//omny.link/user-help/memo/#the_title',
    matchFilter: function(obj) {
      if (ractive.get('filter')==undefined) return true;
      else return ractive.get('filter').value.toLowerCase()==obj[ractive.get('filter').field].toLowerCase();
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
    saveObserver: false,
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
    sortAsc: true,
    sortColumn: 'name',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/memo-navbar.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "memoListSect", "url": "/partials/memo-list-sect.html"},
      { "name": "currentMemoSect", "url": "/partials/memo-current-sect.html"}
    ],
    title: "Template Library"
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var message = { owner:ractive.get('username'), status:'Draft', tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select(-1, message );
  },
  clone: function(message) {
    console.log('clone');
    ractive.set('saveObserver', false);

    var newMemo = JSON.parse(JSON.stringify(message));
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/'+ractive.id(message)+'/clone',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(newMemo),
      success: completeHandler = function(data, textStatus, jqXHR) {
        //console.log('data: '+ data);
        var location = jqXHR.getResponseHeader('Location');
        ractive.set('saveObserver',false);
        if (location != undefined) ractive.set('current._links.self.href',location);
        if (jqXHR.status == 201) {
          ractive.set('currentIdx',ractive.get('memos').push(ractive.get('current'))-1);
        }
        ractive.push('memos', data);
        ractive.showResults();
        ractive.showMessage('Memo copied');
        ractive.set('saveObserver',true);
      }
    });
    ractive.set('saveObserver', true);
  },
  download: function() {
    console.info('download');
    $.ajax({
      headers: { 
        "Accept": "text/csv; charset=utf-8",
        "Content-Type": "text/csv; charset=utf-8"
      },
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/',
      crossDomain: true,
      success: function( data ) {
        console.warn('response;'+data);
        something = window.open("data:text/csv," + encodeURIComponent(data),"_blank");
        //something.focus();
      }   
    });
  },
  edit: function (idx, message) {
    console.log('edit'+message+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.select(idx,message);
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  delete: function (obj) {
    console.log('delete '+obj+'...');
    $.ajax({
      url: ractive.getServer()+ractive.uri(obj),
      type: 'DELETE',
      success: completeHandler = function(data) {
        ractive.fetch();
        ractive.toggleResults();
      }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  fetch: function () {
    console.log('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/?projection=complete',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('memos', data);
        }else{
          ractive.merge('memos', data['_embedded'].memos);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#memosTable tbody tr:visible').length);
        ractive.initEditor();
        ractive.set('saveObserver', true);
      }
    });
  },
  filter: function(field,value) {
    console.log('filter: field '+field+' = '+value);
    if (value==undefined) value = ractive.get('tenant.stagesInActive');
    if (field==undefined) ractive.set('filter',undefined);
    else ractive.set('filter',{field: field,value: value});
    ractive.set('searchMatched',$('#memosTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(messageId) { 
    console.log('find: '+messageId);
    var c; 
    $.each(ractive.get('memos'), function(i,d) { 
      if (messageId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  getId: function(message) { 
    console.log('getId: '+message);
    var uri; 
    if (message['links']!=undefined) {
      $.each(message.links, function(i,d) { 
        if (d.rel == 'self') { 
          uri = d.href;
        }
      });
    } else if (message['_links']!=undefined) {
      uri = message._links.self.href.indexOf('?')==-1 ? message._links.self.href : message._links.self.href.substr(0,message._links.self.href.indexOf('?')-1);
    } 
    return uri;
  },
  initEditor: function() {
    console.info('initEditor');
    ractive.editor = new wysihtml5.Editor("curRichContent", {
      parserRules: wysihtml5ParserRules,
      stylesheets: ["//yui.yahooapis.com/2.9.0/build/reset/reset-min.css", "/css/wysihtml5/editor.css"],
      toolbar:     "wysihtml5-editor-toolbar"
    });
  },
  initEditorContent: function(enabled) {
    console.log('loading wysihtml5');
    if (ractive.get('current.richContent')!=undefined) {
      console.log('  ...'+ractive.get('current.richContent'));
      ractive.editor.setValue(ractive.get('current.richContent'));
    }
    var composer = ractive.editor.composer;
    var h1 = ractive.editor.composer.element.querySelector("h1");
    if (h1) {
      composer.selection.selectNode(h1);
    }
    // don't seem to be able to do this in CSS
    $('.wysihtml5-sandbox')
      .css('border-color','#ccc')
      .css('margin-left',0);
    if (!enabled) ractive.editor.disable();
    
    ractive.editor.on("change", function() {
      console.log('wysihtml5 changed value to: '+ractive.editor.getValue());
      if (ractive.get('current')==undefined) {
        $('#currentForm :invalid').addClass('field-error');
        $('#currentForm :invalid')[0].focus();
        ractive.showMessage('Cannot save yet as message is incomplete');
      } else {
        ractive.set('current.richContent',ractive.editor.getValue());
        ractive.set('current.plainContent',stripTags(ractive.editor.composer.getValue()));
      }
    });
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save message: '+ractive.get('current').name+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    ractive.set('saveObserver',true);
    if (document.getElementById('currentForm')==undefined) {
      // loading... ignore
    } else if(document.getElementById('currentForm').checkValidity()) {
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      tmp.tenantId = ractive.get('tenant.id');
      tmp.richContent = ractive.editor.getValue();
      tmp.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/memos/' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          if (jqXHR.status == 201) {
            ractive.set('currentIdx',ractive.get('memos').push(ractive.get('current'))-1);
          }
          if (jqXHR.status == 204) ractive.splice('memos',ractive.get('currentIdx'),1,ractive.get('current'));

          ractive.showMessage('Memo saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      $('#currentForm :invalid').addClass('field-error');
      $('#currentForm :invalid')[0].focus();
      ractive.showMessage('Cannot save yet as message is incomplete');
    }
  },
  select: function(idx,message) {
    console.log('select: '+JSON.stringify(message));
    ractive.set('currentIdx',idx);
    ractive.set('saveObserver',false);
    // default owner to current user
    if (message.owner == undefined || message.owner == '') message.owner = ractive.get('username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (message._links == undefined && message.links != undefined) { 
	    message._links = message.links;
	    $.each(message.links, function(i,d) { 
        if (d.rel == 'self') message._links.self = { href:d.href };
      });
	  }
	  if (message._links != undefined) {
	    var url = ractive.uri(message); // includes getServer
      if (url == undefined) {
        ractive.showError('No memo selected, please check link');
        return;
      }
	    console.log('loading detail for '+url);
	    $.getJSON(url+'?projection=complete',  function( data ) {
        console.log('found message '+data);
        ractive.set('current', data);
        ractive.initControls();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        if (ractive.get('current.status')=='Published') { 
          $('#currentForm input,#currentForm select,#currentForm textarea').prop('disabled',true).prop('readonly',true);
          $('.glyphicon-remove').remove();
          ractive.initEditorContent(false);
        } else {
          ractive.initEditorContent(true);
        }
        ractive.editor.setValue(ractive.get('current.richContent'));
        ractive.set('saveObserver',true);
      });
    } else { 
      console.log('Skipping load as no _links.'+message.lastName);
      ractive.set('current', message);
      ractive.set('saveObserver',true);
    }
	  ractive.toggleResults();
	  $('#currentSect').slideDown();
  },
  showActivityIndicator: function(msg, addClass) {
    dolastNcument.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showResults: function() {
    $('#memosTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#memosTable').slideDown({ queue: true });
  },
  sortMemos: function() {
    ractive.get('memos').sort(function(a,b) { return new Date(b.lastUpdated)-new Date(a.lastUpdated); });
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#memosTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#memosTable').slideToggle();
  }
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#memosTable tbody tr').length);
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
    console.warn  ('Skipped message save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

function crToSpace(string) {
  return string.replace(/<br>/g,' ');
}

function stripTags(string) {
  return crToSpace(string.replace(/<br>/g,' ').replace(/<\/?[^>]+(>|$)/g, ""));
}
