<!DOCTYPE html>

<html lang="en">
<head>
  <link href='//fonts.googleapis.com/css?family=Roboto:400italic,400,700' rel='stylesheet' type='text/css'>
  <link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
  <link href="/webjars/bootstrap/3.3.5/css/bootstrap-theme.min.css" rel="stylesheet">
  <link href="css/omny-0.9.0.css" rel="stylesheet">
  <link rel="icon" type="image/png" href="images/icon/omny-icon-16x16.png" />
    <style>
    html,body,section#main {
      height:100%;
      text-align: center;
      width: 100%;
    }
    section#main {
      margin: auto;
    }
    section#login {
      padding-top: 0;
      padding-right: 20px;
      position: absolute;
      top: 20px;
      right: 20px;
      width: 30%;
    }
    #container {
      position: fixed;
      top: 20px;
      right: 20px;
    }
    #container a {
      float: right;
    }
    .btn { 
      height: 40px;
    }
    
    @media (max-width: 480px) {
      section#main {
        padding-top: 40%;
      }
    }
    @media (min-width: 481px) {
      section#main {
        padding-top: 10%;
      }
    }
  </style>
<head>
<body>

  <section id="container"></section>

  <section id="main">
    <h1>CRM | Workflow | Decisions</h1>
    <img src="images/omny-logo.png"/>
    <p>For more information see <a href="//omny.link">http://omny.link</a></p>
  </section>


  <script id='template' type='text/ractive'>
      <!--h2>Login with Username and Password</h2-->
      <form name="loginForm" action="/login" method="POST">
        <fieldset>
          <input type="text" id="username" name="username" placeholder="Username" required/>
          <input type="password" id="password" name="password" placeholder="Password" required/>
          <input type="hidden" id="_csrf" name="_csrf" value="{{csrfToken}}" />
          <input type="hidden" id="redirect" name="redirect" value="index.html" />
          <input type="button" id="login" onclick="ractive.login()" value="Login" class="btn btn-primary" />
        </fieldset>
        <a href="http://omny.link/contact-us/">Or click here to sign-up for a trial</a>
      </form>
  </script>

  <script src="/webjars/jquery/1.11.1/jquery.min.js"></script>
  <script src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  <script src="/webjars/Bootstrap-3-Typeahead/3.1.1/bootstrap3-typeahead.js"></script>
  <script src="/webjars/ractive/0.7.1/ractive.min.js"></script>

  <script src="js/login-0.9.0.js"></script>
  <script src="js/login-ui-0.9.0.js"></script>
</body>

</html>
