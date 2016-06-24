package org.olat.modules.wiki.portfolio;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.handler.AbstractMediaHandler;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.wiki.WikiPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class WikiMediaHandler extends AbstractMediaHandler {
	
	public static final String WIKI_HANDLER = WikiResource.TYPE_NAME;

	@Autowired
	private MediaDAO mediaDao;
	
	public WikiMediaHandler() {
		super(WIKI_HANDLER);
	}

	@Override
	public String getIconCssClass(Media media) {
		return "o_wiki_icon";
	}

	@Override
	public VFSLeaf getThumbnail(Media media, Size size) {
		return null;
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		String content = null;
		if(mediaObject instanceof WikiPage) {
			WikiPage page = (WikiPage)mediaObject;
			content = page.getContent();
		}
		
		Media media = mediaDao.createMedia(title, description, content, WIKI_HANDLER, businessPath, 70, author);
		
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new WikiPageMediaController(ureq, wControl, media);
	}
	
	

}
