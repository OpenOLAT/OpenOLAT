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
package org.olat.modules.portfolio.ui.media;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.ui.MediaMetadataController;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMediaController extends BasicController {
	
	private final ImageComponent videoCmp;
	
	public VideoMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("media_video");

		File mediaDir = new File(FolderConfig.getCanonicalRoot(), media.getStoragePath());
		File mediaFile = new File(mediaDir, media.getRootFilename());
		videoCmp = new ImageComponent(ureq.getUserSession(), "image");
		videoCmp.setMedia(mediaFile);
		mainVC.put("video", videoCmp);
		mainVC.contextPut("pdf", hints.isToPdf());
		if(hints.isToPdf()) {
			videoCmp.setMaxWithAndHeightToFitWithin(800, 600);
			Size size = videoCmp.getScaledSize();
			if(size != null) {
				mainVC.contextPut("scaledSize", size);
			}
		}
		
		if(hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		videoCmp.dispose();
        super.doDispose();
	}
}
