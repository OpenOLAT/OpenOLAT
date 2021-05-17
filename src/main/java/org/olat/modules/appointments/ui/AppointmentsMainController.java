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
package org.olat.modules.appointments.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsMainController extends BasicController {

	private final BreadcrumbedStackedPanel stackPanel;
	
	private final Controller topicsCtrl;

	public AppointmentsMainController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent,
			AppointmentsSecurityCallback secCallback, OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("main");
		
		stackPanel = new BreadcrumbedStackedPanel("app", getTranslator(), this);
		stackPanel.setInvisibleCrumb(3);
		
		if (secCallback.canSelectAppointments()) {
			topicsCtrl = new TopicsRunController(ureq, wControl, stackPanel, entry, subIdent, secCallback);
		} else {
			topicsCtrl = new TopicsRunCoachController(ureq, wControl, stackPanel, entry, subIdent, secCallback, organizerCandidateSupplier);
		}
		listenTo(topicsCtrl);
		stackPanel.pushController("topics", topicsCtrl);
		if (topicsCtrl instanceof Activateable2) {
			((Activateable2)topicsCtrl).activate(ureq, null, null);
		}
			
		mainVC.put("topics", stackPanel);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
