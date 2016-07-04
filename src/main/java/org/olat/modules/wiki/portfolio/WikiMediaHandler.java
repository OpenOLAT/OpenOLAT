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
package org.olat.modules.wiki.portfolio;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.handler.AbstractMediaHandler;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.wiki.WikiPage;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
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
	public String getIconCssClass() {
		return "o_wiki_icon";
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		return null;
	}
	
	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		if(mediaObject instanceof WikiPage) {
			WikiPage page = (WikiPage)mediaObject;
			title = page.getPageName();
		}
		return new Informations(title, null);
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		String content = null;
		if(mediaObject instanceof WikiPage) {
			WikiPage page = (WikiPage)mediaObject;
			content = page.getContent();
		}
		return mediaDao.createMedia(title, description, content, WIKI_HANDLER, businessPath, 70, author);
	}

	@Override
	public Media createMedia(AbstractArtefact artefact) {
		String title = artefact.getTitle();
		String description = artefact.getDescription();
		String content = artefact.getFulltextContent();
		String businessPath = artefact.getBusinessPath();
		if(businessPath == null) {
			businessPath = "[PortfolioV2:0][MediaCenter:0]";
		}
		return mediaDao.createMedia(title, description, content, WIKI_HANDLER, businessPath, artefact.getSignature(), artefact.getAuthor());
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new WikiPageMediaController(ureq, wControl, media);
	}
}
