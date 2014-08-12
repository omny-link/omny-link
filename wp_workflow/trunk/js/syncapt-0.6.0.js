// Console-polyfill. MIT license.
// https://github.com/paulmillr/console-polyfill
// Make it safe to do console.log() always.
(function(con) {
  'use strict';
  var prop, method;
  var empty = {};
  var dummy = function() {};
  var properties = 'memory'.split(',');
  var methods = ('assert,clear,count,debug,dir,dirxml,error,exception,group,' +
     'groupCollapsed,groupEnd,info,log,markTimeline,profile,profiles,profileEnd,' +
     'show,table,time,timeEnd,timeline,timelineEnd,timeStamp,trace,warn').split(',');
  while (prop = properties.pop()) con[prop] = con[prop] || empty;
  while (method = methods.pop()) con[method] = con[method] || dummy;
})(this.console = this.console || {}); // Using `this` for web workers.


/*
 * Copyright 2013-14 Tim Stephenson. All rights reserved.
 */
EASING_DURATION = 500;
TAB = 9;
ENTER = 13;
REQUIRED = '<span class="mandatory">*</span>';

if ($ == undefined && jQuery != undefined) { 
  console.log('aliasing jQuery to $'); 
  $ = jQuery; 
}
$.fn.moustache = function(data) {
  //console.info('invoking moustache template with data: '+JSON.stringify(data));
  var output = Mustache.render($(this).html(),data); 
  //console.info('produces: '+output);
  this.empty().append(output);
};

$(document).ready(function() {
  console.log('Ready event fired, binding actions to data-p attributes...');
  $p.init();
});

