String.prototype.toLabel = function() {
  return this.replace(/([A-Z])/, function(v) { return '_'+v; }).replace(/_/g, ' ').toLeadingCaps();
};

String.prototype.toLeadingCaps = function() {
  if (this == undefined) return this
  return this.replace(/(&)?([a-z])([a-z]{2,})(;)?/ig, function(all, prefix, letter, word, suffix) {
    if (prefix && suffix) {
      return all;
    }
    return letter.toUpperCase() + word.toLowerCase();
  });
}

String.prototype.formatNumber = function(thousandsSeparator) {
  // reverse string and insert separator every 3 digits
  var chars = this.split('');
  return chars.reverse().reduce(function(previousValue, currentValue, idx, arr) {
    if (idx % 3 == 0 && idx != 0) { 
      return previousValue + thousandsSeparator + currentValue;
    } else { 
      return previousValue + currentValue;
    }
  }, '') // need to init string
  .split('').reverse().join(''); // don't forget to restore number to correct order!
}
