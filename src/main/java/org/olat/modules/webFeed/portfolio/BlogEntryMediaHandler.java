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

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.handler.AbstractMediaHandler;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
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
	public String getIconCssClass(MediaLight media) {
		return "o_blog_icon";
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
		Feed feed = entry.getFeed();
		Item item = entry.getItem();
		
		Media media = mediaDao.createMedia(title, description, "", BLOG_ENTRY_HANDLER, businessPath, 70, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		media = mediaDao.updateStoragePath(media, storagePath, BlogArtefact.BLOG_FILE_NAME);
		VFSContainer mediaContainer = fileStorage.getMediaContainer(media);
		VFSContainer itemContainer = feedManager.getItemContainer(item, feed);
		VFSManager.copyContent(itemContainer, mediaContainer);

		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new BlogEntryMediaController(ureq, wControl, media, true);
	}
}
