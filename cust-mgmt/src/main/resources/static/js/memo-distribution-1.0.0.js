var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    contacts: [],
    //server: 'http://api.knowprocess.com:8082',
    testMode: false,
    memoDistributions: [],
    memos: [],
    filter: undefined,
    //saveObserver:false,
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    },
    chars: function(string) {
      console.info('chars: '+string);
      console.log('  returning: '+string.length);
      return string.length;
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
      return '<img class="img-rounded" src="http://www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36"/>'
    },
    matchFilter: function(obj) {
      if (ractive.get('filter')==undefined) return true;
      else return ractive.get('filter').value.toLowerCase()==obj[ractive.get('filter').field].toLowerCase();
    },
    saveObserver: false,
    stdPartials: [
      { "name": "helpModal", "url": "/partials/memo-dist-help-modal.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "memoDistListSect", "url": "/partials/memo-dist-list-sect.html"},
      { "name": "currentMemoDistSect", "url": "/partials/memo-dist-current-sect.html"}
    ],
    title: "Distribution Centre"
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var distribution = { activities:[], owner:ractive.get('username'), status: 'Draft', tenantId: ractive.get('tenant.id'), url: undefined };
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
        something = window.open("data:text/csv," + encodeURIComponent(data),"_blank");
        //something.focus();
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
  fetch: function () {
    console.log('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/memo-distributions/?projection=complete',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('memoDistributions', data);
        } else {
          ractive.merge('memoDistributions', data['_embedded'].memoDistributions);
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
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/?projection=complete',
      crossDomain: true,
      success: function( data ) {
        console.log('Found '+data.length+' contacts.');
        //$('#curRecipients').chosen('destroy');
        if (data['_embedded'] == undefined) {
          ractive.merge('contacts', data);
        }else{
          ractive.merge('contacts', data['_embedded'].contacts);
        }
        var tags = [];
        $('#curRecipients').empty();
        $.each(data, function(i,d) {
          $('#curRecipients').append('<option value="'+d.email+'">'+d.email+'</option>');
          var contactTags = d['tags'];
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
        $('#curRecipients option').removeAttr('selected')
        
        // select any recipients already attached to the distribution
        if (ractive.get('current.recipients')!=undefined) {
          $.each(ractive.get('current.recipients').split(','), function(i,d) {
            $('#curRecipients option[value="'+d.trim()+'"]').attr('selected','selected')
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
        if (data['_embedded'] == undefined) {
          ractive.merge('memos', data);
        }else{
          ractive.merge('memos', data['_embedded'].memos);
        }
        $('#curMemoDisplay').typeahead({ 
          items:'all',
          minLength:0,
          source:ractive.initMemosTypeahead(),
          afterSelect:function(d) {
            console.info('afterSelect:'+d);
            ractive.set('current.memoRef',d.id.substring(d.id.lastIndexOf('/')+1));
          }
        });
        $('#curMemoDisplay').on("click", function (ev) {
          newEv = $.Event("keydown");
          newEv.keyCode = newEv.which = 40;
          $(ev.target).trigger(newEv);
          return true;
        });
      }     
    });
  },
  filter: function(field,value) {
    console.log('filter: field '+field+' = '+value);
    if (value==undefined) value = ractive.get('tenant.stagesInActive');
    if (field==undefined) ractive.set('filter',undefined);
    else ractive.set('filter',{field: field,value: value});
    ractive.set('searchMatched',$('#memoDistributionsTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(distributionId) { 
    console.log('find: '+distributionId);
    var c; 
    $.each(ractive.get('memoDistributions'), function(i,d) { 
      if (distributionId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  getId: function(distribution) { 
    console.log('getId: '+distribution);
    var uri; 
    if (distribution['links']!=undefined) {
      $.each(distribution.links, function(i,d) { 
        if (d.rel == 'self') { 
          uri = d.href;
        }
      });
    } else if (distribution['_links']!=undefined) {
      uri = distribution._links.self.href.indexOf('?')==-1 ? distribution._links.self.href : distribution._links.self.href.substr(0,distribution._links.self.href.indexOf('?')-1);
    } 
    return uri;
  },
  getMemoName: function(id) {
    console.log('getMemoName: '+id);
    if (id == undefined) return '';
    var name = '';
    $.each(ractive.get('memos'), function(i,d) {
      if (d.selfRef.endsWith('/'+id)) name = d.name;
    });
    return name;
  },
  initMemosTypeahead: function() {
    console.info('initMemosTypeahead');
    return jQuery.map(ractive.get('memos'), function( n, i ) {
      console.log('n: '+n+', i:'+i);
      return ( {id: n.selfRef, name: n.name} );
    });
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save distribution: '+ractive.get('current').name+'...');
    ractive.set('saveObserver',false);
    var id = ractive.getId(ractive.get('current'));
//    if (ractive.get('current.recipients')!=undefined) {
//      ractive.set('current.recipients',ractive.get('current.recipients').split(",")); 
//    }
    ractive.set('saveObserver',true);
    if (document.getElementById('currentForm')==undefined) {
      // loading... ignore
    } else if (document.getElementById('currentForm').checkValidity()) {
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save distribution'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? '/memo-distributions' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
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
    if (distribution.owner == undefined || distribution.owner == '') distribution.owner = ractive.get('username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (distribution._links == undefined && distribution.links != undefined) { 
	    distribution._links = distribution.links;
	    $.each(distribution.links, function(i,d) { 
        if (d.rel == 'self') distribution._links.self = { href:d.href };
      });
	  }
	  if (distribution._links != undefined) {
	    var url = distribution._links.self.href.indexOf('?')==-1 ? distribution._links.self.href : distribution._links.self.href.substr(0,distribution._links.self.href.indexOf('?')-1);
	    console.log('loading detail for '+url);
	    $.getJSON(ractive.getServer()+url,  function( data ) {
        console.log('found distribution '+data);
        ractive.set('current', data);
        $('#curMemoDisplay').val(ractive.getMemoName(ractive.get('current.memoRef')))
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
	  ractive.toggleResults();
	  $('#currentSect').slideDown();
  },
  startDistribution: function(distribution) {
    console.log('sendMessage');
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
        processVariables: { distributionId: ractive.get('current.id') }
      }),
      success: completeHandler = function(data,textStatus,jqXHR) {
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
        return jQuery.map(tagArray, function(m,j) {
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
        $('[data-contact="'+i+'"].acknowledged').removeClass('bg-danger').removeClass('text-danger').addClass('bg-success').addClass('text-success').empty().append('Acknowledged (GREEN)')
      },(1*1000*60*Math.random()));
    });
  },
  setRecipients: function(ctrl) {
    console.info('setRecipients: '+$(ctrl).val());
    if ($(ctrl).val()!=undefined) {
      ractive.set('current.recipientList',$(ctrl).val());
      ractive.set('current.recipients',$(ctrl).val().join());
    }
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showResults: function() {
    $('#memoDistributionsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#memoDistributionsTable').slideDown({ queue: true });
  },
  stripProjection: function(link) {
    var idx = link.indexOf('{projection');
    if (idx==-1) { 
      return link;
    } else {
      return link.substring(0,idx);
    }
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#memoDistributionsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#memoDistributionsTable').slideToggle();
  }
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#memoDistributionsTable tbody tr').length);
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

function crToSpace(string) {
  return string.replace(/<br>/g,' ');
}

function stripTags(string) {
  return crToSpace(string.replace(/<br>/g,' ').replace(/<\/?[^>]+(>|$)/g, ""));
}