<!DOCTYPE html>
<html lang="en">
<head>
  <link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
  <link href="/webjars/bootstrap/3.3.5/css/bootstrap-theme.min.css" rel="stylesheet">
  <link href="/css/omny-1.0.0.css" rel="stylesheet">
  <link rel="icon" type="image/png" href="/images/icon/omny-icon-16x16.png" />
  <style>
    html,body,section#main {
      height:100%;
      text-align: center;
      width: 100%;
    }
    section#main {
      margin: auto;
    }
    #container {
      position: fixed;
      top: 5px;
      right: 20px;
    }
    #container div {
      position: relative;
      margin-top: 5px;
      }
      #container form {
      text-align: right;
    }
    #container #messages {
      margin: 0 auto;
      width: 100%;
      /*width: 531px;*/
      top: -5px;
    }
    .btn { 
      height: 45px;
    }
    
    @media (max-width: 480px) {
      section#main {
        padding-top: 45%;
      }
    }
    @media (min-width: 481px) {
      section#main {
        padding-top: 30%;
      }
    }
    @media (min-width: 769px) {
      section#main {
        padding-top: 20%;
      }
    }
  </style>
<head>
<body>

  <section id="container" style="width:100%">
    <h2>Oops! Something went wrong!</h2>
    <p>The server response was: ${error}</p>
  </section>

  <section id="main">
    <h1>CRM | Workflow | Decisions</h1>
    <img src="/images/omny-logo.png"/>
    <p>For support and hosted solutions see <a href="//omny.link">http://omny.link</a></p>
  </section>

  <script src="/webjars/jquery/1.11.1/jquery.min.js"></script>
  <script src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>

  <script src="/js/activity-1.0.0.js"></script>
</body>
</html>
