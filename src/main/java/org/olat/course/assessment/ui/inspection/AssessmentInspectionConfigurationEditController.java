/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationEditController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final TabbedPane tabbedPane;
	
	private AssessmentInspectionConfiguration configuration;
	
	private AssessmentInspectionConfigurationEditAccessController accessCtrl;
	private AssessmentInspectionConfigurationEditGeneralController generalCtrl;
	private AssessmentInspectionConfigurationEditSafeExamBrowserController safeExamBrowserCtrl;
	
	public AssessmentInspectionConfigurationEditController(UserRequest ureq, WindowControl wControl,
			AssessmentInspectionConfiguration newConfiguration, RepositoryEntry entry) {
		super(ureq, wControl);
		this.configuration = newConfiguration;
		
		mainVC = createVelocityContainer("edit");
		tabbedPane = new TabbedPane("segments", getLocale());
		mainVC.put("segments", tabbedPane);
		if(newConfiguration == null || newConfiguration.getKey() == null
				|| !StringHelper.containsNonWhitespace(newConfiguration.getName()) ) {
			mainVC.contextPut("name", translate("new.configuration"));
		} else {
			mainVC.contextPut("name", newConfiguration.getName());
		}
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.general"), "o_sel_inspection_general", uureq -> {
			generalCtrl = new AssessmentInspectionConfigurationEditGeneralController(uureq, getWindowControl(), configuration, entry);
			listenTo(generalCtrl);
			return generalCtrl;
		}, true);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.access"), "o_sel_inspection_access", uureq -> {
			accessCtrl = new AssessmentInspectionConfigurationEditAccessController(uureq, getWindowControl(), configuration);
			listenTo(accessCtrl);
			return accessCtrl;
		}, true);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.edit.seb"), "o_sel_inspection_seb", uureq -> {
			safeExamBrowserCtrl = new AssessmentInspectionConfigurationEditSafeExamBrowserController(uureq, getWindowControl(), configuration);
			listenTo(safeExamBrowserCtrl);
			return safeExamBrowserCtrl;
		}, true);

		putInitialPanel(mainVC);
		updateSegments();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.CANCELLED_EVENT) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(generalCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				configuration = generalCtrl.getConfiguration();
				updateSegments();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void updateSegments() {
		boolean persisted = configuration != null && configuration.getKey() != null;
		tabbedPane.setEnabled(1, persisted);
		tabbedPane.setEnabled(2, persisted);
	}

}
