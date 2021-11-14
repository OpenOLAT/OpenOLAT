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
package org.olat.modules.bigbluebutton.ui;

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
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublishRecordingController extends FormBasicController {
	
	private MultipleSelectionElement publishEl;
	
	private final BigBlueButtonRecordingRow row;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public PublishRecordingController(UserRequest ureq, WindowControl wControl, BigBlueButtonRecordingRow row) {
		super(ureq, wControl, "publish_recording");
		this.row = row;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues publishToKeyValues = new SelectionValues();
		publishToKeyValues.add(SelectionValues.entry(BigBlueButtonRecordingsPublishedRoles.coach.name(), translate("publish.to.coach")));
		publishToKeyValues.add(SelectionValues.entry(BigBlueButtonRecordingsPublishedRoles.participant.name(), translate("publish.to.participant")));
		publishToKeyValues.add(SelectionValues.entry(BigBlueButtonRecordingsPublishedRoles.all.name(), translate("publish.to.all")));
		publishToKeyValues.add(SelectionValues.entry(BigBlueButtonRecordingsPublishedRoles.guest.name(), translate("publish.to.guest")));
		publishEl = uifactory.addCheckboxesVertical("publish.to.list", "publish.to.list", formLayout,
				publishToKeyValues.keys(), publishToKeyValues.values(), 1);
		BigBlueButtonRecordingsPublishedRoles[] currentRoles = row.getReference().getPublishToEnum();
		for(BigBlueButtonRecordingsPublishedRoles currentRole:currentRoles) {
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
		List<BigBlueButtonRecordingsPublishedRoles> roles = new ArrayList<>();
		for(String selectedKey:selectedKeys) {
			roles.add(BigBlueButtonRecordingsPublishedRoles.valueOf(selectedKey));
		}
		
		if(roles.isEmpty()) {
			roles.add(BigBlueButtonRecordingsPublishedRoles.none);
		}
		BigBlueButtonRecordingsPublishedRoles[] roleArr = roles.toArray(new BigBlueButtonRecordingsPublishedRoles[roles.size()]);
		BigBlueButtonRecordingReference ref = row.getReference();
		ref.setPublishToEnum(roleArr);
		bigBlueButtonManager.updateRecordingReference(ref);
	}
}