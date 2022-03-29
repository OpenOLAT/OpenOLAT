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
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
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
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.LeaveChatEvent;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyGroup;
import org.olat.instantMessaging.model.BuddyStats;
import org.olat.instantMessaging.model.InstantMessageImpl;
import org.olat.instantMessaging.model.InstantMessageNotificationTypeEnum;
import org.olat.instantMessaging.model.Presence;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	private InstantMessageNotificationDAO notificationDao;
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
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;


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
			notificationDao.deleteNotification(identity, ores, null, null);
		}
		return msg;
	}

	@Override
	public List<InstantMessage> getMessages(Identity identity, OLATResourceable chatResource, String resSubPath, String channel,
			Date from, int firstResult, int maxResults, boolean markedAsRead) {
		List<InstantMessage> msgs = imDao.getMessages(chatResource, resSubPath, channel, from, firstResult, maxResults);
		if(markedAsRead) {
			notificationDao.deleteNotification(identity, chatResource, null, null);
		}
		return msgs;
	}
	
	@Override
	public InstantMessage sendStatusMessage(Identity from, String fromNickName, boolean anonym, InstantMessageTypeEnum type,
			OLATResourceable chatResource, String resSubPath, String channel) {
		return sendMessage(from, fromNickName, anonym, null, type, chatResource, resSubPath, channel, List.of());
	}

	@Override
	public InstantMessage sendMeetingMessage(Identity from, String fromNickName, boolean anonym, String meetingName,
			OLATResourceable chatResource, String resSubPath, String channel) {
		InstantMessage msg = null;
		if(bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isChatExamsEnabled()) {
			msg = sendBigBlueButtonMessage(from, fromNickName, anonym, chatResource, resSubPath, channel);
		} else if(teamsModule.isEnabled() && teamsModule.isChatExamsEnabled()) {
			msg = sendTeamsMessage(from, fromNickName, anonym, chatResource, resSubPath, channel);
		}
		return msg;
	}
	
	private InstantMessage sendTeamsMessage(Identity from, String fromNickName, boolean anonym,
			OLATResourceable chatResource, String resSubPath, String channel) {
		InstantMessage msg = null;
		
		String identifier = resSubPath + "-" + channel;
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(chatResource, false);
		if(entry != null) {
			List<TeamsMeeting> meetings = teamsService.getMeetings(entry, identifier, null);
			TeamsMeeting meeting = null;
			if(meetings.isEmpty()) {
				meeting = teamsService.createMeeting(channel, null, null, entry, identifier, null, from);
			} else {
				meeting = meetings.get(meetings.size() - 1);
			}
			
			if(meeting != null) {
				msg = sendMessage(from, fromNickName, anonym, null, null, meeting, InstantMessageTypeEnum.meeting, chatResource, resSubPath, channel, null);
			}
		}
		
		return msg;
	}
	
	private InstantMessage sendBigBlueButtonMessage(Identity from, String fromNickName, boolean anonym,
			OLATResourceable chatResource, String resSubPath, String channel) {
		InstantMessage msg = null;
		
		String identifier = resSubPath + "-" + channel;
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(chatResource, false);
		if(entry != null) {
			List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getMeetings(entry, identifier, null, false);
			BigBlueButtonMeeting meeting = null;
			if(meetings.isEmpty()) {
				meeting = bigBlueButtonManager.createAndPersistMeeting(fromNickName, entry, identifier, null, from);
			} else {
				meeting = meetings.get(meetings.size() - 1);
			}
			
			if(meeting != null) {
				msg = sendMessage(from, fromNickName, anonym, null, meeting, null, InstantMessageTypeEnum.meeting, chatResource, resSubPath, channel, null);
			}
		}
		
		return msg;
	}

	@Override
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym, String body, InstantMessageTypeEnum type,
			OLATResourceable chatResource, String resSubPath, String channel, List<IdentityRef> toNotifyList) {
		return sendMessage(from, fromNickName, anonym, body, null, null, type, chatResource, resSubPath, channel, toNotifyList);
	}
	
	private InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym,
			String body, BigBlueButtonMeeting bbbMeeting, TeamsMeeting teamsMeeting, InstantMessageTypeEnum type,
			OLATResourceable chatResource, String resSubPath, String channel, List<IdentityRef> toNotifyList) {
		InstantMessage message = null;
		try {
			message = imDao.createMessage(from, fromNickName, anonym, body, bbbMeeting, teamsMeeting, chatResource, resSubPath, channel, type);
			dbInstance.commit();//commit before sending event

			InstantMessageNotificationTypeEnum notification = InstantMessageNotificationTypeEnum.message;
			if(toNotifyList != null && !toNotifyList.isEmpty()) {
				if(type == InstantMessageTypeEnum.request) {
					notification = InstantMessageNotificationTypeEnum.request;
				}
				for(IdentityRef toNotify:toNotifyList) {
					notificationDao.createNotification(from.getKey(), toNotify.getKey(), chatResource, resSubPath, channel, notification);
				}
				dbInstance.commit();
			}
			
			final InstantMessagingEvent event = new InstantMessagingEvent(notification.name(), chatResource, resSubPath, channel);
			event.setFromId(from.getKey());
			event.setName(fromNickName);
			event.setAnonym(anonym);
			event.setMessage(message.getKey(), message.getType());
			coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
			
			if(toNotifyList != null && !toNotifyList.isEmpty()) {
				for(IdentityRef toNotify:toNotifyList) {
					coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event,
							OresHelper.createOLATResourceableInstance(InstantMessagingService.PERSONAL_EVENT_ORES_NAME, toNotify.getKey()));
				}
			}
			
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
			message = imDao.createMessage(from, name, false, body, null, null, chatResource, null, null, InstantMessageTypeEnum.text);
			notificationDao.createNotification(from.getKey(), toIdentityKey, chatResource, null, null, InstantMessageNotificationTypeEnum.message);
			dbInstance.commit();//commit before sending event
			
			InstantMessagingEvent event = new InstantMessagingEvent(InstantMessagingEvent.MESSAGE, chatResource, null, null);
			event.setFromId(from.getKey());
			event.setName(name);
			event.setAnonym(false);
			event.setMessage(message.getKey(), message.getType());
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
		List<InstantMessage> messagesToDelete = imDao.getAllResourcesMessages(ores);
		List<TeamsMeeting> teamsToDelete = new ArrayList<>();
		List<BigBlueButtonMeeting> bigBlueButtonToDelete = new ArrayList<>();
		for(InstantMessage message:messagesToDelete) {
			if(message.getBbbMeeting() != null) {
				bigBlueButtonToDelete.add(message.getBbbMeeting());
				
			} else if(message.getTeamsMeeting() != null) {
				teamsToDelete.add(message.getTeamsMeeting());
			}
		}
		
		imDao.deleteMessages(ores);
		dbInstance.commit();
		
		for(BigBlueButtonMeeting meeting:bigBlueButtonToDelete) {
			BigBlueButtonErrors errors = new BigBlueButtonErrors();
			bigBlueButtonManager.deleteMeeting(meeting, errors);
		}
		for(TeamsMeeting meeting:teamsToDelete) {
			teamsService.deleteMeeting(meeting);
		}
		dbInstance.commit();
	}

	@Override
	public void sendPresence(Identity me, OLATResourceable chatResource, String resSubPath, String channel,
			String nickName, boolean anonym, boolean vip, boolean persistent) {
		InstantMessagingEvent event = new InstantMessagingEvent(InstantMessagingEvent.PARTICIPANT, chatResource, resSubPath, channel);
		event.setAnonym(anonym);
		event.setVip(vip);
		event.setFromId(me.getKey());
		if(StringHelper.containsNonWhitespace(nickName)) {
			event.setName(nickName);
		}
		String fullName = userManager.getUserDisplayName(me);
		rosterDao.updateRosterEntry(chatResource, resSubPath, channel, me, fullName, nickName, anonym, vip, persistent, false, true);
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, chatResource);
	}
	
	@Override
	public void updateLastSeen(Identity identity, OLATResourceable chatResource, String resSubPath, String channel) {
		rosterDao.updateLastSeen(identity, chatResource, resSubPath, channel);
	}
	
	@Override
	public long countRequestNotifications(IdentityRef identity) {
		return notificationDao.countRequestNotifications(identity);
	}

	@Override
	public List<InstantMessageNotification> getRequestNotifications(IdentityRef identity) {
		return notificationDao.getRequestNotifications(identity);
	}

	@Override
	public List<InstantMessageNotification> getPrivateNotifications(IdentityRef identity) {
		return notificationDao.getPrivateNotifications(identity);
	}

	@Override
	public void deleteNotifications(OLATResourceable chatResource, String resSubPath, String channel) {
		List<InstantMessageNotification> notifications = notificationDao.getNotifications(chatResource, resSubPath, channel);
		notificationDao.deleteNotification(null, chatResource, resSubPath, channel);
		dbInstance.commit();

		for(InstantMessageNotification notification:notifications) {
			InstantMessagingEvent event = new InstantMessagingEvent(InstantMessagingEvent.DELETE_NOTIFICATION, chatResource, resSubPath, channel);
			coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, OresHelper
					.createOLATResourceableInstance(PERSONAL_EVENT_ORES_NAME, notification.getToIdentityKey()));
		}
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
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource, String resSubPath, String channel) {
		List<RosterEntry> roster = rosterDao.getRoster(chatResource, resSubPath, channel);
		List<Buddy> buddies = new ArrayList<>();
		if(roster != null) {
			for(RosterEntry entry:roster) {
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
	
	@Override
	public boolean isOnline(IdentityRef identity) {
		return isOnline(identity.getKey());
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
	public boolean listenChat(Identity identity, OLATResourceable chatResource, String resSubPath, String channel,
			String nickName, boolean anonym, boolean vip, boolean persistent, boolean createRosterEntry, GenericEventListener listener) {
		String fullName = userManager.getUserDisplayName(identity);
		boolean entry = rosterDao.updateRosterEntry(chatResource, resSubPath, channel, identity,
				fullName, nickName, anonym, vip, persistent, true, createRosterEntry);
		coordinator.getCoordinator().getEventBus().registerFor(listener, identity, chatResource);
		return entry;
	}

	@Override
	public void unlistenChat(Identity identity, OLATResourceable chatResource, String resSubPath, String channel,
			GenericEventListener listener) {
		rosterDao.inactivateEntry(identity, chatResource, resSubPath, channel);
		dbInstance.commit();
		coordinator.getCoordinator().getEventBus()
			.fireEventToListenersOf(new LeaveChatEvent(identity.getKey(), chatResource), chatResource);
		coordinator.getCoordinator().getEventBus().deregisterFor(listener, chatResource);
	}

	@Override
	public void clearChannel(OLATResourceable chatResource, String resSubPath, String channel) {
		rosterDao.deleteVIPEntries(chatResource, resSubPath, channel);
		dbInstance.commit();
	}

	@Override
	public void endChannel(Identity identity, OLATResourceable chatResource, String resSubPath, String channel) {
		RosterChannelInfos channelInfos = getRoster(chatResource, resSubPath, channel, identity);
		if(channelInfos == null || (channelInfos.getLastStatusMessage() == null && channelInfos.getLastTextMessage() == null)) {
			return;// nothing happened
		}
		
		RosterStatus status= channelInfos.getRosterStatus();
		if(status != RosterStatus.ended) {
			String fullName = userManager.getUserDisplayName(identity);
			sendStatusMessage(identity, fullName, false, InstantMessageTypeEnum.end, chatResource, resSubPath, channel);
		}
		
		// Notify all users with notifications pending
		List<InstantMessageNotification> notifications = notificationDao.getNotifications(chatResource, resSubPath, channel);
		Set<Long> toNotifySet = notifications.stream()
				.filter(notification -> notification.getToIdentityKey() != null)
				.map(InstantMessageNotification::getToIdentityKey)
				.collect(Collectors.toSet());
		
		// delete all notifications
		notificationDao.deleteNotification(null, chatResource, resSubPath, channel);
		dbInstance.commit();

		// Notify all users with roster entry
		List<Long> toNotifyList = channelInfos.getEntries().stream()
				.map(RosterEntry::getIdentityKey)
				.filter(identityKey -> (identity == null || !identity.getKey().equals(identityKey)))
				.collect(Collectors.toList());
		toNotifySet.addAll(toNotifyList);
		toNotifySet.remove(identity.getKey());
		
		if(!toNotifySet.isEmpty()) {
			InstantMessagingEvent event = new InstantMessagingEvent(InstantMessagingEvent.END_CHANNEL, chatResource, resSubPath, channel);
			event.setFromId(identity.getKey());
			event.setAnonym(false);
			for(Long toNotify:toNotifySet) {
				coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event,
						OresHelper.createOLATResourceableInstance(InstantMessagingService.PERSONAL_EVENT_ORES_NAME, toNotify));
			}
		}
	}

	@Override
	public List<RosterChannelInfos> getRosters(OLATResourceable ores, String resSubPath, IdentityRef identity, boolean onlyMyActiveRosters) {
		return rosterDao.getRosterAroundChannels(ores, resSubPath, null, identity, onlyMyActiveRosters);
	}
	
	@Override
	public RosterChannelInfos getRoster(OLATResourceable ores, String resSubPath, String channel, IdentityRef identity) {
		List<RosterChannelInfos> infos = rosterDao.getRosterAroundChannels(ores, resSubPath, channel, identity, false);
		return infos.size() == 1 ? infos.get(0) : null;
	}

	@Override
	public void addToRoster(Identity identity, OLATResourceable chatResource, String resSubPath, String channel, String nickName, boolean anonym, boolean vip) {
		String fullName = userManager.getUserDisplayName(identity);
		rosterDao.updateRosterEntry(chatResource, resSubPath, channel, identity, fullName, nickName, anonym, vip, true, false, true);
	}
}