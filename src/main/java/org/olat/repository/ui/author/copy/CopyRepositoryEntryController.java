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
package org.olat.repository.ui.author.copy;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyRepositoryEntryController extends FormBasicController {

	private static final String KEY_COPY = "copy";
	private static final String KEY_DOWNLOAD = "download";

	private TextElement displaynameEl;
	private TextElement externalRefEl;
	private ObjectSelectionElement organisationsEl;
	private SelectionElement authorsCanEl;
	
	private RepositoryEntry copyEntry;
	private final RepositoryEntry sourceEntry;
	private final boolean saveAsTemplate;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public CopyRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryEntry sourceEntry, boolean saveAsTemplate) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.sourceEntry = sourceEntry;
		this.saveAsTemplate = saveAsTemplate;

		initForm(ureq);
		validateDisplayName(ureq);
		validateExternalRef(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String displayName = translate(saveAsTemplate ? "save.as.template.title" : "copy.entry", sourceEntry.getDisplayname());
		displaynameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, displayName, formLayout);
		displaynameEl.setMandatory(true);
		displaynameEl.setInlineValidationOn(true);
		
		String externalRef = null;
		if (!saveAsTemplate) {
			if (StringHelper.containsNonWhitespace(sourceEntry.getExternalRef())) {
				externalRef = translate("copy.entry", sourceEntry.getExternalRef());
			}
		}
		externalRefEl = uifactory.addTextElement("cif.externalref", "cif.externalref.long", 255, externalRef, formLayout);
		externalRefEl.setHelpText(translate("cif.externalref.hover"));
		externalRefEl.setHelpUrlForManualPage("manual_user/learningresources/Set_up_info_page/");
		externalRefEl.setInlineValidationOn(true);
		
		if (saveAsTemplate) {
			initFormOrgs(formLayout, ureq.getUserSession());
			initAuthorsCan(formLayout);
		}
		
		FormLayoutContainer buttonContainer = uifactory.addButtonsFormLayout("buttonContainer", null, formLayout);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		uifactory.addFormSubmitButton(saveAsTemplate ? "save" : "details.copy", buttonContainer);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	private void initFormOrgs(FormItemContainer formLayout, UserSession userSession) {
		Roles roles = userSession.getRoles();
		Supplier<Collection<? extends OrganisationRef>> organistionSupplier = () -> organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				repositoryService.getOrganisations(sourceEntry),
				organistionSupplier);
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "cif.organisations", formLayout,
				getWindowControl(), true, organisationSource);
		organisationsEl.setVisible(organisationModule.isEnabled());
		organisationsEl.setMandatory(true);
	}
	
	private void initAuthorsCan(FormItemContainer formLayout) {
		SelectionValues authorsCanSV = new SelectionValues();
		authorsCanSV.add(SelectionValues.entry(KEY_COPY, translate("cif.canCopy")));
		authorsCanSV.add(SelectionValues.entry(KEY_DOWNLOAD, translate("cif.canDownload")));

		authorsCanEl = uifactory.addCheckboxesVertical("cif.author.can", formLayout, authorsCanSV.keys(), authorsCanSV.values(), 1);
	}

	public RepositoryEntry getCopiedEntry() {
		return copyEntry;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		String displayname = displaynameEl.getValue();
		String externalRef = externalRefEl.getValue();
		fireEvent(ureq, Event.CLOSE_EVENT);
		boolean canCopy = false;
		boolean canDownload = false;
		if (authorsCanEl != null) {
			canCopy = authorsCanEl.isKeySelected(KEY_COPY);
			canDownload = authorsCanEl.isKeySelected(KEY_DOWNLOAD);
		}
		copyEntry = repositoryService.copy(sourceEntry, getIdentity(), displayname, externalRef, saveAsTemplate, getSelectedOrgs(), canCopy, canDownload);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private List<Organisation> getSelectedOrgs() {
		if (organisationsEl == null) {
			return null;
		}
		return organisationService.getOrganisation(OrganisationSelectionSource.toRefs(organisationsEl.getSelectedKeys()));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (!StringHelper.containsNonWhitespace(displaynameEl.getValue())) {
			displaynameEl.setErrorKey("cif.error.displayname.empty");
			allOk &= false;
		} else if (displaynameEl.hasError()) {
			allOk &= false;
		} else {
			displaynameEl.clearError();
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
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean ok = super.validateFormItem(ureq, item);
		if(ok && item == displaynameEl) {
			validateDisplayName(ureq);
		} else if(ok && item == externalRefEl) {
			validateExternalRef(ureq);
		}
		return ok;
	}
	
	private void validateDisplayName(UserRequest ureq) {
		if(StringHelper.containsNonWhitespace(displaynameEl.getValue())
				&& hasLikeRepositoryEntry(ureq, displaynameEl.getValue().toLowerCase(), null)) {
			displaynameEl.setWarningKey("error.exists.displayname");
		} else {
			displaynameEl.clearWarning();
		}
	}
	
	private void validateExternalRef(UserRequest ureq) {
		if(StringHelper.containsNonWhitespace(externalRefEl.getValue())
				&& hasLikeRepositoryEntry(ureq, null, externalRefEl.getValue().toLowerCase())) {
			externalRefEl.setWarningKey("error.exists.ext.ref");
		} else {
			externalRefEl.clearWarning();
		}
	}
	
	private boolean hasLikeRepositoryEntry(UserRequest ureq, String displayName, String reference) {
		SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(),
				ureq.getUserSession().getRoles());
		params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
		params.setExactSearch(true);
		params.setCanCopy(true);
		params.setDisplayname(displayName);
		params.setReference(reference);
		return repositoryService.countAuthorView(params) > 0;
	}
}