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
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.ExtendedMediaRenderingHints;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.ImageInspectorController;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandlerVersion;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.ui.medias.AddImageController;
import org.olat.modules.cemedia.ui.medias.CollectImageMediaController;
import org.olat.modules.cemedia.ui.medias.ImageMediaController;
import org.olat.modules.cemedia.ui.medias.NewFileMediaVersionController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ImageHandler extends AbstractMediaHandler implements PageElementStore<ImageElement>, InteractiveAddPageElementHandler {
	
	public static final String IMAGE_TYPE = "image";
	public static final Set<String> mimeTypes = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public ImageHandler() {
		super(IMAGE_TYPE);
	}
	
	@Override
	public String getIconCssClass() {
		return "o_icon_image";
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
	public MediaHandlerVersion hasVersion() {
		return new MediaHandlerVersion(true, true, "o_icon_refresh", false, null);
	}

	@Override
	public String getIconCssClass(MediaVersion media) {
		String filename = media.getRootFilename();
		if (filename != null){
			return CSSHelper.createFiletypeIconCssClassFor(filename);
		}
		return "o_icon_image";
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		String storagePath = media.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(media);
			VFSItem item = storageContainer.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES) {
				thumbnail = vfsRepositoryService.getThumbnail(leaf, size.getWidth(), size.getHeight(), true);
			}
		}
		
		return thumbnail;
	}
	
	public VFSItem getImage(MediaVersion media) {
		VFSContainer storageContainer = fileStorage.getMediaContainer(media);
		return storageContainer.resolve(media.getRootFilename());
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof VFSLeaf mediaFile && mediaFile.canMeta() == VFSConstants.YES) {
			VFSMetadata meta = mediaFile.getMetaInfo();
			title = meta.getTitle();
			description = meta.getComment();
		}
		return new Informations(title, description);
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath, Identity author) {
		UploadMedia mObject = (UploadMedia)mediaObject;
		return createMedia(title, description, altText, mObject.getFile(), mObject.getFilename(), businessPath, author);
	}
	
	public Media createMedia(String title, String description, String altText, File file, String filename, String businessPath, Identity author) {
		Media media = mediaDao.createMedia(title, description, altText, IMAGE_TYPE, businessPath, null, 60, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		
		media = mediaDao.createVersion(media, new Date(), filename, storagePath, filename);

		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		
		return media;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof MediaPart mediaPart) {
			return new ImageInspectorController(ureq, wControl, mediaPart, this);
		}
		return super.getInspector(ureq, wControl, element);
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if(element instanceof MediaPart mediaPart) {
			return new ImageMediaController(ureq, wControl, dataStorage, mediaPart, options);
		}
		return super.getContent(ureq, wControl, element, options);
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new ImageMediaController(ureq, wControl, dataStorage, version, ExtendedMediaRenderingHints.valueOf(hints));
	}
	
	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectImageMediaController(ureq, wControl, media, true);
	}
	
	@Override
	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media, CreateVersion createVersion) {
		if(createVersion == CreateVersion.UPLOAD) {
			return new NewFileMediaVersionController(ureq, wControl, media, this,
					CollectImageMediaController.imageMimeTypes, CollectImageMediaController.MAX_FILE_SIZE, true);
		}
		return null;
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new AddImageController(ureq, wControl, this);
	}
	
	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<File> images = new ArrayList<>();
		List<MediaVersion> versions = media.getVersions();
		for(MediaVersion version:versions) {
			File mediaDir = fileStorage.getMediaDirectory(version);
			images.add(new File(mediaDir, version.getRootFilename()));
		}
		super.exportContent(media, null, images, mediaArchiveDirectory, locale);
	}
	
	@Override
	public ImageElement savePageElement(ImageElement element) {
		MediaPart mediaPart = CoreSpringFactory.getImpl(PageService.class).updatePart((MediaPart)element);
		if(mediaPart.getMedia() != null) {
			mediaPart.getMedia().getMetadataXml();
		}
		return mediaPart;
	}
}
