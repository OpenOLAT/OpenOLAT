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
	public String getIconCssClass(Media media) {
		return "o_blog_icon";
	}

	@Override
	public VFSLeaf getThumbnail(Media media, Size size) {
		return null;
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		BlogEntryMedia entry = (BlogEntryMedia)mediaObject;
		Feed feed = entry.getFeed();
		Item item = entry.getItem();
		
		Media media = mediaDao.createMedia(title, description, "", BLOG_ENTRY_HANDLER, businessPath, 70, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		media = mediaDao.updateStoragePath(media, storagePath);
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
