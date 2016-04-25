function RestEntityHelper() {
  this.id = function(entity) {
    //console.log('id: '+JSON.stringify(entity));
    var id = this.uri(entity);
    return id.substring(id.lastIndexOf('/')+1);
  };
  this.stripProjection = function(link) {
    if (link==undefined) return;
    var idx = link.indexOf('{projection');
    if (idx==-1) {
      idx = link.indexOf('{?projection');
      if (idx==-1) {
        return link;
      } else {
        return link.substring(0,idx);
      }
    } else {
      return link.substring(0,idx);
    }
  };
  this.uri = function(entity) {
    //console.log('uri: '+JSON.stringify(entity));
    var uri;
    if (entity['links']!=undefined) {
      $.each(entity.links, function(i,d) {
        if (d.rel == 'self') {
          uri = d.href;
        }
      });
    } else if (entity['_links']!=undefined) {
      uri = this.stripProjection(entity._links.self.href);
    }
    return uri;
  };
}