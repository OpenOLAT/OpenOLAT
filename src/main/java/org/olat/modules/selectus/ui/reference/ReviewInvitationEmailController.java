/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.model.mail.InvitationVariables;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewInvitationEmailController extends AbstractInvitationEmailController {

	public ReviewInvitationEmailController(UserRequest ureq, WindowControl wControl,
			InvitationVariables emailVar, StepsRunContext runContext, Form form) {
		super(ureq, wControl, emailVar.getSelectedReferences(), emailVar.getSortKey(), runContext, form);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.invitation.overview.description");
		super.initForm(formLayout, listener, ureq);
		tableEl.setElementCssClass("o_sel_invitation_mail_to_overview");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
