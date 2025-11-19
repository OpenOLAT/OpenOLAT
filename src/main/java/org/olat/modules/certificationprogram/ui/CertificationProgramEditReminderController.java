/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.Collection;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.component.DurationFormItem;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramEditReminderController extends FormBasicController {
	
	private static final String KEY_UPCOMING = "upcoming";
	private static final String KEY_OVERDUE = "overdue";
	private static final String KEY_CREDIT_BALANCE_TOO_LOW = "balancetoolow";
	
	private TextElement titleEl;
	private DurationFormItem timeEl;
	private SingleSelection recertificationEl;
	private MultipleSelectionElement conditionsEl;
	
	private final CertificationProgram certificationProgram;
	private CertificationProgramMailConfiguration configuration;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramEditReminderController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram, CertificationProgramMailConfiguration configuration) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.configuration = configuration;
		this.certificationProgram = certificationProgram;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = configuration == null ? null : configuration.getTitle();
		titleEl = uifactory.addTextElement("reminder.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		SelectionValues recertificationPK = new SelectionValues();
		recertificationPK.add(SelectionValues.entry(KEY_UPCOMING,
				translate("reminder.recertification.upcoming"), translate("reminder.recertification.upcoming.desc"), null, null, true));
		recertificationPK.add(SelectionValues.entry(KEY_OVERDUE,
				translate("reminder.recertification.overdue"), translate("reminder.recertification.overdue.desc"), null, null, true));
		recertificationEl = uifactory.addCardSingleSelectHorizontal("reminder.recertification", "reminder.recertification", formLayout,
				recertificationPK.keys(), recertificationPK.values(), recertificationPK.descriptions(), null);
		recertificationEl.addActionListener(FormEvent.ONCHANGE);
		if(configuration == null || configuration.getType() == CertificationProgramMailType.reminder_upcoming) {
			recertificationEl.select(KEY_UPCOMING, true);
		} else if(configuration.getType() == CertificationProgramMailType.reminder_overdue) {
			recertificationEl.select(KEY_OVERDUE, true);
		}
		recertificationEl.setEnabled(configuration == null);
		recertificationEl.setVisible(certificationProgram.isRecertificationWindowEnabled());
		
		DurationType timeUnit = configuration == null ? DurationType.week : configuration.getTimeUnit();
		String time = configuration == null ? "1" : Integer.toString(configuration.getTime());
		timeEl = new DurationFormItem("reminder.time", getTranslator(), true);
		timeEl.setLabel("reminder.time", null);
		timeEl.setValue(time, timeUnit);
		formLayout.add(timeEl);
		updateUI();
		
		SelectionValues conditionsPK = new SelectionValues();
		conditionsPK.add(SelectionValues.entry(KEY_CREDIT_BALANCE_TOO_LOW, translate("reminder.conditions.balance.too.low")));
		conditionsEl = uifactory.addCheckboxesVertical("reminder.conditions", formLayout, conditionsPK.keys(), conditionsPK.values(), 1);
		conditionsEl.select(KEY_CREDIT_BALANCE_TOO_LOW, configuration != null && configuration.isCreditBalanceTooLow());
		conditionsEl.setVisible(certificationProgram.getCreditPointSystem() != null && certificationProgram.getCreditPoints() != null);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		boolean overdue = recertificationEl.isOneSelected()
				&& KEY_OVERDUE.equals(recertificationEl.getSelectedKey());
		String i18nAddon = overdue ? "reminder.time.overdue.addon" : "reminder.time.upcoming.addon";
		String i18nHint = overdue ? "reminder.time.overdue.hint" : "reminder.time.upcoming.hint";
		timeEl.setAddOn(translate(i18nAddon));
		
		if(overdue) {
			if(certificationProgram.isRecertificationWindowEnabled()) {
				int time = certificationProgram.getRecertificationWindow();
				String i18nUnit = time <= 1
						? certificationProgram.getRecertificationWindowUnit().i18nSingular()
						: certificationProgram.getRecertificationWindowUnit().i18nPlural();
				timeEl.setExampleKey(i18nHint, new String[] { Integer.toString(time), translate(i18nUnit) });
			} else {
				timeEl.setExampleKey(null, null);
			}
		} else if(certificationProgram.isValidityEnabled()) {
			int time = certificationProgram.getValidityTimelapse();
			String i18nUnit = time <= 1
					? certificationProgram.getValidityTimelapseUnit().i18nSingular()
					: certificationProgram.getValidityTimelapseUnit().i18nPlural();
			timeEl.setExampleKey(i18nHint, new String[] { Integer.toString(time), translate(i18nUnit) });
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(recertificationEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateTextElement(titleEl, 255);
		allOk &= validateDuration(timeEl);
		return allOk;
	}
	
	private boolean validateDuration(DurationFormItem el) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible()) {
			if(el.isOneSelected()) {
				if((el.getValue() != null &&el.getValue().intValue() <= 0)
						|| (el.getType() != null && !StringHelper.isLong(el.getRawValue()))) {
					el.setErrorKey("error.integer.positive");
					allOk &= false;
				} else if(el.getValue() == null || el.getType() == null) {
					el.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			} else {
				el.setErrorKey("error.dropdown.select.one");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement textElement, int lenght) {		
		boolean allOk = true;
		
		textElement.clearError();
		if(StringHelper.containsNonWhitespace(textElement.getValue())) {
			if(textElement.getValue().length() > lenght) {
				textElement.setErrorKey("input.toolong", String.valueOf(lenght));
				allOk &= false;
			}
		} else {
			textElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(configuration == null) {
			CertificationProgramMailType type = recertificationEl.isOneSelected() && KEY_OVERDUE.equals(recertificationEl.getSelectedKey())
					? CertificationProgramMailType.reminder_overdue
					: CertificationProgramMailType.reminder_upcoming;
			configuration = certificationProgramService.createMailConfigurations(certificationProgram, type);
		}
		
		configuration.setTitle(titleEl.getValue());
		Collection<String> selectedConditions = conditionsEl.getSelectedKeys();
		configuration.setCreditBalanceTooLow(conditionsEl.isVisible() && selectedConditions.contains(KEY_CREDIT_BALANCE_TOO_LOW));
		configuration.setTime(timeEl.getValue().intValue());
		configuration.setTimeUnit(timeEl.getType());
		configuration = certificationProgramService.updateMailConfiguration(configuration);
		dbInstance.commit();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
