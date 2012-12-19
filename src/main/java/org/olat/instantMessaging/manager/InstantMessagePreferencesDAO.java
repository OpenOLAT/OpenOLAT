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

import javax.persistence.LockModeType;

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
		StringBuilder sb = new StringBuilder();
		sb.append("select msg.rosterDefaultStatus from ").append(ImPreferencesImpl.class.getName()).append(" msg ")
		  .append(" where msg.identity.key=:identityKey");
		
		List<String> msgs = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identityKey)
				.getResultList();
		
		if(msgs.isEmpty()) {
			return null;
		}
		return msgs.get(0);
	}

	/**
	 * Synchronized to prevent
	 * @param identity
	 * @return
	 */
	public synchronized ImPreferencesImpl getPreferences(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from ").append(ImPreferencesImpl.class.getName()).append(" msg ")
		  .append(" where msg.identity.key=:identityKey");
		
		List<ImPreferencesImpl> msgs = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ImPreferencesImpl.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		
		if(msgs.isEmpty()) {
			return createPreferences(identity, Presence.available.name(), true);
		}
		return msgs.get(0);
	}
	
	public ImPreferencesImpl updatePreferences(Identity identity, String status) {
		ImPreferencesImpl prefs = loadForUpdate(identity);
		if(prefs == null) {
			prefs = createPreferences(identity, status, true);
		} else {
			prefs.setRosterDefaultStatus(status);
			prefs = dbInstance.getCurrentEntityManager().merge(prefs);
		}
		return prefs;
	}
	
	public ImPreferencesImpl updatePreferences(Identity identity, boolean visible) {
		ImPreferencesImpl prefs = loadForUpdate(identity);
		if(prefs == null) {
			prefs = createPreferences(identity, Presence.available.name(), visible);
		} else {
			prefs.setVisibleToOthers(visible);
			prefs = dbInstance.getCurrentEntityManager().merge(prefs);
		}
		return prefs;
	}
	
	private ImPreferencesImpl loadForUpdate(Identity from) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from ").append(ImPreferencesImpl.class.getName()).append(" msg ")
		  .append(" where msg.identity.key=:identityKey");
		
		List<ImPreferencesImpl> msgs = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ImPreferencesImpl.class)
				.setParameter("identityKey", from.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		
		if(msgs.isEmpty()) {
			return null;
		}
		return msgs.get(0);
	}
}
