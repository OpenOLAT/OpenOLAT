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
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.user.UsersAvatarController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteEditController extends FormBasicController {
	
	private FormLink closeLink;
	private FormLink cancelLink;

	private ProjNoteContentEditController contentCtrl;
	private ProjArtefactReferencesController referenceCtrl;
	private UsersAvatarController memberViewCtrl;
	private ProjArtefactMembersEditController memberEditCtrl;
	private ProjArtefactMetadataController metadataCtrl;

	private final ProjectBCFactory bcFactory;
	private final boolean template;
	private final ProjNote note;
	private final Set<Identity> members;
	private final boolean firstEdit;
	private final boolean withOpenInSameWindow;
	private LockResult lockEntry;
	private Boolean referenceOpen = Boolean.FALSE;
	private Boolean memberOpen = Boolean.FALSE;
	private Boolean metadataOpen = Boolean.FALSE;
	
	@Autowired
	private ProjectService projectService;

	public ProjNoteEditController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory, ProjNote note,
			Set<Identity> members, boolean firstEdit, boolean withOpenInSameWindow) {
		super(ureq, wControl, "edit");
		this.bcFactory = bcFactory;
		this.template = note.getArtefact().getProject().isTemplatePrivate() || note.getArtefact().getProject().isTemplatePublic();
		this.note = note;
		this.members = members;
		this.firstEdit = firstEdit;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ProjNote.class, note.getKey());
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, ureq.getIdentity(), "", getWindow());
		if (lockEntry.isSuccess()) {
			initForm(ureq);
		}
	}
	
	public ProjNote getNote() {
		return note;
	}

	public boolean isFirstEdit() {
		return firstEdit;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentCtrl = new ProjNoteContentEditController(ureq, getWindowControl(), mainForm, note, UUID.randomUUID().toString());
		listenTo(contentCtrl);
		formLayout.add("content", contentCtrl.getInitialFormItem());
		
		referenceCtrl = new ProjArtefactReferencesController(ureq, getWindowControl(), mainForm, bcFactory,
				note.getArtefact().getProject(), note.getArtefact(), true, false, withOpenInSameWindow);
		listenTo(referenceCtrl);
		formLayout.add("reference", referenceCtrl.getInitialFormItem());
		flc.contextPut("referenceOpen", referenceOpen);
		
		if (template) {
			memberViewCtrl = new UsersAvatarController(ureq, getWindowControl(), mainForm, members);
			listenTo(memberViewCtrl);
			formLayout.add("member", memberViewCtrl.getInitialFormItem());
			flc.contextPut("memberOpen", memberOpen);
		} else {
			List<Identity> projectMembers = projectService.getMembers(note.getArtefact().getProject(), ProjectRole.PROJECT_ROLES);
			memberEditCtrl = new ProjArtefactMembersEditController(ureq, getWindowControl(), mainForm, bcFactory, projectMembers, members, note.getArtefact());
			listenTo(memberEditCtrl);
			formLayout.add("member", memberEditCtrl.getInitialFormItem());
			flc.contextPut("memberOpen", memberOpen);
		}
		
		metadataCtrl = new ProjArtefactMetadataController(ureq, getWindowControl(), mainForm, note.getArtefact());
		listenTo(metadataCtrl);
		formLayout.add("metadata", metadataCtrl.getInitialFormItem());
		flc.contextPut("metadataOpen", metadataOpen);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		if (firstEdit) {
			closeLink = uifactory.addFormLink("create", buttonLayout, Link.BUTTON);
			closeLink.setPrimary(true);
			closeLink.setElementCssClass("o_sel_notice_close");
			
			cancelLink = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
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
				if (memberOpen.booleanValue() && memberEditCtrl != null) {
					memberEditCtrl.initSelection();
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == closeLink) {
			doSave();
			fireEvent(ureq, FormEvent.DONE_EVENT);
		} else if (source == cancelLink) {
			projectService.deleteNotePermanent(note);
			fireEvent(ureq, FormEvent.CANCELLED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
		super.doDispose();
	}

	public void doSave() {
		contentCtrl.doSave();
	}

}
