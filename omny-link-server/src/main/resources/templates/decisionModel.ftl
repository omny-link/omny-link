<!DOCTYPE html>

<html lang="en">
<head>
  <link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
  <link href="/workmgmt/2.2.0/css/base.css" rel="stylesheet">
  <link href="/onedecision/1.2.0/css/onedecision.css" rel="stylesheet">
  <link href="/onedecision/1.2.0/css/decisions.css" rel="stylesheet">
  <link rel="icon" type="image/png" href="/images/icon/omny-icon-16x16.png" />
</head>
<body>
  <div class="clearfix" id="messages"></div>
  <section class="container" id="container"></section>

  <script id='template' type='text/ractive'>
    {{>profileArea}}
    {{>titleArea}}
    {{>loginSect}}
    {{>poweredBy}}
    {{>sidebar}}
  </script>

  <section class="container">
    <h2>Decision Model ${dmnModel.name} (id: ${dmnModel.shortId})</h2>
    <p></p>
    <section>${decisionHtml}</section>
  </section>

  <script src="/js/md5.min.js"></script>
  <script src="/workmgmt/2.2.0/js/string-functions.js"></script>
  <script src="/js/env.js"></script>

  <script src="/onedecision/1.2.0/js/i18n.js"></script>
  <script src="/workmgmt/2.2.0/js/base.js"></script>
  <script src="/onedecision/1.2.0/js/decisions-table.js"></script>
  <script src="/webjars/auth/1.0.0/js/auth.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/8.7/highlight.min.js"></script>
</body>
</html>
