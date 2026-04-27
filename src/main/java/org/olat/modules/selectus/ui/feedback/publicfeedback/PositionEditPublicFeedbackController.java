/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditableController;

/**
 * 
 * Initial date: 27 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditPublicFeedbackController extends FormBasicController implements PositionEditableController {

	private static final String[] enableKeys = new String[]{ "on" };
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

	private MultipleSelectionElement enableFeedbackEl;
	private TextElement feedbackDeadlineDayElement;
	private SingleSelection feedbackDeadlineMonthElement;
	private TextElement feedbackDeadlineYearElement;
	private FormLayoutContainer feedbackDeadlineContainer;
	
	private Position position;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionEditPublicFeedbackController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		initForm(ureq);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description.public.feedback");
		formLayout.setElementCssClass("o_sel_edit_position_public_feedback_form");
		
		String[] enableValues = new String[]{ translate("enable") };
		
		enableFeedbackEl = uifactory.addCheckboxesHorizontal("edit.public.feedback.enable", formLayout, enableKeys, enableValues);
		enableFeedbackEl.addActionListener(FormEvent.ONCHANGE);
		if(position.isPublicFeedbackEnabled()) {
			enableFeedbackEl.select(enableKeys[0], true);
		}
		
		// deadline container
		String feedbackDeadlineCont = velocity_root + "/edit_public_feedback.html";
		feedbackDeadlineContainer = FormLayoutContainer.createCustomFormLayout("public.feedback.deadline", getTranslator(), feedbackDeadlineCont);
		feedbackDeadlineContainer.setRootForm(mainForm);
		feedbackDeadlineContainer.setLabel("edit.public.feedback.deadline", null);
		feedbackDeadlineContainer.setMandatory(true);
		formLayout.add(feedbackDeadlineContainer);
		
		String feedbackDay = "";
		String feedbackMonth= "0";
		String feedbackYear = "";
		Date feedbackDeadline = position.getPublicFeedbackDeadline();
		if(feedbackDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(feedbackDeadline);
			feedbackDay = Integer.toString(cal.get(Calendar.DATE));
			feedbackMonth = Integer.toString(cal.get(Calendar.MONTH));
			feedbackYear = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		feedbackDeadlineDayElement = uifactory.addTextElement("public.feedback.deadline.day", null, 2, feedbackDay, feedbackDeadlineContainer);
		feedbackDeadlineDayElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineDayElement.setDisplaySize(2);
		feedbackDeadlineDayElement.setMandatory(true);
		
		feedbackDeadlineMonthElement = uifactory.addDropdownSingleselect("public.feedback.deadline.month", null, feedbackDeadlineContainer, monthKeys, monthValues, null);
		feedbackDeadlineMonthElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineMonthElement.setMandatory(true);
		feedbackDeadlineMonthElement.select(feedbackMonth, true);
		
		feedbackDeadlineYearElement = uifactory.addTextElement("public.feedback.deadline.year", null, 4, feedbackYear, feedbackDeadlineContainer);
		feedbackDeadlineYearElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineYearElement.setDisplaySize(4);
		feedbackDeadlineYearElement.setMandatory(true);
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateGUI();
	}
	
	private void updateGUI() {
		boolean feedbackEnabled = enableFeedbackEl.isAtLeastSelected(1);
		feedbackDeadlineContainer.setVisible(feedbackEnabled);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private Date getPublicFeedbackDeadline() {
		String dayStr = feedbackDeadlineDayElement.getValue();
		String monthStr = feedbackDeadlineMonthElement.getSelectedKey();
		String yearStr = feedbackDeadlineYearElement.getValue();
		
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
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		feedbackDeadlineYearElement.clearError();
		if(getPublicFeedbackDeadline() == null) {
			feedbackDeadlineYearElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateYearElement(feedbackDeadlineYearElement);
		}

		return allOk;
	}

	private boolean validateYearElement(TextElement textEl) {
		boolean ok = true;
		if(StringHelper.containsNonWhitespace(textEl.getValue())) {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 5;
			try {
				int year = Integer.parseInt(textEl.getValue());
				if(year < 2010 || year > currentYear) {
					ok &= false;
					textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
				}
			} catch (NumberFormatException e) {
				ok =false;
				textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
			}
		}
		return ok;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableFeedbackEl == source) {
			updateGUI();
		}
		super.formInnerEvent(ureq, source, event);
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

		boolean publicFeedbackEnabled = enableFeedbackEl.isAtLeastSelected(1);
		if(publicFeedbackEnabled != position.isPublicFeedbackEnabled()) {
			logAudit("Referees " + (publicFeedbackEnabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setPublicFeedbackEnabled(publicFeedbackEnabled);
		if(publicFeedbackEnabled) {
			Date deadline = getPublicFeedbackDeadline();
			position.setPublicFeedbackDeadline(deadline);
		} else {
			position.setPublicFeedbackDeadline(null);
		}

		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update public feedback position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	

}
