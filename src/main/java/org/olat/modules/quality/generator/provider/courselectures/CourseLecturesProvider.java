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
package org.olat.modules.quality.generator.provider.courselectures;

import static org.olat.modules.quality.generator.ProviderHelper.addDays;
import static org.olat.modules.quality.generator.ProviderHelper.addMinutes;
import static org.olat.modules.quality.generator.ProviderHelper.subtractDays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.GeneratorPreviewSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.QualityGeneratorOverrides;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.model.QualityPreviewImpl;
import org.olat.modules.quality.generator.provider.courselectures.manager.CourseLecturesProviderDAO;
import org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo;
import org.olat.modules.quality.generator.provider.courselectures.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.courselectures.ui.CourseLectureProviderConfigController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.generator.ui.RepositoryEntryBlackListController;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseLecturesProvider implements QualityGeneratorProvider {

	private static final Logger log = Tracing.createLoggerFor(CourseLecturesProvider.class);

	public static final String TYPE = "course-lecture";
	public static final String CONFIG_KEY_DURATION_DAYS = "duration.days";
	public static final String CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS = "announcement.coach.days";
	public static final String CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS = "invitation.after.dc.start.days";
	public static final String CONFIG_KEY_MINUTES_BEFORE_END = "minutes before end";
	public static final String CONFIG_KEY_REMINDER1_AFTER_DC_DAYS = "reminder1.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER2_AFTER_DC_DAYS = "reminder2.after.dc.start.days";
	public static final String CONFIG_KEY_ROLES = "participants.roles";
	public static final String CONFIG_KEY_SURVEY_LECTURE = "survey.lecture.start";
	public static final String CONFIG_KEY_SURVEY_LECTURE_NUMBER = "survey.lecture";
	public static final String CONFIG_KEY_SURVEY_LECTURE_LAST = "survey.lecture.last";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String CONFIG_KEY_TOTAL_LECTURES_MIN = "total.lecture";
	public static final String CONFIG_KEY_TOTAL_LECTURES_MAX = "total.lecture.max";
	public static final String CONFIG_KEY_TOPIC = "topic";
	public static final String CONFIG_KEY_TOPIC_COACH = "config.topic.coach";
	public static final String CONFIG_KEY_TOPIC_COURSE = "config.topic.course";
	public static final String ROLES_DELIMITER = ",";
	public static final String TEACHING_COACH = "teaching.coach";
	public static final String CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION = "educational.type.exclusion";
	public static final String EDUCATIONAL_TYPE_EXCLUSION_DELIMITER = ",";
	
	@Autowired
	private CourseLecturesProviderDAO providerDao;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private TitleCreator titleCreator;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayname(Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseLectureProviderConfigController.class, locale);
		return translator.translate("provider.display.name");
	}
	
	@Override
	public QualityDataCollectionTopicType getGeneratedTopicType(QualityGeneratorConfigs configs) {
		return CONFIG_KEY_TOPIC_COACH.equals(getTopicKey(configs))
				? QualityDataCollectionTopicType.IDENTIY
				: QualityDataCollectionTopicType.REPOSITORY;
	}

	@Override
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		return new CourseLectureProviderConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverrides overrides, Date fromDate,
			Date toDate, Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseLectureProviderConfigController.class, locale);
		
		EnableInfoStrategy strategy = new EnableInfoStrategy();
		provide(generator, configs, overrides, strategy, fromDate, toDate, null, true, true);
		
		return translator.translate("generate.info", new String[] { String.valueOf(strategy.getCount())});
	}
	
	private class EnableInfoStrategy implements CourseLecturesStrategy {
		
		private int count = 0;
		
		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs, String idenitifer,
				Long generatorProviderKey, QualityGeneratorOverride override, Date generatedStart,
				RepositoryEntry courseEntry, List<Organisation> courseOrganisations, Identity teacher,
				QualityDataCollectionTopicType topicType) {
			count++;
		}
		
		public int getCount() {
			return count;
		}
		
	}

	@Override
	public boolean hasWhiteListController() {
		return true;
	}

	@Override
	public Controller getWhiteListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return new RepositoryEntryWhiteListController(ureq, wControl, stackPanel, configs);
	}

	@Override
	public boolean hasBlackListController() {
		return true;
	}

	@Override
	public Controller getBlackListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return new RepositoryEntryBlackListController(ureq, wControl, stackPanel, configs);
	}

	@Override
	public List<QualityDataCollection> generate(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, Date fromDate, Date toDate) {
		
		DataCollectionStrategy strategy = new DataCollectionStrategy();
		provide(generator, configs, overrides, strategy, fromDate, toDate, null, true, true);
		
		return strategy.getDataCollections();
	}

	private List<LectureBlockInfo> loadLectureBlockInfo(QualityGenerator generator, QualityGeneratorConfigs configs,
			SearchParameters searchParams, boolean excludeDeadlineInPast) {
		log.debug("Generator {} searches with {}", generator, searchParams);
		
		List<LectureBlockInfo> blockInfos = providerDao.loadLectureBlockInfo(searchParams);
		log.debug("Generator {} found {} entries", generator, blockInfos.size());
		
		if (excludeDeadlineInPast) {
			String minutesBeforeEnd = configs.getValue(CONFIG_KEY_MINUTES_BEFORE_END);
			String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
			Predicate<? super LectureBlockInfo> deadlineIsInPast = new DeadlineIsInPast(minutesBeforeEnd, duration);
			blockInfos.removeIf(deadlineIsInPast);
			log.debug("Generator {} has {} entries after removal of entries with deadline in past.", generator, blockInfos.size());
		}
		
		return blockInfos;
	}
	
	private class DataCollectionStrategy implements CourseLecturesStrategy {
		
		private final List<QualityDataCollection> dataCollections = new ArrayList<>();

		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs, String idenitifer,
				Long generatorProviderKey, QualityGeneratorOverride override, Date generatedStart,
				RepositoryEntry courseEntry, List<Organisation> courseOrganisations, Identity teacher,
				QualityDataCollectionTopicType topicType) {
			QualityDataCollection dataCollection = generateDataCollection(generator, configs, 
					generatorProviderKey, override, generatedStart, courseEntry, courseOrganisations,
					teacher, topicType);
			dataCollections.add(dataCollection);
		}
		
		private List<QualityDataCollection> getDataCollections() {
			return dataCollections;
		}
		
	}
	
	private QualityDataCollection generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			Long generatorProviderKey, QualityGeneratorOverride override, Date generatedStart,
			 RepositoryEntry courseEntry, List<Organisation> courseOrganisations,
			Identity teacher, QualityDataCollectionTopicType topicType) {
		// create data collection
		RepositoryEntry formEntry = generator.getFormEntry();
		QualityDataCollection dataCollection = qualityService.createDataCollection(courseOrganisations, formEntry,
				generator, generatorProviderKey);

		// fill in data collection attributes
		Date dcStart = override != null? override.getStart(): generatedStart;
		dataCollection.setStart(dcStart);
		
		Date deadline = getDataCollectionEnd(configs, dcStart);
		dataCollection.setDeadline(deadline);
		
		String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
		String title = titleCreator.merge(titleTemplate, List.of(courseEntry, teacher.getUser()));
		dataCollection.setTitle(title);
		
		dataCollection.setTopicType(topicType);
		if (QualityDataCollectionTopicType.IDENTIY == topicType) {
			dataCollection.setTopicIdentity(teacher);
		} else if (QualityDataCollectionTopicType.REPOSITORY == topicType) {
			dataCollection.setTopicRepositoryEntry(courseEntry);
		}
		
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.READY);
		qualityService.updateDataCollection(dataCollection);
		
		// add participants
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		boolean teachingCoach = false;
		boolean allCoaches = false;
		for (String roleName: roleNames) {
			if (TEACHING_COACH.equals(roleName)) {
				teachingCoach = true;
				continue;
			} else if (GroupRoles.coach.name().equals(roleName)) {
				allCoaches = true;
			}
			GroupRoles role = GroupRoles.valueOf(roleName);
			Collection<Identity> identities = repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, roleName);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, courseEntry, role).build();
			}
		}
		// add teaching coach
		if (teachingCoach && !allCoaches) {
			Collection<Identity> identities = Collections.singletonList(teacher);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, courseEntry, GroupRoles.coach).build();
			}
		}
		
		// make reminders
		QualityReminderType coachReminderType = getReminderType(configs);
		String announcementDay = configs.getValue(CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS);
		if (StringHelper.containsNonWhitespace(announcementDay) && coachReminderType != null) {
			Date announcementDate = subtractDays(dcStart, announcementDay);
			if (dataCollection.getStart().after(new Date())) { // no announcement if already started
				qualityService.createReminder(dataCollection, announcementDate, coachReminderType);
			}
		}
		
		String invitationDay = configs.getValue(CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS);
		if (StringHelper.containsNonWhitespace(invitationDay)) {
			Date invitationDate = addDays(dcStart, invitationDay);
			qualityService.createReminder(dataCollection, invitationDate, QualityReminderType.INVITATION);
		}
		
		String reminder1Day = configs.getValue(CONFIG_KEY_REMINDER1_AFTER_DC_DAYS);
		if (StringHelper.containsNonWhitespace(reminder1Day)) {
			Date reminder1Date = addDays(dcStart, reminder1Day);
			qualityService.createReminder(dataCollection, reminder1Date, QualityReminderType.REMINDER1);
		}

		String reminder2Day = configs.getValue(CONFIG_KEY_REMINDER2_AFTER_DC_DAYS);
		if (StringHelper.containsNonWhitespace(reminder2Day)) {
			Date reminder2Date = addDays(dcStart, reminder2Day);
			qualityService.createReminder(dataCollection, reminder2Date, QualityReminderType.REMINDER2);
		}
		
		return dataCollection;
	}
	
	private SearchParameters getSeachParameters(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<? extends OrganisationRef> organisations, Date fromDate, Date toDate, Collection<Long> teacherKeys,
			Collection<Long> courseEntryKeys, boolean excludeBlacklisted, boolean applyAnnouncement) {
		SearchParameters searchParams = new SearchParameters();
		if (CONFIG_KEY_TOPIC_COACH.equals(getTopicKey(configs))) {
			searchParams.setExcludeGeneratorAndTopicIdentityRef(generator);
		} else {
			searchParams.setExcludeGeneratorAndTopicRepositoryRef(generator);
		}
		searchParams.setOrganisationRefs(organisations);

		String minutesBeforeEnd = configs.getValue(CONFIG_KEY_MINUTES_BEFORE_END);
		minutesBeforeEnd = StringHelper.containsNonWhitespace(minutesBeforeEnd)? minutesBeforeEnd: "0";
		if (fromDate != null) {
			Date from = addMinutes(fromDate, minutesBeforeEnd);
			searchParams.setFrom(from);
		}
		if (toDate != null) {
			Date to = addMinutes(toDate, minutesBeforeEnd);
			if (applyAnnouncement) {
				to = addAnnouncement(configs, to);
			}
			searchParams.setTo(to);
		}
		
		if (courseEntryKeys != null)  {
			searchParams.setWhiteListKeys(courseEntryKeys);
		} else {
			List<RepositoryEntryRef> whiteListRefs = RepositoryEntryWhiteListController.getRepositoryEntryRefs(configs);
			searchParams.setWhiteListRefs(whiteListRefs);
		}
		
		if (excludeBlacklisted) {
			List<RepositoryEntryRef> blackListRefs = RepositoryEntryBlackListController.getRepositoryEntryRefs(configs);
			searchParams.setBlackListRefs(blackListRefs);
		}
		
		if (teacherKeys != null) {
			searchParams.setTeacherKeys(teacherKeys);
		}
		
		String minLectures = configs.getValue(CONFIG_KEY_TOTAL_LECTURES_MIN);
		if (StringHelper.containsNonWhitespace(minLectures)) {
			Integer minExceedingLectures = Integer.parseInt(minLectures);
			searchParams.setMinTotalLectures(minExceedingLectures);
		}
		
		String maxLectures = configs.getValue(CONFIG_KEY_TOTAL_LECTURES_MAX);
		if (StringHelper.containsNonWhitespace(maxLectures)) {
			Integer maxExceedingLectures = Integer.parseInt(maxLectures);
			searchParams.setMaxTotalLectures(maxExceedingLectures);
		}
		
		updateSurveyLectureKey(configs, generator);
		String surveyLecture = configs.getValue(CONFIG_KEY_SURVEY_LECTURE);
		switch (surveyLecture) {
		case CONFIG_KEY_SURVEY_LECTURE_LAST:
			searchParams.setLastLectureBlock(true);
			break;
		case CONFIG_KEY_SURVEY_LECTURE_NUMBER:
			String lectureNumber = configs.getValue(CONFIG_KEY_SURVEY_LECTURE_NUMBER);
			try {
				Integer selectingLecture = Integer.parseInt(lectureNumber);
				searchParams.setSelectingLecture(selectingLecture);
			} catch (Exception e) {
				searchParams.setSelectingLecture(-1); // select nothing
				log.warn("Quality data collection generator is not properly configured: " + generator);
			}
			break;
		default:
			searchParams.setSelectingLecture(-1); // select nothing
			log.warn("Quality data collection generator is not properly configured: " + generator);
			break;
		}
		
		String educationalTypeConfig = configs.getValue(CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION);
		if (StringHelper.containsNonWhitespace(educationalTypeConfig)) {
			Collection<Long> excludedEducationalTypeKeys = Arrays.asList(educationalTypeConfig.split(EDUCATIONAL_TYPE_EXCLUSION_DELIMITER)).stream()
					.filter(StringHelper::isLong)
					.map(Long::valueOf)
					.collect(Collectors.toSet());
			searchParams.setExcludedEducationalTypeKeys(excludedEducationalTypeKeys);
		}
		
		return searchParams;
	}

	/**
	 * CONFIG_KEY_SURVEY_LECTURE was added in the second version of that generator.
	 * If not set, init the value as function of other CONFIG_KEYs.
	 *
	 * @param configs
	 * @param generator 
	 */
	private void updateSurveyLectureKey(QualityGeneratorConfigs configs, QualityGenerator generator) {
		String surveyLecture = configs.getValue(CONFIG_KEY_SURVEY_LECTURE);
		boolean surveyLectureNotSet = !StringHelper.containsNonWhitespace(surveyLecture);
		if (surveyLectureNotSet) {
			configs.setValue(CONFIG_KEY_SURVEY_LECTURE, CONFIG_KEY_SURVEY_LECTURE_NUMBER);
			log.info("Updated CONFIG_KEY_SURVEY_LECTURE to CONFIG_KEY_SURVEY_LECTURE_NUMBER for quality generator: " + generator);
		}
	}

	private String getTopicKey(QualityGeneratorConfigs configs) {
		String topicKey = configs.getValue(CONFIG_KEY_TOPIC);
		return StringHelper.containsNonWhitespace(topicKey)? topicKey: CONFIG_KEY_TOPIC_COACH;
	}
	
	private void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, CourseLecturesStrategy strategy, Date fromDate, Date toDate,
			List<Long> courseEntryKeys, boolean excludeBlacklisted, boolean applyAnnouncement) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters lectureSearchParams = getSeachParameters(generator, configs, organisations,
				fromDate, toDate, null, courseEntryKeys, excludeBlacklisted, applyAnnouncement);
		List<LectureBlockInfo> infos = loadLectureBlockInfo(generator, configs, lectureSearchParams, true);
		Date toWithAnnouncement = applyAnnouncement? addAnnouncement(configs, toDate): toDate;
		List<QualityGeneratorOverride> manualStartInRangeOverrides = overrides.getOverrides(generator, fromDate, toWithAnnouncement);
		
		QualityDataCollectionTopicType topicType = getGeneratedTopicType(configs);
		
		List<LectureBlockInfo> infosStartOutOfRange = new ArrayList<>(1);
		for (LectureBlockInfo lectureBlockInfo: infos) {
			String identifier = getIdentifier(generator, () -> lectureBlockInfo.getCourseRepoKey(), () -> lectureBlockInfo.getTeacherKey());
			QualityGeneratorOverride startOverride = overrides.getOverride(identifier);
			if (startOverride != null) {
				if (startOverride.getStart() != null) {
					Date dcEnd = getDataCollectionEnd(configs, startOverride.getStart());
					if (startOverride.getStart().after(toWithAnnouncement) || dcEnd.before(fromDate)) {
						infosStartOutOfRange.add(lectureBlockInfo);
					}
				}
				manualStartInRangeOverrides.remove(startOverride);
			}
		}
		infos.removeAll(infosStartOutOfRange);
		
		if (!manualStartInRangeOverrides.isEmpty()) {
			List<Long> searchKeys = manualStartInRangeOverrides.stream()
					.map(override -> override.getIdentifier().split("::"))
					.filter(identifierParts -> identifierParts.length == 3)
					.map(identifierParts -> QualityDataCollectionTopicType.IDENTIY == topicType? identifierParts[2]: identifierParts[1])
					.map(Long::valueOf)
					.toList();
			Collection<Long> identityKeys = null;
			Collection<Long> repositoryEntryKeys = null;
			if (QualityDataCollectionTopicType.IDENTIY == topicType) {
				identityKeys = searchKeys;
				repositoryEntryKeys = lectureSearchParams.getWhiteListKeys();
			} else {
				repositoryEntryKeys = searchKeys;
				if (courseEntryKeys != null) {
					repositoryEntryKeys.retainAll(lectureSearchParams.getWhiteListKeys());
				}
			}
			SearchParameters manualStartInRangeSearchParams = getSeachParameters(generator, configs, organisations, null,
					null, identityKeys, repositoryEntryKeys, excludeBlacklisted, applyAnnouncement);
			List<LectureBlockInfo> manualStartInRangeInfos = loadLectureBlockInfo(generator, configs, manualStartInRangeSearchParams, false);
			infos.addAll(manualStartInRangeInfos);
		}
		
		Map<Long, RepositoryEntry> entryKeyToEntry = repositoryService
				.loadByKeys(infos.stream().map(LectureBlockInfo::getCourseRepoKey).toList()).stream()
				.collect(Collectors.toMap(RepositoryEntryRef::getKey, Function.identity(), (u, v) -> v));
		Map<Long, List<Organisation>> entryKeyToOrganisation = repositoryService
				.getRepositoryEntryOrganisations(entryKeyToEntry.values()).entrySet().stream()
				.collect(Collectors.toMap(es -> es.getKey().getKey(), Entry::getValue, (u, v) -> v));
		Map<Long, Identity> identityKeyToIdentity = securityManager
				.loadIdentityByKeys(infos.stream().map(LectureBlockInfo::getTeacherKey).toList()).stream()
				.collect(Collectors.toMap(Identity::getKey, Function.identity()));
		
		for (LectureBlockInfo lectureBlockInfo: infos) {
			RepositoryEntry courseEntry = entryKeyToEntry.get(lectureBlockInfo.getCourseRepoKey());
			Identity teacher = identityKeyToIdentity.get(lectureBlockInfo.getTeacherKey());
			if (courseEntry == null || teacher == null || topicType == null) {
				continue;
			}
			
			Long generatorProviderKey = getGeneratorProviderKey(lectureBlockInfo, topicType);
			String identifier = getIdentifier(generator, courseEntry, teacher);
			QualityGeneratorOverride override = overrides.getOverride(identifier);
			Date generatedStart = getDataCollectionStart(configs, lectureBlockInfo);
			List<Organisation> courseOrganisations = entryKeyToOrganisation.get(lectureBlockInfo.getCourseRepoKey());
			strategy.provide(generator, configs, identifier, generatorProviderKey, override, generatedStart,
					courseEntry, courseOrganisations, teacher, topicType);
		}
	}

	@Override
	public List<QualityPreview> getPreviews(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, GeneratorPreviewSearchParams searchParams) {
		List<Long> repositoryEntryKeys = null;
		if (searchParams.getRepositoryEntryKeys() != null) {
			repositoryEntryKeys = new ArrayList<>(searchParams.getRepositoryEntryKeys());
			List<RepositoryEntryRef> whiteListRefs = RepositoryEntryWhiteListController.getRepositoryEntryRefs(configs);
			if (whiteListRefs != null && !whiteListRefs.isEmpty()) {
				repositoryEntryKeys.retainAll(whiteListRefs.stream().map(RepositoryEntryRef::getKey).toList());
			}
			if (repositoryEntryKeys.isEmpty()) {
				return List.of();
			}
		}
		
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		List<RepositoryEntryRef> blackListRefs = RepositoryEntryBlackListController.getRepositoryEntryRefs(configs);
		PreviewStrategy strategy = new PreviewStrategy(roleNames, blackListRefs);
		provide(generator, configs, overrides, strategy, searchParams.getDateRange().getFrom(),
				searchParams.getDateRange().getTo(), repositoryEntryKeys, false, false);
		
		return strategy.getPreviews();
	}
	
	private class PreviewStrategy implements CourseLecturesStrategy {
		
		private final String[] roleNames;
		private final Set<Long> blackListKeys;
		private final List<QualityPreview> previews = new ArrayList<>();

		public PreviewStrategy(String[] roleNames, List<RepositoryEntryRef> blackListRefs) {
			this.roleNames = roleNames;
			this.blackListKeys = blackListRefs.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toSet());
		}
		
		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs, String idenitifer,
				Long generatorProviderKey, QualityGeneratorOverride override, Date generatedStart,
				RepositoryEntry courseEntry, List<Organisation> courseOrganisations, Identity teacher,
				QualityDataCollectionTopicType topicType) {

			QualityPreviewImpl preview = new QualityPreviewImpl();
			preview.setIdentifier(idenitifer);
			preview.setFormEntry(generator.getFormEntry());
			preview.setGenerator(generator);
			preview.setGeneratorProviderKey(generatorProviderKey);

			preview.setTopicType(topicType);
			if (QualityDataCollectionTopicType.IDENTIY == topicType) {
				preview.setTopicIdentity(teacher);
			} else if (QualityDataCollectionTopicType.REPOSITORY == topicType) {
				preview.setTopicRepositoryEntry(courseEntry);
			}

			Date start = override != null ? override.getStart() : generatedStart;
			preview.setStart(start);

			Date deadline = getDataCollectionEnd(configs, start);
			preview.setDeadline(deadline);

			String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
			String title = titleCreator.merge(titleTemplate, List.of(courseEntry, teacher.getUser()));
			preview.setTitle(title);

			preview.setOrganisations(courseOrganisations);

			// May be very slow :-(
			List<? extends IdentityRef> participants = getPreviewParticipants(courseEntry, roleNames, teacher);
			preview.setParticipants(participants);

			if (blackListKeys.contains(courseEntry.getKey())) {
				preview.setStatus(QualityPreviewStatus.blacklist);
			} else if (override != null) {
				preview.setStatus(QualityPreviewStatus.changed);
			} else {
				preview.setStatus(QualityPreviewStatus.regular);
			}

			previews.add(preview);
		}
		
		private List<Identity> getPreviewParticipants(RepositoryEntry courseEntry, String[] roleNames, Identity teacher) {
			Set<Identity> identities = new HashSet<>();
			
			boolean teachingCoach = false;
			boolean allCoaches = false;
			for (String roleName: roleNames) {
				if (TEACHING_COACH.equals(roleName)) {
					teachingCoach = true;
					continue;
				} else if (GroupRoles.coach.name().equals(roleName)) {
					allCoaches = true;
				}
				identities.addAll(repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, roleName));
			}
			// add teaching coach
			if (teachingCoach && !allCoaches) {
				identities.add(teacher);
			}
			
			return new ArrayList<>(identities);
		}
		
		public List<QualityPreview> getPreviews() {
			return previews;
		}
		
	}

	private Date getDataCollectionStart(QualityGeneratorConfigs configs, LectureBlockInfo lectureBlockInfo) {
		Date dcStart = lectureBlockInfo.getLectureEndDate();
		String minutesBeforeEnd = configs.getValue(CONFIG_KEY_MINUTES_BEFORE_END);
		minutesBeforeEnd = StringHelper.containsNonWhitespace(minutesBeforeEnd)? minutesBeforeEnd: "0";
		dcStart = addMinutes(dcStart, "-" + minutesBeforeEnd);
		return dcStart;
	}

	private Date getDataCollectionEnd(QualityGeneratorConfigs configs, Date dcStart) {
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		Date deadline = addDays(dcStart, duration);
		return deadline;
	}

	private Date addAnnouncement(QualityGeneratorConfigs configs, Date toDate) {
		String announcementDays = configs.getValue(CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS);
		if (StringHelper.containsNonWhitespace(announcementDays)) {
			toDate = addDays(toDate, announcementDays);
		}
		return toDate;
	}
	
	private QualityReminderType getReminderType(QualityGeneratorConfigs configs) {
		return CONFIG_KEY_TOPIC_COACH.equals(getTopicKey(configs))
				? QualityReminderType.ANNOUNCEMENT_COACH_TOPIC
				: QualityReminderType.ANNOUNCEMENT_COACH_CONTEXT;
	}

	Long getGeneratorProviderKey(LectureBlockInfo lectureBlockInfo, QualityDataCollectionTopicType topicType) {
		return QualityDataCollectionTopicType.IDENTIY == topicType
				? lectureBlockInfo.getCourseRepoKey()
				: lectureBlockInfo.getTeacherKey();
	}

	String getIdentifier(QualityGenerator generator, RepositoryEntryRef courseEntry, IdentityRef teacher) {
		return generator.getKey() + "::" + courseEntry.getKey() + "::" + teacher.getKey();
	}
	
	@Override
	public void addToBlacklist(QualityGeneratorConfigs configs, QualityPreview preview) {
		RepositoryEntryBlackListController.addRepositoryEntryRef(configs, () -> getRepositoryEntryKey(preview));
	}

	@Override
	public void removeFromBlacklist(QualityGeneratorConfigs configs, QualityPreview preview) {
		RepositoryEntryBlackListController.removeRepositoryEntryRef(configs, () -> getRepositoryEntryKey(preview));
	}

	private Long getRepositoryEntryKey(QualityPreview preview) {
		return preview.getTopicType() == QualityDataCollectionTopicType.REPOSITORY? preview.getTopicRepositoryEntry().getKey(): preview.getGeneratorProviderKey();
	}
	
	static interface CourseLecturesStrategy {

		void provide(QualityGenerator generator, QualityGeneratorConfigs configs, String idenitifer,
				Long generatorProviderKey, QualityGeneratorOverride override, Date generatedStart,
				RepositoryEntry courseEntry, List<Organisation> courseOrganisations, Identity teacher,
				QualityDataCollectionTopicType topicType);

	}

}
