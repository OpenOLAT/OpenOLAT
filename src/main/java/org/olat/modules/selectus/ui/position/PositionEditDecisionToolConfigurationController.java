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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;

/**
 * 
 * 
 * Initial date: 27 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditDecisionToolConfigurationController extends FormBasicController implements PositionEditableController {

	private static final String[] enabledKeys = new String[] { "on" };

	private MultipleSelectionElement rubricEnableEl;
	
	private Position position;
	private final boolean readOnly;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;

	public PositionEditDecisionToolConfigurationController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description.decision");
		formLayout.setElementCssClass("o_sel_edit_position_configuration_decision_tool_form");

		String[] enabledValues = new String[]{ translate("enable") };

		rubricEnableEl = uifactory.addCheckboxesHorizontal("rubric.enable", "rubric.enable", formLayout,
				enabledKeys, enabledValues);
		rubricEnableEl.setHelpTextKey("rubric.enable.hint", null);
		rubricEnableEl.setVisible(recruitingModule.isDecisionToolEnabled());
		rubricEnableEl.setElementCssClass("o_sel_position_enable_rubric");
		rubricEnableEl.setEnabled(!readOnly);
		if(position.isDecisionTool()) {
			rubricEnableEl.select(enabledKeys[0], true);
		}
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		FormSubmit submitButton = uifactory.addFormSubmitButton("submit", buttonLayout);
		submitButton.setVisible(!readOnly);
		FormCancel cancelButton = uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		cancelButton.setVisible(!readOnly);
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
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = erFrontendManager.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);

		boolean rubricEnabled = rubricEnableEl.isVisible() && rubricEnableEl.isAtLeastSelected(1);
		if(rubricEnabled != position.isDecisionTool()) {
			logAudit("Decision tool " + (rubricEnabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setDecisionTool(rubricEnabled);
		
		position = erFrontendManager.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update position: " + position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
