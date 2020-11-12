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
package org.olat.modules.grading.ui;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.grading.GradingAssessedIdentityVisibility;
import org.olat.modules.grading.GradingNotificationType;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.ui.component.GraderMailTemplate;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingRepositoryEntryConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private static final String[] visibilityKeys = new String[] {
			GradingAssessedIdentityVisibility.anonymous.name(), GradingAssessedIdentityVisibility.nameVisible.name()
		};
	private static final String[] notificationTypeKeys = new String[] {
			GradingNotificationType.afterTestSubmission.name(), GradingNotificationType.onceDay.name()
		};

	private MultipleSelectionElement enableEl;
	private SingleSelection identityVisibilityEl;
	private SingleSelection notificationTypeEl;
	private TextElement gradingPeriodEl;
	private TextElement notificationSubjectEl;
	private TextElement notificationBodyEl;
	
	private TextElement firstReminderPeriodEl;
	private TextElement firstReminderSubjectEl;
	private TextElement firstReminderBodyEl;
	
	private TextElement secondReminderPeriodEl;
	private TextElement secondReminderSubjectEl;
	private TextElement secondReminderBodyEl;
	
	private DropdownItem templatesDropdownEl;
	private SpacerElement spacerReminderEl;
	private SpacerElement spacerNotificationsEl;
	
	private final RepositoryEntry entry;
	private RepositoryEntryGradingConfiguration configuration;
	
	private Integer gradingPeriod;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private GradingService gradingService;
	
	public GradingRepositoryEntryConfigurationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, RepositoryEntryGradingConfiguration configuration) {
		super(ureq, wControl);
		this.entry = entry;
		this.configuration = configuration;
		gradingPeriod = configuration.getGradingPeriod();
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_repo_grading_settings_form");

		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("grading.repo.enabled", formLayout, onKeys, onValues);
		enableEl.setElementCssClass("o_sel_repo_grading_enable");
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(configuration.isGradingEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		String[] visibilityValues = new String[] {
				translate("configuration.assessed.identity.anonyme"), translate("configuration.assessed.identity.name.visible")
			};
		identityVisibilityEl = uifactory.addRadiosHorizontal("anonymous", "configuration.assessed.identity.visibility", formLayout,
				visibilityKeys, visibilityValues);
		identityVisibilityEl.setElementCssClass("o_sel_repo_grading_visibility");
		identityVisibilityEl.select(configuration.getIdentityVisibilityEnum().name(), true);
		
		String[] notificationTypeValues = new String[] {
				translate("configuration.notification.afterTestSubmission"), translate("configuration.notification.onceDay")
			};
		notificationTypeEl = uifactory.addRadiosHorizontal("notificationType", "configuration.notification.type", formLayout,
				notificationTypeKeys, notificationTypeValues);
		notificationTypeEl.setElementCssClass("o_sel_repo_grading_notification_type");
		notificationTypeEl.select(configuration.getNotificationTypeEnum().name(), true);
		
		String period = configuration.getGradingPeriod() == null ? null : configuration.getGradingPeriod().toString();
		gradingPeriodEl = uifactory.addTextElement("configuration.grading.period", 6, period, formLayout);
		initWorkingDays(gradingPeriodEl, "o_sel_repo_grading_period");
		
		notificationSubjectEl = uifactory.addTextElement("configuration.notification.subject", 255, configuration.getNotificationSubject(), formLayout);
		notificationSubjectEl.setElementCssClass("o_sel_repo_grading_notification_subject");
		notificationBodyEl = uifactory.addTextAreaElement("configuration.notification.body", 4, 60, configuration.getNotificationBody(), formLayout);
		notificationBodyEl.setElementCssClass("o_sel_repo_grading_notification_body");
		MailHelper.setVariableNamesAsHelp(notificationBodyEl, GraderMailTemplate.variableNames(), getLocale());
		
		spacerNotificationsEl = uifactory.addSpacerElement("spacer-notification", formLayout, false);
		
		String firstReminder = configuration.getFirstReminder() == null ? null : configuration.getFirstReminder().toString();
		firstReminderPeriodEl = uifactory.addTextElement("configuration.first.reminder.period", 6, firstReminder, formLayout);
		initWorkingDays(firstReminderPeriodEl, "o_sel_repo_grading_first_reminder_period");
		firstReminderSubjectEl = uifactory.addTextElement("configuration.first.reminder.subject", 255, configuration.getFirstReminderSubject(), formLayout);
		firstReminderBodyEl = uifactory.addTextAreaElement("configuration.first.reminder.body", 4, 60, configuration.getFirstReminderBody(), formLayout);
		MailHelper.setVariableNamesAsHelp(firstReminderBodyEl, GraderMailTemplate.variableNames(), getLocale());

		spacerReminderEl = uifactory.addSpacerElement("spacer-reminder", formLayout, false);
		
		String secondReminder = configuration.getSecondReminder() == null ? null : configuration.getSecondReminder().toString();
		secondReminderPeriodEl = uifactory.addTextElement("configuration.second.reminder.period", 6, secondReminder, formLayout);
		secondReminderPeriodEl.setElementCssClass("");
		
		initWorkingDays(secondReminderPeriodEl, "o_sel_repo_grading_second_reminder_period");
		secondReminderSubjectEl = uifactory.addTextElement("configuration.second.reminder.subject", 255, configuration.getSecondReminderSubject(), formLayout);
		secondReminderBodyEl = uifactory.addTextAreaElement("configuration.second.reminder.body", 4, 60, configuration.getSecondReminderBody(), formLayout);
		MailHelper.setVariableNamesAsHelp(secondReminderBodyEl, GraderMailTemplate.variableNames(), getLocale());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		
		templatesDropdownEl = uifactory.addDropdownMenu("choose.template.language", null, buttonsCont, getTranslator());
		templatesDropdownEl.setElementCssClass("o_sel_repo_grading_templates");
		templatesDropdownEl.setOrientation(DropdownOrientation.right);
		templatesDropdownEl.setEmbbeded(true);
		
		String dummyPage = velocity_root + "empty.html";
		FormLayoutContainer dummyCont = FormLayoutContainer.createCustomFormLayout("dummy", getTranslator(), dummyPage);
		dummyCont.setRootForm(mainForm);
		
		Collection<String> enabledKeys = i18nModule.getEnabledLanguageKeys();
		for (String key : enabledKeys) {
			Locale locale = i18nManager.getLocaleOrNull(key);
			if(locale != null) {
				String label = locale.getDisplayLanguage(getLocale());
				FormLink languageLink = uifactory.addFormLink("use.".concat(key), "choose.template", label, null, dummyCont, Link.LINK | Link.NONTRANSLATED);
				languageLink.setUserObject(locale);
				templatesDropdownEl.addElement(languageLink);
			}
		}
	}
	
	private void initWorkingDays(TextElement textEl, String elementCss) {
		textEl.setDisplaySize(6);
		textEl.setMaxLength(6);
		textEl.setElementCssClass("form-inline ".concat(elementCss));
		textEl.setTextAddOn("working.days");
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!identityVisibilityEl.isOneSelected()) {
			identityVisibilityEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		AtomicLong period = new AtomicLong(0l);
		allOk &= validateWorkingsDays(firstReminderPeriodEl, period);
		allOk &= validateWorkingsDays(secondReminderPeriodEl, period);
		allOk &= validateWorkingsDays(gradingPeriodEl, period);
		
		allOk &= validateSubjectBody(notificationSubjectEl, notificationBodyEl, gradingPeriodEl);
		allOk &= validateSubjectBody(firstReminderSubjectEl, firstReminderBodyEl, firstReminderPeriodEl);
		allOk &= validateSubjectBody(secondReminderSubjectEl, secondReminderBodyEl, secondReminderPeriodEl);
		
		return allOk;
	}
	
	private boolean validateWorkingsDays(TextElement el, AtomicLong period) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				int value = Integer.parseInt(el.getValue());
				if(value <= period.intValue()) {
					allOk &= false;
					el.setErrorKey("error.working.days", new String[] { period.toString() });
				}  else {
					period.set(value);
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateSubjectBody(TextElement subjectEl, TextElement bodyEl, TextElement periodEl) {
		boolean allOk = true;
		
		subjectEl.clearError();
		bodyEl.clearError();
		if(StringHelper.containsNonWhitespace(periodEl.getValue())) {
			if(!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
				subjectEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			if(!StringHelper.containsNonWhitespace(bodyEl.getValue())) {
				bodyEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateUI();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("choose.template".equals(link.getCmd())) {
				doChooseTemplateLanguage((Locale)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		identityVisibilityEl.setVisible(enabled);
		notificationTypeEl.setVisible(enabled);
		gradingPeriodEl.setVisible(enabled);
		notificationSubjectEl.setVisible(enabled);
		notificationBodyEl.setVisible(enabled);
		firstReminderPeriodEl.setVisible(enabled);
		firstReminderSubjectEl.setVisible(enabled);
		firstReminderBodyEl.setVisible(enabled);
		secondReminderPeriodEl.setVisible(enabled);
		secondReminderSubjectEl.setVisible(enabled);
		secondReminderBodyEl.setVisible(enabled);
		spacerReminderEl.setVisible(enabled);
		spacerNotificationsEl.setVisible(enabled);
		templatesDropdownEl.setVisible(enabled);
	}
	
	private void doChooseTemplateLanguage(Locale locale) {
		Translator templateTranslator = Util.createPackageTranslator(GradingRepositoryEntryConfigurationController.class, locale);
		
		notificationSubjectEl.setValue(templateTranslator.translate("mail.notification.subject"));
		notificationBodyEl.setValue(templateTranslator.translate("mail.notification.body"));
		firstReminderSubjectEl.setValue(templateTranslator.translate("mail.reminder1.subject"));
		firstReminderBodyEl.setValue(templateTranslator.translate("mail.reminder1.body"));
		secondReminderSubjectEl.setValue(templateTranslator.translate("mail.reminder2.subject"));
		secondReminderBodyEl.setValue(templateTranslator.translate("mail.reminder2.body"));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		configuration = gradingService.getOrCreateConfiguration(entry);
		
		boolean enabled = enableEl.isAtLeastSelected(1);
		configuration.setGradingEnabled(enabled);
		if(enabled) {
			configuration.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.valueOf(identityVisibilityEl.getSelectedKey()));
			configuration.setNotificationTypeEnum(GradingNotificationType.valueOf(notificationTypeEl.getSelectedKey()));
			
			configuration.setGradingPeriod(toInteger(gradingPeriodEl));
			configuration.setNotificationSubject(notificationSubjectEl.getValue());
			configuration.setNotificationBody(notificationBodyEl.getValue());
			
			configuration.setFirstReminder(toInteger(firstReminderPeriodEl));
			configuration.setFirstReminderSubject(firstReminderSubjectEl.getValue());
			configuration.setFirstReminderBody(firstReminderBodyEl.getValue());
			
			configuration.setSecondReminder(toInteger(secondReminderPeriodEl));
			configuration.setSecondReminderSubject(secondReminderSubjectEl.getValue());
			configuration.setSecondReminderBody(secondReminderBodyEl.getValue());
		} else {
			configuration.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.anonymous);
			configuration.setNotificationTypeEnum(GradingNotificationType.afterTestSubmission);
			configuration.setGradingPeriod(null);
			configuration.setNotificationSubject(null);
			configuration.setNotificationBody(null);
			configuration.setFirstReminder(null);
			configuration.setFirstReminderSubject(null);
			configuration.setFirstReminderBody(null);
			configuration.setSecondReminder(null);
			configuration.setSecondReminderSubject(null);
			configuration.setSecondReminderBody(null);
		}
		
		Integer newGradingPeriod = configuration.getGradingPeriod();
		configuration = gradingService.updateConfiguration(configuration);
		dbInstance.commit();
		if((gradingPeriod == null && newGradingPeriod != null)
				|| (gradingPeriod != null && !gradingPeriod.equals(newGradingPeriod))) {
			gradingService.updateDeadline(entry, configuration);
		}
		gradingPeriod = newGradingPeriod;
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private Integer toInteger(TextElement el) {
		if(StringHelper.containsNonWhitespace(el.getValue()) && StringHelper.isLong(el.getValue())) {
			try {
				return Integer.valueOf(el.getValue());
			} catch (NumberFormatException e) {
				logError("Cannot parse: " + el.getValue(), e);
			}
		}
		return null;
	}
}
