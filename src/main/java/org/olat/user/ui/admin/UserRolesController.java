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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.olat.user.ui.organisation.element.OrgSelectorElement;
import org.olat.user.ui.organisation.structure.OrgStructureElement;
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

	private FormLayoutContainer simpleRolesCont;
	private FormLayoutContainer rolesCont;
	private FormLink addToOrganisationButton;
	private OrgSelectorElement affiliationSelectorEl;
	private OrgStructureElement affiliationTreeEl;
	private final List<MultipleSelectionElement> rolesEls = new ArrayList<>();

	private int counter = 0;

	private Roles editedRoles;
	private final Identity editedIdentity;
	private final Set<Organisation> justAddedOrganisation = new HashSet<>();
	private List<Organisation> organisations;
	private List<Organisation> affOrganisations;

	private final Roles managerRoles;
	private final List<Organisation> manageableOrganisations;

	private CloseableModalController cmc;
	private SelectOrganisationController selectOrganisationCtrl;


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
		editedRoles.getOrganisations();

		organisations = editedRoles.getOrganisations().stream()
				.map(organisationService::getOrganisation)
				.toList();

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
			removeEmptyRolesEls();
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
		rolesEl.setMandatory(true);

		for (String key : keys) {
			OrganisationRoles r = OrganisationRoles.valueOf(key);
			if (editedRoles.getRoles(defaultOrg).hasRole(r)) {
				rolesEl.select(key, true);
			}
		}

		RolesElement wrapper = new RolesElement(roleKeys, defaultOrg, rolesEl);
		rolesEl.setUserObject(wrapper);

		rolesEls.clear();
		rolesEls.add(rolesEl);
	}

	private void initFormAffiliation(FormItemContainer formLayout) {
		if (!manageableOrganisations.isEmpty()) {
			affiliationSelectorEl = uifactory.addOrgSelectorElement(
					"affiliation.orgs.label",
					formLayout,
					getWindowControl(),
					manageableOrganisations
			);
			affiliationSelectorEl.setMandatory(true);
			affiliationSelectorEl.setMultipleSelection(true);

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
					.toList();

			affiliationSelectorEl.setSelection(affOrganisations.stream().map(Organisation::getKey).toList());
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

			affiliationTreeEl = uifactory.addOrgStructureElement(
					"orgTreeAff",
					formLayout,
					getWindowControl(),
					affOrganisations
			);
			affiliationTreeEl.setCollapseUnrelatedBranches(true);
		}
	}

	private void initAdditionalRoles() {
		initAddToOrgBtn();

		List<OrganisationRoles> globals = List.of(
				OrganisationRoles.sysadmin,
				OrganisationRoles.groupmanager,
				OrganisationRoles.poolmanager
		);

		List<String> globalRoleNames = organisations.stream()
				.filter(o -> !OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(o.getIdentifier()))
				.map(o -> editedRoles.getRoles(o))
				.filter(Objects::nonNull)
				.flatMap(byOrg ->
						globals.stream().filter(byOrg::hasRole)
				)
				.distinct()
				.map(r -> translate("role." + r.name()))
				.toList();

		if (!globalRoleNames.isEmpty()) {
			rolesCont.contextPut("subOrgGlobalRoleNames", globalRoleNames);
		}

		List<Organisation> hierarchicallySortedOrgs = sortOrganisationsHierarchically(organisations);

		for (Organisation organisation : hierarchicallySortedOrgs) {
			initAdditionalRolesEls(rolesCont, organisation);
		}
		justAddedOrganisation.clear();
		rolesCont.contextPut("rolesEls", rolesEls);
	}

	private void initAddToOrgBtn() {
		List<Organisation> upgradeableToOrganisations = new ArrayList<>(manageableOrganisations);
		upgradeableToOrganisations.removeAll(organisations);
		if (!upgradeableToOrganisations.isEmpty()) {
			addToOrganisationButton = uifactory.addFormLink("rightsForm.add.to.organisation", rolesCont, Link.BUTTON);
			addToOrganisationButton.setIconLeftCSS("o_icon o_icon_add");
			addToOrganisationButton.setElementCssClass("o_sel_add_organisation");
		}
	}

	private void initAdditionalRolesEls(FormItemContainer formLayout, Organisation organisation) {
		List<OrganisationMember> members = getOrganisationMember(organisation);
		if (members.size() == 1 &&
				(members.get(0).getRole().equals(OrganisationRoles.user.name())
						|| members.get(0).getRole().equals(OrganisationRoles.invitee.name()))
				&& (justAddedOrganisation == null || !justAddedOrganisation.contains(organisation))
		) {
			return;
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
		rolesEl.addActionListener(FormEvent.ONCLICK);

		if (organisations.size() > 1 || !OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(organisation.getIdentifier())) {
			rolesEl.setLabel("rightsForm.roles.for", new String[]{StringHelper.escapeHtml(organisation.getDisplayName())});
		}

		applySelectedRolesAndInheritance(rolesEl, members);
		sortExplicitBeforeInherited(rolesEl, roleKeys, roleValues);

		applyRolePermissions(rolesEl, keys, organisation);

		RolesElement wrapper = new RolesElement(roleKeys, organisation, rolesEl);
		rolesEl.setUserObject(wrapper);
		rolesEls.add(rolesEl);

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

		OrgStructureElement orgStructureElement = uifactory.addOrgStructureElement(
				"orgTree_" + currentIndex, formLayout,
				getWindowControl(), Collections.singletonList(organisation)
		);
		orgStructureElement.setCollapseUnrelatedBranches(true);
	}

	int countDescendants(Organisation org, Map<Long, List<Organisation>> childrenMap) {
		List<Organisation> children = childrenMap.getOrDefault(org.getKey(), List.of());
		int count = children.size();
		for (Organisation child : children) {
			count += countDescendants(child, childrenMap);
		}
		return count;
	}

	private List<Organisation> sortOrganisationsHierarchically(List<Organisation> flatList) {
		List<Organisation> sorted = new ArrayList<>();
		Map<Long, Organisation> byKey = flatList.stream()
				.collect(Collectors.toMap(Organisation::getKey, o -> o));
		Map<Long, List<Organisation>> childrenMap = new HashMap<>();

		for (Organisation org : flatList) {
			Long parentKey = org.getParent() != null ? org.getParent().getKey() : null;
			if (parentKey != null && byKey.containsKey(parentKey)) {
				childrenMap.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(org);
			}
		}

		for (Organisation org : flatList) {
			if (org.getParent() == null || !byKey.containsKey(org.getParent().getKey())) {
				addOrgWithChildren(org, childrenMap, sorted);
			}
		}

		return sorted;
	}

	private void addOrgWithChildren(Organisation parent, Map<Long, List<Organisation>> childrenMap, List<Organisation> result) {
		result.add(parent);
		List<Organisation> children = childrenMap.get(parent.getKey());
		if (children != null) {
			children.sort(Comparator.comparing(Organisation::getDisplayName)); // optional, alphabetically
			for (Organisation child : children) {
				addOrgWithChildren(child, childrenMap, result);
			}
		}
	}

	private void fillOrderedRoles(List<String> roleKeys, List<String> roleValues) {
		OrganisationRoles[] orderedRoles = {
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

		for (OrganisationRoles r : orderedRoles) {
			roleKeys.add(r.name());
			roleValues.add(translate("role." + r.name()));
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
		// reload from DB
		editedRoles = securityManager.getRoles(editedIdentity, false);

		// for each dropdown we built above
		for (MultipleSelectionElement dd : rolesEls) {
			Object uo = dd.getUserObject();
			if (!(uo instanceof RolesElement wrapper)) continue;

			// clear everything
			dd.uncheckAll();

			Organisation org = wrapper.organisation();
			List<OrganisationMember> member = getOrganisationMember(org);

			applySelectedRolesAndInheritance(dd, member);

			// re-sort so explicits/roots come first
			sortExplicitBeforeInherited(dd,
					wrapper.roleKeys(),
					wrapper.roleKeys().stream()
							.map(k -> translate("role." + k))
							.collect(Collectors.toCollection(ArrayList::new))
			);
		}
	}

	private void updateRoles() {
		editedRoles = securityManager.getRoles(editedIdentity, false);

		if (organisationModule.isEnabled() && rolesCont != null) {
			if (addToOrganisationButton != null) {
				rolesCont.remove(addToOrganisationButton);
			}
			for (MultipleSelectionElement roleEl : rolesEls) {
				rolesCont.remove(roleEl);
			}
			rolesEls.clear();
			initAdditionalRoles();
			update();
		} else if (simpleRolesCont != null) {
			for (MultipleSelectionElement roleEl : rolesEls) {
				simpleRolesCont.remove(roleEl);
			}
			rolesEls.clear();
			initFormSimpleRoles(simpleRolesCont);
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
			Set<Long> selKeys = affiliationSelectorEl.getSelection();

			affiliationSelectorEl.clearError();
			if (selKeys.isEmpty()) {
				affiliationSelectorEl.setErrorKey("error.roles.atleastone");
				return;
			}

			// map back to Organisation objects
			List<Organisation> active = affOrganisations.stream()
					.filter(o -> selKeys.contains(o.getKey()))
					.toList();
			// push into the tree
			affiliationTreeEl.setActiveOrganisations(active);
			affiliationTreeEl.getComponent().setDirty(true);

			// Current user orgs (where they have the "user" role)
			Set<Long> currentOrgKeys = affOrganisations.stream()
					.map(Organisation::getKey)
					.collect(Collectors.toSet());

			// Loop over all manageable orgs to determine what changed
			for (Organisation org : manageableOrganisations) {
				Long orgKey = org.getKey();
				boolean selected = selKeys.contains(orgKey); // now selected
				boolean currentlyHas = currentOrgKeys.contains(orgKey); // was selected before

				// Only act if there's a change in state
				if (selected != currentlyHas) {
					doModifyIdentityAffiliationToOrganisation(org);
				}
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

	private List<Organisation> addOrganisation(List<Organisation> organisations, Organisation organisation) {
		organisations = new ArrayList<>(organisations);
		if (organisation != null && !organisations.contains(organisation)) {
			organisations.add(organisation);
		}
		return organisations;
	}

	private List<Organisation> removeOrganisation(List<Organisation> organisations, Organisation organisation) {
		organisations = new ArrayList<>(organisations);
		organisations.remove(organisation);
		return organisations;
	}

	private void doAddIdentityToOrganisation(Organisation organisation) {
		editedRoles = securityManager.getRoles(editedIdentity, false);
		organisations = addOrganisation(organisations, organisation);
		justAddedOrganisation.add(organisation);
		addOrganisation(organisations, organisation);
		updateRoles();
	}

	private void doModifyIdentityAffiliationToOrganisation(Organisation organisation) {
		RolesByOrganisation rolesInOrg = editedRoles.getRoles(organisation);
		boolean hasUserRole = rolesInOrg != null && rolesInOrg.hasRole(OrganisationRoles.user);
		boolean shouldHaveUserRole = affiliationSelectorEl.getSelection().contains(organisation.getKey());

		if (shouldHaveUserRole && !hasUserRole) {
			organisationService.addMember(organisation, editedIdentity, OrganisationRoles.user, getIdentity());
		} else if (!shouldHaveUserRole && hasUserRole) {
			organisationService.removeMember(organisation, editedIdentity, getIdentity());
		}

		dbInstance.commit();
	}

	private void removeEmptyRolesEls() {
		List<MultipleSelectionElement> emptyRolesEls =
				rolesEls.stream()
						.filter(r -> !r.isAtLeastSelected(1))
						.toList();
		if (!emptyRolesEls.isEmpty()) {
			for (MultipleSelectionElement emtpyRolesEl : emptyRolesEls) {
				if (!hasAnyExplicitRole(emtpyRolesEl)) {
					RolesElement rolesElUserObj = (RolesElement) emtpyRolesEl.getUserObject();
					organisations = removeOrganisation(organisations, rolesElUserObj.organisation());
					rolesEls.remove(emtpyRolesEl);
				}
			}
		}
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

		for (MultipleSelectionElement rolesEl : rolesEls) {
			if (rolesEl.isEnabled()
					&& rolesEl.getUserObject() instanceof RolesElement rolesElement) {
				saveOrganisationRolesFormData(rolesElement);
			}
		}
	}

	private void saveOrganisationRolesFormData(RolesElement wrapper) {
		Organisation organisation = wrapper.organisation();
		boolean iAmUserManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager);
		boolean iAmRolesManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.rolesmanager);
		boolean iAmAdmin = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator)
				|| managerRoles.isSystemAdmin();

		Set<String> selectedKeys = new HashSet<>(wrapper.rolesDropdown().getSelectedKeys());

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

	public record RolesElement(List<String> roleKeys, Organisation organisation,
							   MultipleSelectionElement rolesDropdown) {
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