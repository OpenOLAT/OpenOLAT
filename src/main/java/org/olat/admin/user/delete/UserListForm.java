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
package org.olat.admin.user.delete;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * Form for bulk-deletion of users
 * @author skoeber
 */
public class UserListForm extends FormBasicController {
	
	private TextElement userList;
	private TextElement reason;
	
	public UserListForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);	
		initForm(ureq);
	}
	
	
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean valid = true;
		if(userList.isEmpty("list.empty"))
			valid = false;
		else if(reason.isEmpty("reason.empty"))
			valid = false;
		else {
			userList.clearError();
			reason.clearError();
		}
		
		return valid;
	}
	

	public String getLogins() {
		return userList.getValue();
	}
	
	public String getReason() {
		return reason.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		userList = uifactory.addTextAreaElement("userlist", "delete.list", -1, 10, 35, true, false, "", formLayout);
		userList.setExampleKey("delete.list.example", null);
		userList.setMandatory(true);
		
		
		reason = uifactory.addTextElement("reason", "delete.reason", 100, "", formLayout);
		reason.setDisplaySize(35);
		reason.setMandatory(true);
		
		uifactory.addFormSubmitButton("subm", "button.delete.users", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}
