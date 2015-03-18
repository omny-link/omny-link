var EASING_DURATION = 500;
fadeOutMessages = true;

// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new AuthenticatedRactive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',
  
  // If two-way data binding is enabled, whether to only update data based on 
  // text inputs on change and blur events, rather than any event (such as key
  // events) that may result in new data.
  lazy: true,
  
  // We could pass in a string, but for the sake of convenience
  // we're passing the ID of the <script> tag above.
  template: '#template',

  // partial templates
  // partials: { question: question },

  // Here, we're passing in some initial data
  data: {
    csrfToken: getCookie(CSRF_COOKIE),
    server: 'http://api.knowprocess.com',
/*    tenant: { 
      name: 'firmgains', 
      contactFields: [], 
      contactListFields: [
        { label: 'Share of Business', key: 'shareOfBusiness' },
        { label: 'Already contacted', key: 'alreadyContacted'}, 
      ],
      */
    contacts: [],
    //saveObserver:false,
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    }
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var contact = { account: {}, author:ractive.data.username, tenantId: ractive.data.tenant.id, url: undefined };
    ractive.select( contact );
  },
  addDoc: function (contact) {
    console.log('addDoc '+contact+' ...');
    ractive.data.currentIdx = ractive.data.contacts.length;
    ractive.set('current.doc', { author:ractive.data.username, contact: contact, url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
  },
  addField: function() { 
    
  },
  edit: function (contact) {
    console.log('edit'+contact+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.data.currentIdx = ractive.data.contacts.indexOf(contact);
    ractive.select( contact );
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
  },
  addNote: function (contact) {
    console.log('addNote '+contact+' ...');
    ractive.set('current.note', { author:ractive.data.username, contact: contact, content: undefined});
    $('#notesTable tr:nth-child(1)').slideDown();
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
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.handleError(jqXHR,textStatus,errorThrown);
        }
    });
  },
  fetch: function () {
    console.log('fetch...');
    ractive.data.saveObserver = false;
    $.getJSON('/'+ractive.data.tenant.id+'/contacts/?projection=complete',  function( data ) {
      if (data._embedded == undefined) {
        ractive.merge('contacts', data);
        ractive.data.saveObserver = true;
      }else{
        ractive.merge('contacts', data._embedded.contacts);
        ractive.data.saveObserver = true;
      }
    });
  },
  handleError: function(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) { 
    case 403: 
      ractive.showError("Session expired, please login again");
      window.location.href='/login';
      break; 
    default: 
      ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);        
    }
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    $.each(ractive.get('tenant.typeaheadControls'), function(i,d) {
      console.log('binding ' +d.url+' to typeahead control: '+d.selector);
      $.get(d.url, function(data){
        $(d.selector).typeahead({ minLength:0,source:data });
        $(d.selector).on("click", function (ev) {
          newEv = $.Event("keydown");
          newEv.keyCode = newEv.which = 40;
          $(ev.target).trigger(newEv);
          return true;
       });
      },'json');
    });
  },
  nextEntity: function() { 
    console.log('nextEntity');
    $('.entity.active').fadeOut().removeClass('active');
    ractive.set('entityIdx', ++ractive.data.entityIdx);
    $('#entity'+ractive.data.entityIdx+'Sect').fadeIn().addClass('active');
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
//	  this.fetch();
  },
  previousEntity: function() { 
    console.log('previousEntity');
    $('.entity.active').fadeOut().removeClass('active');
    ractive.set('entityIdx', --ractive.data.entityIdx);
    $('#entity'+ractive.data.entityIdx+'Sect').fadeIn().addClass('active');
  },
  save: function () {
    console.log('save '+JSON.stringify(ractive.data.current)+' ...');
    ractive.data.saveObserver = false;
    var id = ractive.data.current._links === undefined ? undefined : (
        ractive.data.current._links.self.href.indexOf('?') == -1 ? ractive.data.current._links.self.href : ractive.data.current._links.self.href.substr(0,ractive.data.current._links.self.href.indexOf('?')-1)
    );
    ractive.data.saveObserver = true;
    
    if (document.getElementById('currentForm').checkValidity()) {
      // cannot save contact and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.data.current));
      console.log('account: '+JSON.stringify(tmp.account));
      tmp.notes = undefined;
      tmp.documents = undefined;
      if (id != undefined && tmp.account != undefined && Object.keys(tmp.account).length > 0 && tmp.account.id != undefined) {
        tmp.account = id.substring(0,id.indexOf('/',8))+'/accounts/'+tmp.account.id;  
      } else {
        tmp.account = null;
      } 
      tmp.tenantId = ractive.data.tenant.id;
      $.ajax({
        url: id === undefined ? '/contacts' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current._links.self.href',location);
          if (jqXHR.status == 201) ractive.data.contacts.push(ractive.data.current);
          if (jqXHR.status == 204) ractive.splice('contacts',ractive.data.currentIdx,1,ractive.data.current);
          ractive.showMessage('Contact saved');
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
//      $('#currentForm :invalid')[0].focus();
//      ractive.showFormError(undefined, 'Cannot save yet as contact is invalid');
    }
  },
  saveAccount: function () {
    console.log('saveAccount '+JSON.stringify(ractive.data.current.account)+' ...');
    //if (ractive.data.current.account == undefined) ractive.data.current.account = {};
    var id = ractive.data.current.account.id;
    ractive.set('saveObserver',false);
    ractive.set('current.account.tenantId',ractive.data.tenant.id);
    ractive.set('saveObserver',true);
    if (document.getElementById('currentAccountForm').checkValidity()) { 
      $.ajax({
        url: id === undefined ? '/accounts' : '/accounts/'+id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(ractive.data.current.account),
        success: completeHandler = function(data, textStatus, jqXHR) {
          console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current.account.id',location.substring(location.lastIndexOf('/')+1));
          var contactAccountLink = ractive.data.current._links.self.href.indexOf('?')==-1 
              ? ractive.data.current._links.self.href
              : ractive.data.current._links.self.href.substring(0,ractive.data.current._links.self.href.indexOf('?')-1);
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
          if (jqXHR.status == 201) ractive.data.contacts.push(ractive.data.current);
          if (jqXHR.status == 204) ractive.splice('contacts',ractive.data.currentIdx,1,ractive.data.current);
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    } else {
      console.warn('Cannot save yet as account is invalid');
      $('#currentAccountForm :invalid').addClass('field-error');
      $('#currentAccountForm :invalid')[0].focus();
//        ractive.showFormError(undefined,'Cannot save yet as account is invalid');
    }
  },
  saveDoc: function () {
    console.log('saveDoc '+ractive.data.current.doc+' ...');
    var n = ractive.data.current.doc;
    n.url = $('#doc').val();
    if (n.url.trim().length > 0) { 
      $('#docsTable tr:nth-child(1)').slideUp();
      $.ajax({
        url: '/documents',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(n),
        success: completeHandler = function(data) {
          console.log('data: '+ data);
          ractive.select(ractive.data.current);
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    } 
  },
  saveNote: function () {
    console.log('saveNote '+ractive.data.current.note+' ...');
    var n = ractive.data.current.note;
    n.content = $('#note').val();
    if (n.content.trim().length > 0) { 
      $('#notesTable tr:nth-child(1)').slideUp();
      $.ajax({
        url: '/notes',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(n),
        success: completeHandler = function(data) {
          console.log('data: '+ data);
          ractive.select(ractive.data.current);
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    }
  },
  select: function(contact) {
    console.log('select: '+JSON.stringify(contact));
    ractive.set('saveObserver',false);
    if (contact.account == undefined || contact.account == '') contact.account = new Object();
    // default owner to current user
    if (contact.owner == undefined || contact.owner == '') contact.owner = ractive.data.username;
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
	    $.getJSON(url+'?projection=complete',  function( data ) {
        console.log('found contact '+data);
        ractive.set('current', data);
      });
      $.getJSON(url+'/notes',  function( data ) {
      	if (data._embedded != undefined) {
	        console.log('found notes '+data);
          ractive.merge('current.notes', data._embedded.notes);
	        // sort most recent first
  	      ractive.data.current.notes.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      	}
  	  });
      $.getJSON(url+'/documents',  function( data ) {
        if (data._embedded != undefined) {
        	console.log('found docs '+data);
          ractive.merge('current.documents', data._embedded.documents);
          // sort most recent first
          ractive.data.current.documents.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
        }
        ractive.set('saveObserver',true);
      });
    } else { 
      console.log('Skipping load as no _links.'+JSON.stringify(contact));
      ractive.set('current', contact);
      ractive.set('saveObserver',true);
    }
    $('#currentSect').slideDown();
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showError: function(msg) {
    this.showMessage(msg, 'bg-danger text-danger');
  },
  showFormError: function(formId, msg) {
    this.showError(msg);
    var selector = formId==undefined || formId=='' ? ':invalid' : '#'+formId+' :invalid';
    $(selector).addClass('field-error');
    $(selector)[0].focus();
  },
  showMessage: function(msg, additionalClass) {
    if (additionalClass == undefined) additionalClass = 'bg-info text-info';
    if (msg === undefined) msg = 'Working...';
    $('#messages p').empty().append(msg).removeClass().addClass(additionalClass).show();
//    document.getElementById('messages').scrollIntoView();
    if (fadeOutMessages && additionalClass!='bg-danger text-danger') setTimeout(function() {
      $('#messages p').fadeOut();
    }, EASING_DURATION*10);
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
    ractive.showMessage('Uploading data...');
  
    var formElement = document.getElementById(formId);
    var formData = new FormData(formElement);
    return $.ajax({
        type: 'POST',
        url: '/'+ractive.data.tenantId+'/contacts',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
  //        console.log('successfully uploaded data');
          ractive.showMessage('Successfully uploaded data');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          ractive.handleError(jqXHR, textStatus, errorThrown);
        }
      });
  }
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.documents','current.doc','current.notes','current.note'];
  if (ractive.data.saveObserver && ignored.indexOf(keypath)==-1) {
    if (keypath=='current.account') ractive.saveAccount();
    else ractive.save();
  } else { 
    console.warn  ('Skipped contact save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.data.saveObserver);
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

