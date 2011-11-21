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

package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.control.state.ControllerState;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.tree.TreeHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.GroupLoggingAction;
import org.olat.group.delete.TabbedPaneController;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.BGTranslatorFactory;
import org.olat.group.ui.BusinessGroupFormController;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.util.logging.activity.LoggingResourceable;

import de.bps.olat.util.notifications.SubscriptionProvider;
import de.bps.olat.util.notifications.SubscriptionProviderImpl;

/**
 * Description: <br>
 * Controller to list all groups where the user is owner or participant. This
 * controller does also feature create and delete methods for groups of type
 * buddyGroup <br>
 * 
 * <pre>
 *  Possible activation messages:
 *  &quot;cmd.menu.index&quot; : show groups overview
 *  &quot;cmd.menu.buddy&quot; : show all buddy groups 
 *  &quot;cmd.menu.learn&quot; : show all leanringgroups 
 *  &quot;cmd.menu.right&quot; : show all right groups
 *  &quot;addBuddyGroup&quot; : start add group workflow
 * </pre>
 * 
 * <P>
 * Initial Date: Aug 5, 2004
 * 
 * @author patrick
 */

public class BGMainController extends MainLayoutBasicController implements Activateable {
	private static final String PACKAGE = Util.getPackageName(BGMainController.class);
	/*
	 * things a controller needs during its lifetime
	 */
	private VelocityContainer main;
	private LayoutMain3ColsController columnLayoutCtr;
	private ToolController mainToolC;
	private MenuTree menuTree;

	private static final String ACTION_ADD_BUDDYGROUP = "addBuddyGroup";
	private static final String ACTION_DELETE_UNUSEDGROUP = "deleteunusedgroup";

	private TableController groupListCtr;
	private BusinessGroupTableModelWithType groupListModel;
	private BusinessGroupFormController createBuddyGroupController;
	private BusinessGroup currBusinessGroup;
	private Identity identity;
	private BusinessGroupManager bgm;
	private TabbedPaneController deleteTabPaneCtr;
	private CloseableModalController cmc;
	private DialogBoxController deleteDialogBox;
	private DialogBoxController leaveDialogBox;

	// group list table rows
	private static final String TABLE_ACTION_LEAVE = "bgTblLeave";
	private static final String TABLE_ACTION_DELETE = "bgTblDelete";
	private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	private static final String CMD_MENU_INDEX = "cmd.menu.index";
	private static final String CMD_MENU_BUDDY = "cmd.menu.buddy";
	private static final String CMD_MENU_LEARN = "cmd.menu.learn";
	private static final String CMD_MENU_RIGHT = "cmd.menu.right";

