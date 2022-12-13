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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.ui.event.CloseInspectorEvent;

/**
 * 
 * Initial date: 16 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ModalInspectorController extends BasicController implements PageElementInspectorController {
	
	private final Link closeButton;
	private final VelocityContainer mainVC;
	
	private final String title;
	private final String elementId;
	
	public ModalInspectorController(UserRequest ureq, WindowControl wControl, PageElementInspectorController inspectorCtrl, PageElement element) {
		super(ureq, wControl);
		title = inspectorCtrl.getTitle();
		elementId = element.getId();
		
		listenTo(inspectorCtrl);
		
		mainVC = createVelocityContainer("element_inspector");
		mainVC.put("inspector", inspectorCtrl.getInitialComponent());
		if(title != null) {
			mainVC.contextPut("title", title);
		}
		String title = translate("inspector.hide");
		closeButton = LinkFactory.createIconClose(title, mainVC, this);
		closeButton.setDomReplacementWrapperRequired(false);
		// a11y: Set focus to close link and thus to the dialog itself
		closeButton.setFocus(true, true); 
		closeButton.setTitle(title);
		mainVC.put("close", closeButton);
		
		putInitialPanel(mainVC);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeButton == source) {
			fireEvent(ureq, new CloseInspectorEvent(elementId, false));
		} else if(mainVC == source) {
			if("close_inspector".equals(event.getCommand())) {
				fireEvent(ureq, new CloseInspectorEvent(elementId, false));
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}

}
