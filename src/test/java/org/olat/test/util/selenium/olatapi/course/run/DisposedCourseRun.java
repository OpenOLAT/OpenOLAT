/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the disposed course run.
 * One could get to this, if the course run gets disposed either by a course publish,
 * or by modify course properties.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class DisposedCourseRun extends OLATSeleniumWrapper {

	public DisposedCourseRun(Selenium selenium) {
		super(selenium);

    if(!selenium.isElementPresent("ui=course::disposed_closeAndRestart()")) {
    	//click anywhere, the course run was disposed anyway
    	selenium.click("ui=course::toolbox_generalTools_detailedView()");
  		selenium.waitForPageToLoad("30000");
    }
	}

	public CourseRun closeCourseAndRestart() {
		if(selenium.isElementPresent("ui=course::disposed_closeAndRestart()")) {
		  selenium.click("ui=course::disposed_closeAndRestart()");
		  selenium.waitForPageToLoad("30000");
		  return new CourseRun(selenium);
		}
		throw new IllegalStateException("There is no - Close and restart course - button present!");
	}
}
