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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;

/**
 * 
 * Description:<br>
 * Add a check box to the standard access control controller
 * 
 * <P>
 * Initial Date:  26 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-1,2: access control of resources
public class BusinessGroupEditAccessController extends FormBasicController {
	
	private AccessConfigurationController configController;
	
	public BusinessGroupEditAccessController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(AccessConfigurationController.class, getLocale(), getTranslator()));
		
		AccessControlModule acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		if(acModule.isEnabled()) {
			OLATResource resource = OLATResourceManager.getInstance().findResourceable(businessGroup);
			configController = new AccessConfigurationController(ureq, wControl, resource, businessGroup.getName(), mainForm);
			listenTo(configController);
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(configController != null) {
			formLayout.add(configController.getInitialFormItem());
			uifactory.addSpacerElement("spacer1", formLayout, false);
		}

		setFormTitle("accesscontrol.title");
		setFormDescription("accesscontrol.desc");
		setFormContextHelp(AccessConfigurationController.class.getPackage().getName(), "accesscontrol_group.html", "chelp.accesscontrol_group.hover");

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(configController != null) {
			configController.formOK(ureq);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
