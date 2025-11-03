/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationCoordinator.RequestMode;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AddProgramMemberController extends FormBasicController {
	
	private UserSearchFlexiController searchController;
	
	private final List<Long> excludeIdentityKeys;
	private final CertificationProgram certificationProgram;
	
	@Autowired
	private CertificationCoordinator certificationCoordinator;
	
	public AddProgramMemberController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram, List<Long> excludeIdentityKeys) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.certificationProgram = certificationProgram;
		this.excludeIdentityKeys = excludeIdentityKeys;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchController = new UserSearchFlexiController(ureq, getWindowControl(), mainForm, null, null, excludeIdentityKeys, true, true, true);
		listenTo(searchController);
		formLayout.add("search", searchController.getInitialFormItem());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof SingleIdentityChosenEvent sice) {
			doAddMember(ureq, sice.getChosenIdentity());
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(event instanceof MultiIdentityChosenEvent mice) {
			doAddMembers(ureq, mice.getChosenIdentities());
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doAddMembers(UserRequest ureq, List<Identity> members) {
		for(Identity member: members) {
			doAddMember(ureq, member);
		}
	}
	
	private void doAddMember(UserRequest ureq, Identity member) {
		certificationCoordinator.processCertificationRequest(member, certificationProgram, RequestMode.COACH,
				ureq.getRequestTimestamp(), getIdentity());
	}
}
