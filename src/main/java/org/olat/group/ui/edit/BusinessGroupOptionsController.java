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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.lti13.DeploymentConfigurationPermission;
import org.olat.ims.lti13.LTI13Module;
import org.olat.modules.invitation.InvitationConfigurationPermission;
import org.olat.modules.invitation.InvitationModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupOptionsController extends FormBasicController {

	private MultipleSelectionElement invitationEnableEl;
	private MultipleSelectionElement lti13DeploymentEnableEl;
	
	private BusinessGroup businessGroup;
	
	private final boolean readOnly;

	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public BusinessGroupOptionsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup, boolean readOnly) {
		super(ureq, wControl);
		this.readOnly = readOnly;
		this.businessGroup = businessGroup;
		initForm(ureq);
	}
	
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("invitations.settings.title");
		setFormDescription("invitations.settings.descr");
		
		SelectionValues onKeyValues = new SelectionValues();
		onKeyValues.add(SelectionValues.entry("on", translate("on")));

		invitationEnableEl = uifactory.addCheckboxesHorizontal("business.group.invitation.enable", "business.group.invitation.enable", formLayout,
				onKeyValues.keys(), onKeyValues.values());
		invitationEnableEl.setEnabled(!readOnly);
		invitationEnableEl.setVisible(invitationModule.isBusinessGroupInvitationEnabled()
				&& invitationModule.getBusinessGroupCoachPermission() == InvitationConfigurationPermission.perResource);
		if(businessGroup.isInvitationByCoachWithAuthorRightsEnabled()) {
			invitationEnableEl.select("on", true);
		}
		
		lti13DeploymentEnableEl = uifactory.addCheckboxesHorizontal("business.group.lti.13.deployment.enable", "business.group.lti.13.deployment.enable", formLayout,
				onKeyValues.keys(), onKeyValues.values());
		lti13DeploymentEnableEl.setEnabled(!readOnly);
		lti13DeploymentEnableEl.setVisible(lti13Module.isEnabled()
				&& lti13Module.getDeploymentBusinessGroupCoachPermission() == DeploymentConfigurationPermission.perResource);
		if(businessGroup.isLTI13DeploymentByCoachWithAuthorRightsEnabled()) {
			lti13DeploymentEnableEl.select("on", true);
		}
	
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", formLayout);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		businessGroup = businessGroupService.updateOptions(businessGroup,
				invitationEnableEl.isAtLeastSelected(1), lti13DeploymentEnableEl.isAtLeastSelected(1));
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
