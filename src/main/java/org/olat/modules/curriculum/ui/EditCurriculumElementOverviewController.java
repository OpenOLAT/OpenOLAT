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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;

/**
 * 
 * Initial date: 16 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumElementOverviewController extends BasicController {
	
	private TabbedPane tabPane;
	
	private EditCurriculumElementController metadataCtrl;
	private CurriculumElementResourceListController resourcesCtrl;
	private CurriculumElementUserManagementController userManagementCtrl;
	
	private CurriculumElement element;
	private final CurriculumSecurityCallback secCallback;
	
	public EditCurriculumElementOverviewController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element, Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.element = element;
		this.secCallback = secCallback;
		
		VelocityContainer mainVC = createVelocityContainer("curriculum_element_overview");
		
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		
		metadataCtrl = new EditCurriculumElementController(ureq, getWindowControl(),
				element, element.getParent(), curriculum, secCallback);
		listenTo(metadataCtrl);
		tabPane.addTab(translate("curriculum.element.metadata"), metadataCtrl);
		initTabPane();
		
		mainVC.put("tabs", tabPane);
		
		putInitialPanel(mainVC);
	}
	
	private void initTabPane() {
		tabPane.addTab(translate("tab.resources"), uureq -> {
			resourcesCtrl = new CurriculumElementResourceListController(uureq, getWindowControl(), element, secCallback);
			listenTo(resourcesCtrl);
			return resourcesCtrl.getInitialComponent();
		});
		
		tabPane.addTab(translate("tab.user.management"), uureq -> {
			userManagementCtrl = new CurriculumElementUserManagementController(uureq, getWindowControl(), element, secCallback);
			listenTo(userManagementCtrl);
			return userManagementCtrl.getInitialComponent();
		});
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	

}
