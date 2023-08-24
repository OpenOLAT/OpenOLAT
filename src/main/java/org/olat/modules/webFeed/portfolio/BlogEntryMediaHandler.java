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
package org.olat.modules.webFeed.portfolio;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.AbstractMediaHandler;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.ui.medias.StandardEditMediaController;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BlogEntryMediaHandler extends AbstractMediaHandler {
	
	public static final String BLOG_ENTRY_HANDLER = BlogFileResource.TYPE_NAME;

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private FeedManager feedManager;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	
	public BlogEntryMediaHandler() {
		super(BLOG_ENTRY_HANDLER);
	}

	@Override
	public String getIconCssClass() {
		return "o_blog_icon";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return false;
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		return null;
	}
	
	@Override
	public MediaInformations getInformations(Object mediaObject) {
		BlogEntryMedia entry = (BlogEntryMedia)mediaObject;
		Item item = entry.getItem();
		return new Informations(item.getTitle(), item.getDescription());
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath,
			Identity author, MediaLog.Action action) {
		BlogEntryMedia entry = (BlogEntryMedia)mediaObject;
		Item item = entry.getItem();
		
		Media media = mediaDao.createMedia(title, description, null, altText, BLOG_ENTRY_HANDLER, businessPath, null, 70, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "-", storagePath, "item.xml");
		media = mediaWithVersion.media();
		MediaVersion currentVersion = mediaWithVersion.version();
		VFSContainer mediaContainer = fileStorage.getMediaContainer(currentVersion);
		VFSContainer itemContainer = feedManager.getItemContainer(item);
		FeedManager.getInstance().saveItemAsXML(item);
		VFSManager.copyContent(itemContainer, mediaContainer);
		FeedManager.getInstance().deleteItemXML(item);
		
		return media;
	}
	
	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		return new BlogEntryMediaController(ureq, wControl, version, hints);
	}
	
	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new StandardEditMediaController(ureq, wControl, media);
	}

	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<MediaVersion> versions = media.getVersions();
		List<File> attachments = null;
		if(!versions.isEmpty()) {
			File mediaDir = fileStorage.getMediaDirectory(versions.get(0));
			File[] files = mediaDir.listFiles(SystemFileFilter.FILES_ONLY);
			attachments = files == null ? List.of() : Arrays.asList(files);
		}
		if(attachments == null) {
			attachments = List.of();
		}
		super.exportContent(media, null, attachments, mediaArchiveDirectory, locale);
	}
	
}
