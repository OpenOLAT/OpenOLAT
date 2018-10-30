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

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccessReference;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.model.QualityGeneratorImpl;
import org.olat.modules.quality.model.QualityDataCollectionImpl;
import org.olat.modules.quality.model.QualityReportAccessImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityReportAccessDAO {
	
	@Autowired
	private DB dbInstance;

	QualityReportAccess create(QualityReportAccessReference reference, QualityReportAccess.Type type, String role) {
		QualityReportAccessImpl reportAccess = new QualityReportAccessImpl();
		reportAccess.setCreationDate(new Date());
		reportAccess.setLastModified(reportAccess.getCreationDate());
		reportAccess.setOnline(false);
		reportAccess.setEmailTrigger(QualityReportAccess.EmailTrigger.never);
		reportAccess.setType(type);
		reportAccess.setRole(role);
		if (reference.isDataCollectionRef()) {
			QualityDataCollection dataCollection = dbInstance.getCurrentEntityManager()
					.getReference(QualityDataCollectionImpl.class, reference.getDataCollectionRef().getKey());
			reportAccess.setDataCollection(dataCollection);
		} else {
			QualityGenerator generator = dbInstance.getCurrentEntityManager().getReference(QualityGeneratorImpl.class,
					reference.getGeneratorRef().getKey());
			reportAccess.setGenerator(generator);
		}
		dbInstance.getCurrentEntityManager().persist(reportAccess);
		return reportAccess;
	}

	QualityReportAccess save(QualityReportAccess reportAccess) {
		reportAccess.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(reportAccess);
	}

	void deleteReportAccesses(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualityreportaccess as reportaccess");
		sb.append(" where reportaccess.dataCollection.key = :dataCollectionKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.executeUpdate();	
	}

	List<QualityReportAccess> load(QualityReportAccessSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select reportaccess");
		sb.append("  from qualityreportaccess as reportaccess");
		appendWhere(sb, searchParams);
		
		TypedQuery<QualityReportAccess> query = dbInstance.getCurrentEntityManager().
				createQuery(sb.toString(), QualityReportAccess.class);
		appendParameter(query, searchParams);
		return query.getResultList();
	}

	private void appendWhere(QueryBuilder sb, QualityReportAccessSearchParams searchParams) {
		if (searchParams.getDataCollectionRef() != null) {
			sb.and().append("reportaccess.dataCollection.key = :dataCollectionKey");
		}
	}

	private void appendParameter(TypedQuery<QualityReportAccess> query, QualityReportAccessSearchParams searchParams) {
		if (searchParams.getDataCollectionRef() != null) {
			query.setParameter("dataCollectionKey", searchParams.getDataCollectionRef().getKey());
		}
	}

}
