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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccess.EmailTrigger;
import org.olat.modules.quality.QualityReportAccess.Type;
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
		return create(reference, type, role, false, EmailTrigger.never);
	}

	QualityReportAccess copy(QualityReportAccessReference reference, QualityReportAccess reportAccess) {
		return create(
				reference,
				reportAccess.getType(),
				reportAccess.getRole(),
				reportAccess.isOnline(),
				reportAccess.getEmailTrigger());
	}
	
	private QualityReportAccess create(QualityReportAccessReference reference, QualityReportAccess.Type type, String role, boolean online, EmailTrigger emailTrigger) {
		QualityReportAccessImpl reportAccess = new QualityReportAccessImpl();
		reportAccess.setCreationDate(new Date());
		reportAccess.setLastModified(reportAccess.getCreationDate());
		reportAccess.setOnline(online);
		reportAccess.setEmailTrigger(emailTrigger);
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

	void deleteReportAccesses(QualityReportAccessReference reference) {
		if (reference == null) return;
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("delete from qualityreportaccess as reportaccess");
		if (reference.getDataCollectionRef() != null) {
			sb.and().append("reportaccess.dataCollection.key = :dataCollectionKey");
		}
		if (reference.getGeneratorRef() != null) {
			sb.and().append("reportaccess.generator.key = :generatorKey");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString());
		if (reference.getDataCollectionRef() != null) {
			query.setParameter("dataCollectionKey", reference.getDataCollectionRef().getKey());
		}
		if (reference.getGeneratorRef() != null) {
			query.setParameter("generatorKey", reference.getGeneratorRef().getKey());
		}
		query.executeUpdate();
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
		if (searchParams.getReference() != null) {
			if (searchParams.getReference().getDataCollectionRef() != null) {
				sb.and().append("reportaccess.dataCollection.key = :dataCollectionKey");
			}
			if (searchParams.getReference().getGeneratorRef() != null) {
				sb.and().append("reportaccess.generator.key = :generatorKey");
			}
		}
	}

	private void appendParameter(TypedQuery<QualityReportAccess> query, QualityReportAccessSearchParams searchParams) {
		if (searchParams.getReference() != null) {
			if (searchParams.getReference().getDataCollectionRef() != null) {
				query.setParameter("dataCollectionKey", searchParams.getReference().getDataCollectionRef().getKey());
			}
			if (searchParams.getReference().getGeneratorRef() != null) {
				query.setParameter("generatorKey", searchParams.getReference().getGeneratorRef().getKey());
			}
		}
	}

	List<Identity> loadRecipients(QualityReportAccess reportAccess) {
		Type type = reportAccess.getType();
		switch (type) {
		case Participants: return loadRecipientsOfParticipants(reportAccess);
		case GroupRoles: return loadRecipientsOfGroupRoles(reportAccess);
		case TopicIdentity: return loadRecipientsOfTopicIdentity(reportAccess);
		default: return Collections.emptyList();
		}
	}

	private List<Identity> loadRecipientsOfParticipants(QualityReportAccess reportAccess) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select participation.executor");
		sb.append("  from qualityreportaccess as ra");
		sb.append("       join ra.dataCollection as collection");
		sb.append("       join evaluationformsurvey survey");
		sb.append("         on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("        and survey.resId = collection.key");
		sb.append("       join evaluationformparticipation as participation");
		sb.append("         on participation.survey.key = survey.key");
		sb.and().append("ra.key = :reportAccessKey");
		sb.and().append("((ra.role is null) or (participation.status = ra.role))");
		sb.and().append("participation.executor is not null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("reportAccessKey", reportAccess.getKey())
				.getResultList();
	}

	private List<Identity> loadRecipientsOfGroupRoles(QualityReportAccess reportAccess) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership.identity");
		sb.append("  from qualityreportaccess as ra");
		sb.append("     , qualitydatacollection as collection");
		sb.append("     , qualitycontext as context");
		sb.append("     , repoentrytogroup as rel");
		sb.append("     , bgroupmember as membership");
		sb.and().append("ra.dataCollection.key = context.dataCollection.key");
		sb.and().append("rel.entry.key = context.audienceRepositoryEntry.key");
		sb.and().append("rel.group.key = membership.group.key");
		sb.and().append("membership.role = ra.role");
		sb.and().append("ra.key = :reportAccessKey");
		
		return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Identity.class)
					.setParameter("reportAccessKey", reportAccess.getKey())
					.getResultList();
	}

	private List<Identity> loadRecipientsOfTopicIdentity(QualityReportAccess reportAccess) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select collection.topicIdentity");
		sb.append("  from qualityreportaccess as ra");
		sb.append("       join ra.dataCollection as collection");
		sb.and().append("collection.topicIdentity is not null");
		sb.and().append("ra.key = :reportAccessKey");
		
		return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Identity.class)
					.setParameter("reportAccessKey", reportAccess.getKey())
					.getResultList();
	}


}
