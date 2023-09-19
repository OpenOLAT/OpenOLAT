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
import java.util.List;
import java.util.Set;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
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
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectImageMediaController extends AbstractCollectMediaController implements PageElementAddController {

	public static final Set<String> imageMimeTypes = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");

	private FileElement fileEl;
	private TextElement titleEl;
	private TagSelection tagsEl;
	private TextElement sourceEl;
	private TextElement altTextEl;
	private RichTextElement descriptionEl;
	private TaxonomyLevelSelection taxonomyLevelEl;

	private UploadMedia uploadMedia;
	
	private final Quota quota;
	private final String businessPath;
	private AddElementInfos userObject;
	private final boolean metadataOnly;

	private MediaRelationsController relationsCtrl;
	
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
	}
	
	private void initMetadataForm(FormItemContainer formLayout, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.limitToMimeType(imageMimeTypes, null, null);
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
		
		StaticTextElement filenameEl = uifactory.addStaticTextElement("artefact.filename", "artefact.filename", "", formLayout);
		filenameEl.setVisible(metadataOnly);
		
		if(mediaReference != null) {
			fileEl.setEnabled(false);
			
			MediaVersion currentVersion = mediaReference.getVersions().get(0);
			VFSItem item = fileHandler.getImage(currentVersion);
			if(item instanceof JavaIOItem jItem) {
				fileEl.setInitialFile(jItem.getBasefile());
				filenameEl.setValue(item.getName());
			}
		}
		
		List<TagInfo> tagsInfos = mediaService.getTagInfos(mediaReference, getIdentity(), false);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), tagsInfos);
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		List<TaxonomyLevel> levels = mediaService.getTaxonomyLevels(mediaReference);
		Set<TaxonomyLevel> availableTaxonomyLevels = taxonomyService.getTaxonomyLevelsAsSet(mediaModule.getTaxonomyRefs());
		taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomy.levels", "taxonomy.levels", formLayout,
				getWindowControl(), availableTaxonomyLevels);
		taxonomyLevelEl.setDisplayNameHeader(translate("table.header.taxonomy"));
		taxonomyLevelEl.setSelection(levels);
		
		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 4, -1, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		String altText = mediaReference == null ? null : mediaReference.getAltText();
		altTextEl = uifactory.addTextElement("artefact.alt.text", "artefact.alt.text", 1000, altText, formLayout);
		
		initLicenseForm(formLayout);
		
		String source = (mediaReference != null ? mediaReference.getSource() : null);
		sourceEl = uifactory.addTextElement("source", "mf.source", -1, source, formLayout);

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
			if(mediaService.isInMediaCenter(getIdentity(), fileEl.getUploadFile())) {
				titleEl.setWarningKey("warning.checksum.file");
			}
		}
		super.formInnerEvent(ureq, source, event);
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
		
		Set<TaxonomyLevelRef> updatedLevels = taxonomyLevelEl.getSelection();
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
