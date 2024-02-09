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

import java.util.List;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 3 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LightboxController extends BasicController {

	private static final String[] JS_PATH = new String[] { "js/basicLightbox/basicLightbox.min.js" };
	private static final String[] CSS_PATH = new String[] { StaticMediaDispatcher.getStaticURI("js/basicLightbox/basicLightbox.min.css") };
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(LightboxController.class);
	
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
		final Panel guiMsgPlace = new Panel("guimessage_place");
		mainVC = new VelocityContainer("lightbox", VELOCITY_ROOT + "/lightbox.html", null, this) {
			@Override
			public void validate(UserRequest ureq, ValidationResult vr) {
				super.validate(ureq, vr);
				// just before rendering, we need to tell the windowbackoffice that we are a favorite for accepting gui-messages.
				// the windowbackoffice doesn't know about guimessages, it is only a container that keeps them for one render cycle
				WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
				List<ZIndexWrapper> zindexed = wbo.getGuiMessages();
				zindexed.add(new ZIndexWrapper(guiMsgPlace, 20));
			}
		};
		
		putInitialPanel(mainVC);
		
		mainVC.put("content", content);
		
		lightboxId = "lb" + CodeHelper.getRAMUniqueID();
		mainVC.contextPut("lightboxId", lightboxId);
		
		JSAndCSSComponent js = new JSAndCSSComponent("js", JS_PATH, CSS_PATH);
		mainVC.put("js", js);
	}
	
	@Override
	protected void doDispose() {
		getWindowControl().removeModalDialog(mainVC);
        super.doDispose();
	}
	
	public void activate() {
		getWindowControl().pushAsModalDialog(mainVC);
		// Set the focus to the first element in the lightbox.
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createLightBoxFocus());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == contentCtrl) {
			if (event == Event.CLOSE_EVENT) {
				getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.closeLightBox(lightboxId));
				getWindowControl().removeModalDialog(mainVC);
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("lightbox-closed".equals(event.getCommand())) {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.closeLightBox(lightboxId));
			getWindowControl().removeModalDialog(mainVC);
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}

}
