/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.main.nav;

import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.gui.control.OlatTopNavController;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Description:<br>
 * The DMZ top navigation is empty.
 * 
 * <P>
 * Initial Date:  27 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RecruitingDmzTopNavController extends BasicController implements LockableController {
	
	@Autowired
	private HelpModule helpModule;
	
	public RecruitingDmzTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(OlatTopNavController.class, ureq.getLocale(),
				Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale())));
		
		VelocityContainer topNavVC = createVelocityContainer("dmztopnav");
		if(helpModule.isHelpEnabled() ) {
			/*//TODO selectus
			Component helpLink = helpModule..getHelpProvider()
				.getHelpPageLink(ureq, translate("topnav.help"), translate("topnav.help.alt"), "o_icon o_icon-fw o_icon_help", null, null);
			topNavVC.put("topnav.help", helpLink);
			*/
		}

		putInitialPanel(topNavVC);
	}

	@Override
	public void lock() {
		//
	}

	@Override
	public void unlock() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}