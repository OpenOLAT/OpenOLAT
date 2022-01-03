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
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * Initial date: 3 Jan 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoursePreviewAccounting implements ScoreAccounting {
	
	public static final AssessmentEvaluation EVALALUATION = new AssessmentEvaluation(null, null, null, null, null, null,
			AssessmentEntryStatus.notStarted, null, null, null, null, null, null, null, null, null, 0, null, null, null,
			null, null, null, null, null, null, null);

	@Override
	public void evaluateAll() {
		//
	}

	@Override
	public boolean evaluateAll(boolean update) {
		return false;
	}

	@Override
	public AssessmentEvaluation getScoreEvaluation(CourseNode courseNode) {
		return EVALALUATION;
	}

	@Override
	public AssessmentEvaluation evalCourseNode(CourseNode courseNode) {
		return EVALALUATION;
	}

}
