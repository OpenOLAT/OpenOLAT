/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
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
		userList = uifactory.addTextAreaElement("userlist", "delete.list", -1, 10, 35, true, "", formLayout);
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
