/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns.assessment;

import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.FullyAssessedEvaluator;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 17 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSFullyAssesssedEvaluator implements FullyAssessedEvaluator {

	@Override
	public Boolean getFullyAssessed(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			List<AssessmentEvaluation> children, Blocker blocker) {
		String requiredSelectionsConfig = courseNode.getModuleConfiguration().getStringValue(CNSCourseNode.CONFIG_KEY_REQUIRED_SELECTIONS);
		return getFullyAssessed(requiredSelectionsConfig, blocker, children);
	}
	
	Boolean getFullyAssessed(String requiredSelectionsConfig, Blocker blocker, List<AssessmentEvaluation> children) {
		if (StringHelper.isLong(requiredSelectionsConfig)) {
			Integer requiredSelections = Integer.valueOf(requiredSelectionsConfig);
			
			int numSelected = 0;
			boolean allMandatoryDone = true;
			for (AssessmentEvaluation childEvaluation : children) {
				if (childEvaluation.getObligation() != null
						&& childEvaluation.getObligation().getCurrent() != null
						&& childEvaluation.getObligation().getCurrent() != AssessmentObligation.excluded) {
					numSelected++;
					if (childEvaluation.getObligation().getCurrent() == AssessmentObligation.mandatory
							&& (childEvaluation.getFullyAssessed() == null || !childEvaluation.getFullyAssessed().booleanValue())) {
						allMandatoryDone = false;
					}
				}
			}
			
			if (!allMandatoryDone) {
				blocker.block();
				return Boolean.FALSE;
			}
			if (numSelected >= requiredSelections) {
				return Boolean.TRUE;
			}
			
		}
		blocker.block();
		return Boolean.FALSE;
	}

}
