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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.certificationprogram.CertificationProgram;

/**
 * 
 * Initial date: 10 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMessagesController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	
	private final CertificationProgramRemindersController remindersCtrl;
	private final CertificationProgramNotificationsController notificationsCtrl;
	
	public CertificationProgramMessagesController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("messages");
		initInfosPanel(ureq, certificationProgram);
		
		notificationsCtrl = new CertificationProgramNotificationsController(ureq, wControl, certificationProgram);
		listenTo(notificationsCtrl);
		mainVC.put("notifications", notificationsCtrl.getInitialComponent());
		
		remindersCtrl = new CertificationProgramRemindersController(ureq, wControl, certificationProgram);
		listenTo(remindersCtrl);
		mainVC.put("reminders", remindersCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private void initInfosPanel(UserRequest ureq, CertificationProgram certificationProgram) {
		InfoPanel panel = new InfoPanel("configurationInfos");
		panel.setTitle(translate("messages.configuration.overview.title"));
		panel.setInformations(CertificationUIFactory.getConfiguration(getTranslator(), certificationProgram));
		panel.setPersistedStatusId(ureq, "certification-program-infos-overview-v1");
		mainVC.put("configurationInfos", panel);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
