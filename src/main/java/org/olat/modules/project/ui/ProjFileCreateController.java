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
package org.olat.modules.project.ui;

import java.io.InputStream;
import java.util.List;

import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileCreateController extends FormBasicController {
	
	private SingleSelection docTypeEl;

	private ProjFileContentController fileEditCtrl;

	private final ProjProject project;
	private final List<DocTemplate> templates;
	private ProjFile file;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public ProjFileCreateController(UserRequest ureq, WindowControl wControl, ProjProject project) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(CreateDocumentController.class, getLocale(), getTranslator()));
		this.project = project;
		this.templates = DocTemplates
				.editables(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), true)
				.build()
				.getTemplates();
		
		initForm(ureq);
	}

	public ProjFile getFile() {
		return file;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues docTypeKV = new SelectionValues();
		for (DocTemplate docTemplate : templates) {
			String name = docTemplate.getName() + " (." + docTemplate.getSuffix() + ")";
			String iconCSS = "o_icon " + CSSHelper.createFiletypeIconCssClassFor("dummy." + docTemplate.getSuffix());
			docTypeKV.add(new SelectionValue(docTemplate.getSuffix(), name, null, iconCSS, null, true));
		}
		docTypeEl = uifactory.addCardSingleSelectHorizontal("o_" + CodeHelper.getRAMUniqueID(), "create.doc.format",
				"create.doc.format", formLayout, docTypeKV, true, "create.doc.formats.show.more");
		docTypeEl.setMandatory(true);
		docTypeEl.select(docTypeEl.getKey(0), true);
		if (docTypeEl.getKeys().length == 1) {
			docTypeEl.setEnabled(false);
		}
		
		fileEditCtrl = new ProjFileContentController(ureq, getWindowControl(), mainForm, project, null);
		listenTo(fileEditCtrl);
		formLayout.add("file", fileEditCtrl.getInitialFormItem());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		FormSubmit submitButton = uifactory.addFormSubmitButton("create", buttonLayout);
		submitButton.setNewWindowAfterDispatchUrl(true);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		String jsPage = Util.getPackageVelocityRoot(CreateDocumentController.class) + "/new_filename_js.html";
		FormLayoutContainer jsCont = FormLayoutContainer.createCustomFormLayout("js", getTranslator(), jsPage);
		jsCont.contextPut("titleId", fileEditCtrl.getTitleFormDispatchId());
		jsCont.contextPut("filetypeName", docTypeEl.getName());
		jsCont.contextPut("filetypeDefaultSuffix", templates.get(0).getSuffix());
		jsCont.contextPut("filenameId", fileEditCtrl.getFilenameFormDispatchId());
		formLayout.add(jsCont);
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
		DocTemplate docTemplate = getSelectedTemplate();
		if (docTemplate != null) {
			try (InputStream content = docTemplate.getContentProvider().getContent(getLocale())) {
				String filename = fileEditCtrl.getFilename();
				if (!StringHelper.containsNonWhitespace(FileUtils.getFileSuffix(filename))) {
					filename += "." + docTemplate.getSuffix();
				}
				file = projectService.createFile(getIdentity(), project, filename, content, false);
				if (file != null) {
					projectService.updateTags(getIdentity(), file.getArtefact(), fileEditCtrl.getTagDisplayValues());
					
					VFSMetadata vfsMetadata = file.getVfsMetadata();
					fileEditCtrl.updateVfsMetdata(vfsMetadata);
					vfsMetadata = vfsRepositoryService.updateMetadata(vfsMetadata);
					
					dbInstance.commitAndCloseSession();
					VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
					if (vfsItem instanceof VFSLeaf) {
						VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
						doOpen(ureq, vfsLeaf);
					}
				}
			} catch (Exception e) {
				//
			}
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doOpen(UserRequest ureq, VFSLeaf vfsLeaf) {
		VFSContainer projectContainer = projectService.getProjectContainer(project);
		HTMLEditorConfig htmlEditorConfig = HTMLEditorConfig.builder(projectContainer, vfsLeaf.getName())
				.withAllowCustomMediaFactory(false)
				.withDisableMedia(true)
				.build();
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(DocEditor.Mode.EDIT)
				.withFireSavedEvent(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
		docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_EDIT_VIEW);
	}

}
