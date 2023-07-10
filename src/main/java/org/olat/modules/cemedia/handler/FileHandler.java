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

import static org.olat.core.commons.services.doceditor.DocEditor.Mode.EDIT;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.DocTemplates.Builder;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
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
import org.olat.modules.cemedia.ui.medias.AddFileController;
import org.olat.modules.cemedia.ui.medias.CollectFileMediaController;
import org.olat.modules.cemedia.ui.medias.FileMediaController;
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
public class FileHandler extends AbstractMediaHandler implements PageElementStore<MediaPart>, InteractiveAddPageElementHandler {
	
	public static final String FILE_TYPE = "bc";
	
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public static DocTemplates getEditableTemplates(Identity identity, Roles roles, Locale locale) {
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		Builder builder = DocTemplates.builder(locale);
		if (docEditorService.hasEditor(identity, roles, "docx", EDIT, true, false)) {
			builder.addDocx();
		}
		if (docEditorService.hasEditor(identity, roles, "xlsx", EDIT, true, false)) {
			builder.addXlsx();
		}
		if (docEditorService.hasEditor(identity, roles, "pptx", EDIT, true, false)) {
			builder.addPptx();
		}
		return builder.build();
	}
	
	public FileHandler() {
		super(FILE_TYPE);
	}
	
	@Override
	public String getIconCssClass() {
		return "o_filetype_file";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}
	
	@Override
	public MediaHandlerUISettings getUISettings() {
		return new MediaHandlerUISettings(true, true, "o_icon_refresh", false, null, true);
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return StringHelper.containsNonWhitespace(mimeType)
				&& !ImageHandler.mimeTypes.contains(mimeType)
				&& !VideoHandler.mimeTypes.contains(mimeType);
	}

	@Override
	public String getIconCssClass(MediaVersion mediaVersion) {	
		if (mediaVersion != null && mediaVersion.getRootFilename() != null){
			return CSSHelper.createFiletypeIconCssClassFor(mediaVersion.getRootFilename());
		}
		return "o_filetype_file";
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		String storagePath = media.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(media);
			VFSItem item = storageContainer.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES
					&& vfsRepositoryService.isThumbnailAvailable(leaf)) {
				thumbnail = vfsRepositoryService.getThumbnail(leaf, size.getWidth(), size.getHeight(), true);
			}
		}
		
		return thumbnail;
	}
	
	public VFSItem getItem(MediaVersion media) {
		VFSContainer storageContainer = fileStorage.getMediaContainer(media);
		return storageContainer.resolve(media.getRootFilename());
	}
	
	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof VFSItem item && item.canMeta() == VFSConstants.YES) {
			VFSMetadata meta = item.getMetaInfo();
			title = meta.getTitle();
			description = meta.getComment();
		}
		return new Informations(title, description);
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath,
			Identity author, MediaLog.Action action) {
		UploadMedia mObject = (UploadMedia)mediaObject;
		return createMedia(title, description, altText, mObject.getFile(), mObject.getFilename(), businessPath, author, action);
	}

	public Media createMedia(String title, String description, String altText, File file, String filename, String businessPath, Identity author, MediaLog.Action action) {
		Media media = mediaDao.createMedia(title, description, altText, FILE_TYPE, businessPath, null, 60, author);
		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		media = mediaDao.createVersion(media, new Date(), filename, storagePath, filename);
		mediaLogDao.createLog(action, media, author);
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new FileMediaController(ureq, wControl, version, hints);
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectFileMediaController(ureq, wControl, media, true);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl, AddSettings settings) {
		return new AddFileController(ureq, wControl, this, settings);
	}
	
	@Override
	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media, CreateVersion createVersion) {
		if(CreateVersion.UPLOAD == createVersion) {
			return new NewFileMediaVersionController(ureq, wControl, media, this, null, CollectFileMediaController.MAX_FILE_SIZE, true);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof MediaPart part) {
			return new MediaVersionInspectorController(ureq, wControl, part, this);
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
		List<File> files = new ArrayList<>();
		List<MediaVersion> versions = media.getVersions();
		for(MediaVersion version:versions) {
			if(StringHelper.containsNonWhitespace(version.getStoragePath()) && StringHelper.containsNonWhitespace(version.getRootFilename())) {
				File mediaDir = fileStorage.getMediaDirectory(version);
				files.add(new File(mediaDir, version.getRootFilename()));
			}
		}
		super.exportContent(media, null, files, mediaArchiveDirectory, locale);
	}
}
