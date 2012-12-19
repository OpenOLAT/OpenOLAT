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
	
	public RosterEntryImpl createRosterEntry(OLATResourceable chatResource, Identity from, String fullName, String nickName, boolean anonym) {
		RosterEntryImpl entry = new RosterEntryImpl();
		entry.setIdentityKey(from.getKey());
		entry.setNickName(nickName);
		entry.setFullName(fullName);
		entry.setAnonym(anonym);
		entry.setResourceTypeName(chatResource.getResourceableTypeName());
		entry.setResourceId(chatResource.getResourceableId());
		entry.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(entry);
		return entry;
	}

	public List<RosterEntryImpl> getRoster(OLATResourceable ores, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select entry from ").append(RosterEntryImpl.class.getName()).append(" entry ")
		  .append(" where entry.resourceId=:resid and entry.resourceTypeName=:resname");
		
		TypedQuery<RosterEntryImpl> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), RosterEntryImpl.class)
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public void deleteEntry(Identity identity, OLATResourceable ores) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(RosterEntryImpl.class.getName()).append(" entry ")
		  .append(" where entry.identityKey=:identityKey")
		  .append(" and entry.resourceId=:resid and entry.resourceTypeName=:resname");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("identityKey", identity.getKey())
				.setParameter("resid", ores.getResourceableId())
				.setParameter("resname", ores.getResourceableTypeName())
				.executeUpdate();
	}
}