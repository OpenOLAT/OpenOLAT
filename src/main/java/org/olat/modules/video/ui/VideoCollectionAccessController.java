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
package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.video.VideoManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.RepositoryCatalogInfoFactory;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class VideoCollectionAccessController extends FormBasicController {
	
	private FormToggle videoCollectionEl;
	private MultiSelectionFilterElement organisationsEl;
	
	private final boolean readOnly;
	private final RepositoryEntry entry;
	private final List<Organisation> organisations;
	private final List<Organisation> videoOrganisations;
	
	@Autowired
	private ACService acService;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public VideoCollectionAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		this.entry = entry;
		this.readOnly = readOnly;
		
		organisations = acService.getSelectionOfferOrganisations(getIdentity());
		videoOrganisations = videoManager.getVideoOrganisations(entry);

		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("video.collection.access.title");
		
		videoCollectionEl = uifactory.addToggleButton("video.collection.access", "video.collection.access", translate("on"), translate("off"), formLayout);
		videoCollectionEl.toggle(entry.isVideoCollection());
		videoCollectionEl.addActionListener(FormEvent.ONCHANGE);
		
		Roles roles = ureq.getUserSession().getRoles();
		List<Organisation> availableOrganisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		List<Organisation> organisationList = new ArrayList<>(availableOrganisations);
		SelectionValues organisationSV = OrganisationUIFactory.createSelectionValues(organisationList, getLocale());
		organisationsEl = uifactory.addCheckboxesFilterDropdown("video.collection.organisations", "video.collection.organisations", formLayout, getWindowControl(), organisationSV);
		organisationsEl.setVisible(organisationModule.isEnabled());
		organisationsEl.setEnabled(!readOnly);
		if(videoOrganisations != null) {
			videoOrganisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
		}
		
		List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), repositoryService.getTaxonomy(entry));
		String levels = RepositoryCatalogInfoFactory.wrapTaxonomyLevels(taxonomyLevels);
		StaticTextElement taxonomyEl = uifactory.addStaticTextElement("video.collection.taxonomy", "video.collection.taxonomy", levels, formLayout);
		taxonomyEl.setDomWrapperElement(DomWrapperElement.div);
		taxonomyEl.setVisible(!taxonomyLevels.isEmpty());
		
		FormLayoutContainer layoutCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", "save", layoutCont);
	}
	
	private void updateUI() {
		organisationsEl.setVisible(organisationModule.isEnabled() && videoCollectionEl.isOn());
	}
	
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationsEl.clearError();
		if(organisationsEl.isVisible() && organisationsEl.getSelectedKeys().isEmpty()) {
			organisationsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk; 
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(videoCollectionEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public boolean isVideoCollectionEnabled() {
		return videoCollectionEl.isOn();
	}
	
	public List<Organisation> getVideoOrganisations() {
		if (organisationsEl == null) {
			if (videoOrganisations == null) {
				return Collections.emptyList();
			}
			return List.copyOf(videoOrganisations);
		}
		
		Collection<String> selectedOrgKeys = organisationsEl.getSelectedKeys();
		return organisations.stream()
				.filter(org -> selectedOrgKeys.contains(org.getKey().toString()))
				.toList();
	}
}
