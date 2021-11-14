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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.RoomType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRoomEditController extends FormBasicController {
	
	private TextElement roomNameEl;
	private SingleSelection roomTypeEl;
//	<VCRP-OM>
	private TextElement roomSizeEl;
//	</VCRP-OM>
	private SingleSelection avModeEl;
	private SingleSelection moderationModeEl;
	private TextElement commentEl;
	
	private final String[] roomTypeKeys;
	private final String[] moderationModeKeys;
	private final String[] avModeKeys;

	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	
	private OpenMeetingsRoom room;
	private OpenMeetingsRoom defaultSettings;
	
	@Autowired
	private OpenMeetingsManager openMeetingsManager;

	public OpenMeetingsRoomEditController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, OpenMeetingsRoom defaultSettings) {
		super(ureq, wControl);
		
		this.group = group;
		this.ores = ores;
		this.subIdentifier = subIdentifier;
		this.defaultSettings = defaultSettings;
		this.setFormContextHelp("Communication and Collaboration#_openmeeting_raum");

		roomTypeKeys = new String[]{
				RoomType.conference.typeStr(), RoomType.restricted.typeStr(), RoomType.interview.typeStr()
		};
		moderationModeKeys = new String[]{"yes", "no"};
		avModeKeys = new String[]{"audio", "video"};
		
		try {
			room = openMeetingsManager.getRoom(group, ores, subIdentifier);
		} catch (OpenMeetingsException e) {
			showError(e.i18nKey());
		}
		initForm(ureq);
	}
	
	public OpenMeetingsRoom getRoom() {
		return room;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Communication and Collaboration#CommunicationandCollaboration-_openmeeting_raum");
		
		String name = room == null ? (defaultSettings == null ? null : defaultSettings.getName()) : room.getName();
		roomNameEl = uifactory.addTextElement("roomname", "room.name", 255, name == null ? "" : name, formLayout);
		
		String[] roomTypeValues = new String[]{
				translate(RoomType.conference.i18nKey()), translate(RoomType.restricted.i18nKey()), translate(RoomType.interview.i18nKey())
		};
		roomTypeEl = uifactory.addDropdownSingleselect("roomtype", "room.type", formLayout, roomTypeKeys, roomTypeValues, null);
		if(room != null) {
			String type = Long.toString(room.getType());
			roomTypeEl.select(type, true);
		} else if(defaultSettings != null && defaultSettings.getType() > 0) {
			String type = Long.toString(defaultSettings.getType());
			roomTypeEl.select(type, true);
		}
		
//		<VCRP-OM>
		roomSizeEl = uifactory.addTextElement("roomsize", "room.size", 5, "", formLayout);
		if(room != null) {
			roomSizeEl.setValue(room.getSize()<1000 ? Long.toString(room.getSize()) : "");
		} else if(defaultSettings != null && defaultSettings.getSize() > 0) {
			roomSizeEl.setValue(defaultSettings.getSize()<1000 ? Long.toString(defaultSettings.getSize()) : "");
		}
//		</VCRP-OM>
		 
		String[]  moderationModeValues = new String[]{ translate("room.moderation.yes"), translate("room.moderation.no") };
		moderationModeEl = uifactory.addDropdownSingleselect("moderationmode", "room.moderation.mode", formLayout, moderationModeKeys, moderationModeValues, null);
		if(room != null) {
			String key = room.isModerated() ? moderationModeKeys[0] : moderationModeKeys[1];
			moderationModeEl.select(key, true);
		} else if(defaultSettings != null) {
			String key = defaultSettings.isModerated() ? moderationModeKeys[0] : moderationModeKeys[1];
			moderationModeEl.select(key, true);
		}

		String[] avModeValues = new String[]{ translate("room.av.audio"), translate("room.av.video") };
		avModeEl = uifactory.addDropdownSingleselect("avmode", "room.av.mode", formLayout, avModeKeys, avModeValues, null);
		if(room != null) {
			String key = room.isAudioOnly() ? avModeKeys[0] : avModeKeys[1];
			avModeEl.select(key, true);
		} else if(defaultSettings != null) {
			String key = defaultSettings.isAudioOnly() ? avModeKeys[0] : avModeKeys[1];
			avModeEl.select(key, true);
		}

		String comment = room == null ? (defaultSettings == null ? null : defaultSettings.getComment()) : room.getComment();
		commentEl = uifactory.addRichTextElementForStringData("roomcomment", "room.comment", comment == null ? "" : comment,
				10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonContainer);
		uifactory.addFormSubmitButton("save", "save", buttonContainer);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(room == null) {
			room = new OpenMeetingsRoom();
		}
		room.setComment(commentEl.getValue());
		room.setModerated(moderationModeEl.isOneSelected() && moderationModeEl.isSelected(0));
		room.setName(roomNameEl.getValue());
//		<VCRP-OM>
		try {
			room.setSize(Long.parseLong(roomSizeEl.getValue()));
		} catch (Exception e) {
			room.setSize(1000);
			roomSizeEl.setValue("");
 		}
//		</VCRP-OM>
		room.setAudioOnly(avModeEl.isOneSelected() && avModeEl.isSelected(0));
		if(roomTypeEl.isOneSelected()) {
			String type = roomTypeEl.getSelectedKey();
			long roomType = Long.parseLong(type);
			room.setType(roomType);
		}
		
		if(room.getRoomId() > 0) {
			room = openMeetingsManager.updateRoom(group, ores, subIdentifier, room);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else {
			room = openMeetingsManager.addRoom(group, ores, subIdentifier, room);
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}