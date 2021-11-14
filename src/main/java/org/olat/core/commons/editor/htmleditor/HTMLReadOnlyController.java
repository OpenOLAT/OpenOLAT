/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.editor.htmleditor;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 25 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HTMLReadOnlyController extends BasicController {
	
	private Link closeLink;
	private SinglePageController singlePageCtrl;

	public HTMLReadOnlyController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, String fileName, boolean showClose) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("readonly");
		
		singlePageCtrl = new SinglePageController(ureq, wControl, rootContainer, fileName, false);
		listenTo(singlePageCtrl);
		mainVC.put("content", singlePageCtrl.getInitialComponent());
		
		if (showClose) {
			closeLink = LinkFactory.createButton("close", mainVC, this);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == closeLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

}
