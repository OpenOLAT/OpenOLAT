/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.cemedia.ui;

import java.util.Collection;
import java.util.List;

import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MediaAdminController extends FormBasicController {
	
	private static final String KEY_ALL = "all";
	private static final String KEY_ROLES = "roles";
	private static final List<String> ALL_ROLES = List.of(OrganisationRoles.user.name(), OrganisationRoles.author.name(),
			OrganisationRoles.learnresourcemanager.name(), OrganisationRoles.administrator.name());

	private MultipleSelectionElement taxonomiesEl;
	private SingleSelection permissionsUserEl;
	private SingleSelection permissionsGroupEl;
	private SingleSelection permissionsCourseEl;
	private MultipleSelectionElement rolesUserEl;
	private MultipleSelectionElement withGroupEl;
	private MultipleSelectionElement withCourseEl;
	private MultipleSelectionElement withOrganisationEl;
	private MultipleSelectionElement forceLicenseCheckEl;
	
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public MediaAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "media_admin", Util.createPackageTranslator(UserAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer licenseLayout = uifactory.addDefaultFormLayout("license", null, formLayout);
		initFormLicenseCheck(licenseLayout);
		FormLayoutContainer taxonomyLayout = uifactory.addDefaultFormLayout("taxonomy", null, formLayout);
		initFormTaxonomy(taxonomyLayout);
		FormLayoutContainer permissionsLayout = uifactory.addDefaultFormLayout("permissions", null, formLayout);
		initFormPermissions(permissionsLayout);
	}

	private void initFormLicenseCheck(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.license.title"));

		forceLicenseCheckEl = uifactory.addCheckboxesHorizontal("admin.license.force", formLayout, new String[] { "xx" },
				new String[] { translate("admin.license.force.value") });
		forceLicenseCheckEl.addActionListener(FormEvent.ONCHANGE);
		forceLicenseCheckEl.select(forceLicenseCheckEl.getKey(0), mediaModule.isForceLicenseCheck());
	}

	private void initFormTaxonomy(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.taxonomie.title"));
		
		List<Taxonomy> taxonomies = taxonomyService.getTaxonomyList();
		SelectionValues taxonomyKV = new SelectionValues();
		for(Taxonomy taxonomy:taxonomies) {
			taxonomyKV.add(SelectionValues.entry(taxonomy.getKey().toString(), taxonomy.getDisplayName()));
		}
		taxonomiesEl = uifactory.addCheckboxesVertical("taxonomy.linked.elements", formLayout, taxonomyKV.keys(), taxonomyKV.values(), 1);
		List<TaxonomyRef> taxonomyRefs = mediaModule.getTaxonomyRefs(false);
		for(TaxonomyRef taxonomy:taxonomyRefs) {
			String taxonomyKey = taxonomy.getKey().toString();
			if(taxonomyKV.containsKey(taxonomyKey)) {
				taxonomiesEl.select(taxonomyKey, true);
			}
		}
		taxonomiesEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void initFormPermissions(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.permissions.title"));
		formLayout.setFormInfo(translate("admin.permissions.info"));
		formLayout.setFormInfoHelp("https://jira.openolat.org/browse/OODOC-74");
		
		SelectionValues rolesKV = new SelectionValues();
		rolesKV.add(SelectionValues.entry(OrganisationRoles.author.name(), translate("role.author")));
		rolesKV.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("role.learnresourcemanager")));
		rolesKV.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("role.administrator")));
		
		SelectionValues allKV = new SelectionValues();
		allKV.add(SelectionValues.entry(KEY_ALL, translate("share.role.all")));
		allKV.add(SelectionValues.entry(KEY_ROLES, translate("share.role.roles")));
		
		permissionsUserEl = uifactory.addRadiosVertical("permissions.user", "permissions.user", formLayout, allKV.keys(), allKV.values());
		rolesUserEl = uifactory.addCheckboxesVertical("share.role.user", formLayout, rolesKV.keys(), rolesKV.values(), 1);
		initPermissions(permissionsUserEl, rolesUserEl, mediaModule.getRolesAllowedToShareWithUser(), rolesKV);
		
		permissionsGroupEl = uifactory.addRadiosVertical("permissions.group", "permissions.group", formLayout, allKV.keys(), allKV.values());
		withGroupEl = uifactory.addCheckboxesVertical("share.role.group", formLayout, rolesKV.keys(), rolesKV.values(), 1);
		initPermissions(permissionsGroupEl, withGroupEl, mediaModule.getRolesAllowedToShareWithGroup(), rolesKV);
		
		permissionsCourseEl = uifactory.addRadiosVertical("permissions.course", "permissions.course", formLayout, allKV.keys(), allKV.values());
		withCourseEl = uifactory.addCheckboxesVertical("share.role.course", formLayout, rolesKV.keys(), rolesKV.values(), 1);
		initPermissions(permissionsCourseEl, withCourseEl, mediaModule.getRolesAllowedToShareWithCourse(), rolesKV);
		
		SelectionValues rolesOrganisationKV = new SelectionValues();
		rolesOrganisationKV.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("role.learnresourcemanager")));
		rolesOrganisationKV.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("role.administrator")));
		withOrganisationEl = uifactory.addCheckboxesVertical("permissions.organisation", formLayout, rolesOrganisationKV.keys(), rolesOrganisationKV.values(), 1);
		initPermissions(null, withOrganisationEl, mediaModule.getRolesAllowedToShareWithOrganisation(), rolesOrganisationKV);
	}
	
	private void initPermissions(SingleSelection permissionsEl, MultipleSelectionElement rolesEl, List<OrganisationRoles> selectedList, SelectionValues rolesKV) {	
		rolesEl.addActionListener(FormEvent.ONCHANGE);
		for(OrganisationRoles select:selectedList) {
			String name = select.name();
			if(rolesKV.containsKey(name)) {
				rolesEl.select(name, true);
			}	
		}
		
		if(permissionsEl != null) {
			permissionsEl.addActionListener(FormEvent.ONCHANGE);
			if(selectedList.size() == ALL_ROLES.size()) {
				permissionsEl.select(KEY_ALL, true);
			} else {
				permissionsEl.select(KEY_ROLES, true);
			}
		
			boolean roles = permissionsEl.isOneSelected() && KEY_ROLES.equals(permissionsEl.getSelectedKey());
			rolesEl.setVisible(roles);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(taxonomiesEl == source) {
			List<TaxonomyRef> taxonomies = taxonomiesEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.map(TaxonomyRefImpl::new)
					.map(TaxonomyRef.class::cast)
					.toList();
			mediaModule.setTaxonomyRefs(taxonomies);
		} else if(permissionsUserEl == source) {
			mediaModule.setShareWithUser(switchSelection(permissionsUserEl, rolesUserEl));
		} else if(permissionsGroupEl == source) {
			mediaModule.setShareWithGroup(switchSelection(permissionsGroupEl, withGroupEl));
		} else if(permissionsCourseEl == source) {
			mediaModule.setShareWithCourse(switchSelection(permissionsCourseEl, withCourseEl));
		} else if(rolesUserEl == source) {
			mediaModule.setShareWithUser(toSelectedString(rolesUserEl));
		} else if(withGroupEl == source) {
			mediaModule.setShareWithGroup(toSelectedString(withGroupEl));
		} else if(withCourseEl == source) {
			mediaModule.setShareWithCourse(toSelectedString(withCourseEl));
		} else if(withOrganisationEl == source) {
			mediaModule.setShareWithOrganisation(toSelectedString(withOrganisationEl));
		} else if (forceLicenseCheckEl == source) {
			mediaModule.setForceLicense(forceLicenseCheckEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private String switchSelection(SingleSelection permissionsEl, MultipleSelectionElement rolesEl) {
		boolean all = permissionsEl.isOneSelected() && KEY_ALL.equals(permissionsEl.getSelectedKey());
		rolesEl.setVisible(!all);
		Collection<String> roles;
		if(all) {
			roles = ALL_ROLES;
		} else {
			roles = rolesEl.getSelectedKeys();
		}
		return String.join(",", roles);
	}
	
	private String toSelectedString(MultipleSelectionElement el) {
		Collection<String> selectedKeys = el.getSelectedKeys();
		return String.join(",", selectedKeys);
	}
}
