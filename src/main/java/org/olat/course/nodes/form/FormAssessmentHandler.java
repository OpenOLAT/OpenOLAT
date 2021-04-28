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
package org.olat.course.nodes.form;

import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.learningpath.LearningPathOnlyAssessmentHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.04.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FormAssessmentHandler extends LearningPathOnlyAssessmentHandler {

	private static final AssessmentConfig ASSESSMENT_CONFIG = new FormAssessmentConfig();

	@Override
	public String acceptCourseNodeType() {
		return FormCourseNode.TYPE;
	}
	
	@Override
	public AssessmentConfig getAssessmentConfig(CourseNode courseNode) {
		return ASSESSMENT_CONFIG;
	}

}
