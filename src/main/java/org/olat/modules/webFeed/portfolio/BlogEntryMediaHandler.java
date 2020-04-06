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
import java.util.Collections;
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
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.handler.AbstractMediaHandler;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.ui.media.StandardEditMediaController;
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
	private PortfolioFileStorage fileStorage;
	
	public BlogEntryMediaHandler() {
		super(BLOG_ENTRY_HANDLER);
	}

	@Override
	public String getIconCssClass() {
		return "o_blog_icon";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.embed;
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return false;
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		return null;
	}
	
	@Override
	public MediaInformations getInformations(Object mediaObject) {
		BlogEntryMedia entry = (BlogEntryMedia)mediaObject;
		Item item = entry.getItem();
		return new Informations(item.getTitle(), item.getDescription());
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		BlogEntryMedia entry = (BlogEntryMedia)mediaObject;
		Item item = entry.getItem();
		
		Media media = mediaDao.createMedia(title, description, "", BLOG_ENTRY_HANDLER, businessPath, null, 70, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		media = mediaDao.updateStoragePath(media, storagePath, "item.xml");
		VFSContainer mediaContainer = fileStorage.getMediaContainer(media);
		VFSContainer itemContainer = feedManager.getItemContainer(item);
		FeedManager.getInstance().saveItemAsXML(item);
		VFSManager.copyContent(itemContainer, mediaContainer);
		FeedManager.getInstance().deleteItemXML(item);
		
		return media;
	}
	
	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		return new BlogEntryMediaController(ureq, wControl, media, hints);
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new StandardEditMediaController(ureq, wControl, media);
	}
	
	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		File mediaDir = fileStorage.getMediaDirectory(media);
		File[] files = mediaDir.listFiles(SystemFileFilter.FILES_ONLY);
		List<File> attachments = files == null ? Collections.emptyList() : Arrays.asList(files);
		super.exportContent(media, null, attachments, mediaArchiveDirectory, locale);
	}
	
}
