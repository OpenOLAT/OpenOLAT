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
package org.olat.course.nodes.appointments.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.modules.appointments.ui.AppointmentsMainController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsRunController extends BasicController {

	private Controller appointmentsMainCtrl;

	public AppointmentsRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent,
			AppointmentsSecurityCallback secCallback, OrganizerCandidateSupplier organizerCandidateSupplier) {
		super(ureq, wControl);
		
		appointmentsMainCtrl = new AppointmentsMainController(ureq, wControl, entry, subIdent, secCallback, organizerCandidateSupplier);
		putInitialPanel(appointmentsMainCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
