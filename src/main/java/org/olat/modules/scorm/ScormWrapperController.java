/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.scorm;

import java.io.File;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.dispatcher.mapper.sandbox.ControllerDeliveryCreator;
import org.olat.core.dispatcher.mapper.sandbox.ControllerDeliveryMapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.helpers.Settings;
import org.olat.core.util.CodeHelper;
import org.olat.modules.scorm.events.FinishEvent;

/**
 * 
 * Initial date: 26 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScormWrapperController extends MainLayoutBasicController {
	
	private boolean fullScreen;
	private boolean showNavButtons;
	private DeliveryOptions deliveryOptions;
	private final VelocityContainer mainVC;
	private ChiefController thebaseChief;
	
	public ScormWrapperController(UserRequest ureq, WindowControl wControl, boolean showMenu,
			File cpRoot, Long scormResourceId, String courseIdNodeId, String lessonMode, String creditMode,
			String assessableType, boolean activate,  boolean attemptsAlreadyIncremented, ScormDisplayEnum fullWindow,
			boolean randomizeDelivery, DeliveryOptions deliveryOptions) {
		super(ureq, wControl);
		this.deliveryOptions = deliveryOptions == null
				? DeliveryOptions.defaultContent()
				: deliveryOptions;
		
		mainVC = createVelocityContainer("display_wrapper");
		mainVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID		
		
		ControllerCreator scormDisplayControllerCreator = (lureq, lwControl) -> {
			return new ScormAPIandDisplayController(lureq, lwControl, showMenu,
					cpRoot, scormResourceId, courseIdNodeId, lessonMode, creditMode,
					assessableType, activate,  attemptsAlreadyIncremented, fullWindow,
					randomizeDelivery, getDeliveryOptions());
		};
		
		String id = Long.toString(CodeHelper.getForeverUniqueID());
		ControllerDeliveryMapper mapper = new ControllerDeliveryMapper(getWindowControl(),
				new ControllerDeliveryCreator(scormDisplayControllerCreator, this));
		MapperKey mapperKey = registerSandboxedMapper(ureq, "scorm-sandboxed-" + id, mapper);
		String baseUri = Settings.createContentServerURI() + mapperKey.getUrl();
		mainVC.contextPut("baseURI", baseUri);
		mainVC.contextPut("currentURI", "start.html");
		mainVC.contextPut("token", "?token=" + mapperKey.getToken());
		mainVC.contextPut("iframeHeight", "100%");
		mainVC.contextPut("frameId", "scorm_" + id);
		mainVC.contextPut("debug", Boolean.FALSE);
		
		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/openolat/iFrameResizerHelper.js" }, null);
		mainVC.put("js", js);
		
		if (activate) {
			if(fullWindow == ScormDisplayEnum.fullWindow) {
				setAsFullscreen();
			} else if(fullWindow == ScormDisplayEnum.fullWidthHeight || fullWindow == ScormDisplayEnum.fullWidthHeightWithBack) {
				setAsFullscreen();
				wControl.getWindowBackOffice().getChiefController().addBodyCssClass("o_scorm_full_width");
			}
		} else {
			putInitialPanel(mainVC);
		}
	}
	
	public DeliveryOptions getDeliveryOptions() {
		return deliveryOptions;
	}

	public void setDeliveryOptions(DeliveryOptions deliveryOptions) {
		this.deliveryOptions = deliveryOptions;
	}

	public boolean isShowNavButtons() {
		return showNavButtons;
	}

	public void showNavButtons(boolean show) {
		this.showNavButtons = show;
	}

	public void activate() {
		getWindowControl().pushToMainArea(mainVC);
	}
	
	public void close() {
		getWindowControl().pop();
		if (fullScreen && thebaseChief != null) {
			thebaseChief.getScreenMode().setMode(Mode.standard, null);
		}
		getWindowControl().getWindowBackOffice().getChiefController().removeBodyCssClass("o_scorm_full_width");
		getWindowControl().getWindowBackOffice().getChiefController().removeBodyCssClass("o_scorm_with_back");
	}
	
	public void configurationChanged() {
		close();
		getWindowControl().getWindowBackOffice().getChiefController().removeBodyCssClass("o_scorm_full_width");
		getWindowControl().getWindowBackOffice().getChiefController().removeBodyCssClass("o_scorm_with_back");
	}
	
	public void doGoToSco(TreeEvent te) {
		//TODO scorm
	}
	
	public void setHeightPX(int height) {
		//TODO scorm
	}
	
	public void setAsFullscreen() {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			thebaseChief = cc;
			thebaseChief.getScreenMode().setMode(Mode.full, null);
		}
		fullScreen = true;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof ScormAPIandDisplayController) {
			if(event == Event.BACK_EVENT || event == Event.CLOSE_EVENT) {
				close();
				fireEvent(ureq, event);
			} else if(event instanceof FinishEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}