/*
 *  jsMath-version-check.js
 *  
 *  Part of the jsMath package for mathematics on the web.
 *
 *  This file checks to see if there is a version of jsMath
 *  newer than the one the user is running.
 *
 *  ---------------------------------------------------------------------
 *
 *  Copyright 2004-2006 by Davide P. Cervone
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/********************************************************************/

jsMath.Controls.TestVersion = function () {
  var version = 'v3.5';
  var download = 'http://www.math.union.edu/locate/jsMath/download/jsMath.html';

  jsMath.Controls.panel.style.display = 'none';
  if (version != 'v'+jsMath.version.replace(/-.*/,'')) {
    var OK = confirm('jsMath '+version+' is now available.\n'
                   + 'Do you want to download the new version?\n\n'
                   + '(This is only needed by web-page authors, so there\n'
                   + 'is no need to get the new version if you are not\n'
                   + 'the maintainer for the page you are viewing.)');
    if (OK) {jsMath.window.location = download} else {jsMath.Controls.Close()}
  } else {
    alert('The version of jsMath used on this page is up to date.');
    jsMath.Controls.Close();
  }
}

if (jsMath.Message != null) {
  jsMath.Message.Clear(); jsMath.Message.doClear();
  setTimeout('jsMath.Controls.TestVersion();',1);
}
