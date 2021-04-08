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
package org.olat.modules.taxonomy.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyCompetenceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyCompetenceDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TaxonomyCompetence createTaxonomyCompetence(TaxonomyCompetenceTypes type, TaxonomyLevel taxonomyLevel, Identity identity,
			Date expiration, TaxonomyCompetenceLinkLocations linkLocation) {
		TaxonomyCompetenceImpl competence = new TaxonomyCompetenceImpl();
		competence.setCreationDate(new Date());
		competence.setLastModified(competence.getCreationDate());
		competence.setType(type.name());
		competence.setTaxonomyLevel(taxonomyLevel);
		competence.setIdentity(identity);
		competence.setExpiration(expiration);
		competence.setLinkLocation(linkLocation);
		dbInstance.getCurrentEntityManager().persist(competence);
		return competence;
	}
	
	public TaxonomyCompetence createTaxonomyCompetence(TaxonomyCompetenceTypes type, TaxonomyLevel taxonomyLevel, Identity identity,
			Date expiration) {
		return createTaxonomyCompetence(type, taxonomyLevel, identity, expiration, TaxonomyCompetenceLinkLocations.UNDEFINED);
	}
	
	public TaxonomyCompetence updateCompetence(TaxonomyCompetence competence) {
		((TaxonomyCompetenceImpl)competence).setLastModified(new Date());
		competence = dbInstance.getCurrentEntityManager().merge(competence);
		return competence;
	}
	
	public TaxonomyCompetence loadCompetenceByKey(Long key) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence from ctaxonomycompetence competence")
		  .append(" left join fetch competence.identity ident")
		  .append(" left join fetch competence.taxonomyLevel taxonomyLevel")
		  .append(" where competence.key=:competenceKey");
		
		List<TaxonomyCompetence> competences = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyCompetence.class)
			.setParameter("competenceKey", key)
			.getResultList();
		return competences == null || competences.isEmpty() ? null : competences.get(0);
	}
	
	public List<TaxonomyCompetence> getCompetenceByLevel(TaxonomyLevelRef taxonomyLevel) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence from ctaxonomycompetence competence")
		  .append(" inner join fetch competence.identity ident")
		  .append(" inner join fetch competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.key=:taxonomyLevelKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyCompetence.class)
			.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
			.getResultList();	
	}
	
	public List<TaxonomyCompetence> getCompetences(IdentityRef identity, TaxonomyCompetenceTypes... competenceTypes) {
		List<String> typeList = getTypesAsList(competenceTypes);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join fetch competence.taxonomyLevel taxonomyLevel")
		  .append(" inner join fetch taxonomyLevel.taxonomy taxonomy")
		  .append(" left join fetch taxonomyLevel.type taxonomyLevelType")
		  .append(" where ident.key=:identityKey");
		if(typeList.size() > 0) {
			sb.append(" and competence.type in (:types)");
		}
		
		TypedQuery<TaxonomyCompetence> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyCompetence.class)
			.setParameter("identityKey", identity.getKey());
		if(typeList.size() > 0) {
			query.setParameter("types", typeList);
		}
		return query.getResultList();
	}
	
	public List<TaxonomyCompetence> getCompetenceByLevel(TaxonomyLevelRef taxonomyLevel, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence from ctaxonomycompetence competence")
		  .append(" inner join fetch competence.identity ident")
		  .append(" inner join fetch competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.key=:taxonomyLevelKey and ident.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyCompetence.class)
			.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();	
	}
	
	public boolean hasCompetenceByLevel(TaxonomyLevelRef taxonomyLevel, IdentityRef identity, Date date,
			TaxonomyCompetenceTypes... competenceTypes) {
		List<String> typeList = getTypesAsList(competenceTypes);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(competence.key) from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.key=:taxonomyLevelKey and ident.key=:identityKey")
		  .append(" and (competence.expiration is null or competence.expiration>=:date)");
		if(typeList.size() > 0) {
			sb.append(" and competence.type in (:types)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setFlushMode(FlushModeType.COMMIT)//don't flush for this query
			.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
			.setParameter("identityKey", identity.getKey())
			.setParameter("date", CalendarUtils.removeTime(date));
		if(typeList.size() > 0) {
			query.setParameter("types", typeList);
		}
		Long count = query.getSingleResult();
		return count > 0;
	}
	
	public int countTaxonomyCompetences(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		if(taxonomyLevels == null || taxonomyLevels.isEmpty()) return 0;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(competence.key) from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.key in (:taxonomyLevelKeys)");
		
		List<Long> taxonomyLevelKeys = taxonomyLevels
				.stream()
				.map(l -> l.getKey())
				.collect(Collectors.toList());
		
		List<Number> counts = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
			.setParameter("taxonomyLevelKeys", taxonomyLevelKeys)
			.getResultList();	
		return counts != null && counts.size() == 1 && counts.get(0) != null ? counts.get(0).intValue() : 0;
	}
	
	public boolean hasCompetenceByTaxonomy(TaxonomyRef taxonomy, IdentityRef identity, Date date) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence.key from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join competence.taxonomyLevel taxonomyLevel")
		  .append(" inner join taxonomyLevel.taxonomy taxonomy")
		  .append(" where taxonomy.key=:taxonomyKey and ident.key=:identityKey")
		  .append(" and (competence.expiration is null or competence.expiration>=:date)");
		
		List<Long> competenceKeys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setFlushMode(FlushModeType.COMMIT)//don't flush for this query
			.setParameter("taxonomyKey", taxonomy.getKey())
			.setParameter("identityKey", identity.getKey())
			.setParameter("date", CalendarUtils.removeTime(date))
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return competenceKeys != null && competenceKeys.size() > 0
				&& competenceKeys.get(0) != null && competenceKeys.get(0).longValue() > 0;
	}

	public List<TaxonomyCompetence> getCompetencesByTaxonomy(TaxonomyRef taxonomy, IdentityRef identity, Date date) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join fetch competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.taxonomy.key=:taxonomyKey and ident.key=:identityKey")
		  .append(" and (competence.expiration is null or competence.expiration>=:date)");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaxonomyCompetence.class)
			.setFlushMode(FlushModeType.COMMIT)//don't flush for this query
			.setParameter("taxonomyKey", taxonomy.getKey())
			.setParameter("identityKey", identity.getKey())
			.setParameter("date", CalendarUtils.removeTime(date))
			.getResultList();
	}

	public List<TaxonomyCompetence> getCompetencesByTaxonomy(TaxonomyRef taxonomy, IdentityRef identity, Date date,
			TaxonomyCompetenceTypes... competenceTypes) {
		List<String> typeList = getTypesAsList(competenceTypes);


		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence from ctaxonomycompetence competence")
		  .append(" inner join competence.identity ident")
		  .append(" inner join fetch competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.taxonomy.key=:taxonomyKey and ident.key=:identityKey")
		  .append(" and (competence.expiration is null or competence.expiration>=:date)");
		if(typeList.size() > 0) {
			sb.append(" and competence.type in (:types)");
		}
		
		TypedQuery<TaxonomyCompetence> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyCompetence.class)
				.setFlushMode(FlushModeType.COMMIT)//don't flush for this query
				.setParameter("taxonomyKey", taxonomy.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("date", CalendarUtils.removeTime(date));
			if(typeList.size() > 0) {
				query.setParameter("types", typeList);
			}
			return query.getResultList();
	}
	
	public boolean hasCompetenceByTaxonomy(TaxonomyRef taxonomy, IdentityRef identity, Date date, TaxonomyCompetenceTypes... competences) {
		List<String> competenceList = new ArrayList<>(5);
		if(competences != null && competences.length > 0 && competences[0] != null) {
			for(TaxonomyCompetenceTypes competence:competences) {
				competenceList.add(competence.name());
			}
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select competence.key from ctaxonomycompetence competence")
		  .append(" inner join competence.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.taxonomy.key=:taxonomyKey and competence.identity.key=:identityKey")
		  .append(" and (competence.expiration is null or competence.expiration>=:date)");
		if(competenceList.size() > 0) {
			sb.append(" and competence.type in (:types)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("taxonomyKey", taxonomy.getKey())
			.setParameter("identityKey", identity.getKey())
			.setParameter("date", CalendarUtils.removeTime(date))
			.setFirstResult(0)
			.setMaxResults(1);
		if(competenceList.size() > 0) {
			query.setParameter("types", competenceList);
		}
		
		List<Long> keys = query.getResultList();
		return keys != null && keys.size() > 0 && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public int replace(TaxonomyLevel source, TaxonomyLevel target) {
		String q = "update ctaxonomycompetence competence set competence.taxonomyLevel.key=:targetLevelKey where competence.taxonomyLevel.key=:sourceLevelKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("sourceLevelKey", source.getKey())
				.setParameter("targetLevelKey", target.getKey())
				.executeUpdate();
	}
	
	/**
	 * Delete all competences hold by the specified taxonomy
	 * level.
	 * 
	 * @param level A taxonomy level
	 * @return Number of deleted rows
	 */
	public int deleteCompetences(TaxonomyLevelRef level) {
		String q = "delete from ctaxonomycompetence as competence where competence.taxonomyLevel.key=:levelKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("levelKey", level.getKey())
				.executeUpdate();
	}

	public int deleteCompetences(Identity identity) {
		String q = "delete from ctaxonomycompetence as competence where competence.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public void deleteCompetence(TaxonomyCompetence competence) {
		TaxonomyCompetence reloadedCompetence = dbInstance.getCurrentEntityManager()
			.getReference(TaxonomyCompetenceImpl.class, competence.getKey());
		if(reloadedCompetence != null) {// onnly if not already deleted
			dbInstance.getCurrentEntityManager().remove(reloadedCompetence);
		}
	}

	private List<String> getTypesAsList(TaxonomyCompetenceTypes... competenceTypes) {
		List<String> typeList = new ArrayList<>(4);
		if(competenceTypes != null && competenceTypes.length > 0 && competenceTypes[0] != null) {
			for(TaxonomyCompetenceTypes competenceType: competenceTypes) {
				if(competenceType != null) {
					typeList.add(competenceType.name());
				}
			}
		}
		return typeList;
	}

}
