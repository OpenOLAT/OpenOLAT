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
package org.olat.course.disclaimer.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 
 * Date: 24 Mar 2020<br>
 * @author Alexander Boeckle
 */
@Service
public class CourseDisclaimerDAO {
	
	@Autowired
	private DB dbInstance;
	
	public void revokeAllConsents(RepositoryEntryRef repositoryEntryRef) {
		StringBuilder sb = new StringBuilder();

		sb.append("update coursedisclaimerconsent as consent")
		.append(" set consent.disc1Accepted=false, consent.disc2Accepted=false, lastModified=:now")
		.append(" where consent.repositoryEntry.key=:repoEntryKey");

		dbInstance.getCurrentEntityManager()
		.createQuery(sb.toString())
		.setParameter("repoEntryKey", repositoryEntryRef.getKey())
		.setParameter("now", new Date())
		.executeUpdate();
	}
	
	public void removeAllConsents(RepositoryEntryRef repositoryEntryRef) {
		StringBuilder sb = new StringBuilder();

		sb.append("delete coursedisclaimerconsent as consent")
		.append(" where consent.repositoryEntry.key=:repoEntryKey");

		dbInstance.getCurrentEntityManager()
		.createQuery(sb.toString())
		.setParameter("repoEntryKey", repositoryEntryRef.getKey())
		.executeUpdate();
	}
	
	public List<CourseDisclaimerConsent> getConsents(RepositoryEntryRef repositoryEntryRef) {
		StringBuilder sb = new StringBuilder();

		sb.append("select consent from coursedisclaimerconsent as consent")
		.append(" inner join fetch consent.identity as identity")
		.append(" inner join fetch identity.user as user")
		.append(" where consent.repositoryEntry.key=:repoEntryKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CourseDisclaimerConsent.class)
				.setParameter("repoEntryKey", repositoryEntryRef.getKey())
				.getResultList();
	}
	
	public CourseDisclaimerConsent getCourseDisclaimerConsent(RepositoryEntryRef repositoryEntryRef, IdentityRef identitiyRef) {
		StringBuilder sb = new StringBuilder();

		sb.append("select consent from coursedisclaimerconsent as consent")
		.append(" where consent.repositoryEntry.key=:repoEntryKey")
		.append(" and consent.identity.key=:identityKey");

		List<CourseDisclaimerConsent> consents = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CourseDisclaimerConsent.class)
				.setParameter("repoEntryKey", repositoryEntryRef.getKey())
				.setParameter("identityKey", identitiyRef.getKey())
				.getResultList();
		
		if (consents.isEmpty()) {
			return null;
		} else {
			return consents.get(0);
		}
	}
	
	public void removeConsents(RepositoryEntryRef repositoryEntryRef, List<Long> identityKeys) {
		StringBuilder sb = new StringBuilder();

		sb.append("delete from coursedisclaimerconsent as consent")
		.append(" where consent.repositoryEntry.key=:repoEntryKey")
		.append(" and consent.identity.key in :identityKeyList");

		dbInstance.getCurrentEntityManager()
		.createQuery(sb.toString())
		.setParameter("repoEntryKey", repositoryEntryRef.getKey())
		.setParameter("identityKeyList", identityKeys)
		.executeUpdate();
	}
	
	public void revokeConsents(RepositoryEntryRef repositoryEntryRef, List<Long> identityKeys) {
		StringBuilder sb = new StringBuilder();

		sb.append("update coursedisclaimerconsent as consent")
		.append(" set consent.disc1Accepted=false, consent.disc2Accepted=false, lastModified=:now")
		.append(" where consent.repositoryEntry.key=:repoEntryKey")
		.append(" and consent.identity.key in :identityKeyList");

		dbInstance.getCurrentEntityManager()
		.createQuery(sb.toString())
		.setParameter("repoEntryKey", repositoryEntryRef.getKey())
		.setParameter("identityKeyList", identityKeys)
		.setParameter("now", new Date())
		.executeUpdate();
	}
	
	public Long countConsents(RepositoryEntryRef repositoryEntryRef) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select count(consent.key) from coursedisclaimerconsent as consent")
		.append(" where consent.repositoryEntry.key=:repoEntryKey");
		
		List<Long> count = dbInstance.getCurrentEntityManager()
		.createQuery(sb.toString(), Long.class)
		.setParameter("repoEntryKey", repositoryEntryRef.getKey())
		.getResultList();
		
		return count.isEmpty() ? Long.valueOf(0l) : count.get(0);
	}
}
