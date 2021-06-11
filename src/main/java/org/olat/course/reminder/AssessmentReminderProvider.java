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
package org.olat.course.reminder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.reminder.rule.AttemptsRuleSPI;
import org.olat.course.reminder.rule.InitialAttemptsRuleSPI;
import org.olat.course.reminder.rule.PassedRuleSPI;
import org.olat.course.reminder.rule.ScoreRuleSPI;

/**
 * 
 * Initial date: 9 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentReminderProvider implements CourseNodeReminderProvider {
	
	private final String nodeIdent;
	private final AssessmentConfig assessmentConfig;
	private List<String> mainTypes;
	
	public AssessmentReminderProvider(String nodeIdent, AssessmentConfig assessmentConfig) {
		this.nodeIdent = nodeIdent;
		this.assessmentConfig = assessmentConfig;
	}

	@Override
	public String getCourseNodeIdent() {
		return nodeIdent;
	}

	@Override
	public boolean filter(Collection<String> ruleNodeIdents) {
		return ruleNodeIdents.contains(nodeIdent);
	}

	@Override
	public Collection<String> getMainRuleSPITypes() {
		if (mainTypes == null) {
			mainTypes = new ArrayList<>(4);
			if (assessmentConfig.hasAttempts()) {
				mainTypes.add(InitialAttemptsRuleSPI.class.getSimpleName());
				mainTypes.add(AttemptsRuleSPI.class.getSimpleName());
			}
			if (Mode.none != assessmentConfig.getPassedMode()) {
				mainTypes.add(PassedRuleSPI.class.getSimpleName());
			}
			if (Mode.none != assessmentConfig.getScoreMode()) {
				mainTypes.add(ScoreRuleSPI.class.getSimpleName());
			}
		}
		return mainTypes;
	}

	@Override
	public String getDefaultMainRuleSPIType(List<String> availableRuleTypes) {
		if (availableRuleTypes.contains(PassedRuleSPI.class.getSimpleName())) {
			return PassedRuleSPI.class.getSimpleName();
		} else if (availableRuleTypes.contains(ScoreRuleSPI.class.getSimpleName())) {
			return ScoreRuleSPI.class.getSimpleName();
		} else if (availableRuleTypes.contains(InitialAttemptsRuleSPI.class.getSimpleName())) {
			return InitialAttemptsRuleSPI.class.getSimpleName();
		} else if (availableRuleTypes.contains(AttemptsRuleSPI.class.getSimpleName())) {
			return AttemptsRuleSPI.class.getSimpleName();
		}
		return null;
	}

	@Override
	public void refresh() {
		mainTypes = null;
	}

}
