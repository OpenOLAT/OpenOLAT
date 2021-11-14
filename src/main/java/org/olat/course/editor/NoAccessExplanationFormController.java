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

package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Provides a controller using FlexiForms which allows entering a text which
 * gets displayed as an explanation when the user has no permission to access a
 * ressource.
 * 
 * @author twuersch
 * 
 */
public class NoAccessExplanationFormController extends FormBasicController {

	/**
	 * The text input form.
	 */
	private RichTextElement noAccessExplanationInput;

	/**
	 * Remembers whether the constructor was used to initialize the form.
	 */
	private boolean constructorInitCall;

	/**
	 * The message.
	 */
	private String noAccessString;

	/**
	 * Initializes this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param noAccessString
	 */
	public NoAccessExplanationFormController(UserRequest ureq, WindowControl wControl, String noAccessString) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		this.noAccessString = noAccessString;
		constructorInitCall = true;
		initForm(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Add the rich text element
		noAccessExplanationInput = uifactory.addRichTextElementForStringDataMinimalistic("form.noAccessExplanation", "form.noAccessExplanation",
				(noAccessString == null ? "" : noAccessString), 10, -1, formLayout, getWindowControl());

		if (constructorInitCall) {
			// Create submit button
			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			uifactory.addFormSubmitButton("save", buttonLayout);
			constructorInitCall = false;
		}
	}

	/**
	 * Gets the message string for the no access explanation.
	 * 
	 * @return String The noAccessExplenation
	 */
	public String getNoAccessExplanation() {
		if (noAccessExplanationInput != null) return noAccessExplanationInput.getValue();
		else return null;
	}

	/**
	 * Gets the message string for the no access explanation.
	 * 
	 * @param message The message
	 */
	public void setNoAccessExplanation(String message) {
		if (noAccessExplanationInput != null) {
			noAccessExplanationInput.setValue(message);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean formOK = true;
		if (noAccessExplanationInput.getValue().length() > 4000) {
			formOK = false;
			noAccessExplanationInput.setErrorKey("input.toolong", new String[] {"4000"});
		}
		if (formOK && super.validateFormLogic(ureq)) {
			noAccessExplanationInput.clearError();
			return true;
		} else {
			return false;
		}
	}
}
