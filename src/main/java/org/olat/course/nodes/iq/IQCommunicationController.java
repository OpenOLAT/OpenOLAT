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
package org.olat.course.nodes.iq;

import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.instantMessaging.ui.ChatViewConfig;
import org.olat.instantMessaging.ui.SupervisorChatController;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.teams.TeamsModule;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQCommunicationController extends BasicController implements Activateable2 {

	private final Link addParticipantLink;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private final SupervisorChatController supervisedChatsCtrl;

	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;

	public IQCommunicationController(UserRequest ureq, WindowControl wControl,
			OLATResource resource, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("communication");
		
		addParticipantLink = LinkFactory.createButton("add.participant", mainVC, this);
		
		ChatViewConfig viewConfig = new ChatViewConfig();
		viewConfig.setRoomName(translate("im.title"));
		viewConfig.setResourceInfos(courseNode.getShortTitle());
		CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
		viewConfig.setResourceIconCssClass(nodeConfig.getIconCSSClass());
		viewConfig.setCanClose(true);
		viewConfig.setCanReactivate(true);
		viewConfig.setCanMeeting((bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isChatExamsEnabled())
				|| (teamsModule.isEnabled() && teamsModule.isChatExamsEnabled()));
		viewConfig.setWidth(620);
		viewConfig.setHeight(480);
		
		supervisedChatsCtrl = new SupervisorChatController(ureq, getWindowControl(), resource, courseNode.getIdent(), viewConfig);
		listenTo(supervisedChatsCtrl);
		mainVC.put("chats", supervisedChatsCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}
	
	public void reloadModels() {
		supervisedChatsCtrl.reloadModel();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(userSearchCtrl == source) {
			if(event instanceof MultiIdentityChosenEvent) {
				doAddParticipants(((MultiIdentityChosenEvent)event).getChosenIdentities());
			} else if(event instanceof SingleIdentityChosenEvent) {
				doAddParticipants(List.of(((SingleIdentityChosenEvent)event).getChosenIdentity()));
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addParticipantLink == source) {
			doAddParticipants(ureq);
		}
	}
	
	private void doAddParticipants(UserRequest ureq) {
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		listenTo(userSearchCtrl);
		
		String title = translate("add.participant");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddParticipants(List<Identity> identities) {
		for(Identity identity:identities) {
			supervisedChatsCtrl.add(identity, identity.getKey().toString());
		}
		supervisedChatsCtrl.reloadModel();
	}
}
