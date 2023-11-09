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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler.AddSettings;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.handler.VideoHandler;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddVideoController extends AbstractAddController implements PageElementAddController {
	
	private final Link addVideoButton;
	private Link recordVideoButton;

	private AddElementInfos userObject;
	private final String businessPath;
	
	private AVVideoMediaController recordVideoCtrl;
	private final MediaCenterController mediaCenterCtrl;
	private CollectVideoMediaController videoUploadCtrl;

	@Autowired
	private AVModule avModule;

	public AddVideoController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler, AddSettings settings) {
		super(ureq, wControl, mediaHandler, settings);
		this.businessPath = wControl.getBusinessControl().getAsString();
		
		VelocityContainer mainVC = createVelocityContainer("add_video");
		
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, null,
				MediaCenterConfig.valueOfUploader(mediaHandler, true, settings.baseRepositoryEntry()));
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());
		
		addVideoButton = LinkFactory.createButton("add.video", mainVC, this);
		addVideoButton.setIconLeftCSS("o_icon o_icon_add");
		
		if (avModule.isVideoRecordingEnabled()) {
			recordVideoButton = LinkFactory.createButton("record.video", mainVC, this);
			recordVideoButton.setIconLeftCSS("o_icon o_icon_add");
		}

		putInitialPanel(mainVC);
	}

	@Override
	public PageElement getPageElement() {
		return MediaPart.valueOf(getIdentity(), mediaReference);
	}

	@Override
	public void setUserObject(AddElementInfos uobject) {
		this.userObject = uobject;
	}

	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addVideoButton == source) {
			doAddVideo(ureq);
		} else if(recordVideoButton == source) {
			doRecordVideo(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(mediaCenterCtrl == source) {
			if(event instanceof MediaSelectionEvent se) {
				if(se.getMedia() != null) {
					if(proposeSharing(se.getMedia())) {
						confirmSharing(ureq, se.getMedia());
					} else {
						mediaReference = se.getMedia();
						fireEvent(ureq, Event.DONE_EVENT);
					}
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if(event instanceof UploadMediaEvent upme) {
				doUpload(ureq, upme.getUploadMedia());
			}
		} else if(videoUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(proposeSharing(videoUploadCtrl.getMediaReference())) {
					Media media = videoUploadCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = videoUploadCtrl.getMediaReference();
					fireEvent(ureq, event);
					cmc.deactivate();
					cleanUp();
				}
			} else if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(recordVideoCtrl == source) {
			if(recordVideoCtrl.getMediaReference() == null
					|| event == Event.CANCELLED_EVENT
					|| event == Event.CLOSE_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
				cmc.deactivate();
				cleanUp();
			} else if(event == Event.DONE_EVENT) {
				if(proposeSharing(recordVideoCtrl.getMediaReference())) {
					Media media = recordVideoCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = recordVideoCtrl.getMediaReference();
					fireEvent(ureq, event);
				}
			}	
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(videoUploadCtrl);
		removeAsListenerAndDispose(recordVideoCtrl);
		videoUploadCtrl = null;
		recordVideoCtrl = null;
	}
	
	private void doAddVideo(UserRequest ureq) {
		if(guardModalController(videoUploadCtrl)) return;
		
		videoUploadCtrl = new CollectVideoMediaController(ureq, getWindowControl());
		listenTo(videoUploadCtrl);
		
		String title = translate("add.video");
		cmc = new CloseableModalController(getWindowControl(), null, videoUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUpload(UserRequest ureq, UploadMedia uploadMedia) {
		videoUploadCtrl = new CollectVideoMediaController(ureq, getWindowControl(), uploadMedia, businessPath, true);
		listenTo(videoUploadCtrl);
		
		String title = translate("add.video");
		cmc = new CloseableModalController(getWindowControl(), null, videoUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRecordVideo(UserRequest ureq) {
		if(guardModalController(recordVideoCtrl)) return;
		
		recordVideoCtrl = new AVVideoMediaController(ureq, getWindowControl(), businessPath,
				VideoHandler.MAX_RECORDING_TIME_IN_MS, VideoHandler.VIDEO_QUALITY);
		listenTo(recordVideoCtrl);
		
		String title = translate("record.video");
		cmc = new CloseableModalController(getWindowControl(), null, recordVideoCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
