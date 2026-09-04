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
package org.olat.modules.teams.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.modules.teams.TeamsLoggingAction;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;
import org.olat.modules.teams.TeamsService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PublishRecordingController extends FormBasicController {
	
	private MultipleSelectionElement publishEl;
	
	private final TeamsRecordingRow row;
	
	@Autowired
	private TeamsService teamsService;
	
	public PublishRecordingController(UserRequest ureq, WindowControl wControl, TeamsRecordingRow row) {
		super(ureq, wControl, "publish_recording");
		this.row = row;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues publishToKeyValues = new SelectionValues();
		publishToKeyValues.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.coach.name(), translate("teams.recordings.publish.to.coach")));
		publishToKeyValues.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.participant.name(), translate("teams.recordings.publish.to.participant")));
		publishToKeyValues.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.all.name(), translate("teams.recordings.publish.to.all")));
		publishToKeyValues.add(SelectionValues.entry(TeamsRecordingsPublishedRoles.guest.name(), translate("teams.recordings.publish.to.guest")));
		publishEl = uifactory.addCheckboxesVertical("publish.to.list", "teams.recordings.publish.to.list", formLayout,
				publishToKeyValues.keys(), publishToKeyValues.values(), 1);
		TeamsRecordingsPublishedRoles[] currentRoles = row.getPublishToEnum();
		for(TeamsRecordingsPublishedRoles currentRole:currentRoles) {
			if(publishToKeyValues.containsKey(currentRole.name())) {
				publishEl.select(currentRole.name(), true);
			}
		}
		uifactory.addFormSubmitButton("publish.to", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		savePublish();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void savePublish() {
		Collection<String>  selectedKeys = publishEl.getSelectedKeys();
		List<TeamsRecordingsPublishedRoles> roles = new ArrayList<>();
		for(String selectedKey:selectedKeys) {
			roles.add(TeamsRecordingsPublishedRoles.valueOf(selectedKey));
		}
		
		if(roles.isEmpty()) {
			roles.add(TeamsRecordingsPublishedRoles.none);
		}
		TeamsRecordingsPublishedRoles[] roleArr = roles.toArray(new TeamsRecordingsPublishedRoles[roles.size()]);
		TeamsRecording recording = teamsService.getRecording(row.getRecording().getKey());
		if(recording != null) {
			recording.setPublishToEnum(roleArr);
			teamsService.updateRecording(recording);
			ThreadLocalUserActivityLogger.log(TeamsLoggingAction.TEAMS_RECORDING_PUBLISH, getClass(), LoggingResourceable.wrap(recording));
		}
	}
}