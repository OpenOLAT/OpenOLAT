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
import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.ui.QualityUIFactory.validateInteger;
import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.quality.generator.ProviderHelper;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.course.CourseProvider;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
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
	private static final String TRIGGER_DAILY = "config.trigger.daily";
	private static final String TRIGGER_KEYS[] = {
			TRIGGER_DATE_BEGIN,
			TRIGGER_DATE_END,
			TRIGGER_DAILY
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
	private MultipleSelectionElement daysOfWeekEl;
	private FormLayoutContainer timeCont;
	private TextElement startHourEl;
	private TextElement startMinuteEl;
	private TextElement durationEl;
	private TextElement invitationDaysEl;
	private TextElement reminder1DaysEl;
	private TextElement reminder2DaysEl;
	private MultipleSelectionElement rolesEl;
	private MultipleSelectionElement educationalTypeEl;
	
	private final QualityGeneratorConfigs configs;
	
	@Autowired
	private TitleCreator titleCreator;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CourseProviderConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
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
		triggerTypeEl.addActionListener(FormEvent.ONCHANGE);
		String triggerType = configs.getValue(CourseProvider.CONFIG_KEY_TRIGGER);
		if (CourseProvider.CONFIG_KEY_TRIGGER_DAILY.equals(triggerType)) {
			triggerTypeEl.select(TRIGGER_DAILY, true);
		} else if (CourseProvider.CONFIG_KEY_TRIGGER_BEGIN.equals(triggerType)) {
			triggerTypeEl.select(TRIGGER_DATE_BEGIN, true);
		} else {
			triggerTypeEl.select(TRIGGER_DATE_END, true);
		}
		
		// due date days
		String dueDateDays = configs.getValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS);
		dueDateDaysEl = uifactory.addTextElement("config.due.date.days", 4, dueDateDays, formLayout);
		dueDateDaysEl.setHelpText(translate("config.due.date.days.help"));
		
		// daily days of week
		DayOfWeek[] dayOfWeeks = DayOfWeek.values();
		SelectionValues dayOfWeekKV = new SelectionValues();
		for (int i = 0; i < dayOfWeeks.length; i++) {
			dayOfWeekKV.add(SelectionValues.entry(dayOfWeeks[i].name(), dayOfWeeks[i].getDisplayName(TextStyle.FULL_STANDALONE, getLocale())));
		}
		daysOfWeekEl = uifactory.addCheckboxesHorizontal("config.days.of.week", formLayout, dayOfWeekKV.keys(), dayOfWeekKV.values());
		String dayOfWeekConfig = configs.getValue(CourseProvider.CONFIG_KEY_DAILY_WEEKDAYS);
		List<DayOfWeek> selectedDaysOfWeek = ProviderHelper.splitDaysOfWeek(dayOfWeekConfig);
		for (DayOfWeek selectedDayOfWeek: selectedDaysOfWeek) {
			daysOfWeekEl.select(selectedDayOfWeek.name(), true);
		}
		
		// daily time
		String timePage = velocity_root + "/time.html";
		timeCont = FormLayoutContainer.createCustomFormLayout("time", getTranslator(), timePage);
		timeCont.setLabel("config.start.time", null);
		formLayout.add(timeCont);
		
		String dailyHour = configs.getValue(CourseProvider.CONFIG_KEY_DAILY_HOUR);
		startHourEl = uifactory.addTextElement("config.start.hour", null, 2, dailyHour, timeCont);
		startHourEl.setDomReplacementWrapperRequired(false);
		startHourEl.setDisplaySize(2);
		String dailyMinute = configs.getValue(CourseProvider.CONFIG_KEY_DAILY_MINUTE);
		startMinuteEl = uifactory.addTextElement("config.start.minute", null, 2, dailyMinute, timeCont);
		startMinuteEl.setDomReplacementWrapperRequired(false);
		startMinuteEl.setDisplaySize(2);

		// duration
		String duration = configs.getValue(CourseProvider.CONFIG_KEY_DURATION_HOURS);
		durationEl = uifactory.addTextElement("config.duration", 6, duration, formLayout);

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
		
		// educational type exclusion
		SelectionValues educationalTypeKV = new SelectionValues();
		repositoryManager.getAllEducationalTypes()
				.forEach(type -> educationalTypeKV.add(entry(
						type.getKey().toString(),
						translate(RepositoyUIFactory.getI18nKey(type)))));
		educationalTypeKV.sort(SelectionValues.VALUE_ASC);
		educationalTypeEl = uifactory.addCheckboxesDropdown("educational.type", "config.educational.type.exclusion",
				formLayout, educationalTypeKV.keys(), educationalTypeKV.values());
		String educationalTypeKeys = configs.getValue(CourseProvider.CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION);
		if (StringHelper.containsNonWhitespace(educationalTypeKeys)) {
			Arrays.stream(educationalTypeKeys.split(CourseProvider.EDUCATIONAL_TYPE_EXCLUSION_DELIMITER))
					.filter(key -> educationalTypeEl.getKeys().contains(key))
					.forEach(key -> educationalTypeEl.select(key, true));
		}
		
		updateUI();
	}

	private void updateUI() {
		String triggerType = triggerTypeEl.isOneSelected()? triggerTypeEl.getSelectedKey(): "";
		
		boolean dueDate = TRIGGER_DATE_BEGIN.equals(triggerType) || TRIGGER_DATE_END.equals(triggerType);
		dueDateDaysEl.setVisible(dueDate);
		
		boolean daily = TRIGGER_DAILY.equals(triggerType);
		daysOfWeekEl.setVisible(daily);
		timeCont.setVisible(daily);
		flc.setDirty(true);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		boolean enabled = !readOnly;
		triggerTypeEl.setEnabled(enabled);
		dueDateDaysEl.setEnabled(enabled);
		daysOfWeekEl.setEnabled(enabled);
		startHourEl.setEnabled(enabled);
		startMinuteEl.setEnabled(enabled);
		titleEl.setEnabled(enabled);
		invitationDaysEl.setEnabled(enabled);
		reminder1DaysEl.setEnabled(enabled);
		reminder2DaysEl.setEnabled(enabled);
		rolesEl.setEnabled(enabled);
		durationEl.setEnabled(enabled);
		educationalTypeEl.setEnabled(enabled);
		flc.setDirty(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == triggerTypeEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= validateIsMandatory(titleEl);
		allOk &= validateIsMandatory(triggerTypeEl);
		allOk &= validateIsMandatory(durationEl) && validateInteger(durationEl, 1, 100000);
		allOk &= validateInteger(invitationDaysEl, 0, 10000);
		allOk &= validateInteger(reminder1DaysEl, 1, 10000);
		allOk &= validateInteger(reminder2DaysEl, 1, 10000);
		allOk &= validateIsMandatory(rolesEl);
		
		String triggerType = triggerTypeEl.isOneSelected()? triggerTypeEl.getSelectedKey(): "";
		switch (triggerType) {
		case TRIGGER_DATE_BEGIN:
		case TRIGGER_DATE_END:
			allOk &= validateIsMandatory(dueDateDaysEl) && validateInteger(dueDateDaysEl, -10000, 10000);
			break;
		case TRIGGER_DAILY:
			allOk &= validateIsMandatory(startHourEl) && validateInteger(startHourEl, 0, 23);
			allOk &= validateIsMandatory(startMinuteEl) && validateInteger(startMinuteEl, 0, 59);
			break;
		default:
			//
		}
		
		return allOk;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		titleEl.clearError();
		triggerTypeEl.clearError();
		dueDateDaysEl.clearError();
		daysOfWeekEl.clearError();
		startHourEl.clearError();
		startMinuteEl.clearError();
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
		
		clearTriggerConfigs();
		String dueDateDays = dueDateDaysEl.getValue();
		String triggerType = triggerTypeEl.isOneSelected()? triggerTypeEl.getSelectedKey(): "";
		switch (triggerType) {
		case TRIGGER_DATE_BEGIN:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_TRIGGER_BEGIN);
			configs.setValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS, dueDateDays);
			break;
		case TRIGGER_DATE_END:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_TRIGGER_END);
			configs.setValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS, dueDateDays);
			break;
		case TRIGGER_DAILY:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_TRIGGER_DAILY);
			List<DayOfWeek> daysOfWeek = daysOfWeekEl.getSelectedKeys().stream().map(DayOfWeek::valueOf).collect(Collectors.toList());
			String daysOfWeekConfig = ProviderHelper.concatDaysOfWeek(daysOfWeek);
			configs.setValue(CourseProvider.CONFIG_KEY_DAILY_WEEKDAYS, daysOfWeekConfig);
			String startHour = startHourEl.getValue();
			configs.setValue(CourseProvider.CONFIG_KEY_DAILY_HOUR, startHour);
			String startMinute = startMinuteEl.getValue();
			configs.setValue(CourseProvider.CONFIG_KEY_DAILY_MINUTE, startMinute);
			break;
		default:
			configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, null);
		}
		
		String duration = durationEl.getValue();
		configs.setValue(CourseProvider.CONFIG_KEY_DURATION_HOURS, duration);
		
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
		
		String educationalTypeKeys = educationalTypeEl.isAtLeastSelected(1)
				? educationalTypeEl.getSelectedKeys().stream()
						.collect(Collectors.joining(CourseProvider.EDUCATIONAL_TYPE_EXCLUSION_DELIMITER))
				: null;
		configs.setValue(CourseProvider.CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION, educationalTypeKeys);
	}

	private void clearTriggerConfigs() {
		configs.setValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS, null);
		configs.setValue(CourseProvider.CONFIG_KEY_DAILY_WEEKDAYS, null);
		configs.setValue(CourseProvider.CONFIG_KEY_DAILY_HOUR, null);
		configs.setValue(CourseProvider.CONFIG_KEY_DAILY_MINUTE, null);
	}

}
