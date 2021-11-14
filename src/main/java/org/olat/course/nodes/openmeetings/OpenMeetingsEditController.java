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
package org.olat.course.nodes.openmeetings;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.OpenMeetingsCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_VCCONFIG = "pane.tab.vcconfig";
	private static final String[] paneKeys = { PANE_TAB_VCCONFIG };

	public static final String CONFIG_ROOM_NAME = "roomName";
	public static final String CONFIG_ROOM_SIZE = "roomSize";
	public static final String CONFIG_ROOM_MODERATION = "roomModeration";
	public static final String CONFIG_ROOM_COMMENT = "roomComment";
	public static final String CONFIG_ROOM_TYPE = "roomType";
	public static final String CONFIG_ROOM_AUDIO_ONLY = "roomAudioOnly";

	private VelocityContainer editVc;
	private TabbedPane tabPane;
	private OpenMeetingsEditFormController editForm;

	private final OpenMeetingsCourseNode courseNode;

	public OpenMeetingsEditController(UserRequest ureq, WindowControl wControl, OpenMeetingsCourseNode courseNode,
			ICourse course) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		
		editVc = createVelocityContainer("edit");

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(course.getResourceableTypeName(), course.getResourceableId());
		OpenMeetingsRoom defaultSettings = getDefaultValues();
		editForm = new OpenMeetingsEditFormController(ureq, getWindowControl(), ores, courseNode, defaultSettings);
		listenTo(editForm);
		editVc.put("editRooms", editForm.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editForm) { // config form action
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				OpenMeetingsRoom room = editForm.getRoom();
				if(room != null) {
					ModuleConfiguration moduleConfiguration = courseNode.getModuleConfiguration();
					moduleConfiguration.set(CONFIG_ROOM_NAME, room.getName());
					moduleConfiguration.set(CONFIG_ROOM_SIZE, Long.valueOf(room.getSize()));
					moduleConfiguration.set(CONFIG_ROOM_MODERATION, Boolean.valueOf(room.isModerated()));
					moduleConfiguration.set(CONFIG_ROOM_AUDIO_ONLY, Boolean.valueOf(room.isAudioOnly()));
					moduleConfiguration.set(CONFIG_ROOM_COMMENT, room.getComment());
					moduleConfiguration.set(CONFIG_ROOM_TYPE, Long.valueOf(room.getType()));
				}
				editVc.setDirty(true);
			}
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VCCONFIG), editVc);
	}
	
	private OpenMeetingsRoom getDefaultValues() {
		OpenMeetingsRoom room = new OpenMeetingsRoom();
		ModuleConfiguration moduleConfiguration = courseNode.getModuleConfiguration();
		Object name = moduleConfiguration.get(CONFIG_ROOM_NAME);
		if(name instanceof String) {
			room.setName((String)name);
		}

		Object size = moduleConfiguration.get(CONFIG_ROOM_SIZE);
		if(size instanceof Long) {
			room.setSize(((Long)size).longValue());
		}
		Object moderated = moduleConfiguration.get(CONFIG_ROOM_MODERATION);
		if(moderated instanceof Boolean) {
			room.setModerated(((Boolean)moderated).booleanValue());
		}
		else if (moderated == null) {
			room.setModerated(true);
		}
		Object comment = moduleConfiguration.get(CONFIG_ROOM_COMMENT);
		if(comment instanceof String) {
			room.setComment((String)comment);
		}
		
		Object type = moduleConfiguration.get(CONFIG_ROOM_TYPE);
		if(type instanceof Long) {
			room.setType(((Long)type).longValue());
		}
		else if (type == null) {
			room.setType(3);
		}

		Object isAudioOnly = moduleConfiguration.get(CONFIG_ROOM_AUDIO_ONLY);
		if(isAudioOnly instanceof Boolean) {
			room.setAudioOnly(((Boolean)isAudioOnly).booleanValue());
		}
		else if (isAudioOnly == null) {
			room.setAudioOnly(true);
		}
		return room;
	}
}