	/**
	 * @param ureq
	 * @param wControl
	 * @param flags configuration flags
	 * @param initialViewIdentifier
	 */
	public BGMainController(UserRequest ureq, WindowControl wControl, String initialViewIdentifier) {
		super(ureq, wControl);

		identity = ureq.getIdentity();
		setTranslator(BGTranslatorFactory.createBGPackageTranslator(PACKAGE, BusinessGroup.TYPE_BUDDYGROUP, ureq.getLocale()));
		bgm = BusinessGroupManagerImpl.getInstance();

		// main component layed out in panel
		main = createVelocityContainer("index");
		// toolboxes
		mainToolC = ToolFactory.createToolController(getWindowControl());
		listenTo(mainToolC);
		mainToolC.addHeader(translate("tools.add.header"));
		mainToolC.addLink(ACTION_ADD_BUDDYGROUP, translate("tools.add.buddygroup"));
		if (ureq.getUserSession().getRoles().isOLATAdmin()) {
			mainToolC.addHeader(translate("tools.delete.header"));
			mainToolC.addLink(ACTION_DELETE_UNUSEDGROUP, translate("tools.delete.unusedgroup"));
		}
		// menu
		menuTree = new MenuTree("buddyGroupTree");
		menuTree.setTreeModel( buildTreeModel() );
		menuTree.setSelectedNodeId(menuTree.getTreeModel().getRootNode().getIdent());
		menuTree.addListener(this);
		// layout
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainToolC.getInitialComponent(), main, "groumain");
		columnLayoutCtr.addCssClassToMain("o_groups");
		
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());

		// start with list of all groups
		doAllGroupList(ureq, getWindowControl());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent te = (TreeEvent) event;
				TreeNode clickedNode = menuTree.getTreeModel().getNodeById(te.getNodeId());
				Object userObject = clickedNode.getUserObject();
				activateContent(ureq, userObject);
			}
		}
	}

	/**
	 * Activate the content in the content area based on a user object
	 * representing the identifyer of the content
	 * 
	 * @param ureq
	 * @param userObject
	 */
	private void activateContent(UserRequest ureq, Object userObject) {
		if (userObject.equals(CMD_MENU_INDEX)) {
			doAllGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_BUDDY)) {
			doBuddyGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_LEARN)) {
			doLearningGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_RIGHT)) {
			doRightGroupList(ureq, getWindowControl());
		}
		setState(userObject.toString());
	}
	
	@Override
	protected void adjustState(ControllerState cstate, UserRequest ureq) {
		String cmd = cstate.getSerializedState();
		activateContent(ureq, cmd);
		// adjust the menu
		TreeNode rootNode = this.menuTree.getTreeModel().getRootNode();
		TreeNode activatedNode = TreeHelper.findNodeByUserObject(cmd, rootNode);
		this.menuTree.setSelectedNode(activatedNode);
	}

	/**
	 * @param ureq
	 * @param event
	 */
	private void handleEventsGroupTables(UserRequest ureq, Event event) {
		if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
			TableEvent te = (TableEvent) event;
			String actionid = te.getActionId();
			int rowid = te.getRowId();
			currBusinessGroup = groupListModel.getBusinessGroupAt(rowid);
			String trnslP = currBusinessGroup.getName();

			if (actionid.equals(TABLE_ACTION_LAUNCH)) {
				BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
			} else if (actionid.equals(TABLE_ACTION_DELETE) && currBusinessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP)) {
				// only for buddygroups allowed
				deleteDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.delete.text", trnslP), deleteDialogBox);
			} else if (actionid.equals(TABLE_ACTION_LEAVE) && currBusinessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP)) {
				// only for buddygroups allowed
				leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", trnslP), leaveDialogBox);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupListCtr) {
			// an action from the groupList was clicked
			// e.g. LEAVE, DELETE, LAUNCH
			handleEventsGroupTables(ureq, event);
		} else if (source == mainToolC) {
			if (event.getCommand().startsWith(ACTION_ADD_BUDDYGROUP)) {
				initAddBuddygroupWorkflow(ureq);
			} else if (event.getCommand().startsWith(ACTION_DELETE_UNUSEDGROUP)) {
				initDeleteGroupWorkflow(ureq);
			}
		} else if (source == deleteDialogBox) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doBuddyGroupDelete(ureq);
			}//else cancel was clicked or box closed
		} else if (source == leaveDialogBox) {
			if (event != Event.CANCELLED_EVENT) {
				if (DialogBoxUIFactory.isYesEvent(event)) {
					doBuddyGroupLeave(ureq);
				}
			}// else dialog was simply closed
		} else if (source == this.createBuddyGroupController) {
			this.cmc.deactivate(); // remove modal dialog
			removeAsListenerAndDispose(this.cmc);
			if (event == Event.DONE_EVENT) {
				// create new buddy group with the specified values
				// values are taken from the createBuddyGroupForm
				this.currBusinessGroup = createBuddyGroup(ureq);
				updateGroupListModelAll();

				// after successfully creating a buddygroup 'launch' it
				BusinessGroupMainRunController groupRunCtr = BGControllerFactory.getInstance().createRunControllerAsTopNavTab(this.currBusinessGroup,
						ureq, getWindowControl(), false, null);
				if (groupRunCtr != null) groupRunCtr.activateAdministrationMode(ureq);
			} else if (event == Event.FAILED_EVENT) {
				this.cmc = new CloseableModalController(getWindowControl(), translate("close"), this.createBuddyGroupController.getInitialComponent(), true, translate("create.form.title"));
				this.cmc.activate();
				listenTo(this.cmc);
			} else if (event == Event.CANCELLED_EVENT) {
				// notthing to do
			}
		}
	}

	private void initDeleteGroupWorkflow(UserRequest ureq) {
		removeAsListenerAndDispose(deleteTabPaneCtr);
		deleteTabPaneCtr = new TabbedPaneController(ureq, getWindowControl());
		listenTo(deleteTabPaneCtr);
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/delete.html");
		main.put("deleteTabs", deleteTabPaneCtr.getInitialComponent());		
	}

	/**
	 * 
	 */
	private void initAddBuddygroupWorkflow(UserRequest ureq) {
		BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
		
		if (this.createBuddyGroupController != null) {
			removeAsListenerAndDispose(this.createBuddyGroupController);
		}
		this.createBuddyGroupController = new BusinessGroupFormController(ureq, getWindowControl(), null, flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE));
		listenTo(this.createBuddyGroupController);
		this.cmc = new CloseableModalController(getWindowControl(), translate("close"), this.createBuddyGroupController.getInitialComponent(), true, translate("create.form.title"));
		this.cmc.activate();
		listenTo(this.cmc);
	}

	/**
	 * deletes this.currBusinessGroup. Checks if user is in owner group,
	 * otherwhise does nothing
	 * 
	 * @param ureq
	 */
	private void doBuddyGroupDelete(UserRequest ureq) {
		// 1) send notification mails to users
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		ContactList owners = new ContactList(translate("userlist.owners.title"));
		List ow = securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getOwnerGroup());
		owners.addAllIdentites(ow);
		ContactList participants = new ContactList(translate("userlist.participants.title"));
		participants.addAllIdentites(securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getPartipiciantGroup()));
		// check if user is in owner group (could fake link in table)
		if (!PersistenceHelper.listContainsObjectByKey(ow, ureq.getIdentity())) {
			Tracing.logWarn("User tried to delete a group but he was not owner of the group", null, BGMainController.class);
			return;
		}

		List everybody = new ArrayList();
		everybody.add(owners);
		everybody.add(participants);
		// inform Indexer about change
		// 3) delete the group
		currBusinessGroup = bgm.loadBusinessGroup(currBusinessGroup);
		
		//change state of publisher so that notifications of deleted group calendars make no problems
		CalendarManager calMan = CalendarManagerFactory.getInstance().getCalendarManager();
		NotificationsManager nfm = NotificationsManager.getInstance();
		KalendarRenderWrapper calRenderWrapper = calMan.getGroupCalendar(currBusinessGroup);
		SubscriptionProvider subProvider = new SubscriptionProviderImpl(calRenderWrapper);
		SubscriptionContext subsContext = subProvider.getSubscriptionContext();
		Publisher pub = nfm.getPublisher(subsContext);
		if (pub != null) {
			pub.setState(1); //int 0 is OK -> all other is not OK
		}
		
		bgm.deleteBusinessGroupWithMail(currBusinessGroup, getWindowControl(), ureq, getTranslator(), everybody);
		// do Logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), LoggingResourceable.wrap(currBusinessGroup));
		// 4) update Tables
		doAllGroupList(ureq, getWindowControl());
		
		showInfo("info.group.deleted");
	}

	/**
	 * removes user from this.currBusinessGroup's owner and participant group. If
	 * no other owner are found the user won't be removed from the owner group
	 * 
	 * @param ureq
	 */
	private void doBuddyGroupLeave(UserRequest ureq) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
		// 1) remove as owner
		SecurityGroup owners = currBusinessGroup.getOwnerGroup();
		if (securityManager.isIdentityInSecurityGroup(identity, owners)) {
			List ownerList = securityManager.getIdentitiesOfSecurityGroup(owners);
			if (ownerList.size() > 1) {
				bgm.removeOwnerAndFireEvent(ureq.getIdentity(), ureq.getIdentity(), currBusinessGroup, flags, false);
				// update model
				updateGroupListModelAll();
			} else {
				// he is the last owner, but there must be at least one oner
				// give him a warning, as long as he tries to leave, he gets
				// this warning.
				getWindowControl().setError(translate("msg.atleastone"));
				return;
			}
		}
		// if identity was also owner it must have succesfully removed to end here.
		// now remove the identity also as pariticipant.
		// 2) remove as participant
		List<Identity> identities = new ArrayList<Identity>(1);
		identities.add(ureq.getIdentity());
		bgm.removeParticipantsAndFireEvent(ureq.getIdentity(), identities, currBusinessGroup, flags);
		
		// update Tables
		doAllGroupList(ureq, getWindowControl());
	}

	/**
	 * Creates a new business group of type buddy group and adds this.identity as
	 * owner to the new group.
	 * 
	 * @return BusinessGroup
	 */
	private BusinessGroup createBuddyGroup(UserRequest ureq) {
		String bgName = this.createBuddyGroupController.getGroupName();
		String bgDesc = this.createBuddyGroupController.getGroupDescription();
		Integer bgMin = this.createBuddyGroupController.getGroupMin();
		Integer bgMax = this.createBuddyGroupController.getGroupMax();
		/*
		 * this creates a BusinessGroup as BuddyGroup with the specified name and
		 * description and also the CollaborationTools are enabled during creation.
		 * The GroupContext is null in the case of BuddyGroups.
		 */
		BusinessGroup newGroup = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, identity, bgName, bgDesc, bgMin, bgMax, null, null, null);
		// create buddylist for group
		// 2. Add user to group, fire events, do loggin etc.
		BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
		// do Logging
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(newGroup));
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CREATED, getClass());
		bgm.addOwnerAndFireEvent(ureq.getIdentity(), ureq.getIdentity(), newGroup, flags, true);
		return newGroup;
	}

	/**
	 * Prepare everything and show all groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doAllGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(true, ureq);
		// 2) load data into model
		updateGroupListModelAll();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/index.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(false);
	}

	/**
	 * Prepare everything and show all buddy groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doBuddyGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(true, ureq);
		// 2) load data into model
		updateGroupListModelBuddygroups();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/buddy.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(false);
	}

	/**
	 * Prepare everything and show all learning groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doLearningGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(false, ureq);
		// 2) load data into model
		updateGroupListModelLearninggroups();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/learning.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(true);
	}

	/**
	 * Prepare everything and show all right groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doRightGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(false, ureq);
		// 2) load data into model
		updateGroupListModelRightgroups();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/right.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(true);
	}

	/**
	 * Prepare everything and show delete groups workflow
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doDeleteGroups(UserRequest ureq, WindowControl wControl) {
	}

	/**
	 * Initialize the group list controller and the group list model given.
	 * 
	 * @param withLeaveAndDelete config flag: true: leave and delete button are
	 *          showed, false: not showed
	 * @param ureq
	 */
	private void initGroupListCtrAndModel(boolean withLeaveAndDelete, UserRequest ureq) {
		// 1) init listing controller
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("index.table.nogroup"));
		removeAsListenerAndDispose(groupListCtr);
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(groupListCtr);
		
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.bgname", 0, TABLE_ACTION_LAUNCH, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.description", 1, null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.type", 2, null, getLocale()));
		if (withLeaveAndDelete) {
			groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.leave", 3, TABLE_ACTION_LEAVE, 
					translate("table.header.leave"), null));
			groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.delete", 4, TABLE_ACTION_DELETE, 
					translate("table.header.delete"), null));
		}
		// 2) init list model
		groupListModel = new BusinessGroupTableModelWithType(new ArrayList(), getTranslator());
		groupListCtr.setTableDataModel(groupListModel);
		main.put("groupList", groupListCtr.getInitialComponent());
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for all groups
	 */
	private void updateGroupListModelAll() {
		List wrapped = new ArrayList();
		// buddy groups
		List groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, Boolean.TRUE, Boolean.TRUE));
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
		iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, Boolean.TRUE, null));
		}
		// learning groups
		groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, null, null));
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, null, null));
		}
		// right groups
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
		iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, null, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for buddy groups
	 */
	private void updateGroupListModelBuddygroups() {
		List wrapped = new ArrayList();
		// buddy groups
		List groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, Boolean.TRUE, Boolean.TRUE));
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
		iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, Boolean.TRUE, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for learning groups
	 */
	private void updateGroupListModelLearninggroups() {
		List wrapped = new ArrayList();
		// learning groups
		List groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, null, null));
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, null, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for right groups
	 */
	private void updateGroupListModelRightgroups() {
		List wrapped = new ArrayList();
		// buddy groups
		// right groups
		List groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			wrapped.add(wrapGroup(group, null, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}

	/**
	 * Wrapps a group and some data into an object[] that can be displayed by the
	 * group list model
	 * 
	 * @param group
	 * @param allowLeave true: user can leave
	 * @param allowDelete true: user can delete
	 * @return Object[]
	 */
	private Object[] wrapGroup(BusinessGroup group, Boolean allowLeave, Boolean allowDelete) {
		return new Object[] { group, allowLeave, allowDelete };
	}

	/**
	 * @return TreeModel
	 */
	private TreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();

		GenericTreeNode rootNode = new GenericTreeNode();
		rootNode.setTitle(translate("menu.index"));
		rootNode.setUserObject(CMD_MENU_INDEX);
		rootNode.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(rootNode);

		GenericTreeNode myEntriesTn = new GenericTreeNode();
		myEntriesTn.setTitle(translate("menu.buddygroups"));
		myEntriesTn.setUserObject(CMD_MENU_BUDDY);
		myEntriesTn.setAltText(translate("menu.buddygroups.alt"));
		rootNode.addChild(myEntriesTn);

		myEntriesTn = new GenericTreeNode();
		myEntriesTn.setTitle(translate("menu.learninggroups"));
		myEntriesTn.setUserObject(CMD_MENU_LEARN);
		myEntriesTn.setAltText(translate("menu.learninggroups.alt"));
		rootNode.addChild(myEntriesTn);

		myEntriesTn = new GenericTreeNode();
		myEntriesTn.setTitle(translate("menu.rightgroups"));
		myEntriesTn.setUserObject(CMD_MENU_RIGHT);
		myEntriesTn.setAltText(translate("menu.rightgroups.alt"));
		rootNode.addChild(myEntriesTn);

		return gtm;
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest,
	 *      java.lang.String)
	 */
	public void activate(UserRequest ureq, String viewIdentifier) {
		// find the menu node that has the user object that represents the
		// viewIdentifyer
		GenericTreeNode rootNode = (GenericTreeNode) this.menuTree.getTreeModel().getRootNode();
		TreeNode activatedNode = TreeHelper.findNodeByUserObject(viewIdentifier, rootNode);
		if (activatedNode != null) {
			this.menuTree.setSelectedNodeId(activatedNode.getIdent());
			activateContent(ureq, activatedNode.getUserObject());
		} else {
			// not found, activate the root node
			this.menuTree.setSelectedNodeId(rootNode.getIdent());
			activateContent(ureq, rootNode.getUserObject());
			// cehck for toolbox activation points
			if (viewIdentifier.equals(ACTION_ADD_BUDDYGROUP)) {
				initAddBuddygroupWorkflow(ureq);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}