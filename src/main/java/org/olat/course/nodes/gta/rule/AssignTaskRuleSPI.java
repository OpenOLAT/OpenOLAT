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

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.BeforeDateTaskRuleEditor;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssignTaskRuleSPI extends AbstractDueDateTaskRuleSPI {

	@Override
	public String getLabelI18nKey() {
		return "rule.assign.task";
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new BeforeDateTaskRuleEditor(rule, entry, AssignTaskRuleSPI.class.getSimpleName());
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		//node ident must be the same
		return rule.clone();
	}

	@Override
	protected Date getDueDate(GTACourseNode gtaNode) {
		Date dueDate = null;
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		boolean assignment = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
		if(assignment) {
			dueDate = config.getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		}
		return dueDate;
	}

	@Override
	protected List<Identity> evaluateRelativeDateRule(RepositoryEntry entry, GTACourseNode gtaNode, ReminderRuleImpl rule) {
		List<Identity> identities = null;
		int numOfDays = gtaNode.getModuleConfiguration().getIntegerSafe(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, -1);
		String relativeTo = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO);
		if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
			identities = getPeopleToRemindRelativeTo(entry, gtaNode, numOfDays, relativeTo, rule);
		}
		return identities;
	}
}
