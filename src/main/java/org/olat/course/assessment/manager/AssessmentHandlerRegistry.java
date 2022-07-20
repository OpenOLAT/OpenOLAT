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
package org.olat.course.assessment.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.olat.course.assessment.handler.AssessmentHandler;
import org.olat.course.learningpath.LearningPathOnlyAssessmentHandler;
import org.olat.course.nodes.CourseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class AssessmentHandlerRegistry {

	private static final String DEFAULT_ASSESSMENT_TYPE = LearningPathOnlyAssessmentHandler.TYPE;
	
	@Autowired
	private List<AssessmentHandler> loadedAssessmentHandlers;
	private Map<String, AssessmentHandler> assessmentHandlers = new HashMap<>();
	private AssessmentHandler defaultAssessmentHandler;
	
	@PostConstruct
	void initProviders() {
		for (AssessmentHandler handler: loadedAssessmentHandlers) {
			if (DEFAULT_ASSESSMENT_TYPE.equals(handler.acceptCourseNodeType())) {
				defaultAssessmentHandler = handler;
			} else {
				assessmentHandlers.put(handler.acceptCourseNodeType(), handler);
			}
		}
	}

	AssessmentHandler getAssessmentHandler(CourseNode courseNode) {
		AssessmentHandler handler = null;
		if (courseNode != null) {
			handler = assessmentHandlers.get(courseNode.getType());
		}
		if (handler == null) {
			handler = defaultAssessmentHandler;
		}
		return handler;
	}
	
}
