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
package org.olat.core.gui.control.generic.lightbox;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 3 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LightboxController extends BasicController {

	private static final String[] JS_PATH = new String[] { "js/basicLightbox/basicLightbox.min.js" };
	private static final String[] CSS_PATH = new String[] { StaticMediaDispatcher.getStaticURI("js/basicLightbox/basicLightbox.min.css") };
	
	private Controller contentCtrl;

	private VelocityContainer mainVC;
	private String lightboxId;

	public LightboxController(UserRequest ureq, WindowControl wControl, Controller contentCtrl) {
		super(ureq, wControl);
		this.contentCtrl = contentCtrl;
		listenTo(contentCtrl);
		init(contentCtrl.getInitialComponent());
	}
	
	public LightboxController(UserRequest ureq, WindowControl wControl, Component content) {
		super(ureq, wControl);
		init(content);
	}

	private void init(Component content) {
		mainVC = createVelocityContainer("lightbox");
		putInitialPanel(mainVC);
		
		mainVC.put("content", content);
		
		lightboxId = "lb" + CodeHelper.getRAMUniqueID();
		mainVC.contextPut("lightboxId", lightboxId);
		
		JSAndCSSComponent js = new JSAndCSSComponent("js", JS_PATH, CSS_PATH);
		mainVC.put("js", js);
	}
	
	public void activate() {
		getWindowControl().pushAsModalDialog(mainVC, false);
		
		// Set the focus to the first element in the lightbox.
		// Invoke it slightly delayed to be executed after the regular OpenOlat focus function.
		String command = """
				try {
					setTimeout(() => {document.querySelector( '.basicLightbox__placeholder :first-child:not(div)' ).focus();}, 500);
				} catch(e) {
					if (window.console) console.log(e);
				}
				""";
		getWindowControl().getWindowBackOffice().sendCommandTo( new JSCommand(command));
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == contentCtrl) {
			if (event == Event.CLOSE_EVENT) {
				JSCommand command = new JSCommand("try {" + lightboxId + ".close();} catch(e){}");
				getWindowControl().getWindowBackOffice().sendCommandTo(command);
				getWindowControl().removeTopModalDialog(mainVC);
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("lightbox-closed".equals(event.getCommand())) {
			getWindowControl().removeTopModalDialog(mainVC);
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}

}
