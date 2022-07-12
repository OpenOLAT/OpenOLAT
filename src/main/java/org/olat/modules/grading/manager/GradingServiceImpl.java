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
package org.olat.modules.grading.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssessedIdentityVisibility;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentRef;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingModule;
import org.olat.modules.grading.GradingNotificationType;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.modules.grading.GradingTimeRecordRef;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.CourseElementKey;
import org.olat.modules.grading.model.GraderStatistics;
import org.olat.modules.grading.model.GraderWithStatistics;
import org.olat.modules.grading.model.GradersSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentImpl;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.modules.grading.model.GradingSecurity;
import org.olat.modules.grading.model.IdentityTimeRecordStatistics;
import org.olat.modules.grading.model.OlatResourceMapKey;
import org.olat.modules.grading.model.ReferenceEntryStatistics;
import org.olat.modules.grading.model.ReferenceEntryTimeRecordStatistics;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.modules.grading.ui.GradingAssignmentsListController;
import org.olat.modules.grading.ui.component.GraderMailTemplate;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.resource.OLATResource;
import org.olat.user.AbsenceLeave;
import org.olat.user.UserDataDeletable;
import org.olat.user.manager.AbsenceLeaveDAO;
import org.olat.user.manager.AbsenceLeaveHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradingServiceImpl implements GradingService, UserDataDeletable, RepositoryEntryDataDeletable, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(GradingServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private GradingModule gradingModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private AbsenceLeaveDAO absenceLeaveDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	@Autowired
	private GradingTimeRecordDAO gradingTimeRecordDao;
	@Autowired
	private GradingConfigurationDAO gradingConfigurationDao;

	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;

	private CacheWrapper<CourseElementKey,String> courseElementTitleCache;

	@Override
	public void afterPropertiesSet() throws Exception {
		courseElementTitleCache = coordinatorManager.getCoordinator().getCacher().getCache(GradingService.class.getSimpleName(), "courseElementsTitle");
	}
	
	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		boolean hasAssignment = gradingAssignmentDao.hasGradingAssignment(re); // or grader to identity
		if(!hasAssignment) {
			RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(re);
			if(config != null) {
				gradedToIdentityDao.deleteGradersRelations(re);
				gradingConfigurationDao.deleteConfiguration(config);
			}
		}
		return !hasAssignment;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(identity);
		for(GradingAssignment assignment:assignments) {
			unassignGrader(assignment);
		}
		dbInstance.commitAndCloseSession();
		
		List<GraderToIdentity> relations = gradedToIdentityDao.getGraderRelations(identity);
		for(GraderToIdentity relation:relations) {
			gradingTimeRecordDao.deleteTimeRecords(relation);
			gradedToIdentityDao.deleteGraderRelation(relation);
		}
		dbInstance.commitAndCloseSession();
	}

	@Override
	public GradingSecurity isGrader(IdentityRef identity, Roles roles) {
		boolean enabled = gradingModule.isEnabled();
		boolean grader = enabled && gradedToIdentityDao.isGrader(identity);
		boolean resourcesManager = enabled && (roles.isAdministrator() || roles.isLearnResourceManager()
				|| gradedToIdentityDao.isGradingManager(identity, ImsQTI21Resource.TYPE_NAME));
		return new GradingSecurity(grader, resourcesManager, roles);
	}

	@Override
	public RepositoryEntryGradingConfiguration getOrCreateConfiguration(RepositoryEntry entry) {
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(entry);
		if(config == null) {
			config = gradingConfigurationDao.createConfiguration(entry);
			dbInstance.commit();
		}
		return config;
	}

	@Override
	public RepositoryEntryGradingConfiguration updateConfiguration(RepositoryEntryGradingConfiguration configuration) {
		return gradingConfigurationDao.updateConfiguration(configuration);
	}
	
	
	@Override
	public boolean isGradingEnabled(RepositoryEntryRef entry, String softKey) {
		boolean enabled = false;
		List<RepositoryEntryGradingConfiguration> configs = gradingConfigurationDao.getConfiguration(entry, softKey);
		for(RepositoryEntryGradingConfiguration config:configs) {
			if(config != null && config.isGradingEnabled()) {
				enabled |= true;
			}
		}
		return enabled;
	}

	@Override
	public Map<Long,GradingAssessedIdentityVisibility> getIdentityVisibility(Collection<RepositoryEntryRef> entries) {
		return gradingConfigurationDao.getIdentityVisibility(entries);
	}

	@Override
	public GradingAssessedIdentityVisibility getIdentityVisibility(RepositoryEntryRef entry) {
		Map<Long,GradingAssessedIdentityVisibility> visibilityMap = gradingConfigurationDao
				.getIdentityVisibility(Collections.singletonList(entry));
		if(visibilityMap.containsKey(entry.getKey())) {
			return visibilityMap.get(entry.getKey());
		}
		return null;
	}

	@Override
	public void addGraders(RepositoryEntry referenceEntry, List<Identity> identities, GraderMailTemplate mailTemplate, MailerResult result) {
		int count = 0;
		List<GraderToIdentity> newGraders = new ArrayList<>();
		for(Identity identity:identities) {
			GraderToIdentity relation;
			if(gradedToIdentityDao.isGraderOf(referenceEntry, identity)) {
				relation = gradedToIdentityDao.getGrader(referenceEntry, identity);
				if(relation.getGraderStatus() != GraderStatus.activated) {
					relation = activateGrader(relation);
				}
			} else {
				relation = gradedToIdentityDao.createRelation(referenceEntry, identity);
			}
			newGraders.add(relation);
			if(++count % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		dbInstance.commit();
		
		if(mailTemplate != null) {
			MailContext context = new MailContextImpl("[CoachSite:0][Grading:0]");
			decorateGraderMailTemplate(referenceEntry, mailTemplate);
			doSendEmails(context, newGraders, mailTemplate, result);
		}
	}
	
	private boolean doSendEmails(MailContext context, List<GraderToIdentity> graders, MailTemplate template, MailerResult result) {
		for(GraderToIdentity grader:graders) {
			if(grader != null && grader.getIdentity() != null) {
				Identity recipient = grader.getIdentity();
				MailBundle bundle = mailManager.makeMailBundle(context, recipient, template, null, null, result);
				MailerResult sendResult = mailManager.sendMessage(bundle);
				result.append(sendResult);
			}
		}
		return result.isSuccessful();
	}

	@Override
	public List<GraderToIdentity> getGraders(RepositoryEntry entry) {
		return gradedToIdentityDao.getGraders(entry);
	}	

	@Override
	public List<AbsenceLeave> getGradersAbsenceLeaves(RepositoryEntry entry) {
		return gradedToIdentityDao.getGradersAbsenceLeaves(entry);
	}

	@Override
	public List<GraderWithStatistics> getGradersWithStatistics(GradersSearchParameters searchParams) {
		boolean useDatesForGraders = searchParams.getReferenceEntry() == null;
		List<GraderToIdentity> graders = gradedToIdentityDao.findGraders(searchParams, useDatesForGraders);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		Date absentFrom = cal.getTime();
		cal.add(Calendar.YEAR, 2);
		Date absentTo = cal.getTime();
		List<AbsenceLeave> absenceLeaves = gradedToIdentityDao.findGradersAbsenceLeaves(searchParams, absentFrom, absentTo);
		
		List<IdentityTimeRecordStatistics> records = gradedToIdentityDao.findGradersRecordedTimeGroupByIdentity(searchParams);

		List<GraderStatistics> rawStatistics = gradedToIdentityDao.getGradersStatistics(searchParams);
		Map<Long,GraderStatistics> rawStatisticsMap = rawStatistics.stream()
				.collect(Collectors.toMap(GraderStatistics::getKey, Function.identity(), (u, v) -> u));

		Map<Long, GraderWithStatistics> identityToStatistics = new HashMap<>();
		for(GraderToIdentity grader:graders) {
			Identity identity = grader.getIdentity();
			GraderStatistics rawStats = rawStatisticsMap.get(identity.getKey());
			GraderWithStatistics statistics = identityToStatistics.computeIfAbsent(identity.getKey(), key
					-> new GraderWithStatistics(identity, rawStats));
			statistics.addGraderStatus(grader.getGraderStatus());
		}
		
		for(IdentityTimeRecordStatistics record:records) {
			Long graderIdentityKey = record.getKey();
			GraderWithStatistics statistics = identityToStatistics.computeIfAbsent(graderIdentityKey, key -> {
				Identity grader = securityManager.loadIdentityByKey(graderIdentityKey);
				return new GraderWithStatistics(grader, GraderStatistics.empty(graderIdentityKey));
			});
			statistics.addRecordedTimeInSeconds(record.getTime());
			statistics.addRecordedMetadataTimeInSeconds(record.getMetadataTime());
		}
		
		for(AbsenceLeave absenceLeave:absenceLeaves) {
			Identity absentIdentity = absenceLeave.getIdentity();
			GraderWithStatistics statistics = identityToStatistics.get(absentIdentity.getKey());
			statistics.addAbsenceLeave(absenceLeave);
		}
		
		return new ArrayList<>(identityToStatistics.values());
	}

	@Override
	public List<ReferenceEntryWithStatistics> getGradedEntriesWithStatistics(Identity grader) {
		final List<ReferenceEntryStatistics> entriesStatistics = gradedToIdentityDao.getReferenceEntriesStatistics(grader);
		final Map<Long,ReferenceEntryStatistics> entryKeyStatistics = entriesStatistics.stream()
				.collect(Collectors.toMap(ReferenceEntryStatistics::getKey, Function.identity(), (u, v) -> u));
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setGrader(grader);
		
		final List<RepositoryEntry> entries = gradedToIdentityDao.getReferenceRepositoryEntriesAsGrader(grader);
		Map<OlatResourceMapKey,ReferenceEntryWithStatistics> resourceKeyStatistics = entries.stream().map(entry -> {
			ReferenceEntryStatistics stats = entryKeyStatistics.get(entry.getKey());
			if(stats == null) {
				return new ReferenceEntryWithStatistics(entry);
			}
			return new ReferenceEntryWithStatistics(stats);
		}).collect(Collectors.toMap(OlatResourceMapKey::new, Function.identity(), (u, v) -> u));

		List<ReferenceEntryWithStatistics> statistics = new ArrayList<>(resourceKeyStatistics.values());
		List<AbsenceLeave> absenceLeaves = absenceLeaveDao.getAbsenceLeaves(grader);
		for(AbsenceLeave absenceLeave:absenceLeaves) {
			if(StringHelper.containsNonWhitespace(absenceLeave.getResName())) {
				ReferenceEntryWithStatistics stats = resourceKeyStatistics
						.get(new OlatResourceMapKey(absenceLeave.getResName(), absenceLeave.getResId()));
				if(stats != null) {
					stats.addAbsenceLeave(absenceLeave);
				}
			} else {
				for(ReferenceEntryWithStatistics stats:statistics) {
					stats.addAbsenceLeave(absenceLeave);
				}
			}
		}
		
		List<ReferenceEntryTimeRecordStatistics> records = gradedToIdentityDao.findGradersRecordedTimeGroupByEntry(searchParams);
		if(!records.isEmpty()) {
			Map<Long,ReferenceEntryWithStatistics> keyStatistics = statistics.stream()
					.collect(Collectors.toMap(ReferenceEntryWithStatistics::getKey, Function.identity(), (u, v) -> u));
			for(ReferenceEntryTimeRecordStatistics record:records) {
				ReferenceEntryWithStatistics stats = keyStatistics.get(record.getKey());
				stats.addRecordedTimeInSeconds(record.getTime());
				stats.addRecordedMetadataTimeInSeconds(record.getMetadataTime());
			}
		}
		return statistics;
	}
	
	@Override
	public void activateGrader(Identity identity) {
		List<GraderToIdentity> graderRelations = gradedToIdentityDao.getGraderRelations(identity);
		for(GraderToIdentity graderRelation:graderRelations) {
			activateGrader(graderRelation);
		}
	}

	@Override
	public void activateGrader(RepositoryEntry entry, Identity identity) {
		GraderToIdentity grader = gradedToIdentityDao.getGrader(entry, identity);
		activateGrader(grader);
	}
	
	private GraderToIdentity activateGrader(GraderToIdentity graderRelation) {
		if(graderRelation != null && graderRelation.getGraderStatus() != GraderStatus.activated) {
			graderRelation.setGraderStatus(GraderStatus.activated);
			graderRelation = gradedToIdentityDao.updateGrader(graderRelation);
			log.info(Tracing.M_AUDIT, "Activate grader {} in resource {} ({})",
					graderRelation.getIdentity(), graderRelation.getEntry().getKey(), graderRelation.getEntry().getDisplayname());
		}
		return graderRelation;
	}
	
	@Override
	public void deactivateGrader(Identity identity, Identity replacementGrader,
			GraderMailTemplate reassignmentTemplate, MailerResult result) {
		List<GraderToIdentity> graderRelations = gradedToIdentityDao.getGraderRelations(identity);
		for(GraderToIdentity graderRelation:graderRelations) {
			deactivate(graderRelation, replacementGrader, reassignmentTemplate, result);
		}
	}

	@Override
	public void deactivateGrader(RepositoryEntry entry, Identity identity, Identity replacementGrader,
			GraderMailTemplate reassignmentTemplate, MailerResult result) {
		GraderToIdentity graderRelation = gradedToIdentityDao.getGrader(entry, identity);
		deactivate(graderRelation, replacementGrader, reassignmentTemplate, result);
	}
	
	private void deactivate(GraderToIdentity graderRelation, Identity replacementGrader,
			GraderMailTemplate reassignmentTemplate, MailerResult result) {
		if(graderRelation == null
				|| graderRelation.getGraderStatus() == GraderStatus.deactivated
				|| graderRelation.getGraderStatus() == GraderStatus.removed) {
			return;
		}
		
		graderRelation.setGraderStatus(GraderStatus.deactivated);
		graderRelation = gradedToIdentityDao.updateGrader(graderRelation);
		dbInstance.commit();

		log.info(Tracing.M_AUDIT, "Deactivate grader {} {}",
				graderRelation.getKey(), graderRelation.getIdentity());
		
		moveAssignments(graderRelation, replacementGrader, reassignmentTemplate, result);
	}

	@Override
	public void removeGrader(Identity identity, Identity replacementGrader, GraderMailTemplate reassignmentTemplate,
			MailerResult result) {
		List<GraderToIdentity> graderRelations = gradedToIdentityDao.getGraderRelations(identity);
		for(GraderToIdentity graderRelation:graderRelations) {
			remove(graderRelation, replacementGrader, reassignmentTemplate, result);
		}
	}

	@Override
	public void removeGrader(RepositoryEntry entry, Identity identity, Identity replacementGrader,
			GraderMailTemplate reassignmentTemplate, MailerResult result) {
		GraderToIdentity graderRelation = gradedToIdentityDao.getGrader(entry, identity);
		remove(graderRelation, replacementGrader, reassignmentTemplate, result);
	}
	
	private void remove(GraderToIdentity graderRelation, Identity replacementGrader,
			GraderMailTemplate reassignmentTemplate, MailerResult result) {
		if(graderRelation == null || graderRelation.getGraderStatus() == GraderStatus.removed) {
			return;
		}
		graderRelation.setGraderStatus(GraderStatus.removed);
		graderRelation = gradedToIdentityDao.updateGrader(graderRelation);
		dbInstance.commit();
		moveAssignments(graderRelation, replacementGrader, reassignmentTemplate, result);
	}
	
	private void moveAssignments(GraderToIdentity graderRelation, Identity replacementGrader,
			GraderMailTemplate reassignmentTemplate, MailerResult result) {
		
		// move assignments
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(graderRelation);
		for(GradingAssignment assignment:assignments) {
			if(assignment.getAssignmentStatus() == GradingAssignmentStatus.done) {
				continue;
			}
			
			if(replacementGrader == null) {
				unassignGrader(assignment);
			} else {
				assignGrader(assignment, replacementGrader, reassignmentTemplate, result);
			}
		}
		dbInstance.commit();
	}

	@Override
	public GradingAssignment getGradingAssignment(GradingAssignmentRef ref) {
		return gradingAssignmentDao.loadFullByKey(ref.getKey());
	}
	
	@Override
	public GradingAssignment getGradingAssignment(RepositoryEntryRef referenceEntry, AssessmentEntry assessmentEntry) {
		return gradingAssignmentDao.getGradingAssignment(referenceEntry, assessmentEntry);
	}

	@Override
	public List<GradingAssignmentWithInfos> getGradingAssignmentsWithInfos(GradingAssignmentSearchParameters searchParams, Locale locale) {
		RepositoryEntry referenceEntry = searchParams.getReferenceEntry();
		List<GradingAssignmentWithInfos> assignmentWithInfos = gradingAssignmentDao.findGradingAssignments(searchParams);
		loadTaxonomy(referenceEntry, assignmentWithInfos, locale);
		loadCourseElements(assignmentWithInfos);
		loadAssessedIdentityVisibility(referenceEntry, assignmentWithInfos);
		return assignmentWithInfos;
	}
	
	private void loadTaxonomy(RepositoryEntry referenceEntry, List<GradingAssignmentWithInfos> rows, Locale locale) {
		if(!taxonomyModule.isEnabled()) return;
		
		if(referenceEntry != null) {
			List<TaxonomyLevel> levels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(referenceEntry);
			String levelsToString = taxonomyLevelToString(levels, locale);
			if(StringHelper.containsNonWhitespace(levelsToString)) {
				rows.stream().forEach(row -> row.setTaxonomyLevels(levelsToString));
			}
		} else {
			Set<RepositoryEntryRef> entryRefs = new HashSet<>();
			for(GradingAssignmentWithInfos row:rows) {
				entryRefs.add(row.getReferenceEntry());
			}
			
			Map<RepositoryEntryRef,List<TaxonomyLevel>> levelsMap = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(new ArrayList<>(entryRefs), false);
			for(GradingAssignmentWithInfos row:rows) {
				List<TaxonomyLevel> levels = levelsMap.get(row.getReferenceEntry());
				String levelsToString = taxonomyLevelToString(levels, locale);
				row.setTaxonomyLevels(levelsToString);
			}
		}
	}
	
	private String taxonomyLevelToString(List<TaxonomyLevel> levels, Locale locale) {
		if(levels == null || levels.isEmpty()) return null;

		Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
		StringBuilder sb = new StringBuilder(128);
		for(TaxonomyLevel level:levels) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level));
		}
		return sb.toString();
	}
	
	private void loadCourseElements(List<GradingAssignmentWithInfos> rows) {
		for(GradingAssignmentWithInfos row:rows) {
			if(!StringHelper.containsNonWhitespace(row.getSubIdent())) {
				continue;
			}
			
			final String subIdent = row.getSubIdent();
			final RepositoryEntry entry = row.getEntry();
			String title = getCachedCourseElementTitle(entry, subIdent);
			if(title == null) {
				title = "???";
			}
			row.setCourseElementTitle(title);
		}
	}
	
	private void loadAssessedIdentityVisibility(RepositoryEntry referenceEntry, List<GradingAssignmentWithInfos> rows) {
		if(referenceEntry == null) {
			Set<RepositoryEntryRef> entries = new HashSet<>();
			for(GradingAssignmentWithInfos row:rows) {
				entries.add(row.getReferenceEntry());
			}
			Map<Long,GradingAssessedIdentityVisibility> visibility = getIdentityVisibility(entries);
			for(GradingAssignmentWithInfos row:rows) {
				GradingAssessedIdentityVisibility configVisibility = visibility.get(row.getReferenceEntry().getKey());
				boolean assessedIdentityVisibility = configVisibility == GradingAssessedIdentityVisibility.nameVisible;
				row.setAssessedIdentityVisible(assessedIdentityVisibility);
			}
		} else {
			GradingAssessedIdentityVisibility testEntryAssessedIdentityVisibility = GradingAssessedIdentityVisibility.anonymous;
			RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(referenceEntry);
			if(config != null && config.getIdentityVisibilityEnum() != null) {
				testEntryAssessedIdentityVisibility = config.getIdentityVisibilityEnum();
			}
			boolean assessedIdentityVisibility = testEntryAssessedIdentityVisibility == GradingAssessedIdentityVisibility.nameVisible;
			for(GradingAssignmentWithInfos row:rows) {
				row.setAssessedIdentityVisible(assessedIdentityVisibility);
			}
		}
	}
	
	@Override
	public void sendReminders() {
		// the query returns only an approximation because of the working days part of the configuration
		List<GradingAssignment> inexactList = gradingAssignmentDao.getGradingAssignmentsOpenWithPotentialToRemind();
		for(GradingAssignment assignment:inexactList) {
			try {
				RepositoryEntry referenceEntry = assignment.getReferenceEntry();
				RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(referenceEntry);
				if(exactReminderCalculation(assignment, assignment.getReminder1Date(), config.getFirstReminder())) {
					GraderMailTemplate template = reminderTemplate(assignment, config, true);
					reminder(assignment, template);
					assignment.setReminder1Date(new Date());
					gradingAssignmentDao.updateAssignment(assignment);
				} else if(exactReminderCalculation(assignment, assignment.getReminder2Date(), config.getSecondReminder())) {
					GraderMailTemplate template = reminderTemplate(assignment, config, true);
					reminder(assignment, template);
					assignment.setReminder2Date(new Date());
					gradingAssignmentDao.updateAssignment(assignment);
				}
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				dbInstance.rollbackAndCloseSession();
			}
		}
	}
	
	private GraderMailTemplate reminderTemplate(GradingAssignment assignment, RepositoryEntryGradingConfiguration config, boolean first) {
		assignment = gradingAssignmentDao.loadFullByKey(assignment.getKey());
		
		RepositoryEntry referenceEntry = assignment.getReferenceEntry();
		GraderToIdentity grader = assignment.getGrader();
		String language = grader.getIdentity().getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(GradingAssignmentsListController.class, locale);

		return first ? GraderMailTemplate.firstReminder(translator, null, null, referenceEntry, config)
				: GraderMailTemplate.secondReminder(translator, null, null, referenceEntry, config);
	}
	
	private boolean exactReminderCalculation(GradingAssignment assignment, Date sendReminderDate, Integer reminderPeriod) {
		if(reminderPeriod == null || sendReminderDate != null) return false;
		
		Date assignmentDate = assignment.getAssignmentDate();
		Date assignmentDatePlusPeriod = CalendarUtils.addWorkingDays(assignmentDate, reminderPeriod.intValue());
		assignmentDatePlusPeriod = CalendarUtils.startOfDay(assignmentDatePlusPeriod);
		return new Date().after(assignmentDatePlusPeriod);
	}
	
	private MailerResult reminder(GradingAssignment assignment, GraderMailTemplate template) {

		assignment = decorateGraderMailTemplate(assignment, template);

		MailContext context = new MailContextImpl("[CoachSite:0][Grading:0][Assignments:0]");
		
		MailerResult result = new MailerResult();
		Identity recipient = assignment.getGrader().getIdentity();
		MailBundle bundle = mailManager.makeMailBundle(context, recipient, template, null, null, result);
		MailerResult sendResult = mailManager.sendMessage(bundle);
		result.append(sendResult);
		return sendResult;
	}
	
	private void notificationDaily(Identity grader, RepositoryEntry referenceEntry,
			List<GradingAssignment> assignments, String subject, String body) {
		
		Set<RepositoryEntry> entries = new HashSet<>();
		for(GradingAssignment assignment: assignments) {
			RepositoryEntry entry = assignment.getAssessmentEntry().getRepositoryEntry();
			entries.add(entry);
		}

		Set<CourseNode> courseNodes = new HashSet<>();
		for(RepositoryEntry entry:entries) {
			ICourse course = CourseFactory.loadCourse(entry);
			Set<String> courseNodeIds = assignments.stream()
					.filter(assignment -> entry.equals(assignment.getAssessmentEntry().getRepositoryEntry()))
					.map(assignment -> assignment.getAssessmentEntry().getSubIdent())
					.collect(Collectors.toSet());
			for(String courseNodeId:courseNodeIds) {
				CourseNode courseNode = course.getRunStructure().getNode(courseNodeId);
				courseNodes.add(courseNode);
			}
		}

		String language = grader.getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		GraderMailTemplate template = new GraderMailTemplate(subject, body, locale);
		// test and taxonomy
		decorateGraderMailTemplate(referenceEntry, template);
		
		template.setEntries(new ArrayList<>(entries));
		template.setAssessmentDate(assignments.get(0).getAssessmentDate());
		template.setCourseNodes(new ArrayList<>(courseNodes));

		MailContext context = new MailContextImpl("[CoachSite:0][Grading:0]");
		
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, grader, template, null, null, result);
		MailerResult sendResult = mailManager.sendMessage(bundle);
		result.append(sendResult);
	}
	
	private GradingAssignment decorateGraderMailTemplate(GradingAssignment assignment, GraderMailTemplate template) {
		if(template == null) return assignment;
		
		assignment = gradingAssignmentDao.loadFullByKey(assignment.getKey());
		template.setAssessmentDate(assignment.getAssessmentDate());

		RepositoryEntry entry = assignment.getAssessmentEntry().getRepositoryEntry();
		template.setEntry(entry);
		decorateGraderMailTemplate(assignment.getReferenceEntry(), template);
		
		CourseNode courseNode = null;
		OLATResource courseResource = entry.getOlatResource();
		if(StringHelper.containsNonWhitespace(assignment.getAssessmentEntry().getSubIdent())
				&& "CourseModule".equals(courseResource.getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(courseResource);
			courseNode = course.getRunStructure().getNode(assignment.getAssessmentEntry().getSubIdent());
		}
		template.setCourseNode(courseNode);
		return assignment;
	}
	
	private void decorateGraderMailTemplate(RepositoryEntry referenceEntry, GraderMailTemplate template) {
		if(referenceEntry == null || template == null) return;

		template.setReferenceEntry(referenceEntry);

		StringBuilder taxonomyLevelPath = new StringBuilder();
		List<TaxonomyLevel> levels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(referenceEntry);

		String taxonomyLevels = taxonomyLevelToString(new ArrayList<>(levels), template.getLocale());
		template.setTaxonomyLevel(taxonomyLevels);
		
		if(!levels.isEmpty()) {
			Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, template.getLocale());
			for(TaxonomyLevel level:levels) {
				List<TaxonomyLevel> parentLine = taxonomyLevelDao.getParentLine(level, level.getTaxonomy());
				
				StringBuilder sb = new StringBuilder(256);
				for(TaxonomyLevel parent:parentLine) {
					if(sb.length() > 0) sb.append(" / ");
					sb.append(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, parent));
				}
				
				if(taxonomyLevelPath.length() > 0) {
					taxonomyLevelPath.append(", ");
				}
				taxonomyLevelPath.append(sb);
			}
		}
		
		if(taxonomyLevelPath.length() > 0) {
			template.setTaxonomyLevelPath(taxonomyLevelPath.toString());
		}
	}
	
	@Override
	public void sendGradersAsssignmentsNotification() {
		List<Identity> gradersToNotify = gradingAssignmentDao.getGradersIdentityToNotify();
		dbInstance.commit();

		for(Identity graderToNotify:gradersToNotify) {
			sendGraderAsssignmentsNotification(graderToNotify);
		}	
	}
	
	public void sendGraderAsssignmentNotification(GraderToIdentity grader, RepositoryEntry referenceEntry,
			GradingAssignment assignment, RepositoryEntryGradingConfiguration config) {
		if(grader == null) return; // nothing to do
		
		String language = grader.getIdentity().getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(GradingAssignmentsListController.class, locale);

		GraderMailTemplate mailTemplate = GraderMailTemplate.notification(translator, null, null, referenceEntry, config);
		decorateGraderMailTemplate(referenceEntry, mailTemplate);

		MailerResult result = new MailerResult();
		MailContext context = new MailContextImpl("[CoachSite:0][Orders:0][Assignments:0]");
		decorateGraderMailTemplate(assignment, mailTemplate);
		doSendEmails(context, Collections.singletonList(grader), mailTemplate, result);
	}
	
	public void sendGraderAsssignmentsNotification(Identity grader) {
		List<GradingAssignment> assignments = gradingAssignmentDao.getAssignmentsForGradersNotify(grader);
		List<GradingAssignment> assignmentsToNotify= assignments.stream()
				.filter(assignment -> assignment.getAssignmentNotificationDate() == null)
				.distinct().collect(Collectors.toList());
		
		if(!assignmentsToNotify.isEmpty()) {
			List<RepositoryEntry> newReferenceEntries = assignmentsToNotify.stream()
					.map(GradingAssignment::getReferenceEntry)
					.distinct().collect(Collectors.toList());
			for(RepositoryEntry newReferenceEntry:newReferenceEntries) {
				RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(newReferenceEntry);
				List<GradingAssignment> refAssignmentsToNotify = assignmentsToNotify.stream()
						.filter(assignment -> newReferenceEntry.equals(assignment.getReferenceEntry()))
						.collect(Collectors.toList());
				if(config.getNotificationTypeEnum() == GradingNotificationType.onceDay
						&& StringHelper.containsNonWhitespace(config.getNotificationBody())
						&& !refAssignmentsToNotify.isEmpty()) {
					notificationDaily(grader, newReferenceEntry, refAssignmentsToNotify, config.getNotificationSubject(), config.getNotificationBody());
				}
			}
			
			Date now = new Date();
			for(GradingAssignment assignmentToNotify:assignmentsToNotify) {
				assignmentToNotify.setAssignmentNotificationDate(now);
				gradingAssignmentDao.updateAssignment(assignmentToNotify);
			}
		}
		dbInstance.commitAndCloseSession();
	}

	@Override
	public GradingAssignment assignGrader(RepositoryEntry referenceEntry, AssessmentEntry assessmentEntry, Date assessmentDate, boolean updateAssessmentDate) {	
		if(assessmentDate == null) {
			assessmentDate = new Date();
		}
		
		GradingAssignment assignment = gradingAssignmentDao.getGradingAssignment(referenceEntry, assessmentEntry);
		if(assignment != null && assignment.getGrader() != null) {
			if(updateAssessmentDate) {
				assignment.setAssessmentDate(assessmentDate);
				assignment = gradingAssignmentDao.updateAssignment(assignment);
			}
			return assignment;
		}
		
		GraderToIdentity choosedGrader = selectGrader(referenceEntry);
		
		Date deadLine = null;
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(referenceEntry);
		if(config != null && config.getGradingPeriod() != null) {
			deadLine = CalendarUtils.addWorkingDays(new Date(), config.getGradingPeriod().intValue());
			deadLine = CalendarUtils.endOfDay(deadLine);
		}
		
		if(assignment == null) {
			assignment = gradingAssignmentDao.createGradingAssignment(choosedGrader, referenceEntry, assessmentEntry, new Date(), deadLine);
		} else {
			assignment.setAssessmentDate(assessmentDate);
			assignment.setDeadline(deadLine);
			if(choosedGrader == null) {
				assignment.setAssignmentStatus(GradingAssignmentStatus.unassigned);
			} else {
				assignment.setAssignmentStatus(GradingAssignmentStatus.assigned);
				assignment.setAssignmentDate(assessmentDate);
				assignment.setGrader(choosedGrader);
			}
			assignment = gradingAssignmentDao.updateAssignment(assignment);
		}
		dbInstance.commit();
		
		if(config != null && config.getNotificationTypeEnum() == GradingNotificationType.afterTestSubmission) {
			if(choosedGrader != null) {
				sendGraderAsssignmentNotification(choosedGrader, referenceEntry, assignment, config);
			}
			assignment.setAssignmentNotificationDate(new Date());
			assignment = gradingAssignmentDao.updateAssignment(assignment);
			dbInstance.commit();
		}
		return assignment;
	}
	
	protected GraderToIdentity selectGrader(RepositoryEntry referenceEntry) {
		List<GraderToIdentity> activeGraders = activeGraders(referenceEntry);
		GraderToIdentity choosedGrader = null;
		if(activeGraders.size() == 1) {
			choosedGrader = activeGraders.get(0);
		} else if(activeGraders.size() > 1) {
			GradersSearchParameters searchParameters = new GradersSearchParameters();
			searchParameters.setReferenceEntry(referenceEntry);
			List<GraderStatistics> gradersStatistics = gradedToIdentityDao.getGradersStatistics(searchParameters);
			choosedGrader = selectedLessAssigned(gradersStatistics, activeGraders);
		}
		return choosedGrader;
	}
	
	/**
	 * @param referenceEntry The reference / test entry (mandatory)
	 * @return A list of graders, active and not in vacation
	 */
	private List<GraderToIdentity> activeGraders(RepositoryEntry referenceEntry) {
		OLATResource resource = referenceEntry.getOlatResource();
		List<GraderToIdentity> graders = gradedToIdentityDao.getGraders(referenceEntry);
		List<AbsenceLeave> absenceLeaves = gradedToIdentityDao.getGradersAbsenceLeaves(referenceEntry);
		final Set<Long> excludedGraderKeys = new HashSet<>();
		Date nextWorkingDay = CalendarUtils.addWorkingDays(new Date(), 1);
		for(AbsenceLeave absenceLeave:absenceLeaves) {
			// the absence leaves are on the reference entry (no sub-identifier needed)
			if(AbsenceLeaveHelper.isOnLeave(nextWorkingDay, absenceLeave, resource, null)) {
				excludedGraderKeys.add(absenceLeave.getIdentity().getKey());
			}
		}

		return graders.stream()
				.filter(grader -> grader.getGraderStatus().equals(GraderStatus.activated))
				.filter(grader -> !excludedGraderKeys.contains(grader.getIdentity().getKey()))
				.collect(Collectors.toList());
	}
	
	private GraderToIdentity selectedLessAssigned(List<GraderStatistics> gradersStatistics, List<GraderToIdentity> activeGraders) {
		Map<Long, GraderStatistics> statisticsMap = gradersStatistics.stream()
				.collect(Collectors.toMap(GraderStatistics::getKey, Function.identity(), (u, v) -> u));

		long minDone = Long.MAX_VALUE;
		List<GraderToIdentity> finalCandidates = new ArrayList<>(activeGraders.size());
		for(GraderToIdentity grader:activeGraders) {
			long done = 0;
			if(statisticsMap.containsKey(grader.getIdentity().getKey())) {
				done = statisticsMap.get(grader.getIdentity().getKey()).getTotalAssignments();
			}
			if(done < 0) {
				done = 0;
			}
			
			if(done < minDone) {
				minDone = done;
				finalCandidates.clear();
				finalCandidates.add(grader);
			} else if(done == minDone) {
				finalCandidates.add(grader);
			}
		}

		if(finalCandidates.isEmpty()) {
			if(activeGraders.isEmpty()) {
				return null;
			} else if(activeGraders.size() == 1) {
				return activeGraders.get(0);
			} else {
				Collections.shuffle(activeGraders);
				return activeGraders.get(0);
			}
		} else if(finalCandidates.size() == 1) {
			return finalCandidates.get(0);
		}
		Collections.shuffle(finalCandidates);
		return finalCandidates.get(0);
	}

	@Override
	public GradingAssignment assignGrader(GradingAssignment assignment, Identity graderIdentity, GraderMailTemplate mailTemplate, MailerResult result) {
		RepositoryEntry refEntry = assignment.getReferenceEntry();
		GraderToIdentity grader = gradedToIdentityDao.getGrader(refEntry, graderIdentity);
		if(grader == null) {
			grader = gradedToIdentityDao.createRelation(refEntry, graderIdentity);
		} else if(grader.getGraderStatus() != GraderStatus.activated) {
			grader.setGraderStatus(GraderStatus.activated);
			grader = gradedToIdentityDao.updateGrader(grader);
		}
		return assignGrader(assignment, grader, mailTemplate, result);
	}
	
	private GradingAssignment assignGrader(GradingAssignment assignment, GraderToIdentity grader, GraderMailTemplate mailTemplate, MailerResult result) {
		assignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		assignment.setAssignmentStatus(GradingAssignmentStatus.assigned);
		assignment.setExtendedDeadline(null);
		assignment.setAssignmentDate(new Date());
		assignment.setReminder1Date(null);
		assignment.setReminder2Date(null);
		((GradingAssignmentImpl)assignment).setGrader(grader);
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		dbInstance.commit();
		
		log.info(Tracing.M_AUDIT, "Assign assignment {} to grader {} ({})",
				assignment.getKey(), grader.getKey(), grader.getIdentity());
		
		if(mailTemplate != null) {
			MailContext context = new MailContextImpl("[CoachSite:0][Orders:0][Assignments:0]");
			decorateGraderMailTemplate(assignment, mailTemplate);
			doSendEmails(context, Collections.singletonList(grader), mailTemplate, result);
		}
		return assignment;
	}

	@Override
	public GradingAssignment unassignGrader(GradingAssignment assignment) {
		assignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		
		assignment.setAssignmentStatus(GradingAssignmentStatus.unassigned);
		assignment.setExtendedDeadline(null);
		assignment.setAssignmentDate(null);
		assignment.setReminder1Date(null);
		assignment.setReminder2Date(null);
		((GradingAssignmentImpl)assignment).setGrader(null);
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		log.info(Tracing.M_AUDIT, "Unassign assignment {}", assignment.getKey());
		return assignment;
	}
	
	@Override
	public GradingAssignment deactivateAssignment(GradingAssignment assignment) {
		assignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		
		assignment.setAssignmentStatus(GradingAssignmentStatus.deactivated);
		assignment.setExtendedDeadline(null);
		assignment.setAssignmentDate(null);
		assignment.setReminder1Date(null);
		assignment.setReminder2Date(null);
		((GradingAssignmentImpl)assignment).setGrader(null);
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		log.info(Tracing.M_AUDIT, "Deactivate assignment {}", assignment.getKey());
		return assignment;
	}

	@Override
	public GradingAssignment assignmentDone(GradingAssignment assignment, Long metadataTime, Boolean resultsVisibleToUser) {
		assignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		assignment.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment.setClosingDate(new Date());
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		if(metadataTime != null) {
			GradingTimeRecord timeRecord = (GradingTimeRecord)getCurrentTimeRecord(assignment, new Date());
			timeRecord.setMetadataTime(metadataTime.longValue());
			gradingTimeRecordDao.updateTimeRecord(timeRecord);
		}
		log.info(Tracing.M_AUDIT, "Assignment done {}", assignment.getKey());
		dbInstance.commit();
		
		if(resultsVisibleToUser != null && resultsVisibleToUser.booleanValue()) {
			sendParticipantNotification(assignment);
		}
		return assignment;
	}
	
	private void sendParticipantNotification(GradingAssignment assignment) {
		RepositoryEntry referenceEntry = assignment.getReferenceEntry();
		RepositoryEntry entry = assignment.getAssessmentEntry().getRepositoryEntry();
		Identity assessedIdentity = assignment.getAssessmentEntry().getIdentity();
		String language = assessedIdentity.getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(GradingAssignmentsListController.class, locale);

		GraderMailTemplate template = GraderMailTemplate.notificationParticipant(translator, entry, null, referenceEntry);
		if(template == null) {
			return;
		}
		
		MailContext context = new MailContextImpl("[CoachSite:0][Grading:0]");
		decorateGraderMailTemplate(assignment, template);

		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, assessedIdentity, template, null, null, result);
		MailerResult sendResult = mailManager.sendMessage(bundle);
		result.append(sendResult);
		
		if(result.getReturnCode() != MailerResult.OK) {
			log.warn(Tracing.M_AUDIT, "Cannot send notification mail to {} for assignment: {}", assessedIdentity, assignment);
		}
	}
	
	@Override
	public GradingAssignment reopenAssignment(GradingAssignment assignment, Date assessmentDate) {
		assignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		assignment.setAssignmentStatus(GradingAssignmentStatus.assigned);
		if(assessmentDate != null) {
			assignment.setAssessmentDate(assessmentDate);
		}
		assignment.setClosingDate(null);
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		log.info(Tracing.M_AUDIT, "Assignment reopened {}", assignment.getKey());
		return assignment;
	}

	@Override
	public GradingAssignment extendAssignmentDeadline(GradingAssignment assignment, Date newDeadline) {
		assignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		assignment.setExtendedDeadline(newDeadline);
		assignment.setClosingDate(null);
		return gradingAssignmentDao.updateAssignment(assignment);
	}

	@Override
	public void updateDeadline(RepositoryEntry referenceEntry, RepositoryEntryGradingConfiguration configuration) {
		Integer gradingPeriod = configuration.getGradingPeriod();
		if(gradingPeriod == null) {
			gradingAssignmentDao.removeDeadline(referenceEntry);
		} else {
			int count = 0;
			List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(referenceEntry);
			for(GradingAssignment assignment:assignments) {
				Date assignmentDate = assignment.getAssignmentDate();
				if(assignmentDate == null) {
					continue;
				}
				
				Date deadline = assignment.getDeadline();
				Date newDeadline = CalendarUtils.addWorkingDays(assignmentDate, gradingPeriod.intValue());
				newDeadline = CalendarUtils.endOfDay(newDeadline);
				if(deadline == null || !deadline.equals(newDeadline)) {
					assignment.setDeadline(newDeadline);
					if(assignment.getExtendedDeadline() != null && assignment.getExtendedDeadline().before(newDeadline)) {
						assignment.setExtendedDeadline(null);
					}
					gradingAssignmentDao.updateAssignment(assignment);
				}
				
				if(++count % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
		}
		dbInstance.commit();
	}

	@Override
	public List<RepositoryEntry> getReferenceRepositoryEntriesWithGrading(Identity identity) {
		List<RepositoryEntry> entries = gradedToIdentityDao.getReferenceRepositoryEntries(identity, ImsQTI21Resource.TYPE_NAME);
		return new ArrayList<>(new HashSet<>(entries));
	}

	@Override
	public List<RepositoryEntry> getReferenceRepositoryEntriesAsGrader(IdentityRef grader) {
		List<RepositoryEntry> entries = gradedToIdentityDao.getReferenceRepositoryEntriesAsGrader(grader);
		return new ArrayList<>(new HashSet<>(entries));
	}

	@Override
	public List<RepositoryEntry> getEntriesWithGrading(RepositoryEntryRef referenceEntry) {
		return gradingAssignmentDao.getEntries(referenceEntry);
	}

	@Override
	public List<RepositoryEntry> getEntriesWithGrading(IdentityRef identity) {
		return gradingAssignmentDao.getEntries(identity);
	}

	@Override
	public List<Identity> getGraders(Identity identity) {
		List<Identity> graders = gradedToIdentityDao.getGraders(identity);
		return graders.stream().filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public GradingTimeRecordRef getCurrentTimeRecord(GradingAssignment assignment, Date date) {
		date = CalendarUtils.startOfDay(date);
		GradingTimeRecord record = gradingTimeRecordDao.loadRecord(assignment.getGrader(), assignment, date);
		if(record == null) {
			record = gradingTimeRecordDao.createRecord(assignment.getGrader(), assignment, date);
			dbInstance.commit();
		}
		return record;
	}
	
	@Override
	public void appendTimeTo(GradingTimeRecordRef record, long addedTime, TimeUnit unit) {
		gradingTimeRecordDao.appendTimeInSeconds(record, unit.toSeconds(addedTime));
	}

	@Override
	public boolean hasRecordedTime(GradingAssignment assignment) {
		return gradingTimeRecordDao.hasRecordedTime(assignment);
	}

	@Override
	public AssessmentEntry loadFullAssessmentEntry(AssessmentEntry assessmentEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select data from assessmententry data")
		  .append(" left join fetch data.repositoryEntry as cEntry")
		  .append(" left join fetch cEntry.olatResource as cOlatResource")
		  .append(" left join fetch data.referenceEntry as rEntry")
		  .append(" left join fetch rEntry.olatResource as rOlatResource")
		  .append(" left join fetch data.identity as ident")
		  .append(" left join fetch ident.user as identUser")
		  .append(" where data.key=:key");
		
		List<AssessmentEntry> entries = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentEntry.class)
			.setParameter("key", assessmentEntry.getKey())
			.getResultList();
		return entries == null || entries.isEmpty() ? null : entries.get(0);
	}

	@Override
	public String getCachedCourseElementTitle(final RepositoryEntry entry, final String subIdent) {
		String elementTitle = null;
		if(StringHelper.containsNonWhitespace(subIdent)) {
			final CourseElementKey elementKey = new CourseElementKey(entry.getKey(), subIdent);
			elementTitle = courseElementTitleCache.computeIfAbsent(elementKey, key -> {
				OLATResource courseResource = entry.getOlatResource();
				if("CourseModule".equals(courseResource.getResourceableTypeName())) {
					try {
						ICourse course = CourseFactory.loadCourse(courseResource);
						CourseNode node = course.getRunStructure().getNode(subIdent);
						if(node != null) {
							return node.getShortTitle();
						}
					} catch (CorruptedCourseException e) {
						log.error("Course corrupted: {} ({})", entry.getKey(), entry.getOlatResource().getResourceableId(), e);
					}
				}
				return null;
			});
		}
		return elementTitle;
	}
	
	@Override
	public void graderAbsenceLeavesCheckWorkingDays() {
		Date now = new Date();
		if(CalendarUtils.isWorkingDay(now)) {
			graderAbsenceLeavesCheck();
		}
	}

	public void graderAbsenceLeavesCheck() {
		List<GraderToIdentity> graders = gradedToIdentityDao.findGradersWithAssignmentInAbsenceLeave(new Date());
		for(GraderToIdentity grader:graders) {
			reassignGraderAssignments(grader);
		}
	}
	
	/**
	 * The method make a rigorous check of the absence leaves
	 * per resource.
	 * 
	 * @param grader The grader to reassign
	 */
	private void reassignGraderAssignments(GraderToIdentity grader) {
		String language = grader.getIdentity().getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(grader,
				GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess);
		Translator translator = Util.createPackageTranslator(GradingAssignmentsListController.class, locale);
		
		List<AbsenceLeave> absenceLeaves = absenceLeaveDao.getAbsenceLeaves(grader.getIdentity());
		
		for(GradingAssignment assignment:assignments) {
			RepositoryEntry referenceEntry = assignment.getReferenceEntry();
			boolean matchAbsence = false;
			for(AbsenceLeave absenceLeave:absenceLeaves) {
				if(AbsenceLeaveHelper.isOnLeave(new Date(), absenceLeave, referenceEntry.getOlatResource(), null)) {
					matchAbsence = true;
				}
			}
			
			if(matchAbsence) {
				log.info(Tracing.M_AUDIT, "Reassign assigment ({}) of grader on absence leaves {} in resource {} ({})",
						assignment.getKey(), grader.getIdentity().getKey(), referenceEntry.getKey(), referenceEntry.getDisplayname());
			
				RepositoryEntryGradingConfiguration config = gradingConfigurationDao.getConfiguration(referenceEntry);
				unassignGrader(assignment);
				dbInstance.commit();
				
				MailerResult result = new MailerResult();
				GraderToIdentity replacementGrader = selectGrader(referenceEntry);
				if(replacementGrader != null) {
					GraderMailTemplate reassignmentTemplate = GraderMailTemplate.notification(translator, null, null, referenceEntry, config);
					assignGrader(assignment, replacementGrader, reassignmentTemplate, result);
					log.info(Tracing.M_AUDIT, "Reassignment of {} from grader {} (due to absence leaves) to {} in resource {} ({})",
							assignment.getKey(), grader.getIdentity(), replacementGrader.getIdentity(), referenceEntry.getKey(), referenceEntry.getDisplayname());
				}
			}
			dbInstance.commit();
		}
	}
}
