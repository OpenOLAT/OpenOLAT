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

package org.olat.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.admin.user.DeletedUsersController;
import org.olat.admin.user.NewUsersNotificationsController;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UserCreateController;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.admin.user.delete.DirectDeleteController;
import org.olat.admin.user.delete.TabbedPaneController;
import org.olat.admin.user.imp.UserImportController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <pre>
 * Initial Date:  Jan 16, 2006
 * @author Florian Gnaegi
 * 
 * Comment:
 * This controller offers user and system group administrative functionality. The 
 * features can be enabled / disabled in the spring file for the user
 * manager, OLAT administrators always have full access to the tools. 
 * 
 * To append predefined searches use ActionExtensions and register them for UserAdminMainController.EXTENSIONPOINT_MENU_MENUQUERIES.
 * </pre>
 */
public class UserAdminMainController extends MainLayoutBasicController implements Activateable2 {
	public static final String EXTENSIONPOINT_MENU_MENUQUERIES = ".menu.menuqueries";
	private static boolean extensionLogged = false;
	private static final OLog log = Tracing.createLoggerFor(UserAdminMainController.class);
	private MenuTree olatMenuTree;
	private Panel content;
	
	private LayoutMain3ColsController columnLayoutCtr;
	private Controller contentCtr;
	private UserAdminController userAdminCtr;
	private UsermanagerUserSearchController userSearchCtrl;
	private VelocityContainer rolesVC, queriesVC;
	
	private String activatePaneInDetailView = null;

	private LockResult lock;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;

	/**
	 * Constructor of the home main controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 */
	public UserAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		olatMenuTree = new MenuTree("olatMenuTree");
		olatMenuTree.setExpandSelectedNode(false);
		olatMenuTree.setScrollTopOnClick(true);
		olatMenuTree.setRootVisible(false);
		TreeModel tm = buildTreeModel(ureq); 
		olatMenuTree.setTreeModel(tm);
		TreeNode firstNode = (TreeNode)tm.getRootNode().getChildAt(0);
		olatMenuTree.setSelectedNodeId(firstNode.getIdent());
		olatMenuTree.addListener(this);
		// allow closing of active menu tree element
		olatMenuTree.setExpandSelectedNode(false);

		// we always start with a search controller
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(firstNode.getUserObject().toString(), 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contentCtr = new UsermanagerUserSearchController(ureq, bwControl);
		listenTo(contentCtr); // auto dispose later
		
		content = new Panel("content");
		content.setContent(contentCtr.getInitialComponent());

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, content, "useradminmain");
		columnLayoutCtr.addCssClassToMain("o_useradmin");
		
