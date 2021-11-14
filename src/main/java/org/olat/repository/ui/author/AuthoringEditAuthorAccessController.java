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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * 
 * Initial date: 5 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditAuthorAccessController extends FormBasicController {
	
	private static final String YES_KEY = "y";
	private static final String[] yesKeys = new String[] { YES_KEY };

	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canDownload;

	private RepositoryEntry entry;
	private final boolean embedded;
	private final boolean readOnly;
	
	/**
	 * The details form is initialized with data collected from entry and
	 * typeName. Handler is looked-up by the given handlerName and not by the
	 * entry's resourceableType. This is to allow for an entry with no
	 * resourceable to initialize correctly (c.f. RepositoryAdd workflow). The
	 * typeName may be null.
	 */
	public AuthoringEditAuthorAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		embedded = false;
		initForm(ureq);
		initFormData();
	}
	
	public AuthoringEditAuthorAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = false;
		embedded = true;
		initForm(ureq);
		initFormData();
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}

	/**
	 * Return true when 'canCopy' is selected.
	 */
	public boolean canCopy() {
		return canCopy.isSelected(0);
	}

	/**
	 * Return true when 'canReference' is selected.
	 */
	public boolean canReference() {
		return canReference.isSelected(0);
	}

	/**
	 * Return true when 'canDownload' is selected.
	 */
	public boolean canDownload() {
		return canDownload.isSelected(0);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rentry.access.author.title");
		setFormContextHelp("Course Settings#_zugriff");
		formLayout.setElementCssClass("o_sel_repositoryentry_author_access");

		final boolean managedSettings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.settings);
		final boolean managedAccess = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.access);
		final boolean closedOrDeleted = entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted;
		
		String typeName = entry.getOlatResource().getResourceableTypeName();
		boolean supportsDownload = false;
		if (typeName != null) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(typeName);
			supportsDownload = handler != null && handler.supportsDownload();
		}
		
		canReference = uifactory.addCheckboxesVertical("cif_canReference", "cif.author.can", formLayout, yesKeys, new String[] { translate("cif.canReference") }, 1);
		canReference.setEnabled(!managedSettings && !closedOrDeleted && !readOnly);
		canReference.setElementCssClass("o_repo_with_explanation");
		canCopy = uifactory.addCheckboxesVertical("cif_canCopy", null, formLayout, yesKeys, new String[] { translate("cif.canCopy") }, 1);
		canCopy.setEnabled(!managedSettings && !closedOrDeleted && !readOnly);
		canCopy.setElementCssClass("o_repo_with_explanation");
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", null, formLayout, yesKeys, new String[] { translate("cif.canDownload") }, 1);
		canDownload.setEnabled(!managedSettings && !closedOrDeleted && !readOnly);
		canDownload.setElementCssClass("o_repo_with_explanation");
		canDownload.setVisible(supportsDownload);
		
		String explainAccess = "<i class='o_icon o_icon_warn'> </i> ".concat(translate("rentry.access.author.explain"));
		StaticTextElement explainAccessEl = uifactory.addStaticTextElement("rentry.access.author.explain", null, explainAccess, formLayout);
		explainAccessEl.setElementCssClass("o_repo_explanation");

		if (!embedded && (!managedAccess || !managedSettings) && !readOnly) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}

	private void initFormData() {
		// init author visibility and flags
		canReference.select(YES_KEY, entry.getCanReference()); 
		canCopy.select(YES_KEY, entry.getCanCopy()); 
		canDownload.select(YES_KEY, entry.getCanDownload());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

}