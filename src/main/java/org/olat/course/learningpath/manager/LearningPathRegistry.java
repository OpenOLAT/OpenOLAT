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
package org.olat.course.learningpath.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.nodes.CourseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class LearningPathRegistry {
	
	private static final String UNSUPPORTED_LEARNING_PATH_TYPE = UnsupportedLearningPathNodeHandler.NODE_TYPE;

	@Autowired
	private List<LearningPathNodeHandler> learningPathNodeHandlers;
	private Map<String, LearningPathNodeHandler> nodeTypeToLearningPathNodeHandlers;
	private LearningPathNodeHandler nonLearningPathNodeHandler;
	@Autowired
	private List<ExceptionalObligationHandler> exceptionalObligationHandlers;
	
	@PostConstruct
	void initProviders() {
		initLearningPathHandlers();
	}

	private void initLearningPathHandlers() {
		nodeTypeToLearningPathNodeHandlers = new HashMap<>();
		for (LearningPathNodeHandler handler: learningPathNodeHandlers) {
			if (UNSUPPORTED_LEARNING_PATH_TYPE.equals(handler.acceptCourseNodeType())) {
				nonLearningPathNodeHandler = handler;
			} else {
				nodeTypeToLearningPathNodeHandlers.put(handler.acceptCourseNodeType(), handler);
			}
		}
	}

	LearningPathNodeHandler getLearningPathNodeHandler(String courseNodeType) {
		LearningPathNodeHandler handler = nodeTypeToLearningPathNodeHandlers.get(courseNodeType);
		if (handler == null) {
			handler = nonLearningPathNodeHandler;
		}
		return handler;
	}

	LearningPathNodeHandler getLearningPathNodeHandler(CourseNode courseNode) {
		return getLearningPathNodeHandler(courseNode.getType());
	}
	
	List<ExceptionalObligationHandler> getExceptionalObligationHandler() {
		return exceptionalObligationHandlers.stream()
				.filter(ExceptionalObligationHandler::isEnabled)
				.collect(Collectors.toList());
	}
	
	ExceptionalObligationHandler getExceptionalObligationHandler(String type, boolean disabled) {
		return exceptionalObligationHandlers.stream()
				.filter(handler -> disabled || handler.isEnabled())
				.filter(handler -> handler.getType().equals(type))
				.findFirst()
				.orElseGet(() -> null);
	}
	
}
