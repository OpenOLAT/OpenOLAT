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
package org.olat.course.nodes.gta.ui;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.rule.AssignTaskRuleSPI;
import org.olat.course.nodes.gta.rule.SubmissionTaskRuleSPI;
import org.olat.course.reminder.ui.BeforeDueDateRuleEditor;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.reminder.ReminderRule;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 30 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BeforeDateTaskRuleEditor extends BeforeDueDateRuleEditor {

	public BeforeDateTaskRuleEditor(ReminderRule rule, RepositoryEntry entry, String ruleType) {
		super(rule, entry, ruleType);
	}

	@Override
	public boolean isNodeWithDeadline(CourseNode courseNode) {
		if (courseNode instanceof GTACourseNode) {
			GTACourseNode assessableCourseNode = (GTACourseNode) courseNode;
			ModuleConfiguration config = assessableCourseNode.getModuleConfiguration();
			
			if(AssignTaskRuleSPI.class.getSimpleName().equals(ruleType)) {
				boolean assignment = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
				if(assignment) {
					Date dueDate = config.getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
					int numOfDays = config.getIntegerSafe(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, -1);
					String relativeTo = config.getStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO);
					if(dueDate != null) {
						return true;
					} else if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
						return true;
					}
				}
			} else if(SubmissionTaskRuleSPI.class.getSimpleName().equals(ruleType)) {
				boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
				if(submit) {
					Date dueDate = config.getDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE);
					int numOfDays = config.getIntegerSafe(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE, -1);
					String relativeTo = config.getStringValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO);
					if(dueDate != null) {
						return true;
					} else if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}