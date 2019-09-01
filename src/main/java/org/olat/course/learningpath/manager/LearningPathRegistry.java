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

import javax.annotation.PostConstruct;

import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.learningpath.evaluation.ConfigNodeObligationEvaluatorProvider;
import org.olat.course.learningpath.evaluation.DefaultNodeLinearStatusEvaluatorProvider;
import org.olat.course.learningpath.evaluation.NodeLinearStatusEvaluatorProvider;
import org.olat.course.learningpath.evaluation.NodeObligationEvaluatorProvider;
import org.olat.course.learningpath.evaluation.ObligationEvaluator;
import org.olat.course.learningpath.evaluation.ObligationEvaluatorProvider;
import org.olat.course.learningpath.evaluation.StatusEvaluator;
import org.olat.course.learningpath.evaluation.StatusEvaluatorProvider;
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
	private static final String DEFAULT_OBLIGATION_EVALUATOR_NODE_TYPE = ConfigNodeObligationEvaluatorProvider.NODE_TYPE;
	private static final String DEFAULT_LINEAR_STATUS_EVALUATOR_NODE_TYPE = DefaultNodeLinearStatusEvaluatorProvider.NODE_TYPE;

	@Autowired
	private List<LearningPathNodeHandler> learningPathNodeHandlers;
	private Map<String, LearningPathNodeHandler> nodeTypeToLearningPathNodeHandlers;
	private LearningPathNodeHandler nonLearningPathNodeHandler;

	@Autowired
	private List<NodeObligationEvaluatorProvider> nodeObligationEvaluatorProviders;
	private Map<String, ObligationEvaluator> nodeTypeToObligationEvaluator;

	@Autowired
	private List<NodeLinearStatusEvaluatorProvider> nodeLinearStatusEvaluatorProviders;
	private Map<String, StatusEvaluator> nodeTypeToLinearStatusEvaluator;
	
	@PostConstruct
	void initProviders() {
		initLearningPathHandlers();
		initObligationEvaluator();
		initLinearStatusEvaluator();
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

	private void initObligationEvaluator() {
		nodeTypeToObligationEvaluator = new HashMap<>();
		for (NodeObligationEvaluatorProvider provider : nodeObligationEvaluatorProviders) {
			nodeTypeToObligationEvaluator.put(provider.acceptCourseNodeType(), provider.getObligationEvaluator());
		}
	}

	private void initLinearStatusEvaluator() {
		nodeTypeToLinearStatusEvaluator = new HashMap<>();
		for (NodeLinearStatusEvaluatorProvider provider : nodeLinearStatusEvaluatorProviders) {
			nodeTypeToLinearStatusEvaluator.put(provider.acceptCourseNodeType(), provider.getStatusEvaluator());
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

	ObligationEvaluatorProvider getObligationEvaluatorProvider() {
		return (node) -> {
			ObligationEvaluator evaluator = nodeTypeToObligationEvaluator.get(node.getType());
			if (evaluator == null) {
				evaluator = nodeTypeToObligationEvaluator.get(DEFAULT_OBLIGATION_EVALUATOR_NODE_TYPE);
			}
			return evaluator;
		};
	}

	StatusEvaluatorProvider getLinearStatusEvaluatorProvider() {
		return (node) -> {
			StatusEvaluator evaluator = nodeTypeToLinearStatusEvaluator.get(node.getType());
			if (evaluator == null) {
				evaluator = nodeTypeToLinearStatusEvaluator.get(DEFAULT_LINEAR_STATUS_EVALUATOR_NODE_TYPE);
			}
			return evaluator;
		};
	}

}
