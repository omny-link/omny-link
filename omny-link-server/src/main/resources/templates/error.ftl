<!DOCTYPE html>
<html lang="en">
<head>
  <link href="css/bootstrap.min.css" rel="stylesheet">
  <link href="css/omny-1.0.0.css" rel="stylesheet">
  <link rel="icon" type="image/png" href="images/icon/omny-icon-16x16.png" />
  <style>
    html,body,section#main {
      height:100%;
      text-align: center;
      width: 100%;
    }
    section#main {
      margin: auto;
      padding-top: 10%;
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
      left: auto;
      right: auto;
      top: 20px;
    }
    #container a {
      float: right;
    }
    .btn { 
      height: 40px;
    }
  </style>
<head>
<body>

  <section id="container">
    <h2>Oops! Something went wrong!</h2>
    <p>The server response was: ${error}</p>
  </section>

  <section id="main">
    <h1>Omny Link Decisions Management</h1>
    <img src="images/omny-logo.png"/>
    <p>For support and hosted solutions see <a href="//omny.link">http://omny.link</a></p>
  </section>

  <script src="/webjars/jquery/1.11.1/jquery.min.js"></script>
  <script src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>

  <script src="/js/activity-1.0.0.js"></script>
</body>
</html>
