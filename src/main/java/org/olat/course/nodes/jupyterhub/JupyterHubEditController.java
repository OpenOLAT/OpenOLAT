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
package org.olat.course.nodes.jupyterhub;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.JupyterHubCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.jupyterhub.ui.JupyterHubConfigTabController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-04-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubEditController extends ActivateableTabbableDefaultController {
	public static final String PANE_TAB_JUPYTER_HUB_CONFIG = "pane.tab.jupyterHubConfig";
	final static String[] paneKeys = { PANE_TAB_JUPYTER_HUB_CONFIG };

	private final ModuleConfiguration moduleConfiguration;
	private final JupyterHubConfigTabController jupyterHubConfig;
	private TabbedPane tabbedPane;

	public JupyterHubEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);
		this.moduleConfiguration = moduleConfiguration;
		jupyterHubConfig = new JupyterHubConfigTabController(ureq, wControl, entry, subIdent);
		listenTo(jupyterHubConfig);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (jupyterHubConfig == source) {
			if (event == Event.CANCELLED_EVENT) {
				//
			} else if (event == Event.DONE_EVENT) {
				moduleConfiguration.setStringValue(JupyterHubCourseNode.CLIENT_ID, jupyterHubConfig.getClientId());
				moduleConfiguration.setStringValue(JupyterHubCourseNode.IMAGE, jupyterHubConfig.getImage());
				moduleConfiguration.setBooleanEntry(JupyterHubCourseNode.SUPPRESS_DATA_TRANSMISSION_AGREEMENT, jupyterHubConfig.isSuppressDataTransmissionAgreement());
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
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
	public void addTabs(TabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
		tabbedPane.addTab(translate("pane.tab.jupyterHubConfig"), "o_sel_jupyterhub_configuration", jupyterHubConfig.getInitialComponent());
	}
}
