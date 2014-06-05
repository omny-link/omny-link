<?php
/*
* Plugin Name: Syncapt
* Plugin URI: http://knowprocess.com/wp-plugins/syncapt
* Description: Syncapt integrates the whole web with your WordPress app.
* Author: Tim Stephenson
* Version: 0.5.0
* Author URI: http://knowprocess.com
* License: GPLv2 or later
*/
?>

<?php
  define("P_VERSION", "0.5.0");
  define("P_DEBUG", false);
  require_once("includes/shortcodes.php");

  function syncapt_wp_footer() {
    echo 'Running Syncapt plugin!';
  }
  if (P_DEBUG) add_action( 'wp_footer', 'syncapt_wp_footer' );

  function p_load_styles() {
    if ( is_admin() ) {
      wp_enqueue_style(
        'syncapt-plugin-admin',
        plugins_url( 'css/admin-0.5.0.css', __FILE__ ),
        array(),
        null, /* Force no version as query string */
        'screen'
      );
    } else {
      wp_enqueue_style(
        'syncapt-plugin-frontend',
        plugins_url( 'css/frontend-0.5.0.css', __FILE__ ),
        array(  ),
        null, /* Force no version as query string */
        'screen'
      );
    }
  }
  add_action( 'init', 'p_load_styles' );

  function p_load_scripts() {
    if ( is_admin() ) {
      /*
      wp_enqueue_script(
        'syncapt-plugin-admin',
        plugins_url( 'js/admin.js', __FILE__ ),
        array( 'jquery' ),
        P_VERSION
      );
      */
    } else {
      wp_enqueue_script(
        'syncapt-client',
        plugins_url( 'js/syncapt-0.4.0.js', __FILE__ ),
        array( 'jquery' ),
        null, /* Force no version as query string */
        true /* Force load in footer */
      );
    }
  }
  add_action( 'init', 'p_load_scripts' );

  function p_create_mail_page() { 
    $page = get_page_by_path('syncapt-mail');
    if ( $page==null ) {
      $post = array(
          //'post_content'   => file_get_contents(plugins_url( 'syncapt/pages/email.html' , dirname(__FILE__) )), // The full text of the post.
          'post_content'   => '[p_page page="email"]', // The full text of the post.
          'post_name'      => 'syncapt-mail', // The name (slug) for your post
          'post_title'     => 'Send pro-forma mail', // The title of your post.
          'post_status'    => 'publish',
          'post_type'      => 'page', 
          'ping_status'    => 'closed', // Pingbacks or trackbacks allowed. Default is the option 'default_ping_status'.
          'comment_status' => 'closed', // Default is the option 'default_comment_status', or 'closed'.
          );  
      // Insert the post into the database
      wp_insert_post( $post );
    }
  } 
  add_action( 'init', 'p_create_mail_page' );
  

?>
