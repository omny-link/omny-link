var TRANSITION_DURATION = 500;
var REPO_SERVER = 'http://localhost:8081'
// 4. We've got an element in the DOM, we've created a template, and we've
// loaded the library - now it's time to build our Hello World app.
var ractive = new BaseRactive({
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
    models: []
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
    $.getJSON(REPO_SERVER+"/models",  function( data ) {
      ractive.merge('models', data._embedded.models);
	});
  },
  oninit: function() {

	  this.fetch();
  },
  upload: function() {
	var form = document.forms['modelRepository'];
    if (form.checkValidity()) {
      var msg = JSON.stringify(ractive.data.models);
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
    	                url: '/models/upload',  //server script to process data
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
