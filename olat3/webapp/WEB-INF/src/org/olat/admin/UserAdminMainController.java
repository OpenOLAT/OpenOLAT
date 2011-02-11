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

package org.olat.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.admin.user.NewUsersNotificationsController;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UserCreateController;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.admin.user.delete.DirectDeleteController;
import org.olat.admin.user.delete.TabbedPaneController;
import org.olat.admin.user.imp.UserImportController;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.PermissionOnResourceable;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.action.ActionExtension;
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
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;

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
public class UserAdminMainController extends MainLayoutBasicController implements Activateable {
	public static final String EXTENSIONPOINT_MENU_MENUQUERIES = ".menu.menuqueries";
	private static boolean extensionLogged = false;
	OLog log = Tracing.createLoggerFor(this.getClass());
	private MenuTree olatMenuTree;
	private Panel content;
	
	private LayoutMain3ColsController columnLayoutCtr;
	private Controller contentCtr;
	private UserAdminController userAdminCtr;
	private VelocityContainer rolesVC, queriesVC;
	
	private String activatePaneInDetailView = null;

	private LockResult lock;

	/**
	 * Constructor of the home main controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 */
	public UserAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);		
		olatMenuTree = new MenuTree("olatMenuTree");				
		TreeModel tm = buildTreeModel(ureq.getUserSession().getRoles().isOLATAdmin()); 
		olatMenuTree.setTreeModel(tm);
		INode firstNode = tm.getRootNode().getChildAt(0);
		olatMenuTree.setSelectedNodeId(firstNode.getIdent());
		olatMenuTree.addListener(this);

		// we always start with a search controller
		contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl());
		listenTo(contentCtr); // auto dispose later
		
		content = new Panel("content");
		content.setContent(contentCtr.getInitialComponent());

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, content, "useradminmain");
		columnLayoutCtr.addCssClassToMain("o_useradmin");
		
		listenTo(columnLayoutCtr); // auto dispose later
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
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
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == contentCtr ) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent userChoosenEvent = (SingleIdentityChosenEvent) event;
				Identity identity = userChoosenEvent.getChosenIdentity();
				// cleanup old userAdminCtr controller
				removeAsListenerAndDispose(userAdminCtr);
				userAdminCtr = new UserAdminController(ureq, getWindowControl(), identity);
				listenTo(userAdminCtr);
				// activate a special pane in the tabbed pane when set
				if (	activatePaneInDetailView != null)
					userAdminCtr.activate(ureq, activatePaneInDetailView);
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
				removeAsListenerAndDispose(userAdminCtr);
				userAdminCtr = null;
				// update data model of content controller when of type user search
				// to display correct values of identity
				if (contentCtr instanceof UsermanagerUserSearchController) {
					UsermanagerUserSearchController userSearchCtr = (UsermanagerUserSearchController) contentCtr;
					userSearchCtr.reloadFoundIdentity();
				}
				content.setContent(contentCtr.getInitialComponent());
			}
		}
	}
	
	private Component initComponentFromMenuCommand(Object uobject, UserRequest ureq) {
		//in any case release delete user gui lock (reaquired if user deletion is again clicked)
		releaseDeleteUserLock();
		
		if (uobject instanceof ActionExtension) {
			ActionExtension ae = (ActionExtension) uobject;
			contentCtr = ae.createController(ureq, getWindowControl(), null);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}	
		
		//first check if it is node which opens a subtree with further uobject.tree.commands
		if (uobject.equals("menuroles")) {
			if (rolesVC == null)
				rolesVC = createVelocityContainer("systemroles");
			return rolesVC;
		}		
		else if (uobject.equals("menuqueries")) {
			if (queriesVC == null)
				queriesVC = createVelocityContainer("predefinedqueries");
			return queriesVC;
		}
		else if (uobject.equals("menuaccess")) {
			if (queriesVC == null)
				queriesVC = createVelocityContainer("systemroles");
			return queriesVC;
		} else if (uobject.equals("userdelete")) {
			//creates the user deletin controller
			//if locking fails -> a contentCtrl is created
			//-> hence removeAsListenerAndDispose(contentCtr) is delegated to the method called!
			return createAndLockUserDeleteController(ureq);
		} else if (uobject.equals("userdelete_direct")) {
			//creates the user deletin controller
			//if locking fails -> a contentCtrl is created
			//-> hence removeAsListenerAndDispose(contentCtr) is delegated to the method called!
			return createAndLockDirectUserDeleteController(ureq);
		} 		
		
		//these nodes re-create (not stateful) content Controller (contentCtrl)
		//
		removeAsListenerAndDispose(contentCtr);
		if (uobject.equals("usearch") || uobject.equals("useradmin")) {
			activatePaneInDetailView = null;
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl());
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("ucreate")) {
			activatePaneInDetailView = null;
			boolean canCreateOLATPassword = false;
			if (ureq.getUserSession().getRoles().isOLATAdmin()) {
				// admin will override configuration
				canCreateOLATPassword = true;
			} else {
				Boolean canCreatePwdByConfig = BaseSecurityModule.USERMANAGER_CAN_CREATE_PWD;				
				canCreateOLATPassword = canCreatePwdByConfig.booleanValue();
			}
			contentCtr = new UserCreateController(ureq, getWindowControl(), canCreateOLATPassword);
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
			contentCtr = new UserImportController(ureq, getWindowControl(), canCreateOLATPassword);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("admingroup")) {
			activatePaneInDetailView = "";
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_ADMIN)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("authorgroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_AUTHORS)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("coauthors")) {
			activatePaneInDetailView = "edit.uroles";
			// special case: use user search controller and search for all users that have author rights
			PermissionOnResourceable[] permissions = {new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR)};
			UsermanagerUserSearchController myCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),null, permissions, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			// now subtract users that are in the author group to get the co-authors
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_AUTHORS)};
			List identitiesFromAuthorGroup = BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(null, null, true, secGroup , null, null, null, null);
			myCtr.removeIdentitiesFromSearchResult(ureq, identitiesFromAuthorGroup);
			contentCtr = myCtr;
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("resourceowners")) {
			activatePaneInDetailView = "edit.uroles";
			PermissionOnResourceable[] permissions = {new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),null, permissions, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("groupmanagergroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("usermanagergroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_USERMANAGERS)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("usergroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_OLATUSERS)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}		
		else if (uobject.equals("anonymousgroup")) {
			activatePaneInDetailView = "edit.uroles";
			SecurityGroup[] secGroup = {BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_ANONYMOUS)};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),secGroup, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("logondeniedgroup")) {
			activatePaneInDetailView = "edit.uroles";
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),null, null, null, null, null, Identity.STATUS_LOGIN_DENIED, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}		
		else if (uobject.equals("deletedusers")) {
			activatePaneInDetailView = "list.deletedusers";
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(),null, null, null, null, null, Identity.STATUS_DELETED, false);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.lastweek")) {
			activatePaneInDetailView = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -7);
			Date time = cal.getTime();
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), null, null, null, time, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.lastmonth")) {
			activatePaneInDetailView = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			Date time = cal.getTime();
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), null, null, null, time, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.sixmonth")) {
			activatePaneInDetailView = null;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -6);
			Date time = cal.getTime();
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), null, null, null, time, null, Identity.STATUS_VISIBLE_LIMIT, true);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("created.newUsersNotification")) {
			activatePaneInDetailView = null;
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(new OLATResourceable() {
				@Override
				public Long getResourceableId() { return 0l; }
				@Override
				public String getResourceableTypeName() { return "NewIdentityCreated"; }
			});
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());	
			contentCtr = new NewUsersNotificationsController(ureq, bwControl);
			listenTo(contentCtr);
			return contentCtr.getInitialComponent();
		}
		else if (uobject.equals("noauthentication")) {
			activatePaneInDetailView = null;
			String[] auth = {null};
			contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), null, null, auth, null, null, Identity.STATUS_VISIBLE_LIMIT, true);
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
	private Component createAndLockDirectUserDeleteController(UserRequest ureq) {
		Controller lockCtrl = acquireDeleteUserLock(ureq);
		if (lockCtrl == null) {
			//success -> create new User deletion workflow
			removeAsListenerAndDispose(contentCtr);
			contentCtr = new DirectDeleteController(ureq, getWindowControl());
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
	private Component createAndLockUserDeleteController(UserRequest ureq) {
		Controller lockCtrl = acquireDeleteUserLock(ureq);

		if (lockCtrl == null) {
			//success -> create new User deletion workflow
			activatePaneInDetailView = null;
			removeAsListenerAndDispose(contentCtr);
			contentCtr = new TabbedPaneController(ureq, getWindowControl());
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
			String text = getTranslator().translate("error.deleteworkflow.locked.by", new String[]{lock.getOwner().getName()});
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

	private TreeModel buildTreeModel(boolean isOlatAdmin) {
		GenericTreeNode gtnChild, admin;
		Translator translator = getTranslator();
		
		GenericTreeModel gtm = new GenericTreeModel();
		admin = new GenericTreeNode();		
		admin.setTitle(translator.translate("menu.useradmin"));
		admin.setUserObject("useradmin");
		admin.setAltText(translator.translate("menu.useradmin.alt"));
		gtm.setRootNode(admin);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.usearch"));
		gtnChild.setUserObject("usearch");
		gtnChild.setAltText(translator.translate("menu.usearch.alt"));
		admin.setDelegate(gtnChild);
		admin.addChild(gtnChild);

		Boolean canCreate = BaseSecurityModule.USERMANAGER_CAN_CREATE_USER;
		if (canCreate.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.ucreate"));
			gtnChild.setUserObject("ucreate");
			gtnChild.setAltText(translator.translate("menu.ucreate.alt"));
			admin.addChild(gtnChild);
			
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.usersimport"));
			gtnChild.setUserObject("usersimport");
			gtnChild.setAltText(translator.translate("menu.usersimport.alt"));
			admin.addChild(gtnChild);
		}
		Boolean canDelete = BaseSecurityModule.USERMANAGER_CAN_DELETE_USER;
		if (canDelete.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.userdelete"));
			gtnChild.setUserObject("userdelete");
			gtnChild.setAltText(translator.translate("menu.userdelete.alt"));
			admin.addChild(gtnChild);

			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.userdelete.direct"));
			gtnChild.setUserObject("userdelete_direct");
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

		Boolean canGroupmanagers = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS;
		if (canGroupmanagers.booleanValue() || isOlatAdmin) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.groupmanagergroup"));
			gtnChild.setUserObject("groupmanagergroup");
			gtnChild.setAltText(translator.translate("menu.groupmanagergroup.alt"));
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
			ActionExtension ae = (ActionExtension) anExt.getExtensionFor(UserAdminMainController.class.getName() + EXTENSIONPOINT_MENU_MENUQUERIES);
			if (ae != null && anExt.isEnabled()) {
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
	
	public void activate(UserRequest ureq, String viewIdentifier) {
		if(viewIdentifier.startsWith("notifications") || viewIdentifier.startsWith("NewIdentityCreated")) {
			GenericTreeModel tm = (GenericTreeModel)olatMenuTree.getTreeModel();
			TreeNode node = tm.findNodeByUserObject("created.newUsersNotification");
			olatMenuTree.setSelectedNode(node);
			Component resComp = initComponentFromMenuCommand("created.newUsersNotification", ureq);
			content.setContent(resComp);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers disposed in BasicController
		releaseDeleteUserLock();
	}

}
