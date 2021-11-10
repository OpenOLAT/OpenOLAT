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
package org.olat.course.nodes.gta.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAAssessmentConfig;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;

/**
 * 
 * Initial date: 09.06.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTAReminderProvider implements CourseNodeReminderProvider {
	
	private final GTACourseNode gtaNode;
	private AssessmentReminderProvider assessmentReminderProvider;
	private List<String> mainTypes;
	
	public GTAReminderProvider(GTACourseNode gtaNode) {
		this.gtaNode = gtaNode;
		this.assessmentReminderProvider = new AssessmentReminderProvider(gtaNode.getIdent(), new GTAAssessmentConfig(gtaNode.getModuleConfiguration()));
	}

	@Override
	public String getCourseNodeIdent() {
		return gtaNode.getIdent();
	}

	@Override
	public boolean filter(Collection<String> ruleNodeIdents) {
		return ruleNodeIdents.contains(gtaNode.getIdent());
	}

	@Override
	public Collection<String> getMainRuleSPITypes() {
		if (mainTypes == null) {
			mainTypes = new ArrayList<>(6);
			mainTypes.addAll(assessmentReminderProvider.getMainRuleSPITypes());
			
			if (DueDateConfig.isDueDate(gtaNode.getDueDateConfig(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE))) {
				mainTypes.add(AssignTaskRuleSPI.class.getSimpleName());
			}
			
			if (DueDateConfig.isDueDate(gtaNode.getDueDateConfig(GTACourseNode.GTASK_SUBMIT_DEADLINE))) {
				mainTypes.add(SubmissionTaskRuleSPI.class.getSimpleName());
			}
		}
		
		return mainTypes;
	}

	@Override
	public String getDefaultMainRuleSPIType(List<String> availableRuleTypes) {
		if (availableRuleTypes.contains(AssignTaskRuleSPI.class.getSimpleName())) {
			return AssignTaskRuleSPI.class.getSimpleName();
		} else if (availableRuleTypes.contains(SubmissionTaskRuleSPI.class.getSimpleName())) {
			return SubmissionTaskRuleSPI.class.getSimpleName();
		}
		return assessmentReminderProvider.getDefaultMainRuleSPIType(availableRuleTypes);
	}
	
	@Override
	public void refresh() {
		assessmentReminderProvider = new AssessmentReminderProvider(gtaNode.getIdent(), new GTAAssessmentConfig(gtaNode.getModuleConfiguration()));
		mainTypes = null;
	}
	
}