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
package org.olat.group.ui.homepage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.memberlist.ui.MembersDisplayRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.run.GroupMembersRunController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date:  Aug 19, 2009 <br>
 * @author twuersch, www.frentix.com
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @author fkiefer
 */
public class GroupMembersDisplayController extends BasicController {


	private MembersDisplayRunController membersDisplayRunController;
	
	@Autowired
	private BusinessGroupService businessGroupService;	
	

	public GroupMembersDisplayController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GroupMembersRunController.class, getLocale()));
		// display owners and participants
		
		List<Identity> coaches, participants, waiting;
		boolean showCoaches = businessGroup.isOwnersVisiblePublic();
		if (showCoaches) {
			coaches = businessGroupService.getMembers(businessGroup, GroupRoles.coach.name());			
		} else {
			coaches = Collections.emptyList();
		}
		boolean showParticipants = businessGroup.isParticipantsVisiblePublic();
		if (showParticipants) {
			participants = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());		
		} else {
			participants = Collections.emptyList();
		}
		boolean showWaiting = businessGroup.isWaitingListVisiblePublic();
		if (showWaiting) {
			waiting = businessGroupService.getMembers(businessGroup, GroupRoles.waiting.name());
		} else {
			waiting = Collections.emptyList();
		}	
		
		membersDisplayRunController = new MembersDisplayRunController(ureq, wControl, getTranslator(), null, businessGroup,	new ArrayList<>(), 
				coaches, participants, waiting, null, false, false, true, false, showCoaches, showParticipants, showWaiting, false);
		listenTo(membersDisplayRunController);
		
		putInitialPanel(membersDisplayRunController.getInitialComponent());	
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// events handled in child controller
	}
}
