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
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.modules.quality.model.QualityContextToCurriculumElementImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityContextToCurriculumElementDAO {

	private static final OLog log = Tracing.createLoggerFor(QualityContextToCurriculumElementDAO.class);

	@Autowired
	private DB dbInstance;
	
	QualityContextToCurriculumElement createRelation(QualityContext context, CurriculumElement curriculumElement) {
		QualityContextToCurriculumElementImpl rel = new QualityContextToCurriculumElementImpl();
		rel.setCreationDate(new Date());
		rel.setContext(context);
		rel.setCurriculumElement(curriculumElement);
		dbInstance.getCurrentEntityManager().persist(rel);
		log.debug("Quality context to curriculum element created: " + rel.toString());
		return rel;
	}
	
	List<QualityContextToCurriculumElement> loadByContextKey(QualityContextRef contextRef) {
		if (contextRef == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel");
		sb.append("  from contexttocurriculumelement as rel");
		sb.append(" where rel.context.key = :contextKey");
		
		 List<QualityContextToCurriculumElement> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContextToCurriculumElement.class)
				.setParameter("contextKey", contextRef.getKey())
				.getResultList();
		return relations;
	}

	void deleteRelations(QualityContextRef context) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete");
		sb.append("  from contexttocurriculumelement rel");
		sb.append(" where rel.context.key = :contextKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("contextKey", context.getKey())
				.executeUpdate();
		
		log.debug("All quality context to curriculum element deleted: " + context.toString());
	}
	
}
