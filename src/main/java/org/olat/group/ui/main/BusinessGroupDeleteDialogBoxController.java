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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;

/**
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupDeleteDialogBoxController extends FormBasicController {
	
	private SingleSelection sendMail;
	
	private final String[] keys = {"yes","no"};
	
	private final List<BusinessGroup> groupsToDelete;
	
	public BusinessGroupDeleteDialogBoxController(UserRequest ureq, WindowControl wControl, List<BusinessGroup> groupsToDelete) {
		super(ureq, wControl, "group_delete_confirmation");
		this.groupsToDelete = groupsToDelete;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder names = new StringBuilder();
		for(BusinessGroup group:groupsToDelete) {
			if(names.length() > 0) names.append(", ");
			names.append(group.getName());
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("groups", names.toString());
		}

		FormLayoutContainer mailCont = FormLayoutContainer.createDefaultFormLayout("sendmail", getTranslator());
		formLayout.add(mailCont);
		String[] values = new String[]{
				translate("yes"),
				translate("no")
		};
		sendMail = uifactory.addRadiosVertical("send.mail", "dialog.modal.bg.mail.text", mailCont, keys, values);
		sendMail.select("no", true);
		
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("deleteButton", "yes", buttonsContainer);
		uifactory.addFormCancelButton("no", buttonsContainer, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	public boolean isSendMail() {
		return sendMail.isOneSelected() && sendMail.isSelected(0);
	}

	public List<BusinessGroup> getGroupsToDelete() {
		return groupsToDelete;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
