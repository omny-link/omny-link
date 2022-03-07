/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributorss
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
var ractive = new BaseRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    memos: [],
    filter: undefined,
    entityName: 'memo',
    entityPath: '/memos',
    customField: function(obj, name) {
      if (!('customFields' in obj)) {
        return undefined;
      } else if (!Array.isArray(obj.customFields)) {
        return obj.customFields[name];
      } else {
        var val;
        $.each(obj.customFields, function(i,d) {
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
    gravatar: function(email) {
      return ractive.gravatar(email);
    },
    helpUrl: '//omny-link.github.io/user-help/memo/#the_title',
    matchFilter: function(obj) {
      if (ractive.get('filter')==undefined) return true;
      else return ractive.get('filter').value.toLowerCase()==obj[ractive.get('filter').field].toLowerCase();
    },
    matchRole: function(role) {
      console.info('matchRole: '+role);
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    matchSearch: function(obj) {
      if (ractive.get('searchTerm')==undefined || ractive.get('searchTerm').length==0) {
        return true;
      } else {
        var search = ractive.get('searchTerm').split(' ');
        for (var idx = 0 ; idx < search.length ; idx++) {
          var searchTerm = search[idx].toLowerCase();
          var match = ( (ractive.localId(obj)!=undefined && ractive.localId(obj).indexOf(searchTerm)>=0) ||
              (obj.name.toLowerCase().indexOf(searchTerm.toLowerCase())>=0) ||
              (obj.title.toLowerCase().indexOf(searchTerm.toLowerCase())>=0) ||
              (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('created>') && new Date(obj.created)>new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('created<') && new Date(obj.created)<new Date(ractive.get('searchTerm').substring(8))) ||
              (searchTerm.startsWith('status:') && obj.status!=undefined && obj.status.toLowerCase().indexOf(ractive.get('searchTerm').substring(7))!=-1)
          );
          //no match is definitive but matches may fail other terms (AND logic)
          if (!match) return false;
        }
        return true;
      }
    },
    saveObserver: false,
    server: $env.server,
    sort: function (array, column, asc) {
      return ractive.sortBy(array, column, asc);
    },
    sortAsc: true,
    sortColumn: 'name',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc';
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "navbar", "url": "/partials/memo-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "memoListSect", "url": "/partials/memo-list-sect.html"},
      { "name": "currentMemoSect", "url": "/partials/memo-current-sect.html"},
      { "name": "currentMemoSignatorySect", "url": "/partials/memo-current-signatory-sect.html"}
    ],
    title: "Template Library"
  },
  partials: {
    helpModal: '',
    navbar: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    titleArea: '',
    memoListSect: '',
    currentMemoSect: ''
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var message = {
        owner: ractive.get('profile.username'),
        shortContent: 'Not currently used',
        status:'Draft',
        tenantId: ractive.get('tenant.id'),
        url: undefined
    };
    ractive.select(-1, message);
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
      success: function(data, textStatus, jqXHR) {
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
      url: ractive.tenantUri(obj),
      type: 'DELETE',
      success: function() {
        ractive.fetch();
        $('#currentSect').slideUp();
      }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  fetch: function () {
    console.log('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/',
      crossDomain: true,
      success: function( data ) {
        if ('_embedded' in data) {
          ractive.merge('memos', data._embedded.memos);
        } else {
          ractive.merge('memos', data);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#memosTable tbody tr:visible').length);
        ractive.showSearchMatched();
        ractive.set('saveObserver', true);
      }
    });
  },
  find: function(messageId) {
    console.log('find: '+messageId);
    var c;
    $.each(ractive.get('memos'), function(i,d) {
      if (messageId.endsWith(ractive.id(d))) {
        c = d;
      }
    });
    return c;
  },
  findRequiredVars: function() {
    console.info('findRequiredVars');
    var rich = CKEDITOR.instances.curRichContent.getData();
    var requiredVars = [];
    var idx = 0;
    while ((idx = rich.indexOf('${', idx)) != -1) {
      var endIdx = rich.indexOf('}', idx);
      var dotIdx = rich.indexOf('.', idx);
      var nextIdx = dotIdx == -1 || dotIdx > endIdx ? endIdx : dotIdx;
      var obj = rich.substring(idx+2, nextIdx);
      if (requiredVars.indexOf(obj)==-1) requiredVars.push(obj);
      idx = nextIdx;
    }
    return requiredVars.join();
  },
  initEditor: function() {
    console.info('initEditor');
    if ('curRichContent' in CKEDITOR.instances) {
      try {
        if (ractive.get('current.status')=='Published') {
          CKEDITOR.instances.curRichContent.setReadOnly(true);
        } else {
          CKEDITOR.instances.curRichContent.setReadOnly(false);
        }
      } catch (e) {
        console.warn('setReadOnly raises error first time but is apparently ignorable.');
      }
    } else {
      CKEDITOR.replace( 'curRichContent' );
      CKEDITOR.instances.curRichContent.on('blur', ractive.save);
    }
      CKEDITOR.instances.curRichContent.setData(ractive.get('current.richContent'));
  },
  save: function () {
    console.log('save message: '+ractive.get('current').name+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    ractive.set('saveObserver',true);
    if (document.getElementById('currentForm')==undefined) {
      // loading... ignore
    } else if(document.getElementById('currentForm').checkValidity()) {
      ractive.set('current.requiredVars',ractive.findRequiredVars());
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      tmp.tenantId = ractive.get('tenant.id');
      tmp.richContent = CKEDITOR.instances.curRichContent.getData();
      if ('signatories' in tmp) {
        if (tmp.signatories[0] != undefined) delete tmp.signatories[0].signHereTabs;
        if (tmp.signatories[1] != undefined) delete tmp.signatories[1].signHereTabs;
      }
      $.ajax({
        url: id === undefined ?
            ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/' :
            ractive.tenantUri(tmp),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          ractive.fetch();

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
    if (message.owner == undefined || message.owner == '') message.owner = ractive.get('profile.username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (message._links == undefined && message.links != undefined) {
	    message._links = message.links;
	    $.each(message.links, function(i,d) {
        if (d.rel == 'self') message._links.self = { href:d.href };
      });
	  }
	  if (message._links != undefined) {
	    var url = ractive.tenantUri(message); // includes getServer
      if (url == undefined) {
        ractive.showError('No memo selected, please check link');
        return;
      }
	    console.log('loading detail for '+url);
	    $.getJSON(url,  function( data ) {
        console.log('found message '+data);
        ractive.set('saveObserver',false);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initEditor();
        if (ractive.get('current.status')=='Published') {
          $('#currentForm input,#currentForm select,#currentForm textarea').prop('disabled',true).prop('readonly',true);
          $('.glyphicon-remove').hide();
        } else {
          $('#currentForm input:not("#curRequiredVars"),#currentForm select,#currentForm textarea').prop('disabled',false).prop('readonly',false);
          if (ractive.hasRole('admin')) $('.glyphicon-remove').show();
        }
        ractive.set('saveObserver',true);
      });
    } else {
      console.log('Skipping load as no _links.'+message.lastName);
      ractive.set('current', message);
      ractive.initEditor();
      ractive.set('saveObserver',true);
    }
    ractive.hideResults();
    $('#currentSect').slideDown({ queue: true });
  }
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete
// controls done that way save the oldValue
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  var ignored=['current.documents','current.doc','current.notes','current.note'];
  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    ractive.save();
  } else {
    console.warn  ('Skipped message save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});
/* TODO check nt needed
function crToSpace(string) {
  return string.replace(/<br>/g,' ');
}

function stripTags(string) {
  return crToSpace(string.replace(/<br>/g,' ').replace(/<\/?[^>]+(>|$)/g, ""));
}*/
