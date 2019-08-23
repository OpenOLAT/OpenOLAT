/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this doc except in compliance with the License.<br>
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
package org.olat.core.commons.services.doceditor.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.List;

import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
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
public class CreateDocumentController extends FormBasicController {
	
	private SingleSelection docTypeEl;
	private TextElement docNameEl;
	
	private MetaInfoFormController metadataCtrl;

	private final VFSContainer vfsContainer;
	private final List<DocTemplate> templates;
	private VFSLeaf vfsLeaf;
	
	@Autowired
	private VFSRepositoryService vfsService;
	
	public CreateDocumentController(UserRequest ureq, WindowControl wControl, VFSContainer vfsContainer, DocTemplates templates) {
		super(ureq, wControl, "create_document");
		this.vfsContainer = vfsContainer;
		this.templates = templates.getTemplates();
		initForm(ureq);
	}
	
	public VFSLeaf getCreatedLeaf() {
		return vfsLeaf;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer docCont = FormLayoutContainer.createDefaultFormLayout("doc", getTranslator());
		formLayout.add(docCont);
		
		KeyValues docTypeKV = new KeyValues();
		for (int i = 0; i < templates.size(); i++) {
			DocTemplate docTemplate = templates.get(i);
			String name = docTemplate.getName() + " (." + docTemplate.getSuffix() + ")";
			docTypeKV.add(entry(String.valueOf(i), name));
		}
		docTypeEl = uifactory.addDropdownSingleselect("create.doc.type", docCont, docTypeKV.keys(), docTypeKV.values());
		docTypeEl.setElementCssClass("o_sel_folder_new_doc_type");
		docTypeEl.setMandatory(true);
		
		docNameEl = uifactory.addTextElement("create.doc.name", -1, "", docCont);
		docNameEl.setElementCssClass("o_sel_folder_new_doc_name");
		docNameEl.setDisplaySize(100);
		docNameEl.setMandatory(true);
		
		// metadata
		metadataCtrl = new MetaInfoFormController(ureq, getWindowControl(), mainForm, false);
		formLayout.add("metadata", metadataCtrl.getFormItem());
		listenTo(metadataCtrl);
		
		FormLayoutContainer butonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		formLayout.add(butonsCont);
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButtons", getTranslator());
		butonsCont.add(formButtons);
		uifactory.addFormSubmitButton("submit", "create.doc.button", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		String docName = docNameEl.getValue();
		docNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(docName)) {
			docNameEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		} else {
			// update in GUI so user sees how we optimized
			docNameEl.setValue(docName);
			if (invalidFilenName(docName)) {
				docNameEl.setErrorKey("create.doc.name.notvalid", null);
				allOk = false;
			} else if (docExists()){
				docNameEl.setErrorKey("create.doc.already.exists", new String[] { getFileName() });
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
	
	private String getFileName() {
		String docName = docNameEl.getValue();
		DocTemplate docTemplate = getSelectedTemplate();
		String suffix = docTemplate != null? docTemplate.getSuffix(): "";
		return docName.endsWith("." + suffix)
				? docName
				: docName + "." + suffix;
	}

	private DocTemplate getSelectedTemplate() {
		int index = docTypeEl.getSelected();
		return index > -1? templates.get(index): null;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String docName = getFileName();
		createFile(docName);
		createContent();
		createMetadata();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void createFile(String docName) {
		vfsLeaf = vfsContainer.createChildLeaf(docName);
	}

	private void createContent() {
		if (vfsLeaf != null) {
			DocTemplate docTemplate = getSelectedTemplate();
			if (docTemplate != null) {
				VFSManager.copyContent(docTemplate.getContentProvider().getContent(getLocale()), vfsLeaf);
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
