var gulp = require('gulp');
var sass = require('gulp-sass');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var rename = require('gulp-rename');
let cleanCSS = require('gulp-clean-css');

var theme = 'light';
var assetsPath = 'src/main/webapp/static/';

// set to true, if you run OpenOlat in debug mode
var guidebug = false;


gulp.task('theme', function() {

    return gulp.src(assetsPath + 'themes/' + theme + '/theme.scss')
        .pipe(sass({
            includePaths: [
                assetsPath + 'themes',
                assetsPath + 'themes/light'
            ],
        }))
        .pipe(gulp.dest(assetsPath + 'themes/' + theme))

});


gulp.task('js:build', function () {

    // jquery: copy only
    gulp.src('node_modules/jquery/dist/jquery.min.js')
        .pipe(rename('jquery-3.3.1.min.js'))
        .pipe(gulp.dest(assetsPath + 'js/jquery'));


    // d3: copy only
    gulp.src(
        [
            'node_modules/d3/build/d3.min.js',
            'node_modules/d3/build/d3.js',
        ]
    )
        .pipe(gulp.dest(assetsPath + 'js/d3'))

    // dragula: copy only
    gulp.src(
        [
            'node_modules/dragula/dist/*',
        ]
    )
        .pipe(gulp.dest(assetsPath + 'js/dragula'))

    // movie player: copy only
    gulp.src('static/movie/player.min.js')
        .pipe(gulp.dest(assetsPath + 'movie'));

    // iframe resizer: copy only
    gulp.src('node_modules/iframe-resizer/js/iframeResizer.min.js')
        .pipe(gulp.dest(assetsPath + 'js/iframeResizer'))

    // qrcodejs: copy only
    gulp.src(
        [
            'node_modules/qrcodejs/qrcode.min.js',
            'node_modules/qrcodejs/qrcode.js',
        ]
    )
        .pipe(gulp.dest(assetsPath + 'js/jquery/qrcodejs'))

    // bundle plugins: bootstrap (sass), jquery plugins
    if (!guidebug) {
        // production mode: concat and uglify
        gulp.src([
            assetsPath + 'static/js/jquery/jquery.periodic.js',
            assetsPath + 'static/js/jshashtable-2.1_src.js',
            assetsPath + 'static/js/jquery/openolat/jquery.translator.js',
            assetsPath + 'static/js/jquery/openolat/jquery.navbar.js',
            assetsPath + 'static/js/jquery/openolat/jquery.bgcarrousel.js',
            assetsPath + 'static/js/tinymce4/tinymce/jquery.tinymce.min.js',
            assetsPath + 'static/functions.js',
            assetsPath + 'node_modules/jquery.transit/jquery.transit.js',
            assetsPath + 'node_modules/bootstrap-sass/assets/javascripts/bootstrap.min.js'
        ])
            .pipe(concat('js.plugins.min.js'))
            .pipe(uglify())
            .pipe(gulp.dest(assetsPath + 'js'))
    } else {
        // debug mode: copy only
        gulp.src([
            'node_modules/jquery.transit/jquery.transit.js'
        ])
            .pipe(gulp.dest(assetsPath + 'js/jquery/transit'))

        gulp.src([
            'node_modules/bootstrap-sass/assets/javascripts/bootstrap/*'
        ])
            .pipe(gulp.dest(assetsPath + 'bootstrap/javascripts/bootstrap'))

    }

});


gulp.task('css:build', function () {

        if (!guidebug) {
            // production mode: concat and minify
            gulp.src(
                [
                    assetsPath + 'static/js/jquery/tagsinput/bootstrap-tagsinput.css',
                    assetsPath + 'static/js/jquery/fullcalendar/fullcalendar.css',
                    assetsPath + 'static/js/jquery/cropper/cropper.css',
                    assetsPath + 'static/js/jquery/sliderpips/jquery-ui-slider-pips.css',
                    assetsPath + 'static/js/jquery/ui/jquery-ui-1.11.4.custom.min.css',
                    assetsPath + 'static/js/dragula/dragula.css'
                ])
                .pipe(concat('js.plugins.min.css'))
                .pipe(cleanCSS({debug: true, level: {1: {specialComments: 'none'}}}, (details) => {
                    console.log(`${details.name}: ${details.stats.originalSize}`);
                    console.log(`${details.name}: ${details.stats.minifiedSize}`);
                }))
                .pipe(gulp.dest(assetsPath + 'jquery'))
        } else {
            // debug mode: copy only
            // nothing at the moment

        }

        // font-awesome: copy only
        gulp.src(
            [
                'node_modules/font-awesome/**/*'
            ]
        )
            .pipe(gulp.dest(assetsPath + 'font-awesome'))

})


gulp.task('watch', function() {
    gulp.watch('static/themes/**/*.scss', ['theme']);
    gulp.watch('node_modules/**/*', ['default']);
});

// Default Task
gulp.task('default', ['css:build', 'js:build', 'theme']);