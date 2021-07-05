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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.AccessSearchParams;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Feb 2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CopyDocumentController extends FormBasicController {
	
	private TextElement filenameEl;
	private SingleSelection sourceFileEl;

	private final VFSContainer sourceContainer;
	private final VFSContainer targetContainer;
	private final String copyEnding;
	
	@Autowired
	private VFSRepositoryService vfsService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;

	public CopyDocumentController(UserRequest ureq, WindowControl wControl, VFSContainer sourceContainer,
			VFSContainer targetContainer, String copyEnding) {
		super(ureq, wControl);
		this.sourceContainer = sourceContainer;
		this.targetContainer = targetContainer;
		this.copyEnding = copyEnding;
		initForm(ureq);
	}

	public String getFilename() {
		String filename = null;
		
		if (filenameEl != null) {
			filename = filenameEl.getValue();
			if (StringHelper.containsNonWhitespace(filename)) {
				if (sourceFileEl.isOneSelected()) {
					String selectedKey = sourceFileEl.getSelectedKey();
					VFSMetadata metadata = vfsService.getMetadata(() -> Long.valueOf(selectedKey));
					if (metadata != null) {
						String suffix = FileUtils.getFileSuffix(metadata.getFilename());
						String dotSuffix = "." + suffix;
						if (!filename.endsWith(dotSuffix)) {
							filename = filename + dotSuffix;
						}
					}
				}
				filename = FileUtils.cleanFilename(filename);
			}
		}
		
		return filename;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> copySuffixes = GTAUIFactory.getCopySuffix(getIdentity(), ureq.getUserSession().getRoles());
		if (copySuffixes.isEmpty()) {
			setFormWarning("error.copy.no.editor");
		} else {
			// Prepare files
			// 1) Get all files in the container
			List<VFSMetadata> metadatas = new ArrayList<>();
			for (VFSItem vfsItem : sourceContainer.getItems()) {
				if (vfsItem instanceof VFSLeaf) {
					VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
					String suffix = FileUtils.getFileSuffix(vfsItem.getName()).toLowerCase();
					if (copySuffixes.contains(suffix)) {
						metadatas.add(vfsLeaf.getMetaInfo());
					}
				}
			}
			
			// 2) Remove documents which are not saved
			AccessSearchParams params = new AccessSearchParams();
			params.setMode(Mode.EDIT);
			params.setMatadatas(metadatas);
			List<Access> accesses = docEditorService.getAccesses(params);
			
			List<VFSMetadata> metadataInEdit = new ArrayList<>();
			for (Access access : accesses) {
				for (VFSMetadata metadata : metadatas) {
					if (metadata.getKey().equals(access.getMetadata().getKey())) {
						metadataInEdit.add(metadata);
					}
				}
			}
			
			// Warning if some documents are not saved
			if (!metadataInEdit.isEmpty()) {
				metadatas.removeAll(metadataInEdit);
				setFormWarning("error.copy.still.edit");
			}
			
			if (metadatas.isEmpty()) {
				setFormWarning("error.copy.no.documents.edit");
			} else {
				metadatas.sort((m1, m2) -> m1.getFilename().compareToIgnoreCase(m2.getFilename()));
				
				SelectionValues metadataKV = new SelectionValues();
				metadatas.stream()
						.sorted((m1, m2) -> m1.getFilename().compareToIgnoreCase(m2.getFilename()))
						.forEach(metadata -> metadataKV.add(SelectionValues.entry(metadata.getKey().toString(), getDisplayname(metadata))));
				
				sourceFileEl = uifactory.addRadiosVertical("copy.document", formLayout, metadataKV.keys(), metadataKV.values());
				sourceFileEl.setMandatory(true);
				sourceFileEl.select(sourceFileEl.getKey(0), true);
				sourceFileEl.addActionListener(FormEvent.ONCHANGE);
				
				filenameEl = uifactory.addTextElement("copy.name", "copy.name", -1, "", formLayout);
				filenameEl.setMandatory(true);
				updateFileName();
			}
		}
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		if (sourceFileEl != null) {
			FormSubmit submitButton = uifactory.addFormSubmitButton("submit", "create", formButtons);
			submitButton.setNewWindowAfterDispatchUrl(true);
		}
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
	}

	private String getDisplayname(VFSMetadata metadata) {
		StringBuilder sb = new StringBuilder();
		
		String iconCss = CSSHelper.createFiletypeIconCssClassFor(metadata.getFilename());
		sb.append("<i class='o_icon o_icon-fw o_filetype_file").append(iconCss).append("'></i> ");
		sb.append(metadata.getFilename());
		if (metadata.getFileInitializedBy() != null || metadata.getFileLastModified() != null) {
			sb.append("<small><span class='text-muted'>");
			if (metadata.getFileInitializedBy() != null) {
				sb.append(" ");
				String userDisplayName = userManager.getUserDisplayName(metadata.getFileInitializedBy().getKey());
				sb.append(translate("created.by", new String[] { userDisplayName }));
			}
			if (metadata.getFileLastModified() != null) {
				sb.append(" ");
				String formatLastModified = Formatter.getInstance(getLocale()).formatDateAndTime(metadata.getFileLastModified());
				sb.append(translate("lastmodified", new String[] { formatLastModified }));
			}
			sb.append("</span></small>");
		}
		
		return sb.toString();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == sourceFileEl) {
			updateFileName();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateFileName() {
		if (sourceFileEl.isOneSelected()) {
			String selectedKey = sourceFileEl.getSelectedKey();
			VFSMetadata metadata = vfsService.getMetadata(() -> Long.valueOf(selectedKey));
			if (metadata != null) {
				String filename = metadata.getFilename();
				filename = FileUtils.insertBeforeSuffix(filename, copyEnding);
				filenameEl.setValue(filename);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		sourceFileEl.clearError();
		if (!sourceFileEl.isOneSelected()) {
			filenameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		filenameEl.clearError();
		String val = filenameEl.getValue();
		if(!StringHelper.containsNonWhitespace(val)) {
			filenameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			String filename = getFilename();
			filenameEl.setValue(filename); // Maybe the filename was normalized
			if(targetContainer.resolve(filename) != null) {
				filenameEl.setErrorKey("error.file.exists", new String[]{filename});
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String filename = getFilename();
		VFSItem item = targetContainer.resolve(filename);
		VFSLeaf targetLeaf = null;
		if (item == null) {
			targetLeaf = targetContainer.createChildLeaf(filename);
		} else {
			filename = VFSManager.rename(targetContainer, filename);
			targetLeaf = targetContainer.createChildLeaf(filename);
		}
		
		String selectedKey = sourceFileEl.getSelectedKey();
		VFSMetadata metadata = vfsService.getMetadata(() -> Long.valueOf(selectedKey));
		VFSItem sourceItem = vfsService.getItemFor(metadata);
		if (sourceItem instanceof VFSLeaf) {
			VFSManager.copyContent((VFSLeaf)sourceItem, targetLeaf, true, getIdentity());
			
			doOpen(ureq, targetLeaf);
			fireEvent(ureq, Event.DONE_EVENT);
		}
		
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doOpen(UserRequest ureq, VFSLeaf vfsLeaf) {
		DocEditorConfigs editorConfigs = DocEditorConfigs.builder().withMode(Mode.EDIT).build(vfsLeaf);
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), editorConfigs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
