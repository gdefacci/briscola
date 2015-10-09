module.exports = function(grunt) {

  grunt.file.defaultEncoding = 'utf8';

  grunt.loadNpmTasks("grunt-ts");
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-stylus');

  var contextPath         = "/app"

  var mainDir             = "src/main/";

  var tsSourceDir         = mainDir + "ts/";
  var tsSources           = tsSourceDir + "**/*.ts";
  var tsxSources          = tsSourceDir + "**/*.tsx";

  var cssSourceDir        = mainDir + "css/";
  var cssSources          = cssSourceDir + "**/*.styl";

  var webappDir           = mainDir + "webapp/";
  var jsDir               = webappDir + "js/";
  var appTargetDir        = jsDir + "app/";

  var cssDir              = webappDir + "css/";
  var appCss              = cssDir + "app.css";

  var tsDebugSoucesDir    = appTargetDir + "ts/";

  var mainApplication     = appTargetDir + "App.js"

  var stylusFiles = {};

  var tsConfig = grunt.file.readJSON("tsconfig.json")

  grunt.task.registerTask('gen-all-ts', 'A task to generate the _all.ts. file', function() {
    var content = tsConfig.files.map(function(f) { return "/// <reference path='" + f + "' />"; }).join("\n")
    grunt.file.write(tsSourceDir + "_all.ts", content)
  });

  function addStylusResource(nm) {
    stylusFiles[cssDir + nm + ".css"] = cssSourceDir + nm + ".styl";
  }

  addStylusResource("app")

  grunt.initConfig({
    ts: {
      options: tsConfig.options,
      build: {
        src: tsConfig.files.map(function(f) { return tsSourceDir + f; }),
        out: mainApplication,
        options: {
          additionalFlags: "--jsx react",
          sourceRoot: contextPath + "/js/app/ts"
        }
      }
    },
    stylus: {
      options: {
        compress: false,
        use: [
          require('jeet')
        ]
      },
      compile: {
        files: stylusFiles
      }
    },
    watch: {
      options: {
        livereload: true
      },
      src: {
        files: [tsSources, tsxSources, cssSources],
        tasks: ['build']
      }
    },
    clean: {
      js: {
        src: [appTargetDir]
      }
    },
    copy: {
      tsSources: {
        cwd: tsSourceDir,
        expand: true,
        src: ["**/*.ts", "**/*.tsx"],
        dest: tsDebugSoucesDir
      }
    }
  });

  grunt.registerTask("default", ["ts:build"]);
  grunt.registerTask("build", ["stylus:compile", "gen-all-ts", "ts:build", "copy:tsSources"]);
  grunt.registerTask("build-watch", ["clean", "build", "watch"]);

  grunt.registerTask("build-copy", ["copy:tsSources"]);

};
