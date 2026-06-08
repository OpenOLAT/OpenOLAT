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
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

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

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionEditStatusController extends FormBasicController implements PositionEditableController {

	private SingleSelection statusElement;
	private FormLayoutContainer statusContainer;

	private TextElement deadlineDayElement;
	private TextElement reminderDayElement;
	private TextElement ratingDeadlineDayElement;
	private SingleSelection deadlineMonthElement;
	private SingleSelection reminderMonthElement;
	private SingleSelection ratingDeadlineMonthElement;
	private TextElement deadlineYearElement;
	private TextElement reminderYearElement;
	private TextElement ratingDeadlineYearElement;
	private TextElement ratingDeadlineTimeElement;
	private MultipleSelectionElement reminderEnableElement;
	private MultipleSelectionElement advertiseElement;
	private FormLayoutContainer deadlineContainer;
	private FormLayoutContainer reminderContainer;
	private FormLayoutContainer ratingDeadlineContainer;

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
	

	private static final String[] docKeys = new String[]{"available","mandatory"};
	private final String[] docValues = new String[docKeys.length];

	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private final String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	public PositionEditStatusController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;

		docValues[0] = translate("document.available");
		docValues[1] = translate("document.mandatory");
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		
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
		String pageDeadline = velocity_root + "/edit_deadline.html";
		deadlineContainer = FormLayoutContainer.createCustomFormLayout("deadline", getTranslator(), pageDeadline);
		deadlineContainer.setRootForm(mainForm);
		deadlineContainer.setLabel("edit.deadline", null);
		formLayout.add(deadlineContainer);
		
		String day = "";
		String month= "0";
		String year = "";
		Date birthday = position.getApplicationDeadline();
		if(birthday != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(birthday);
			day = Integer.toString(cal.get(Calendar.DATE));
			month = Integer.toString(cal.get(Calendar.MONTH));
			year = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		deadlineDayElement = uifactory.addTextElement("deadline.day", "", 2, day, deadlineContainer);
		deadlineDayElement.setDomReplacementWrapperRequired(false);
		deadlineDayElement.setDisplaySize(2);
		deadlineDayElement.setMandatory(true);
		deadlineDayElement.setEnabled(!readOnly);
		
		deadlineMonthElement = uifactory.addDropdownSingleselect("deadline.month", "", deadlineContainer, monthKeys, monthValues, null);
		deadlineMonthElement.setDomReplacementWrapperRequired(false);
		deadlineMonthElement.setMandatory(true);
		deadlineMonthElement.select(month, true);
		deadlineMonthElement.setEnabled(!readOnly);
		
		deadlineYearElement = uifactory.addTextElement("deadline.year", "", 4, year, deadlineContainer);
		deadlineYearElement.setDomReplacementWrapperRequired(false);
		deadlineYearElement.setDisplaySize(4);
		deadlineYearElement.setMandatory(true);
		deadlineYearElement.setEnabled(!readOnly);
	}
	
	private void initRatingDeadlineForm(FormItemContainer formLayout) {
		String pageRatingDeadline = velocity_root + "/edit_rating_deadline.html";
		ratingDeadlineContainer = FormLayoutContainer.createCustomFormLayout("rating.deadline", getTranslator(), pageRatingDeadline);
		ratingDeadlineContainer.setRootForm(mainForm);
		ratingDeadlineContainer.setLabel("edit.rating.deadline", null);
		formLayout.add(ratingDeadlineContainer);
		
		String ratingDay = "";
		String ratingMonth= "0";
		String ratingYear = "";
		String ratingTime = "";
		Date ratingDate = position.getRatingDeadline();
		if(ratingDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(ratingDate);
			ratingDay = Integer.toString(cal.get(Calendar.DATE));
			ratingMonth = Integer.toString(cal.get(Calendar.MONTH));
			ratingYear = Integer.toString(cal.get(Calendar.YEAR));
			if(cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) == 59) {
				ratingTime = "";//short cut
			} else {
				ratingTime = Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + ":" + Integer.toString(cal.get(Calendar.MINUTE));
			}
		}
		
		ratingDeadlineDayElement = uifactory.addTextElement("rating.deadline.day", "", 2, ratingDay, ratingDeadlineContainer);
		ratingDeadlineDayElement.setElementCssClass("o_sel_rating_day");
		ratingDeadlineDayElement.setDomReplacementWrapperRequired(false);
		ratingDeadlineDayElement.setDisplaySize(2);
		ratingDeadlineDayElement.setMandatory(true);
		ratingDeadlineDayElement.setEnabled(!readOnly);
		
		ratingDeadlineMonthElement = uifactory.addDropdownSingleselect("rating.deadline.month", "", ratingDeadlineContainer, monthKeys, monthValues, null);
		ratingDeadlineMonthElement.setElementCssClass("o_sel_rating_month");
		ratingDeadlineMonthElement.setDomReplacementWrapperRequired(false);
		ratingDeadlineMonthElement.setMandatory(true);
		ratingDeadlineMonthElement.select(ratingMonth, true);
		ratingDeadlineMonthElement.setEnabled(!readOnly);
		
		ratingDeadlineYearElement = uifactory.addTextElement("rating.deadline.year", "", 4, ratingYear, ratingDeadlineContainer);
		ratingDeadlineYearElement.setElementCssClass("o_sel_rating_year");
		ratingDeadlineYearElement.setDomReplacementWrapperRequired(false);
		ratingDeadlineYearElement.setDisplaySize(4);
		ratingDeadlineYearElement.setMandatory(true);
		ratingDeadlineYearElement.setEnabled(!readOnly);
		
		ratingDeadlineTimeElement = uifactory.addTextElement("rating.deadline.time", "", 5, ratingTime, ratingDeadlineContainer);
		ratingDeadlineTimeElement.setDomReplacementWrapperRequired(false);
		ratingDeadlineTimeElement.setDisplaySize(5);
		ratingDeadlineTimeElement.setMandatory(true);
		ratingDeadlineTimeElement.setEnabled(!readOnly);
		
		updateSummerTime();
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

		String pageRatingDeadline = velocity_root + "/edit_reminder_date.html";
		reminderContainer = FormLayoutContainer.createCustomFormLayout("reminder.date", getTranslator(), pageRatingDeadline);
		reminderContainer.setRootForm(mainForm);
		reminderContainer.setLabel(null, null);
		reminderContainer.setVisible(reminderEnableElement.isAtLeastSelected(1));
		formLayout.add(reminderContainer);
		
		String ratingDay = "";
		String ratingMonth= "0";
		String ratingYear = "";
		if(reminderDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(reminderDate);
			ratingDay = Integer.toString(cal.get(Calendar.DATE));
			ratingMonth = Integer.toString(cal.get(Calendar.MONTH));
			ratingYear = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		reminderDayElement = uifactory.addTextElement("reminder.date.day", "", 2, ratingDay, reminderContainer);
		reminderDayElement.setDomReplacementWrapperRequired(false);
		reminderDayElement.setDisplaySize(2);
		reminderDayElement.setMandatory(true);
		reminderDayElement.setEnabled(!readOnly);
		
		reminderMonthElement = uifactory.addDropdownSingleselect("reminder.date.month", "", reminderContainer, monthKeys, monthValues, null);
		reminderMonthElement.setDomReplacementWrapperRequired(false);
		reminderMonthElement.setMandatory(true);
		reminderMonthElement.select(ratingMonth, true);
		reminderMonthElement.setEnabled(!readOnly);
		
		reminderYearElement = uifactory.addTextElement("reminder.date.year", "", 4, ratingYear, reminderContainer);
		reminderYearElement.setDomReplacementWrapperRequired(false);
		reminderYearElement.setDisplaySize(4);
		reminderYearElement.setMandatory(true);
		reminderYearElement.setEnabled(!readOnly);
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

		allOk &= validateDeadline();
		allOk &= validateRatingDeadline();
		if(reminderEnableElement.isSelected(0)) {
			allOk &= validateReminder();
		}

		return allOk;
	}
	
	private boolean validateDeadline() {
		return validateYearMonthDay(deadlineYearElement, deadlineMonthElement, deadlineDayElement,
				getDeadline(), null, deadlineContainer, false);
	}
	
	private boolean validateRatingDeadline() {
		return validateYearMonthDay(ratingDeadlineYearElement, ratingDeadlineMonthElement, ratingDeadlineDayElement,
				getRatingDeadline(), ratingDeadlineTimeElement, ratingDeadlineContainer, false);
	}
	
	private boolean validateReminder() {
		return validateYearMonthDay(reminderYearElement, reminderMonthElement, reminderDayElement,
				getCommitteeReminderDate(), null, reminderContainer, reminderEnableElement.isSelected(0));
	}
	
	private boolean validateYearMonthDay(TextElement yearEl, SingleSelection monthEl, TextElement dayEl,
			Date deadline, TextElement timeEl, FormLayoutContainer container, boolean mandatory) {
		boolean allOk = true;
		container.clearError();
		int month = -1;
		try {
			String monthStr = monthEl.getSelectedKey();
			monthEl.clearError();
			if(StringHelper.containsNonWhitespace(monthStr)) {
				month = Integer.parseInt(monthStr);
				if(month < 0 || month > 11) {
					allOk =false;
					container.setErrorKey("deadline.error");
				}
			}
		} catch (NumberFormatException e) {
			allOk = false;
			container.setErrorKey("deadline.error");
		}
		int year = -1;
		try {
			String yearStr = yearEl.getValue();
			yearEl.clearError();
			if(StringHelper.containsNonWhitespace(yearStr)) {
				year = Integer.parseInt(yearStr);
				if(year < 2010) {
					allOk = false;
					container.setErrorKey("deadline.error");
				}
			} else if(mandatory) {
				container.setErrorKey("form.legende.mandatory");
			}
		} catch (NumberFormatException e) {
			allOk = false;
			container.setErrorKey("deadline.error");
		}
		
		
		int day = -1;
		try {
			String dayStr = dayEl.getValue();
			dayEl.clearError();
			if(StringHelper.containsNonWhitespace(dayStr)) {
				day = Integer.parseInt(dayStr);
				
				int maxDay = 31;
				if(month >= 0 && year >= 2010) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(getDeadline(1, month, year, 0, 0));
					maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				}
				if(day < 1 || day > maxDay) {
					allOk = false;
					container.setErrorKey("deadline.error");
				}
				if(deadline == null) {
					allOk = false;
					container.setErrorKey("deadline.error");
				}
			} else if(mandatory) {
				container.setErrorKey("form.legende.mandatory");
			}
		} catch (NumberFormatException e) {
			allOk = false;
			container.setErrorKey("deadline.error");
		}
		
		if(timeEl != null) {
			try {
				String timeStr = timeEl.getValue();
				timeEl.clearError();
				if(StringHelper.containsNonWhitespace(timeStr)) {
					String[] timeArr = splitTime(timeStr);
					if(timeArr == null || timeArr.length != 2) {
						allOk &= false;
						container.setErrorKey("deadline.error");
					} else {
						int hh = Integer.parseInt(timeArr[0]);
						int mm = Integer.parseInt(timeArr[1]);
						if(hh < 0 && hh > 23) {
							allOk = false;
							container.setErrorKey("deadline.error");
						}
						if(mm < 0 && mm > 59) {
							allOk = false;
							container.setErrorKey("deadline.error");
						}
					}
				}
			} catch (NumberFormatException e) {
				allOk = false;
				container.setErrorKey("deadline.error");
			}
		}
		
		if((year == -1 && (day != -1)) || (year != -1 && day == -1)) {
			allOk = false;
			container.setErrorKey("deadline.error");
		}

		return allOk;
	}
	
	private Date getCommitteeReminderDate() {
		String dayStr = reminderDayElement.getValue();
		String monthStr = reminderMonthElement.getSelectedKey();
		String yearStr = reminderYearElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getDeadline(day, month, year, 0, 0);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getDeadline() {
		String dayStr = deadlineDayElement.getValue();
		String monthStr = deadlineMonthElement.getSelectedKey();
		String yearStr = deadlineYearElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getDeadline(day, month, year, 0, 0);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getDeadline(int day, int month, int year, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	private Date getRatingDeadline() {
		String dayStr = ratingDeadlineDayElement.getValue();
		String monthStr = ratingDeadlineMonthElement.getSelectedKey();
		String yearStr = ratingDeadlineYearElement.getValue();
		String timeStr = ratingDeadlineTimeElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			int hour = 0;
			int minute = 0;
			if(StringHelper.containsNonWhitespace(timeStr)) {
				String[] timeArr = splitTime(timeStr);
				if(timeArr != null) {
					String hourStr = timeArr[0];
					String minuteStr = timeArr[1];
					hour = Integer.parseInt(hourStr);
					minute = Integer.parseInt(minuteStr);
				}
			} else {
				hour = 23;
				minute = 59;
			}
			return getDeadline(day, month, year, hour, minute);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private String[] splitTime(String time) {
		String[] timeArr = time.split("[:.]");
		if(timeArr.length == 2) {
			return timeArr;
		}
		return null;
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
		position.setApplicationDeadline(getDeadline());
		if(reminderEnableElement.isAtLeastSelected(1)) {
			position.setCommitteeReminderDate(getCommitteeReminderDate());
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
			reminderContainer.setVisible(reminderEnableElement.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateSummerTime() {
		Date date = getRatingDeadline();
		String dst = "CET";
		if(date != null) {
			dst = RecruitingHelper.isSummerTime(date) ? "CEST" : "CET";
		}
		ratingDeadlineContainer.getFormItemComponent().contextPut("dst", dst);
	}
}