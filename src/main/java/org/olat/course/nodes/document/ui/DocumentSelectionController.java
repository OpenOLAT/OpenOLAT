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
package org.olat.course.nodes.document.ui;

import java.io.File;
import java.util.List;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 13 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentSelectionController extends FormBasicController {
	
	public static final Event EVENT_UPLOADED = new Event("file-uploaded");
	public static final Event EVENT_SELECT_COURSE = new Event("select-course");
	public static final Event EVENT_SELECT_REPOSITORY = new Event("select-repo");

	private FileElement uploadEl;
	private FormLink selectCourseFolderLink;
	private FormLink selectRepositoryEntryLink;
	private DropdownItem createDropdown;
	
	private final long leftQuotaKB;
	private final DocTemplates docTemplates;

	public DocumentSelectionController(UserRequest ureq, WindowControl wControl, long leftQuotaKB) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util.createPackageTranslator(FileUploadController.class, ureq.getLocale()));
		this.leftQuotaKB = leftQuotaKB;
		this.docTemplates = DocTemplates.editablesOffice(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), true).build();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Upload
		uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "config.file.upload", formLayout);
		uploadEl.addActionListener(FormEvent.ONCHANGE);
		if(leftQuotaKB <= 0l) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			uploadEl.setErrorKey("QuotaExceededSupport", new String[] { supportAddr });
			uploadEl.setMaxUploadSizeKB(0l, null, null);
		} else {
			uploadEl.setMaxUploadSizeKB(leftQuotaKB, null, null);
		}
		
		// Select
		FormLayoutContainer selectCont = FormLayoutContainer.createButtonLayout("select", getTranslator());
		selectCont.setLabel("config.select.existing", null);
		selectCont.setRootForm(mainForm);
		formLayout.add(selectCont);
		selectCourseFolderLink = uifactory.addFormLink("config.select.course.folder", selectCont, "btn btn-default");
		selectRepositoryEntryLink = uifactory.addFormLink("config.select.repo.entry", selectCont, "btn btn-default");
		selectRepositoryEntryLink.setElementCssClass("o_sel_doc_select_repository_entry");
		
		// Create
		List<DocTemplate> templates = docTemplates.getTemplates();
		if (!templates.isEmpty()) {
			FormLayoutContainer createCont = FormLayoutContainer.createButtonLayout("create", getTranslator());
			createCont.setLabel("config.create.title", null);
			createCont.setRootForm(mainForm);
			formLayout.add(createCont);
			
			// Invisible as workaround to avoid rendering the links twice
			FormLayoutContainer invisibleCont = FormLayoutContainer.createButtonLayout("invisible", getTranslator());
			createCont.setRootForm(mainForm);
			formLayout.add(invisibleCont);
			invisibleCont.setVisible(false);
			
			createDropdown = uifactory.addDropdownMenu("config.create", "config.create", createCont, getTranslator());
			createDropdown.setOrientation(DropdownOrientation.normal);
			for (DocTemplate docTemplate : templates) {
				FormLink templateLink = uifactory.addFormLink(docTemplate.getName(), invisibleCont, Link.LINK | Link.NONTRANSLATED);
				templateLink.setUserObject(docTemplate);
				createDropdown.addElement(templateLink);
			}
			// prevent cut drop downs
			createDropdown.setExpandContentHeight(true); 
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadEl && event.wasTriggerdBy(FormEvent.ONCHANGE) && uploadEl.isUploadSuccess()) {
			fireEvent(ureq, EVENT_UPLOADED);
		} else if (source == selectCourseFolderLink) {
			fireEvent(ureq, EVENT_SELECT_COURSE);
		} else if (source == selectRepositoryEntryLink) {
			fireEvent(ureq, EVENT_SELECT_REPOSITORY);
		} else if (source instanceof FormLink) {
			Object userObject = source.getUserObject();
			if (userObject instanceof DocTemplate) {
				DocTemplate docTemplate = (DocTemplate)userObject;
				fireEvent(ureq, new CreateEvent(docTemplate));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public File getUploadedFile() {
		return uploadEl.getUploadFile();
	}
	
	public String getUploadedFileName() {
		return uploadEl.getUploadFileName();
	}
	
	/**
	 * Moves the uploaded File to the destinationContainer.
	 *
	 * @param destinationContainer
	 * @return file name of the uploaded file
	 */
	public String moveUploadFileTo(VFSContainer destinationContainer) {
		if (uploadEl.isUploadSuccess()) {
			uploadEl.moveUploadFileTo(destinationContainer);
			return uploadEl.getUploadFileName();
		}
		return null;
	}
	
	public static final class CreateEvent extends Event {
		
		private static final long serialVersionUID = 6094152852836129765L;
		
		private final DocTemplate docTemplate;
		
		public CreateEvent(DocTemplate docTemplate) {
			super("create-document");
			this.docTemplate = docTemplate;
		}
		
		public DocTemplate getDocTemplate() {
			return docTemplate;
		}
		
	}

}
