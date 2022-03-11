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
package org.olat.course.assessment.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CoachingAssessmentEntry;
import org.olat.course.assessment.CoachingAssessmentSearchParams;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.CoachingAssessmentEntryImpl;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.grading.GradingAssignment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Work with the datas for the assessment tool
 * 
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentToolManagerImpl implements AssessmentToolManager {

	@Autowired
	private DB dbInstance;

	@Override
	public int getNumberOfAssessedIdentities(Identity coach, SearchAssessedIdentityParams params) {
		if (params.isAdmin() && params.isNonMembers() && params.isNonMemebersOnly() && (params.hasBusinessGroupKeys() || params.hasCurriculumElementKeys())) {
			return 0;
		}
		
		TypedQuery<Long> participantsQuery = createAssessedParticipants(coach, params, Long.class);
		return participantsQuery.getResultList().size();
	}

	@Override
	public AssessmentMembersStatistics getNumberOfParticipants(Identity coach, SearchAssessedIdentityParams params) {
		RepositoryEntry courseEntry = params.getEntry();
		
		int othersLoggedIn = 0;
		int numOfOtherUsers = 0;
		int numOfParticipants = 0;
		int participantLoggedIn = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select infos.identity.key from usercourseinfos as infos")
		  .append(" inner join infos.resource as infosResource on (infosResource.key=:resourceKey)");
		List<Long> launchedKeys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("resourceKey", courseEntry.getOlatResource().getKey())
			.getResultList();
		
		if(params.isAdmin()) {
			sb = new StringBuilder();
			sb.append("select participant.identity.key from repoentrytogroup as rel")
	          .append("  inner join rel.group as bGroup")
	          .append("  inner join bGroup.members as participant on (participant.role='").append(GroupRoles.participant.name()).append("')")
	          .append("  where rel.entry.key=:repoEntryKey");
			
			List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repoEntryKey", courseEntry.getKey())
				.getResultList();
			Set<Long> participantKeys = new HashSet<>(keys);
			numOfParticipants = participantKeys.size();
			
			Set<Long> participantLoggedInKeys = new HashSet<>(participantKeys);
			participantLoggedInKeys.retainAll(launchedKeys);
			participantLoggedIn = participantLoggedInKeys.size();

			//count the users which login but are not members of the course
			if(params.isNonMembers()) {
				sb = new StringBuilder();
				sb.append("select ae.identity.key");
				sb.append("  from assessmententry ae");
				sb.append(" where ae.identity.key is not null"); // exclude anonymous 
				sb.append("   and ae.repositoryEntry.key = :entryKey");
				if(params.getSubIdent() != null) {
					sb.append(" and ae.subIdent=:subIdent");
				}
				TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Long.class)
					.setParameter("entryKey", courseEntry.getKey());
				if(params.getSubIdent() != null) {
					query.setParameter("subIdent", params.getSubIdent());
				}
				List<Long> allKeys =  query.getResultList();
				
				Set<Long> otherKeys = new HashSet<>(allKeys);
				otherKeys.removeAll(participantKeys);
				numOfOtherUsers = otherKeys.size();
				otherKeys.retainAll(launchedKeys);
				othersLoggedIn = otherKeys.size();
			}
		} else if(params.isCoach()) {
			sb = new StringBuilder();
			sb.append("select participant.identity.key from repoentrytogroup as rel")
	          .append("  inner join rel.group as bGroup")
	          .append("  inner join bGroup.members as coach on (coach.identity.key=:identityKey and coach.role='").append(GroupRoles.coach.name()).append("')")
	          .append("  inner join bGroup.members as participant on (participant.role='").append(GroupRoles.participant.name()).append("')")
	          .append("  where rel.entry.key=:repoEntryKey");
			
			List<Long> keys  = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", coach.getKey())
				.setParameter("repoEntryKey", courseEntry.getKey())
				.getResultList();

			Set<Long> participantKeys = new HashSet<>(keys);
			numOfParticipants = participantKeys.size();
			
			Set<Long> participantLoggedInKeys = new HashSet<>(keys);
			participantLoggedInKeys.retainAll(launchedKeys);
			participantLoggedIn = participantLoggedInKeys.size();
		}
		return new AssessmentMembersStatistics(numOfParticipants, participantLoggedIn, numOfOtherUsers, othersLoggedIn);
	}
	
	@Override
	public AssessmentStatistics getStatistics(Identity coach, SearchAssessedIdentityParams params) {
		RepositoryEntry courseEntry = params.getEntry();

		QueryBuilder sf = new QueryBuilder();
		sf.append("select avg(aentry.score) as scoreAverage, ")
		  .append(" sum(case when aentry.passed=true then 1 else 0 end) as numOfPassed,")
		  .append(" sum(case when aentry.passed=false then 1 else 0 end) as numOfFailed,")
		  //.append(" sum(case when (aentry.status is null or not(aentry.status='").append(AssessmentEntryStatus.notStarted.name()).append("') or aentry.passed is null) then 1 else 0 end) as numOfNotAttempted,")
		  //.append(" sum(aentry.key) as numOfStatements,")
		  .append(" v.key as repoKey")
		  .append(" from assessmententry aentry ")
		  .append(" inner join aentry.repositoryEntry v ")
		  .append(" where v.key=:repoEntryKey");
		if(params.getReferenceEntry() != null) {
			sf.append(" and aentry.referenceEntry.key=:referenceKey");
		}
		if(params.getSubIdent() != null) {
			sf.append(" and aentry.subIdent=:subIdent");
		}
		if(params.getAssessmentObligations() != null && !params.getAssessmentObligations().isEmpty()) {
			sf.append(" and (");
			if (params.getAssessmentObligations().contains(AssessmentObligation.mandatory)) {
				sf.append("aentry.obligation is null or ");
			}
			sf.append(" aentry.obligation in (:assessmentObligations))");
		}
		sf.append(" and (aentry.identity.key in");
		if(params.isAdmin()) {
			if (params.isMemebersOnly() || !params.isNonMembers()) {
				sf.append(" (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
				  .append("    where rel.entry.key=:repoEntryKey and rel.group.key=participant.group.key")
				  .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
				  .append("  )");
			} else {
				sf.append(" (select ae.identity.key from assessmententry ae")
				  .append(" where ae.identity.key is not null") // exclude anonymous 
				  .append("   and ae.repositoryEntry.key = :repoEntryKey");
				if(params.getSubIdent() != null) {
					sf.append(" and ae.subIdent=:subIdent");
				}
				sf.append("  )");
			}
		} else if(params.isCoach()) {
			sf.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group.key=coach.group.key and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group.key=participant.group.key and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}
		sf.append(" ) group by v.key");

		TypedQuery<Object[]> stats = dbInstance.getCurrentEntityManager()
			.createQuery(sf.toString(), Object[].class)
			.setParameter("repoEntryKey", courseEntry.getKey());
		if(!params.isAdmin()) {
			stats.setParameter("identityKey", coach.getKey());
		}
		if(params.getReferenceEntry() != null) {
			stats.setParameter("referenceKey", params.getReferenceEntry().getKey());
		}
		if(params.getSubIdent() != null) {
			stats.setParameter("subIdent", params.getSubIdent());
		}
		if(params.getAssessmentObligations() != null && !params.getAssessmentObligations().isEmpty()) {
			stats.setParameter("assessmentObligations", params.getAssessmentObligations());
		}

		AssessmentStatistics entry = new AssessmentStatistics();
		List<Object[]> results = stats.getResultList();
		if(results != null && !results.isEmpty()) {
			Object[] result = results.get(0);
			Double averageScore = (Double)result[0];
			Long numOfPassed = (Long)result[1];
			Long numOfFailed = (Long)result[2];
			
			entry.setAverageScore(averageScore);
			entry.setCountPassed(numOfPassed == null ? 0 : numOfPassed.intValue());
			entry.setCountFailed(numOfFailed == null ? 0 : numOfFailed.intValue());
		}
		return entry;
	}

	@Override
	public List<Identity> getAssessedIdentities(Identity coach, SearchAssessedIdentityParams params) {
		if (params.isAdmin() && params.isNonMembers() && params.isNonMemebersOnly() && (params.hasBusinessGroupKeys() || params.hasCurriculumElementKeys())) {
			return new ArrayList<>(0);
		}
		
		TypedQuery<Identity> list = createAssessedParticipants(coach, params, Identity.class);
		return list.getResultList();
	}
	
	private <T> TypedQuery<T> createAssessedParticipants(Identity coach, SearchAssessedIdentityParams params, Class<T> classResult) {
		boolean needsSubIdent = false;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ");
		if(Identity.class.equals(classResult)) {
			sb.append("ident").append(" from ").append(IdentityImpl.class.getName()).append(" as ident")
			  .append(" inner join fetch ident.user user");
		} else {
			sb.append(" ident.key").append(" from ").append(IdentityImpl.class.getName()).append(" as ident");
		}
		sb.append(" where ident.status<").append(Identity.STATUS_DELETED).append(" and");
		
		if (params.isAdmin() && params.isNonMembers() && params.isNonMemebersOnly()) {
			if (!params.hasBusinessGroupKeys() && !params.hasCurriculumElementKeys()) {
				sb.append(" ident.key not in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
				  .append("    where rel.entry.key=:repoEntryKey and rel.group.key=participant.group.key")
				  .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
				  .append("  ) and ")
				  .append(" ident.key in (select ae.identity.key from assessmententry ae")
				  .append(" where ae.identity.key is not null") // exclude anonymous 
				  .append("   and ae.repositoryEntry.key = :repoEntryKey");
				if(params.getSubIdent() != null) {
					sb.append(" and ae.subIdent=:subIdent");
					needsSubIdent = true;
				}
				sb.append("  )");
			}
		} else if((params.hasBusinessGroupKeys() || params.hasCurriculumElementKeys())) {
			sb.append("(");
			if(params.hasBusinessGroupKeys()) {
				sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, businessgroup bgi, bgroupmember as participant")
				.append("    where rel.entry.key=:repoEntryKey and rel.group.key=bgi.baseGroup.key and rel.group.key=participant.group.key and bgi.key in (:businessGroupKeys) ")
				  .append("    and participant.role='").append(GroupRoles.participant.name()).append("'")
				  .append("  )");
			}
			if(params.hasCurriculumElementKeys()) {
				if(params.hasBusinessGroupKeys()) {
					sb.append(" or ");
				}
				sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, curriculumelement curEl, bgroupmember as participant")
				  .append("    where rel.entry.key=:repoEntryKey and rel.group.key=curEl.group.key and rel.group.key=participant.group.key and curEl.key in (:curriculumElementKeys) ")
				  .append("    and participant.role='").append(GroupRoles.participant.name()).append("'")
				  .append("  )");
			}
			sb.append(")");
		} else if(params.isAdmin()) {
			if (params.isMemebersOnly() || !params.isNonMembers()) {
				sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
				  .append("    where rel.entry.key=:repoEntryKey and rel.group.key=participant.group.key")
				  .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
				  .append("  )");
			} else {
				sb.append(" ident.key in (select ae.identity.key from assessmententry ae")
				  .append(" where ae.identity.key is not null") // exclude anonymous 
				  .append("   and ae.repositoryEntry.key = :repoEntryKey");
				if(params.getSubIdent() != null) {
					sb.append(" and ae.subIdent=:subIdent");
					needsSubIdent = true;
				}
				sb.append("  )");
			}
		} else if(params.isCoach()) {
			sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group.key=coach.group.key and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group.key=participant.group.key and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}
		
		Long identityKey = appendUserSearchByKey(sb, params.getSearchString());
		String[] searchArr = appendUserSearchFull(sb, params.getSearchString(), identityKey == null);

		TypedQuery<T> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), classResult)
				.setParameter("repoEntryKey", params.getEntry().getKey());
		if(params.isCoach() && !params.isAdmin() && !params.hasBusinessGroupKeys() && !params.hasCurriculumElementKeys()) {
			query.setParameter("identityKey", coach.getKey());
		}
		if(identityKey != null) {
			query.setParameter("searchIdentityKey", identityKey);
		}
		if(needsSubIdent) {
			query.setParameter("subIdent", params.getSubIdent());
		}
		if(params.hasBusinessGroupKeys() && !params.isNonMemebersOnly()) {
			query.setParameter("businessGroupKeys", params.getBusinessGroupKeys());
		}
		if(params.hasCurriculumElementKeys() && !params.isNonMemebersOnly()) {
			query.setParameter("curriculumElementKeys", params.getCurriculumElementKeys());
		}
		appendUserSearchToQuery(searchArr, query);
		return query;
	}
	
	private Long appendUserSearchByKey(QueryBuilder sb, String search) {
		Long identityKey = null;
		if(StringHelper.containsNonWhitespace(search)) {
			if(StringHelper.isLong(search)) {
				try {
					identityKey = Long.valueOf(search);
				} catch (NumberFormatException e) {
					//it can happens
				}
			}
			
			if(identityKey != null) {
				sb.append(" and ident.key=:searchIdentityKey");
			}
		}
		return identityKey;

	}
	
	private String[] appendUserSearchFull(QueryBuilder sb, String search, boolean and) {
		String[] searchArr = null;

		if(StringHelper.containsNonWhitespace(search)) {
			String dbVendor = dbInstance.getDbVendor();
			searchArr = search.split(" ");
			String[] attributes = new String[]{ "firstName", "lastName", "email" };

			if(and) {
				sb.append(" and (");
			} else {
				sb.append(" or (");
			}
			boolean start = true;
			for(int i=0; i<searchArr.length; i++) {
				for(String attribute:attributes) {
					if(start) {
						start = false;
					} else {
						sb.append(" or ");
					}
					
					if(dbVendor.equals("mysql")) {
						sb.append(" user.").append(attribute).append(" like :search").append(i).append(" ");
					} else {
						sb.append(" lower(user.").append(attribute).append(") like :search").append(i).append(" ");
					}
					if(dbVendor.equals("oracle")) {
						sb.append(" escape '\\'");
					}
				}
			}
			sb.append(")");
		}
		return searchArr;
	}
	
	private void appendUserSearchToQuery(String[] searchArr, TypedQuery<?> query) {
		if(searchArr != null) {
			for(int i=searchArr.length; i-->0; ) {
				query.setParameter("search" + i, PersistenceHelper.makeFuzzyQueryString(searchArr[i]));
			}
		}
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select aentry from assessmententry aentry")
		  .append(" inner join fetch aentry.identity as assessedIdentity")
		  .append(" inner join fetch assessedIdentity.user as assessedUser");
		applySearchAssessedIdentityParams(sb, params, status);
		
		TypedQuery<AssessmentEntry> list = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentEntry.class);
		
		applySearchAssessedIdentityParams(list, coach, params, status);
		
		return list.getResultList();
	}
	
	@Override
	public List<GradingAssignment> getGradingAssignments(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" inner join fetch assignment.assessmentEntry as aentry")
		  .append(" inner join aentry.identity as assessedIdentity")
		  .append(" inner join fetch assignment.grader as grader")
		  .append(" inner join fetch grader.identity as graderIdent")
		  .append(" inner join fetch graderIdent.user as graderUser");
		applySearchAssessedIdentityParams(sb, params, status);
		
		TypedQuery<GradingAssignment> list = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), GradingAssignment.class);

		applySearchAssessedIdentityParams(list, coach, params, status);
		
		return list.getResultList();
	}
	
	private void applySearchAssessedIdentityParams(TypedQuery<?> list, Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		list.setParameter("repoEntryKey", params.getEntry().getKey());
		if(params.getReferenceEntry() != null) {
			list.setParameter("referenceKey", params.getReferenceEntry().getKey());
		}
		if(params.getSubIdent() != null) {
			list.setParameter("subIdent", params.getSubIdent());
		}
		if(!params.isAdmin()) {
			list.setParameter("identityKey", coach.getKey());
		}
		if(status != null) {
			list.setParameter("assessmentStatus", status.name());
		}
		if(params.getUserVisibility() != null) {
			list.setParameter("userVisibility", params.getUserVisibility());
		}
		if(params.getAssessmentObligations() != null && !params.getAssessmentObligations().isEmpty()) {
			list.setParameter("assessmentObligations", params.getAssessmentObligations());
		}
		if(params.getUserProperties() != null && !params.getUserProperties().isEmpty()) {
			for(Map.Entry<String, String> entry:params.getUserProperties().entrySet()) {
				String fuzzyValue = PersistenceHelper.makeFuzzyQueryString(entry.getValue());
				list.setParameter("uprop" + entry.getKey(), fuzzyValue);
			}
		}
	}
	
	private void applySearchAssessedIdentityParams(QueryBuilder sb, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		sb.append(" where aentry.repositoryEntry.key=:repoEntryKey");
		if(params.getReferenceEntry() != null) {
			sb.append(" and aentry.referenceEntry.key=:referenceKey");
		}
		if(params.getSubIdent() != null) {
			sb.append(" and aentry.subIdent=:subIdent");
		}
		if(status != null) {
			sb.append(" and aentry.status=:assessmentStatus");
		}
		if(params.getUserVisibility() != null) {
			sb.append(" and (");
			if (params.getUserVisibility().booleanValue()) {
				sb.append("aentry.userVisibility is null or ");
			}
			sb.append(" aentry.userVisibility = :userVisibility)");
		}
		if(params.getAssessmentObligations() != null && !params.getAssessmentObligations().isEmpty()) {
			sb.append(" and (");
			if (params.getAssessmentObligations().contains(AssessmentObligation.mandatory)) {
				sb.append("aentry.obligation is null or ");
			}
			sb.append(" aentry.obligation in (:assessmentObligations))");
		}
		sb.append(" and (aentry.identity.key in");
		if(params.isAdmin()) {
			if (params.isMemebersOnly() || !params.isNonMembers()) {
				sb.append(" (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
				  .append("    where rel.entry.key=:repoEntryKey and rel.group.key=participant.group.key")
				  .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
				  .append("  )");
			} else {
				sb.append(" (select ae.identity.key from assessmententry ae")
				  .append(" where ae.identity.key is not null") // exclude anonymous 
				  .append("   and ae.repositoryEntry.key = :repoEntryKey");
				if(params.getSubIdent() != null) {
					sb.append(" and ae.subIdent=:subIdent");
				}
				sb.append("  )");
			}
		} else if(params.isCoach()) {
			sb.append(" (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}
		sb.append(" )");
		
		if(params.getUserProperties() != null && !params.getUserProperties().isEmpty()) {
			for(Map.Entry<String, String> entry:params.getUserProperties().entrySet()) {
				sb.append(" and ")
				  .appendFuzzyLike("assessedUser." + entry.getKey(), "uprop" + entry.getKey());
			}
		}
	}

	@Override
	public AssessmentEntry getAssessmentEntries(IdentityRef assessedIdentity, RepositoryEntry entry, String subIdent) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select aentry from assessmententry aentry")
		  .append(" inner join fetch aentry.identity as assessedIdentity")
		  .append(" inner join fetch assessedIdentity.user as assessedUser")
		  .append(" where aentry.repositoryEntry.key=:repoEntryKey")
		  .append(" and assessedIdentity.key=:identityKey");
		if(subIdent != null) {
			sb.append(" and aentry.subIdent=:subIdent");
		}
		
		TypedQuery<AssessmentEntry> list = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repoEntryKey", entry.getKey())
				.setParameter("identityKey", assessedIdentity.getKey());
		if(subIdent != null) {
			list.setParameter("subIdent", subIdent);
		}
		List<AssessmentEntry> entries = list.getResultList();
		return entries == null || entries.isEmpty() ? null : entries.get(0);
	}

	@Override
	public List<CoachingAssessmentEntry> getCoachingEntries(CoachingAssessmentSearchParams params) {
		// This method queries the assessment entries of all relevant repository entries.
		// In a second step the users without a relation to the coach are excluded.
		// In this second step some other search parameters are applied.
		List<CoachingAssessmentEntryImpl> loadedCoachedEntries = loadCoachingEntries(params);
		
		Map<Long, Identity> assessedIdentityKeyToIdentity = loadAssessedIdentities(loadedCoachedEntries, params).stream()
				.collect(Collectors.toMap(Identity::getKey, Function.identity()));
		Map<Long, Identity> statusDoneByIdentityKeyToIdentity = loadStatusDoneByIdentities(loadedCoachedEntries, params).stream()
				.collect(Collectors.toMap(Identity::getKey, Function.identity()));
		List<RepositoryEntry> repositoryEntries = loadRepositoryEntries(loadedCoachedEntries);
		Map<Long, RepositoryEntry> repoKeyToEntry = repositoryEntries.stream()
				.collect(Collectors.toMap(RepositoryEntry::getKey, Function.identity()));
		Map<Long, Boolean> repoKeyToCoachUserVisibilitySettable = params.isUserVisibilitySettable()
				? getCoachUserVisibleSettable(repositoryEntries)
				: Collections.emptyMap();
		
		List<CoachingAssessmentEntry> coachedEntries = new ArrayList<>();
		for (CoachingAssessmentEntryImpl coachedEntry : loadedCoachedEntries) {
			filterAndAppend(coachedEntries, params, coachedEntry, assessedIdentityKeyToIdentity,
					statusDoneByIdentityKeyToIdentity, repoKeyToEntry, repoKeyToCoachUserVisibilitySettable);
		}
		
		return coachedEntries;
	}

	private void filterAndAppend(List<CoachingAssessmentEntry> coachedEntries, CoachingAssessmentSearchParams params,
			CoachingAssessmentEntryImpl coachedEntry, Map<Long, Identity> assessedIdentityKeyToIdentity,
			Map<Long, Identity> statusDoneByIdentityKeyToIdentity, Map<Long, RepositoryEntry> repoKeyToEntry,
			Map<Long, Boolean> repoKeyToCoachUserVisibilitySettable) {
		if (!coachedEntry.isOwner() && !coachedEntry.isCoach()) {
			return;
		}
		
		Identity identity = assessedIdentityKeyToIdentity.get(coachedEntry.getAssessedIdentityKey());
		if (identity == null) {
			return;
		}
		coachedEntry.setAssessedIdentity(identity);
		
		// Remove if not course or decommissioned
		RepositoryEntry repositoryEntry = repoKeyToEntry.get(coachedEntry.getRepositoryEntryKey());
		if (repositoryEntry == null) {
			return;
		}
		coachedEntry.setRepositoryEntryName(repositoryEntry.getDisplayname());
		
		if (params.isUserVisibilitySettable() && !canSetUserVisibility(coachedEntry, repoKeyToCoachUserVisibilitySettable)) {
			return;
		}
		
		Identity statusDoneBy = statusDoneByIdentityKeyToIdentity.get(coachedEntry.getStatusDoneByKey());
		coachedEntry.setStatusDoneBy(statusDoneBy);
		
		coachedEntries.add(coachedEntry);
	}

	private List<CoachingAssessmentEntryImpl> loadCoachingEntries(CoachingAssessmentSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.course.assessment.model.CoachingAssessmentEntryImpl(");
		sb.append("   aentry.key");
		sb.append(" , aentry.identity.key");
		sb.append(" , aentry.repositoryEntry.key");
		sb.append(" , aentry.subIdent");
		sb.append(" , courseele.type");
		sb.append(" , courseele.shortTitle");
		sb.append(" , courseele.longTitle");
		sb.append(" , aentry.lastUserModified");
		sb.append(" , aentry.assessmentDoneBy.key");
		sb.append(" , aentry.assessmentDone");
		sb.append(" , (");
		sb.append("      select count(*) > 0 from repoentrytogroup as rel, bgroupmember as owner");
		sb.append("       where rel.entry.key=aentry.repositoryEntry.key");
		sb.append("         and rel.group=owner.group and owner.role='").append(GroupRoles.owner.name()).append("' and owner.identity.key=:identityKey");
		sb.append("   ) as owner");
		sb.append(" , (");
		sb.append("      select count(*) > 0 from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach");
		sb.append("       where rel.entry.key=aentry.repositoryEntry.key");
		sb.append("         and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey");
		sb.append("         and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("' and participant.identity=aentry.identity");
		sb.append("   ) as coach");
		sb.append(")");
		sb.append("  from assessmententry aentry");
		sb.append("       inner join courseelement courseele");
		sb.append("               on courseele.repositoryEntry.key = aentry.repositoryEntry.key");
		sb.append("              and courseele.subIdent = aentry.subIdent");
		sb.append("              and courseele.assesseable is true");
		sb.append("              and (courseele.scoreMode = '").append(Mode.setByNode).append("'");
		sb.append("                   or courseele.passedMode = '").append(Mode.setByNode).append("')");
		sb.and().append("(aentry.obligation is null or aentry.obligation <> '").append(AssessmentObligation.excluded).append("')");
		sb.and().append(" exists (select 1");
		sb.append("                 from repoentrytogroup as rtg, bgroupmember as rtgm");
		sb.append("                where rtg.entry.key=aentry.repositoryEntry.key");
		sb.append("                  and rtg.group=rtgm.group and rtgm.role").in(GroupRoles.owner, GroupRoles.coach);
		sb.append("                  and rtgm.identity.key=:identityKey");
		sb.append(")");
		if (params.getStatus() != null) {
			sb.and().append("aentry.status = :status");
		}
		if (params.getUserVisibility() != null) {
			sb.and().append("aentry.userVisibility = :userVisibility");
		}
		
		TypedQuery<CoachingAssessmentEntryImpl> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CoachingAssessmentEntryImpl.class)
				.setParameter("identityKey", params.getCoach().getKey());
		if (params.getStatus() != null) {
			query.setParameter("status", params.getStatus().name());
		}
		if (params.getUserVisibility() != null) {
			query.setParameter("userVisibility", params.getUserVisibility());
		}
		
		return query.getResultList();
	}
	
	private List<Identity> loadAssessedIdentities(List<CoachingAssessmentEntryImpl> coachedEntries, CoachingAssessmentSearchParams params) {
		if (coachedEntries == null || coachedEntries.isEmpty()) return Collections.emptyList();
		
		Set<Long> identityKeys = coachedEntries.stream()
				.map(CoachingAssessmentEntryImpl::getAssessedIdentityKey)
				.collect(Collectors.toSet());
		
		return loadIdentities(identityKeys, params.getSearchString());
	}
	
	private List<Identity> loadStatusDoneByIdentities(List<CoachingAssessmentEntryImpl> coachedEntries, CoachingAssessmentSearchParams params) {
		if (coachedEntries == null || coachedEntries.isEmpty()) return Collections.emptyList();
		
		Set<Long> identityKeys = coachedEntries.stream()
				.map(CoachingAssessmentEntryImpl::getStatusDoneByKey)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		return loadIdentities(identityKeys, params.getSearchString());
	}
	
	private List<Identity> loadIdentities(Collection<Long> identityKeys, String searchString) {
		if (identityKeys == null || identityKeys.isEmpty()) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident from ").append(Identity.class.getName()).append(" as ident");
		sb.append(" inner join fetch ident.user user");
		sb.and().append(" ident.key in (:identityKeys)");
		sb.and().append(" ident.status<").append(Identity.STATUS_DELETED);
		String[] searchArr = appendUserSearchFull(sb, searchString, true);
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("identityKeys", identityKeys);
		appendUserSearchToQuery(searchArr, query);
		
		return query.getResultList();
	}
	
	private List<RepositoryEntry> loadRepositoryEntries(List<CoachingAssessmentEntryImpl> coachedEntries) {
		if (coachedEntries == null || coachedEntries.isEmpty()) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v");
		sb.append("  from ").append(RepositoryEntry.class.getName()).append(" as v ");
		sb.append("        inner join fetch v.olatResource as ores"); // Ores is not uses but this join avoids 1:n selects?!
		sb.and().append("v.key in (:repoKeys)");
		sb.and().append("v.status ").in(RepositoryEntryStatusEnum.preparationToPublished());
		sb.and().append("ores.resName ='CourseModule'");

		Set<Long> repositoryEntryKeys = coachedEntries.stream()
				.map(CoachingAssessmentEntry::getRepositoryEntryKey)
				.collect(Collectors.toSet());

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("repoKeys", repositoryEntryKeys)
				.getResultList();
	}

	private HashMap<Long, Boolean>  getCoachUserVisibleSettable(List<RepositoryEntry> repositoryEntries) {
		HashMap<Long, Boolean> repoEntryKeyToCoachUserVisibilitySettable = new HashMap<>(repositoryEntries.size());
		for (RepositoryEntry repositoryEntry : repositoryEntries) {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			Boolean coachUserVisibility = course.getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY, true);
			repoEntryKeyToCoachUserVisibilitySettable.put(repositoryEntry.getKey(), coachUserVisibility);
		}
		return repoEntryKeyToCoachUserVisibilitySettable;
	}
	
	private boolean canSetUserVisibility(CoachingAssessmentEntry coachedEntry, Map<Long, Boolean> repoEntryKeyToCoachUserVisibilitySettable) {
		if (coachedEntry.isOwner()) {
			return true;
		} else if (coachedEntry.isCoach()) {
			return repoEntryKeyToCoachUserVisibilitySettable.get(coachedEntry.getRepositoryEntryKey()).booleanValue();
		}
		return false;
	}
	
}
