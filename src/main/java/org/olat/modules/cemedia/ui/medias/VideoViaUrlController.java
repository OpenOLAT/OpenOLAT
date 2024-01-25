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

import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.ModalInspectorController;
import org.olat.modules.ceditor.ui.component.EditModeAware;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaMetadataController;
import org.olat.modules.cemedia.ui.MediaVersionChangedEvent;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-11-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoViaUrlController extends BasicController {

	private final EditModeAwareVelocityContainer mainVC;
	private MediaVersion mediaVersion;

	@Autowired
	private MediaService mediaService;

	public VideoViaUrlController(UserRequest ureq, WindowControl wControl, PageElement pageElement,
								 MediaVersion mediaVersion, RenderingHints hints) {
		super(ureq, wControl);

		this.mediaVersion = mediaVersion;

		mainVC = new EditModeAwareVelocityContainer("media_video_via_url", getTranslator(), this);
		mainVC.contextPut("editMode", !hints.isEditable());

		setBlockLayoutClass(pageElement);

		if (mediaVersion.getVersionMetadata() != null) {
			String url = mediaVersion.getVersionMetadata().getUrl();
			VideoAudioPlayerController videoAudioPlayerController = new VideoAudioPlayerController(ureq, wControl,
					null, url, false,
					false, false, false, true);
			listenTo(videoAudioPlayerController);
			mainVC.put("videoViaUrl", videoAudioPlayerController.getInitialComponent());
		}

		if (hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, mediaVersion.getMedia());
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}

	private void setBlockLayoutClass(PageElement pageElement) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(pageElement, false));
	}

	class EditModeAwareVelocityContainer extends VelocityContainer implements EditModeAware {

		public EditModeAwareVelocityContainer(String page, Translator translator, ComponentEventListener listeningController) {
			super("vc_" + page, velocity_root + "/" + page + ".html", translator, listeningController);
		}

		@Override
		public void editModeSet(boolean editMode) {
			if (!Boolean.valueOf(editMode).equals(contextGet("editMode"))) {
				contextPut("editMode", editMode);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);

		if (source instanceof VideoAudioPlayerController) {
			try {
				if ("success".equals(event.getCommand())) {
					int width = Integer.parseInt(ureq.getParameter("width"));
					int height = Integer.parseInt(ureq.getParameter("height"));
					mediaService.updateMediaVersionMetadata(mediaVersion.getKey(), width, height);
					fireEvent(ureq, new MediaVersionChangedEvent(mediaVersion.getKey()));
				} else if ("loadedmetadata".equals(event.getCommand()) || "play".equals(event.getCommand())) {
					double duration = Double.parseDouble(ureq.getParameter("duration"));
					long durationInMs = (long) (duration * 1000);
					if (durationInMs > 0) {
						mediaService.updateMediaVersionMetadata(mediaVersion.getKey(), Formatter.formatTimecode(durationInMs));
						fireEvent(ureq, new MediaVersionChangedEvent(mediaVersion.getKey()));
					}
				}
			} catch (Exception e) {
				logError("Error parsing metadata", e);
			}
		} else if (source instanceof ModalInspectorController && event instanceof ChangePartEvent changePartEvent) {
			setBlockLayoutClass(changePartEvent.getElement());
		}
	}
}
