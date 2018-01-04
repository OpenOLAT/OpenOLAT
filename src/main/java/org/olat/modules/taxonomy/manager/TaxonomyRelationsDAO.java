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

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyRelationsDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LifeFullIndexer lifeIndexer;
	
	public int countQuestionItems(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		if(taxonomyLevels == null || taxonomyLevels.isEmpty()) return 0;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(item.key) from questionitem item")
		  .append(" where item.taxonomyLevel.key in (:taxonomyLevelKeys)");
		
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
	
	public int replaceQuestionItem(TaxonomyLevelRef source, TaxonomyLevelRef target) {
		List<Long> questionItemKeys = getQuestionItemKeys(source);
		
		int row;
		if(questionItemKeys.isEmpty()) {
			row = 0;
		} else {
			String q = "update questionitem item set item.taxonomyLevel.key=:targetLevelKey where item.taxonomyLevel.key=:sourceLevelKey";
			row = dbInstance.getCurrentEntityManager()
					.createQuery(q)
					.setParameter("sourceLevelKey", source.getKey())
					.setParameter("targetLevelKey", target.getKey())
					.executeUpdate();
			dbInstance.commit();
			for(Long questionItemKey:questionItemKeys) {
				lifeIndexer.indexDocument(QItemDocument.TYPE, questionItemKey);
			}
		}
		
		return row;
	}
	
	public int removeFromQuestionItems(TaxonomyLevelRef level) {
		List<Long> questionItemKeys = getQuestionItemKeys(level);
		
		int row;
		if(questionItemKeys.isEmpty()) {
			row = 0;
		} else {
			String q = "update questionitem item set item.taxonomyLevel.key=null where item.taxonomyLevel.key=:levelKey";
			row = dbInstance.getCurrentEntityManager()
					.createQuery(q)
					.setParameter("levelKey", level.getKey())
					.executeUpdate();
			dbInstance.commit();
			for(Long questionItemKey:questionItemKeys) {
				lifeIndexer.indexDocument(QItemDocument.TYPE, questionItemKey);
			}
		}
		
		return row;
	}
	
	private List<Long> getQuestionItemKeys(TaxonomyLevelRef level) {
		String q = "select item.key from questionitem item where item.taxonomyLevel.key=:levelKey";
		return  dbInstance.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.setParameter("levelKey", level.getKey())
				.getResultList();
	}
}
