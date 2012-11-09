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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRecording;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRunController extends BasicController {

	private Link openLink, closeLink, startLink, startGuestLink;
	private VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private OpenMeetingsGuestController guestController;
	
	private final boolean admin;
	private final Long roomId;
	private final OpenMeetingsModule openMeetingsModule;
	private final OpenMeetingsManager openMeetingsManager;

	public OpenMeetingsRunController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, String resourceName, boolean admin) {
		super(ureq, wControl);
		
		this.admin = admin;
		openMeetingsModule = CoreSpringFactory.getImpl(OpenMeetingsModule.class);
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);
		roomId = openMeetingsManager.getRoomId(group, ores, subIdentifier);
		
		try {
			List<OpenMeetingsRecording> recList = openMeetingsManager.getRecordings(roomId);
			for(OpenMeetingsRecording rec:recList) {
				System.out.println(openMeetingsManager.getRecordingURL(rec, null));
			}
			System.out.println(recList);
		} catch (OpenMeetingsException e) {
			e.printStackTrace();
		}
		
		mainVC = createVelocityContainer("room");
		init(ureq);
		
		putInitialPanel(mainVC);
	}

	protected void init(UserRequest ureq) {
		if(!openMeetingsModule.isEnabled()) {
			mainVC.contextPut("disabled", Boolean.TRUE);
		} else if(roomId == null) {
			mainVC.contextPut("norroom", Boolean.TRUE);
		} else if (ureq.getUserSession().getRoles().isGuestOnly() || ureq.getUserSession().getRoles().isInvitee()){
			startGuestLink = LinkFactory.createButton("start.room.guest", mainVC, this);
			mainVC.put("start.room.guest", startGuestLink);
		} else {
			if (admin) {
				openLink = LinkFactory.createButton("open.room", mainVC, this);
				mainVC.put("open.room", openLink);
				
				closeLink = LinkFactory.createButton("close.room", mainVC, this);
				mainVC.put("close.room", closeLink);
			}
			startLink = LinkFactory.createButton("start.room", mainVC, this);
			startLink.setTarget("openmeetings");
			mainVC.put("start.room", startLink);
		}
	}

	@Override
	protected void doDispose() {
		//
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == startLink) {
			doStart(ureq);
		} else if (source == startGuestLink) {
			doStartAsGuest(ureq);
		} else if(source == openLink) {
			doOpen(ureq);
		} else if(source == closeLink) {
			doClose(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == guestController) {
			if(event == Event.DONE_EVENT) {
				String firstName = guestController.getFirstName();
				String lastName = guestController.getLastName();
				redirectToGuestRoom(ureq, firstName, lastName);
			}
			cmc.deactivate();
			cleanupPopups();
		} else if(source == cmc) {
			cleanupPopups();
		}
		super.event(ureq, source, event);
	}

	private void cleanupPopups() {
		removeAsListenerAndDispose(guestController);
		removeAsListenerAndDispose(cmc);
		guestController = null;
		cmc = null;
	}
	
	private void doOpen(UserRequest ureq) {
		try {
			openMeetingsManager.openRoom(roomId);
		} catch (OpenMeetingsException e) {
			showError(e.getType().i18nKey());
		}
	}
	
	private void doClose(UserRequest ureq) {
		try {
			openMeetingsManager.closeRoom(roomId);
		} catch (OpenMeetingsException e) {
			showError(e.getType().i18nKey());
		}
	}
	
	private void doStartAsGuest(UserRequest ureq) {
		cleanupPopups();
		guestController = new OpenMeetingsGuestController(ureq, getWindowControl());
		listenTo(guestController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), guestController.getInitialComponent(), true, translate("guest.room"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void redirectToGuestRoom(UserRequest ureq, String firstName, String lastName) {	
		if(roomId == null && roomId.longValue() <= 0) {
			showError("room.notfound.error");
		} else {
			try {
				String securedHash = openMeetingsManager.setGuestUserToRoom(firstName, lastName, roomId, true);
				String url = openMeetingsManager.getURL(getIdentity(), roomId.longValue(), securedHash, getLocale());
				DisplayOrDownloadComponent cmp = new DisplayOrDownloadComponent("openCommand", url);
				mainVC.put("openCmd", cmp);
			} catch (OpenMeetingsException e) {
				showError(e.getType().i18nKey());
			}
		}
	}

	private void doStart(UserRequest ureq) {	
		if(roomId == null && roomId.longValue() <= 0) {
			showError("room.notfound.error");
		} else {
			try {
				String securedHash = openMeetingsManager.setUserToRoom(getIdentity(), roomId, true);
				String url = openMeetingsManager.getURL(getIdentity(), roomId.longValue(), securedHash, getLocale());
				RedirectMediaResource redirect = new RedirectMediaResource(url);
				ureq.getDispatchResult().setResultingMediaResource(redirect);
			} catch (OpenMeetingsException e) {
				showError(e.getType().i18nKey());
			}
		}
	}
}
