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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMemberView;
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.group.manager.ContactDAO;
import org.olat.group.model.BusinessGroupOwnerViewImpl;
import org.olat.group.model.BusinessGroupParticipantViewImpl;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageNotification;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyGroup;
import org.olat.instantMessaging.model.BuddyStats;
import org.olat.instantMessaging.model.InstantMessageImpl;
import org.olat.instantMessaging.model.Presence;
import org.olat.instantMessaging.model.RosterEntryView;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstantMessagingServiceImpl extends BasicManager implements InstantMessagingService,
	ApplicationListener<ContextRefreshedEvent>, DeletableGroupData {
	
	@Autowired
	private RosterDAO rosterDao;
	@Autowired
	private InstantMessageDAO imDao;
	@Autowired
	private InstantMessagePreferencesDAO prefsDao;
	@Autowired
	private ChatLogHelper logHelper;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private ContactDAO contactDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private DB dbInstance;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//rosterDao.clear();
		businessGroupService.registerDeletableGroupDataListener(this);
	}

	@Override
	@Transactional
	public boolean deleteGroupDataFor(BusinessGroup group) {
		imDao.deleteMessages(group);
		return true;
	}

	@Override
	@Transactional(readOnly=true)
	public String getStatus(Long identityKey) {
		return prefsDao.getStatus(identityKey);
	}

	@Override
	@Transactional
	public ImPreferences getImPreferences(Identity identity) {
		return prefsDao.getPreferences(identity);
	}

	@Override
	@Transactional
	public void updateImPreferences(Identity identity, boolean visible) {
		prefsDao.updatePreferences(identity, visible);
	}

	@Override
	@Transactional
	public void updateStatus(Identity identity, String status) {
		prefsDao.updatePreferences(identity, status);
	}

	@Override
	public OLATResourceable getPrivateChatResource(Long identityKey1, Long identityKey2) {
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
	@Transactional
	public InstantMessage getMessageById(Identity identity, Long messageId, boolean markedAsRead) {
		InstantMessageImpl msg = imDao.loadMessageById(messageId);
		if(markedAsRead && msg != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(msg.getResourceTypeName(), msg.getResourceId());
			imDao.deleteNotification(identity, ores);
		}
		return msg;
	}

	@Override
	@Transactional
	public List<InstantMessage> getMessages(Identity identity, OLATResourceable chatResource,
			Date from, int firstResult, int maxResults, boolean markedAsRead) {
		List<InstantMessage> msgs = imDao.getMessages(chatResource, from, firstResult, maxResults);
		if(markedAsRead) {
			imDao.deleteNotification(identity, chatResource);
		}
		return msgs;
	}

	@Override
	@Transactional
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym, String body, OLATResourceable chatResource) {
		InstantMessage message = imDao.createMessage(from, fromNickName, anonym, body, chatResource);
		InstantMessagingEvent event = new InstantMessagingEvent("message", chatResource);
		event.setFromId(from.getKey());
		event.setName(fromNickName);
		event.setAnonym(anonym);
		event.setMessageId(message.getKey());
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
		return message;
	}
	
	@Override
	@Transactional
	public InstantMessage sendPrivateMessage(Identity from, Long toIdentityKey, String body, OLATResourceable chatResource) {
		String name = userManager.getUserDisplayName(from.getUser());
		InstantMessage message = imDao.createMessage(from, name, false, body, chatResource);
		imDao.createNotification(from.getKey(), toIdentityKey, chatResource);
		
		InstantMessagingEvent event = new InstantMessagingEvent("message", chatResource);
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
	@Transactional
	public void deleteMessages(OLATResourceable ores) {
		imDao.deleteMessages(ores);
	}

	@Override
	@Transactional
	public void sendPresence(Identity me, String nickName, boolean anonym, boolean vip, OLATResourceable chatResource) {
		InstantMessagingEvent event = new InstantMessagingEvent("participant", chatResource);
		event.setAnonym(anonym);
		event.setVip(vip);
		event.setFromId(me.getKey());
		if(StringHelper.containsNonWhitespace(nickName)) {
			event.setName(nickName);
		}
		String fullName = userManager.getUserDisplayName(me.getUser());
		rosterDao.updateRosterEntry(chatResource, me, fullName, nickName, anonym, vip);
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<InstantMessageNotification> getNotifications(Identity identity) {
		return imDao.getNotifications(identity);
	}

	@Override
	@Transactional(readOnly=true)
	public Buddy getBuddyById(Long identityKey) {
		IdentityShort identity = securityManager.loadIdentityShortByKey(identityKey);
		String fullname = userManager.getUserDisplayName(identity);
		String status = getOnlineStatus(identityKey);
		return new Buddy(identity.getKey(), identity.getName(), fullname, false, status);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BuddyGroup> getBuddyGroups(Identity me, boolean offlineUsers) {
		List<BuddyGroup> groups = new ArrayList<BuddyGroup>(25);
		Map<Long,BuddyGroup> groupMap = new HashMap<Long,BuddyGroup>();
		Map<Long, String> identityKeys = new HashMap<Long, String>();
		for(BusinessGroupOwnerViewImpl owner:contactDao.getGroupOwners(me)) {
			addBuddyToGroupList(owner, me, groupMap, groups, identityKeys, true, offlineUsers);
		}
		for(BusinessGroupParticipantViewImpl participant:contactDao.getParticipants(me)) {
			addBuddyToGroupList(participant, me, groupMap, groups, identityKeys, false, offlineUsers);
		}
		
		Map<Long,String> nameMap = userManager.getUserDisplayNames(identityKeys.keySet());
		for(BuddyGroup group:groups) {
			for(Buddy buddy:group.getBuddy()) {
				buddy.setName(nameMap.get(buddy.getIdentityKey()));	
			}
		}
		return groups;
	}
	private void addBuddyToGroupList(BusinessGroupMemberView member, Identity me, Map<Long,BuddyGroup> groupMap,
			List<BuddyGroup> groups, Map<Long, String> identityKeys, boolean vip, boolean offlineUsers) {
		if(me != null && me.getKey().equals(member.getIdentityKey())) {
			return;
		}
		String status = identityKeys.get(member.getIdentityKey());
		if(status == null) {
			status = getOnlineStatus(member.getIdentityKey());
			identityKeys.put(member.getIdentityKey(), status);
		}
		
		if(offlineUsers || Presence.available.name().equals(status)) {
			BuddyGroup group = groupMap.get(member.getGroupKey());
			if(group == null) {
				group = new BuddyGroup(member.getGroupKey(), member.getGroupName());
				groupMap.put(member.getGroupKey(), group);
				groups.add(group);
			}
			group.addBuddy(new Buddy(member.getIdentityKey(), member.getUsername(), null, false, vip, status));	
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<Buddy> getOnlineBuddies() {
		Collection<Long> ids = sessionManager.getUsersOnline();
		List<IdentityShort> contacts = securityManager.loadIdentityShortByKeys(ids);
		List<Buddy> buddies = new ArrayList<Buddy>(contacts.size());
		for(IdentityShort contact:contacts) {
			String fullname = userManager.getUserDisplayName(contact);
			String status = getOnlineStatus(contact.getKey());
			buddies.add(new Buddy(contact.getKey(), contact.getName(), fullname, false, status));
		}
		return buddies;
	}

	@Override
	@Transactional(readOnly=true)
	public BuddyStats getBuddyStats(Identity me) {
		BuddyStats stats = new BuddyStats();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		stats.setOfflineBuddies(securityManager.countUniqueUserLoginsSince(cal.getTime()));
		stats.setOnlineBuddies(sessionManager.getUserSessionsCnt());
		return stats;
	}

	@Override
	@Transactional(readOnly=true)
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource) {
		List<RosterEntryView> roster = rosterDao.getRosterView(chatResource, 0, -1);
		List<Buddy> buddies = new ArrayList<Buddy>();
		if(roster != null) {
			for(RosterEntryView entry:roster) {
				String name = entry.isAnonym() ? entry.getNickName() : entry.getFullName();
				String status = getOnlineStatus(entry.getIdentityKey());
				buddies.add(new Buddy(entry.getIdentityKey(), entry.getUsername(), name, entry.isAnonym(), entry.isVip(), status));
			}
		}
		return buddies;
	}
	
	private String getOnlineStatus(Long identityKey) {
		boolean online = sessionManager.isOnline(identityKey);
		return online ? Presence.available.name() : Presence.unavailable.name();
	}

	@Override
	@Transactional
	public void listenChat(Identity identity, OLATResourceable chatResource,
			boolean anonym, boolean vip, GenericEventListener listener) {
		String fullName = userManager.getUserDisplayName(identity.getUser());
		rosterDao.updateRosterEntry(chatResource, identity, fullName, null, anonym, vip);
		coordinator.getCoordinator().getEventBus().registerFor(listener, identity, chatResource);
	}

	@Override
	@Transactional
	public void unlistenChat(Identity identity, OLATResourceable chatResource, GenericEventListener listener) {
		rosterDao.deleteEntry(identity, chatResource);
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