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
package org.olat.group.ui.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.ui.LTI13ResourceAccessController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupEditAccessController extends BasicController {

	private LTI13ResourceAccessController lti13AccessCtrl;
	private BusinessGroupEditAccessAndBookingController accessCtrl;
	
	@Autowired
	private LTI13Module lti13Module;
	
	public BusinessGroupEditAccessController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("edit_access");
		
		accessCtrl = new BusinessGroupEditAccessAndBookingController(ureq, getWindowControl(), businessGroup);
		listenTo(accessCtrl);
		mainVC.put("accessAndBooking", accessCtrl.getInitialComponent());
		
		if(lti13Module.isEnabled()) {
			lti13AccessCtrl = new LTI13ResourceAccessController(ureq, getWindowControl(), businessGroup);
			listenTo(lti13AccessCtrl);
			mainVC.put("lti13Access", lti13AccessCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}
	
	public boolean isPaymentMethodInUse() {
		return accessCtrl.isPaymentMethodInUse();
	}
	
	public int getNumOfBookingConfigurations() {
		return accessCtrl.getNumOfBookingConfigurations();
	}
	
	public void updateBusinessGroup(BusinessGroup updatedGroup) {
		accessCtrl.updateBusinessGroup(updatedGroup);
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
