var ractive = new BaseRactive({
  el: 'container',
  template: '#template',
  partials: {

  },
  data: {
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    jobs: [],
    diagPanZoomHandlers: [],
    showDetails: false,
    duration: function(timeString) {
      return i18n.getDurationString(new Date(timeString))
    },
    featureEnabled: function(feature) {
      console.log('featureEnabled: '+feature);
      if (feature==undefined || feature.length==0) return true;
      else return ractive.get('tenant.show.'+feature);
    },
    formatDateTime: function(timeString) {
//    console.log('formatDate: '+timeString);
      if (timeString==undefined) return 'n/a';
      return new Date(timeString).toLocaleString(navigator.languages);
    },
    formatJson: function(json) {
      console.log('formatJson: '+json);
      try {
        var obj = JSON.parse(json);
        var html = '<ul class="json">';
        $.each(Object.keys(obj), function(i,d) {
          if (typeof obj[d] == 'object' && obj[d]['string'] != undefined) {
            html += '<li><label>'+d.toLabel()+':</label><span>'+obj[d]['string']+'</span>';
          } else if (typeof obj[d] == 'object') {
            // currently ignore keys without value, may change that?
          } else {
            html += '<label>'+d+':</label><span>'+obj[d]+'</span>';
          }
        });
        return html;
      } catch (e) {
        // So it wasn't JSON
        return json;
      }
    },
    hasRole: function(role) {
      return ractive.hasRole(role);
    },
    helpUrl: '//omny.link/user-help/jobs/#the_title',
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
      //console.info('matchSearch: '+searchTerm);
      if (ractive.get('searchTerm')==undefined || ractive.get('searchTerm').length==0) {
        return true;
      } else {
        var search = ractive.get('searchTerm').split(' ');
        for (var idx = 0 ; idx < search.length ; idx++) {
          var searchTerm = search[idx].toLowerCase();
          var match = ( (obj.id.indexOf(searchTerm)!=-1)
            || (obj.processDefinitionId.toLowerCase().indexOf(searchTerm)!=-1)
            || (obj.processInstanceId.indexOf(searchTerm)!=-1)
            || (searchTerm.startsWith('#') && obj.retries >= parseInt(searchTerm.substring(1)))
            || (searchTerm=='errors' && (obj.exceptionMessage!=undefined && obj.exceptionMessage!=''))
            || (searchTerm=='normal' && obj.exceptionMessage==undefined)
          );
          //no match is definitive but matches may fail other terms (AND logic)
          if (!match) return false;
        }
        return true;
      }
    },
    searchTerm: 'errors',
    server: $env.server,
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
    sortColumn: 'startTime',
    sorted: function(column) {
      console.info('sorted');
      if (ractive.get('sortColumn') == column && ractive.get('sortAsc')) return 'sort-asc';
      else if (ractive.get('sortColumn') == column && !ractive.get('sortAsc')) return 'sort-desc'
      else return 'hidden';
    },
    stdPartials: [
      { "name": "jobListSect", "url": "/partials/job-list-sect.html"},
      { "name": "helpModal", "url": "/partials/help-modal.html"},
      { "name": "loginSect", "url": "/webjars/auth/1.0.0/partials/login-sect.html"},
      { "name": "navbar", "url": "/partials/job-navbar.html"},
      { "name": "profileArea", "url": "/partials/profile-area.html"},
      { "name": "sidebar", "url": "/partials/sidebar.html"},
      { "name": "titleArea", "url": "/partials/title-area.html"}
    ],
    title: 'Omny Bot Tasks'
  },
  partials: {
    profileArea: '',
    titleArea: '',
    loginSect: '',
    jobListSect: '',
    sidebar: '',
    helpModal: ''
  },
  delete: function(jobId) {
    console.log('delete '+jobId+'...');
    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/jobs/'+jobId,
        type: 'DELETE',
        success: completeHandler = function(data) {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  fetch: function () {
    console.log('fetch...');
    $.getJSON(ractive.getServer()+'/'+ractive.get('tenant.id')+'/jobs', function( data ) {
      ractive.set('jobs', data);
      ractive.set('searchMatched',$('#jobsTable tbody tr:visible').length);
    });
  },
  retry: function(jobId) {
    console.log('retry '+jobId+'...');
    $.ajax({
        url: ractive.getServer()+'/'+ractive.get('tenant.id')+'/jobs/'+jobId,
        dataType: 'text', /* in fact no body expected, avoid attempt to parse */
        type: 'POST',
        success: completeHandler = function() {
          ractive.fetch();
          ractive.showResults();
        }
    });
    return false; // cancel bubbling to prevent edit as well as delete
  },
  showResults: function() {
    console.log('showResults');
    $('#currentSect').slideUp();
    $('#jobsTableToggle').addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
    $('#jobsTable').slideDown();
  },
  toggleResults: function() {
    console.log('toggleResults');
    $('#jobsTableToggle').toggleClass('glyphicon-triangle-bottom').toggleClass('glyphicon-triangle-right');
    $('#jobsTable').slideToggle();
  }
});

