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
package org.olat.modules.project.ui.wizard;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ui.ProjMemberRolesController;

/**
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class AddMemberRolesController extends StepFormBasicController {
	
	private final ProjMemberRolesController rolesCtrl;
	
	private final ProjectRolesContext context;

	public AddMemberRolesController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			ProjectRolesContext context, boolean ownerAllowed) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		this.context = context;
		
		rolesCtrl = new ProjMemberRolesController(ureq, getWindowControl(), rootForm, context.getProjectRoles(), ownerAllowed);
		listenTo(rolesCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(rolesCtrl.getInitialFormItem());	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<ProjectRole> roles = rolesCtrl.getRoles();
		context.setProjectRoles(roles);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
}