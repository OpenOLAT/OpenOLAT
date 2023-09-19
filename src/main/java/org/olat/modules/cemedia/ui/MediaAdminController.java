/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaAdminController extends FormBasicController {

	private MultipleSelectionElement taxonomiesEl;
	private MultipleSelectionElement withUserEl;
	private MultipleSelectionElement withGroupEl;
	private MultipleSelectionElement withCourseEl;
	private MultipleSelectionElement withOrganisationEl;
	
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
		FormLayoutContainer taxonomyLayout = uifactory.addDefaultFormLayout("taxonomy", null, formLayout);
		initFormTaxonomy(taxonomyLayout);
		FormLayoutContainer permissionsLayout = uifactory.addDefaultFormLayout("permissions", null, formLayout);
		initFormPermissions(permissionsLayout);
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
		rolesKV.add(SelectionValues.entry(OrganisationRoles.user.name(), translate("role.user")));
		rolesKV.add(SelectionValues.entry(OrganisationRoles.author.name(), translate("role.author")));
		rolesKV.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("role.learnresourcemanager")));
		rolesKV.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("role.administrator")));
		
		withUserEl = uifactory.addCheckboxesVertical("permissions.user", formLayout, rolesKV.keys(), rolesKV.values(), 1);
		initPermissions(withUserEl, mediaModule.getRolesAllowedToShareWithUser(), rolesKV);
		withGroupEl = uifactory.addCheckboxesVertical("permissions.group", formLayout, rolesKV.keys(), rolesKV.values(), 1);
		initPermissions(withGroupEl, mediaModule.getRolesAllowedToShareWithGroup(), rolesKV);
		withCourseEl = uifactory.addCheckboxesVertical("permissions.course", formLayout, rolesKV.keys(), rolesKV.values(), 1);
		initPermissions(withCourseEl, mediaModule.getRolesAllowedToShareWithCourse(), rolesKV);
		
		SelectionValues rolesOrganisationKV = new SelectionValues();
		rolesOrganisationKV.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("role.learnresourcemanager")));
		rolesOrganisationKV.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("role.administrator")));
		withOrganisationEl = uifactory.addCheckboxesVertical("permissions.organisation", formLayout, rolesOrganisationKV.keys(), rolesOrganisationKV.values(), 1);
		initPermissions(withOrganisationEl, mediaModule.getRolesAllowedToShareWithOrganisation(), rolesOrganisationKV);
	}
	
	private void initPermissions(MultipleSelectionElement selectEl, List<OrganisationRoles> selectedList, SelectionValues rolesKV) {
		selectEl.addActionListener(FormEvent.ONCHANGE);
		for(OrganisationRoles select:selectedList) {
			String name = select.name();
			if(rolesKV.containsKey(name)) {
				selectEl.select(name, true);
			}	
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
		} else if(withUserEl == source) {
			mediaModule.setShareWithUser(toSelectedString(withUserEl));
		} else if(withGroupEl == source) {
			mediaModule.setShareWithGroup(toSelectedString(withGroupEl));
		} else if(withCourseEl == source) {
			mediaModule.setShareWithCourse(toSelectedString(withCourseEl));
		} else if(withOrganisationEl == source) {
			mediaModule.setShareWithOrganisation(toSelectedString(withOrganisationEl));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private String toSelectedString(MultipleSelectionElement el) {
		Collection<String> selectedKeys = el.getSelectedKeys();
		return String.join(",", selectedKeys);
	}
}
