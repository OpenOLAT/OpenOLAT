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
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.model.QualityDataCollectionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityDataCollectionDAO {
	
	@Autowired
	private DB dbInstance;

	QualityDataCollection createDataCollection() {
		QualityDataCollectionImpl dataCollectionImpl = new QualityDataCollectionImpl();
		dataCollectionImpl.setCreationDate(new Date());
		dataCollectionImpl.setLastModified(dataCollectionImpl.getCreationDate());
		dataCollectionImpl.setStatus(QualityDataCollectionStatus.PREPARATION);
		dbInstance.getCurrentEntityManager().persist(dataCollectionImpl);
		return dataCollectionImpl;
	}
	
	QualityDataCollection updateDataCollection(QualityDataCollection dataCollection) {
		if (dataCollection instanceof QualityDataCollectionImpl) {
			QualityDataCollectionImpl dataCollectionImpl = (QualityDataCollectionImpl) dataCollection;
			dataCollectionImpl.setLastModified(new Date());
			return dbInstance.getCurrentEntityManager().merge(dataCollectionImpl);
		}
		return dataCollection;
	}

	QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return null;
			
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection from qualitydatacollection as collection");
		sb.append(" where collection.key=:collectionKey");
		
		 List<QualityDataCollection> dataCollections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("collectionKey", dataCollectionRef.getKey())
				.getResultList();
		return dataCollections.isEmpty() ? null : dataCollections.get(0);
	}

	void deleteDataCollection(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualitydatacollection as collection");
		sb.append(" where collection.key=:collectionKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("collectionKey", dataCollectionRef.getKey())
				.executeUpdate();
	}

	int getDataCollectionCount() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(collection.key) from qualitydatacollection as collection");
		
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		return Math.toIntExact(counts.get(0));
	}

	List<QualityDataCollectionView> loadDataCollections(Translator translator, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityDataCollectionViewImpl(");
		sb.append("       collection.key as key");
		sb.append("     , collection.status as status");
		sb.append("     , collection.title as title");
		sb.append("     , collection.start as start");
		sb.append("     , collection.deadline as deadline");
		sb.append("     , form.displayname as formName");
		sb.append("     , case");
		sb.append("       when curriculumElementType is not null");
		sb.append("       then curriculumElementType.displayName");
		for (QualityDataCollectionTopicType topicType: QualityDataCollectionTopicType.values()) {
			sb.append("       when collection.topicType = '").append(topicType.toString()).append("'");
			sb.append("       then '").append(translator.translate(topicType.getI18nKey())).append("'");
		}
		sb.append("       end as topicType");
		sb.append("     , case collection.topicType");
		sb.append("            when '").append(QualityDataCollectionTopicType.CUSTOM).append("'");
		sb.append("            then collection.topicCustom");
		sb.append("            when '").append(QualityDataCollectionTopicType.IDENTIY).append("'");
		sb.append("            then concat(user.lastName, ' ', user.firstName)");
		sb.append("            when '").append(QualityDataCollectionTopicType.ORGANISATION).append("'");
		sb.append("            then organisation.displayName");
		sb.append("            when '").append(QualityDataCollectionTopicType.CURRICULUM).append("'");
		sb.append("            then curriculum.displayName");
		sb.append("            when '").append(QualityDataCollectionTopicType.CURRICULUM_ELEMENT).append("'");
		sb.append("            then curriculumElement.displayName");
		sb.append("            when '").append(QualityDataCollectionTopicType.REPOSITORY).append("'");
		sb.append("            then repository.displayname");
		sb.append("       end as topic");
		sb.append("     , ( select count(participation.key)");
		sb.append("           from evaluationformparticipation participation");
		sb.append("          where participation.survey.key = survey.key");
		sb.append("       ) as numberParticipants");
		sb.append("       )");
		sb.append("  from qualitydatacollection as collection");
		sb.append("       join evaluationformsurvey survey on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                       and survey.resId = collection.key");
		sb.append("       join survey.formEntry as form");
		sb.append("       left join collection.topicIdentity.user as user");
		sb.append("       left join collection.topicOrganisation as organisation");
		sb.append("       left join collection.topicCurriculum as curriculum");
		sb.append("       left join collection.topicCurriculumElement as curriculumElement");
		sb.append("       left join curriculumElement.type as curriculumElementType");
		sb.append("       left join collection.topicRepositoryEntry as repository");
		
		appendOrderBy(sb, orderBy);

		TypedQuery<QualityDataCollectionView> query = dbInstance.getCurrentEntityManager().
				createQuery(sb.toString(), QualityDataCollectionView.class);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	private void appendOrderBy(StringBuilder sb, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			sb.append(sortKey);
			appendAsc(sb, asc);
		} else {
			sb.append(" order by collection.key asc ");
		}
	}
	
	private final StringBuilder appendAsc(StringBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
}
