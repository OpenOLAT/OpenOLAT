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
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupView;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.model.MembershipModification;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.right.BGRightManager;
import org.olat.group.ui.NewBGController;
import org.olat.group.ui.wizard.BGConfigBusinessGroup;
import org.olat.group.ui.wizard.BGConfigToolsStep;
import org.olat.group.ui.wizard.BGCopyBusinessGroup;
import org.olat.group.ui.wizard.BGCopyPreparationStep;
import org.olat.group.ui.wizard.BGEmailSelectReceiversStep;
import org.olat.group.ui.wizard.BGMergeStep;
import org.olat.group.ui.wizard.BGUserMailTemplate;
import org.olat.group.ui.wizard.BGUserManagementController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractBusinessGroupListController extends BasicController implements Activateable2 {
	protected static final String TABLE_ACTION_LEAVE = "bgTblLeave";
	protected static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	protected static final String TABLE_ACTION_ACCESS = "bgTblAccess";
	protected static final String TABLE_ACTION_DUPLICATE = "bgTblDuplicate";
	protected static final String TABLE_ACTION_MERGE = "bgTblMerge";
	protected static final String TABLE_ACTION_USERS = "bgTblUser";
	protected static final String TABLE_ACTION_CONFIG = "bgTblConfig";
	protected static final String TABLE_ACTION_EMAIL = "bgTblEmail";
	protected static final String TABLE_ACTION_DELETE = "bgTblDelete";
	protected static final String TABLE_ACTION_SELECT = "bgTblSelect";
	
	protected final VelocityContainer mainVC;

	protected final TableController groupListCtr;
	protected final BusinessGroupTableModelWithType groupListModel;
	protected SearchBusinessGroupParams lastSearchParams;
	
	private DialogBoxController leaveDialogBox;
	
	private Link createButton;
	
	private NewBGController groupCreateController;
	private BGUserManagementController userManagementController;
	private MailNotificationEditController userManagementSendMailController;
	private BusinessGroupDeleteDialogBoxController deleteDialogBox;
	private StepsMainRunController businessGroupWizard;
	protected CloseableModalController cmc;
	
	private final boolean admin;
	protected final MarkManager markManager;
	protected final BaseSecurity securityManager;
	protected final BusinessGroupModule groupModule;
	protected final ACService acService;
	protected final BGAreaManager areaManager;
	protected final BGRightManager rightManager;
	protected final BusinessGroupService businessGroupService;
	protected final CollaborationToolsFactory collaborationTools;
	
	public AbstractBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page) {
		super(ureq, wControl, Util.createPackageTranslator(AbstractBusinessGroupListController.class, ureq.getLocale()));
		
		admin = ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isGroupManager();
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		acService = CoreSpringFactory.getImpl(ACService.class);
		groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		collaborationTools = CollaborationToolsFactory.getInstance();
		
		mainVC = createVelocityContainer(page);

		//table
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, this.getClass().getSimpleName());
		tableConfig.setTableEmptyMessage(translate("open.nogroup"));			
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator(), false);
		listenTo(groupListCtr);

		int numOfColumns = initColumns();
		groupListModel = new BusinessGroupTableModelWithType(getTranslator(), numOfColumns);
		groupListCtr.setTableDataModel(groupListModel);

		mainVC.put("groupList", groupListCtr.getInitialComponent());
		
		initButtons(ureq);

		putInitialPanel(mainVC);
	}
	
	protected abstract void initButtons(UserRequest ureq);

	protected void initButtons(UserRequest ureq, boolean create) {
		if(create && canCreateBusinessGroup(ureq)) {
			createButton = LinkFactory.createButton("create.group", mainVC, this);
		}
	}
	
	protected boolean canCreateBusinessGroup(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		if(roles.isOLATAdmin() || roles.isGroupManager()
				|| (roles.isAuthor() && groupModule.isAuthorAllowedCreate())
				|| (!roles.isGuestOnly() && !roles.isInvitee() && groupModule.isUserAllowedCreate())) {
			return true;
		}
		return false;
	}
	
	protected abstract int initColumns();
	
	protected boolean isAdmin() {
		return admin;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link && source.getComponentName().startsWith("repo_entry_")) {
			Object uobj = ((Link)source).getUserObject();
			if (uobj instanceof RepositoryEntryShort) {
				RepositoryEntryShort re = (RepositoryEntryShort)((Link)source).getUserObject();
				NewControllerFactory.getInstance().launch("[RepositoryEntry:" + re.getKey() + "]", ureq, getWindowControl());
			} else if(uobj instanceof BusinessGroupShort) {
				BusinessGroupShort bg = (BusinessGroupShort)uobj;
				NewControllerFactory.getInstance().launch("[BusinessGroup:" + bg.getKey() + "][toolresources:0]", ureq, getWindowControl());
			}
		} else if (source instanceof Link && source.getComponentName().startsWith("marked_")) {
			Object uobj = ((Link)source).getUserObject();
			if(uobj instanceof BGTableItem) {
				toogleMark((BGTableItem)uobj);
			}
		} else if (source == createButton) {
			doCreate(ureq, getWindowControl(), null);
		}
	}
	
	/**
	 * Add/remove as favorite
	 * @param item
	 */
	private void toogleMark(BGTableItem item) {
		OLATResourceable bgResource = OresHelper.createOLATResourceableInstance("BusinessGroup", item.getBusinessGroupKey());
		//		item.getBusinessGroup().getResource();
		if(markManager.isMarked(bgResource, getIdentity(), null)) {
			markManager.removeMark(bgResource, getIdentity(), null);
			item.setMarked(false);
		} else {
			String businessPath = "[BusinessGroup:" + item.getBusinessGroupKey() + "]";
			markManager.setMark(bgResource, getIdentity(), null, businessPath);
			item.setMarked(true);
		}
		groupListCtr.modelChanged();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();

				Long businessGroupKey = groupListModel.getObject(te.getRowId()).getBusinessGroupKey();
				BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(businessGroupKey);
				//prevent rs after a group is deleted by someone else
				if(businessGroup == null) {
					groupListModel.removeBusinessGroup(businessGroup);
					groupListCtr.modelChanged();
				} else if(actionid.equals(TABLE_ACTION_LAUNCH)) {
					doLaunch(ureq, businessGroup);
				} else if(actionid.equals(TABLE_ACTION_LEAVE)) {
					leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", businessGroup.getName()), leaveDialogBox);
					leaveDialogBox.setUserObject(businessGroup);
				} else if (actionid.equals(TABLE_ACTION_ACCESS)) {
					doAccess(ureq, businessGroup);
				} else if (actionid.equals(TABLE_ACTION_SELECT)) {
					doSelect(ureq, businessGroup);
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent te = (TableMultiSelectEvent)event;
				List<BGTableItem> selectedItems = groupListModel.getObjects(te.getSelection());
				if(TABLE_ACTION_DELETE.equals(te.getAction())) {
					confirmDelete(ureq, selectedItems);
				} else if(TABLE_ACTION_DUPLICATE.equals(te.getAction())) {
					doCopy(ureq, selectedItems);
				} else if(TABLE_ACTION_CONFIG.equals(te.getAction())) {
					doConfiguration(ureq, selectedItems);
				} else if(TABLE_ACTION_EMAIL.equals(te.getAction())) {
					doEmails(ureq, selectedItems);
				} else if(TABLE_ACTION_USERS.equals(te.getAction())) {
					doUserManagement(ureq, selectedItems);
				} else if(TABLE_ACTION_MERGE.equals(te.getAction())) {
					doMerge(ureq, selectedItems);
				} else if(TABLE_ACTION_SELECT.equals(te.getAction())) {
					doSelect(ureq, selectedItems);
				}
			}
		} else if (source == deleteDialogBox) {
			if(event == Event.DONE_EVENT) {
				boolean withEmail = deleteDialogBox.isSendMail();
				List<BusinessGroup> groupsToDelete = deleteDialogBox.getGroupsToDelete();
				doDelete(ureq, withEmail, groupsToDelete);
				reloadModel();
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == leaveDialogBox) {
			if (event != Event.CANCELLED_EVENT && DialogBoxUIFactory.isYesEvent(event)) {
				doLeave(ureq, (BusinessGroup)leaveDialogBox.getUserObject());
				reloadModel();
			}
		} else if (source == groupCreateController) {
			BusinessGroup group = null;
			if(event == Event.DONE_EVENT) {
				group = groupCreateController.getCreatedGroup();
				if(group != null) {
					reloadModel();
				}
			}
			cmc.deactivate();
			cleanUpPopups();
			//if new group -> go to the tab
			if(group != null) {
				String businessPath = "[BusinessGroup:" + group.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
		} else if (source == businessGroupWizard) { 
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(businessGroupWizard);
				businessGroupWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadModel();
				}
			}
		} else if (source == userManagementController) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				//confirm sending emails
				MembershipModification mod = userManagementController.getModifications();
				List<BusinessGroup> groups = userManagementController.getGroups();
				confirmUserManagementEmail(ureq, mod, groups);
			} else {
				cleanUpPopups();
			}
		} else if (source == userManagementSendMailController) {
			if(event == Event.DONE_EVENT) {
				BGUserMailTemplate sendMail = (BGUserMailTemplate)userManagementSendMailController.getTemplate();
				MembershipModification mod = sendMail.getModifications();
				List<BusinessGroup> groups = sendMail.getGroups();
				finishUserManagement(mod, groups, sendMail, userManagementSendMailController.isSendMail());
				reloadModel();
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == cmc) {
			cleanUpPopups();
		}
		super.event(ureq, source, event);
	}
	
	/**
	 * Aggressive clean up all popup controllers
	 */
	protected void cleanUpPopups() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(deleteDialogBox);
		removeAsListenerAndDispose(groupCreateController);
		removeAsListenerAndDispose(businessGroupWizard);
		removeAsListenerAndDispose(leaveDialogBox);
		cmc = null;
		leaveDialogBox = null;
		deleteDialogBox = null;
		groupCreateController = null;
		businessGroupWizard = null;
	}
	
	/**
	 * Launch a business group with its business path
	 * @param ureq
	 * @param group
	 */
	protected void doAccess(UserRequest ureq, BusinessGroup group) {
		String businessPath = "[BusinessGroup:" + group.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	/**
	 * Launch a business group with its business path
	 * @param ureq
	 * @param group
	 */
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {
		String businessPath = "[BusinessGroup:" + group.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	/**
	 * Removes user from the group as owner and participant. If
	 * no other owner are found the user won't be removed from the owner group
	 * 
	 * @param ureq
	 */
	private void doLeave(UserRequest ureq, BusinessGroup group) {
		// 1) remove as owner
		if (securityManager.isIdentityInSecurityGroup(getIdentity(), group.getOwnerGroup())) {
			List<Identity> ownerList = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
			if (ownerList.size() > 1) {
				businessGroupService.removeOwners(ureq.getIdentity(), Collections.singletonList(getIdentity()), group);
			} else {
				// he is the last owner, but there must be at least one oner
				// give him a warning, as long as he tries to leave, he gets
				// this warning.
				getWindowControl().setError(translate("msg.atleastone"));
				return;
			}
		}
		// if identity was also owner it must have successfully removed to end here.
		// now remove the identity also as participant.
		// 2) remove as participant
		List<Identity> identities = Collections.singletonList(getIdentity());
		businessGroupService.removeParticipants(ureq.getIdentity(), identities, group);
	}
	
	/**
	 * Create a new business group
	 * @param ureq
	 * @param wControl
	 */
	protected void doCreate(UserRequest ureq, WindowControl wControl, RepositoryEntry re) {				
		removeAsListenerAndDispose(groupCreateController);
		groupCreateController = new NewBGController(ureq, wControl, re, false, null);
		listenTo(groupCreateController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), groupCreateController.getInitialComponent(), true, translate("create.form.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	/**
	 * Make copies of a list of business groups
	 * @param ureq
	 * @param items
	 */
	private void doCopy(UserRequest ureq, List<BGTableItem> items) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(items == null || items.isEmpty()) return;
		
		List<BusinessGroup> groups = toBusinessGroups(ureq, items, false);
		
		boolean enableCoursesCopy = businessGroupService.hasResources(groups);
		boolean enableAreasCopy = areaManager.countBGAreasOfBusinessGroups(groups) > 0;
		boolean enableRightsCopy = rightManager.hasBGRight(groups);

		Step start = new BGCopyPreparationStep(ureq, groups, enableCoursesCopy, enableAreasCopy, enableRightsCopy);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				@SuppressWarnings("unchecked")
				List<BGCopyBusinessGroup> copies = (List<BGCopyBusinessGroup>)runContext.get("groupsCopy");
				if(copies != null && !copies.isEmpty()) {
					boolean copyAreas = convertToBoolean(runContext, "areas");
					boolean copyCollabToolConfig = convertToBoolean(runContext, "tools");
					boolean copyRights = convertToBoolean(runContext, "rights");
					boolean copyOwners = convertToBoolean(runContext, "owners");
					boolean copyParticipants = convertToBoolean(runContext, "participants");
					boolean copyMemberVisibility = convertToBoolean(runContext, "membersvisibility");
					boolean copyWaitingList = convertToBoolean(runContext, "waitingList");
					boolean copyRelations = convertToBoolean(runContext, "resources");

					for(BGCopyBusinessGroup copy:copies) {
						businessGroupService.copyBusinessGroup(getIdentity(), copy.getOriginal(), copy.getName(), copy.getDescription(),
								copy.getMinParticipants(), copy.getMaxParticipants(),
								copyAreas, copyCollabToolConfig, copyRights, copyOwners, copyParticipants,
								copyMemberVisibility, copyWaitingList, copyRelations);
					
					}
					return StepsMainRunController.DONE_MODIFIED;
				} else {
					return StepsMainRunController.DONE_UNCHANGED;
				}
			}
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("copy.group"));
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
	}
	
	private boolean convertToBoolean(StepsRunContext runContext, String key) {
		Object obj = runContext.get(key);
		if(obj instanceof Boolean) {
			return ((Boolean)obj).booleanValue();
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doConfiguration(UserRequest ureq, List<BGTableItem> selectedItems) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(selectedItems == null || selectedItems.isEmpty()) return;
		
		final List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		} else if(CollaborationTools.TOOLS == null) {
			//init the available tools
			CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(groups.get(0));
		}
		
		boolean isAuthor = ureq.getUserSession().getRoles().isAuthor()
				|| ureq.getUserSession().getRoles().isInstitutionalResourceManager();

		Step start = new BGConfigToolsStep(ureq, isAuthor);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				//configuration
				BGConfigBusinessGroup configuration = (BGConfigBusinessGroup)runContext.get("configuration");
				if(!configuration.getToolsToEnable().isEmpty() || !configuration.getToolsToDisable().isEmpty()) {
					
					for(BusinessGroup group:groups) {
						CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
						for(String enabledTool:configuration.getToolsToEnable()) {
							tools.setToolEnabled(enabledTool, true);
							if(CollaborationTools.TOOL_FOLDER.equals(enabledTool)) {
								tools.saveFolderAccess(new Long(configuration.getFolderAccess()));
							} else if (CollaborationTools.TOOL_CALENDAR.equals(enabledTool)) {
								tools.saveCalendarAccess(new Long(configuration.getCalendarAccess()));
							}
						}
						for(String disabledTool:configuration.getToolsToDisable()) {
							tools.setToolEnabled(disabledTool, false);
						}
					}
				}
				if(configuration.getResources() != null && !configuration.getResources().isEmpty()) {
					businessGroupService.addResourcesTo(groups, configuration.getResources());
				}
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("config.group"));
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doEmails(UserRequest ureq, List<BGTableItem> selectedItems) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(selectedItems == null || selectedItems.isEmpty()) return;
		
		List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, false);

		Step start = new BGEmailSelectReceiversStep(ureq, groups);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				//mails are send by the last controller of the wizard
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("email.group"));
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doUserManagement(UserRequest ureq, List<BGTableItem> selectedItems) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(userManagementController);
		if(selectedItems == null || selectedItems.isEmpty()) return;
		
		List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		
		userManagementController = new BGUserManagementController(ureq, getWindowControl(), groups);
		listenTo(userManagementController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userManagementController.getInitialComponent(),
				true, translate("users.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void confirmUserManagementEmail(UserRequest ureq, MembershipModification mod, List<BusinessGroup> groups) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(userManagementSendMailController);
		
		MailTemplate template = new BGUserMailTemplate(groups, mod, "", "");
		userManagementSendMailController = new MailNotificationEditController(getWindowControl(), ureq, template, false);
		Component cmp = userManagementSendMailController.getInitialComponent();
		listenTo(userManagementSendMailController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), cmp, true, translate("users.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void finishUserManagement(MembershipModification mod, List<BusinessGroup> groups, MailTemplate template, boolean sendMail) {
		businessGroupService.updateMembership(getIdentity(), mod, groups);
		
		if (template != null && !mod.isEmpty()) {
			List<Identity> movedIdentities = mod.getAllIdentities();
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			MailContext context = new MailContextImpl(null, null, getWindowControl().getBusinessControl().getAsString());
			MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, movedIdentities, null, null, template, getIdentity());
			MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), getLocale());
		}
	}
	
	private void doSelect(UserRequest ureq, List<BGTableItem> items) {
		List<BusinessGroup> selection = toBusinessGroups(ureq, items, false);
		fireEvent(ureq, new BusinessGroupSelectionEvent(selection));
	}
	
	private void doSelect(UserRequest ureq, BusinessGroup group) {
		List<BusinessGroup> selection = Collections.singletonList(group);
		fireEvent(ureq, new BusinessGroupSelectionEvent(selection));
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doMerge(UserRequest ureq, List<BGTableItem> selectedItems) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(selectedItems == null || selectedItems.isEmpty()) return;

		final List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}

		Step start = new BGMergeStep(ureq, groups);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				BusinessGroup targetGroup = (BusinessGroup)runContext.get("targetGroup");
				groups.remove(targetGroup);
				businessGroupService.mergeBusinessGroups(getIdentity(), targetGroup, groups);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("merge.group"));
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
		
	}
	
	/**
	 * Confirmation panel before deleting the groups
	 * @param ureq
	 * @param selectedItems
	 */
	private void confirmDelete(UserRequest ureq, List<BGTableItem> selectedItems) {
		StringBuilder names = new StringBuilder();
		List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}

		for(BusinessGroup group:groups) {
			if(names.length() > 0) names.append(", ");
			names.append(group.getName());
		}
		
		deleteDialogBox = new BusinessGroupDeleteDialogBoxController(ureq, getWindowControl(), groups);
		listenTo(deleteDialogBox);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteDialogBox.getInitialComponent(),
				true, translate("dialog.modal.bg.delete.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private List<BusinessGroup> toBusinessGroups(UserRequest ureq, List<BGTableItem> items, boolean editableOnly) {
		List<Long> groupKeys = new ArrayList<Long>();
		for(BGTableItem item:items) {
			groupKeys.add(item.getBusinessGroupKey());
		}
		if(editableOnly) {
			groupListModel.filterEditableGroupKeys(ureq, groupKeys);
		}
		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(groupKeys);
		return groups;
	}
	
	/**
	 * Deletes the group. Checks if user is in owner group,
	 * otherwise does nothing
	 * 
	 * @param ureq
	 * @param doSendMail specifies if notification mails should be sent to users of delted group
	 */
	private void doDelete(UserRequest ureq, boolean doSendMail, List<BusinessGroup> groups) {
		for(BusinessGroup group:groups) {
			//check security
			boolean ow = ureq.getUserSession().getRoles().isOLATAdmin()
					|| ureq.getUserSession().getRoles().isGroupManager()
					|| securityManager.isIdentityInSecurityGroup(getIdentity(), group.getOwnerGroup());

			if (ow) {
				if (doSendMail) {
					String businessPath = getWindowControl().getBusinessControl().getAsString();
					businessGroupService.deleteBusinessGroupWithMail(group, businessPath, getIdentity(), getLocale());
				} else {
					businessGroupService.deleteBusinessGroup(group);
				}
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), LoggingResourceable.wrap(group));
			}
		}
		showInfo("info.group.deleted");
	}
	
	protected void reloadModel() {
		updateTableModel(lastSearchParams, false);
	}
	
	protected OLATResource getResource() {
		return null;
	}
	
	protected List<BusinessGroupView> searchBusinessGroupViews(SearchBusinessGroupParams params) {
		List<BusinessGroupView> groups;
		if(params == null) {
			groups = new ArrayList<BusinessGroupView>();
		} else {
			groups = businessGroupService.findBusinessGroupViews(params, getResource(), 0, -1);
		}
		return groups;
	}
	
	protected List<BusinessGroupView> updateTableModel(SearchBusinessGroupParams params, boolean alreadyMarked) {
		List<BusinessGroupView> groups = searchBusinessGroupViews(params);
		lastSearchParams = params;
		if(groups.isEmpty()) {
			groupListModel.setEntries(Collections.<BGTableItem>emptyList());
			groupListCtr.modelChanged();
			return groups;
		}

		List<Long> groupKeysWithMembers;
		if(groups.size() > 50) {
			groupKeysWithMembers = null;
		} else {
			groupKeysWithMembers = new ArrayList<Long>(groups.size());
			for(BusinessGroupView view:groups) {
				groupKeysWithMembers.add(view.getKey());
			}
		}

		//retrieve all user's membership if there are more than 50 groups
		List<BusinessGroupMembership> groupsAsOwner = businessGroupService.getBusinessGroupMembership(groupKeysWithMembers, getIdentity());
		Map<Long, BusinessGroupMembership> memberships = new HashMap<Long, BusinessGroupMembership>();
		for(BusinessGroupMembership membership: groupsAsOwner) {
			memberships.put(membership.getGroupKey(), membership);
		}
		
		//find resources / courses
		List<Long> groupKeysWithRelations = new ArrayList<Long>();
		for(BusinessGroupView view:groups) {
			if(view.getNumOfRelations() > 0) {
				groupKeysWithRelations.add(view.getKey());
			}
		}
		List<BGRepositoryEntryRelation> resources = businessGroupService.findRelationToRepositoryEntries(groupKeysWithRelations, 0, -1);
		
		//find offers
		List<Long> groupWithOfferKeys = new ArrayList<Long>(groups.size());
		for(BusinessGroupView view:groups) {
			if(view.getNumOfOffers() > 0) {
				groupWithOfferKeys.add(view.getResource().getKey());
			}
		}
		List<OLATResourceAccess> resourcesWithAC;
		if(groupWithOfferKeys.size() > 50) {
			resourcesWithAC	= acService.getAccessMethodForResources(null, "BusinessGroup", true, new Date());
		} else {
			resourcesWithAC	= acService.getAccessMethodForResources(groupWithOfferKeys, "BusinessGroup", true, new Date());
		}
		
		Set<Long> markedResources = new HashSet<Long>(groups.size() * 2 + 1);
		for(BusinessGroupView group:groups) {
			markedResources.add(group.getResource().getResourceableId());
		}
		if(!alreadyMarked) {
			markManager.filterMarks(getIdentity(), "BusinessGroup", markedResources);
		}
		
		List<BGTableItem> items = new ArrayList<BGTableItem>();
		for(BusinessGroupView group:groups) {
			Long oresKey = group.getResource().getKey();
			List<PriceMethodBundle> accessMethods = null;
			for(OLATResourceAccess access:resourcesWithAC) {
				if(oresKey.equals(access.getResource().getKey())){
					accessMethods = access.getMethods();
					break;
				}
			}
			
			BusinessGroupMembership membership =  memberships.get(group.getKey());
			Boolean allowLeave =  membership != null;
			Boolean allowDelete = admin ? Boolean.TRUE : null;
			boolean marked = markedResources.contains(group.getResource().getResourceableId());
			BGTableItem tableItem = new BGTableItem(group, marked, membership, allowLeave, allowDelete, accessMethods);
			tableItem.setUnfilteredRelations(resources);
			items.add(tableItem);
		}
		
		groupListModel.setEntries(items);
		groupListCtr.modelChanged();
		return groups;
	}
}
