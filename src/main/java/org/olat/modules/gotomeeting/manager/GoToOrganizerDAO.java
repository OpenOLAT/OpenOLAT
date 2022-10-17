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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.LockModeType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.model.GoToOrganizerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoToOrganizerDAO {
	
	@Autowired
	private DB dbInstance;
	
	/**
	 * 
	 * @param username
	 * @param accessToken
	 * @param organizerKey
	 * @param firstName (optional)
	 * @param lastName (optional)
	 * @param email (optional)
	 * @param accountKey (optional)
	 * @param expireIn (seconds)
	 * @param owner null for system wide organizer
	 * @return
	 */
	public GoToOrganizer createOrganizer(String name, String username, String accessToken, String refreshToken, String organizerKey,
			String firstName, String lastName, String email, String accountKey, Long expireIn, Identity owner) {
		GoToOrganizerImpl organizer = new GoToOrganizerImpl();
		organizer.setCreationDate(new Date());
		organizer.setLastModified(organizer.getCreationDate());
		organizer.setName(name);
		organizer.setOrganizerKey(organizerKey);
		organizer.setAccessToken(accessToken);
		organizer.setRefreshToken(refreshToken);
		organizer.setUsername(username);
		organizer.setFirstName(firstName);
		organizer.setLastName(lastName);
		organizer.setAccountKey(accountKey);
		organizer.setEmail(email);
		organizer.setOwner(owner);
		if(StringHelper.containsNonWhitespace(refreshToken)) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 30);
			organizer.setRenewRefreshDate(cal.getTime());
		}
		
		if(expireIn != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, expireIn.intValue());
			organizer.setRenewDate(cal.getTime());
		}

		dbInstance.getCurrentEntityManager().persist(organizer);
		return organizer;
	}
	
	public GoToOrganizer loadOrganizerByKey(Long key) {
		List<GoToOrganizer> organizers = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadOrganizerByKey", GoToOrganizer.class)
			.setParameter("key", key)
			.getResultList();
		return organizers == null || organizers.isEmpty() ? null : organizers.get(0);
	}
	
	public GoToOrganizer loadOrganizerForUpdate(GoToOrganizer organizer) {
		//first remove it from caches
		dbInstance.getCurrentEntityManager().detach(organizer);
		
		List<GoToOrganizer> organizers = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadOrganizerByKey", GoToOrganizer.class)
			.setParameter("key", organizer.getKey())
	  		.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.getResultList();
		return organizers == null || organizers.isEmpty() ? null : organizers.get(0);
	}
	
	public GoToOrganizer updateOrganizer(GoToOrganizer organizer, String name) {
		GoToOrganizerImpl organizerImpl = (GoToOrganizerImpl)organizer;
		organizerImpl.setName(name);
		return update(organizerImpl);
	}
	
	public GoToOrganizer updateOrganizer(GoToOrganizer organizer, String name, String accessToken, String refreshToken, String organizerKey,
			String firstName, String lastName, String email, String accountKey, Long expireIn) {
		GoToOrganizerImpl organizerImpl = (GoToOrganizerImpl)organizer;
		organizerImpl.setAccessToken(accessToken);
		organizerImpl.setRefreshToken(refreshToken);
		organizerImpl.setOrganizerKey(organizerKey);
		
		if(StringHelper.containsNonWhitespace(name)) {
			organizerImpl.setName(name);
		}
		if(StringHelper.containsNonWhitespace(firstName)) {
			organizerImpl.setFirstName(firstName);
		}
		if(StringHelper.containsNonWhitespace(lastName)) {
			organizerImpl.setLastName(lastName);
		}
		if(StringHelper.containsNonWhitespace(email)) {
			organizerImpl.setEmail(email);
		}
		if(StringHelper.containsNonWhitespace(accountKey)) {
			organizerImpl.setAccountKey(accountKey);
		}
		if(StringHelper.containsNonWhitespace(accountKey)) {
			organizerImpl.setAccountKey(accountKey);
		}
		if(expireIn != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, expireIn.intValue());
			organizerImpl.setRenewDate(cal.getTime());	
		}
		return update(organizerImpl);
	}
	
	public GoToOrganizer updateOrganizer(GoToOrganizer organizer, String accessToken, String refreshToken, Long expireIn) {
		GoToOrganizerImpl organizerImpl = (GoToOrganizerImpl)organizer;
		if(organizerImpl.getAccessToken() == null || !organizerImpl.getAccessToken().equals(accessToken)) {
			organizerImpl.setAccessToken(accessToken);
		}
		if(expireIn != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, expireIn.intValue());
			organizerImpl.setRenewDate(cal.getTime());	
		}
		
		if(organizerImpl.getRefreshToken() == null || !organizerImpl.getRefreshToken().equals(refreshToken)) {
			organizerImpl.setRefreshToken(refreshToken);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 30);
			organizerImpl.setRenewRefreshDate(cal.getTime());
		}
		organizerImpl.setLastModified(new Date());
		return update(organizerImpl);
	}
	
	private GoToOrganizer update(GoToOrganizer organizer) {
		GoToOrganizerImpl organizerImpl = (GoToOrganizerImpl)organizer;
		organizerImpl.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(organizerImpl);
	}
	
	public List<GoToOrganizer> getOrganizers() {
		String q = "select organizer from gotoorganizer organizer";
		return dbInstance.getCurrentEntityManager()
			.createQuery(q, GoToOrganizer.class)
			.getResultList();
	}
	
	public List<GoToOrganizer> getOrganizers(String accountKey, String organizerKey) {
		StringBuilder q = new StringBuilder();
		q.append("select organizer from gotoorganizer organizer")
		 .append(" where organizer.accountKey=:accountKey and organizer.organizerKey=:organizerKey");
		return dbInstance.getCurrentEntityManager()
			.createQuery(q.toString(), GoToOrganizer.class)
			.setParameter("accountKey", accountKey)
			.setParameter("organizerKey", organizerKey)
			.getResultList();
	}
	
	public List<GoToOrganizer> getOrganizersFor(IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("getSystemOrganizersAndMy", GoToOrganizer.class)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
	}
	
	public List<GoToOrganizer> getSystemOrganizersFor() {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("getSystemOrganizers", GoToOrganizer.class)
			.getResultList();
	}
	
	public void deleteOrganizer(GoToOrganizer organizer) {
		GoToOrganizer organizerRef = dbInstance.getCurrentEntityManager()
				.getReference(GoToOrganizerImpl.class, organizer.getKey());
		dbInstance.getCurrentEntityManager().remove(organizerRef);
	}

}
