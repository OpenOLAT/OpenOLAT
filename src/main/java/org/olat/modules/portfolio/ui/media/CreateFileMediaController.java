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
package org.olat.modules.portfolio.ui.media;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.FileHandler;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateFileMediaController extends FormBasicController implements PageElementAddController {
	
	private TextElement titleEl;
	private RichTextElement descriptionEl;
	private TextBoxListElement categoriesEl;
	private SingleSelection fileTypeEl;
	private TextElement fileNameEl;

	private Media mediaReference;
	private List<TextBoxItem> categories = new ArrayList<>();
	
	private final List<DocTemplate> docTemplates;
	private final String businessPath;
	private AddElementInfos userObject;
	
	@Autowired
	private FileHandler fileHandler;
	@Autowired
	private PortfolioService portfolioService;

	public CreateFileMediaController(UserRequest ureq, WindowControl wControl, DocTemplates docTemplates) {
		super(ureq, wControl);
		this.docTemplates = docTemplates.getTemplates();
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		initForm(ureq);
	}
	
	public Media getMediaReference() {
		return mediaReference;
	}
	
	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	public void setUserObject(AddElementInfos userObject) {
		this.userObject = userObject;
	}

	@Override
	public PageElement getPageElement() {
		MediaPart part = new MediaPart();
		part.setMedia(mediaReference);
		return part;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_create_document_form");
		
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, "", formLayout);
		titleEl.setElementCssClass("o_sel_pf_collect_title");
		titleEl.setMandatory(true);
		
		String desc = mediaReference == null ? null : mediaReference.getTitle();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 8, 60, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		
		SelectionValues fileTypeKV = new SelectionValues();
		for (int i = 0; i < docTemplates.size(); i++) {
			DocTemplate docTemplate = docTemplates.get(i);
			String name = docTemplate.getName() + " (." + docTemplate.getSuffix() + ")";
			fileTypeKV.add(entry(String.valueOf(i), name));
		}
		fileTypeEl = uifactory.addDropdownSingleselect("create.file.type", formLayout, fileTypeKV.keys(), fileTypeKV.values());
		fileTypeEl.setMandatory(true);
		
		fileNameEl = uifactory.addTextElement("create.file.name", -1, "", formLayout);
		fileNameEl.setDisplaySize(100);
		fileNameEl.setMandatory(true);

		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setHelpText(translate("categories.hint"));
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		
		Date collectDate = mediaReference == null ? new Date() : mediaReference.getCollectionDate();
		String date = Formatter.getInstance(getLocale()).formatDate(collectDate);
		uifactory.addStaticTextElement("artefact.collect.date", "artefact.collect.date", date, formLayout);

		if(StringHelper.containsNonWhitespace(businessPath)) {
			String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
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
			}
		}

		return allOk;
	}
	
	private boolean invalidFilenName(String fileName) {
		return !FileUtils.validateFilename(fileName);
	}
	
	private String getFileName() {
		String fileName = fileNameEl.getValue();
		DocTemplate docTemplate = getSelectedFileType();
		String suffix = docTemplate != null? docTemplate.getSuffix(): "";
		return fileName.endsWith("." + suffix)
				? fileName
				: fileName + "." + suffix;
	}

	private DocTemplate getSelectedFileType() {
		int index = fileTypeEl.getSelected();
		return index > -1? docTemplates.get(index): null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String fileName = getFileName();
		File tempDir = new File(WebappHelper.getTmpDir(), "pf" + UUID.randomUUID());
		tempDir.mkdirs();
		File tempFile = new File(tempDir, fileName);
		createContent(tempFile);
		
		String title = titleEl.getValue();
		String description = descriptionEl.getValue();
		String mimeType = WebappHelper.getMimeType(fileName);
		UploadMedia mObject = new UploadMedia(tempFile, fileName, mimeType);
		mediaReference = fileHandler.createMedia(title, description, mObject, businessPath, getIdentity());
		FileUtils.deleteFile(tempFile);
		FileUtils.deleteFile(tempDir);

		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(mediaReference, updatedCategories);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void createContent(File file) {
		VFSLeaf vfsLeaf = new LocalFileImpl(file);
		DocTemplate docTemplate = getSelectedFileType();
		if (docTemplate != null) {
			VFSManager.copyContent(docTemplate.getContentProvider().getContent(getLocale()), vfsLeaf, getIdentity());
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
