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
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;

/**
 * Initial date: 2023-12-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddVideoViaUrlController extends AbstractAddController implements PageElementAddController {

	private final Link addVideoViaUrlButton;

	private AddElementInfos userObject;

	private final MediaCenterController mediaCenterCtrl;
	private CollectUrlVideoMediaController addVideoViaUrlCtrl;

	public AddVideoViaUrlController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler, AddSettings settings) {
		super(ureq, wControl, mediaHandler, settings);

		VelocityContainer mainVC = createVelocityContainer("add_video_via_url");
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, null,
				MediaCenterConfig.valueOfUploader(mediaHandler, false, settings.baseRepositoryEntry()));
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());

		addVideoViaUrlButton = LinkFactory.createButton("add.video.via.url", mainVC, this);
		addVideoViaUrlButton.setIconLeftCSS("o_icon o_icon_add");

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
		if (addVideoViaUrlButton == source) {
			doAddVideoViaUrl(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (mediaCenterCtrl == source) {
			if (event instanceof MediaSelectionEvent se) {
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
			}
		} else if (addVideoViaUrlCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (proposeSharing(addVideoViaUrlCtrl.getMediaReference())) {
					Media media = addVideoViaUrlCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = addVideoViaUrlCtrl.getMediaReference();
					fireEvent(ureq, event);
					cmc.deactivate();
					cleanUp();
				}
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(addVideoViaUrlCtrl);
		addVideoViaUrlCtrl = null;
	}

	private void doAddVideoViaUrl(UserRequest ureq) {
		if (guardModalController(addVideoViaUrlCtrl)) {
			return;
		}

		addVideoViaUrlCtrl = new CollectUrlVideoMediaController(ureq, getWindowControl(), null);
		listenTo(addVideoViaUrlCtrl);

		String title = translate("add.video.via.url");
		cmc = new CloseableModalController(getWindowControl(), null, addVideoViaUrlCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
