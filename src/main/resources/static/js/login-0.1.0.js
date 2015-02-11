function login(form) { 
  console.log('login....');
  localStorage['username'] = $('#'+form.id+' #username').val();
  localStorage['password'] = $('#'+form.id+' #password').val();
  
  window.location.href = '/work.html';
}
