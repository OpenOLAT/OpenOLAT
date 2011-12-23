package org.olat.test.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

public class GuiDemoIconsController extends BasicController {

	VelocityContainer vcMain;

	public GuiDemoIconsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vcMain = createVelocityContainer("guidemo-icons");
		putInitialPanel(vcMain);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
