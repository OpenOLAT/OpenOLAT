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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.admin.user.SelectOrganisationController;
import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.basesecurity.model.SearchMemberParameters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.olat.user.ui.organisation.structure.OrgStructureComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jan 27, 2006
 * @author gnaegi
 * <pre>
 * Description:
 * Controller that is used to manipulate the users system roles and rights. When calling
 * this controller make sure the user who calls the controller meets the following 
 * criterias:
 * - user is system administrator
 * or
 * - user tries not to modify a system administrator or user administrator
 * - user tries not to modify an author if author rights are not enabled for user managers
 * - user tries not to modify a group manager if group manager rights are not enabled for user managers 
 * - user tries not to modify a guest if guest rights are not enabled for user managers 
 *
 * Usually this controller is called by the UserAdminController that takes care of all this. 
 * There should be no need to use it anywhere else.
 */
public class UserRolesController extends FormBasicController {

	private final static Set<String> GLOBAL_ROLE_NAMES = Set.of(
			OrganisationRoles.sysadmin.name(),
			OrganisationRoles.groupmanager.name(),
			OrganisationRoles.poolmanager.name());
	private final static OrganisationRoles[] ORDERED_ROLES = {
			OrganisationRoles.author,
			OrganisationRoles.learnresourcemanager,
			OrganisationRoles.linemanager,
			OrganisationRoles.educationmanager,
			OrganisationRoles.principal,
			OrganisationRoles.lecturemanager,
			OrganisationRoles.curriculummanager,
			OrganisationRoles.projectmanager,
			OrganisationRoles.qualitymanager,
			OrganisationRoles.usermanager,
			OrganisationRoles.rolesmanager,
			OrganisationRoles.administrator,
			OrganisationRoles.groupmanager,
			OrganisationRoles.poolmanager,
			OrganisationRoles.sysadmin
	};

	private FormLayoutContainer simpleRolesCont;
	private FormLayoutContainer rolesCont;
	private FormLink addToOrganisationButton;
	private ObjectSelectionElement affiliationSelectorEl;
	private OrgStructureComponent affiliationTreeComp;
	private List<RolesElement> rolesEls = new ArrayList<>(1);

	private CloseableModalController cmc;
	private SelectOrganisationController selectOrganisationCtrl;

	private Roles editedRoles;
	private final Roles managerRoles;
	private final Identity editedIdentity;
	private final List<Organisation> manageableOrganisations;
	private Set<Organisation> organisations;
	private List<Organisation> affOrganisations;
	private int counter = 0;

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OrganisationModule organisationModule;

