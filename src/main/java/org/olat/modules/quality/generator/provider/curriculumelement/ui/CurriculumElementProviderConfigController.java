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
package org.olat.modules.quality.generator.provider.curriculumelement.ui;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_CURRICULUM_ELEMENT_TYPE;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_DUE_DATE_BEGIN;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_DUE_DATE_DAYS;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_DUE_DATE_END;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_DUE_DATE_TYPE;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_DURATION_DAYS;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_ROLES;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.CONFIG_KEY_TITLE;
import static org.olat.modules.quality.generator.provider.curriculumelement.CurriculumElementProvider.ROLES_DELIMITER;
import static org.olat.modules.quality.ui.QualityUIFactory.validateInteger;
import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementProviderConfigController extends ProviderConfigController {

	private static final String DUE_DATE_KEYS[] = {
			CONFIG_KEY_DUE_DATE_BEGIN,
			CONFIG_KEY_DUE_DATE_END
	};
	private static final String ROLES_PREFIX = "config.roles.";
	private static final String ROLES_OWNER_KEY = ROLES_PREFIX + CurriculumRoles.owner.name();
	private static final String ROLES_COACH_KEY = ROLES_PREFIX + CurriculumRoles.coach.name();
	private static final String ROLES_PARTICIPANTS_KEY = ROLES_PREFIX + CurriculumRoles.participant.name();
	private static final String ROLES_KEYS[] = {
			ROLES_OWNER_KEY,
			ROLES_COACH_KEY,
			ROLES_PARTICIPANTS_KEY
	};

	private SingleSelection ceTypeEl;
	private SingleSelection dueDateTypeEl;
	private TextElement dueDateDaysEl;
	private TextElement titleEl;
	private TextElement invitationDaysEl;
	private TextElement reminder1DaysEl;
	private TextElement reminder2DaysEl;
	private MultipleSelectionElement rolesEl;
	private TextElement durationEl;
	
	private final QualityGeneratorConfigs configs;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private TitleCreator titleCreator;

	public CurriculumElementProviderConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.configs = configs;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = configs.getValue(CONFIG_KEY_TITLE);
		titleEl = uifactory.addTextElement("config.title", 200, title, formLayout);
		String identifiers = titleCreator.getIdentifiers(singletonList(CurriculumElement.class)).stream()
				.collect(Collectors.joining(", "));
		titleEl.setHelpTextKey("config.title.help", new String[] {identifiers} );

		// curriculum element type
		List<CurriculumElementType> ceTypes = curriculumService.getCurriculumElementTypes();
		String[] ceTypeKeys = ceTypes.stream().map(CurriculumElementType::getKey).map(String::valueOf).toArray(String[]::new);
		String[] ceTypeValues = ceTypes.stream().map(CurriculumElementType::getDisplayName).toArray(String[]::new);
		ceTypeEl = uifactory.addDropdownSingleselect("config.curriculum.element.type", formLayout, ceTypeKeys, ceTypeValues);
		String ceType = configs.getValue(CONFIG_KEY_CURRICULUM_ELEMENT_TYPE);
		if (ceType != null) {
			if (Arrays.stream(ceTypeKeys).anyMatch(key -> key.equals(ceType))) {
				ceTypeEl.select(ceType, true);
			} else {
				showWarning("error.curriculum.element.not.found");
			}
		}
		
		// dates
		dueDateTypeEl = uifactory.addDropdownSingleselect("config.due.date.type", formLayout, DUE_DATE_KEYS,
				translateAll(getTranslator(), DUE_DATE_KEYS));
		String dueDateType = configs.getValue(CONFIG_KEY_DUE_DATE_TYPE);
		if (StringHelper.containsNonWhitespace(dueDateType) && Arrays.stream(DUE_DATE_KEYS).anyMatch(key -> key.equals(dueDateType))) {
			dueDateTypeEl.select(dueDateType, true);
		}
		
		String daysAfterStart = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
		dueDateDaysEl = uifactory.addTextElement("config.due.date.days", 4, daysAfterStart, formLayout);
		dueDateDaysEl.setHelpText(translate("config.due.date.days.help"));

		// duration
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		durationEl = uifactory.addTextElement("config.duration", 4, duration, formLayout);

		// reminders
		String invitationDays = configs.getValue(CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS);
		invitationDaysEl = uifactory.addTextElement("config.invitation.days", 4, invitationDays, formLayout);

		String reminder1Days = configs.getValue(CONFIG_KEY_REMINDER1_AFTER_DC_DAYS);
		reminder1DaysEl = uifactory.addTextElement("config.reminder1.days", 4, reminder1Days, formLayout);

		String reminder2Days = configs.getValue(CONFIG_KEY_REMINDER2_AFTER_DC_DAYS);
		reminder2DaysEl = uifactory.addTextElement("config.reminder2.days", 4, reminder2Days, formLayout);

		// roles
		rolesEl = uifactory.addCheckboxesHorizontal("config.roles", formLayout, ROLES_KEYS,
				translateAll(getTranslator(), ROLES_KEYS));
		String concatedRoles = configs.getValue(CONFIG_KEY_ROLES);
		if (StringHelper.containsNonWhitespace(concatedRoles)) {
			String[] roles = concatedRoles.split(ROLES_DELIMITER);
			String[] roleKeys = Arrays.stream(roles).map(r -> ROLES_PREFIX + r).toArray(String[]::new);
			for (String role: roleKeys) {
				rolesEl.select(role, true);
			}
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		boolean enabled = !readOnly;
		ceTypeEl.setEnabled(enabled);
		dueDateTypeEl.setEnabled(enabled);
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
		allOk &= validateIsMandatory(ceTypeEl);
		allOk &= validateIsMandatory(dueDateTypeEl);
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
		ceTypeEl.clearError();
		dueDateTypeEl.clearError();
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
		configs.setValue(CONFIG_KEY_TITLE, title);
		
		String ceTypeKey = ceTypeEl.getSelectedKey();
		configs.setValue(CONFIG_KEY_CURRICULUM_ELEMENT_TYPE, ceTypeKey);
		
		String dueDateType = dueDateTypeEl.getSelectedKey();
		configs.setValue(CONFIG_KEY_DUE_DATE_TYPE, dueDateType);
		
		String dueDateDays = dueDateDaysEl.getValue();
		configs.setValue(CONFIG_KEY_DUE_DATE_DAYS, dueDateDays);
		
		String duration = durationEl.getValue();
		configs.setValue(CONFIG_KEY_DURATION_DAYS, duration);
		
		String invitationDays = invitationDaysEl.getValue();
		configs.setValue(CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS, invitationDays);
		
		String reminder1Days = reminder1DaysEl.getValue();
		configs.setValue(CONFIG_KEY_REMINDER1_AFTER_DC_DAYS, reminder1Days);

		String reminder2Days = reminder2DaysEl.getValue();
		configs.setValue(CONFIG_KEY_REMINDER2_AFTER_DC_DAYS, reminder2Days);
		
		String roles = rolesEl.getSelectedKeys().stream()
				.map(r -> r.substring(ROLES_PREFIX.length()))
				.collect(joining(ROLES_DELIMITER));
		configs.setValue(CONFIG_KEY_ROLES, roles);
	}
}
