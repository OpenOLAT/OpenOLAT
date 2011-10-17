
//
// ROLLUPS.JS
// ==========
// This file contains rollup definitions used in OLAT. It is attached into 
// build/classes/user-extensions.js in the build.xml
//

var myRollupManager = new RollupManager();

myRollupManager.addRollupRule({
    name: 'replace_click_with_clickAndWait'
    , description: 'replaces commands where a click was detected with clickAndWait instead'
    , alternateCommand: 'clickAndWait'
    , commandMatchers: [
        {
            command: 'click'
            , target: 'ui=(tabs|home|groups|learningResources|course|courseEditor|group|groupManagement|qti|testEditor|userManagement)::.+\\(.*\\)'
        }
    ]
    , expandedCommands: []
});

myRollupManager.addRollupRule({
    name: 'replace_click_login_with_clickAndWait'
    , description: 'replaces commands where a click was detected with clickAndWait instead'
    , alternateCommand: 'clickAndWait'
    , commandMatchers: [
        {
            command: 'click'
            , target: 'ui=dmz::login\\(.*\\)'
        }
    ]
    , expandedCommands: []
});
