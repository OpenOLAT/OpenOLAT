/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 3 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PreviewBlacklistConfirmationController extends FormBasicController {

	private boolean addToBlacklist;
	private String message;

	protected PreviewBlacklistConfirmationController(UserRequest ureq, WindowControl wControl, boolean addToBlacklist, String message) {
		super(ureq, wControl, "confirmation");
		this.addToBlacklist = addToBlacklist;
		this.message = message;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layout = (FormLayoutContainer)formLayout;
			layout.contextPut("message", message);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		if (addToBlacklist) {
			uifactory.addFormSubmitButton("preview.blacklist.add", buttonLayout);
		} else {
			uifactory.addFormSubmitButton("preview.blacklist.remove", buttonLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());

	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