var $p = new App();
function App() {
  this.enhanceForms=true;
  this.server = 'http://api.syncapt.com:8080';
  //this.server = 'http://localhost:9090';
  this.init = function() {
    $p.bind();
  };
  this.initObj = function(d,dataAttr) {
    //console.log('init '+d.id+' from '+dataAttr);
    // check any objects needed by binding are initialised
    var expr = d.data(dataAttr);
    //console.log('expr:'+expr);
    var parts = expr.split('.');
    var ref = '';
    for (idx in parts) {
      if (ref.length >0) ref+='.';
      ref+=(parts[idx]);
      //console.log('Needs initialising? '+ref);
      var obj = eval(ref);
      if (obj == undefined && idx<(parts.length-1)) {
        console.log('Initialising '+ref);
        eval(ref+' = new Object();');
      }
    }
  };
  this.bind = function() {
    $('[data-p-init]').each(function(i,d) {
      eval($(d).data('p-init')+'=new Object();');
    });
    $p.sync();
    $p.bindActionHandlers();
    $p.bindControls();
    $p.bindCombos();
    $p.bindTables();
    $p.bindSectionsToNav();
  };
  this.bindActionHandlers = function() {
    $('[data-p-action]').click(function(ev) {
      var action = $(ev.target).data('p-action');
      if (action.indexOf('(') != -1) {
        console.log('invoking custom action "'+action+'"');
        eval(action);
      } else {
        console.log('sending message "'+msgName+'"');
        var mep = ($(ev.target).data('p-action')===undefined?'inOnly':'inOut');
        var msg = eval(action.substring(action.indexOf(':')+1));
        var msgName = action.substring(0,action.indexOf(':'));
        $p.sendMessage(mep,msgName,JSON.stringify(msg));
        ev.preventDefault();
      }
    });
  };
  this.bindCombos = function() {
    console.log('bind combos');
    $('[data-p-combo]').each(function(i,d) {
      var val = $(d).data('p-combo');
      console.log('setting up combo for '+d.name+' with values of '+val);
      $(d).autocomplete({
        change: function( event, ui ) {
          console.info('Change event '+event+' for '+event.target.id+', new val '+event.target.value);
          if (event.target.value == '') {
            var dataHolder = $(event.target).data('p-combo-bind'); 
            if ($(event.target).data('p-combo-type') != 'string') dataHolder = dataHolder.substring(0,dataHolder.lastIndexOf('.'));
            var cmd=dataHolder+'= null;';
            //console.log(cmd);
            eval(cmd);
          }
        },
        minLength: 0,
        source: eval(val),
        select: function( event, ui ) {
          console.info('select event '+event+' for '+event.target.id+', selecting '+ui.item.label);
          event.target.value= ui.item.label;
          if ($(event.target).data('p-combo-bind') !== undefined) {
            var cmd=$(event.target).data('p-combo-bind')+'= ui.item.value;';
            console.log(cmd);
            eval(cmd);
          } else {
            console.warn('no data binding for '+d.id);
          }
          if ($(event.target).data('p-combo-display') !== undefined) {
            var cmd=$(event.target).data('p-combo-display')+'= ui.item.label;';
            console.log(cmd);
            eval(cmd);
          } else {
            console.warn('no label binding for '+d.id);
          }
          return false;
        }
      });
      $(d).focus(function(ev) { 
        console.info('focus on '+ev.target.id+' opening drop down'); 
        $('#'+ev.target.id).autocomplete('search');
      });
    });
  };
  this.bindControls = function() {
    $('[data-p-bind].decorate')
      .addClass('form-control')
      .before(function(i) { 
        return '<label for="'+this.id+'">'+this.name+(this.required ? REQUIRED : '')+'</label>';
     })
      .removeClass('decorate')
      .wrap('<div class="form-group">');
    $('[data-p-display].decorate')
      .addClass('form-control')
      .removeClass('decorate')
      .wrap('<div class="form-group">');
    $('[data-p-bind]').each(function(i,d) {
      // check we do not have moustache template
      if ($(d).data('p-bind').indexOf('{')==-1) {
        console.info('binding control '+d.name+' to '+$(d).data('p-bind'));
        if ($(d).data('p-type')=='number') $(d).autoNumeric('init', {mDec:0});
        var val = eval($(d).data('p-bind'));
        $(d).on('blur', function(ev) {
          console.info('Blur on '+ev.target.name+'='+ev.target.value+', checked:'+$(ev.target).find(':checked').name);
          var cmd = ($(d).data('p-type')=='number') 
              ? $(d).data('p-bind')+'="'+$(d).autoNumeric('get')+'";'
              : $(d).data('p-bind')+'="'+$(d).val()+'";';
          if ($(d).val().length==0) cmd = $(d).data('p-bind')+'=null;';
          console.log('updating data model for '+d.name+' using '+cmd);
          eval(cmd);
          $p.sync();
        });
      }
    });
    $('[data-p-display]').each(function(i,d) {
      if ($(d).data('p-type')=='number') $(d).autoNumeric('init', {mDec:0});
    });
  };
  this.bindSectionsToNav = function() {
    $('[data-p-section]').each(function(i,d) {
      console.log('bind nav handler to '+$(d).data('p-section'));
      $(d).on('click',function() {
        var sect = $(d).data('p-section');
        console.log('clicked on nav section: '+sect);
        $('.nav li').removeClass('active');
        $(d).parent().addClass('active');

        $('p-section').fadeOut(500);
        $('#'+sect).delay(500).removeClass('hide').fadeIn(500);
      });
    });
  };
  this.bindTables = function() {
    $('[data-p-table]').each(function(i,d) {
      var entity = $(d).data('p-table');
      console.info('Binding data from "'+entity+'" to: "'+d.id+'"');
      console.info('Col Names are: '+$(d).data('p-table-colnames'));
      var colNames = $(d).data('p-table-colnames').split(',');
      var cols = [
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
        ];
      console.info('colNames found: '+colNames);
      var hot = $(d).handsontable({
        startRows: 1,
        startCols: colNames.length,
        rowHeaders: true,
        colHeaders: colNames,
        minSpareRows: 1,
        contextMenu: false,
        columns: cols,
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
            console.log('TODO update record: '+row);
            //pta.data.splice(row,1);
            //var p = pta.data[row];
            // LAST USED pta.set(row,col,to);
  //          pta.data.push(p);
            }
            //pta.save();
          } else {
            console.log('type change:'+typeof change);
            console.log(' source:'+source);
          }
          console.log('Autosaved (' + change.length + ' cell' + (change.length > 1 ? 's' : '') + ')');
          console.log('... source: '+source+', change:'+change);
  //          pta.data.push(new Person(change.splice(0,1)));
        },
        afterRemoveRow: function(idx, amount) {
          console.log('removed row: '+idx+','+amount);
  //          pta.data.splice(idx, amount);
          //pta.save();
        }
      });
      $('html, body').css("cursor", "wait");
      return $.ajax({
        type: 'GET',
        url: $p.server+'/'+entity,
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('success fetching data');
          //localStorage['GET_repository_definitions']=response;
          hot.loadData(response);
          $('html, body').css("cursor", "auto");
        },
        error: function(jqXHR, textStatus, errorThrown) {
          console.log('error:'+textStatus+':'+errorThrown);
          console.log('  headers: '+JSON.stringify(jqXHR.getAllResponseHeaders()));
          $('html, body').css("cursor", "auto");
        }
      });
    });
  };
  this.getResource = function(resource, searchExpr, callback) {
    console.log('get resource "'+resource+'", filtered by: '+JSON.stringify(searchExpr));
    if ($p.isOffline()) {
      callback(JSON.parse(localStorage['GET_'+resource]));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+resource,
        contentType: 'application/json',
        data: searchExpr,
        dataType: 'text',
        username: app.username,
        password: app.password,
        done: function() {
          console.info('Received reply');
        },
        success: function(response) {
          console.info('Received reply');
          localStorage['GET_'+resource]=response;
          callback(JSON.parse(response));
        },
        error: function(jqXHR, textStatus, errorThrown) { 
          console.log('ERROR '+ jqXHR.statusCode());
          console.log('error:'+textStatus+':'+errorThrown);
        }
      });
    }
  };
  this.hideActivityIndicator = function(msg) {
    if (msg === undefined) msg = 'Success!';
    $('.p-messages').empty().append(msg).removeClass('blink');
    document.body.style.cursor='auto';
    // enable to allow messages to fade away
    /*setTimeout(function() {
      $('.p-messages').fadeOut();
    }, EASING_DURATION*10);*/
  };
  this.isOffline = function() { 
    return false; 
  };
  this.sendMessage = function(mep, msgName, msg) {
    console.log('Sending '+msgName+' as mep: '+mep);
    var type = ((mep == 'inOut' || mep == 'outOnly') ? 'GET' : 'POST');
    var d = (mep == 'inOut' ? {query:msg} : {json:msg});
    console.log('msg: '+ msg);
    $p.json = msg;
    // this strips non-significant white space
    if (msg.length>0) msg = JSON.stringify(JSON.parse(msg));
    console.log('msg: '+ msg);
    $('html, body').css("cursor", "wait");
    return $.ajax({
      type: type,
      url: $p.server+'/msg/'+msgName,
      /* Uncomment to send as single JSON blob instead of form params
      contentType: 'application/json',*/
      data: d,
      dataType: 'text',
      timeout: 30000,
      success: function(response, textStatus, jqxhr) {
        console.log('successfully start instance by msg: '+jqxhr.getResponseHeader('Location'));
        console.log('  headers: '+JSON.stringify(jqxhr.getAllResponseHeaders()));
        console.log('  response: '+JSON.stringify(response));
        $p.hideActivityIndicator();
      },
      error: function(jqXHR, textStatus, errorThrown) {
        var msg = 'Error saving: '+textStatus+' '+errorThrown;
        console.error(msg);
        $p.hideActivityIndicator(msg);
      },
      complete: function(data, textStatus, jqxhr) {
        //console.log('successfully start instance by msg: '+jqxhr.getResponseHeader('Location'));
        console.log('complete:'+textStatus+' data: '+JSON.stringify(data)+' jqxhr'+jqxhr);
      }
    });
  };
  this.showActivityIndicator = function(msg) {
    document.body.style.cursor='progress';
    this.showMessage(msg);
  };
  this.showMessage = function(msg, additionalClass) {
    if (msg === undefined) msg = 'Working...';
    $('.p-messages').empty().append(msg).addClass(additionalClass).show();
  };
  this.sync = function() {
    //console.log('... contact is: '+JSON.stringify($p.contact));
    $('[data-p-bind]').each(function(i,d) {
      // check we do not have moustache template
      if ($(d).data('p-bind').indexOf('{')==-1) {
        $p.initObj($(d), 'p-bind');
        // create data binding
        var val = eval($(d).data('p-bind'));
        if (val != undefined) {
          console.log('... '+i+':'+val+' into '+d.name);
          $(d).val(val);
        }
        if ($(d).data('p-type')=='number') $(d).autoNumeric('init', {mDec:0});
        if (val != undefined && $(d).data('p-type')=='number') $(d).autoNumeric('set',val);
      }
    });
    $('[data-p-combo-bind]').each(function(i,d) {
      // check we do not have moustache template
      if ($(d).data('p-combo-bind').indexOf('{')==-1) {
      $p.initObj($(d), 'p-combo-bind');
      var val = eval($(d).data('p-combo-display'));
      if ($(d).data('p-l10n')!=undefined && $p.l10n!=undefined) val=$p.l10n.getLabelText(val);
      $(d).val(val);
      }
    });
    $('[data-p-display]').each(function(i,d) {
      // check we do not have moustache template
      if ($(d).data('p-display').indexOf('{')==-1) {
      $p.initObj($(d), 'p-display');
      // check we do not have moustache template
      if ($(d).data('p-display').indexOf('{')==-1) {
        // create display only binding
        var val = eval($(d).data('p-display'));
        if ($(d).data('p-l10n')!=undefined && $p.l10n!=undefined) val=$p.l10n.getLabelText(val);
        console.log('... '+i+':'+val+' into '+d.name);
        $(d).empty().append(val);
        if ($(d).data('p-type')=='number') $(d).autoNumeric('init', {mDec:0});
        if (val != undefined && $(d).data('p-type')=='number') $(d).autoNumeric('set',val);
      }
      }
    });
  };
}
String.prototype.toLeadingCaps = function() {
    return this.substring(0,1).toUpperCase()+this.substring(1).toLowerCase();
};
