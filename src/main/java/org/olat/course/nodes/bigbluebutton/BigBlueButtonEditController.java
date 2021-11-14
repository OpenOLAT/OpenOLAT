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
package org.olat.course.nodes.bigbluebutton;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BigBlueButtonCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonEditController extends ActivateableTabbableDefaultController {

	public static final String ACCESS_BY_DATES = "accessByDates";
	public static final String GUEST_ACCESS_ALLOWED = "guestAccessAllowed";
	public static final String MODERATOR_START_MEETING = "moderatorStartMeeting";
	
	public static final String PANE_TAB_VCCONFIG = "pane.tab.vcconfig";
	private static final String[] paneKeys = { PANE_TAB_VCCONFIG };
	
	private TabbedPane tabPane;

	private BigBlueButtonConfigForm configCtrl;
	
	private final ModuleConfiguration config;
	
	public BigBlueButtonEditController(UserRequest ureq, WindowControl wControl, BigBlueButtonCourseNode courseNode) {
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();	
		configCtrl = new BigBlueButtonConfigForm(ureq, getWindowControl(), config);
		listenTo(configCtrl);
	}
	
	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				// do nothing
			} else if (event == Event.DONE_EVENT || event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}
	
	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VCCONFIG), configCtrl.getInitialComponent());
	}
}