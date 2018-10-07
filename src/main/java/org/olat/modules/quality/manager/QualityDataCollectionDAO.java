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

import static java.util.stream.Collectors.toList;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionSearchParams;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.model.QualityDataCollectionImpl;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityDataCollectionDAO {
	
	@Autowired
	private DB dbInstance;

	QualityDataCollection createDataCollection() {
		return createDataCollection(null, null);
	}
	
	QualityDataCollection createDataCollection(QualityGenerator generator, Long generatorProviderKey) {
		QualityDataCollectionImpl dataCollectionImpl = new QualityDataCollectionImpl();
		dataCollectionImpl.setCreationDate(new Date());
		dataCollectionImpl.setLastModified(dataCollectionImpl.getCreationDate());
		dataCollectionImpl.setStatus(QualityDataCollectionStatus.PREPARATION);
		dataCollectionImpl.setGenerator(generator);
		dataCollectionImpl.setGeneratorProviderKey(generatorProviderKey);
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
	
	List<QualityDataCollection> loadAllDataCollections() {
		String query = "select collection from qualitydatacollection as collection";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, QualityDataCollection.class)
				.getResultList();
	}
 
	QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return null;
			
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection");
		sb.append("  from qualitydatacollection as collection");
		sb.append("       left join fetch collection.topicIdentity");
		sb.append("       left join fetch collection.topicOrganisation");
		sb.append(" where collection.key=:collectionKey");
		
		 List<QualityDataCollection> dataCollections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("collectionKey", dataCollectionRef.getKey())
				.getResultList();
		return dataCollections.isEmpty() ? null : dataCollections.get(0);
	}

	public QualityDataCollection loadPrevious(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder(512);
		sb.append("select previousCollection");
		sb.append("  from qualitydatacollection as followUpCollection");
		sb.append("       join evaluationformsurvey followUpSurvey on followUpSurvey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                               and followUpSurvey.resId = followUpCollection.key");
		sb.append("       join followUpSurvey.seriesPrevious as previousSurvey");
		sb.append("       join qualitydatacollection as previousCollection on previousSurvey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                                       and previousSurvey.resId = previousCollection.key");
		sb.append(" where followUpCollection.key =: collectionKey");
		
		 List<QualityDataCollection> dataCollections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("collectionKey", dataCollectionRef.getKey())
				.getResultList();
		return dataCollections.isEmpty() ? null : dataCollections.get(0);
	}

	public QualityDataCollection loadFollowUp(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder(512);
		sb.append("select followUpCollection");
		sb.append("  from qualitydatacollection as followUpCollection");
		sb.append("       join evaluationformsurvey followUpSurvey on followUpSurvey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                               and followUpSurvey.resId = followUpCollection.key");
		sb.append("       join followUpSurvey.seriesPrevious as previousSurvey");
		sb.append("       join qualitydatacollection as previousCollection on previousSurvey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                                       and previousSurvey.resId = previousCollection.key");
		sb.append(" where previousCollection.key =: collectionKey");
		
		 List<QualityDataCollection> dataCollections = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("collectionKey", dataCollectionRef.getKey())
				.getResultList();
		return dataCollections.isEmpty() ? null : dataCollections.get(0);
	}

	Collection<QualityDataCollection> loadWithPendingStart(Date until) {
		if (until == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection");
		sb.append("  from qualitydatacollection as collection");
		sb.append(" where collection.status = '").append(READY).append("'");
		sb.append("   and collection.start <= :until");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("until", until)
				.getResultList();
	}

	Collection<QualityDataCollection> loadWithPendingDeadline(Date until) {
		if (until == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection");
		sb.append("  from qualitydatacollection as collection");
		sb.append(" where collection.status in :status");
		sb.append("   and collection.deadline <= :until");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("status", Arrays.asList(READY, RUNNING))
				.setParameter("until", until)
				.getResultList();
	}

	List<QualityDataCollection> loadDataCollections(QualityDataCollectionSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select collection");
		sb.append("  from qualitydatacollection as collection");
		appendWhere(sb, searchParams);
		
		TypedQuery<QualityDataCollection> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class);
		appendParameters(query, searchParams);
		
		return query.getResultList();
	}
	
	private void appendWhere(QueryBuilder sb, QualityDataCollectionSearchParams searchParams) {
		if (searchParams.getGeneratorRef() != null) {
			sb.and().append("collection.generator.key = :generatorKey");
		}
		if (searchParams.getGeneratorProviderKey() != null) {
			sb.and().append("collection.generatorProviderKey = :generatorProviderKey");
		}
		if (searchParams.getTopicIdentityRef() != null) {
			sb.and().append("collection.topicIdentity.key = :topicIdentityKey");
		}
		if (searchParams.getTopicRepositoryRef() != null) {
			sb.and().append("collection.topicRepositoryEntry.key = :topicRepositoryKey");
		}
	}

	private void appendParameters(TypedQuery<QualityDataCollection> query,
			QualityDataCollectionSearchParams searchParams) {
		if (searchParams.getGeneratorRef() != null) {
			query.setParameter("generatorKey", searchParams.getGeneratorRef().getKey());
		}
		if (searchParams.getGeneratorProviderKey() != null) {
			query.setParameter("generatorProviderKey", searchParams.getGeneratorProviderKey());
		}
		if (searchParams.getTopicIdentityRef() != null) {
			query.setParameter("topicIdentityKey", searchParams.getTopicIdentityRef().getKey());
		}
		if (searchParams.getTopicRepositoryRef() != null) {
			query.setParameter("topicRepositoryKey", searchParams.getTopicRepositoryRef().getKey());
		}
	}

	List<QualityDataCollection> loadDataCollectionsByTaxonomyLevel(TaxonomyLevelRef taxonomyLevel) {
		if (taxonomyLevel == null || taxonomyLevel.getKey() == null) return Collections.emptyList();
			
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct collection from qualitydatacollection as collection")
		  .append(" inner join qualitycontext context on (context.dataCollection.key=collection.key)")
		  .append(" inner join context.contextToTaxonomyLevel ctxToTax")
		  .append(" where ctxToTax.taxonomyLevel.key=:taxonomyLevelKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityDataCollection.class)
				.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
				.getResultList();
	}
	
	public int countDataCollectionsByTaxonomyLevel(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		if (taxonomyLevels == null || taxonomyLevels.isEmpty()) return 0;
			
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(distinct collection.key) from qualitydatacollection as collection")
		  .append(" inner join qualitycontext context on (context.dataCollection.key=collection.key)")
		  .append(" inner join context.contextToTaxonomyLevel ctxToTax")
		  .append(" where ctxToTax.taxonomyLevel.key in (:taxonomyLevelKeys)");
		
		List<Long> taxonomyLevelKeys = taxonomyLevels.stream().map(TaxonomyLevelRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("taxonomyLevelKeys", taxonomyLevelKeys)
				.getSingleResult().intValue();
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
	
	boolean hasDataCollection(OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection.key from qualitydatacollection as collection")
		  .append(" where collection.topicOrganisation.key=:organisationKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("organisationKey", organisation.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}
	
	boolean hasDataCollection(CurriculumRef curriculum) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection.key from qualitydatacollection as collection")
		  .append(" where collection.topicCurriculum.key=:curriculumKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("curriculumKey", curriculum.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}
	
	boolean hasDataCollection(CurriculumElementRef curriculumElement) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection.key from qualitydatacollection as collection")
		  .append(" where collection.topicCurriculumElement.key=:curriculumElementKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("curriculumElementKey", curriculumElement.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}

	int getDataCollectionCount(QualityDataCollectionViewSearchParams searchParams) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(collection)");
		sb.append("  from qualitydatacollection as collection");
		sb.append(" where 1=1");
		appendWhereClause(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameter(query, searchParams);
		
		List<Long> counts = query.getResultList();
		return Math.toIntExact(counts.get(0));
	}

	List<QualityDataCollectionView> loadDataCollections(Translator translator,
			QualityDataCollectionViewSearchParams searchParams, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityDataCollectionViewImpl(");
		sb.append("       collection.key as key");
		sb.append("     , collection.status as status");
		sb.append("     , collection.title as title");
		sb.append("     , collection.start as start");
		sb.append("     , collection.deadline as deadline");
		sb.append("     , collection.creationDate as creationDate");
		sb.append("     , generator.title as generatorTitle");
		sb.append("     , form.displayname as formName");
		sb.append("     , collection.topicType as topicType");
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
		sb.append("     , previousCollection.title as previousTitle");
		sb.append("     , ( select count(participation.key)");
		sb.append("           from evaluationformparticipation participation");
		sb.append("          where participation.survey.key = survey.key");
		sb.append("       ) as numberParticipants");
		sb.append("       )");
		sb.append("  from qualitydatacollection as collection");
		sb.append("       join evaluationformsurvey survey on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                       and survey.resId = collection.key");
		sb.append("       join survey.formEntry as form");
		sb.append("       left join collection.generator as generator");
		sb.append("       left join collection.topicIdentity.user as user");
		sb.append("       left join collection.topicOrganisation as organisation");
		sb.append("       left join collection.topicCurriculum as curriculum");
		sb.append("       left join collection.topicCurriculumElement as curriculumElement");
		sb.append("       left join curriculumElement.type as curriculumElementType");
		sb.append("       left join collection.topicRepositoryEntry as repository");
		sb.append("       left join survey.seriesPrevious as previousSurvey");
		sb.append("       left join qualitydatacollection as previousCollection on previousSurvey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                                            and previousSurvey.resId = previousCollection.key");
		sb.append(" where 1=1");
		appendWhereClause(sb, searchParams);
		
		appendOrderBy(sb, orderBy);

		TypedQuery<QualityDataCollectionView> query = dbInstance.getCurrentEntityManager().
				createQuery(sb.toString(), QualityDataCollectionView.class);
		appendParameter(query, searchParams);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	private void appendWhereClause(StringBuilder sb, QualityDataCollectionViewSearchParams searchParams) {
		if (searchParams != null) {
			if (searchParams.getDataCollectionRef() != null && searchParams.getDataCollectionRef().getKey() != null) {
				sb.append(" and collection.key = :collectionKey");
			}
			if (searchParams.getOrgansationRefs() != null && !searchParams.getOrgansationRefs().isEmpty()) {
				sb.append(" and collection.key in (");
				sb.append("     select collectionToOrganisation.dataCollection.key");
				sb.append("       from qualitydatacollectiontoorganisation as collectionToOrganisation");
				sb.append("      where collectionToOrganisation.organisation.key in :organisationKeys");
				sb.append(" )");
			}
		}
	}
	
	private void appendParameter(TypedQuery<?> query, QualityDataCollectionViewSearchParams searchParams) {
		if (searchParams != null) {
			if (searchParams.getDataCollectionRef() != null && searchParams.getDataCollectionRef().getKey() != null) {
				query.setParameter("collectionKey", searchParams.getDataCollectionRef().getKey());
			}
			if (searchParams.getOrgansationRefs() != null && !searchParams.getOrgansationRefs().isEmpty()) {
				List<Long> organiationKeys = searchParams.getOrgansationRefs().stream().map(OrganisationRef::getKey).collect(toList());
				query.setParameter("organisationKeys", organiationKeys);
			}
		}
	}

	private void appendOrderBy(StringBuilder sb, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			sb.append(sortKey);
			appendAsc(sb, asc);
		} else {
			sb.append(" order by collection.deadline desc nulls first ");
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
