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
package org.olat.test.load;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Performance test; it measures the time needed to open course, click few times (numOfIterations) 
 * in course, close course, and repeat for all courses.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class OpenAllCoursesTest extends BaseSeleneseTestCase {

	private final int numOfIterations = 5;

	public void testVisitAllCourses() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		LearningResources learningResources = workflow.getLearningResources();
		//go to courses and open each course on each table page, then close the course
		boolean selectNextPage = false;
		int courseIndex = 1;
		int pageLength = 20;
		while(courseIndex<=pageLength) {
			CourseRun courseRun = learningResources.showCourseContent(selectNextPage, courseIndex);
			if(courseRun!=null) {
				visitCourse(courseRun);
				courseIndex++;
				selectNextPage = false;
			} else if(courseRun==null && !learningResources.hasMorePages()) {
				break;
			}
			if(courseIndex==21 && learningResources.hasMorePages()) {
				courseIndex = 1;
				selectNextPage = true;				
			}				
		}		

	}

	/**
	 * Select first numOfIterations elements in this course.
	 * 
	 * @param courseRun
	 */
	public void visitCourse(CourseRun courseRun) throws InterruptedException {
		System.out.println("visit course");
		//select node in course
		for(int i=1; i<=numOfIterations; i++) {
			boolean elemFound = courseRun.selectCourseElement(i);
			if(!elemFound) {
				break;
			}
		}		
		Thread.sleep(3000);
		courseRun.closeAny();
		System.out.println("closed course");
	}

}
