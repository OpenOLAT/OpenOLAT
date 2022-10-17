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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.model.ImPreferencesImpl;
import org.olat.instantMessaging.model.Presence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstantMessagePreferencesDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ImPreferencesImpl createPreferences(Identity identity, String status, boolean visible) {
		ImPreferencesImpl msg = new ImPreferencesImpl();
		msg.setCreationDate(new Date());
		msg.setIdentity(identity);
		msg.setVisibleToOthers(visible);
		msg.setRosterDefaultStatus(status);
		dbInstance.getCurrentEntityManager().persist(msg);
		return msg;
	}
	
	public String getStatus(Long identityKey) {
		List<String> msgs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMRosterStatusByIdentity", String.class)
				.setParameter("identityKey", identityKey)
				.getResultList();
		
		if(msgs.isEmpty()) {
			return null;
		}
		return msgs.get(0);
	}
	
	public int countAvailableBuddies(List<Long> buddies) {
		if(buddies == null || buddies.isEmpty()) {
			return 0;
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("countAvailableBuddiesIn", Number.class);

		int hibernateInBatch = 500;
		int firstResult = 0;
		int total = 0;
		do {
			int toIndex = Math.min(firstResult + hibernateInBatch, buddies.size());
			List<Long> inParameter = buddies.subList(firstResult, toIndex);
			query.setParameter("buddyKeys", inParameter);
			firstResult += inParameter.size();
			
			Number count = query.getSingleResult();
			total += count.intValue();
		} while(firstResult < buddies.size());

		return total;
	}
	
	public Map<Long,String> getBuddyStatus(List<Long> buddies) {
		if(buddies == null || buddies.isEmpty()) {
			return new HashMap<>();
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createNamedQuery("mapStatusByBuddiesIn", Object[].class);

		int hibernateInBatch = 250;
		int firstResult = 0;
		Map<Long,String> statusMap = new HashMap<>();
		do {
			int toIndex = Math.min(firstResult + hibernateInBatch, buddies.size());
			List<Long> inParameter = buddies.subList(firstResult, toIndex);
			query.setParameter("buddyKeys", inParameter);
			firstResult += inParameter.size();
			
			List<Object[]> statusList = query.getResultList();
			for(Object[] status:statusList) {
				Long identityKey = (Long)status[0];
				String state = (String)status[1];
				statusMap.put(identityKey, state);
			}
		} while(firstResult < buddies.size());

		return statusMap;
	}

	/**
	 * Synchronized to prevent
	 * @param identity
	 * @return
	 */
	public synchronized ImPreferencesImpl getPreferences(Identity identity) {
		List<ImPreferencesImpl> msgs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMPreferencesByIdentity", ImPreferencesImpl.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		
		if(msgs.isEmpty()) {
			return createPreferences(identity, Presence.available.name(), true);
		}
		return msgs.get(0);
	}
	
	public void updatePreferences(Identity identity, String status) {
		int updateRows = dbInstance.getCurrentEntityManager()
				.createNamedQuery("updateIMPreferencesStatusByIdentity")
				.setParameter("identityKey", identity.getKey())
				.setParameter("status", status)
				.executeUpdate();
		if(updateRows == 0) {
			createPreferences(identity, status, true);
		}
	}
	
	public void updatePreferences(Identity identity, boolean visible) {
		int updateRows = dbInstance.getCurrentEntityManager()
				.createNamedQuery("updateIMPreferencesVisibilityByIdentity")
				.setParameter("identityKey", identity.getKey())
				.setParameter("visible", visible)
				.executeUpdate();
		if(updateRows == 0) {
			createPreferences(identity, Presence.available.name(), visible);
		}
	}
	
	public void deletePreferences(IdentityRef identity) {
		List<ImPreferencesImpl> prefs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMPreferencesByIdentity", ImPreferencesImpl.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		for(ImPreferencesImpl pref:prefs) {
			dbInstance.getCurrentEntityManager().remove(pref);
		}
	}
}
