/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.instantMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.Formatter;

/**
 * Helper class needed for rendering the roster and message stuff Iterator free
 * for velocity. Velocity produces warnings when passing objects that return
 * Iterators for looping over collections. Class currenty only needed for roster
 * rendering in navigation bar.
 * <P>
 * Initial Date: 20.01.2005
 * 
 * @author guido
 */
public class ClientHelper extends LogDelegator {



	private InstantMessagingClient imc;
	private Controller controller;
	private VelocityContainer vc;
	private Translator translator;

	/**
	 * @param username
	 * @param locale
	 */
	public ClientHelper(String username, Controller controller, VelocityContainer vc, Translator translator) {
		this.imc = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(username);
		this.controller = controller;
		this.vc = vc;
		this.translator = translator;
	}

	/**
	 * @return a List
	 */
	public List<RosterGroup> getRoster() {
		if (imc.isConnected()) {
			List<RosterGroup> groups = new ArrayList<RosterGroup>();
			for (Iterator <RosterGroup>i = imc.getRoster().getGroups().iterator(); i.hasNext();) {
				groups.add(i.next());
			}
			return groups;
		}
		return new ArrayList<RosterGroup>(0);
	}

	/**
	 * used by velocity
	 * @return a list of distinct usernames of the roster
	 */
	public List<String> getDistinctRoster() {
		if (imc.isConnected()) {
			return createLinkList(imc.getRoster().getEntries().iterator(), null);
		}
		return new ArrayList<String>(0);
	}

	/**
	 * used by velocity
	 * @param groupname
	 * @return a List
	 */
	public List<String> getRosterGroupEntries(String groupname) {
		if (imc.isConnected()) {
			return createLinkList(imc.getRoster().getGroup(groupname).getEntries().iterator(), groupname);
		}
		return new ArrayList<String>();
	}

	private List<String> createLinkList(Iterator <RosterEntry> iter, String groupname) {
		List<String> entries = new ArrayList<String>();
		Link link;
		for (Iterator <RosterEntry> i = iter; i.hasNext();) {
			RosterEntry entry = i.next();
			String entryPresence = getUserPresence(entry.getUser());
			if (getShowOfflineBuddies() || entryPresence != "offline") {
				if (groupname != null) {
					link = LinkFactory.createCustomLink(entry.getUser()+createAppendixFromGroupName(groupname), "cmd.chat", "", Link.NONTRANSLATED, vc, controller);
				} else {
					link = LinkFactory.createCustomLink(entry.getUser(), "cmd.chat", "", Link.NONTRANSLATED, vc, controller);
				}
				Identity ident = BaseSecurityManager.getInstance().findIdentityByName(entry.getName());
				if (ident != null) {
					link.setCustomDisplayText(ident.getUser().getProperty(UserConstants.FIRSTNAME, null)+" "+ident.getUser().getProperty(UserConstants.LASTNAME, null)+" ("+ident.getName()+")");
				} else {
					link.setCustomDisplayText(entry.getName());
				}
				link.setCustomEnabledLinkCSS("o_instantmessaging_" + entryPresence +"_icon");
				link.setUserObject(entry.getUser());
				StringBuilder sb = new StringBuilder();
				if (!imc.isChatDisabled()) {
					sb.append(translator.translate("im.status")).append(" ");
					sb.append(translator.translate("presence."+entryPresence));
					sb.append("<br />");
					if(ident != null){
						//TODO:gs:a how to get the roster entries presence msg? new clienthelper will work but creates a im client!
						sb.append(translator.translate("im.status.msg")).append(" ").append("");
					}
					sb.append("<br /><br />");
					sb.append(translator.translate("im.start.chat"));
				} else {
					sb.append(translator.translate("im.chat.disabled"));
				}
				link.setTooltip(sb.toString(), false);
				link.registerForMousePositionEvent(true);
				entries.add(entry.getUser());
			}
		}
		return entries;

	}

	public static String createAppendixFromGroupName(String groupname) {
		int groupAsInt = 0;
		char[] letters = groupname.toCharArray();
		for (int i = 0; i < letters.length; i++) {
			groupAsInt = groupAsInt + letters[i];
		}
		return String.valueOf(groupAsInt);
	}

