package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;

/**
 * @author Martin Schraner
 */
public interface RolesAndDelegationsControllerFactory {

	RolesAndDelegationsController create(UserRequest ureq, WindowControl wControl, Identity userIdentity, Roles userRoles);
}
