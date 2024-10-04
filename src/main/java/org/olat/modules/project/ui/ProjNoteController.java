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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteController extends FormBasicController {

	private FormToggle editToggle;
	private DropdownItem cmdsDropDown;
	private FormLink openNewWindowLink;
	private FormLink downloadLink;
	private FormLink deleteLink;
	
	private ProjNoteViewController noteViewCtrl;
	private ProjNoteEditController noteEditCtrl;
	private CloseableModalController cmc;
	private ConfirmationController deleteConfirmationCtrl;
	
	private final ProjectBCFactory bcFactory;
	private final ProjProjectSecurityCallback secCallback;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;
	private ProjNoteInfo noteInfo;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;

	public ProjNoteController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProjectSecurityCallback secCallback, ProjNoteInfo noteInfo, boolean edit, MapperKey avatarMapperKey) {
		super(ureq, wControl, "note");
		this.bcFactory = bcFactory;
		this.noteInfo = noteInfo;
		this.secCallback = secCallback;
		this.avatarMapperKey = avatarMapperKey;
		this.formatter = Formatter.getInstance(getLocale());
		
		projectService.createActivityRead(getIdentity(), noteInfo.getNote().getArtefact());
		
		initForm(ureq);
		
		if (edit && secCallback.canEditNote(noteInfo.getNote())) {
			doOpenEdit(ureq);
		} else {
			doOpenView(ureq);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		editToggle = uifactory.addToggleButton("edit", "edit.mode", translate("on"), translate("off"), formLayout);
		flc.contextPut("editLableFor", editToggle.getFormDispatchId());
		
		cmdsDropDown = uifactory.addDropdownMenu("cmds", null, null, formLayout, getTranslator());
		cmdsDropDown.setElementCssClass("o_proj_cmds");
		cmdsDropDown.setCarretIconCSS("o_icon o_icon_commands");
		cmdsDropDown.setButton(true);
		cmdsDropDown.setEmbbeded(true);
		cmdsDropDown.setOrientation(DropdownOrientation.right);
		
		openNewWindowLink = uifactory.addFormLink("open.in.new.window", formLayout);
		openNewWindowLink.setIconLeftCSS("o_icon o_icon-fw o_icon_content_popup");
		openNewWindowLink.setNewWindow(true, true, true);
		cmdsDropDown.addElement(openNewWindowLink);
		
		downloadLink = uifactory.addFormLink("download", formLayout);
		downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		cmdsDropDown.addElement(downloadLink);
		
		if (secCallback.canDeleteNote(noteInfo.getNote(), getIdentity())) {
			cmdsDropDown.addElement(new SpacerItem("delete-spacer"));
			
			deleteLink = uifactory.addFormLink("delete", formLayout);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
			cmdsDropDown.addElement(deleteLink);
		}
	}
	
	private void updateHeaderUI(UserRequest ureq) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(noteInfo.getMembers()));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "members", null, null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		flc.put("members", usersPortraitCmp);
		
		flc.contextPut("numReferences", noteInfo.getNumReferences());
		
		String modifiedDate = formatter.formatDateRelative(noteInfo.getNote().getArtefact().getContentModifiedDate());
		String modifiedBy = userManager.getUserDisplayName(noteInfo.getNote().getArtefact().getContentModifiedBy().getKey());
		String modified = StringHelper.escapeHtml(translate("date.by", modifiedDate, modifiedBy));
		flc.contextPut("modified", modified);
	}

	private void doOpenView(UserRequest ureq) {
		cleanUpNoteUI();
		updateHeaderUI(ureq);
		
		noteViewCtrl = new ProjNoteViewController(ureq, getWindowControl(), mainForm, bcFactory, noteInfo, false);
		listenTo(noteViewCtrl);
		flc.add("viewNote", noteViewCtrl.getInitialFormItem());
		flc.contextPut("edit", Boolean.FALSE);
		
		editToggle.setVisible(secCallback.canEditNote(noteInfo.getNote()));
	}
	
	private void doOpenEdit(UserRequest ureq) {
		cleanUpNoteUI();
		updateHeaderUI(ureq);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ProjNote.class, noteInfo.getNote().getKey());
		LockEntry lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().getLockEntry(ores, "");
		
		if (lockEntry != null) {
			String displayName = userManager.getUserDisplayName(lockEntry.getOwner().getKey());
			showInfo("error.note.locked", displayName);
			doOpenView(ureq);
		} else {
			noteEditCtrl = new ProjNoteEditController(ureq, getWindowControl(), mainForm, bcFactory, noteInfo.getNote(),
					noteInfo.getMembers(), false, true);
			listenTo(noteEditCtrl);
			flc.add("editNote", noteEditCtrl.getInitialFormItem());
			flc.contextPut("edit", Boolean.TRUE);
		}
	}
	
	private void cleanUpNoteUI() {
		if (noteViewCtrl != null) {
			flc.remove(noteViewCtrl.getInitialFormItem());
		}
		if (noteEditCtrl != null) {
			flc.remove(noteEditCtrl.getInitialFormItem());
		}
		removeAsListenerAndDispose(noteViewCtrl);
		removeAsListenerAndDispose(noteEditCtrl);
		noteViewCtrl = null;
		noteEditCtrl = null;
	}

	private boolean reloadNote() {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(noteInfo.getNote()));
		List<ProjNoteInfo> noteInfos = projectService.getNoteInfos(searchParams, ProjArtefactInfoParams.ALL);
		if (noteInfos.isEmpty()) {
			// Should not happen. We do without error message for the moment.
			return false;
		}
		
		noteInfo = noteInfos.get(0);
		return true;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == noteEditCtrl) {
			if (event == Event.DONE_EVENT) {
				if (reloadNote()) {
					doOpenView(ureq);
				}
			} else if (event instanceof OpenArtefactEvent) {
				fireEvent(ureq, event);
			}
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq, (ProjNoteRef)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editToggle) {
			if (!editToggle.isOn()) {
				if (noteEditCtrl != null) {
					noteEditCtrl.doSave();
				}
			}
			
			if (reloadNote()) {
				if (editToggle.isOn()) {
					doOpenEdit(ureq);
				} else {
					doOpenView(ureq);
				}
			}
		} else if (source == openNewWindowLink) {
			doOpenWindow();
		} else if (source == downloadLink) {
			doDownload(ureq);
		} else if (source == deleteLink) {
			if (reloadNote()) {
				doConfirmDelete(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenWindow() {
		String url = bcFactory.getNoteUrl(noteInfo.getNote());
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
	
	private void doDownload(UserRequest ureq) {
		StringMediaResource resource = ProjectUIFactory.createMediaResource(noteInfo.getNote());
		ureq.getDispatchResult().setResultingMediaResource(resource);
		
		projectService.createActivityDownload(getIdentity(), noteInfo.getNote().getArtefact());
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		ProjNote note = noteInfo.getNote();
		if (ProjectStatus.deleted == note.getArtefact().getStatus()) {
			return;
		}
		
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("note.delete.confirmation.message", StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(getTranslator(), note))),
				translate("note.delete.confirmation.confirm"),
				translate("note.delete.confirmation.button"), true);
		deleteConfirmationCtrl.setUserObject(note);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("note.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, ProjNoteRef note) {
		projectService.deleteNoteSoftly(getIdentity(), note);
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
