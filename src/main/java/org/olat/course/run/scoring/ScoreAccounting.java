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
package org.olat.course.run.scoring;

import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 17 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ScoreAccounting {
	
	void setObligationContext(ObligationContext obligationContext);

	/**
	 * Retrieve all the score evaluations for all course nodes
	 */
	void evaluateAll();

	boolean evaluateAll(boolean update);

	/**
	 * Get the score evaluation for a given course node without using the cache.
	 * @param courseNode
	 * @return The score evaluation
	 */
	AssessmentEvaluation getScoreEvaluation(CourseNode courseNode);

	/**
	 * Evaluates the course node or simply returns the evaluation from the cache.
	 * @param cncourseNode
	 * @return ScoreEvaluation
	 */
	AssessmentEvaluation evalCourseNode(CourseNode courseNode);

}