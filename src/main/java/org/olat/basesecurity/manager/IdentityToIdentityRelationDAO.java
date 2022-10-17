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
package org.olat.basesecurity.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationSearchParams;
import org.olat.basesecurity.model.IdentityToIdentityRelationImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class IdentityToIdentityRelationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public IdentityToIdentityRelation createRelation(Identity source, Identity target, RelationRole role,
			String externalId, String managedFlagsString) {
		IdentityToIdentityRelationImpl relation = new IdentityToIdentityRelationImpl();
		relation.setCreationDate(new Date());
		relation.setExternalId(externalId);
		relation.setManagedFlagsString(managedFlagsString);
		relation.setSource(source);
		relation.setTarget(target);
		relation.setRole(role);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}
	
	public boolean isUsed(RelationRole relationRole) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select identRel.key from identitytoidentity as identRel")
		  .append(" where identRel.role.key=:roleKey");
		
		List<Long> usages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("roleKey", relationRole.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return usages != null && !usages.isEmpty() && usages.get(0) != null;
	}
	
	public boolean hasRelation(IdentityRef source, IdentityRef target, RelationRole relationRole) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select identRel.key from identitytoidentity as identRel")
		  .append(" where identRel.role.key=:roleKey and identRel.source.key=:sourceKey and identRel.target.key=:targetKey");
		
		List<Long> usages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("roleKey", relationRole.getKey())
			.setParameter("sourceKey", source.getKey())
			.setParameter("targetKey", target.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return usages != null && !usages.isEmpty() && usages.get(0) != null;
	}
	
	public IdentityToIdentityRelation getRelation(IdentityRef source, IdentityRef target, RelationRole relationRole) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select identRel from identitytoidentity as identRel")
		  .append(" where identRel.role.key=:roleKey and identRel.source.key=:sourceKey and identRel.target.key=:targetKey");
		
		List<IdentityToIdentityRelation> usages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), IdentityToIdentityRelation.class)
			.setParameter("roleKey", relationRole.getKey())
			.setParameter("sourceKey", source.getKey())
			.setParameter("targetKey", target.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return usages != null && !usages.isEmpty() ? usages.get(0) : null;
	}
	
	public IdentityToIdentityRelation getRelation(Long relationKey) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select identRel from identitytoidentity as identRel")
		  .append(" inner join fetch identRel.role as relRol")
		  .append(" inner join fetch identRel.target as identTarget")
		  .append(" inner join fetch identTarget.user as userTarget")
		  .append(" inner join fetch identRel.source as identSource")
		  .append(" inner join fetch identSource.user as userSource")
		  .append(" where identRel.key=:relationKey");
		
		List<IdentityToIdentityRelation> usages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), IdentityToIdentityRelation.class)
			.setParameter("relationKey", relationKey)
			.getResultList();
		return usages != null && !usages.isEmpty() ? usages.get(0) : null;
	}
	
	public List<IdentityToIdentityRelation> getRelationsAsSource(IdentityRef source) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select identRel from identitytoidentity as identRel")
		  .append(" inner join fetch identRel.role as relRol")
		  .append(" inner join fetch identRel.target as identTarget")
		  .append(" inner join fetch identTarget.user as userTarget")
		  .append(" where identRel.source.key=:sourceKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityToIdentityRelation.class)
				.setParameter("sourceKey", source.getKey())
				.getResultList();
	}
	
	public List<IdentityToIdentityRelation> getRelationsAsTarget(IdentityRef target, RelationSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select identRel from identitytoidentity as identRel")
		  .append(" inner join fetch identRel.role as relRol")
		  .append(" inner join fetch identRel.source as identSource")
		  .append(" inner join fetch identSource.user as userSource");
		if (searchParams.getRight() != null) {
			sb.append(" inner join relationroletoright roleToRight");
			sb.append("    on roleToRight.role.key = relRol.key");
		}
		sb.and().append("identRel.target.key=:targetKey");
		if (searchParams.getRight() != null) {
			sb.and().append("roleToRight.right.key = :rightKey");
		}
		
		TypedQuery<IdentityToIdentityRelation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityToIdentityRelation.class)
				.setParameter("targetKey", target.getKey());
		if (searchParams.getRight() != null) {
			query.setParameter("rightKey", searchParams.getRight().getKey());
		}
		return query.getResultList();
	}
	
	public List<Identity> getSources(RelationRole role) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select distinct identSource from identitytoidentity as identRel")
		  .append(" inner join identRel.source as identSource")
		  .append(" inner join fetch identSource.user as userSource")
		  .append(" where identRel.role.key=:roleKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("roleKey", role.getKey())
				.getResultList();
	}
	
	public List<Identity> getTargets(RelationRole role) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select distinct identTarget from identitytoidentity as identRel")
		  .append(" inner join identRel.target as identTarget")
		  .append(" inner join fetch identTarget.user as userTarget")
		  .append(" where identRel.role.key=:roleKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("roleKey", role.getKey())
				.getResultList();
	}
	
	public void removeRelation(IdentityToIdentityRelation relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}

}
