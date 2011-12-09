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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium;


/**
 * Defines different page load waiting times in ms and allows nowait time.
 * The facility "Not waiting" for page load was introduced in conjunction with codepoints. See the following example
 * <pre>
        //trigger "Activation Content" which in turn loads each of its children for displaying the Previews -> Changed behavior of test!
		StructureElement selectActivation = courseRun.selectAnyButGetToRoot("Activation Interaction", PageLoadWait.NO_WAIT);
		selenium = selectActivation.getSelenium();
		
		// ASSERTION check if codepoint reached, if yes continue
		beforeSyncCp_A.assertBreakpointReached(1, 10000);
		System.out.println("beforeSyncCp_A.assertBreakpointReached");
		TemporaryPausedThread[] threadsA = beforeSyncCp_A.getPausedThreads();
		threadsA[0].continueThread();
		
		doInSyncCp_A.assertBreakpointReached(1, 10000);
		System.out.println("doInSyncCp_A.assertBreakpointReached");
		threadsA = doInSyncCp_A.getPausedThreads(); //overwrite threadsA
		threadsA[0].continueThread();
		
		// activate actual "Forum" content for proceeding
		selenium.waitForPageToLoad("30000");//wait for previous NO_WAITED Action
		courseRun.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
		</pre> 
 * <P>
 * Initial Date: Apr 29, 2011 <br>
 * 
 * @author patrick
 */
public enum PageLoadWait {

	LONG("60000"), DEFAULT("30000"), SHORT("5000"), NO_WAIT("0");

	//ms are specified via String to Selenium
	private String ms;

	PageLoadWait(String msValue) {
		this.ms = msValue;
	}

	public String getMs() {
		return ms;
	}

}
