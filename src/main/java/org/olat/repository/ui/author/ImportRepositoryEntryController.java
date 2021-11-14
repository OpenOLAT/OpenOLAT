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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
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
 * Initial date: 30.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportRepositoryEntryController extends FormBasicController {
	
	private static final String[] refKeys = new String[]{ "checked" };
	
	private String[] limitTypes;
	private RepositoryEntry importedEntry;
	private final List<Organisation> manageableOrganisations;
	private List<ResourceHandler> handlerForUploadedResources;
	
	private FormSubmit importButton;
	private SingleSelection selectType;
	private FileElement uploadFileEl;
	private StaticTextElement typeEl;
	private TextElement displaynameEl;
	private SingleSelection organisationEl;
	private MultipleSelectionElement referencesEl;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public ImportRepositoryEntryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager);
		
		initForm(ureq);
	}
	
	public ImportRepositoryEntryController(UserRequest ureq, WindowControl wControl, String[] limitTypes) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.limitTypes = limitTypes;
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("cmd.import.ressource.desc");
		
		uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", "import.file", formLayout);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);

		
		SpacerElement spacerEl = uifactory.addSpacerElement("spacer1", formLayout, false);
		spacerEl.setVisible(false);
		
		typeEl = uifactory.addStaticTextElement("cif.type", "cif.type", "", formLayout);
		typeEl.setElementCssClass("o_sel_author_type");
		typeEl.setVisible(false);
		
		selectType = uifactory.addDropdownSingleselect("cif.types", "cif.type", formLayout, new String[0], new String[0], null);
		selectType.addActionListener(FormEvent.ONCHANGE);
		selectType.setVisible(false);
		
		displaynameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, "", formLayout);
		displaynameEl.setDisplaySize(30);
		displaynameEl.setMandatory(true);
		displaynameEl.setVisible(false);
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

		String[] refValues = new String[]{ translate("references.expl") };
		referencesEl = uifactory.addCheckboxesHorizontal("references", "references", formLayout, refKeys, refValues);
		referencesEl.setVisible(false);
		
		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		importButton = uifactory.addFormSubmitButton("cmd.import.ressource", buttonContainer);
		importButton.setEnabled(false);

		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	public RepositoryEntry getImportedEntry() {
		return importedEntry;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(uploadFileEl == source) {
			doAnalyseUpload();
		} else if(selectType == source) {
			if(selectType.isOneSelected()) {
				String type = selectType.getSelectedKey();
				for(ResourceHandler handler:handlerForUploadedResources) {
					if(type.equals(handler.getHandler().getSupportedType())) {
						boolean references = handler.getEval().isReferences();
						referencesEl.setVisible(references);
						if(references && !referencesEl.isSelected(0)) {
							referencesEl.select(refKeys[0], true);
						}
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(handlerForUploadedResources != null) {
			doImport();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
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
		
		allOk &= validLimitationOnType(handlerForUploadedResources);
		allOk &= handlerForUploadedResources != null && !handlerForUploadedResources.isEmpty();
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
				uploadFileEl.setErrorKey("add.failed", new String[] {});
			}
		}
		
		return allOk;
	}
	
	private void doImport() {
		RepositoryHandler handler;
		if(handlerForUploadedResources == null || handlerForUploadedResources.isEmpty()) {
			handler = null;
		} else if(handlerForUploadedResources.size() == 1) {
			handler = handlerForUploadedResources.get(0).getHandler();
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
			File uploadedFile = uploadFileEl.getUploadFile();
			String uploadedFilename = uploadFileEl.getUploadFileName();
			boolean withReferences = referencesEl.isAtLeastSelected(1);
			
			importedEntry = handler.importResource(getIdentity(), null, displayname,
					"", withReferences, organisation, getLocale(), uploadedFile, uploadedFilename);
			
			if(importedEntry == null) {
				showWarning("error.import");
			} else {
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
						LoggingResourceable.wrap(importedEntry, OlatResourceableType.genRepoEntry));
				
				repositoryManager.triggerIndexer(importedEntry);
			}
		}
	}

	private void doAnalyseUpload() {
		File uploadedFile = uploadFileEl.getUploadFile();
		if(uploadedFile == null) {//OO-1320
			typeEl.setVisible(false);
			selectType.setVisible(false);
			uploadFileEl.reset();
			importButton.setEnabled(false);
		} else {
			String uploadedFilename = uploadFileEl.getUploadFileName();
			
			List<ResourceHandler> handlers = new ArrayList<>(3);
			for(String type:repositoryHandlerFactory.getSupportedTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				ResourceEvaluation eval = handler.acceptImport(uploadedFile, uploadedFilename);
				if(eval != null && eval.isValid()) {
					handlers.add(new ResourceHandler(handler, eval));
				}
			}

			updateResourceInfos(handlers);
			validLimitationOnType(handlers);
		}
	}
	
	private void updateResourceInfos(List<ResourceHandler> handlers) {
		handlerForUploadedResources = handlers;

		String displayName = "";
		boolean references = false;
		if (handlers != null && !handlers.isEmpty()) { // add image and typename code
			ResourceHandler handler = handlers.get(0);
			displayName = handler.getEval().getDisplayname();
			references = handler.getEval().isReferences();
			
			if(handlers.size() == 1) {
				String resourceType = handler.getHandler().getSupportedType();
				String tName = NewControllerFactory.translateResourceableTypeName(resourceType, getLocale());
				typeEl.setValue(tName);
				typeEl.setVisible(true);
				selectType.setVisible(false);
			} else {
				int numOfHandlers = handlers.size();
				String[] keys = new String[numOfHandlers];
				String[] values = new String[numOfHandlers];
				for(int i=0; i<numOfHandlers; i++) {
					String type = handlers.get(i).getHandler().getSupportedType();
					keys[i] = type;
					values[i] = NewControllerFactory.translateResourceableTypeName(type, getLocale());
				}
				selectType.setKeysAndValues(keys, values, null);
				selectType.select(keys[0], true);
				selectType.setVisible(true);
				typeEl.setVisible(false);
				
				references = handlers.get(0).getEval().isReferences();
			}
		} else {
			typeEl.setValue(translate("cif.type.na"));
			typeEl.setVisible(true);
			selectType.setVisible(false);
		}
		displaynameEl.setVisible(true);
		displaynameEl.setValue(displayName);
		referencesEl.setVisible(references);
		if(references) {
			referencesEl.select(refKeys[0], true);
		}
		importButton.setEnabled(!handlers.isEmpty());
	}
	
	private class ResourceHandler {
		
		private final RepositoryHandler handler;
		private final ResourceEvaluation eval;
		
		public ResourceHandler(RepositoryHandler handler, ResourceEvaluation eval) {
			this.handler = handler;
			this.eval = eval;
		}

		public RepositoryHandler getHandler() {
			return handler;
		}

		public ResourceEvaluation getEval() {
			return eval;
		}	
	}
}