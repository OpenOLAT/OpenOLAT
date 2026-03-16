/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

import org.olat.modules.selectus.model.mail.EmailVariables;

/**
 * 
 * Initial date: 29 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AttachmentWarningController extends FormBasicController {
	
	private final EmailVariables emailVar;
	
	public AttachmentWarningController(UserRequest ureq, WindowControl wControl, EmailVariables emailVar) {
		super(ureq, wControl, "attachment_warning");
		this.emailVar = emailVar;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(emailVar.getSelectedApps().size() <= 1) {
			formLayout.contextPut("msg", translate("warning.attachment"));
		} else {
			formLayout.contextPut("msg", translate("warning.attachments"));
		}
		uifactory.addFormSubmitButton("send.emails", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
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
