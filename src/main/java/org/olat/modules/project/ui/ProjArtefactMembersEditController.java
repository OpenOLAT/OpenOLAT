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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactMembersEditController extends FormBasicController {
	
	private MultipleSelectionElement membersEl;
	
	private final ProjArtefact artefact;
	private final Set<Identity> currentMembers;
	private final List<Identity> projectMembers;
	private Collection<String> selectedKeys;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;

	public ProjArtefactMembersEditController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjArtefact artefact, Set<Identity> currentMembers) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		this.artefact = artefact;
		this.currentMembers = currentMembers;
		projectMembers = projectService.getMembers(artefact.getProject(), ProjectRole.PROJECT_ROLES);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		membersEl = ProjectUIFactory.createMembersElement(uifactory, formLayout, userManager, projectMembers, currentMembers);
		membersEl.addActionListener(FormEvent.ONCLICK);
		selectedKeys = membersEl.getSelectedKeys();
	}
	
	//Hack to avoid empty selection
	public void initSelection() {
		selectedKeys.forEach(key -> membersEl.select(key, true));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == membersEl) {
			if (validateFormLogic(ureq)) {
				selectedKeys = membersEl.getSelectedKeys();
				doUpdateMembers();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		membersEl.clearError();
		if (!membersEl.isAtLeastSelected(1)) {
			membersEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doUpdateMembers() {
		List<IdentityRef> selectedMembers = membersEl.getSelectedKeys().stream()
				.map(key -> new IdentityRefImpl(Long.valueOf(key)))
				.collect(Collectors.toList());
		projectService.updateMembers(getIdentity(), artefact, selectedMembers);
	}

}
