var $env = (function ($) {
  var me = {
    appName: '${appName}',
    server: '${restBaseUri}',
    tagLine: '${tagLine}',
    tenantConfig: '${tenantConfig}'
  };

  return me;
}($));
