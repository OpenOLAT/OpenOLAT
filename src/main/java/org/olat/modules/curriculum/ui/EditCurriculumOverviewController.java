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
import org.olat.modules.curriculum.CurriculumSecurityCallback;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumOverviewController extends BasicController {

	private TabbedPane tabPane;
	
	private EditCurriculumController editMetadataCtrl;
	private CurriculumUserManagementController userManagementCtrl;
	
	private Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	
	public EditCurriculumOverviewController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		
		VelocityContainer mainVC = createVelocityContainer("curriculum_overview");
		
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		
		editMetadataCtrl = new EditCurriculumController(ureq, getWindowControl(), curriculum, secCallback);
		listenTo(editMetadataCtrl);
		tabPane.addTab(translate("curriculum.metadata"), editMetadataCtrl);
		initTabPane(ureq);
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
	}
	
	private void initTabPane(UserRequest ureq) {
		tabPane.addTab(ureq, translate("tab.user.management"), uureq -> {
			userManagementCtrl = new CurriculumUserManagementController(uureq, getWindowControl(), curriculum, secCallback);
			listenTo(userManagementCtrl);
			return userManagementCtrl.getInitialComponent();
		});
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
