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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.lti13.DeploymentConfigurationPermission;
import org.olat.ims.lti13.LTI13Module;
import org.olat.modules.invitation.InvitationConfigurationPermission;
import org.olat.modules.invitation.InvitationModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The controller is about who has permissions to add deployments and invites
 * someone in course.
 * 
 * Initial date: 11 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseInvitationsAndDeploymentsSettingsController extends FormBasicController {
	
	private MultipleSelectionElement invitationEnableEl;
	private MultipleSelectionElement lti13DeploymentEnableEl;
	
	private final boolean editable;
	private RepositoryEntry entry;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private RepositoryManager repositoryManager;
	
	CourseInvitationsAndDeploymentsSettingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);
		this.entry = entry;
		this.editable = editable;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("invitations.settings.title");
		setFormDescription("invitations.settings.descr");
		
		SelectionValues onKeyValues = new SelectionValues();
		onKeyValues.add(SelectionValues.entry("on", translate("on")));
		
		invitationEnableEl = uifactory.addCheckboxesHorizontal("course.invitation.enable", "course.invitation.enable", formLayout,
				onKeyValues.keys(), onKeyValues.values());
		invitationEnableEl.setEnabled(editable);
		invitationEnableEl.setVisible(invitationModule.isCourseInvitationEnabled()
				&& invitationModule.getCourseOwnerPermission() == InvitationConfigurationPermission.perResource);
		if(entry.isInvitationByOwnerWithAuthorRightsEnabled()) {
			invitationEnableEl.select("on", true);
		}
		
		lti13DeploymentEnableEl = uifactory.addCheckboxesHorizontal("course.lti.13.deployment.enable", "course.lti.13.deployment.enable", formLayout,
				onKeyValues.keys(), onKeyValues.values());
		lti13DeploymentEnableEl.setEnabled(editable);
		lti13DeploymentEnableEl.setVisible(lti13Module.isEnabled()
				&& lti13Module.getDeploymentRepositoryEntryOwnerPermission() == DeploymentConfigurationPermission.perResource);
		if(entry.isLTI13DeploymentByOwnerWithAuthorRightsEnabled()) {
			lti13DeploymentEnableEl.select("on", true);
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		entry = repositoryManager.setOptions(entry, invitationEnableEl.isAtLeastSelected(1), lti13DeploymentEnableEl.isAtLeastSelected(1));
		fireEvent(ureq, new ReloadSettingsEvent(false, false, false, false));
	}
}
