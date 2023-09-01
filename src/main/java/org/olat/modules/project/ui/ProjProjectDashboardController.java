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


import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjDecisionRef;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjMilestoneRef;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjToDoRef;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.component.ProjAvatarComponent;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.project.ui.event.OpenNoteEvent;
import org.olat.modules.project.ui.event.OpenProjectEvent;
import org.olat.modules.project.ui.event.QuickStartEvents;
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
	private static final String CMD_EDIT_MEMBER_MANAGEMENT = "member.management";
	private static final String CMD_COPY_PROJECT = "copy.project";
	private static final String CMD_TEMPLATE = "template";
	private static final String CMD_STATUS_DONE = "status.done";
	private static final String CMD_REOPEN = "reopen";
	private static final String CMD_STATUS_DELETED = "status.deleted";

	private final BreadcrumbedStackedPanel stackPanel;
	private VelocityContainer mainVC;
	private Dropdown cmdsDropDown;
	private Link editProjectLink;
	private Link membersManagementLink;
	private Link copyProjectLink;
	private Link templateLink;
	private Link statusDoneLink;
	private Link reopenLink;
	private Link statusDeletedLink;
	private UsersPortraitsComponent usersPortraitCmp;
	
	private CloseableModalController cmc;
	private ProjProjectEditController editCtrl;
	private ProjConfirmationController doneConfirmationCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	private DialogBoxController reopenConfirmationCtrl;
	private ProjMembersManagementController membersManagementCtrl;
	private ContextualSubscriptionController subscriptionCtrl;
	private ProjQuickStartWidgetController quickWidgetCtrl;
	private ProjFileWidgetController fileWidgetCtrl;
	private ProjFileAllController fileAllCtrl;
	private ProjToDoWidgetController toDoWidgetCtrl;
	private ProjToDoAllController toDoAllCtrl;
	private ProjDecisionWidgetController decisionWidgetCtrl;
	private ProjDecisionAllController decisionAllCtrl;
	private ProjNoteWidgetController noteWidgetCtrl;
	private ProjNoteAllController noteAllCtrl;
	private ProjCalendarWidgetController calendarWidgetCtrl;
	private ProjCalendarAllController calendarAllCtrl;
	private ProjWhiteboardController whiteboardCtrl;
	private ProjTimelineController timelineCtrl;

	private ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final boolean createForEnabled;
	private final MapperKey avatarMapperKey;
	private final ProjProjectImageMapper projectImageMapper;
	private final String projectMapperUrl;
	private Date lastVisitDate;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private DrawioModule drawioModule;

	public ProjProjectDashboardController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			ProjProject project, ProjProjectSecurityCallback secCallback, boolean createForEnabled) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.project = project;
		this.secCallback = secCallback;
		this.createForEnabled = createForEnabled;
		this.avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));
		this.projectImageMapper = new ProjProjectImageMapper(projectService);
		this.projectMapperUrl = registerCacheableMapper(ureq, ProjProjectImageMapper.DEFAULT_ID, projectImageMapper,
				ProjProjectImageMapper.DEFAULT_EXPIRATION_TIME);
		
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
		
		editProjectLink = LinkFactory.createToolLink(CMD_EDIT_PROJECT, translate(ProjectUIFactory.templateSuffix("project.edit", project)), this, "o_icon_edit");
		cmdsDropDown.addComponent(editProjectLink);
		
		membersManagementLink = LinkFactory.createToolLink(CMD_EDIT_MEMBER_MANAGEMENT, translate("members.management"), this, "o_icon_membersmanagement");
		cmdsDropDown.addComponent(membersManagementLink);
		
		copyProjectLink = LinkFactory.createToolLink(CMD_COPY_PROJECT, translate("project.copy"), this, "o_icon_copy");
		cmdsDropDown.addComponent(copyProjectLink);
		
		templateLink = LinkFactory.createToolLink(CMD_TEMPLATE, translate("project.save.template"), this, "o_icon_template");
		cmdsDropDown.addComponent(templateLink);
		
		statusDoneLink = LinkFactory.createToolLink(CMD_STATUS_DONE, translate(ProjectUIFactory.templateSuffix("project.set.status.done", project)), this,
				ProjectUIFactory.getStatusIconCss(ProjectStatus.done));
		cmdsDropDown.addComponent(statusDoneLink);
		
		reopenLink = LinkFactory.createToolLink(CMD_REOPEN, translate(ProjectUIFactory.templateSuffix("project.reopen", project)), this,
				ProjectUIFactory.getStatusIconCss(ProjectStatus.active));
		cmdsDropDown.addComponent(reopenLink);
		
		statusDeletedLink = LinkFactory.createToolLink(CMD_STATUS_DELETED, translate(ProjectUIFactory.templateSuffix("project.set.status.deleted", project)),
				this, ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
		cmdsDropDown.addComponent(statusDeletedLink);
		updateCmdsUI();
		
		List<Identity> members = projectService.getMembers(project, ProjectRole.PROJECT_ROLES);
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(members);
		usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users", mainVC, null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setUsers(portraitUsers);
		
		if (secCallback.canSubscribe()) {
			subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(),
					projectService.getSubscriptionContext(project),
					projectService.getPublisherData(project));
			listenTo(subscriptionCtrl);
			mainVC.put("subscription", subscriptionCtrl.getInitialComponent());
		}
		
		//Widgets
		if (secCallback.canViewFiles() || secCallback.canViewToDos() || secCallback.canViewDecisions()
				|| secCallback.canViewNotes() || secCallback.canViewAppointments() || secCallback.canViewMilestones()) {
			quickWidgetCtrl = new ProjQuickStartWidgetController(ureq, wControl, project, secCallback);
			listenTo(quickWidgetCtrl);
			mainVC.put("quick", quickWidgetCtrl.getInitialComponent());
		}
		
		if (secCallback.canViewFiles()) {
			fileWidgetCtrl = new ProjFileWidgetController(ureq, wControl, project, secCallback, lastVisitDate, avatarMapperKey);
			listenTo(fileWidgetCtrl);
			mainVC.put("files", fileWidgetCtrl.getInitialComponent());
		}
		
		if (secCallback.canViewToDos()) {
			toDoWidgetCtrl = new ProjToDoWidgetController(ureq, wControl, project, secCallback, lastVisitDate, avatarMapperKey);
			listenTo(toDoWidgetCtrl);
			mainVC.put("toDos", toDoWidgetCtrl.getInitialComponent());
		}
		
		if (secCallback.canViewDecisions()) {
			decisionWidgetCtrl = new ProjDecisionWidgetController(ureq, wControl, project, secCallback, lastVisitDate, avatarMapperKey);
			listenTo(decisionWidgetCtrl);
			mainVC.put("decisions", decisionWidgetCtrl.getInitialComponent());
		}
		
		if (secCallback.canViewNotes()) {
			noteWidgetCtrl = new ProjNoteWidgetController(ureq, wControl, stackPanel, project, secCallback, lastVisitDate, avatarMapperKey);
			listenTo(noteWidgetCtrl);
			mainVC.put("notes", noteWidgetCtrl.getInitialComponent());
		}
		
		if (secCallback.canViewAppointments() || secCallback.canViewMilestones()) {
			calendarWidgetCtrl = new ProjCalendarWidgetController(ureq, wControl, project, secCallback);
			listenTo(calendarWidgetCtrl);
			mainVC.put("calendar", calendarWidgetCtrl.getInitialComponent());
		}
		
		// Whiteboard
		if (drawioModule.isEnabled() && secCallback.canViewWhiteboard()) {
			whiteboardCtrl = new ProjWhiteboardController(ureq, wControl, project, secCallback);
			listenTo(whiteboardCtrl);
			mainVC.put("whiteboard", whiteboardCtrl.getInitialComponent());
		}
		
		// Timeline
		if (secCallback.canViewTimeline()) {
			timelineCtrl = new ProjTimelineController(ureq, wControl, project, members, avatarMapperKey);
			listenTo(timelineCtrl);
			mainVC.put("timeline", timelineCtrl.getInitialComponent());
		}
	}
	
	public void reload(UserRequest ureq) {
		reload(ureq, null);
	}
	
	public void reload(UserRequest ureq, Controller exceptCtrl) {
		if (exceptCtrl != this) {
			project = projectService.getProject(project);
			putProjectToVC();
		}
		if (exceptCtrl != quickWidgetCtrl) {
			quickWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != fileWidgetCtrl) {
			fileWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != toDoWidgetCtrl) {
			toDoWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != decisionWidgetCtrl) {
			decisionWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != noteWidgetCtrl) {
			noteWidgetCtrl.reload(ureq);
		}
		if (exceptCtrl != calendarWidgetCtrl) {
			calendarWidgetCtrl.reload();
		}
		if (exceptCtrl != whiteboardCtrl) {
			whiteboardCtrl.reload(ureq);
		}
		if (exceptCtrl != timelineCtrl) {
			timelineCtrl.reload(ureq);
		}
	}
	
	private void putProjectToVC() {
		stackPanel.changeDisplayname(project.getTitle(), null, this);
		
		mainVC.contextPut("projectExternalRef", project.getExternalRef());
		mainVC.contextPut("projectTitle", project.getTitle());
		mainVC.contextPut("status", ProjectUIFactory.translateStatus(getTranslator(), project.getStatus()));
		mainVC.contextPut("statusCssClass", "o_proj_project_status_" + project.getStatus().name());
		Object deletedMessageI18nKey = ProjectStatus.deleted == project.getStatus()
				? ProjectUIFactory.templateSuffix("project.message.deleted", project)
				: null;
		mainVC.contextPut("deletedMessageI18nKey", deletedMessageI18nKey);
		mainVC.contextPut("template", project.isTemplatePrivate() || project.isTemplatePublic());
		if (secCallback.canViewProjectMetadata()) {
			mainVC.contextPut("projectTeaser", project.getTeaser());
		}
		
		String backgroundUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.background);
		mainVC.contextPut("backgroundUrl", backgroundUrl);
		String avatarUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.avatar);
		Size size = backgroundUrl != null? Size.large: Size.medium;
		mainVC.put("avatar", new ProjAvatarComponent("avatar", project, avatarUrl, size, true));
	}
	
	private void updateCmdsUI() {
		editProjectLink.setVisible(secCallback.canViewProjectMetadata());
		membersManagementLink.setVisible(secCallback.canEditMembers());
		copyProjectLink.setVisible(secCallback.canCopyProject());
		templateLink.setVisible(secCallback.canCreateTemplate());
		
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
			if (secCallback.canViewFiles()) {
				doOpenFiles(ureq, null);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				fileAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if (ProjectBCFactory.TYPE_TODOS.equalsIgnoreCase(typeName)) {
			if (secCallback.canViewToDos()) {
				doOpenToDos(ureq, null);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				toDoAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if (ProjectBCFactory.TYPE_DECISIONS.equalsIgnoreCase(typeName)) {
			if (secCallback.canViewDecisions()) {
				doOpenDecisions(ureq, null);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				decisionAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if (ProjectBCFactory.TYPE_NOTES.equalsIgnoreCase(typeName)) {
			if (secCallback.canViewNotes()) {
				doOpenNotes(ureq, null);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				noteAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if (ProjectBCFactory.TYPE_CALENDAR.equalsIgnoreCase(typeName)) {
			if (secCallback.canViewAppointments() || secCallback.canViewMilestones()) {
				doOpenCalendar(ureq, null, null);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				calendarAllCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			}
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
		} else if (source == reopenConfirmationCtrl ) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doReopen(ureq);
			}
		} else if (source == quickWidgetCtrl) {
			if (event == QuickStartEvents.CALENDAR_EVENT) {
				doOpenCalendar(ureq, null, null);
			} else if (event == QuickStartEvents.TODOS_EVENT) {
				doOpenToDos(ureq, null);
			} else if (event == QuickStartEvents.DECISIONS_EVENT) {
				doOpenDecisions(ureq, null);
			} else if (event == QuickStartEvents.NOTES_EVENT) {
				doOpenNotes(ureq, null);
			} else if (event == QuickStartEvents.FILES_EVENT) {
				doOpenFiles(ureq, null);
			} else if (event == FormEvent.CHANGED_EVENT) {
				reload(ureq, quickWidgetCtrl);
			}
		} else if (source == fileWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenFiles(ureq, null);
			} else if (event == Event.CHANGED_EVENT) {
				reload(ureq, fileWidgetCtrl);
			}
		} else if (source == toDoWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenToDos(ureq, null);
			} else if (event == Event.CHANGED_EVENT) {
				reload(ureq, toDoWidgetCtrl);
			}
		} else if (source == decisionWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenDecisions(ureq, null);
			} else if (event == Event.CHANGED_EVENT) {
				reload(ureq, decisionWidgetCtrl);
			}
		} else if (source == noteWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenNotes(ureq, null);
			} else if (event == Event.CHANGED_EVENT) {
				reload(ureq, noteWidgetCtrl);
			} else if (event instanceof OpenNoteEvent oEvent) {
				doOpenNotes(ureq, oEvent.getNote());
			}
		} else if (source == calendarWidgetCtrl) {
			if (event == SHOW_ALL) {
				doOpenCalendar(ureq, null, null);
			} else if (event == Event.CHANGED_EVENT) {
				reload(ureq, calendarWidgetCtrl);
			}
		} else if (source == whiteboardCtrl) {
			if (event == Event.CHANGED_EVENT) {
				reload(ureq, whiteboardCtrl);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		if (event instanceof OpenArtefactEvent oae) {
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
		} else if (source == copyProjectLink) {
			doCopyProject(ureq);
		} else if (source == templateLink) {
			doCreateTemplate(ureq);
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
		mapperService.cleanUp(List.of(avatarMapperKey));
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	private void doEditProject(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		project = projectService.getProject(project);
		putProjectToVC();
		editCtrl = ProjProjectEditController.createEditCtrl(ureq, getWindowControl(), project, !secCallback.canEditProjectMetadata());
		listenTo(editCtrl);
		
		String title = translate(ProjectUIFactory.templateSuffix("project.edit", project));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
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
	
	private void doCopyProject(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = ProjProjectEditController.createCopyCtrl(ureq, getWindowControl(), project, createForEnabled);
		listenTo(editCtrl);
		
		String title = translate("project.copy");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateTemplate(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = ProjProjectEditController.createTemplateCtrl(ureq, getWindowControl(), project);
		listenTo(editCtrl);
		
		String title = translate("project.save.template");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
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
		String message = translate(ProjectUIFactory.templateSuffix("project.set.status.done.message", project), Integer.toString(numOfMembers));
		doneConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				ProjectUIFactory.templateSuffix("project.set.status.done.confirm", project),
				ProjectUIFactory.templateSuffix("project.set.status.done.button", project), false);
		listenTo(doneConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), doneConfirmationCtrl.getInitialComponent(),
				true, translate(ProjectUIFactory.templateSuffix("project.set.status.done.title", project)), true);
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
		String message = translate(ProjectUIFactory.templateSuffix("project.set.status.deleted.message", project), Integer.toString(numOfMembers));
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				ProjectUIFactory.templateSuffix("project.set.status.deleted.confirm", project),
				ProjectUIFactory.templateSuffix("project.set.status.deleted.button", project), true);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate(ProjectUIFactory.templateSuffix("project.set.status.deleted.title", project)), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmReopen(UserRequest ureq) {
		String title = translate(ProjectUIFactory.templateSuffix("project.reopen.title", project));
		String msg = translate(ProjectUIFactory.templateSuffix("project.reopen.text", project));
		reopenConfirmationCtrl = activateOkCancelDialog(ureq, title, msg, reopenConfirmationCtrl);
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
	
	private void doOpenFiles(UserRequest ureq, ProjFileRef file) {
		removeAsListenerAndDispose(fileAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_FILES), null);
		ContextEntry contextEntry = null;
		if (file != null) {
			contextEntry = ProjectBCFactory.createFileCe(file);
			swControl = addToHistory(ureq, contextEntry.getOLATResourceable(), null, swControl, true);
		}
		fileAllCtrl = new ProjFileAllController(ureq, swControl, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(fileAllCtrl);
		stackPanel.pushController(translate("file.all.title"), fileAllCtrl);
		
		if (contextEntry != null) {
			fileAllCtrl.activate(ureq, List.of(contextEntry), null);
		}
	}
	
	private void doOpenToDos(UserRequest ureq, ProjToDoRef toDo) {
		removeAsListenerAndDispose(toDoAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_TODOS), null);
		ContextEntry contextEntry = null;
		if (toDo != null) {
			contextEntry = ProjectBCFactory.createToDoCe(toDo);
			swControl = addToHistory(ureq, contextEntry.getOLATResourceable(), null, swControl, true);
		}
		toDoAllCtrl = new ProjToDoAllController(ureq, swControl, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(toDoAllCtrl);
		stackPanel.pushController(translate("todo.all.title"), toDoAllCtrl);
		
		if (contextEntry != null) {
			toDoAllCtrl.activate(ureq, List.of(contextEntry), null);
		}
	}
	
	private void doOpenDecisions(UserRequest ureq, ProjDecisionRef decision) {
		removeAsListenerAndDispose(decisionAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_DECISIONS), null);
		ContextEntry contextEntry = null;
		if (decision != null) {
			contextEntry = ProjectBCFactory.createDecisionCe(decision);
			swControl = addToHistory(ureq, contextEntry.getOLATResourceable(), null, swControl, true);
		}
		decisionAllCtrl = new ProjDecisionAllController(ureq, swControl, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(decisionAllCtrl);
		stackPanel.pushController(translate("decision.all.title"), decisionAllCtrl);
		
		if (contextEntry != null) {
			decisionAllCtrl.activate(ureq, List.of(contextEntry), null);
		}
	}
	
	private void doOpenNotes(UserRequest ureq, ProjNoteRef note) {
		removeAsListenerAndDispose(noteAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_NOTES), null);
		ContextEntry contextEntry = null;
		if (note != null) {
			contextEntry = ProjectBCFactory.createNoteCe(note);
			swControl = addToHistory(ureq, contextEntry.getOLATResourceable(), null, swControl, true);
		}
		noteAllCtrl = new ProjNoteAllController(ureq, swControl, stackPanel, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(noteAllCtrl);
		stackPanel.pushController(translate("note.all.title"), noteAllCtrl);
		
		if (contextEntry != null) {
			noteAllCtrl.activate(ureq, List.of(contextEntry), null);
		}
	}
	
	private void doOpenCalendar(UserRequest ureq, ProjAppointmentRef appointment, ProjMilestoneRef milestone) {
		removeAsListenerAndDispose(calendarAllCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_CALENDAR), null);
		ContextEntry contextEntry = null;
		if (appointment != null) {
			contextEntry = ProjectBCFactory.createAppointmentCe(appointment);
			swControl = addToHistory(ureq, contextEntry.getOLATResourceable(), null, swControl, true);
		} else if (milestone != null) {
			contextEntry = ProjectBCFactory.createMilestoneCe(milestone);
			swControl = addToHistory(ureq, contextEntry.getOLATResourceable(), null, swControl, true);
		}
		calendarAllCtrl = new ProjCalendarAllController(ureq, swControl, project, secCallback, lastVisitDate, avatarMapperKey);
		listenTo(calendarAllCtrl);
		stackPanel.pushController(translate("calendar.all.title"), calendarAllCtrl);
		
		if (contextEntry != null) {
			calendarAllCtrl.activate(ureq, List.of(contextEntry), null);
		}
	}
	
	private void doOpenArtefact(UserRequest ureq, ProjArtefact artefact) {
		stackPanel.popUpToController(this);
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setArtefacts(List.of(artefact));
		ProjArtefactItems artefacts = projectService.getArtefactItems(searchParams);
		
		if (artefacts.getFiles() != null && !artefacts.getFiles().isEmpty()) {
			doOpenFiles(ureq, artefacts.getFiles().get(0));
		} else if (artefacts.getToDos() != null && !artefacts.getToDos().isEmpty()) {
			doOpenToDos(ureq, artefacts.getToDos().get(0));
		} else if (artefacts.getDecisions() != null && !artefacts.getDecisions().isEmpty()) {
			doOpenDecisions(ureq, artefacts.getDecisions().get(0));
		} else if (artefacts.getNotes() != null && !artefacts.getNotes().isEmpty()) {
			doOpenNotes(ureq, artefacts.getNotes().get(0));
		} else if (artefacts.getAppointments() != null && !artefacts.getAppointments().isEmpty()) {
			doOpenCalendar(ureq, artefacts.getAppointments().get(0), null);
		} else if (artefacts.getMilestones() != null && !artefacts.getMilestones().isEmpty()) {
			doOpenCalendar(ureq, null, artefacts.getMilestones().get(0));
		}
	}

}
