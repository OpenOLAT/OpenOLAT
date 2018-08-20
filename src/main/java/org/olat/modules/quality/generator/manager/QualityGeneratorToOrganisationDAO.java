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
package org.olat.modules.quality.generator.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorToOrganisation;
import org.olat.modules.quality.generator.model.QualityGeneratorToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 * @param <QualityGeneratorToOrganisation>
 *
 */
@Service
class QualityGeneratorToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	QualityGeneratorToOrganisation createRelation(QualityGenerator generator, Organisation organisation) {
		QualityGeneratorToOrganisationImpl rel = new QualityGeneratorToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setGenerator(generator);
		rel.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	List<QualityGeneratorToOrganisation> loadByGeneratorKey(QualityGeneratorRef generatorRef) {
		if (generatorRef == null || generatorRef.getKey() == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel");
		sb.append("  from qualitygeneratortoorganisation as rel");
		sb.append(" where rel.generator.key = :generatorKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGeneratorToOrganisation.class)
				.setParameter("generatorKey", generatorRef.getKey())
				.getResultList();
	}
	
	List<Organisation> loadOrganisationsByGeneratorKey(QualityGeneratorRef generatorRef) {
		if (generatorRef == null || generatorRef.getKey() == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.organisation");
		sb.append("  from qualitygeneratortoorganisation as rel");
		sb.append("       inner join rel.organisation as org");
		sb.append(" where rel.generator.key = :generatorKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("generatorKey", generatorRef.getKey())
				.getResultList();
	}
	
	void delete(QualityGeneratorToOrganisation relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}

	void deleteRelations(QualityGeneratorRef generator) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete");
		sb.append("  from qualitygeneratortoorganisation rel");
		sb.append(" where rel.generator.key = :generatorKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("generatorKey", generator.getKey())
				.executeUpdate();
	}
	
	boolean hasRelations(OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.key");
		sb.append("  from qualitygeneratortoorganisation as rel");
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
