$.fn.moustache = function(data) {
  //console.debug('invoking moustache template with data: '+JSON.stringify(data));
  var output = Mustache.render($(this).html(),data);
  //console.debug('produces: '+output);
  this.empty().append(output);
};

EASING_DURATION = 500;
TAB = 9;
ENTER = 13;
CSRF_COOKIE = 'XSRF-TOKEN';

function MarkupParser() {
  this.parse = function(s) {
    var searchExpr = new Object();
    var exprs = s.split(' ');
    $.each(exprs, function(i,d) {
      d = d.toLowerCase();
      if (d=='--assigned') {
        searchExpr.assignee = username;
      } else if (d=='--offered') {
        searchExpr.candidateUser = username;
      } else if (d.substring(0,1)=='+') {
        if (d.indexOf('@')!=-1) {
          searchExpr.user==undefined
              ? searchExpr.user=d.substring(1,d.length)
              : searchExpr.user+=d.substring(1,d.length);
          searchExpr.user+=',';
        } else {
          searchExpr.group==undefined
              ? searchExpr.group=d.substring(1,d.length)
              : searchExpr.group+=d.substring(1,d.length);
          searchExpr.group+=',';
        }
      } else if (d=='@monday') {
        console.error('Not yet supported');
      } else if (d.substring(0,1)=='@') {
        var number = d.substring(d.search(/\d/),d.search(/[dwmy]/));
        var unit = d.substring(d.search(/[dwmy]/),d.length).substring(0,1);
        var factor = 1;
        switch (unit) {
        case 'd':
          break;
        case 'w':
          factor =7;
          break;
        case 'm':
          factor=30;// TODO approximation...
          break;
        case 'y':
          factor=365;
          break;
        }
        searchExpr.date = new Date(new Date().getTime()+(factor*number*24*60*60*1000));
      } else {
        console.error('unhandled expr: '+d);
      }
    });
    if (searchExpr.user!=undefined && searchExpr.user.substring(searchExpr.user.length-1,searchExpr.user.length)==',')
      searchExpr.user=searchExpr.user.substring(0,searchExpr.user.length-1);
    if (searchExpr.group!=undefined && searchExpr.group.substring(searchExpr.group.length-1,searchExpr.group.length)==',')
      searchExpr.group=searchExpr.group.substring(0,searchExpr.group.length-1);
    return searchExpr;
  };
}

var app = new App(new Controller());
app.init();

/**
 * Encapsulates access to data whether from server or local.
 */
