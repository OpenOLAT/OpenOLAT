/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.cemedia.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiImageDescriptionService;
import org.olat.core.commons.services.ai.AiImageHelper;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.ImageDescriptionData;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectOption;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.ui.medias.AbstractCollectMediaController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MediaUploadController extends AbstractCollectMediaController implements PageElementAddController {

	private final FileElement fileEl;
	private final VFSLeaf selectedLeaf;

	private TextElement titleEl;
	private TagSelection tagsEl;
	private TextElement altTextEl;
	private RichTextElement descriptionEl;
	private ObjectSelectionElement taxonomyLevelEl;
	private TaxonomyLevelSelectionSource taxonomyLevelSource;

	private final String businessPath;
	private AddElementInfos userObject;
	
	private final MediaRelationsController relationsCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AiImageDescriptionService imageDescriptionService;
	@Autowired
	private AiImageHelper aiImageHelper;
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private TaxonomyService taxonomyService;

	public MediaUploadController(UserRequest ureq, WindowControl wControl, FileElement fileEl, VFSLeaf selectedLeaf) {
		super(ureq, wControl, null, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))));
		this.fileEl = fileEl;
		this.selectedLeaf = selectedLeaf;

		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		
		relationsCtrl = new MediaRelationsController(ureq, getWindowControl(), mainForm, null, true, true);
		relationsCtrl.setOpenClose(false);
		listenTo(relationsCtrl);
		
		initForm(ureq);
		initMetadata();
		updateUILicense();
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
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, "", formLayout);
		titleEl.setElementCssClass("o_sel_media_title");
		titleEl.setMandatory(true);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), new ArrayList<>());
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		taxonomyLevelSource = new TaxonomyLevelSelectionSource(getLocale(),
				List.of(),
				() -> taxonomyService.getTaxonomyLevels(mediaModule.getTaxonomyRefs()),
				translate("taxonomy.levels"), translate("table.header.taxonomy"));
		taxonomyLevelEl = uifactory.addObjectSelectionElement("taxonomy", "taxonomy.levels", formLayout, getWindowControl(), true, taxonomyLevelSource);
		
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

		if (fileEl != null) {
			fileEl.clearError();
			if(fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1 || getHandler() == null) {
				fileEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= validateFormItem(ureq, fileEl);
			}
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
		processMediaForm();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void processMediaForm() {
		if (mediaReference == null) {
			createMediaReference();
		}

		saveLicense();
		updateTags();
		updateTaxonomyLevels();

		relationsCtrl.saveRelations(mediaReference);

		dbInstance.commit();
	}

	private void createMediaReference() {
		String title = titleEl.getValue();
		String altText = altTextEl.isVisible() ? altTextEl.getValue() : null;
		String description = descriptionEl.getValue();

		File uploadedFile = null;
		String uploadedFilename = "";
		if (selectedLeaf != null && selectedLeaf.getRelPath() != null) {
			uploadedFile = VFSManager.olatRootFile(selectedLeaf.getRelPath());
			uploadedFilename = selectedLeaf.getName();
		} else if (fileEl != null && fileEl.getUploadFile() != null) {
			uploadedFile = fileEl.getUploadFile();
			uploadedFilename = fileEl.getUploadFileName();
		}

		MediaHandler mediaHandler = getHandler();
		if (mediaHandler == null || uploadedFile == null) {
			return;  // Early exit if no media handler or file
		}

		String mimeType = WebappHelper.getMimeType(uploadedFilename);

		UploadMedia mObject = new UploadMedia(uploadedFile, uploadedFilename, mimeType);
		mediaReference = mediaHandler.createMedia(
				title, description, altText, mObject, businessPath, getIdentity(), MediaLog.Action.UPLOAD
		);
	}

	private void updateTags() {
		List<String> updatedTags = tagsEl.getDisplayNames();
		mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
	}

	private void updateTaxonomyLevels() {
		Collection<TaxonomyLevelRef> updatedLevels = TaxonomyLevelSelectionSource.toRefs(taxonomyLevelEl.getSelectedKeys());
		mediaService.updateTaxonomyLevels(mediaReference, updatedLevels);
	}

	private void initMetadata() {
		MediaHandler handler = getHandler();
		if (titleEl.isEmpty()) {
			titleEl.setValue(fileEl != null ? fileEl.getUploadFileName() : selectedLeaf.getName());
			titleEl.getComponent().setDirty(true);
		}
		titleEl.clearWarning();
		File uploadedFile = null;
		if (fileEl != null && fileEl.getUploadFile() != null) {
			uploadedFile = fileEl.getUploadFile();
		} else if (selectedLeaf != null) {
			uploadedFile = VFSManager.olatRootFile(selectedLeaf.getRelPath());
		}

		if(uploadedFile != null && mediaService.isInMediaCenter(getIdentity(), uploadedFile)) {
			titleEl.setWarningKey("warning.checksum.file");
		}
		altTextEl.setVisible(handler != null && ImageHandler.IMAGE_TYPE.equals(handler.getType()));
		updateUILicense();
		doAutoGenerateAiMetadata(uploadedFile, fileEl != null ? fileEl.getUploadFileName() : selectedLeaf.getName());
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
			if(fileEl.getUploadFile() != null && mediaService.isInMediaCenter(getIdentity(), fileEl.getUploadFile())) {
				titleEl.setWarningKey("warning.checksum.file");
			}
			altTextEl.setVisible(handler != null && ImageHandler.IMAGE_TYPE.equals(handler.getType()));
			updateUILicense();
			doAutoGenerateAiMetadata(fileEl.getUploadFile(), fileEl.getUploadFileName());
		} else {
			updateUILicense();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void updateUILicense() {
		MediaHandler handler = this.getHandler();
		if(handler != null && handler.getUISettings(null).hasLicense()) {
			setLicenseVisibility(true);
			super.updateUILicense();
		} else {
			setLicenseVisibility(false);
		}
	}
	
	private MediaHandler getHandler() {
		MediaHandler handler = null;
		String mimeType = "";
		if (fileEl != null) {
			mimeType = fileEl.getUploadMimeType();
		} else if (selectedLeaf != null) {
			mimeType = WebappHelper.getMimeType(selectedLeaf.getName());
		}

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

	private void doAutoGenerateAiMetadata(File imageFile, String filename) {
		if (imageFile == null || filename == null) return;
		if (!imageDescriptionService.isEnabled()) return;

		// Only for supported raster images
		String suffix = getSuffix(filename);
		if (suffix == null) return;
		String mimeType = aiImageHelper.getMimeType(suffix);
		if (mimeType == null) return;

		String base64 = aiImageHelper.prepareImageBase64(imageFile, suffix);
		if (base64 == null) return;

		AiUsageContext usageContext = AiUsageContext.builder()
				.usageContextType("mc-upload-image")
				.identity(getIdentity())
				.locale(getLocale())
				.resourceType("MediaCenter")
				.resourceId(0L)
				.build();
		AiImageDescriptionResponse response = imageDescriptionService.generateImageDescription(usageContext, base64, mimeType, getLocale());
		if (!response.isSuccess() || response.getDescription() == null) return;

		ImageDescriptionData data = response.getDescription();

		// Populate title if empty or filename-style
		if (data.getTitle() != null) {
			String currentTitle = titleEl.getValue();
			if (!StringHelper.containsNonWhitespace(currentTitle) || isFilenameLike(currentTitle)) {
				titleEl.setValue(data.getTitle());
			}
		}
		if (StringHelper.containsNonWhitespace(data.getDescription())
				&& !StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
			descriptionEl.setValue(data.getDescription());
		}
		if (altTextEl.isVisible()
				&& (altTextEl.getValue() == null || altTextEl.getValue().isBlank())
				&& data.getAltText() != null) {
			altTextEl.setValue(data.getAltText());
		}

		// Add tags
		Set<String> newTags = new LinkedHashSet<>();
		if (StringHelper.containsNonWhitespace(data.getOrientation())) {
			newTags.add(data.getOrientation().toLowerCase());
		}
		for (String tag : data.getColorTags()) {
			newTags.add(tag.toLowerCase());
		}
		for (String tag : data.getCategoryTags()) {
			newTags.add(tag.toLowerCase());
		}
		for (String tag : data.getKeywords()) {
			newTags.add(tag.toLowerCase());
		}
		tagsEl.addNewDisplayNames(newTags);

		// Map AI subject to taxonomy level
		if (StringHelper.containsNonWhitespace(data.getSubject())) {
			mapSubjectToTaxonomy(data.getSubject());
		}

		setFormWarning("ai.generate.metadata.done");
	}

	private void mapSubjectToTaxonomy(String subject) {
		if (taxonomyLevelEl == null || taxonomyLevelSource == null) {
			return;
		}
		String subjectLower = subject.trim().toLowerCase();
		for (ObjectOption option : taxonomyLevelSource.getOptions()) {
			String title = option.getTitle();
			if (title != null && subjectLower.equals(title.trim().toLowerCase())) {
				taxonomyLevelEl.select(option.getKey());
				return;
			}
		}
	}

	private String getSuffix(String filename) {
		if (filename == null) return null;
		int dotPos = filename.lastIndexOf('.');
		if (dotPos >= 0 && dotPos < filename.length() - 1) {
			return filename.substring(dotPos + 1);
		}
		return null;
	}

	private boolean isFilenameLike(String title) {
		if (title == null) return false;
		return title.matches("(?i).*\\.(jpe?g|png|gif|webp|svg|bmp|tiff?)$");
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
