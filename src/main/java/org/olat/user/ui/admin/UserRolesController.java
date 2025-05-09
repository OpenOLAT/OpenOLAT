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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.SelectOrganisationController;
import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationMember;
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
import org.olat.core.id.OrganisationRef;
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

	private FormLayoutContainer rolesCont;
	private FormLink addToOrganisationButton;
	private OrgSelectorElement affiliationSelectorEl;
	private OrgStructureElement affiliationTreeEl;
	private final List<MultipleSelectionElement> rolesEls = new ArrayList<>();

	private int counter = 0;
	
	/**
	 * The roles without inheritance
	 */
	private Roles editedRoles;
	private final Identity editedIdentity;
	private List<Organisation> organisations;
	private List<Organisation> affOrgs;
	
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
	public UserRolesController(WindowControl wControl, UserRequest ureq, Identity identity) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(UserAdminController.class, getLocale(), getTranslator()));
		this.editedIdentity = identity;
		
		editedRoles = securityManager.getRoles(editedIdentity, false);
		editedRoles.getOrganisations();
		
		organisations = new ArrayList<>();
		for(OrganisationRef organisation: editedRoles.getOrganisations()) {
			organisations.add(organisationService.getOrganisation(organisation));
		}

		managerRoles = ureq.getUserSession().getRoles();
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), managerRoles,
				OrganisationRoles.administrator, OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
		
		initForm(ureq);
		update();
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
			initFormRoles();
		} else {
			FormLayoutContainer simpleRolesCont = FormLayoutContainer
					.createDefaultFormLayout("rolesCont", getTranslator());
			simpleRolesCont.setFormTitle(translate("form.additional.roles"));
			formLayout.add(simpleRolesCont);
			initFormSimpleRoles(simpleRolesCont);
		}
		FormLayoutContainer statusCont = FormLayoutContainer.createDefaultFormLayout("statusc", getTranslator());
		formLayout.add(statusCont);
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		statusCont.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	private void initFormSimpleRoles(FormItemContainer formLayout) {
		List<String> roleKeys   = new ArrayList<>();
		List<String> roleValues = new ArrayList<>();
		fillOrderedRoles(roleKeys, roleValues);

		String[] keys   = roleKeys.toArray(new String[0]);
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
			if (editedRoles.hasRole(r)) {
				rolesEl.select(key, true);
			}
		}

		rolesEls.clear();
		rolesEls.add(rolesEl);
	}

	private void initFormAffiliation(FormItemContainer formLayout) {
		// gather only the orgs where the user has the “user” role
		affOrgs = organisations.stream()
				.filter(org -> {
					RolesByOrganisation r = editedRoles.getRoles(org);
					return r != null && r.hasRole(OrganisationRoles.user);
				})
				.toList();

		if (!affOrgs.isEmpty()) {
			affiliationSelectorEl = uifactory.addOrgSelectorElement(
					"affiliation.orgs.label",
					formLayout,
					getWindowControl(),
					affOrgs
			);
			affiliationSelectorEl.setMandatory(true);
			affiliationSelectorEl.setMultipleSelection(true);
			List<Long> allKeys = affOrgs.stream()
					            .map(Organisation::getKey)
					            .toList();
			affiliationSelectorEl.setSelection(allKeys);
			affiliationSelectorEl.addActionListener(FormEvent.ONCHANGE);

			affiliationTreeEl = uifactory.addOrgStructureElement(
					"orgTreeAff",
					formLayout,
					getWindowControl(),
					affOrgs
			);
		}
	}

	private void initFormRoles() {
		List<Organisation> upgradeableToOrganisations = new ArrayList<>(manageableOrganisations);
		upgradeableToOrganisations.removeAll(organisations);
		if(!upgradeableToOrganisations.isEmpty()) {
			addToOrganisationButton = uifactory.addFormLink("rightsForm.add.to.organisation", rolesCont, Link.BUTTON);
			addToOrganisationButton.setIconLeftCSS("o_icon o_icon_add");
			addToOrganisationButton.setElementCssClass("o_sel_add_organisation");
		}

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

		for(Organisation organisation:organisations) {
			initFormRoles(rolesCont, organisation);
		}
	}

	private void initFormRoles(FormItemContainer formLayout, Organisation organisation) {
		int currentIndex = counter++;

		List<String> roleKeys = new ArrayList<>();
		List<String> roleValues = new ArrayList<>();
		fillOrderedRoles(roleKeys, roleValues);

		String[] keys = roleKeys.toArray(new String[0]);
		String[] values = roleValues.toArray(new String[0]);

		MultipleSelectionElement rolesDD = uifactory.addCheckboxesDropdown(
				"roles_" + currentIndex,
				"rightsForm.roles",
				formLayout,
				keys, values,
				null, null
		);
		rolesDD.setBadgeStyle(true);
		rolesDD.setNonSelectedText(translate("no.additional.role.selected"));
		rolesDD.addActionListener(FormEvent.ONCLICK);

		if (organisations.size() > 1 || !OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(organisation.getIdentifier())) {
			rolesDD.setLabel("rightsForm.roles.for", new String[]{ StringHelper.escapeHtml(organisation.getDisplayName()) });
		}

		OrganisationMember member = getOrganisationMember(organisation);
		applySelectedRolesAndInheritance(rolesDD, keys, member, organisation);
		sortExplicitBeforeInherited(rolesDD, roleKeys, roleValues);

		applyRolePermissions(rolesDD, keys, organisation);

		RolesElement wrapper = new RolesElement(roleKeys, organisation, rolesDD);
		rolesDD.setUserObject(wrapper);
		rolesEls.add(rolesDD);

		rolesCont.contextPut("rolesEls", rolesEls);

		uifactory.addOrgStructureElement(
				"orgTree_" + currentIndex, formLayout,
				getWindowControl(), Collections.singletonList(organisation)
		);
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

	private OrganisationMember getOrganisationMember(Organisation organisation) {
		List<OrganisationMember> members = organisationService.getMembers(organisation, new SearchMemberParameters());
		return members.stream()
				.filter(m -> m.getIdentity().equals(editedIdentity))
				.findAny()
				.orElse(null);
	}

	private void applySelectedRolesAndInheritance(MultipleSelectionElement rolesDD, String[] keys,
												  OrganisationMember member, Organisation organisation) {
		if (member == null) return;

		// explicit/root roles on this org
		RolesByOrganisation byOrg = editedRoles.getRoles(organisation);
		Set<OrganisationRoles> explicit = Arrays.stream(keys)
				.map(OrganisationRoles::valueOf)
				.filter(byOrg::hasRole)
				.collect(Collectors.toSet());

		// inherited roles from parent chain
		Set<OrganisationRoles> inherited = new HashSet<>();
		for (OrganisationRef parentRef : organisation.getParentLine()) {
			Organisation parent = organisationService.getOrganisation(parentRef);
			RolesByOrganisation parentRoles = editedRoles.getRoles(parent);
			if (parentRoles == null) continue;
			for (String k : keys) {
				OrganisationRoles r = OrganisationRoles.valueOf(k);
				if (parentRoles.hasRole(r) && !explicit.contains(r)) {
					inherited.add(r);
				}
			}
		}

		// now select all explicit/root and inherited, and set their CSS
		for (String k : keys) {
			OrganisationRoles r = OrganisationRoles.valueOf(k);

			// select if explicit/root OR inherited
			if (explicit.contains(r) || inherited.contains(r)) {
				rolesDD.select(k, true);
			}

			if (explicit.contains(r)) {
				rolesDD.setCssClass(k, "root");
			} else if (inherited.contains(r)) {
				rolesDD.setCssClass(k, "inherited");
			} else {
				rolesDD.setCssClass(k, "");
			}
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
			OrganisationMember member = getOrganisationMember(org);
			String[] keys = wrapper.roleKeys().toArray(new String[0]);

			applySelectedRolesAndInheritance(dd, keys, member, org);

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
		if(addToOrganisationButton != null) {
			rolesCont.remove(addToOrganisationButton);
		}
		for(MultipleSelectionElement roleEl:rolesEls) {
			rolesCont.remove(roleEl);
		}
		rolesEls.clear();
		initFormRoles();
		update();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(selectOrganisationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doAddIdentityToOrganisation(selectOrganisationCtrl.getSelectedOrganisation());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (rolesEls.isEmpty()) {
			return false;
		}
		for (MultipleSelectionElement rolesEl : rolesEls) {
			rolesEl.clearError();
		}

		int numOfRoles = 0;
		for(MultipleSelectionElement rolesEl:rolesEls) {
			Collection<String> selectedRoles = rolesEl.getSelectedKeys();
			numOfRoles += selectedRoles.size();
		}

		if(numOfRoles == 0) {
			rolesEls.get(0).setErrorKey("error.roles.atleastone");
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addToOrganisationButton == source) {
			doAddToOrganisation(ureq);
		} else if (source == affiliationSelectorEl) {
			// get all selected keys
			Set<Long> selKeys = affiliationSelectorEl.getSelection();
			// map back to Organisation objects
			List<Organisation> active = affOrgs.stream()
					.filter(o -> selKeys.contains(o.getKey()))
					.toList();
			// push into the tree
			affiliationTreeEl.setActiveOrganisations(active);
			affiliationTreeEl.getComponent().setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAddToOrganisation(UserRequest ureq) {
		if(guardModalController(selectOrganisationCtrl)) return;
		
		List<Organisation> upgradeableToOrganisations = new ArrayList<>(manageableOrganisations);
		upgradeableToOrganisations.removeAll(organisations);
		selectOrganisationCtrl = new SelectOrganisationController(ureq, getWindowControl(), upgradeableToOrganisations, editedIdentity);
		listenTo(selectOrganisationCtrl);
		
		String title = translate("rightsForm.add.to.organisation");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectOrganisationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}

	private void doAddIdentityToOrganisation(Organisation organisation) {
		organisationService.addMember(organisation, editedIdentity, OrganisationRoles.user, getIdentity());
		dbInstance.commit();
		organisations = organisationService.getOrganisations(editedIdentity, OrganisationRoles.values());
		
		updateRoles();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveFormData();
		update();
	}

	/**
	 * Persist form data in database. User needs to logout / login to activate changes. A bit tricky here
	 * is that only form elements should be gettet that the user is allowed to manipulate. See also the 
	 * comments in SystemRolesAndRightsForm. 
	 * @param myIdentity
	 * @param form
	 */
	private void saveFormData() {
		editedRoles = securityManager.getRoles(editedIdentity, false);

		for(MultipleSelectionElement rolesEl:rolesEls) {
			if(rolesEl.isEnabled()
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

			boolean selected = selectedKeys.contains(role.name());
			boolean currentlyAssigned = editedRoles.getRoles(organisation) != null &&
					editedRoles.getRoles(organisation).hasRole(role);

			boolean allowedToManage = switch (role) {
				case author -> iAmAdmin || iAmUserManager;
				case groupmanager, poolmanager, curriculummanager, linemanager,
					 projectmanager, qualitymanager, lecturemanager, usermanager,
					 rolesmanager, learnresourcemanager, educationmanager -> iAmAdmin || iAmRolesManager;
				case principal, administrator, sysadmin -> iAmAdmin;
				default -> false;
			};

			if (!allowedToManage) continue;

			if (selected && !currentlyAssigned) {
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