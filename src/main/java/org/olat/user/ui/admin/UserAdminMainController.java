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

package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.DeletedUsersController;
import org.olat.admin.user.NewUsersNotificationsController;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UserCreateController;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.admin.user.delete.DirectDeleteController;
import org.olat.admin.user.delete.TabbedPaneController;
import org.olat.admin.user.imp.UserImportController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserManager;
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
	public static final String EXTENSIONPOINT_MENU_MENUQUERIES = UserAdminMainController.class.getName() + ".menu.menuqueries";
	
	private Link createLink;
	private Link importLink;
	private Link deleteLink;
	private Link deleteDirectLink;
	private MenuTree menuTree;
	private TooledStackedPanel content;
	
	private Controller contentCtr;
	private UserAdminController editCtrl;
	private UserCreateController createCtrl;
	private UserImportController importCtrl;
	private TabbedPaneController deleteCtrl;
	private DirectDeleteController directDeleteCtrl;
	private LayoutMain3ColsController columnLayoutCtr;

	private final Roles identityRoles;
	private final List<Organisation> manageableOrganisations;

	private LockResult lock;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;

	/**
	 * Constructor of the home main controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 */
	public UserAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		UserSession usess = ureq.getUserSession();
		identityRoles = usess.getRoles();
		
		if(identityRoles.isSystemAdmin()) {
			manageableOrganisations = organisationService.getOrganisations();
		} else {
			manageableOrganisations = organisationService.getOrganisations(getIdentity(), usess.getRoles(),
					OrganisationRoles.administrator, OrganisationRoles.principal,
					OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
		}

		menuTree = new MenuTree("olatMenuTree");
		menuTree.setExpandSelectedNode(false);
		menuTree.setScrollTopOnClick(true);
		menuTree.setRootVisible(false);
		TreeModel tm = buildTreeModel(ureq); 
		menuTree.setTreeModel(tm);
		TreeNode firstNode = (TreeNode)tm.getRootNode().getChildAt(0);
		menuTree.setSelectedNodeId(firstNode.getIdent());
		menuTree.addListener(this);
		// allow closing of active menu tree element
		menuTree.setExpandSelectedNode(false);
		
		content = new TooledStackedPanel("user-admin-stack", getTranslator(), this);
		content.setInvisibleCrumb(0);
		content.setNeverDisposeRootController(true);

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, content, "useradminmain");
		columnLayoutCtr.addCssClassToMain("o_useradmin");
		
		listenTo(columnLayoutCtr); // auto dispose later
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	private void initTools() {
		if (identityRoles.isAdministrator() || identityRoles.isSystemAdmin() || identityRoles.isUserManager() || identityRoles.isRolesManager()) {
			createLink = LinkFactory.createToolLink("ucreate", translate("menu.ucreate"), this, "o_icon_add_member");
			createLink.setElementCssClass("o_sel_useradmin_create");
			content.addTool(createLink, Align.right);
		}

		if (identityRoles.isAdministrator() || identityRoles.isUserManager() || identityRoles.isRolesManager()) {
			importLink = LinkFactory.createToolLink("usersimport", translate("menu.usersimport"), this, "o_icon_import");
			importLink.setElementCssClass("o_sel_useradmin_import");
			content.addTool(importLink, Align.right);
		}

		if (identityRoles.isAdministrator() || ((identityRoles.isUserManager() || identityRoles.isRolesManager()) && BaseSecurityModule.USERMANAGER_CAN_DELETE_USER.booleanValue())) {
			deleteLink = LinkFactory.createToolLink("userdelete", translate("menu.userdelete"), this, "o_icon_delete");
			deleteLink.setElementCssClass("o_sel_useradmin_delete");
			content.addTool(deleteLink, Align.right);

			deleteDirectLink = LinkFactory.createToolLink("userdelete_direct", translate("menu.userdelete.direct"), this, "o_icon_delete");
			deleteDirectLink.setElementCssClass("o_sel_useradmin_direct_delete");
			content.addTool(deleteDirectLink, Align.right);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();
				contentCtr = pushController(ureq, selTreeNode);
			} else { // the action was not allowed anymore
				content.popUpToRootController(ureq);
			}
		} else if(source == content) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == editCtrl) {
					
				}
			}
		} else if(createLink == source) {
			doCreateUser(ureq);
		} else if(importLink == source) {
			doImportUser(ureq);
		} else if(deleteLink == source) {
			doUserDelete(ureq);
		} else if(deleteDirectLink == source) {
			doUserDirectDelete(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(createCtrl == source) {
			if(event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent sice = (SingleIdentityChosenEvent)event;
				doEditCreatedUser(ureq, sice.getChosenIdentity());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doCreateUser(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		boolean canCreateOLATPassword = roles.isAdministrator() || roles.isSystemAdmin() || roles.isRolesManager() || roles.isUserManager();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Create", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		createCtrl = new UserCreateController(ureq, bwControl, canCreateOLATPassword);
		listenTo(createCtrl);
		content.rootController(translate("menu.ucreate"), createCtrl);
		menuTree.setSelectedNode(null);
	}
	
	private void doEditCreatedUser(UserRequest ureq, Identity newIdentity) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Create", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		editCtrl = new UserAdminController(ureq, bwControl, content, newIdentity);
		editCtrl.setBackButtonEnabled(false);
		editCtrl.setShowTitle(false);
		listenTo(editCtrl);
		content.rootController(translate("menu.ucreate"), editCtrl);
		menuTree.setSelectedNode(null);
	}
	
	private void doImportUser(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		boolean canCreateOLATPassword = roles.isAdministrator() || roles.isRolesManager() || roles.isUserManager();

		importCtrl = new UserImportController(ureq, getWindowControl(), canCreateOLATPassword);
		addToHistory(ureq, importCtrl);
		listenTo(importCtrl);
		content.rootController(translate("menu.usersimport"), importCtrl);
		menuTree.setSelectedNode(null);
	}
	
	private Controller pushController(UserRequest ureq, TreeNode treeNode) {
		releaseDeleteUserLock();
		
		Controller ctrl = null;
		Object uobject = treeNode.getUserObject();
		if (uobject instanceof GenericActionExtension) {
			ctrl = getController(ureq, (GenericActionExtension)uobject);
		} else if(uobject instanceof String) {
			ctrl = getController(ureq, (String)uobject);
		} else if(uobject instanceof Organisation) {
			ctrl = getController(ureq, (Organisation)uobject);
		}
		if(ctrl != null) {
			listenTo(ctrl);
			content.popUpToRootController(ureq);
			content.rootController(treeNode.getTitle(), ctrl);
			initTools();
		}
		return ctrl;
	}

	private Controller getController(UserRequest ureq, String uobject) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(uobject, 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		switch(uobject) {
			case "menuroles": return createInfoController(ureq, bwControl, "systemroles");
			case "menuqueries": return createInfoController(ureq, bwControl, "predefinedqueries");
			case "menuaccess": return createInfoController(ureq, bwControl, "systemroles");
			case "organisations": return createInfoController(ureq, bwControl, "systemorganisations");
			case "usearch":
			case "useradmin": return createUserSearchController(ureq, bwControl);
			case "admingroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.administrator);
			case "sysadmingroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.sysadmin);
			case "principalgroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.principal);
			case "usermanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.usermanager);
			case "rolesmanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.rolesmanager);
			case "groupmanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.groupmanager);
			case "learnresourcemanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.learnresourcemanager);
			case "linemanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.linemanager);
			case "lecturemanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.lecturemanager);
			case "qualitymanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.qualitymanager);
			case "curriculummanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.curriculummanager);
			case "poolmanagergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.poolmanager);
			case "authorgroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.author);
			case "usergroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.user);
			case "anonymousgroup": return createUserSearchController(ureq, bwControl, OrganisationRoles.guest);
			case "logondeniedgroup": return createUserSearchController(ureq, bwControl, Identity.STATUS_LOGIN_DENIED);
			case "deletedusers": return createDeletedUserController(ureq, bwControl);
			case "created.lastweek": return createUserSearchControllerAfterDate(ureq, bwControl, Calendar.DAY_OF_MONTH, -7);
			case "created.lastmonth": return createUserSearchControllerAfterDate(ureq, bwControl, Calendar.MONTH, -1);
			case "created.sixmonth": return createUserSearchControllerAfterDate(ureq, bwControl, Calendar.MONTH, -6);
			case "created.newUsersNotification": return new NewUsersNotificationsController(ureq, bwControl, content);
			// repository entry owners - authors
			case "coauthors": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(GroupRoles.owner, null, null, new OrganisationRoles[] { OrganisationRoles.author }, Identity.STATUS_VISIBLE_LIMIT));
			// authors + repository entry owners
			case "resourceowners": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.authorsAndCoAuthors());
			case "courseparticipants": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(GroupRoles.participant, null, null, null, Identity.STATUS_VISIBLE_LIMIT));
			case "coursecoach": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(GroupRoles.coach, null, null, null, Identity.STATUS_VISIBLE_LIMIT));
			case "groupcoach": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(null, GroupRoles.coach, null, null, Identity.STATUS_VISIBLE_LIMIT));
			case "noauthentication": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.authenticationProviders(new String[]{ null }, Identity.STATUS_VISIBLE_LIMIT));
			case "userswithoutgroup":
				return createUserSearchController(ureq, bwControl, SearchIdentityParams.withBusinesGroups());
			case "userswithoutemail":
				List<Identity> usersWithoutEmail = userManager.findVisibleIdentitiesWithoutEmail();
				return new UsermanagerUserSearchController(ureq, bwControl, content, usersWithoutEmail, true, true);
			case "usersemailduplicates":
				List<Identity> usersEmailDuplicates = userManager.findVisibleIdentitiesWithEmailDuplicates();
				return new UsermanagerUserSearchController(ureq, bwControl, content, usersEmailDuplicates, true, true);
			default: return null;		
		}
	}

	private Controller getController(UserRequest ureq, Organisation organisation) {
		SearchIdentityParams predefinedQuery = SearchIdentityParams.organisation(organisation, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, getWindowControl(), predefinedQuery);
	}
	
	private Controller createInfoController(UserRequest ureq, WindowControl bwControl, String template) {
		return new InfoController(ureq, bwControl, template);
	}

	private UsermanagerUserSearchController createUserSearchControllerAfterDate(UserRequest ureq, WindowControl bwControl, int unit, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(unit, amount);
		SearchIdentityParams predefinedQuery = SearchIdentityParams.params(cal.getTime(), null, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, bwControl, predefinedQuery);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, OrganisationRoles role) {
		final OrganisationRoles[] roles = { role };
		SearchIdentityParams predefinedQuery = SearchIdentityParams.params(roles, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, bwControl, predefinedQuery);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, Integer status) {
		SearchIdentityParams predefinedQuery = SearchIdentityParams.params(null, status);
		return createUserSearchController(ureq, bwControl, predefinedQuery);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl) {
		return new UsermanagerUserSearchController(ureq, bwControl, content, manageableOrganisations);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, SearchIdentityParams predefinedQuery) {
		if(manageableOrganisations != null) {
			List<OrganisationRef> allowedOrganisations = new ArrayList<>(manageableOrganisations);
			if(predefinedQuery.getOrganisations() != null) {
				allowedOrganisations.retainAll(predefinedQuery.getOrganisations());
			}
			predefinedQuery.setOrganisations(allowedOrganisations);
		}
		return new UsermanagerUserSearchController(ureq, bwControl, content, predefinedQuery, true);
	}
	
	private DeletedUsersController createDeletedUserController(UserRequest ureq, WindowControl bwControl) {
		return new DeletedUsersController(ureq, bwControl);
	}
	
	private Controller getController(UserRequest ureq, GenericActionExtension ae) {
		TreeNode node = ((GenericTreeModel)menuTree.getTreeModel()).findNodeByUserObject(ae);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("AE", Long.valueOf(node.getPosition()));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		return ae.createController(ureq, bwControl, content);
	}

	/**
	 * Creates a DirectDeleteController and acquire a 'delete-user-lock'.
	 * The lock is for both direct-deletion and workflow with email.
	 * @param ureq The user request
	 */
	private void doUserDirectDelete(UserRequest ureq) {
		Controller controller = acquireDeleteUserLock(ureq);
		if (controller == null) {
			//success -> create new User deletion workflow
			directDeleteCtrl = new DirectDeleteController(ureq, getWindowControl());
			controller = directDeleteCtrl;
			listenTo(controller);
		}

		content.popUpToRootController(ureq);
		content.rootController(translate("menu.userdelete.direct"), controller);
	}

	/**
	 * Creates a TabbedPaneController (delete workflow with email)  and acquire a 'delete-user-lock'.
	 * The lock is for both direct-deletion and workflow with email.
	 * @param ureq The user request
	 */
	private void doUserDelete(UserRequest ureq) {
		Controller controller = acquireDeleteUserLock(ureq);
		if (controller == null) {
			//success -> create new User deletion workflow
			deleteCtrl = new TabbedPaneController(ureq, getWindowControl());
			controller = deleteCtrl;
			listenTo(deleteCtrl);
		}
		content.popUpToRootController(ureq);
		content.rootController(translate("menu.userdelete"), controller);
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
			return MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
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
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();		
		root.setTitle(translate("menu.useradmin"));
		root.setUserObject("useradmin");
		root.setCssClass("o_sel_useradmin");
		root.setAltText(translate("menu.useradmin.alt"));
		gtm.setRootNode(root);
		appendNode("menu.usearch", "menu.usearch.alt", "usearch", "o_sel_useradmin_search", root);
		
		// Sub menu with organizations
		GenericTreeNode organisationsNode = appendNode("menu.organisations", "menu.organisations.alt", "organisations", "o_sel_useradmin_organisations", root);
		buildTreeOrganisationSubMenu(organisationsNode);
		// Sub menu access and rights
		GenericTreeNode accessNode = appendNode("menu.menuaccess", "menu.menuaccess.alt", "menuaccess", "o_sel_useradmin_access", root);
		buildTreeAccessSubMenu(accessNode);
		// Sub menu queries
		GenericTreeNode queriesNode = appendNode("menu.menuqueries", "menu.menuqueries.alt", "menuqueries", "o_sel_useradmin_menuqueries", root);
		buildTreeQueriesSubMenu(queriesNode);
		buildTreeExtensionsSubMenu(ureq, queriesNode);
		return gtm;
	}

	private void buildTreeOrganisationSubMenu(GenericTreeNode accessNode) {
		List<Organisation> organisations;
		if(manageableOrganisations != null) {
			organisations = new ArrayList<>();
			List<Organisation> allOrganisations = organisationService.getOrganisations();
			for(Organisation organisation:allOrganisations) {
				String path = organisation.getMaterializedPathKeys();
				for(Organisation userManagerOrganisation:manageableOrganisations) {
					if(path.startsWith(userManagerOrganisation.getMaterializedPathKeys())) {
						organisations.add(organisation);
					}
				}
			}
		} else {
			organisations = organisationService.getOrganisations();
		}
		
		Map<Long,Organisation> keytoOrganisations = new HashMap<>();
		for(Organisation organisation:organisations) {
			keytoOrganisations.put(organisation.getKey(), organisation);
		}

		Map<Long, GenericTreeNode> fieldKeyToNode = new HashMap<>();
		for(Organisation organisation:organisations) {
			Long key = organisation.getKey();
			GenericTreeNode node = fieldKeyToNode.computeIfAbsent(key, organisationKey -> 
				new GenericTreeNode(organisation.getDisplayName(), organisation));

			Organisation parentOrganisation = organisation.getParent();
			if(parentOrganisation == null || !keytoOrganisations.containsKey(parentOrganisation.getKey())) {
				//this is a root, or the user has not access to parent
				accessNode.addChild(node);
			} else {
				Long parentKey = parentOrganisation.getKey();
				GenericTreeNode parentNode = fieldKeyToNode.computeIfAbsent(parentKey, parentOrganisationKey -> {
					Organisation parent = keytoOrganisations.get(parentKey);//to use the fetched type
					return new GenericTreeNode(parent.getDisplayName(), parent);
				});
				parentNode.addChild(node);
			}
		}
	}
	
	private void buildTreeAccessSubMenu(GenericTreeNode accessNode) {
		appendNode("menu.usergroup", "menu.usergroup.alt", "usergroup", "o_sel_useradmin_usergroup", accessNode);
		
		boolean isAdministrator = identityRoles.isSystemAdmin() || identityRoles.isAdministrator()
				|| identityRoles.isPrincipal() || identityRoles.isUserManager() || identityRoles.isRolesManager();
		if (isAdministrator) {
			appendNode("menu.authorgroup", "menu.authorgroup.alt", "authorgroup", "o_sel_useradmin_authorgroup", accessNode);
			appendNode("menu.coauthors", "menu.coauthors.alt", "coauthors", "o_sel_useradmin_coauthors", accessNode);
			appendNode("menu.resourceowners", "menu.resourceowners.alt", "resourceowners", "o_sel_useradmin_resourceowners", accessNode);
		}
		
		appendNode("menu.coursecoach", "menu.coursecoach.alt", "coursecoach", "o_sel_useradmin_coursecoach", accessNode);
		appendNode("menu.courseparticipants", "menu.courseparticipants.alt", "courseparticipants", "o_sel_useradmin_courseparticipants", accessNode);

		if (isAdministrator) {
			appendNode("menu.groupmanagergroup", "menu.groupmanagergroup.alt", "groupmanagergroup", "o_sel_useradmin_groupmanagergroup", accessNode);
			appendNode("menu.groupcoach", "menu.groupcoach.alt", "groupcoach", "o_sel_useradmin_groupcoach", accessNode);
		}
		
		// admin group and user manager group always restricted to admins
		if (isAdministrator) {
			appendNode("menu.lecturemanagergroup", "menu.lecturemanagergroup.alt", "lecturemanagergroup", "o_sel_useradmin_lecturemanagergroup", accessNode);
			appendNode("menu.qualitymanagergroup", "menu.qualitymanagergroup.alt", "qualitymanagergroup", "o_sel_useradmin_qualitymanagergroup", accessNode);
			appendNode("menu.poolmanagergroup", "menu.poolmanagergroup.alt", "poolmanagergroup", "o_sel_useradmin_poolmanagergroup", accessNode);
			
			appendNode("menu.usermanagergroup", "menu.usermanagergroup.alt", "usermanagergroup", "o_sel_useradmin_usermanagergroup", accessNode);
			appendNode("menu.rolesmanagergroup", "menu.rolesmanagergroup.alt", "rolesmanagergroup", "o_sel_useradmin_rolesmanagergroup", accessNode);
			appendNode("menu.learnresourcemanagergroup", "menu.learnresourcemanagergroup.alt", "learnresourcemanagergroup", "o_sel_useradmin_learnresourcemanagergroup", accessNode);
			
			appendNode("menu.linemanagergroup", "menu.linemanagergroup.alt", "linemanagergroup", "o_sel_useradmin_linemanagergroup", accessNode);
			

			appendNode("menu.principalgroup", "menu.principalgroup.alt", "principalgroup", "o_sel_useradmin_principalgroup", accessNode);
			appendNode("menu.admingroup", "menu.admingroup.alt", "admingroup", "o_sel_useradmin_admingroup", accessNode);
			appendNode("menu.sysadmingroup", "menu.sysadmingroup.alt", "sysadmingroup", "o_sel_useradmin_sysadmingroup", accessNode);
		}
		
		if (identityRoles.isRolesManager() || identityRoles.isAdministrator() || identityRoles.isSystemAdmin()) {
			appendNode("menu.anonymousgroup", "menu.anonymousgroup.alt", "anonymousgroup", "o_sel_useradmin_anonymousgroup", accessNode);
		}
		
		appendNode("menu.noauthentication", "menu.noauthentication.alt", "noauthentication", "o_sel_useradmin_noauthentication", accessNode);
		appendNode("menu.logondeniedgroup", "menu.logondeniedgroup.alt", "logondeniedgroup", "o_sel_useradmin_logondeniedgroup", accessNode);
		appendNode("menu.deletedusers", "menu.deletedusers.alt", "deletedusers", "o_sel_useradmin_deletedusers", accessNode);
	}

	private void buildTreeQueriesSubMenu(GenericTreeNode queriesNode) {
		appendNode("menu.userswithoutgroup", "menu.userswithoutgroup.alt", "userswithoutgroup", "o_sel_useradmin_userswithoutgroup", queriesNode);
		if(identityRoles.isRolesManager() || identityRoles.isAdministrator() || identityRoles.isSystemAdmin()) {
			appendNode("menu.users.without.email", "menu.users.without.email.alt", "userswithoutemail", "o_sel_useradmin_userswithoutemail", queriesNode);
			appendNode("menu.users.email.duplicate", "menu.users.email.duplicate.alt", "usersemailduplicates", "o_sel_useradmin_usersemailduplicates", queriesNode);
		}
		appendNode("menu.created.lastweek", "menu.created.lastweek.alt", "created.lastweek", "o_sel_useradmin_createdlastweek", queriesNode);
		appendNode("menu.created.lastmonth", "menu.created.lastmonth.alt", "created.lastmonth", "o_sel_useradmin_createdlastmonth", queriesNode);
		appendNode("menu.created.sixmonth", "menu.created.sixmonth.alt", "created.sixmonth", "o_sel_useradmin_createdsixmonth", queriesNode);
		appendNode("menu.created.newUsersNotification", "menu.created.newUsersNotification.alt", "created.newUsersNotification", "o_sel_useradmin_creatednewusers", queriesNode);
	}

	/**
	 * Add extension menues as child items
	 * @param ureq The user request
	 * @param queriesNode The parent node
	 */
	private void buildTreeExtensionsSubMenu(UserRequest ureq, GenericTreeNode queriesNode) {
		ExtManager extm = ExtManager.getInstance();
		int cnt = extm.getExtensionCnt();
		for (int i = 0; i < cnt; i++) {
			Extension anExt = extm.getExtension(i);
			// 1) general menu extensions
			ExtensionElement ee = anExt.getExtensionFor(EXTENSIONPOINT_MENU_MENUQUERIES, ureq);
			if (ee instanceof GenericActionExtension && anExt.isEnabled()) {
				GenericActionExtension ae = (GenericActionExtension)ee;
				GenericTreeNode gtnChild = new GenericTreeNode();
				String menuText = ae.getActionText(getLocale());
				gtnChild.setTitle(menuText);
				gtnChild.setUserObject(ae);
				gtnChild.setAltText(ae.getDescription(getLocale()));
				queriesNode.addChild(gtnChild);
			} 
		}
	}
	
	private GenericTreeNode appendNode(String i18nTitle, String i18nTitleAlt, Object uobject, String cssClass, GenericTreeNode parent) {
		GenericTreeNode treeNode = new GenericTreeNode();		
		treeNode.setTitle(translate(i18nTitle));
		treeNode.setUserObject(uobject);
		treeNode.setCssClass(cssClass);
		treeNode.setAltText(translate(i18nTitleAlt));
		parent.addChild(treeNode);
		return treeNode;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		GenericTreeModel tm = (GenericTreeModel)menuTree.getTreeModel();
		if(entries != null && !entries.isEmpty()) {
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
					contentCtr = selectNode(ureq, childNode);
				}
			} else {
				TreeNode node = tm.findNodeByUserObject(entryPoint);
				if(node != null) {
					contentCtr = selectNode(ureq, node);
					if(contentCtr instanceof Activateable2) {
						entries = entries.subList(1, entries.size());
						((Activateable2)contentCtr).activate(ureq, entries, entry.getTransientState());
					}
				}
			}
		}
		
		if(contentCtr == null) {
			TreeNode node = tm.getRootNode();
			if(node.getChildCount() > 0) {
				node = (TreeNode)node.getChildAt(0);
			}
			contentCtr = selectNode(ureq, node);
		}
	}
	
	private Controller selectNode(UserRequest ureq, TreeNode node) {
		menuTree.setSelectedNode(node);
		return pushController(ureq, node);
	}

	@Override
	protected void doDispose() {
		// controllers disposed in BasicController
		releaseDeleteUserLock();
	}
}
