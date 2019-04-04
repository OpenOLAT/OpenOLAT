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

import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.filetemplate.FileType;
import org.olat.core.commons.services.filetemplate.FileTypes;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
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
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateFileController extends FormBasicController {
	
	private SingleSelection fileTypeEl;
	private TextElement fileNameEl;
	
	private MetaInfoFormController metadataCtrl;

	private final VFSContainer vfsContainer;
	private final List<FileType> fileTypes;
	private VFSLeaf vfsLeaf;
	
	@Autowired
	private VFSRepositoryService vfsService;
	
	public CreateFileController(UserRequest ureq, WindowControl wControl, VFSContainer vfsContainer, FileTypes fileTypes) {
		super(ureq, wControl, "create_file");
		this.vfsContainer = vfsContainer;
		this.fileTypes = fileTypes.getFileTypes();
		initForm(ureq);
	}
	
	public VFSLeaf getCreatedLeaf() {
		return vfsLeaf;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer fileCont = FormLayoutContainer.createDefaultFormLayout("file", getTranslator());
		formLayout.add(fileCont);
		
		KeyValues fileTypeKV = new KeyValues();
		for (int i = 0; i < fileTypes.size(); i++) {
			FileType fileType = fileTypes.get(i);
			String name = fileType.getName() + " (." + fileType.getSuffix() + ")";
			fileTypeKV.add(entry(String.valueOf(i), name));
		}
		fileTypeEl = uifactory.addDropdownSingleselect("create.file.type", fileCont, fileTypeKV.keys(), fileTypeKV.values());
		fileTypeEl.setElementCssClass("o_sel_folder_new_file_type");
		fileTypeEl.setMandatory(true);
		
		fileNameEl = uifactory.addTextElement("create.file.name", -1, "", fileCont);
		fileNameEl.setElementCssClass("o_sel_folder_new_file_name");
		fileNameEl.setDisplaySize(100);
		fileNameEl.setMandatory(true);
		
		// metadata
		metadataCtrl = new MetaInfoFormController(ureq, getWindowControl(), mainForm, false);
		formLayout.add("metadata", metadataCtrl.getFormItem());
		listenTo(metadataCtrl);
		
		FormLayoutContainer butonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		formLayout.add(butonsCont);
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButtons", getTranslator());
		butonsCont.add(formButtons);
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
		return vfsContainer.resolve(getFileName()) != null? true: false;
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
		return index > -1? fileTypes.get(index): null;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String fileName = getFileName();
		createFile(fileName);
		createContent();
		createMetadata();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void createFile(String fileName) {
		vfsLeaf = vfsContainer.createChildLeaf(fileName);
	}

	private void createContent() {
		if (vfsLeaf != null) {
			FileType fileType = getSelectedFileType();
			if (fileType != null) {
				VFSManager.copyContent(fileType.getContentProvider().getContent(), vfsLeaf);
			}
		}
	}

	private void createMetadata() {
		if (vfsLeaf != null && vfsLeaf.canMeta() == VFSConstants.YES) {
			VFSMetadata meta = vfsLeaf.getMetaInfo();
			if (metadataCtrl != null) {
				meta = metadataCtrl.getMetaInfo(meta);
			}
			meta.setAuthor(getIdentity());
			vfsService.updateMetadata(meta);
			vfsService.resetThumbnails(vfsLeaf);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
