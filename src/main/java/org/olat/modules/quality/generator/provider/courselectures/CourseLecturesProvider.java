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
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
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
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.courselectures.manager.CourseLecturesProviderDAO;
import org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo;
import org.olat.modules.quality.generator.provider.courselectures.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.courselectures.ui.CourseLectureProviderConfigController;
import org.olat.modules.quality.generator.ui.CurriculumElementBlackListController;
import org.olat.modules.quality.generator.ui.CurriculumElementWhiteListController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.repository.RepositoryEntry;
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
	public static final String CONFIG_KEY_WHITE_LIST = "white.list";
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
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		return new CourseLectureProviderConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate,
			Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseLectureProviderConfigController.class, locale);

		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations, fromDate, toDate);
		int count = loadLectureBlockInfo(generator, configs, searchParams).size();
		
		return translator.translate("generate.info", new String[] { String.valueOf(count)});
	}

	@Override
	public boolean hasWhiteListController() {
		return true;
	}

	@Override
	public Controller getWhiteListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return new CurriculumElementWhiteListController(ureq, wControl, stackPanel, generator, configs);
	}

	@Override
	public boolean hasBlackListController() {
		return true;
	}

	@Override
	public Controller getBlackListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return new CurriculumElementBlackListController(ureq, wControl, stackPanel, generator, configs);
	}

	@Override
	public List<QualityDataCollection> generate(QualityGenerator generator, QualityGeneratorConfigs configs,
			Date fromDate, Date toDate) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations, fromDate, toDate);
		List<LectureBlockInfo> infos = loadLectureBlockInfo(generator, configs, searchParams);
		
		List<QualityDataCollection> dataCollections = new ArrayList<>();
		for (LectureBlockInfo lectureBlockInfo: infos) {
			QualityDataCollection dataCollection = generateDataCollection(generator, configs, lectureBlockInfo);
			dataCollections.add(dataCollection);
		}
		return dataCollections;
	}

	private List<LectureBlockInfo> loadLectureBlockInfo(QualityGenerator generator, QualityGeneratorConfigs configs,
			SearchParameters searchParams) {
		log.debug("Generator {} searches with {}", generator, searchParams);
		
		List<LectureBlockInfo> blockInfos = providerDao.loadLectureBlockInfo(searchParams);
		log.debug("Generator {} found {} entries", generator, blockInfos.size());
		
		String minutesBeforeEnd = configs.getValue(CONFIG_KEY_MINUTES_BEFORE_END);
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		Predicate<? super LectureBlockInfo> deadlineIsInPast = new DeadlineIsInPast(minutesBeforeEnd, duration);
		blockInfos.removeIf(deadlineIsInPast);
		log.debug("Generator {} has {} entries after removal of entries with deadline in past..", generator, blockInfos.size());
		
		return blockInfos;
	}
	
	private QualityDataCollection generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			LectureBlockInfo lectureBlockInfo) {
		// Load data
		RepositoryEntry formEntry = generator.getFormEntry();
		RepositoryEntry course = repositoryService.loadByKey(lectureBlockInfo.getCourseRepoKey());
		Collection<Organisation> courseOrganisations = repositoryService.getOrganisations(course);
		Identity teacher = securityManager.loadIdentityByKey(lectureBlockInfo.getTeacherKey());
		String topicKey =  getTopicKey(configs);
		
		// create data collection
		Long generatorProviderKey = CONFIG_KEY_TOPIC_COACH.equals(topicKey)? course.getKey(): teacher.getKey();
		QualityDataCollection dataCollection = qualityService.createDataCollection(courseOrganisations, formEntry,
				generator, generatorProviderKey);

		// fill in data collection attributes
		Date dcStart = lectureBlockInfo.getLectureEndDate();
		String minutesBeforeEnd = configs.getValue(CONFIG_KEY_MINUTES_BEFORE_END);
		minutesBeforeEnd = StringHelper.containsNonWhitespace(minutesBeforeEnd)? minutesBeforeEnd: "0";
		dcStart = addMinutes(dcStart, "-" + minutesBeforeEnd);
		dataCollection.setStart(dcStart);
		
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		Date deadline = addDays(dcStart, duration);
		dataCollection.setDeadline(deadline);
		
		String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
		String title = titleCreator.merge(titleTemplate, Arrays.asList(course, teacher.getUser()));
		dataCollection.setTitle(title);

		QualityReminderType coachReminderType = null;
		if (CONFIG_KEY_TOPIC_COACH.equals(topicKey)) {
			dataCollection.setTopicType(QualityDataCollectionTopicType.IDENTIY);
			dataCollection.setTopicIdentity(teacher);
			coachReminderType = QualityReminderType.ANNOUNCEMENT_COACH_TOPIC;
		} else if (CONFIG_KEY_TOPIC_COURSE.equals(topicKey)) {
			dataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
			dataCollection.setTopicRepositoryEntry(course);
			coachReminderType = QualityReminderType.ANNOUNCEMENT_COACH_CONTEXT;
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
			Collection<Identity> identities = repositoryService.getMembers(course, RepositoryEntryRelationType.all, roleName);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, course, role).build();
			}
		}
		// add teaching coach
		if (teachingCoach && !allCoaches) {
			Collection<Identity> identities = Collections.singletonList(teacher);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, course, GroupRoles.coach).build();
			}
		}
		
		// make reminders
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
			List<? extends OrganisationRef> organisations, Date fromDate, Date toDate) {
		SearchParameters searchParams = new SearchParameters();
		if (CONFIG_KEY_TOPIC_COACH.equals(getTopicKey(configs))) {
			searchParams.setExcludeGeneratorAndTopicIdentityRef(generator);
		} else {
			searchParams.setExcludeGeneratorAndTopicRepositoryRef(generator);
		}
		searchParams.setOrganisationRefs(organisations);

		String minutesBeforeEnd = configs.getValue(CONFIG_KEY_MINUTES_BEFORE_END);
		minutesBeforeEnd = StringHelper.containsNonWhitespace(minutesBeforeEnd)? minutesBeforeEnd: "0";
		Date from = addMinutes(fromDate, minutesBeforeEnd);
		Date to = addMinutes(toDate, minutesBeforeEnd);
		
		String announcementDays = configs.getValue(CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS);
		if (StringHelper.containsNonWhitespace(announcementDays)) {
			to = addDays(to, announcementDays);
		}
		
		searchParams.setFrom(from);
		searchParams.setTo(to);
		
		Collection<CurriculumElementRef> curriculumElementRefs = CurriculumElementWhiteListController.getCurriculumElementRefs(configs);
		searchParams.setWhiteListRefs(curriculumElementRefs);
		
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

}
