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
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextToOrganisation;
import org.olat.modules.quality.model.QualityContextToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityContextToOrganisationDAO {

	private static final Logger log = Tracing.createLoggerFor(QualityContextToOrganisationDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	QualityContextToOrganisation createRelation(QualityContext context, Organisation organisation) {
		QualityContextToOrganisationImpl rel = new QualityContextToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setContext(context);
		rel.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(rel);
		log.debug("Quality context to organisation created: " + rel.toString());
		return rel;
	}
	
	List<QualityContextToOrganisation> loadByContextKey(QualityContextRef contextRef) {
		if (contextRef == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel");
		sb.append("  from contexttoorganisation as rel");
		sb.append(" where rel.context.key = :contextKey");
		
		 List<QualityContextToOrganisation> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContextToOrganisation.class)
				.setParameter("contextKey", contextRef.getKey())
				.getResultList();
		return relations;
	}

	void deleteRelations(QualityContextRef context) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete");
		sb.append("  from contexttoorganisation rel");
		sb.append(" where rel.context.key = :contextKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("contextKey", context.getKey())
				.executeUpdate();
		
		log.debug("All quality context to organisation deleted: " + context.toString());
	}
	
	boolean hasRelations(OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.key");
		sb.append("  from contexttoorganisation as rel");
		sb.append(" where rel.organisation.key = :organisationKey");
		
		 List<Long> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("organisationKey", organisation.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return relations != null && !relations.isEmpty() && relations.get(0) != null;
	}
}
