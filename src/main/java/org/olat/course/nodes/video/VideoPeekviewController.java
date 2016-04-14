package org.olat.course.nodes.video;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.video.manager.MediaMapper;

public class VideoPeekviewController  extends BasicController implements Controller{

	public VideoPeekviewController(UserRequest ureq, WindowControl wControl,  VFSContainer posterFolder) {
		super(ureq, wControl);
		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		String mediaUrl = registerMapper(ureq, new MediaMapper(posterFolder));
		peekviewVC.contextPut("mediaUrl", mediaUrl);
		peekviewVC.contextPut("nodeLink", posterFolder);
		putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
	}

}