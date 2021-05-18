package org.olat.ims.lti13.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteSharedToolDeploymentController extends FormBasicController {
	
	private final LTI13SharedToolDeployment deployment;
	
	@Autowired
	private LTI13Service lti13Service;
	
	public ConfirmDeleteSharedToolDeploymentController(UserRequest ureq, WindowControl wControl, LTI13SharedToolDeployment deployment) {
		super(ureq, wControl, "confirm_delete_deployment");
		this.deployment = deployment;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("issuer",
					StringHelper.escapeHtml(deployment.getPlatform().getIssuer()));
		}
		
		uifactory.addFormSubmitButton("delete", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lti13Service.deleteSharedToolDeployment(deployment);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
