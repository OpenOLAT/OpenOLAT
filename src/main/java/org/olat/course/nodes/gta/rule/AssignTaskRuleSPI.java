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

import org.olat.course.duedate.DueDateConfig;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.BeforeDateTaskRuleEditor;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
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
	public int getSortValue() {
		return 1000;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.assign.task";
	}
	
	@Override
	protected String getStaticTextPrefix() {
		return "rule.assignment.";
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
	protected DueDateConfig getDueDateConfig(CourseNode courseNode) {
		if (courseNode instanceof GTACourseNode) {
			GTACourseNode gtaCourseNode = (GTACourseNode)courseNode;
			return gtaCourseNode.getDueDateConfig(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		}
		return DueDateConfig.noDueDateConfig();
	}
	
}
