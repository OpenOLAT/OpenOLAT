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
package org.olat.modules.portfolio.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.ui.AccessRightsEditController;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessRightsEditStepController extends StepFormBasicController {

	private final AccessRightsContext rightsContext;
	private final AccessRightsEditController accessRightsCtrl;
	
	public AccessRightsEditStepController(UserRequest ureq, WindowControl wControl, Binder binder, Form form, StepsRunContext runContext) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "access_rights_step");

		rightsContext = (AccessRightsContext)runContext.get("rightsContext");
		Identity selectedIdentity = null;
		if(rightsContext.getIdentities() != null && rightsContext.getIdentities().size() == 1) {
			selectedIdentity = rightsContext.getIdentities().get(0);
		}
		accessRightsCtrl = new AccessRightsEditController(ureq, getWindowControl(), form, binder, selectedIdentity);
		listenTo(accessRightsCtrl);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("access_rights", accessRightsCtrl.getInitialFormItem());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		accessRightsCtrl.getInitialFormItem().clearError();
		List<AccessRightChange> accessRightChanges = accessRightsCtrl.getChanges();
		if(accessRightChanges == null || accessRightChanges.isEmpty()) {
			accessRightsCtrl.getInitialFormItem().setErrorKey("error.missing.permissions", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<AccessRightChange> accessRightChanges = accessRightsCtrl.getChanges();
		rightsContext.setAccessRightChanges(accessRightChanges);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
