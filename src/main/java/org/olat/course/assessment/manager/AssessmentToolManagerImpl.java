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
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.AssessedIdentity;
import org.olat.course.assessment.model.AssessmentEntryRow;
import org.olat.course.assessment.model.CourseStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
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
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentToolManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	
	public List<AssessedIdentity> getIdentities() {
		StringBuilder sf = new StringBuilder();
		sf.append("select ident from asidentity as ident");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sf.toString(), AssessedIdentity.class)
				.getResultList();
	}
	
	@Override
	public CourseStatistics getStatistics(Identity coach, SearchAssessedIdentityParams params) {
		CourseStatistics entry = new CourseStatistics();
		
		//count all possible participants for the coach permissions
		TypedQuery<Long> countUsers = createAssessedIdentities(coach, params, Long.class);
		int numOfAssessedIdentites = 0;
		List<Long> numOfUsersList = countUsers.getResultList();
		if(numOfUsersList.size() == 1) {
			numOfAssessedIdentites = numOfUsersList.get(0) == null ? 0 : numOfUsersList.get(0).intValue();
		}
		entry.setNumOfAssessedIdentities(numOfAssessedIdentites);

		//retrive statistcis about efficicency statements
		efficiencyStatementsStatistics(coach, params, entry);
		
		//retrieve statistcs in user course infos
		userCourseInfosStatistics(coach, params, entry);

		return entry;
	}
	
	private void userCourseInfosStatistics(Identity coach, SearchAssessedIdentityParams params, CourseStatistics entry) {
		RepositoryEntry courseEntry = params.getEntry();
		try {
			StringBuilder sf = new StringBuilder();
			sf.append("select count(infos.key), infos.resource.key from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" where infos.resource.key=:resourceKey and (infos.identity in");
			if(params.isAdmin()) {
				sf.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant")
		          .append("    where rel.entry.key=:repoEntryKey and rel.group=participant.group")
		          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
		          .append("  )");
				if(params.isNonMembers()) {
					sf.append(" or not exists (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
			          .append("    where rel.entry.key=:repoEntryKey and rel.group=membership.group and membership.identity=infos.identity")
			          .append("  )");
				}
			} else if(params.isBusinessGroupCoach() || params.isRepositoryEntryCoach()) {
				sf.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
		          .append("    where rel.entry.key=:repoEntryKey")
		          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
		          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
		          .append("  )");
			}
			sf.append(" ) group by infos.resource.key");

			TypedQuery<Object[]> infos = dbInstance.getCurrentEntityManager()
				.createQuery(sf.toString(), Object[].class)
				.setParameter("resourceKey", courseEntry.getOlatResource().getKey())
				.setParameter("repoEntryKey", courseEntry.getKey());
			if(!params.isAdmin()) {
				infos.setParameter("identityKey", coach.getKey());
			}

			Object[] result = infos.getSingleResult();
			Long initalLaunch = (Long)result[0];
			entry.setInitialLaunch(initalLaunch == null ? 0 : initalLaunch.intValue());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
	}
	
	private void efficiencyStatementsStatistics(Identity coach, SearchAssessedIdentityParams params, CourseStatistics entry) {
		RepositoryEntry courseEntry = params.getEntry();
		try {
			StringBuilder sf = new StringBuilder();
			sf.append("select avg(statement.score) as scoreAverage, ")
			  .append(" sum(case when statement.passed=true then 1 else 0 end) as numOfPassed,")
			  .append(" sum(case when statement.passed=false then 1 else 0 end) as numOfFailed,")
			  .append(" sum(case when statement.passed is null then 1 else 0 end) as numOfNotAttempted,")
			  .append(" sum(statement.key) as numOfStatements,")
			  .append(" v.key as repoKey")
			  .append(" from effstatementrepo as statement ")
			  .append(" inner join statement.repositoryEntry v")
			  .append(" where v.key=:repoEntryKey and (statement.identity in ");
			if(params.isAdmin()) {
				sf.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant")
		          .append("    where rel.entry.key=:repoEntryKey and rel.group=participant.group")
		          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
		          .append("  )");
				if(params.isNonMembers()) {
					sf.append(" or not exists (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
			          .append("    where rel.entry.key=:repoEntryKey and rel.group=membership.group and membership.identity=statement.identity")
			          .append("  )");
				}
			} else if(params.isBusinessGroupCoach() || params.isRepositoryEntryCoach()) {
				sf.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
		          .append("    where rel.entry.key=:repoEntryKey")
		          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
		          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
		          .append("  )");
			}
			sf.append(" ) group by v.key");
			
			
			TypedQuery<Object[]> stats = dbInstance.getCurrentEntityManager()
				.createQuery(sf.toString(), Object[].class)
				.setParameter("repoEntryKey", courseEntry.getKey());
			if(!params.isAdmin()) {
				stats.setParameter("identityKey", coach.getKey());
			}
			
			Object[] result = stats.getSingleResult();
			Double averageScore = (Double)result[0];
			Long numOfPassed = (Long)result[1];
			Long numOfFailed = (Long)result[2];
			Long numOfNotAttempted = (Long)result[3];
			
			entry.setAverageScore(averageScore);
			entry.setCountPassed(numOfPassed == null ? 0 : numOfPassed.intValue());
			entry.setCountFailed(numOfFailed == null ? 0 : numOfFailed.intValue());
			entry.setCountNotAttempted(numOfNotAttempted == null ? 0 : numOfNotAttempted.intValue());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
	}

	@Override
	public List<Identity> getAssessedIdentities(Identity coach, SearchAssessedIdentityParams params) {
		TypedQuery<Identity> list = createAssessedIdentities(coach, params, Identity.class);
		return list.getResultList();
	}
	
	private <T> TypedQuery<T> createAssessedIdentities(Identity coach, SearchAssessedIdentityParams params, Class<T> classResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		if(Identity.class.equals(classResult)) {
			sb.append("ident");
		} else {
			sb.append("count(ident.key)");
		}
		
		sb.append(" from ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join ident.user user ")
		  .append(" where ");
		if(params.getBusinessGroupKeys() != null && params.getBusinessGroupKeys().size() > 0) {
			sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, businessgroup bgi, bgroupmember as participant")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group=bgi.baseGroup and rel.group=participant.group and bgi.key in (:businessGroupKeys) ")
	          .append("  )");
		} else if(params.isAdmin()) {
			sb.append(" (ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group=participant.group")
	          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append(" )");
			if(params.isNonMembers()) {
				sb.append(" or ident.key in (select aentry.identity.key from assessmententry aentry")
				  .append("  where aentry.repositoryEntry.key=:repoEntryKey")
				  .append("  and not exists (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
		          .append("    where rel.entry.key=:repoEntryKey and rel.group=membership.group and membership.identity=aentry.identity)")
		          .append(" )");
			}
			sb.append(")");
		} else if(params.isBusinessGroupCoach() || params.isRepositoryEntryCoach()) {
			sb.append(" ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}
		
		Long identityKey = appendUserSearchByKey(sb, params.getSearchString());
		String[] searchArr = appendUserSearchFull(sb, params.getSearchString());
		
		System.out.println(sb.toString());
		
		TypedQuery<T> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), classResult)
				.setParameter("repoEntryKey", params.getEntry().getKey());
		if(!params.isAdmin()) {
			query.setParameter("identityKey", coach.getKey());
		}
		if(identityKey != null) {
			query.setParameter("searchIdentityKey", identityKey);
		}
		if(params.getBusinessGroupKeys() != null && params.getBusinessGroupKeys().size() > 0) {
			query.setParameter("businessGroupKeys", params.getBusinessGroupKeys());
		}
		appendUserSearchToQuery(searchArr, query);
		return query;
	}
	
	@Override
	public List<IdentityShort> getShortAssessedIdentities(Identity coach, SearchAssessedIdentityParams params, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident")
		  .append(" from bidentityshort as ident ")
		  .append(" where ");
		if(params.isAdmin()) {
			sb.append(" (ident.key in (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group=participant.group")
	          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append(" )");
			if(params.isNonMembers()) {
				sb.append(" or ident.key in (select aentry.identity.key from assessmententry aentry")
				  .append("  where aentry.repositoryEntry.key=:repoEntryKey")
				  .append("  and not exists (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
		          .append("    where rel.entry.key=:repoEntryKey and rel.group=membership.group and membership.identity=aentry.identity)")
		          .append(" )");
			}
			sb.append(")");
		} else if(params.isBusinessGroupCoach() || params.isRepositoryEntryCoach()) {
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
	
	private Long appendUserSearchByKey(StringBuilder sb, String search) {
		Long identityKey = null;
		if(StringHelper.containsNonWhitespace(search)) {
			if(StringHelper.isLong(search)) {
				try {
					identityKey = new Long(search);
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
	
	private String[] appendUserSearch(StringBuilder sb, String search) {
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
	
	private String[] appendUserSearchFull(StringBuilder sb, String search) {
		String[] searchArr = null;

		if(StringHelper.containsNonWhitespace(search)) {
			String dbVendor = dbInstance.getDbVendor();
			searchArr = search.split(" ");
			String[] attributes = new String[]{ "firstName", "lastName", "email" };

			sb.append(" and (");
			boolean start = true;
			for(int i=0; i<searchArr.length; i++) {
				for(String attribute:attributes) {
					if(start) {
						start = false;
					} else {
						sb.append(" or ");
					}
					
					sb.append(" exists (select prop").append(attribute).append(".value from userproperty prop").append(attribute).append(" where ")
					  .append(" prop").append(attribute).append(".propertyId.userId=user.key and prop").append(attribute).append(".propertyId.name ='").append(attribute).append("'")
					  .append(" and ");
					if(dbVendor.equals("mysql")) {
						sb.append(" prop").append(attribute).append(".value like :search").append(i).append(" ");
					} else {
						sb.append(" lower(prop").append(attribute).append(".value) like :search").append(i).append(" ");
					}
					if(dbVendor.equals("oracle")) {
						sb.append(" escape '\\'");
					}
					sb.append(")");
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
	public List<AssessmentEntryRow> getAssessmentEntryRows(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select asident, aentry from asidentity asident");
		if(params.isFailed() || params.isPassed() || (params.getAssessmentStatus() != null && params.getAssessmentStatus().size() > 0)) {
			sb.append(" inner join asident.assessmentEntries as aentry ");
		} else {
			sb.append(" left join asident.assessmentEntries as aentry ");
		}
		sb.append(" on ( aentry.repositoryEntry.key=:repoEntryKey");
		if(params.getReferenceEntry() != null) {
			sb.append(" and aentry.referenceEntry.key=:referenceKey");
		}
		if(params.getSubIdent() != null) {
			sb.append(" and aentry.subIdent=:subIdent");
		}
		sb.append(")");

		sb.append(" where (asident.key in");
		if(params.isAdmin()) {
			sb.append(" (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group=participant.group")
	          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
			if(params.isNonMembers()) {
				sb.append(" or asident.key in (select aentry.identity.key from assessmententry aentryInvitee")
				  .append("  where aentryInvitee.repositoryEntry.key=:repoEntryKey")
				  .append("  and not exists (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
		          .append("    where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key and membership.identity.key=asident.key)")
		          .append(" )");
			}
		} else if(params.isBusinessGroupCoach() || params.isRepositoryEntryCoach()) {
			sb.append(" (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}
		sb.append(" )");

		if(params.isFailed()) {
			sb.append(" aentry.passed=false");
		}
		if(params.isPassed()) {
			sb.append(" aentry.passed=true");
		}
		if(params.getAssessmentStatus() != null && params.getAssessmentStatus().size() > 0) {
			sb.append(" aentry.status in (:status)");
		}
		
		sb.append(" order by asident.name");
		
		TypedQuery<Object[]> list = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("repoEntryKey", params.getEntry().getKey());
		if(params.getReferenceEntry() != null) {
			list.setParameter("referenceKey", params.getReferenceEntry().getKey());
		}
		if(params.getSubIdent() != null) {
			list.setParameter("subIdent", params.getSubIdent());
		}
		if(!params.isAdmin()) {
			list.setParameter("identityKey", coach.getKey());
		}
		
		if(params.getAssessmentStatus() != null && params.getAssessmentStatus().size() > 0) {
			List<String> statusList = new ArrayList<>();
			for(AssessmentEntryStatus assessmentStatus:params.getAssessmentStatus()) {
				statusList.add(assessmentStatus.name());
			}
			list.setParameter("status", statusList);
		}
		
		List<Object[]> objects = list.getResultList();
		List<AssessmentEntryRow> rows = new ArrayList<>();
		for(Object[] object:objects) {
			Identity identity = (Identity)object[0];
			AssessmentEntry entry = (AssessmentEntry)object[1];
			rows.add(new AssessmentEntryRow(identity, entry));
		}
		
		return rows;
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select aentry from assessmententry aentry")
		  .append(" where aentry.repositoryEntry.key=:repoEntryKey");
		if(params.getReferenceEntry() != null) {
			sb.append(" and aentry.referenceEntry.key=:referenceKey");
		}
		if(params.getSubIdent() != null) {
			sb.append(" and aentry.subIdent=:subIdent");
		}
		if(status != null) {
			sb.append(" and aentry.status=:assessmentStatus");
		}
		sb.append(" and (aentry.identity in");
		if(params.isAdmin()) {
			sb.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant")
	          .append("    where rel.entry.key=:repoEntryKey and rel.group=participant.group")
	          .append("      and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
			if(params.isNonMembers()) {
				sb.append(" or aentry.identity not in (select membership.identity from repoentrytogroup as rel, bgroupmember as membership")
		          .append("    where rel.entry.key=:repoEntryKey and rel.group=membership.group and membership.identity=aentry.identity")
		          .append(" )");
			}
		} else if(params.isBusinessGroupCoach() || params.isRepositoryEntryCoach()) {
			sb.append(" (select participant.identity from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
	          .append("    where rel.entry.key=:repoEntryKey")
	          .append("      and rel.group=coach.group and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
	          .append("      and rel.group=participant.group and participant.role='").append(GroupRoles.participant.name()).append("'")
	          .append("  )");
		}
		sb.append(" )");
		
		TypedQuery<AssessmentEntry> list = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentEntry.class)
			.setParameter("repoEntryKey", params.getEntry().getKey());
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
		return list.getResultList();
	}
}
