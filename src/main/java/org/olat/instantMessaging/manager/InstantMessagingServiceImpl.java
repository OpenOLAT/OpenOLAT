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
package org.olat.instantMessaging.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.group.BusinessGroupService;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstantMessagingServiceImpl extends BasicManager implements InstantMessagingService {
	
	@Autowired
	private InstantMessageDAO imDao;
	@Autowired
	private InstantMessagePreferencesDAO prefsDao;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private BaseSecurity securityManager;

	
	@Override
	public String getStatus(Long identityKey) {
		return "available";
	}

	@Override
	public ImPreferences getImPreferences(Identity identity) {
		return prefsDao.getPreferences(identity);
	}

	@Override
	public void updateImPreferences(Identity identity, boolean visible) {
		prefsDao.updatePreferences(identity, visible);
	}

	@Override
	public void updateStatus(Identity identity, String status) {
		prefsDao.updatePreferences(identity, status);
	}

	@Override
	public int getNumOfconnectedUsers() {
		return 0;
	}

	@Override
	public OLATResourceable getPrivateChatresource(Long identityKey1, Long identityKey2) {
		String resName;
		if(identityKey1.longValue() > identityKey2.longValue()) {
			resName = identityKey2 + "-" + identityKey1;
		} else {
			resName = identityKey1 + "-" + identityKey2;
		}
		long key = identityKey1.longValue() + identityKey2.longValue();
		return OresHelper.createOLATResourceableInstance(resName, new Long(key));
	}

	@Override
	public InstantMessage getMessageById(Long messageId) {
		return imDao.loadMessageById(messageId);
	}

	@Override
	public List<InstantMessage> getMessages(OLATResourceable chatResource, int firstResult, int maxResults) {
		return imDao.getMessages(chatResource, firstResult, maxResults);
	}

	@Override
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym, String body, OLATResourceable chatResource) {
		InstantMessage message = imDao.createMessage(from, fromNickName, anonym, body, chatResource);
		InstantMessagingEvent event = new InstantMessagingEvent("message");
		event.setFromId(from.getKey());
		event.setName(fromNickName);
		event.setAnonym(anonym);
		event.setMessageId(message.getKey());
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
		return message;
	}
	
	@Override
	public InstantMessage sendPrivateMessage(Identity from, Long toIdentityKey, String body, OLATResourceable chatResource) {
		String name = userManager.getUserDisplayName(from.getUser());
		InstantMessage message = imDao.createMessage(from, name, false, body, chatResource);
		InstantMessagingEvent event = new InstantMessagingEvent("message");
		event.setFromId(from.getKey());
		event.setName(name);
		event.setAnonym(false);
		event.setMessageId(message.getKey());
		//general event
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
		//buddy event
		OLATResourceable buddy = OresHelper.createOLATResourceableInstance("Buddy", toIdentityKey);
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, buddy);
		return message;
	}

	@Override
	public void sendPresence(Identity me, String nickName, boolean anonym, OLATResourceable chatResource) {
		InstantMessagingEvent event = new InstantMessagingEvent("participant");
		event.setAnonym(anonym);
		event.setFromId(me.getKey());
		if(StringHelper.containsNonWhitespace(nickName)) {
			event.setName(nickName);
		}
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
	}
	
	@Override
	public Buddy getBuddyById(Long identityKey) {
		IdentityShort identity = securityManager.loadIdentityShortByKey(identityKey);
		String fullname = userManager.getUserDisplayName(identity);
		return new Buddy(identity.getKey(), fullname);
	}

	@Override
	public List<Buddy> getBuddies(Identity me) {
		List<Identity> contacts = businessGroupService.findContacts(me, 0, -1);
		List<Buddy> buddies = new ArrayList<Buddy>();
		for(Identity contact:contacts) {
			String fullname = userManager.getUserDisplayName(contact.getUser());
			buddies.add(new Buddy(contact.getKey(), fullname));
		}
		return buddies;
	}

	@Override
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource) {
		Set<String> names = coordinator.getCoordinator().getEventBus().getListeningIdentityNamesFor(chatResource);
		List<Identity> identities = securityManager.findIdentitiesByName(names);
		List<Buddy> buddies = new ArrayList<Buddy>();
		for(Identity identity:identities) {
			String fullname = userManager.getUserDisplayName(identity.getUser());
			buddies.add(new Buddy(identity.getKey(), fullname));
		}
		return buddies;
		
	}

	@Override
	public void listenChat(Identity identity, OLATResourceable chatResource, GenericEventListener listener) {
		coordinator.getCoordinator().getEventBus().registerFor(listener, identity, chatResource);
	}

	@Override
	public void unlistenChat(OLATResourceable chatResource, GenericEventListener listener) {
		coordinator.getCoordinator().getEventBus().deregisterFor(listener, chatResource);
	}

	@Override
	public void disableChat(Identity identity) {
		//
	}

	@Override
	public void enableChat(Identity identity) {
		//
	}
	
	

	


}
