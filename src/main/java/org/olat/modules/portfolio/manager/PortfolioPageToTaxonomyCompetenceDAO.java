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
package org.olat.modules.portfolio.manager;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioPageToTaxonomyCompetence;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.PortfolioPageToTaxonomyCompetenceImpl;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 11.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class PortfolioPageToTaxonomyCompetenceDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PortfolioPageToTaxonomyCompetence createRelation(Page portfolioPage, TaxonomyCompetence taxonomyCompetence) {
		
		PortfolioPageToTaxonomyCompetenceImpl relation = new PortfolioPageToTaxonomyCompetenceImpl();
		
		relation.setCreationDate(new Date());
		relation.setPortfolioPage(portfolioPage);
		relation.setTaxonomyCompetence(taxonomyCompetence);
		
		dbInstance.getCurrentEntityManager().persist(relation);
		
		return relation;
	}
	
	public List<TaxonomyCompetence> getCompetencesToPortfolioPage(Page portfolioPage, boolean fetchTaxonomies) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select competence from pfpagetotaxonomycompetence rel")
		  .append(" inner join rel.taxonomyCompetence as competence");
		
		if (fetchTaxonomies) {
			sb.append(" inner join fetch competence.taxonomyLevel level");
			sb.append(" inner join fetch level.taxonomy taxonomy");
			sb.append(" left join fetch level.parent levelParent");
		}
			
		sb.append(" where rel.portfolioPage.key = :pageKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyCompetence.class)
				.setParameter("pageKey", portfolioPage.getKey())
				.getResultList();
	}
	
	public void deleteRelation(Page portfolioPage, TaxonomyCompetence taxonomyCompetence) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select rel from pfpagetotaxonomycompetence rel")
		  .append(" where rel.portfolioPage.key = :pageKey and rel.taxonomyCompetence.key = :competenceKey");
		
		List<PortfolioPageToTaxonomyCompetence> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PortfolioPageToTaxonomyCompetence.class)
				.setParameter("pageKey", portfolioPage.getKey())
				.setParameter("competenceKey", taxonomyCompetence.getKey())
				.getResultList();
		
		for(PortfolioPageToTaxonomyCompetence relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public void deleteRelation(Page portfolioPage) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select rel from pfpagetotaxonomycompetence rel")
		  .append(" where rel.portfolioPage.key = :pageKey");
		
		List<PortfolioPageToTaxonomyCompetence> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PortfolioPageToTaxonomyCompetence.class)
				.setParameter("pageKey", portfolioPage.getKey())
				.getResultList();
		
		for(PortfolioPageToTaxonomyCompetence relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}
	
	public LinkedHashMap<TaxonomyLevel, Long> getCompetencesAndUsage(Section section) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select level as level, count(*) as competenceCount from pfpagetotaxonomycompetence rel")
		  .append(" inner join rel.portfolioPage as page")
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
	
	public LinkedHashMap<TaxonomyLevel, Long> getCompetencesAndUsage(List<Page> pages) {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append("select level as level, count(*) as competenceCount from pfpagetotaxonomycompetence rel")
		  .append(" inner join rel.portfolioPage as page")
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
