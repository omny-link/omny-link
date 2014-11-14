<?php

  function p_register_async_callback() {
    if (P_DEBUG) error_log('Call to p_register_async_callback');
    $user_id = get_current_user_id();
  	$user_name = $_POST['log'];
    $user_pass = $_POST['pwd'];
    if ($user_id == 0) {
      $user_id = username_exists( $user_name );
      if ( !$user_id and email_exists($user_name) == false ) {
        if ($user_pass == null) {
	         $user_pass = wp_generate_password( $length=12, $include_standard_special_chars=false );
	      }
        $user_id = wp_create_user( $user_name, $user_pass, $user_name );
        if (P_DEBUG) error_log('Created user id '.$user_id.' with username: '.$user_name);

        // Now store any user info we have received
        foreach($_POST as $key=>$value) {
          if ($key != 'log' && $key != 'action') {
            if(P_DEBUG) error_log( "Storing user field: $key=$value" );
            update_user_meta( $user_id, $key, $value );
          }
        }

        $xmlResponse = new WP_Ajax_Response(array(
           'what'=>'Registration',
           'action'=>'p_register_async',
           'id'=>1,
           'data'=>'Created user id '.$user_id.' with username: '.$user_name));
        $xmlResponse->send();
      } else {
        if (P_DEBUG) error_log('Either username '.$user_name.' or email '.$user_name.' already exists');
        $xmlResponse = new WP_Ajax_Response(array(
           'what'=>'Registration',
           'action'=>'p_register_async',
           'id'=>new WP_Error('already_registered',$user_name." already registered")));
        $xmlResponse->send();
      }
    } else {
      if (P_DEBUG) error_log('Attempt to register '.$user_name.' when already logged in with id: '.$user_id);
      $xmlResponse = new WP_Ajax_Response(array(
         'what'=>'Registration',
         'action'=>'p_register_async',
         'id'=>new WP_Error('already_logged_in',"You're already logged in with user id: ".$user_id.", cannot register as: ".$user_name)));
      $xmlResponse->send();
    }
  	die(); // this is required to return a proper result
  }
  if (P_DEBUG) error_log('Adding register ajax action');
  add_action( 'wp_ajax_p_register_async', 'p_register_async_callback' );
  add_action( 'wp_ajax_nopriv_p_register_async', 'p_register_async_callback' );

?>
