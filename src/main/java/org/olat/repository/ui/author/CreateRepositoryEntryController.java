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
package org.olat.repository.ui.author;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.wizard.RepositoryWizardProvider;
import org.olat.repository.wizard.RepositoryWizardService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateRepositoryEntryController extends FormBasicController implements CreateEntryController {
	
	public static final Event CREATION_WIZARD = new Event("start_wizard");
	
	private static final String[] dateKeys = new String[]{ "none", "private", "public"};
	private static final String CMD_WIZARD = "wizard";
	
	private TextElement displaynameEl;
	private TextElement externalRef;
	private SingleSelection dateTypesEl;
	private SingleSelection publicDatesEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private FormLayoutContainer privateDatesCont;
	private TaxonomyLevelSelection taxonomyLevelEl;
	private SingleSelection educationalTypeEl;
	private SingleSelection organisationEl;
	
	protected RepositoryEntry repositoryEntry;
	private final RepositoryHandler handler;
	private final boolean wizardsEnabled;
	private final List<Organisation> manageableOrganisations;
	private RepositoryWizardProvider wizardProvider;
	
	private Object createObject;
	private LicenseType licenseType;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	protected RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryWizardService wizardService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	
	public CreateRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryHandler handler, boolean wizardsEnabled) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.wizardsEnabled = wizardsEnabled;
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.handler = handler;
		
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		initForm(ureq);
	}
	
	protected boolean hasLifecycle() {
		return false;
	}

	protected boolean hasEducationalType() {
		return false;
	}
	
	@SuppressWarnings("unused")
	protected void initAdditionalFormElements(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// 
	}

	@Override
	public RepositoryEntry getAddedEntry() {
		return repositoryEntry;
	}

	@Override
	public RepositoryHandler getHandler() {
		return handler;
	}
	
	@Override
	public RepositoryWizardProvider getWizardProvider() {
		return wizardProvider;
	}

	@Override
	public void setCreateObject(Object createObject) {
		this.createObject = createObject;
	}

	@Override
	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public void setDisplayname(String displayname) {
		displaynameEl.setValue(displayname);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("default", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		displaynameEl = uifactory.addTextElement("cif.title", 100, "", generalCont);
		displaynameEl.setElementCssClass("o_sel_author_displayname");
		displaynameEl.setFocus(true);
		displaynameEl.setDisplaySize(30);
		displaynameEl.setMandatory(true);
		displaynameEl.addActionListener(FormEvent.ONCHANGE);
		
		externalRef = uifactory.addTextElement("cif.externalref", 255, null, generalCont);
		externalRef.setHelpText(translate("cif.externalref.hover"));
		externalRef.setHelpUrlForManualPage("manual_user/authoring/Set_up_info_page/");
		externalRef.addActionListener(FormEvent.ONCHANGE);
		
		if (hasLifecycle()) {
			initLifecycle(generalCont);
		}
		
		List<TaxonomyRef> taxonomyRefs = repositoryModule.getTaxonomyRefs();
		if (taxonomyModule.isEnabled() && !taxonomyRefs.isEmpty()) {
			Set<TaxonomyLevel> allTaxonomieLevels = new HashSet<>(taxonomyService.getTaxonomyLevels(taxonomyRefs));
			
			String labelI18nKey = catalogModule.isEnabled()? "cif.taxonomy.levels.catalog": "cif.taxonomy.levels";
			taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomyLevel", labelI18nKey, generalCont,
					getWindowControl(), allTaxonomieLevels);
			taxonomyLevelEl.setDisplayNameHeader(translate(labelI18nKey));
			if (catalogModule.isEnabled()) {
				taxonomyLevelEl.setHelpTextKey("cif.taxonomy.levels.help.catalog", null);
			}
		}
		
		if (hasEducationalType()) {
			SelectionValues educationalTypeKV = new SelectionValues();
			repositoryManager.getAllEducationalTypes()
					.forEach(type -> educationalTypeKV.add(entry(type.getIdentifier(), translate(RepositoyUIFactory.getI18nKey(type)))));
			educationalTypeKV.sort(SelectionValues.VALUE_ASC);
			educationalTypeEl = uifactory.addDropdownSingleselect("cif.educational.type", generalCont, educationalTypeKV.keys(), educationalTypeKV.values());
			educationalTypeEl.enableNoneSelection();
		}
		
		List<String> organisationKeys = new ArrayList<>();
		List<String> organisationValues = new ArrayList<>();
		for(Organisation organisation:manageableOrganisations) {
			organisationKeys.add(organisation.getKey().toString());
			organisationValues.add(organisation.getDisplayName());
		}
		organisationEl = uifactory.addDropdownSingleselect("cif.organisations", "cif.organisations",
				generalCont, organisationKeys.toArray(new String[organisationKeys.size()]), organisationValues.toArray(new String[organisationValues.size()]));
		if(!organisationKeys.isEmpty()) {
			organisationEl.select(organisationKeys.get(0), true);
		}
		organisationEl.setVisible(organisationKeys.size() > 1 && organisationModule.isEnabled());
		
		initAdditionalFormElements(formLayout, listener, ureq);
		
		// Buttons
		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_button_group_right o_sel_repo_save_details");
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		
		if (wizardsEnabled) {
			List<RepositoryWizardProvider> wizardProviders = wizardService.getProviders(handler.getSupportedType());
			if (!wizardProviders.isEmpty()) {
				FormLayoutContainer dummyCont = FormLayoutContainer.createBareBoneFormLayout("dummy", getTranslator());
				dummyCont.setRootForm(mainForm);
				
				DropdownItem wizardDropdown = uifactory.addDropdownMenu("cmd.create.wizard", null,
						buttonContainer, getTranslator());
				wizardDropdown.setOrientation(DropdownOrientation.right);
				wizardDropdown.setExpandContentHeight(true);
				
				for (RepositoryWizardProvider wizardProvider : wizardProviders) {
					FormLink link = uifactory.addFormLink(wizardProvider.getType(), CMD_WIZARD, null, null, dummyCont, Link.LINK + Link.NONTRANSLATED);
					link.setI18nKey(wizardProvider.getDisplayName(getLocale()));
					link.setUserObject(wizardProvider);
					wizardDropdown.addElement(link);
				}
			}
		}
		
		FormSubmit submit = uifactory.addFormSubmitButton("cmd.create.ressource", buttonContainer);
		submit.setElementCssClass("o_sel_author_create_submit");
	}
	
	private void initLifecycle(FormItemContainer formLayout) {
		String[] dateValues = new String[] {
				translate("cif.dates.none"),
				translate("cif.dates.private"),
				translate("cif.dates.public")
		};
		dateTypesEl = uifactory.addRadiosVertical("cif.dates", formLayout, dateKeys, dateValues);
		dateTypesEl.setHelpText(translate("cif.dates.help"));
		dateTypesEl.select("none", true);
		dateTypesEl.addActionListener(FormEvent.ONCHANGE);

		List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
		List<RepositoryEntryLifecycle> filteredCycles = new ArrayList<>();
		//just make the upcomming and acutual running cycles or the pre-selected visible in the UI
		LocalDateTime now = LocalDateTime.now();
		for(RepositoryEntryLifecycle cycle:cycles) {
			if(cycle.getValidTo() == null
					|| now.isBefore(LocalDateTime.ofInstant(cycle.getValidTo().toInstant(), ZoneId.systemDefault()))) {
				filteredCycles.add(cycle);
			}
		}
		
		String[] publicKeys = new String[filteredCycles.size()];
		String[] publicValues = new String[filteredCycles.size()];
		int count = 0;
		for(RepositoryEntryLifecycle cycle:filteredCycles) {
				publicKeys[count] = cycle.getKey().toString();
				
				StringBuilder sb = new StringBuilder(32);
				boolean labelAvailable = StringHelper.containsNonWhitespace(cycle.getLabel());
				if(labelAvailable) {
					sb.append(cycle.getLabel());
				}
				if(StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
					if(labelAvailable) sb.append(" - ");
					sb.append(cycle.getSoftKey());
				}
				publicValues[count++] = sb.toString();
		}
		publicDatesEl = uifactory.addDropdownSingleselect("cif.public.dates", formLayout, publicKeys, publicValues, null);

		String privateDatePage = Util.getPackageVelocityRoot(RepositoryService.class) + "/cycle_dates.html";
		privateDatesCont = FormLayoutContainer.createCustomFormLayout("private.date", getTranslator(), privateDatePage);
		privateDatesCont.setRootForm(mainForm);
		privateDatesCont.setLabel("cif.private.dates", null);
		formLayout.add("private.date", privateDatesCont);
		
		startDateEl = uifactory.addDateChooser("date.start", "cif.date.from", null, privateDatesCont);
		endDateEl = uifactory.addDateChooser("date.end", "cif.date.to", null, privateDatesCont);
		
		if (startDateEl != null && endDateEl != null) {
			startDateEl.addActionListener(FormEvent.ONCHANGE);
			endDateEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		updateDatesVisibility();
	}

	private void updateDatesVisibility() {
		if(dateTypesEl.isOneSelected()) {
			String type = dateTypesEl.getSelectedKey();
			if("none".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(false);
			} else if("public".equals(type)) {
				publicDatesEl.setVisible(true);
				privateDatesCont.setVisible(false);
			} else if("private".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(true);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == displaynameEl) {
			validateDisplaynameUnique(ureq);
		} else if (source == externalRef) {
			validateDisplaynameUnique(ureq);
			validateExtRefUnique(ureq);
		} else if (source == dateTypesEl) {
			updateDatesVisibility();
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_WIZARD.equals(link.getCmd())) {
				wizardProvider = (RepositoryWizardProvider)link.getUserObject();
				mainForm.submit(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		boolean displaynameOk = RepositoyUIFactory.validateTextElement(displaynameEl, true, 110);
		if (displaynameOk) {
			validateDisplaynameUnique(ureq);
		} else {
			allOk &= false;
		}
		
		boolean extRefOk = RepositoyUIFactory.validateTextElement(externalRef, false, 255);
		if (extRefOk) {
			validateExtRefUnique(ureq);
		} else {
			allOk &= false;
		}
		
		if (publicDatesEl != null) {
			publicDatesEl.clearError();
			if(publicDatesEl.isEnabled() && publicDatesEl.isVisible()) {
				if(!publicDatesEl.isOneSelected()) {
					publicDatesEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
			}
		}
		
		organisationEl.clearError();
		if(organisationEl.isVisible() && !organisationEl.isOneSelected()) {
			organisationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (!allOk) {
			wizardProvider = null;
		}
		
		return allOk;
	}

	private void validateDisplaynameUnique(UserRequest ureq) {
		displaynameEl.clearError();
		if (StringHelper.containsNonWhitespace(displaynameEl.getValue()) && !StringHelper.containsNonWhitespace(externalRef.getValue())) {
			SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
			params.setExactSearch(true);
			params.setDisplayname(displaynameEl.getValue().toLowerCase());
			if (repositoryService.countAuthorView(params) > 0) {
				displaynameEl.setErrorKey("error.exists.displayname", true);
			}
		}
	}
	private void validateExtRefUnique(UserRequest ureq) {
		externalRef.clearError();
		if (StringHelper.containsNonWhitespace(externalRef.getValue())) {
			SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
			params.setExactSearch(true);
			params.setReference(externalRef.getValue().toLowerCase());
			if (repositoryService.countAuthorView(params) > 0) {
				externalRef.setErrorKey("error.exists.ext.ref", true);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doCreate();
		if (wizardProvider != null) {
			fireEvent(ureq, CREATION_WIZARD);
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		fireEvent(ureq, new EntryChangedEvent(repositoryEntry, getIdentity(), Change.added, "create"));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doCreate() {
		String displayname = displaynameEl.getValue();
		
		Organisation organisation;
		if(organisationEl.isVisible() && organisationEl.isOneSelected()) {
			Long organisationKey = Long.valueOf(organisationEl.getSelectedKey());
			organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		} else if(manageableOrganisations.size() == 1) {
			organisation = organisationService.getOrganisation(manageableOrganisations.get(0));
		} else {
			organisation = organisationService.getDefaultOrganisation();
		}
		
		repositoryEntry = handler.createResource(getIdentity(), displayname, "", createObject, organisation, getLocale());
		
		String ref = externalRef.getValue().trim();
		repositoryEntry.setExternalRef(ref);
		
		// Life cycle
		if (hasLifecycle()) {
			String type = "none";
			if(dateTypesEl.isOneSelected()) {
				type = dateTypesEl.getSelectedKey();
			}
			
			if("none".equals(type)) {
				repositoryEntry.setLifecycle(null);
			} else if("public".equals(type)) {
				String key = publicDatesEl.getSelectedKey();
				if(StringHelper.isLong(key)) {
					Long cycleKey = Long.parseLong(key);
					RepositoryEntryLifecycle cycle = lifecycleDao.loadById(cycleKey);
					repositoryEntry.setLifecycle(cycle);
				}
			} else if("private".equals(type)) {
				Date start = startDateEl.getDate();
				Date end = endDateEl.getDate();
				RepositoryEntryLifecycle cycle = repositoryEntry.getLifecycle();
				if(cycle == null || !cycle.isPrivateCycle()) {
					String softKey = "lf_" + repositoryEntry.getSoftkey();
					cycle = lifecycleDao.create(repositoryEntry.getDisplayname(), softKey, true, start, end);
				} else {
					cycle.setValidFrom(start);
					cycle.setValidTo(end);
					cycle = lifecycleDao.updateLifecycle(cycle);
				}
				repositoryEntry.setLifecycle(cycle);
			}
		}
		
		// Taxonomy
		Set<TaxonomyLevel> taxonomyLevels = null;
		if (taxonomyLevelEl != null && !taxonomyLevelEl.getSelection().isEmpty()) {
			taxonomyLevels = Set.copyOf(taxonomyService.getTaxonomyLevelsByRefs(taxonomyLevelEl.getSelection()));
		}
		
		repositoryEntry = repositoryManager.setDescriptionAndName(repositoryEntry,
				repositoryEntry.getDisplayname(), repositoryEntry.getExternalRef(), repositoryEntry.getAuthors(),
				repositoryEntry.getDescription(), repositoryEntry.getTeaser(), repositoryEntry.getObjectives(),
				repositoryEntry.getRequirements(), repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(),
				repositoryEntry.getLocation(), repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(),
				null, taxonomyLevels, repositoryEntry.getEducationalType());
		
		if (educationalTypeEl != null) {
			String identifier = educationalTypeEl.isOneSelected()? educationalTypeEl.getSelectedKey(): null;
			RepositoryEntryEducationalType educationalType = repositoryManager.getEducationalType(identifier);
			repositoryEntry.setEducationalType(educationalType);
		}
		
		if (licenseModule.isEnabled(licenseHandler)) {
			if(licenseType != null) {
				ResourceLicense license = licenseService.loadOrCreateLicense(repositoryEntry.getOlatResource());
				license.setLicenseType(licenseType);
				licenseService.update(license);
			} else {
				licenseService.createDefaultLicense(repositoryEntry.getOlatResource(), licenseHandler, getIdentity());
			}
		}
		
		afterEntryCreated();
		dbInstance.commit();

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
				LoggingResourceable.wrap(repositoryEntry, OlatResourceableType.genRepoEntry));
	}

	protected void afterEntryCreated() {
		// May be overridden.
	}
}
