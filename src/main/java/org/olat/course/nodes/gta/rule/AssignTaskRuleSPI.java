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
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.BeforeDateTaskRuleEditor;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.modules.reminder.ui.ReminderAdminController;
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
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(ReminderAdminController.class, locale);
			translator = Util.createPackageTranslator(BeforeDateTaskRuleEditor.class, locale, translator);
			String currentUnit = r.getRightUnit();
			String currentValue = r.getRightOperand();
			String nodeIdent = r.getLeftOperand();
			
			try {
				LaunchUnit.valueOf(currentUnit);
			} catch (Exception e) {
				return null;
			}
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				return null;
			}
			
			Date dueDate = null;
			boolean assignment = courseNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
			if(assignment) {
				dueDate = courseNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
			}
			
			String deadline = dueDate != null
					? Formatter.getInstance(locale).formatDateAndTime(dueDate)
					: translator.translate("missing.value");
			String[] args = new String[] { courseNode.getShortTitle(), courseNode.getIdent(), currentValue, deadline };
			return translator.translate("rule.assignment." + currentUnit, args);
		}
		return null;
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
