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
package org.olat.course.nodes.page;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.PageCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 4 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditController extends ActivateableTabbableDefaultController {
	

	public static final String CONFIG_KEY_PAGE = "page";
	
	public static final String PANE_TAB_CEPCONFIG = "pane.tab.cepconfig";

	private static final String[] paneKeys = { PANE_TAB_CEPCONFIG };
	
	private TabbedPane myTabbedPane;
	private final PageContentConfigurationController contentCtrl;
	
	public PageEditController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			ICourse course, PageCourseNode courseNode) {
		super(ureq, wControl);
		
		contentCtrl = new PageContentConfigurationController(ureq, getWindowControl(), userCourseEnv, course, courseNode);
		listenTo(contentCtrl);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CEPCONFIG), "o_sel_cep_content_config", contentCtrl.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(contentCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(source instanceof NodeEditController editCtrl) {
			if(event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				contentCtrl.updateTitle();
			}
		}
	}
}
