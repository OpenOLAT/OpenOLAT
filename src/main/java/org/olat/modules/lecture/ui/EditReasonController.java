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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditReasonController extends FormBasicController {

	private TextElement titleEl;
	private TextElement descriptionEl;
	private SingleSelection enableEl;
	
	private Reason reason;
	
	@Autowired
	private LectureService lectureService;
	
	public EditReasonController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}
	
	public EditReasonController(UserRequest ureq, WindowControl wControl, Reason reason) {
		super(ureq, wControl);
		this.reason = reason;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = reason == null ? "" : reason.getTitle();
		titleEl = uifactory.addTextElement("title", "reason.title", 128, title, formLayout);
		titleEl.setMandatory(true);
		
		SelectionValues activeKeyValues = new SelectionValues();
		activeKeyValues.add(SelectionValues.entry("true", translate("reason.enabled")));
		activeKeyValues.add(SelectionValues.entry("false", translate("reason.disabled")));
		enableEl = uifactory.addRadiosHorizontal("reason.activated", "reason.activated", formLayout,
				activeKeyValues.keys(), activeKeyValues.values());
		if(reason != null) {
			enableEl.select(Boolean.toString(reason.isEnabled()), true);
		} else {
			enableEl.select("true", true);
		}
		
		String description = reason == null ? "" : reason.getDescription();
		descriptionEl = uifactory.addTextAreaElement("reason.description", 4, 72, description, formLayout);
		descriptionEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		enableEl.clearError();
		if(!enableEl.isEnabled()) {
			enableEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		descriptionEl.clearError();
		if(!StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
			descriptionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = "true".equals(enableEl.getSelectedKey());
		if(reason == null) {
			reason = lectureService.createReason(titleEl.getValue(), descriptionEl.getValue(), enabled);
		} else {
			reason.setEnabled(enabled);
			reason.setTitle(titleEl.getValue());
			reason.setDescription(descriptionEl.getValue());
			reason = lectureService.updateReason(reason);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
