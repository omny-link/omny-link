var TRANSITION_DURATION = 500;
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
      accountFields: [ 
        { label: 'EBITDA', key: 'ebitda'},
        { label: 'Surplus', key: 'surplus'},
        { label: 'Depreciation', key: 'depreciationAmortisation'},
        { label: 'Operating Profit', key: 'operatingProfit'},
        { label: 'Adjustments', key: 'adjustments'},
        { label: 'Borrowing', key: 'borrowing'},
        { label: 'Low Quote', key: 'lowQuote'},
        { label: 'Medium Quote', key: 'mediumQuote'},
        { label: 'High Quote', key: 'highQuote'},
        { label: 'Asking Price', key: 'askingPrice'},
        { label: 'Property Info', key: 'propertyInfo'},
        { label: 'Reason for Selling', key: 'reasonForSelling'},
        { label: 'Potential Purchasers', key: 'potentialPurchasers'},
        { label: 'Sub-Sector', key: 'subSector'},
        { label: 'Assets', key: 'assets'},
        { label: 'Preferred Timing', key: 'preferredTiming'},
        { label: 'Account Type', key: 'accountType'},
        { label: 'Sector', key: 'sector'},
        { label: 'Stage in Sale', key: 'saleStage'},
        { label: 'Region', key: 'region'},
        { label: 'Current Financial Year', key: 'currentFinancialYear'},
        { label: 'Current Turnover', key: 'currentTurnover'},
        { label: 'Current Profit', key: 'currentProfit'},
        { label: 'Last Financial Year', key: 'lastFinancialYear'},
        { label: 'Last Turnover', key: 'lastTurnover'},
        { label: 'Last Profit', key: 'lastProfit'},
        { label: 'Previous Financial Year', key: 'previousFinancialYear'},
        { label: 'Previous Turnover', key: 'previousTurnover'},
        { label: 'Previous Profit', key: 'previousProfit'}
      ] 
    },*/
    contacts: [],
    username: localStorage['username']
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var contact = { account: {}, author:ractive.data.username, tenantId: ractive.data.tenant.name, url: undefined };
    ractive.select( contact );
  },
  addDoc: function (contact) {
    console.log('addDoc '+contact+' ...');
    ractive.data.currentIdx = ractive.data.contacts.length;
    ractive.set('current.doc', { author:ractive.data.username, contact: contact, url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
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
  delete: function (url) {
    console.log('delete '+url+'...');
    if (url.indexOf('?')!=-1) url = url.substr(0,url.indexOf('?'));
    $.ajax({
        url: url,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
        }
    });
  },
  fetch: function () {
    console.log('fetch...');
//    $.getJSON('/'+ractive.get('tenant.name')+"/contacts",  function( data ) {
    $.getJSON("/contacts?projection=complete",  function( data ) {
      if (data._embedded == undefined) {
        ractive.merge('contacts', data);
      }else{
        ractive.merge('contacts', data._embedded.contacts);
      }
    });
  },
  initAutoComplete: function() {
    console.log('initAutoComplete');
    $.each(ractive.get('tenant.typeaheadControls'), function(i,d) {
      console.log('binding ' +d.url+' to typeahead control: '+d.selector);
      $.get(d.url, function(data){
        $(d.selector).typeahead({ source:data });
      },'json');
    });
  },
  
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
//    this.fetch();
  },
  save: function () {
    console.log('save '+JSON.stringify(ractive.data.current)+' ...');
    var id = ractive.data.current._links === undefined ? undefined : (
        ractive.data.current._links.self.href.indexOf('?') == -1 ? ractive.data.current._links.self.href : ractive.data.current._links.self.href.substr(0,ractive.data.current._links.self.href.indexOf('?')-1)
    );
    
    if (document.getElementById('currentForm').checkValidity()) {
      // cannot save contact and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.data.current));
      console.log('account: '+JSON.stringify(tmp.account));
      if (tmp.account != undefined && Object.keys(tmp.account).length > 0) {
        tmp.account = id.substring(0,id.indexOf('/',8))+'/accounts/'+tmp.account.id;  
      }else{
        tmp.account = null;
      } 
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
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      ractive.showFormError(undefined, 'Cannot save yet as contact is invalid');
    }
  },
  saveAccount: function () {
    console.log('saveAccount '+JSON.stringify(ractive.data.current.account)+' ...');
    //if (ractive.data.current.account == undefined) ractive.data.current.account = {};
    var id = ractive.data.current.account.id;
    ractive.set('current.account.tenantId',ractive.data.tenant.name);
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
              },
              error: errorHandler = function(jqXHR, textStatus, errorThrown) {
                  ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
              }
            });
          }
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
        }
      });
    } else {
        ractive.showFormError(undefined,'Cannot save yet as account is invalid');
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
            ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
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
            ractive.showError("Bother! Something has gone wrong: "+textStatus+':'+errorThrown);
        }
      });
    }
  },
  select: function(contact) {
    console.log('select: '+JSON.stringify(contact));
    ractive.set('suspendSaveObserver',false);
    if (contact.account == undefined || contact.account == '') contact.account = new Object();
    ractive.set('current',contact);
    if (contact._links != undefined) {
      var url = contact._links.self.href.indexOf('?')==-1 ? contact._links.self.href : contact._links.self.href.substr(0,contact._links.self.href.indexOf('?')-1);
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
      });
    }
    ractive.set('suspendSaveObserver',true);
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
    if (msg === undefined) msg = 'Working...';
    $('#messages p').empty().append(msg).addClass(additionalClass).show();
    document.getElementById('messages').scrollIntoView();
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
  }
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.account','current.documents','current.doc','current.notes','current.note'];
  // console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
  if (ractive.data.suspendSaveObserver && significantDifference(newValue,oldValue) && ignored.indexOf(keypath)==-1) {
    ractive.save();
  }
});

ractive.observe('current.account.*', function(newValue, oldValue, keypath) {
  alwaysSave=['current.account.customFields'];
  ignored=[];
  console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
  if (alwaysSave.indexOf(keypath)!=-1 || (ractive.data.suspendSaveObserver  && ignored.indexOf(keypath)==-1 && significantDifference(newValue,oldValue))) {
    ractive.saveAccount();
  } else { 
    console.log('Skipped save of '+keypath);
  }
});

function significantDifference(newValue,oldValue) {
  if (newValue=='') { console.log('new value is empty');newValue = null;}
  if (oldValue=='') { console.log('oldvalue is empty');oldValue = null;}
  var newVal = JSON.stringify(newValue);
  var oldVal = JSON.stringify(oldValue);
  console.log('sig diff between \n  '+newVal+' and \n  '+oldVal+ '\n  '+(newVal!=oldVal));
  return newValue != oldValue;
}

