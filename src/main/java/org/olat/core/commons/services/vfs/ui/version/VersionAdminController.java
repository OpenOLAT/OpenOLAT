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
package org.olat.core.commons.services.vfs.ui.version;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff FXOLAT-127: file versions maintenance tool
public class VersionAdminController extends BasicController {
	
	private final VersionSettingsForm settingsForm;
	private final VersionMaintenanceForm maintenanceForm;
	
	private VelocityContainer mainVC;

	public VersionAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// use combined translator from system admin main
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));

		settingsForm = new VersionSettingsForm(ureq, getWindowControl());
		listenTo(settingsForm);
		
		maintenanceForm = new VersionMaintenanceForm(ureq, this.getWindowControl());
		listenTo(maintenanceForm);
		
		mainVC = createVelocityContainer("admin");
		mainVC.put("settings", settingsForm.getInitialComponent());
		mainVC.put("maintenance", maintenanceForm.getInitialComponent());
		
		putInitialPanel(mainVC);
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
