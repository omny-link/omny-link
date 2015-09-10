var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;

// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new AuthenticatedRactive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',
  
  // If two-way data binding is enabled, whether to only update data based on 
  // text inputs on change and blur events, rather than any event (such as key
  // events) that may result in new data
  lazy: true,
  
  // We could pass in a string, but for the sake of convenience
  // we're passing the ID of the <script> tag above.
  template: '#template',

  // partial templates
  // partials: { question: question },

  // Here, we're passing in some initial data
  data: {
    csrfToken: getCookie(CSRF_COOKIE),
    //server: 'http://api.knowprocess.com:8082',
/*    tenant: { 
      name: 'firmgains', 
      contactFields: [], 
      contactListFields: [
        { label: 'Share of Business', key: 'shareOfBusiness' },
        { label: 'Already contacted', key: 'alreadyContacted'}, 
      ],
      */
    contacts: [],
    filter: undefined,
    //saveObserver:false,
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
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
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatDate: function(timeString) {
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
      var tagArr = tags.split(',');
      var html = '';
      $.each(tagArr, function(i,d) {
        html += '<span class="img-rounded" style="background-color:'+d+'">&nbsp;&nbsp;</span>';
      });
      return html;
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
    stdPartials: [
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var contact = { account: {}, author:ractive.get('username'), tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select( contact );
  },
  addDoc: function (contact) {
    console.log('addDoc '+contact+' ...');
    ractive.set('current.doc', { author:ractive.get('username'), contact: ractive.stripProjection(contact), url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
  },
  addNote: function (contact) {
    console.log('addNote '+contact+' ...');
    ractive.set('current.note', { author:ractive.get('username'), contact: ractive.stripProjection(contact), content: undefined});
    $('#notesTable tr:nth-child(1)').slideDown();
  },
  edit: function (contact) {
    console.log('edit'+contact+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('currentIdx',ractive.get('contacts').indexOf(contact));
    ractive.select( contact );
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  /*followUp: function(contactId) { 
    console.log('followUp: '+JSON.stringify(contactId));
    $.ajax({
      type: 'GET',
      url: '/msg/firmgains/firmgains.followUp.json?query={"contactId":"'+contactId+'","tenantId":"'+ractive.get('tenant.id')+'"}&businessDescription=FollowUp',
      crossDomain: true,
      success: function( data ) {
        console.log('data: '+data);
      }
    });
  },*/
  /*formatJson: function(json) { 
    console.log('formatJson: '+json);
    var obj = JSON.parse(json);
    var html = '';
    $.each(Object.keys(obj), function(i,d) {
      html += (typeof obj[d] == 'object' ? '' : d+': '+obj[d]+'<br/>');
    });5
    //console.log('HTML: '+html);
    //ractive.set('current.html',html);
    return html;
//    $(selector).append(html);
  },*/
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
  fetch: function () {
    console.log('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/contacts/?projection=complete',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('contacts', data);
        } else {
          ractive.merge('contacts', data['_embedded'].contacts);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#contactsTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }//,
//      fail: function( jqXHR, textStatus, errorThrown) {
//        console.log( "error" );
//        ractive.handleError(jqXHR,textStatus,errorThrown);
//      });      
    });
  },
  fetchDocs: function() { 
    $.getJSON(ractive.getId(ractive.get('current'))+'/documents',  function( data ) {
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
    $.getJSON(ractive.getId(ractive.get('current'))+'/notes',  function( data ) {
      if (data['_embedded'] != undefined) {
        console.log('found notes '+data);
        ractive.merge('current.notes', data['_embedded'].notes);
        // sort most recent first
        ractive.get('current.notes').sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      }
    });
  },
  filter: function(field,value) {
    console.log('filter: field '+field+' = '+value);
    if (value==undefined) value = ractive.get('tenant.stagesInActive');
    if (field==undefined) ractive.set('filter',undefined);
    else ractive.set('filter',{field: field,value: value});
    ractive.set('searchMatched',$('#contactsTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(contactId) { 
    console.log('find: '+contactId);
    var c; 
    $.each(ractive.get('contacts'), function(i,d) { 
      if (contactId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  getId: function(contact) { 
    console.log('getId: '+contact);
    var uri; 
    if (contact['links']!=undefined) {
      $.each(contact.links, function(i,d) { 
        if (d.rel == 'self') { 
          uri = d.href;
        }
      });
    } else if (contact['_links']!=undefined) {
      uri = contact._links.self.href.indexOf('?')==-1 ? contact._links.self.href : contact._links.self.href.substr(0,contact._links.self.href.indexOf('?')-1);
    } 
    return uri;
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save contact: '+ractive.get('current').lastName+'...');
    ractive.set('saveObserver',false);
    var id = ractive.get('current')._links === undefined ? undefined : (
        ractive.get('current')._links.self.href.indexOf('?') == -1 ? ractive.get('current')._links.self.href : ractive.get('current')._links.self.href.substr(0,ractive.get('current')._links.self.href.indexOf('?')-1)
    );
    ractive.set('saveObserver',true);
    if (document.getElementById('currentForm').checkValidity()) {
      // cannot save contact and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      //console.log('account: '+JSON.stringify(tmp.account));
      tmp.notes = undefined;
      tmp.documents = undefined;
      if (id != undefined && tmp.account != undefined && Object.keys(tmp.account).length > 0 && tmp.account.id != undefined) {
        tmp.account = id.substring(0,id.indexOf('/',8))+'/accounts/'+tmp.account.id;  
      } else {
        tmp.account = null;
      }       
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save contact'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? '/contacts' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          if (jqXHR.status == 201) ractive.get('contacts').push(ractive.get('current'));
          if (jqXHR.status == 204) ractive.splice('contacts',ractive.get('currentIdx'),1,ractive.get('current'));
          ractive.showMessage('Contact saved');
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
    }
  },
  saveAccount: function () {
    if (ractive.get('current.account')==undefined) return;
    console.log('saveAccount '+ractive.get('current.account').name+' ...');
    if (ractive.get('current.account') == undefined) ractive.set('current.account',{});
    var id = ractive.get('current.account.id');
    ractive.set('saveObserver',false);
    ractive.set('current.account.tenantId',ractive.get('tenant.id'));
    ractive.set('saveObserver',true);
    if ($('#currentAccountForm:visible').length!=0 && document.getElementById('currentAccountForm').checkValidity()) { 
      $.ajax({
        url: ractive.getServer()+(id === undefined ? '/accounts' : '/accounts/'+id),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(ractive.get('current.account')),
        success: completeHandler = function(data, textStatus, jqXHR) {
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current.account.id',location.substring(location.lastIndexOf('/')+1));
          var contactAccountLink = ractive.get('current')._links.self.href.indexOf('?')==-1 
              ? ractive.get('current')._links.self.href
              : ractive.get('current')._links.self.href.substring(0,ractive.get('current')._links.self.href.indexOf('?')-1);
          contactAccountLink+='/account';
          console.log(' attempt to link account: '+location+' to '+contactAccountLink);
          if (jqXHR.status == 201) { 
            $.ajax({
              url: contactAccountLink,
              type: 'PUT',
              contentType: 'text/uri-list',
              data: location,
              success: completeHandler = function(data, textStatus, jqXHR) {
                console.log('linked account: '+location+' to '+contactAccountLink);
                ractive.showMessage('Account saved');
              },
              error: errorHandler = function(jqXHR, textStatus, errorThrown) {
                  ractive.handleError(jqXHR,textStatus,errorThrown);
              }
            });
          }
          if (jqXHR.status == 201) ractive.get('contacts').push(ractive.get('current'));
          if (jqXHR.status == 204) ractive.splice('contacts',ractive.get('currentIdx'),1,ractive.get('current'));
          //ractive.get('contacts')[ractive.get('currentIdx')].lastUpdated=new Date().toISOString();
          //ractive.sortContacts();
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    } else if ($('#currentAccountForm:visible').length!=0) {
      console.warn('Cannot save yet as account is invalid');
      $('#currentAccountForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as account is incomplete');
    }
  },
  saveDoc: function () {
    console.log('saveDoc '+JSON.stringify(ractive.get('current.doc'))+' ...');
    var n = ractive.get('current.doc');
    n.url = $('#doc').val();
    var url = ractive.getId(ractive.get('current'))+'/documents';
    url = url.replace('contacts/',ractive.get('tenant.id')+'/contacts/');
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
  saveNote: function () {
    console.info('saveNote '+JSON.stringify(ractive.get('current.note'))+' ...');
    var n = ractive.get('current.note');
    n.content = $('#note').val();
    var url = ractive.getId(ractive.get('current'))+'/notes';
    url = url.replace('contacts/',ractive.get('tenant.id')+'/contacts/');
    console.log('  url:'+url);
    if (n.content.trim().length > 0) { 
      $('#notesTable tr:nth-child(1)').slideUp();
      $.ajax({
        /*url: '/notes',
        contentType: 'application/json',*/
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
  select: function(contact) {
    console.log('select: '+JSON.stringify(contact));
    ractive.set('saveObserver',false);
    if (contact.account == undefined || contact.account == '') contact.account = new Object();
    // default owner to current user
    if (contact.owner == undefined || contact.owner == '') contact.owner = ractive.get('username');
	  // adapt between Spring Hateos and Spring Data Rest
	  if (contact._links == undefined && contact.links != undefined) { 
	    contact._links = contact.links;
	    $.each(contact.links, function(i,d) { 
        if (d.rel == 'self') contact._links.self = { href:d.href };
      });
	  }
	  if (contact._links != undefined) {
	    var url = contact._links.self.href.indexOf('?')==-1 ? contact._links.self.href : contact._links.self.href.substr(0,contact._links.self.href.indexOf('?')-1);
	    console.log('loading detail for '+url);
	    $.getJSON(ractive.getServer()+url+'?projection=complete',  function( data ) {
        console.log('found contact '+data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        ractive.fetchNotes();
        ractive.fetchDocs();
        // sort most recent first
        ractive.get('current.activities').sort(function(a,b) { return new Date(b.occurred)-new Date(a.occurred); });
      });
    } else { 
      console.log('Skipping load as no _links.'+contact.lastName);
      ractive.set('current', contact);
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
    $('#contactsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#contactsTable').slideDown({ queue: true });
  },
  sortContacts: function() {
    ractive.get('contacts').sort(function(a,b) { return new Date(b.lastUpdated)-new Date(a.lastUpdated); });
  },
  startCustomerAction: function(key, label, contact) {
    console.log('startCustomerAction: '+key+' for '+JSON.stringify(contact));
    var instanceToStart = {
        processDefinitionId: key,
        businessKey: contact.firstName+' '+contact.lastName,
        processVariables: { contactId: ractive.getId(contact), initiator: ractive.get('username') }
      };
    console.log(JSON.stringify(instanceToStart));
    $.ajax({
      url: '/'+ractive.get('tenant.id')+'/process-instances/',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(instanceToStart),
      success: completeHandler = function(data, textStatus, jqXHR) {
        console.log('response: '+ jqXHR.status+", Location: "+jqXHR.getResponseHeader('Location'));
        ractive.showMessage('Started workflow "'+label+'" for '+instanceToStart.businessKey);
      },
    });
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
    $('#contactsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#contactsTable').slideToggle();
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
    return $.ajax({
        type: 'POST',
        url: '/'+ractive.get('tenant.id')+'/'+entity+'/upload',
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
    ractive.set('searchMatched',$('#contactsTable tbody tr').length);
  }, 500);
});


// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.documents','current.doc','current.notes','current.note'];
  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    if (keypath=='current.account') ractive.saveAccount();
    else ractive.save();
  } else { 
    console.warn  ('Skipped contact save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

// Cannot work due to http://docs.ractivejs.org/0.5/observers#a-gotcha-to-be-aware-of
function significantDifference(newValue,oldValue) {
//  if (newValue=='') { console.log('new value is empty');newValue = null;}
//  if (oldValue=='') { console.log('oldvalue is empty');oldValue = null;}
  var newVal = JSON.stringify(newValue);
  var oldVal = JSON.stringify(oldValue);
  console.debug('sig diff between  '+newVal+' and '+oldVal+ ': '+(newVal!=oldVal));
  return newValue != oldValue;
}
