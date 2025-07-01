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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyRepositoryEntryController extends FormBasicController {

	private TextElement displaynameEl;
	private TextElement externalRefEl;
	
	private RepositoryEntry copyEntry;
	private final RepositoryEntry sourceEntry;

	@Autowired
	private RepositoryService repositoryService;
	
	public CopyRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryEntry sourceEntry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.sourceEntry = sourceEntry;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String displayName = translate("copy.entry", sourceEntry.getDisplayname());
		displaynameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, displayName, formLayout);
		displaynameEl.setMandatory(true);
		
		String externalRef = StringHelper.containsNonWhitespace(sourceEntry.getExternalRef())
				? translate("copy.entry", sourceEntry.getExternalRef())
				: null;
		externalRefEl = uifactory.addTextElement("cif.externalref", "cif.externalref.long", 255, externalRef, formLayout);
		
		FormLayoutContainer buttonContainer = uifactory.addButtonsFormLayout("buttonContainer", null, formLayout);
		buttonContainer.setElementCssClass("o_sel_repo_save_details");
		uifactory.addFormSubmitButton("details.copy", buttonContainer);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	public RepositoryEntry getCopiedEntry() {
		return copyEntry;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		String displayname = displaynameEl.getValue();
		String externalRef = externalRefEl.getValue();
		fireEvent(ureq, Event.CLOSE_EVENT);
		copyEntry = repositoryService.copy(sourceEntry, getIdentity(), displayname, externalRef);
		fireEvent(ureq, Event.DONE_EVENT);
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
			allOk = false;
		} else if (displaynameEl.hasError()) {
			allOk = false;
		} else {
			displaynameEl.clearError();
		}

		return allOk;
	}
}