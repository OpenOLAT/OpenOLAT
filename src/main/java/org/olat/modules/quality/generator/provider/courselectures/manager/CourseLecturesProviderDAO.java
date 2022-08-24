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
package org.olat.modules.quality.generator.provider.courselectures.manager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseLecturesProviderDAO {
	
	@Autowired
	private DB dbInstance;

	public Long loadLectureBlockCount(SearchParameters searchParams) {
		if (dbInstance.isMySQL() || dbInstance.isOracle()) {
			return loadLectureBlockCountHql(searchParams);
		}
		return loadLectureBlockCountNative(searchParams);
	}
	
	private Long loadLectureBlockCountNative(SearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		appendFromNative(sb, searchParams);
		appendWhereNative(sb, searchParams);
		
		Query query = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString());
		appendParametersNative(query, searchParams);

		BigInteger count = (BigInteger) query.getResultList().get(0);
		return count.longValueExact();
	}

	private Long loadLectureBlockCountHql(SearchParameters searchParams) {
		return Long.valueOf(loadLectureBlockInfoHql(searchParams).size());
	}

	public List<LectureBlockInfo> loadLectureBlockInfo(SearchParameters searchParams) {
		if (dbInstance.isMySQL()) {
			return loadLectureBlockInfoHql(searchParams);
		}
		return loadLectureBlockInfoNative(searchParams);
	}
	
	private List<LectureBlockInfo> loadLectureBlockInfoNative(SearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ");
		sb.append("       lecture_block_key as lectureBlockKey");
		sb.append("     , teacher_key as teacherKey");
		sb.append("     , course_key as courseKey");
		sb.append("     , lecture_end_date as lectureEndDate");
		sb.append("     , total_lectures as lecturesTotal");
		sb.append("     , first_lecture as lecturesFrom");
		sb.append("     , last_lecture as lecturesTo");
		appendFromNative(sb, searchParams);
		appendWhereNative(sb, searchParams);
		
		Query query = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString());
		appendParametersNative(query, searchParams);

		List<?> rawLectureBlockInfos = query.getResultList();
		List<LectureBlockInfo> lectureBlockInfos = new ArrayList<>(rawLectureBlockInfos.size());
		for (Object rawLectureBlockInfoObject: rawLectureBlockInfos) {
			Object[] rawLectureBlockInfo = (Object[]) rawLectureBlockInfoObject;
			int pos = 0;

			Long lectureBlockKey = ((Number)rawLectureBlockInfo[pos++]).longValue();
			Long teacherKey = ((Number)rawLectureBlockInfo[pos++]).longValue();
			Long courseRepoKey = ((Number)rawLectureBlockInfo[pos++]).longValue();
			Date lectureEndDate = (Date)rawLectureBlockInfo[pos++];
			Long lecturesTotal = ((Number)rawLectureBlockInfo[pos++]).longValue();
			Long firstLecture = ((Number)rawLectureBlockInfo[pos++]).longValue();
			Long lastLecture = ((Number)rawLectureBlockInfo[pos++]).longValue();
			
			LectureBlockInfo lectureBlockInfo = new LectureBlockInfo(lectureBlockKey, teacherKey, courseRepoKey,
					lectureEndDate, lecturesTotal, firstLecture, lastLecture);
			lectureBlockInfos.add(lectureBlockInfo);
		}
		
		return lectureBlockInfos;
	}

	private void appendFromNative(QueryBuilder sb, SearchParameters searchParams) {
		sb.append("  from (          ");
		sb.append(getSubselect(searchParams));
		sb.append("    ) lectureinfos");
	}

	/**
	 * Idea to squeeze some more performance:
	 * lb.fk_entry in
	 *  (SELECT course.repositoryentry_id
	 *     FROM o_repositoryentry course
	 *          INNER JOIN o_lecture_block lb2 on lb2.fk_entry = course.repositoryentry_id
	 *                 AND lb2.l_end_date > '2002-08-23 09:52:42'
	 *                 AND lb2.l_end_date <= '2022-08-31 09:53:42'
	 *  )
	 */
	private QueryBuilder getSubselect(SearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("   select lb.id as lecture_block_key");
		sb.append("        , tm.fk_identity_id as teacher_key");
		sb.append("        , lb.fk_entry as course_key");
		sb.append("        , lb.l_end_date as lecture_end_date");
		sb.append("        , sum(lb.l_planned_lectures_num) OVER (PARTITION BY lb.fk_entry, tm.fk_identity_id ) as total_lectures");
		sb.append("        , sum(lb.l_planned_lectures_num) OVER (PARTITION BY lb.fk_entry, tm.fk_identity_id order by lb.l_end_date) - lb.l_planned_lectures_num + 1 as first_lecture");
		sb.append("        , sum(lb.l_planned_lectures_num) OVER (PARTITION BY lb.fk_entry, tm.fk_identity_id order by lb.l_end_date) as last_lecture");
		sb.append("     from o_lecture_block lb");
		sb.append("          inner join o_repositoryentry course");
		sb.append("                on course.repositoryentry_id = lb.fk_entry");
		sb.append("          inner join o_bs_group_member tm");
		sb.append("                on tm.fk_group_id = lb.fk_teacher_group");
		sb.and().append("course.status").in(RepositoryEntryStatusEnum.preparationToClosed());
		if (!searchParams.getOrganisationRefs().isEmpty()) {
			sb.and().append("exists (");
			sb.append("              select fk_entry");
			sb.append("                from o_re_to_organisation ro");
			sb.append("                   , o_org_organisation org");
			sb.append("               where ro.fk_entry = lb.fk_entry");
			sb.append("                 and ro.fk_organisation = org.id");
			sb.append("                 and ");
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("org.o_m_path_keys like :orgPath").append(i);
				if (i == searchParams.getOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
			sb.append(")");
		}
		if (!searchParams.getWhiteListRefs().isEmpty()) {
			sb.and().append("lb.fk_entry in :whiteListKeys");
		}
		if (!searchParams.getBlackListRefs().isEmpty()) {
			sb.and().append("lb.fk_entry not in :blackListKeys");
		}
		if (searchParams.getFinishedDataCollectionForGeneratorAndTopicIdentityRef() != null) {
			sb.and().append("(tm.fk_identity_id, lb.fk_entry) in (");
			sb.append("              select dc.q_topic_fk_identity, dc.q_generator_provider_key");
			sb.append("                from o_qual_data_collection dc");
			sb.append("               where dc.q_topic_fk_identity = tm.fk_identity_id");
			sb.append("                 and dc.q_generator_provider_key = lb.fk_entry");
			sb.append("                 and dc.fk_generator = :generatorIdentKey");
			sb.append("                 and dc.q_status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
			sb.append("              )");
		}
		if (searchParams.getFinishedDataCollectionForGeneratorAndTopicRepositoryRef() != null) {
			sb.and().append("(tm.fk_identity_id, lb.fk_entry) in (");
			sb.append("              select dc.q_generator_provider_key, dc.q_topic_fk_repository");
			sb.append("                from o_qual_data_collection dc");
			sb.append("               where dc.q_generator_provider_key = tm.fk_identity_id");
			sb.append("                 and dc.q_topic_fk_repository = lb.fk_entry");
			sb.append("                 and dc.fk_generator = :generatorRepoKey");
			sb.append("                 and dc.q_status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
			sb.append("              )");
		}
		if (searchParams.getExcludeGeneratorAndTopicIdentityRef() != null) {
			sb.and().append("(tm.fk_identity_id, lb.fk_entry) not in (");
			sb.append("              select dc.q_topic_fk_identity, dc.q_generator_provider_key");
			sb.append("                from o_qual_data_collection dc");
			sb.append("               where dc.fk_generator = :excludeGeneratorIdentKey");
			sb.append("              )");
		}
		if (searchParams.getExcludeGeneratorAndTopicRepositoryRef() != null) {
			sb.and().append("(tm.fk_identity_id, lb.fk_entry) not in (");
			sb.append("              select dc.q_generator_provider_key, dc.q_topic_fk_repository");
			sb.append("                from o_qual_data_collection dc");
			sb.append("               where dc.fk_generator = :excludeGeneratorRepoKey");
			sb.append("              )");
		}
		if (searchParams.getExcludedEducationalTypeKeys()!= null && !searchParams.getExcludedEducationalTypeKeys().isEmpty()) {
			sb.and().append("(course.fk_educational_type is null or course.fk_educational_type not in (:educationalTypeKeys))");
		}
		return sb;
	}

	private void appendWhereNative(QueryBuilder sb, SearchParameters searchParams) {
		if (searchParams.getFrom() != null) {
			sb.and().append("lecture_end_date > :from");
		}
		if (searchParams.getTo() != null) {
			sb.and().append("lecture_end_date <= :to");
		}
		if (searchParams.getTeacherRef() != null) {
			sb.and().append("teacher_key = :teacherKey");
		}
		if (searchParams.getMinTotalLectures() != null) {
			sb.and().append("total_lectures >= :minTotalLectures");
		}
		if (searchParams.getMaxTotalLectures() != null) {
			sb.and().append("total_lectures <= :maxTotalLectures");
		}
		if (searchParams.getSelectingLecture() != null) {
			sb.and().append("first_lecture <= :selectingLecture");
			sb.and().append("last_lecture >= :selectingLecture");
		}
		if (searchParams.isLastLectureBlock()) {
			sb.and().append("last_lecture = total_lectures");
		}
	}
	
	private void appendParametersNative(Query query, SearchParameters searchParams) {
		appendParametersHql(query, searchParams);
		if (searchParams.getMinTotalLectures() != null) {
			query.setParameter("minTotalLectures", searchParams.getMinTotalLectures());
		}
		if (searchParams.getMaxTotalLectures() != null) {
			query.setParameter("maxTotalLectures", searchParams.getMaxTotalLectures());
		}
		if (searchParams.getSelectingLecture() != null) {
			query.setParameter("selectingLecture", searchParams.getSelectingLecture());
		}
	}

	/**
	 * The native query is for large data sets - thanks to the window functions -
	 * very much faster. The hql query is a fallback to the native query for MySQL.
	 * MySQL introduced window function in version 8. So use the native query and
	 * delete the hql query if OpenOLAT does not to have to support MySQL prior to
	 * version 8.
	 *
	 * @param searchParams
	 * @return
	 */
	private List<LectureBlockInfo> loadLectureBlockInfoHql(SearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo(");
		sb.append("       lectureblock.key");
		sb.append("     , membership.identity.key");
		sb.append("     , course.key");
		sb.append("     , lectureblock.endDate");
		sb.append("     , (");
		sb.append("         select sum(lectureblock2.plannedLecturesNumber)");
		sb.append("           from lectureblock as lectureblock2");
		sb.append("                inner join lectureblock2.entry as course2");
		sb.append("                inner join lectureblock2.teacherGroup as teacherGroup2");
		sb.append("                inner join teacherGroup2.members as membership2");
		sb.append("          where course2.key = course.key");
		sb.append("            and membership2.identity.key = membership.identity.key");
		sb.append("       ) as lectures_total");
		sb.append("     , (");
		sb.append("         select 1 + sum(lectureblock2.plannedLecturesNumber)");
		sb.append("           from lectureblock as lectureblock2");
		sb.append("                inner join lectureblock2.entry as course2");
		sb.append("                inner join lectureblock2.teacherGroup as teacherGroup2");
		sb.append("                inner join teacherGroup2.members as membership2");
		sb.append("          where course2.key = course.key");
		sb.append("            and membership2.identity.key = membership.identity.key");
		sb.append("            and lectureblock2.endDate < lectureblock.endDate");
		sb.append("       ) as lectures_from");
		sb.append("     , (");
		sb.append("         select sum(lectureblock2.plannedLecturesNumber)");
		sb.append("           from lectureblock as lectureblock2");
		sb.append("                inner join lectureblock2.entry as course2");
		sb.append("                inner join lectureblock2.teacherGroup as teacherGroup2");
		sb.append("                inner join teacherGroup2.members as membership2");
		sb.append("          where course2.key = course.key");
		sb.append("            and membership2.identity.key = membership.identity.key");
		sb.append("            and lectureblock2.endDate <= lectureblock.endDate");
		sb.append("       ) as lectures_to");
		sb.append("     )");
		appendFromHql(sb);
		appendWhereHql(sb, searchParams);
		
		TypedQuery<LectureBlockInfo> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockInfo.class);
		appendParametersHql(query, searchParams);
		
		List<LectureBlockInfo> infos = query.getResultList();
		
		if (searchParams.getMinTotalLectures() != null) {
			infos.removeIf(lb -> lb.getLecturesTotal() < searchParams.getMinTotalLectures());
		}
		if (searchParams.getMaxTotalLectures() != null) {
			infos.removeIf(lb -> lb.getLecturesTotal() > searchParams.getMaxTotalLectures());
		}
		if (searchParams.getSelectingLecture() != null) {
			infos.removeIf(lb -> 
					   lb.getFirstLecture() > searchParams.getSelectingLecture() 
					|| lb.getLastLecture() < searchParams.getSelectingLecture());
		}
		if (searchParams.isLastLectureBlock()) {
			infos.removeIf(lb -> !lb.getLastLecture().equals(lb.getLecturesTotal()));
		}
		return infos;
	}
	
	private void appendFromHql(QueryBuilder sb) {
		sb.append("  from lectureblock as lectureblock");
		sb.append("       inner join lectureblock.entry as course");
		sb.append("       inner join lectureblock.teacherGroup as teacherGroup");
		sb.append("       inner join teacherGroup.members as membership");
	}

	private void appendWhereHql(QueryBuilder sb, SearchParameters searchParams) {
		sb.and().append("course.status").in(RepositoryEntryStatusEnum.preparationToClosed());
		if (searchParams.getFinishedDataCollectionForGeneratorAndTopicIdentityRef() != null) {
			sb.and();
			sb.append("(membership.identity.key, course.key) in (");
			sb.append("    select datacollection.topicIdentity.key, datacollection.generatorProviderKey");
			sb.append("      from qualitydatacollection as datacollection");
			sb.append("     where datacollection.generator.key = :generatorIdentKey");
			sb.append("       and datacollection.topicIdentity.key = membership.identity.key");
			sb.append("       and datacollection.generatorProviderKey = course.key");
			sb.append("       and datacollection.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
			sb.append("    )");
		}
		if (searchParams.getFinishedDataCollectionForGeneratorAndTopicRepositoryRef() != null) {
			sb.and();
			sb.append("(membership.identity.key, course.key) in (");
			sb.append("    select datacollection.generatorProviderKey, datacollection.topicRepositoryEntry.key");
			sb.append("      from qualitydatacollection as datacollection");
			sb.append("     where datacollection.generator.key = :generatorRepoKey");
			sb.append("       and datacollection.topicRepositoryEntry.key = course.key");
			sb.append("       and datacollection.generatorProviderKey = membership.identity.key");
			sb.append("       and datacollection.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
			sb.append("    )");
		}
		if (searchParams.getExcludeGeneratorAndTopicIdentityRef() != null) {
			sb.and();
			sb.append("(membership.identity.key, course.key) not in (");
			sb.append("    select datacollection.topicIdentity.key, datacollection.generatorProviderKey");
			sb.append("      from qualitydatacollection as datacollection");
			sb.append("     where datacollection.generator.key = :excludeGeneratorIdentKey");
			sb.append("       and datacollection.topicIdentity.key = membership.identity.key");
			sb.append("       and datacollection.generatorProviderKey = course.key");
			sb.append("    )");
		}
		if (searchParams.getExcludeGeneratorAndTopicRepositoryRef() != null) {
			sb.and();
			sb.append("(membership.identity.key, course.key) not in (");
			sb.append("    select datacollection.generatorProviderKey, datacollection.topicRepositoryEntry.key");
			sb.append("      from qualitydatacollection as datacollection");
			sb.append("     where datacollection.generator.key = :excludeGeneratorRepoKey");
			sb.append("       and datacollection.topicRepositoryEntry.key = course.key");
			sb.append("       and datacollection.generatorProviderKey = membership.identity.key");
			sb.append("    )");
		}
		if (searchParams.getTeacherRef() != null) {
			sb.and().append("membership.identity.key = :teacherKey");
		}
		if (!searchParams.getWhiteListRefs().isEmpty()) {
			sb.and().append("course.key in :whiteListKeys");
		}
		if (!searchParams.getBlackListRefs().isEmpty()) {
			sb.and().append("course.key not in :blackListKeys");
		}
		if (!searchParams.getOrganisationRefs().isEmpty()) {
			sb.and();
			sb.append("course.key in (");
			sb.append("    select courseOrg.entry.key");
			sb.append("      from repoentrytoorganisation courseOrg");
			sb.append("     where ");
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append("courseOrg.organisation.materializedPathKeys like :orgPath").append(i);
				if (i == searchParams.getOrganisationRefs().size() - 1) {
					sb.append(")");
				}
			}
			sb.append(")");
		}
		if (searchParams.getFrom() != null) {
			sb.and().append("lectureblock.endDate > :from");
		}
		if (searchParams.getTo() != null) {
			sb.and().append("lectureblock.endDate <= :to");
		}
		if (searchParams.getExcludedEducationalTypeKeys()!= null && !searchParams.getExcludedEducationalTypeKeys().isEmpty()) {
			sb.and().append("(course.educationalType.key is null or course.educationalType.key not in (:educationalTypeKeys))");
		}
	}

	private void appendParametersHql(Query query, SearchParameters searchParams) {
		if (searchParams.getFinishedDataCollectionForGeneratorAndTopicIdentityRef() != null) {
			query.setParameter("generatorIdentKey", searchParams.getFinishedDataCollectionForGeneratorAndTopicIdentityRef().getKey());
		}
		if (searchParams.getFinishedDataCollectionForGeneratorAndTopicRepositoryRef() != null) {
			query.setParameter("generatorRepoKey", searchParams.getFinishedDataCollectionForGeneratorAndTopicRepositoryRef().getKey());
		}
		if (searchParams.getExcludeGeneratorAndTopicIdentityRef() != null) {
			query.setParameter("excludeGeneratorIdentKey", searchParams.getExcludeGeneratorAndTopicIdentityRef().getKey());
		}
		if (searchParams.getExcludeGeneratorAndTopicRepositoryRef() != null) {
			query.setParameter("excludeGeneratorRepoKey", searchParams.getExcludeGeneratorAndTopicRepositoryRef().getKey());
		}
		if (searchParams.getTeacherRef() != null) {
			query.setParameter("teacherKey", searchParams.getTeacherRef().getKey());
		}
		if (!searchParams.getWhiteListRefs().isEmpty()) {
			List<Long> whiteListKeys = searchParams.getWhiteListRefs().stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
			query.setParameter("whiteListKeys", whiteListKeys);
		}
		if (!searchParams.getBlackListRefs().isEmpty()) {
			List<Long> blackListKeys = searchParams.getBlackListRefs().stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
			query.setParameter("blackListKeys", blackListKeys);
		}
		if (!searchParams.getOrganisationRefs().isEmpty()) {
			for (int i = 0; i < searchParams.getOrganisationRefs().size(); i++) {
				String parameter = new StringBuilder(12).append("orgPath").append(i).toString();
				Long key = searchParams.getOrganisationRefs().get(i).getKey();
				String value = new StringBuilder(32).append("%/").append(key).append("/%").toString();
				query.setParameter(parameter, value);
			}
		}
		if (searchParams.getFrom() != null) {
			query.setParameter("from", searchParams.getFrom());
		}
		if (searchParams.getTo() != null) {
			query.setParameter("to", searchParams.getTo());
		}
		if (searchParams.getExcludedEducationalTypeKeys() != null && !searchParams.getExcludedEducationalTypeKeys().isEmpty()) {
			query.setParameter("educationalTypeKeys", searchParams.getExcludedEducationalTypeKeys());
		}
	}

}
