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
package org.olat.instantMessaging;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyGroup;
import org.olat.instantMessaging.model.BuddyStats;
import org.olat.instantMessaging.model.RosterChannelInfos;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface InstantMessagingService {

	public static final String PRESENCE_MODE_AVAILABLE = "online";
	public static final String PRESENCE_MODE_DND = "do not disturb";
	public static final String PRESENCE_MODE_UNAVAILABLE = "unavailable";
	
	public static final String PERSONAL_EVENT_ORES_NAME = "PersonalChatNotification";
	
	public static final OLATResourceable TOWER_EVENT_ORES = OresHelper.createOLATResourceableType("InstantMessagingTower");
	public static final OLATResourceable ASSESSMENT_EVENT_ORES = OresHelper.createOLATResourceableType(AssessmentEvent.class);

	/**
	 * 
	 * @param me
	 * @return
	 */
	public List<BuddyGroup> getBuddyGroups(Identity me, boolean offlineUsers);
	
	public Buddy getBuddyById(Long identityKey);
	
	/**
	 * Return the list of users who are chating
	 * @param chatResource
	 * @return
	 */
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource, String resSubPath, String channel);
	
	public BuddyStats getBuddyStats(Identity me);
	
	public Map<Long,String> getBuddyStatus(List<Long> identityKeys);
	
	public boolean isOnline(IdentityRef identity);
	
	/**
	 * Enter a chat conversation.
	 * 
	 * @param identity The user
	 * @param chatResource The resource
	 * @param resSubPath The sub-path
	 * @param channel The channel
	 * @param nickName A nick name
	 * @param anonym If the user is anonym
	 * @param asVip If the user is VIP
	 * @param persistent Persist the entry
	 * @param createRosterEntry Create the roster entry if it's doesn't exist
	 * @param listener The listener
	 * @return true if a roster entry exists
	 */
	public boolean listenChat(Identity identity, OLATResourceable chatResource, String resSubPath, String channel,
			String nickName, boolean anonym, boolean asVip, boolean persistent, boolean createRosterEntry, GenericEventListener listener);
	
	/**
	 * Go away
	 * @param chatResource
	 * @param listener
	 */
	public void unlistenChat(Identity identity, OLATResourceable chatResource, String resSubPath, String channel,
			GenericEventListener listener);
	
	/**
	 * Set the status to close and notify people.
	 * 
	 * @param identity The identity which ends the channel
	 * @param chatResource The chat resource
	 * @param resSubPath The resource sub-path
	 * @param channel The channel
	 */
	public void endChannel(Identity identity, OLATResourceable chatResource, String resSubPath, String channel);
	
	public void clearChannel(OLATResourceable chatResource, String resSubPath, String channel);
	
	/**
	 * Factory method to build the OLATResourceable for private chat
	 * @param identityKey1
	 * @param identityKey2
	 * @return
	 */
	public OLATResourceable getPrivateChatResource(Long identityKey1, Long identityKey2);
	
	
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym, String body, InstantMessageTypeEnum type,
			OLATResourceable chatResource, String resSubPath, String channel, List<IdentityRef> toNotifyList);
	
	public InstantMessage sendStatusMessage(Identity from, String fromNickName, boolean anonym, InstantMessageTypeEnum type,
			OLATResourceable chatResource, String resSubPath, String channel);
	
	public InstantMessage sendMeetingMessage(Identity from, String fromNickName, boolean anonym, String meetingName,
			OLATResourceable chatResource, String resSubPath, String channel);
	
	
	/**
	 * One to one/direct messaging.
	 * 
	 * @param from The identity which send the message
	 * @param toIdentityKey The receiver of the message
	 * @param body The text
	 * @param chatResource The resourceable identifier of the chat
	 * @return
	 */
	public InstantMessage sendPrivateMessage(Identity from, Long toIdentityKey,
			String body, OLATResourceable chatResource);
	
	public void updateLastSeen(Identity identity, OLATResourceable chatResource, String resSubPath, String channel);
	
	/**
	 * 
	 * @param me
	 * @param messageId
	 * @param markAsRead
	 * @return
	 */
	public InstantMessage getMessageById(Identity me, Long messageId, boolean markAsRead);
	
	/**
	 * Get the messages of a chat
	 * @param ores
	 * @param firstResult
	 * @param maxResults
	 * @param markAsRead
	 * @return
	 */
	public List<InstantMessage> getMessages(Identity me, OLATResourceable ores, String resSubPath, String channel,
			Date from, int firstResult, int maxResults, boolean markAsRead);
	
	/**
	 * Delete the chat log
	 * @param ores
	 */
	public void deleteMessages(OLATResourceable ores);
	
	public void sendPresence(Identity me, OLATResourceable chatResource, String resSubPath, String channel,
			String nickName, boolean anonym, boolean vip, boolean persistent);
	
	/**
	 * Get the notifications of message waiting to be read
	 * @param identity
	 * @return
	 */
	public List<InstantMessageNotification> getPrivateNotifications(IdentityRef identity);

	public long countRequestNotifications(IdentityRef identity);
	
	public List<InstantMessageNotification> getRequestNotifications(IdentityRef identity);
	
	/**
	 * Delete the notifications of all users for the specified chat, specified by its
	 * resource, sub-path and channel. If sub-path or channel are null, the delelte will only
	 * to be applied to null.
	 * 
	 * @param ores The chat resource
	 * @param resSubPath The resource sub-path
	 * @param channel The channel
	 */
	public void deleteNotifications(OLATResourceable ores, String resSubPath, String channel);
	
	
	public List<RosterChannelInfos> getRosters(OLATResourceable ores, String resSubPath, IdentityRef me, boolean onlyMyActiveRosters);
	
	public RosterChannelInfos getRoster(OLATResourceable ores, String resSubPath, String channel, IdentityRef identity);
	
	/**
	 * Add a permanent roster entry.
	 * 
	 * @param identity The identity
	 * @param ores The resource
	 * @param resSubPath The sub identifier
	 * @param channel The sub-sub identifier
	 * @param anonym If the user is anonymous
	 * @param vip If the user is VIP (owner/coach typically)
	 */
	public void addToRoster(Identity identity, OLATResourceable ores, String resSubPath, String channel,
			String nickName, boolean anonym, boolean vip);
	
	
	/**
	 * Return the status of an user, available, unavailable or dnd (do not disturb)
	 * @param identityKey
	 * @return
	 */
	public String getStatus(Long identityKey);
	
	/**
	 * Get or create the instant messaging preferences of an user
	 * @param identity
	 * @return
	 */
	public ImPreferences getImPreferences(Identity identity);
	
	/**
	 * Update the preference of an user
	 * @param identity
	 * @param visible
	 */
	public void updateImPreferences(Identity identity, boolean visible);
	
	/**
	 * Update the status of an user
	 * @param identity
	 * @param status
	 */
	public void updateStatus(Identity identity, String status);

}