function App(controller) {
  // By default assume BPM server is co-located. If not, can revise by changing this. 
  this.server= "";
  //this.server = "https://www.knowprocess.com";
//  this.server= "http://api.trakeo.com";
//  this.server="http://localhost:8080/bpm-server-1.1.0.BUILD-SNAPSHOT";
//  this.server= "http://api.syncapt.com";

  this.locale = 'en_GB';
  // Callback to handle rendering
  this.ctrl = controller;
  this.markupParser = new MarkupParser();
  this.connect = function(url) {
    console.log('connect to '+url);
    app.server = url;
    var userId = getSearchParameters()['email'];
    if (userId === undefined) {
        app.identity();
    } else {
      console.log('Logged in as '+userId);
      app.username = userId;
          app.ctrl.loadImage(app.username);
    }
    //$($('.nav li[class="active"] a')[0]).click();
  };
  this.init = function() {
  // detect if mobile or desktop
    if (window.PhoneGap === undefined && window.Cordova === undefined) {
      console.log('Environment: Browser');
    }else{
      console.log('Environment: PhoneGap/Cordova');
    }
    // dynamically create nav
    $('#nav').moustache(app);
    // localise strings
//    if (app.urlParam('locale')!==undefined && app.urlParam('locale')!=null) {
//      app.locale = app.urlParam('locale');
//    }
    i18n.localize(app.locale);
    // Enable self-test
    if (app.urlParam('test')!==undefined && app.urlParam('test')!=null) {
      $('#selfTest').removeClass('hidden');
    }
    // set up search
    $('.search-query').on('keydown', function(ev) {
      // Note that keydown does not contain the last key stroke as keypress does
      var key = ev.keyCode || ev.charCode;
        if( key == TAB || key == ENTER ) {
          app.taskList(app.markupParser.parse($('.search-query').val()));
          //console.log('after tasklist');
          ev.preventDefault();
        }
    });

    $('#serverUrl').val(app.server);
//    app.connect(app.server);

    // Setup to be performed every time we change data.
//    $(document).ajaxSuccess(function() {
//      console.log( "Triggered ajaxSuccess handler." );
//      app.initDeferred();
//    });
    
    
  };
//  this.initDeferred = function() {
//
//  };
  this.transition = function(navLink) {
    console.log('transition...');
    $('.nav li').removeClass('active');
    $('[data-section]').each(function(i,d) {
      //console.log('... hide '+$(d).data('section')+'...');
      $('#'+$(d).data('section')).hide();
    });
    var sect = $(navLink).parent().addClass('active').data('section');
    console.log('... to '+sect);
    $('#'+sect).show().removeClass('hidden');
    /*$('.nav > li').each(function(i,d) {
        console.log(i+','+$(d).data('section')+' has classes: '+$('#'+$(d).data('section')).attr('class'));
    });*/
  };
  this.addDeploymentResource = function() {
    $('#deployProcessTemplate > fieldset').append($('#resourceControl').html());
  };
  this.addFormControls = function(executable) {
    $.each(executable.formProperties, function(i,d) {
        console.log('create control for '+d.id);
        d.requiredattr = function() {
          console.log('setting required = '+ d.required);
          if (d.required) return 'required';
        };
        d.control = function() {
            console.log('ctrl: '+ d.type);
            switch (d.type) {
            case 'date':
              return function (text, render) {
                console.log('date ctrl: '+text);
                return render(text.replace(/input-xlarge/g,'input-medium'));
              };
            case 'enum':
              return function (text, render) {
                console.log('enum 2: ');
                text = '<select class="input-xlarge" id="'+d.id+'" name="'+d.id+'">';
                if (!d.required) {
                  text += '<option></option>';
                }
                for ( var idx in d.enumValues) {
                  text += '<option value="'+d.enumValues[idx].id+'">'+d.enumValues[idx].name+'</option>';
                }
                text += '</select>';
                return text;
              };
            default:
              return function (text, render) {
                console.log('ctrl 2: '+text);
                return render(text);
              };
            }
          };
      });
  };
  this.definitionCategory = function(definitionId, newCategory) {
    // Note this throws '405 (HTTP method PUT is not supported by this URL)'
    // TODO Maybe new in 5.14?
    console.log('setting category of '+definitionId+' to '+newCategory);
    return $.ajax({
      type: 'PUT',
      url: app.server+'/process-definitions/'+definitionId,
      contentType: 'application/json',
      data: '{"category" : "'+newCategory+'"}',
      dataType: 'json',
      success: function(response) {
        console.log('successfully updated category');
      },
      error: function(jqXHR, textStatus, errorThrown) {
        app.handleError(jqXHR, textStatus, errorThrown)
      }
    });
  };
  this.dashboardList = function() {
    if (app.ctrl.offline) {
      app.ctrl.loadCaseList(JSON.parse(localStorage['GET_cases']));
    } else {
      /*return $.ajax({
        type: 'GET',
        url: app.server+'/cases',
        contentType: 'application/json',
        dataType: 'text',
        success: function(response) {
          console.log('success fetching definitions');
          localStorage['GET_cases']=response;
          app.ctrl.loadCaseList(JSON.parse(response));
        },
        error: function(jqXHR, textStatus, errorThrown) {
          console.log('error:'+textStatus+':'+errorThrown);
          console.log('  headers: '+JSON.stringify(jqXHR.getAllResponseHeaders()));
        }
      });*/
      var cases = [
        { id: "1234", name: "AB-1234", description: "Wheelchair carbon footprint reduction" },
        { id: "5678", name: "AB-5678", description: "Replacement hip joint child labour risk" },
        { id: "9876", name: "TS-9876", description: "Uniforms security of supply" },
        { id: "5432", name: "XY-5432", description: "Wound kit packaging reduction" }
      ]
      localStorage['GET_cases']=JSON.stringify(cases);
      app.ctrl.loadCaseList(cases);

      var tasks = [
        { id: "544", assignee: "Tim", code: "AB-1234-11", createTime: new Date().getTime()-1000000, dueDate: new Date().getTime()+900000000, name: "Estimate carbon footprint of new steel wheelchair" },
        { id: "583", assignee: "Fiona", code: "AB-1234-32", createTime: new Date().getTime()-5000000, dueDate: new Date().getTime()+500000000, name: "Review and update aluminium wheelchair footprint" },
        { id: "633", assignee: "Derek", code: "AB-1234-43", createTime: new Date().getTime()-20000000, dueDate: new Date().getTime()+100000000, name: "Respond to supplier on clean energy proposal for plant" }
      ]
      localStorage['GET_runtime_tasks']=JSON.stringify(tasks);
      app.ctrl.loadTaskList(tasks, '#caseTasks');

      var events = [
        { id: "524", name: "AB-1234-11", type: "Task initiated", description: "Estimate carbon footprint of new steel wheelchair" },
        { id: "563", name: "AB-1234-32", type: "Process initiated", description: "Product Review Process: Aluminium wheelchair by ACME" },
        { id: "613", name: "AB-1234-43", type: "Received", description: "Clean energy proposal for plant from ACME" }
      ]
      localStorage['GET_history_activities']=JSON.stringify(events);
      app.ctrl.loadEventList(events, '#caseEvents');

    }
  };
  this.definition = function(id) {
    if (app.ctrl.offline) {
      $.each(JSON.parse(localStorage['GET_repository_definitions']), function(i,d) {
        if (d.id == id) {
//          app.
        }
      });
      // TODO 
      console.error('Not available offline');
      //app.ctrl.loadInstanceList(JSON.parse(localStorage['GET_repository_definitions']));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/process-definitions/'+id,
        contentType: 'application/json',
        dataType: 'text',
        success: function(response) {
          console.log('success fetching definition of '+id);
//          localStorage['GET_repository_definitions']=response;
          app.definition = (JSON.parse(response));
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.definitionAsBpmn = function(id) {
    if (app.ctrl.offline) {
      app.definition = undefined;
      $.each(JSON.parse(localStorage['GET_repository_definitions']), function(i,d) {
        if (d.id == id) {
          app.definition = d; 
        }
      });
      if (app.definition == undefined) {
        console.error('Definition of '+id+' is not available offline');
        app.showError('Definition of '+id+' is not available offline');
      }
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/process-definitions/'+id,
        dataType: 'xml',
        success: function(response) {
          console.log('success fetching definition of '+id+': '+response);
//          localStorage['GET_repository_definitions']=response;
          app.response = response;
          var ser = new XMLSerializer();
          app.definitionBpmn = ser.serializeToString(response);
          var newTab = window.open('data:application/xml;'+encodeURI(app.definitionBpmn),'_newtab');
          //newTab.document.write(app.definitionBpmn);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.definitionImage = function(id) {
    if (app.ctrl.offline) {
      $.each(JSON.parse(localStorage['GET_repository_definitions']), function(i,d) {
        if (d.id == id) {
//          app.
        }
      });
      // TODO 
      console.error('Not available offline');
      //app.ctrl.loadInstanceList(JSON.parse(localStorage['GET_repository_definitions']));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/process-definitions/'+id,
        accepts: 'image/png',
        success: function(response) {
          console.log('success fetching definition of '+id);
//          localStorage['GET_repository_definitions']=response;
          app.definitionImage = response;
          var newTab = window.open(id+'.png','_newtab');
          newTab.document.write(app.definitionImage);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.definitionList = function() {
    if (app.ctrl.offline) {
      app.ctrl.loadInstanceList(JSON.parse(localStorage['GET_repository_definitions']));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/process-definitions/',
        contentType: 'application/json',
        dataType: 'text',
        success: function(response) {
          console.log('success fetching definitions');
          localStorage['GET_repository_definitions']=response;
          app.ctrl.renderDefinitionsList(JSON.parse(response));
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.definitionUpload = function (formId) {
    console.log('definitionUpload, id: '+formId);
    app.showActivityIndicator('Uploading definition...');

    var formElement = document.getElementById(formId);
    var formData = new FormData(formElement);
    return $.ajax({
        type: 'POST',
        url: app.server+'/deployments',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
//          console.log('successfully uploaded definition');
          app.hideActivityIndicator('successfully uploaded definition');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          console.log(textStatus+':'+errorThrown+','+jqXHR.responseText);
          var resp = JSON.parse(jqXHR.responseText.replace(/\n/g,''));
          app.hideActivityIndicator('Failed to upload definition, error is: '+resp.error);
        }
      });
  };
  this.deleteDefinition = function(definitionId,deploymentId) {
    console.log('delete: '+ deploymentId);
    return $.ajax({
        type: 'DELETE',
        url: app.server+'/deployments/'+deploymentId,
        contentType: 'application/json',
        dataType: 'text',
        success: function() {
          console.log('successfully deleted:'+definitionId);
          $('[data-id="'+definitionId+'"]').hide(EASING_DURATION);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.deleteInstance = function(instanceId) {
    console.log('delete instance: '+ instanceId);
    return $.ajax({
        type: 'DELETE',
        url: app.server+'/process-instances/'+instanceId,
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('successfully deleted:'+instanceId);
          $('[data-id="'+instanceId+'"]').hide(EASING_DURATION);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.handleError = function(jqXHR, textStatus, errorThrown) {
    console.log(textStatus+':'+errorThrown+','+jqXHR.responseText);
    console.log('  headers: '+JSON.stringify(jqXHR.getAllResponseHeaders()));
    var response = JSON.parse(jqXHR.responseText);
    if (response.path == '/login') window.location.href = '/login';
    app.hideActivityIndicatorWithError(jqXHR);
  },
  this.help = function() {
    return $.ajax({
      type: 'GET',
      url: '/markup.html',
      success: function(response) {
        $('#helpSect').empty().append(response.replace(/h2/g,'h4').replace(/h3/g,'h5'));
      },
      error: function(jqXHR, textStatus, errorThrown) {
        app.handleError(jqXHR, textStatus, errorThrown)
      }
    });
  };
  this.identity = function() {
    console.log('TODO find identity of current user');
    /*return $.ajax({
        type: 'GET',
        url: app.server+'/identity.jspx',
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('success fetching identity:'+response);
          if (response.email === undefined) {
            // no authenticated session in the bpms server
            window.location.href = app.server + '/login.html';
          } else {
            app.currentIdentity = response;
            app.username = app.currentIdentity.email;
            app.ctrl.loadImage(app.username);
          }
        },
        error: function(jqXHR, textStatus, errorThrown) {
          console.log(textStatus+':'+errorThrown);
          // no authenticated session in the bpms server
          if (app.server=='') {
            app.showActivityIndicator('Enter BPMS server url to connect to.');
          } else {
            window.location.href = app.server + '/login?callback='+window.location.href;
          }
        }
    });*/
  };
  this.instance = function(id, name) {
    console.log('loading instance start form: '+ id+', '+name);
    return $.ajax({
        type: 'GET',
        url: app.server+'/process-definitions/'+id,
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('success fetching definition:'+response);
          app.activeInstance = response ;
          app.activeInstance.name=name;
          app.addFormControls(app.activeInstance);
          $('#instance').html($('#startFormTemplate').html()).moustache(app.activeInstance);
          $('#startInstanceModal .btn-primary').on('click', function() {
            app.newInstance(app.activeInstance.id,$('#instance').serializeArray());
          });
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.instanceAudit = function(id, name, businessKey) {
    console.log("loading audit trail for "+id+': '+name);
    if (app.ctrl.offline) {
      // TODO this is not implemented!
//      app.ctrl.loadInstance(id, JSON.parse(localStorage['GET_runtime_instance']));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/process-instances/'+escape(id),
        /*url: app.server+'/form/form-data?taskId='+id,*/
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('success fetching instance:'+JSON.stringify(response));
          app.ctrl.loadInstance(id, [response]);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.instanceBusinessKey = function(instanceId) {
    var instances = bpms.adapt(JSON.parse(localStorage['GET_runtime_instances']));
    var key;
    $.each(instances, function(i,d) {
      console.debug('checking '+d.id+' matches '+instanceId+': '+(d.id == instanceId));
      if (d.id == instanceId) {
        console.log('returning businessKey: '+d.businessKey);
        key = d.businessKey;
      }
    });
    return key ;
  };
  this.instanceList = function(searchExpr) {
    console.info('instance list, filtered by: '+JSON.stringify(searchExpr));
    if (app.ctrl.offline) {
      app.ctrl.loadInstanceList(JSON.parse(localStorage['GET_runtime_instances']));
    } else {
      return $.ajax({
          type: 'GET',
          url: app.server+'/process-instances/',
          contentType: 'application/json',
  //        data: { "tags":tags },
          dataType: 'text',
          success: function(response) {
            localStorage['GET_runtime_instances']=response;
            var json = JSON.parse(response);
            console.log('success fetching instance list:'+json.length);
            app.ctrl.loadInstanceList(json);
          },
          error: function(jqXHR, textStatus, errorThrown) {
            app.handleError(jqXHR, textStatus, errorThrown)
          }
      });
    }
  };
  this.loadForm = function(task) {
    app.showActivityIndicator('Loading...');
    // formKey is a logical name (URI) not URL but for now at least we will
    // assume can convert to URL by adding html
    if (!task.formKey.endsWith('.html')) task.formKey += '.html';
    // TODO remove?
    var dt = task.formKey.substring(task.formKey.lastIndexOf('.')+1)=='js'? 'script':'html';
    return $.ajax({
        type: 'GET',
        url: task.formKey,
        dataType: dt,
        success: function(response) {
          console.log('successfully fetched form: '+response);
          switch (dt) {
          case 'script':
            //
            console.log('dont think its a good idea to exec arbitrary code')
//            eval(response);
            break;
          default:
            $('#taskModal .form-inline').empty().append(response);
          }
          app.hideActivityIndicator();
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.logout = function() {
    window.location.href = app.server+'/resources/j_spring_security_logout';
  }
  this.nav = function() {
    console.log('nav');
    return [
             //{ entity:"sop" },
             { entity:"message"},
             { entity:"upload" },
             { entity:"definition" },
             { entity:"instance" },
             //{ entity:"dashboard" }
           ];
  };
  this.newInstance = function(processDefinitionId, varArr) {
    console.log('newInstance of '+processDefinitionId+', passing '+JSON.stringify(varArr));
    varArr.push({"name":"initiator","value":"tim"});
    // set default biz key
    var bizKey = 'myBusinessKey'+new Date().getTime();
    // override biz key if user specified one
    $.each(varArr, function(i,d) {
      if (d.name=='businessKey') bizKey = d.value;
    });
    var payload = '{ "processDefinitionId":"'+processDefinitionId+'","businessKey":"'+bizKey+'","variables":'+JSON.stringify(varArr)+' }';
    console.log('Payload:'+payload);
    app.showActivityIndicator('Starting instance...');
    return $.ajax({
        type: 'POST',
        url: app.server+'/process-instances/',
        contentType: 'application/json',
        data: payload,
        dataType: 'json',
        success: function(response) {
          console.log('successfully start instance:'+JSON.stringify(response));
          app.instance = response.data;
//          console.log('instances found: '+app.instance.length);
//          $('#instances').moustache(app);
          app.hideActivityIndicator('Instance started successfully');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.newMessage = function(mep, msgName, msg, bizDesc, tenantId) {
    console.log('starting '+msgName+'="'+msg+'" as mep: '+mep);
    app.showActivityIndicator('Starting instance...');
    var type = (mep == 'inOut' ? 'GET' : 'POST');
    // this strips non-significant white space
    msg = (msg.length==0 ? '' : JSON.stringify(JSON.parse(msg)));
    var d = (mep == 'inOut' ? {query:msg} : {json:msg});
    d.businessDescription = bizDesc;
    console.log('msg: '+ msg);
    return $.ajax({
      type: type,
      url: app.server+'/msg/'+tenantId+'/'+msgName,
      /*contentType: 'application/json', uncomment to send as single JSON blob instead of form params*/
      data: d,
      dataType: 'text',
      timeout: 30000,
      success: function(response, textStatus, request) {
        console.log('successfully start instance by msg: '+request.getResponseHeader('Location'));
        app.hideActivityIndicator('Instance started successfully');
      },
      error: function(jqXHR, textStatus, errorThrown) {
        app.handleError(jqXHR, textStatus, errorThrown)
      }
    });
  };
  this.profile = function() {
    console.log('loading profile for: '+ app.username);
    return $.ajax({
        type: 'GET',
        url: app.server+'/users/'+app.username,
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('success fetching profile:'+response);
          var tmp = new Array();
          app.activeProfile = response ;
          $.each(app.activeProfile.info, function(i,d) {
//            console.log(i+':'+d.key+'='+d.value);
            tmp[d.key.replace('.','_')]=d.value;
          });
          app.activeProfile.info = tmp;
          $('#linkedInForm').attr('action', app.server+'/linkedin');
          $('#linkedInForm input').val(app.activeProfile.email);
          app.activeProfile.info['linkedIn_connect']=(app.activeProfile.info['linkedIn_secret']===undefined);
          if (!app.activeProfile.info['linkedIn_connect']) $('#linkedInConnectedMsg').removeClass('hidden');
          $('#profile').html($('#profileTemplate').html()).moustache(app.activeProfile);
          /*$('#startInstanceModal .btn-primary').on('click', function(){
            app.newInstance(app.activeInstance.processDefinitionId,$('#instance').serializeArray());
          });*/
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.profileUpdate = function(varArr) {
    console.log('profileUpdate passing '+varArr);
    var profile = new ObjectHelper().arrToObj(varArr);
    profile.info = [];
    for (i in profile) {
      if (i!='username'&&i!='firstName'&&i!='lastName'&&i!='info'){
        console.log('set '+i+' to '+profile[i]);
        profile.info.push({key:i,value:profile[i]});
      }
    }
    var payload = JSON.stringify(profile);
    console.log('Payload:'+payload);
    app.showActivityIndicator('Updating profile...');
    return $.ajax({
        type: 'PUT',
        /* .json to work around spring mvc weirdnessthat removes extension */
        url: app.server+'/users/'+profile.username+'.json',
        contentType: 'application/json',
        data: payload,
        dataType: 'text',
        success: function(response) {
          console.log('successfully updated profile:'+JSON.stringify(response));
          app.activeProfile = response.data;
//          console.log('instances found: '+app.instance.length);
//          $('#instances').moustache(app);
          app.hideActivityIndicator('Profile updated successfully');
//          app.taskList();
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.sopList = function() {
    console.log('sops');
    app.sops = [
               { key:"GTD", name:"Collect Stuff", description:"Collect everything that is competing for your time to 'Get Things Done'" }
             ];
    $('#sops').moustache(app);
  };
  this.task = function(id, name, businessKey) {
    console.log('loading task: '+ id+', '+name);
    if (app.ctrl.offline) {
      app.ctrl.loadTask(id, name, businessKey, JSON.parse(localStorage['GET_runtime_tasks']));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/tasks/'+escape(id),
        /*url: app.server+'/form/form-data?taskId='+id,*/
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          console.log('success fetching task:'+JSON.stringify(response));
          app.ctrl.loadTask(id, name, businessKey, [response]);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.taskList = function(searchExpr) {
    console.log('task list, filtered by: '+JSON.stringify(searchExpr));
    if (app.ctrl.offline) {
      app.ctrl.loadTaskList(JSON.parse(localStorage['GET_runtime_tasks']));
    } else {
      return $.ajax({
        type: 'GET',
        url: app.server+'/tasks',
        contentType: 'application/json',
        data: searchExpr,
        dataType: 'text',
        success: function(response) {
          localStorage['GET_runtime_tasks']=response;
          app.ctrl.loadTaskList(JSON.parse(response));
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
      });
    }
  };
  this.updateTask = function(taskId, varArr) {
    console.log('updateTask of '+taskId+', passing '+varArr);
    var action = 'complete';
    var payload = '{ "action":"'+action+'","variables":'+JSON.stringify(varArr)+' }';
    console.log('Payload:'+payload);
    app.showActivityIndicator('Submitting task...');
    return $.ajax({
        type: 'POST',
        url: app.server+'/tasks/'+taskId,
        contentType: 'application/json',
        data: payload,
        dataType: 'text',
        success: function(response) {
          console.log('successfully updated task:'+JSON.stringify(response));
          app.activeTask = response.data;
//          console.log('instances found: '+app.instance.length);
//          $('#instances').moustache(app);
          app.hideActivityIndicator('Task submitted successfully');
          app.taskList();
        },
        error: function(jqXHR, textStatus, errorThrown) {
          app.handleError(jqXHR, textStatus, errorThrown)
        }
    });
  };
  this.uploadDefinition = function (varArr) {
    console.log('uploadDefinition, passing: '+ varArr);
    var payload = '{ "variables":'+JSON.stringify(varArr)+' }';
    console.log('Payload:'+payload);
    app.showActivityIndicator('Submitting new definition...');
    return $.ajax({
      type: 'POST',
      url: app.server+'/deployments',
      contentType: 'application/json',
      data: payload,
      dataType: 'json',
      success: function(response) {
        console.log('successfully uploaded definition:'+JSON.stringify(response));
        app.hideActivityIndicator('successfully uploaded definition');
      },
      error: function(jqXHR, textStatus, errorThrown) {
        app.handleError(jqXHR, textStatus, errorThrown)
      }
    });
  };
  this.uploadList = function () {
    console.log('Nothing required');
  };
  this.urlParam = function(name) {
    var val = (location.search.match(RegExp("[?|&]"+name+'=(.+?)(&|$)'))||[,null])[1];
    return val == null ? null : decodeURIComponent(val);
  };
  this.showMessage = function(msg) {
    if (msg === undefined) msg = 'Working...';
    $('.a-messages').empty().append(msg).addClass('blink').show();
  };
  this.showActivityIndicator = function(msg) {
    document.body.style.cursor='progress';
    this.showMessage(msg);
  };
  this.hideActivityIndicator = function(msg) {
    if (msg === undefined) msg = 'Success!';
    $('.a-messages').empty().append(msg).removeClass('blink');
    document.body.style.cursor='auto';
    setTimeout(function() {
      $('.a-messages').fadeOut();
    }, EASING_DURATION*10);
  };
  this.hideActivityIndicatorWithError = function(jqXHR) {
  console.log(jqXHR.statusText+':'+jqXHR.responseText);
  var resp = JSON.parse(jqXHR.responseText.replace(/\n/g,''));
    $('.a-messages').empty().append(resp.error).removeClass('blink');
    document.body.style.cursor='auto';
    setTimeout(function() {
      $('.a-messages').fadeOut();
    }, EASING_DURATION*10);
  };
};

/**
 * Glues data received from app to UI.
 */
function Controller() {
  this.offline = false;
  this.loadCaseList = function(caseObjs) {
    app.cases = bpms.adapt(caseObjs);
    console.log('cases found: '+app.cases.length);
    $('#cases').html($('#casesTemplate').html()).moustache(app);
  };
  this.loadEventList = function(eventObjs) {
    app.events = bpms.adapt(eventObjs);
    console.log('events found: '+app.events.length);
    $('#events').html($('#eventsTemplate').html()).moustache(app);
  };
  this.renderDefinitionsList = function(definitionObjs) {
    app.definitions = bpms.adapt(definitionObjs);
    $.each(app.definitions, function(i,d) {
      if (d.name==null) d.name=d.key;
    });
    $('#definitions').html($('#definitionsTemplate').html()).moustache(app);
    $('[data-instance]').click(function() {
      app.instance($(this).data('instance'), $(this).data('name'));
    });
    // set up editable handlers
    $('.a-definition-category[contenteditable]').on('input',function() {
      console.log('fired change handler for '+$(this).data('id'));
      if ($(this).text().trim().length > 0) {
        app.definitionCategory($(this).data('id'), $(this).text().trim());
      }
    });
  };
  this.loadImage = function(uname, size, selector) {
      console.log('loadImage for: '+uname);
      if (size === undefined) size=30;
      if (selector === undefined) selector='.kp-profile';
      $(selector).empty().append('<a>');
      $(selector+' > a').append('<img class="img-rounded" style="margin-top:5px;" title="Logged in as '+uname+'. Image from Gravatar.com" src="http://www.gravatar.com/avatar/'+hex_md5(uname)+'?s='+size+'&d=mm"/>')
  };
  this.loadInstance = function(id, instanceObj) {
    console.log('load instance id:'+id);
    // TODO unlike tasks we do not attempt to make this work offline 
    app.activeInstance = Array.isArray(instanceObj) ? instanceObj[0] : instanceObj;
    console.log('instance: '+JSON.stringify(app.activeInstance));
    $.each(app.activeInstance.auditTrail, function(i,d) {
      d.startedAt = function() { return new Date(this.startTime).toLocaleString() };
      d.startedAge = function() { return i18n.getAgeString(new Date(this.startTime)) };
      d.endedAt = function() { return this.endTime == null ? undefined : new Date(this.endTime).toLocaleString() };
      d.endedAge = function() { return this.endTime == null ? undefined : i18n.getAgeString(new Date(this.endTime)) };
    });
    $('#instanceModalTitle').html($('#instanceModalTitleTemplate').html()).moustache(app.activeInstance);
    $('#instanceModalBody').html($('#instanceModalBodyTemplate').html()).moustache(app.activeInstance);
  };
  this.loadInstanceList = function(instanceObjs) {
    app.instances = bpms.adapt(instanceObjs);
    console.log('instances found: '+app.instances.length);
    $('#instances').html($('#instancesTemplate').html()).moustache(app);
  };
  this.loadTask = function(id, name, businessKey, taskObjs) {
    console.log('load task id:'+id+', name'+name);
    app.activeTask = taskFromTasks(taskObjs, id);
    app.activeTask.name=name;
    $('#taskTitle').html($('#taskTitleTemplate').html()).moustache(app.activeTask);
    $('#taskModal .a-business-key').empty().append(businessKey);
    if (app.activeTask.formKey == null) {
        app.addFormControls(app.activeTask);
        $('#task').html($('#formDataTemplate').html()).moustache(app.activeTask);
    } else {
      app.loadForm(app.activeTask);
    }
    $('#taskModal .btn-primary').on('click', function(){
      app.updateTask(app.activeTask.taskId,$('#task').serializeArray());
    });
  };
  this.loadTaskList = function(taskObjs,selector) {
    if (selector == undefined) selector = '#tasks';
    console.log('render task list in '+selector);
    console.log('success fetching task list');
    app.tasks = bpms.adapt(taskObjs);
    $.each(app.tasks, function(i,d) {
      d.createString = i18n.getAgeString(new Date(d.createTime));
      d.dueString = i18n.getDeadlineString(new Date(d.dueDate));
    });
    $(selector).html($('#tasksTemplate').html()).moustache(app);
    $.each($('#tasks .a-business-key'), function(i,d) {
      var key = app.instanceBusinessKey($(d).data('instance'));
      console.log('found key: '+key);
      if (key!==undefined) {
        $(d).empty().append(key);
        $('#tasks .a-task-link').data('business-key',key);
      }
    });
    $('[data-task]').click(function() {
      app.task($(this).data('task'), $(this).data('name'), $(this).data('business-key'));
    });
  };
  this.sendMessage = function(mep, msgName, msg, bizDesc, formId, tenantId) {
    $('#'+formId+' .a-response').addClass('hidden');
    jqXHR = app.newMessage(mep, msgName, msg, bizDesc, tenantId);
    jqXHR.success(function(response, textStatus, request) {
      $('#'+formId+' .a-response').removeClass('hidden');
      var location = request.getResponseHeader('Location');
      if (location == null) location = request.getResponseHeader('Content-Location');
      $('#'+formId+' #responseLocation').empty()
          .append('<a href="'+location+'">View</a>');
    });
  };
}

function taskFromTasks(objs, id) {
  console.log('taskFromTasks, seeking id='+id+' from '+objs.length);
  var o;
  $.each(objs, function(i,d) {
    console.log('checking '+JSON.stringify(d));
    // activiti-spring-rest || activiti
    if (d !== undefined && (d.id == id || d.taskId == id)) o = d;
  });
  return o;
}

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
var bpms = new BpmsAdapter();
function BpmsAdapter() {
  this.adapt = function(from, to) {
    if (from.data === undefined) {
      // activiti-roo
      console.log('deteted a-roo, setting: '+to+' to '+from);
      to = from ;
      console.log(to);
    } else {
      // Activiti
      to = from.data ;
    }
    return to;
  };
}

function ObjectHelper() {
  this.arrToObj = function(arr) {
    var obj = new Object();
    for (idx in arr) {
      obj[arr[idx].name] = arr[idx].value;
    }
    return obj;
  };
}

/******** Poly fills ************/
/*!
 * From: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith
 * This method has been added to the ECMAScript 6 specification and may not be
 * available in all JavaScript implementations yet
 * http://mths.be/endswith v0.2.0 by @mathias
 */
if (!String.prototype.endsWith) {
  (function() {
    'use strict'; // needed to support `apply`/`call` with `undefined`/`null`
    var defineProperty = (function() {
      // IE 8 only supports `Object.defineProperty` on DOM elements
      try {
        var object = {};
        var $defineProperty = Object.defineProperty;
        var result = $defineProperty(object, object, object) && $defineProperty;
      } catch(error) {}
      return result;
    }());
    var toString = {}.toString;
    var endsWith = function(search) {
      if (this == null) {
        throw TypeError();
      }
      var string = String(this);
      if (search && toString.call(search) == '[object RegExp]') {
        throw TypeError();
      }
      var stringLength = string.length;
      var searchString = String(search);
      var searchLength = searchString.length;
      var pos = stringLength;
      if (arguments.length > 1) {
        var position = arguments[1];
        if (position !== undefined) {
          // `ToInteger`
          pos = position ? Number(position) : 0;
          if (pos != pos) { // better `isNaN`
            pos = 0;
          }
        }
      }
      var end = Math.min(Math.max(pos, 0), stringLength);
      var start = end - searchLength;
      if (start < 0) {
        return false;
      }
      var index = -1;
      while (++index < searchLength) {
        if (string.charCodeAt(start + index) != searchString.charCodeAt(index)) {
          return false;
        }
      }
      return true;
    };
    if (defineProperty) {
      defineProperty(String.prototype, 'endsWith', {
        'value': endsWith,
        'configurable': true,
        'writable': true
      });
    } else {
      String.prototype.endsWith = endsWith;
    }
  }());
}

$.ajaxSetup({
  username: localStorage['username'],
  password: localStorage['password'],
  headers: { 'X-CSRF-TOKEN': this.getCookie(CSRF_COOKIE) }
});

function getCookie(name) {
  console.log('getCookie: '+name)
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}
