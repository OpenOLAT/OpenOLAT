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
package org.olat.modules.selectus.ui.position;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PositionEditStatusController extends FormBasicController implements PositionEditableController {

	private DateChooser deadlineEl;
	private DateChooser reminderDateEl;
	private DateChooser ratingDeadlineEl;
	private SingleSelection statusElement;
	private FormLayoutContainer statusContainer;
	private MultipleSelectionElement advertiseElement;
	private MultipleSelectionElement reminderEnableElement;

	private Position position;
	private final boolean readOnly;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditStatusController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description.status");
		formLayout.setElementCssClass("o_sel_edit_position_status_form");
		
		//deadline container
		initDeadlineForm(formLayout);
		//rating deadline
		initRatingDeadlineForm(formLayout);
		//reminder
		initReminderForm(formLayout);
		//status container
		initStatus( formLayout);
		//advertise
		initAdvertisementForm(formLayout);
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setVisible(!readOnly);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void initAdvertisementForm(FormItemContainer formLayout) {
		String[] onKeys = new String[] { "on" };
		String[] onValues = new String[] { "" };
		advertiseElement = uifactory.addCheckboxesHorizontal("edit.advertise.position", "edit.advertise.position", formLayout, onKeys, onValues);
		advertiseElement.setHelpText(translate("edit.advertise.position.hint"));
		if(position.isAdvertised()) {
			advertiseElement.select("on", true);
		}
	}
	
	private void initStatus(FormItemContainer formLayout) {
		//start status container
		String page = velocity_root + "/edit_status.html";
		statusContainer = FormLayoutContainer.createCustomFormLayout("status_sel_cont", getTranslator(), page);
		statusContainer.setRootForm(mainForm);
		statusContainer.setLabel("edit.status", null);
		formLayout.add(statusContainer);
		
		String currentStatus = position.getStatus();
		boolean found = false;

		SelectionValues statusKeysValues = new SelectionValues();
		PositionStatus[] statusList = recruitingModule.getPositionStatus();
		for(PositionStatus s:statusList) {
			statusKeysValues.add(SelectionValues.entry(s.name(), translate("status." + s.name())));
			if(s.name().equals(currentStatus)) {
				found = true;
			}
		}
		
		if(StringHelper.containsNonWhitespace(currentStatus) && !found) {
			statusKeysValues.add(SelectionValues.entry(currentStatus, translate("status." + currentStatus)));
		}
		
		statusElement = uifactory.addDropdownSingleselect("pos_status", "edit.status", statusContainer,
				statusKeysValues.keys(), statusKeysValues.values(), null);
		statusElement.setDomReplacementWrapperRequired(false);
		statusElement.setElementCssClass("o_sel_position_status");
		statusElement.setEnabled(!readOnly);
		if(StringHelper.containsNonWhitespace(position.getStatus())) {
			statusElement.select(position.getStatus(), true);
			statusContainer.contextPut("statusCss", position.getStatus());
		}
		statusElement.addActionListener(FormEvent.ONCHANGE);
	}

	private void initDeadlineForm(FormItemContainer formLayout) {
		Date deadline = position.getApplicationDeadline();
		deadlineEl = uifactory.addDateChooser("edit.deadline", deadline, formLayout);
		deadlineEl.setMandatory(true);
		deadlineEl.setEnabled(!readOnly);
	}
	
	private void initRatingDeadlineForm(FormItemContainer formLayout) {
		Date ratingDeadline = position.getRatingDeadline();
		if(ratingDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(ratingDeadline);
			if(cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) == 59) {
				// Until end of day
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				ratingDeadline = cal.getTime();
			}
		}
		ratingDeadlineEl = uifactory.addDateChooser("edit.rating.deadline", ratingDeadline, formLayout);
		ratingDeadlineEl.setDateChooserTimeEnabled(true);
		ratingDeadlineEl.setElementCssClass("o_sel_rating_deadline");
		ratingDeadlineEl.setEnabled(!readOnly);
		updateSummerTime();

		// Update the CET/CEST hint client side, a form event would redraw the date chooser
		String dstPage = velocity_root + "/edit_rating_deadline_dst.html";
		uifactory.addCustomFormLayout("dst", null, dstPage, formLayout);
	}
	
	private void initReminderForm(FormItemContainer formLayout) {
		Date reminderDate = position.getCommitteeReminderDate();
		
		String[] onKeys = new String[] { "on" };
		String[] onValues = new String[] { "" };
		reminderEnableElement = uifactory.addCheckboxesHorizontal("edit.reminder.date", formLayout, onKeys, onValues);
		reminderEnableElement.addActionListener(FormEvent.ONCHANGE);
		reminderEnableElement.setEnabled(!readOnly);
		if(reminderDate != null) {
			reminderEnableElement.select(onKeys[0], true);
		}

		reminderDateEl = uifactory.addDateChooser("", reminderDate, formLayout);
		reminderDateEl.setMandatory(true);
		reminderDateEl.setEnabled(!readOnly);
		reminderDateEl.setVisible(reminderEnableElement.isAtLeastSelected(1));
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateDate(deadlineEl, false);
		allOk &= validateDate(ratingDeadlineEl, false);
		if(reminderEnableElement.isSelected(0)) {
			allOk &= validateDate(reminderDateEl, true);
		}

		return allOk;
	}
	
	private boolean validateDate(DateChooser dateEl, boolean mandatory) {
		boolean allOk = true;
		
		dateEl.clearError();
		if(mandatory && dateEl.getDate() == null) {
			dateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);
		String beforeStatus = position.getStatus();
		
		position.setRatingDeadline(getRatingDeadline());
		position.setApplicationDeadline(deadlineEl.getDate());
		if(reminderEnableElement.isAtLeastSelected(1)) {
			position.setCommitteeReminderDate(reminderDateEl.getDate());
		} else {
			position.setCommitteeReminderDate(null);
			position.setCommitteeReminderSentDate(null);
		}
		
		boolean changedStatus = !statusElement.getSelectedKey().equals(position.getStatus());
		if(changedStatus) {
			logAudit("Status changed from " + position.getStatus() + " to " + statusElement.getSelectedKey() + " for position: " + position.toString(), null);
		}
		position.setStatus(statusElement.getSelectedKey());
		position.setAdvertised(advertiseElement.isAtLeastSelected(1));
		
		position = recruitingService.savePosition(position);
		dbInstance.commit();
		String after = auditService.toAuditXml(position);
		String afterStatus = position.getStatus();
		
		if(changedStatus) {
			String messageI18n = "audit.log.position.change.status";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()),
					translate("status." + beforeStatus), translate("status." + afterStatus) };
			auditService.auditPositionLog(Action.changeStatus, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		} else if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}

		getLogger().info(Tracing.M_AUDIT, "Update position status: {}", position.toStringFull());
		fireEvent(ureq, doneEvent);
		updateSummerTime();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (statusElement == source) {
			if(statusElement.isOneSelected()) {
				String status = statusElement.getSelectedKey();
				statusContainer.contextPut("statusCss", status);
			}
		} else if(reminderEnableElement == source) {
			reminderDateEl.setVisible(reminderEnableElement.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	/**
	 * @return The rating deadline as it is saved: midnight is a short cut for the end of the day.
	 */
	private Date getRatingDeadline() {
		Date ratingDeadline = ratingDeadlineEl.getDate();
		if(ratingDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(ratingDeadline);
			if(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
				// Until end of day
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				ratingDeadline = cal.getTime();
			}
		}
		return ratingDeadline;
	}

	private void updateSummerTime() {
		Date date = getRatingDeadline();
		String dst = "CET";
		if(date != null) {
			dst = RecruitingHelper.isSummerTime(date) ? "CEST" : "CET";
		}
		ratingDeadlineEl.setTextAddOn(dst, false);
	}
}