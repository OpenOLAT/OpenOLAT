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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.generic.confirmation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 6 Dev 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.comm
 *
 */
public class ConfirmationController extends FormBasicController {
	
	private FormLink confirmLink;
	private MultipleSelectionElement confirmationEl;
	
	private final String message;
	private final String confirmation;
	private final String confirmButton;
	private final boolean confirmDanger;
	private Object userObject;
	
	public ConfirmationController(UserRequest ureq, WindowControl wControl, String message, String confirmation,
			String confirmButton) {
		this(ureq, wControl, message, confirmation, confirmButton, false);
	}
	
	public ConfirmationController(UserRequest ureq, WindowControl wControl, String message, String confirmation,
			String confirmButton, boolean confirmDanger) {
		this(ureq, wControl, message, confirmation, confirmButton, confirmDanger, true);
	}

	public ConfirmationController(UserRequest ureq, WindowControl wControl, String message, String confirmation,
			String confirmButton, boolean confirmDanger, boolean init) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.message = message;
		this.confirmation = confirmation;
		this.confirmButton = confirmButton;
		this.confirmDanger = confirmDanger;
		
		if (init) {
			initForm(ureq);
		}
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	@SuppressWarnings("unused")
	protected void initFormElements(FormLayoutContainer confirmCont) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer messageCont = FormLayoutContainer.createCustomFormLayout("messageCont", getTranslator(),
				Util.getPackageVelocityRoot(ConfirmationController.class) + "/confirmation.html");
		formLayout.add(messageCont);
		messageCont.contextPut("message", message);
		
		FormLayoutContainer confirmCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
		formLayout.add("confirm", confirmCont);
		confirmCont.setRootForm(mainForm);
		confirmCont.setElementCssClass("o_sel_confirm_form");
		
		initFormElements(confirmCont);
		
		if (StringHelper.containsNonWhitespace(confirmation)) {
			confirmationEl = uifactory.addCheckboxesHorizontal("confirmation", "confirmation", confirmCont,
					new String[] { "" }, new String[] { confirmation });
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		confirmCont.add(buttonsCont);
		confirmLink = uifactory.addFormLink("confirm", buttonsCont, Link.BUTTON + Link.NONTRANSLATED);
		confirmLink.setElementCssClass("o_sel_confirm");
		confirmLink.setI18nKey(confirmButton);
		if (confirmDanger) {
			confirmLink.setElementCssClass("btn-danger");
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (confirmationEl != null) {
			confirmationEl.clearError();
			if (!confirmationEl.isAtLeastSelected(1)) {
				confirmationEl.setErrorKey("error.confirmation.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (confirmLink == source) {
			if (validateFormLogic(ureq)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
