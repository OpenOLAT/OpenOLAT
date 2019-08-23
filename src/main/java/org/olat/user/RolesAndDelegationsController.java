package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;

/**
 * @author Martin Schraner
 */
public class RolesAndDelegationsController extends BasicController {

	protected final Identity userIdentity;
	protected final VelocityContainer myContent;

	protected RolesAndDelegationsController(UserRequest ureq,
											WindowControl wControl,
											Identity userIdentity, Roles userRoles) {
		super(ureq, wControl);
		this.userIdentity = userIdentity;

		myContent = createVelocityContainer("rolesAndDelegations");
		putInitialPanel(myContent);

		myContent.contextPut("isOLATAdmin", userRoles.isOLATAdmin());
		myContent.contextPut("isAuthor", userRoles.isAuthor());
		myContent.contextPut("isGroupManager", userRoles.isGroupManager());
		myContent.contextPut("isUserManager", userRoles.isUserManager());
		myContent.contextPut("isInstitutionalResourceManager", userRoles.isInstitutionalResourceManager());
		myContent.contextPut("isPoolAdmin", userRoles.isPoolAdmin());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void doDispose() {
	}
}
