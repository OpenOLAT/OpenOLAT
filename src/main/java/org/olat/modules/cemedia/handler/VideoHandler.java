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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVVideoQuality;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.MediaVersionInspectorController;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandlerUISettings;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.manager.MediaLogDAO;
import org.olat.modules.cemedia.ui.medias.AVVideoVersionMediaController;
import org.olat.modules.cemedia.ui.medias.AddVideoController;
import org.olat.modules.cemedia.ui.medias.CollectVideoMediaController;
import org.olat.modules.cemedia.ui.medias.NewFileMediaVersionController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.modules.cemedia.ui.medias.VideoMediaController;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoHandler extends AbstractMediaHandler implements PageElementStore<MediaPart>, InteractiveAddPageElementHandler {
	
	public static final String VIDEO_TYPE = "video";
	public static final Set<String> mimeTypes = Set.of("video/mp4");
	
	public static final int MAX_RECORDING_TIME_IN_MS = 600 * 1000;
	public static final AVVideoQuality VIDEO_QUALITY = AVVideoQuality.medium;

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private AVModule avModule;
	@Autowired
	private VideoViaUrlHandlerDelegate urlDelegate;

	public VideoHandler() {
		super(VIDEO_TYPE);
	}
	
	@Override
	public String getSubType(MediaVersion mediaVersion) {
		if (mediaVersion.hasUrl()) {
			return urlDelegate.getSubType();
		}
		return super.getSubType(mediaVersion);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_video";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return mimeTypes.contains(mimeType);
	}
	
	@Override
	public MediaHandlerUISettings getUISettings(MediaVersion mediaVersion) {
		if (mediaVersion != null && mediaVersion.hasUrl()) {
			return urlDelegate.getUISettings();
		}
		return new MediaHandlerUISettings(true, true, "o_icon_refresh",
				avModule.isVideoRecordingEnabled(), "o_icon_video_record", true, true);
	}

	@Override
	public String getIconCssClass(MediaVersion mediaVersion) {
		if (mediaVersion.hasUrl()) {
			return urlDelegate.getIconCssClass();
		}

		if (mediaVersion != null && mediaVersion.getRootFilename() != null){
			return CSSHelper.createFiletypeIconCssClassFor(mediaVersion.getRootFilename());
		}
		return getIconCssClass();
	}

	@Override
	public boolean hasMediaThumbnail(MediaVersion mediaVersion) {
		if (mediaVersion.hasUrl()) {
			return urlDelegate.hasMediaThumbnail(mediaVersion);
		}
		return super.hasMediaThumbnail(mediaVersion);
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion mediaVersion, Size size) {
		String storagePath = mediaVersion.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(mediaVersion);
			VFSItem item = storageContainer.resolve(mediaVersion.getRootFilename());
			if(item instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES) {
				if (leaf.getSize() > 0) {
					thumbnail = vfsRepositoryService.getThumbnail(leaf, size.getWidth(), size.getHeight(), true);
				}
			}
		}
		
		return thumbnail;
	}
	
	public VFSItem getVideoItem(MediaVersion media) {
		VFSContainer storageContainer = fileStorage.getMediaContainer(media);
		return storageContainer.resolve(media.getRootFilename());
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES) {
			VFSMetadata meta = leaf.getMetaInfo();
			title = meta.getTitle();
			description = meta.getComment();
		}
		return new Informations(title, description);
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath,
			Identity author, MediaLog.Action action) {
		if (mediaObject instanceof String streamingUrl) {
			return urlDelegate.createMedia(title, description, altText, streamingUrl, businessPath, author, action);
		}
		if (mediaObject instanceof UploadMedia uploadMedia) {
			return createMedia(title, description, altText, uploadMedia.getFile(), uploadMedia.getFilename(),
					businessPath, author, action);
		}
		return null;
	}
	
	public Media createMedia(String title, String description, String altText, File file, String filename, String businessPath,
			Identity author, MediaLog.Action action) {
		Media media = mediaDao.createMedia(title, description, null, altText, VIDEO_TYPE, businessPath, null, 60, author);
		
		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		media = mediaDao.createVersion(media, new Date(), null, filename, storagePath, filename).media();
		mediaLogDao.createLog(action, null, media, author);
		return media;
	}

	public void addVersion(Long key, String url, Identity identity) {
		urlDelegate.addVersion(key, url, identity);
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		if (version.hasUrl()) {
			return urlDelegate.getMediaController(ureq, wControl, version, hints);
		}
		return new VideoMediaController(ureq, wControl, dataStorage, version, hints);
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion mediaVersion) {
		if (mediaVersion.hasUrl()) {
			return urlDelegate.getEditMetadataController(ureq, wControl, media, mediaVersion);
		}
		return new CollectVideoMediaController(ureq, wControl, media, true);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl, AddSettings settings) {
		return new AddVideoController(ureq, wControl, this, settings);
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof MediaPart part) {
			return new MediaVersionInspectorController(ureq, wControl, part, this);
		}
		return null;
	}
	
	@Override
	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media,
											  MediaVersion mediaVersion, CreateVersion createVersion) {
		if (mediaVersion.hasUrl()) {
			return urlDelegate.getNewVersionController(ureq, wControl, media, mediaVersion, createVersion,
					this);
		}
		if(createVersion == CreateVersion.UPLOAD) {
			return new NewFileMediaVersionController(ureq, wControl, media, this,
					CollectVideoMediaController.videoMimeTypes, true);
		} else if(createVersion == CreateVersion.CREATE) {
			return new AVVideoVersionMediaController(ureq, wControl, media, MAX_RECORDING_TIME_IN_MS, VIDEO_QUALITY);
		}
		return null;
	}
	
	@Override
	public MediaPart savePageElement(MediaPart element) {
		MediaPart mediaPart = CoreSpringFactory.getImpl(PageService.class).updatePart(element);
		if(mediaPart.getMedia() != null) {
			mediaPart.getMedia().getMetadataXml();
		}
		return mediaPart;
	}

	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<File> videos = new ArrayList<>();
		List<MediaVersion> versions = media.getVersions();
		for(MediaVersion version:versions) {
			File mediaDir = fileStorage.getMediaDirectory(version);
			videos.add(new File(mediaDir, version.getRootFilename()));
		}
		super.exportContent(media, null, videos, mediaArchiveDirectory, locale);
	}

	@Override
	public boolean hasDownload(MediaVersion mediaVersion) {
		if (mediaVersion.hasUrl()) {
			return urlDelegate.hasDownload(mediaVersion);
		}
		return super.hasDownload(mediaVersion);
	}
}
