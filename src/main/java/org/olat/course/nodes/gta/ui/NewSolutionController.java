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

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
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
	private SingleSelection docTypeEl;
	private final VFSContainer documentContainer;
	private final List<DocTemplate> templates;
	
	@Autowired
	private DocEditorService docEditorService;
	
	
	public NewSolutionController(UserRequest ureq, WindowControl wControl, VFSContainer documentContainer, DocTemplates docTemplates) {
		super(ureq, wControl);
		this.documentContainer = documentContainer;
		this.templates = docTemplates.getTemplates();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_new_task_form");

		titleEl = uifactory.addTextElement("title", "task.title", 128, "", formLayout);
		titleEl.setElementCssClass("o_sel_course_gta_upload_task_title");
		titleEl.setMandatory(true);
		
		String[] fileTypeKeys = new String[templates.size()];
		String[] fileTypeValues = new String[templates.size()];
		String[] fileTypeSuffix = new String[templates.size()];
		for (int i = 0; i < templates.size(); i++) {
			DocTemplate docTemplate = templates.get(i);
			String name = docTemplate.getName() + " (." + docTemplate.getSuffix() + ")";
			fileTypeKeys[i] = String.valueOf(i);
			fileTypeValues[i] = name;
			fileTypeSuffix[i] = docTemplate.getSuffix();
		}
		docTypeEl = uifactory.addDropdownSingleselect("file.type", formLayout, fileTypeKeys, fileTypeValues, fileTypeSuffix);
		docTypeEl.setElementCssClass("o_sel_course_gta_doc_filetype");
		docTypeEl.setMandatory(true);
		if (templates.size() == 1) {
			docTypeEl.setVisible(false);
		}

		filenameEl = uifactory.addTextElement("fileName", "file.name", -1, "", formLayout);
		filenameEl.setElementCssClass("o_sel_course_gta_doc_filename");
		filenameEl.setExampleKey("file.name.example", null);
		filenameEl.setDisplaySize(20);
		filenameEl.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		FormSubmit submitButton = uifactory.addFormSubmitButton("submit", "create", formButtons);
		submitButton.setNewWindowAfterDispatchUrl(true);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
		
		String jsPage = velocity_root + "/new_task_js.html";
		FormLayoutContainer jsCont = FormLayoutContainer.createCustomFormLayout("js", getTranslator(), jsPage);
		jsCont.contextPut("titleId", titleEl.getFormDispatchId());
		jsCont.contextPut("filetypeId", docTypeEl.getFormDispatchId());
		jsCont.contextPut("filetypeDefaultSuffix", templates.get(0).getSuffix());
		jsCont.contextPut("filenameId", filenameEl.getFormDispatchId());
		formLayout.add(jsCont);
	}
	
	private String getFilename() {
		String fileName = filenameEl.getValue().toLowerCase();
		DocTemplate docTemplate = getSelectedTemplate();
		String suffix = docTemplate != null? docTemplate.getSuffix(): "";
		return fileName.endsWith("." + suffix)
				? fileName
				: fileName + "." + suffix;
	}

	private DocTemplate getSelectedTemplate() {
		int index = docTypeEl.getSelected();
		return index >= 0? templates.get(index): templates.get(0);
	}
	
	public Solution getSolution() {
		Solution solution = new Solution();
		solution.setTitle(titleEl.getValue());
		solution.setFilename(getFilename());
		return solution; 
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
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

		return allOk;
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
		DocTemplate docTemplate = getSelectedTemplate();
		if (docTemplate != null) {
			VFSManager.copyContent(docTemplate.getContentProvider().getContent(getLocale()), vfsLeaf, getIdentity());
		}
		
		doOpen(ureq, vfsLeaf);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doOpen(UserRequest ureq, VFSLeaf vfsLeaf) {
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(documentContainer, vfsLeaf, vfsLeaf.getName(), Mode.EDIT, null);
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
}
