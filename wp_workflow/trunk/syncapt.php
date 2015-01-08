<?php
/*
 * Plugin Name: Syncapt
 * Plugin URI: http://knowprocess.com/wp-plugins/syncapt
 * Description: Integrates web APIs with your WordPress app.
 * Author: Tim Stephenson
 * Version: 0.9.0
 * Author URI: http://syncapt.com
 * License: GPLv2 or later
 */

  define("P_ID", 'wp-workflow');
  define('P_VERSION', '0.9.0');
  define("P_NAME", 'Syncapt');
  define("P_TEXT_DOMAIN", 'p-textdomain');

  require_once("includes/options.php");
  $syncapt_options = new SyncaptOptions();
  define("P_DEBUG", $syncapt_options->is_debug_on());
  if (P_DEBUG) error_log('Syncapt debug logging is on');

  require_once("includes/ajax_support.php");
  require_once("includes/events.php");
  require_once("includes/shortcodes.php");
  require_once("includes/forms.php");

  if ( is_admin() ) {
    // admin only actions
  } else {
    // front end only 
  }
  add_action( 'wp_enqueue_scripts', 'p_load_scripts' );
  add_action( 'init', 'p_create_capabilities' );
  //add_action( 'init', 'p_create_mail_page' );
  add_action( 'wp_enqueue_styles', 'p_load_styles' );

  function syncapt_wp_footer() {
    echo 'Running Syncapt plugin in debug mode!';
  }
  if (P_DEBUG) add_action( 'wp_footer', 'syncapt_wp_footer' );

  function p_load_styles() {
    if ( is_admin() ) {
      /* Currently empty */
    } else {
      wp_enqueue_style(
        P_ID.'-frontend',
        plugins_url( 'css/frontend-'.P_VERSION.'.css', __FILE__ ),
        array(),
        null /* Force no version as query string */
      );
    }
  }

  function p_load_scripts() {
    if (P_DEBUG) error_log('Loading scripts for '.P_ID.' plugin');
    // used for both admin and front end
    wp_enqueue_script(
      P_ID.'-client',
      plugins_url( 'js/syncapt-'.P_VERSION.(P_DEBUG ? '' : '.min').'.js', __FILE__ ),
      array( 'jquery' ),
      null, /* Force no version as query string */
      true /* Force load in footer */
    );

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
        'moustache.js',
        plugins_url( 'js/moustache.js', __FILE__ ),
        array(),
        null, /* Force no version as query string */
        true /* Force load in footer */
      );
      wp_enqueue_script(
        P_ID.'-ui',
        plugins_url( 'js/workflow-'.P_VERSION.(P_DEBUG ? '' : '.min').'.js', __FILE__ ),
        array( 'jquery' ),
        null, /* Force no version as query string */
        true /* Force load in footer */
      );
    }
  }

  function p_create_capabilities() {
    // gets the author role
    $role = get_role( 'administrator' );

    // This only works, because it accesses the class instance.
    // would allow the author to edit others' posts for current theme only
    $role->add_cap( 'send_email' );
  }

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

?>
