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
package org.olat.modules.message.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmWithdrawMessagesController extends FormBasicController {
	
	private FormLink deleteButton;
	
	private List<AssessmentMessage> messagesToDelete;
	
	@Autowired
	private AssessmentMessageService assessmentMessageService;
	
	public ConfirmWithdrawMessagesController(UserRequest ureq, WindowControl wControl, List<AssessmentMessage> messagesToDelete) {
		super(ureq, wControl, "confirm_withdraw");
		this.messagesToDelete = messagesToDelete;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String[] args = new String[] { Integer.toString(messagesToDelete.size()) };
			String i18n = messagesToDelete.size() <= 1 ? "confirm.withdraw.text" : "confirm.withdraw.text.plural";
			layoutCont.contextPut("message", translate(i18n, args));
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		deleteButton = uifactory.addFormLink("withdraw", formLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doDelete();
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doDelete() {
		for(AssessmentMessage messageToDelete:messagesToDelete) {
			assessmentMessageService.withdrawMessage(messageToDelete);
		}
	}
}
