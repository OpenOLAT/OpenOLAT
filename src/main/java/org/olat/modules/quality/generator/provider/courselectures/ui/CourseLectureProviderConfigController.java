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
package org.olat.modules.quality.generator.provider.courselectures.ui;

import static java.util.stream.Collectors.joining;
import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.ui.QualityUIFactory.validateInteger;
import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.courselectures.CourseLecturesFollowUpProvider;
import org.olat.modules.quality.generator.provider.courselectures.CourseLecturesProvider;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseLectureProviderConfigController extends ProviderConfigController {
	
	private static final String ROLES_PREFIX = "config.roles.";
	private static final String SURVEY_START_LAST = "config.survey.last";
	private static final String SURVEY_START_NUMBER = "config.survey.number";
	private static final String SURVEY_START_KEYS[] = {
			SURVEY_START_LAST,
			SURVEY_START_NUMBER
	};
	private static final String ROLES_OWNER_KEY = ROLES_PREFIX + CurriculumRoles.owner.name();
	private static final String ROLES_COACH_KEY = ROLES_PREFIX + CurriculumRoles.coach.name();
	private static final String ROLES_SINGLE_COACH_KEY = ROLES_PREFIX + CourseLecturesProvider.TEACHING_COACH;
	private static final String ROLES_PARTICIPANTS_KEY = ROLES_PREFIX + CurriculumRoles.participant.name();
	private static final String ROLES_KEYS[] = {
			ROLES_OWNER_KEY,
			ROLES_COACH_KEY,
			ROLES_SINGLE_COACH_KEY,
			ROLES_PARTICIPANTS_KEY
	};
	private static final String TOPIC_KEYS[] = {
			CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH,
			CourseLecturesProvider.CONFIG_KEY_TOPIC_COURSE
	};

	private TextElement titleEl;
	private SingleSelection topicEl;
	private TextElement lecturesTotalMinEl;
	private TextElement lecturesTotalMaxEl;
	private SingleSelection surveyLectureStartEl;
	private TextElement surveyLectureEl;
	private TextElement minutesBeforeEndEl;
	private TextElement announcementCoachDaysEl;
	private TextElement invitationDaysEl;
	private TextElement reminder1DaysEl;
	private TextElement reminder2DaysEl;
	private MultipleSelectionElement rolesEl;
	private TextElement durationEl;
	private MultipleSelectionElement educationalTypeEl;
	
	private final QualityGeneratorConfigs configs;

	@Autowired
	private TitleCreator titleCreator;
	@Autowired
	private RepositoryManager repositoryManager;

	public CourseLectureProviderConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.configs = configs;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = configs.getValue(CourseLecturesProvider.CONFIG_KEY_TITLE);
		titleEl = uifactory.addTextElement("config.title", 200, title, formLayout);
		String identifiers = titleCreator.getIdentifiers(Arrays.asList(RepositoryEntry.class, User.class)).stream()
				.collect(Collectors.joining(", "));
		titleEl.setHelpTextKey("config.title.help", new String[] {identifiers} );
		
		// topic
		topicEl = uifactory.addDropdownSingleselect("config.topic", formLayout, TOPIC_KEYS, translateAll(getTranslator(), TOPIC_KEYS));
		String topicKey = configs.getValue(CourseLecturesProvider.CONFIG_KEY_TOPIC);
		if (topicKey != null && Arrays.stream(TOPIC_KEYS).anyMatch(key -> key.equals(topicKey))) {
			topicEl.select(topicKey, true);
		}

		// lectures
		String lecturesTotalMin = configs.getValue(CourseLecturesProvider.CONFIG_KEY_TOTAL_LECTURES_MIN);
		lecturesTotalMinEl = uifactory.addTextElement("config.lectures.total.min", 4, lecturesTotalMin, formLayout);
		
		String lecturesTotalMax = configs.getValue(CourseLecturesProvider.CONFIG_KEY_TOTAL_LECTURES_MAX);
		lecturesTotalMaxEl = uifactory.addTextElement("config.lectures.total.max", 4, lecturesTotalMax, formLayout);
		
		surveyLectureStartEl = uifactory.addDropdownSingleselect("config.survey.start", formLayout, SURVEY_START_KEYS,
				translateAll(getTranslator(), SURVEY_START_KEYS));
		surveyLectureStartEl.addActionListener(FormEvent.ONCHANGE);
		String surveyLecture = configs.getValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE);
		if (CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE_NUMBER.equals(surveyLecture)) {
			surveyLectureStartEl.select(SURVEY_START_NUMBER, true);
		} else {
			surveyLectureStartEl.select(SURVEY_START_LAST, true);
		}
		String surveyLectureNumber = configs.getValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE_NUMBER);
		surveyLectureEl = uifactory.addTextElement("config.survey.lectures", 4, surveyLectureNumber, formLayout);
		
		String minutesBeforeEnd = configs.getValue(CourseLecturesProvider.CONFIG_KEY_MINUTES_BEFORE_END);
		minutesBeforeEndEl = uifactory.addTextElement("config.minutes.before.end", 3, minutesBeforeEnd, formLayout);

		// duration
		String duration = configs.getValue(CourseLecturesProvider.CONFIG_KEY_DURATION_DAYS);
		durationEl = uifactory.addTextElement("config.duration", 4, duration, formLayout);

		// reminders
		String announcementCoachDays = configs.getValue(CourseLecturesProvider.CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS);
		announcementCoachDaysEl = uifactory.addTextElement("config.announcement.coach.days", 4, announcementCoachDays, formLayout);

		String invitationDays = configs.getValue(CourseLecturesProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS);
		invitationDaysEl = uifactory.addTextElement("config.invitation.days", 4, invitationDays, formLayout);

		String reminder1Days = configs.getValue(CourseLecturesProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS);
		reminder1DaysEl = uifactory.addTextElement("config.reminder1.days", 4, reminder1Days, formLayout);

		String reminder2Days = configs.getValue(CourseLecturesProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS);
		reminder2DaysEl = uifactory.addTextElement("config.reminder2.days", 4, reminder2Days, formLayout);

		// roles
		rolesEl = uifactory.addCheckboxesHorizontal("config.roles", formLayout, ROLES_KEYS,
				translateAll(getTranslator(), ROLES_KEYS));
		String concatedRoles = configs.getValue(CourseLecturesProvider.CONFIG_KEY_ROLES);
		if (StringHelper.containsNonWhitespace(concatedRoles)) {
			String[] roles = concatedRoles.split(CourseLecturesProvider.ROLES_DELIMITER);
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
		educationalTypeEl = uifactory.addCheckboxesDropdown("type.exclusion", "config.educational.type.exclusion",
				formLayout, educationalTypeKV.keys(), educationalTypeKV.values());
		String educationalTypeKeys = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION);
		if (StringHelper.containsNonWhitespace(educationalTypeKeys)) {
			Arrays.stream(educationalTypeKeys.split(CourseLecturesFollowUpProvider.EDUCATIONAL_TYPE_EXCLUSION_DELIMITER))
					.filter(key -> educationalTypeEl.getKeys().contains(key))
					.forEach(key -> educationalTypeEl.select(key, true));
		}
		
		updateUI();
	}

	private void updateUI() {
		boolean surveyLectureNumber = surveyLectureStartEl.isOneSelected()
				&& surveyLectureStartEl.getSelectedKey().equals(SURVEY_START_NUMBER);
		surveyLectureEl.setVisible(surveyLectureNumber);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		boolean enabled = !readOnly;
		titleEl.setEnabled(enabled);
		topicEl.setEnabled(enabled);
		lecturesTotalMinEl.setEnabled(enabled);
		lecturesTotalMaxEl.setEnabled(enabled);
		surveyLectureStartEl.setEnabled(enabled);
		surveyLectureEl.setEnabled(enabled);
		minutesBeforeEndEl.setEnabled(enabled);
		announcementCoachDaysEl.setEnabled(enabled);
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
		if (source == surveyLectureStartEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= validateIsMandatory(titleEl);
		allOk &= validateIsMandatory(topicEl);
		allOk &= validateIsMandatory(surveyLectureEl) && validateInteger(surveyLectureEl, 1, 10000);
		allOk &= validateIsMandatory(minutesBeforeEndEl) && validateInteger(minutesBeforeEndEl, 0, 1000);
		allOk &= validateIsMandatory(durationEl) && validateInteger(durationEl, 1, 10000);
		allOk &= validateInteger(announcementCoachDaysEl, 0, 10000);
		allOk &= validateInteger(invitationDaysEl, 0, 10000);
		allOk &= validateInteger(reminder1DaysEl, 1, 10000);
		allOk &= validateInteger(reminder2DaysEl, 1, 10000);
		allOk &= validateIsMandatory(rolesEl);
		
		boolean lecturesTotalOk = validateInteger(lecturesTotalMinEl, 1, 10000);
		lecturesTotalOk &= validateInteger(lecturesTotalMaxEl, 1, 10000);
		if (lecturesTotalOk && lecturesTotalMinEl.isEnabled() && lecturesTotalMaxEl.isEnabled()) {
			String minString = lecturesTotalMinEl.getValue();
			String maxString = lecturesTotalMaxEl.getValue();
			if (StringHelper.containsNonWhitespace(minString) && StringHelper.containsNonWhitespace(maxString)) {
				int min = Integer.parseInt(minString);
				int max = Integer.parseInt(maxString);
				if (min >= max) {
					lecturesTotalMaxEl.setErrorKey("error.lectures.min.higher.max", null);
					lecturesTotalOk = false;
				} 
			}
		}
		allOk &= lecturesTotalOk;
		
		return allOk;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		titleEl.clearError();
		topicEl.clearError();
		lecturesTotalMinEl.clearError();
		lecturesTotalMaxEl.clearError();
		surveyLectureEl.clearError();
		minutesBeforeEndEl.clearError();
		durationEl.clearError();
		announcementCoachDaysEl.clearError();
		invitationDaysEl.clearError();
		reminder1DaysEl.clearError();
		reminder2DaysEl.clearError();
		rolesEl.clearError();
		
		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TITLE, title);
		
		String topicKey = topicEl.isOneSelected()? topicEl.getSelectedKey(): null;
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TOPIC, topicKey);
		
		String lecturesTotalMin = lecturesTotalMinEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TOTAL_LECTURES_MIN, lecturesTotalMin);
		
		String lecturesTotalMax = lecturesTotalMaxEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TOTAL_LECTURES_MAX, lecturesTotalMax);
		
		if (surveyLectureStartEl.isOneSelected()) {
			String selectedKey = surveyLectureStartEl.getSelectedKey();
			if (SURVEY_START_LAST.equals(selectedKey)) {
				configs.setValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE,
						CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE_LAST);
			} else {
				configs.setValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE,
						CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE_NUMBER);
			}
		} else {
			configs.setValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE, null);
		}
		
		String surveyLecture = surveyLectureEl.isVisible()? surveyLectureEl.getValue(): null;
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE_NUMBER, surveyLecture);
		
		String minutesBeforeEnd = minutesBeforeEndEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_MINUTES_BEFORE_END, minutesBeforeEnd);
		
		String duration = durationEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_DURATION_DAYS, duration);

		String announcementCoachDays = announcementCoachDaysEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS, announcementCoachDays);
		
		String invitationDays = invitationDaysEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS, invitationDays);
		
		String reminder1Days = reminder1DaysEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS, reminder1Days);

		String reminder2Days = reminder2DaysEl.getValue();
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS, reminder2Days);
		
		String roles = rolesEl.getSelectedKeys().stream()
				.map(r -> r.substring(ROLES_PREFIX.length()))
				.collect(joining(CourseLecturesProvider.ROLES_DELIMITER));
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_ROLES, roles);
		
		String educationalTypeKeys = educationalTypeEl.isAtLeastSelected(1)
				? educationalTypeEl.getSelectedKeys().stream()
						.collect(Collectors.joining(CourseLecturesFollowUpProvider.EDUCATIONAL_TYPE_EXCLUSION_DELIMITER))
				: null;
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION, educationalTypeKeys);
	}

}
