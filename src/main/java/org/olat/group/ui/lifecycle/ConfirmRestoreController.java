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

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 déc. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRestoreController extends FormBasicController {
	
	private SingleSelection activationEl;
	
	private final List<BusinessGroup> businessGroups;
	
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	public ConfirmRestoreController(UserRequest ureq, WindowControl wControl, List<BusinessGroup> businessGroups) {
		super(ureq, wControl);
		this.businessGroups = businessGroups;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues modeValues = new SelectionValues();
		modeValues.add(new SelectionValue("inactive", translate("restore.to.inactive.label"), translate("restore.to.inactive.desc")));
		modeValues.add(new SelectionValue("active", translate("restore.to.active.label"), translate("restore.to.active.desc")));
		activationEl = uifactory.addCardSingleSelectHorizontal("restore.mode", formLayout, modeValues.keys(), modeValues.values(), modeValues.descriptions(), null);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("restore", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		activationEl.clearError();
		if(!activationEl.isOneSelected()) {
			activationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean activate = "active".equals(activationEl.getSelectedKey());
		for(BusinessGroup group:businessGroups) {
			boolean asOwner = businessGroupService.hasRoles(getIdentity(), group, GroupRoles.coach.name());
			if(activate) {
				businessGroupLifecycleManager.reactivateBusinessGroup(group, getIdentity(), asOwner);
			} else {
				businessGroupLifecycleManager.changeBusinessGroupStatus(group, BusinessGroupStatusEnum.inactive, getIdentity(), asOwner);
			}
		}

		if(businessGroups.size() == 1) {
			showInfo("group.reactivated");
		} else {
			showInfo("groups.reactivated", new String[] { Integer.toString(businessGroups.size()) });
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
