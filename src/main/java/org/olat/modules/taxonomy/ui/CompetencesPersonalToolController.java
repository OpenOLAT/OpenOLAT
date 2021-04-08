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
package org.olat.modules.taxonomy.ui;

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
 * Initial date: 19.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetencesPersonalToolController extends BasicController implements Activateable2, ReusableHomeController {

	private final TooledStackedPanel stackPanel;
	private final CompetencesOverviewController competencesController;
	
	public CompetencesPersonalToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		stackPanel = new TooledStackedPanel("competencesStackPanel", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(true);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setInvisibleCrumb(1);
		stackPanel.setCssClass("o_competences");
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("CompetencesOverview", 0l);
		WindowControl swControl = addToHistory(ureq, ores, null, getWindowControl(), true);
		competencesController = new CompetencesOverviewController(ureq, swControl, stackPanel, getIdentity(), false, true);
		listenTo(competencesController);
		
		stackPanel.pushController(translate("competences.root.breadcrump"), competencesController);		
		putInitialPanel(stackPanel);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// No events
	}

	@Override
	protected void doDispose() {
		// Nothing to dispose here
	}

}
