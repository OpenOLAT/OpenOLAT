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
package org.olat.course.nodes.form.ui;

import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.reminder.ui.BeforeDueDateRuleEditor;
import org.olat.modules.reminder.ReminderRule;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 30 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormBeforeDueDateRuleEditor extends BeforeDueDateRuleEditor {

	public FormBeforeDueDateRuleEditor(ReminderRule rule, RepositoryEntry entry, String ruleType) {
		super(rule, entry, ruleType);
	}

	@Override
	protected DueDateConfig getDueDateConfig(CourseNode courseNode) {
		if (courseNode instanceof FormCourseNode) {
			FormCourseNode formCourseNode = (FormCourseNode)courseNode;
			return formCourseNode.getDueDateConfig(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE);
		}
		return DueDateConfig.noDueDateConfig();
	}

}
