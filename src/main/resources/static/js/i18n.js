var i18n = new I18nController();

function I18nController() {
	this.localize = function(locale) {
		if (locale===undefined) locale = 'en_GB';
		console.log('localising to: '+locale+'...');
		var jqxhr = $.ajax({
		      type: 'GET',
		      url: '/js/i18n_'+locale+'.json',
		      contentType: 'application/json',
		      dataType: 'json',
		      success: function(s) {
			      console.log('strings returned: '+ s.length);
			      i18n.strings = s;
			      switch (jqxhr.status) {
			      case 200: 
			    	$('[data-i18n]').each(function(i,d){
			  			var code = $(d).data('i18n');
			  			console.log('... '+code+' = '+s[code]);
			  			if (s[code]!==undefined) $(d).empty().append(s[code]);
			  		});
			        break; 
			      default: 
			        sect.append('<p>...Failure: '+jqxhr.status);
			      }
			  },
		      error: function(jqXHR, textStatus, errorThrown) { 
		        console.log('error:'+textStatus);
		      }
		    });
	};
	this.getAgeString = function(millis) {
    return this.getDurationString(new Date()-millis)+' ago';
  };
  this.getDeadlineString = function(millis) {
    var millisToDeadline = millis-new Date(); 
    if (millisToDeadline <= 0) {
      return 'n/a';
    } else {
      return 'In '+this.getDurationString(millisToDeadline);
    }
  };
  this.getDurationString = function(millis) {
      mins = millis / (60 * 1000);
      hours = mins / 60;
      days = hours / 24;
      weeks = days / 7;
      years = days / 365;
      if (mins < 1) {
            return 'less than a minute';
      } else if (mins < 2) {
          return 'about a minute';
      } else if (mins < 60) {
          return 'about '+Math.floor(mins) + ' minutes';
      } else if (hours < 2) {
          return 'about an hour';
      } else if (days < 1) {
          return 'about '+Math.floor(hours) + ' hours';
      } else if (days < 2) {
          return 'about a day';
      } else if (years > 1) {
          return 'about ' + Math.floor(years) + ' years';
      } else if (weeks > 2) {
        return 'about ' + Math.floor(weeks) + ' weeks';
//      } else if (weeks > 1) {
//          return 'about ' + Math.floor(weeks) + ' weeks';
      } else {
          return 'about ' + Math.floor(days) + ' days';
      }
  };
}