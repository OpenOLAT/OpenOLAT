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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.admin.user.UserSearchFlexiController.UserSearchProvider;
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
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.ims.qti21.QTI21Service;
import org.olat.instantMessaging.ui.ChatViewConfig;
import org.olat.instantMessaging.ui.SupervisorChatController;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.message.ui.AssessmentMessageListController;
import org.olat.modules.teams.TeamsModule;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQCommunicationController extends BasicController implements Activateable2 {

	private final Link addParticipantLink;
	
	private final RepositoryEntry entry;
	private final IQTESTCourseNode courseNode;
	
	private CloseableModalController cmc;
	private UserSearchFlexiController userSearchCtrl;
	private final SupervisorChatController supervisedChatsCtrl;
	private final AssessmentMessageListController assessmentMessagesCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private QTI21Service qti21Service;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;

	public IQCommunicationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, IQTESTCourseNode courseNode, boolean admin) {
		super(ureq, wControl);
		this.entry = entry;
		this.courseNode = courseNode;
		
		VelocityContainer mainVC = createVelocityContainer("communication");
		
		assessmentMessagesCtrl = new AssessmentMessageListController(ureq, wControl, entry, courseNode.getIdent(), admin);
		listenTo(assessmentMessagesCtrl);
		mainVC.put("messages", assessmentMessagesCtrl.getInitialComponent());
		
		addParticipantLink = LinkFactory.createButton("add.participant", mainVC, this);
		addParticipantLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		
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
		
		supervisedChatsCtrl = new SupervisorChatController(ureq, getWindowControl(), entry.getOlatResource(), courseNode.getIdent(), viewConfig);
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
		AssessmentSearchProvider provider = new AssessmentSearchProvider();
		userSearchCtrl = new UserSearchFlexiController(ureq, getWindowControl(), provider, true);
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
	
	private class AssessmentSearchProvider implements UserSearchProvider {

		@Override
		public int getMaxEntries() {
			return 15;
		}

		@Override
		public void getResult(String searchValue, ListReceiver receiver) {
			Map<String, String> userProperties = new HashMap<>();
			userProperties.put(UserConstants.FIRSTNAME, searchValue);
			userProperties.put(UserConstants.LASTNAME, searchValue);
			userProperties.put(UserConstants.EMAIL, searchValue);
			userProperties.put(UserConstants.NICKNAME, searchValue);
			List<Identity> res = searchUsers(null, userProperties, false);
			
			boolean hasMore = false;
			int maxEntries = getMaxEntries();
			for (Iterator<Identity> it_res = res.iterator(); (hasMore=it_res.hasNext()) && maxEntries > 0;) {
				maxEntries--;
				Identity ident = it_res.next();
				String key = ident.getKey().toString();
				String displayKey = ident.getUser().getNickName();
				String displayText = userManager.getUserDisplayName(ident);
				receiver.addEntry(key, displayKey, displayText, CSSHelper.CSS_CLASS_USER);
			}
			
			if(hasMore) {
				receiver.addEntry(".....",".....");
			}
		}

		@Override
		public List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch,
				boolean userPropertiesAsIntersectionSearch) {
			return qti21Service.getRunningAssessmentTestSessions(entry, courseNode.getIdent(), userPropertiesSearch, userPropertiesAsIntersectionSearch);
		}
	}
}
