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
import org.olat.modules.ceditor.InteractiveAddPageElementHandler.AddSettings;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.handler.AudioHandler;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;

/**
 * Initial date: 2023-10-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddAudioController extends AbstractAddController implements PageElementAddController {

	private final Link addAudioButton;
	private final Link recordAudioButton;

	private AddElementInfos userObject;
	private final String businessPath;

	private AVAudioMediaController audioMediaCtrl;
	private final MediaCenterController mediaCenterCtrl;
	private CollectAudioMediaController audioUploadCtrl;

	public AddAudioController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler, AddSettings settings) {
		super(ureq, wControl, mediaHandler, settings);
		this.businessPath = wControl.getBusinessControl().getAsString();
		
		VelocityContainer mainVC = createVelocityContainer("add_audio");
		
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, null,
				MediaCenterConfig.valueOfUploader(mediaHandler, true, settings.baseRepositoryEntry()));
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());
		
		addAudioButton = LinkFactory.createButton("add.audio", mainVC, this);
		addAudioButton.setIconLeftCSS("o_icon o_icon_add");
		
		recordAudioButton = LinkFactory.createButton("record.audio", mainVC, this);
		recordAudioButton.setIconLeftCSS("o_icon o_icon_add");
		
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
		if (addAudioButton == source) {
			doAddAudio(ureq);
		} else if (recordAudioButton == source) {
			doRecordAudio(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (mediaCenterCtrl == source) {
			if(event instanceof MediaSelectionEvent se) {
				if (se.getMedia() != null) {
					if (proposeSharing(se.getMedia())) {
						confirmSharing(ureq, se.getMedia());
					} else {
						mediaReference = se.getMedia();
						fireEvent(ureq, Event.DONE_EVENT);
					}
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event instanceof UploadMediaEvent upme) {
				doUpload(ureq, upme.getUploadMedia());
			}
		} else if (audioUploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (proposeSharing(audioUploadCtrl.getMediaReference())) {
					Media media = audioUploadCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = audioUploadCtrl.getMediaReference();
					fireEvent(ureq, event);
					cmc.deactivate();
					cleanUp();
				}
			} else if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if (audioMediaCtrl == source) {
			if (audioMediaCtrl.getMediaReference() == null
					|| event == Event.CANCELLED_EVENT
					|| event == Event.CLOSE_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
				cmc.deactivate();
				cleanUp();
			} else if (event == Event.DONE_EVENT) {
				if (proposeSharing(audioMediaCtrl.getMediaReference())) {
					Media media = audioMediaCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = audioMediaCtrl.getMediaReference();
					fireEvent(ureq, event);
				}
			}	
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(audioUploadCtrl);
		removeAsListenerAndDispose(audioMediaCtrl);
		audioUploadCtrl = null;
		audioMediaCtrl = null;
	}
	
	private void doAddAudio(UserRequest ureq) {
		if (guardModalController(audioUploadCtrl)) {
			return;
		}
		
		audioUploadCtrl = new CollectAudioMediaController(ureq, getWindowControl());
		listenTo(audioUploadCtrl);
		
		String title = translate("add.audio");
		cmc = new CloseableModalController(getWindowControl(), null, audioUploadCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUpload(UserRequest ureq, UploadMedia uploadMedia) {
		audioUploadCtrl = new CollectAudioMediaController(ureq, getWindowControl(), uploadMedia, businessPath,
				true);
		listenTo(audioUploadCtrl);
		
		String title = translate("add.audio");
		cmc = new CloseableModalController(getWindowControl(), null,
				audioUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRecordAudio(UserRequest ureq) {
		if (guardModalController(audioMediaCtrl)) {
			return;
		}
		
		audioMediaCtrl = new AVAudioMediaController(ureq, getWindowControl(), businessPath,
				AudioHandler.MAX_RECORDING_TIME_IN_MS);
		listenTo(audioMediaCtrl);
		
		String title = translate("record.audio");
		cmc = new CloseableModalController(getWindowControl(), null, audioMediaCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
