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
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewDocumentController extends FormBasicController {
	
	private TextElement filenameEl;
	private SingleSelection docTypeEl;
	private final VFSContainer documentContainer;
	private final List<DocTemplate> templates;
	
	@Autowired
	private DocEditorService docEditorService;
	
	public NewDocumentController(UserRequest ureq, WindowControl wControl, VFSContainer documentContainer,
			DocTemplates templates) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CreateDocumentController.class, getLocale(), getTranslator()));
		this.documentContainer = documentContainer;
		this.templates = templates.getTemplates();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_new_doc_form");
		
		SelectionValues fileTypeKV = new SelectionValues();
		for (DocTemplate docTemplate : templates) {
			String name = docTemplate.getName() + " (." + docTemplate.getSuffix() + ")";
			String iconCSS = "o_icon " + CSSHelper.createFiletypeIconCssClassFor("dummy." + docTemplate.getSuffix());
			fileTypeKV.add(new SelectionValue(docTemplate.getSuffix(), name, null, iconCSS, null, true));
		}
		docTypeEl = uifactory.addCardSingleSelectHorizontal("o_" + CodeHelper.getRAMUniqueID(), "create.doc.format",
				"create.doc.format", formLayout, fileTypeKV, true, "create.doc.formats.show.more");
		docTypeEl.select(docTypeEl.getKey(0), true);
		docTypeEl.setElementCssClass("o_sel_course_gta_doc_filetype");
		docTypeEl.setMandatory(true);
		if (templates.size() == 1) {
			docTypeEl.setVisible(false);
		}
		
		filenameEl = uifactory.addTextElement("fileName", "file.name", -1, "", formLayout);
		filenameEl.setElementCssClass("o_sel_course_gta_doc_filename");
		filenameEl.setDisplaySize(20);
		filenameEl.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		FormSubmit submitButton = uifactory.addFormSubmitButton("submit", "create", formButtons);
		submitButton.setNewWindowAfterDispatchUrl(true);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
	}
	
	public String getFilename() {
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

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		filenameEl.clearError();
		String val = filenameEl.getValue();
		if(!StringHelper.containsNonWhitespace(val)) {
			filenameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			String filename = getFilename();
			if(documentContainer.resolve(filename) != null) {
				filenameEl.setErrorKey("error.file.exists", filename);
				allOk &= false;
			} else if (!FileUtils.validateFilename(filename)) {
				filenameEl.setErrorKey("error.file.invalid");
				allOk &= false;
			} else if(filename.length() > 128) {
				filenameEl.setErrorKey("form.error.toolong", "128");
				allOk &= false;
			}
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

	private void doOpen(UserRequest ureq, VFSLeaf vfsLeaf) {
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(documentContainer, vfsLeaf, vfsLeaf.getName(), Mode.EDIT, null);
		docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_EDIT_VIEW);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
