var EASING_DURATION = 500;
fadeOutMessages = true;
var newLineRegEx = /\n/g;
var DEFAULT_INACTIVE_STAGES = 'cold,complete,on hold,unqualified,waiting list';

var ractive = new AuthenticatedRactive({
  el: 'container',
  lazy: true,
  template: '#template',
  data: {
    accounts: [],
    entityPath: '/metrics',
    csrfToken: getCookie(CSRF_COOKIE),
    metrics: [],
    //saveObserver:false,
    title: 'Metrics',
    username: localStorage['username'],
    age: function(timeString) {
      if (timeString==undefined) return;
      return i18n.getAgeString(ractive.parseDate(timeString))
    },
    alerts: function(selector) {
      console.log('alerts for '+selector);
      return $(selector+' :invalid').length;
    },
    customField: function(obj, name) {
      if (obj['customFields']==undefined) {
        return undefined;
      } else if (!Array.isArray(obj['customFields'])) {
        return obj.customFields[name];
      } else {
        var val;
        $.each(obj['customFields'], function(i,d) {
          if (d.name == name) val = d.value;
        });
        return val; 
      }
    },
    fields: ["id", "country", "owner", "fullName", "title", "customFields",
              "tenantId", "lastUpdated", "firstName", "lastName", "tags",
              "source", "email", "postCode", "account", "uuid", "phone1",
              "stage", "doNotCall", "doNotEmail", "firstMetric", "accountId",
              "phone2", "address1", "address2", "town", "countyOrCity",
              "postCode", "country",
              "enquiryType", "accountType", "medium", "campaign", "keyword",
              "emailConfirmed", "account.name", "account.businessWebsite",
              "account.companyNumber", "account.incorporationYear",
              "account.sic"],
    fieldValidators: {
      "phone1": "^\\+?[0-9, \\-()]{0,15}$",
      "phone2": "^\\+?[0-9, \\-()]{0,15}$"
    },
    formatAge: function(timeString) {
      console.log('formatAge: '+timeString);
      return timeString == "-1" ? 'n/a' : i18n.getDurationString(timeString)+' ago';
    },
    formatContent: function(content) {
      console.info('formatContent');
      content = content.replace(/\n/g,'<br/>');
      content = Autolinker.link(content);
      return content;
    },
    formatDate: function(timeString) {
      if (timeString==undefined) return 'n/a';
      var d = ractive.parseDate(timeString);
      return d.toLocaleDateString(navigator.languages);
    },
    formatFavorite: function(obj) {
      if (obj['favorite']) return 'glyphicon-star';
      else return 'glyphicon-star-empty';
    },
    formatJson: function(json) { 
      console.log('formatJson: '+json);
      try {
        var obj = JSON.parse(json);
        var html = '';
        $.each(Object.keys(obj), function(i,d) {
          html += (typeof obj[d] == 'object' ? '' : '<b>'+d+'</b>: '+obj[d]+'<br/>');
        });
        return html;
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    formatTags: function(tags) {
      var html = '';
      if (tags==undefined) return html;
      var tagArr = tags.split(',');
      $.each(tagArr, function(i,d) {
        html += '<span class="img-rounded" style="background-color:'+d+'">&nbsp;&nbsp;</span>';
      });
      return html;
    },
    gravatar: function(email) {
      if (email == undefined) return '';
      return '<img class="img-rounded" style="width:36px" src="//www.gravatar.com/avatar/'+ractive.hash(email)+'?s=36&d=https%3A%2F%2Fapi.omny.link%2F'+ractive.get('tenant.id')+'%2Fgravatars%2F'+ractive.hash(email)+'.png"/>'
    },
    hash: function(email) {
      if (email == undefined) return '';
      return ractive.hash(email);
    },
    haveStageReasons: function() {
      console.info('haveStageReasons?');
      if (ractive.get('current.stage')!='Cold') return false;
      return ractive.get('tenant.typeaheadControls').filter(function(d) {
        return d.name=='stageReasons';
      }).length > 0;
    },
    helpUrl: '//omny.link/user-help/metrics/#the_title',
    inactiveStages: function() {
      return ractive.get('tenant.serviceLevel.inactiveStages')==undefined
          ? DEFAULT_INACTIVE_STAGES
          : ractive.get('tenant.serviceLevel.inactiveStages').join();
    },
    lessThan24hAgo: function(isoDateTime) {
      if (isoDateTime == undefined || (new Date().getTime()-new Date(isoDateTime).getTime()) < 1000*60*60*24) {
        return true;
      }
      return false;
    },
    matchFilter: function(obj) {
      var filter = ractive.get('filter');
      //console.info('matchFilter: '+JSON.stringify(filter));
      if (filter==undefined) {
        return true;
      } else {
        try {
          if (filter.operator=='in') {
            var values = filter.value.toLowerCase().split(',');
            return values.indexOf(obj[filter.field].toLowerCase())!=-1;
          } else if (filter.operator=='!in') {
            var values = filter.value.toLowerCase().split(',');
            return values.indexOf(obj[filter.field].toLowerCase())==-1;
          } else {
            if (filter.operator==undefined) filter.operator='==';
            return eval("'"+filter.value.toLowerCase()+"'"+filter.operator+"'"+(obj[filter.field]==undefined ? '' : obj[filter.field]).toLowerCase()+"'");
          }
        } catch (e) {
          //console.debug('Exception during filter, probably means record does not have a value for the filtered field');
          return true;
        }
      }
    },
    matchRole: function(role) {
      console.info('matchRole: '+role)
      if (role==undefined || ractive.hasRole(role)) {
        $('.'+role).show();
        return true;
      } else {
        return false;
      }
    },
    matchSearch: function(obj) {
      var searchTerm = ractive.get('searchTerm');
      //console.info('matchSearch: '+searchTerm);
      if (searchTerm==undefined || searchTerm.length==0) {
        return true;
      } else {
        return ( (obj.selfRef.indexOf(searchTerm)>=0)
          || (obj.firstName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.lastName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.email.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (obj.phone1!=undefined && obj.phone1.indexOf(searchTerm)>=0)
          || (obj.phone2!=undefined && obj.phone2.indexOf(searchTerm)>=0)
          || (obj.accountName!=undefined && obj.accountName.toLowerCase().indexOf(searchTerm.toLowerCase())>=0)
          || (searchTerm.startsWith('updated>') && new Date(obj.lastUpdated)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created>') && new Date(obj.firstMetric)>new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('updated<') && new Date(obj.lastUpdated)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('created<') && new Date(obj.firstMetric)<new Date(ractive.get('searchTerm').substring(8)))
          || (searchTerm.startsWith('#') && obj.tags.indexOf(ractive.get('searchTerm').substring(1))!=-1)
        );
      }
    },
    selectMultiple: [],
    sort: function (array, column, asc) {
      console.info('sort '+(asc ? 'ascending' : 'descending')+' on: '+column);
      array = array.slice(); // clone, so we don't modify the underlying data

      return array.sort( function ( a, b ) {
        if (b[column]==undefined || b[column]==null || b[column]=='') {
          return (a[column]==undefined || a[column]==null || a[column]=='') ? 0 : -1;
        } else if (asc) {
          return a[ column ] < b[ column ] ? -1 : 1;
        } else {
          return a[ column ] > b[ column ] ? -1 : 1;
        }
      });
    },
    sortAsc: false,
    sortColumn: 'lastUpdated',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "poweredBy", "url": "/partials/powered-by.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"},
      { "name": "metricListSect", "url": "/partials/metric-list-sect.html"},
      { "name": "metricListTable", "url": "/partials/metric-list-table.html"},
      { "name": "navbar", "url": "/partials/metric-navbar.html"},
      { "name": "currentMetricSect", "url": "/partials/metric-current-sect.html"}
    ],
  },
  add: function () {
    console.log('add...');
    $('h2.edit-form,h2.edit-field').hide();
    $('.create-form,create-field').show();
    var metric = { account: {}, author:ractive.get('username'), tenantId: ractive.get('tenant.id'), url: undefined };
    ractive.select( metric );
    ractive.initTags();
  },
  edit: function (metric) {
    console.log('edit'+metric+'...');
    $('h2.edit-form,h2.edit-field').show();
    $('.create-form,create-field').hide();
    ractive.set('currentIdx',ractive.get('metrics').indexOf(metric));
    ractive.select( metric );
  },
  delete: function (obj) {
    console.log('delete '+obj+'...');
    $.ajax({
        url: ractive.getServer()+'/metrics/'+ractive.id(obj),
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  fetch: function () {
    console.info('fetch...');
    ractive.set('saveObserver', false);
    $.ajax({
      dataType: "json",
      url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/metrics/?projection=complete',
      crossDomain: true,
      success: function( data ) {
        if (data['_embedded'] == undefined) {
          ractive.merge('metrics', data);
        } else {
          ractive.merge('metrics', data['_embedded'].metrics);
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        if (ractive.hasRole('power-user')) $('.power-user').show();
        if (ractive.fetchCallbacks!=null) ractive.fetchCallbacks.fire();
        ractive.set('searchMatched',$('#metricsTable tbody tr:visible').length);
        ractive.set('saveObserver', true);
      }
    });
  },
  filter: function(filter) {
    console.log('filter: '+JSON.stringify(filter));
    ractive.set('filter',filter);
    $('.omny-dropdown.dropdown-menu li').removeClass('selected')
    $('.omny-dropdown.dropdown-menu li:nth-child('+filter.idx+')').addClass('selected')
    ractive.set('searchMatched',$('#metricsTable tbody tr:visible').length);
    $('input[type="search"]').blur();
  },
  find: function(metricId) { 
    console.log('find: '+metricId);
    var c; 
    $.each(ractive.get('metrics'), function(i,d) { 
      if (metricId.endsWith(ractive.getId(d))) { 
        c = d;
      }
    });
    return c;
  },
  hideResults: function() {
    $('#metricsTableToggle').removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-right');
    $('#metricsTable').slideUp();
    $('#currentSect').slideDown({ queue: true });
  },
  oninit: function() {
    console.log('oninit');
    this.ajaxSetup();
    this.loadStandardPartials(this.get('stdPartials'));
  },
  save: function () {
    console.log('save metric: '+ractive.get('current').lastName+'...');
    ractive.set('saveObserver',false);
    var id = ractive.uri(ractive.get('current'));
    if (document.getElementById('currentForm')==undefined) { 
      console.debug('still loading, safe to ignore');
    } else if (document.getElementById('currentForm').checkValidity()) {
      // cannot save metric and account in one (grrhh), this will clone...
      var tmp = JSON.parse(JSON.stringify(ractive.get('current')));
      //console.log('account: '+JSON.stringify(tmp.account));
      delete tmp.fullName;
      delete tmp.notes;
      delete tmp.documents;
      if (id != undefined && tmp.account != undefined && Object.keys(tmp.account).length > 0 && tmp.account.id != undefined) {
        tmp.account = id.substring(0,id.indexOf('/',8))+'/accounts/'+tmp.account.id;  
      } else {
        delete tmp.account;
        delete tmp.accountId;
      }       
      tmp.tenantId = ractive.get('tenant.id');
//      console.log('ready to save metric'+JSON.stringify(tmp)+' ...');
      $.ajax({
        url: id === undefined ? ractive.getServer()+'/'+tmp.tenantId+'/metrics/' : ractive.tenantUri(tmp),
        type: id === undefined ? 'POST' : 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(tmp),
        success: completeHandler = function(data, textStatus, jqXHR) {
          //console.log('data: '+ data);
          var location = jqXHR.getResponseHeader('Location');
          ractive.set('saveObserver',false);
          if (location != undefined) ractive.set('current._links.self.href',location);
          switch (jqXHR.status) {
          case 201: 
            ractive.set('current.fullName',ractive.get('current.firstName')+' '+ractive.get('current.lastName'));
            var currentIdx = ractive.get('metrics').push(ractive.get('current'))-1;
            ractive.set('currentIdx',currentIdx);
            if (ractive.uri(ractive.get('current.account'))==undefined) ractive.saveAccount();
            break;
          case 204: 
            ractive.splice('metrics',ractive.get('currentIdx'),1,ractive.get('current'));
            break;
          }
          ractive.showMessage('Metric saved');
          ractive.addServiceLevelAlerts();
          ractive.set('saveObserver',true);
        }
      });
    } else {
      console.warn('Cannot save yet as metric is invalid');
      $('#currentForm :invalid').addClass('field-error');
      ractive.showMessage('Cannot save yet as metric is incomplete');
      ractive.set('saveObserver',true);
    }
  },
  select: function(metric) {
    console.log('select: '+JSON.stringify(metric));
    ractive.set('saveObserver',false);
    if (metric.account == undefined || metric.account == '') metric.account = new Object();
    // default owner to current user
    if (metric.owner == undefined || metric.owner == '') metric.owner = ractive.get('username');
    // adapt between Spring Hateos and Spring Data Rest
    if (metric._links == undefined && metric.links != undefined) {
      metric._links = metric.links;
      $.each(metric.links, function(i,d) {
        if (d.rel == 'self') metric._links.self = { href:d.href };
      });
    }
    if (metric._links != undefined) {
      var url = ractive.uri(metric); // includes getServer
      if (url == undefined) {
        ractive.showError('No metric selected, please check link');
        return;
      }
      console.log('loading detail for '+url);
      $.getJSON(url+'?projection=complete', function( data ) {
        console.log('found metric '+data);
        ractive.set('current', data);
        ractive.initControls();
        ractive.initTags();
        // who knows why this is needed, but it is, at least for first time rendering
        $('.autoNumeric').autoNumeric('update',{});
        ractive.sortChildren('notes','created',false);
        ractive.addServiceLevelAlerts();
        ractive.sortChildren('documents','created',false);
        if (ractive.get('current.account.companyNumber')!=undefined) ractive.fetchCompaniesHouseInfo();
        ractive.sortChildren('activities','occurred',false);
        if (ractive.get('current.account')==undefined || ractive.get('current.account.name')==undefined) {
          //ractive.initAccountTypeahead();
        } else if (ractive.get('current.account.businessWebsite')==undefined || ractive.get('current.account.businessWebsite')=='') {
          ractive.inferDomainName();
        }
        if (ractive.hasRole('admin')) $('.admin').show();
        ractive.set('saveObserver',true);
      });
    } else { 
      console.log('Skipping load as no _links.'+metric.lastName);
      ractive.set('current', metric);
      ractive.set('saveObserver',true);
    }
    ractive.hideResults();
  },
  showActivityIndicator: function(msg, addClass) {
    document.body.style.cursor='progress';
    this.showMessage(msg, addClass);
  },
  showAlertCounters: function() {
    console.info('showAlertCounters');
    var alerts = {
      account:$('#currentAccount :invalid').length,
      activities:$('#activitySect :invalid').length,
      activityAnalysis:$('#activityAnalysisSect :invalid').length,
      budget:$('#budgetSect :invalid').length,
      connections:$('#connectionsSect :invalid').length,
      documents:$('#documentsTable .alert-danger').length,
      notes:$('#notesTable .alert-danger, #notesSect .messages.alert-danger').length,
    }
    ractive.set('alerts',alerts);
  },
  showResults: function() {
    $('#metricsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#currentSect').slideUp();
    $('#metricsTable').slideDown({ queue: true });
  },
  toggleResults: function() {
    console.info('toggleResults');
    $('#metricsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#metricsTable').slideToggle();
  }
});

ractive.observe('profile', function(newValue, oldValue, keypath) {
  console.log('profile changed');
  ractive.filter( {field: 'owner', idx: 3, value: ractive.get('profile.id')} );
});

ractive.observe('searchTerm', function(newValue, oldValue, keypath) {
  console.log('searchTerm changed');
  ractive.showResults();
  setTimeout(function() {
    ractive.set('searchMatched',$('#metricsTable tbody tr').length);
  }, 500);
});


// Save on model change
// done this way rather than with on-* attributes because autocomplete 
// controls done that way save the oldValue 
ractive.observe('current.*', function(newValue, oldValue, keypath) {
  if (ractive.get('current')!=undefined) ractive.showAlertCounters();
  ignored=[];
  if (ractive.get('saveObserver') && ignored.indexOf(keypath)==-1) {
    console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    ractive.save();
  } else { 
    console.warn  ('Skipped metric save of '+keypath);
    //console.log('current prop change: '+newValue +','+oldValue+' '+keypath);
    //console.log('  saveObserver: '+ractive.get('saveObserver'));
  }
});

ractive.on( 'filter', function ( event, filter ) {
  console.info('filter on '+JSON.stringify(event)+','+filter.idx);
  ractive.filter(filter);
});
