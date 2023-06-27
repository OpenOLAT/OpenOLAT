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
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.MediaVersionInspectorController;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.MediaRenderingHints;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.ui.medias.CitationMediaController;
import org.olat.modules.cemedia.ui.medias.CollectCitationMediaController;
import org.olat.modules.cemedia.ui.medias.NewTextVersionController;
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
public class CitationHandler extends AbstractMediaHandler implements PageElementStore<MediaPart>, InteractiveAddPageElementHandler {
	
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
	public boolean hasVersion() {
		return true;
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
		return new Informations(null, null);
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath, Identity author) {
		Media media = mediaDao.createMedia(title, description, altText, (String)mediaObject, CITATION_MEDIA, businessPath, null, 60, author);
		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion mediaVersion, MediaRenderingHints hints) {
		return new CitationMediaController(ureq, wControl, mediaVersion, hints);
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectCitationMediaController(ureq, wControl, media, false);
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectCitationMediaController(ureq, wControl, media, true);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CollectCitationMediaController(ureq, wControl);
	}

	@Override
	public Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media) {
		return new NewTextVersionController(ureq, wControl, media, this);
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
		super.exportContent(media, null, null, mediaArchiveDirectory, locale);
	}
}
