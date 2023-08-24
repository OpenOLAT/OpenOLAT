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
package org.olat.modules.ceditor.manager;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageToTaxonomyCompetence;
import org.olat.modules.ceditor.model.jpa.PageToTaxonomyCompetenceImpl;
import org.olat.modules.portfolio.Section;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 11.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class PageToTaxonomyCompetenceDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PageToTaxonomyCompetence createRelation(Page page, TaxonomyCompetence taxonomyCompetence) {
		
		PageToTaxonomyCompetenceImpl relation = new PageToTaxonomyCompetenceImpl();
		
		relation.setCreationDate(new Date());
		relation.setPage(page);
		relation.setTaxonomyCompetence(taxonomyCompetence);
		
		dbInstance.getCurrentEntityManager().persist(relation);
		
		return relation;
	}
	
	public List<TaxonomyCompetence> getCompetencesToPage(Page page, boolean fetchTaxonomies) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select competence from cepagetotaxonomycompetence rel")
		  .append(" inner join rel.taxonomyCompetence as competence");
		
		if (fetchTaxonomies) {
			sb.append(" inner join fetch competence.taxonomyLevel level");
			sb.append(" inner join fetch level.taxonomy taxonomy");
			sb.append(" left join fetch level.parent levelParent");
		}
			
		sb.append(" where rel.page.key = :pageKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyCompetence.class)
				.setParameter("pageKey", page.getKey())
				.getResultList();
	}
	
	public Page getPageToCompetence(TaxonomyCompetence competence) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select page from cepagetotaxonomycompetence rel")
		  .append(" inner join rel.page as page")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch section.binder as binder")
		  .append(" where rel.taxonomyCompetence.key = :competenceKey");
		
		List<Page> pages = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Page.class)
				.setParameter("competenceKey", competence.getKey())
				.getResultList();
		
		return !pages.isEmpty() ? pages.get(0) : null;
	}
	
	public void deleteRelation(Page page, TaxonomyCompetence taxonomyCompetence) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select rel from cepagetotaxonomycompetence rel")
		  .append(" where rel.page.key = :pageKey and rel.taxonomyCompetence.key = :competenceKey");
		
		List<PageToTaxonomyCompetence> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PageToTaxonomyCompetence.class)
				.setParameter("pageKey", page.getKey())
				.setParameter("competenceKey", taxonomyCompetence.getKey())
				.getResultList();
		
		for(PageToTaxonomyCompetence relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public void deleteRelation(TaxonomyCompetence taxonomyCompetence) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select rel from cepagetotaxonomycompetence rel")
		  .append(" where rel.taxonomyCompetence.key = :competenceKey");
		
		List<PageToTaxonomyCompetence> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PageToTaxonomyCompetence.class)
				.setParameter("competenceKey", taxonomyCompetence.getKey())
				.getResultList();
		
		for(PageToTaxonomyCompetence relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public void deleteRelationsByLevelType(TaxonomyLevelType levelType) {
		if (levelType == null || levelType.isAllowedAsCompetence()) {
			return;
		}
		
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select rel from cepagetotaxonomycompetence rel")
		  .append(" inner join rel.taxonomyCompetence.taxonomyLevel as level")
		  .append(" inner join level.type as type")
		  .append(" where type.key = :typeKey");
		
		
		List<PageToTaxonomyCompetence> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PageToTaxonomyCompetence.class)
				.setParameter("typeKey", levelType.getKey())
				.getResultList();
		
		for(PageToTaxonomyCompetence relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public void deleteRelation(Page page) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select rel from cepagetotaxonomycompetence rel")
		  .append(" where rel.page.key = :pageKey");
		
		List<PageToTaxonomyCompetence> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PageToTaxonomyCompetence.class)
				.setParameter("pageKey", page.getKey())
				.getResultList();
		
		for(PageToTaxonomyCompetence relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public Map<TaxonomyLevel,Long> getCompetencesAndUsage(Section section) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select level as level, count(*) as competenceCount from cepagetotaxonomycompetence rel")
		  .append(" inner join rel.page as page")
		  .append(" inner join rel.taxonomyCompetence as competence")
		  .append(" inner join competence.taxonomyLevel as level")
		  .append(" where page.section.key = :sectionKey")
		  .append(" group by level")
		  .append(" order by competenceCount desc, level.displayName asc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tuple.class)
				.setParameter("sectionKey", section.getKey())
				.getResultStream()
				.collect(
					Collectors.toMap(
						tuple -> ((TaxonomyLevel) tuple.get("level")), 
						tuple -> ((Long) tuple.get("competenceCount")),
						(level1, level2) -> level1, 
						LinkedHashMap::new));
	}
	
	public Map<TaxonomyLevel, Long> getCompetencesAndUsage(List<Page> pages) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select level as level, count(*) as competenceCount from cepagetotaxonomycompetence rel")
		  .append(" inner join rel.page as page")
		  .append(" inner join rel.taxonomyCompetence as competence")
		  .append(" inner join competence.taxonomyLevel as level")
		  .append(" where page.key in :pageKeys")
		  .append(" group by level")
		  .append(" order by competenceCount desc, level.displayName asc");
		
		List<Long> pageKeys = pages.stream().filter(page -> page != null).map(page -> page.getKey()).collect(Collectors.toList());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tuple.class)
				.setParameter("pageKeys", pageKeys)
				.getResultStream()
				.collect(
					Collectors.toMap(
						tuple -> ((TaxonomyLevel) tuple.get("level")), 
						tuple -> ((Long) tuple.get("competenceCount")),
						(level1, level2) -> level1, 
						LinkedHashMap::new));
	}
}
