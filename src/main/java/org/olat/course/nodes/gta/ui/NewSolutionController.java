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
package org.olat.course.nodes.gta.ui;

import java.util.List;

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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.gta.model.Solution;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewSolutionController extends FormBasicController {
	
	private TextElement filenameEl, titleEl;
	private SingleSelection fileTypeEl;
	private final VFSContainer documentContainer;
	private final List<FileType> fileTypes;
	
	@Autowired
	private VFSRepositoryService vfsService;
	
	public NewSolutionController(UserRequest ureq, WindowControl wControl, VFSContainer documentContainer, FileTypes fileTypes) {
		super(ureq, wControl);
		this.documentContainer = documentContainer;
		this.fileTypes = fileTypes.getFileTypes();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_new_task_form");

		titleEl = uifactory.addTextElement("title", "task.title", 128, "", formLayout);
		titleEl.setElementCssClass("o_sel_course_gta_upload_task_title");
		titleEl.setMandatory(true);
		
		String[] fileTypeKeys = new String[fileTypes.size()];
		String[] fileTypeValues = new String[fileTypes.size()];
		String[] fileTypeSuffix = new String[fileTypes.size()];
		for (int i = 0; i < fileTypes.size(); i++) {
			FileType fileType = fileTypes.get(i);
			String name = fileType.getName() + " (." + fileType.getSuffix() + ")";
			fileTypeKeys[i] = String.valueOf(i);
			fileTypeValues[i] = name;
			fileTypeSuffix[i] = fileType.getSuffix();
		}
		fileTypeEl = uifactory.addDropdownSingleselect("file.type", formLayout, fileTypeKeys, fileTypeValues, fileTypeSuffix);
		fileTypeEl.setElementCssClass("o_sel_course_gta_doc_filetype");
		fileTypeEl.setMandatory(true);
		if (fileTypes.size() == 1) {
			fileTypeEl.setVisible(false);
		}

		filenameEl = uifactory.addTextElement("fileName", "file.name", -1, "", formLayout);
		filenameEl.setElementCssClass("o_sel_course_gta_doc_filename");
		filenameEl.setExampleKey("file.name.example", null);
		filenameEl.setDisplaySize(20);
		filenameEl.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit", "create", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
		
		String jsPage = velocity_root + "/new_task_js.html";
		FormLayoutContainer jsCont = FormLayoutContainer.createCustomFormLayout("js", getTranslator(), jsPage);
		jsCont.contextPut("titleId", titleEl.getFormDispatchId());
		jsCont.contextPut("filetypeId", fileTypeEl.getFormDispatchId());
		jsCont.contextPut("filetypeDefaultSuffix", fileTypes.get(0).getSuffix());
		jsCont.contextPut("filenameId", filenameEl.getFormDispatchId());
		formLayout.add(jsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private String getFilename() {
		String fileName = filenameEl.getValue().toLowerCase();
		FileType fileType = getSelectedFileType();
		String suffix = fileType != null? fileType.getSuffix(): "";
		return fileName.endsWith("." + suffix)
				? fileName
				: fileName + "." + suffix;
	}

	private FileType getSelectedFileType() {
		int index = fileTypeEl.getSelected();
		return index >= 0? fileTypes.get(index): fileTypes.get(0);
	}
	
	public Solution getSolution() {
		Solution solution = new Solution();
		solution.setTitle(titleEl.getValue());
		solution.setFilename(getFilename());
		return solution; 
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		filenameEl.clearError();
		String val = filenameEl.getValue();
		if(!StringHelper.containsNonWhitespace(val)) {
			filenameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			String filename = getFilename();
			if(documentContainer.resolve(filename) != null) {
				filenameEl.setErrorKey("error.file.exists", new String[]{filename});
				allOk &= false;
			} else if (!FileUtils.validateFilename(filename)) {
				filenameEl.setErrorKey("error.file.invalid", null);
				allOk &= false;
			}
		}
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String documentName = getFilename();
		VFSItem item = documentContainer.resolve(documentName);
		VFSLeaf vfsLeaf = null;
		if(item == null) {
			vfsLeaf = documentContainer.createChildLeaf(documentName);
		} else {
			documentName = VFSManager.rename(documentContainer, documentName);
			vfsLeaf = documentContainer.createChildLeaf(documentName);
		}
		FileType fileType = getSelectedFileType();
		if (fileType != null) {
			VFSManager.copyContent(fileType.getContentProvider().getContent(), vfsLeaf);
		}
		if(vfsLeaf.canMeta() == VFSConstants.YES) {
			VFSMetadata metaInfo = vfsLeaf.getMetaInfo();
			metaInfo.setAuthor(getIdentity());
			vfsService.updateMetadata(metaInfo);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
