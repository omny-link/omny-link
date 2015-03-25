var EASING_DURATION = 500;
fadeOutMessages = true;

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
    server: 'http://api.knowprocess.com',
    entities: [],
    //saveObserver:false,
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    }
  },
  addField: function(dataName) {
    console.log('addField...'+dataName);
    var newField = ractive.get(dataName);
    console.log('  '+JSON.stringify(newField));
    ractive.get('entities')[ractive.get('entityIdx')].fields.push(newField);
    $('#fieldModal').modal('hide');
  },
  editField: function (selector, path) {
    console.log('editField '+path+'...');
    $(selector).css('border-width','1px').css('padding','5px 10px 5px 10px');
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
    ractive.set('saveObserver', false);
    $.getJSON('/'+ractive.get('tenant.id')+'/domain/?projection=complete',  function( data ) {
      console.log('loaded entities...');
      ractive.merge('entities', data.entities);
      ractive.set('saveObserver',true);
      $('.entity.active').fadeIn();
    });
  },
  handleError: function(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) {
    case 401:
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
    ractive.set('entityIdx', ractive.get('entityIdx')+1);
    $('#entity'+ractive.get('entityIdx')+'Sect').fadeIn().addClass('active');
  },
  oninit: function() {
    console.log('oninit');
    //this.ajaxSetup();
    $( document ).ajaxStart(function() {
      $( "#ajax-loader" ).show();
    });
    $( document ).ajaxStop(function() {
      $( "#ajax-loader" ).hide();
    });
  },
  previousEntity: function() {
    console.log('previousEntity');
    $('.entity.active').fadeOut().removeClass('active');
    ractive.set('entityIdx', ractive.get('entityIdx')-1);
    $('#entity'+ractive.get('entityIdx')+'Sect').fadeIn().addClass('active');
  },
  save: function() { 
    setTimeout(1000, function() {
      showMessage('All entities saved');
    })
  },
  /*
  save: function () {
    console.log('save '+JSON.stringify(ractive.get('current'))+' ...');
    ractive.set('saveObserver',false);
    var id = ractive.get('current')._links === undefined ? undefined : (
        ractive.get('current')._links.self.href.indexOf('?') == -1 ? ractive.get('current')._links.self.href : ractive.get('current')._links.self.href.substr(0,ractive.get('current')._links.self.href.indexOf('?')-1)
    );
    ractive.set('saveObserver',true);

    if (document.getElementById('currentForm').checkValidity()) {
      // cannot save contact and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      console.log('account: '+JSON.stringify(tmp.account));
      tmp.notes = undefined;
      tmp.documents = undefined;
      if (id != undefined && tmp.account != undefined && Object.keys(tmp.account).length > 0 && tmp.account.id != undefined) {
        tmp.account = id.substring(0,id.indexOf('/',8))+'/accounts/'+tmp.account.id;
      } else {
        tmp.account = null;
      }
      tmp.tenantId = ractive.get('tenant.id');
      $.ajax({
        url: id === undefined ? '/contacts' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current._links.self.href',location);
          if (jqXHR.status == 201) ractive.get('contacts').push(ractive.get('current'));
          if (jqXHR.status == 204) ractive.splice('contacts',ractive.get('currentIdx'),1,ractive.get('current'));
          ractive.showMessage('Contact saved');
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
          ractive.handleError(jqXHR,textStatus,errorThrown);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as contact is incomplete');
    }
  },*/
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
  toggleResults: function(ctrl) {
    console.log('toggleResults: '+ctrl);
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
          ractive.showMessage('Successfully uploaded data');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          ractive.handleError(jqXHR, textStatus, errorThrown);
        }
      });
  }
});

ractive.observe('username', function(newValue, oldValue, keypath) {
  ractive.getProfile();
});

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

