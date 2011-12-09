/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.jmx;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description:<br>
 * TODO:
 * 
 * <P>
 * Initial Date:  01.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class JMXInfoController extends BasicController {

	private VelocityContainer mainVc;
	//private Link dumpAllLink;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public JMXInfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		if (!JMXManager.getInstance().isActive()) {
			showError("nojmx");
			putInitialPanel(new Panel("empty"));
			return;
		}
		
		mainVc = createVelocityContainer("jmxmain");
		//dumpAllLink = LinkFactory.createButton("dumpall", mainVc, this);
		List<String> jmxres = JMXManager.getInstance().dumpJmx("org.olat.core.commons.modules.bc:name=FilesInfoMBean");
		mainVc.contextPut("jmxlist", jmxres);
		String htmlRes = JMXManager.getInstance().dumpAll();
		mainVc.contextPut("jmxdump", htmlRes);
		
		putInitialPanel(mainVc);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
