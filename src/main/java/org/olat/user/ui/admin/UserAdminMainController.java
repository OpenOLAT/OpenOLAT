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
import java.util.Collections;
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
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.RelationRole;
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
import org.olat.core.gui.components.tree.TreeEvent;
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
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.quality.QualityModule;
import org.olat.user.UserManager;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
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
	private LayoutMain3ColsController columnLayoutCtr;

	private final Roles identityRoles;
	private GenericTreeNode organisationsNode;
	private final List<Organisation> manageableOrganisations;

	private LockResult lock;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QualityModule qualityModule;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private QuestionPoolModule poolModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private IdentityRelationshipService relationshipService;

	/**
	 * Constructor of the home main controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 */
	public UserAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		UserSession usess = ureq.getUserSession();
		identityRoles = usess.getRoles();
		
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), usess.getRoles(),
				OrganisationRoles.administrator, OrganisationRoles.principal,
				OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);

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
		if (identityRoles.isAdministrator() || identityRoles.isUserManager() || identityRoles.isRolesManager()) {
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
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED) && event instanceof TreeEvent) {
				TreeNode selTreeNode = menuTree.getTreeModel().getNodeById(((TreeEvent)event).getNodeId());
				if(selTreeNode != null) {
					contentCtr = pushController(ureq, selTreeNode);
				}
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
		boolean canCreateOLATPassword = roles.isAdministrator() || roles.isRolesManager() || roles.isUserManager();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Create", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		Organisation preselectedOrganisation = getPreselectedOrganisation();
		createCtrl = new UserCreateController(ureq, bwControl, preselectedOrganisation, canCreateOLATPassword);
		listenTo(createCtrl);
		content.rootController(translate("menu.ucreate"), createCtrl);
		menuTree.setSelectedNode(null);
	}
	
	private Organisation getPreselectedOrganisation() {
		TreeNode selectedNode = menuTree.getSelectedNode();
		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof Organisation) {
			return (Organisation)uobject;
		}
		
		if(organisationsNode != null && organisationsNode.getChildCount() > 0) {
			TreeNode firstNode = (TreeNode)organisationsNode.getChildAt(0);
			Object fobject = firstNode.getUserObject();
			if(fobject instanceof Organisation) {
				return (Organisation)fobject;
			}
		}
		
		if(!manageableOrganisations.isEmpty()) {
			return manageableOrganisations.get(0);
		}
		return null;	
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

		Organisation preselectedOrganisation = getPreselectedOrganisation();
		UserImportController importCtrl = new UserImportController(ureq, getWindowControl(), preselectedOrganisation, canCreateOLATPassword);
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
		} else if(uobject instanceof OrganisationRoles) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(uobject.toString()), null);
			ctrl = createUserSearchController(ureq, bwControl, (OrganisationRoles)uobject);
		} else if(uobject instanceof CurriculumRoles) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(uobject.toString()), null);
			ctrl = createUserSearchController(ureq, bwControl, (CurriculumRoles)uobject);
		} else if(uobject instanceof String) {
			ctrl = getController(ureq, (String)uobject);
		} else if(uobject instanceof Organisation) {
			ctrl = getController(ureq, (Organisation)uobject);
		} else if(uobject instanceof IdentityRelation) {
			IdentityRelation rel = (IdentityRelation)uobject;
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Relation", rel.getRelationRole().getKey()), null);
			ctrl = createUserSearchController(ureq, bwControl, (IdentityRelation)uobject);
		} else if(uobject instanceof Presentation) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(treeNode.getIdent()), null);
			ctrl = createInfoController(ureq, bwControl, (Presentation)uobject);
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
			// mains
			case "usearch":
			case "useradmin": return createUserSearchController(ureq, bwControl);
			// groups
			case "groupcoach": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(null, true, GroupRoles.coach, null, null, null, Identity.STATUS_VISIBLE_LIMIT), false);
			case "groupparticipant": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(null, true, GroupRoles.participant, null, null, null, Identity.STATUS_VISIBLE_LIMIT), false);
			// resources
			case "coauthors": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(GroupRoles.owner, true, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT), false);
			case "courseparticipants": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(GroupRoles.participant, true, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT), false);
			case "coursecoach": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.resources(GroupRoles.coach, true, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT), false);
			// status
			case "pendinggroup": return createUserSearchController(ureq, bwControl, Identity.STATUS_PENDING);
			case "logondeniedgroup": return createUserSearchController(ureq, bwControl, Identity.STATUS_LOGIN_DENIED);
			case "deletedusers": return createDeletedUserController(ureq, bwControl);
			// predefined queries
			case "userswithoutgroup":
				return createUserSearchController(ureq, bwControl, SearchIdentityParams.withBusinesGroups(), false);
			case "userswithoutemail":
				List<Identity> usersWithoutEmail = userManager.findVisibleIdentitiesWithoutEmail();
				return new UsermanagerUserSearchController(ureq, bwControl, content, usersWithoutEmail, true, false);
			case "usersemailduplicates":
				List<Identity> usersEmailDuplicates = userManager.findVisibleIdentitiesWithEmailDuplicates();
				return new UsermanagerUserSearchController(ureq, bwControl, content, usersEmailDuplicates, true, false);
			case "noauthentication": return createUserSearchController(ureq, bwControl,
					SearchIdentityParams.authenticationProviders(new String[]{ null }, Identity.STATUS_VISIBLE_LIMIT), false);
			// time based predefined queries
			case "created.lastweek": return createUserSearchControllerAfterDate(ureq, bwControl, Calendar.DAY_OF_MONTH, -7);
			case "created.lastmonth": return createUserSearchControllerAfterDate(ureq, bwControl, Calendar.MONTH, -1);
			case "created.sixmonth": return createUserSearchControllerAfterDate(ureq, bwControl, Calendar.MONTH, -6);
			case "created.newUsersNotification": return new NewUsersNotificationsController(ureq, bwControl, content);
			default: return null;		
		}
	}

	private Controller getController(UserRequest ureq, Organisation organisation) {
		SearchIdentityParams predefinedQuery = SearchIdentityParams.organisation(organisation, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, getWindowControl(), predefinedQuery, true);
	}
	
	private Controller createInfoController(UserRequest ureq, WindowControl bwControl, Presentation template) {
		return new InfoController(ureq, bwControl, template.getTitleKey(), template.getDescriptionKey());
	}

	private UsermanagerUserSearchController createUserSearchControllerAfterDate(UserRequest ureq, WindowControl bwControl, int unit, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(unit, amount);
		SearchIdentityParams predefinedQuery = SearchIdentityParams.params(cal.getTime(), null, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, bwControl, predefinedQuery, false);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, OrganisationRoles role) {
		final OrganisationRoles[] roles = { role };
		SearchIdentityParams predefinedQuery = SearchIdentityParams.params(roles, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, bwControl, predefinedQuery, false);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, CurriculumRoles role) {
		SearchIdentityParams predefinedQuery = SearchIdentityParams.resources(null, true, null, role, null, null, Identity.STATUS_VISIBLE_LIMIT);
		return createUserSearchController(ureq, bwControl, predefinedQuery, false);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, Integer status) {
		SearchIdentityParams predefinedQuery = SearchIdentityParams.params(null, status);
		return createUserSearchController(ureq, bwControl, predefinedQuery, false);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl) {
		return new UsermanagerUserSearchController(ureq, bwControl, content, manageableOrganisations);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl,
			SearchIdentityParams predefinedQuery, boolean showOrganisationMove) {
		if(manageableOrganisations != null) {
			List<OrganisationRef> allowedOrganisations = new ArrayList<>(manageableOrganisations);
			if(predefinedQuery.getOrganisations() != null) {
				allowedOrganisations.retainAll(predefinedQuery.getOrganisations());
			}
			predefinedQuery.setOrganisations(allowedOrganisations);
		}
		return new UsermanagerUserSearchController(ureq, bwControl, content, predefinedQuery, true, showOrganisationMove);
	}
	
	private UsermanagerUserSearchController createUserSearchController(UserRequest ureq, WindowControl bwControl, IdentityRelation relation) {
		List<Identity> identities;
		if(relation.isContra()) {
			identities = relationshipService.getTargets(relation.getRelationRole());
		} else {
			identities = relationshipService.getSources(relation.getRelationRole());
		}
		return new UsermanagerUserSearchController(ureq, bwControl, content, identities, true, false);
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
			DirectDeleteController directDeleteCtrl = new DirectDeleteController(ureq, getWindowControl());
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
			TabbedPaneController deleteCtrl = new TabbedPaneController(ureq, getWindowControl());
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
		organisationsNode = appendNode("menu.organisations", "menu.organisations.alt",
				new Presentation("menu.organisations", "organisations.intro"), "o_sel_useradmin_organisations", root);
		buildTreeOrganisationSubMenu(organisationsNode);

		// Sub menu organizations roles
		GenericTreeNode organisationsRolesNode = appendNode("menu.organisations.roles", "menu.organisations.roles.alt",
				new Presentation("menu.organisations.roles", "menu.organisations.roles.intro"), "o_sel_useradmin_organisationsroles", root);
		buildTreeOrganisationsRoles(organisationsRolesNode);
		// Sub menu course roles
		GenericTreeNode resourcesRolesNode = appendNode("menu.resources.roles", "menu.resources.roles.alt",
				new Presentation("menu.resources.roles", "menu.resources.roles.intro"), "o_sel_useradmin_resourcesroles", root);
		buildTreeResourcesRoles(resourcesRolesNode);
		// Sub menu groups roles
		GenericTreeNode groupsRolesNode = appendNode("menu.groups.roles", "menu.groups.roles.alt",
				new Presentation("menu.groups.roles", "menu.groups.roles.intro"), "o_sel_useradmin_groupsroles", root);
		buildTreeBusinessGroupsRoles(groupsRolesNode);
		//Sub menu curriculum roles
		if(curriculumModule.isEnabled()) {
			GenericTreeNode curriculumsRolesNode = appendNode("menu.curriculums.roles", "menu.curriculums.roles.alt",
					new Presentation("menu.curriculums.roles", "menu.curriculums.roles.intro"), "o_sel_useradmin_curriculumsroles", root);
			buildTreeCurriculumsRoles(curriculumsRolesNode);
		}
		// Sub menu identity to identity relations
		if(securityModule.isRelationRoleEnabled()) {
			List<RelationRole> roles = relationshipService.getAvailableRoles();
			if(!roles.isEmpty()) {
			
				GenericTreeNode relationsNode = appendNode("menu.relations", "menu.relations.alt",
						new Presentation("menu.relations", "menu.relations.intro"), "o_sel_useradmin_relations", root);
				buildTreeRelationsSubMenu(relationsNode, roles);
			}
		}
		// Sub menu status
		GenericTreeNode statusNode = appendNode("menu.status", "menu.status.alt",
				new Presentation("menu.status", "menu.status.intro"), "o_sel_useradmin_status", root);
		buildTreeStatusSubMenu(statusNode);
		// Sub menu queries
		GenericTreeNode queriesNode = appendNode("menu.menuqueries", "menu.menuqueries.alt",
				new Presentation("menu.menuqueries", "queries.intro"), "o_sel_useradmin_menuqueries", root);
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
		
		Collections.sort(organisations, new OrganisationNameComparator(getLocale()));
		
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

	private void buildTreeOrganisationsRoles(GenericTreeNode accessNode) {
		boolean isAdministrator = identityRoles.isAdministrator()
				|| identityRoles.isPrincipal() || identityRoles.isUserManager() || identityRoles.isRolesManager();
		// admin group and user manager group always restricted to admins
		if (isAdministrator) {
			buildTreeNodeRole(accessNode, OrganisationRoles.user);
			buildTreeNodeRole(accessNode, OrganisationRoles.author);
			buildTreeNodeRole(accessNode, OrganisationRoles.groupmanager);
			if(lectureModule.isEnabled()) {
				buildTreeNodeRole(accessNode, OrganisationRoles.lecturemanager);
			}
			if(qualityModule.isEnabled()) {
				buildTreeNodeRole(accessNode, OrganisationRoles.qualitymanager);
			}
			if(poolModule.isEnabled()) {
				buildTreeNodeRole(accessNode, OrganisationRoles.poolmanager);
			}
			buildTreeNodeRole(accessNode, OrganisationRoles.usermanager);
			buildTreeNodeRole(accessNode, OrganisationRoles.rolesmanager);
			if(curriculumModule.isEnabled()) {
				buildTreeNodeRole(accessNode, OrganisationRoles.curriculummanager);
			}
			buildTreeNodeRole(accessNode, OrganisationRoles.learnresourcemanager);
			buildTreeNodeRole(accessNode, OrganisationRoles.linemanager);
			buildTreeNodeRole(accessNode, OrganisationRoles.principal);
			buildTreeNodeRole(accessNode, OrganisationRoles.administrator);
			buildTreeNodeRole(accessNode, OrganisationRoles.sysadmin);
		}
		
		if (identityRoles.isRolesManager() || identityRoles.isAdministrator()) {
			buildTreeNodeRole(accessNode, OrganisationRoles.invitee);
		}
	}
	
	private void buildTreeNodeRole(GenericTreeNode accessNode, Enum<?> role) {
		String i18n = "menu." + role.name() + "group";
		appendNode(i18n, i18n, role, "o_sel_useradmin_".concat(role.name()), accessNode);
	}
	
	private void buildTreeResourcesRoles(GenericTreeNode accessNode) {
		appendNode("menu.coauthors", "menu.coauthors.alt", "coauthors", "o_sel_useradmin_coauthors", accessNode);
		appendNode("menu.coursecoach", "menu.coursecoach.alt", "coursecoach", "o_sel_useradmin_coursecoach", accessNode);
		appendNode("menu.courseparticipants", "menu.courseparticipants.alt", "courseparticipants", "o_sel_useradmin_courseparticipants", accessNode);
	}

	private void buildTreeBusinessGroupsRoles(GenericTreeNode accessNode) {
		appendNode("menu.groupcoach", "menu.groupcoach.alt", "groupcoach", "o_sel_useradmin_groupcoach", accessNode);
		appendNode("menu.groupparticipant", "menu.groupparticipant.alt", "groupparticipant", "o_sel_useradmin_groupparticipant", accessNode);
	}

	private void buildTreeCurriculumsRoles(GenericTreeNode accessNode) {
		buildTreeNodeRole(accessNode, CurriculumRoles.curriculumowner);
		buildTreeNodeRole(accessNode, CurriculumRoles.curriculumelementowner);
		buildTreeNodeRole(accessNode, CurriculumRoles.mastercoach);
		buildTreeNodeRole(accessNode, CurriculumRoles.owner);
		buildTreeNodeRole(accessNode, CurriculumRoles.coach);
		buildTreeNodeRole(accessNode, CurriculumRoles.participant);
	}
	
	private void buildTreeRelationsSubMenu(GenericTreeNode relationsNode, List<RelationRole> roles ) {
		for(RelationRole relationRole:roles) {
			String relationName = RelationRolesAndRightsUIFactory
					.getTranslatedRole(relationRole, getLocale());
			GenericTreeNode treeNode = new GenericTreeNode();		
			treeNode.setTitle(relationName);
			treeNode.setUserObject(new IdentityRelation(relationRole, false));
			relationsNode.addChild(treeNode);
			
			String contraRelationName = RelationRolesAndRightsUIFactory
					.getTranslatedContraRole(relationRole, getLocale());
			GenericTreeNode contraTreeNode = new GenericTreeNode();		
			contraTreeNode.setTitle(contraRelationName);
			contraTreeNode.setUserObject(new IdentityRelation(relationRole, true));
			relationsNode.addChild(contraTreeNode);
		}
	}
	
	private void buildTreeStatusSubMenu(GenericTreeNode accessNode) {
		appendNode("menu.pendinggroup", "menu.pendinggroup.alt", "pendinggroup", "o_sel_useradmin_pendinggroup", accessNode);
		appendNode("menu.logondeniedgroup", "menu.logondeniedgroup.alt", "logondeniedgroup", "o_sel_useradmin_logondeniedgroup", accessNode);
		appendNode("menu.deletedusers", "menu.deletedusers.alt", "deletedusers", "o_sel_useradmin_deletedusers", accessNode);
	}

	private void buildTreeQueriesSubMenu(GenericTreeNode queriesNode) {
		appendNode("menu.userswithoutgroup", "menu.userswithoutgroup.alt", "userswithoutgroup", "o_sel_useradmin_userswithoutgroup", queriesNode);
		if(identityRoles.isRolesManager() || identityRoles.isAdministrator()) {
			appendNode("menu.users.without.email", "menu.users.without.email.alt", "userswithoutemail", "o_sel_useradmin_userswithoutemail", queriesNode);
			appendNode("menu.users.email.duplicate", "menu.users.email.duplicate.alt", "usersemailduplicates", "o_sel_useradmin_usersemailduplicates", queriesNode);
		}
		appendNode("menu.noauthentication", "menu.noauthentication.alt", "noauthentication", "o_sel_useradmin_noauthentication", queriesNode);
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
	
	private static class Presentation {
		private final String titleKey;
		private final String descriptionKey;
		
		public Presentation(String titleKey, String descriptionKey) {
			this.titleKey = titleKey;
			this.descriptionKey = descriptionKey;
		}

		public String getTitleKey() {
			return titleKey;
		}

		public String getDescriptionKey() {
			return descriptionKey;
		}
	}
	
	private static class IdentityRelation {
		private final boolean contra;
		private final RelationRole relationRole;
		
		public IdentityRelation(RelationRole relationRole, boolean contra) {
			this.contra = contra;
			this.relationRole = relationRole;
		}
		
		public boolean isContra() {
			return contra;
		}
		
		public RelationRole getRelationRole() {
			return relationRole;
		}
	}
}
