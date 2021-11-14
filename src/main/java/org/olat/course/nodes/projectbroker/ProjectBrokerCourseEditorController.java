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

import org.olat.admin.quota.QuotaConstants;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.ms.MSCourseNodeEditController;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.nodes.ta.DropboxForm;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BGMailHelper;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  
 *  
 *   @author guretzki
 */

public class ProjectBrokerCourseEditorController extends ActivateableTabbableDefaultController implements ControllerEventListener {	

	public static final String PANE_TAB_CONF_DROPBOX        = "pane.tab.conf.dropbox";
	public static final String PANE_TAB_CONF_MODULES        = "pane.tab.conf.modules";
	private static final String PANE_TAB_OPTIONS            = "pane.tab.options";
	private static final String PANE_TAB_ACCOUNT_MANAGEMENT = "pane.tab.accountmanagement";
	
	private static final String[] paneKeys = { PANE_TAB_CONF_DROPBOX, PANE_TAB_CONF_MODULES };


	private Long courseId;
	private ProjectBrokerCourseNode node;
	private ModuleConfiguration config;
	private ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;
	private BusinessGroup accountManagerGroup;

	private VelocityContainer optionsFormVC, accountManagementFormVC;
	private VelocityContainer editModules, editDropbox, editScoring;
	private TabbedPane myTabbedPane;
	private int dropboxTabPosition;
	private ModulesFormController modulesForm;
	private DropboxForm dropboxForm;
	private MSEditFormController scoringController;
	private FolderRunController frc;
	private ConditionEditController projectBrokerConditionController;
	private boolean hasLogEntries;	
	private DialogBoxController dialogBoxController;
	private OptionsFormController optionsForm;
	private GroupController accountManagerGroupController;

	private Link editScoringConfigButton;

	private CustomfieldsFormController customfieldsForm;

	private ProjectEventFormController projectEventForm;

	private CloseableModalController cmc;
	private Long projectBrokerId;
	
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private ProjectBrokerManager projectBrokerManager;
	@Autowired
	private ProjectGroupManager projectGroupManager;
	
