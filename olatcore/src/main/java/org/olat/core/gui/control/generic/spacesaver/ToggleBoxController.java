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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.gui.control.generic.spacesaver;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.prefs.Preferences;

/**
 *
 * Description:<br>
 * This controller wraps a component or a controller, add a link hide/show.
 * The status is persisted in a user property which key is the persistedId
 * defined in constructor.  
 * 
 * <P>
 * Initial Date:  16 sept. 2009 <br>
 * @author srosse
 */
public class ToggleBoxController extends BasicController {

	private static final String CSS_OPENED = "b_togglebox_opened";
	private static final String CSS_CLOSED = "b_togglebox_closed";

	private Boolean toggleStatus;
	private final String titleOpen;
	private final String titleClose;
	private final String key;
	private Link toggleButton;
	private Link hideButton;
	private final VelocityContainer mainVC;

	public ToggleBoxController(UserRequest ureq, WindowControl wControl, String key, String titleOpen, String titleClose, Controller controllerToToggle) {
		this(ureq, wControl, key, titleOpen, titleClose, controllerToToggle.getInitialComponent());
	}

	public ToggleBoxController(UserRequest ureq, WindowControl wControl, String key, String titleOpen, String titleClose, Component componentToToggle) {
		super(ureq, wControl);

		this.key = key;
		this.titleOpen = titleOpen;
		this.titleClose = titleClose;
		
		mainVC = createVelocityContainer("togglebox");
		toggleButton = LinkFactory.createCustomLink("toggle", "toggle", "", Link.NONTRANSLATED, mainVC, this);

		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		toggleStatus = (Boolean) prefs.get(this.getClass(), key, Boolean.TRUE);
		mainVC.put("cmpToToggle", componentToToggle);

		hideButton = LinkFactory.createLink("hide", mainVC, this);
		//hideButton = LinkFactory.createCustomLink("hide", "hide", "Hide", Link.LINK, mainVC, this);
		hideButton.setCustomEnabledLinkCSS("b_togglebox_hide");
	
		updateUI();

		putInitialPanel(mainVC);
	}
	
	protected void updateUI() {
		if(toggleStatus.booleanValue()) {
			toggleButton = LinkFactory.createCustomLink("toggle", "toggle", titleClose, Link.NONTRANSLATED, mainVC, this);
			toggleButton.setCustomEnabledLinkCSS(CSS_OPENED);
		} else {
			toggleButton = LinkFactory.createCustomLink("toggle", "toggle", titleOpen, Link.NONTRANSLATED, mainVC, this);
			toggleButton.setCustomEnabledLinkCSS(CSS_CLOSED);
		}
		mainVC.contextPut("toggleStatus", toggleStatus);
	}

	@Override
	protected void doDispose() {
	//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == toggleButton) {
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			toggleStatus = new Boolean(!toggleStatus.booleanValue());
			prefs.putAndSave(this.getClass(), key, toggleStatus);
			updateUI();
		} else if (source == hideButton) {
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			toggleStatus = Boolean.FALSE;
			prefs.putAndSave(this.getClass(), key, toggleStatus);
			updateUI();
		} else if (source == mainVC) {
			if ("hide".equals(event.getCommand())) {
				Preferences prefs = ureq.getUserSession().getGuiPreferences();
				toggleStatus = Boolean.FALSE;
				prefs.putAndSave(this.getClass(), key, toggleStatus);
				updateUI();
			}
		}
	}
	//backup of the shrink link
	//<a class="b_togglebox_hide" href="#" onclick="Effect.Shrink('my_toggle_box_uuid',{ direction:'top-left', afterFinish: function() { top.o_openUriInMainWindow('$r.commandURI("hide")'); }}); return false;"><span>Hide</span></a>
}
