var TRANSITION_DURATION = 500;
// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new Ractive({
  // The `el` option can be a node, an ID, or a CSS selector.
  el: 'container',

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
  addDoc: function (contact) {
    console.log('addDoc '+contact+' ...');
    ractive.set('current.doc', {author:"tstephen", contact: contact, url: undefined});
    $('#docsTable tr:nth-child(1)').slideDown();
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
    ractive.set('current.note', {author:"tstephen", contact: contact, content: undefined});
    $('#notesTable tr:nth-child(1)').slideDown();
  },
  saveNote: function () {
    console.log('saveNote '+ractive.data.current.note.contact+' ...');
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
  },
  select: function(contact) { 
	ractive.set('current',contact);
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
	$('#currentSect').slideDown();
  },
  upload: function() {
	var form = document.forms['contactList'];
    if (form.checkValidity()) {
      var msg = JSON.stringify(ractive.data.contacts);
//      console.log('Sending message: '+msg);
      // $('html, body').css("cursor", "wait");

//      $(':file').change(function(){
//    	    var file = this.files[0];
//    	    name = file.name;
//    	    size = file.size;
//    	    type = file.type;
//
//    	    if(file.name.length < 1) {
//    	    }
//    	    else if(file.size > 100000) {
//    	        alert("File is to big");
//    	    }
//    	    else if(file.type != 'image/png' && file.type != 'image/jpg' && !file.type != 'image/gif' && file.type != 'image/jpeg' ) {
//    	        alert("File doesnt match png, jpg or gif");
//    	    }
//    	    else {
//    	        $(':submit').click(function(){
    	            var formData = new FormData();
    	            formData.append('file', $(':file')[0].files[0])
    	            console.log('form data: '+formData);
    	            $.ajax({
    	                url: '/contacts/upload',  //server script to process data
    	                type: 'POST',
    	                // Form data
    	                data: formData,
    	                //Options to tell JQuery not to process data or worry about content-type
    	                cache: false,
    	                contentType: false,
    	                processData: false,
    	                xhr: function() {  // custom xhr
    	                    myXhr = $.ajaxSettings.xhr();
    	                    if(myXhr.upload){ // if upload property exists
//    	                        myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // progressbar
    	                    }
    	                    return myXhr;
    	                },
    	                //Ajax events
    	                success: completeHandler = function(data) {
    	                    /*
    	                    * workaround for chrome browser // delete the fakepath
    	                    */
    	                    if(navigator.userAgent.indexOf('Chrom')) {
    	                        var catchFile = $(":file").val().replace(/fakepath\\/i, '');
    	                    } else {
    	                        var catchFile = $(":file").val();
    	                    }
    	                    var writeFile = $(":file");
//    	                    console.debug(writer(catchFile));
//    	                    $("*setIdOfImageInHiddenInput*").val(data.logo_id);
                          ractive.fetch();
    	                },
    	                error: errorHandler = function(jqXHR, textStatus, errorThrown) {
    	                    alert("Bother: "+textStatus+':'+errorThrown);
    	                }
    	            }, 'json');
//    	        });
//    	    }
//    	});

    } else {
      console.log('Disclosure incomplete, returning to form');
    }
    return false;
  }
});
