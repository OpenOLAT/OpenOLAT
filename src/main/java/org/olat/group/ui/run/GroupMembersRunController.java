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
package org.olat.group.ui.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.memberlist.ui.MembersDisplayRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 22.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class GroupMembersRunController extends BasicController {
	
	private MembersDisplayRunController membersDisplayRunController;
	
	@Autowired
	private BusinessGroupService businessGroupService;	
	
	public GroupMembersRunController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup, boolean canEmail, boolean readOnly) {
		super(ureq, wControl);
		
		List<Identity> coaches, participants, waiting;
		boolean showCoaches = businessGroup.isOwnersVisibleIntern();
		if (showCoaches) {
			coaches = businessGroupService.getMembers(businessGroup, GroupRoles.coach.name());			
		} else {
			coaches = Collections.emptyList();
		}
		boolean showParticipants = businessGroup.isParticipantsVisibleIntern();
		if (showParticipants) {
			participants = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());		
		} else {
			participants = Collections.emptyList();
		}
		boolean showWaiting = businessGroup.isWaitingListVisibleIntern();
		if (showWaiting) {
			waiting = businessGroupService.getMembers(businessGroup, GroupRoles.waiting.name());
		} else {
			waiting = Collections.emptyList();
		}
		boolean canDownload = businessGroup.isDownloadMembersLists();
		membersDisplayRunController = new MembersDisplayRunController(ureq, wControl, getTranslator(), null, businessGroup, new ArrayList<>(), 
				coaches, participants, waiting, null, canEmail, canDownload, false, false, showCoaches, showParticipants, showWaiting, !readOnly);
		listenTo(membersDisplayRunController);
		
		putInitialPanel(membersDisplayRunController.getInitialComponent());		
	}

	

	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	

}
