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
package org.olat.modules.quality.generator.provider.course;

import static org.olat.modules.quality.generator.ProviderHelper.addDays;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.ProviderHelper;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.course.manager.CourseProviderDAO;
import org.olat.modules.quality.generator.provider.course.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.course.ui.CourseProviderConfigController;
import org.olat.modules.quality.generator.ui.GeneratorWhiteListController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseProvider implements QualityGeneratorProvider {

	private static final OLog log = Tracing.createLoggerFor(CourseProvider.class);
	
	public static final String CONFIG_KEY_TRIGGER = "trigger.type";
	public static final String CONFIG_KEY_TRIGGER_BEGIN = "due.date.begin.type";
	public static final String CONFIG_KEY_TRIGGER_END = "due.date.end.type";
	public static final String CONFIG_KEY_DUE_DATE_DAYS = "due.date.days";
	public static final String CONFIG_KEY_TRIGGER_DAILY = "trigger.daily";
	public static final String CONFIG_KEY_DAILY_HOUR = "daily.hour";
	public static final String CONFIG_KEY_DAILY_MINUTE = "daily.minute";
	public static final String CONFIG_KEY_DAILY_WEEKDAYS = "daily.weekdays";
	public static final String CONFIG_KEY_DURATION_HOURS = "duration.hours";
	public static final String CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS = "invitation.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER1_AFTER_DC_DAYS = "reminder1.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER2_AFTER_DC_DAYS = "reminder2.after.dc.start.days";
	public static final String CONFIG_KEY_ROLES = "participants.roles";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String CONFIG_KEY_WHITE_LIST = "white.list";
	public static final String ROLES_DELIMITER = ",";

	@Autowired
	private CourseProviderDAO providerDao;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private TitleCreator titleCreator;
	@Autowired
	private RepositoryService repositoryService;

	@Override
	public String getType() {
		return "course-provider";
	}

	@Override
	public String getDisplayname(Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseProviderConfigController.class, locale);
		return translator.translate("provider.display.name");
	}

	@Override
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		return new CourseProviderConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate,
			Date toDate, Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseProviderConfigController.class, locale);
		
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations);
		
		int numberDataCollections = 0;
		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		if (CONFIG_KEY_TRIGGER_BEGIN.equals(trigger) || CONFIG_KEY_TRIGGER_END.equals(trigger)) {
			appendForDueDate(searchParams, generator, configs, fromDate, toDate);
			List<RepositoryEntry> courses = loadCourses(generator, searchParams);
			numberDataCollections = courses.size();
		} else if (CONFIG_KEY_TRIGGER_DAILY.equals(trigger)) {
			List<Date> dcStarts = getDailyStarts(configs, fromDate, toDate);
			for (Date dcStart : dcStarts) {
				appendForDaily(searchParams, dcStart);
				List<RepositoryEntry> courses = loadCourses(generator, searchParams);
				numberDataCollections += courses.size();
			}
		}
		
		return translator.translate("generate.info", new String[] { String.valueOf( numberDataCollections )});
	}
	
	@Override
	public boolean hasWhiteListController() {
		return true;
	}

	@Override
	public GeneratorWhiteListController getWhiteListController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return new RepositoryEntryWhiteListController(ureq, wControl, stackPanel, configs);
	}

	@Override
	public void generate(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations);
		
		int numberDataCollections = 0;
		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		if (CONFIG_KEY_TRIGGER_BEGIN.equals(trigger) || CONFIG_KEY_TRIGGER_END.equals(trigger)) {
			numberDataCollections = generateByDueDate(generator, configs, fromDate, toDate, organisations, searchParams);
		} else if (CONFIG_KEY_TRIGGER_DAILY.equals(trigger)) {
			numberDataCollections = generateDaily(generator, configs, fromDate, toDate, organisations, searchParams);
		}
		
		if (numberDataCollections > 0) {
			log.info(numberDataCollections + " data collections created by generator " + numberDataCollections);
		}
	}

	private int generateByDueDate(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate,
			Date toDate, List<Organisation> organisations, SearchParameters searchParams) {
		int numberDataCollections;
		appendForDueDate(searchParams, generator, configs, fromDate, toDate);
		List<RepositoryEntry> courses = loadCourses(generator, searchParams);
		numberDataCollections = courses.size();

		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		for (RepositoryEntry course : courses) {
			Date dcStart = null;
			switch (trigger) {
			case CONFIG_KEY_TRIGGER_BEGIN:
				Date courseBegin = course.getLifecycle().getValidFrom();
				String beginDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
				dcStart = addDays(courseBegin, beginDays);
				break;
			case CONFIG_KEY_TRIGGER_END:
				Date courseEnd = course.getLifecycle().getValidTo();
				String endDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
				dcStart = addDays(courseEnd, endDays);
				break;
			default:
				//
			}
			
			if (dcStart != null) {
				generateDataCollection(generator, configs, organisations, course, dcStart);
			}
		}
		return numberDataCollections;
	}

	private int generateDaily(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate,
			List<Organisation> organisations, SearchParameters searchParams) {
		List<Date> dcStarts = getDailyStarts(configs, fromDate, toDate);
	
		int numberDataCollections = 0;
		for (Date dcStart : dcStarts) {
			appendForDaily(searchParams, dcStart);
			List<RepositoryEntry> courses = loadCourses(generator, searchParams);
			numberDataCollections += courses.size();
			for (RepositoryEntry course : courses) {
				generateDataCollection(generator, configs, organisations, course, dcStart);
			}
		}
		return numberDataCollections;
	}

	private List<Date> getDailyStarts(QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		LocalDate startDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<LocalDate> daysInRange = ProviderHelper.generateDaysInRange(startDate, endDate);
		removeIfUnwantedDayOfWeek(daysInRange, configs);
		List<LocalDateTime> startDateTimes = addStartTime(daysInRange, configs);
		removeIfPlusDurationInPast(startDateTimes, configs);
		removeIfIsInFuture(startDateTimes);
		List<Date> dcStarts = startDateTimes.stream().map(dt -> Date.from(dt.atZone(ZoneId.systemDefault()).toInstant())).collect(Collectors.toList());
		return dcStarts;
	}

	private void removeIfUnwantedDayOfWeek(List<LocalDate> daysInRange, QualityGeneratorConfigs configs) {
		String daysOfWeekConfig = configs.getValue(CONFIG_KEY_DAILY_WEEKDAYS);
		List<DayOfWeek> daysOfWeek = ProviderHelper.splitDaysOfWeek(daysOfWeekConfig);
		daysInRange.removeIf(d -> !daysOfWeek.contains(d.getDayOfWeek()));
	}
	
	private List<LocalDateTime> addStartTime(List<LocalDate> dates, QualityGeneratorConfigs configs) {
		String hourConfig = configs.getValue(CONFIG_KEY_DAILY_HOUR);
		final int hour = ProviderHelper.toIntOrZero(hourConfig);
		String minuteConfig = configs.getValue(CONFIG_KEY_DAILY_MINUTE);
		final int minute = ProviderHelper.toIntOrZero(minuteConfig);
		
		return dates.stream().map(date -> date.atTime(hour, minute)).collect(Collectors.toList());
	}
	
	private void removeIfPlusDurationInPast(List<LocalDateTime> dateTimes, QualityGeneratorConfigs configs) {
		String durationConfig = configs.getValue(CONFIG_KEY_DURATION_HOURS);
		final long duration = ProviderHelper.toLongOrZero(durationConfig);
		
		LocalDateTime now = LocalDateTime.now();
		dateTimes.removeIf(date -> date.plusHours(duration).isBefore(now));
	}

	private void removeIfIsInFuture(List<LocalDateTime> dateTimes) {
		LocalDateTime now = LocalDateTime.now();
		dateTimes.removeIf(date -> date.isAfter(now));
	}

	private void generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<Organisation> organisations, RepositoryEntry course, Date dcStart) {
		// create data collection	
		RepositoryEntry formEntry = generator.getFormEntry();
		Long generatorProviderKey = course.getKey();
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry, generator, generatorProviderKey);

		dataCollection.setStart(dcStart);
		String duration = configs.getValue(CONFIG_KEY_DURATION_HOURS);
		Date deadline = ProviderHelper.addHours(dataCollection.getStart(), duration);
		dataCollection.setDeadline(deadline);
		
		String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
		String title = titleCreator.merge(titleTemplate, Collections.singletonList(course));
		dataCollection.setTitle(title);
		
		dataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
		dataCollection.setTopicRepositoryEntry(course);
		
		dataCollection = qualityService.updateDataCollection(dataCollection);
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.READY);
		
		// add participants
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		for (String roleName: roleNames) {
			GroupRoles role = GroupRoles.valueOf(roleName);
			Collection<Identity> identities = repositoryService.getMembers(course, RepositoryEntryRelationType.all, roleName);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, course, role).build();
			}
		}
		
		// make reminders
		String invitationDay = configs.getValue(CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS);
		if (StringHelper.containsNonWhitespace(invitationDay)) {
			Date invitationDate = addDays(dataCollection.getStart(), invitationDay);
			qualityService.createReminder(dataCollection, invitationDate, QualityReminderType.INVITATION);
		}
		
		String reminder1Day = configs.getValue(CONFIG_KEY_REMINDER1_AFTER_DC_DAYS);
		if (StringHelper.containsNonWhitespace(reminder1Day)) {
			Date reminder1Date = addDays(dataCollection.getStart(), reminder1Day);
			qualityService.createReminder(dataCollection, reminder1Date, QualityReminderType.REMINDER1);
		}

		String reminder2Day = configs.getValue(CONFIG_KEY_REMINDER2_AFTER_DC_DAYS);
		if (StringHelper.containsNonWhitespace(reminder2Day)) {
			Date reminder2Date = addDays(dataCollection.getStart(), reminder2Day);
			qualityService.createReminder(dataCollection, reminder2Date, QualityReminderType.REMINDER2);
		}
	}

	private List<RepositoryEntry> loadCourses(QualityGenerator generator, SearchParameters seachParameters) {
		if(log.isDebug()) log.debug("Generator " + generator + " searches with " + seachParameters);
		
		List<RepositoryEntry> courses = providerDao.loadCourses(seachParameters);
		if(log.isDebug()) log.debug("Generator " + generator + " found " + courses.size() + " entries");
		return courses;
	}
	
	private SearchParameters getSeachParameters(QualityGenerator generator, QualityGeneratorConfigs configs,
			Collection<? extends OrganisationRef> organisations) {
		SearchParameters searchParams = new SearchParameters();
		searchParams.setGeneratorRef(generator);
		searchParams.setOrganisationRefs(organisations);
		List<RepositoryEntryRef> repositoryEntryRefs = RepositoryEntryWhiteListController.getRepositoryEntryRefs(configs);
		searchParams.setRepositoryEntryRefs(repositoryEntryRefs);
		return searchParams;
	}
	
	private void appendForDueDate(SearchParameters searchParams, QualityGenerator generator,
			QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		switch (trigger) {
		case CONFIG_KEY_TRIGGER_BEGIN:
			String beginDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
			Date beginFrom = addDays(fromDate, beginDays);
			Date beginTo = addDays(toDate, beginDays);
			searchParams.setBeginFrom(beginFrom);
			searchParams.setBeginTo(beginTo);
			break;
		case CONFIG_KEY_TRIGGER_END:
			String endDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
			Date endFrom = addDays(fromDate, endDays);
			Date endTo = addDays(toDate, endDays);
			searchParams.setEndFrom(endFrom);
			searchParams.setEndTo(endTo);
			break;
		default:
			// Do not load anything
			searchParams.setEndFrom(new Date());
			searchParams.setEndTo(addDays(searchParams.getEndFrom(), "-1"));
			log.warn("Quality data collection generator is not properly configured: " + generator);
		}
	}

	private void appendForDaily(SearchParameters searchParams, Date dcStart) {
		searchParams.setGeneratorDataCollectionStart(dcStart);
		searchParams.setLifecycleValidAt(dcStart);
	}

}
