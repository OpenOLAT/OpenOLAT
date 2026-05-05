/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.main.nav;

import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.gui.control.OlatTopNavController;

import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Top navigation for the wizard, without help
 * 
 * Initial date: 28.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingWizardTopNavController extends BasicController implements LockableController {
	
	public RecruitingWizardTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(OlatTopNavController.class, ureq.getLocale(),
				Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale())));
		
		VelocityContainer topNavVC = createVelocityContainer("dmztopnav");
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