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
* <p>
*/ 

package org.olat.group.ui.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.dispatcher.jumpin.JumpInManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.BGTranslatorFactory;
import org.olat.group.ui.BusinessGroupTableModel;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.NewBGController;
import org.olat.group.ui.area.BGAreaEditController;
import org.olat.group.ui.area.BGAreaFormController;
import org.olat.group.ui.area.BGAreaTableModel;
import org.olat.group.ui.context.BGContextEditController;
import org.olat.group.ui.context.BGContextEvent;
import org.olat.group.ui.edit.BusinessGroupEditController;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.group.ui.run.BusinessGroupSendToChooserForm;
import org.olat.group.ui.wizard.BGCopyWizardController;
import org.olat.group.ui.wizard.BGMultipleCopyWizardController;
import org.olat.group.ui.wizard.MemberListWizardController;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryTableModel;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.HomePageDisplayController;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<BR/> This controller provides a complete groupmanagement for a
 * given group context. The provided functionality is configured using the
 * BGConfigurationFlags. If you want to use this class, don't use the
 * constructor but get an instance using the BGControllerFactory <P/> Initial
 * Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public class BGManagementController extends MainLayoutBasicController implements GenericEventListener {
	private static final String PACKAGE = Util.getPackageName(BGManagementController.class);

	// Menu commands
	private static final String CMD_OVERVIEW = "cmd.overview";
	private static final String CMD_EDITCONTEXT = "cmd.editcontext";
	private static final String CMD_GROUPLIST = "cmd.grouplist";
	private static final String CMD_AREALIST = "cmd.arealist";
	// Toolbox commands
	private static final String CMD_GROUP_CREATE = "cmd.group.create";
	private static final String CMD_AREA_CREATE = "cmd.area.create";
	private static final String CMD_CLOSE = "cmd.close";
	private static final String CMD_BACK = "cmd.back";
	// List commands
	private static final String CMD_GROUP_RUN = "cmd.group.run";
	private static final String CMD_GROUP_MESSAGE = "cmd.group.message";
	private static final String CMD_GROUP_EDIT = "cmd.group.edit";
	private static final String CMD_GROUP_DELETE = "cmd.group.delete";
	private static final String CMD_GROUP_COPY = "cmd.group.copy";
	private static final String CMD_GROUP_COPY_MULTIPLE = "cmd.group.copy.multiple";
	private static final String CMD_AREA_EDIT = "cmd.area.edit";
	private static final String CMD_AREA_DELETE = "cmd.area.delete";
	// User commands
	private static final String CMD_USER_LIST = "cmd.user.list";
	private static final String CMD_USER_DETAILS = "cmd.user.details";
	private static final String CMD_USER_REMOVE_GROUP_PART = "cmd.user.remove.group.part";
	private static final String CMD_USER_REMOVE_GROUP_OWN = "cmd.user.remove.group.own";
	private static final String CMD_USER_MESSAGE = "cmd.user.message";
	private static final String CMD_OWNERS_MESSAGE = "cmd.owners.message";
	private static final String CMD_PARTICIPANTS_MESSAGE = "cmd.participants.message";
	private static final String CMD_LIST_MEMBERS_WITH_GROUPS = "cmd.list.members.with.groups";
	private static final String CMD_LIST_MEMBERS_WITH_AREAS = "cmd.list.members.with.areas";

	private Translator areaTrans, userTrans;
	private BGContext bgContext;
	private String groupType;
	private BGConfigFlags flags;
	private boolean isContextOwner;
	private static final int STATE_OVERVIEW = 1;
	private static final int STATE_CONTEXT_EDIT = 2;
	private static final int STATE_CONTEXT_REMOVED = 3;
	private static final int STATE_GROUP_CREATE_FORM = 100;
	private static final int STATE_GROUP_EDIT = 101;
	private static final int STATE_GROUP_LIST = 102;
	private static final int STATE_AREA_CREATE_FORM = 200;
	private static final int STATE_AREA_EDIT = 201;
	private static final int STATE_AREA_LIST = 202;

	private static final int STATE_USER_LIST = 300;
	private static final int STATE_USER_DETAILS = 301;

	private BusinessGroupEditController groupEditCtr;
	private BGAreaEditController areaEditCtr;
	private VelocityContainer overviewVC, newGroupVC, sendMessageVC, contextEditVC, vc_sendToChooserForm;
	private BusinessGroupSendToChooserForm sendToChooserForm;
	private Translator businessGroupTranslator;
	private boolean isGMAdminOwner;
	private VelocityContainer newAreaVC, areaListVC, groupListVC, userListVC, userDetailsVC;
	private BusinessGroupTableModel groupListModel;
	private BGAreaTableModel areaListModel;
	private TableController groupListCtr, areaListCtr, ownerListCtr, participantListCtr;
	private UserTableDataModel ownerListModel, participantListModel;
	private HomePageDisplayController homePageDisplayController;
	private DialogBoxController confirmDeleteGroup, confirmDeleteArea;
	private ContactFormController contactCtr;
	private BGCopyWizardController bgCopyWizardCtr;
	private BGMultipleCopyWizardController bgMultipleCopyWizardCtr;
	private BGContextEditController contextEditCtr;
	private TableController resourcesCtr;
	private GroupController contextOwnersCtr;

	// Layout components and controllers
	private Panel content;
	private LayoutMain3ColsController columnLayoutCtr;
	private MenuTree olatMenuTree;
	private ToolController toolC;

	// Managers
	private BusinessGroupManager groupManager;
	private BGContextManager contextManager;
	private BGAreaManager areaManager;

	// Workflow variables
	private List areaFilters;
	private BGArea currentAreaFilter;
	private Component backComponent, currentComponent;
	private BusinessGroup currentGroup;
	private BGArea currentArea;
	private Identity currentIdentity;
	private Link backButton;
	private NewBGController groupCreateController;
	private NewAreaController areaCreateController;
		
	private CloseableModalController closeableModalController;
	private MemberListWizardController memberListWizardController;

	/**
	 * Use BGControllerFactrory to create such a controller. DO NOT USE THIS
	 * CONSTRUCTOR!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param bgContext
	 * @param controllerFlags
	 */
	public BGManagementController(UserRequest ureq, WindowControl wControl, BGContext bgContext, BGConfigFlags controllerFlags) {
		super(ureq, wControl);
		this.bgContext = bgContext;
		this.groupType = bgContext.getGroupType();
		this.flags = controllerFlags;

		// Initialize managers
		groupManager = BusinessGroupManagerImpl.getInstance();
		contextManager = BGContextManagerImpl.getInstance();
		if (flags.isEnabled(BGConfigFlags.AREAS)) areaManager = BGAreaManagerImpl.getInstance();

		businessGroupTranslator = Util.createPackageTranslator(BusinessGroupMainRunController.class, ureq.getLocale());
		// Initialize translator
		// 1 - package translator with default group fallback translators and type
		// translator
		setTranslator(BGTranslatorFactory.createBGPackageTranslator(PACKAGE, this.groupType, ureq.getLocale()));
		// 2 - area specific translator
		if (flags.isEnabled(BGConfigFlags.AREAS)) {
			//areaTrans = new PackageTranslator(Util.getPackageName(BGAreaForm.class), ureq.getLocale(), trans);
			areaTrans = Util.createPackageTranslator(BGAreaFormController.class, ureq.getLocale(), getTranslator());
		}
		// user translator
		this.userTrans = Util.createPackageTranslator(UserManager.class, ureq.getLocale());

		// initialize all velocity containers
		initVC();

		// check if user is owner of this group context
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		this.isContextOwner = securityManager.isIdentityInSecurityGroup(ureq.getIdentity(), this.bgContext.getOwnerGroup());

		// Layout is controlled with generic controller: menu - content - tools
		// Navigation menu
		olatMenuTree = new MenuTree("olatMenuTree");
		TreeModel tm = buildTreeModel(ureq);
		olatMenuTree.setTreeModel(tm);
		olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		olatMenuTree.addListener(this);
		// Content
		content = new Panel("content");
		// Tools
		// 1 create empty Tools and init menuAndToolController
		// 2 set correct tools using setTools method (override step 1)
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, toolC.getInitialComponent(), content, "groupmngt" + bgContext.getKey());
		listenTo(columnLayoutCtr);

		doOverview(ureq);

		putInitialPanel(columnLayoutCtr.getInitialComponent());

		
		
		//disposed message controller
		//must be created beforehand
		Panel empty = new Panel("empty");//empty panel set as "menu" and "tool"
		Controller courseCloser = new DisposedBGAManagementController(ureq, wControl, this);
		listenTo(courseCloser);
		Controller disposedBGAManagementController = new LayoutMain3ColsController(ureq, wControl, empty, empty, courseCloser.getInitialComponent(), "disposed " + "groupmngt" + bgContext.getKey());
		listenTo(disposedBGAManagementController);
		setDisposedMsgController(disposedBGAManagementController);

		
		
		// register for changes in this group context
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), this.bgContext);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		String cmd = event.getCommand();
		if (source == olatMenuTree) {
			if (cmd.equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				handleMenuCommands(ureq);
			}
		} else if (source == backButton){
			doUsersList(ureq, true); // for now init whole table models again
		} else if (source instanceof Link) {
			Link link = (Link) source;
			BusinessGroup group = (BusinessGroup) link.getUserObject();
			String groupKey = group.getKey().toString();
			if (link.getCommand().indexOf(CMD_USER_REMOVE_GROUP_PART) == 0) {
				doRemoveUserFromParticipatingGroup(ureq.getIdentity(), this.currentIdentity, groupKey);
				doUserDetails(ureq);
			} else if (link.getCommand().indexOf(CMD_USER_REMOVE_GROUP_OWN) == 0) {
				doRemoveUserFromOwnedGroup(ureq, groupKey);
				doUserDetails(ureq);
			}
		}
	}
	
	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof BGContextEvent) {
			BGContextEvent contextEvent = (BGContextEvent) event;
			if (contextEvent.getBgContextKey().equals(this.bgContext.getKey())) {
				if (contextEvent.getCommand().equals(BGContextEvent.CONTEXT_DELETED)
						|| contextEvent.getCommand().equals(BGContextEvent.RESOURCE_REMOVED)) {
					//this results in a screen where the BGManagementController
					//is no longer functional -> hence only closeable
					dispose();//disposed message is defined in constructor!
				}
			}

		} else if (event instanceof BusinessGroupModifiedEvent) {
			if (event.getCommand().equals(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT)) {
				// update reference to updated business group object
				BusinessGroup modifiedGroup = groupManager.loadBusinessGroup(this.currentGroup);
				if (groupListModel != null) {
					List groups = groupListModel.getObjects();
					if (groups.contains(this.currentGroup)) {
						int i = groups.indexOf(this.currentGroup);
						groups.set(i, modifiedGroup);
					}
				}
				this.currentGroup = modifiedGroup;
			}
		}
	}

	private void doGroupMessage(UserRequest ureq) {
		List list = new ArrayList();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		list.addAll(securityManager.getIdentitiesOfSecurityGroup(currentGroup.getPartipiciantGroup()));
		if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) list
				.addAll(securityManager.getIdentitiesOfSecurityGroup(currentGroup.getOwnerGroup()));
		// right group has a different groupname in the to field.
		String groupMailToName;
		if (flags.isEnabled(BGConfigFlags.RIGHTS)) groupMailToName = translate("rightgroup.message.to");
		else groupMailToName = translate("group.message.to");
		doContactForm(ureq);
		sendMessageVC.contextPut("title", translate("group.message", this.currentGroup.getName()));
	}
	
	/**removeAsListenerAndDispose
	 * @param ureq
	 */
	private void doContactForm(UserRequest ureq) {
		if (vc_sendToChooserForm == null) {
			vc_sendToChooserForm = new VelocityContainer("cosendtochooser", BusinessGroupMainRunController.class, "cosendtochooser", businessGroupTranslator, this);
		}
		removeAsListenerAndDispose(sendToChooserForm);
		sendToChooserForm = new BusinessGroupSendToChooserForm(ureq, getWindowControl(), this.currentGroup, getIsGMAdminOwner(ureq));
		listenTo(sendToChooserForm);
		vc_sendToChooserForm.put("vc_sendToChooserForm", sendToChooserForm.getInitialComponent());
		content.setContent(vc_sendToChooserForm);
	}

	/**
	 * @param ureq
	 * @return
	 */
	private boolean getIsGMAdminOwner(UserRequest ureq) {
		boolean isOwner = false;
		if (this.currentGroup != null) {
			isOwner = BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(ureq.getIdentity(), Constants.PERMISSION_ACCESS, this.currentGroup);
		}
		isGMAdminOwner = isOwner || flags.isEnabled(BGConfigFlags.IS_GM_ADMIN);
		return isGMAdminOwner;
	}

	private void doOwnersMessage(UserRequest ureq) {
		List owners = ownerListModel.getObjects();
		doSendMessage(owners, translate("owners.message.to"), ureq);
		sendMessageVC.contextPut("title", translate("owners.message"));
	}

	private void doParticipantsMessage(UserRequest ureq) {
		List participants = participantListModel.getObjects();
		doSendMessage(participants, translate("participants.message.to"), ureq);
		sendMessageVC.contextPut("title", translate("participants.message"));
	}

	private void doUserMessage(UserRequest ureq) {
		List users = new ArrayList();
		users.add(this.currentIdentity);
		User user = this.currentIdentity.getUser();
		Locale loc = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		doSendMessage(users, user.getProperty(UserConstants.FIRSTNAME, loc) + " " + user.getProperty(UserConstants.LASTNAME, loc), ureq);
		sendMessageVC.contextPut("title", getTranslator().translate("user.message", new String[] { this.currentIdentity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()),
				this.currentIdentity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()) }));
	}

	private void doSendMessage(List identities, String mailToName, UserRequest ureq) {
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList contactList = new ContactList(mailToName);
		contactList.addAllIdentites(identities);
		cmsg.addEmailTo(contactList);
		removeAsListenerAndDispose(contactCtr);
		contactCtr = new ContactFormController(ureq, getWindowControl(), false, true, false, false, cmsg);
		listenTo(contactCtr);
		sendMessageVC.put("contactForm", contactCtr.getInitialComponent());
		setMainContent(sendMessageVC);
	}

	private void doAreaDeleteConfirm(UserRequest ureq) {
		confirmDeleteArea = activateYesNoDialog(ureq, null, translate("area.delete", this.currentArea.getName() ), confirmDeleteArea);
	}

	private void doGroupDeleteConfirm(UserRequest ureq) {
		String confirmDeleteGroupText;
		List<String> deleteableList = groupManager.getDependingDeletablableListFor(currentGroup, ureq.getLocale());
		if (deleteableList.isEmpty()) {
			confirmDeleteGroupText = translate("group.delete", this.currentGroup.getName() );
		} else {
			StringBuilder buf = new StringBuilder();
			for (String element : deleteableList) {
				if (buf.length() > 0) buf.append(" ,");
				buf.append(element);
			}
			String[] args = new String[] {this.currentGroup.getName(), buf.toString()};
			confirmDeleteGroupText = translate("group.delete.in.use", args );
		}
		confirmDeleteGroup = activateYesNoDialog(ureq, null, confirmDeleteGroupText, confirmDeleteGroup);
	}

	private void doContextEdit(UserRequest ureq) {
		if (isContextOwner || ureq.getUserSession().getRoles().isOLATAdmin()) {
			removeAsListenerAndDispose(contextEditCtr);
			contextEditCtr = new BGContextEditController(ureq, getWindowControl(), this.bgContext);
			listenTo(contextEditCtr);
			contextEditVC.put("contexteditor", contextEditCtr.getInitialComponent());
			contextEditVC.contextPut("editingAllowed", Boolean.TRUE);
		} else {
			// show who is the owner of this context
			removeAsListenerAndDispose(contextOwnersCtr);
			contextOwnersCtr = new GroupController(ureq, getWindowControl(), false, true, false, this.bgContext.getOwnerGroup());
			listenTo(contextOwnersCtr);
			contextEditVC.put("owners", contextOwnersCtr.getInitialComponent());
			contextEditVC.contextPut("editingAllowed", Boolean.FALSE);
		}

		setMainContent(contextEditVC);
		setTools(STATE_CONTEXT_EDIT);
	}
	
	private void listMembers(UserRequest ureq, String cmd) {
		if(CMD_LIST_MEMBERS_WITH_GROUPS.equals(cmd)) {
			if(BGContextManagerImpl.getInstance().getGroupsOfBGContext(bgContext).size()==0) {
				showError("tools.title.listmembers.warning.noGroups");
				return;
			}
			removeAsListenerAndDispose(memberListWizardController);
			memberListWizardController = new MemberListWizardController(ureq, getWindowControl(), bgContext, MemberListWizardController.GROUPS_MEMBERS);
			listenTo(memberListWizardController);
		} else if(CMD_LIST_MEMBERS_WITH_AREAS.equals(cmd)) {
			if(BGAreaManagerImpl.getInstance().findBGAreasOfBGContext(bgContext).size()==0) {
				showError("tools.title.listmembers.warning.noAreas");
				return;
			}
			removeAsListenerAndDispose(memberListWizardController);
			memberListWizardController = new MemberListWizardController(ureq, getWindowControl(), bgContext, MemberListWizardController.AREAS_MEMBERS);
			listenTo(memberListWizardController);
		}
		if(memberListWizardController!=null) {
			removeAsListenerAndDispose(closeableModalController);
			closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), memberListWizardController.getInitialComponent());
			listenTo(closeableModalController);
			closeableModalController.activate();   
		}
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		String cmd = event.getCommand();
		if (source == toolC) {
			handleToolCommands(ureq, cmd);
		} else if (source == groupEditCtr) {
			if (event == Event.CANCELLED_EVENT) { // when group was locked
				releaseAdminLockAndGroupMUE();
				doBack();
			}
		} else if (source == bgCopyWizardCtr) {
			if (event.equals(Event.DONE_EVENT)) {
				BusinessGroup newGroup = bgCopyWizardCtr.getNewGroup();
				if (newGroup == null) {
					throw new AssertException("bgCopyWizardCtr.getNewGroup returned null");
				} else {
					releaseAdminLockAndGroupMUE();
					getWindowControl().pop();
					this.currentGroup = newGroup;
					doGroupEdit(ureq);
				}
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
			}
		} else if (source == bgMultipleCopyWizardCtr) {
			if (event.equals(Event.DONE_EVENT)) {
				releaseAdminLockAndGroupMUE();
				getWindowControl().pop();
				doGroupList(ureq, true);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
			}
		} else if (source == confirmDeleteGroup) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				releaseAdminLockAndGroupMUE();
				String deletedGroupName = this.currentGroup.getName();
				LoggingResourceable lri = LoggingResourceable.wrap(currentGroup);
				doGroupDelete();
				doGroupList(ureq, false);
				// do logging
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), lri);
				showInfo("info.group.deleted");
			}
		} else if (source == areaEditCtr) {
			// TODO event: changed area: update models
		} else if (source == confirmDeleteArea) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				String deletedAreaName = this.currentArea.getName();
				LoggingResourceable lri = LoggingResourceable.wrap(currentArea);
				doAreaDelete();
				doAreaList(ureq, false);
				// do logging
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.AREA_DELETED, getClass(), lri);
			}
		} else if (source == contactCtr) {
			if (event.equals(Event.DONE_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				doBack();
			}
		} else if (source == groupListCtr) {
			if (event.equals(TableController.EVENT_NOFILTER_SELECTED)) {
				this.currentAreaFilter = null;
				doGroupList(ureq, true);
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				this.currentAreaFilter = (BGArea) groupListCtr.getActiveFilter();
				doGroupList(ureq, true);
			} else if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				this.currentGroup = groupListModel.getBusinessGroupAt(rowid);
				if (actionid.equals(CMD_GROUP_EDIT)) {
					doGroupEdit(ureq);
				} else if (actionid.equals(CMD_GROUP_RUN)) {
					doGroupRun(ureq);
				} else if (actionid.equals(CMD_GROUP_DELETE)) {
					doGroupDeleteConfirm(ureq);
				}
			}
		} else if (source == areaListCtr) {
			if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				this.currentArea = areaListModel.getBGAreaAt(rowid);
				if (actionid.equals(CMD_AREA_EDIT)) {
					doAreaEdit(ureq);
				} else if (actionid.equals(CMD_AREA_DELETE)) {
					doAreaDeleteConfirm(ureq);
				}
			}
		} else if (source == ownerListCtr) {
			if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				this.currentIdentity = ownerListModel.getIdentityAt(rowid);
				if (actionid.equals(CMD_USER_DETAILS)) {
					doUserDetails(ureq);
				}
			}
		} else if (source == participantListCtr) {
			if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				this.currentIdentity = participantListModel.getIdentityAt(rowid);
				if (actionid.equals(CMD_USER_DETAILS)) {
					doUserDetails(ureq);
				}
			}
		} else if (source == contextEditCtr) {
			if (event == Event.CHANGED_EVENT) {
				// reload context, maybe updated title or something
				this.bgContext = contextManager.loadBGContext(this.bgContext);
			}
		} else if (source == groupCreateController){
			if (event == Event.DONE_EVENT) {
				releaseAdminLockAndGroupMUE();
				this.currentGroup = groupCreateController.getCreatedGroup();
				doGroupEdit(ureq);
				// do loggin is already done in create controller
			} else if (event == Event.CANCELLED_EVENT) {
				doBack();
			}
		} else if (source == areaCreateController){
			if (event == Event.DONE_EVENT) {
				releaseAdminLockAndGroupMUE();
				BGArea createdArea = areaCreateController.getCreatedArea();
				if (createdArea != null) {
					this.currentArea = createdArea;
					doAreaEdit(ureq);
				} else {
					showInfo("error.area.name.exists");
				}
				// do loggin is already done in create controller
			} else if (event == Event.CANCELLED_EVENT) {
				doBack();
			}
		} else if (source == memberListWizardController) {		
			closeableModalController.deactivate();			
		} else if (source == sendToChooserForm) {
			if (event == Event.DONE_EVENT) {
				removeAsListenerAndDispose(contactCtr);
				contactCtr = createContactFormController(ureq);
				listenTo(contactCtr);
				sendMessageVC.put("contactForm", contactCtr.getInitialComponent());
				setMainContent(sendMessageVC);
			} else if (event == Event.CANCELLED_EVENT) {
				content.setContent(this.currentComponent);
			}
		}
	}		

	private void handleToolCommands(UserRequest ureq, String cmd) {
		if (cmd.equals(CMD_CLOSE)) {
			releaseAdminLockAndGroupMUE();
			// Send done event to parent controller
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (cmd.equals(CMD_BACK)) {
			releaseAdminLockAndGroupMUE();
			// Send back event to parent controller
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (cmd.equals(CMD_GROUP_CREATE)) {
			createNewGroupController(ureq, getWindowControl());
		} else if (cmd.equals(CMD_AREA_CREATE)) {
			createNewAreaController(ureq, getWindowControl());
		} else if (cmd.equals(CMD_GROUP_RUN)) {
			doGroupRun(ureq);
		} else if (cmd.equals(CMD_GROUP_COPY)) {
			doGroupCopy(ureq);
		} else if (cmd.equals(CMD_GROUP_COPY_MULTIPLE)) {
			doMultipleGroupCopy(ureq);
		} else if (cmd.equals(CMD_GROUP_DELETE)) {
			doGroupDeleteConfirm(ureq);
		} else if (cmd.equals(CMD_GROUP_MESSAGE)) {
			doGroupMessage(ureq);
		} else if (cmd.equals(CMD_AREA_DELETE)) {
			doAreaDeleteConfirm(ureq);
		} else if (cmd.equals(CMD_PARTICIPANTS_MESSAGE)) {
			doParticipantsMessage(ureq);
		} else if (cmd.equals(CMD_OWNERS_MESSAGE)) {
			doOwnersMessage(ureq);
		} else if (cmd.equals(CMD_USER_MESSAGE)) {
			doUserMessage(ureq);
		} else if (cmd.equals(CMD_LIST_MEMBERS_WITH_GROUPS)) {
			listMembers(ureq,CMD_LIST_MEMBERS_WITH_GROUPS);
		} else if(cmd.equals(CMD_LIST_MEMBERS_WITH_AREAS)) {
			listMembers(ureq,CMD_LIST_MEMBERS_WITH_AREAS);
		}

	}

	private void handleMenuCommands(UserRequest ureq) {
		TreeNode selTreeNode = olatMenuTree.getSelectedNode();
		String cmd = (String) selTreeNode.getUserObject();

		releaseAdminLockAndGroupMUE();
		if (cmd.equals(CMD_OVERVIEW)) {
			doOverview(ureq);
		} else if (cmd.equals(CMD_EDITCONTEXT)) {
			doContextEdit(ureq);
		} else if (cmd.equals(CMD_GROUPLIST)) {
			this.currentAreaFilter = null;
			doGroupList(ureq, true);
		} else if (cmd.equals(CMD_AREALIST)) {
			doAreaList(ureq, true);
		} else if (cmd.equals(CMD_USER_LIST)) {
			doUsersList(ureq, true);
		}
	}

	private TreeModel buildTreeModel(UserRequest ureq) {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject(CMD_OVERVIEW);
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		if (!this.bgContext.isDefaultContext() || ureq.getUserSession().getRoles().isOLATAdmin()) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.editcontext"));
			gtn.setUserObject(CMD_EDITCONTEXT);
			gtn.setAltText(translate("menu.editcontext.alt"));
			root.addChild(gtn);
		}

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.allgroups"));
		gtn.setUserObject(CMD_GROUPLIST);
		gtn.setAltText(translate("menu.allgroups.alt"));
		root.addChild(gtn);

		if (flags.isEnabled(BGConfigFlags.AREAS)) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.allareas"));
			gtn.setUserObject(CMD_AREALIST);
			gtn.setAltText(translate("menu.allareas.alt"));
			root.addChild(gtn);
		}

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.allusers"));
		gtn.setUserObject(CMD_USER_LIST);
		gtn.setAltText(translate("menu.allusers.alt"));
		root.addChild(gtn);

		return gtm;
	}

	private void setTools(int state) {
		removeAsListenerAndDispose(toolC);
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		columnLayoutCtr.setCol2(toolC.getInitialComponent());
		if (state == STATE_CONTEXT_REMOVED) {
			toolC.addHeader(translate("tools.title.groupmanagement"));
			toolC.addLink(CMD_CLOSE, translate(CMD_CLOSE), null, "b_toolbox_close");
			return;
		}

		// header for generic action. if groups have rights, assueme
		// rightsmanagement
		// otherwhise groupmanagement
		if (flags.isEnabled(BGConfigFlags.RIGHTS)) toolC.addHeader(translate("tools.title.rightmanagement"));
		else toolC.addHeader(translate("tools.title.groupmanagement"));

		// Generic actions
		if (flags.isEnabled(BGConfigFlags.GROUPS_CREATE)) toolC.addLink(CMD_GROUP_CREATE, translate(CMD_GROUP_CREATE));
		if (flags.isEnabled(BGConfigFlags.AREAS)) toolC.addLink(CMD_AREA_CREATE, translate(CMD_AREA_CREATE));
		if (flags.isEnabled(BGConfigFlags.BACK_SWITCH)) toolC.addLink(CMD_BACK, translate(CMD_BACK));
		toolC.addLink(CMD_CLOSE, translate(CMD_CLOSE), null, "b_toolbox_close");
		
		//TODO: (LD) check where is this displayable.
		toolC.addHeader(translate("tools.title.listmembers"));
		//TODO: (LD) check flags
		toolC.addLink(CMD_LIST_MEMBERS_WITH_GROUPS, translate(CMD_LIST_MEMBERS_WITH_GROUPS));
		toolC.addLink(CMD_LIST_MEMBERS_WITH_AREAS, translate(CMD_LIST_MEMBERS_WITH_AREAS));

		if (state == STATE_GROUP_EDIT) {
			toolC.addHeader(translate("tools.title.group"));
			toolC.addLink(CMD_GROUP_MESSAGE, translate(CMD_GROUP_MESSAGE));
			toolC.addLink(CMD_GROUP_RUN, translate(CMD_GROUP_RUN));
			toolC.addLink(CMD_GROUP_COPY, translate(CMD_GROUP_COPY));
			toolC.addLink(CMD_GROUP_COPY_MULTIPLE, translate(CMD_GROUP_COPY_MULTIPLE));
			if (flags.isEnabled(BGConfigFlags.GROUPS_DELETE)) toolC.addLink(CMD_GROUP_DELETE, translate(CMD_GROUP_DELETE));
		}

		if (state == STATE_AREA_EDIT && flags.isEnabled(BGConfigFlags.AREAS_DELETE)) {
			toolC.addHeader(translate("tools.title.area"));
			toolC.addLink(CMD_AREA_DELETE, translate(CMD_AREA_DELETE));
		}

		if (state == STATE_USER_LIST) {
			toolC.addHeader(translate("tools.title.userlist"));
			if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) toolC.addLink(CMD_OWNERS_MESSAGE, translate(CMD_OWNERS_MESSAGE));
			toolC.addLink(CMD_PARTICIPANTS_MESSAGE, translate(CMD_PARTICIPANTS_MESSAGE));
		}

		if (state == STATE_USER_DETAILS) {
			toolC.addHeader(translate("tools.title.user"));
			toolC.addLink(CMD_USER_MESSAGE, translate(CMD_USER_MESSAGE));
		}

	}

	private void initVC() {
		// push group type as 'type' for type specific help pages
		// Overview page
		overviewVC = createVelocityContainer("overview");
		overviewVC.contextPut("flags", flags);
		overviewVC.contextPut("type", this.groupType);
		// Context edit container - init anyway, maybe not used
		contextEditVC = createVelocityContainer("contextedit");
		// Create new group form
		newGroupVC = createVelocityContainer("newgroup");
		newGroupVC.contextPut("type", this.groupType);
		// Group list
		groupListVC = createVelocityContainer("grouplist");
		groupListVC.contextPut("type", this.groupType);
		// Group message
		sendMessageVC = createVelocityContainer("sendmessage");
		sendMessageVC.contextPut("type", this.groupType);
		if (flags.isEnabled(BGConfigFlags.AREAS)) {
			// Create new area form
			newAreaVC = createVelocityContainer("newarea");
			newAreaVC.contextPut("type", this.groupType);
			// Area list
			areaListVC = createVelocityContainer("arealist");
			areaListVC.contextPut("type", this.groupType);
		}
		// User list
		userListVC = createVelocityContainer("userlist");
		userListVC.contextPut("type", this.groupType);
		// User details
		userDetailsVC = new VelocityContainer("userdetails", Util.getPackageVelocityRoot(this.getClass()) + "/userdetails.html", 
				Util.createPackageTranslator(HomePageDisplayController.class, getLocale(), getTranslator())
		, this);
		backButton = LinkFactory.createButtonSmall("back", userDetailsVC, this);
		userDetailsVC.contextPut("type", this.groupType);
	}

	private void doOverview(UserRequest ureq) {
		setMainContent(overviewVC);
		// number of groups
		overviewVC.contextPut("numbGroups", new Integer(contextManager.countGroupsOfBGContext(bgContext)));
		// number of owners
		if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
			int total = (contextManager.countBGOwnersOfBGContext(bgContext) + contextManager.countBGParticipantsOfBGContext(bgContext));
			overviewVC.contextPut("numbTotal", new Integer(total));
			overviewVC.contextPut("numbOwners", new Integer(contextManager.countBGOwnersOfBGContext(bgContext)));
		}
		overviewVC.contextPut("numbParticipants", new Integer(contextManager.countBGParticipantsOfBGContext(bgContext)));
		// number of areas
		if (flags.isEnabled(BGConfigFlags.AREAS)) {
			overviewVC.contextPut("numbAreas", new Integer(areaManager.countBGAreasOfBGContext(bgContext)));
		}
		// context name
		if (this.bgContext.isDefaultContext()) {
			overviewVC.contextPut("showContextName", Boolean.FALSE);
		} else {
			overviewVC.contextPut("showContextName", Boolean.TRUE);
			overviewVC.contextPut("contextName", bgContext.getName());
			overviewVC.contextPut("contextDesc", bgContext.getDescription());
		}
		if (this.bgContext.isDefaultContext()) {
			overviewVC.contextPut("isDefaultContext", Boolean.TRUE);
		} else {
			overviewVC.contextPut("isDefaultContext", Boolean.FALSE);
			// other resources that also use this context
			doAddOtherResourcesList(ureq);
		}

		setTools(STATE_OVERVIEW);
	}

	private void doAddOtherResourcesList(UserRequest ureq) {
		List repoTableModelEntries = contextManager.findRepositoryEntriesForBGContext(this.bgContext);
		if (repoTableModelEntries.size() > 1) {
			Translator resourceTrans = Util.createPackageTranslator(RepositoryTableModel.class, ureq.getLocale(), getTranslator()); 
			
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();		
			removeAsListenerAndDispose(resourcesCtr);
			resourcesCtr = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans);
			listenTo(resourcesCtr);
			RepositoryTableModel repoTableModel = new RepositoryTableModel(resourceTrans);
			repoTableModel.setObjects(repoTableModelEntries);
			repoTableModel.addColumnDescriptors(resourcesCtr, null, false);
			resourcesCtr.setTableDataModel(repoTableModel);
			overviewVC.put("otherResources", resourcesCtr.getInitialComponent());
			overviewVC.contextPut("usedByOtherResources", Boolean.TRUE);
		} else {
			overviewVC.contextRemove("otherResources");
			overviewVC.contextPut("usedByOtherResources", Boolean.FALSE);
		}
	}

	/*
	 * create and init controller to create new area(s)
	 */
	private void createNewAreaController(UserRequest ureq, WindowControl wControl) {
		removeAsListenerAndDispose(areaCreateController);
		areaCreateController = BGControllerFactory.getInstance().createNewAreaController(ureq, wControl, bgContext);
		listenTo(areaCreateController);
				
		newAreaVC.put("areaCreateForm", areaCreateController.getInitialComponent());
		setMainContent(newAreaVC);
		setTools(STATE_AREA_CREATE_FORM);
	}
	
	private void createNewGroupController(UserRequest ureq, WindowControl wControl) {				
		removeAsListenerAndDispose(groupCreateController);
		groupCreateController = BGControllerFactory.getInstance().createNewBGController(ureq, wControl,
				flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE), bgContext);
		listenTo(groupCreateController);
		
		newGroupVC.put("groupCreateForm", groupCreateController.getInitialComponent());
		setMainContent(newGroupVC);
		setTools(STATE_GROUP_CREATE_FORM);
	}

	private void doGroupCopy(UserRequest ureq) {
		removeAsListenerAndDispose(bgCopyWizardCtr);
		bgCopyWizardCtr = new BGCopyWizardController(ureq, getWindowControl(), this.currentGroup, this.flags);
		listenTo(bgCopyWizardCtr);
		removeAsListenerAndDispose(closeableModalController);
		closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), bgCopyWizardCtr.getInitialComponent());
		listenTo(closeableModalController);
		
		closeableModalController.activate();
	}

	private void doMultipleGroupCopy(UserRequest ureq) {
		removeAsListenerAndDispose(bgMultipleCopyWizardCtr);
		bgMultipleCopyWizardCtr = new BGMultipleCopyWizardController(ureq, getWindowControl(), this.currentGroup, this.flags);
		listenTo(bgMultipleCopyWizardCtr);
		removeAsListenerAndDispose(closeableModalController);
		closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), bgMultipleCopyWizardCtr.getInitialComponent());
		listenTo(closeableModalController);
		closeableModalController.activate();
	}

	private void doGroupEdit(UserRequest ureq) {
		removeAsListenerAndDispose(groupEditCtr);
		groupEditCtr = BGControllerFactory.getInstance().createEditControllerFor(ureq, getWindowControl(), this.currentGroup);
		listenTo(groupEditCtr);
		// add as listener to BusinessGroup so we are being notified about changes.
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), this.currentGroup);

		setMainContent(groupEditCtr.getInitialComponent());
		if (groupEditCtr.isLockAcquired()) {
			setTools(STATE_GROUP_EDIT);
		}
		// else don't change the tools state
	}

	private void doGroupRun(UserRequest ureq) {
		BGControllerFactory.getInstance().createRunControllerAsTopNavTab(this.currentGroup, ureq, getWindowControl(), true, null);
	}

	private void doGroupDelete() {
		// remove this controller as listener from the group
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.currentGroup);
		// now delete group and update table model
		groupManager.deleteBusinessGroup(this.currentGroup);
		if (groupListModel != null) {
			groupListModel.getObjects().remove(this.currentGroup);
			groupListCtr.modelChanged();
		}
		this.currentGroup = null;
	}

	private void doGroupList(UserRequest ureq, boolean initializeModel) {
		// Init table only once
		if (groupListCtr == null) {
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setTableEmptyMessage(translate("grouplist.no.groups"));
			// init group list filter controller
			
			removeAsListenerAndDispose(groupListCtr);
			groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), this.areaFilters, this.currentAreaFilter, 
					translate("grouplist.areafilter.title"), translate("grouplist.areafilter.nofilter"), getTranslator());
			listenTo(groupListCtr);
			groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.name", 0, CMD_GROUP_RUN, ureq.getLocale()));
			groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.desc", 1, null, ureq.getLocale()));
			groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_GROUP_EDIT, "grouplist.table.edit", translate(CMD_GROUP_EDIT)));
			if (flags.isEnabled(BGConfigFlags.GROUPS_DELETE)) {
				groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_GROUP_DELETE, "grouplist.table.delete", 
						translate(CMD_GROUP_DELETE)));
			}
			groupListVC.put("groupListTableCtr", groupListCtr.getInitialComponent());

		}
		if (groupListModel == null || initializeModel) {
			// 1. group list model: if area filter is set use only groups from given
			// area
			List groups;
			if (this.currentAreaFilter == null) {
				groups = contextManager.getGroupsOfBGContext(bgContext); // all groups
			} else {
				groups = areaManager.findBusinessGroupsOfArea(this.currentAreaFilter); // filtered
				// groups
			}
			groupListModel = new BusinessGroupTableModel(groups);
			groupListCtr.setTableDataModel(groupListModel);

			// 2. find areas for group list filter
			if (flags.isEnabled(BGConfigFlags.AREAS)) {
				this.areaFilters = areaManager.findBGAreasOfBGContext(bgContext);
				groupListCtr.setFilters(this.areaFilters, this.currentAreaFilter);
			}

		}
		setMainContent(groupListVC);
		setTools(STATE_GROUP_LIST);
	}

	private void doAreaEdit(UserRequest ureq) {
		removeAsListenerAndDispose(areaEditCtr);
		areaEditCtr = new BGAreaEditController(ureq, getWindowControl(), this.currentArea);
		listenTo(areaEditCtr);
		
		setMainContent(areaEditCtr.getInitialComponent());
		setTools(STATE_AREA_EDIT);
	}

	private void doAreaDelete() {
		areaManager.deleteBGArea(this.currentArea);
		if (areaListModel != null) {
			areaListModel.getObjects().remove(this.currentArea);
			areaListCtr.modelChanged();
		}
		this.currentArea = null;
	}

	private void doAreaList(UserRequest ureq, boolean initializeModel) {
		if (areaListModel == null || initializeModel) {
			List areas = areaManager.findBGAreasOfBGContext(bgContext);
			areaListModel = new BGAreaTableModel(areas, getTranslator());

			if (areaListCtr != null) areaListCtr.dispose();
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setTableEmptyMessage(translate("arealist.no.areas"));
			removeAsListenerAndDispose(areaListCtr);
			areaListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
			listenTo(areaListCtr);
			areaListCtr.addColumnDescriptor(new DefaultColumnDescriptor("arealist.table.name", 0, null, ureq.getLocale()));
			areaListCtr.addColumnDescriptor(new DefaultColumnDescriptor("arealist.table.desc", 1, null, ureq.getLocale()));
			areaListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_AREA_EDIT, "arealist.table.edit", translate(CMD_AREA_EDIT)));
			if (flags.isEnabled(BGConfigFlags.AREAS_DELETE)) {
				areaListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_AREA_DELETE, "arealist.table.delete", 
						translate(CMD_AREA_DELETE)));
			}
			areaListCtr.setTableDataModel(areaListModel);
			areaListVC.put("arealisttable", areaListCtr.getInitialComponent());
		}
		setMainContent(areaListVC);
		setTools(STATE_AREA_LIST);
	}

	private void doUsersList(UserRequest ureq, boolean initializeModel) {
		// 1. init owners list
		if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
			if (ownerListModel == null || initializeModel) {
				List owners = contextManager.getBGOwnersOfBGContext(bgContext);
	
				TableGuiConfiguration tableConfig = new TableGuiConfiguration();
				tableConfig.setPreferencesOffered(true, "ownerListController");
				tableConfig.setTableEmptyMessage(translate("userlist.owners.noOwners"));
				
				removeAsListenerAndDispose(ownerListCtr);
				ownerListCtr = UserControllerFactory.createTableControllerFor(tableConfig, owners, ureq, getWindowControl(), CMD_USER_DETAILS);
				listenTo(ownerListCtr);
				
				ownerListModel = (UserTableDataModel) ownerListCtr.getTableDataModel();

				userListVC.put("ownerlisttable", ownerListCtr.getInitialComponent());
			}
			userListVC.contextPut("showOwners", Boolean.TRUE);
		} else {
			userListVC.contextPut("showOwners", Boolean.FALSE);
		}

		// 2. init participants list
		if (participantListModel == null || initializeModel) {
			List participants = contextManager.getBGParticipantsOfBGContext(bgContext);

			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setPreferencesOffered(true, "participantsListController");
			tableConfig.setTableEmptyMessage(translate("userlist.participants.noParticipants"));
			
			removeAsListenerAndDispose(participantListCtr);
			participantListCtr = UserControllerFactory.createTableControllerFor(tableConfig, participants, ureq, getWindowControl(), CMD_USER_DETAILS);
			listenTo(participantListCtr);

			participantListModel = (UserTableDataModel) participantListCtr.getTableDataModel();

			userListVC.put("participantlisttable", participantListCtr.getInitialComponent());
		}
		// 3. set content
		setMainContent(userListVC);
		setTools(STATE_USER_LIST);
	}

	private void doUserDetails(UserRequest ureq) {
		// 1. expose the identity details
		userDetailsVC.contextPut("identity", this.currentIdentity);
		Translator babel = UserManager.getInstance().getPropertyHandlerTranslator(userTrans);
		
		HomePageConfig homePageConfig = HomePageConfigManagerImpl.getInstance().loadConfigFor(currentIdentity.getName());
		removeAsListenerAndDispose(homePageDisplayController);
		homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), homePageConfig);
		listenTo(homePageDisplayController);
		userDetailsVC.put("userdetailsform", homePageDisplayController.getInitialComponent());
		// 2. expose the owner groups of the identity
		if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
			List ownerGroups = groupManager.findBusinessGroupsOwnedBy(bgContext.getGroupType(), this.currentIdentity, bgContext);
			
			Link[] ownerGroupLinks= new Link[ownerGroups.size()];
			int ownerNumber = 0;
			
			for (Iterator iter = ownerGroups.iterator(); iter.hasNext();) {
				BusinessGroup group = (BusinessGroup) iter.next();
				Link tmp = LinkFactory.createCustomLink("cmd.user.remove.group.own." + group.getKey(), "cmd.user.remove.group.own." + group.getKey(), "userdetails.remove", Link.BUTTON_SMALL, userDetailsVC, this);
				tmp.setUserObject(group);
				ownerGroupLinks[ownerNumber] = tmp;
				ownerNumber++;
			}
			userDetailsVC.contextPut("ownerGroupLinks", ownerGroupLinks);
			userDetailsVC.contextPut("noOwnerGroups", (ownerGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
			userDetailsVC.contextPut("showOwnerGroups", Boolean.TRUE);
		} else {
			userDetailsVC.contextPut("showOwnerGroups", Boolean.FALSE);
		}
		// 3. expose the participant groups of the identity
		List participantGroups = groupManager.findBusinessGroupsAttendedBy(bgContext.getGroupType(), this.currentIdentity, bgContext);
		
		Link[] participantGroupLinks= new Link[participantGroups.size()];
		int participantNumber = 0;
		
		for (Iterator iter = participantGroups.iterator(); iter.hasNext();) {
			BusinessGroup group = (BusinessGroup) iter.next();
			Link tmp = LinkFactory.createCustomLink("cmd.user.remove.group.part." + group.getKey(), "cmd.user.remove.group.part." + group.getKey(), "userdetails.remove", Link.BUTTON_SMALL, userDetailsVC, this);
			tmp.setUserObject(group);
			participantGroupLinks[participantNumber] = tmp;
			participantNumber++;
		}
		userDetailsVC.contextPut("noParticipantGroups", (participantGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
		userDetailsVC.contextPut("participantGroupLinks", participantGroupLinks);
		// 4. set content
		setMainContent(userDetailsVC);
		setTools(STATE_USER_DETAILS);
	}

	private void doRemoveUserFromParticipatingGroup(Identity ureqIdentity, Identity toRemoveIdentity, String groupKey) {
		Long key = Long.valueOf(groupKey);
		BusinessGroup group = groupManager.loadBusinessGroup(key, true);
		List<Identity> identities = new ArrayList<Identity>(1);
		identities.add(toRemoveIdentity);
		groupManager.removeParticipantsAndFireEvent(ureqIdentity, identities, group, flags);
	}

	private void doRemoveUserFromOwnedGroup(UserRequest ureq, String groupKey) {
		Long key = Long.valueOf(groupKey);
		BusinessGroup group = groupManager.loadBusinessGroup(key, true);
		groupManager.removeOwnerAndFireEvent(ureq.getIdentity(), currentIdentity, group, flags, false);
	}
	
	/**
	 * generates the email adress list.
	 * @param ureq
	 * @return a contact form controller for this group
	 */
	private ContactFormController createContactFormController(UserRequest ureq) {
		BaseSecurity scrtMngr = BaseSecurityManager.getInstance();

		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		// two named ContactLists, the new way using the contact form
		// the same name as in the checkboxes are taken as contactlist names
		ContactList ownerCntctLst = new ContactList(businessGroupTranslator.translate("sendtochooser.form.chckbx.owners"));
		ContactList partipCntctLst = new ContactList(businessGroupTranslator.translate("sendtochooser.form.chckbx.partip"));
		ContactList waitingListContactList = new ContactList(businessGroupTranslator.translate("sendtochooser.form.chckbx.waitingList"));
		if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
			if (sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL)) {
				SecurityGroup owners = this.currentGroup.getOwnerGroup();
				List<Identity> ownerList = scrtMngr.getIdentitiesOfSecurityGroup(owners);
				ownerCntctLst.addAllIdentites(ownerList);
				cmsg.addEmailTo(ownerCntctLst);
			} else {
				if (sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE)) {
					SecurityGroup owners = this.currentGroup.getOwnerGroup();
					List<Identity> ownerList = scrtMngr.getIdentitiesOfSecurityGroup(owners);
					for (Identity identity : new ArrayList<Identity>(ownerList)) {
						boolean keyIsSelected = false;
						for (Long key : sendToChooserForm.getSelectedOwnerKeys()) {
							if (key.equals(identity.getKey())) {
								keyIsSelected = true;
								break;
							}
						}
						if (!keyIsSelected) {
							ownerList.remove(identity);
						}
					}
					ownerCntctLst.addAllIdentites(ownerList);
					cmsg.addEmailTo(ownerCntctLst);
				}
			}
		}
		if (sendToChooserForm != null) {
			if  (sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL)) {
				SecurityGroup participants = this.currentGroup.getPartipiciantGroup();
				List<Identity> participantsList = scrtMngr.getIdentitiesOfSecurityGroup(participants);
				partipCntctLst.addAllIdentites(participantsList);
				cmsg.addEmailTo(partipCntctLst);
			} else {
				if (sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE)) {
					SecurityGroup participants = this.currentGroup.getPartipiciantGroup();
					List<Identity> participantsList = scrtMngr.getIdentitiesOfSecurityGroup(participants);
					for (Identity identity : new ArrayList<Identity>(participantsList)) {
						boolean keyIsSelected = false;
						for (Long key : sendToChooserForm.getSelectedPartipKeys()) {
							if (key.equals(identity.getKey())) {
								keyIsSelected = true;
								break;
							}
						}
						if (!keyIsSelected) {
							participantsList.remove(identity);
						}
					}
					partipCntctLst.addAllIdentites(participantsList);
					cmsg.addEmailTo(partipCntctLst);
				}
			}
			
		}
		if (sendToChooserForm != null && getIsGMAdminOwner(ureq) && this.currentGroup.getWaitingListEnabled().booleanValue()) {
			if (sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL)) {
				SecurityGroup waitingList = this.currentGroup.getWaitingGroup();
				List<Identity> waitingListIdentities = scrtMngr.getIdentitiesOfSecurityGroup(waitingList);
				waitingListContactList.addAllIdentites(waitingListIdentities);
				cmsg.addEmailTo(waitingListContactList);
			} else {
				if (sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE)) {
					SecurityGroup waitingList = this.currentGroup.getWaitingGroup();
					List<Identity> waitingListIdentities = scrtMngr.getIdentitiesOfSecurityGroup(waitingList);
					for (Identity identity : new ArrayList<Identity>(waitingListIdentities)) {
						boolean keyIsSelected = false;
						for (Long key : sendToChooserForm.getSelectedWaitingKeys()) {
							if (key.equals(identity.getKey())) {
								keyIsSelected = true;
								break;
							}
						}
						if (!keyIsSelected) {
							waitingListIdentities.remove(identity);
						}
					}
					waitingListContactList.addAllIdentites(waitingListIdentities);
					cmsg.addEmailTo(waitingListContactList);
				}
			}
		}
		String resourceUrl = JumpInManager.getJumpInUri(this.getWindowControl().getBusinessControl());
		cmsg.setSubject( businessGroupTranslator.translate("businessgroup.contact.subject", new String[]{ this.currentGroup.getName()} ) );
		cmsg.setBodyText( businessGroupTranslator.translate("businessgroup.contact.bodytext", new String[]{ this.currentGroup.getName(), resourceUrl} ) );
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(this.currentGroup);
		ContactFormController cofocntrllr = collabTools.createContactFormController(ureq, getWindowControl(), cmsg);
		return cofocntrllr;
	}

	/**
	 * Use the flags to configure the runtime behaviour of this controller
	 * 
	 * @return the configuration flags
	 */
	public BGConfigFlags getControllerFlags() {
		return flags;
	}

	private void setMainContent(Component component) {
		content.setContent(component);
		this.backComponent = this.currentComponent;
		this.currentComponent = component;
	}

	private void doBack() {
		content.setContent(this.backComponent);
		this.currentComponent = this.backComponent;
	}

	@Override
	protected void doDispose() {
	
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.bgContext);

		releaseAdminLockAndGroupMUE();
	}

	/**
	 * add every Admin child controller which must be disposed. So that all locks
	 * on (OLAT)resources are free-ed up on leaving an admin gui area.
	 */
	private void releaseAdminLockAndGroupMUE() {
		// deregister for group change events
		if (this.currentGroup != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.currentGroup);
		}
		// this is done by simply disposing the businessgroup managing controller
		removeAsListenerAndDispose(groupEditCtr);
	}

	/**
	 * only for disposedBGAmanagementController!
	 * @param ureq
	 */
	void fireDoneEvent(UserRequest ureq){
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
}