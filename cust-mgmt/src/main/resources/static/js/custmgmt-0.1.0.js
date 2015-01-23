var TRANSITION_DURATION = 500;
// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new Ractive({
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
    server: 'http://api.knowprocess.com',
    contacts: []
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form').hide();
    $('.create-form').show();
    var contact = { author:localStorage['username'], url: undefined };
    ractive.select( contact );
  },
  addDoc: function (contact) {
    console.log('addDoc '+contact+' ...');
    ractive.set('current.doc', { author:localStorage['username'], contact: contact, url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
  },
  edit: function (contact) {
    console.log('edit'+contact+'...');
    $('h2.edit-form').show();
    $('.create-form').hide();
    ractive.select( contact );
  },
  save: function () {
    console.log('save '+ractive.data.current+' ...');
    var id = ractive.data.current._links === undefined ? undefined : ractive.data.current._links.self.href;
    if (document.getElementById('currentForm').checkValidity()) { 
      $.ajax({
        url: id === undefined ? '/contacts' : id,
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(ractive.data.current),
        success: completeHandler = function(data, textStatus, jqXHR) {
          console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          if (location != undefined) ractive.set('current._links.self.href',location);
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            alert("Bother: "+textStatus+':'+errorThrown);
        }
      });
    } else {
      console.warn('Cannot save yet as contact is invalid');
    }
  },
  saveDoc: function () {
    console.log('saveDoc '+ractive.data.current.doc.contact+' ...');
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
            alert("Bother: "+textStatus+':'+errorThrown);
        }
      });
    } 
  },
  addNote: function (contact) {
    console.log('addNote '+contact+' ...');
    ractive.set('current.note', { author:localStorage['username'], contact: contact, content: undefined});
    $('#notesTable tr:nth-child(1)').slideDown();
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
            alert("Bother: "+textStatus+':'+errorThrown);
        }
      });
    }
  },
  delete: function (url) {
    console.log('delete '+url+'...');
    $.ajax({
        url: url,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
        },
        error: errorHandler = function(jqXHR, textStatus, errorThrown) {
            alert("Bother: "+textStatus+':'+errorThrown);
        }
    });
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON("/contacts",  function( data ) {
      ractive.merge('contacts', data._embedded.contacts);
	});
  },
  oninit: function() {
	  this.fetch();

	  // init auto complete values
    $.get('/data/enquiry-types.json', function(data){
      $("#curEnquiryType").typeahead({ source:data });
	  },'json');
    $.get('/data/media.json', function(data){
      $("#curMedium").typeahead({ source:data });
    },'json');
    $.get('/data/sources.json', function(data){
      $("#curSource").typeahead({ source:data });
    },'json');
    $.get('/data/stages.json', function(data){
      $("#curStage").typeahead({ source:data });
    },'json');
  },
  select: function(contact) {
    console.log('select: '+JSON.stringify(contact));
    ractive.set('suspendSaveObserver',false);
	  ractive.set('current',contact);
	  if (contact._links != undefined) {
      $.getJSON(contact._links.self.href+'/notes',  function( data ) {
      	if (data._embedded != undefined) {
	        console.log('found notes '+data);
          ractive.merge('current.notes', data._embedded.notes);
	        // sort most recent first
  	      ractive.data.current.notes.sort(function(a,b) { return new Date(b.created)-new Date(a.created); });
      	}
  	  });
      $.getJSON(contact._links.self.href+'/documents',  function( data ) {
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
  }
});

// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  ignored=['current.documents','current.doc','current.notes','current.note'];
  // console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
  if (ractive.data.suspendSaveObserver && significantDifference(newValue,oldValue) && ignored.indexOf(keypath)==-1) {
    ractive.save();
  }
});

function significantDifference(newValue,oldValue) {
  if (newValue=='') { console.log('new value is empty');newValue = null;}
  if (oldValue=='') { console.log('oldvalue is empty');oldValue = null;}
  console.log('sig diff? '+newValue != oldValue);
  return newValue != oldValue;
}