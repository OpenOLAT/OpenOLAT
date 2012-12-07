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
	public void updateImPreferences(Identity identity, boolean visible, boolean onlineTime) {
		//
	}

	@Override
	public void updateStatus(Identity identity, String status) {
		//
	}

	@Override
	public int getNumOfconnectedUsers() {
		return 0;
	}

	@Override
	public InstantMessage getMessageById(Long messageId) {
		return imDao.loadMessageById(messageId);
	}

	@Override
	public InstantMessage createMessage(Identity from, String body) {
		return imDao.createMessage(from, body);
	}

	@Override
	public InstantMessage sendMessage(Identity from, String body, OLATResourceable to) {
		InstantMessage message = imDao.createMessage(from, body);
		InstantMessagingEvent event = new InstantMessagingEvent("message");
		event.setFromId(from.getKey());
		event.setMessageId(message.getKey());
		
		//reverse the target as the target listen to me
		if("Buddy".equals(to.getResourceableTypeName())
				&& !from.getKey().equals(to.getResourceableId())) {
			to = OresHelper.createOLATResourceableInstance("Buddy", from.getKey());
		}
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, to);
		return message;
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
