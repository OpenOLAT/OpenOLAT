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
import static org.olat.modules.quality.generator.ProviderHelper.subtractDays;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
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
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionSearchParams;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.GeneratorPreviewSearchParams;
import org.olat.modules.quality.generator.ProviderHelper;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.QualityGeneratorOverrides;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.model.QualityPreviewImpl;
import org.olat.modules.quality.generator.provider.course.manager.CourseProviderDAO;
import org.olat.modules.quality.generator.provider.course.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.course.ui.CourseProviderConfigController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.generator.ui.RepositoryEntryBlackListController;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
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

	private static final Logger log = Tracing.createLoggerFor(CourseProvider.class);
	
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
	public static final String ROLES_DELIMITER = ",";
	public static final String CONFIG_KEY_EDUCATIONAL_TYPE_EXCLUSION = "educational.type.exclusion";
	public static final String EDUCATIONAL_TYPE_EXCLUSION_DELIMITER = ",";
	
	private static final DateFormat dailyIdentitfierDateFormat = new SimpleDateFormat("yyyyMMdd");

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
	public QualityDataCollectionTopicType getGeneratedTopicType(QualityGeneratorConfigs configs) {
		return QualityDataCollectionTopicType.REPOSITORY;
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverrides overrides,
			Date fromDate, Date toDate, Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseProviderConfigController.class, locale);
		
		EnableInfoStrategy strategy = new EnableInfoStrategy();
		provide(generator, configs, overrides, strategy, fromDate, toDate, true, true);
		
		return translator.translate("generate.info", new String[] { String.valueOf( strategy.getCoursesCount() )});
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
		provide(generator, configs, overrides, strategy, fromDate, toDate, true, true);
		return strategy.getDataCollections();
	}

	private void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, CourseProviderStrategy strategy, Date fromDate, Date toDate,
			boolean excludeFutureBegins, boolean excludeBlacklisted) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = getSeachParameters(configs, organisations, excludeFutureBegins, null, excludeBlacklisted);
		
		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		if (CONFIG_KEY_TRIGGER_BEGIN.equals(trigger) || CONFIG_KEY_TRIGGER_END.equals(trigger)) {
			provideByDueDate(generator, configs, overrides, fromDate, toDate, searchParams, strategy);
		} else if (CONFIG_KEY_TRIGGER_DAILY.equals(trigger)) {
			provideDaily(generator, configs, fromDate, toDate, searchParams, overrides, strategy);
		}
	}
	
	private void provideByDueDate(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, Date fromDate, Date toDate, SearchParameters searchParams,
			CourseProviderStrategy strategy) {
		appendForDueDate(searchParams, generator, configs, fromDate, toDate);
		List<RepositoryEntry> courseEntries = loadCourses(generator, searchParams);
		courseEntries.removeIf(new PlusDurationIsBefore(configs, fromDate));
		
		List<QualityGeneratorOverride> manualStartInRangeOverrides = overrides.getOverrides(generator, fromDate, toDate);
		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		for (RepositoryEntry courseEntry : courseEntries) {
			QualityGeneratorOverride override = null;
			Date generatedDcStart = null;
			boolean provide = false;
			
			String identifier = getDueDateIdentifier(generator, courseEntry);
			QualityGeneratorOverride startOverride = overrides.getOverride(identifier);
			if (startOverride != null) {
				if (startOverride.getStart() != null) {
					int durationHours = ProviderHelper.toIntOrZero(configs.getValue(CourseProvider.CONFIG_KEY_DURATION_HOURS));
					Date dcEnd = ProviderHelper.addHours(startOverride.getStart(), durationHours);
					if (startOverride.getStart().before(toDate) && dcEnd.after(fromDate)) {
						override = startOverride;
						manualStartInRangeOverrides.remove(override);
						provide = true;
					}
				}
			} else if (CONFIG_KEY_TRIGGER_BEGIN.equals(trigger)) {
				Date courseBegin = courseEntry.getLifecycle().getValidFrom();
				String beginDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
				generatedDcStart = addDays(courseBegin, beginDays);
				provide = true;
			} else if (CONFIG_KEY_TRIGGER_END.equals(trigger)) {
				Date courseEnd = courseEntry.getLifecycle().getValidTo();
				String endDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
				generatedDcStart = addDays(courseEnd, endDays);
				provide = true;
			}
			
			if (provide) {
				strategy.provide(generator, configs, override, generatedDcStart, courseEntry);
			}
		}
		
		provideDueDateOverrideToRange(generator, configs, overrides, searchParams.getOrganisationRefs(), strategy, manualStartInRangeOverrides);
	}

	private void provideDueDateOverrideToRange(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, List<? extends OrganisationRef> oraganisations,
			CourseProviderStrategy strategy, List<QualityGeneratorOverride> manualStartInRangeOverrides) {
		if (manualStartInRangeOverrides.isEmpty()) {
			return;
		}
		
		List<Long> repoEntryKeys = manualStartInRangeOverrides.stream().map(QualityGeneratorOverride::getGeneratorProviderKey).distinct().toList();
		SearchParameters seachParameters = getSeachParameters(configs, oraganisations, false, repoEntryKeys, true);
		List<RepositoryEntry> courseEntries = loadCourses(generator, seachParameters);
		
		for (RepositoryEntry courseEntry : courseEntries) {
			QualityGeneratorOverride startOverride = overrides.getOverride(getDueDateIdentifier(generator, courseEntry));
			if (startOverride != null && startOverride.getStart() != null) {
				Date dcStart = startOverride.getStart();
				strategy.provide(generator, configs, startOverride, dcStart, courseEntry);
			}
		}
	}
	
	String getDueDateIdentifier(QualityGeneratorRef generator, RepositoryEntryRef courseEntry) {
		return generator.getKey() + "::" + courseEntry.getKey();
	}
	
	private void provideDaily(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate,
			SearchParameters searchParams, QualityGeneratorOverrides overrides, CourseProviderStrategy strategy) {
		List<Date> dcStarts = getDailyStarts(configs, fromDate, toDate, searchParams);
		if (dcStarts.isEmpty()) {
			return;
		}
		
		List<RepositoryEntry> courseEntries = loadCourses(generator, searchParams);
		
		QualityDataCollectionSearchParams dcSearchParams = new QualityDataCollectionSearchParams();
		dcSearchParams.setGeneratorRef(generator);
		dcSearchParams.setStartDateAfter(DateUtils.getStartOfDay(dcStarts.get(0)));
		dcSearchParams.setGeneratorOverrideAvailable(Boolean.FALSE);
		List<QualityDataCollection> dataCollections = qualityService.loadDataCollections(dcSearchParams);
		
		List<QualityGeneratorOverride> manualStartInRangeOverrides = overrides.getOverrides(generator, fromDate, toDate);
		
		for (Date dcStart : dcStarts) {
			for (RepositoryEntry courseEntry: courseEntries) {
				if (dataCollectionNotGenerated(courseEntry, dcStart, dataCollections)) {
					QualityGeneratorOverride override = null;
					boolean provide = false;
					
					String identifier = getDailyIdentifier(generator, courseEntry, dcStart);
					QualityGeneratorOverride startOverride = overrides.getOverride(identifier);
					if (startOverride != null) {
						if (startOverride.getStart() != null) {
							int durationHours = ProviderHelper.toIntOrZero(configs.getValue(CourseProvider.CONFIG_KEY_DURATION_HOURS));
							Date dcEnd = ProviderHelper.addHours(startOverride.getStart(), durationHours);
							if (startOverride.getStart().before(toDate) && dcEnd.after(fromDate)) {
								provide = true;
								override = startOverride;
								manualStartInRangeOverrides.remove(override);
							}
						}
					} else {
						if (lifecycleValidAt(courseEntry, dcStart)) {
							provide = true;
						}
					}
					
					if (provide) {
						strategy.provide(generator, configs, override, dcStart, courseEntry);
					}
				}
			}
		}
		
		provideDailyOverrideToRange(generator, configs, searchParams.getOrganisationRefs(), strategy, manualStartInRangeOverrides);
	}

	private void provideDailyOverrideToRange(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<? extends OrganisationRef> oraganisations, CourseProviderStrategy strategy,
			List<QualityGeneratorOverride> manualStartInRangeOverrides) {
		if (manualStartInRangeOverrides.isEmpty()) {
			return;
		}
		
		List<Long> repoEntryKeys = manualStartInRangeOverrides.stream().map(QualityGeneratorOverride::getGeneratorProviderKey).distinct().toList();
		SearchParameters seachParameters = getSeachParameters(configs, oraganisations, false, repoEntryKeys, true);
		List<RepositoryEntry> courseEntries = loadCourses(generator, seachParameters);
		
		for (QualityGeneratorOverride override : manualStartInRangeOverrides) {
			for (RepositoryEntry courseEntry : courseEntries) {
				if (override.getGeneratorProviderKey().equals(courseEntry.getKey())) {
					String[] identParts = override.getIdentifier().split("::");
					if (identParts.length == 3) {
						try {
							Date date = dailyIdentitfierDateFormat.parse(identParts[2]);
							strategy.provide(generator, configs, override, date, courseEntry);
						} catch (ParseException e) {
							log.warn("Generator {}", generator.getKey(), e);
						}
					}
				}
			}
		}
	}

	String getDailyIdentifier(QualityGenerator generator, RepositoryEntry courseEntry, Date dcStart) {
		return generator.getKey() + "::" + courseEntry.getKey() + "::" + dailyIdentitfierDateFormat.format(dcStart);
	}

	private List<Date> getDailyStarts(QualityGeneratorConfigs configs, Date fromDate, Date toDate, SearchParameters searchParams) {
		LocalDate startDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<LocalDate> daysInRange = ProviderHelper.generateDaysInRange(startDate, endDate);
		removeIfUnwantedDayOfWeek(daysInRange, configs);
		List<LocalDateTime> startDateTimes = addStartTime(daysInRange, configs);
		removeIfPlusDurationInPast(startDateTimes, configs);
		if (searchParams.isExcludeFutureBegins()) {
			removeIfIsInFuture(startDateTimes);
		}
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
	
	private boolean lifecycleValidAt(RepositoryEntry courseEntry, Date dcStart) {
		RepositoryEntryLifecycle lifecycle = courseEntry.getLifecycle();
		if (lifecycle == null) {
			return true;
		}
		if (lifecycle.getValidFrom() != null && DateUtils.getStartOfDay(lifecycle.getValidFrom()).after(DateUtils.getStartOfDay(dcStart))) {
			return false;
		}
		if (lifecycle.getValidTo() != null && DateUtils.getStartOfDay(lifecycle.getValidTo()).before(DateUtils.getStartOfDay(dcStart))) {
			return false;
		}
		return true;
	}

	private boolean dataCollectionNotGenerated(RepositoryEntry courseEntry, Date dcStart, List<QualityDataCollection> dataCollections) {
		return !dataCollections.stream()
				.filter(dc -> dc.getGeneratorProviderKey().equals(courseEntry.getKey()))
				.filter(dc -> DateUtils.isSameDay(dc.getStart(), dcStart))
				.findFirst()
				.isPresent();
	}

	public QualityDataCollection generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverride override, RepositoryEntry course, Date generatedStart) {
		RepositoryEntry formEntry = generator.getFormEntry();
		Long generatorProviderKey = course.getKey();
		Collection<Organisation> courseOrganisations = repositoryService.getOrganisations(course);
		QualityDataCollection dataCollection = qualityService.createDataCollection(courseOrganisations, formEntry,
				generator, generatorProviderKey);
		if (override != null) {
			override.setDataCollection(dataCollection);
			generatorService.updateOverride(override);
		}
		
		Date start = override != null? override.getStart(): generatedStart;
		dataCollection.setStart(start);
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
		
		return dataCollection;
	}

	private List<RepositoryEntry> loadCourses(QualityGenerator generator, SearchParameters seachParameters) {
		if(log.isDebugEnabled()) log.debug("Generator " + generator + " searches with " + seachParameters);
		
		List<RepositoryEntry> courses = providerDao.loadCourses(seachParameters);
		if(log.isDebugEnabled()) log.debug("Generator " + generator + " found " + courses.size() + " entries");
		return courses;
	}
	
	private SearchParameters getSeachParameters(QualityGeneratorConfigs configs, List<? extends OrganisationRef> organisations,
			boolean excludeFutureBegins, List<Long> courseEntryKeys, boolean excludeBlacklisted) {
		SearchParameters searchParams = new SearchParameters();
		searchParams.setOrganisationRefs(organisations);
		searchParams.setExcludeFutureBegins(excludeFutureBegins);
		if (courseEntryKeys != null) {
			searchParams.setWhiteListKeys(courseEntryKeys);
		} else {
			List<RepositoryEntryRef> whiteListRefs = RepositoryEntryWhiteListController.getRepositoryEntryRefs(configs);
			searchParams.setWhiteListRefs(whiteListRefs);
		}
		if (excludeBlacklisted) {
			List<RepositoryEntryRef> blackListRefs = RepositoryEntryBlackListController.getRepositoryEntryRefs(configs);
			searchParams.setBlackListRefs(blackListRefs);
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
	
	private void appendForDueDate(SearchParameters searchParams, QualityGenerator generator,
			QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		String trigger = configs.getValue(CONFIG_KEY_TRIGGER);
		String duration = configs.getValue(CONFIG_KEY_DURATION_HOURS);
		switch (trigger) {
		case CONFIG_KEY_TRIGGER_BEGIN:
			searchParams.setGeneratorRef(generator);
			String beginDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
			Date beginFrom = subtractDays(fromDate, beginDays);
			beginFrom = ProviderHelper.subtractHours(beginFrom, duration);
			Date beginTo = subtractDays(toDate, beginDays);
			searchParams.setBeginFrom(beginFrom);
			searchParams.setBeginTo(beginTo);
			break;
		case CONFIG_KEY_TRIGGER_END:
			searchParams.setGeneratorRef(generator);
			String endDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
			Date endFrom = subtractDays(fromDate, endDays);
			endFrom = ProviderHelper.subtractHours(endFrom, duration);
			Date endTo = subtractDays(toDate, endDays);
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

	@Override
	public List<QualityPreview> getPreviews(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, GeneratorPreviewSearchParams previewSearchParams) {
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		List<RepositoryEntryRef> blackListRefs = RepositoryEntryBlackListController.getRepositoryEntryRefs(configs);
		PreviewStrategy strategy = new PreviewStrategy(roleNames, blackListRefs);
		provide(generator, configs, overrides, strategy, previewSearchParams.getDateRange().getFrom(),
				previewSearchParams.getDateRange().getTo(), false, false);
		
		return strategy.getPreviews();
	}
	
	static interface CourseProviderStrategy {
		
		void provide(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverride override,
				Date generatedStart, RepositoryEntry courseEntry);
		
	}
	
	private class EnableInfoStrategy implements CourseProviderStrategy {
		
		private int coursesCount = 0;
		
		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
				QualityGeneratorOverride override, Date generatedStart, RepositoryEntry courseEntry) {
			coursesCount++;
		}
		
		public int getCoursesCount() {
			return coursesCount;
		}
		
	}
	
	private class DataCollectionStrategy implements CourseProviderStrategy {
		
		private final List<QualityDataCollection> dataCollections = new ArrayList<>();

		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
				QualityGeneratorOverride override, Date generatedStart, RepositoryEntry courseEntry) {
			QualityDataCollection dataCollection = generateDataCollection(generator, configs, override, courseEntry, generatedStart);
			dataCollections.add(dataCollection);
		}
		
		private List<QualityDataCollection> getDataCollections() {
			return dataCollections;
		}
		
	}
	
	private class PreviewStrategy implements CourseProviderStrategy {
		
		private final String[] roleNames;
		private final Set<Long> blackListKeys;
		private final Map<Long, List<Organisation>> repoKeyToOrganisations = new HashMap<>();
		private final Map<Long, List<Identity>> repoKeyToParticipants = new HashMap<>();
		private final List<QualityPreview> previews = new ArrayList<>();

		public PreviewStrategy(String[] roleNames, List<RepositoryEntryRef> blackListRefs) {
			this.roleNames = roleNames;
			this.blackListKeys = blackListRefs.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toSet());
		}
		
		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
				QualityGeneratorOverride override, Date generatedStart, RepositoryEntry courseEntry) {
			QualityPreviewImpl preview = new QualityPreviewImpl();
			preview.setGenerator(generator);
			preview.setGeneratorProviderKey(courseEntry.getKey());
			preview.setFormEntry(generator.getFormEntry());
			if (CONFIG_KEY_TRIGGER_DAILY.equals(configs.getValue(CONFIG_KEY_TRIGGER))) {
				preview.setIdentifier(getDailyIdentifier(generator, courseEntry, generatedStart));
			} else {
				preview.setIdentifier(getDueDateIdentifier(generator, courseEntry));
			}
			
			List<Organisation> courseOrganisations = getOrganisations(courseEntry);
			preview.setOrganisations(courseOrganisations);
			
			Date start = override != null? override.getStart(): generatedStart;
			preview.setStart(start);
			String duration = configs.getValue(CONFIG_KEY_DURATION_HOURS);
			Date deadline = ProviderHelper.addHours(preview.getStart(), duration);
			preview.setDeadline(deadline);
			
			String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
			String title = titleCreator.merge(titleTemplate, Collections.singletonList(courseEntry));
			preview.setTitle(title);
			
			preview.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
			preview.setTopicRepositoryEntry(courseEntry);
			
			List<Identity> participants = getParticipants(courseEntry);
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
		
		public List<QualityPreview> getPreviews() {
			return previews;
		}
		
		private List<Organisation> getOrganisations(RepositoryEntryRef courseEntry) {
			return repoKeyToOrganisations.computeIfAbsent(courseEntry.getKey(),
					key -> repositoryService.getOrganisations(courseEntry));
		}
		
		private List<Identity> getParticipants(RepositoryEntryRef courseEntry) {
			return repoKeyToParticipants.computeIfAbsent(courseEntry.getKey(),
					key -> repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, roleNames));
		}
		
	}

	@Override
	public void addToBlacklist(QualityGeneratorConfigs configs, QualityPreview preview) {
		RepositoryEntryBlackListController.addRepositoryEntryRef(configs, () -> preview.getGeneratorProviderKey());
	}

	@Override
	public void removeFromBlacklist(QualityGeneratorConfigs configs, QualityPreview preview) {
		RepositoryEntryBlackListController.removeRepositoryEntryRef(configs, () -> preview.getGeneratorProviderKey());
	}

}
