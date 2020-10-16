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
package org.olat.modules.dcompensation.manager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.modules.dcompensation.model.DisadvantageCompensationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DisadvantageCompensationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public DisadvantageCompensation createDisadvantageCompensation(Identity identity,
			Integer extraTime, String approvedBy, Date approval,
			Identity creator, RepositoryEntry entry, String subIdent, String subIdentName) {
		DisadvantageCompensationImpl compensation = new DisadvantageCompensationImpl();
		compensation.setCreationDate(new Date());
		compensation.setLastModified(compensation.getCreationDate());
		compensation.setExtraTime(extraTime);
		compensation.setApproval(approval);
		compensation.setApprovedBy(approvedBy);
		compensation.setStatusEnum(DisadvantageCompensationStatusEnum.active);
		compensation.setIdentity(identity);
		compensation.setCreator(creator);
		compensation.setSubIdent(subIdent);
		compensation.setSubIdentName(subIdentName);
		compensation.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(compensation);
		return compensation;
	}
	
	public DisadvantageCompensation updateDisadvantageCompensation(DisadvantageCompensation compensation) {
		((DisadvantageCompensationImpl)compensation).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(compensation);
	}
	
	public List<DisadvantageCompensation> getDisadvantageCompensations(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select compensation from dcompensation as compensation")
		  .append(" inner join fetch compensation.creator as creator")
		  .append(" inner join fetch creator.user as userCreator")
		  .append(" inner join fetch compensation.entry as v")
		  .append(" where compensation.identity.key=:identityKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DisadvantageCompensation.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<DisadvantageCompensation> getDisadvantageCompensations(RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select compensation from dcompensation as compensation")
		  .append(" inner join fetch compensation.identity as ident")
		  .append(" where compensation.entry.key=:entryKey and compensation.subIdent=:subIdent");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DisadvantageCompensation.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	public List<DisadvantageCompensation> getDisadvantageCompensations(IdentityRef identity, RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select compensation from dcompensation as compensation")
		  .append(" inner join fetch compensation.identity as ident")
		  .append(" where compensation.entry.key=:entryKey and compensation.subIdent=:subIdent")
		  .append(" and compensation.identity.key=:identityKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DisadvantageCompensation.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	public DisadvantageCompensation getActiveDisadvantageCompensation(IdentityRef identity, RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select compensation from dcompensation as compensation")
		  .append(" where compensation.entry.key=:entryKey and compensation.subIdent=:subIdent")
		  .append(" and compensation.identity.key=:identityKey and compensation.status=:status");

		List<DisadvantageCompensation> compensations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DisadvantageCompensation.class)
				.setParameter("status", DisadvantageCompensationStatusEnum.active.name())
				.setParameter("identityKey", identity.getKey())
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return compensations == null || compensations.isEmpty() ? null : compensations.get(0);
	}
	
	public List<IdentityRef> getActiveDisadvantagedUsers(RepositoryEntryRef entry, List<String> subIdents) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select compensation.identity.key from dcompensation as compensation")
		  .append(" where compensation.entry.key=:entryKey and compensation.status=:status");
		if(subIdents != null && !subIdents.isEmpty()) {
			sb.append(" and compensation.subIdent in (:subIdent)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("status", DisadvantageCompensationStatusEnum.active.name())
				.setParameter("entryKey", entry.getKey());
		if(subIdents != null && !subIdents.isEmpty()) {
			query.setParameter("subIdent", subIdents);
		}
		
		List<Long> identityKeys = query.getResultList();
		return identityKeys.stream()
				.map(IdentityRefImpl::new)
				.collect(Collectors.toList());
	}
	
	public boolean isActiveDisadvantagedUser(IdentityRef identity, RepositoryEntryRef entry, List<String> subIdents) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select compensation.key from dcompensation as compensation")
		  .append(" where compensation.identity.key=:identityKey")
		  .append(" and compensation.entry.key=:entryKey and compensation.status=:status");
		if(subIdents != null && !subIdents.isEmpty()) {
			sb.append(" and compensation.subIdent in (:subIdent)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFirstResult(0)
				.setMaxResults(1)
				.setParameter("status", DisadvantageCompensationStatusEnum.active.name())
				.setParameter("identityKey", identity.getKey())
				.setParameter("entryKey", entry.getKey());
		if(subIdents != null && !subIdents.isEmpty()) {
			query.setParameter("subIdent", subIdents);
		}
		
		List<Long> keys = query.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
}
