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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.license.License;
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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.course.CourseModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
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

	private final boolean readOnly;
	private final boolean showHeading;
	private final boolean usedInWizard;
	private ResourceLicense license;
	private RepositoryEntry repositoryEntry;
	private Set<TaxonomyLevel> taxonomyLevels;

	private TextElement authors;
	private TextElement language;
	private SingleSelection educationalTypeEl;
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
	private RepositoryHandlerFactory repositoryHandlerFactory;
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
	public RepositoryEntryMetadataController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly, boolean showHeading) {
		super(ureq, wControl);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		this.readOnly = readOnly;
		this.showHeading = showHeading;
		this.usedInWizard = false;
		initForm(ureq);
	}
	
	public RepositoryEntryMetadataController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		this(ureq, wControl, entry, readOnly, true);
	}
	
	/**
	 * Used in wizard 
	 * 
	 * @param ureq
	 * @param wControl
	 * @param entry
	 * @param rootForm
	 */
	public RepositoryEntryMetadataController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10, null, rootForm);
		setBasePackage(RepositoryService.class);
		this.repositoryEntry = entry;
		this.usedInWizard = true;
		this.readOnly = false;
		this.showHeading = false;
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
		formLayout.setElementCssClass("o_sel_repo_metadata");
		if (showHeading) {
			setFormContextHelp("manual_user/authoring/Set_up_info_page/");
			setFormTitle("details.metadata.title");
		}
		
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
		
		if (!usedInWizard) {
			
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
		}

		authors = uifactory.addTextElement("cif.authors", "cif.authors", 255, repositoryEntry.getAuthors(), formLayout);
		authors.setDisplaySize(60);
		authors.setEnabled(!readOnly);
		
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			TaxonomyRef taxonomyRef = new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey));
			initFormTaxonomy(formLayout, taxonomyRef);
		}
		
		if (!usedInWizard && CourseModule.ORES_TYPE_COURSE.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			SelectionValues educationalTypeKV = new SelectionValues();
			repositoryManager.getAllEducationalTypes()
					.forEach(type -> educationalTypeKV.add(entry(type.getIdentifier(), translate(RepositoyUIFactory.getI18nKey(type)))));
			educationalTypeKV.sort(SelectionValues.VALUE_ASC);
			educationalTypeEl = uifactory.addDropdownSingleselect("cif.educational.type", formLayout, educationalTypeKV.keys(), educationalTypeKV.values());
			educationalTypeEl.enableNoneSelection();
			RepositoryEntryEducationalType educationalType = repositoryEntry.getEducationalType();
			if (educationalType != null && Arrays.asList(educationalTypeEl.getKeys()).contains(educationalType.getIdentifier())) {
				educationalTypeEl.select(educationalType.getIdentifier(), true);
			}
			educationalTypeEl.setEnabled(!readOnly);
		}
		
		if (!usedInWizard) {
			language = uifactory.addTextElement("cif.mainLanguage", "cif.mainLanguage", 16, repositoryEntry.getMainLanguage(), formLayout);
			language.setEnabled(!readOnly);
		
			expenditureOfWork = uifactory.addTextElement("cif.expenditureOfWork", "cif.expenditureOfWork", 100, repositoryEntry.getExpenditureOfWork(), formLayout);
			expenditureOfWork.setExampleKey("details.expenditureOfWork.example", null);
			expenditureOfWork.setEnabled(!readOnly);
			
			uifactory.addSpacerElement("spacer2", formLayout, false);
		}
		
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
			licenseEl.setEnabled(!readOnly);
			
			licensorEl = uifactory.addTextElement("cif.licensor", 1000, license.getLicensor(), formLayout);
			licensorEl.setEnabled(!readOnly);

			String freetext = licenseService.isFreetext(license.getLicenseType()) ? license.getFreetext() : "";
			licenseFreetextEl = uifactory.addTextAreaElement("cif.freetext", 4, 72, freetext, formLayout);
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
			licenseFreetextEl.setEnabled(!readOnly);

			RepositoryHandler repositoryHandler = repositoryHandlerFactory.getRepositoryHandler(repositoryEntry);
			List<License> elementsLicenses = repositoryHandler.getElementsLicenses(repositoryEntry);
			if(!elementsLicenses.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				buildLicensesList(sb, elementsLicenses);
				String licensesText = translate("cif.license.elements.content", new String[] { sb.toString(), typeDisplay });
				uifactory.addStaticTextElement("cif.license.elements", "cif.license.elements", licensesText, formLayout);
			}
		}

		if (!usedInWizard) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
	
			FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
			formLayout.add("buttonContainer", buttonContainer);
			buttonContainer.setElementCssClass("o_sel_repo_save_details");
			buttonContainer.setVisible(!readOnly);
			uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
			FormSubmit submit = uifactory.addFormSubmitButton("submit", buttonContainer);
			submit.setVisible(!managed && !readOnly);
		}
	}
	
	private void buildLicensesList(StringBuilder sb, List<License> elementsLicenses) {
		Set<String> deduplicates = new HashSet<>();
		sb.append("<ul>");
		for(License elementLicense:elementsLicenses) {
			String text = elementLicense.getFreetext();
			if(!StringHelper.containsNonWhitespace(text) && elementLicense.getLicenseType() != null) {
				LicenseType licenseType = elementLicense.getLicenseType();
				text = LicenseUIFactory.translate(licenseType, getLocale());
			}
			if(StringHelper.containsNonWhitespace(text) && !deduplicates.contains(text)) {
				sb.append("<li>").append(StringHelper.escapeHtml(text)).append("</li>");
				deduplicates.add(text);
			}	
		}
		sb.append("</ul>");
	}

	private void initFormTaxonomy(FormItemContainer formLayout, TaxonomyRef taxonomyRef) {
		List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRef);
		taxonomyLevels = new HashSet<>(repositoryService.getTaxonomy(repositoryEntry));

		SelectionValues keyValues = RepositoyUIFactory.createTaxonomyLevelKV(allTaxonomyLevels);
		taxonomyLevelEl = uifactory.addCheckboxesDropdown("taxonomyLevels", "cif.taxonomy.levels", formLayout,
				keyValues.keys(), keyValues.values(), null, null);
		RepositoyUIFactory.selectTaxonomyLevels(taxonomyLevelEl, taxonomyLevels);
		taxonomyLevelEl.setEnabled(!readOnly);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= RepositoyUIFactory.validateTextElement(language, false, 255);
		allOk &= RepositoyUIFactory.validateTextElement(expenditureOfWork, false, 225);
		allOk &= RepositoyUIFactory.validateTextElement(authors, false, 2000);

		if (licenseEl != null) {
			licenseEl.clearError();
			if (LicenseUIFactory.validateLicenseTypeMandatoryButNonSelected(licenseEl)) {
				licenseEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk;
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
		if (!usedInWizard) {
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
			
			if (educationalTypeEl != null) {
				String identifier = educationalTypeEl.isOneSelected()? educationalTypeEl.getSelectedKey(): null;
				RepositoryEntryEducationalType educationalType = repositoryManager.getEducationalType(identifier);
				repositoryEntry.setEducationalType(educationalType);
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
					repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(), null, taxonomyLevels,
					repositoryEntry.getEducationalType());
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
	}
	
	public boolean saveToContext(UserRequest ureq, CopyCourseContext context) {
		String licenseTypeKey = null;
		String licensor = null;
		String freetext = null;
		
		String authorsValue = null;
		
		String expenditureOfWorkValue = null;
		
		if (validateFormLogic(ureq)) {
			if (licenseModule.isEnabled(licenseHandler)) {
				if (licenseEl != null && licenseEl.isOneSelected()) {
					licenseTypeKey = licenseEl.getSelectedKey();
				}
				
				if (licensorEl != null && licensorEl.isVisible()) {
					licensor = StringHelper.containsNonWhitespace(licensorEl.getValue())? licensorEl.getValue(): null;
				}
				if (licenseFreetextEl != null && licenseFreetextEl.isVisible()) {
					freetext = StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())? licenseFreetextEl.getValue(): null;
				}
			}
			
	
			if(authors != null) {
				authorsValue = authors.getValue().trim();
			}
			
			if(expenditureOfWork != null) {
				expenditureOfWorkValue = expenditureOfWork.getValue().trim();
			}
			
			context.setAuthors(authorsValue);
			context.setExpenditureOfWork(expenditureOfWorkValue);
			context.setLicenseTypeKey(licenseTypeKey);
			context.setLicensor(licensor);
			context.setLicenseFreetext(freetext);
			
			return true;
		} else {
			return false;
		}
	}
	
	public void loadFromContext(CopyCourseContext context) {
		
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}