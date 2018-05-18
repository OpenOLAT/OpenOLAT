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

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.instantMessaging.model.RosterEntryImpl;
import org.olat.instantMessaging.model.RosterEntryView;
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
	
	public RosterEntryImpl createRosterEntry(OLATResourceable chatResource, Identity from, String fullName, String nickName,
			boolean anonym, boolean vip) {
		
		RosterEntryImpl entry = new RosterEntryImpl();
		entry.setIdentityKey(from.getKey());
		entry.setNickName(nickName);
		entry.setFullName(fullName);
		entry.setAnonym(anonym);
		entry.setVip(vip);
		entry.setResourceTypeName(chatResource.getResourceableTypeName());
		entry.setResourceId(chatResource.getResourceableId());
		entry.setCreationDate(new Date());
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
	public void updateRosterEntry(OLATResourceable chatResource, Identity identity, String fullName, String nickName,
			boolean anonym, boolean vip) {
		RosterEntryImpl entry = load(chatResource, identity);
		if(entry == null) {
			createRosterEntry(chatResource, identity, fullName, nickName, anonym, vip);
		} else {
			if(entry.isAnonym() == anonym
				&& ((fullName == null && entry.getFullName() == null) || (fullName != null && fullName.equals(entry.getFullName())))
				&& ((nickName == null && entry.getNickName() == null) || (nickName != null && nickName.equals(entry.getNickName())))) {
				return;
			}
			
			RosterEntryImpl reloadedEntry = loadForUpdate(entry);
			reloadedEntry.setFullName(fullName);
			reloadedEntry.setNickName(nickName);
			reloadedEntry.setAnonym(anonym);
			dbInstance.getCurrentEntityManager().merge(reloadedEntry);
			dbInstance.commit();
		}
	}
	
	private RosterEntryImpl load(OLATResourceable ores, Identity identity) {
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterEntry", RosterEntryImpl.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setParameter("identityKey", identity.getKey());
		List<RosterEntryImpl> entries = query.getResultList();
		if(entries.size() > 0) {
			return entries.get(0);
		}
		return null;
	}
	
	private RosterEntryImpl loadForUpdate(RosterEntryImpl rosterEntry) {
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterEntryForUpdate", RosterEntryImpl.class)
				.setParameter("entryKey", rosterEntry.getKey());
		List<RosterEntryImpl> entries = query.getResultList();
		if(entries.size() > 0) {
			return entries.get(0);
		}
		return null;
	}
	
	public List<RosterEntryImpl> getRoster(OLATResourceable ores, int firstResult, int maxResults) {
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterEntryByResource", RosterEntryImpl.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setFirstResult(firstResult)
				.setHint("org.hibernate.cacheable", Boolean.TRUE);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<RosterEntryView> getRosterView(OLATResourceable ores, int firstResult, int maxResults) {
		TypedQuery<RosterEntryView> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterEntryViewByResource", RosterEntryView.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public void deleteEntry(IdentityRef identity, OLATResourceable ores) {
		String del = "delete from imrosterentry entry where entry.identityKey=:identityKey and entry.resourceId=:resid and entry.resourceTypeName=:resname";
		dbInstance.getCurrentEntityManager().createQuery(del)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.executeUpdate();
	}
	
	public void deleteEntry(IdentityRef identity) {
		String del = "delete from imrosterentry entry where entry.identityKey=:identityKey";
		dbInstance.getCurrentEntityManager().createQuery(del)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
}