/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import java.util.List;

import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 28 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ZipConfirmationController extends FormBasicController {
	
	private static final String SUFFIX = "zip";

	private TextElement docNameEl;

	private final VFSContainer vfsContainer;
	private final List<VFSItem> itemsToZip;

	public ZipConfirmationController(UserRequest ureq, WindowControl wControl, VFSContainer vfsContainer, List<VFSItem> itemsToZip) {
		super(ureq, wControl);
		setTranslator( Util.createPackageTranslator(CreateDocumentController.class, getLocale(), getTranslator()));
		this.vfsContainer = vfsContainer;
		this.itemsToZip = itemsToZip;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> filenames = itemsToZip.stream()
				.map(VFSItem::getName)
				.sorted()
				.toList();
		uifactory.addStaticListElement("folder.and.files", "folder.and.files", filenames, formLayout);
		
		docNameEl = uifactory.addTextElement("zip.filename", -1, "", formLayout);
		docNameEl.setDisplaySize(100);
		docNameEl.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButtons", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit", "zip.button", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String docName = docNameEl.getValue();
		docNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(docName)) {
			docNameEl.setErrorKey("form.mandatory.hover");
			allOk = false;
		} else {
			// update in GUI so user sees how we optimized
			docNameEl.setValue(docName);
			if (invalidFilenName(docName)) {
				docNameEl.setErrorKey("create.doc.name.notvalid");
				allOk = false;
			} else if (docExists()){
				docNameEl.setErrorKey("create.doc.already.exists", getFileName());
				allOk = false;
			}
		}
		
		return allOk;
	}

	private boolean invalidFilenName(String docName) {
		return !FileUtils.validateFilename(docName);
	}
	
	private boolean docExists() {
		return vfsContainer.resolve(getFileName()) != null? true: false;
	}
	
	public String getFileName() {
		String docName = docNameEl.getValue();
		return docName.endsWith("." + SUFFIX)
				? docName
				: docName + "." + SUFFIX;
	}
	
	public List<VFSItem> getItemsToZip() {
		return itemsToZip;
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
