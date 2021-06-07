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
package org.olat.modules.reminder.rule;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.modules.reminder.ui.UserPropertyEditor;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserPropertyRuleSPI implements RuleSPI {
	
	public static final String USER_PROPS_ID = UserPropertyRuleSPI.class.getName();
	
	@Autowired
	private UserManager userManager;

	@Override
	public int getSortValue() {
		return 102;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.user.property";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(ReminderAdminController.class, locale);
			translator = Util.createPackageTranslator(UserPropertyHandler.class, locale);
			translator = Util.createPackageTranslator(UserPropertyEditor.class, locale, translator);
			String currentPropertyName = r.getLeftOperand();
			String currentPropertyValue = r.getRightOperand();
			
			UserPropertyHandler propertyHandler = null;
			for (UserPropertyHandler userPropertyHandler : userManager.getUserPropertyHandlersFor(USER_PROPS_ID, false)) {
				if (currentPropertyName.equals(userPropertyHandler.getName())) {
					propertyHandler = userPropertyHandler;
				}
			}
			
			String propertyName = propertyHandler != null
					? translator.translate(propertyHandler.i18nFormElementLabelKey())
					: translator.translate("missing.value");
			String[] args = new String[] { propertyName, currentPropertyValue };
			return translator.translate("rule.user.property.text", args);
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new UserPropertyEditor(rule);
	}

	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	public boolean accept(ReminderRule rule, Identity identity) {
		boolean allOk = false;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String propertyName = r.getLeftOperand();
			String reference = r.getRightOperand();
			String value = identity.getUser().getProperty(propertyName, null);
			if(reference != null && reference.equalsIgnoreCase(value)) {
				allOk = true;
			}
		}
		return allOk;
	}
}
