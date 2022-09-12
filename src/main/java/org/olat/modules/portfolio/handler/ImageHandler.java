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
package org.olat.modules.portfolio.handler;

import java.io.File;
import java.util.ArrayList;
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
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.ui.ImageInspectorController;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.model.ExtendedMediaRenderingHints;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.media.CollectImageMediaController;
import org.olat.modules.portfolio.ui.media.ImageMediaController;
import org.olat.modules.portfolio.ui.media.UploadMedia;
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
	private PortfolioFileStorage fileStorage;
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
	public String getIconCssClass(MediaLight media) {
		String filename = media.getRootFilename();
		if (filename != null){
			return CSSHelper.createFiletypeIconCssClassFor(filename);
		}
		return "o_icon_image";
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		String storagePath = media.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(media);
			VFSItem item = storageContainer.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf && item.canMeta() == VFSConstants.YES) {
				thumbnail = vfsRepositoryService.getThumbnail((VFSLeaf)item, size.getHeight(), size.getWidth(), true);
			}
		}
		
		return thumbnail;
	}
	
	public VFSItem getImage(Media media) {
		VFSContainer storageContainer = fileStorage.getMediaContainer(media);
		return storageContainer.resolve(media.getRootFilename());
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof VFSLeaf && ((VFSLeaf)mediaObject).canMeta() == VFSConstants.YES) {
			VFSMetadata meta = ((VFSLeaf)mediaObject).getMetaInfo();
			title = meta.getTitle();
			description = meta.getComment();
		}
		return new Informations(title, description);
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		UploadMedia mObject = (UploadMedia)mediaObject;
		return createMedia(title, description, mObject.getFile(), mObject.getFilename(), businessPath, author);
	}
	
	public Media createMedia(String title, String description, File file, String filename, String businessPath, Identity author) {
		Media media = mediaDao.createMedia(title, description, filename, IMAGE_TYPE, businessPath, null, 60, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		mediaDao.updateStoragePath(media, storagePath, filename);
		
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		
		return media;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)element;
			return new ImageInspectorController(ureq, wControl, mediaPart, this);
		}
		return super.getInspector(ureq, wControl, element);
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
		if(element instanceof MediaPart) {
			return new ImageMediaController(ureq, wControl, dataStorage, (MediaPart)element, options);
		}
		return super.getContent(ureq, wControl, element, options);
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		return new ImageMediaController(ureq, wControl, dataStorage, media, ExtendedMediaRenderingHints.valueOf(hints));
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectImageMediaController(ureq, wControl, media);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CollectImageMediaController(ureq, wControl);
	}
	
	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<File> images = new ArrayList<>();
		File mediaDir = fileStorage.getMediaDirectory(media);
		images.add(new File(mediaDir, media.getRootFilename()));
		super.exportContent(media, null, images, mediaArchiveDirectory, locale);
	}
	
	@Override
	public ImageElement savePageElement(ImageElement element) {
		MediaPart mediaPart = CoreSpringFactory.getImpl(PortfolioService.class).updatePart((MediaPart)element);
		if(mediaPart.getMedia() != null) {
			mediaPart.getMedia().getMetadataXml();
		}
		return mediaPart;
	}
}
