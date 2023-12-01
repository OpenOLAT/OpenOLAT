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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.handler.VideoViaUrlHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaVersionChangedEvent;
import org.olat.modules.video.VideoFormatExtended;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.ui.VideoAdminController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-11-17<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CollectUrlVideoMediaController extends AbstractCollectMediaController implements PageElementAddController {

	private TextElement titleEl;
	private TextElement urlEl;
	private FormLink lookUpTitleButton;
	private StaticTextElement durationEl;
	private StaticTextElement widthEl;
	private StaticTextElement heightEl;
	private StaticTextElement aspectRatioEl;
	private AddElementInfos userObject;
	private Long mediaVersionKey;

	@Autowired
	private VideoViaUrlHandler handler;

	@Autowired
	private VideoManager videoManager;

	@Autowired
	private MediaService mediaService;

	public CollectUrlVideoMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl, media, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(VideoAdminController.class, ureq.getLocale())));
		setBusinessPath("[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]");
		createRelationsController(ureq);
		initForm(ureq);
	}

	public CollectUrlVideoMediaController(UserRequest ureq, WindowControl wControl, Media media,
										  MediaVersion mediaVersion, boolean metadataOnly) {
		super(ureq, wControl, media, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(VideoAdminController.class, ureq.getLocale())), metadataOnly);
		setBusinessPath(media.getBusinessPath());
		this.mediaReference = media;
		this.mediaVersionKey = mediaVersion != null ? mediaVersion.getKey() : null;
		initForm(ureq);
	}

	public Media getMediaReference() {
		return mediaReference;
	}

	@Override
	public PageElement getPageElement() {
		return null;
	}

	@Override
	public void setUserObject(AddElementInfos userObject) {
		this.userObject = userObject;
	}

	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!isMetadataOnly()) {
			setFormInfo("add.video.via.url.info");
		}
		initMetadataForm(formLayout);
		initRelationsAndSaveCancel(formLayout, ureq);
	}

	private void initMetadataForm(FormItemContainer formLayout) {
		if (isMetadataOnly()) {
			initTitle(formLayout);
			initVersionMetadata(formLayout);
		} else {
			initVersionMetadata(formLayout);
			initLookUpTitleButton(formLayout);
			initTitle(formLayout);
		}

		initCommonMetadata(formLayout);
	}

	private void initVersionMetadata(FormItemContainer formLayout) {
		urlEl = uifactory.addTextElement("artefact.url", 512, null, formLayout);
		durationEl = uifactory.addStaticTextElement("video.config.duration", null, formLayout);
		widthEl = uifactory.addStaticTextElement("video.config.width", null, formLayout);
		heightEl = uifactory.addStaticTextElement("video.config.height", null, formLayout);
		aspectRatioEl = uifactory.addStaticTextElement("video.config.ratio", null, formLayout);

		updateVersionMetadata(mediaVersionKey);
	}

	private void updateVersionMetadata(Long versionKey) {
		String url = null;
		String duration = null;
		String width = null;
		String height = null;
		String aspectRatio = null;
		if (mediaReference != null) {
			List<MediaVersion> mediaVersions = mediaReference.getVersions();
			MediaVersion currentVersion = mediaVersions.stream().filter(v -> v.getKey().equals(versionKey))
					.findFirst().orElse(mediaReference.getVersions().get(0));
			MediaVersionMetadata mediaVersionMetadata = currentVersion.getVersionMetadata();
			if (mediaVersionMetadata != null) {
				url = mediaVersionMetadata.getUrl();
				duration = mediaVersionMetadata.getLength();
				if (mediaVersionMetadata.getWidth() != null) {
					width = Integer.toString(mediaVersionMetadata.getWidth());
				}
				if (mediaVersionMetadata.getHeight() != null) {
					height = Integer.toString(mediaVersionMetadata.getHeight());
				}
				if (width != null && height != null) {
					aspectRatio = videoManager.getAspectRatio(mediaVersionMetadata.getWidth(),
							mediaVersionMetadata.getHeight());
				}
			}
		}

		if (isMetadataOnly()) {
			urlEl.setMandatory(false);
			urlEl.setEnabled(false);

			if (url != null) {
				urlEl.setValue(url);
				urlEl.setVisible(true);
			} else {
				urlEl.setVisible(false);
			}
		} else {
			urlEl.setVisible(true);
			urlEl.setMandatory(true);
			urlEl.setEnabled(true);
		}

		if (duration != null) {
			durationEl.setValue(duration);
			durationEl.setVisible(true);
		} else {
			durationEl.setVisible(false);
		}

		if (width != null) {
			widthEl.setValue(width);
			widthEl.setVisible(true);
		} else {
			widthEl.setVisible(false);
		}

		if (height != null) {
			heightEl.setValue(height);
			heightEl.setVisible(true);
		} else {
			heightEl.setVisible(false);
		}

		if (aspectRatio != null) {
			aspectRatioEl.setValue(aspectRatio);
			aspectRatioEl.setVisible(true);
		} else {
			aspectRatioEl.setVisible(false);
		}
	}

	private void initLookUpTitleButton(FormItemContainer formLayout) {
		lookUpTitleButton = uifactory.addFormLink("look.up.title", "look.up.title", null,
				formLayout, Link.BUTTON);
	}

	private void initTitle(FormItemContainer formLayout) {
		String title = mediaReference != null ? mediaReference.getTitle() : null;
		titleEl = uifactory.addTextElement("artefact.title", 255, title, formLayout);
		titleEl.setMandatory(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);

		if (source == lookUpTitleButton) {
			String title = videoManager.lookUpTitle(urlEl.getValue());
			if (StringHelper.containsNonWhitespace(title)) {
				titleEl.setValue(title);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		urlEl.clearError();
		if( !StringHelper.containsNonWhitespace(urlEl.getValue())) {
			urlEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (VideoFormatExtended.valueOfUrl(urlEl.getValue()) == null) {
			urlEl.setErrorKey("error.format.not.supported");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
		if (event instanceof MediaVersionChangedEvent mediaVersionChangedEvent) {
			reload(mediaVersionChangedEvent.getVersionKey());
		}
	}

	private void reload(Long versionKey) {
		mediaReference = mediaService.getMediaByKey(mediaReference.getKey());
		updateVersionMetadata(versionKey);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (mediaReference == null) {
			String title = titleEl.getValue();
			String description = getDescription();

			mediaReference = handler.createMedia(title, description, null, urlEl.getValue(),
					getBusinessPath(), getIdentity(), MediaLog.Action.CREATED);
		} else {
			mediaReference.setTitle(titleEl.getValue());
			mediaReference.setDescription(getDescription());
			mediaReference = handler.updateMedia(mediaReference, urlEl.getValue());
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
