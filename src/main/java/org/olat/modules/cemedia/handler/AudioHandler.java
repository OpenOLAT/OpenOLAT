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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
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
import org.olat.modules.cemedia.ui.medias.AVAudioVersionMediaController;
import org.olat.modules.cemedia.ui.medias.AddAudioController;
import org.olat.modules.cemedia.ui.medias.AudioMediaController;
import org.olat.modules.cemedia.ui.medias.CollectAudioMediaController;
import org.olat.modules.cemedia.ui.medias.NewFileMediaVersionController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-10-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class AudioHandler extends AbstractMediaHandler implements PageElementStore<MediaPart>, InteractiveAddPageElementHandler {

	public static final String AUDIO_TYPE = "audio";
	public static final Set<String> mimeTypes = Set.of("audio/mp4", "audio/mp3", "audio/mpeg", "audio/m4a");

	public static final int MAX_RECORDING_TIME_IN_MS = 600 * 1000;

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;

	public AudioHandler() {
		super(AUDIO_TYPE);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_audio";
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
	public MediaHandlerUISettings getUISettings() {
		return new MediaHandlerUISettings(true, true, "o_icon_refresh",
				true, "o_icon_audio_record", true, true);
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		return null;
	}

	public VFSItem getAudioItem(MediaVersion media) {
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
		if (mediaObject instanceof UploadMedia uploadMedia) {
			return createMedia(title, description, altText, uploadMedia.getFile(), uploadMedia.getFilename(),
					businessPath, author, action);
		}
		return null;
	}

	public Media createMedia(String title, String description, String altText, File file, String filename,
							 String businessPath, Identity author, MediaLog.Action action) {
		Media media = mediaDao.createMedia(title, description, null, altText, AUDIO_TYPE, businessPath,
				null, 60, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);

		media = mediaDao.createVersion(media, new Date(), null, filename, storagePath, filename).media();
		mediaLogDao.createLog(action, null, media, author);
		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));

		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new AudioMediaController(ureq, wControl, version, hints);
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectAudioMediaController(ureq, wControl, media, true);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl, AddSettings settings) {
		return new AddAudioController(ureq, wControl, this, settings);
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
											  CreateVersion createVersion) {
		if (createVersion == CreateVersion.UPLOAD) {
			return new NewFileMediaVersionController(ureq, wControl, media, this, mimeTypes, false);
		} else if (createVersion == CreateVersion.CREATE) {
			return new AVAudioVersionMediaController(ureq, wControl, media, MAX_RECORDING_TIME_IN_MS);
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
		List<File> audioFiles = new ArrayList<>();
		List<MediaVersion> versions = media.getVersions();
		for (MediaVersion version : versions) {
			File mediaDir = fileStorage.getMediaDirectory(version);
			audioFiles.add(new File(mediaDir, version.getRootFilename()));
		}
		super.exportContent(media, null, audioFiles, mediaArchiveDirectory, locale);
	}
}