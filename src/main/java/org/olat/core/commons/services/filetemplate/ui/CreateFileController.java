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
package org.olat.core.commons.services.filetemplate.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.List;

import org.olat.core.commons.services.filetemplate.FileType;
import org.olat.core.commons.services.filetemplate.FileTypes;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

/**
 * 
 * Initial date: 19 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateFileController extends FormBasicController {
	
	private SingleSelection fileTypeEl;
	private TextElement fileNameEl;
	
	private final VFSContainer container;
	private final List<FileType> fileTypes;
	private VFSLeaf vfsLeaf;
	
	public CreateFileController(UserRequest ureq, WindowControl wControl, VFSContainer container, FileTypes fileTypes) {
		super(ureq, wControl);
		this.container = container;
		this.fileTypes = fileTypes.getFileTypes();
		initForm(ureq);
	}
	
	public VFSLeaf getCreatedLeaf() {
		return vfsLeaf;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		KeyValues fileTypeKV = new KeyValues();
		for (int i = 0; i < fileTypes.size(); i++) {
			FileType fileType = fileTypes.get(i);
			String name = fileType.getName() + " (." + fileType.getSuffix() + ")";
			fileTypeKV.add(entry(String.valueOf(i), name));
		}
		fileTypeEl = uifactory.addDropdownSingleselect("create.file.type", formLayout, fileTypeKV.keys(), fileTypeKV.values());
		fileTypeEl.setMandatory(true);
		
		fileNameEl = uifactory.addTextElement("create.file.name", -1, "", formLayout);
		fileNameEl.setDisplaySize(100);
		fileNameEl.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit", "create.file.button", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		String fileName = fileNameEl.getValue();
		fileNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(fileName)) {
			fileNameEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		} else {
			// update in GUI so user sees how we optimized
			fileNameEl.setValue(fileName);
			if (invalidFilenName(fileName)) {
				fileNameEl.setErrorKey("create.file.name.notvalid", null);
				allOk = false;
			} else if (fileExists()){
				fileNameEl.setErrorKey("create.file.already.exists", new String[] { getFileName() });
				allOk = false;
			}
		}
		
		return allOk;
	}

	private boolean invalidFilenName(String fileName) {
		return !FileUtils.validateFilename(fileName);
	}
	
	private boolean fileExists() {
		return container.resolve(getFileName()) != null? true: false;
	}
	
	private String getFileName() {
		String fileName = fileNameEl.getValue();
		FileType fileType = getSelectedFileType();
		String suffix = fileType != null? fileType.getSuffix(): "";
		return fileName.endsWith("." + suffix)
				? fileName
				: fileName + "." + suffix;
	}

	private FileType getSelectedFileType() {
		int index = fileTypeEl.getSelected();
		return index >= -1? fileTypes.get(index): null;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//create the file
		String fileName = getFileName();
		vfsLeaf = container.createChildLeaf(fileName);
		FileType fileType = getSelectedFileType();
		if (fileType != null) {
			VFSManager.copyContent(fileType.getContentProvider().getContent(), vfsLeaf);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
