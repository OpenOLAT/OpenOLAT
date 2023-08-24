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
package org.olat.course.certificate.ui;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificationTimeUnit;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.reminder.rule.NextRecertificationDateSPI;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecertificationOptionsController extends FormBasicController {
	
	private FormToggle enabledEl;
	private TextElement reCertificationTimelapseEl;

	private final boolean editable;
	private final RepositoryEntry entry;
	private RepositoryEntryCertificateConfiguration certificateConfig;
	
	private CloseableModalController cmc;
	private ConfirmDisableRecertificationController confirmDisableCtrl;
	private RecertificationLeadTimeOptionController confirmLeadTimeCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public RecertificationOptionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);

		this.entry = entry;
		this.editable = editable;
		certificateConfig = certificatesManager.getConfiguration(entry);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("recertification.options.description");
		setFormInfoHelp("manual_user/course_create/Course_Settings/#certificate");
		
		enabledEl = uifactory.addToggleButton("enabled.recertification", "enabled.recertification", null, null, formLayout);
		enabledEl.setEnabled(editable);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		if(certificateConfig.isRecertificationLeadTimeEnabled()) {
			enabledEl.toggleOn();
		}

		int leadtime = certificateConfig.getRecertificationLeadTimeInDays();
		reCertificationTimelapseEl = uifactory.addTextElement("recertification.after.days", 6, Integer.toString(leadtime), formLayout);
		reCertificationTimelapseEl.setElementCssClass("form-inline");
		reCertificationTimelapseEl.setEnabled(editable);
		reCertificationTimelapseEl.setTextAddOn("recertification.after.days.addon");

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		reCertificationTimelapseEl.setVisible(enabled);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmLeadTimeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				setLeadTime(ureq, confirmLeadTimeCtrl.getLeadTimeInDays());
			} else if(event == Event.CANCELLED_EVENT) {
				enabledEl.toggleOff();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDisableCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doDisableRecertification(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				enabledEl.toggleOn();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmLeadTimeCtrl);
		removeAsListenerAndDispose(confirmDisableCtrl);
		removeAsListenerAndDispose(cmc);
		confirmLeadTimeCtrl = null;
		confirmDisableCtrl = null;
		cmc = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		certificateConfig = certificatesManager.getConfiguration(entry);

		reCertificationTimelapseEl.clearError();
		if(StringHelper.containsNonWhitespace(reCertificationTimelapseEl.getValue())) {
			try {
				Integer days = Integer.parseInt(reCertificationTimelapseEl.getValue());
				if(days.intValue() < 0) {
					reCertificationTimelapseEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				} else {
					int time = certificateConfig.getValidityTimelapse();
					CertificationTimeUnit timeUnit = certificateConfig.getValidityTimelapseUnit();
					Date nextRecertification = timeUnit.toDate(ureq.getRequestTimestamp(), time);
					nextRecertification = CalendarUtils.endOfDay(nextRecertification);
					long nextRecertificationInDays = DateUtils.countDays(ureq.getRequestTimestamp(), nextRecertification);
					if(days >= nextRecertificationInDays) {
						reCertificationTimelapseEl.setErrorKey("error.recertication.time");
						allOk &= false;
					}
				}
			} catch (NumberFormatException e) {
				logDebug("Wrong format: " + reCertificationTimelapseEl.getValue());
				reCertificationTimelapseEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabledEl == source) {
			if(enabledEl.isOn()) {
				confirmLeadTime(ureq);
			} else {
				confirmDisable(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveConfig();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void confirmDisable(UserRequest ureq) {
		removeAsListenerAndDispose(confirmDisableCtrl);
		confirmDisableCtrl = new ConfirmDisableRecertificationController(ureq, getWindowControl(), entry);
		listenTo(confirmDisableCtrl);
		
		String title = translate("confirm.disable.recertification.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDisableCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDisableRecertification(UserRequest ureq) {
		enabledEl.toggleOff();
		saveConfig();
		updateUI();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void confirmLeadTime(UserRequest ureq) {
		certificateConfig = certificatesManager.getConfiguration(entry);
		
		removeAsListenerAndDispose(confirmLeadTimeCtrl);
		confirmLeadTimeCtrl = new RecertificationLeadTimeOptionController(ureq, getWindowControl(), certificateConfig);
		listenTo(confirmLeadTimeCtrl);
		
		String title = translate("confirm.activate.recertification.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmLeadTimeCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void setLeadTime(UserRequest ureq, int days) {
		enabledEl.toggleOn();
		reCertificationTimelapseEl.setValue(Integer.toString(days));
		
		List<ReminderInfos> reminders = reminderService.getReminderInfos(entry);
		createReminder("reminder.expiration.description", "reminder.expiration.subject", "reminder.expiration.body",
				reminders, 0);
		if(days > 10) {
			createReminder("reminder.recertification.window.repeat.description", "reminder.recertification.window.repeat.subject", "reminder.recertification.window.repeat.body",
					reminders, -10);
		}
		createReminder("reminder.recertification.window.open.description", "reminder.recertification.window.open.subject", "reminder.recertification.window.open.body",
				reminders, -days);
		
		saveConfig();
		updateUI();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	/**
	 * @param reminders The list of reminders
	 * @param days The number of days
	 * @return true if a reminder with the exact same rule already exists
	 */
	private boolean reminderAlreadyExists(List<ReminderInfos> reminders, int days) {
		String daysString = Integer.toString(days);
		for(ReminderInfos reminder:reminders) {
			reminder.getConfiguration();
			String configuration = reminder.getConfiguration();
			if (StringHelper.containsNonWhitespace(configuration)) {
				List<ReminderRule> rules = reminderService.toRules(configuration).getRules();
				if(rules != null && rules.size() == 1 && rules.get(0) instanceof ReminderRuleImpl rule
						&& rule.getType().equals(NextRecertificationDateSPI.class.getSimpleName())
						&& rule.getOperator().endsWith(">")
						&& daysString.equals(rule.getRightOperand())
						&& LaunchUnit.day.name().equals(rule.getRightUnit()) ) {
					return true;
				}
			}
		}
		return false;
	}

	private void createReminder(String i18nDesc, String i18nSubject, String i18nBody,
			List<ReminderInfos> reminders, int days) {
		if(reminderAlreadyExists(reminders, days)) {
			return;
		}

		Reminder  expirationReminder = reminderService.createReminder(entry, getIdentity());
		String[] args = new String[] {
			Integer.toString(Math.abs(days))
		};

		expirationReminder.setDescription(translate(i18nDesc, args));
		expirationReminder.setEmailSubject(translate(i18nSubject, args));
		expirationReminder.setEmailBody(translate(i18nBody, args));
		
		ReminderRules rules = new ReminderRules();
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(NextRecertificationDateSPI.class.getSimpleName());
		rule.setLeftOperand(null);
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(days));
		rule.setRightUnit(LaunchUnit.day.name());
		rules.getRules().add(rule);
		
		String configuration = reminderService.toXML(rules);
		expirationReminder.setConfiguration(configuration);
		reminderService.save(expirationReminder);
	}
	
	private void saveConfig() {
		certificateConfig = certificatesManager.getConfiguration(entry);
		
		certificateConfig.setRecertificationEnabled(enabledEl.isOn());
		if (enabledEl.isOn()) {
			int timelapse = 0;
			if(StringHelper.isLong(reCertificationTimelapseEl.getValue())) {
				timelapse = Math.abs(Integer.parseInt(reCertificationTimelapseEl.getValue()));
			}
			certificateConfig.setRecertificationLeadTimeInDays(timelapse);
			certificateConfig.setRecertificationLeadTimeEnabled(timelapse > 0);
		} else {
			certificateConfig.setRecertificationLeadTimeInDays(0);
			certificateConfig.setRecertificationLeadTimeEnabled(false);
		}

		certificateConfig = certificatesManager.updateConfiguration(certificateConfig);
		dbInstance.commit();
	}
}
