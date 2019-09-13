package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
public class RolesAndDelegationsControllerFactoryImpl implements RolesAndDelegationsControllerFactory {

	@Override
	public RolesAndDelegationsController create(UserRequest ureq, WindowControl wControl, Identity userIdentity, Roles userRoles) {
		return new RolesAndDelegationsController(ureq, wControl, userIdentity, userRoles);
	}
}
