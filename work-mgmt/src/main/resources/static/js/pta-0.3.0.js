$.fn.moustache = function(data) {
  var output = Mustache.render($(this).html(),data); 
  this.empty().append(output);
};

function toObject(arr) {
	var c = new Object();
	$.each(arr, function(i,d) {
		console.log();
		eval('c.'+d.name+'="'+escape(d.value)+'"');
	});
	return c; 
}
 
EASING_DURATION = 1000;
DATA_KEY = 'data';
TABLE_KEY = '#edit';

var username = 'tim'; 
var pta = new Pta();
$(document).ready(function() {
	pta.init();
});

function Person(name, childrenStartYears, email, phone, lists, crb, firstAid) {
	
	this.setChildrenStartYears = function(csList) {
		this.childrenStartYears = csList === undefined ? [] : csList.split(',');
	};
	this.setMailingLists=function(csList) {
		this.mailingLists = csList === undefined ? [] : csList.split(',');
	};
	this.setCrb = function(bool) {
		this.crb = (bool == true || bool == 'true' || bool == 'yes') ? true : false; 
	};
	this.setFirstAid = function(bool) {
		this.firstAid = (bool == true || bool == 'true' || bool == 'yes') ? true : false; 
	};
	
  this.name = name === undefined ? 'n/a' : name;
	this.setChildrenStartYears(childrenStartYears);
	this.email = email=== undefined ? 'n/a' : email; 
	this.phone = phone=== undefined ? 'n/a' :phone; 
	this.setMailingLists(lists);
	this.setCrb(crb);
	this.setFirstAid(firstAid);
	return this;
}
Person.labels = 
	 ['Name', 'Children Years', 'Email Address', 'Phone Number', 'Mailing Lists', 'CRB', 'First Aid'];

