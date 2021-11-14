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
package org.olat.course.nodes.document.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.WebDocumentHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CopyToRepositoryController extends FormBasicController {
	
	private static final Logger log = Tracing.createLoggerFor(CopyToRepositoryController.class);

	private TextElement displayNameEl;
	private SingleSelection organisationEl;
	
	private final LocalFileImpl vfsLeaf;
	private final VFSMetadata vfsMetadata;
	private final String initialDisplayname;
	private RepositoryEntry entry;

	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired 
	private OrganisationService organisationService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler folderLicenseHandler;
	@Autowired
	private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;
	
	public CopyToRepositoryController(UserRequest ureq, WindowControl wControl, LocalFileImpl vfsLeaf) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		vfsMetadata = vfsLeaf.getMetaInfo();
		this.initialDisplayname = vfsMetadata != null && StringHelper.containsNonWhitespace(vfsMetadata.getTitle())
				? vfsMetadata.getTitle()
				: vfsLeaf.getName();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		displayNameEl = uifactory.addTextElement("config.copy.displayname", 255, initialDisplayname, formLayout);
		displayNameEl.setMandatory(true);
		
		initFormOrganisations(formLayout, ureq.getUserSession());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("config.copy.button", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}

	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		Roles roles = usess.getRoles();
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles, OrganisationRoles.administrator);
		
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		for(Organisation organisation:organisations) {
			keyList.add(organisation.getKey().toString());
			valueList.add(organisation.getDisplayName());
		}

		organisationEl = uifactory.addDropdownSingleselect("config.copy.organisation", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]));
		organisationEl.setVisible(organisationModule.isEnabled());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String displayname = displayNameEl.getValue();
		
		Organisation organisation;
		if( organisationEl.isOneSelected()) {
			Long organisationKey = Long.valueOf(organisationEl.getSelectedKey());
			organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		} else {
			organisation = organisationService.getDefaultOrganisation();
		}
		doCopyToRepository(displayname, organisation);
		if (entry != null) {
			doCopyLicense();
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
	
	private void doCopyToRepository(String displayname, Organisation organisation) {
		File file = vfsLeaf.getBasefile();
		String filename = vfsLeaf.getName();
		RepositoryHandler repositoryHandler = getRepositoryHandler(file);
		if (repositoryHandler != null) {
			try {
				Path tempDir = Paths.get(WebappHelper.getTmpDir()).resolve(CodeHelper.getUniqueID());
				Files.createDirectories(tempDir);
				Path copy = tempDir.resolve(filename);
				Files.copy(file.toPath(), copy, StandardCopyOption.REPLACE_EXISTING);
					
				VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
				String description = vfsMetadata != null? vfsMetadata.getComment(): null;
				entry = repositoryHandler.importResource(getIdentity(), null, displayname, description, false, organisation, getLocale(), copy.toFile(), filename);
				Files.delete(tempDir);
			} catch (Exception e) {
				log.error("Copy document to repository failed.", e);
			}
		}
	}
	
	private RepositoryHandler getRepositoryHandler(File file) {
		for (String type : repositoryHandlerFactory.getSupportedTypes()) {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
			// Only documents but not other resources
			if (handler instanceof WebDocumentHandler) {
				ResourceEvaluation eval = handler.acceptImport(file, file.getName());
				if (eval != null && eval.isValid()) {
					return handler;
				}
			}
		}
		return null;
	}

	private void doCopyLicense() {
		if (licenseModule.isEnabled(folderLicenseHandler) && licenseModule.isEnabled(repositoryEntryLicenseHandler) && vfsMetadata != null) {
			LicenseType licenseType = vfsMetadata.getLicenseType();
			if (licenseType != null) {
				ResourceLicense license = licenseService.loadOrCreateLicense(entry.getOlatResource());
				license.setLicenseType(licenseType);
				license.setLicensor(vfsMetadata.getLicensor());
				if (licenseService.isFreetext(licenseType)) {
					license.setFreetext(vfsMetadata.getLicenseText());
				}
				licenseService.update(license);
			}
		}
		
	}

}
