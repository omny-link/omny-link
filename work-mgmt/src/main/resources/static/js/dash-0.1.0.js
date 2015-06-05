$ = jQuery;

var EASING_DURATION = 500;
fadeOutMessages = true;

// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new Ractive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',

  // If two-way data binding is enabled, whether to only update data based on
  // text inputs on change and blur events, rather than any event (such as key
  // events) that may result in new data
  lazy: true,

  // We could pass in a string, but for the sake of convenience
  // we're passing the ID of the <script> tag above.
  template: '#dashTemplate',
  // partial templates
  // partials: { question: question },

  // Here, we're passing in some initial data
  data: {
    //csrfToken: getCookie(CSRF_COOKIE),
    //server: 'http://api.knowprocess.com',
    //content: 'test',
    entities: [],
    //saveObserver:false,
    username: localStorage['username'],
    age: function(timeString) {
      return i18n.getAgeString(new Date(timeString))
    }
  },
  fetchByProxy: function (resource,keypath) {
    console.log('fetchByProxy...');

    var d = {
      action: 'p_resource',
      resource: resource 
    };
    return $.ajax({
      type: 'GET',
      url: '/wp-admin/admin-ajax.php',
      data: d,
      dataType: 'json',
      timeout: 30000,
      success: function(data, textStatus, jqxhr) {
        console.log('loaded list...');
        ractive.set(keypath, data);
        ractive.initAutoComplete();
      }
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
//    $('.expr-name .typeahead').click(function(ev) {
//      console.log(this);
//      console.log(ev);
//      if (this.typeahead()==undefined) {
//        console.error('not inited');
//      } else {
//        console.log('inited');
//      }
//    });
    $('.expr-name .typeahead').each(function(i,d) {
      console.log('binding entities to typeahead control: '+d.name);
      $(d).typeahead({ minLength:0,source:ractive.get('entityAttrs')});
    });
//      $(d.selector).on("click", function (ev) {
//        newEv = $.Event("keydown");
//        newEv.keyCode = newEv.which = 40;
//        $(ev.target).trigger(newEv);
//        return true;
//     });
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
  }
});

$(document).ready(function() { 
  ractive.fetchByProxy('/contacts/','contacts');
  ractive.fetchByProxy('/domain/?projection=complete','domain');
  ractive.fetchByProxy('/decision-models/','decisions');
  ractive.fetchByProxy('/tasks/','tasks');
});
