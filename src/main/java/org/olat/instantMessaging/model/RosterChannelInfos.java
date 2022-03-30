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
package org.olat.instantMessaging.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.RosterEntry;

/**
 * 
 * Initial date: 22 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterChannelInfos {
	
	private final String channel;
	private Long unreadMessages;
	private InstantMessage lastTextMessage;
	private InstantMessage lastStatusMessage;
	private final List<RosterEntry> entries = new ArrayList<>();
	
	public RosterChannelInfos(String channel, RosterEntry entry, InstantMessage lastTextMessage, InstantMessage lastStatusMessage) {
		this.channel = channel;
		entries.add(entry);
		this.lastTextMessage = lastTextMessage;
		this.lastStatusMessage = lastStatusMessage;
	}
	
	public String getChannel() {
		return channel;
	}

	public List<RosterEntry> getEntries() {
		return entries;
	}
	
	public boolean hasActiveVipEntries() {
		return entries.stream()
				.filter(RosterEntry::isVip)
				.filter(RosterEntry::isActive)
				.count() > 0;
	}
	
	public boolean hasVipEntries() {
		return entries.stream()
				.anyMatch(RosterEntry::isVip);
	}
	
	public boolean inRoster(IdentityRef ref) {
		for(RosterEntry entry:entries) {
			if(ref.getKey().equals(entry.getIdentityKey())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean inRosterAndActive(IdentityRef ref) {
		for(RosterEntry entry:entries) {
			if(ref.getKey().equals(entry.getIdentityKey())) {
				return entry.isActive();
			}
		}
		return false;
	}
	
	public List<RosterEntry> getVipEntries() {
		return entries.stream()
				.filter(RosterEntry::isVip)
				.collect(Collectors.toList());
	}
	
	public List<RosterEntry> getNonVipEntries() {
		return entries.stream()
				.filter(entry -> !entry.isVip())
				.collect(Collectors.toList());
	}
	
	public Date getLastActivity() {
		Date date = lastTextMessage == null ? null : lastTextMessage.getCreationDate();
		if(lastStatusMessage != null && (date == null || date.before(lastStatusMessage.getCreationDate()))) {
			date = lastStatusMessage.getCreationDate();
		}
		return date;
	}
	
	public Date getVipLastSeen() {
		Date lastSeen = null;
		for(RosterEntry entry:entries) {
			if(!entry.isVip()) continue;
			
			if(lastSeen == null
					|| (entry.getLastSeen() != null && lastSeen.before(entry.getLastSeen()))) {
				lastSeen = entry.getLastSeen();
			}
		}
		return lastSeen;
	}

	public InstantMessage getLastTextMessage() {
		return lastTextMessage;
	}
	
	public InstantMessage getLastStatusMessage() {
		return lastStatusMessage;
	}
	
	public boolean hasUnreadMessages() {
		return unreadMessages != null && unreadMessages.longValue() > 0;
	}

	public Long getUnreadMessages() {
		return unreadMessages;
	}

	public void setUnreadMessages(Long unreadMessages) {
		this.unreadMessages = unreadMessages;
	}
	
	/**
	 * An artificial status based on the last messages.
	 * 
	 * @return The status of the roster
	 */
	public RosterStatus getRosterStatus() {
		InstantMessage lastText = getLastTextMessage();
		InstantMessage lastStatus = getLastStatusMessage();
		
		if(lastText != null && lastText.getType() == InstantMessageTypeEnum.request
				&& (lastStatus == null || lastText.getCreationDate().after(lastStatus.getCreationDate()))) {
			return RosterStatus.request;
		}
		
		if(lastStatus != null
				&& (lastText == null || lastStatus.getCreationDate().after(lastText.getCreationDate()))) {
			if(lastStatus.getType() == InstantMessageTypeEnum.close) {
				return RosterStatus.completed;
			}
			if(lastStatus.getType() == InstantMessageTypeEnum.end) {
				return RosterStatus.ended;
			}
		}
		return RosterStatus.active;
	}
	
	public enum RosterStatus {
		request,
		active,
		completed,
		ended
	}
}
