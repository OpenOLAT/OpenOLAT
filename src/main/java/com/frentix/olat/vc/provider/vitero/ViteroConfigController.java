
package com.frentix.olat.vc.provider.vitero;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for ViteroConfigController
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroConfigController extends BasicController {

	private VelocityContainer editVC;


	protected ViteroConfigController(UserRequest ureq, WindowControl wControl, String roomId, ViteroBookingProvider adobe, ViteroBookingConfiguration config) {
		super(ureq, wControl);
		

		editVC = createVelocityContainer("edit");

		putInitialPanel(editVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	protected void doDispose() {

	}

}