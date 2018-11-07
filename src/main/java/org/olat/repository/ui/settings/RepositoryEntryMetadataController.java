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
package org.olat.repository.ui.settings;

import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryMetadataController extends FormBasicController {

	private ResourceLicense license;
	private RepositoryEntry repositoryEntry;
	private Set<TaxonomyLevel> taxonomyLevels;

	private TextElement authors;
	private TextElement language;
	private TextElement licensorEl;
	private TextElement expenditureOfWork;
	private TextAreaElement licenseFreetextEl;
	private SingleSelection licenseEl;
	private MultipleSelectionElement taxonomyLevelEl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;

	/**
	 * Create a repository add controller that adds the given resourceable.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param sourceEntry
	 */
	public RepositoryEntryMetadataController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		initForm(ureq);
	}

	/**
	 * @return Returns the repositoryEntry.
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Set up info page");
		formLayout.setElementCssClass("o_sel_repo_metadata");
		setFormTitle("details.metadata.title");
		
		// Add resource type
		String typeName = null;
		OLATResource res = repositoryEntry.getOlatResource();
		if (res != null) {
			typeName = res.getResourceableTypeName();
		}
		String typeDisplay ;
		if (typeName != null) { // add image and typename code
			typeDisplay = NewControllerFactory.translateResourceableTypeName(typeName, getLocale());
		} else {
			typeDisplay = translate("cif.type.na");
		}
		uifactory.addStaticTextElement("cif.type", typeDisplay, formLayout);

		String id = repositoryEntry.getResourceableId() == null ? "-" : repositoryEntry.getResourceableId().toString();
		uifactory.addStaticTextElement("cif.id", id, formLayout);
		
		String externalId = repositoryEntry.getExternalId();
		if(StringHelper.containsNonWhitespace(externalId)) {
			uifactory.addStaticTextElement("cif.externalid", externalId, formLayout);
		}

		String initalAuthor = repositoryEntry.getInitialAuthor() == null ? "-" : repositoryEntry.getInitialAuthor();
		if(repositoryEntry.getInitialAuthor() != null) {
			initalAuthor = userManager.getUserDisplayName(initalAuthor);
		}
		initalAuthor = StringHelper.escapeHtml(initalAuthor);
		uifactory.addStaticTextElement("cif.initialAuthor", initalAuthor, formLayout);

		uifactory.addSpacerElement("spacer1", formLayout, false);

		authors = uifactory.addTextElement("cif.authors", "cif.authors", 255, repositoryEntry.getAuthors(), formLayout);
		authors.setDisplaySize(60);
		
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			TaxonomyRef taxonomyRef = new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey));
			initFormTaxonomy(formLayout, taxonomyRef);
		}
		
		language = uifactory.addTextElement("cif.mainLanguage", "cif.mainLanguage", 16, repositoryEntry.getMainLanguage(), formLayout);
		
		expenditureOfWork = uifactory.addTextElement("cif.expenditureOfWork", "cif.expenditureOfWork", 100, repositoryEntry.getExpenditureOfWork(), formLayout);
		expenditureOfWork.setExampleKey("details.expenditureOfWork.example", null);
		
		uifactory.addSpacerElement("spacer2", formLayout, false);
		
		if (licenseModule.isEnabled(licenseHandler)) {
			license = licenseService.loadOrCreateLicense(res);

			LicenseSelectionConfig licenseSelectionConfig = LicenseUIFactory
					.createLicenseSelectionConfig(licenseHandler, license.getLicenseType());
			licenseEl = uifactory.addDropdownSingleselect("cif.license", formLayout,
					licenseSelectionConfig.getLicenseTypeKeys(),
					licenseSelectionConfig.getLicenseTypeValues(getLocale()));
			licenseEl.setElementCssClass("o_sel_repo_license");
			licenseEl.setMandatory(licenseSelectionConfig.isLicenseMandatory());
			if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
				licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
			}
			licenseEl.addActionListener(FormEvent.ONCHANGE);
			
			licensorEl = uifactory.addTextElement("cif.licensor", 1000, license.getLicensor(), formLayout);

			String freetext = licenseService.isFreetext(license.getLicenseType()) ? license.getFreetext() : "";
			licenseFreetextEl = uifactory.addTextAreaElement("cif.freetext", 4, 72, freetext, formLayout);
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
		}

		boolean managed = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		FormSubmit submit = uifactory.addFormSubmitButton("submit", buttonContainer);
		submit.setVisible(!managed);
	}

	private void initFormTaxonomy(FormItemContainer formLayout, TaxonomyRef taxonomyRef) {
		taxonomyLevels = repositoryEntry.getTaxonomyLevels().stream()
				.map(RepositoryEntryToTaxonomyLevel::getTaxonomyLevel)
				.collect(Collectors.toSet());
		List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRef);

		KeyValues keyValues = new KeyValues();
		for (TaxonomyLevel level:allTaxonomyLevels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			addParentNames(names, level);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(key, value));
		}
		keyValues.sort(VALUE_ASC);
	
		taxonomyLevelEl = uifactory.addCheckboxesDropdown("taxonomyLevels", "cif.taxonomy.levels", formLayout,
				keyValues.keys(), keyValues.values(), null, null);
		List<TaxonomyLevel> reLevels = repositoryService.getTaxonomy(repositoryEntry);
		for (TaxonomyLevel reLevel : reLevels) {
			String key = reLevel.getKey().toString();
			if (keyValues.containsKey(key)) {
				taxonomyLevelEl.select(key, true);
			}
		}
	}
	
	private void addParentNames(List<String> names, TaxonomyLevel level) {
		names.add(level.getDisplayName());
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentNames(names, parent);
		}
	}

	@Override
	protected void doDispose() {
		// Controllers autodisposed by basic controller
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateTextElement(language, 255);
		allOk &= validateTextElement(expenditureOfWork, 225);
		allOk &= validateTextElement(authors, 2000);

		if (licenseEl != null) {
			licenseEl.clearError();
			if (LicenseUIFactory.validateLicenseTypeMandatoryButNonSelected(licenseEl)) {
				licenseEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength) {
		boolean ok;
		if(el == null) {
			ok = true;
		} else {
			String val = el.getValue();
			el.clearError();
			if(val != null && val.length() > maxLength) {
				el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
				ok = false;
			} else {
				ok = true;
			}
		}
		return ok;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == licenseEl) {
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (licenseModule.isEnabled(licenseHandler)) {
			if (licenseEl != null && licenseEl.isOneSelected()) {
				String licenseTypeKey = licenseEl.getSelectedKey();
				LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
				license.setLicenseType(licneseType);
			}
			String licensor = null;
			String freetext = null;
			if (licensorEl != null && licensorEl.isVisible()) {
				licensor = StringHelper.containsNonWhitespace(licensorEl.getValue())? licensorEl.getValue(): null;
			}
			if (licenseFreetextEl != null && licenseFreetextEl.isVisible()) {
				freetext = StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())? licenseFreetextEl.getValue(): null;
			}
			license.setLicensor(licensor);
			license.setFreetext(freetext);
			license = licenseService.update(license);
			licensorEl.setValue(license.getLicensor());
			licenseFreetextEl.setValue(license.getFreetext());
		}

		String mainLanguage = language.getValue();
		if(StringHelper.containsNonWhitespace(mainLanguage)) {
			repositoryEntry.setMainLanguage(mainLanguage);
		} else {
			repositoryEntry.setMainLanguage(null);
		}

		if(authors != null) {
			String auth = authors.getValue().trim();
			repositoryEntry.setAuthors(auth);
		}
		if(expenditureOfWork != null) {
			String exp = expenditureOfWork.getValue().trim();
			repositoryEntry.setExpenditureOfWork(exp);
		}
		
		// Taxonomy levels
		if (taxonomyLevelEl != null) {
			Collection<String> selectedLevelKeys = taxonomyLevelEl.getSelectedKeys();
			List<String> currentKeys = taxonomyLevels.stream()
					.map(l -> l.getKey().toString())
					.collect(Collectors.toList());
			// add newly selected keys
			Collection<String> addKeys = new HashSet<>(selectedLevelKeys);
			addKeys.removeAll(currentKeys);
			for (String addKey : addKeys) {
				TaxonomyLevel level = taxonomyService.getTaxonomyLevel(() -> Long.valueOf(addKey));
				taxonomyLevels.add(level);
			}
			// remove newly unselected keys
			Collection<String> removeKeys = new HashSet<>(currentKeys);
			removeKeys.removeAll(selectedLevelKeys);
			for (String removeKey: removeKeys) {
				taxonomyLevels.removeIf(level -> removeKey.equals(level.getKey().toString()));
			}
		}

		repositoryEntry = repositoryManager.setDescriptionAndName(repositoryEntry,
				repositoryEntry.getDisplayname(), repositoryEntry.getExternalRef(), repositoryEntry.getAuthors(),
				repositoryEntry.getDescription(), repositoryEntry.getObjectives(), repositoryEntry.getRequirements(),
				repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(), repositoryEntry.getLocation(),
				repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(), null, taxonomyLevels);
		if(repositoryEntry == null) {
			showWarning("repositoryentry.not.existing");
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else {
			fireEvent(ureq, Event.CHANGED_EVENT);
			MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, getIdentity(), Change.modifiedDescription, "authoring");
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}