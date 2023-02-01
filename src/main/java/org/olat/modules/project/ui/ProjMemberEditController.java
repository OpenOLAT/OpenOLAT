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
package org.olat.modules.project.ui;

import java.util.Set;

import org.olat.admin.user.UserShortDescription;
import org.olat.admin.user.UserShortDescription.Rows;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.user.DisplayPortraitController;

/**
 * 
 * Initial date: 2 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMemberEditController extends FormBasicController {
	
	private ProjMemberRolesController rolesCtrl;

	private final ProjProject project;
	private final Identity member;
	private final Set<ProjectRole> initialRoles;

	public ProjMemberEditController(UserRequest ureq, WindowControl wControl, ProjProject project, Identity member, Set<ProjectRole> initialRoles) {
		super(ureq, wControl, "member_edit");
		this.project = project;
		this.member = member;
		this.initialRoles = initialRoles;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
		
			DisplayPortraitController portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), member, true, false);
			listenTo(portraitCtr);
			layoutCont.getFormItemComponent().put("portrait", portraitCtr.getInitialComponent());
			
			Rows additionalRows = Rows.builder().build();
			UserShortDescription userDescrCtrl = new UserShortDescription(ureq, getWindowControl(), member, additionalRows);
			listenTo(userDescrCtrl);
			layoutCont.getFormItemComponent().put("userDescr", userDescrCtrl.getInitialComponent());
			
			FormLayoutContainer titleCont = FormLayoutContainer.createDefaultFormLayout("title", getTranslator());
			titleCont.setFormTitle(translate("member.edit.roles.title", project.getTitle()));
			formLayout.add("title", titleCont);
			
			boolean ownerAllowed = !initialRoles.contains(ProjectRole.invitee);
			rolesCtrl = new ProjMemberRolesController(ureq, getWindowControl(), mainForm, initialRoles, ownerAllowed);
			listenTo(rolesCtrl);
			formLayout.add("roles", rolesCtrl.getInitialFormItem());	
		}
		
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsWrapperCont.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	public Identity getMember() {
		return member;
	}

	public Set<ProjectRole> getRoles() {
		return rolesCtrl.getRoles();
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
