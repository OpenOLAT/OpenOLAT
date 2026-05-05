/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.mail.EmailVariables;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewForEmailController extends AbstractEmailController {
	
	public ReviewForEmailController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form,
			EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, runContext, form, emailVar.getSelectedApps(), emailVar.getMailLog(), emailVar.getRatings(),
				emailVar.getCommittee(), emailVar.getPosition(), emailVar.isShowDecisions(), secCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.mail.overview.description");
		super.initForm(formLayout, listener, ureq);
		tableEl.setElementCssClass("o_sel_rejection_mail_to_overview");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}