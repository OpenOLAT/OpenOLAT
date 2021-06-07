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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.reminder.manager.ReminderRuleDAO;
import org.olat.course.reminder.ui.LearningProgressEditor;
import org.olat.modules.reminder.FilterRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LearningProgressRuleSPI implements FilterRuleSPI {

	private static final double ROUND = 0.000001d;
	
	@Autowired
	private ReminderRuleDAO helperDao;
	
	@Override
	public int getSortValue() {
		return 6;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.learning.progress";
	}

	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(LearningProgressEditor.class, locale);
			String operator = r.getOperator();
			String percent = r.getRightOperand();
			switch(operator) {
				case "<": return translator.translate("rule.learning.progress.less", new String[] {percent});
				case "<=": return translator.translate("rule.learning.progress.less.equals", new String[] {percent});
				case "=": return translator.translate("rule.learning.progress.equals", new String[] {percent});
				case "=>": return translator.translate("rule.learning.progress.greater.equals", new String[] {percent});
				case ">": return translator.translate("rule.learning.progress.greater", new String[] {percent});
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled(RepositoryEntry entry) {
		ICourse course = CourseFactory.loadCourse(entry);
		return LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
	}

	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new LearningProgressEditor(rule);
	}

	@Override
	public void filter(RepositoryEntry entry, List<Identity> identities, ReminderRule rule) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String operator = r.getOperator();
			float value = Float.parseFloat(r.getRightOperand()) / 100;
			
			Map<Long, Double> completions = helperDao.getRootCompletions(entry, identities);
			
			for (Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				Double completion = completions.get(identity.getKey());
				if (completion == null) {
					completion = Double.valueOf(0);
				}
				if (!evaluateCompletion(completion.floatValue(), operator, value)) {
					identityIt.remove();
				}
			}
		}
	}

	private boolean evaluateCompletion(float completion, String operator, float value) {
		boolean eval = false;
		switch(operator) {
			case "<": eval = completion < value; break;
			case "<=": eval = Math.abs(completion - value) < ROUND || completion <= value; break;
			case "=": eval = Math.abs(completion - value) < ROUND; break;
			case "=>": eval = Math.abs(completion - value) < ROUND || completion >= value; break;
			case ">": eval = completion > value;  break;
		}
		return eval;
	}

}
