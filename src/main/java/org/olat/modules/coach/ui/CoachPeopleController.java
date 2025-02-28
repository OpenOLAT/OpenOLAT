/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserCreateController;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.olat.user.ui.admin.UserSearchTableController;
import org.olat.user.ui.admin.UserSearchTableSettings;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoachPeopleController extends BasicController implements Activateable2 {

	private static final String MAIN_CONTROLLER = "component";
	
	private static final String COACH_SCOPE = "coach";
	private static final String PRINCIPAL_SCOPE = "principal";
	private static final String LINE_MANAGER_SCOPE = "linemanager";
	private static final String EDU_MANAGER_SCOPE = "edumanager";
	private static final String RELATION_PREFIX_SCOPE = "rr_";
	
	private final VelocityContainer mainVC;
	private Link activatePendingAccountsButton;
	private Link createAccountButton;
	private final TooledStackedPanel content;
	private final ScopeSelection searchScopes;
	private final List<Scope> scopes;

	private List<RelationRole> userRelationRoles;
	
	private StudentListController studentListCtrl;
	private OrganisationListController organisationListCtrl;
	private UserRelationListController userRelationsListController;
	private UserSearchTableController userSearchTableCtrl;
	private UserCreateController userCreateCtrl;
	
	@Autowired
	private IdentityRelationshipService identityRelationsService;
	
	@Autowired
	private OrganisationService organisationService;

	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;

	public CoachPeopleController(UserRequest ureq, WindowControl wControl, TooledStackedPanel content, CoachingSecurity coachingSec) {
		super(ureq, wControl);
		this.content = content;
		
		mainVC = createVelocityContainer("people");
		
		addActivatePendingAccountsButton(ureq);
		addCreateAccountButton(ureq);
		
		// As coach
		scopes = new ArrayList<>(4);
		if(coachingSec.isCoach()) {
			scopes.add(ScopeFactory.createScope(COACH_SCOPE, translate("lectures.teacher.menu.title"), null, "o_icon o_icon_coaching_tool"));
		}
		
		Roles roles = ureq.getUserSession().getRoles();
		if(roles.isPrincipal()) {
			scopes.add(ScopeFactory.createScope(PRINCIPAL_SCOPE, translate("coaching.as.principal"), null, "o_icon o_icon_eye"));
		}
		
		// Relations user to user
		userRelationRoles = listAvailableRoles(identityRelationsService.getRelationsAsSource(ureq.getIdentity()));
		for (RelationRole relationRole : userRelationRoles) {
			String name = RelationRolesAndRightsUIFactory.getTranslatedContraRole(relationRole, getLocale());
			String scopeName = translate("relation.as", name);
			scopes.add(ScopeFactory.createScope(RELATION_PREFIX_SCOPE + relationRole.getRole(), scopeName, null, "o_icon o_icon_right_left"));
		}
		
		if(roles.isLineManager()) {
			scopes.add(ScopeFactory.createScope(LINE_MANAGER_SCOPE, translate("line.manager.title"), null, "o_icon o_icon_manager"));
		}
		if(roles.isEducationManager()) {
			scopes.add(ScopeFactory.createScope(EDU_MANAGER_SCOPE, translate("education.manager.title"), null, "o_icon o_icon_manager"));
		}
		
		searchScopes = ScopeFactory.createScopeSelection("search.scopes", mainVC, this, scopes);

		putInitialPanel(mainVC);
		openFirstScope(ureq);
	}

	private void openFirstScope(UserRequest ureq) {
		cleanUp();

		if (!scopes.isEmpty()) {
			Scope firstScope = scopes.get(0);
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(firstScope.getKey());
			activate(ureq, entries, null);
		}
	}

	private void addActivatePendingAccountsButton(UserRequest ureq) {
		activatePendingAccountsButton = LinkFactory.createButton("activate.pending.accounts", mainVC, this);
		activatePendingAccountsButton.setVisible(false);
		mainVC.put("activate.pending.accounts", activatePendingAccountsButton);

		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isEducationManager()) {
			List<Organisation> orgs = organisationService.getOrganisations(getIdentity(), OrganisationRoles.educationmanager);
			if (orgs.isEmpty()) {
				return;
			}
			Organisation org = orgs.get(0);
			RoleSecurityCallback roleSecurityCallback = RoleSecurityCallbackFactory.create(
					organisationService.getGrantedOrganisationRights(org, OrganisationRoles.educationmanager));
			if (!roleSecurityCallback.canActivatePendingAccounts()) {
				return;
			}
			
			SearchIdentityParams params = new SearchIdentityParams();
			params.setOrganisations(orgs);
			params.setUserPropertiesAsIntersectionSearch(true);
			params.setExactStatusList(List.of(Identity.STATUS_PENDING));
			if (identityPowerSearchQueries.countIdentitiesByPowerSearch(params) == 0) {
				return;
			}

			activatePendingAccountsButton.setVisible(true);
		}
	}

	private void addCreateAccountButton(UserRequest ureq) {
		createAccountButton = LinkFactory.createButton("create.account", mainVC, this);
		createAccountButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		createAccountButton.setVisible(false);
		mainVC.put("create.account", createAccountButton);

		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isEducationManager()) {
			List<Organisation> orgs = organisationService.getOrganisations(getIdentity(), OrganisationRoles.educationmanager);
			if (orgs.isEmpty()) {
				return;
			}
			Organisation org = orgs.get(0);
			RoleSecurityCallback roleSecurityCallback = RoleSecurityCallbackFactory.create(
					organisationService.getGrantedOrganisationRights(org, OrganisationRoles.educationmanager));
			if (!roleSecurityCallback.canCreateAccounts()) {
				return;
			}
			createAccountButton.setVisible(true);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(COACH_SCOPE.equalsIgnoreCase(type)) {
			doOpenAsCoach(ureq);
			searchScopes.setSelectedKey(COACH_SCOPE);
		} else if(PRINCIPAL_SCOPE.equalsIgnoreCase(type)) {
			doOrganisationsWithRole(ureq, OrganisationRoles.principal);
			searchScopes.setSelectedKey(PRINCIPAL_SCOPE);
		} else if(LINE_MANAGER_SCOPE.equalsIgnoreCase(type)) {
			doOrganisationsWithRole(ureq, OrganisationRoles.linemanager);
			searchScopes.setSelectedKey(LINE_MANAGER_SCOPE);
		} else if(EDU_MANAGER_SCOPE.equals(type)) {
			doOrganisationsWithRole(ureq, OrganisationRoles.educationmanager);
			searchScopes.setSelectedKey(EDU_MANAGER_SCOPE);
		} else {
			for(RelationRole role:userRelationRoles) {
				if((RELATION_PREFIX_SCOPE + role.getRole()).equalsIgnoreCase(type)) {
					doOpenRelation(ureq, role);
					searchScopes.setSelectedKey(RELATION_PREFIX_SCOPE + role.getRole());
					break;
				}
			}
		}
	}



	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(searchScopes == source) {
			if (event instanceof ScopeEvent se) {
				doOpen(ureq, se.getSelectedKey());
			}
		} else if (activatePendingAccountsButton == source) {
			doActivatePendingAccounts(ureq);
		} else if (createAccountButton == source) {
			doCreateAccount(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (userCreateCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				openFirstScope(ureq);
			}
		}
		
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(userRelationsListController);
		removeAsListenerAndDispose(organisationListCtrl);
		removeAsListenerAndDispose(studentListCtrl);
		removeAsListenerAndDispose(userCreateCtrl);
		removeAsListenerAndDispose(userSearchTableCtrl);
		userRelationsListController = null;
		organisationListCtrl = null;
		studentListCtrl = null;
		userCreateCtrl = null;
		userSearchTableCtrl = null;
	}
	
	private void doOpen(UserRequest ureq, String scope) {
		if(COACH_SCOPE.equalsIgnoreCase(scope)) {
			doOpenAsCoach(ureq);
		} else if(PRINCIPAL_SCOPE.equalsIgnoreCase(scope)) {
			doOrganisationsWithRole(ureq, OrganisationRoles.principal);
		} else if(LINE_MANAGER_SCOPE.equalsIgnoreCase(scope)) {
			doOrganisationsWithRole(ureq, OrganisationRoles.linemanager);
		} else if(EDU_MANAGER_SCOPE.equals(scope)) {
			doOrganisationsWithRole(ureq, OrganisationRoles.educationmanager);
		} else if(scope.startsWith(RELATION_PREFIX_SCOPE)) {
			RelationRole relationRole = userRelationRoles.stream()
					.filter(rel -> scope.equalsIgnoreCase(RELATION_PREFIX_SCOPE + rel.getRole()))
					.findFirst().orElse(null);			
			doOpenRelation(ureq, relationRole);
		}
	}
	
	private void doOpenAsCoach(UserRequest ureq) {
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("AsCoach", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		studentListCtrl = new StudentListController(ureq, bwControl, content);
		listenTo(studentListCtrl);
		mainVC.put(MAIN_CONTROLLER, studentListCtrl.getInitialComponent());
	}
	
	private void doOpenRelation(UserRequest ureq, RelationRole relationRole) {
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Relations", relationRole.getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		userRelationsListController = new UserRelationListController(ureq, bwControl, content, relationRole);
		listenTo(userRelationsListController);
		mainVC.put(MAIN_CONTROLLER, userRelationsListController.getInitialComponent());
	}
	
	private void doOrganisationsWithRole(UserRequest ureq, OrganisationRoles role) {
		cleanUp();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(role.name(), 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		organisationListCtrl = new OrganisationListController(ureq, bwControl, content, role);
		listenTo(organisationListCtrl);
		mainVC.put(MAIN_CONTROLLER, organisationListCtrl.getInitialComponent());
	}

	private void doActivatePendingAccounts(UserRequest ureq) {
		cleanUp();
		
		UserSearchTableSettings userSearchTableSettings = UserSearchTableSettings.minimal();
		userSearchTableSettings.setBulkStatus(true);
		userSearchTableCtrl = new UserSearchTableController(ureq, getWindowControl(), content, userSearchTableSettings);
		SearchIdentityParams searchIdentityParams = new SearchIdentityParams();
		searchIdentityParams.setUserPropertiesAsIntersectionSearch(true);
		searchIdentityParams.setExactStatusList(List.of(Identity.STATUS_PENDING));
		searchIdentityParams.setOrganisations(
				organisationService.getOrganisations(getIdentity(), OrganisationRoles.educationmanager));
		userSearchTableCtrl.loadModel(searchIdentityParams);
		listenTo(userSearchTableCtrl);
		mainVC.put(MAIN_CONTROLLER, userSearchTableCtrl.getInitialComponent());
	}

	private void doCreateAccount(UserRequest ureq) {
		cleanUp();
		
		List<Organisation> orgs = organisationService.getOrganisations(getIdentity(), OrganisationRoles.educationmanager);
		if (orgs.isEmpty()) {
			return;
		}
		Organisation org = orgs.get(0);
		userCreateCtrl = new UserCreateController(ureq, getWindowControl(), org, true);
		listenTo(userCreateCtrl);
		mainVC.put(MAIN_CONTROLLER, userCreateCtrl.getInitialComponent());
	}
	
	/**
	 * Returns different roles for a given list of relations
	 *
	 * @param relations
	 * @return
	 */
	private List<RelationRole> listAvailableRoles(List<IdentityToIdentityRelation> relations) {
		List<RelationRole> relationRoles = new ArrayList<>();
		Set<String> roles = new HashSet<>();
		for (IdentityToIdentityRelation relation : relations) {
			// Prevent double entries
			if (!roles.contains(relation.getRole().getRole())) {
				relationRoles.add(relation.getRole());
				roles.add(relation.getRole().getRole());
			}
		}
		return relationRoles;
	}
}
