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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.tu;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.TUCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.tu.IframeTunnelController;

/**
 * Description:<BR/>
 * The tunneling edit controller is used to edit a course building block of typ tu
 * <P/>
 * Initial Date:  Oct 12, 2004
 *
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class TUEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_TUCONFIG = "pane.tab.tuconfig";
	
	private static final String[] paneKeys = {PANE_TAB_TUCONFIG};

	private ModuleConfiguration config;	
	private VelocityContainer myContent;
	private final BreadcrumbPanel stackPanel;

	private TUConfigForm tuConfigForm;	
	private TUCourseNode courseNode;
	private TabbedPane myTabbedPane;
	private LayoutMain3ColsController previewLayoutCtr;
	private Link previewButton;
	private final UserCourseEnvironment euce;

	public TUEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			TUCourseNode tuCourseNode, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.config = config;
		this.courseNode = tuCourseNode;
		this.stackPanel = stackPanel;
		this.euce = euce;
		
		myContent = createVelocityContainer("edit");
		previewButton = LinkFactory.createButtonSmall("command.preview", myContent, this);
		previewButton.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		tuConfigForm = new TUConfigForm(ureq, wControl, config);
		listenTo(tuConfigForm);
		myContent.put("tuConfigForm", tuConfigForm.getInitialComponent());

		// Enable preview button only if node configuration is valid
		if (!(tuCourseNode.isConfigValid().isError())) myContent.contextPut("showPreviewButton", Boolean.TRUE);
		else myContent.contextPut("showPreviewButton", Boolean.FALSE);
		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewButton) { // those must be links
			Controller tunnelRunCtr;
			if (config.getBooleanSafe(TUConfigForm.CONFIG_IFRAME)) {  
				tunnelRunCtr = new IframeTunnelController(ureq, getWindowControl(), config);
			} else {					
				tunnelRunCtr = new TURunController(getWindowControl(), config, ureq, courseNode, euce);
			}
			if (previewLayoutCtr != null) previewLayoutCtr.dispose();
			// preview layout: only center column (col3) used
			previewLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), tunnelRunCtr);
			stackPanel.pushController(translate("preview"), previewLayoutCtr);
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == tuConfigForm) {
			if (event == Event.DONE_EVENT) {
				config = tuConfigForm.getUpdatedConfig();
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				// form valid -> node config valid -> show preview button
				myContent.contextPut("showPreviewButton", Boolean.TRUE);
			}
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_TUCONFIG), myContent);
	}

	@Override
	protected void doDispose() {
		if (previewLayoutCtr != null) {
			previewLayoutCtr.dispose();
			previewLayoutCtr = null;
		}
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
	
}