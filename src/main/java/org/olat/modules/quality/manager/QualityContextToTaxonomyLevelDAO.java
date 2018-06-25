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
package org.olat.modules.quality.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.model.QualityContextToTaxonomyLevelImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityContextToTaxonomyLevelDAO {

	private static final OLog log = Tracing.createLoggerFor(QualityContextToTaxonomyLevelDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	QualityContextToTaxonomyLevel createRelation(QualityContext context, TaxonomyLevel taxonomyLevel) {
		QualityContextToTaxonomyLevelImpl rel = new QualityContextToTaxonomyLevelImpl();
		rel.setCreationDate(new Date());
		rel.setContext(context);
		rel.setTaxonomyLevel(taxonomyLevel);
		dbInstance.getCurrentEntityManager().persist(rel);
		log.debug("Quality context to taxonomy level created: " + rel.toString());
		return rel;
	}
	
	List<QualityContextToTaxonomyLevel> loadByContextKey(QualityContextRef contextRef) {
		if (contextRef == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel");
		sb.append("  from contexttotaxonomylevel as rel");
		sb.append(" where rel.context.key = :contextKey");
		
		 List<QualityContextToTaxonomyLevel> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContextToTaxonomyLevel.class)
				.setParameter("contextKey", contextRef.getKey())
				.getResultList();
		return relations;
	}

	void deleteRelations(QualityContextRef context) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete");
		sb.append("  from contexttotaxonomylevel rel");
		sb.append(" where rel.context.key = :contextKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("contextKey", context.getKey())
				.executeUpdate();
		
		log.debug("All quality context to taxonomy level deleted: " + context.toString());
	}

}
