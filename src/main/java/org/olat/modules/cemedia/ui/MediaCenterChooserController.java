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
package org.olat.modules.cemedia.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;
import org.olat.modules.cemedia.ui.medias.AbstractCollectMediaController;
import org.olat.modules.cemedia.ui.medias.RepositoryEntryShareController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 10 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCenterChooserController extends BasicController implements PageElementAddController  {
	
	private Media mediaReference;
	private AddElementInfos userObject;
	private RepositoryEntry baseRepositoryEntry;
	
	private CloseableModalController cmc;
	private RepositoryEntryShareController shareCtrl;
	private final MediaCenterController mediaListCtrl;
	private AbstractCollectMediaController collectMediaCtrl;

	@Autowired
	private MediaService mediaService;
	
	public MediaCenterChooserController(UserRequest ureq, WindowControl wControl, RepositoryEntry baseRepositoryEntry) {
		super(ureq, wControl);
		this.baseRepositoryEntry = baseRepositoryEntry;

		MediaCenterConfig mediaCenterConfig = new MediaCenterConfig(true, true, true,
				true, true, false, false,
				true, null, null,
				(baseRepositoryEntry == null ? MediaCenterController.SHARED_TAB_WITH_ME_ID : MediaCenterController.SHARED_TAB_WITH_ENTRY),
				SearchMediaParameters.Access.DIRECT, baseRepositoryEntry);
		mediaListCtrl = new MediaCenterController(ureq, getWindowControl(), null, mediaCenterConfig);
		mediaListCtrl.setFormTranslatedTitle(translate("choose.media"));
		listenTo(mediaListCtrl);
		putInitialPanel(mediaListCtrl.getInitialComponent());
	}

	@Override
	public PageElement getPageElement() {
		return MediaPart.valueOf(getIdentity(), mediaReference);
	}

	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	public void setUserObject(AddElementInfos userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof MediaSelectionEvent mse) {
			if (mse.getMedia() != null) {
				if (proposeSharing(mse.getMedia())) {
					confirmSharing(ureq, mse.getMedia());
				} else {
					mediaReference = mse.getMedia();
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		} else if (event instanceof UploadMediaEvent uploadMediaEvent) {
			doUpload(ureq, uploadMediaEvent.getUploadMedia());
		} else if(shareCtrl == source) {
			mediaReference = mediaService.getMediaByKey(shareCtrl.getMedia().getKey());
			fireEvent(ureq, Event.DONE_EVENT);
			cmc.deactivate();
			cleanUp();
		} else if (collectMediaCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (proposeSharing(collectMediaCtrl.getMediaReference())) {
					Media media = collectMediaCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = collectMediaCtrl.getMediaReference();
					fireEvent(ureq, event);
					cmc.deactivate();
					cleanUp();
				}
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(shareCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(collectMediaCtrl);
		shareCtrl = null;
		cmc = null;
		collectMediaCtrl = null;
	}

	protected boolean proposeSharing(Media media) {
		if(baseRepositoryEntry == null) {
			return false;
		}
		
		if(mediaService.isMediaEditable(getIdentity(), media)) {
			List<MediaShare> shares = mediaService.getMediaShares(media, baseRepositoryEntry);
			return shares.isEmpty();
		}
		return false;
	}
	
	protected void confirmSharing(UserRequest ureq, Media media) {
		shareCtrl = new RepositoryEntryShareController(ureq, getWindowControl(), media, baseRepositoryEntry);
		listenTo(shareCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), null, shareCtrl.getInitialComponent(),
				true, shareCtrl.getTitle(), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doUpload(UserRequest ureq, UploadMedia uploadMedia) {
		MediaHandler mediaHandler = getMediaHandler(uploadMedia);
		if (mediaHandler == null) {
			return;
		}

		Controller collectMetadataController = mediaHandler.getCollectMetadataController(ureq, getWindowControl(), uploadMedia);
		if (collectMetadataController instanceof AbstractCollectMediaController abstractCollectMediaController) {
			collectMediaCtrl = abstractCollectMediaController;
		}

		if (collectMediaCtrl == null) {
			return;
		}
		listenTo(collectMediaCtrl);

		String title = translate("add.media");
		cmc = new CloseableModalController(getWindowControl(), null,
				collectMediaCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private MediaHandler getMediaHandler(UploadMedia uploadMedia) {
		if (!StringHelper.containsNonWhitespace(uploadMedia.getMimeType())) {
			return null;
		}

		for (MediaHandler mediaHandler : mediaService.getMediaHandlers()) {
			if (mediaHandler.acceptMimeType(uploadMedia.getMimeType())) {
				return mediaHandler;
			}
		}

		return null;
	}
}
