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
package org.olat.modules.quality.generator.provider.course.ui;


import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.ui.QualityUIFactory.validateInteger;
import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.course.CourseProvider;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseProviderConfigController extends ProviderConfigController {
	
	private static final String TRIGGER_DATE_BEGIN = "config.trigger.date.begin";
	private static final String TRIGGER_DATE_END = "config.trigger.date.end";
	private static final String TRIGGER_KEYS[] = {
			TRIGGER_DATE_BEGIN,
			TRIGGER_DATE_END
	};
	private static final String ROLES_PREFIX = "config.roles.";
	private static final String ROLES_OWNER_KEY = ROLES_PREFIX + GroupRoles.owner.name();
	private static final String ROLES_COACH_KEY = ROLES_PREFIX + GroupRoles.coach.name();
	private static final String ROLES_PARTICIPANTS_KEY = ROLES_PREFIX + GroupRoles.participant.name();
	private static final String ROLES_KEYS[] = {
			ROLES_OWNER_KEY,
			ROLES_COACH_KEY,
			ROLES_PARTICIPANTS_KEY
	};

	private TextElement titleEl;
	private SingleSelection triggerTypeEl;
	private TextElement dueDateDaysEl;
	private TextElement durationEl;
	private TextElement invitationDaysEl;
	private TextElement reminder1DaysEl;
	private TextElement reminder2DaysEl;
	private MultipleSelectionElement rolesEl;
	
	private final QualityGeneratorConfigs configs;
	
	@Autowired
	private TitleCreator titleCreator;
	
	public CourseProviderConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.configs = configs;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = configs.getValue(CourseProvider.CONFIG_KEY_TITLE);
		titleEl = uifactory.addTextElement("config.title", 200, title, formLayout);
		String identifiers = titleCreator.getIdentifiers(singletonList(RepositoryEntry.class)).stream()
				.collect(Collectors.joining(", "));
		titleEl.setHelpTextKey("config.title.help", new String[] {identifiers} );
		
		// trigger
		triggerTypeEl = uifactory.addDropdownSingleselect("config.trigger.type", formLayout, TRIGGER_KEYS,
				translateAll(getTranslator(), TRIGGER_KEYS));
		String triggerType = configs.getValue(CourseProvider.CONFIG_KEY_TRIGGER);
		if (StringHelper.containsNonWhitespace(triggerType) && Arrays.stream(TRIGGER_KEYS).anyMatch(key -> key.equals(triggerType))) {
			triggerTypeEl.select(triggerType, true);
		} else {
			triggerTypeEl.select(TRIGGER_DATE_END, true);
		}
		
		String dueDateDays = configs.getValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS);
		dueDateDaysEl = uifactory.addTextElement("config.due.date.days", 4, dueDateDays, formLayout);
		dueDateDaysEl.setHelpText(translate("config.due.date.days.help"));

		// duration
		String duration = configs.getValue(CourseProvider.CONFIG_KEY_DURATION_DAYS);
		durationEl = uifactory.addTextElement("config.duration", 4, duration, formLayout);

		// reminders
		String invitationDays = configs.getValue(CourseProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS);
		invitationDaysEl = uifactory.addTextElement("config.invitation.days", 4, invitationDays, formLayout);

		String reminder1Days = configs.getValue(CourseProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS);
		reminder1DaysEl = uifactory.addTextElement("config.reminder1.days", 4, reminder1Days, formLayout);

		String reminder2Days = configs.getValue(CourseProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS);
		reminder2DaysEl = uifactory.addTextElement("config.reminder2.days", 4, reminder2Days, formLayout);

		// roles
		rolesEl = uifactory.addCheckboxesHorizontal("config.roles", formLayout, ROLES_KEYS,
				translateAll(getTranslator(), ROLES_KEYS));
		String concatedRoles = configs.getValue(CourseProvider.CONFIG_KEY_ROLES);
		if (StringHelper.containsNonWhitespace(concatedRoles)) {
			String[] roles = concatedRoles.split(CourseProvider.ROLES_DELIMITER);
			String[] roleKeys = Arrays.stream(roles).map(r -> ROLES_PREFIX + r).toArray(String[]::new);
			for (String role: roleKeys) {
				rolesEl.select(role, true);
			}
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		boolean enabled = !readOnly;
		triggerTypeEl.setEnabled(enabled);
		dueDateDaysEl.setEnabled(enabled);
		titleEl.setEnabled(enabled);
		invitationDaysEl.setEnabled(enabled);
		reminder1DaysEl.setEnabled(enabled);
		reminder2DaysEl.setEnabled(enabled);
		rolesEl.setEnabled(enabled);
		durationEl.setEnabled(enabled);
		flc.setDirty(true);
	}

	@Override
	public boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= validateIsMandatory(titleEl);
		allOk &= validateIsMandatory(triggerTypeEl);
		allOk &= validateIsMandatory(dueDateDaysEl) && validateInteger(dueDateDaysEl, -10000, 10000);
		allOk &= validateIsMandatory(durationEl) && validateInteger(durationEl, 1, 10000);
		allOk &= validateInteger(invitationDaysEl, 0, 10000);
		allOk &= validateInteger(reminder1DaysEl, 1, 10000);
		allOk &= validateInteger(reminder2DaysEl, 1, 10000);
		allOk &= validateIsMandatory(rolesEl);
		
		return allOk;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		titleEl.clearError();
		triggerTypeEl.clearError();
		dueDateDaysEl.clearError();
		invitationDaysEl.clearError();
		reminder1DaysEl.clearError();
		reminder2DaysEl.clearError();
		rolesEl.clearError();
		
		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_TITLE, title);
		
		String triggerType = triggerTypeEl.isOneSelected()? triggerTypeEl.getSelectedKey(): "";
		switch (triggerType) {
		case TRIGGER_DATE_BEGIN:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_DUE_DATE_BEGIN);
			break;
		case TRIGGER_DATE_END:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_DUE_DATE_END);
			break;
		default:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, null);
		}
		
		String dueDateDays = dueDateDaysEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS, dueDateDays);
		
		String duration = durationEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_DURATION_DAYS, duration);
		
		String invitationDays = invitationDaysEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS, invitationDays);
		
		String reminder1Days = reminder1DaysEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS, reminder1Days);

		String reminder2Days = reminder2DaysEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS, reminder2Days);
		
		String roles = rolesEl.getSelectedKeys().stream()
				.map(r -> r.substring(ROLES_PREFIX.length()))
				.collect(joining(CourseProvider.ROLES_DELIMITER));
		configs.setValue(CourseProvider.CONFIG_KEY_ROLES, roles);
	}

	@Override
	protected void doDispose() {
		//
	}

}
