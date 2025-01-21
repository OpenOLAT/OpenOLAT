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
package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Initial date: Jan 09, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChangeMailUMDialogController extends FormBasicController {

	private final String changedEMail;
	private MultipleSelectionElement notifyUserEl;

	protected ChangeMailUMDialogController(UserRequest ureq, WindowControl wControl, String changedEMail) {
		super(ureq, wControl, "confirm_mail_change_um");
		this.changedEMail = changedEMail;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String desc = translate("email.change.dialog.um.desc");
		uifactory.addStaticTextElement("desc", null, desc, formLayout);

		notifyUserEl = uifactory.addCheckboxesHorizontal("notifyCheck", "change.mail.notify", formLayout,
				new String[]{"on"}, new String[]{translate("change.mail.notify.desc")});

		uifactory.addFormSubmitButton("confirm.email.in.process", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new ChangeMailEvent(ChangeMailEvent.CHANGED_EMAIL_EVENT, changedEMail));
	}

	public boolean getIsNotifyUser() {
		return notifyUserEl.isAtLeastSelected(1);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
