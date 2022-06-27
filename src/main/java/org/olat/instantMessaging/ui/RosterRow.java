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
package org.olat.instantMessaging.ui;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;

/**
 * 
 * Initial date: 21 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterRow {
	
	private RosterChannelInfos roster;
	private String onlineStatus;
	private boolean canOpenChat = true;
	
	private FormLink joinLink;
	private FormLink toolLink;
	
	public RosterRow(RosterChannelInfos roster) {
		this.roster = roster;
	}
	
	public RosterChannelInfos getRoster() {
		return roster;
	}
	
	public void setRoster(RosterChannelInfos roster) {
		this.roster = roster;
	}
	
	public InstantMessage getLastTextMessage() {
		return roster.getLastTextMessage();
	}
	
	public boolean hasUnreadMessages() {
		return roster.hasUnreadMessages();
	}
	
	public String getChannel() {
		return roster.getChannel();
	}
	
	public Date getLastActivity() {
		return roster.getLastActivity();
	}
	
	public Date getVipLastSeen() {
		return roster.getVipLastSeen();
	}
	
	public boolean inRoster(IdentityRef identity) {
		return roster.inRoster(identity);
	}
	
	public RosterStatus getRosterStatus() {
		return roster.getRosterStatus();
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public boolean isCanOpenChat() {
		return canOpenChat;
	}

	public void setCanOpenChat(boolean canOpenChat) {
		this.canOpenChat = canOpenChat;
	}

	public FormLink getJoinLink() {
		return joinLink;
	}

	public void setJoinLink(FormLink joinLink) {
		this.joinLink = joinLink;
	}
	
	public FormLink getToolLink() {
		return toolLink;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}
}
