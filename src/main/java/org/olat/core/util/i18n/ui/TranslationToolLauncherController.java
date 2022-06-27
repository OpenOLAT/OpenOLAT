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

package org.olat.core.util.i18n.ui;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> This controller offers a panel for translators. On the
 * panel, translators can launch the translation tool in a new window and modify
 * some settings. <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>No events fired by this controller</li>
 * </ul>
 * <p>
 * Initial Date: 24.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class TranslationToolLauncherController extends BasicController {
	private VelocityContainer translationToolLauncherVC;
	private Link startTranslationToolLink;
	private Link cacheFlushLink;
	
	@Autowired
	private I18nModule i18nModule;

	/**
	 * Constructor for the translation tool start panel controller
	 * 
	 * @param ureq
	 * @param control
	 */
	TranslationToolLauncherController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		translationToolLauncherVC = createVelocityContainer("translationToolLauncher");
		// Add start link to launch translation tool
		startTranslationToolLink = LinkFactory.createButton("start", translationToolLauncherVC, this);
		startTranslationToolLink.setTarget("_transtool");
		// Add link to flush the cache
		if (i18nModule.isCachingEnabled()) {
			cacheFlushLink = LinkFactory.createButton("cache.flush", translationToolLauncherVC, this);
		}
		putInitialPanel(translationToolLauncherVC);
		// Enable or disable entire translation tool
		boolean isTranslationToolEnabled = i18nModule.isTransToolEnabled();
		translationToolLauncherVC.contextPut("transToolEnabled", Boolean.valueOf(isTranslationToolEnabled));
		// Enable or disable customizing tool. The customzing tool is only enabled
		// when not configured as translation tool server
		translationToolLauncherVC.contextPut("customizingToolEnabled", Boolean.valueOf(!isTranslationToolEnabled && i18nModule.isOverlayEnabled()));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startTranslationToolLink) {
			// wrap the content controller into a full header layout
			ControllerCreator controllerCreator = (uureq, wControl) -> {
				return new TranslationToolMainController(uureq, wControl, !i18nModule.isTransToolEnabled());
			};
			// no need for later disposal, opens in popup window and will be disposed
			// by window manager
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, controllerCreator);
			PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager()
					.createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
			pbw.open(ureq);

		} else if (source == cacheFlushLink) {
			// clear i18n cache
			i18nModule.reInitializeAndFlushCache();
			showInfo("cache.flush.ok");
		}
	}
}
