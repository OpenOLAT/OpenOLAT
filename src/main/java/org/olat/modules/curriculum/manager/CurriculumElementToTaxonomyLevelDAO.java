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
package org.olat.modules.curriculum.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.model.CurriculumElementToTaxonomyLevelImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementToTaxonomyLevelDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CurriculumElementToTaxonomyLevel createRelation(CurriculumElement curriculumElement, TaxonomyLevel taxonomyLevel) {
		CurriculumElementToTaxonomyLevelImpl rel = new CurriculumElementToTaxonomyLevelImpl();
		rel.setCreationDate(new Date());
		rel.setCurriculumElement(curriculumElement);
		rel.setCurriculumElement(curriculumElement);
		rel.setTaxonomyLevel(taxonomyLevel);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<TaxonomyLevel> getTaxonomyLevels(CurriculumElementRef curriculumElement) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.taxonomyLevel from curriculumelementtotaxonomylevel rel")
		  .append(" where rel.curriculumElement.key=:elementKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaxonomyLevel.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.getResultList();
	}
	
	public void deleteRelation(CurriculumElementRef curriculumElement, TaxonomyLevelRef taxonomyLevel) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel from curriculumelementtotaxonomylevel rel")
		  .append(" where rel.curriculumElement.key=:elementKey and rel.taxonomyLevel.key=:levelKey ");
		List<CurriculumElementToTaxonomyLevel> relationsToDelete = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElementToTaxonomyLevel.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.setParameter("levelKey", taxonomyLevel.getKey())
				.getResultList();
		for(CurriculumElementToTaxonomyLevel relationToDelete:relationsToDelete) {
			dbInstance.getCurrentEntityManager().remove(relationToDelete);
		}
	}

}