	/**
	 * @param groupname
	 * @return online buddies for a certain group
	 */
	public String buddyCountOnlineForGroup(String groupname) {
		if (imc.isConnected()) {
			try {
				return imc.buddyCountOnlineForGroup(groupname);
			} catch (RuntimeException e) {
				logWarn("Error while trying to count buddies for group", e);
				return "(?/?)";
			}
		}
		return "(?/?)";
	}

	/**
	 * @param jid
	 * @return the presence
	 */
	public String getUserPresence(String jid) {
		try {
			return imc.getUserPresence(jid);
		} catch (RuntimeException e) {
			logWarn("Error while trying to get user presence. User: "+imc.getUsername(), e);
			return "";
		}
	}

	/**
	 * @param xmppAddressWithRessource
	 * @return a string like test@testserver.ch
	 */
	public String parseJid(String xmppAddressWithRessource) {
		return imc.parseJid(xmppAddressWithRessource);
	}

	/**
	 * @return true if the user is connected
	 */
	public boolean isConnected() {
		return imc.isConnected();
	}

	/**
	 * @return a number of the online users for a user
	 */
	public String buddyCountOnline() {
		if (imc.isConnected()) {
			try {
				return imc.buddyCountOnline();
			} catch (RuntimeException e) {
				logWarn("Error while trying to count buddies for group", e);
				return "(?/?)";
			}
		}
		return "(?/?)";
	}

	/**
	 * @param type
	 * @param status
	 * @param priority
	 * @param mode
	 */
	public void sendPresence(Type type, String status, int priority, Mode mode) {
		imc.sendPresence(type, status, priority, mode);
	}

	/**
	 * @return password
	 */
	public String getPassword() {
		return imc.getPassword();
	}

	/**
	 * @return status
	 */
	public String getStatus() {
		return imc.getStatus();
	}

	/**
	 * @return status message
	 */
	public String getStatusMsg() {
		return imc.getStatusMsg();
	}

	/**
	 * @return true if user likes seeing also his offline buddies in the roster
	 */
	public boolean getShowOfflineBuddies() {
		return imc.getShowOfflineBuddies();
	}

	/**
	 * @param showOfflineBuddies
	 */
	public void setShowOfflineBuddies(boolean showOfflineBuddies) {
		imc.setShowOfflineBuddies(showOfflineBuddies);
	}

	/**
	 * @return online time in minutes
	 */
	public String getOnlineTime() {
		return imc.getOnlineTime();
	}

	/**
	 * @return true if groups should be shown in roster
	 */
	public boolean isShowGroupsInRoster() {
		return imc.isShowGroupsInRoster();
	}

	/**
	 * @param showGroupsInRoster
	 */
	public void setShowGroupsInRoster(boolean showGroupsInRoster) {
		imc.setShowGroupsInRoster(showGroupsInRoster);
	}

	/**
	 * @return the users JID like test@testserver.ch
	 */
	public String getJid() {
		return imc.getJid();
	}


	public static String getSendDate(Message msg, Locale loc) {
		for (Iterator iter = msg.getExtensions().iterator(); iter.hasNext();) {
			PacketExtension extension = (PacketExtension) iter.next();
			if (extension.getNamespace().equals("jabber:x:delay")) {
				DelayInformation delayInfo = (DelayInformation) extension;
				Date date = delayInfo.getStamp();
				// why does formatter with this method return a time in the afternoon
				// like 03:24 instead of 15:24 like formatTime does??
				return Formatter.getInstance(loc).formatDateAndTime(date);
			}
		}
		// if no delay time now is returned
		// return Formatter.getInstance(locale).formatTime(new Date());
		Long receiveTime = (Long) msg.getProperty("receiveTime");
		Date d = new Date();
		d.setTime(receiveTime.longValue());
		return Formatter.getInstance(loc).formatTime(d);
	}

	/**
	 * send a presence packet "available" with a certain mode e.g. "away" to all
	 * buddies
	 * 
	 * @param mode
	 */
	public void sendPresenceAvailable(Presence.Mode mode) {
		imc.sendPresence(Presence.Type.available, null, 0, mode);
	}

	/**
	 * send a presence packet "unavailable" to all buddies
	 */
	public void sendPresenceUnavailable() {
		imc.sendPresence(Presence.Type.unavailable, null, 0, null);
	}
	
	/**
	 * 
	 * @return boolean
	 * true if user is allowed to chat and false during assessments when chat is disabled
	 */
	public boolean isChatDisabled() {
		return imc.isChatDisabled();
	}

}