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
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.AddMediaEvent;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;

/**
 * 
 * Initial date: 9 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddDrawioController extends AbstractAddController implements PageElementAddController {
	
	private final Link createButton;

	private AddElementInfos userObject;
	
	private final MediaCenterController mediaCenterCtrl;
	private CreateDrawioMediaController drawioCreateCtrl;

	public AddDrawioController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler, AddSettings settings) {
		super(ureq, wControl, mediaHandler, settings);
		
		VelocityContainer mainVC = createVelocityContainer("add_drawio");
		
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, mediaHandler,
				false, settings.baseRepositoryEntry());
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());
		
		createButton = LinkFactory.createButton("create.drawio", mainVC, this);
		createButton.setElementCssClass("o_sel_create_drawio");
		createButton.setIconLeftCSS("o_icon o_filetype_drawio");
		
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
		if(createButton == source) {
			doAddDrawio(ureq);
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
						fireEvent(ureq, new AddMediaEvent(true));
					}
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}
		} else if(drawioCreateCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(proposeSharing(drawioCreateCtrl.getMediaReference())) {
					Media media = drawioCreateCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = drawioCreateCtrl.getMediaReference();
					fireEvent(ureq, new AddMediaEvent(true));
					cmc.deactivate();
					cleanUp();
				}
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(drawioCreateCtrl);
		drawioCreateCtrl = null;
	}
	
	private void doAddDrawio(UserRequest ureq) {
		if(guardModalController(drawioCreateCtrl)) return;
		
		drawioCreateCtrl = new CreateDrawioMediaController(ureq, getWindowControl());
		listenTo(drawioCreateCtrl);
		
		String title = translate("create.drawio");
		cmc = new CloseableModalController(getWindowControl(), null, drawioCreateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
