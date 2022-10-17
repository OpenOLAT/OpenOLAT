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
package org.olat.modules.gotomeeting.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToRegistrant;
import org.olat.modules.gotomeeting.model.GoToRegistrantImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoToRegistrantDAO {
	
	@Autowired
	private DB dbInstance;
	
	
	public GoToRegistrant createRegistrant(GoToMeeting meeting, Identity identity,
			String registrantKey, String joinUrl, String confirmUrl) {
		GoToRegistrantImpl registrant = new GoToRegistrantImpl();
		registrant.setCreationDate(new Date());
		registrant.setLastModified(new Date());
		registrant.setIdentity(identity);
		registrant.setMeeting(meeting);
		registrant.setRegistrantKey(registrantKey);
		registrant.setJoinUrl(joinUrl);
		registrant.setConfirmUrl(confirmUrl);
		dbInstance.getCurrentEntityManager().persist(registrant);
		return registrant;
	}
	
	public GoToRegistrant getRegistrant(GoToMeeting meeting, IdentityRef identity) {
		String q = "select registrant from gotoregistrant registrant where registrant.meeting.key=:meetingKey and registrant.identity.key=:identityKey";
		
		List<GoToRegistrant> registrants = dbInstance.getCurrentEntityManager()
			.createQuery(q, GoToRegistrant.class)
			.setParameter("meetingKey", meeting.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return registrants == null || registrants.isEmpty() ? null : registrants.get(0);
	}
	
	public List<GoToRegistrant> getRegistrants(IdentityRef identity,
			RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder();
		sb.append("select registrant from gotoregistrant registrant")
		  .append(" inner join fetch registrant.meeting meeting")
		  .append(" where registrant.identity.key=:identityKey");
		if(entry != null) {
			sb.append(" and meeting.entry.key=:entryKey");
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and meeting.subIdent=:subIdent");
		}
		if(businessGroup != null) {
			sb.append(" and meeting.businessGroup.key=:groupKey");
		}
		
		TypedQuery<GoToRegistrant> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), GoToRegistrant.class)
			.setParameter("identityKey", identity.getKey());
		if(entry != null) {
			query.setParameter("entryKey", entry.getKey());
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		if(businessGroup != null) {
			query.setParameter("groupKey", businessGroup.getKey());
		}
		return query.getResultList();
	}

}
