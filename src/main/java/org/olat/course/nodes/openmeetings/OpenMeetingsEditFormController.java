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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.OpenMeetingsCourseNode;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.ui.OpenMeetingsRoomEditController;

/**
 * 
 * Initial date: 08.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsEditFormController extends FormBasicController {
	
	private FormLink editLink;
	private StaticTextElement roomNameEl;
	private CloseableModalController cmc;
	private OpenMeetingsRoomEditController editController;
	
	private String courseTitle;
	private final OLATResourceable course;
	private final OpenMeetingsCourseNode courseNode;
	private final OpenMeetingsManager openMeetingsManager;
	
	private OpenMeetingsRoom room;
	private OpenMeetingsRoom defaultSettings;

	public OpenMeetingsEditFormController(UserRequest ureq, WindowControl wControl, OLATResourceable course,
			OpenMeetingsCourseNode courseNode, String courseTitle, OpenMeetingsRoom defaultSettings)
	    throws OpenMeetingsException{
		super(ureq, wControl);
		
		this.course = course;
		this.courseNode = courseNode;
		this.courseTitle = courseTitle;
		this.defaultSettings = defaultSettings;
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);

		room = openMeetingsManager.getRoom(null, course, courseNode.getIdent());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = room == null ? "" : room.getName();
		roomNameEl = uifactory.addStaticTextElement("room.name", "room.name", name, formLayout);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonContainer);
		String key = room == null ? "create.room" : "edit.room";
		editLink = uifactory.addFormLink(key, buttonContainer, Link.BUTTON);	
	}
	
	public OpenMeetingsRoom getRoom() {
		return room;
	}
	
	private void updateUI() {
		String name = room == null ? "" : room.getName();
		roomNameEl.setValue(name);
		String key = room == null ? "create.room" : "edit.room";
		editLink.setI18nKey(key);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanupPopups();
		} else if(source == editController) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				room = editController.getRoom();
				updateUI();
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanupPopups();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanupPopups() {
		this.removeAsListenerAndDispose(cmc);
		this.removeAsListenerAndDispose(editController);
		cmc = null;
		editController = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == editLink) {
			doEditRoom(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	protected void doEditRoom(UserRequest ureq) {
		try {
			cleanupPopups();
			editController = new OpenMeetingsRoomEditController(ureq, getWindowControl(), null, course, courseNode.getIdent(), courseTitle, defaultSettings,  true);
			listenTo(editController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent(), true, translate("edit.room"));
			listenTo(cmc);
			cmc.activate();
		} catch (Exception e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
		}
	}
}