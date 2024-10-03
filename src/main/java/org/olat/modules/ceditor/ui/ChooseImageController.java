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
package org.olat.modules.ceditor.ui;

import java.util.Collection;
import java.util.List;

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
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaMultiSelectionEvent;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;
import org.olat.modules.cemedia.ui.medias.CollectImageMediaController;
import org.olat.modules.cemedia.ui.medias.RepositoryEntryShareController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-03-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChooseImageController extends BasicController {

	private final Link addImageButton;
	private final MediaCenterController mediaCenterController;
	private final RepositoryEntry entry;
	private CollectImageMediaController collectImageMediaController;
	private RepositoryEntryShareController repositoryEntryShareController;
	private CloseableModalController cmc;
	private Media mediaReference;
	private Collection<Long> mediaKeys;
	private Object userData;

	@Autowired
	private MediaService mediaService;

	public ChooseImageController(UserRequest ureq, WindowControl wControl, boolean multiSelect, 
								 RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;

		VelocityContainer mainVC = createVelocityContainer("choose_image");

		addImageButton = LinkFactory.createButton("add.image.button", mainVC, this);
		addImageButton.setElementCssClass("o_sel_upload_image");
		addImageButton.setIconLeftCSS("o_icon o_icon_add");

		MediaCenterConfig mediaCenterConfig = new MediaCenterConfig(true, false, false,
				true, true, multiSelect, false, true, "image", null,
				MediaCenterController.ALL_TAB_ID, SearchMediaParameters.Access.DIRECT, null);
		mediaCenterController = new MediaCenterController(ureq, wControl, null, mediaCenterConfig);
		listenTo(mediaCenterController);
		mainVC.put("mediaCenter", mediaCenterController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	public Object getUserData() {
		return userData;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (addImageButton == source) {
			doAddImage(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (mediaCenterController == source) {
			if (event instanceof MediaSelectionEvent mediaSelectionEvent) {
				if (mediaSelectionEvent.getMedia() != null) {
					if (proposeSharing(mediaSelectionEvent.getMedia())) {
						confirmSharing(ureq, mediaSelectionEvent.getMedia());
					} else {
						mediaReference = mediaSelectionEvent.getMedia();
						fireEvent(ureq, Event.DONE_EVENT);
					}
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event instanceof MediaMultiSelectionEvent mediaMultiSelectionEvent) {
				mediaKeys = mediaMultiSelectionEvent.getMediaKeys();
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event instanceof UploadMediaEvent uploadMediaEvent) {
				doUpload(ureq, uploadMediaEvent.getUploadMedia());
			}
		} else if (collectImageMediaController == source) {
			if (event == Event.DONE_EVENT) {
				if (proposeSharing(collectImageMediaController.getMediaReference())) {
					Media media = collectImageMediaController.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = collectImageMediaController.getMediaReference();
					fireEvent(ureq, Event.DONE_EVENT);
					cmc.deactivate();
					cleanUp();
				}
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (repositoryEntryShareController == source) {
			mediaReference = mediaService.getMediaByKey(repositoryEntryShareController.getMedia().getKey());
			fireEvent(ureq, Event.DONE_EVENT);
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private boolean proposeSharing(Media media) {
		if (entry == null) {
			return false;
		}

		if (mediaService.isMediaEditable(getIdentity(), media)) {
			List<MediaShare> shares = mediaService.getMediaShares(media, entry);
			return shares.isEmpty();
		}

		return false;
	}

	private void confirmSharing(UserRequest ureq, Media media) {
		repositoryEntryShareController = new RepositoryEntryShareController(ureq, getWindowControl(), media, entry);
		listenTo(repositoryEntryShareController);

		cmc = new CloseableModalController(getWindowControl(), null,
				repositoryEntryShareController.getInitialComponent(), true,
				repositoryEntryShareController.getTitle(), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doUpload(UserRequest ureq, UploadMedia uploadMedia) {
		collectImageMediaController = new CollectImageMediaController(ureq, getWindowControl(), uploadMedia);
		listenTo(collectImageMediaController);

		String title = translate("add.image.button");
		cmc = new CloseableModalController(getWindowControl(), null,
				collectImageMediaController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddImage(UserRequest ureq) {
		collectImageMediaController = new CollectImageMediaController(ureq, getWindowControl());
		listenTo(collectImageMediaController);

		String title = translate("add.image.button");
		cmc = new CloseableModalController(getWindowControl(), null,
				collectImageMediaController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(collectImageMediaController);
		collectImageMediaController = null;
		removeAsListenerAndDispose(repositoryEntryShareController);
		repositoryEntryShareController = null;
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	public Media getMediaReference() {
		return mediaReference;
	}

	public Collection<Long> getMediaKeys() {
		return mediaKeys;
	}
}
