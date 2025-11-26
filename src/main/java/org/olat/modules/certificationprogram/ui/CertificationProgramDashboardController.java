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
package org.olat.modules.certificationprogram.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.certificationprogram.CertificationProgram;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramDashboardController extends BasicController {

	private final ActiveMembersWidgetController activeMembersCtrl;
	
	public CertificationProgramDashboardController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("program_dashboard");
		
		activeMembersCtrl = new ActiveMembersWidgetController(ureq, getWindowControl(), certificationProgram);
		listenTo(activeMembersCtrl);
		mainVC.put("courseCoach", activeMembersCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public void reload(UserRequest ureq) {
		if (activeMembersCtrl != null) {
			activeMembersCtrl.reload(ureq);
		}
	}
}
