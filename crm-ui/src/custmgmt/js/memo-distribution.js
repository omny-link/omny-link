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
    contacts: [],
    entityName: 'memoDistribution',
    entityPath: '/memo-distributions',
    testMode: false,
    memoDistributions: [],
    memos: [],
    filter: undefined,
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.features.'+feature);
    },
    formatDate: function(timeString) {
      return new Date(timeString).toLocaleDateString(navigator.languages).replace('Invalid Date','n/a').replace('01/01/1970','n/a');
    },
    formatObj: function(obj) {
      console.log('formatObj:'+typeof obj+', stringified: '+JSON.stringify(obj));
      if (obj==undefined) return obj;
      try {
        var html = '<table class="table table-striped">';
        $.each(Object.keys(obj), function(i,d) {
          html += (typeof obj[d] == 'object' ? '' : '<tr><th style="text-align:right">'+d.toLabel()+'</th><td>'+obj[d]+'</td></tr>');
        });
        html += '</table>';
        return html;
      } catch (e) {
        // So it wasn't an object?
        console.error(e);
        return obj;
      }
    },
    helpUrl: '//omny-link.github.io/user-help/memo-distribution/#the_title',
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
      { "name": "navbar", "url": "/partials/memo-dist-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "supportBar", "url": "/partials/support-bar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "memoDistListSect", "url": "/partials/memo-dist-list-sect.html"},
      { "name": "currentMemoDistSect", "url": "/partials/memo-dist-current-sect.html"}
    ],
    title: "Distribution Centre"
  },
  partials: {
    helpModal: '',
    navbar: '',
    profileArea: '',
    sidebar: '',
    supportBar: '',
    titleArea: '',
    memoDistListSect: '',
    currentMemoDistSect: ''
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var distribution = { activities:[], owner: ractive.get('profile.username'), status: 'Draft', tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select(-1, distribution );
  },
  clone: function(distribution) {
    console.log('clone');
    if (distribution._links != undefined) {
      delete distribution._links;
    }
    if (distribution.links != undefined) {
      $.each(distribution.links, function(i,d) {
        if (d.rel == 'self') distribution.links.splice(i,1);
      });
    }
    distribution.name += ' (copy)';
    ractive.set('current', distribution);
    ractive.save();
  },
  delete: function(obj) {
    console.log('delete '+obj+'...');
    $.ajax({
      url: ractive.getServer()+ractive.uri(obj),
      type: 'DELETE',
      success: function() {
        ractive.fetch();
        $('#currentSect').slideUp();
      }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  download: function() {
    console.info('download');
    $.ajax({
      headers: {
        "Accept": "text/csv; charset=utf-8",
        "Content-Type": "text/csv; charset=utf-8"
      },
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memo-distributions/',
      crossDomain: true,
      success: function( data ) {
        console.warn('response;'+data);
        window.open("data:text/csv," + encodeURIComponent(data),"_blank");
      }
    });
  },
  edit: function (idx, distribution) {
    console.log('edit'+distribution+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.select(idx,distribution);
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  fetch: function() {
    console.log('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memo-distributions/',
      crossDomain: true,
      success: function( data ) {
        if ('_embedded' in data) {
          ractive.merge('memoDistributions', data._embedded.memoDistributions);
        } else {
          ractive.merge('memoDistributions', data);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#memoDistributionsTable tbody tr:visible').length);
        ractive.set('saveObserver',true);
      }
    });
  },
  fetchContacts: function () {
    console.log('fetchContacts...');
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/',
      crossDomain: true,
      success: function( data ) {
        console.log('Found '+data.length+' contacts.');
        if ('_embedded' in data) {
          ractive.merge('contacts', data._embedded.contacts);
        } else {
          ractive.merge('contacts', data);
        }
        var tags = [];
        $('#curRecipients').empty();
        $.each(data, function(i,d) {
          $('#curRecipients').append('<option value="'+d.email+'">'+d.email+'</option>');
          var contactTags = d.tags;
          if (contactTags!=undefined) {
            $.each(contactTags.split(','), function(j,e) {
              e = e.trim();
              console.log('  ...'+e);
              if (tags.indexOf(e)==-1) {
                tags.push(e);
                $('#curRecipients').append('<option value="'+e+'">'+e+'</option>');
              }
            });
          }
        });

        $('#curRecipients').chosen();
        $('#curRecipients option').removeAttr('selected');

        // select any recipients already attached to the distribution
        if (ractive.get('current.recipients')!=undefined) {
          $.each(ractive.get('current.recipients').split(','), function(i,d) {
            $('#curRecipients option[value="'+d.trim()+'"]').attr('selected','selected');
          });
          $('#curRecipients').trigger("chosen:updated");
        }
      }
    });
  },
  fetchMemos: function () {
    console.log('fetchMemos...');
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memos/',
      crossDomain: true,
      success: function( data ) {
        console.log('Found '+data.length+' memos.');
        if ('_embedded' in data) {
          ractive.merge('memos', data._embedded.memos);
        } else {
          ractive.merge('memos', data);
        }
        ractive.addDataList({ name: "memos" }, ractive.get('memos'));
       }
    });
  },
  fetchStatus: function() {
    console.info('fetchStatus');
    var campaignId = ractive.get('current.providerRef');
    ractive.sendMessage({
      name:"omny.campaignStatus",
      body:JSON.stringify({campaignId:campaignId}),
      callback:function(results) {
        results = JSON.parse(results);
        ractive.set('current.providerStatus',results);
      }
    });
  },
  find: function(distributionId) {
    console.log('find: '+distributionId);
    var c;
    $.each(ractive.get('memoDistributions'), function(i,d) {
      if (distributionId.endsWith(ractive.id(d))) {
        c = d;
      }
    });
    return c;
  },
  getMemoName: function(id) {
    console.log('getMemoName: '+id);
    if (id == undefined) return '';
    var name = '';
    $.each(ractive.get('memos'), function(i,d) {
      if (ractive.localId(d)==id) name = d.name;
    });
    return name;
  },
  save: function () {
    console.log('save distribution: '+ractive.get('current').name+'...');

    if (document.getElementById('currentForm')==undefined) {
      // loading... ignore
    } else if (document.getElementById('currentForm').checkValidity()) {
      ractive.set('saveObserver',false);
      var id = ractive.uri(ractive.get('current'));
//      if (ractive.get('current.recipients')!=undefined) {
//        ractive.set('current.recipients',ractive.get('current.recipients').split(","));
//      }
//      ractive.set('saveObserver',true);
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save distribution'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/memo-distributions' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          if (jqXHR.status == 201) {
            ractive.set('currentIdx',ractive.get('memoDistributions').push(ractive.get('current'))-1);
          }
          if (jqXHR.status == 204) ractive.splice('memoDistributions',ractive.get('currentIdx'),1,ractive.get('current'));

          ractive.showMessage('Distribution saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as distribution is incomplete');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as distribution is incomplete');
    }
  },
  select: function(idx,distribution) {
    console.log('select: '+JSON.stringify(distribution));
    ractive.set('currentIdx',idx);
    ractive.fetchContacts();
    ractive.fetchMemos();
    ractive.set('saveObserver',false);
    // default owner to current user
    if (distribution.owner == undefined || distribution.owner == '') distribution.owner = ractive.get('profile.username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (distribution._links == undefined && distribution.links != undefined) {
	    distribution._links = distribution.links;
	    $.each(distribution.links, function(i,d) {
        if (d.rel == 'self') distribution._links.self = { href:d.href };
      });
	  }
	  if (distribution._links != undefined) {
      var url = ractive.uri(distribution); // includes getServer
      if (url == undefined) {
        ractive.showError('No distribution selected, please check link');
        return;
      }
	    console.log('loading detail for '+url);
	    $.getJSON(ractive.getServer()+url,  function( data ) {
        console.log('found distribution '+data);
        ractive.set('saveObserver',false);
        ractive.set('current', data);
        $('#curMemoDisplay').val(ractive.getMemoName(ractive.get('current.memoRef')));
        ractive.initControls();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        ractive.set('saveObserver',true);
      });
    } else {
      console.log('Skipping load as no _links.'+distribution.name);
      ractive.set('current', distribution);
      ractive.set('saveObserver',true);
    }
	  ractive.hideResults();
    $('#currentSect').slideDown({ queue: true });
  },
  startDistribution: function() {
    console.log('startDistribution');
    if (ractive.get('testMode')==true) {
      ractive.startDistributionInTestMode();
    } else {
      ractive.startDistributionForReal();
    }
  },
  startDistributionForReal: function() {
    console.log('startDistributionForReal');
    $.ajax({
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        processDefinitionId: 'DistributeMemo',
        businessKey: ractive.get('current.name')+' '+new Date().toISOString(),
        processVariables: { distributionId: ractive.id(ractive.get('current')) }
      }),
      success: function(data,textStatus,jqXHR) {
        console.log('response code: '+ jqXHR.status+', Location: '+jqXHR.getResponseHeader('Location'));
        ractive.showMessage('Started distribution of memos: '+ractive.get('current.name'));
      },
    });
  },
  startDistributionInTestMode: function() {
    ractive.showMessage("No memos are actually sent because you are in simulation mode.", 'bg-warning text-warning');
    var distribution = { recipients: ['Red','White','sandrab@yahoo.com'] };
    var toContact = jQuery.map(ractive.get('contacts'), function( n, i ) {
      console.log('n: '+n.email+', i:'+i);
      if (distribution.recipients.indexOf(n.email)!=-1) {
        return (n);
      } else if (n.tags != undefined) {
        var tagArray = n.tags.split(',');
        return jQuery.map(tagArray, function(m) {
          if (distribution.recipients.indexOf(m)!=-1) {
            return (n);
          } else {
            return;
          }
        });
      } else {
        return;
      }
    });
    ractive.set('toContact',toContact);
    if (ractive.get('current.activities') == undefined) ractive.set('current.activities', []);
    $.each(toContact, function(i,d) {
      ractive.get('current.activities').push({occurred:new Date(),type:'Sent memo to '+d.firstName+' '+d.lastName,content:'Status: <span class="sent bg-success text-success" data-contact="'+i+'">Sent (GREEN)</span> <span class="acknowledged bg-danger text-danger" data-contact="'+i+'">Acknowledged (RED)</span>'});
      setTimeout(function() {
        $('[data-contact="'+i+'"].acknowledged').removeClass('bg-danger').removeClass('text-danger').addClass('bg-success').addClass('text-success').empty().append('Acknowledged (GREEN)');
      },(1*1000*60*Math.random()));
    });
  },
  setRecipients: function(ctrl) {
    console.info('setRecipients: '+$(ctrl).val());
    if ($(ctrl).val()!=undefined) {
      ractive.set('current.recipientList',$(ctrl).val());
      ractive.set('current.recipients',$(ctrl).val().join());
    }
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
    console.warn  ('Skipped distribution save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

/*ractive.observe('current.sendAt', function(newValue, oldValue, keypath) {
  console.error('send at change: '+newValue +','+oldValue+' '+keypath);
  var matches = newValue.match(new RegExp(/^([0-9]{4})-(1[0-2]|0[1-9])-(1[0-3]|0[1-9])T(1[0-2]|0[1-9]):(1[0-5]|0[1-9])$/));
  if (newValue==undefined){
    console.error('send at save needed 0: ');
    ractive.save();
    ractive.set('saveObserver',true);
  }else if (matches[0].length==16){
    console.error('send at save needed: ');
    newValue = matches[0]+':00.000Z';
    ractive.save();
    ractive.set('saveObserver',true);
  }else{
    ractive.set('saveObserver',false);
  }
});*/
