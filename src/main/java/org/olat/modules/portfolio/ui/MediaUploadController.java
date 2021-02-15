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
package org.olat.modules.portfolio.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.media.UploadMedia;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUploadController extends FormBasicController implements PageElementAddController {
	
	private FileElement fileEl;
	private TextElement titleEl;
	private RichTextElement descriptionEl;
	private TextBoxListElement categoriesEl;

	private Media mediaReference;
	private List<TextBoxItem> categories = new ArrayList<>();
	
	private final String businessPath;
	private AddElementInfos userObject;

	@Autowired
	private PortfolioService portfolioService;

	public MediaUploadController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
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
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, "", formLayout);
		titleEl.setMandatory(true);
		
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", "", 8, 60, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.setMandatory(true);
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setHelpText(translate("categories.hint"));
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		
		String date = Formatter.getInstance(getLocale()).formatDate(new Date());
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
		
		fileEl.clearError();
		if(fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1) {
			fileEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(getHandler() == null) {
			fileEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} 
		
		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String description = descriptionEl.getValue();
			File uploadedFile = fileEl.getUploadFile();
			String uploadedFilename = fileEl.getUploadFileName();
			MediaHandler mediaHandler = getHandler();
			if(mediaHandler != null) {
				UploadMedia mObject = new UploadMedia(uploadedFile, uploadedFilename, fileEl.getUploadMimeType());
				mediaReference = mediaHandler.createMedia(title, description, mObject, businessPath, getIdentity());
			}
		}

		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(mediaReference, updatedCategories);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			getHandler();
			if (this.titleEl.isEmpty()) {
				this.titleEl.setValue(fileEl.getUploadFileName());
				this.titleEl.getComponent().setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private MediaHandler getHandler() {
		MediaHandler handler = null;
		String mimeType = fileEl.getUploadMimeType();
		if(StringHelper.containsNonWhitespace(mimeType)) {
			List<MediaHandler> availableHandlers = portfolioService.getMediaHandlers();
			for(MediaHandler availableHandler:availableHandlers) {
				if(availableHandler.acceptMimeType(mimeType)) {
					handler = availableHandler;
					break;
				}
			}
		}
		return handler;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
