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
package org.olat.modules.cemedia.ui.medias;

import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaMetadataController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-10-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AudioMediaController extends BasicController {

	@Autowired
	VFSTranscodingService vfsTranscodingService;

	@Autowired
	VFSRepositoryService vfsRepositoryService;

	public AudioMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("media_audio");

		if (version.getMetadata() instanceof VFSMetadataImpl metadata &&
				vfsRepositoryService.getItemFor(metadata.getParent()) instanceof VFSContainer container &&
				vfsRepositoryService.getItemFor(metadata) instanceof VFSLeaf audioLeaf) {
			audioLeaf.setParentContainer(container);
			VideoAudioPlayerController videoAudioPlayerController = new VideoAudioPlayerController(ureq, wControl,
					audioLeaf, null, false,
					false, true, true, false);
			listenTo(videoAudioPlayerController);
			mainVC.put("audio", videoAudioPlayerController.getInitialComponent());
			mainVC.contextPut("mediaElementId", videoAudioPlayerController.getMediaElementId());
		}

		if (hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, version.getMedia());
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {

	}
}
