/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.CustomField;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerMailer;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;

/**
 *
 * @author guretzki
 *
 */

public class ProjectListController extends BasicController implements GenericEventListener {

	private static final String OPEN_IDENTITY_CMD = "openID";
	// List commands
	private static final String TABLE_ACTION_SHOW_DETAIL     = "cmd.show.detail";
	private static final String TABLE_ACTION_ACCOUNT_MANAGER = "cmd.account.manager";
	private static final String TABLE_ACTION_SELECT          = "cmd.select";
	private static final String TABLE_ACTION_CANCEL_SELECT   = "cmd.cancel.select";


	private VelocityContainer contentVC;
	private StackedPanel mainPanel;
	private ProjectListTableModel projectListTableModel;
	private TableController tableController;
	private Controller projectController;

	private Link createNewProjectButton;

	private Long courseId;
	private CourseNode courseNode;
	private UserCourseEnvironment userCourseEnv;

	private ProjectBrokerModuleConfiguration moduleConfig;
	private Long projectBrokerId;
	private int numberOfCustomFieldInTable = 0;
	private int numberOfEventInTable = 0;
	private int nbrSelectedProjects;
	private boolean isParticipantInAnyProject;
	private CloseableCalloutWindowController calloutCtrl;
	private Project currentProject;

	private DialogBoxController noDeselectWarning;

	private final ProjectBrokerMailer projectBrokerMailer;
	private final ProjectGroupManager projectGroupManager;
	private final ProjectBrokerManager projectBrokerManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param ne
	 * @param previewMode
	 */
	protected ProjectListController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		projectBrokerMailer = CoreSpringFactory.getImpl(ProjectBrokerMailer.class);
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
		courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		moduleConfig = new ProjectBrokerModuleConfiguration(courseNode.getModuleConfiguration());

		contentVC = createVelocityContainer("project_list");
		// set header info with project-broker run mode [accept.automatically.limited , accept.manually.limited etc.]
		String infoProjectBrokerRunMode = "";
		if (moduleConfig.isAcceptSelectionManually() && moduleConfig.isAutoSignOut()) {
			infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.manually.auto.sign.out", Integer.toString(moduleConfig.getNbrParticipantsPerTopic()) );
		} else if (moduleConfig.isAcceptSelectionManually()) {
			if (moduleConfig.getNbrParticipantsPerTopic() == ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) {
				infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.manually.unlimited" );
			} else {
				infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.manually.limited", Integer.toString(moduleConfig.getNbrParticipantsPerTopic()) );
			}
		} else {
			if (moduleConfig.getNbrParticipantsPerTopic() == ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) {
				infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.automatically.unlimited" );
			} else {
				infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.automatically.limited", Integer.toString(moduleConfig.getNbrParticipantsPerTopic()) );
			}
		}
		contentVC.contextPut("infoProjectBrokerRunMode", infoProjectBrokerRunMode);
		mainPanel = new SimpleStackedPanel("projectlist_panel");
		CoursePropertyManager cpm = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		if (projectGroupManager.isAccountManager(ureq.getIdentity(), cpm, courseNode, userCourseEnv)) {
			contentVC.contextPut("isAccountManager", true);
			createNewProjectButton = LinkFactory.createButtonSmall("create.new.project.button", contentVC, this);
			createNewProjectButton.setIconLeftCSS("o_icon o_icon_add");
		} else {
			contentVC.contextPut("isAccountManager", false);
		}
		// push title and learning objectives, only visible on intro page
		contentVC.contextPut("menuTitle", courseNode.getShortTitle());
		contentVC.contextPut("displayTitle", courseNode.getLongTitle());

