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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.resource.OresHelper;
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
public class ProjNoteController extends BasicController {
	
	private static final String CMD_OPEN_WINDOW = "open.window";
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_DELETE = "delete";

	private final VelocityContainer mainVC;
	private final Link editLink;
	private Dropdown cmdsDropDown;
	private Link openNewWindowLink;
	private Link downloadLink;
	private Link deleteLink;
	
	private ProjNoteViewController noteViewCtrl;
	private ProjNoteEditController noteEditCtrl;
	private CloseableModalController cmc;
	private ProjConfirmationController deleteConfirmationCtrl;
	
	private final ProjProjectSecurityCallback secCallback;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;
	private ProjNoteInfo noteInfo;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;

	public ProjNoteController(UserRequest ureq, WindowControl wControl, ProjProjectSecurityCallback secCallback,
			ProjNoteInfo noteInfo, boolean edit, MapperKey avatarMapperKey) {
		super(ureq, wControl);
		this.noteInfo = noteInfo;
		this.secCallback = secCallback;
		this.avatarMapperKey = avatarMapperKey;
		this.formatter = Formatter.getInstance(getLocale());
		
		projectService.createActivityRead(getIdentity(), noteInfo.getNote().getArtefact());
		
		mainVC = createVelocityContainer("note");
		putInitialPanel(mainVC);
		
		editLink = LinkFactory.createButton("edit", mainVC, this);
		editLink.setIconLeftCSS("o_icon o_icon_lg o_icon_edit");
		
		cmdsDropDown = new Dropdown("cmds", null, false, getTranslator());
		cmdsDropDown.setElementCssClass("o_proj_cmds");
		cmdsDropDown.setCarretIconCSS("o_icon o_icon_commands");
		cmdsDropDown.setButton(true);
		cmdsDropDown.setEmbbeded(true);
		cmdsDropDown.setOrientation(DropdownOrientation.right);
		mainVC.put("cmds", cmdsDropDown);
		
		openNewWindowLink = LinkFactory.createToolLink(CMD_OPEN_WINDOW, translate("open.in.new.window"), this, "o_icon_content_popup");
		openNewWindowLink.setNewWindow(true, true);
		cmdsDropDown.addComponent(openNewWindowLink);
		
		downloadLink = LinkFactory.createToolLink(CMD_DOWNLOAD, translate("download"), this, "o_icon_download");
		cmdsDropDown.addComponent(downloadLink);
		
		if (secCallback.canDeleteNote(noteInfo.getNote(), noteInfo.getMembers().contains(getIdentity()))) {
			cmdsDropDown.addComponent(new Spacer("delete-spacer"));
			
			deleteLink = LinkFactory.createToolLink(CMD_DELETE, translate("delete"), this, "o_icon " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
			cmdsDropDown.addComponent(deleteLink);
		}
		
		if (edit && secCallback.canEditNote(noteInfo.getNote(), noteInfo.getMembers().contains(getIdentity()))) {
			doOpenEdit(ureq);
		} else {
			doOpenView(ureq);
		}
	}
	
	private void updateHeaderUI(UserRequest ureq) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(noteInfo.getMembers()));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "members", mainVC, null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("members"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		
		mainVC.contextPut("numReferences", noteInfo.getNumReferences());
		
		String modifiedDate = formatter.formatDateRelative(noteInfo.getNote().getArtefact().getContentModifiedDate());
		String modifiedBy = userManager.getUserDisplayName(noteInfo.getNote().getArtefact().getContentModifiedBy().getKey());
		String modified = translate("date.by", modifiedDate, modifiedBy);
		mainVC.contextPut("modified", modified);
	}

	private void doOpenView(UserRequest ureq) {
		cleanUpNoteUI();
		updateHeaderUI(ureq);
		
		noteViewCtrl = new ProjNoteViewController(ureq, getWindowControl(), noteInfo, false);
		listenTo(noteViewCtrl);
		mainVC.put("viewNote", noteViewCtrl.getInitialComponent());
		
		editLink.setVisible(secCallback.canEditNote(noteInfo.getNote(), noteInfo.getMembers().contains(getIdentity())));
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
			noteEditCtrl = new ProjNoteEditController(ureq, getWindowControl(), noteInfo.getNote(), noteInfo.getMembers(), false, true);
			listenTo(noteEditCtrl);
			mainVC.put("editNote", noteEditCtrl.getInitialComponent());
			
			editLink.setVisible(false);
		}
	}
	
	private void cleanUpNoteUI() {
		removeAsListenerAndDispose(noteViewCtrl);
		removeAsListenerAndDispose(noteEditCtrl);
		noteViewCtrl = null;
		noteEditCtrl = null;
		mainVC.remove("viewNote");
		mainVC.remove("editNote");
	}

	private boolean reloadNote() {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(noteInfo.getNote()));
		List<ProjNoteInfo> noteInfos = projectService.getNoteInfos(searchParams);
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
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editLink) {
			if (reloadNote()) {
				doOpenEdit(ureq);
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
	}

	private void doOpenWindow() {
		String url = ProjectBCFactory.getNoteUrl(noteInfo.getNote());
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
		
		String message = translate("note.delete.confirmation.message", note.getTitle());
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"note.delete.confirmation.confirm", "note.delete.confirmation.button");
		deleteConfirmationCtrl.setUserObject(note);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", deleteConfirmationCtrl.getInitialComponent(),
				true, translate("note.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, ProjNoteRef note) {
		projectService.deleteNoteSoftly(getIdentity(), note);
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
