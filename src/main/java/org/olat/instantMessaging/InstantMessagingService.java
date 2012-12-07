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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.instantMessaging.model.Buddy;

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
	public List<Buddy> getBuddies(Identity me);
	
	public Buddy getBuddyById(Long identityKey);
	
	public List<Buddy> getBuddiesListenTo(OLATResourceable chatResource);
	
	public void listenChat(Identity identity, OLATResourceable chatResource,  GenericEventListener listener);
	
	public void unlistenChat(OLATResourceable chatResource, GenericEventListener listener);
	
	public OLATResourceable getPrivateChatresource(Long identityKey1, Long identityKey2);
	
	public InstantMessage getMessageById(Long messageId);
	
	public InstantMessage sendMessage(Identity from, String fromNickName, boolean anonym,
			String body, OLATResourceable chatResource);
	
	public InstantMessage sendPrivateMessage(Identity from, Long toIdentityKey,
			String body, OLATResourceable chatResource);
	
	public List<InstantMessage> getMessages(OLATResourceable ores, int firstResult, int maxResults);
	
	public void sendPresence(Identity me, String nickName, boolean anonym, OLATResourceable chatResource);
	
	
	public String getStatus(Long identityKey);
	
	public ImPreferences getImPreferences(Identity identity);
	
	public void updateImPreferences(Identity identity, boolean visible);
	
	public void updateStatus(Identity identity, String status);

	public void enableChat(Identity identity);
	
	public void disableChat(Identity identity);
	
	public int getNumOfconnectedUsers();

}
