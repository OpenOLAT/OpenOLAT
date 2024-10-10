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
import java.util.Set;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.AudioHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectAudioMediaController extends AbstractCollectMediaController implements PageElementAddController {

	public static final Set<String> audioMimeTypes = Set.of("audio/mp3", "audio/mpeg", "audio/m4a", "audio/mp4");

	private FileElement fileEl;
	private TextElement titleEl;

	private UploadMedia uploadedMedia;

	private final Quota quota;
	private AddElementInfos userObject;

	@Autowired
	private AudioHandler fileHandler;

	public CollectAudioMediaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))));
		setBusinessPath("[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]");
		quota = mediaService.getQuota(getIdentity(), ureq.getUserSession().getRoles());
		createRelationsController(ureq);
		initForm(ureq);
	}

	public CollectAudioMediaController(UserRequest ureq, WindowControl wControl, Media media, boolean metadataOnly) {
		super(ureq, wControl, media, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))), metadataOnly);
		setBusinessPath(media.getBusinessPath());
		quota = mediaService.getQuota(getIdentity(), ureq.getUserSession().getRoles());
		this.mediaReference = media;
		initForm(ureq);
	}

	public CollectAudioMediaController(UserRequest ureq, WindowControl wControl,
									   UploadMedia uploadedMedia, String businessPath, boolean metadataOnly) {
		super(ureq, wControl, null, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))), metadataOnly);
		quota = mediaService.getQuota(getIdentity(), ureq.getUserSession().getRoles());
		this.uploadedMedia = uploadedMedia;
		setBusinessPath(businessPath);
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
		formLayout.setElementCssClass("o_sel_pf_collect_video_form");
		initMetadataForm(formLayout);
		initRelationsAndSaveCancel(formLayout, ureq);
	}

	private void initMetadataForm(FormItemContainer formLayout) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.limitToMimeType(audioMimeTypes, "error.audio.mimetype", null);
		fileEl.setVisible(!isMetadataOnly());
		fileEl.addActionListener(FormEvent.ONCHANGE);
		MediaUIHelper.setQuota(quota, fileEl);
		fileEl.setDeleteEnabled(true);
		
		String title = null;
		if(mediaReference != null) {
			title = mediaReference.getTitle();
		} else if(uploadedMedia != null) {
			title = uploadedMedia.getFilename();
		}
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		StaticTextElement filenameEl = uifactory.addStaticTextElement("artefact.filename", "artefact.filename", "", formLayout);
		filenameEl.setVisible(isMetadataOnly());
		if(mediaReference != null) {
			fileEl.setEnabled(false);
			
			MediaVersion currentVersion = mediaReference.getVersions().get(0);
			VFSItem item = fileHandler.getAudioItem(currentVersion);
			if(item instanceof JavaIOItem jItem) {
				fileEl.setInitialFile(jItem.getBasefile());
				filenameEl.setValue(item.getName());
			}
		}

		initCommonMetadata(formLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if(fileEl.isVisible()) {
			if(fileEl.getInitialFile() == null && (fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1)) {
				fileEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= fileEl.validate();
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
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String description = getDescription();
			
			File uploadedFile;
			String uploadedFilename;
			if(uploadedMedia != null) {
				uploadedFile = uploadedMedia.getFile();
				uploadedFilename = uploadedMedia.getFilename();
			} else {
				uploadedFile = fileEl.getUploadFile();
				uploadedFilename = fileEl.getUploadFileName();
			}
			mediaReference = fileHandler.createMedia(title, description, null, uploadedFile, uploadedFilename,
					getBusinessPath(), getIdentity(), MediaLog.Action.UPLOAD);
		} else {
			mediaReference.setTitle(titleEl.getValue());
			mediaReference.setDescription(getDescription());
			mediaReference = mediaService.updateMedia(mediaReference);
		}
		
		saveLicense();
		saveTags();
		saveTaxonomyLevels();
		saveRelations();

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
