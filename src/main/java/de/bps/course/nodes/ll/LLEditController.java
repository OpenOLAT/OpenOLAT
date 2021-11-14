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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.ll;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Edit controller for link list course nodes.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_LLCONFIG = "pane.tab.llconfig";
	private ModuleConfiguration moduleConfiguration;
	private VelocityContainer editVc;
	private static final String[] paneKeys = { PANE_TAB_LLCONFIG };
	private TabbedPane tabPane;
	private LLEditForm llFormContr;

	public LLEditController(ModuleConfiguration moduleConfiguration, UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl);
		this.moduleConfiguration = moduleConfiguration;

		editVc = createVelocityContainer("edit");

		llFormContr = new LLEditForm(ureq, getWindowControl(), this.moduleConfiguration, course.getCourseEnvironment());
		llFormContr.addControllerListener(this);
		editVc.put("llEditForm", llFormContr.getInitialComponent());
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
		if (source == llFormContr) {
			moduleConfiguration = llFormContr.getModuleConfiguration();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_LLCONFIG), editVc);
	}

	public static boolean isConfigValid(ModuleConfiguration moduleConfig) {
		List<LLModel> linkList = (List<LLModel>) moduleConfig.get(LLCourseNode.CONF_LINKLIST);
		if (linkList != null) {
			for (LLModel link : linkList) {
				if (link.isIntern() && StringHelper.containsNonWhitespace(link.getTarget()) && 
						StringHelper.containsNonWhitespace(link.getDescription())) {
					return true;
				}
				if (link.getTarget().isEmpty() || link.getDescription().isEmpty()) { return false; }
				URL target = null;
				try {
					target = new URL(link.getTarget());
				} catch (MalformedURLException e) {
					target = null;
				}
				if (target == null) {  return false; }
			}
			return true;
		}
		return false;
	}
}
