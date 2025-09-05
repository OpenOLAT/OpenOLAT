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
package org.olat.modules.coach.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.RelationRole;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementForCoaching;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.CoursesStatisticsRuntimeTypesGroup;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GeneratedReport;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.SearchParticipantsStatisticsParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.lecture.LectureModule;
import org.olat.repository.RepositoryEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class CoachingServiceImpl implements CoachingService {
	
	private static final Logger log = Tracing.createLoggerFor(CoachingServiceImpl.class);

	@Autowired
	private CoachingDAO coachingDao;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	@Autowired
	private IdentityRelationshipService identityRelationsService;
	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;
	
	private static final String GENERATED_REPORT_FOLDER_NAME = "ooo-generated-reports-ooo"; 
	
	@Override
	public CoachingSecurity isCoach(Identity identity, Roles roles) {
		boolean coach = coachingDao.isCoach(identity);
		boolean teacher = lectureModule.isEnabled() && coachingDao.isTeacher(identity);
		boolean masterCoach =  lectureModule.isEnabled() && coachingDao.isMasterCoach(identity);
		boolean isUserRelationSource = !identityRelationsService.getRelationsAsSource(identity).isEmpty();
		boolean lineManager = organisationModule.isEnabled() && roles.isLineManager();
		boolean educationManager = organisationModule.isEnabled() && roles.isEducationManager();
		return new CoachingSecurity(masterCoach, coach, teacher, isUserRelationSource, lineManager, educationManager);
	}

	@Override
	public boolean isMasterCoach(Identity identity) {
		return coachingDao.isMasterCoach(identity);
	}

	@Override
	public boolean hasResourcesAsOwner(IdentityRef identity, CoursesStatisticsRuntimeTypesGroup runtimeTypesGroup) {
		return coachingDao.hasResourcesAsOwner(identity, runtimeTypesGroup);
	}

	@Override
	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student, boolean fetch) {
		return coachingDao.getStudentsCourses(coach, student, fetch);
	}
	
	@Override
	public List<StudentStatEntry> getUsersStatistics(SearchCoachedIdentityParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		return coachingDao.getUsersStatisticsNative(params, userPropertyHandlers, locale);
	}

	@Override
	public List<StudentStatEntry> getUsersByOrganization(List<UserPropertyHandler> userPropertyHandlers, Identity identity, List<Organisation> organisations, OrganisationRoles organisationRole, Locale locale) {
		return coachingDao.getUsersByOrganization(userPropertyHandlers, identity, organisations, organisationRole, locale);
	}
	
	@Override
	public List<ParticipantStatisticsEntry> getParticipantsStatistics(SearchParticipantsStatisticsParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		Identity identity = params.getIdentity();
		GroupRoles role = params.getRole();
		RelationRole userRelation = params.getRelationRole();
		List<Organisation> organisations = params.getOrganisations();
		List<Group> organisationsGroups = organisations == null || organisations.isEmpty()
				? List.of()
				: organisations.stream().map(Organisation::getGroup).toList();
		
		List<ParticipantStatisticsEntry> statsEntries = coachingDao.loadParticipantsCoursesStatistics(identity, role,
				organisationsGroups, userRelation, params.excludedRoles(), userPropertyHandlers, locale);
		Map<Long,ParticipantStatisticsEntry> statisticsEntries = statsEntries.stream()
				.collect(Collectors.toMap(ParticipantStatisticsEntry::getIdentityKey, stats -> stats, (u, v) -> u));
		
		if(!organisationsGroups.isEmpty()) {
			coachingDao.loadOrganisationsMembers(organisationsGroups, params.excludedRoles(),
					statsEntries, statisticsEntries, userPropertyHandlers, locale);
		}
		if(userRelation != null) {
			coachingDao.loadRelationUsers(identity, userRelation, statsEntries, statisticsEntries, userPropertyHandlers, locale);
		}
		
		if(params.withCourseStatus()) {
			coachingDao.processParticipantsPassedFailedStatistics(identity, role,
				organisationsGroups, userRelation, statisticsEntries);
		}
		if(params.withCourseCompletion()) {
			coachingDao.processParticipantsCompletionStatistics(identity, role,
				organisationsGroups, userRelation, statisticsEntries);
		}
		
		if(params.withReservations()) {
			if(curriculumModule.isEnabled()) {
				coachingDao.loadCurriculumElementsReservations(identity, role,
					organisationsGroups, userRelation, statisticsEntries);
			}
			coachingDao.loadRepositoryEntryReservation(identity, role,
				organisationsGroups, userRelation, statisticsEntries);
		}
		
		if(params.withOrganisations()) {
			identityPowerSearchQueries.appendOrganisations(statsEntries);
		}
		
		return statsEntries;
	}

	@Override
	public List<RepositoryEntry> getUserCourses(Identity student, boolean fetch) {
		return coachingDao.getUserCourses(student, fetch);
	}

	@Override
	public List<CourseStatEntry> getCoursesStatistics(Identity coach, GroupRoles role,
			CoursesStatisticsRuntimeTypesGroup runtimeTypesGroup) {
		if(role != GroupRoles.coach && role != GroupRoles.owner
				&& runtimeTypesGroup == CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular) {
			log.warn("Search courses in course with illegal role: {}", role);
			return new ArrayList<>();
		}
		return coachingDao.getCoursesStatistics(coach, role, runtimeTypesGroup);
	}

	@Override
	public List<GroupStatEntry> getGroupsStatistics(Identity coach) {
		return coachingDao.getGroupsStatisticsNative(coach);
	}

	@Override
	public List<EfficiencyStatementEntry> getGroup(BusinessGroup group, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<Identity> students = businessGroupService.getMembers(group, GroupRoles.participant.name());
		List<RepositoryEntry> courses = businessGroupService.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		List<UserEfficiencyStatementForCoaching> statements = efficiencyStatementManager.getUserEfficiencyStatementForCoaching(group);
		
		Map<IdentityRepositoryEntryKey,UserEfficiencyStatementForCoaching> identityToStatements = new HashMap<>();
		for(UserEfficiencyStatementForCoaching statement:statements) {
			IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(statement.getIdentityKey(), statement.getCourseRepoKey());
			identityToStatements.put(key, statement);
		}
		
		List<EfficiencyStatementEntry> entries = new ArrayList<>(students.size() * courses.size());
		for(RepositoryEntry course:courses) {
			for(Identity student:students) {
				IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(student.getKey(), course.getKey());
				UserEfficiencyStatementForCoaching statement = identityToStatements.get(key);
				entries.add(new EfficiencyStatementEntry(student, course, statement, userPropertyHandlers, locale));
			}
		}
		return entries;
	}

	@Override
	public List<EfficiencyStatementEntry> getCourse(Identity coach, RepositoryEntry entry, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<Identity> students = coachingDao.getStudents(coach, entry);

		List<UserEfficiencyStatementForCoaching> statements = efficiencyStatementManager.getUserEfficiencyStatementForCoaching(entry);
		Map<Long,UserEfficiencyStatementForCoaching> identityToStatements = new HashMap<>();
		for(UserEfficiencyStatementForCoaching statement:statements) {
			identityToStatements.put(statement.getIdentityKey(), statement);
		}
		
		List<EfficiencyStatementEntry> entries = new ArrayList<>(students.size());
		for(Identity student:students) {
			UserEfficiencyStatementForCoaching statement = identityToStatements.get(student.getKey());
			entries.add(new EfficiencyStatementEntry(student, entry, statement, userPropertyHandlers, locale));
		}
		return entries;
	}

	@Override
	public EfficiencyStatementEntry getEfficencyStatement(UserEfficiencyStatement statement, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		return coachingDao.getEfficencyStatementEntry(statement, userPropertyHandlers, locale);
	}

	@Override
	public List<EfficiencyStatementEntry> getEfficencyStatements(Identity student, List<RepositoryEntry> courses, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<UserEfficiencyStatement> statements = efficiencyStatementManager.getUserEfficiencyStatementLight(student, courses);
		Map<Long,UserEfficiencyStatement> courseKeyToStatements = new HashMap<>();
		for(UserEfficiencyStatement statement:statements) {
			courseKeyToStatements.put(statement.getCourseRepoKey(), statement);
		}
		
		List<EfficiencyStatementEntry> entries = new ArrayList<>(courses.size());
		for(RepositoryEntry course:courses) {
			UserEfficiencyStatement statement = courseKeyToStatements.get(course.getKey());
			entries.add(new EfficiencyStatementEntry(student, course, statement, userPropertyHandlers, locale));
		}
		return entries;
	}
	
	@Override
	public List<UserEfficiencyStatement> getEfficencyStatements(Identity student) {
		return efficiencyStatementManager.getUserEfficiencyStatementLight(student);
	}
	
	private static class IdentityRepositoryEntryKey {
		private final Long identityKey;
		private final Long repositoryEntryKey;

		public IdentityRepositoryEntryKey(Long identityKey, Long repositoryEntryKey) {
			this.identityKey = identityKey;
			this.repositoryEntryKey = repositoryEntryKey;
		}
		
		@Override
		public int hashCode() {
			return (identityKey == null ? 365784 : identityKey.hashCode())
					+ (repositoryEntryKey == null ? 234 : repositoryEntryKey.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof IdentityRepositoryEntryKey) {
				IdentityRepositoryEntryKey key = (IdentityRepositoryEntryKey)obj;
				return identityKey != null && identityKey.equals(key.identityKey)
						&& repositoryEntryKey != null && repositoryEntryKey.equals(key.repositoryEntryKey);
			}
			return false;
		}
	}

	@Override
	public List<GeneratedReport> getGeneratedReports(Identity coach) {
		LocalFolderImpl reportsFolder = getGeneratedReportsFolder(coach);

		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		vfsRepositoryService.getMetadataFor(reportsFolder.getBasefile());

		List<GeneratedReport> generatedReports = new ArrayList<>();
		for (VFSItem vfsItem : reportsFolder.getItems()) {
			if (vfsItem instanceof VFSLeaf reportLeaf) {
				VFSMetadata metadata = vfsRepositoryService.getMetadataFor(reportLeaf);	
				if (metadata != null) {
					GeneratedReport report = new GeneratedReport();
					report.setMetadata(metadata);
					generatedReports.add(report);
				}
			}
		}
		
		return generatedReports;
	}
	
	@Override
	public LocalFolderImpl getGeneratedReportsFolder(Identity coach) {
		LocalFolderImpl folder = VFSManager.olatRootContainer(FolderConfig.getUserHome(coach) + "/private/" + GENERATED_REPORT_FOLDER_NAME);
		if (!folder.exists()) {
			folder.getBasefile().mkdirs();
		}
		return folder;
	}

	@Override
	public void setGeneratedReport(Identity coach, String name, String fileName) {
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);

		LocalFolderImpl folder = getGeneratedReportsFolder(coach);
		if (folder.resolve(fileName) instanceof VFSLeaf leaf) {
			VFSMetadata metadata = vfsRepositoryService.getMetadataFor(leaf);
			if (metadata != null) {
				metadata.setTitle(name);
				metadata.setExpirationDate(DateUtils.addDays(new Date(), 10));
				vfsRepositoryService.updateMetadata(metadata);
			}
		}
	}

	@Override
	public void deleteGeneratedReport(Identity coach, VFSMetadata vfsMetadata) {
		String fileName = vfsMetadata.getFilename();
		LocalFolderImpl folder = getGeneratedReportsFolder(coach);
		if (folder.resolve(fileName) instanceof VFSLeaf leaf) {
			leaf.deleteSilently();
		}
	}
	
	public VFSLeaf getGeneratedReportLeaf(Identity coach, VFSMetadata vfsMetadata) {
		LocalFolderImpl folder = getGeneratedReportsFolder(coach);
		if (folder.resolve(vfsMetadata.getFilename()) instanceof VFSLeaf leaf) {
			return leaf;
		}
		return null;
	}
}
