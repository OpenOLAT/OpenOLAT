/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.model.VFSTransientMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.CopySourceLeaf;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.AudioHandler;
import org.olat.modules.cemedia.handler.DrawioHandler;
import org.olat.modules.cemedia.handler.FileHandler;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.handler.VideoHandler;
import org.olat.modules.cemedia.model.SearchMediaParameters.Access;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserMediaCenterController extends BasicController {
	
	private static final MediaCenterConfig MEDIA_CENTER_CONFIG = new MediaCenterConfig(true, false, false, false, true, false, false, false, null,
			List.of(AudioHandler.AUDIO_TYPE, DrawioHandler.DRAWIO_TYPE, FileHandler.FILE_TYPE, ImageHandler.IMAGE_TYPE, VideoHandler.VIDEO_TYPE),
			MediaCenterController.ALL_TAB_ID, Access.DIRECT, null);
	
	private final MediaCenterController mediaCenterCtrl;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private LicenseService licenseService;

	protected FileBrowserMediaCenterController(UserRequest ureq, WindowControl wControl, String title) {
		super(ureq, wControl);
		mediaCenterCtrl = new MediaCenterController(ureq, getWindowControl(), null, MEDIA_CENTER_CONFIG);
		mediaCenterCtrl.setFormTranslatedTitle(title);
		listenTo(mediaCenterCtrl);
		putInitialPanel(mediaCenterCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mediaCenterCtrl) {
			if (event instanceof MediaSelectionEvent mediaSelectionEvent) {
				doFireMediaSelectionEvent(ureq, mediaSelectionEvent);
			}
		}
		super.event(ureq, source, event);
	}

	private void doFireMediaSelectionEvent(UserRequest ureq, MediaSelectionEvent mediaSelectionEvent) {
		List<MediaVersion> versions = mediaSelectionEvent.getMedia().getVersions();
		if (!versions.isEmpty()) {
			VFSMetadata vfsMetadata = versions.get(0).getMetadata();
			if (vfsMetadata != null) {
				VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
				if (vfsItem instanceof VFSLeaf vfsLeaf) {
					VFSMetadata mappedMedadata = getMetadata(versions.get(0));
					CopySourceLeaf copySource = new CopySourceLeaf(vfsLeaf, mappedMedadata);
					fireEvent(ureq, new FileBrowserSelectionEvent(List.of(copySource)));
					return;
				}
			}
		}
		
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private VFSMetadata getMetadata(MediaVersion mediaVersion) {
		VFSTransientMetadata metadata = new VFSTransientMetadata();
		Media media = mediaVersion.getMedia();
	
		if (StringHelper.containsNonWhitespace(media.getCreators())) {
			metadata.setCreator(media.getCreators());
		}
		metadata.setTitle(media.getTitle());
		metadata.setComment(media.getDescription());
		metadata.setCity(media.getPlace());
		metadata.setPublisher(media.getPublisher());
		if (media.getPublicationDate() != null) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(media.getPublicationDate());
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR);
			metadata.setPublicationDate(String.valueOf(month), String.valueOf(year));
		}
		metadata.setUrl(media.getUrl());
		metadata.setSource(media.getSource());
		metadata.setLanguage(media.getLanguage());
		
		License license = licenseService.loadOrCreateLicense(media);
		if (license != null) {
			LicenseType licenseType = license.getLicenseType();
			metadata.setLicenseType(licenseType);
			metadata.setLicenseTypeName(licenseType.getName());
			metadata.setLicensor(license.getLicensor());
			metadata.setLicenseText(license.getFreetext());
		}
		
		return metadata;
	}

}
