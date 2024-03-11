/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.video.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.id.Organisation;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.site.VideoSiteDef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.AuthoringEditAccessController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class VideoEditAccessController extends AuthoringEditAccessController {
	
	private VideoCollectionAccessController videoCollectionAccessController;
	
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private SiteDefinitions siteDefinitions;
	
	public VideoEditAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, entry, readOnly);
	}

	@Override
	protected void initAccessOffers(UserRequest ureq, VelocityContainer vc) {
		// only show VideoCollectionAccessController if VideoCollection Site is enabled
		if (siteDefinitions.isSiteEnabled(VideoSiteDef.class)) {
			removeAsListenerAndDispose(videoCollectionAccessController);

			videoCollectionAccessController = new VideoCollectionAccessController(ureq, getWindowControl(), entry, readOnly);
			listenTo(videoCollectionAccessController);
			vc.put("videoCollectionAccess", videoCollectionAccessController.getInitialComponent());
		}
		super.initAccessOffers(ureq, vc);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(videoCollectionAccessController == source) {
			if(event == Event.DONE_EVENT) {
				doSaveVideoToOrganisations();
			} else if(event == Event.CANCELLED_EVENT) {
				initAccessOffers(ureq, mainVC);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doSaveVideoToOrganisations() {
		entry = repositoryService.loadByKey(entry.getKey());
		boolean videoCollectionEnabled = videoCollectionAccessController.isVideoCollectionEnabled();
		List<Organisation> selectedOrganisations = videoCollectionAccessController.getVideoOrganisations();	
		entry = videoManager.setVideoCollection(entry, videoCollectionEnabled, selectedOrganisations);
	}
}
