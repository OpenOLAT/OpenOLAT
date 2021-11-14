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
package org.olat.instantMessaging.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.manager.ChatLogHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IMArchiverController extends BasicController {
	
	private final Link exportLink;
	private final Link deleteLink;
	private DialogBoxController deleteDialogCtr;
	
	private final OLATResourceable chatResource;
	private final VelocityContainer mainVC;
	
	@Autowired
	private ChatLogHelper helper;
	@Autowired
	private InstantMessagingService imService;
	
	public IMArchiverController(UserRequest ureq, WindowControl wControl, OLATResourceable chatResource) {
		super(ureq, wControl);
		this.chatResource = chatResource;
		
		mainVC = createVelocityContainer("archive");
		exportLink = LinkFactory.createButton("im.archive.export", mainVC, this);
		deleteLink = LinkFactory.createButton("delete", mainVC, this);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(exportLink == source) {
			doExport(ureq);
		} else if(deleteLink == source) {
			String title = translate("im.archive.delete.title");
			String text = translate("im.archive.delete.text");
			deleteDialogCtr = activateYesNoDialog(ureq, title, text, deleteDialogCtr);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(deleteDialogCtr == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doDelete();
			}
		}
		super.event(ureq, source, event);
	}

	private void doExport(UserRequest ureq) {
		MediaResource download = helper.logMediaResource(chatResource, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(download);
	}
	
	private void doDelete() {
		imService.deleteMessages(chatResource);
	}
}
