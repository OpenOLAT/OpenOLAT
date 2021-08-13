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
package org.olat.course.reminder.rule;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.reminder.manager.ReminderRuleDAO;
import org.olat.course.reminder.ui.InitialAttemptsRuleEditor;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.AbstractLaunchDateRuleSPI;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InitialAttemptsRuleSPI extends AbstractLaunchDateRuleSPI implements CourseNodeRuleSPI {

	@Autowired
	private ReminderRuleDAO helperDao;
	
	@Override
	public String getLabelI18nKey() {
		return "rule.initial.attempts.date";
	}
	
	@Override
	public int getSortValue() {
		return 300;
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(InitialAttemptsRuleEditor.class, locale);
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
				courseNode = course.getEditorTreeModel().getCourseNode(nodeIdent);
				if (courseNode == null) {
					return null;
				}
			}
			
			String[] args = new String[] { courseNode.getShortTitle(), courseNode.getIdent(), currentValue };
			return translator.translate("rule.initial.attempt.date." + currentUnit, args);
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new InitialAttemptsRuleEditor(rule, entry);
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	protected Map<Long, Date> getLaunchDates(ReminderRule rule, RepositoryEntry entry, List<Identity> identities) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String nodeIdent = r.getLeftOperand();

			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			
			// We use the last attempt date because, we do not know the first attempt date.
			// Since the reminder is sent only once, this should not matter (unless a
			// reminder is explicitly sent multiple times by the admin).
			return helperDao.getLastAttemptsDates(entry, courseNode, identities);
		}
		return null;
	}
	
	@Override
	public String getCourseNodeIdent(ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			return r.getLeftOperand();
		}
		return null;
	}
}
