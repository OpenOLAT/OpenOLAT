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
import java.util.Iterator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportURLRepositoryEntryController extends FormBasicController {

	private String[] limitTypes;
	private RepositoryEntry importedEntry;
	private final List<Organisation> manageableOrganisations;
	
	private String handlerForUrl;
	private List<ResourceHandler> handlerForUploadedResources;
	
	private TextElement urlEl;
	private SingleSelection selectType;
	private TextElement displaynameEl;
	private SingleSelection organisationEl;
	
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public ImportURLRepositoryEntryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager);
		
		initForm(ureq);
	}
	
	public ImportURLRepositoryEntryController(UserRequest ureq, WindowControl wControl, String[] limitTypes) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.limitTypes = limitTypes;
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("cmd.import.ressource.url.desc");
		
		urlEl = uifactory.addTextElement("upload", "upload.url", 128, null, formLayout);
		urlEl.setFocus(true);

		SpacerElement spacerEl = uifactory.addSpacerElement("spacer1", formLayout, false);
		spacerEl.setVisible(false);
		
		selectType = uifactory.addDropdownSingleselect("cif.types", "cif.type", formLayout, new String[0], new String[0], null);
		selectType.setVisible(false);

		displaynameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, "", formLayout);
		displaynameEl.setDisplaySize(30);
		displaynameEl.setMandatory(true);
		displaynameEl.setElementCssClass("o_sel_author_imported_name");
		
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
		organisationEl.setVisible(organisationKeys.size() > 1);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		uifactory.addFormSubmitButton("cmd.import.ressource", buttonContainer);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public RepositoryEntry getImportedEntry() {
		return importedEntry;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		updateHandlerForUrl();
		if(validateResources()) {
			doImport();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void updateHandlerForUrl() {
		String url = urlEl.getValue();
		ResourceEvaluation validEvaluation = null;
		if(handlerForUrl == null || !handlerForUrl.equals(url)) {
			List<ResourceHandler> handlers = new ArrayList<>(3);
			for(String type:repositoryHandlerFactory.getSupportedTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				ResourceEvaluation eval = handler.acceptImport(url);
				if(eval != null && eval.isValid()) {
					validEvaluation = eval;
					handlers.add(new ResourceHandler(eval, handler));
				}
			}
			
			handlerForUploadedResources = handlers;
		} else {
			handlerForUploadedResources = null;
		}
		
		if(handlerForUploadedResources == null || handlerForUploadedResources.isEmpty()) {
			selectType.setKeysAndValues(new String[0], new String[0], null);
			selectType.setVisible(false);
		} else {
			String selectedKey = null;
			if(selectType.isOneSelected()) {
				selectedKey = selectType.getSelectedKey();
			}

			int numOfHandlers = handlerForUploadedResources.size();
			String[] keys = new String[numOfHandlers];
			String[] values = new String[numOfHandlers];
			for(int i=0; i<numOfHandlers; i++) {
				String type = handlerForUploadedResources.get(i).getHandler().getSupportedType();
				keys[i] = type;
				values[i] = NewControllerFactory.translateResourceableTypeName(type, getLocale());
			}
			selectType.setKeysAndValues(keys, values, null);
			
			if(handlerForUploadedResources.size() == 1) {
				selectType.select(keys[0], true);
				selectType.setVisible(true);
			} else {
				if(selectedKey != null) {
					for(String key:keys) {
						if(selectedKey.equals(key)) {
							selectType.select(key, true);
						}
					}
				}
				selectType.setVisible(true);
			}
		}
		
		if(validEvaluation != null && StringHelper.containsNonWhitespace(validEvaluation.getDisplayname())
				&& !StringHelper.containsNonWhitespace(displaynameEl.getValue())) {
			String displayname = validEvaluation.getDisplayname();
			if(displayname != null) {
				if(displayname.length() > 100) {
					displayname = displayname.substring(0, 100);
				}
				displaynameEl.setValue(displayname);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		updateHandlerForUrl();
		
		boolean allOk = super.validateFormLogic(ureq);

		organisationEl.clearError();
		if(organisationEl.isVisible() && !organisationEl.isOneSelected()) {
			organisationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (!StringHelper.containsNonWhitespace(displaynameEl.getValue())) {
			displaynameEl.setErrorKey("cif.error.displayname.empty", new String[] {});
			allOk = false;
		} else if (displaynameEl.hasError()) {
			allOk = false;
		} else {
			displaynameEl.clearError();
		}

		return allOk;
	}
	
	private boolean validateResources() {
		boolean allOk = true;
		allOk &= validLimitationOnType(handlerForUploadedResources);
		allOk &= validateHandlers(handlerForUploadedResources);
		return allOk;
	}
	
	private boolean validateHandlers(List<ResourceHandler> handlers) {
		boolean allOk = true;
		
		urlEl.clearError();
		selectType.clearError();
		if(handlers == null || handlers.isEmpty()) {
			urlEl.setErrorKey("add.failed", null);
			allOk &= false;
		} else if(handlers.size() > 1) {
			if(!selectType.isOneSelected()) {
				selectType.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validLimitationOnType(List<ResourceHandler> handlers) {
		boolean allOk = true;
		
		if(limitTypes != null && handlers != null) {
			for(Iterator<ResourceHandler> handlerIt=handlers.iterator(); handlerIt.hasNext(); ) {
				boolean match = false;
				ResourceHandler handler = handlerIt.next();
				for(String limitType:limitTypes) {
					if(limitType.equals(handler.getHandler().getSupportedType())) {
						match = true;
					}
				}
				if(!match) {
					handlerIt.remove();
				}
			}
			if(handlers.isEmpty()) {
				allOk = false;
				urlEl.setErrorKey("add.failed", new String[] {});
			}
		}
		
		return allOk;
	}
	
	private void doImport() {
		RepositoryHandler handler;
		ResourceEvaluation evaluation = null;
		if(handlerForUploadedResources == null || handlerForUploadedResources.isEmpty()) {
			handler = null;
		} else if(handlerForUploadedResources.size() == 1) {
			handler = handlerForUploadedResources.get(0).getHandler();
			evaluation = handlerForUploadedResources.get(0).getEvaluation();
		} else if(selectType.isOneSelected()){
			String type = selectType.getSelectedKey();
			handler = repositoryHandlerFactory.getRepositoryHandler(type);
		} else {
			handler = null;
		}
		
		if(handler != null) {
			Organisation organisation;
			if(organisationEl.isOneSelected()) {
				Long organisationKey = Long.valueOf(organisationEl.getSelectedKey());
				organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
			} else {
				organisation = organisationService.getDefaultOrganisation();
			}

			String displayname = displaynameEl.getValue();
			if(!StringHelper.containsNonWhitespace(displayname) && evaluation != null
					&& StringHelper.containsNonWhitespace(evaluation.getDisplayname())) {
				displayname = evaluation.getDisplayname();
			}
			if(displayname.length() > 100) {
				displayname = displayname.substring(0, 100);
			}
			String url = urlEl.getValue();
			importedEntry = handler.importResource(getIdentity(), null, displayname,
					"", organisation, getLocale(), url);
			
			if(evaluation != null) {
				String expenditureOfWork = evaluation.getDuration() == null
						? null : Formatter.formatTimecode(evaluation.getDuration().longValue() * 1000l);
				importedEntry = repositoryManager.setDescriptionAndName(importedEntry, displayname, null,
						evaluation.getAuthors(), evaluation.getDescription(), null, null, null, null,
						null, expenditureOfWork, null, null, null, null);
				if(StringHelper.containsNonWhitespace(evaluation.getLicense())) {
					updateLicense(importedEntry, evaluation.getLicensor(), evaluation.getLicense());
				}
			}
			
			if(importedEntry == null) {
				showWarning("error.import");
			} else {
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
						LoggingResourceable.wrap(importedEntry, OlatResourceableType.genRepoEntry));
				
				repositoryManager.triggerIndexer(importedEntry);
			}
		}
	}

	private void updateLicense(RepositoryEntry importedEntry, String licensor, String licenseName) {
		LicenseType licenseType = licenseService.loadLicenseTypeByName(licenseName);
		if(licenseType != null) {
			ResourceLicense license = licenseService.loadOrCreateLicense(importedEntry.getOlatResource());
			license.setLicenseType(licenseType);
			license.setLicensor(licensor);
			licenseService.update(license);
		}
	}
	
	private class ResourceHandler {
		
		private final RepositoryHandler handler;
		private final ResourceEvaluation evaluation;

		public ResourceHandler(ResourceEvaluation evaluation, RepositoryHandler handler) {
			this.handler = handler;
			this.evaluation = evaluation;
		}

		public RepositoryHandler getHandler() {
			return handler;
		}

		public ResourceEvaluation getEvaluation() {
			return evaluation;
		}
	}
}