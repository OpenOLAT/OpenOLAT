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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.group.ui.main.EditSingleOrImportMembershipController;
import org.olat.group.ui.main.MemberPermissionChangeEvent;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberPermissionChoiceController extends StepFormBasicController {
	private EditSingleOrImportMembershipController permissionCtrl;

	public ImportMemberPermissionChoiceController(UserRequest ureq, WindowControl wControl,
			MembersContext membersContext, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		
		permissionCtrl = new EditSingleOrImportMembershipController(ureq, getWindowControl(), null, membersContext, rootForm);
		listenTo(permissionCtrl);

		initForm (ureq);
	}

	public boolean validate() {
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		MemberPermissionChangeEvent e = new MemberPermissionChangeEvent(null);
		permissionCtrl.collectRepoChanges(e);
		permissionCtrl.collectGroupChanges(e);
		permissionCtrl.collectCurriculumElementChanges(e);
		addToRunContext("permissions", e);
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		MemberPermissionChangeEvent e = new MemberPermissionChangeEvent(null);
		permissionCtrl.collectRepoChanges(e);
		permissionCtrl.collectGroupChanges(e);
		permissionCtrl.collectCurriculumElementChanges(e);
		int size = e.size();
		flc.contextRemove("off_warn");
		if(size == 0) {
			String warning = translate("error.select.role");
			flc.contextPut("off_warn", warning);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(permissionCtrl.getInitialFormItem());	
	}
}