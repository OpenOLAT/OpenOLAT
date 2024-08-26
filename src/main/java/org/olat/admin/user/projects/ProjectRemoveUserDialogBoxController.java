/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.admin.user.projects;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjProject;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 26, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ProjectRemoveUserDialogBoxController extends FormBasicController {

	private final Identity leavingIdentity;
	private final List<ProjProject> projectsToLeave;

	@Autowired
	private UserManager userManager;

	public ProjectRemoveUserDialogBoxController(UserRequest ureq, WindowControl wControl, Identity leavingIdentity, List<ProjProject> projectsToLeave) {
		super(ureq, wControl);
		this.leavingIdentity = leavingIdentity;
		this.projectsToLeave = projectsToLeave;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder projectsToLeaveNames = new StringBuilder();
		for (ProjProject project : projectsToLeave) {
			if (!projectsToLeaveNames.isEmpty()) projectsToLeaveNames.append(", ");
			projectsToLeaveNames.append(project.getTitle());
		}

		String identityName = userManager.getUserDisplayName(leavingIdentity);
		String removeText;

		removeText = translate("remove.user.text", identityName, projectsToLeaveNames.toString());

		setFormTranslatedWarning(removeText);

		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormCancelButton("cancel", buttonsContainer, ureq, getWindowControl());
		uifactory.addFormSubmitButton("deleteButton", "ok", buttonsContainer);
	}

	public List<ProjProject> getProjectsToLeave() {
		return projectsToLeave;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
