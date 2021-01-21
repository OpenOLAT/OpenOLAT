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

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
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
	
	private TextElement displaynameEl;
	private SingleSelection organisationEl;
	private FormLayoutContainer exampleHelpEl;
	private SingleSelection wizardEl;
	
	protected RepositoryEntry addedEntry;
	private final RepositoryHandler handler;
	private final boolean wizardsEnabled;
	private final List<Organisation> manageableOrganisations;
	
	private Object createObject;
	private LicenseType licenseType;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	protected RepositoryManager repositoryManager;
	@Autowired
	private RepositoryWizardService wizardService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;

	public CreateRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryHandler handler, boolean wizardsEnabled) {
		super(ureq, wControl);
		this.wizardsEnabled = wizardsEnabled;
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.handler = handler;
		
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		initForm(ureq);
	}

	@Override
	public RepositoryEntry getAddedEntry() {
		return addedEntry;
	}

	@Override
	public RepositoryHandler getHandler() {
		return handler;
	}
	
	@Override
	public RepositoryWizardProvider getWizardProvider() {
		return wizardEl != null && wizardEl.isOneSelected()
				? wizardService.getProvider(wizardEl.getSelectedKey())
				: null;
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
	
	public void setExampleAndHelp(String text, String helpUrl) {
		exampleHelpEl.contextPut("text", text);
		exampleHelpEl.contextPut("helpUrl", helpUrl);
		exampleHelpEl.setVisible(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String typeName;
		if (handler != null) {
			typeName = NewControllerFactory.translateResourceableTypeName(handler.getSupportedType(), getLocale());
		} else {
			typeName = translate("cif.type.na");
		}
		StaticTextElement typeEl = uifactory.addStaticTextElement("cif.type", typeName, formLayout);
		typeEl.setElementCssClass("o_sel_author_type");
		
		displaynameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, "", formLayout);
		displaynameEl.setElementCssClass("o_sel_author_displayname");
		displaynameEl.setFocus(true);
		displaynameEl.setDisplaySize(30);
		displaynameEl.setMandatory(true);
		
		String page = velocity_root + "/example_help.html";
		exampleHelpEl = FormLayoutContainer.createCustomFormLayout("example.help", "example.help", getTranslator(), page);
		formLayout.add(exampleHelpEl);
		exampleHelpEl.setVisible(false);
		
		List<String> organisationKeys = new ArrayList<>();
		List<String> organisationValues = new ArrayList<>();
		for(Organisation organisation:manageableOrganisations) {
			organisationKeys.add(organisation.getKey().toString());
			organisationValues.add(organisation.getDisplayName());
		}
		organisationEl = uifactory.addDropdownSingleselect("cif.organisations", "cif.organisations",
				formLayout, organisationKeys.toArray(new String[organisationKeys.size()]), organisationValues.toArray(new String[organisationValues.size()]));
		if(!organisationKeys.isEmpty()) {
			organisationEl.select(organisationKeys.get(0), true);
		}
		organisationEl.setVisible(organisationKeys.size() > 1 && organisationModule.isEnabled());
		
		initAdditionalFormElements(formLayout, listener, ureq);
		
		if (wizardsEnabled) {
			List<RepositoryWizardProvider> wizardProviders = wizardService.getProviders(handler.getSupportedType());
			if (!wizardProviders.isEmpty()) {
				KeyValues wizardKV = new KeyValues();
				wizardProviders.forEach(provider -> wizardKV.add(KeyValues.entry(provider.getType(), provider.getDisplayName(getLocale()))));
				wizardKV.sort(KeyValues.VALUE_ASC);
				wizardEl = uifactory.addRadiosVertical("csc.wizard", formLayout, wizardKV.keys(), wizardKV.values());
				wizardEl.enableNoneSelection();
			}
		}
		
		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		FormSubmit submit = uifactory.addFormSubmitButton("cmd.create.ressource", buttonContainer);
		submit.setElementCssClass("o_sel_author_create_submit");
		
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	@SuppressWarnings("unused")
	protected void initAdditionalFormElements(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// 
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationEl.clearError();
		if(organisationEl.isVisible() && !organisationEl.isOneSelected()) {
			organisationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		displaynameEl.clearError();
		if (!StringHelper.containsNonWhitespace(displaynameEl.getValue())) {
			displaynameEl.setErrorKey("cif.error.displayname.empty", new String[] {});
			allOk &= false;
		} else if (displaynameEl.hasError()) {
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doCreate();
		if (wizardEl != null && wizardEl.isOneSelected()) {
			fireEvent(ureq, CREATION_WIZARD);
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		fireEvent(ureq, new EntryChangedEvent(addedEntry, getIdentity(), Change.added, "create"));
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
		
		addedEntry = handler.createResource(getIdentity(), displayname, "", createObject, organisation, getLocale());
		if (licenseModule.isEnabled(licenseHandler)) {
			if(licenseType != null) {
				ResourceLicense license = licenseService.loadOrCreateLicense(addedEntry.getOlatResource());
				license.setLicenseType(licenseType);
				licenseService.update(license);
			} else {
				licenseService.createDefaultLicense(addedEntry.getOlatResource(), licenseHandler, getIdentity());
			}
		}
		
		afterEntryCreated();
		dbInstance.commit();

		repositoryManager.triggerIndexer(addedEntry);

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
				LoggingResourceable.wrap(addedEntry, OlatResourceableType.genRepoEntry));
	}

	protected void afterEntryCreated() {
		// May be overridden.
	}
}
