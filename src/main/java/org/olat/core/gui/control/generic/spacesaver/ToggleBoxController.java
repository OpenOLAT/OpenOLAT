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

	private static final String CSS_OPENED = "o_opener o_in";
	private static final String CSS_CLOSED = "o_opener";

	private Boolean toggleStatus;
	private final String titleOpen;
	private final String titleClose;
	private final String key;
	private final Component componentToToggle;
	private final boolean defaultToggleStatus;
	private Link toggleButton;
	private Link hideButton;
	private final VelocityContainer mainVC;

	public ToggleBoxController(UserRequest ureq, WindowControl wControl, String key, String titleOpen, String titleClose, Controller controllerToToggle) {
		this(ureq, wControl, key, titleOpen, titleClose, controllerToToggle.getInitialComponent());
	}

	public ToggleBoxController(UserRequest ureq, WindowControl wControl, String key, String titleOpen, String titleClose, Component componentToToggle) {
		this(ureq, wControl, key, titleOpen, titleClose, componentToToggle, true);
	}

	public ToggleBoxController(UserRequest ureq, WindowControl wControl, String key, String titleOpen, String titleClose, Component componentToToggle,
			boolean defaultToggleStatus) {
		super(ureq, wControl);

		this.key = key;
		this.titleOpen = titleOpen;
		this.titleClose = titleClose;
		this.componentToToggle = componentToToggle;
		this.defaultToggleStatus = defaultToggleStatus;
		
		mainVC = createVelocityContainer("togglebox");
		toggleButton = LinkFactory.createCustomLink("toggle", "toggle", "", Link.NONTRANSLATED, mainVC, this);
		toggleButton.setIconLeftCSS("o_icon o_icon-fw");

		hideButton = LinkFactory.createLink("hide", mainVC, this);	
		hideButton.setCustomEnabledLinkCSS("o_hide");
		reload(ureq);

		putInitialPanel(mainVC);
	}
	
	public void reload(UserRequest ureq) {
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		toggleStatus = (Boolean) prefs.get(this.getClass(), key, defaultToggleStatus);
		mainVC.put("cmpToToggle", componentToToggle);
		updateUI();
	}
	
	protected void updateUI() {
		if(toggleStatus.booleanValue()) {
			toggleButton.setCustomDisplayText(titleClose);
			toggleButton.setCustomEnabledLinkCSS(CSS_OPENED);
		} else {
			toggleButton.setCustomDisplayText(titleOpen);
			toggleButton.setCustomEnabledLinkCSS(CSS_CLOSED);
		}
		mainVC.contextPut("toggleStatus", toggleStatus);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == toggleButton) {
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			toggleStatus = Boolean.valueOf(!toggleStatus.booleanValue());
			prefs.putAndSave(this.getClass(), key, toggleStatus);
			updateUI();
		} else if (source == hideButton) {
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			toggleStatus = Boolean.FALSE;
			prefs.putAndSave(this.getClass(), key, toggleStatus);
			updateUI();
		}
	}
}
