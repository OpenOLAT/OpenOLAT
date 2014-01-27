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

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyGroup;
import org.olat.instantMessaging.model.BuddyStats;

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
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource);
	
	public BuddyStats getBuddyStats(Identity me);
	
	public Map<Long,String> getBuddyStatus(List<Long> identityKeys);
	
	/**
	 * Enter a chat conversation
	 * @param identity
	 * @param chatResource
	 * @param listener
	 */
	public void listenChat(Identity identity, OLATResourceable chatResource, String nickName, boolean anonym, boolean asVip, GenericEventListener listener);
	
	/**
	 * Go away
	 * @param chatResource
	 * @param listener
	 */
	public void unlistenChat(Identity identity, OLATResourceable chatResource, GenericEventListener listener);
	
	/**
	 * Factory method to build the OLATResourceable for privat chat
	 * @param identityKey1
	 * @param identityKey2
	 * @return
	 */
	public OLATResourceable getPrivateChatResource(Long identityKey1, Long identityKey2);
	

	
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym,
			String body, OLATResourceable chatResource);
	
	public InstantMessage sendPrivateMessage(Identity from, Long toIdentityKey,
			String body, OLATResourceable chatResource);
	
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
	public List<InstantMessage> getMessages(Identity me, OLATResourceable ores, Date from, int firstResult, int maxResults, boolean markAsRead);
	
	/**
	 * Delete the chat log
	 * @param ores
	 */
	public void deleteMessages(OLATResourceable ores);
	
	public void sendPresence(Identity me, String nickName, boolean anonym, boolean vip, OLATResourceable chatResource);
	
	/**
	 * Get the notifications of message waiting to be read
	 * @param identity
	 * @return
	 */
	public List<InstantMessageNotification> getNotifications(Identity identity);
	
	
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

	/**
	 * Enable chat of an user (this is a dummy implementation!!! used as marker)
	 * @param identity
	 */
	public void enableChat(Identity identity);
	
	/**
	 * Disable the chat function of an user (this is a dummy implementation!!! used as marker)
	 * @param identity
	 */
	public void disableChat(Identity identity);
}
