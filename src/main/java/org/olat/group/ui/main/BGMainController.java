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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.GroupLoggingAction;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.delete.TabbedPaneController;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.BGTranslatorFactory;
import org.olat.group.ui.BusinessGroupFormController;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACUIFactory;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.model.BusinessGroupAccess;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.AccessEvent;
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

public class BGMainController extends MainLayoutBasicController implements Activateable, Activateable2 {
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
	private final Identity identity;
	private final BusinessGroupManager bgm;
	//fxdiff VCRP-1,2: access control of resources
	private final ACFrontendManager acFrontendManager;
	private final BGContextManager contextManager;
	private TabbedPaneController deleteTabPaneCtr;
	private CloseableModalController cmc;
	private DialogBoxController deleteDialogBox;
	private DialogBoxController sendEMailOnDeleteDialogBox;
	private DialogBoxController leaveDialogBox;
	//fxdiff VCRP-1,2: access control of resources
	private Controller accessController;
	private Link allOpenLink;
	private Link searchOpenLink;
	private SegmentViewComponent segmentView;
	private BGSearchController searchController;

	// group list table rows
	private static final String TABLE_ACTION_LEAVE = "bgTblLeave";
	private static final String TABLE_ACTION_DELETE = "bgTblDelete";
	private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	//fxdiff VCRP-1,2: access control of resources
	private static final String TABLE_ACTION_ACCESS = "bgTblAccess";
	private static final String CMD_MENU_INDEX = "cmd.menu.index";
	private static final String CMD_MENU_BUDDY = "cmd.menu.buddy";
	private static final String CMD_MENU_LEARN = "cmd.menu.learn";
	private static final String CMD_MENU_RIGHT = "cmd.menu.right";
	//fxdiff VCRP-1,2: access control of resources
	private static final String CMD_MENU_OPEN = "cmd.menu.open";
	private static final String CMD_MENU_OPEN_SEARCH = "cmd.menu.open.search";

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
		contextManager = BGContextManagerImpl.getInstance();
		//fxdiff VCRP-1,2: access control of resources
		acFrontendManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");

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
		//fxdiff VCRP-1,2: access control of resources
		searchController = new BGSearchController(ureq, getWindowControl(), ureq.getUserSession().getRoles().isOLATAdmin());
		listenTo(searchController);
		
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
		} else if (source instanceof Link && source.getComponentName().startsWith("repo_entry_")) {
			Long repoEntryKey = (Long)((Link)source).getUserObject();
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString("[RepositoryEntry:" + repoEntryKey + "]");
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		//fxdiff VCRP-1,2: access control of resources
		} else if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = main.getComponent(segmentCName);
				if (clickedLink == allOpenLink) {
					doOpenGroupList(ureq, getWindowControl());
				} else if (clickedLink == searchOpenLink){
					doSearchOpenGroupList(ureq, getWindowControl());
				}
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
		//fxdiff VCRP-1,2: access control of resources
		if(this.accessController != null) {
			main.remove(accessController.getInitialComponent());
			removeAsListenerAndDispose(accessController);
			accessController = null;
		}
		
		if (userObject.equals(CMD_MENU_INDEX)) {
			doAllGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_BUDDY)) {
			doBuddyGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_LEARN)) {
			doLearningGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_RIGHT)) {
			doRightGroupList(ureq, getWindowControl());
		//fxdiff VCRP-1,2: access control of resources
		} else if (userObject.equals(CMD_MENU_OPEN)) {
			doOpenGroupList(ureq, getWindowControl());
		} else if (userObject.equals(CMD_MENU_OPEN_SEARCH)) {
			doSearchOpenGroupList(ureq, getWindowControl());
		}
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
				//fxdiff BAKS-7 Resume function
				Controller ctrl = BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
				if(ctrl != null) {
					addToHistory(ureq, ctrl);
				}
			} else if (actionid.equals(TABLE_ACTION_DELETE) && currBusinessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP)) {
				// only for buddygroups allowed
				deleteDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.delete.text", trnslP), deleteDialogBox);
			} else if (actionid.equals(TABLE_ACTION_LEAVE) && currBusinessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP)) {
				// only for buddygroups allowed
				leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", trnslP), leaveDialogBox);
			//fxdiff VCRP-1,2: access control of resources
			} else if (actionid.equals(TABLE_ACTION_ACCESS)) {
				handleAccess(ureq);
			}
		}
	}
	//fxdiff VCRP-1,2: access control of resources
	private void handleAccess(UserRequest ureq) {
		if(bgm.isIdentityInBusinessGroup(getIdentity(), currBusinessGroup)) {
			BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
			return;
		}

		AccessResult acResult = acFrontendManager.isAccessible(currBusinessGroup, getIdentity(), true);
		if(acResult.isAccessible()) {
			BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
		} else if (currBusinessGroup != null && acResult.getAvailableMethods().size() > 0) {
			accessController = ACUIFactory.createAccessController(ureq, getWindowControl(), acResult.getAvailableMethods());
			listenTo(accessController);
			main.put("access", accessController.getInitialComponent());
			main.setDirty(true);
		} else {
			showError("No access");
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
				sendEMailOnDeleteDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.mail.text"), sendEMailOnDeleteDialogBox);
			}//else cancel was clicked or box closed
		} else if(source == sendEMailOnDeleteDialogBox){
			if(DialogBoxUIFactory.isOkEvent(event))
				doBuddyGroupDelete(ureq,true);
			else
				doBuddyGroupDelete(ureq,false);
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
		//fxdiff VCRP-1,2: access control of resources
		} else if (source == accessController) {
			if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
				activateContent(ureq, CMD_MENU_OPEN);
				BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
			} else if(event.equals(AccessEvent.ACCESS_FAILED_EVENT)) {
				String msg = ((AccessEvent)event).getMessage();
				if(StringHelper.containsNonWhitespace(msg)) {
					getWindowControl().setError(msg);
				} else {
					showError("error.accesscontrol");
				}
			}
		} else if (source == searchController) {
			if(event.equals(Event.DONE_EVENT)) {
				updateSearchGroupList();
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
	 * @param doSendMail specifies if notification mails should be sent to users of delted group
	 */
	private void doBuddyGroupDelete(UserRequest ureq, boolean doSendMail) {
		// 1) send notification mails to users
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		ContactList owners = new ContactList(translate("userlist.owners.title"));
		List<Identity> ow = securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getOwnerGroup());
		owners.addAllIdentites(ow);
		ContactList participants = new ContactList(translate("userlist.participants.title"));
		participants.addAllIdentites(securityManager.getIdentitiesOfSecurityGroup(currBusinessGroup.getPartipiciantGroup()));
		// check if user is in owner group (could fake link in table)
		if (!PersistenceHelper.listContainsObjectByKey(ow, ureq.getIdentity())) {
			Tracing.logWarn("User tried to delete a group but he was not owner of the group", null, BGMainController.class);
			return;
		}

		List<ContactList> everybody = new ArrayList<ContactList>();
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
		
		// fxdiff: FXOLAT-138
		if (doSendMail) {
			bgm.deleteBusinessGroupWithMail(currBusinessGroup, getWindowControl(), ureq, getTranslator(), everybody);
		} else {
			bgm.deleteBusinessGroup(currBusinessGroup);
		}
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
		initGroupListCtrAndModel(true, true, false, CMD_MENU_INDEX, ureq);
		// 2) load data into model
		updateGroupListModelAll();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/index.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(false);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_MENU_INDEX, 0l);
		addToHistory(ureq, ores, null, wControl, true);
	}

	/**
	 * Prepare everything and show all buddy groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doBuddyGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(true, false, false, CMD_MENU_BUDDY, ureq);
		// 2) load data into model
		updateGroupListModelBuddygroups();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/buddy.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(false);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_MENU_BUDDY, 0l);
		addToHistory(ureq, ores, null, wControl, true);
	}

	/**
	 * Prepare everything and show all learning groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doLearningGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(false, true, false, CMD_MENU_LEARN, ureq);
		// 2) load data into model
		updateGroupListModelLearninggroups();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/learning.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(true);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_MENU_LEARN, 0l);
		addToHistory(ureq, ores, null, wControl, true);
	}

	/**
	 * Prepare everything and show all right groups
	 * 
	 * @param ureq
	 * @param wControl
	 */
	private void doRightGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(false, true, false, CMD_MENU_RIGHT, ureq);
		// 2) load data into model
		updateGroupListModelRightgroups();
		// 3) set correct page
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/right.html");
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(true);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_MENU_RIGHT, 0l);
		addToHistory(ureq, ores, null, wControl, true);
	}
	//fxdiff VCRP-1,2: access control of resources
	private void doOpenGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(false, false, true, CMD_MENU_OPEN, ureq);
		// 2) load data into model
		updateOpenGroupList();
		// 3) set correct page and segmented view
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/open.html");
		
		main.put("segmentContent", groupListCtr.getInitialComponent());
		main.remove(searchController.getInitialComponent());

		if(segmentView == null) {
			segmentView = SegmentViewFactory.createSegmentView("segments", main, this);
			allOpenLink = LinkFactory.createLink("opengroups.all", main, this);
			segmentView.addSegment(allOpenLink, true);
		
			searchOpenLink = LinkFactory.createLink("opengroups.search", main, this);
			segmentView.addSegment(searchOpenLink, false);
		} else {
			segmentView.select(allOpenLink);
		}
		
		
		// 4) update toolboxe
		columnLayoutCtr.hideCol2(true);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_MENU_OPEN, 0l);
		addToHistory(ureq, ores, null, wControl, true);
	}
	//fxdiff VCRP-1,2: access control of resources
	private void doSearchOpenGroupList(UserRequest ureq, WindowControl wControl) {
		// 1) initialize list controller and datamodel
		initGroupListCtrAndModel(false, false, true, CMD_MENU_OPEN_SEARCH, ureq);
		// 2) load data into model
		updateSearchGroupList();
		// 3) set correct page and segmented view
		main.setPage(Util.getPackageVelocityRoot(this.getClass()) + "/open.html");
		main.put("search", searchController.getInitialComponent());
		
		if(segmentView == null) {
			segmentView = SegmentViewFactory.createSegmentView("segments", main, this);
			allOpenLink = LinkFactory.createLink("opengroups.all", main, this);
			segmentView.addSegment(allOpenLink, false);
		
			searchOpenLink = LinkFactory.createLink("opengroups.search", main, this);
			segmentView.addSegment(searchOpenLink, true);
		} else {
			segmentView.select(searchOpenLink);
		}

		// 4) update toolboxe
		columnLayoutCtr.hideCol2(true);
		
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CMD_MENU_OPEN_SEARCH, 0l);
		addToHistory(ureq, ores, null, wControl, true);
	}

	/**
	 * Initialize the group list controller and the group list model given.
	 * 
	 * @param withLeaveAndDelete config flag: true: leave and delete button are
	 *          showed, false: not showed
	 * @param ureq
	 */
	 //fxdiff VCRP-1,2: access control of resources
	private void initGroupListCtrAndModel(boolean withLeaveAndDelete, boolean withCourse, boolean withAC, String tableId, UserRequest ureq) {
		// 1) init listing controller
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, tableId);
		if (CMD_MENU_OPEN.equals(tableId) || CMD_MENU_OPEN_SEARCH.equals(tableId)) {
			tableConfig.setTableEmptyMessage(translate("open.nogroup"));			
		} else {
			tableConfig.setTableEmptyMessage(translate("index.table.nogroup"));			
		}
		removeAsListenerAndDispose(groupListCtr);
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator(), false);
		listenTo(groupListCtr);

		int columnCount = 0;
		if(withAC) {
			CustomCellRenderer acRenderer = new BGAccessControlledCellRenderer();
			groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.ac", 8, null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
			columnCount++;
		}
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.bgname", 0, TABLE_ACTION_LAUNCH, getLocale()));
		groupListCtr.addColumnDescriptor(!withCourse, new DefaultColumnDescriptor("table.header.description", 1, null, getLocale()));
		columnCount += 2;
		if(withCourse) {
			CustomCellRenderer resourcesRenderer = new BGResourcesCellRenderer(this, main, getTranslator());
			groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.resources", 5, null, getLocale(), 
				ColumnDescriptor.ALIGNMENT_LEFT, resourcesRenderer));
			columnCount++;
		}
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.type", 2, null, getLocale()));
		columnCount++;
		if (withLeaveAndDelete) {
			groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.leave", 3, TABLE_ACTION_LEAVE, 
					translate("table.header.leave"), null));
			groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.delete", 4, TABLE_ACTION_DELETE, 
					translate("table.header.delete"), null));
			columnCount += 2;
		}
		if(withAC) {
			groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.ac", 7, TABLE_ACTION_ACCESS, getLocale()));
			columnCount++;
		}
		// 2) init list model
		groupListModel = new BusinessGroupTableModelWithType(new ArrayList<BGTableItem>(), getTranslator(), columnCount);
		groupListCtr.setTableDataModel(groupListModel);
		main.put("groupList", groupListCtr.getInitialComponent());
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for all groups
	 */
	 //fxdiff VCRP-1,2: access control of resources
	private void updateGroupListModelAll() {
		List<BGTableItem> wrapped = new ArrayList<BGTableItem>();
		// buddy groups
		collectGroupListModelBuddygroups(wrapped);
		// learning groups
		List<BusinessGroup> groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		for (BusinessGroup group:groups) {
			wrapped.add(wrapGroup(group, true, null, null, false, null));
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		for (BusinessGroup group:groups) {
			wrapped.add(wrapGroup(group, true, null, null, false, null));
		}
		// right groups
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
		for (BusinessGroup group:groups) {
			wrapped.add(wrapGroup(group, true, null, null, false, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for buddy groups
	 */
	 //fxdiff VCRP-1,2: access control of resources
	private void updateGroupListModelBuddygroups() {
		List<BGTableItem> wrapped = new ArrayList<BGTableItem>();
		collectGroupListModelBuddygroups(wrapped);
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}
	//fxdiff VCRP-1,2: access control of resources
	private void collectGroupListModelBuddygroups(List<BGTableItem> wrapped) {
		// buddy groups
		List<BusinessGroup> groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
		for (BusinessGroup group:groups) {
			BGTableItem item = wrapGroup(group, true, Boolean.TRUE, Boolean.TRUE, false, null);
			wrapped.add(item);
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
		for (BusinessGroup group:groups) {
			BGTableItem item = wrapGroup(group, true, Boolean.TRUE, null, false, null);
			wrapped.add(item);
		}
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for learning groups
	 */
	private void updateGroupListModelLearninggroups() {
		List<BGTableItem> wrapped = new ArrayList<BGTableItem>();
		// learning groups
		List<BusinessGroup> groups = bgm.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		for (BusinessGroup group:groups) {
			wrapped.add(wrapGroup(group, true, null, null, false, null));
		}
		groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
		for (BusinessGroup group:groups) {
			wrapped.add(wrapGroup(group, true, null, null, false, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}

	/**
	 * Get most recent data from the database and init the group list model with
	 * data for right groups
	 */
	private void updateGroupListModelRightgroups() {
		List<BGTableItem> wrapped = new ArrayList<BGTableItem>();
		// buddy groups
		// right groups
		List<BusinessGroup> groups = bgm.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
		for(BusinessGroup group:groups) {
			wrapped.add(wrapGroup(group, true, null, null, false, null));
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}
	//fxdiff VCRP-1,2: access control of resources
	private void updateOpenGroupList() {
		List<BGTableItem> wrapped = new ArrayList<BGTableItem>();

		List<BusinessGroupAccess> bgAccess = acFrontendManager.getOfferAccessForBusinessGroup(true, new Date());
		for(BusinessGroupAccess bga:bgAccess) {
			BusinessGroup group = bga.getGroup();
			boolean member = bgm.isIdentityInBusinessGroup(getIdentity(), group);
			if(bga.getMethods() != null && !bga.getMethods().isEmpty()) {
				wrapped.add(wrapGroup(group, member, Boolean.TRUE, null, true, bga.getMethods()));
			}
		}
		groupListModel.setEntries(wrapped);
		groupListCtr.modelChanged();
	}
	//fxdiff VCRP-1,2: access control of resources
	private void updateSearchGroupList() {
		List<BusinessGroup> groups; 
		if(searchController.isEmpty()) {
			groups = Collections.emptyList();
		} else {
			Long id = searchController.getId();
			String name = searchController.getName();
			String description = searchController.getDescription();
			String owner = searchController.getOwner();
			groups = bgm.findBusinessGroups(null, getIdentity(), id, name, description, owner);
		}

		List<BGTableItem> wrapped = new ArrayList<BGTableItem>();
		Set<Long> membership = new HashSet<Long>();
		Map<Long,Long> resourceKeys = new HashMap<Long,Long>();
		for(BusinessGroup group:groups) {
			OLATResource ores = OLATResourceManager.getInstance().findResourceable(group);
			resourceKeys.put(group.getKey(), ores.getKey());
			if(bgm.isIdentityInBusinessGroup(this.getIdentity(), group)) {
				membership.add(group.getKey());
			}
		}
		
		List<OLATResourceAccess> resourcesWithAC = acFrontendManager.getAccessMethodForResources(resourceKeys.values(), true, new Date());
		for(BusinessGroup group:groups) {
			Long oresKey = resourceKeys.get(group.getKey());
			OLATResourceAccess bgAccess = null;
			
			for(OLATResourceAccess access:resourcesWithAC) {
				if(oresKey.equals(access.getResource().getKey())){
					bgAccess = access;
					break;
				}
			}
			if(bgAccess != null) {
				wrapped.add(wrapGroup(group, true, Boolean.TRUE, null, true, bgAccess.getMethods()));
			}
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
	//fxdiff VCRP-1,2: access control of resources
	private BGTableItem wrapGroup(BusinessGroup group, boolean member, Boolean allowLeave, Boolean allowDelete, boolean accessControl,
			List<PriceMethodBundle> access) {
		BGTableItem tableItem = new BGTableItem(group, member, allowLeave, allowDelete, accessControl, access);
		
		if(group.getGroupContext() != null) {
			List<RepositoryEntry> resources = contextManager.findRepositoryEntriesForBGContext(group.getGroupContext());
			tableItem.setResources(resources);
		}
		
		return tableItem;
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
		//fxdiff VCRP-1,2: access control of resources
		myEntriesTn = new GenericTreeNode();
		myEntriesTn.setTitle(translate("menu.opengroups"));
		myEntriesTn.setUserObject(CMD_MENU_OPEN);
		myEntriesTn.setAltText(translate("menu.opengroups.alt"));
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
		//fxdiff BAKS-7 Resume function
		if(viewIdentifier != null && viewIdentifier.endsWith(":0")) {
			viewIdentifier = viewIdentifier.substring(0, viewIdentifier.length() - 2);
		}

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

	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		
		TreeNode rootNode = menuTree.getTreeModel().getRootNode();
		TreeNode activatedNode = TreeHelper.findNodeByUserObject(type, rootNode);
		if (activatedNode != null) {
			menuTree.setSelectedNode(activatedNode);
			activateContent(ureq, activatedNode.getUserObject());
		} else if(CMD_MENU_OPEN_SEARCH.equals(type)) {
			//segmented view ?
			activatedNode = TreeHelper.findNodeByUserObject(CMD_MENU_OPEN, rootNode);
			if(activatedNode != null) {
				menuTree.setSelectedNode(activatedNode);
				activateContent(ureq, CMD_MENU_OPEN_SEARCH);
			}
		}
		
		if(activatedNode == null) {
			// not found, activate the root node
			menuTree.setSelectedNode(rootNode);
			activateContent(ureq, rootNode.getUserObject());
			//check for toolbox activation points
			if (type.equals(ACTION_ADD_BUDDYGROUP)) {
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