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

import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.user.UsersAvatarController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjDecisionEditController extends FormBasicController {

	private ProjDecisionContentEditController contentCtrl;
	private ProjArtefactReferencesController referenceCtrl;
	private ProjArtefactMembersEditController memberCtrl;
	private ProjArtefactMetadataController metadataCtrl;

	private final ProjProject project;
	private final ProjDecision decision;
	private final Set<Identity> members;
	private final boolean readOnly;
	private final boolean withOpenInSameWindow;
	private Boolean referenceOpen = Boolean.FALSE;
	private Boolean memberOpen = Boolean.FALSE;
	private Boolean metadataOpen = Boolean.FALSE;
	
	@Autowired
	private ProjectService projectService;
	private UsersAvatarController memberViewCtrl;
	
	public ProjDecisionEditController(UserRequest ureq, WindowControl wControl, ProjProject project,
			Set<Identity> members, boolean withOpenInSameWindow) {
		super(ureq, wControl, "edit");
		this.project = project;
		this.decision = null;
		this.members = members;
		this.readOnly = false;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}

	public ProjDecisionEditController(UserRequest ureq, WindowControl wControl, ProjDecision decision,
			Set<Identity> members, boolean readOnly, boolean withOpenInSameWindow) {
		super(ureq, wControl, "edit");
		this.project = decision.getArtefact().getProject();
		this.decision = decision;
		this.members = members;
		this.readOnly = readOnly;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}

	public ProjDecision getDecision() {
		return contentCtrl.getDecision();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentCtrl = new ProjDecisionContentEditController(ureq, getWindowControl(), mainForm, project, decision, readOnly);
		listenTo(contentCtrl);
		formLayout.add("content", contentCtrl.getInitialFormItem());
		
		ProjArtefact artefact = decision != null? decision.getArtefact(): null;
		referenceCtrl = new ProjArtefactReferencesController(ureq, getWindowControl(), mainForm, project, artefact, false, readOnly, withOpenInSameWindow);
		listenTo(referenceCtrl);
		formLayout.add("reference", referenceCtrl.getInitialFormItem());
		flc.contextPut("referenceOpen", referenceOpen);
		
		if (readOnly) {
			memberViewCtrl = new UsersAvatarController(ureq, getWindowControl(), mainForm, members);
			listenTo(memberViewCtrl);
			formLayout.add("member", memberViewCtrl.getInitialFormItem());
		} else {
			List<Identity> projectMembers = projectService.getMembers(project, ProjectRole.PROJECT_ROLES);
			memberCtrl = new ProjArtefactMembersEditController(ureq, getWindowControl(), mainForm, projectMembers, members, null);
			listenTo(memberCtrl);
			formLayout.add("member", memberCtrl.getInitialFormItem());
		}
		flc.contextPut("memberOpen", memberOpen);
		
		if (artefact != null) {
			metadataCtrl = new ProjArtefactMetadataController(ureq, getWindowControl(), mainForm, artefact);
			listenTo(metadataCtrl);
			formLayout.add("metadata", metadataCtrl.getInitialFormItem());
			flc.contextPut("metadataOpen", metadataOpen);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		if (!readOnly) {
			if (decision != null) {
				uifactory.addFormSubmitButton("save", buttonLayout);
			} else {
				uifactory.addFormSubmitButton("create", buttonLayout);
			}
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		} else {
			uifactory.addFormCancelButton("close", buttonLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String referenceOpenVal = ureq.getParameter("referenceOpen");
			if (StringHelper.containsNonWhitespace(referenceOpenVal)) {
				referenceOpen = Boolean.valueOf(referenceOpenVal);
				flc.contextPut("referenceOpen", referenceOpen);
			}
			String memberOpenVal = ureq.getParameter("memberOpen");
			if (StringHelper.containsNonWhitespace(memberOpenVal)) {
				memberOpen = Boolean.valueOf(memberOpenVal);
				flc.contextPut("memberOpen", memberOpen);
				if (memberOpen.booleanValue()) {
					if (memberCtrl != null) {
						memberCtrl.initSelection();
					}
				}
			}
			String metadataOpenVal = ureq.getParameter("metadataOpen");
			if (StringHelper.containsNonWhitespace(metadataOpenVal)) {
				metadataOpen = Boolean.valueOf(metadataOpenVal);
				flc.contextPut("metadataOpen", metadataOpen);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		contentCtrl.formOK(ureq);
		ProjDecision decision = contentCtrl.getDecision();
		referenceCtrl.save(decision.getArtefact());
		memberCtrl.save(decision.getArtefact());
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