function Pta() {
//	this.server = 'http://cms.bigbpmcloud.cloudbees.net/';
	this.server = 'http://localhost:8080/';
	this.supportsLocalStorage = function() {
	  try {
	    return 'localStorage' in window && window['localStorage'] !== null;
	  } catch (e) {
	    return false;
	  }
	};
	this.bindActions = function() {
    $('[data-action]').each(function(i,d) { 
  	  console.log('bind click handler to '+d.id);  
  	  $(d).on('click',function() {
    		console.log('clicked button: '+d.id);  
    		eval($(d).data('action'));
      });
    });
  }
	this.bindSectionsToNav = function() {
	  $('[data-section]').each(function(i,d) { 
      console.log('bind nav handler to '+$(d).data('section'));  
      $(d).on('click',function() {
        var sect = $(d).data('section');
        console.log('clicked on nav section: '+sect);  
        $('.nav li').removeClass('active');
        $(d).parent().addClass('active');
        
        $('section').fadeOut(500);
        $('#'+sect).delay(500).removeClass('hide').fadeIn(500);
      });
    }); 
	};
	this.deleteAll = function() {
	  if (confirm("STOP! If you click 'ok' we will delete all the data and it will not be possible to retrieve it. You are advised to make a backup.")) {
	    pta.initData();
	  }
	}
	this.handleAjaxError = function(textStatus, errorThrown) { 
		pta.showMessage(textStatus+':'+errorThrown);
	};
	this.init = function() {
      this.$console = $("#msg");
      if (!this.supportsLocalStorage()) {
    	pta.showMessage("You seem to be running an ancient browser, please use Google Chrome, Internet Explorer 10 or higher or Mozilla Firefox.");   
        return false;
      }		
		
      var tmp = localStorage[DATA_KEY];
      switch (typeof tmp) {
      case 'string':
    	  if (tmp.length==0) {
      		console.log('initialised empty data');
      		pta.initData();
    	  } else {
	    	  pta.data=[];
	    	  pta.load(JSON.parse(tmp));
    	  }
    	  break ; 
      case 'object':
    	  pta.data=[];
    	  pta.data.push(tmp);
    	  console.log('Added single object to: '+ pta.data);
    	  break; 
      default: 
    	  pta.initData();
    	  break;
      }
      
      /*$('#table').handsontable({
        data: data,
        minSpareRows: 1,
        colHeaders: true,
        contextMenu: true
      });*/

      
      var $container = $(TABLE_KEY);
      
      $container.handsontable({
        startRows: 1,
        startCols: Person.labels.length,
        rowHeaders: true,
        colHeaders: Person.labels,
        minSpareRows: 1,
        contextMenu: false,
        columns: [
          { data: "name" },
          { data: "childrenStartYears", validator: this.csNumericValidator },
          { data: "email", validator: this.emailValidator, allowInvalid: false },
          { data: "phone" },
          { 
        	data: "mailingLists", 
	        /* autocomplete only supports single select
	         * type: "autocomplete",
	        source: ["Committee", "Meetings", "Volunteers", ""], //empty string is a valid value
	        strict: true*/
	        },
          { data: "crb", type: "checkbox", allowInvalid:true  },
          { data: "firstAid", type: "checkbox" }
        ],
        columnSorting: true,
        /* This works but is very verbose. would prefer placeholder
         * consider custom renderer: http://handsontable.com/demo/renderers_html.html
         * or http://handsontable.com/demo/prepopulate.html
        dataSchema: { name: "", 
        	childrenStartYears: "", 
        	email: "", 
        	phone: "", 
        	mailingLists: "", 
        	crb: false, 
        	firstAid: false
        },*/
        removeRowPlugin: true,
        afterChange: function (change, source) {
          if (source === 'loadData') {
	          return; //don't save this change
          } else if (source === 'edit') {
            console.log('type change:'+typeof change);
    			  console.log('json change:'+JSON.stringify(change));
    			  window.change = change; 
  		      row = change[0][0];
  		      col = change[0][1]; 
  		      from = change[0][2];
  		      to = change[0][3];
  		      console.log('my change:'+row+','+col+' from '+from+' to '+to);
  				
  		      if (row>=pta.data.length) { // create 
  		    	console.log('create new record');
  		    	var p = new Person();
  		    	pta.data.push(p);	  
  		    	pta.set(row,col,to);
  		    } else { // update
  		    	console.log('update record: '+row);
  		    	//pta.data.splice(row,1);
  		    	//var p = pta.data[row];
  		    	pta.set(row,col,to);
  //		    	pta.data.push(p);	
  		      }
    			  pta.save();
    			} else {
    				console.log('type change:'+typeof change);
    				console.log(' source:'+source);
    			}
	        console.log('Autosaved (' + change.length + ' cell' + (change.length > 1 ? 's' : '') + ')');
	        console.log('... source: '+source+', change:'+change);
//	        pta.data.push(new Person(change.splice(0,1)));
        },
        afterRemoveRow: function(idx, amount) {
    	    console.log('removed row: '+idx+','+amount); 
//          pta.data.splice(idx, amount);
    	    pta.save();
        }
      });
      $('.htCore [data-column="0"]').attr('title',"parent's name");
      $('.htCore [data-column="2"]').attr('title',"comma-separated list of children's year class");
      $('.htCore [data-column="3"]').attr('title','email address');
      $('.htCore [data-column="4"]').attr('title','phone number');
      $('.htCore [data-column="5"]').attr('title','comma separated list, e.g. committee, meetings, volunteers');
      $('.htCore [data-column="6"]').attr('title','holds current CRB check');
      $('.htCore [data-column="7"]').attr('title','holds current first aider certificate');
      var handsontable = $container.data('handsontable');

      console.log('data: '+ pta.data);
      //console.log('data type: '+ typeof data);
      if (pta.data.length>0){
        handsontable.loadData(pta.data);
        pta.$console.text('Data loaded');
      }
      
      pta.bindActions();
      pta.bindSectionsToNav();
      pta.initBackup();
	};
	this.initBackup = function() {
    var data = localStorage[DATA_KEY];
    window.URL = window.URL || window.webkitURL;
    var blob = new Blob([data], {type: 'application/json'});
    $('#backupJson')
      .attr('href',window.URL.createObjectURL(blob), 'pta.json')
      .attr('download','pta.json').removeClass('hide');
  };
  this.initData = function() {
    pta.data=[];
    pta.data.push(new Person('Vicky','2012,2012','','','committee,meetings,supporters',true,true));
    pta.save();
    console.log('initialised empty data');
  };
  this.load = function(json) {
    console.log('Parsed JSON to: '+ json);
//     JSON serialisation to localStorage seems to lose type
    pta.rejected = [];
    for(var idx in json) {
      var o = json[idx];
      o.prototype=Person.prototype;
      var add = true;
      $.each(pta.data, function(i,d) {
        if (d.email != null && d.email.length > 0 && d.email == o.email) {
          pta.rejected.push(o);
          add = false; 
          return; 
        } 
      });
      if (add) pta.data.push(o);
    }
    if (pta.rejected.length>0) {
      console.log('error: rejected duplicates: '+pta.rejected);
    }
    return pta;
  };
  this.mailingList = function(list) {
    var addresses = '';
    $.each(pta.data, function(i,d) {
      console.log('test idx '+i+' for inclusion:'+JSON.stringify(d));
      if (d.email != null && (list=='all' || (d.mailingLists!=null && d.mailingLists.indexOf(list)!=-1))) {
        addresses += (d.email+',');
      } else {
        try {
          console.log('... skip: '+d.email != null
              +','+d.mailingLists!=null+', '+d.mailingLists.indexOf(list)!=-1);
        } catch (e) {};
      }
    });
    $('#addresses').empty().append(addresses);
  };
  this.restore = function(fileCtrl) {
    console.log('restore: '+fileCtrl);
    var reader = new FileReader();
    reader.onload = function(event) {
        var contents = event.target.result;
        var records = JSON.parse(contents); 
        console.log('File contents: '+ contents+'parsed into '+records.length+' records');
        console.log(JSON.stringify(pta.data));
        // remove final empty row. 
        pta.data.splice(pta.data.length-1,1);
        pta.load(records).save();
        // TODO this doesn't work here but does in console 
        //$(TABLE_KEY).data('handsontable').loadData(pta.data);
//        pta.init();
//        $('[data-section="edit"]').click();
        window.location.reload();
    };

    reader.onerror = function(event) {
        console.error("File could not be read! Code " + event.target.error.code);
    };

    for (idx in fileCtrl.files) {
      // Check for the various File API support.
      if (window.File && window.FileReader && window.FileList && window.Blob) {
        reader.readAsText(fileCtrl.files[idx]);
      } else {
        var msg = 'The File APIs are not fully supported by your browser.'; 
        console.log(msg);
        pta.showMessage(msg);
      }
    }
  };
	this.showMessage = function(msg) {
		pta.$console.empty().append(msg);
		pta.$console.show();
		setTimeout(function () {
		  pta.$console.fadeOut(1000);
		}, 1000);	
	};
	this.set = function(row,col, val) {
		console.log('set... row:'+row+' col:'+col+', val:'+val);
	  var p = pta.data[row];
      switch(col){
	  case 0: 
		  p.name = val; 
		  break;
	  case 1: 
		  p.setChildrenStartYears(val); 
		  break;
	  case 2: 
		  p.email = val; 
		  console.log('set email to: '+p.email);
		  break;
	  case 3: 
		  p.phone = val; 
		  break;
	  case 4: 
		  p.setMailingLists(val); 
		  break;
	  case 5: 
		  p.setCrb(val); 
		  break;
	  case 6: 
		  p.setFirstAid(val); 
		  break;
	  }	
    };
	
	this.save = function() {
		console.log('storing: '+JSON.stringify(pta.data));
        localStorage['data']=JSON.stringify(pta.data);
        pta.showMessage('Changes auto-saved');
	};
	this.emailValidator = function(value, callback) {
	  setTimeout(function(){
	    if (/.+@.+/.test(value)) {
	      callback(true);
	    } else {
	      pta.showMessage("'"+value+"' is not a valid email address.");
	      callback(false);
	    }
	  }, 1000);
	};
	this.csNumericValidator = function(value, callback) {
    setTimeout(function(){
      if (/[\d+,]+/.test(value)) {
        callback(true);
      } else {
        pta.showMessage("'"+value+"' is not a valid, Please enter a comma-separated list of years.");
        callback(false);
      }
    }, 1000);
  };
}