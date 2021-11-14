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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Wrapper for the resource's access control.
 * 
 * <P>
 * Initial Date:  26 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupEditAccessAndBookingController extends FormBasicController {
	
	private final boolean managed;
	private BusinessGroup businessGroup;
	private AccessConfigurationController configController;
	
	@Autowired
	private AccessControlModule acModule;
	
	public BusinessGroupEditAccessAndBookingController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, "tab_bgBooking");
		setTranslator(Util.createPackageTranslator(AccessConfigurationController.class, getLocale(), getTranslator()));
		this.businessGroup = businessGroup;
		managed = BusinessGroupManagedFlag.isManaged(businessGroup, BusinessGroupManagedFlag.bookings);

		if(acModule.isEnabled()) {
			initConfigurationController(ureq);
		}
		
		initForm(ureq);
	}
	
	public int getNumOfBookingConfigurations() {
		return configController == null ? 0 : configController.getNumOfBookingConfigurations();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("accesscontrol.title");
		setFormDescription("accesscontrol_group.desc");
		setFormContextHelp("Group Administration#gruppensystem_buchung_ag");
		formLayout.setElementCssClass("o_block_large_bottom");

		if(configController != null) {
			formLayout.add("access", configController.getInitialFormItem());
			
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
			uifactory.addFormSubmitButton("save", formLayout);
		}
	}
	
	private void initConfigurationController(UserRequest ureq) {
		removeAsListenerAndDispose(configController);
		
		OLATResource resource = businessGroup.getResource();
		boolean waitingList = businessGroup.getWaitingListEnabled();
		configController = new AccessConfigurationController(ureq, getWindowControl(), resource, businessGroup.getName(), !waitingList, !managed, mainForm);
		listenTo(configController);
	}
	
	public void updateBusinessGroup(BusinessGroup updatedGroup) {
		this.businessGroup = updatedGroup;
		boolean waitingList = updatedGroup.getWaitingListEnabled();
		configController.setAllowPaymentMethod(!waitingList);
	}
	
	public boolean isPaymentMethodInUse() {
		return configController.isPaymentMethodInUse();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(configController != null) {
			configController.commitChanges();
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		if(configController != null) {
			initConfigurationController(ureq);
			flc.add("access", configController.getInitialFormItem());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == configController) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
}
