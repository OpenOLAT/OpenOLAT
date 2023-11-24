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
package org.olat.modules.cemedia.handler;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandlerUISettings;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.manager.MediaLogDAO;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.ui.medias.CollectUrlVideoMediaController;
import org.olat.modules.cemedia.ui.medias.NewVideoViaUrlController;
import org.olat.modules.cemedia.ui.medias.VideoViaUrlController;
import org.olat.user.manager.ManifestBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-11-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class VideoViaUrlHandler extends AbstractMediaHandler implements PageElementStore<MediaPart>, InteractiveAddPageElementHandler  {

	public static final String VIDEO_VIA_URL_TYPE = "video-via-url";
	private static final Set<String> mimeTypes = Set.of("video/quicktime", "video/mp4");

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private DB dbInstance;

	public VideoViaUrlHandler() {
		super(VIDEO_VIA_URL_TYPE);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_youtube";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return false;
//		return mimeTypes.contains(mimeType);
	}

	@Override
	public MediaHandlerUISettings getUISettings() {
		return new MediaHandlerUISettings(true, true, "o_icon_refresh",
				false, null, true, true);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl, AddSettings settings) {
		return null; //AddVideoViaUrlController(ureq, wControl, this, settings);
	}

	@Override
	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media, CreateVersion createVersion) {
		if (createVersion == CreateVersion.UPLOAD) {
			return new NewVideoViaUrlController(ureq, wControl, media, this);
		}
		return null;
	}

	@Override
	public MediaPart savePageElement(MediaPart element) {
		return null;
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		return null;
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		return null;
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath,
							 Identity author, MediaLog.Action action) {
		Media media = mediaDao.createMedia(title, description, null, altText, VIDEO_VIA_URL_TYPE, businessPath,
				null, 60, author);
		MediaVersionMetadata mediaVersionMetadata = mediaDao.createVersionMetadata();
		mediaVersionMetadata.setUrl((String) mediaObject);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), mediaVersionMetadata);
		mediaLogDao.createLog(action, null, mediaWithVersion.media(), author);
		dbInstance.commitAndCloseSession();
		return media;
	}

	public Media updateMedia(Media media, String url) {
		Media updatedMedia = mediaService.updateMedia(media);
		MediaVersion currentVersion = updatedMedia.getVersions().get(0);
		MediaVersionMetadata mediaVersionMetadata = currentVersion.getVersionMetadata();
		if (mediaVersionMetadata == null) {
			mediaVersionMetadata = mediaDao.createVersionMetadata();
			currentVersion.setVersionMetadata(mediaVersionMetadata);
			mediaDao.update(currentVersion);
		}
		mediaVersionMetadata.setUrl(url);
		mediaDao.update(mediaVersionMetadata);
		dbInstance.commitAndCloseSession();
		return updatedMedia;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new VideoViaUrlController(ureq, wControl, version, hints);
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return null;
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion mediaVersion) {
		return new CollectUrlVideoMediaController(ureq, wControl, media, mediaVersion , true);
	}

	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {

	}

	@Override
	public boolean hasDownload() {
		return false;
	}
}
