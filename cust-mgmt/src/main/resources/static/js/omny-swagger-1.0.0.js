var os = new OmnySwagger();

function OmnySwagger() { 
  this.onSwaggerComplete = function() { 
    console.log('onSwaggerComplete');
    
    $('.info_title').empty().append('API Documentation');
    $('.info_description').remove();
    $('.info_name').empty().append('Created by Tim Stephenson (tim at omny.link)');
    $('.info_license').empty().append('<a href="http://www.gnu.org/licenses/lgpl-3.0.en.html">LGPL license v3.0</a>');
    $('#header').css('background-color','white');
    $('#logo').remove();
    $('#header .swagger-ui-wrap .navbar-brand').remove();
    $('#header .swagger-ui-wrap').prepend('<a class="nav navbar-brand" href="//api.omny.link"><img src="/images/omny-logo.png" style="height: 70px" alt="logo"></a>');
  }
}