	/**
	 * Constructor for a controller that lets you edit the users system roles and rights.
	 * @param wControl
	 * @param ureq
	 * @param identity identity to be edited
	 */
	public UserRolesController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(UserAdminController.class, getLocale(), getTranslator()));
		this.editedIdentity = identity;

		editedRoles = securityManager.getRoles(editedIdentity, false);
		organisations = editedRoles.getOrganisations().stream()
				.map(organisationService::getOrganisation)
				.collect(Collectors.toSet());

		managerRoles = ureq.getUserSession().getRoles();
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), managerRoles,
				OrganisationRoles.administrator, OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);

		initForm(ureq);
		if (organisationModule.isEnabled()) {
			update();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (organisationModule.isEnabled()) {
			// Group: Affiliation
			FormLayoutContainer affiliationCont = FormLayoutContainer.createCustomFormLayout("affiliationCont", getTranslator(), this.velocity_root + "/aff_org.html");
			affiliationCont.setFormTitle(translate("form.affiliation"));
			formLayout.add(affiliationCont);
			initFormAffiliation(affiliationCont);

			// Group: Additional Roles
			rolesCont = FormLayoutContainer.createCustomFormLayout("additionalRolesCont", getTranslator(), this.velocity_root + "/add_org.html");
			rolesCont.setFormTitle(translate("form.additional.roles"));
			formLayout.add(rolesCont);
			initAdditionalRoles();
		} else {
			simpleRolesCont = uifactory.addDefaultFormLayout("rolesCont", null, formLayout);
			simpleRolesCont.setFormTitle(translate("form.additional.roles"));
			initFormSimpleRoles(simpleRolesCont);
		}
		
		FormLayoutContainer statusCont = uifactory.addDefaultFormLayout("statusc", null, formLayout);
		FormLayoutContainer buttonGroupLayout = uifactory.addButtonsFormLayout("buttonGroupLayout", null, statusCont);
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	private void initFormSimpleRoles(FormItemContainer formLayout) {
		Organisation defaultOrg = organisationService.getDefaultOrganisation();
		List<String> roleKeys = new ArrayList<>();
		List<String> roleValues = new ArrayList<>();
		fillOrderedRoles(roleKeys, roleValues);

		String[] keys = roleKeys.toArray(new String[0]);
		String[] values = roleValues.toArray(new String[0]);

		MultipleSelectionElement rolesEl = uifactory.addCheckboxesDropdown(
				"roles",
				"form.roles",
				formLayout,
				keys, values,
				null,
				null
		);

		rolesEl.setBadgeStyle(true);
		rolesEl.setNonSelectedText(translate("no.additional.role.selected"));

		RolesByOrganisation roles = editedRoles.getRoles(defaultOrg);
		if (roles != null) {
			for (String key : keys) {
				OrganisationRoles r = OrganisationRoles.valueOf(key);
				if (roles.hasRole(r)) {
					rolesEl.select(key, true);
				}
			}
		}
		
		RolesElement wrapper = new RolesElement(roleKeys, defaultOrg, rolesEl, null);
		rolesEl.setUserObject(wrapper);
		
		rolesEls.clear();
		rolesEls.add(wrapper);
	}

	private void initFormAffiliation(FormItemContainer formLayout) {
		if (!manageableOrganisations.isEmpty()) {
			affOrganisations = organisations.stream()
					.filter(org -> {
						RolesByOrganisation roles = editedRoles.getRoles(org);
						if (roles == null) return false;
						for (OrganisationRoles check : OrganisationRoles.values()) {
							if (check == OrganisationRoles.user && roles.hasRole(check)) {
								return true;
							}
						}
						return false;
					})
					.collect(Collectors.toList());
			
			
			OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
					affOrganisations,
					() -> manageableOrganisations);
			affiliationSelectorEl = uifactory.addObjectSelectionElement("affiliation.orgs.label", "affiliation.orgs.label",
					formLayout, getWindowControl(), true, organisationSource);
			affiliationSelectorEl.setMandatory(true);
			affiliationSelectorEl.addActionListener(FormEvent.ONCHANGE);

			List<Organisation> notManageableOrgs = affOrganisations.stream()
					.filter(org -> !manageableOrganisations.contains(org))
					.toList();
			if (!notManageableOrgs.isEmpty()) {
				String notManageableOrgNames = notManageableOrgs.stream()
						.map(Organisation::getDisplayName)
						.collect(Collectors.joining(", "));

				affiliationSelectorEl.setExampleKey("affiliation.orgs.information", new String[]{notManageableOrgNames});
			}
			
			affiliationTreeComp = new OrgStructureComponent("orgTreeAff", getWindowControl(), getLocale());
			affiliationTreeComp.setActiveOrganisations(affOrganisations);
			affiliationTreeComp.setCollapseUnrelated(true);
			formLayout.add(new ComponentWrapperElement(affiliationTreeComp));
		}
	}

	private void initAdditionalRoles() {
		addToOrganisationButton = uifactory.addFormLink("rightsForm.add.to.organisation", rolesCont, Link.BUTTON);
		addToOrganisationButton.setIconLeftCSS("o_icon o_icon_add");
		addToOrganisationButton.setElementCssClass("o_sel_add_organisation");
		
		for (Organisation organisation : organisations) {
			addAdditionalRolesWrapper(organisation);
		}
		
		updateUI();
	}

	private void updateUI() {
		sortAdditionalRolesEl();
		updateAddOrganisationUI();
		updateGlobalRolesUI();
	}
	
	private void updateAddOrganisationUI() {
		boolean orgsToAddAvailable = new ArrayList<>(manageableOrganisations).removeAll(organisations);
		addToOrganisationButton.setVisible(orgsToAddAvailable);
	}

	private void updateGlobalRolesUI() {
		List<String> globalRoleNames = rolesEls.stream()
				.filter(wrapper -> !OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(wrapper.getOrganisation().getIdentifier()))
				.flatMap(wrapper -> wrapper.getRolesDropdown().getSelectedKeys().stream())
				.distinct()
				.filter(roleName -> GLOBAL_ROLE_NAMES.contains(roleName))
				.map(roleName -> translate("role." + roleName))
				.toList();
		
		rolesCont.contextPut("subOrgGlobalRoleNames", globalRoleNames);
	}

	private void addAdditionalRolesWrapper(Organisation organisation) {
		RolesElement wrapper = createAdditionalRolesWrapper(rolesCont, organisation);
		if (wrapper != null) {
			rolesEls.add(wrapper);
		}
	}

	private RolesElement createAdditionalRolesWrapper(FormItemContainer formLayout, Organisation organisation) {
		List<OrganisationMember> members = getOrganisationMember(organisation);
		if (members.size() == 1 && members.get(0).getRole().equals(OrganisationRoles.invitee.name())) {
			return null;
		}
		
		int currentIndex = counter++;
		
		List<String> roleKeys = new ArrayList<>();
		List<String> roleValues = new ArrayList<>();
		fillOrderedRoles(roleKeys, roleValues);

		String[] keys = roleKeys.toArray(new String[0]);
		String[] values = roleValues.toArray(new String[0]);

		MultipleSelectionElement rolesEl = uifactory.addCheckboxesDropdown(
				"roles_" + currentIndex,
				"rightsForm.roles",
				formLayout,
				keys, values,
				null, null
		);
		rolesEl.setBadgeStyle(true);
		rolesEl.setNonSelectedText(translate("no.additional.role.selected"));
		rolesEl.addActionListener(FormEvent.ONCHANGE);

		if (organisationModule.isEnabled()) {
			rolesEl.setLabel("rightsForm.roles.for", new String[]{StringHelper.escapeHtml(organisation.getDisplayName())});
		}

		applySelectedRolesAndInheritance(rolesEl, members);
		sortExplicitBeforeInherited(rolesEl, roleKeys, roleValues);

		applyRolePermissions(rolesEl, keys, organisation);

		List<OrganisationWithParents> tree = organisationService.getOrderedTreeOrganisationsWithParents();
		Map<Long, List<Organisation>> childrenByParent = new HashMap<>();
		for (OrganisationWithParents entry : tree) {
			Organisation org = entry.getOrganisation();
			Organisation parent = org.getParent();
			if (parent != null) {
				childrenByParent.computeIfAbsent(parent.getKey(), k -> new ArrayList<>()).add(org);
			}
		}

		int descendantCount = countDescendants(organisation, childrenByParent);
		if (descendantCount > 0) {
			rolesEl.setExampleKey("rightsForm.child.orgs", new String[] { String.valueOf(descendantCount) });
		}

		OrgStructureComponent orgStructureElement = new OrgStructureComponent("orgTree_" + currentIndex, getWindowControl(), getLocale());
		orgStructureElement.setActiveOrganisations(Collections.singletonList(organisation));
		orgStructureElement.setCollapseUnrelated(true);
		formLayout.add(new ComponentWrapperElement(orgStructureElement));
		
		RolesElement wrapper = new RolesElement(roleKeys, organisation, rolesEl, orgStructureElement);
		rolesEl.setUserObject(wrapper);
		return wrapper;
	}

	private int countDescendants(Organisation org, Map<Long, List<Organisation>> childrenMap) {
		List<Organisation> children = childrenMap.getOrDefault(org.getKey(), List.of());
		int count = children.size();
		for (Organisation child : children) {
			count += countDescendants(child, childrenMap);
		}
		return count;
	}
	
	private void sortAdditionalRolesEl() {
		Map<Organisation, RolesElement> orgToEl = rolesEls.stream()
				.collect(Collectors.toMap(
						RolesElement::getOrganisation,
						Function.identity()));
		
		List<Organisation> hierarchicallySortedOrgs = sortOrganisationsHierarchically(orgToEl.keySet());
		
		List<RolesElement> sortedRolesEls = new ArrayList<>(rolesEls.size());
		for (Organisation organisation : hierarchicallySortedOrgs) {
			sortedRolesEls.add(orgToEl.get(organisation));
		}
		rolesEls = sortedRolesEls;
		
		rolesCont.contextPut("rolesEls", rolesEls);
	}

	private List<Organisation> sortOrganisationsHierarchically(Collection<Organisation> flatList) {
		return organisationService.getOrderedTreeOrganisationsWithParents().stream()
				.map(OrganisationWithParents::getOrganisation)
				.filter(organisation -> flatList.contains(organisation))
				.toList();
	}

	private void fillOrderedRoles(List<String> roleKeys, List<String> roleValues) {
		for (OrganisationRoles role : ORDERED_ROLES) {
			roleKeys.add(role.name());
			roleValues.add(translate("role." + role.name()));
		}
	}

	private List<OrganisationMember> getOrganisationMember(Organisation organisation) {
		SearchMemberParameters searchParams = new SearchMemberParameters();
		searchParams.setIdentityKey(editedIdentity.getKey());
		return organisationService.getMembers(organisation, searchParams);
	}

	private void applySelectedRolesAndInheritance(MultipleSelectionElement rolesDD, List<OrganisationMember> members) {
		if (members == null || members.isEmpty()) return;

		for (OrganisationMember member : members) {
			if (member.getInheritanceMode().equals(GroupMembershipInheritance.root)) {
				rolesDD.select(member.getRole(), true);
			}
			rolesDD.setCssClass(member.getRole(), member.getInheritanceMode().name());
		}
	}

	private void sortExplicitBeforeInherited(MultipleSelectionElement rolesDD, List<String> roleKeys, List<String> roleValues) {
		List<String> sortedKeys = new ArrayList<>();
		List<String> sortedValues = new ArrayList<>();

		for (int i = 0; i < roleKeys.size(); i++) {
			String key = roleKeys.get(i);
			String cssClass = rolesDD.getCssClass(key); // will return e.g. "root" or "inherited"

			if ("root".equals(cssClass)) {
				sortedKeys.add(key);
				sortedValues.add(roleValues.get(i));
			}
		}
		for (int i = 0; i < roleKeys.size(); i++) {
			String key = roleKeys.get(i);
			String cssClass = rolesDD.getCssClass(key);

			if (!"root".equals(cssClass)) {
				sortedKeys.add(key);
				sortedValues.add(roleValues.get(i));
			}
		}

		roleKeys.clear();
		roleKeys.addAll(sortedKeys);

		roleValues.clear();
		roleValues.addAll(sortedValues);
	}

	private void applyRolePermissions(MultipleSelectionElement rolesDD, String[] keys, Organisation organisation) {
		boolean isAdmin = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator)
				|| managerRoles.isSystemAdmin();
		boolean isUserManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager);
		boolean isRolesMgr = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.rolesmanager);
		boolean hasSome = editedRoles.isSystemAdmin()
				|| editedRoles.isAdministrator()
				|| editedRoles.isPrincipal()
				|| editedRoles.isManager()
				|| editedRoles.isAuthor();

		for (String k : keys) {
			OrganisationRoles r = OrganisationRoles.valueOf(k);
			boolean enable = false;

			if (isAdmin) {
				enable = true;
			} else if (isUserManager) {
				if (!hasSome && r == OrganisationRoles.invitee) enable = true;
				if (r == OrganisationRoles.user || r == OrganisationRoles.author) enable = true;
			} else if (isRolesMgr) {
				if (!hasSome && r == OrganisationRoles.invitee) enable = true;
				switch (r) {
					case user, author, curriculummanager, groupmanager,
						 learnresourcemanager, lecturemanager, linemanager,
						 poolmanager, projectmanager, qualitymanager,
						 rolesmanager, usermanager, educationmanager -> enable = true;
					default -> { }
				}
			}
			rolesDD.setEnabled(k, enable);
		}
	}

	private void update() {
		for (RolesElement wrapper : rolesEls) {
			MultipleSelectionElement dd  = wrapper.getRolesDropdown();
			// clear everything
			dd.uncheckAll();

			Organisation org = wrapper.getOrganisation();
			List<OrganisationMember> member = getOrganisationMember(org);

			applySelectedRolesAndInheritance(dd, member);

			// re-sort so explicits/roots come first
			sortExplicitBeforeInherited(dd,
					wrapper.getRoleKeys(),
					wrapper.getRoleKeys().stream()
							.map(k -> translate("role." + k))
							.collect(Collectors.toCollection(ArrayList::new))
			);
		}
	}

	private void updateRoles() {
		editedRoles = securityManager.getRoles(editedIdentity, false);
		
		if (organisationModule.isEnabled()) {
			if (rolesCont != null) {
				rolesCont.removeAll();
				rolesEls.clear();
				initAdditionalRoles();
			}
		} else {
			if (simpleRolesCont != null) {
				for (RolesElement wrapper : rolesEls) {
					simpleRolesCont.remove(wrapper.getRolesDropdown());
				}
				rolesEls.clear();
				initFormSimpleRoles(simpleRolesCont);
				
			}
		} 
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (selectOrganisationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doAddIdentityToOrganisation(selectOrganisationCtrl.getSelectedOrganisation());
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(selectOrganisationCtrl);
		removeAsListenerAndDispose(cmc);
		selectOrganisationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addToOrganisationButton == source) {
			doAddToOrganisation(ureq);
		} else if (source == affiliationSelectorEl) {
			// get all selected keys
			Set<Long> selKeys = affiliationSelectorEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet());

			affiliationSelectorEl.clearError();
			if (selKeys.isEmpty()) {
				affiliationSelectorEl.setErrorKey("error.roles.atleastone");
				return;
			}
			
			// map back to Organisation objects
			List<Organisation> active = manageableOrganisations.stream()
					.filter(o -> selKeys.contains(o.getKey()))
					.toList();
			// push into the tree
			affiliationTreeComp.setActiveOrganisations(active);
			
			for (Organisation organisation : active) {
				if (!organisations.contains(organisation)) {
					doAddIdentityToOrganisation(organisation);
				}
			}
			
			removeEmptyRolesEls();
			
			markDirty();
		} else if (source instanceof MultipleSelectionElement) {
			if (source.getUserObject() instanceof RolesElement) {
				updateGlobalRolesUI();
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	} 

	private void doAddToOrganisation(UserRequest ureq) {
		if (guardModalController(selectOrganisationCtrl)) return;

		List<Organisation> upgradeableToOrganisations = new ArrayList<>(manageableOrganisations);

		List<Organisation> onlyBaseRoles = organisations.stream()
				.filter(org -> {
					RolesByOrganisation roles = editedRoles.getRoles(org);
					if (roles == null) return true; // Include orgs where user has NO roles at all
					boolean hasBaseRole = roles.hasSomeRoles(OrganisationRoles.user, OrganisationRoles.invitee);
					for (OrganisationRoles check : OrganisationRoles.values()) {
						if (check != OrganisationRoles.user && check != OrganisationRoles.invitee) {
							if (roles.hasRole(check)) {
								return true;
							}
						}
					}
					return !hasBaseRole;
				})
				.toList();

		upgradeableToOrganisations.removeAll(onlyBaseRoles);

		selectOrganisationCtrl = new SelectOrganisationController(ureq, getWindowControl(), upgradeableToOrganisations, editedIdentity);
		listenTo(selectOrganisationCtrl);

		String title = translate("rightsForm.add.to.organisation");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectOrganisationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddIdentityToOrganisation(Organisation organisation) {
		editedRoles = securityManager.getRoles(editedIdentity, false);
		organisations.add(organisation);
		addAdditionalRolesWrapper(organisation);
		updateUI();
	}

	private void doModifyIdentityAffiliationToOrganisation() {
		if (affiliationSelectorEl == null) {
			return;
		}
		
		Set<Long> selKeys = affiliationSelectorEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet());
		
		// Current user orgs (where they have the "user" role)
		Set<Long> currentOrgKeys = affOrganisations.stream()
				.map(Organisation::getKey)
				.collect(Collectors.toSet());
		
		// Loop over all manageable orgs to determine what changed
		for (Organisation organisation : manageableOrganisations) {
			Long orgKey = organisation.getKey();
			boolean selected = selKeys.contains(orgKey); // now selected
			boolean currentlyHas = currentOrgKeys.contains(orgKey); // was selected before
			
			// Only act if there's a change in state
			if (selected != currentlyHas) {
				RolesByOrganisation rolesInOrg = editedRoles.getRoles(organisation);
				boolean hasUserRole = rolesInOrg != null && rolesInOrg.hasRole(OrganisationRoles.user);
				boolean shouldHaveUserRole = selKeys.contains(organisation.getKey());
				
				if (shouldHaveUserRole && !hasUserRole) {
					organisationService.addMember(organisation, editedIdentity, OrganisationRoles.user, getIdentity());
					if (!affOrganisations.contains(organisation)) {
						affOrganisations.add(organisation);
					}
				} else if (!shouldHaveUserRole && hasUserRole) {
					organisationService.removeMember(organisation, editedIdentity, getIdentity());
					if (affOrganisations.contains(organisation)) {
						affOrganisations.remove(organisation);
					}
				}
			}
		}
		
		dbInstance.commit();
	}

	private void removeEmptyRolesEls() {
		if (affiliationSelectorEl == null) {
			return;
		}
		
		List<RolesElement> wrappersToRemove = new ArrayList<>(1);
		for (RolesElement wrapper : rolesEls) {
			MultipleSelectionElement rolesDropdown = wrapper.getRolesDropdown();
			if (!rolesDropdown.isAtLeastSelected(1)
					&& !affiliationSelectorEl.getSelectedKeys().contains(wrapper.getOrganisation().getKey().toString())
					&& !hasAnyExplicitRole(rolesDropdown)) {
				organisations.remove(wrapper.getOrganisation());
				wrappersToRemove.add(wrapper);
			}
		}
		
		if (!wrappersToRemove.isEmpty()) {
			rolesEls.removeAll(wrappersToRemove);
			updateUI();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
	
		if (affiliationSelectorEl != null) {
			affiliationSelectorEl.clearError();
			if (affiliationSelectorEl.getSelectedKeys().isEmpty()) {
				affiliationSelectorEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveFormData();
		updateRoles();
		removeEmptyRolesEls();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private boolean hasAnyExplicitRole(MultipleSelectionElement rolesEl) {
		Organisation organisation = ((RolesElement) rolesEl.getUserObject()).organisation;
		List<OrganisationMember> organisationMember = getOrganisationMember(organisation);
		
		return organisationMember.stream().anyMatch(orgMem -> orgMem.getInheritanceMode().equals(GroupMembershipInheritance.root));
	}

	/**
	 * Persist form data in database. User needs to logout / login to activate changes. A bit tricky here
	 * is that only form elements should be gettet that the user is allowed to manipulate. See also the 
	 * comments in SystemRolesAndRightsForm.
	 */
	private void saveFormData() {
		editedRoles = securityManager.getRoles(editedIdentity, false);
		doModifyIdentityAffiliationToOrganisation();

		editedRoles = securityManager.getRoles(editedIdentity, false);
		for (RolesElement wrapper : rolesEls) {
			if (wrapper.getRolesDropdown().isEnabled()) {
				saveOrganisationRolesFormData(wrapper);
			}
		}
	}

	private void saveOrganisationRolesFormData(RolesElement wrapper) {
		Organisation organisation = wrapper.getOrganisation();
		boolean iAmUserManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager);
		boolean iAmRolesManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.rolesmanager);
		boolean iAmAdmin = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator)
				|| managerRoles.isSystemAdmin();

		Set<String> selectedKeys = new HashSet<>(wrapper.getRolesDropdown().getSelectedKeys());

		List<OrganisationRoles> rolesToAdd = new ArrayList<>();
		List<OrganisationRoles> rolesToRemove = new ArrayList<>();

		for (OrganisationRoles role : OrganisationRoles.values()) {
			if (role == OrganisationRoles.user || role == OrganisationRoles.invitee) continue;

			boolean allowedToManage = switch (role) {
				case author -> iAmAdmin || iAmUserManager;
				case groupmanager, poolmanager, curriculummanager, linemanager,
					 projectmanager, qualitymanager, lecturemanager, usermanager,
					 rolesmanager, learnresourcemanager, educationmanager -> iAmAdmin || iAmRolesManager;
				case principal, administrator, sysadmin -> iAmAdmin;
				default -> false;
			};

			if (!allowedToManage) continue;

			boolean selected = selectedKeys.contains(role.name());
			boolean currentlyAssigned = editedRoles.getRoles(organisation) != null &&
					editedRoles.getRoles(organisation).hasRole(role);

			OrganisationMember orgMember = getOrganisationMember(organisation).stream().filter(om -> om.getRole().equals(role.name())).findAny().orElse(null);
			GroupMembershipInheritance inheritanceMode = null;
			if (orgMember != null) {
				inheritanceMode = orgMember.getInheritanceMode();
			}
			if (selected && (!currentlyAssigned || (inheritanceMode != null && inheritanceMode.equals(GroupMembershipInheritance.inherited)))) {
				rolesToAdd.add(role);
			} else if (!selected && currentlyAssigned) {
				rolesToRemove.add(role);
			}
		}

		RolesByOrganisation editedOrganisationRoles = editedRoles.getRoles(organisation);
		if (editedOrganisationRoles == null) {
			editedOrganisationRoles = new RolesByOrganisation(organisation, OrganisationRoles.EMPTY_ROLES);
		}
		RolesByOrganisation updatedRoles = RolesByOrganisation.enhance(editedOrganisationRoles, rolesToAdd, rolesToRemove);
		securityManager.updateRoles(getIdentity(), editedIdentity, updatedRoles);
		for (OrganisationRoles role : rolesToAdd) {
			organisationService.addMember(organisation, editedIdentity, role, getIdentity());
		}
		dbInstance.commit();
	}

	public static final class RolesElement {
		
		private final List<String> roleKeys;
		private final Organisation organisation;
		private final MultipleSelectionElement rolesDropdown;
		private final OrgStructureComponent orgStructureElement;
		
		RolesElement(List<String> roleKeys, Organisation organisation, MultipleSelectionElement rolesDropdown,
				OrgStructureComponent orgStructureElement) {
			this.roleKeys = roleKeys;
			this.organisation = organisation;
			this.rolesDropdown = rolesDropdown;
			this.orgStructureElement = orgStructureElement;
		}
		
		public List<String> getRoleKeys() {
			return roleKeys;
		}
		
		public Organisation getOrganisation() {
			return organisation;
		}
		
		public String getRolesDropdownName() {
			return rolesDropdown.getComponent().getComponentName();
		}
		
		public MultipleSelectionElement getRolesDropdown() {
			return rolesDropdown;
		}
		
		public String getOrgStructureElementName() {
			return orgStructureElement != null? orgStructureElement.getComponentName(): null;
		}
		
		public OrgStructureComponent getOrgStructureElement() {
			return orgStructureElement;
		}
		
		public void commit(OrganisationRoles k, List<OrganisationRoles> rolesToAdd, List<OrganisationRoles> rolesToRemove) {
			if (roleKeys.contains(k.name())) {
				if (rolesDropdown.isKeySelected(k.name())) {
					rolesToAdd.add(k);
				} else {
					rolesToRemove.add(k);
				}
			}
		}
	}
}