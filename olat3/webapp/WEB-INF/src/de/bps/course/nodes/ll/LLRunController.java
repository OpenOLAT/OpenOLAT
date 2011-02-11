/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.course.nodes.ll;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Run controller for link list nodes.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLRunController extends BasicController {

	private VelocityContainer runVC;

	public LLRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, LLCourseNode llCourseNode,
			UserCourseEnvironment userCourseEnv, boolean showLinkComments) {
		super(ureq, wControl);
		this.runVC = this.createVelocityContainer("run");
		final List<LLModel> linkList = (List<LLModel>) llCourseNode.getModuleConfiguration().get(LLCourseNode.CONF_LINKLIST);
		this.runVC.contextPut("linkList", linkList);
		this.runVC.contextPut("showLinkComments", Boolean.valueOf(showLinkComments));
		putInitialPanel(runVC);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose here

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}

}
