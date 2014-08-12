<?php
/*
 * Plugin Name: Syncapt
 * Plugin URI: http://knowprocess.com/wp-plugins/syncapt
 * Description: Integrates the whole web with your WordPress app.
 * Author: Tim Stephenson
 * Version: 0.6.0
 * Author URI: http://knowprocess.com
 * License: GPLv2 or later
 */

  define("P_ID", 'wp-workflow');
  define("P_VERSION", "0.6.0");
  define("P_NAME", 'Syncapt');
  define("P_DEBUG", true);
  require_once("includes/shortcodes.php");

  if ( is_admin() ) { // admin actions
    add_action( 'admin_menu', 'add_p_admin_menu' );
    add_action( 'admin_init', 'register_p_admin_settings' );
  } else {
    // non-admin enqueues, actions, and filters
    // Not sure of the rights and wrongs but wp_enqueue_styles did not work
    add_action( 'wp_head', 'p_load_styles' );
    add_action( 'wp_enqueue_scripts', 'p_load_scripts' );
  }
  add_action( 'init', 'p_create_capabilities' );
  add_action( 'init', 'p_create_mail_page' );
  add_action( 'wp_ajax_change_subscription', 'change_subscription' );

  function syncapt_wp_footer() {
    echo 'Running Syncapt plugin in debug mode!';
  }
  if (P_DEBUG) add_action( 'wp_footer', 'syncapt_wp_footer' );

  function p_load_styles() {
    if ( is_admin() ) {
      wp_enqueue_style(
        P_ID.'-admin',
        plugins_url( 'css/admin-'.P_VERSION.'.css', __FILE__ ),
        array(),
        null /* Force no version as query string */
      );
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
        P_ID.'-client',
        plugins_url( 'js/syncapt-'.P_VERSION.(P_DEBUG ? '' : '.min').'.js', __FILE__ ),
        array( 'jquery' ),
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
  
  /** Render the settings / options page in the admin dashboard */
  function p_options_page() {
    if ( !current_user_can( 'manage_options' ) )  {
      wp_die( __( 'You do not have sufficient permissions to access this page.' ) );
    }?>
    <div class="wrap">
    <h2>Syncapt Settings</h2>
    <form method="post" action="options.php">
      <table class="form-table">
        <tr valign="top">
          <th scope="row">Syncapt URL</th>
          <td><input type="text" name="server_url" value="<?php echo get_option('server_url'); ?>" /></td>
        </tr>
        <tr valign="top">
          <th scope="row">API key</th>
          <td><input type="text" name="api_key" value="<?php echo get_option('api_key'); ?>" /></td>
        </tr>
        <tr valign="top">
          <th scope="row">API secret</th>
          <td><input type="text" name="api_secret" value="<?php echo get_option('api_secret'); ?>" /></td>
        </tr>
      </table>
    <?php 
    settings_fields( P_ID.'-basic-group' );
    do_settings_sections( P_ID.'-basic-group' );
    submit_button();?>
    </form>
    </div>
    <?php
  }

  function add_p_admin_menu() {
    add_options_page( P_NAME.' Options', P_NAME, 'manage_options', P_ID, 'p_options_page' );
  }

  function register_p_admin_settings() { 
    error_log('Registering settings...');
    register_setting( P_ID.'-basic-group', 'server_url' );
    register_setting( P_ID.'-basic-group', 'api_key' );
    register_setting( P_ID.'-basic-group', 'api_secret' );
  }

  function change_subscription() {
    if (!empty($_POST['syncapt_user']) && !empty($_POST['syncapt_pass'])) {
      //error_log('Request to change subscription WITH expected params: syncapt_user and syncapt_pass: '.$_POST['syncapt_user']);
      $user = wp_get_current_user();

      update_user_meta( $user->ID, 'syncapt_user', $_POST['syncapt_user']);
      update_user_meta( $user->ID, 'syncapt_pass', $_POST['syncapt_pass']);
      die();
    } else {
      error_log('Request to change subscription without expected params: syncapt_user and syncapt_pass');
      die($st);
    }
  }
    add_action( 'wp_enqueue_styles', 'p_load_styles' );
?>
