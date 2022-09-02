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
package org.olat.course.member.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.member.wizard.InvitationContext.TransientInvitation;
import org.olat.course.member.wizard.InvitationUsersListInfosDataModel.InfosCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationUsersListInfosController extends StepFormBasicController {
	
	@Autowired
	private UserManager userManager;
	
	private final InvitationContext context;
	
	public InvitationUsersListInfosController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, InvitationContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.context = context;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InfosCols.email));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InfosCols.firstName));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InfosCols.lastName));
		
		// External users
		FormLayoutContainer externalCont = FormLayoutContainer.createVerticalFormLayout("external", getTranslator());
		externalCont.setElementCssClass("o_external_user_data");
		externalCont.setFormTitle(translate("import.external.users"));
		formLayout.add(externalCont);
		InvitationUsersListInfosDataModel externalUsersTableModel = new InvitationUsersListInfosDataModel(tableColumnModel, getLocale());
		FlexiTableElement externalTableEl = uifactory.addTableElement(getWindowControl(), "externalUsers", externalUsersTableModel, getTranslator(), externalCont);
		externalTableEl.setCustomizeColumns(false);
		externalTableEl.setNumOfRowsEnabled(false);
		
		// Existing users
		FormLayoutContainer existingCont = FormLayoutContainer.createVerticalFormLayout("existing", getTranslator());
		existingCont.setElementCssClass("o_existing_user_data");
		existingCont.setFormWarning(translate("warn.users.already.exist"));
		existingCont.setFormTitle(translate("import.external.users.existing"));
		formLayout.add(existingCont);
		
		InvitationUsersListInfosDataModel existingUsersTableModel = new InvitationUsersListInfosDataModel(tableColumnModel, getLocale());
		FlexiTableElement existingUsersTableEl = uifactory.addTableElement(getWindowControl(), "existingUsers", existingUsersTableModel, getTranslator(), existingCont);
		existingUsersTableEl.setCustomizeColumns(false);
		existingUsersTableEl.setNumOfRowsEnabled(false);
		
		// Triage
		List<TransientInvitation> externalUsers = new ArrayList<>();
		List<TransientInvitation> existingUsers = new ArrayList<>();
		for(TransientInvitation invitation:context.getInvitations()) {
			if(invitation.getIdentity() != null && !invitation.isIdentityInviteeOnly()) {
				existingUsers.add(invitation);
			} else {
				externalUsers.add(invitation);
			}
		}
		externalUsersTableModel.setObjects(externalUsers);
		externalCont.setVisible(!externalUsers.isEmpty());
		
		existingUsersTableModel.setObjects(existingUsers);
		existingCont.setVisible(!existingUsers.isEmpty());
	}

	@Override
	protected void formNext(UserRequest ureq) {
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
