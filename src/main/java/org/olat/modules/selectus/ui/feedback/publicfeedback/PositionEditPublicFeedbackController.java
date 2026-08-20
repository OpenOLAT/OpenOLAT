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
package org.olat.modules.selectus.ui.feedback.publicfeedback;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditableController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 27 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditPublicFeedbackController extends FormBasicController implements PositionEditableController {

	private static final String[] enableKeys = new String[]{ "on" };
	
	private MultipleSelectionElement enableFeedbackEl;
	private DateChooser feedbackDeadlineEl;
	
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

		Date feedbackDeadline = position.getPublicFeedbackDeadline();
		feedbackDeadlineEl = uifactory.addDateChooser("edit.public.feedback.deadline", feedbackDeadline, formLayout);
		feedbackDeadlineEl.setMandatory(true);
		
		final FormLayoutContainer buttonLayout = uifactory.addButtonsFormLayout("button_layout", null, formLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateGUI();
	}
	
	private void updateGUI() {
		boolean feedbackEnabled = enableFeedbackEl.isAtLeastSelected(1);
		feedbackDeadlineEl.setVisible(feedbackEnabled);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		feedbackDeadlineEl.clearError();
		if(feedbackDeadlineEl.isVisible() && feedbackDeadlineEl.getDate() == null) {
			feedbackDeadlineEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= RecruitingHelper.validateYearElement(feedbackDeadlineEl);
		}

		return allOk;
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
			Date deadline = feedbackDeadlineEl.getDate();
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
