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
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaHandlerUISettings;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.manager.MediaLogDAO;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.ui.medias.CollectUrlVideoMediaController;
import org.olat.modules.cemedia.ui.medias.NewVideoViaUrlController;
import org.olat.modules.cemedia.ui.medias.VideoViaUrlController;
import org.olat.modules.video.VideoManager;
import org.olat.util.logging.activity.LoggingResourceable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-11-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class VideoViaUrlHandlerDelegate {


	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private DB dbInstance;
	@Autowired
	private ContentEditorFileStorage contentEditorFileStorage;
	@Autowired
	private VideoManager videoManager;

	public VideoViaUrlHandlerDelegate() {
	}

	public String getSubType() {
		return "video-via-url";
	}

	public String getIconCssClass() {
		return "o_icon_youtube";
	}

	public MediaHandlerUISettings getUISettings() {
		return new MediaHandlerUISettings(true, true, "o_icon_refresh",
				false, null, true, true);
	}

	public boolean hasMediaThumbnail(MediaVersion mediaVersion) {
		String storagePath = mediaVersion.getStoragePath();
		if (!StringHelper.containsNonWhitespace(storagePath)) {
			return false;
		}

		VFSContainer storageContainer = contentEditorFileStorage.getMediaContainer(mediaVersion);
		VFSItem item = storageContainer.resolve(mediaVersion.getRootFilename());
		return item instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES;
	}

	public Media createMedia(String title, String description, String altText, String streamingUrl, String businessPath,
							 Identity author, MediaLog.Action action) {
		Media media = mediaDao.createMedia(title, description, null, altText, VideoHandler.VIDEO_TYPE, businessPath,
				null, 60, author);

		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));

		String versionUuid = UUID.randomUUID().toString();
		File mediaDir = contentEditorFileStorage.generateMediaSubDirectory(media);
		String storagePath = contentEditorFileStorage.getRelativePath(mediaDir);

		MediaVersionMetadata mediaVersionMetadata = mediaDao.createVersionMetadata();
		mediaVersionMetadata.setUrl(streamingUrl);

		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), versionUuid, null,
				storagePath, null, mediaVersionMetadata);
		mediaLogDao.createLog(action, null, mediaWithVersion.media(), author);
		dbInstance.commitAndCloseSession();

		String fileName = lookUpMasterThumbnail(versionUuid, storagePath, streamingUrl);
		if (StringHelper.containsNonWhitespace(fileName)) {
			MediaVersion mediaVersion = mediaDao.loadVersionByKey(mediaWithVersion.version().getKey());
			mediaVersion.setRootFilename(fileName);
			mediaVersion.setContent(fileName);
			mediaDao.update(mediaVersion);
			dbInstance.commitAndCloseSession();
		}

		return media;
	}

	public void addVersion(Long mediaKey, String streamingUrl, Identity author) {
		Media media = mediaService.getMediaByKey(mediaKey);

		String versionUuid = UUID.randomUUID().toString();
		File mediaDir = contentEditorFileStorage.generateMediaSubDirectory(media);
		String storagePath = contentEditorFileStorage.getRelativePath(mediaDir);

		MediaVersionMetadata mediaVersionMetadata = mediaDao.createVersionMetadata();
		mediaVersionMetadata.setUrl(streamingUrl);

		media = mediaDao.addVersion(media, new Date(), versionUuid, null, storagePath, null,
				mediaVersionMetadata);
		mediaLogDao.createLog(MediaLog.Action.UPLOAD, null, media, author);
		dbInstance.commitAndCloseSession();
		String fileName = lookUpMasterThumbnail(versionUuid, storagePath, streamingUrl);
		if (StringHelper.containsNonWhitespace(fileName)) {
			MediaVersion mediaVersion = mediaDao.loadByKey(media.getKey()).getVersions().get(0);
			mediaVersion.setRootFilename(fileName);
			mediaVersion.setContent(fileName);
			mediaDao.update(mediaVersion);
			dbInstance.commitAndCloseSession();
		}
	}

	private String lookUpMasterThumbnail(String versionUuid, String storagePath, String url) {
		VFSContainer targetContainer = VFSManager.olatRootContainer("/" + storagePath, null);
		return videoManager.lookUpThumbnail(url, targetContainer, versionUuid);
	}

	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new VideoViaUrlController(ureq, wControl, version, hints);
	}

	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion mediaVersion) {
		return new CollectUrlVideoMediaController(ureq, wControl, media, mediaVersion , true);
	}

	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion mediaVersion,
											  MediaHandler.CreateVersion createVersion, MediaHandler mediaHandler) {
		if (createVersion == MediaHandler.CreateVersion.UPLOAD) {
			return new NewVideoViaUrlController(ureq, wControl, media, mediaHandler);
		}
		return null;
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

	public boolean hasDownload(MediaVersion mediaVersion) {
		return false;
	}
}
