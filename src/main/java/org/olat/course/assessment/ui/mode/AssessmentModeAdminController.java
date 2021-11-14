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
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 06.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeAdminController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final AssessmentModeAdminListController modeListCtrl;
	private final AssessmentModeAdminSettingsController settingsCtrl;
	
	public AssessmentModeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		settingsCtrl = new AssessmentModeAdminSettingsController(ureq, wControl);
		listenTo(settingsCtrl);
		
		modeListCtrl = new AssessmentModeAdminListController(ureq, wControl);
		listenTo(modeListCtrl);
		
		mainVC = createVelocityContainer("admin");
		mainVC.put("settings", settingsCtrl.getInitialComponent());
		mainVC.put("list", modeListCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
