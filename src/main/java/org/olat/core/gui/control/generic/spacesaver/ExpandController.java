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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.prefs.Preferences;

/**
 * 
 * Initial date: 24 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExpandController extends BasicController {
	
	private Link expandButton;
	
	private VelocityContainer mainVC;
	private ExpandableController expandableCtrl;

	private final String guiPrefsKey;
	private Boolean expand;

	public ExpandController(UserRequest ureq, WindowControl wControl, String guiPrefsKey) {
		super(ureq, wControl);
		this.guiPrefsKey = guiPrefsKey;
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		expand = (Boolean) prefs.get(ToggleBoxController.class, guiPrefsKey, Boolean.TRUE);
		
		mainVC = createVelocityContainer("expand");
		
		expandButton = LinkFactory.createCustomLink("expandButton", "expandButton", null,
				Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
		
		putInitialPanel(mainVC);
		
		updateUI();
	}

	private void updateUI() {
		String expandIcon = expand.booleanValue()? "o_icon_details_collaps": "o_icon_details_expand";
		expandButton.setIconLeftCSS("o_icon o_icon_lg " + expandIcon);
		expandButton.setElementCssClass("o_button_details");
		
		if (expandableCtrl != null) {
			expandableCtrl.setExpanded(getExpandSwitch(expand));
		}
	}
	
	public void setExpandableController(ExpandableController newCtrl) {
		removeAsListenerAndDispose(expandableCtrl);
		expandableCtrl = newCtrl;
		
		listenTo(expandableCtrl);
		mainVC.put("expandable", expandableCtrl.getInitialComponent());
		expandableCtrl.setExpanded(getExpandSwitch(expand));
		expandButton.setVisible(expandableCtrl.isExpandable());
	}
	
	private boolean getExpandSwitch(Boolean expand) {
		return expandableCtrl.isExpandable()? expand.booleanValue(): false;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == expandButton) {
			toggle(ureq);
		}
	}

	private void toggle(UserRequest ureq) {
		expand = Boolean.valueOf(!expand.booleanValue());
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		prefs.putAndSave(ToggleBoxController.class, guiPrefsKey, expand);
		
		updateUI();
	}

}
