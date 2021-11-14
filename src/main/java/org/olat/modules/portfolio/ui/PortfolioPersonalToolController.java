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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.home.ReusableHomeController;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioPersonalToolController extends BasicController implements Activateable2, ReusableHomeController {

	private final TooledStackedPanel stackPanel;
	private final PortfolioHomeController overviewCtrl;
	
	public PortfolioPersonalToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		stackPanel = new TooledStackedPanel("portfolioStackPanel", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(true);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setInvisibleCrumb(0);
		putInitialPanel(stackPanel);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("PortfolioV2", 0l);
		WindowControl swControl = addToHistory(ureq, ores, null, getWindowControl(), true);
		overviewCtrl = new PortfolioHomeController(ureq, swControl, stackPanel);
		listenTo(overviewCtrl);
		stackPanel.pushController(translate("portfolio.root.breadcrump"), overviewCtrl);
		stackPanel.setCssClass("o_portfolio");
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries.size() > 0) {
			String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Portfolio".equalsIgnoreCase(resName) || "PortfolioV2".equalsIgnoreCase(resName)) {
				entries = entries.subList(1, entries.size());
			}
		}
		
		if(overviewCtrl != null) {
			overviewCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
