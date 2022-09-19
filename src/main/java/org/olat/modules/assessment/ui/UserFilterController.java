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
package org.olat.modules.assessment.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.assessment.ui.event.UserFilterEvent;

/**
 * 
 * Initial date: 19.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserFilterController extends FormBasicController {
	
	private FormToggle membersToggle;
	private FormToggle nonMembersToggle;
	private FormToggle fakeParticipantsToggle;
	private FormToggle anonymousToggle;
	
	private final boolean canMembers;
	private final boolean canOtherUsers;
	private final boolean canFakeParticipants;
	private final boolean canAnonymous;
	private final boolean initialMembers;
	private final boolean initialOtherUser;
	private final boolean initialFakeParticipants;
	private final boolean initialAnonymous;

	public UserFilterController(UserRequest ureq, WindowControl wControl, boolean canMembers, boolean canOtherUsers,
			boolean canFakeParticipants, boolean canAnonymous, boolean initialMembers, boolean initialOtherUser,
			boolean initialFakeParticipants, boolean initialAnonymous) {
		super(ureq, wControl, "user_filter");
		this.canMembers = canMembers;
		this.canOtherUsers = canOtherUsers;
		this.canFakeParticipants = canFakeParticipants;
		this.canAnonymous = canAnonymous;
		this.initialMembers = initialMembers;
		this.initialOtherUser = initialOtherUser;
		this.initialFakeParticipants = initialFakeParticipants;
		this.initialAnonymous = initialAnonymous;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (canMembers) {
			membersToggle = uifactory.addToggleButton("members", "&nbsp;&nbsp;", formLayout, null, null);
			membersToggle.addActionListener(FormEvent.ONCHANGE);
			if (initialMembers) {
				membersToggle.toggleOn();
			}
		}
		if (canOtherUsers) {
			nonMembersToggle = uifactory.addToggleButton("non.members", "&nbsp;&nbsp;", formLayout, null, null);
			nonMembersToggle.addActionListener(FormEvent.ONCHANGE);
			if (initialOtherUser) {
				nonMembersToggle.toggleOn();
			}
		}
		if (canFakeParticipants) {
			fakeParticipantsToggle = uifactory.addToggleButton("fake.participants", "&nbsp;&nbsp;", formLayout, null, null);
			fakeParticipantsToggle.addActionListener(FormEvent.ONCHANGE);
			if (initialFakeParticipants) {
				fakeParticipantsToggle.toggleOn();
			}
		}
		if (canAnonymous) {
			anonymousToggle = uifactory.addToggleButton("anonymous", "&nbsp;&nbsp;", formLayout, null, null);
			anonymousToggle.addActionListener(FormEvent.ONCHANGE);
			if (initialAnonymous) {
				anonymousToggle.toggleOn();
			}
		}
	}
	
	public void select(boolean members, boolean nonMembers, boolean fakeParticipants, boolean anonymous) {
		if (membersToggle != null) {
			if (members) {
				membersToggle.toggleOn();
			} else {
				membersToggle.toggleOff();
			}
		}
		if (nonMembersToggle != null) {
			if (nonMembers) {
				nonMembersToggle.toggleOn();
			} else {
				nonMembersToggle.toggleOff();
			}
		}
		if (fakeParticipantsToggle != null) {
			if (fakeParticipants) {
				fakeParticipantsToggle.toggleOn();
			} else {
				fakeParticipantsToggle.toggleOff();
			}
		}
		if (anonymousToggle != null) {
			if (anonymous) {
				anonymousToggle.toggleOn();
			} else {
				anonymousToggle.toggleOff();
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(membersToggle == source || nonMembersToggle == source || fakeParticipantsToggle == source || anonymousToggle == source) {
			boolean withMembers = membersToggle != null && membersToggle.isOn();
			boolean withOtherUsers = nonMembersToggle != null && nonMembersToggle.isOn();
			boolean withFakeParticipants = fakeParticipantsToggle != null && fakeParticipantsToggle.isOn();
			boolean withAnonymousUsers = anonymousToggle != null && anonymousToggle.isOn();
			fireEvent(ureq, new UserFilterEvent(withMembers, withOtherUsers, withFakeParticipants, withAnonymousUsers));
		}
		super.formInnerEvent(ureq, source, event);
	}
}