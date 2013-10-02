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

	public void updateRosterEntry(OLATResourceable chatResource, Identity identity, String fullName, String nickName,
			boolean anonym, boolean vip) {
		RosterEntryImpl entry = loadForUpdate(chatResource, identity);
		if(entry == null) {
			createRosterEntry(chatResource, identity, fullName, nickName, anonym, vip);
		} else {
			entry.setFullName(fullName);
			entry.setNickName(nickName);
			entry.setAnonym(anonym);
			dbInstance.getCurrentEntityManager().merge(entry);
		}
	}
	
	private RosterEntryImpl loadForUpdate(OLATResourceable ores, Identity identity) {
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterEntryForUpdate", RosterEntryImpl.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setParameter("identityKey", identity.getKey());
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
	
	public void deleteEntry(Identity identity, OLATResourceable ores) {
		dbInstance.getCurrentEntityManager().createNamedQuery("deleteIMRosterEntryByIdentityAndResource")
				.setParameter("identityKey", identity.getKey())
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.executeUpdate();
	}
}