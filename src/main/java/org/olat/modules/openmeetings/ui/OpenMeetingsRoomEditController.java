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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.RoomType;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsRoomEditController extends FormBasicController {
	
	private TextElement roomNameEl;
	private SingleSelection roomTypeEl;
	private SingleSelection roomSizeEl;
	private SingleSelection moderationModeEl;
	private SingleSelection recordingEl;
	private TextElement commentEl;
	
	private final String[] roomTypeKeys;
	private final String[] roomTypeValues;
	private final String[] roomSizes;
	private final String[] moderationModeKeys;
	private final String[] moderationModeValues;
	private final String[] recordingKeys = {"xx"};
	private final String[] recordingValues = {"???"};
	
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	
	private OpenMeetingsRoom room;
	private final OpenMeetingsManager openMeetingsManager;
	

	public OpenMeetingsRoomEditController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, String resourceName, boolean admin) {
		super(ureq, wControl);
		
		this.group = group;
		this.ores = ores;
		this.subIdentifier = subIdentifier;
		
		roomTypeKeys = new String[]{
				RoomType.conference.typeStr(), RoomType.audience.typeStr(), RoomType.restricted.typeStr(), RoomType.interview.typeStr()
		};
		roomTypeValues = new String[]{
				translate(RoomType.conference.i18nKey()), translate(RoomType.audience.i18nKey()), translate(RoomType.restricted.i18nKey()), translate(RoomType.interview.i18nKey())
		};
		
		roomSizes = new String[]{"16", "100"};
		
		moderationModeKeys = new String[]{"yes", "no"};
		moderationModeValues = new String[]{ translate("room.moderation.yes"), translate("room.moderation.no") };
		
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);
		room = openMeetingsManager.getRoom(group, ores, subIdentifier);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		String name = room == null ? "" : room.getName();
		roomNameEl = uifactory.addTextElement("roomname", "room.name", 255, name, formLayout);
		
		roomTypeEl = uifactory.addDropdownSingleselect("roomtype", "room.type", formLayout, roomTypeKeys, roomTypeValues, null);
		if(room != null) {
			String type = Long.toString(room.getType());
			roomTypeEl.select(type, true);
		}
		
		roomSizeEl = uifactory.addDropdownSingleselect("roomsize", "room.size", formLayout, roomSizes, roomSizes, null);
		if(room != null) {
			String size = Long.toString(room.getSize());
			roomSizeEl.select(size, true);
		}
		 
		moderationModeEl = uifactory.addDropdownSingleselect("moderationmode", "room.moderation.mode", formLayout, moderationModeKeys, moderationModeValues, null);
		if(room != null) {
			String key = room.isModerated() ? moderationModeKeys[0] : moderationModeKeys[1];
			moderationModeEl.select(key, true);
		};

		recordingEl = uifactory.addDropdownSingleselect("recording", "room.recording", formLayout, recordingKeys, recordingValues, null);
		if(room != null) {
			String key = room.isRecordingAllowed() ? recordingKeys[0] : recordingKeys[1];
			recordingEl.select(key, true);
		};

		String comment = room == null ? "" : room.getComment();
		commentEl = uifactory.addRichTextElementForStringData("roomcomment", "room.comment", comment,
				10, -1, false, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonContainer);
		uifactory.addFormSubmitButton("save", "save", buttonContainer);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(room == null) {
			room = new OpenMeetingsRoom();
		}
		room.setComment(commentEl.getValue());
		room.setModerated(moderationModeEl.isOneSelected() && moderationModeEl.isSelected(0));
		room.setName(roomNameEl.getValue());
		room.setRecordingAllowed(recordingEl.isOneSelected() && recordingEl.isSelected(0));
		if(roomSizeEl.isOneSelected()) {
			String key = roomSizeEl.getSelectedKey();
			if(StringHelper.isLong(key)) {
				room.setSize(Long.parseLong(key));
			} else {
				room.setSize(16l);
			}
		}
		if(roomTypeEl.isOneSelected()) {
			String type = roomTypeEl.getSelectedKey();
			long roomType = Long.parseLong(type);
			room.setType(roomType);
		}
		
		if(room.getRoomId() > 0) {
			openMeetingsManager.updateRoom(group, ores, subIdentifier, room);
		} else {
			openMeetingsManager.addRoom(group, ores, subIdentifier, room);
		}
	}
}
