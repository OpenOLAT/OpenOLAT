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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterEntryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 07.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class RosterDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RosterEntryImpl createRosterEntry(OLATResourceable chatResource, String resSubPath, String channel,
			Identity from, String fullName, String nickName,
			boolean anonym, boolean vip, boolean persistent, boolean active) {
		RosterEntryImpl entry = new RosterEntryImpl();
		entry.setIdentityKey(from.getKey());
		entry.setNickName(nickName);
		entry.setFullName(fullName);
		entry.setAnonym(anonym);
		entry.setVip(vip);
		entry.setResourceTypeName(chatResource.getResourceableTypeName());
		entry.setResourceId(chatResource.getResourceableId());
		entry.setResSubPath(resSubPath);
		entry.setChannel(channel);
		entry.setCreationDate(new Date());
		entry.setPersistent(persistent);
		entry.setActive(active);
		dbInstance.getCurrentEntityManager().persist(entry);
		return entry;
	}

	/**
	 * The method commit the transaction in case of a select for update
	 * @param chatResource
	 * @param identity
	 * @param fullName
	 * @param nickName
	 * @param anonym
	 * @param vip
	 */
	public void updateRosterEntry(OLATResourceable chatResource, String resSubPath, String channel,
			Identity identity, String fullName, String nickName, boolean anonym, boolean vip, boolean persistent, boolean active) {
		RosterEntry entry = load(chatResource, resSubPath, channel, identity);
		if(entry == null) {
			createRosterEntry(chatResource, resSubPath, channel, identity, fullName, nickName, anonym, vip, persistent, active);
		} else {
			if(entry.isAnonym() == anonym
				&& ((fullName == null && entry.getFullName() == null) || (fullName != null && fullName.equals(entry.getFullName())))
				&& ((nickName == null && entry.getNickName() == null) || (nickName != null && nickName.equals(entry.getNickName())))
				&& entry.isActive() == active) {
				return;
			}
			
			RosterEntryImpl reloadedEntry = loadForUpdate(entry);
			if(reloadedEntry != null) {
				reloadedEntry.setFullName(fullName);
				reloadedEntry.setNickName(nickName);
				reloadedEntry.setAnonym(anonym);
				reloadedEntry.setActive(active);
				dbInstance.getCurrentEntityManager().merge(reloadedEntry);
			}
			dbInstance.commit();
		}
	}
	
	public void updateLastSeen(Identity identity, OLATResourceable chatResource, String resSubPath, String channel) {
		RosterEntryImpl entry = load(chatResource, resSubPath, channel, identity);
		if(entry != null) {
			entry.setLastSeen(new Date());
			dbInstance.getCurrentEntityManager().merge(entry);
			dbInstance.commit();
		}
	}
	
	private RosterEntryImpl load(OLATResourceable ores, String resSubPath, String channel, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select entry from imrosterentry entry")
		  .where().append(" entry.identityKey=:identityKey and entry.resourceId=:resid and entry.resourceTypeName=:resname");
		if(resSubPath == null) {
			sb.and().append(" entry.resSubPath is null");
		} else {
			sb.and().append(" entry.resSubPath=:ressubPath");
		}
		if(channel == null) {
			sb.and().append(" entry.channel is null");
		} else {
			sb.and().append(" entry.channel=:channel");
		}		
		
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RosterEntryImpl.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setParameter("identityKey", identity.getKey());
		if(resSubPath != null) {
			query.setParameter("ressubPath", resSubPath);
		}
		if(channel != null) {
			query.setParameter("channel", channel);
		}
		List<RosterEntryImpl> entries = query.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	private RosterEntryImpl loadForUpdate(RosterEntry rosterEntry) {
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterEntryForUpdate", RosterEntryImpl.class)
				.setParameter("entryKey", rosterEntry.getKey());
		List<RosterEntryImpl> entries = query.getResultList();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}
	
	/**
	 * 
	 * @param ores The resource
	 * @param resSubPath The resource sub-path, can be null
	 * @param firstResult The first result to search for
	 * @param maxResults 
	 * @return
	 */
	public List<RosterEntry> getRoster(OLATResourceable ores, String resSubPath, String channel) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select entry from imrosterentry entry")
		  .where().append(" entry.resourceId=:resid and entry.resourceTypeName=:resname");
		if(resSubPath == null) {
			sb.and().append(" entry.resSubPath is null");
		} else {
			sb.and().append(" entry.resSubPath=:ressubPath");
		}
		if(channel == null) {
			sb.and().append(" entry.channel is null");
		} else {
			sb.and().append(" entry.channel=:channel");
		}
		
		TypedQuery<RosterEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RosterEntry.class)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName());
		if(resSubPath != null) {
			query.setParameter("ressubPath", resSubPath);
		}
		if(channel != null) {
			query.setParameter("channel", channel);
		}
		return query.getResultList();
	}
	
	/**
	 * 
	 * @param ores The resource
	 * @param resSubPath The resource sub-path (check null value)
	 * @param channel The channel (optional)
	 * @param identity The identity which search
	 * @param onlyMyActiveRosters
	 * @return
	 */
	public List<RosterChannelInfos> getRosterAroundChannels(OLATResourceable ores, String resSubPath, String channel, IdentityRef identity, boolean onlyMyActiveRosters) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select entry, lastMessage, lastStatusMessage,")
		  .append(" (select count(unread.key) from instantmessage unread")
		  .append("  where entry.resourceTypeName=unread.resourceTypeName and entry.resourceId=unread.resourceId")
		  .append("  and ((entry.resSubPath is null and unread.resSubPath is null) or (entry.resSubPath=unread.resSubPath))")
		  .append("  and ((entry.channel is null and unread.channel is null) or (entry.channel=unread.channel))")
		  .append("  and unread.type ").in(InstantMessageTypeEnum.text, InstantMessageTypeEnum.request)
		  .append("  and unread.creationDate > entry.lastSeen and entry.identityKey<>unread.fromKey")
		  .append(" ) as unreadMsgs")
		  .append(" from imrosterentry entry")
		  .append(" left join instantmessage lastMessage on (entry.resourceTypeName=lastMessage.resourceTypeName and entry.resourceId=lastMessage.resourceId")
		  .append("  and ((entry.resSubPath is null and lastMessage.resSubPath is null) or (entry.resSubPath=lastMessage.resSubPath))")
		  .append("  and ((entry.channel is null and lastMessage.channel is null) or (entry.channel=lastMessage.channel))")
		  .append("  and lastMessage.type ").in(InstantMessageTypeEnum.text, InstantMessageTypeEnum.request)
		  .append(" )")
		  .append(" left join instantmessage lastStatusMessage on (entry.resourceTypeName=lastStatusMessage.resourceTypeName and entry.resourceId=lastStatusMessage.resourceId")
		  .append("  and ((entry.resSubPath is null and lastStatusMessage.resSubPath is null) or (entry.resSubPath=lastStatusMessage.resSubPath))")
		  .append("  and ((entry.channel is null and lastStatusMessage.channel is null) or (entry.channel=lastStatusMessage.channel))")
		  .append("  and lastStatusMessage.type ").in(InstantMessageTypeEnum.accept, InstantMessageTypeEnum.join, InstantMessageTypeEnum.close, InstantMessageTypeEnum.end)
		  .append(" )")
		  
		  .where().append(" entry.resourceId=:resid and entry.resourceTypeName=:resname");
		if(resSubPath == null) {
			sb.and().append(" entry.resSubPath is null");
		} else {
			sb.and().append(" entry.resSubPath=:ressubPath");
		}
		if(StringHelper.containsNonWhitespace(channel)) {
			sb.and().append(" entry.channel=:channel");
		}
		// limit to last message
		sb.and().append("(lastMessage.creationDate is null or lastMessage.creationDate = (select max(msg.creationDate) from instantmessage msg where")
		  .append(" entry.resourceTypeName=msg.resourceTypeName and entry.resourceId=msg.resourceId")
		  .append(" and ((entry.resSubPath is null and msg.resSubPath is null) or (entry.resSubPath=msg.resSubPath))")
		  .append(" and ((entry.channel is null and msg.channel is null) or (entry.channel=msg.channel))")
		  .append(" and msg.type ").in(InstantMessageTypeEnum.text, InstantMessageTypeEnum.request)
		  .append("))");
		// limit to last status
		sb.and().append("(lastStatusMessage.creationDate is null or lastStatusMessage.creationDate = (select max(status.creationDate) from instantmessage status where")
		  .append(" entry.resourceTypeName=status.resourceTypeName and entry.resourceId=status.resourceId")
		  .append(" and ((entry.resSubPath is null and status.resSubPath is null) or (entry.resSubPath=status.resSubPath))")
		  .append(" and ((entry.channel is null and status.channel is null) or (entry.channel=status.channel))")
		  .append(" and status.type ").in(InstantMessageTypeEnum.accept, InstantMessageTypeEnum.join, InstantMessageTypeEnum.close, InstantMessageTypeEnum.end)
		  .append("))");
		
		// order by channel
		sb.append(" order by entry.channel asc nulls last");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName());
		if(resSubPath != null) {
			query.setParameter("ressubPath", resSubPath);
		}
		if(StringHelper.containsNonWhitespace(channel)) {
			query.setParameter("channel", channel);
		}
		List<Object[]> rawObjects = query.getResultList();
		List<RosterChannelInfos> entries = new ArrayList<>(rawObjects.size());

		RosterChannelInfos currentInfos = null;
		for(Object[] objects:rawObjects) {
			RosterEntry entry = (RosterEntry)objects[0];
			InstantMessage lastMessage = (InstantMessage)objects[1];
			InstantMessage lastStatusMessage = (InstantMessage)objects[2];
			if(currentInfos == null || !Objects.equals(currentInfos.getChannel(), entry.getChannel())) {
				currentInfos = new RosterChannelInfos(entry.getChannel(), entry, lastMessage, lastStatusMessage);
				entries.add(currentInfos);
			} else {
				currentInfos.getEntries().add(entry);
			}
			
			if(identity.getKey().equals(entry.getIdentityKey())) {
				Long unread = PersistenceHelper.extractLong(objects, 3);
				currentInfos.setUnreadMessages(unread);
			}
		}
		
		// post query filtering, every object need all rosters informations
		if(onlyMyActiveRosters) {
			entries = entries.stream()
					.filter(entry -> entry.inRoster(identity))
					.collect(Collectors.toList());
		}

		return entries;
	}
	
	public void inactivateEntry(IdentityRef identity, OLATResourceable ores, String resSubPath, String channel) {
		RosterEntryImpl entry = load(ores, resSubPath, channel, identity);
		if(entry != null) {
			if(entry.isPersistent()) {
				entry.setActive(false);
				dbInstance.getCurrentEntityManager().merge(entry);
			} else {
				dbInstance.getCurrentEntityManager().remove(entry);
			}
			dbInstance.commit();
		}		
	}
	
	public void deleteEntry(IdentityRef identity) {
		String del = "delete from imrosterentry entry where entry.identityKey=:identityKey";
		dbInstance.getCurrentEntityManager().createQuery(del)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	

	public void deleteVIPEntries(OLATResourceable ores, String resSubPath, String channel) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from imrosterentry entry ")
		  .where().append(" entry.vip=true and entry.resourceId=:resid and entry.resourceTypeName=:resname");
		if(resSubPath == null) {
			sb.and().append(" entry.resSubPath is null");
		} else {
			sb.and().append(" entry.resSubPath=:ressubPath");
		}
		if(channel == null) {
			sb.and().append(" entry.channel is null");
		} else {
			sb.and().append(" entry.channel=:channel");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName());
		if(resSubPath != null) {
			query.setParameter("ressubPath", resSubPath);
		}
		if(channel != null) {
			query.setParameter("channel", channel);
		}
		query.executeUpdate();
	}
	
	public static class RosterEntryAndMessage {
		
		private final RosterEntry rosterEntry;
		private final InstantMessage message;
		
		public RosterEntryAndMessage(RosterEntry rosterEntry, InstantMessage message) {
			this.rosterEntry = rosterEntry;
			this.message = message;
		}
		
		public String getChannel() {
			return rosterEntry.getChannel();
		}

		public RosterEntry getRosterEntry() {
			return rosterEntry;
		}

		public InstantMessage getMessage() {
			return message;
		}
	}
}