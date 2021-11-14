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
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRunController extends BasicController {

	private Link openLink, closeLink, startLink, startGuestLink, recordingLink, membersLink, editLink;
//	<VCRP-OM>
	private Link checkLink, checkEmailLink;
//	</VCRP-OM>
	private VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private OpenMeetingsGuestController guestController;
	private OpenMeetingsRecordingsController recordingsController;
	private OpenMeetingsAdminRoomMembersController membersController;
	private OpenMeetingsRoomEditController editController;
	
	private final boolean admin;
	private final boolean moderator;
	private final boolean readOnly;
	private OpenMeetingsRoom room;
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	
	@Autowired
	private OpenMeetingsModule openMeetingsModule;
	@Autowired
	private OpenMeetingsManager openMeetingsManager;

	public OpenMeetingsRunController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, boolean admin, boolean moderator, boolean readOnly) {
		super(ureq, wControl);
		
		this.admin = admin;
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.group = group;
		this.ores = ores;
		this.subIdentifier = subIdentifier;

		try {
			room = openMeetingsManager.getRoom(group, ores, subIdentifier);
		} catch (OpenMeetingsException e) {
			logError("", e);
		}
		
		mainVC = createVelocityContainer("room");
		init(ureq);
		
		putInitialPanel(mainVC);
	}

	protected void init(UserRequest ureq) {
		if(!openMeetingsModule.isEnabled()) {
			mainVC.contextPut("disabled", Boolean.TRUE);
		} else if(room == null) {
			mainVC.contextPut("noroom", Boolean.TRUE);
		} else if (ureq.getUserSession().getRoles().isGuestOnly() || ureq.getUserSession().getRoles().isInvitee()){
			startGuestLink = LinkFactory.createButton("start.room.guest", mainVC, this);
			startGuestLink.setVisible(!readOnly);
			mainVC.put("start.room.guest", startGuestLink);
		} else {
			if (moderator) {
				openLink = LinkFactory.createButton("open.room", mainVC, this);
				openLink.setVisible(!readOnly);
				mainVC.put("open.room", openLink);
				
				closeLink = LinkFactory.createButton("close.room", mainVC, this);
				closeLink.setVisible(!readOnly);
				mainVC.put("close.room", closeLink);
				
				membersLink = LinkFactory.createButton("room.members", mainVC, this);
				mainVC.put("room.members", membersLink);
			}
			if(admin && !readOnly) {
				editLink = LinkFactory.createButton("edit.room", mainVC, this);
				mainVC.put("edit", editLink);
			}
			
			recordingLink = LinkFactory.createButton("recordings", mainVC, this);
			mainVC.put("open.recordings", recordingLink);
			
			startLink = LinkFactory.createButton("start.room", mainVC, this);
			startLink.setVisible(!readOnly);
			startLink.setTarget("openmeetings");
			mainVC.put("start.room", startLink);
		}

//		<VCRP-OM>
		checkLink = LinkFactory.createButton("check.test", mainVC, this);
		checkLink.setTarget("openmeetings");
		mainVC.put("check.test", checkLink);
		
		if(StringHelper.containsNonWhitespace(openMeetingsModule.getSupportEmail())) {
			checkEmailLink = LinkFactory.createButton("check.email", mainVC, this);
			checkEmailLink.setTarget("openmeetings");
			mainVC.put("check.email", checkEmailLink);
		}
//		</VCRP-OM>
		
		updateState();
	}
	
	private void updateState() {
		if(!openMeetingsModule.isEnabled()) {
			mainVC.contextPut("disabled", Boolean.TRUE);
		} else if(room == null) {
			mainVC.contextPut("noroom", Boolean.TRUE);
		} else {
			boolean closed = room.isClosed();
			if(openLink != null) {
				openLink.setVisible(closed && !readOnly);
			}
			if(closeLink != null) {
				closeLink.setVisible(!closed && !readOnly);
			}
			if(startLink != null) {
				startLink.setEnabled(!closed && !readOnly);
			}
			if(startGuestLink != null) {
				startGuestLink.setEnabled(!closed && !readOnly);
			}
			
			mainVC.contextPut("roomName", room.getName());
			if (StringHelper.containsNonWhitespace(room.getComment())) {
				mainVC.contextPut("roomComment", room.getComment());
			} else {
				mainVC.contextRemove("roomComment");
			}
			mainVC.contextPut("roomClosed", new Boolean(closed));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == startLink) {
			doStart(ureq);
		} else if (source == startGuestLink) {
			doStartAsGuest(ureq);
		} else if(source == openLink) {
			doOpen();
		} else if(source == closeLink) {
			doClose();
		} else if(source == recordingLink) {
			doOpenRecordings(ureq);
		} else if(source == membersLink) {
			doOpenMembers(ureq);
		} else if(source == editLink) {
			doEdit(ureq);
//		<VCRP-OM>
		} else if(source == checkLink) {
			doCheck(ureq);
		} else if(source == checkEmailLink) {
			doEmail(ureq);
//		</VCRP-OM>
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == guestController) {
			if(event == Event.DONE_EVENT) {
				String firstName = guestController.getFirstName();
				String lastName = guestController.getLastName();
				redirectToGuestRoom(firstName, lastName);
			}
			cmc.deactivate();
			cleanupPopups();
		} else if (source == recordingsController) {
			cmc.deactivate();
			cleanupPopups();
		} else if(source == membersController) {
			cmc.deactivate();
			cleanupPopups();
		} else if(source == editController) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				room = editController.getRoom();
				updateState();
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
		removeAsListenerAndDispose(recordingsController);
		removeAsListenerAndDispose(membersController);
		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(cmc);
		guestController = null;
		editController = null;
		recordingsController = null;
		membersController = null;
		cmc = null;
	}
	
	private void doOpen() {
		try {
			room = openMeetingsManager.openRoom(room);
		} catch (OpenMeetingsException e) {
			showError(e.i18nKey());
		}
		updateState();
	}
	
	private void doClose() {
		try {
			room = openMeetingsManager.closeRoom(room);
		} catch (OpenMeetingsException e) {
			showError(e.i18nKey());
		}
		updateState();
	}
	
	private void doOpenRecordings(UserRequest ureq) {
		cleanupPopups();
		recordingsController = new OpenMeetingsRecordingsController(ureq, getWindowControl(), room.getRoomId(), admin || moderator);
		listenTo(recordingsController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), recordingsController.getInitialComponent(), true, translate("recordings"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenMembers(UserRequest ureq) {
		cleanupPopups();
		try {
			OpenMeetingsRoom reloadedRoom = openMeetingsManager.getRoom(group, ores, subIdentifier);
			membersController = new OpenMeetingsAdminRoomMembersController(ureq, getWindowControl(), reloadedRoom, readOnly);
			listenTo(membersController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), membersController.getInitialComponent(), true, translate("room.members"));
			listenTo(cmc);
			cmc.activate();
		} catch (OpenMeetingsException e) {
			showError(e.i18nKey());
		}
	}
	
	private void doEdit(UserRequest ureq) {
		cleanupPopups();
		editController = new OpenMeetingsRoomEditController(ureq, getWindowControl(), group, ores, subIdentifier, null);
		listenTo(editController);

		String edit = translate("edit.room");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent(), true, edit);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doStartAsGuest(UserRequest ureq) {
		cleanupPopups();
		guestController = new OpenMeetingsGuestController(ureq, getWindowControl());
		listenTo(guestController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), guestController.getInitialComponent(), true, translate("guest.room"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void redirectToGuestRoom(String firstName, String lastName) {	
		if(room == null && room.getRoomId() <= 0) {
			showError("room.notfound.error");
		} else {
			try {
				String securedHash = openMeetingsManager.setGuestUserToRoom(firstName, lastName, room.getRoomId());
				String url = openMeetingsManager.getURL(getIdentity(), room.getRoomId(), securedHash, getLocale());
				DisplayOrDownloadComponent cmp = new DisplayOrDownloadComponent("openCommand", url);
				mainVC.put("openCmd", cmp);
			} catch (OpenMeetingsException e) {
				showError(e.i18nKey());
			}
		}
	}

	private void doStart(UserRequest ureq) {	
		if(room == null && room.getRoomId() <= 0) {
			showError("room.notfound.error");
		} else {
			try {
				String securedHash = openMeetingsManager.setUserToRoom(getIdentity(), room.getRoomId(), moderator);
				String url = openMeetingsManager.getURL(getIdentity(), room.getRoomId(), securedHash, getLocale());
				RedirectMediaResource redirect = new RedirectMediaResource(url);
				ureq.getDispatchResult().setResultingMediaResource(redirect);
			} catch (OpenMeetingsException e) {
				showError(e.i18nKey());
			}
		}
	}

//	<VCRP-OM>
	private void doCheck(UserRequest ureq) {	
		String url = openMeetingsModule.getOpenMeetingsURI().toString() + "/swf?swf=networktesting.swf10.swf";
		RedirectMediaResource redirect = new RedirectMediaResource(url);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}

	private void doEmail(UserRequest ureq) {	
		String id = "";
		if (ores != null) id = "Kurs "+ores.getResourceableId().toString();
		else if (group != null) id = "Gruppe "+group.getKey();
		String url = "mailto:" + openMeetingsModule.getSupportEmail() + "?subject=Probleme beim Aufruf von OpenMeetings aus " + id + "&body=BITTE HIER DAS PROBLEM BESCHREIBEN";
		RedirectMediaResource redirect = new RedirectMediaResource(url);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}
//	</VCRP-OM>
}
