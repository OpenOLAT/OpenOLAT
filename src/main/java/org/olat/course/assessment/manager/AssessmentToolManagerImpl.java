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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.grading.GradingAssignment;
import org.olat.repository.RepositoryEntry;
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
				} else {
					sb.append(" and ae.entryRoot = true");
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
				} else {
					sf.append(" and ae.entryRoot = true");
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
				} else {
					sb.append(" and ae.entryRoot = true");
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
				} else {
					sb.append(" and ae.entryRoot = true");
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

	@Override
	public List<IdentityShort> getShortAssessedIdentities(Identity coach, SearchAssessedIdentityParams params, int maxResults) {
		List<IdentityShort> participants = getShortAssessedParticipants(coach, params, maxResults);
		if(params.isAdmin() && params.isNonMembers() && participants.size() < maxResults) {
			List<IdentityShort> notMembers = getShortAssessedNonMembers(params, maxResults - participants.size());
			if(notMembers != null && !notMembers.isEmpty()) {
				Set<IdentityShort> participantSet = new HashSet<>(participants);
				for(IdentityShort notMember:notMembers) {
					if(!participantSet.contains(notMember)) {
						participants.add(notMember);
					}
				}
			}
		}
		return participants;
	}
	
	private List<IdentityShort> getShortAssessedNonMembers(SearchAssessedIdentityParams params, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident")
		  .append(" from bidentityshort as ident ")
		  .append(" where ");
		if(params.isAdmin() && params.isNonMembers()) {
			sb.append(" ident.key in (select aentry.identity.key from assessmententry aentry")
			  .append("  where aentry.repositoryEntry.key=:repoEntryKey")
			  .append("  and not exists (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key and membership.role ").in(GroupRoles.participant, GroupRoles.coach, GroupRoles.owner)
	          .append("    and membership.identity.key=aentry.identity.key)")
	          .append(" )");
		} else {
			return Collections.emptyList();
		}

		Long identityKey = appendUserSearchByKey(sb, params.getSearchString());
		String[] searchArr = appendUserSearch(sb, params.getSearchString());

		TypedQuery<IdentityShort> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.setParameter("repoEntryKey", params.getEntry().getKey());
		if(identityKey != null) {
			query.setParameter("searchIdentityKey", identityKey);
		}
		appendUserSearchToQuery(searchArr, query);
		return query.getResultList();
	}
	
	private List<IdentityShort> getShortAssessedParticipants(Identity coach, SearchAssessedIdentityParams params, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident")
		  .append(" from bidentityshort as ident ")
		  .append(" where ");
		if(params.isAdmin()) {
			sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group.key=participant.group.key")
	          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append(" )");
		} else if(params.isCoach()) {
			sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}

		Long identityKey = appendUserSearchByKey(sb, params.getSearchString());
		String[] searchArr = appendUserSearch(sb, params.getSearchString());

		TypedQuery<IdentityShort> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityShort.class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.setParameter("repoEntryKey", params.getEntry().getKey());
		if(!params.isAdmin()) {
			query.setParameter("identityKey", coach.getKey());
		}
		if(identityKey != null) {
			query.setParameter("searchIdentityKey", identityKey);
		}
		appendUserSearchToQuery(searchArr, query);
		return query.getResultList();
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
	
	private String[] appendUserSearch(QueryBuilder sb, String search) {
		String[] searchArr = null;

		if(StringHelper.containsNonWhitespace(search)) {
			String dbVendor = dbInstance.getDbVendor();
			searchArr = search.split(" ");
			String[] attributes = new String[]{ "name", "firstName", "lastName", "email" };

			sb.append(" and (");
			boolean start = true;
			for(int i=0; i<searchArr.length; i++) {
				for(String attribute:attributes) {
					if(start) {
						start = false;
					} else {
						sb.append(" or ");
					}
					
					if (searchArr[i].contains("_") && dbVendor.equals("oracle")) {
						//oracle needs special ESCAPE sequence to search for escaped strings
						sb.append(" lower(ident.").append(attribute).append(") like :search").append(i).append(" ESCAPE '\\'");
					} else if (dbVendor.equals("mysql")) {
						sb.append(" ident.").append(attribute).append(" like :search").append(i);
					} else {
						sb.append(" lower(ident.").append(attribute).append(") like :search").append(i);
					}
				}
			}
			sb.append(")");
		}
		return searchArr;
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
	
	@Override
	public List<Long> getIdentityKeys(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct aentry.identity.key from assessmententry aentry");
		applySearchAssessedIdentityParams(sb, params, status);
		
		TypedQuery<Long> list = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class);
		
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
		if(params.getAssessmentObligations() != null && !params.getAssessmentObligations().isEmpty()) {
			list.setParameter("assessmentObligations", params.getAssessmentObligations());
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
				} else {
					sb.append(" and ae.entryRoot = true");
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
	
}
