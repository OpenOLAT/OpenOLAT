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
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 9 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupLifecycleAdminController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final BusinessGroupAdminProcessController processCtrl;
	private final BusinessGroupInactivateAdminController inactivationCtrl;
	private final BusinessGroupSoftDeleteAdminController softDeletionCtrl;
	private final BusinessGroupLifecycleTypeOptionsController typeOptionsCtrl;
	private final BusinessGroupDefinitivelyDeleteAdminController definitiveDeletionCtrl;
	
	public BusinessGroupLifecycleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin_overview");
		
		processCtrl = new BusinessGroupAdminProcessController(ureq, wControl);
		listenTo(processCtrl);
		mainVC.put("process", processCtrl.getInitialComponent());
		
		typeOptionsCtrl = new BusinessGroupLifecycleTypeOptionsController(ureq, wControl);
		listenTo(typeOptionsCtrl);
		mainVC.put("types", typeOptionsCtrl.getInitialComponent());
		
		inactivationCtrl = new BusinessGroupInactivateAdminController(ureq, wControl);
		listenTo(inactivationCtrl);
		mainVC.put("inactivation", inactivationCtrl.getInitialComponent());
		
		softDeletionCtrl = new BusinessGroupSoftDeleteAdminController(ureq, wControl);
		listenTo(softDeletionCtrl);
		mainVC.put("soft.delete", softDeletionCtrl.getInitialComponent());
		
		definitiveDeletionCtrl = new BusinessGroupDefinitivelyDeleteAdminController(ureq, wControl);
		listenTo(definitiveDeletionCtrl);
		mainVC.put("hard.delete", definitiveDeletionCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == inactivationCtrl || source == softDeletionCtrl || source == definitiveDeletionCtrl) {
			processCtrl.updateUI();
		}
	}

}
