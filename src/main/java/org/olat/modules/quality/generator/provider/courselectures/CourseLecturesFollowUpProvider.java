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
import static org.olat.modules.quality.generator.ProviderHelper.toDouble;

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
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionSearchParams;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.courselectures.manager.CourseLecturesProviderDAO;
import org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo;
import org.olat.modules.quality.generator.provider.courselectures.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.courselectures.ui.CourseLectureFollowUpProviderConfigController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseLecturesFollowUpProvider implements QualityGeneratorProvider {

	private static final Logger log = Tracing.createLoggerFor(CourseLecturesFollowUpProvider.class);

	public static final String CONFIG_KEY_DURATION_DAYS = "duration.days";
	public static final String CONFIG_KEY_GRADE_TOTAL_LIMIT = "grade.total.limit";
	public static final String CONFIG_KEY_GRADE_TOTAL_CHECK_KEY = "grade.total.check.key";
	public static final String CONFIG_KEY_GRADE_SINGLE_LIMIT = "grade.single.limit";
	public static final String CONFIG_KEY_GRADE_SINGLE_CHECK_KEY = "grade.single.check.key";
	public static final String CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS = "accouncement.coach.days";
	public static final String CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS = "invitation.after.dc.start.days";
	public static final String CONFIG_KEY_MINUTES_BEFORE_END = "minutes before end";
	public static final String CONFIG_KEY_PREVIOUS_GENERATOR_KEY = "previous.generator.key";
	public static final String CONFIG_KEY_REMINDER1_AFTER_DC_DAYS = "reminder1.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER2_AFTER_DC_DAYS = "reminder2.after.dc.start.days";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String CONFIG_KEY_TOTAL_LECTURES_MIN = "total.lecture";
	public static final String CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION = "educational.type.exclusion";
	public static final String EDUCATIONAL_TYPE_EXCLUSION_DELIMITER = ",";

	public static final String ROLES_DELIMITER = ",";
	
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
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	@Override
	public String getType() {
		return "course-lecture-followup";
	}

	@Override
	public String getDisplayname(Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseLectureFollowUpProviderConfigController.class, locale);
		return translator.translate("followup.provider.display.name");
	}

	@Override
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		return  new CourseLectureFollowUpProviderConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate,
			Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseLectureFollowUpProviderConfigController.class, locale);

		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		String previousGeneratorKey = configs.getValue(CONFIG_KEY_PREVIOUS_GENERATOR_KEY);
		QualityGeneratorRef previousGeneratorRef = QualityGeneratorRef.of(previousGeneratorKey);
		QualityGeneratorConfigs previosGeneratorConfigs = getPreviosGeneratorConfigs(previousGeneratorRef);
		
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations, fromDate, toDate,
				previousGeneratorRef, previosGeneratorConfigs);
		List<LectureBlockInfo> lectureBlockInfos = loadLectureBlockInfo(generator, configs, searchParams);
		lectureBlockInfos.removeIf(lb -> gradeIsSufficient(lb, configs, previousGeneratorRef, previosGeneratorConfigs));
		int count = lectureBlockInfos.size();
		
		return translator.translate("followup.generate.info", new String[] { String.valueOf(count)});
	}

	@Override
	public boolean hasWhiteListController() {
		return false;
	}

	@Override
	public Controller getWhiteListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return null;
	}

	@Override
	public boolean hasBlackListController() {
		return false;
	}

	@Override
	public Controller getBlackListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return null;
	}

	@Override
	public List<QualityDataCollection> generate(QualityGenerator generator, QualityGeneratorConfigs configs,
			Date fromDate, Date toDate) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		
		String previousGeneratorKey = configs.getValue(CONFIG_KEY_PREVIOUS_GENERATOR_KEY);
		QualityGeneratorRef previousGeneratorRef = QualityGeneratorRef.of(previousGeneratorKey);
		QualityGeneratorConfigs previosGeneratorConfigs = getPreviosGeneratorConfigs(previousGeneratorRef);
		
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations, fromDate, toDate,
				previousGeneratorRef, previosGeneratorConfigs);
		List<LectureBlockInfo> lectureBlockInfos = loadLectureBlockInfo(generator, configs, searchParams);
		lectureBlockInfos.removeIf(lb -> gradeIsSufficient(lb, configs, previousGeneratorRef, previosGeneratorConfigs));
		
		List<QualityDataCollection> dataCollections = new ArrayList<>();
		for (LectureBlockInfo lectureBlockInfo: lectureBlockInfos) {
			QualityDataCollection dataCollection = generateDataCollection(generator, configs, organisations,
					lectureBlockInfo, previousGeneratorRef, previosGeneratorConfigs);
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
		log.debug("Generator {} has {} entries after removal of entries with deadline in past.", generator, blockInfos.size());
		
		return blockInfos;
	}

	private QualityDataCollection generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<Organisation> organisations, LectureBlockInfo lectureBlockInfo,
			QualityGeneratorRef previousGeneratorRef, QualityGeneratorConfigs previosGeneratorConfigs) {
		// Load data
		RepositoryEntry course = repositoryService.loadByKey(lectureBlockInfo.getCourseRepoKey());
		Identity teacher = securityManager.loadIdentityByKey(lectureBlockInfo.getTeacherKey());
		String topicKey = getTopicKey(previosGeneratorConfigs);
		QualityDataCollection previousDataCollection = loadPreviousDataCollection(lectureBlockInfo,
				previousGeneratorRef, previosGeneratorConfigs);

		// create data collection
		Long generatorProviderKey = CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH.equals(topicKey)
				? course.getKey()
				: teacher.getKey();
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, previousDataCollection, generator,
				generatorProviderKey);

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
		if (CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH.equals(topicKey)) {
			dataCollection.setTopicType(QualityDataCollectionTopicType.IDENTIY);
			dataCollection.setTopicIdentity(teacher);
			coachReminderType = QualityReminderType.ANNOUNCEMENT_COACH_TOPIC;
		} else if (CourseLecturesProvider.CONFIG_KEY_TOPIC_COURSE.equals(topicKey)) {
			dataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
			dataCollection.setTopicRepositoryEntry(course);
			coachReminderType = QualityReminderType.ANNOUNCEMENT_COACH_CONTEXT;
		}
		
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.READY);
		qualityService.updateDataCollection(dataCollection);
		
		// add participants
		String[] roleNames = previosGeneratorConfigs.getValue(CourseLecturesProvider.CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		boolean teachingCoach = false;
		boolean allCoaches = false;
		for (String roleName: roleNames) {
			if (CourseLecturesProvider.TEACHING_COACH.equals(roleName)) {
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
		
		// make reminder
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
			List<? extends OrganisationRef> organisations, Date fromDate, Date toDate,
			QualityGeneratorRef previousGeneratorRef, QualityGeneratorConfigs previosGeneratorConfigs) {
		SearchParameters searchParams = new SearchParameters();
		searchParams.setOrganisationRefs(organisations);

		if (CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH.equals(getTopicKey(previosGeneratorConfigs))) {
			searchParams.setFinishedDataCollectionForGeneratorAndTopicIdentityRef(previousGeneratorRef);
			searchParams.setExcludeGeneratorAndTopicIdentityRef(generator);
		} else {
			searchParams.setFinishedDataCollectionForGeneratorAndTopicRepositoryRef(previousGeneratorRef);
			searchParams.setExcludeGeneratorAndTopicRepositoryRef(generator);
		}
		
		String minLectures = configs.getValue(CONFIG_KEY_TOTAL_LECTURES_MIN);
		if (StringHelper.containsNonWhitespace(minLectures)) {
			Integer minExceedingLectures = Integer.parseInt(minLectures);
			searchParams.setMinTotalLectures(minExceedingLectures);
		}
		
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
		
		searchParams.setLastLectureBlock(true);
		
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

	private boolean gradeIsSufficient(LectureBlockInfo lectureBlockInfo, QualityGeneratorConfigs configs,
			QualityGeneratorRef previousGeneratorRef, QualityGeneratorConfigs previosGeneratorConfigs) {
		// Load from entry from data collection
		QualityDataCollection dataCollection = loadPreviousDataCollection(lectureBlockInfo, previousGeneratorRef,
				previosGeneratorConfigs);
		if (dataCollection == null) return true;

		// Get config values
		String singleLimitCheckKey = configs.getValue(CONFIG_KEY_GRADE_SINGLE_CHECK_KEY);
		LimitCheck singleLimitCheck = LimitCheck.getEnum(singleLimitCheckKey);
		String singleLimitString = configs.getValue(CONFIG_KEY_GRADE_SINGLE_LIMIT);
		Double singleLimit = toDouble(singleLimitString);
		boolean checkSingleLimit = singleLimitCheck != null && singleLimit != null;
		
		String totalLimitCheckKey = configs.getValue(CONFIG_KEY_GRADE_TOTAL_CHECK_KEY);
		LimitCheck totalLimitCheck = LimitCheck.getEnum(totalLimitCheckKey);
		String totalLimitString = configs.getValue(CONFIG_KEY_GRADE_TOTAL_LIMIT);
		Double totalLimit = toDouble(totalLimitString);
		boolean checkTotalLimit = totalLimitCheck != null && totalLimit != null;
		
		// If no check is defined, create always a follow-up data collection.
		if (!checkSingleLimit && !checkTotalLimit) return false;
		
		// Load evaluation form and sessions
		EvaluationFormSurvey survey = qualityService.loadSurvey(dataCollection);
		org.olat.modules.forms.model.xml.Form evaluationForm = evaluationFormManager.loadForm(survey.getFormEntry());
		SessionFilter filter = SessionFilterFactory.createSelectDone(survey);

		// Load results and calculate if grade is sufficient
		long numberResponses = 0;
		double sumOfAvgs = 0;
		for (AbstractElement element: evaluationForm.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, filter);
				SliderStatistic totalStatistic = rubricStatistic.getTotalStatistic();
				numberResponses = numberResponses + totalStatistic.getNumberOfResponses();
				sumOfAvgs = sumOfAvgs + totalStatistic.getNumberOfResponses() * totalStatistic.getAvg();
				
				// Check the single limit
				if (checkSingleLimit && singleLimitCheck != null) {
					for (Slider slider: rubric.getSliders()) {
						SliderStatistic sliderStatistic = rubricStatistic.getSliderStatistic(slider);
						if (sliderStatistic.getNumberOfResponses() > 0 && singleLimitCheck.isTrue(sliderStatistic.getAvg(), singleLimit)) {
							return false;
						}
					}
				}
			}
		}
		
		// If no one has filled out the previous data collection, no follow-up data collection is needed.
		if (numberResponses == 0) return true;
		
		// Check the total limit
		if (checkTotalLimit && totalLimitCheck != null) {
			double totalAvg = sumOfAvgs / numberResponses;
			return !totalLimitCheck.isTrue(totalAvg, totalLimit);
		}
		return false;
	}

	public QualityDataCollection loadPreviousDataCollection(LectureBlockInfo lectureBlockInfo,
			QualityGeneratorRef previousGeneratorRef, QualityGeneratorConfigs previosGeneratorConfigs) {
		QualityDataCollectionSearchParams searchParams = new QualityDataCollectionSearchParams();
		searchParams.setGeneratorRef(previousGeneratorRef);
		if (CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH.equals(getTopicKey(previosGeneratorConfigs))) {
			searchParams.setTopicIdentityRef(() -> lectureBlockInfo.getTeacherKey());
			searchParams.setGeneratorProviderKey(lectureBlockInfo.getCourseRepoKey());
		} else {
			searchParams.setTopicRepositoryRef(() -> lectureBlockInfo.getCourseRepoKey());
			searchParams.setGeneratorProviderKey(lectureBlockInfo.getTeacherKey());
		}
		List<QualityDataCollection> dataCollections = qualityService.loadDataCollections(searchParams);
		return !dataCollections.isEmpty()? dataCollections.get(0): null;
	}

	private String getTopicKey(QualityGeneratorConfigs configs) {
		String topicKey = configs.getValue(CourseLecturesProvider.CONFIG_KEY_TOPIC);
		return StringHelper.containsNonWhitespace(topicKey)? topicKey: CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH;
	}
	
	private QualityGeneratorConfigs getPreviosGeneratorConfigs(QualityGeneratorRef generatorRef) {
		QualityGenerator generator = generatorService.loadGenerator(generatorRef);
		return generatorService.loadGeneratorConfigs(generator);
	}

}
