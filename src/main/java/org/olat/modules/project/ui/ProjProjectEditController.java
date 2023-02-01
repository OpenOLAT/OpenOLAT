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
package org.olat.modules.project.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectEditController extends FormBasicController {
	
	private static final OrganisationRoles[] ROLES_PROJECT_MANAGER = { OrganisationRoles.administrator,
			OrganisationRoles.projectmanager };

	private TextElement titleEl;
	private TextElement externalRefEl;
	private TextElement teaserEl;
	private TextAreaElement descriptionEl;
	private MultiSelectionFilterElement organisationsEl;

	private ProjProject project;
	private final boolean readOnly;
	private List<Organisation> organisations;
	private List<Organisation> projectOrganisations;
	
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public ProjProjectEditController(UserRequest ureq, WindowControl wControl, ProjProject project, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.project = project;
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = project != null? project.getTitle(): null;
		titleEl = uifactory.addTextElement("project.title", 100, title, formLayout);
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String externalRef = project != null? project.getExternalRef(): null;
		externalRefEl = uifactory.addTextElement("project.external.ref", 100, externalRef, formLayout);
		externalRefEl.setEnabled(!readOnly);
		
		String teaser = project != null? project.getTeaser(): null;
		teaserEl = uifactory.addTextElement("project.teaser", 150, teaser, formLayout);
		teaserEl.setEnabled(!readOnly);
		
		String description = project != null? project.getDescription(): null;
		descriptionEl = uifactory.addTextAreaElement("project.description", 4, 6, description, formLayout);
		descriptionEl.setEnabled(!readOnly);
		
		if (organisationModule.isEnabled()) {
			initFormOrganisations(formLayout, ureq.getUserSession());
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		if (readOnly) {
			uifactory.addFormCancelButton("close", buttonLayout, ureq, getWindowControl());
		} else {
			uifactory.addFormSubmitButton("save", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		OrganisationRoles[] orgRoles;
		Set<OrganisationRoles> createRoles = projectModule.getCreateRoles();
		if (!createRoles.isEmpty()) {
			Set<OrganisationRoles> allRoles = new HashSet<>(createRoles);
			allRoles.addAll(Arrays.asList(ROLES_PROJECT_MANAGER));
			orgRoles = allRoles.stream().toArray(OrganisationRoles[]::new);
		} else {
			orgRoles = ROLES_PROJECT_MANAGER;
		}
		organisations = organisationService.getOrganisations(getIdentity(), usess.getRoles(), orgRoles);
		projectOrganisations = projectService.getOrganisations(project);
		
		if (!projectOrganisations.isEmpty()) {
			for (Organisation projectOrganisation : projectOrganisations) {
				if (projectOrganisation != null && !organisations.contains(projectOrganisation)) {
					organisations.add(projectOrganisation);
				}
			}
		}
		
		SelectionValues orgSV = OrganisationUIFactory.createSelectionValues(organisations);
		organisationsEl = uifactory.addCheckboxesFilterDropdown("organisations", "project.organisations", formLayout, getWindowControl(), orgSV);
		organisationsEl.setMandatory(true);
		organisationsEl.setEnabled(!readOnly);
		projectOrganisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		if (organisationsEl != null) {
			organisationsEl.clearError();
			if (organisationsEl.getSelectedKeys().isEmpty()) {
				organisationsEl.setErrorKey("form.legende.mandatory");
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
		if (project == null) {
			project = projectService.createProject(getIdentity());
		}
		
		project.setTitle(titleEl.getValue());
		project.setExternalRef(externalRefEl.getValue());
		project.setTeaser(teaserEl.getValue());
		project.setDescription(descriptionEl.getValue());
		project = projectService.updateProject(getIdentity(), project);
		
		if (organisationsEl != null) {
			Collection<String> selectedOrgKeys = organisationsEl.getSelectedKeys();
			List<Organisation> selectedOrganisations = organisations.stream()
					.filter(org -> selectedOrgKeys.contains(org.getKey().toString()))
					.collect(Collectors.toList());
			projectService.updateProjectOrganisations(getIdentity(), project, selectedOrganisations);
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
