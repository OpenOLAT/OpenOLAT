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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMemberView;
import org.olat.group.DeletableGroupData;
import org.olat.group.manager.ContactDAO;
import org.olat.group.model.ContactViewExtended;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageNotification;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.LeaveChatEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyGroup;
import org.olat.instantMessaging.model.BuddyStats;
import org.olat.instantMessaging.model.InstantMessageImpl;
import org.olat.instantMessaging.model.Presence;
import org.olat.instantMessaging.model.RosterEntryView;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.UserManager;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstantMessagingServiceImpl implements InstantMessagingService, DeletableGroupData, UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(InstantMessagingServiceImpl.class);
	
	@Autowired
	private RosterDAO rosterDao;
	@Autowired
	private InstantMessageDAO imDao;
	@Autowired
	private InstantMessagePreferencesDAO prefsDao;
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
	private DB dbInstance;


	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		imDao.deleteMessages(group);
		dbInstance.commit();
		return true;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		imDao.deleteMessages(identity);
		rosterDao.deleteEntry(identity);
		prefsDao.deletePreferences(identity);
	}
	
	@Override
	public String getExporterID() {
		return "chat";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File chatArchive = new File(archiveDirectory, "Chat.xlsx");
		try(OutputStream out = new FileOutputStream(chatArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			
			Row header = sheet.newRow();
			header.addCell(0, "Created");
			header.addCell(1, "Message");
			
			List<InstantMessage> messages = imDao.loadMessageBy(identity);
			dbInstance.commitAndCloseSession();
			for (InstantMessage message : messages) {
				Row row = sheet.newRow();
				row.addCell(0, message.getCreationDate(), workbook.getStyles().getDateTimeStyle());
				row.addCell(1, Formatter.truncate(message.getBody(), 32000));
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(chatArchive.getName());
	}

	@Override
	public String getStatus(Long identityKey) {
		return prefsDao.getStatus(identityKey);
	}

	@Override
	public ImPreferences getImPreferences(Identity identity) {
		ImPreferences prefs = prefsDao.getPreferences(identity);
		dbInstance.commit();
		return prefs;
	}

	@Override
	public void updateImPreferences(Identity identity, boolean visible) {
		prefsDao.updatePreferences(identity, visible);
		dbInstance.commit();
	}

	@Override
	public void updateStatus(Identity identity, String status) {
		prefsDao.updatePreferences(identity, status);
		dbInstance.commit();
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
		return OresHelper.createOLATResourceableInstance(resName, Long.valueOf(key));
	}

	@Override
	public InstantMessage getMessageById(Identity identity, Long messageId, boolean markedAsRead) {
		InstantMessageImpl msg = imDao.loadMessageById(messageId);
		if(markedAsRead && msg != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(msg.getResourceTypeName(), msg.getResourceId());
			imDao.deleteNotification(identity, ores);
		}
		return msg;
	}

	@Override
	public List<InstantMessage> getMessages(Identity identity, OLATResourceable chatResource,
			Date from, int firstResult, int maxResults, boolean markedAsRead) {
		List<InstantMessage> msgs = imDao.getMessages(chatResource, from, firstResult, maxResults);
		if(markedAsRead) {
			imDao.deleteNotification(identity, chatResource);
		}
		return msgs;
	}

	@Override
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym, String body, OLATResourceable chatResource) {
		InstantMessage message = null;
		try {
			message = imDao.createMessage(from, fromNickName, anonym, body, chatResource);
			dbInstance.commit();//commit before sending event
			
			InstantMessagingEvent event = new InstantMessagingEvent("message", chatResource);
			event.setFromId(from.getKey());
			event.setName(fromNickName);
			event.setAnonym(anonym);
			event.setMessageId(message.getKey());
			coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
		} catch (DBRuntimeException e) {
			dbInstance.rollbackAndCloseSession();
			log.error("", e);
			message = null;
		}
		return message;
	}
	
	@Override
	public InstantMessage sendPrivateMessage(Identity from, Long toIdentityKey, String body, OLATResourceable chatResource) {
		InstantMessage message = null;
		try {
			String name = userManager.getUserDisplayName(from);
			message = imDao.createMessage(from, name, false, body, chatResource);
			imDao.createNotification(from.getKey(), toIdentityKey, chatResource);
			dbInstance.commit();//commit before sending event
			
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
		} catch (DBRuntimeException e) {
			dbInstance.rollbackAndCloseSession();
			log.error("", e);
			message = null;
		}
		return message;
	}

	@Override
	public void deleteMessages(OLATResourceable ores) {
		imDao.deleteMessages(ores);
	}

	@Override
	public void sendPresence(Identity me, String nickName, boolean anonym, boolean vip, OLATResourceable chatResource) {
		InstantMessagingEvent event = new InstantMessagingEvent("participant", chatResource);
		event.setAnonym(anonym);
		event.setVip(vip);
		event.setFromId(me.getKey());
		if(StringHelper.containsNonWhitespace(nickName)) {
			event.setName(nickName);
		}
		String fullName = userManager.getUserDisplayName(me);
		rosterDao.updateRosterEntry(chatResource, me, fullName, nickName, anonym, vip);
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
	}
	
	@Override
	public List<InstantMessageNotification> getNotifications(Identity identity) {
		return imDao.getNotifications(identity);
	}

	@Override
	public Buddy getBuddyById(Long identityKey) {
		IdentityShort identity = securityManager.loadIdentityShortByKey(identityKey);
		String fullname = userManager.getUserDisplayName(identity);
		
		String status;
		boolean online = isOnline(identityKey);
		if(online) {
			String prefStatus = prefsDao.getStatus(identityKey);
			if(prefStatus == null) {
				status = Presence.available.name();
			} else {
				status = prefStatus;
			}
		} else {
			status = Presence.unavailable.name();
		}
		return new Buddy(identity.getKey(), fullname, false, status);
	}

	@Override
	public BuddyStats getBuddyStats(Identity me) {
		BuddyStats stats = new BuddyStats();

		//count all my buddies
		Collection<Long> buddiesColl = contactDao.getDistinctGroupOwnersParticipants(me);
		buddiesColl.remove(me.getKey());
		List<Long> buddies = new ArrayList<>(buddiesColl);
		stats.setOfflineBuddies(buddies.size());

		//filter online users
		for(Iterator<Long> buddyIt=buddies.iterator(); buddyIt.hasNext(); ) {
			Long buddyKey = buddyIt.next();
			boolean online = isOnline(buddyKey);
			if(!online) {
				buddyIt.remove();
			}
		}
		
		//count online users which are available
		int online = prefsDao.countAvailableBuddies(buddies);
		stats.setOnlineBuddies(online);
		return stats;
	}

	@Override
	public Map<Long, String> getBuddyStatus(List<Long> identityKeys) {
		return prefsDao.getBuddyStatus(identityKeys);
	}

	@Override
	public List<BuddyGroup> getBuddyGroups(Identity me, boolean offlineUsers) {
		List<BuddyGroup> groups = new ArrayList<>(25);
		Map<Long,BuddyGroup> groupMap = new HashMap<>();
		Map<Long, String> identityKeyToStatus = new HashMap<>();
		List<ContactViewExtended> contactList = contactDao.getContactWithExtendedInfos(me);
		collectMembersStatus(contactList, identityKeyToStatus);
		for(ContactViewExtended contact:contactList) {
			addBuddyToGroupList(contact, me, groupMap, groups, identityKeyToStatus, offlineUsers);
		}
		return groups;
	}
	
	private void collectMembersStatus(List<? extends BusinessGroupMemberView> members, Map<Long, String> identityKeyToStatus) {
		Set<Long> loadStatus = new HashSet<>();
		for(BusinessGroupMemberView member:members) {
			Long identityKey = member.getIdentityKey();
			if(!identityKeyToStatus.containsKey(identityKey) && !loadStatus.contains(identityKey)) {
				boolean online = isOnline(member.getIdentityKey());
				if(online) {
					loadStatus.add(identityKey);
				} else {
					identityKeyToStatus.put(identityKey, Presence.unavailable.name());
				}
			}
		}
		
		if(!loadStatus.isEmpty()) {
			List<Long> statusToLoadList = new ArrayList<>(loadStatus);
			Map<Long,String> statusMap = prefsDao.getBuddyStatus(statusToLoadList);
			for(Long toLoad:statusToLoadList) {
				String status = statusMap.get(toLoad);
				if(status == null) {
					identityKeyToStatus.put(toLoad, Presence.available.name());	
				} else {
					identityKeyToStatus.put(toLoad, status);	
				}
			}
		}	
	}
	
	private void addBuddyToGroupList(ContactViewExtended member, Identity me, Map<Long,BuddyGroup> groupMap,
			List<BuddyGroup> groups, Map<Long, String> identityKeyToStatus, boolean offlineUsers) {
		if(me != null && me.getKey().equals(member.getIdentityKey())) {
			return;
		}
		String status = identityKeyToStatus.get(member.getIdentityKey());
		if(status == null) {
			boolean online = isOnline(member.getIdentityKey());
			if(online) {
				status = prefsDao.getStatus(member.getIdentityKey());
				if(status == null) {
					status = Presence.available.name();
				}
			} else {
				status = Presence.unavailable.name();
			}
			identityKeyToStatus.put(member.getIdentityKey(), status);
		}
		
		if(offlineUsers || Presence.available.name().equals(status)) {
			BuddyGroup group = groupMap.get(member.getGroupKey());
			if(group == null) {
				group = new BuddyGroup(member.getGroupKey(), member.getGroupName());
				groupMap.put(member.getGroupKey(), group);
				groups.add(group);
			}
			boolean vip = GroupRoles.coach.name().equals(member.getRole());
			String name = userManager.getUserDisplayName(member);
			group.addBuddy(new Buddy(member.getIdentityKey(), name, false, vip, status));	
		}
	}

	@Override
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource) {
		List<RosterEntryView> roster = rosterDao.getRosterView(chatResource, 0, -1);
		List<Buddy> buddies = new ArrayList<>();
		if(roster != null) {
			for(RosterEntryView entry:roster) {
				String name = entry.isAnonym() ? entry.getNickName() : entry.getFullName();
				String status = getOnlineStatus(entry.getIdentityKey());
				buddies.add(new Buddy(entry.getIdentityKey(), name, entry.isAnonym(), entry.isVip(), status));
			}
		}
		return buddies;
	}
	
	private String getOnlineStatus(Long identityKey) {
		return isOnline(identityKey) ? Presence.available.name() : Presence.unavailable.name();
	}
	
	/**
	 * Return true if the identity is logged in on the instance
	 * @param identityKey
	 * @return
	 */
	private boolean isOnline(Long identityKey) {
		return sessionManager.isOnline(identityKey);
	}

	@Override
	public void listenChat(Identity identity, OLATResourceable chatResource, String nickName,
			boolean anonym, boolean vip, GenericEventListener listener) {
		String fullName = userManager.getUserDisplayName(identity);
		rosterDao.updateRosterEntry(chatResource, identity, fullName, nickName, anonym, vip);
		coordinator.getCoordinator().getEventBus().registerFor(listener, identity, chatResource);
	}

	@Override
	public void unlistenChat(Identity identity, OLATResourceable chatResource, GenericEventListener listener) {
		rosterDao.deleteEntry(identity, chatResource);
		dbInstance.commit();
		coordinator.getCoordinator().getEventBus()
			.fireEventToListenersOf(new LeaveChatEvent(identity.getKey(), chatResource), chatResource);
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