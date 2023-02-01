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


import static java.util.Collections.singletonList;

import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.project.ui.event.OpenNoteEvent;
import org.olat.modules.project.ui.event.OpenProjectEvent;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectDashboardController extends BasicController implements Activateable2 {
	
	public static final Event SHOW_ALL = new Event("show.all");
	
	private static final String CMD_EDIT_PROJECT = "edit.project";
	private static final String CMD_STATUS_DONE = "status.done";
	private static final String CMD_REOPEN = "reopen";
	private static final String CMD_STATUS_DELETED = "status.deleted";

	private final BreadcrumbedStackedPanel stackPanel;
	private VelocityContainer mainVC;
	private Dropdown cmdsDropDown;
	private Link editProjectLink;
	private Link membersManagementLink;
	private Link statusDoneLink;
	private Link reopenLink;
	private Link statusDeletedLink;
	private UsersPortraitsComponent usersPortraitCmp;
	
	private CloseableModalController cmc;
	private ProjProjectEditController editCtrl;
	private ProjConfirmationController doneConfirmationCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	private DialogBoxController reopenConfirmationCtrk;
	private ProjMembersManagementController membersManagementCtrl;
	private ProjFileWidgetController fileWidgetCtrl;
	private ProjFileAllController fileAllCtrl;
	private ProjNoteWidgetController noteWidgetCtrl;
	private ProjNoteAllController noteAllCtrl;
	private ProjTimelineController timelineCtrl;

	private ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final MapperKey avatarMapperKey;
	private Date lastVisitDate;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private MapperService mapperService;

	public ProjProjectDashboardController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			ProjProject project, ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.project = project;
		this.secCallback = secCallback;
		this.avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));
		
		ProjProjectUserInfo projectUserInfo = projectService.getOrCreateProjectUserInfo(project, getIdentity());
		lastVisitDate = projectUserInfo.getLastVisitDate();
		projectUserInfo.setLastVisitDate(new Date());
		projectUserInfo = projectService.updateProjectUserInfo(projectUserInfo);
		
		mainVC = createVelocityContainer("project_dashboard");
		putProjectToVC();
		putInitialPanel(mainVC);
		
		//Commands
		cmdsDropDown = new Dropdown("cmds", null, false, getTranslator());
		cmdsDropDown.setCarretIconCSS("o_icon o_icon_commands");
		cmdsDropDown.setButton(true);
		cmdsDropDown.setEmbbeded(true);
		cmdsDropDown.setOrientation(DropdownOrientation.right);
		mainVC.put("cmds", cmdsDropDown);
		
		editProjectLink = LinkFactory.createToolLink(CMD_EDIT_PROJECT, translate("project.edit"), this, "o_icon_edit");
		cmdsDropDown.addComponent(editProjectLink);
		
		membersManagementLink = LinkFactory.createToolLink(CMD_EDIT_PROJECT, translate("members.management"), this, "o_icon_membersmanagement");
		cmdsDropDown.addComponent(membersManagementLink);
		
		statusDoneLink = LinkFactory.createToolLink(CMD_STATUS_DONE, translate("project.set.status.done"), this,
				ProjectUIFactory.getStatusIconCss(ProjectStatus.done));
		cmdsDropDown.addComponent(statusDoneLink);
		
		reopenLink = LinkFactory.createToolLink(CMD_REOPEN, translate("project.reopen"), this,
				ProjectUIFactory.getStatusIconCss(ProjectStatus.active));
		cmdsDropDown.addComponent(reopenLink);
		
		statusDeletedLink = LinkFactory.createToolLink(CMD_STATUS_DELETED, translate("project.set.status.deleted"),
				this, ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
		cmdsDropDown.addComponent(statusDeletedLink);
		updateCmdsUI();
		
		List<Identity> members = projectService.getMembers(project, ProjectRole.PROJECT_ROLES);
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(members);
		usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users", mainVC, null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setUsers(portraitUsers);
		
		//Widgets
		fileWidgetCtrl = new ProjFileWidgetController(ureq, wControl, project, secCallback, lastVisitDate);
		listenTo(fileWidgetCtrl);
		mainVC.put("files", fileWidgetCtrl.getInitialComponent());
		
		noteWidgetCtrl = new ProjNoteWidgetController(ureq, wControl, stackPanel, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(noteWidgetCtrl);
		mainVC.put("notes", noteWidgetCtrl.getInitialComponent());
		
		// Timeline
		timelineCtrl = new ProjTimelineController(ureq, wControl, project, members, avatarMapperKey);
		listenTo(timelineCtrl);
		mainVC.put("timeline", timelineCtrl.getInitialComponent());
	}
	
	public void reload(UserRequest ureq) {
		reload(ureq, null);
	}
	
	public void reload(UserRequest ureq, Controller exceptCtrl) {
		if (exceptCtrl != this) {
			putProjectToVC();
		}
		if (exceptCtrl != fileWidgetCtrl) {
			fileWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != noteWidgetCtrl) {
			noteWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != timelineCtrl) {
			timelineCtrl.reload(ureq);
		}
	}
	
	private void putProjectToVC() {
		mainVC.contextPut("projectTitle", project.getTitle());
		mainVC.contextPut("projectTeaser", project.getTeaser());
	}
	
	private void updateCmdsUI() {
		editProjectLink.setVisible(secCallback.canViewProjectMetadata());
		membersManagementLink.setVisible(secCallback.canEditMembers());
		
		statusDoneLink.setVisible(secCallback.canEditProjectStatus() && ProjectStatus.active == project.getStatus());
		reopenLink.setVisible(secCallback.canEditProjectStatus() && ProjectStatus.done == project.getStatus());
		statusDeletedLink.setVisible(secCallback.canDeleteProject() && ProjectStatus.deleted != project.getStatus());
		
		boolean visibleLinks = StreamSupport.stream(cmdsDropDown.getComponents().spliterator(), false).anyMatch(Component::isVisible);
		cmdsDropDown.setVisible(visibleLinks);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String typeName = entry.getOLATResourceable().getResourceableTypeName();
		if (ProjectBCFactory.TYPE_MEMBERS_MANAGEMENT.equalsIgnoreCase(typeName) && membersManagementLink.isVisible()) {
			doOpenMembersManagement(ureq);
		} else if (ProjectBCFactory.TYPE_FILES.equalsIgnoreCase(typeName)) {
			doOpenFiles(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			fileAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (ProjectBCFactory.TYPE_NOTES.equalsIgnoreCase(typeName)) {
			doOpenNotes(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			noteAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == membersManagementCtrl) {
			if (event instanceof OpenProjectEvent) {
				fireEvent(ureq, event);
			}
		} else if (editCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (doneConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doSetStatusDone(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doSetStatusDeleted(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == reopenConfirmationCtrk ) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doReopen(ureq);
			}
		} else if(cmc == source) {
			cleanUp();
		} else if (source == fileWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenFiles(ureq);
			} else if (event == Event.CHANGED_EVENT) {
				reload(ureq, fileWidgetCtrl);
			}
		} else if (source == noteWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenNotes(ureq);
			} else if (event instanceof OpenNoteEvent) {
				doOpenNote(ureq, (OpenNoteEvent)event);
			}
		} else if (event instanceof OpenArtefactEvent) {
			OpenArtefactEvent oae = (OpenArtefactEvent)event;
			doOpenArtefact(ureq, oae.getArtefact());
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(doneConfirmationCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		doneConfirmationCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editProjectLink) {
			doEditProject(ureq);
		} else if (source == membersManagementLink) {
			doOpenMembersManagement(ureq);
		} else if (source == statusDoneLink) {
			doConfirmStatusDone(ureq);
		} else if (source == reopenLink) {
			doConfirmReopen(ureq);
		} else if (source == statusDeletedLink) {
			doConfirmStatusDeleted(ureq);
		} else if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == this) {
					reload(ureq);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		mapperService.cleanUp(singletonList(avatarMapperKey));
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	private void doEditProject(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		project = projectService.getProject(project);
		putProjectToVC();
		editCtrl = new ProjProjectEditController(ureq, getWindowControl(), project, !secCallback.canEditProjectMetadata());
		listenTo(editCtrl);
		
		String title = translate("project.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenMembersManagement(UserRequest ureq) {
		removeAsListenerAndDispose(membersManagementCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_MEMBERS_MANAGEMENT), null);
		membersManagementCtrl = new ProjMembersManagementController(ureq, swControl, stackPanel, project, secCallback);
		listenTo(membersManagementCtrl);
		stackPanel.pushController(translate("members.management"), membersManagementCtrl);
	}

	private void doConfirmStatusDone(UserRequest ureq) {
		if (guardModalController(doneConfirmationCtrl)) return;
		
		project = projectService.getProject(project);
		putProjectToVC();
		
		// Project has not the right status anymore to set the target status.
		if (ProjectStatus.active != project.getStatus()) {
			fireEvent(ureq, new OpenProjectEvent(project));
			return;
		}
		
		int numOfMembers = projectService.countMembers(project);
		String message = translate("project.set.status.done.message", Integer.toString(numOfMembers));
		doneConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"project.set.status.done.confirm", "project.set.status.done.button");
		listenTo(doneConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", doneConfirmationCtrl.getInitialComponent(),
				true, translate("project.set.status.done.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmStatusDeleted(UserRequest ureq) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		project = projectService.getProject(project);
		putProjectToVC();
		
		// Project has not the right status anymore to set the target status.
		if (ProjectStatus.deleted == project.getStatus()) {
			fireEvent(ureq, new OpenProjectEvent(project));
			return;
		}
		
		int numOfMembers = projectService.countMembers(project);
		String message = translate("project.set.status.deleted.message", Integer.toString(numOfMembers));
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"project.set.status.deleted.confirm", "project.set.status.deleted.button");
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", deleteConfirmationCtrl.getInitialComponent(),
				true, translate("project.set.status.deleted.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmReopen(UserRequest ureq) {
		String title = translate("project.reopen.title");
		String msg = translate("project.reopen.text");
		reopenConfirmationCtrk = activateOkCancelDialog(ureq, title, msg, reopenConfirmationCtrk);
	}
	
	private void doSetStatusDone(UserRequest ureq) {
		project = projectService.setStatusDone(getIdentity(), project);
		fireEvent(ureq, new OpenProjectEvent(project));
	}
	
	private void doReopen(UserRequest ureq) {
		project = projectService.reopen(getIdentity(), project);
		fireEvent(ureq, new OpenProjectEvent(project));
	}

	private void doSetStatusDeleted(UserRequest ureq) {
		project = projectService.setStatusDeleted(getIdentity(), project);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doOpenFiles(UserRequest ureq) {
		removeAsListenerAndDispose(fileAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_FILES), null);
		fileAllCtrl = new ProjFileAllController(ureq, swControl, project, secCallback, lastVisitDate);
		listenTo(fileAllCtrl);
		stackPanel.pushController(translate("file.all.title"), fileAllCtrl);
	}
	
	private void doOpenNotes(UserRequest ureq) {
		removeAsListenerAndDispose(noteAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_NOTES), null);
		noteAllCtrl = new ProjNoteAllController(ureq, swControl, stackPanel, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(noteAllCtrl);
		stackPanel.pushController(translate("note.all.title"), noteAllCtrl);
	}
	
	private void doOpenArtefact(UserRequest ureq, ProjArtefact artefact) {
		stackPanel.popUpToController(this);
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setArtefacts(List.of(artefact));
		ProjArtefactItems artefacts = projectService.getArtefactItems(searchParams);
		
		if (artefacts.getFiles() != null && !artefacts.getFiles().isEmpty()) {
			doOpenFiles(ureq);
			List<ContextEntry> contextEntries = List.of(ProjectBCFactory.createFileCe(artefacts.getFiles().get(0)));
			fileAllCtrl.activate(ureq, contextEntries, null);
		} else if (artefacts.getNotes() != null && !artefacts.getNotes().isEmpty()) {
			doOpenNotes(ureq);
			List<ContextEntry> contextEntries = List.of(ProjectBCFactory.createNoteCe(artefacts.getNotes().get(0)));
			noteAllCtrl.activate(ureq, contextEntries, null);
		}
	}
	
	private void doOpenNote(UserRequest ureq, OpenNoteEvent event) {
		doOpenNotes(ureq);
		noteAllCtrl.activate(ureq, null, event);
	}

}
