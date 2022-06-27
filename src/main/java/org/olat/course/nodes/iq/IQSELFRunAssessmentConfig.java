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
package org.olat.course.nodes.iq;

import org.olat.course.learningpath.LearningPathOnlyAssessmentConfig;
import org.olat.course.run.scoring.AssessmentEvaluation;

/**
 * Just a wrapper to get the same display as before.
 * 
 * Initial date: 27 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQSELFRunAssessmentConfig extends LearningPathOnlyAssessmentConfig {
	
	private AssessmentEvaluation assessmentEval;

	public IQSELFRunAssessmentConfig(AssessmentEvaluation assessmentEval) {
		this.assessmentEval = assessmentEval;
	}

	@Override
	public Mode getScoreMode() {
		return Mode.setByNode;
	}

	@Override
	public Mode getPassedMode() {
		return assessmentEval.getPassed() == null ? Mode.none : Mode.setByNode;
	}

	@Override
	public boolean hasAttempts() {
		return true;
	}
	
	

}
