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
package org.olat.course.nodes.mediasite;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_VCCONFIG = "pane.tab.vcconfig";
	private static final String[] paneKeys = { PANE_TAB_VCCONFIG }; 
	
	private MediaSiteConfigController mediaSiteConfigController;
	private TabbedPane tabbedPane;
	
	public MediaSiteEditController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, MediaSiteCourseNode courseNode, ICourse course) {
		super(ureq, wControl);
		mediaSiteConfigController = new MediaSiteConfigController(ureq, wControl, config, courseNode, course);
		listenTo(mediaSiteConfigController);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VCCONFIG), mediaSiteConfigController.getInitialComponent());
		
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mediaSiteConfigController && event.equals(Event.DONE_EVENT)) {
			mediaSiteConfigController.getUpdatedConfig();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