		listenTo(columnLayoutCtr); // auto dispose later
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == olatMenuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = olatMenuTree.getSelectedNode();
				Object userObject = selTreeNode.getUserObject();
				Component resComp = initComponentFromMenuCommand(userObject, ureq);
				content.setContent(resComp);
			} else { // the action was not allowed anymore
				content.setContent(null); // display an empty field (empty panel)
			}
		} else {
			log.warn("Unhandled olatMenuTree event: " + event.getCommand());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == contentCtr ) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent userChoosenEvent = (SingleIdentityChosenEvent) event;
				Identity identity = userChoosenEvent.getChosenIdentity();
				// cleanup old userAdminCtr controller
				removeAsListenerAndDispose(userAdminCtr);

				OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				
				WindowControl bwControl;
				if(contentCtr instanceof UsermanagerUserSearchController) {
					UsermanagerUserSearchController ctrl = (UsermanagerUserSearchController)contentCtr;
					WindowControl tableControl = ctrl.getTableControl();
					if(tableControl == null) {
						tableControl = ctrl.getWindowControlForDebug();
					}
					bwControl = addToHistory(ureq, ores, null, tableControl, true);
				} else {
					bwControl = addToHistory(ureq, ores, null, contentCtr.getWindowControlForDebug(), true);
				}
				userAdminCtr = new UserAdminController(ureq, bwControl, identity);
				listenTo(userAdminCtr);
				// activate a special pane in the tabbed pane when set
				if (activatePaneInDetailView != null) {
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(activatePaneInDetailView);
					userAdminCtr.activate(ureq, entries, null);
				}
				content.setContent(userAdminCtr.getInitialComponent());
				// deactivate back button for user create controller, kames no sense there
				if (contentCtr instanceof UserCreateController) {
					userAdminCtr.setBackButtonEnabled(false);
				} else {
					userAdminCtr.setBackButtonEnabled(true);
				}
				
			}
		} else if (source == userAdminCtr) {
			if (event == Event.BACK_EVENT) {
				Identity editedIdentity = userAdminCtr.getEditedIdentity();
				removeAsListenerAndDispose(userAdminCtr);
				userAdminCtr = null;
				// update data model of content controller when of type user search
				// to display correct values of identity
				if (contentCtr instanceof UsermanagerUserSearchController) {
					UsermanagerUserSearchController userSearchCtr = (UsermanagerUserSearchController) contentCtr;
					userSearchCtr.reloadFoundIdentity(editedIdentity);
					addToHistory(ureq, userSearchCtr);
				}
				content.setContent(contentCtr.getInitialComponent());
			}
		}
	}
	
	private Component initComponentFromMenuCommand(Object uobject, UserRequest ureq) {
		//in any case release delete user gui lock (reaquired if user deletion is again clicked)
		releaseDeleteUserLock();
		
		if (uobject instanceof GenericActionExtension) {
			GenericActionExtension ae = (GenericActionExtension) uobject;
			TreeNode node = ((GenericTreeModel)olatMenuTree.getTreeModel()).findNodeByUserObject(uobject);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("AE", new Long(node.getPosition()));
			WindowControl bwControl = addToHistory(ureq, ores, null);
			contentCtr = ae.createController(ureq, bwControl, null);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}	

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(uobject.toString(), 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		//first check if it is node which opens a subtree with further uobject.tree.commands
		if (uobject.equals("menuroles")) {
			if (rolesVC == null) {
				rolesVC = createVelocityContainer("systemroles");
			}
			addToHistory(ureq, bwControl);
			return rolesVC;
		}		
		else if (uobject.equals("menuqueries")) {
			if (queriesVC == null) {
				queriesVC = createVelocityContainer("predefinedqueries");
			}
			addToHistory(ureq, bwControl);
			return queriesVC;
		}
		else if (uobject.equals("menuaccess")) {
			if (queriesVC == null) {
				queriesVC = createVelocityContainer("systemroles");
			}
			addToHistory(ureq, bwControl);
			return queriesVC;
		} else if (uobject.equals("userdelete")) {
			//creates the user deletin controller
			//if locking fails -> a contentCtrl is created
			//-> hence removeAsListenerAndDispose(contentCtr) is delegated to the method called!
			addToHistory(ureq, bwControl);
			return createAndLockUserDeleteController(ureq, bwControl);
		} else if (uobject.equals("userdelete_direct")) {
			//creates the user deletin controller
			//if locking fails -> a contentCtrl is created
			//-> hence removeAsListenerAndDispose(contentCtr) is delegated to the method called!
			addToHistory(ureq, bwControl);
			return createAndLockDirectUserDeleteController(ureq, bwControl);
		} 		
		
		
		if (uobject.equals("usearch") || uobject.equals("useradmin")) {
			activatePaneInDetailView = null;
			removeAsListenerAndDispose(userSearchCtrl);
			contentCtr = userSearchCtrl = new UsermanagerUserSearchController(ureq, bwControl);
			listenTo(contentCtr);

			addToHistory(ureq, bwControl);
			return contentCtr.getInitialComponent();
		}
		//these nodes re-create (not stateful) content Controller (contentCtrl)
		removeAsListenerAndDispose(contentCtr);
		if (uobject.equals("ucreate")) {
			activatePaneInDetailView = null;
			boolean canCreateOLATPassword = false;
			if (ureq.getUserSession().getRoles().isOLATAdmin()) {
				// admin will override configuration
				canCreateOLATPassword = true;
			} else {
				Boolean canCreatePwdByConfig = BaseSecurityModule.USERMANAGER_CAN_CREATE_PWD;				
				canCreateOLATPassword = canCreatePwdByConfig.booleanValue();
			}
			contentCtr = new UserCreateController(ureq, bwControl, canCreateOLATPassword);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("usersimport")) {
			activatePaneInDetailView = null;
			boolean canCreateOLATPassword = false;
			if (ureq.getUserSession().getRoles().isOLATAdmin()) {
				// admin will override configuration
				canCreateOLATPassword = true;
			} else {
				Boolean canCreatePwdByConfig = BaseSecurityModule.USERMANAGER_CAN_CREATE_PWD;				
				canCreateOLATPassword = canCreatePwdByConfig.booleanValue();
			}
			contentCtr = new UserImportController(ureq, bwControl, canCreateOLATPassword);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("admingroup")) {
			activatePaneInDetailView = null;
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN)};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("authorgroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS)};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("coauthors")) {
			activatePaneInDetailView = "edit.uroles";
			// special case: use user search controller and search for all users that have author rights
			List<Identity> resourceOwners = repositoryService.getIdentitiesWithRole(GroupRoles.owner.name());
			UsermanagerUserSearchController myCtr = new UsermanagerUserSearchController(ureq, bwControl, resourceOwners, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			// now subtract users that are in the author group to get the co-authors
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS)};
			List<Identity> identitiesFromAuthorGroup = securityManager.getVisibleIdentitiesByPowerSearch(null, null, true, secGroup , null, null, null, null);
			myCtr.removeIdentitiesFromSearchResult(ureq, identitiesFromAuthorGroup);
			contentCtr = myCtr;
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("resourceowners")) {
			activatePaneInDetailView = "edit.uroles";
			// First get all resource owners (co-authors) ...
			List<Identity> resourceOwners = repositoryService.getIdentitiesWithRole(GroupRoles.owner.name());
			UsermanagerUserSearchController myCtr = new UsermanagerUserSearchController(ureq, bwControl, resourceOwners, Identity.STATUS_VISIBLE_LIMIT, true);
			// ... now add users that are in the author group but don't own a resource yet
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS)};
			List<Identity> identitiesFromAuthorGroup = securityManager.getVisibleIdentitiesByPowerSearch(null, null, true, secGroup , null, null, null, null);
			myCtr.addIdentitiesToSearchResult(ureq, identitiesFromAuthorGroup);
			contentCtr = myCtr;
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("courseparticipants")) {
			activatePaneInDetailView = "edit.courses";
			List<Identity> resourceOwners = repositoryService.getIdentitiesWithRole(GroupRoles.participant.name());
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, resourceOwners, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("groupmanagergroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS)};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		if (uobject.equals("coursecoach")) {
			activatePaneInDetailView = "edit.uroles";
			List<Identity> coaches = repositoryService.getIdentitiesWithRole(GroupRoles.coach.name());
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, coaches, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		if (uobject.equals("groupcoach")) {
			activatePaneInDetailView = "edit.uroles";
			List<Identity> coaches = businessGroupService.getIdentitiesWithRole(GroupRoles.coach.name());
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, coaches, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("usermanagergroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_USERMANAGERS)};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("usergroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS)};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}		
		else if (uobject.equals("anonymousgroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS)};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("userswithoutgroup")) {
			activatePaneInDetailView = "edit.withoutgroup";
			List<Identity> usersWithoutGroup = securityManager.findIdentitiesWithoutBusinessGroup(Identity.STATUS_VISIBLE_LIMIT);
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, usersWithoutGroup, null, true, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("userswithoutemail")) {
			activatePaneInDetailView = "userswithoutemail";
			List<Identity> usersWithoutEmail = userManager.findVisibleIdentitiesWithoutEmail();
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, usersWithoutEmail, null, true, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("usersemailduplicates")) {
			activatePaneInDetailView = "users.email.duplicate";
			List<Identity> usersEmailDuplicates = userManager.findVisibleIdentitiesWithEmailDuplicates();
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, usersEmailDuplicates, null, true, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("logondeniedgroup")) {
			activatePaneInDetailView = "edit.uroles";
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl,null, null, null, null, null, Identity.STATUS_LOGIN_DENIED, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}		
		else if (uobject.equals("deletedusers")) {
			activatePaneInDetailView = "list.deletedusers";
			contentCtr = new DeletedUsersController(ureq, bwControl);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.lastweek")) {
			activatePaneInDetailView = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -7);
			Date time = cal.getTime();
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, null, null, null, time, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.lastmonth")) {
			activatePaneInDetailView = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			Date time = cal.getTime();
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, null, null, null, time, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.sixmonth")) {
			activatePaneInDetailView = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -6);
			Date time = cal.getTime();
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, null, null, null, time, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.newUsersNotification")) {
			activatePaneInDetailView = null;
			bwControl = addToHistory(ureq, ores, null);	
			contentCtr = new NewUsersNotificationsController(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("noauthentication")) {
			activatePaneInDetailView = null;
			String[] auth = {null};
			contentCtr = new UsermanagerUserSearchController(ureq, bwControl, null, null, auth, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			addToHistory(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		} else{
			//to be removed
			throw new AssertException("did not expect to land here in UserAdminMainController this is because uboject is "+uobject.toString());
		}
	}

	/**
	 * Creates a DirectDeleteController and acquire a 'delete-user-lock'.
	 * The lock is for both direct-deletion and workflow with email.
	 * @param ureq
	 * @return
	 */
	private Component createAndLockDirectUserDeleteController(UserRequest ureq, WindowControl wControl) {
		Controller lockCtrl = acquireDeleteUserLock(ureq);
		if (lockCtrl == null) {
			//success -> create new User deletion workflow
			removeAsListenerAndDispose(contentCtr);
			contentCtr = new DirectDeleteController(ureq, wControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		} else {
			//failure -> monolog controller with message that lock failed
			return lockCtrl.getInitialComponent();
		}
	}

	/**
	 * Creates a TabbedPaneController (delete workflow with email)  and acquire a 'delete-user-lock'.
	 * The lock is for both direct-deletion and workflow with email.
	 * @param ureq
	 * @return
	 */
	private Component createAndLockUserDeleteController(UserRequest ureq, WindowControl wControl) {
		Controller lockCtrl = acquireDeleteUserLock(ureq);

		if (lockCtrl == null) {
			//success -> create new User deletion workflow
			activatePaneInDetailView = null;
			removeAsListenerAndDispose(contentCtr);
			contentCtr = new TabbedPaneController(ureq, wControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		} else {
			//failure -> monolog controller with message that lock failed
			return lockCtrl.getInitialComponent();
		}
	}

	/**
   * Acquire lock for whole delete-user workflow
   */
	private Controller acquireDeleteUserLock(UserRequest ureq) {
		OLATResourceable lockResourceable = OresHelper.createOLATResourceableTypeWithoutCheck(TabbedPaneController.class.getName());
		lock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(lockResourceable, ureq.getIdentity(), "deleteGroup");
		if (!lock.isSuccess()) {
			String fullname = userManager.getUserDisplayName(lock.getOwner());
			String text = getTranslator().translate("error.deleteworkflow.locked.by", new String[]{ fullname });
			Controller monoCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
			return monoCtr;
		}
		return null;
	}

	/**
	 * Releases the lock for this page if set
	 */
	private void releaseDeleteUserLock() {
		if (lock != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
			lock = null;
		}
	}

	private TreeModel buildTreeModel(UserRequest ureq) {
		boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		GenericTreeNode gtnChild, admin;
		Translator translator = getTranslator();
		
		GenericTreeModel gtm = new GenericTreeModel();
		admin = new GenericTreeNode();		
		admin.setTitle(translator.translate("menu.useradmin"));
		admin.setUserObject("useradmin");
		admin.setCssClass("o_sel_useradmin");
		admin.setAltText(translator.translate("menu.useradmin.alt"));
		gtm.setRootNode(admin);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.usearch"));
		gtnChild.setUserObject("usearch");
		gtnChild.setCssClass("o_sel_useradmin_search");
		gtnChild.setAltText(translator.translate("menu.usearch.alt"));
		admin.setDelegate(gtnChild);
		admin.addChild(gtnChild);

		Boolean canCreate = BaseSecurityModule.USERMANAGER_CAN_CREATE_USER;
		if (canCreate.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.ucreate"));
			gtnChild.setUserObject("ucreate");
			gtnChild.setCssClass("o_sel_useradmin_create");
			gtnChild.setAltText(translator.translate("menu.ucreate.alt"));
			admin.addChild(gtnChild);
			
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.usersimport"));
			gtnChild.setUserObject("usersimport");
			gtnChild.setCssClass("o_sel_useradmin_import");
			gtnChild.setAltText(translator.translate("menu.usersimport.alt"));
			admin.addChild(gtnChild);
		}
		Boolean canDelete = BaseSecurityModule.USERMANAGER_CAN_DELETE_USER;
		if (canDelete.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.userdelete"));
			gtnChild.setUserObject("userdelete");
			gtnChild.setCssClass("o_sel_useradmin_delete");
			gtnChild.setAltText(translator.translate("menu.userdelete.alt"));
			admin.addChild(gtnChild);

			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.userdelete.direct"));
			gtnChild.setUserObject("userdelete_direct");
			gtnChild.setCssClass("o_sel_useradmin_direct_delete");
			gtnChild.setAltText(translator.translate("menu.userdelete.direct.alt"));
			admin.addChild(gtnChild);
		}

		
		// START submenu access and rights
		GenericTreeNode gtn3 = new GenericTreeNode();		
		gtn3.setTitle(translator.translate("menu.menuaccess"));
		gtn3.setUserObject("menuaccess");
		gtn3.setAltText(translator.translate("menu.menuaccess.alt"));
		admin.addChild(gtn3);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.usergroup"));
		gtnChild.setUserObject("usergroup");
		gtnChild.setAltText(translator.translate("menu.usergroup.alt"));
		gtn3.addChild(gtnChild);

		Boolean canAuthors = BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS;
		if (canAuthors.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.authorgroup"));
			gtnChild.setUserObject("authorgroup");
			gtnChild.setAltText(translator.translate("menu.authorgroup.alt"));
			gtn3.addChild(gtnChild);
			
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.coauthors"));
			gtnChild.setUserObject("coauthors");
			gtnChild.setAltText(translator.translate("menu.coauthors.alt"));
			gtn3.addChild(gtnChild);

			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.resourceowners"));
			gtnChild.setUserObject("resourceowners");
			gtnChild.setAltText(translator.translate("menu.resourceowners.alt"));
			gtn3.addChild(gtnChild);

		}

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.coursecoach"));
		gtnChild.setUserObject("coursecoach");
		gtnChild.setAltText(translator.translate("menu.coursecoach.alt"));
		gtn3.addChild(gtnChild);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.courseparticipants"));
		gtnChild.setUserObject("courseparticipants");
		gtnChild.setAltText(translator.translate("menu.courseparticipants.alt"));
		gtn3.addChild(gtnChild);

		Boolean canGroupmanagers = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS;
		if (canGroupmanagers.booleanValue() || isOlatAdmin) {			
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.groupmanagergroup"));
			gtnChild.setUserObject("groupmanagergroup");
			gtnChild.setAltText(translator.translate("menu.groupmanagergroup.alt"));
			gtn3.addChild(gtnChild);

			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.groupcoach"));
			gtnChild.setUserObject("groupcoach");
			gtnChild.setAltText(translator.translate("menu.groupcoach.alt"));
			gtn3.addChild(gtnChild);
		}

		// admin group and user manager group always restricted to admins
		if (isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.usermanagergroup"));
			gtnChild.setUserObject("usermanagergroup");
			gtnChild.setAltText(translator.translate("menu.usermanagergroup.alt"));
			gtn3.addChild(gtnChild);

			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.admingroup"));
			gtnChild.setUserObject("admingroup");
			gtnChild.setAltText(translator.translate("menu.admingroup.alt"));
			gtn3.addChild(gtnChild);
		}

		Boolean canGuests = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS;
		if (canGuests.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.anonymousgroup"));
			gtnChild.setUserObject("anonymousgroup");
			gtnChild.setAltText(translator.translate("menu.anonymousgroup.alt"));
			gtn3.addChild(gtnChild);
		}
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.noauthentication"));
		gtnChild.setUserObject("noauthentication");
		gtnChild.setAltText(translator.translate("menu.noauthentication.alt"));
		gtn3.addChild(gtnChild);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.logondeniedgroup"));
		gtnChild.setUserObject("logondeniedgroup");
		gtnChild.setAltText(translator.translate("menu.logondeniedgroup.alt"));
		gtn3.addChild(gtnChild);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.deletedusers"));
		gtnChild.setUserObject("deletedusers");
		gtnChild.setAltText(translator.translate("menu.deletedusers.alt"));
		gtn3.addChild(gtnChild);
		
		// END submenu access and rights

		// START other queries
		gtn3 = new GenericTreeNode();		
		gtn3.setTitle(translator.translate("menu.menuqueries"));
		gtn3.setUserObject("menuqueries");
		gtn3.setAltText(translator.translate("menu.menuqueries.alt"));
		admin.addChild(gtn3);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.userswithoutgroup"));
		gtnChild.setUserObject("userswithoutgroup");
		gtnChild.setAltText(translator.translate("menu.userswithoutgroup.alt"));
		gtn3.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.users.without.email"));
		gtnChild.setUserObject("userswithoutemail");
		gtnChild.setAltText(translator.translate("menu.users.without.email.alt"));
		gtn3.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.users.email.duplicate"));
		gtnChild.setUserObject("usersemailduplicates");
		gtnChild.setAltText(translator.translate("menu.users.email.duplicate.alt"));
		gtn3.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.created.lastweek"));
		gtnChild.setUserObject("created.lastweek");
		gtnChild.setAltText(translator.translate("menu.created.lastweek.alt"));
		gtn3.addChild(gtnChild);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.created.lastmonth"));
		gtnChild.setUserObject("created.lastmonth");
		gtnChild.setAltText(translator.translate("menu.created.lastmonth.alt"));
		gtn3.addChild(gtnChild);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.created.sixmonth"));
		gtnChild.setUserObject("created.sixmonth");
		gtnChild.setAltText(translator.translate("menu.created.sixmonth.alt"));
		gtn3.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.created.newUsersNotification"));
		gtnChild.setUserObject("created.newUsersNotification");
		gtnChild.setAltText(translator.translate("menu.created.newUsersNotification.alt"));
		gtn3.addChild(gtnChild);

		//add extension menues as child items
		ExtManager extm = ExtManager.getInstance();
		int cnt = extm.getExtensionCnt();
		for (int i = 0; i < cnt; i++) {
			Extension anExt = extm.getExtension(i);
			// 1) general menu extensions
			ExtensionElement ee = anExt.getExtensionFor(UserAdminMainController.class.getName() + EXTENSIONPOINT_MENU_MENUQUERIES, ureq);
			if (ee instanceof GenericActionExtension && anExt.isEnabled()) {
				GenericActionExtension ae = (GenericActionExtension)ee;
				gtnChild = new GenericTreeNode();
				String menuText = ae.getActionText(getLocale());
				gtnChild.setTitle(menuText);
				gtnChild.setUserObject(ae);
				gtnChild.setAltText(ae.getDescription(getLocale()));
				gtn3.addChild(gtnChild);
				// inform only once
				if (!extensionLogged) {
					logInfo("added menu entry for locale " + getLocale().toString() + " '" + menuText + "'", null);
				}
			} 
		}
		extensionLogged = true;
		
		// END other queries
		return gtm;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		GenericTreeModel tm = (GenericTreeModel)olatMenuTree.getTreeModel();
		ContextEntry entry = entries.get(0);
		String entryPoint = entry.getOLATResourceable().getResourceableTypeName();
		if(entryPoint.startsWith("notifications") || entryPoint.startsWith("NewIdentityCreated")) {
			TreeNode node = tm.findNodeByUserObject("created.newUsersNotification");
			selectNode(ureq, node);
		} else if(entryPoint.startsWith("AE")) {
			TreeNode node = tm.findNodeByUserObject("menuqueries");
			int pos = entries.get(0).getOLATResourceable().getResourceableId().intValue();
			if(pos >= 0 && pos < node.getChildCount()) {
				TreeNode childNode = (TreeNode)node.getChildAt(pos);
				selectNode(ureq, childNode);
			}
		} else {
			TreeNode node = tm.findNodeByUserObject(entryPoint);
			if(node != null) {
				selectNode(ureq, node);
				entries = entries.subList(1, entries.size());
				if(contentCtr instanceof Activateable2) {
					((Activateable2)contentCtr).activate(ureq, entries, entry.getTransientState());
				}
				if(!entries.isEmpty() && userAdminCtr != null) {
					userAdminCtr.activate(ureq, entries, null);
				}
			}
		}
	}
	
	private void selectNode(UserRequest ureq, TreeNode childNode) {
		olatMenuTree.setSelectedNode(childNode);
		Component resComp = initComponentFromMenuCommand(childNode.getUserObject(), ureq);
		content.setContent(resComp);
	}

	public void activate(UserRequest ureq, String viewIdentifier) {
		if(viewIdentifier == null) return;
		
		if(viewIdentifier.startsWith("notifications") || viewIdentifier.startsWith("NewIdentityCreated")) {
			GenericTreeModel tm = (GenericTreeModel)olatMenuTree.getTreeModel();
			TreeNode node = tm.findNodeByUserObject("created.newUsersNotification");
			olatMenuTree.setSelectedNode(node);
			Component resComp = initComponentFromMenuCommand("created.newUsersNotification", ureq);
			content.setContent(resComp);
		} else if(viewIdentifier.startsWith("AE")) {
			String posStr = viewIdentifier.substring(3);
			try {
				GenericTreeModel treeModel = (GenericTreeModel)olatMenuTree.getTreeModel();
				TreeNode node = treeModel.findNodeByUserObject("menuqueries");
				int pos = Integer.parseInt(posStr);
				if(pos >= 0 && pos < node.getChildCount()) {
					TreeNode childNode = (TreeNode)node.getChildAt(pos);
					olatMenuTree.setSelectedNode(childNode);
					Component resComp = initComponentFromMenuCommand(childNode.getUserObject(), ureq);
					content.setContent(resComp);
				}
			} catch (Exception e) {
				logWarn("", e);
			}	
		} else {
			int first = viewIdentifier.indexOf(":");
			String uobject = viewIdentifier.substring(0, first);
			GenericTreeModel treeModel = (GenericTreeModel)olatMenuTree.getTreeModel();
			TreeNode node = treeModel.findNodeByUserObject(uobject);
			if(node == null) {
				node = treeModel.getRootNode();
				uobject = (String)node.getUserObject();
			}
			olatMenuTree.setSelectedNode(node);
			Component resComp = initComponentFromMenuCommand(uobject, ureq);
			content.setContent(resComp);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// controllers disposed in BasicController
		releaseDeleteUserLock();
	}
}
