var del         = require('del');
var log         = require('fancy-log');
var gulp        = require('gulp');
var babel       = require('gulp-babel');
var bSync       = require('browser-sync').create();
var jshint      = require('gulp-jshint');
var cleanCSS    = require('gulp-clean-css');
var minimist    = require('minimist');
var replace     = require('gulp-replace');
var rsync       = require('gulp-rsync');
var through2    = require('through2');
var zip         = require('gulp-zip');
var vsn         = '3.1.10-SNAPSHOT';

var buildDir = 'target/classes';
var finalName = 'crm-ui-'+vsn+'.jar'

var argv = minimist(process.argv.slice(2));
var env = argv['env'] || 'dev';
log.warn('ENVIRONMENT SET TO: '+env);
var config = require('./config.js')[env];

gulp.task('clean', function(done) {
  return del([buildDir], done);
});

gulp.task('assets', function() {
  gulp.src([ 'src/d3-funnel/**/js/*.js' ])
      .pipe(gulp.dest(buildDir+'/d3-funnel/'));
  return gulp.src([ 'src/.htaccess', 'src/**/*.gif', 'src/**/*.jpg', 'src/**/*.json', 'src/**/*.ico', 'src/**/*.png', 'src/**/*.svg', 'knowprocess-icons/src/main/resources/META-INF/resources/webjars/**/*' ])
      .pipe(gulp.dest(buildDir+'/'));
});

gulp.task('scripts', function() {
  //gulp.src([ 'src/sw.js' ])
  //    .pipe(gulp.dest(buildDir));
  return gulp.src([
    'src/js/**/*.js', '!src/js/**/*.min.js',
    'src/catalog/js/**/*.js', '!src/catalog/js/**/*.min.js',
    'src/custmgmt/js/**/*.js', '!src/custmgmt/js/**/*.min.js',
    'src/workmgmt/js/**/*.js', '!src/workmgmt/js/**/*.min.js'
  ])
  .pipe(replace(/__CONFIG_SERVER_URL__/, config.configServerUrl))
  .pipe(replace('/vsn/', '/'+vsn+'/'))
  .pipe(config.js.minify ? babel({ presets: [ ["minify", { "builtIns": false }] ] }) : through2.obj())
  .pipe(gulp.dest(buildDir+'/'+vsn+'/js'));
});

gulp.task('lint', function() {
  return gulp.src([
    'src/js/**/*.js', '!src/js/**/*.min.js',
    'src/catalog/js/**/*.js', '!src/catalog/js/**/*.min.js',
    'src/custmgmt/js/**/*.js', '!src/custmgmt/js/**/*.min.js',
    '!src/js/vendor/**/*.js'
  ])
  .pipe(jshint())
  .pipe(jshint.reporter('default'))
  .pipe(jshint.reporter('fail'));
});

gulp.task('styles', function() {
  return gulp.src([
    'src/css/**/*.css'
  ])
  .pipe(config.css.minify ? cleanCSS() : through2.obj())
  .pipe(gulp.dest(buildDir+'/'+vsn+'/css'));
});

gulp.task('compile',
  gulp.series('lint', 'scripts', 'styles')
);

gulp.task('server', function(done) {
  bSync.init({
    server: {
      baseDir: [buildDir]
    }
  });
  gulp.watch(
    [ 'src/manifest.json', 'src/**/*.html' ],
    gulp.parallel('assets')
  );
  gulp.watch(
    ['src/**/*.js'],
    gulp.parallel('scripts')
  );
  gulp.watch(
    'src/css/**/*.css',
    gulp.parallel('styles')
  );
  gulp.watch(
    buildDir+'/**/*',
    bSync.reload
  );
  done();
});

gulp.task('fix-paths', function() {
  return gulp.src([
      'src/**/*.html',
    ])
    .pipe(replace('/vsn/', '/'+vsn+'/'))
    .pipe(replace('http://localhost:8080', config.apiServerUrl))
    .pipe(gulp.dest(buildDir));
});

gulp.task('package', () =>
  gulp.src([buildDir+'/*','!'+buildDir+'/*.jar','!'+buildDir+'/*.zip'])
      .pipe(zip(finalName))
      .pipe(gulp.dest(buildDir))
);

gulp.task('install',
  gulp.series('assets', 'fix-paths', 'compile', 'package')
);

gulp.task('default',
  gulp.series('install')
);

gulp.task('_deploy', function() {
  log.warn('Deploying from '+buildDir+' to '+env);
  if (config.server != undefined) {
    return gulp.src([ buildDir+'/**', buildDir+'/.htaccess'])
    .pipe(rsync({
      root: buildDir+'/',
      hostname: config.server.host,
      destination: config.server.dir,
      archive: false,
      silent: false,
      compress: true,
      exclude: ['*.jar', '*.tgz', '*.zip'],
      include: ['.htaccess']
    }))
    .on('error', function(err) {
      console.log(err);
    });
  } else {
    log.error('No config.server specified for '+env);
  }
});

gulp.task('deploy',
  gulp.series('install', '_deploy')
);

