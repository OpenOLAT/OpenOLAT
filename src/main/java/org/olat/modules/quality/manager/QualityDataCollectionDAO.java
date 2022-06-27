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
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
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
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccessRightProvider;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.model.QualityDataCollectionImpl;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
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

	QualityDataCollection updateDataCollectionStatus(QualityDataCollection dataCollection,
			QualityDataCollectionStatus status) {
		if (dataCollection instanceof QualityDataCollectionImpl) {
			QualityDataCollectionImpl dataCollectionImpl = (QualityDataCollectionImpl) dataCollection;
			dataCollectionImpl.setStatus(status);
			return updateDataCollection(dataCollectionImpl);
		}
		return dataCollection;
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
		sb.append("       left join fetch collection.topicCurriculum");
		sb.append("       left join fetch collection.topicCurriculumElement");
		sb.append("       left join fetch collection.topicRepositoryEntry");
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
	
	boolean hasDataCollection(RepositoryEntryRef entryRef) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select collection.key from qualitydatacollection as collection")
		  .append(" where collection.topicRepositoryEntry.key=:entryKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", entryRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
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
	
	List<QualityGenerator> loadGenerators(QualityDataCollectionViewSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct(collection.generator)");
		sb.append("  from qualitydatacollection as collection");
		appendWhereClause(sb, searchParams);
		
		TypedQuery<QualityGenerator> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGenerator.class);
		appendParameter(query, searchParams);
		
		return query.getResultList();
	}
	
	public List<RepositoryEntry> loadFormEntries(QualityDataCollectionViewSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct(form)");
		sb.append("  from qualitydatacollection as collection");
		sb.append("       join evaluationformsurvey survey on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                       and survey.resId = collection.key");
		sb.append("       join survey.formEntry as form");
		appendWhereClause(sb, searchParams);
		
		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class);
		appendParameter(query, searchParams);
		
		return query.getResultList();
	}
	
	int getDataCollectionCount(QualityDataCollectionViewSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(collection)");
		sb.append("  from qualitydatacollection as collection");
		if (searchParams != null) {
			if (searchParams.getFormEntryRefs() != null) {
				sb.append("       join evaluationformsurvey survey on survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
				sb.append("                                       and survey.resId = collection.key");
				sb.append("       join survey.formEntry as form");
			}
			if (StringHelper.containsNonWhitespace(searchParams.getTopic())
					|| StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
				sb.append("       left join collection.topicIdentity.user as user");
				sb.append("       left join collection.topicOrganisation as organisation");
				sb.append("       left join collection.topicCurriculum as curriculum");
				sb.append("       left join collection.topicCurriculumElement as curriculumElement");
				sb.append("       left join curriculumElement.type as curriculumElementType");
				sb.append("       left join collection.topicRepositoryEntry as repository");
			}
		}
		appendWhereClause(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		appendParameter(query, searchParams);
		
		List<Long> counts = query.getResultList();
		return Math.toIntExact(counts.get(0));
	}

	List<QualityDataCollectionView> loadDataCollections(Translator translator,
			QualityDataCollectionViewSearchParams searchParams, int firstResult, int maxResults, SortKey... orderBy) {
		QueryBuilder sb = new QueryBuilder();
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
		sb.append("              then case when collection.topicIdentity.status < ").append(Identity.STATUS_DELETED);
		sb.append("                then concat(user.lastName, ' ', user.firstName) end");
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
	
	private void appendWhereClause(QueryBuilder sb, QualityDataCollectionViewSearchParams searchParams) {
		if (searchParams != null) {
			if (searchParams.getDataCollectionRef() != null && searchParams.getDataCollectionRef().getKey() != null) {
				sb.and().append("collection.key = :collectionKey");
			}
			if (searchParams.getFormEntryRefs() != null) {
				sb.and().append("form.key in (:formKeys)");
			}
			if (StringHelper.containsNonWhitespace(searchParams.getTitle())) {
				sb.and().append("lower(collection.title) like :title");
			}
			if (searchParams.getStartAfter() != null) {
				sb.and().append("collection.start >= :startAfter");
			}
			if (searchParams.getStartBefore() != null) {
				sb.and().append("collection.start <= :startBefore");
			}
			if (searchParams.getDeadlineAfter() != null) {
				sb.and().append("collection.deadline >= :deadlineAfter");
			}
			if (searchParams.getDeadlineBefore() != null) {
				sb.and().append("collection.deadline <= :deadlineBefore");
			}
			if (StringHelper.containsNonWhitespace(searchParams.getTopic())
					|| StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
				sb.and().append("(");
				PersistenceHelper.appendFuzzyLike(sb, "collection.topicCustom", "topic", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "user.lastName", "topic", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "user.firstName", "topic", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "organisation.displayName", "topic", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "curriculum.displayName", "topic", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "curriculumElement.displayName", "topic", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "repository.displayname", "topic", dbInstance.getDbVendor());
				if (StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
					sb.append(" or ");
					PersistenceHelper.appendFuzzyLike(sb, "collection.title", "topic", dbInstance.getDbVendor());
				}
				sb.append(")");
			}
			if (searchParams.getTopicTypes() != null) {
				sb.and().append("collection.topicType in (:topicTypes)");
			}
			if (searchParams.getGeneratorRefs() != null) {
				sb.and().append("collection.generator.key in (:generatorKeys)");
			}
			if (searchParams.getStatus() != null) {
				sb.and().append("collection.status in (:status)");
			}
			// (searchParams.getOrgansationRefs() == null): show all data collections
			if (searchParams.getOrgansationRefs() != null) {
				sb.and().append("(");
				sb.append("collection.key in (");
				sb.append("select collectionToOrganisation.dataCollection.key");
				sb.append("  from qualitydatacollectiontoorganisation as collectionToOrganisation");
				sb.append("  where collectionToOrganisation.organisation.key in :organisationKeys");
				sb.append(")");
				if (searchParams.getReportAccessIdentity() != null) {
					sb.append(" or ");
					sb.append("collection.key in (");
					sb.append("select ra.dataCollection.key");
					sb.append("  from qualityreportaccess ra");
					sb.append("       join ra.dataCollection dc");
					sb.append("       join qualitycontext as context");
					sb.append("         on context.dataCollection.key = dc.key");
					sb.append("       join repoentrytogroup as rel");
					sb.append("         on rel.entry.key = context.audienceRepositoryEntry.key");
					sb.append("       join bgroupmember as membership");
					sb.append("         on membership.group.key = rel.group.key");
					sb.append(" where ra.online = true");
					sb.append("   and ra.type = '").append(QualityReportAccess.Type.GroupRoles).append("'");
					sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
					sb.append("   and membership.role = ra.role");
					sb.append("   and membership.identity.key = :reportAccessIdentityKey");
					sb.append(")");
					sb.append(" or ");
					sb.append("collection.key in (");
					sb.append("select ra.dataCollection.key");
					sb.append("  from qualityreportaccess ra");
					sb.append("       join ra.dataCollection dc");
					sb.append("       join qualitycontext as context");
					sb.append("         on context.dataCollection.key = dc.key");
					sb.append("       join curriculumelement as ele");
					sb.append("         on ele.key = context.audienceCurriculumElement.key");
					sb.append("       join bgroupmember as membership");
					sb.append("         on membership.group.key = ele.group.key");
					sb.append("   where ra.online = true");
					sb.append("   and ra.type = '").append(QualityReportAccess.Type.GroupRoles).append("'");
					sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
					sb.append("   and membership.role = ra.role");
					sb.append("   and membership.identity.key = :reportAccessIdentityKey");
					sb.append(")");
					sb.append(" or ");
					sb.append("collection.key in (");
					sb.append("select ra.dataCollection.key");
					sb.append("  from qualityreportaccess as ra");
					sb.append("       join ra.dataCollection dc");
					sb.append("       join evaluationformsurvey survey");
					sb.append("         on survey.resId = dc.key");
					sb.append("        and survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
					sb.append("       join evaluationformparticipation as participation");
					sb.append("         on participation.survey.key = survey.key");
					sb.append("   where ra.online = true");
					sb.append("   and ra.type = '").append(QualityReportAccess.Type.Participants).append("'");
					sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
					sb.append("   and ((ra.role is null) or (participation.status = ra.role))");
					sb.append("   and participation.executor.key = :reportAccessIdentityKey");
					sb.append(")");
					sb.append(" or ");
					sb.append("collection.key in (");
					sb.append("select ra.dataCollection.key");
					sb.append("  from qualityreportaccess as ra");
					sb.append("       join ra.dataCollection dc");
					sb.append(" where ra.online = true");
					sb.append("   and ra.type = '").append(QualityReportAccess.Type.TopicIdentity).append("'");
					sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
					sb.append("   and dc.topicIdentity.key = :reportAccessIdentityKey");
					sb.append(")");
					sb.append(" or ");
					sb.append("collection.key in (");
					sb.append("select ra.dataCollection.key");
					sb.append("  from qualityreportaccess as ra");
					sb.append("       join ra.dataCollection dc");
					sb.append("       join bgroupmember as membership");
					sb.append("         on  membership.group.key = ra.group.key");
					sb.append(" where ra.online = true");
					sb.append("   and ra.type = '").append(QualityReportAccess.Type.ReportMember).append("'");
					sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
					sb.append("   and membership.identity.key = :reportAccessIdentityKey");
					sb.append(")");
					if (!searchParams.isIgnoreReportAccessRelationRole()) {
						sb.append(" or ");
						sb.append("collection.key in (");
						sb.append("select ra.dataCollection.key");
						sb.append("  from qualityreportaccess as ra");
						sb.append("       join ra.dataCollection dc");
						sb.append("       join identitytoidentity as identRel");
						sb.append("         on identRel.target.key = dc.topicIdentity.key");
						sb.append("       join relationroletoright as roleRel");
						sb.append("         on roleRel.role.key = identRel.role.key");
						sb.append(" where ra.online = true");
						sb.append("   and ra.type = '").append(QualityReportAccess.Type.RelationRole).append("'");
						sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
						sb.append("   and cast(identRel.role.key as string) = ra.role");
						sb.append("   and roleRel.right.right = '").append(QualityReportAccessRightProvider.RELATION_RIGHT).append("'");
						sb.append("   and identRel.source.key = :reportAccessIdentityKey");
						sb.append(")");
					}
				}
				if (searchParams.getLearnResourceManagerOrganisationRefs() != null) {
					sb.append(" or ");
					sb.append("collection.key in (");
					sb.append("select ra.dataCollection.key");
					sb.append("  from qualityreportaccess ra");
					sb.append("       join ra.dataCollection dc");
					sb.append("       join qualitycontext as context");
					sb.append("         on context.dataCollection.key = dc.key");
					sb.append("       join repoentrytoorganisation as re_org");
					sb.append("         on re_org.entry.key = context.audienceRepositoryEntry.key");
					sb.append("        and re_org.organisation.key in (:learnResourceManagerKeys)");
					sb.append(" where ra.online = true");
					sb.append("   and ra.type = '").append(QualityReportAccess.Type.LearnResourceManager).append("'");
					sb.append("   and dc.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
					sb.append(")");
				}
				sb.append(")");
			}
		}
	}

	private void appendParameter(TypedQuery<?> query, QualityDataCollectionViewSearchParams searchParams) {
		if (searchParams != null) {
			if (searchParams.getDataCollectionRef() != null && searchParams.getDataCollectionRef().getKey() != null) {
				query.setParameter("collectionKey", searchParams.getDataCollectionRef().getKey());
			}
			if (searchParams.getFormEntryRefs() != null) {
				List<Long> generatorKeys = searchParams.getFormEntryRefs().stream().map(RepositoryEntryRef::getKey).collect(toList());
				query.setParameter("formKeys", generatorKeys);
			}
			if (StringHelper.containsNonWhitespace(searchParams.getTitle())) {
				query.setParameter("title", PersistenceHelper.makeFuzzyQueryString(searchParams.getTitle().toLowerCase()));
			}
			if (searchParams.getStartAfter() != null) {
				query.setParameter("startAfter", searchParams.getStartAfter());
			}
			if (searchParams.getStartBefore() != null) {
				query.setParameter("startBefore", searchParams.getStartBefore());
			}
			if (searchParams.getDeadlineAfter() != null) {
				query.setParameter("deadlineAfter", searchParams.getDeadlineAfter());
			}
			if (searchParams.getDeadlineBefore() != null) {
				query.setParameter("deadlineBefore", searchParams.getDeadlineBefore());
			}
			if (StringHelper.containsNonWhitespace(searchParams.getTopic())) {
				query.setParameter("topic", PersistenceHelper.makeFuzzyQueryString(searchParams.getTopic().toLowerCase()));
			}
			if (StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
				query.setParameter("topic", PersistenceHelper.makeFuzzyQueryString(searchParams.getSearchString().toLowerCase()));
			}
			if (searchParams.getTopicTypes() != null) {
				query.setParameter("topicTypes", searchParams.getTopicTypes());
			}
			if (searchParams.getGeneratorRefs() != null) {
				List<Long> generatorKeys = searchParams.getGeneratorRefs().stream().map(QualityGeneratorRef::getKey).collect(toList());
				query.setParameter("generatorKeys", generatorKeys);
			}
			if (searchParams.getStatus() != null) {
				query.setParameter("status", searchParams.getStatus());
			}
			// (searchParams.getOrgansationRefs() == null): show all data collections
			if (searchParams.getOrgansationRefs() != null) {
				List<Long> organiationKeys = searchParams.getOrgansationRefs().stream().map(OrganisationRef::getKey).collect(toList());
				organiationKeys = !organiationKeys.isEmpty() ? organiationKeys : Collections.singletonList(-1l);
				query.setParameter("organisationKeys", organiationKeys);
				if (searchParams.getReportAccessIdentity() != null) {
					query.setParameter("reportAccessIdentityKey", searchParams.getReportAccessIdentity().getKey());
				}
				if (searchParams.getLearnResourceManagerOrganisationRefs() != null) {
					List<Long> learnResourceManagerKeys = searchParams.getLearnResourceManagerOrganisationRefs().stream()
							.map(OrganisationRef::getKey).collect(toList());
					learnResourceManagerKeys = !learnResourceManagerKeys.isEmpty() ? learnResourceManagerKeys : Collections.singletonList(-1l);
					query.setParameter("learnResourceManagerKeys", learnResourceManagerKeys);
				}
			}
		}
	}

	private void appendOrderBy(QueryBuilder sb, SortKey... orderBy) {
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
	
	private final QueryBuilder appendAsc(QueryBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}

}
