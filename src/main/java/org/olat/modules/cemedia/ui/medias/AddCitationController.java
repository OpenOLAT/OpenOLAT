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
 * Initial date: 28 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddCitationController extends AbstractAddController implements PageElementAddController {
	
	private final Link addCitationButton;

	private AddElementInfos userObject;
	
	private final MediaCenterController mediaCenterCtrl;
	private CollectCitationMediaController addCitationCtrl;

	public AddCitationController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler, AddSettings settings) {
		super(ureq, wControl, mediaHandler, settings);
		
		VelocityContainer mainVC = createVelocityContainer("add_citation");
		
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, mediaHandler,
				false, settings.baseRepositoryEntry());
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());
		
		addCitationButton = LinkFactory.createButton("add.citation", mainVC, this);
		addCitationButton.setElementCssClass("o_sel_add_citation");
		addCitationButton.setIconLeftCSS("o_icon o_icon_add");
		
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
		if(addCitationButton == source) {
			doAddCitation(ureq);
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
						fireEvent(ureq, new AddMediaEvent(false));
					}
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}
		} else if(addCitationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(proposeSharing(addCitationCtrl.getMediaReference())) {
					Media media = addCitationCtrl.getMediaReference();
					cmc.deactivate();
					cleanUp();
					confirmSharing(ureq, media);
				} else {
					mediaReference = addCitationCtrl.getMediaReference();
					fireEvent(ureq, new AddMediaEvent(false));
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
		removeAsListenerAndDispose(addCitationCtrl);
		addCitationCtrl = null;
	}
	
	private void doAddCitation(UserRequest ureq) {
		if(guardModalController(addCitationCtrl)) return;
		
		addCitationCtrl = new CollectCitationMediaController(ureq, getWindowControl());
		listenTo(addCitationCtrl);
		
		String title = translate("add.citation");
		cmc = new CloseableModalController(getWindowControl(), null, addCitationCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
