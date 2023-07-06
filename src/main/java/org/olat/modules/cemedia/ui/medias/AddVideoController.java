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
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.handler.VideoHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;

/**
 * 
 * Initial date: 28 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddVideoController extends BasicController implements PageElementAddController {
	
	private final Link addVideoButton;
	private final Link recordVideoButton;

	private Media mediaReference;
	private AddElementInfos userObject;
	private final String businessPath;
	
	private CloseableModalController cmc;
	private AVVideoMediaController recordVideoCtrl;
	private final MediaCenterController mediaCenterCtrl;
	private CollectVideoMediaController videoUploadCtrl;

	public AddVideoController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale()));
		this.businessPath = wControl.getBusinessControl().getAsString();

		VelocityContainer mainVC = createVelocityContainer("add_video");
		
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, mediaHandler, true);
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());
		
		addVideoButton = LinkFactory.createButton("add.video", mainVC, this);
		addVideoButton.setIconLeftCSS("o_icon o_icon_add");
		
		recordVideoButton = LinkFactory.createButton("record.video", mainVC, this);
		recordVideoButton.setIconLeftCSS("o_icon o_icon_add");
		
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
					mediaReference = se.getMedia();
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if(event instanceof UploadMediaEvent upme) {
				doUpload(ureq, upme.getUploadMedia());
			}
		} else if(videoUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				mediaReference = videoUploadCtrl.getMediaReference();
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(recordVideoCtrl == source) {
			if(event == Event.DONE_EVENT) {
				mediaReference = recordVideoCtrl.getMediaReference();
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				if(mediaReference != null) {
					fireEvent(ureq, event);
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(videoUploadCtrl);
		removeAsListenerAndDispose(recordVideoCtrl);
		removeAsListenerAndDispose(cmc);
		videoUploadCtrl = null;
		recordVideoCtrl = null;
		cmc = null;
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