		projectBrokerId = projectBrokerManager.getProjectBrokerId(cpm, courseNode);
		if (projectBrokerId == null) {
			// no project-broker exist => create a new one, happens only once
			ProjectBroker projectBroker = projectBrokerManager.createAndSaveProjectBroker();
			projectBrokerId = projectBroker.getKey();
			projectBrokerManager.saveProjectBrokerId(projectBrokerId, cpm, courseNode);
			getLogger().info("no project-broker exist => create a new one projectBrokerId={}", projectBrokerId);
		}

		tableController = createTableController(ureq, wControl);

		OLATResourceable projectBroker = projectBrokerManager.getProjectBroker(projectBrokerId);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), projectBroker);
		updateProjectListModelOf(tableController, ureq.getIdentity());
		contentVC.put("projectList", tableController.getInitialComponent());
		mainPanel.setContent(contentVC);

		// jump to either the forum or the folder if the business-launch-path says so.
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null) { // a context path is left for me
			if (isLogDebugEnabled()) logDebug("businesscontrol (for further jumps) would be: " + bc);
			OLATResourceable ores = ce.getOLATResourceable();
			if (isLogDebugEnabled()) logDebug("OLATResourceable= " + ores.toString());
			Long resId = ores.getResourceableId();
			if (resId.longValue() != 0) {
				if (isLogDebugEnabled()) logDebug("projectId=" + ores.getResourceableId());

				Project proj = projectBrokerManager.getProject(ores.getResourceableId());
				if (proj != null) {
					activateProjectController(proj, ureq);
				} else {
					// message not found, do nothing. Load normal start screen
					logDebug("Invalid projectId=" + ores.getResourceableId());
				}
			} else {
				//FIXME:chg: Should not happen, occurs when course-node are called
				if (isLogDebugEnabled()) logDebug("Invalid projectId=" + ores.getResourceableId());
			}
		}

		putInitialPanel(mainPanel);
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == createNewProjectButton) {
			String projectTitle = translate("new.project.title");
			int i = 1;
			while (projectBrokerManager.existProjectName(projectBrokerId, projectTitle)) {
				projectTitle = translate("new.project.title") + i++;
			}
			String projectGroupName = translate("project.member.groupname", projectTitle);
			String projectGroupDescription = translate("project.member.groupdescription", projectTitle);
			BusinessGroup projectGroup = projectGroupManager.createProjectGroupFor(projectBrokerId,ureq.getIdentity(), projectGroupName, projectGroupDescription, courseId);
			Project project = projectBrokerManager.createAndSaveProjectFor(projectTitle, projectTitle, projectBrokerId, projectGroup);
			projectGroupManager.sendGroupChangeEvent(project, courseId, ureq.getIdentity());
			getLogger().debug("Created a new project=" + project);
			projectController = new ProjectController(ureq, this.getWindowControl(), userCourseEnv, courseNode, project, true, moduleConfig);
			listenTo(projectController);
			mainPanel.pushContent(projectController.getInitialComponent());
		} else if (event.getCommand().equals(OPEN_IDENTITY_CMD)){
			Link link = (Link) source;
			if (calloutCtrl!=null) {
				calloutCtrl.deactivate();
				removeAsListenerAndDispose(calloutCtrl);
				calloutCtrl = null;
			}
			openUserInPopup(ureq, (Identity) link.getUserObject());
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if ( (source == tableController) && (event instanceof TableEvent) ) {
			handleTableEvent(urequest, (TableEvent)event);
		} else if ( (source == projectController) && (event == Event.BACK_EVENT) ) {
			mainPanel.popContent();
		} else if ((source == projectController) && (event instanceof ProjectBrokerEditorEvent)) {
			final ProjectBrokerEditorEvent pbEditEvent = (ProjectBrokerEditorEvent) event;
			if (pbEditEvent.isCancelEvent()){
				getLogger().info("event form cancelled => delete project");
				projectBrokerManager.deleteProject(pbEditEvent.getProject(), true, userCourseEnv.getCourseEnvironment(),
						courseNode, getIdentity());
				mainPanel.popContent();
				updateProjectListModelOf(tableController, urequest.getIdentity());
			} else if (pbEditEvent.isCreateEvent() || pbEditEvent.isDeletedEvent()){
				mainPanel.popContent();
				updateProjectListModelOf(tableController, urequest.getIdentity());
			}
		} else if (source == noDeselectWarning) {
			if(DialogBoxUIFactory.isOkEvent(event)){
				handleEnrollAction(urequest, currentProject);
			}
		}
	}


	private void handleTableEvent(UserRequest urequest, TableEvent te) {
		currentProject = (Project)tableController.getTableDataModel().getObject(te.getRowId());
		if ( projectBrokerManager.existsProject( currentProject.getKey() ) ) {
			handleTableEventForProject(urequest, te, currentProject);
		} else {
			this.showInfo("info.project.nolonger.exist", currentProject.getTitle());
			updateProjectListModelOf(tableController, urequest.getIdentity());
		}
	}


	private void handleTableEventForProject(UserRequest urequest, TableEvent te, Project selectedProject) {
		if ( te.getActionId().equals(TABLE_ACTION_SHOW_DETAIL)) {
			activateProjectController(selectedProject, urequest);
		} else if ( te.getActionId().equals(TABLE_ACTION_ACCOUNT_MANAGER)) {
			activateUserController(selectedProject, urequest, te);
		} else if ( te.getActionId().equals(TABLE_ACTION_SELECT)) {
			if(!projectGroupManager.isDeselectionAllowed(selectedProject)){
				List<String> warningButtons = new ArrayList<>();
				warningButtons.add(translate("info.projectbroker.no.deselect.select"));
				warningButtons.add(translate("info.projectbroker.no.deselect.cancel"));
				String message = translate("info.projectbroker.deselect.confirmation",selectedProject.getTitle())+"<br/><div class=\"o_important\">"+translate("info.projectbroker.no.deselect")+"</div>";
				noDeselectWarning = activateGenericDialog(urequest, translate("info.projectbroker.no.deselect.title"), message, warningButtons, noDeselectWarning);
				return;
			}
			handleEnrollAction(urequest, selectedProject);
		} else if ( te.getActionId().equals(TABLE_ACTION_CANCEL_SELECT)) {
			handleCancelEnrollmentAction(urequest, selectedProject);
		} else {
			getLogger().warn("Controller-event-handling: Unkown event=" + te);
		}
		fireEvent(urequest, te);
	}


	private void handleCancelEnrollmentAction(UserRequest urequest, Project selectedProject) {
		getLogger().debug("start cancelProjectEnrollmentOf identity=" + urequest.getIdentity() + " to project=" + selectedProject);
		boolean cancelledEnrollmend = projectBrokerManager.cancelProjectEnrollmentOf(urequest.getIdentity(), selectedProject, moduleConfig);
		if (cancelledEnrollmend) {
			projectBrokerMailer.sendCancelEnrollmentEmailToParticipant(urequest.getIdentity(), selectedProject, this.getTranslator());
			if (selectedProject.isMailNotificationEnabled()) {
				projectBrokerMailer.sendCancelEnrollmentEmailToManager(urequest.getIdentity(), selectedProject, this.getTranslator());
			}
			projectGroupManager.sendGroupChangeEvent(selectedProject, courseId, urequest.getIdentity());
		} else {
			showInfo("info.msg.could.not.cancel.enrollment");
		}
		updateProjectListModelOf(tableController, urequest.getIdentity());
	}


	private void handleEnrollAction(UserRequest urequest, Project selectedProject) {
		getLogger().debug("start enrollProjectParticipant identity=" + urequest.getIdentity() + " to project=" + selectedProject);
		boolean enrolled = projectBrokerManager.enrollProjectParticipant(urequest.getIdentity(), selectedProject, moduleConfig, nbrSelectedProjects, isParticipantInAnyProject);
		if (enrolled) {
			projectBrokerMailer.sendEnrolledEmailToParticipant(urequest.getIdentity(), selectedProject, this.getTranslator());
			if (selectedProject.isMailNotificationEnabled()) {
				projectBrokerMailer.sendEnrolledEmailToManager(urequest.getIdentity(), selectedProject, this.getTranslator());
			}
			projectGroupManager.sendGroupChangeEvent(selectedProject, courseId, urequest.getIdentity());
		} else {
			showInfo("info.msg.could.not.enroll");
		}
		updateProjectListModelOf(tableController, urequest.getIdentity());
	}

	private void updateProjectListModelOf(TableController tableCtrl, Identity identity) {
		List<Project> projects = new ArrayList<>(projectBrokerManager.getProjectListBy(projectBrokerId));
		nbrSelectedProjects = projectBrokerManager.getNbrSelectedProjects(identity, projects);
		isParticipantInAnyProject = projectBrokerManager.isParticipantInAnyProject( identity,  projects);
		projectListTableModel = new ProjectListTableModel(projects, identity, getTranslator(), moduleConfig, numberOfCustomFieldInTable, numberOfEventInTable, nbrSelectedProjects, isParticipantInAnyProject);
		tableCtrl.setTableDataModel(projectListTableModel);
	}

	private void activateUserController(final Project projectAt, UserRequest urequest, TableEvent tableEvent) {
		if (projectAt.getProjectLeaders().isEmpty()) {
			this.showInfo("show.info.no.project.leader");
		} else if (projectAt.getProjectLeaders().size() > 1) {
			VelocityContainer identityVC = createVelocityContainer("identityCallout");
			List<Identity> allIdents = projectAt.getProjectLeaders();
			ArrayList<Link> identLinks = new ArrayList<>(allIdents.size());
			for (Identity identity : allIdents) {
				String last = identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
				String first = identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
				String linkName = last + " " + first;

				Link idLink = LinkFactory.createCustomLink(linkName, OPEN_IDENTITY_CMD, linkName, Link.NONTRANSLATED, identityVC, this);
				idLink.setUserObject(identity);
				identLinks.add(idLink);
			}
			identityVC.contextPut("identLinks", identLinks);

			int row = tableEvent.getRowId();
			String targetDomID = ProjectManagerColumnRenderer.PROJECTMANAGER_COLUMN_ROW_IDENT + row;
			String title = translate("projectlist.callout.title", projectAt.getTitle());
			removeAsListenerAndDispose(calloutCtrl);
			calloutCtrl = new CloseableCalloutWindowController(urequest, getWindowControl(), identityVC, targetDomID, title, true, null);
			calloutCtrl.activate();
			listenTo(calloutCtrl);
		} else if (projectAt.getProjectLeaders().size() == 1) {
			// no callout, if its only one user
			Identity leader = projectAt.getProjectLeaders().get(0);
			openUserInPopup(urequest, leader);
		}
	}

	private void openUserInPopup(UserRequest ureq, final Identity ident){
		String businessPath = "[HomePage:" + ident.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}


	private void activateProjectController(Project project, UserRequest urequest) {
		removeAsListenerAndDispose(projectController);
		projectController = new ProjectController(urequest, this.getWindowControl(), userCourseEnv, courseNode, project, false, moduleConfig);
		listenTo(projectController);
		mainPanel.pushContent(projectController.getInitialComponent());
	}

	private TableController createTableController(final UserRequest ureq, WindowControl wControl) {
		numberOfCustomFieldInTable = 0;
		numberOfEventInTable = 0;
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("projectlist.no.projects"), null, "o_projectbroker_icon");
		tableConfig.setPreferencesOffered(true, "projectbrokerList");
		tableConfig.setShowAllLinkEnabled(false);// Do not allow show all because many entries takes too long to render

		removeAsListenerAndDispose(tableController);
		tableController = new TableController(tableConfig, ureq, wControl, this.getTranslator(), true);
		listenTo(tableController);

		int dataColumn = 0;
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("projectlist.tableheader.title", dataColumn++, TABLE_ACTION_SHOW_DETAIL, getLocale()));

		CustomRenderColumnDescriptor projectManagerDescriptor = new CustomRenderColumnDescriptor("projectlist.tableheader.account.manager", dataColumn++, TABLE_ACTION_ACCOUNT_MANAGER, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new ProjectManagerColumnRenderer()){

			/**
			 * @see org.olat.core.gui.components.table.DefaultColumnDescriptor#compareTo(int, int)
			 */
			@Override
			public int compareTo(int rowa, int rowb) {
				return super.compareTo(rowa, rowb);
			}

			/**
			 * @see org.olat.core.gui.components.table.CustomRenderColumnDescriptor#renderValue(org.olat.core.gui.render.StringOutput, int, org.olat.core.gui.render.Renderer)
			 */
			@Override
			public void renderValue(StringOutput sb, int row, Renderer renderer) {
					Object val = getModelData(row);
					String rowSt = Integer.toString(row); // to get info about row in Renderer!
					getCustomCellRenderer().render(sb, renderer, val, getLocale(), getAlignment(), rowSt);
			}
		};
		tableController.addColumnDescriptor(projectManagerDescriptor);
		// Custom-Fields
		List<CustomField> customFieldList = moduleConfig.getCustomFields();
		for (Iterator<CustomField> iterator = customFieldList.iterator(); iterator.hasNext();) {
			CustomField customField = iterator.next();
			if (customField.isTableViewEnabled()) {
				numberOfCustomFieldInTable++;
				DefaultColumnDescriptor columnDescriptor = new DefaultColumnDescriptor(customField.getName(), dataColumn++,null, getLocale());
				columnDescriptor.setTranslateHeaderKey(false);
				tableController.addColumnDescriptor(columnDescriptor);
			}
		}
		// Project Events
		for (Project.EventType eventType : Project.EventType.values()) {
			if (moduleConfig.isProjectEventEnabled(eventType) && moduleConfig.isProjectEventTableViewEnabled(eventType)) {
				numberOfEventInTable ++;
				tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("projectlist.tableheader.event." + eventType.getI18nKey(), dataColumn++,
						null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new ProjectEventColumnRenderer()));
			}
		}

		tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("projectlist.tableheader.state", dataColumn++,
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new ProjectStateColumnRenderer()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("projectlist.tableheader.numbers", dataColumn++, null, getLocale()) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
				if (a == null || b == null) {
					boolean bb = (b == null);
					return (a == null) ? (bb ? 0: -1) : (bb ? 1: 0);
				}
				try {
					Long la = Long.valueOf((String)a);
					Long lb = Long.valueOf((String)b);
					return la.compareTo(lb);
				} catch (NumberFormatException e) {
					return super.compareTo(rowa, rowb);
				}
			}
		});

		String selectCmd = userCourseEnv.isCourseReadOnly() ? null : TABLE_ACTION_SELECT;
		tableController.addColumnDescriptor(new BooleanColumnDescriptor("projectlist.tableheader.select", dataColumn++, selectCmd,
				translate("table.action.select"), "-" ));
		String cancelCmd = userCourseEnv.isCourseReadOnly() ? null : TABLE_ACTION_CANCEL_SELECT;
		tableController.addColumnDescriptor(new BooleanColumnDescriptor("projectlist.tableheader.cancel.select", dataColumn++, cancelCmd,
				translate("projectlist.tableheader.cancel.select"), "-" ));
		return tableController;

	}


	/**
	 * Is called when a project is deleted via group-management
	 * (ProjectBrokerManager.deleteGroupDataFor(BusinessGroup group) , DeletableGroupData-interface)
	 *
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {
		updateProjectListModelOf(tableController, getIdentity());
	}

}

