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
package org.olat.repository.bulk.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationController extends StepFormBasicController {
	
	private MultiSelectionFilterElement organisationAddEl;
	private MultipleSelectionElement organisationRemoveEl;
	private StaticTextElement organisationRemoveInfoEl;
	
	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final Map<Long, List<Long>> repositoryEntryKeyOrganisationKeys;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;

	public OrganisationController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		this.editables = (SettingsBulkEditables)runContext.get(SettingsBulkEditables.DEFAULT_KEY);
		
		Map<RepositoryEntryRef, List<Organisation>> repositoryEntryOrganisations = repositoryService.getRepositoryEntryOrganisations(context.getRepositoryEntries());
		repositoryEntryKeyOrganisationKeys = new HashMap<>(repositoryEntryOrganisations.size());
		for (Entry<RepositoryEntryRef, List<Organisation>> entryOrganisations : repositoryEntryOrganisations.entrySet()) {
			repositoryEntryKeyOrganisationKeys.put(
					entryOrganisations.getKey().getKey(),
					entryOrganisations.getValue().stream().map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("settings.bulk.organisation.title");
		setFormInfo("noTransOnlyParam",
				new String[] {RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.organisation.desc")});
		
		Roles roles = ureq.getUserSession().getRoles();
		List<Organisation> availableOrganisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		SelectionValues addOrganisationSV = OrganisationUIFactory.createSelectionValues(availableOrganisations);
		organisationAddEl = uifactory.addCheckboxesFilterDropdown("organisations.add",
				"settings.bulk.organisation.add", formLayout, getWindowControl(), addOrganisationSV);
		organisationAddEl.addActionListener(FormEvent.ONCHANGE);
		if (context.getOrganisationAddKeys() != null) {
			context.getOrganisationAddKeys().forEach(key -> organisationAddEl.select(key.toString(), true));
		}
		
		SelectionValues removeOrganisationSV = new SelectionValues();
		repositoryService.getOrganisations(context.getRepositoryEntries())
				.forEach(organisation -> removeOrganisationSV.add(entry(organisation.getKey().toString(), organisation.getDisplayName())));
		organisationRemoveEl = uifactory.addCheckboxesDropdown("organisations.remove", "settings.bulk.organisation.remove", formLayout,
				removeOrganisationSV.keys(), removeOrganisationSV.values());
		organisationRemoveEl.setElementCssClass("o_form_explained");
		organisationRemoveEl.addActionListener(FormEvent.ONCLICK);
		if (context.getOrganisationRemoveKeys() != null) {
			context.getOrganisationRemoveKeys().forEach(key -> organisationRemoveEl.select(key.toString(), true));
		}
		
		String organisationRemoveInfo = "<i class='o_icon o_icon_warn'> </i> " + translate("settings.bulk.organisation.last");
		organisationRemoveInfoEl = uifactory.addStaticTextElement("educational.type.info", null, organisationRemoveInfo, formLayout);
		organisationRemoveInfoEl.setElementCssClass("o_form_explanation");
	}

	private void updateUI() {
		organisationRemoveInfoEl.setVisible(false);
		
		// The RepositoryEntry get a new organisation. It has at least one organisation.
		if (!organisationAddEl.getSelectedKeys().isEmpty()) {
			return;
		}
		
		// No Organisation is removed. The RepositoryEntry has at least one organisation.
		if (organisationRemoveEl.getSelectedKeys().isEmpty()) {
			return;
		}
		
		// Check if a RepositoryEntry has no organisations anymore.
		Set<Long> organisationRemoveKeys = organisationRemoveEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet());
		for (RepositoryEntry repositoryEntry : context.getRepositoryEntries()) {
			if (!RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.organisations)) {
				List<Long> organisationKeys = repositoryEntryKeyOrganisationKeys.get(repositoryEntry.getKey());
				if (organisationKeys != null && !organisationKeys.isEmpty()) {
					List<Long> organisationKeysCopy = new ArrayList<>(organisationKeys);
					organisationKeysCopy.removeAll(organisationRemoveKeys);
					if (organisationKeysCopy.isEmpty()) {
						organisationRemoveInfoEl.setVisible(true);
						return;
					}
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == organisationAddEl || source == organisationRemoveEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.organisationsAdd, !organisationAddEl.getSelectedKeys().isEmpty());
		Set<Long> organisationAddKeys = !organisationAddEl.getSelectedKeys().isEmpty()
				? organisationAddEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet())
				: null;
		context.setOrganisationAddKeys(organisationAddKeys);
		
		context.select(SettingsBulkEditable.organisationsRemove, !organisationRemoveEl.getSelectedKeys().isEmpty());
		Set<Long> organisationRemoveKeys = !organisationRemoveEl.getSelectedKeys().isEmpty()
				? organisationRemoveEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet())
				: null;
		context.setOrganisationRemoveKeys(organisationRemoveKeys);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
