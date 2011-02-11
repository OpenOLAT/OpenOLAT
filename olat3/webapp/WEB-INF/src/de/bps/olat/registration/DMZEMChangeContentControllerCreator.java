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
package de.bps.olat.registration;

import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.login.DmzBFWCParts;

import de.bps.olat.user.ChangeEMailController;

/**
 * 
 * Description:<br>
 * TODO: bja Class Description for DMZEMChangeContentControllerCreator
 * 
 * <P>
 * Initial Date:  21.11.2008 <br>
 * @author bja
 */
public class DMZEMChangeContentControllerCreator implements ControllerCreator {

	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		DmzBFWCParts dmzSitesAndNav = new DmzBFWCParts();
		dmzSitesAndNav.showTopNav(false);
		AutoCreator contentControllerCreator = new AutoCreator();
		contentControllerCreator.setClassName(ChangeEMailController.class.getName());
		dmzSitesAndNav.setContentControllerCreator(contentControllerCreator);
		return new BaseFullWebappController(lureq, lwControl, dmzSitesAndNav );		
	}

}
