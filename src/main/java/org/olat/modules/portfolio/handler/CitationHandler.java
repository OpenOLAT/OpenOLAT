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
import java.util.Locale;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.portfolio.ui.media.CitationMediaController;
import org.olat.modules.portfolio.ui.media.CollectCitationMediaController;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CitationHandler extends AbstractMediaHandler implements InteractiveAddPageElementHandler {
	
	public static final String CITATION_MEDIA = "citation";
	
	@Autowired
	private MediaDAO mediaDao;

	public CitationHandler() {
		super(CITATION_MEDIA);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_citation";
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
		return new Informations(null, null);
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		Media media = mediaDao.createMedia(title, description, (String)mediaObject, CITATION_MEDIA, businessPath, null, 60, author);
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		return new CitationMediaController(ureq, wControl, media, hints);
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectCitationMediaController(ureq, wControl, media);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CollectCitationMediaController(ureq, wControl);
	}

	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		super.exportContent(media, null, null, mediaArchiveDirectory, locale);
	}
}
