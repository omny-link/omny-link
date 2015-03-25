<!DOCTYPE html>

<html lang="en">
<head>
  <link href='http://fonts.googleapis.com/css?family=Roboto:400italic,400,700' rel='stylesheet' type='text/css'>
  <link href="css/bootstrap.min.css" rel="stylesheet">
  <link href="css/omny-0.1.0.css" rel="stylesheet">
  <link rel="icon" type="image/png" href="images/icon/omny-icon-16x16.png" />
<head>
<body>

  <div id="container"></div>

  <script id='template' type='text/ractive'>
    <div class="content">
      <h2>Login with Username and Password (FTL)</h2>
      <form name="loginForm" action="/login" method="POST">
        <fieldset>
          <input type="text" id="username" name="username" placeholder="Username" required/>
          <input type="password" id="password" name="password" placeholder="Password" required/>
          <input type="hidden" id="_csrf" name="_csrf" value="{{csrfToken}}" />
        </fieldset>
        <input type="button" id="login" onclick="ractive.login()" value="Login" class="btn btn-primary" />
      </form>
    </div>
  </script>

  <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
  <script src="js/jquery-1.11.0.min.js"></script>
  <!-- Include all compiled plugins (below), or include individual files as needed -->
  <script src="js/bootstrap.min.js"></script>
  <script src="js/bootstrap3-typeahead.js"></script>
  <script src="js/ractive.min.js"></script>
  <script src="js/custmgmt-0.1.0.js"></script>
  <script>
    $(document).ready(function() {
      ractive.set('tenant', { 
        name: 'omny', 
        typeaheadControls: [
          { selector: '#curEnquiryType', url: '/data/omny/enquiry-types.json'},
          { selector: '#curMedium', url: '/data/media.json'},
          { selector: '#curSource', url: '/data/sources.json'},
          { selector: '#curStage', url: '/data/stages.json'}
        ]
      });
      $.ajaxSetup({
        headers: {'X-Tenant': ractive.data.tenant.name}
      });
    });
  </script>
</body>

</html>