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
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionToOrganisation;
import org.olat.modules.quality.model.QualityDataCollectionToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityDataCollectionToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	QualityDataCollectionToOrganisation createRelation(QualityDataCollection dataCollection, Organisation organisation) {
		QualityDataCollectionToOrganisationImpl rel = new QualityDataCollectionToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setDataCollection(dataCollection);
		rel.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	List<QualityDataCollectionToOrganisation> loadByDataCollectionKey(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel");
		sb.append("  from qualitydatacollectiontoorganisation as rel");
		sb.append(" where rel.dataCollection.key = :dataCollectionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollectionToOrganisation.class)
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.getResultList();
	}
	
	List<Organisation> loadOrganisationsByDataCollectionKey(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.organisation");
		sb.append("  from qualitydatacollectiontoorganisation as rel");
		sb.append("       inner join rel.organisation as org");
		sb.append(" where rel.dataCollection.key = :dataCollectionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.getResultList();
	}
	
	void delete(QualityDataCollectionToOrganisation relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}

	void deleteRelations(QualityDataCollectionRef dataCollection) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete");
		sb.append("  from qualitydatacollectiontoorganisation rel");
		sb.append(" where rel.dataCollection.key = :dataCollectionKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("dataCollectionKey", dataCollection.getKey())
				.executeUpdate();
	}
	
	boolean hasRelations(OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rel.key");
		sb.append("  from qualitydatacollectiontoorganisation as rel");
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
