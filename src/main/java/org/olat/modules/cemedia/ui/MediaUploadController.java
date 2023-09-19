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
package org.olat.modules.cemedia.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.Quota;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.ui.medias.AbstractCollectMediaController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUploadController extends AbstractCollectMediaController implements PageElementAddController {
	
	private FileElement fileEl;
	private TextElement titleEl;
	private TagSelection tagsEl;
	private TextElement altTextEl;
	private RichTextElement descriptionEl;
	private TaxonomyLevelSelection taxonomyLevelEl;
	
	private final Quota quota;
	private final String businessPath;
	private AddElementInfos userObject;
	
	private MediaRelationsController relationsCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private TaxonomyService taxonomyService;

	public MediaUploadController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
				Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale())));
		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		quota = mediaService.getQuota(getIdentity(), ureq.getUserSession().getRoles());
		
		relationsCtrl = new MediaRelationsController(ureq, getWindowControl(), mainForm, null, true, true);
		relationsCtrl.setOpenClose(false);
		listenTo(relationsCtrl);
		
		initForm(ureq);
		updateUILicense();
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
		return MediaPart.valueOf(getIdentity(), mediaReference);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initMediaForm(formLayout);
		formLayout.setElementCssClass("o_sel_upload_media_form");
		
		FormItem relationsItem = relationsCtrl.getInitialFormItem();
		relationsItem.setFormLayout("0_12");
		formLayout.add("relations", relationsItem);
		
		FormLayoutContainer buttonsCont = uifactory.addInlineFormLayout("buttons", null, formLayout);
		buttonsCont.setFormLayout("0_12");
		buttonsCont.setElementCssClass("o_sel_buttons");
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
		
	private void initMediaForm(FormItemContainer formLayout) {	
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.setMandatory(true);
		MediaUIHelper.setQuota(quota, fileEl);

		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, "", formLayout);
		titleEl.setElementCssClass("o_sel_media_title");
		titleEl.setMandatory(true);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), new ArrayList<>());
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		Set<TaxonomyLevel> availableTaxonomyLevels = taxonomyService.getTaxonomyLevelsAsSet(mediaModule.getTaxonomyRefs());
		taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomy.levels", "taxonomy.levels", formLayout,
				getWindowControl(), availableTaxonomyLevels);
		taxonomyLevelEl.setDisplayNameHeader(translate("table.header.taxonomy"));
		
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", "", 8, -1, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		altTextEl = uifactory.addTextElement("artefact.alt.text", "artefact.alt.text", 1000, "", formLayout);

		initLicenseForm(formLayout);
		
		String date = Formatter.getInstance(getLocale()).formatDate(new Date());
		uifactory.addStaticTextElement("artefact.collect.date", "artefact.collect.date", date, formLayout);

		if(MediaUIHelper.showBusinessPath(businessPath)) {
			String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if(fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1 || getHandler() == null) {
			fileEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateFormItem(ureq, fileEl);
		}
		
		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String altText = altTextEl.isVisible() ? altTextEl.getValue() : null;
			String description = descriptionEl.getValue();
			File uploadedFile = fileEl.getUploadFile();
			String uploadedFilename = fileEl.getUploadFileName();
			MediaHandler mediaHandler = getHandler();
			if(mediaHandler != null) {
				UploadMedia mObject = new UploadMedia(uploadedFile, uploadedFilename, fileEl.getUploadMimeType());
				mediaReference = mediaHandler.createMedia(title, description, altText, mObject, businessPath,
						getIdentity(), MediaLog.Action.UPLOAD);
			}
		}
		
		saveLicense();

		List<String> updatedTags = tagsEl.getDisplayNames();
		mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
		
		Set<TaxonomyLevelRef> selectedlevels = taxonomyLevelEl.getSelection();
		mediaService.updateTaxonomyLevels(mediaReference, selectedlevels);
		
		relationsCtrl.saveRelations(mediaReference);
		
		dbInstance.commit();

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			MediaHandler handler = getHandler();
			if (titleEl.isEmpty()) {
				titleEl.setValue(fileEl.getUploadFileName());
				titleEl.getComponent().setDirty(true);
			}
			titleEl.clearWarning();
			if(mediaService.isInMediaCenter(getIdentity(), fileEl.getUploadFile())) {
				titleEl.setWarningKey("warning.checksum.file");
			}
			altTextEl.setVisible(handler != null && ImageHandler.IMAGE_TYPE.equals(handler.getType()));
			updateUILicense();
		} else {
			updateUILicense();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void updateUILicense() {
		MediaHandler handler = this.getHandler();
		if(handler != null && handler.getUISettings().hasLicense()) {
			setLicenseVisibility(true);
			super.updateUILicense();
		} else {
			setLicenseVisibility(false);
		}
	}
	
	private MediaHandler getHandler() {
		MediaHandler handler = null;
		String mimeType = fileEl.getUploadMimeType();
		if(StringHelper.containsNonWhitespace(mimeType)) {
			List<MediaHandler> availableHandlers = mediaService.getMediaHandlers();
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
