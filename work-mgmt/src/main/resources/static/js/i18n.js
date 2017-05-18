var i18n = new I18nController();

function I18nController() {
  /**
   * @deprecated Use getStrings. 
   */
  this.localize = function(locale) {
    this.getStrings(locale);
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
      if (isNaN(millis)) return 'n/a';
      secs = millis / 1000;
      mins = secs / 60;
      hours = mins / 60;
      days = hours / 24;
      weeks = days / 7;
      years = days / 365;
      if (secs < 1) {
          return 'less than a second';
      } else if (secs < 2) {
          return 'about a second';
      } else if (mins < 1) {
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
  this.getStrings = function(locale) {
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
          i18n.l10n();
          break; 
        default: 
          console.error('  failed to load i18n strings: '+jqxhr.status);
        }
      }
    });
  };
  this.l10n = function() {
    $('[data-i18n]').each(function(i,d){
      var code = $(d).data('i18n');
      console.log('... '+code+' = '+i18n.strings[code]);
      if (i18n.strings[code]!==undefined) $(d).empty().append(i18n.strings[code]);
    });
  };
  this.getStrings();
}