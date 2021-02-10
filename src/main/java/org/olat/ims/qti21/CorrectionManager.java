/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21;

import org.olat.core.id.Identity;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;

/**
 * Some helper methods to correct tests.
 * 
 * Initial date: 10 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CorrectionManager {
	
	/**
	 * Update the score evaluation of the course element, update the
	 * passed value if the cut value is defined.
	 * 
	 * @param testSession The candidate session
	 * @param assessmentTest The test
	 * @param courseNode The course element
	 * @param courseEnv The course environment
	 * @param doer Who is doing the change
	 */
	public void updateCourseNode(AssessmentTestSession testSession, AssessmentTest assessmentTest,
			IQTESTCourseNode courseNode, CourseEnvironment courseEnv, Identity doer);

}
