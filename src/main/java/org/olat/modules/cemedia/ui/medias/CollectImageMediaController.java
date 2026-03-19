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
package org.olat.modules.cemedia.ui.medias;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.ai.AiImageDescriptionSPI;
import org.olat.core.commons.services.ai.AiImageHelper;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.model.AiImageDescriptionData;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaRelationsController;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectImageMediaController extends AbstractCollectMediaController implements PageElementAddController {

	public static final Set<String> imageMimeTypes = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png", "image/svg+xml");

	private FileElement fileEl;
	private StaticTextElement filenameEl;
	private TextElement titleEl;
	private TagSelection tagsEl;
	private TextElement sourceEl;
	private TextElement altTextEl;
	private RichTextElement descriptionEl;
	private ObjectSelectionElement taxonomyLevelEl;
	private FormLink generateAiLink;

	private UploadMedia uploadMedia;
	
	private final Quota quota;
	private final String businessPath;
	private AddElementInfos userObject;
	private final boolean metadataOnly;

	private MediaRelationsController relationsCtrl;
	
	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiImageHelper aiImageHelper;
	@Autowired
	private ImageHandler fileHandler;
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private TaxonomyService taxonomyService;


	public CollectImageMediaController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null, null, true, false);
	}
	
	public CollectImageMediaController(UserRequest ureq, WindowControl wControl, UploadMedia uploadMedia) {
		this(ureq, wControl, null, uploadMedia, true, true);
	}
	
	public CollectImageMediaController(UserRequest ureq, WindowControl wControl, Media media, boolean metadataOnly) {
		this(ureq, wControl, media, null, false, metadataOnly);
	}
	
	private CollectImageMediaController(UserRequest ureq, WindowControl wControl, Media media, UploadMedia uploadMedia,
			boolean withRelations, boolean metadataOnly) {
		super(ureq, wControl, media, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))));
		this.metadataOnly = metadataOnly;
		this.uploadMedia = uploadMedia;
		quota = mediaService.getQuota(getIdentity(), ureq.getUserSession().getRoles());
		if(media != null) {
			businessPath = media.getBusinessPath();
		} else {
			businessPath = "[HomeSite:" + getIdentity().getKey() + "][MediaCenter:0]";
		}

		if(withRelations) {
			relationsCtrl = new MediaRelationsController(ureq, getWindowControl(), mainForm, null, true, true);
			relationsCtrl.setOpenClose(false);
			listenTo(relationsCtrl);
		}
		
		initForm(ureq);
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
		formLayout.setElementCssClass("o_sel_ce_collect_image_form");
		initMetadataForm(formLayout, ureq);
		
		if(relationsCtrl != null) {
			FormItem relationsItem = relationsCtrl.getInitialFormItem();
			relationsItem.setFormLayout("0_12");
			formLayout.add(relationsItem);
		}

		FormLayoutContainer buttonsCont = uifactory.addInlineFormLayout("buttons", null, formLayout);
		if(relationsCtrl != null) {
			buttonsCont.setFormLayout("0_12");
		}
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if (aiModule.isImageDescriptionGeneratorEnabled()) {
			generateAiLink = uifactory.addFormLink("ai.generate.metadata", buttonsCont, Link.BUTTON);
			generateAiLink.setIconLeftCSS("o_icon o_icon-fw o_icon_ai");
			generateAiLink.setElementCssClass("o_button_ghost");
			updateAiButtonVisibility();
		}
	}
	
	private void initMetadataForm(FormItemContainer formLayout, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.limitToMimeType(imageMimeTypes, "error.image.mimetype", null);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		MediaUIHelper.setQuota(quota, fileEl);
		fileEl.setPreview(ureq.getUserSession(), true);
		fileEl.setVisible(!metadataOnly);
		
		String title = null;
		if(mediaReference != null) {
			title = mediaReference.getTitle();
		} else if(uploadMedia != null) {
			title = uploadMedia.getFilename();
		}
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_collect_title");
		titleEl.setMandatory(true);
		
		filenameEl = uifactory.addStaticTextElement("artefact.filename", "artefact.filename", "", formLayout);
		filenameEl.setVisible(metadataOnly);
		
		if(mediaReference != null) {
			fileEl.setEnabled(false);
			
			MediaVersion currentVersion = mediaReference.getVersions().get(0);
			VFSItem item = fileHandler.getImage(currentVersion);
			if(item instanceof JavaIOItem jItem) {
				fileEl.setInitialFile(jItem.getBasefile());
				filenameEl.setValue(item.getName());
			}
		} else if (metadataOnly && uploadMedia != null) {
			filenameEl.setValue(uploadMedia.getFilename());
		}
		
		List<TagInfo> tagsInfos = mediaService.getTagInfos(mediaReference, getIdentity(), false);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), tagsInfos);
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		ObjectSelectionSource source = new TaxonomyLevelSelectionSource(getLocale(),
				mediaService.getTaxonomyLevels(mediaReference),
				() -> taxonomyService.getTaxonomyLevels(mediaModule.getTaxonomyRefs()),
				translate("taxonomy.levels"), translate("table.header.taxonomy"));
		taxonomyLevelEl = uifactory.addObjectSelectionElement("taxonomy", "taxonomy.levels", formLayout, getWindowControl(), true, source);
		
		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 4, -1, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		String altText = mediaReference == null ? null : mediaReference.getAltText();
		altTextEl = uifactory.addTextElement("artefact.alt.text", "artefact.alt.text", 1000, altText, formLayout);
		
		initLicenseForm(formLayout);
		
		String mediaReferenceSource = (mediaReference != null ? mediaReference.getSource() : null);
		sourceEl = uifactory.addTextElement("source", "mf.source", -1, mediaReferenceSource, formLayout);

		String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		StaticTextElement linkEl = uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		linkEl.setVisible(!metadataOnly && MediaUIHelper.showBusinessPath(businessPath));
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if(fileEl.isVisible() && fileEl.getInitialFile() == null
				&& (fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1)) {
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

		 if (metadataOnly && uploadMedia != null && uploadMedia.getMimeType() != null) {
			filenameEl.clearError();
			if (!imageMimeTypes.contains(uploadMedia.getMimeType().toLowerCase())) {
				filenameEl.setErrorKey("error.image.mimetype");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			if (titleEl.isEmpty()) {
				titleEl.setValue(fileEl.getUploadFileName());
				titleEl.getComponent().setDirty(true);
			}

			titleEl.clearWarning();
			if(fileEl.getUploadFile() != null && mediaService.isInMediaCenter(getIdentity(), fileEl.getUploadFile())) {
				titleEl.setWarningKey("warning.checksum.file");
			}
			updateAiButtonVisibility();
		} else if (generateAiLink == source) {
			doGenerateAiMetadata();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doGenerateAiMetadata() {
		// Get the image file
		File imageFile = null;
		String filename = null;
		if (uploadMedia != null) {
			imageFile = uploadMedia.getFile();
			filename = uploadMedia.getFilename();
		} else if (fileEl.getUploadFile() != null) {
			imageFile = fileEl.getUploadFile();
			filename = fileEl.getUploadFileName();
		} else if (mediaReference != null) {
			MediaVersion currentVersion = mediaReference.getVersions().get(0);
			VFSItem item = fileHandler.getImage(currentVersion);
			if (item instanceof JavaIOItem jItem) {
				imageFile = jItem.getBasefile();
				filename = item.getName();
			}
		}

		if (imageFile == null || filename == null) {
			showWarning("ai.generate.no.image");
			return;
		}

		// Check for SVG
		String suffix = getSuffix(filename);
		if ("svg".equalsIgnoreCase(suffix)) {
			showWarning("ai.generate.svg.not.supported");
			return;
		}

		// Get MIME type and prepare image
		String mimeType = aiImageHelper.getMimeType(suffix);
		if (mimeType == null) {
			showWarning("ai.generate.error");
			return;
		}

		String base64 = aiImageHelper.prepareImageBase64(imageFile, suffix);
		if (base64 == null) {
			showWarning("ai.generate.error");
			return;
		}

		// Get the generator
		AiImageDescriptionSPI generator = aiModule.getImageDescriptionGenerator();
		if (generator == null) {
			showWarning("ai.generate.not.configured");
			return;
		}

		// Call the AI
		AiImageDescriptionResponse response = generator.generateImageDescription(base64, mimeType, getLocale());
		if (!response.isSuccess()) {
			showWarning("ai.generate.failed", new String[] { StringHelper.escapeHtml(response.getError()) });
			return;
		}

		AiImageDescriptionData data = response.getDescription();
		if (data == null) {
			showWarning("ai.generate.error");
			return;
		}

		// Populate title when empty or filename-style (contains a dot extension like "IMG_1234.jpg")
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
		if ((altTextEl.getValue() == null || altTextEl.getValue().isBlank()) && data.getAltText() != null) {
			altTextEl.setValue(data.getAltText());
		}

		// Add AI-generated tags directly to the tag selection (lowercase, deduplicated)
		Set<String> newTags = new LinkedHashSet<>();
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

		setFormWarning("ai.generate.metadata.done");
	}

	private void updateAiButtonVisibility() {
		if (generateAiLink == null) return;
		String filename = getCurrentFilename();
		boolean supported = filename != null && isSupportedRasterImage(filename);
		generateAiLink.setVisible(supported);
	}

	private String getCurrentFilename() {
		if (uploadMedia != null) {
			return uploadMedia.getFilename();
		} else if (fileEl.getUploadFile() != null) {
			return fileEl.getUploadFileName();
		} else if (fileEl.getInitialFile() != null) {
			return fileEl.getInitialFile().getName();
		}
		return null;
	}

	private boolean isSupportedRasterImage(String filename) {
		if (filename == null) return false;
		String suffix = getSuffix(filename);
		if (suffix == null) return false;
		return switch (suffix.toLowerCase()) {
			case "jpg", "jpeg", "png", "gif", "webp" -> true;
			default -> false;
		};
	}

	private boolean isFilenameLike(String title) {
		if (title == null) return false;
		// Filename-style: contains a dot followed by a common image extension
		return title.matches("(?i).*\\.(jpe?g|png|gif|webp|svg|bmp|tiff?)$");
	}

	private String getSuffix(String filename) {
		if (filename == null) return null;
		int dotPos = filename.lastIndexOf('.');
		if (dotPos >= 0 && dotPos < filename.length() - 1) {
			return filename.substring(dotPos + 1);
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String altText = altTextEl.getValue();
			String description = descriptionEl.getValue();
			File uploadedFile;
			String uploadedFilename;
			if(uploadMedia != null) {
				uploadedFile = uploadMedia.getFile();
				uploadedFilename = uploadMedia.getFilename();
			} else {
				uploadedFile = fileEl.getUploadFile();
				uploadedFilename = fileEl.getUploadFileName();
			}
			mediaReference = fileHandler.createMedia(title, description, altText, uploadedFile, uploadedFilename, businessPath,
					getIdentity(), MediaLog.Action.UPLOAD);
		} else {
			mediaReference.setTitle(titleEl.getValue());
			mediaReference.setAltText(altTextEl.getValue());
			mediaReference.setDescription(descriptionEl.getValue());
			mediaReference.setSource(sourceEl.getValue());
			mediaReference = mediaService.updateMedia(mediaReference);
		}
		
		saveLicense();

		List<String> updatedTags = tagsEl.getDisplayNames();
		mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
		
		Collection<TaxonomyLevelRef> updatedLevels = TaxonomyLevelSelectionSource.toRefs(taxonomyLevelEl.getSelectedKeys());
		mediaService.updateTaxonomyLevels(mediaReference, updatedLevels);

		if(relationsCtrl != null) {
			relationsCtrl.saveRelations(mediaReference);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
