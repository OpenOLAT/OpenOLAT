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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.course.condition.additionalconditions;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;

/**
 * Initial Date:  17.09.2010 <br>
 * @author blaw
 * @author srosse, stephane.rosse@frentix.com
 */
public class PasswordConditionEditController extends FormBasicController {

	private TextElement passwordField;
	private MultipleSelectionElement passwordSwitch;
	private DialogBoxController overwriteDialogBox;
	
	private PasswordCondition condition;
	private boolean hasAlreadyPassword;
	
	public PasswordConditionEditController(UserRequest ureq, WindowControl wControl, PasswordCondition condition) {
		super(ureq, wControl);
		this.condition = condition;
		hasAlreadyPassword = StringHelper.containsNonWhitespace(condition.getPassword());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		passwordSwitch = uifactory.addCheckboxesHorizontal("password.field", "password.field", formLayout, new String[]{"ison"}, new String[]{ "" });
		passwordSwitch.addActionListener(FormEvent.ONCHANGE);
		passwordSwitch.setElementCssClass("o_sel_course_password_condition_switch");
		
		passwordField = uifactory.addTextElement("passwordField", null, 30, "", formLayout);
		passwordField.setExampleKey("password.example", null);
		passwordField.setElementCssClass("o_sel_course_password_condition_value");
		passwordField.showError(false);
		if (condition != null && StringHelper.containsNonWhitespace(condition.getPassword())) {
			passwordSwitch.select("ison", true);
			passwordField.setVisible(true);
			passwordField.setValue(condition.getPassword());
		} else {
			passwordField.setVisible(false);
			passwordSwitch.select("ison", false);
			passwordField.setValue("");
		}
		
		uifactory.addFormSubmitButton("save","password.set", formLayout);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == overwriteDialogBox) {
			if (event != Event.CANCELLED_EVENT && DialogBoxUIFactory.isYesEvent(event)) {
				condition.setPassword(passwordField.getValue());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(passwordSwitch.isAtLeastSelected(1)){
			if(hasAlreadyPassword) {
				String title = translate("password.overwrite.title");
				String msg = translate("password.overwrite.description");
				overwriteDialogBox = activateYesNoDialog(ureq, title, msg, overwriteDialogBox);
			} else {
				condition.setPassword(passwordField.getValue());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else {
			condition.setPassword(null);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean valid = true;
		passwordField.clearError();
		if(passwordSwitch.isAtLeastSelected(1)){
			if(!StringHelper.containsNonWhitespace(passwordField.getValue())) {
				passwordField.setErrorKey("password.error", null);
				valid = false;
			}
		} else {
			passwordField.clearError();
		}
		return valid;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == passwordSwitch) {
			passwordField.setVisible(passwordSwitch.isAtLeastSelected(1));
			flc.setDirty(true);
		}
	}
}