	protected ProjectBrokerCourseEditorController(UserRequest ureq, WindowControl wControl, ICourse course, ProjectBrokerCourseNode node) {
		super(ureq, wControl);

		this.node = node;
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.courseId = course.getResourceableId();
		this.config = node.getModuleConfiguration();
		projectBrokerModuleConfiguration	= new ProjectBrokerModuleConfiguration(node.getModuleConfiguration());
		Translator fallbackTranslator = Util.createPackageTranslator(DropboxForm.class, ureq.getLocale(), Util.createPackageTranslator(MSCourseNodeEditController.class, ureq.getLocale()));
		Translator myTranslator = Util.createPackageTranslator(ProjectBrokerCourseEditorController.class, ureq.getLocale(),	fallbackTranslator);
		setTranslator(myTranslator);

		// check if a project-broker exists
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		projectBrokerId = projectBrokerManager.getProjectBrokerId(cpm, node);
		if (projectBrokerId == null) {
			// no project-broker exist => create a new one, happens only once
			ProjectBroker projectBroker = projectBrokerManager.createAndSaveProjectBroker();
			projectBrokerId = projectBroker.getKey();
			projectBrokerManager.saveProjectBrokerId(projectBrokerId, cpm, node);
		} 
		
		// Options with dates and custom-fields		
    optionsFormVC = this.createVelocityContainer("optionsForm");
    optionsForm = new OptionsFormController(ureq, wControl, projectBrokerModuleConfiguration, projectBrokerId);
    listenTo(optionsForm);
    optionsFormVC.put("optionsForm", optionsForm.getInitialComponent());
    customfieldsForm = new CustomfieldsFormController(ureq, wControl, projectBrokerModuleConfiguration);
    customfieldsForm.addControllerListener(this);
    optionsFormVC.put("customfieldsForm", customfieldsForm.getInitialComponent());
    projectEventForm = new ProjectEventFormController(ureq, wControl, projectBrokerModuleConfiguration);
    projectEventForm.addControllerListener(this);
    optionsFormVC.put("projectEventForm", projectEventForm.getInitialComponent());

		// Account-Managment 		
    accountManagementFormVC = this.createVelocityContainer("account_management");
    String groupName = translate("account.manager.groupname", node.getShortTitle());
    String groupDescription = translate("account.manager.groupdescription", node.getShortTitle());
    accountManagerGroup = projectGroupManager.getAccountManagerGroupFor(cpm, node, course, groupName, groupDescription, ureq.getIdentity());
    if (accountManagerGroup != null) {
    	Group group = businessGroupService.getGroup(accountManagerGroup);
    	accountManagerGroupController = new GroupController(ureq, getWindowControl(), true, false, true, false, true, false, group, GroupRoles.participant.name());
			listenTo(accountManagerGroupController);
			// add mail templates used when adding and removing users
			MailTemplate ownerAddUserMailTempl = BGMailHelper.createAddParticipantMailTemplate(accountManagerGroup, ureq.getIdentity());
			accountManagerGroupController.setAddUserMailTempl(ownerAddUserMailTempl,false);
			MailTemplate ownerAremoveUserMailTempl = BGMailHelper.createRemoveParticipantMailTemplate(accountManagerGroup, ureq.getIdentity());
			accountManagerGroupController.setRemoveUserMailTempl(ownerAremoveUserMailTempl,false);
	    accountManagementFormVC.put("accountManagementController", accountManagerGroupController.getInitialComponent());
    }
    
		// Modules config		
		editModules = createVelocityContainer("editModules");
		modulesForm = new ModulesFormController(ureq, wControl, config);
		listenTo(modulesForm);
		
		editModules.put("editModules", modulesForm.getInitialComponent());
		
		// DropBox config	(re-used from task-node) 
		editDropbox = this.createVelocityContainer("editDropbox");
		editDropbox.setTranslator(myTranslator);
		dropboxForm = new DropboxForm(ureq, wControl, config);
		listenTo(dropboxForm);
		editDropbox.put("dropboxform", dropboxForm.getInitialComponent());

		// Scoring config		
		editScoring = this.createVelocityContainer("editScoring");
		editScoringConfigButton = LinkFactory.createButtonSmall("scoring.config.enable.button", editScoring, this);

		scoringController = new MSEditFormController(ureq, wControl, config, NodeAccessType.of(course));
		listenTo(scoringController);
		editScoring.put("scoringController", scoringController.getInitialComponent());
		
		// if there is already user data available, make for read only
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		hasLogEntries = am.hasUserNodeLogs(node);
		editScoring.contextPut("hasLogEntries", Boolean.valueOf(hasLogEntries));
		if (hasLogEntries) {
			scoringController.setDisplayOnly(true);
		}
		//Initialstate
		editScoring.contextPut("isOverwriting", Boolean.FALSE);
		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {		
		if (getLogger().isDebugEnabled()) getLogger().debug("event source=" + source + " " + event.toString());		
		if (source == editScoringConfigButton){
			scoringController.setDisplayOnly(false);
			editScoring.contextPut("isOverwriting", Boolean.TRUE);
		} 
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == projectBrokerConditionController) {
			if (event == Event.CHANGED_EVENT) {
				node.setConditionProjectBroker(projectBrokerConditionController.getCondition());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == dialogBoxController) {			
			if (DialogBoxUIFactory.isOkEvent(event)) {
				// ok: open task folder
				String relPath = TACourseNode.getTaskFolderPathRelToFolderRoot(CourseFactory.loadCourse(courseId), node);
				VFSContainer rootFolder = VFSManager.olatRootContainer(relPath, null);
				VFSContainer namedFolder = new NamedContainerImpl(translate("taskfolder"), rootFolder);

				Quota folderQuota = quotaManager.getCustomQuota(relPath);
				if (folderQuota == null) {
					Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
					folderQuota = quotaManager.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
				}
				namedFolder.setLocalSecurityCallback(new FolderCallback(false, folderQuota));
				
				removeAsListenerAndDispose(frc);
				frc = new FolderRunController(namedFolder, false, urequest, getWindowControl());
				listenTo (frc);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(
						getWindowControl(), translate("folder.close"), frc.getInitialComponent()
				);
				listenTo (cmc);
				
				cmc.activate();
				fireEvent(urequest, Event.CHANGED_EVENT);
			}
		} else if (source == scoringController) {
			if (event == Event.CANCELLED_EVENT) {
				if (hasLogEntries) {
					scoringController.setDisplayOnly(true);}
				editScoring.contextPut("isOverwriting", Boolean.FALSE);			
			} else if (event == Event.DONE_EVENT){
				scoringController.updateModuleConfiguration(config);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}	else if (source == modulesForm) {
			boolean onoff = event.getCommand().endsWith("true");
			if (event.getCommand().startsWith("dropbox")) {
				config.set(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLED, onoff);
			} else if (event.getCommand().startsWith("returnbox")) {
				config.set(ProjectBrokerCourseNode.CONF_RETURNBOX_ENABLED, onoff);				
			}
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			return;		
		} else if (source == accountManagerGroupController) {
			if (event instanceof IdentitiesAddEvent) {
				IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent)event;
				BusinessGroupAddResponse response = businessGroupService.addParticipants(urequest.getIdentity(), urequest.getUserSession().getRoles(),
						identitiesAddedEvent.getAddIdentities(), accountManagerGroup, null);
				identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
				identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
				identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
				getLogger().info("Add users as account-managers");
				fireEvent(urequest, Event.CHANGED_EVENT );			
			} else if (event instanceof IdentitiesRemoveEvent) {
				businessGroupService.removeParticipants(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(), accountManagerGroup, null);
				getLogger().info("Remove users as account-managers");
				fireEvent(urequest, Event.CHANGED_EVENT );
			}
		} else if (source == optionsForm) {
			if (event == Event.CANCELLED_EVENT) {
				return;
			} else if (event == Event.DONE_EVENT) {
				projectBrokerModuleConfiguration.setNbrParticipantsPerTopic(optionsForm.getNnbrOfAttendees());
				if (projectBrokerModuleConfiguration.isAcceptSelectionManually() && !optionsForm.getSelectionAccept()) {
					// change 'Accept manually' to 'Accept automatically' => enroll all candidates
					projectGroupManager.acceptAllCandidates(projectBrokerId, urequest.getIdentity(), projectBrokerModuleConfiguration.isAutoSignOut(), optionsForm.getSelectionAccept());
				}
				projectBrokerModuleConfiguration.setAcceptSelectionManaually(optionsForm.getSelectionAccept());
				projectBrokerModuleConfiguration.setSelectionAutoSignOut(optionsForm.getSelectionAutoSignOut());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);				
			}
		}	else if (source == customfieldsForm || source == projectEventForm) {
			if (event == Event.CANCELLED_EVENT) {
				return;
			} else if (event == Event.DONE_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);				
			}	
		} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT){
			getLogger().debug("NODECONFIG_CHANGED_node.shortTitle=" + node.getShortTitle());
	    	String groupName = translate("account.manager.groupname", node.getShortTitle());
	    	String groupDescription = translate("account.manager.groupdescription", node.getShortTitle());
	    	accountManagerGroup = projectGroupManager.updateAccountManagerGroupName(getIdentity(), groupName, groupDescription, accountManagerGroup);
		} else if (source == dropboxForm) {
				if (event == Event.CANCELLED_EVENT) {
					return;
				} else if (event == Event.DONE_EVENT) {
					config.set(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLEMAIL, dropboxForm.mailEnabled());
					config.set(ProjectBrokerCourseNode.CONF_DROPBOX_CONFIRMATION, dropboxForm.getConfirmation());
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
					return;
				}
		} else {
			getLogger().warn("Can not handle event in ProjectBrokerCourseEditorController source=" + source + " " + event.toString());
		}
	}

	@Override
	public void addTabs(TabbedPane theTabbedPane) {
		this.myTabbedPane = theTabbedPane;
		myTabbedPane.addTab(translate(PANE_TAB_OPTIONS), optionsFormVC);
		myTabbedPane.addTab(translate(PANE_TAB_ACCOUNT_MANAGEMENT), accountManagementFormVC);
		myTabbedPane.addTab(translate(PANE_TAB_CONF_MODULES), editModules);
		dropboxTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_DROPBOX), editDropbox);

		Boolean bool = (Boolean) config.get(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLED);
		myTabbedPane.setEnabled(dropboxTabPosition, (bool != null) ? bool.booleanValue() : true);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

}

class FolderCallback implements VFSSecurityCallback {

	private Quota folderQuota;
	private final boolean folderLocked;

	/**
	 * @param folderLocked
	 */
	public FolderCallback(boolean folderLocked, Quota folderQuota) {
		this.folderLocked = folderLocked;
		this.folderQuota = folderQuota;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return !folderLocked;
	}

	@Override
	public boolean canCreateFolder() {
		return !folderLocked;
	}

	@Override
	public boolean canDelete() {
		return !folderLocked;
	}

	@Override
	public boolean canList() {
		return true;
	}

	@Override
	public boolean canCopy() {
		return true;
	}

	@Override
	public Quota getQuota() {
		return folderQuota;
	}

	@Override
	public void setQuota(Quota quota) {
		folderQuota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return !folderLocked;
	}
}