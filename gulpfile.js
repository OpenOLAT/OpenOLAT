const gulp = require('gulp');
const sass = require('gulp-sass');
const uglify = require('gulp-uglify');
const concat = require('gulp-concat');
const rename = require('gulp-rename');
const cleanCSS = require('gulp-clean-css');
const log = require('fancy-log');

var theme = 'light';

gulp.task('theme', function() {

    return gulp.src('static/themes/' + theme + '/theme.scss')
        .pipe(sass({
            includePaths: [
                'static/themes',
                'static/themes/light'
            ],
//            outputStyle: 'compressed'
        }))
        .pipe(gulp.dest('static/themes/' + theme))

});

//script paths
assetsPath = 'src/main/webapp/static/';


gulp.task('js:build', done =>  {

    // jquery
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
    gulp.src(assetsPath + 'movie/player.min.js')
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

    // plugins: bootstrap, jquery plugins
    gulp.src([
            assetsPath + 'js/jquery/periodic/jquery.periodic.js',
            assetsPath + 'js/jshashtable-2.1_src.js',
            assetsPath + 'js/jquery/openolat/jquery.translator.js',
            assetsPath + 'js/jquery/openolat/jquery.navbar.js',
            assetsPath + 'js/jquery/openolat/jquery.bgcarrousel.js',
            assetsPath + 'js/tinymce4/tinymce/jquery.tinymce.min.js',
            assetsPath + 'js/functions.js',
            'node_modules/jquery.transit/jquery.transit.js',
            'node_modules/bootstrap/dist/js/bootstrap.min.js'
        ])
        .pipe(concat('js.plugins.min.js'))
        .pipe(uglify())
        .pipe(gulp.dest(assetsPath + 'js'))

    done();
});

gulp.task('css:build', done =>  {

        // minify
        gulp.src(
            [
                assetsPath + 'js/jquery/tagsinput/bootstrap-tagsinput.css',
                assetsPath + 'js/jquery/fullcalendar/fullcalendar.css',
                assetsPath + 'js/jquery/cropper/cropper.css',
                assetsPath + 'js/jquery/sliderpips/jquery-ui-slider-pips.css',
                assetsPath + 'js/jquery/ui/jquery-ui-1.11.4.custom.min.css',
                assetsPath + 'js/dragula/dragula.css'
            ])
            .pipe(concat('js.plugins.min.css'))
            .pipe(cleanCSS({debug: true, level: {1: {specialComments: 'none'}}}, (details) => {
                console.log(`${details.name}: ${details.stats.originalSize}`);
                console.log(`${details.name}: ${details.stats.minifiedSize}`);
            }))
            .pipe(gulp.dest(assetsPath + 'jquery'))

        // font-awesome: copy only
        gulp.src(
            [
                'node_modules/font-awesome/**/*'
            ]
        )
            .pipe(gulp.dest(assetsPath + 'font-awesome'))

    done();
})


gulp.task('watch', function() {
    gulp.watch('static/themes/**/*.scss', ['theme']);
});


// Default Task
gulp.task('default', gulp.parallel('js:build', 'css:build'));
