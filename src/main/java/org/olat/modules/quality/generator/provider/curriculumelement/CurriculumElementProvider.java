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
package org.olat.modules.quality.generator.provider.curriculumelement;

import static java.util.Collections.singletonList;
import static org.olat.modules.quality.generator.ProviderHelper.addDays;
import static org.olat.modules.quality.generator.ProviderHelper.subtractDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
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
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.model.QualityPreviewImpl;
import org.olat.modules.quality.generator.provider.curriculumelement.manager.CurriculumElementProviderDAO;
import org.olat.modules.quality.generator.provider.curriculumelement.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.curriculumelement.ui.CurriculumElementProviderConfigController;
import org.olat.modules.quality.generator.ui.CurriculumElementBlackListController;
import org.olat.modules.quality.generator.ui.CurriculumElementWhiteListController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementProvider implements QualityGeneratorProvider {

	private static final Logger log = Tracing.createLoggerFor(CurriculumElementProvider.class);
	
	public static final String CONFIG_KEY_CURRICULUM_ELEMENT_TYPE = "curriculum.element.type";
	public static final String CONFIG_KEY_DUE_DATE_BEGIN = "config.due.date.begin";
	public static final String CONFIG_KEY_DUE_DATE_DAYS = "due.date.days";
	public static final String CONFIG_KEY_DUE_DATE_END = "config.due.date.end";
	public static final String CONFIG_KEY_DUE_DATE_TYPE = "due.date.type";
	public static final String CONFIG_KEY_DURATION_DAYS = "duration.days";
	public static final String CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS = "invitation.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER1_AFTER_DC_DAYS = "reminder1.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER2_AFTER_DC_DAYS = "reminder2.after.dc.start.days";
	public static final String CONFIG_KEY_ROLES = "participants.roles";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String ROLES_DELIMITER = ",";

	@Autowired
	private CurriculumElementProviderDAO providerDao;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private TitleCreator titleCreator;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Override
	public String getType() {
		return "curriculum-element-provider";
	}

	@Override
	public String getDisplayname(Locale locale) {
		Translator translator = Util.createPackageTranslator(CurriculumElementProviderConfigController.class, locale);
		return translator.translate("provider.display.name");
	}

	@Override
	public QualityDataCollectionTopicType getGeneratedTopicType(QualityGeneratorConfigs configs) {
		return QualityDataCollectionTopicType.CURRICULUM_ELEMENT;
	}

	@Override
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		return new CurriculumElementProviderConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverrides overrides,
			Date fromDate, Date toDate, Locale locale) {
		Translator translator = Util.createPackageTranslator(CurriculumElementProviderConfigController.class, locale);
		
		EnableInfoStrategy strategy = new EnableInfoStrategy();
		provide(generator, configs, overrides, strategy, fromDate, toDate, null, true);
		
		return translator.translate("generate.info", new String[] { String.valueOf( strategy.getCount() )});
	}
	
	private class EnableInfoStrategy implements CurriculumElementProviderStrategy {
		
		private int count = 0;
		
		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverride override,
				Date generatedStart, CurriculumElement curriculumElement) {
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
			QualityGeneratorOverrides overrides, Date fromDate, Date toDate) {
		DataCollectionStrategy strategy = new DataCollectionStrategy();
		provide(generator, configs, overrides, strategy, fromDate, toDate, null, true);
		return strategy.getDataCollections();
	}
	
	private void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, CurriculumElementProviderStrategy strategy, Date fromDate, Date toDate,
			List<Long> curriculumElementKeys, boolean excludeBlacklisted) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = createSearchParams(generator, configs, organisations, fromDate, toDate,
				curriculumElementKeys, excludeBlacklisted);
		List<CurriculumElement> elements = loadCurriculumElements(generator, searchParams);
		
		List<QualityGeneratorOverride> manualStartInRangeOverrides = overrides.getOverrides(generator, fromDate, toDate);
		
		List<CurriculumElement> elementsOutOfRange = new ArrayList<>(1);
		for (CurriculumElement element : elements) {
			String identifier = getIdentifier(generator, element);
			QualityGeneratorOverride startOverride = overrides.getOverride(identifier);
			if (startOverride != null) {
				if (startOverride.getStart() != null) {
					Date dcEnd = getDataCollectionEnd(configs, startOverride.getStart());
					if (startOverride.getStart().after(toDate) || dcEnd.before(fromDate)) {
						elementsOutOfRange.add(element);
					}
				}
				manualStartInRangeOverrides.remove(startOverride);
			}
		}
		elements.removeAll(elementsOutOfRange);
		
		if (!manualStartInRangeOverrides.isEmpty()) {
			List<Long> manualCurriculumElementKeys = manualStartInRangeOverrides.stream()
					.map(override -> override.getIdentifier().split("::"))
					.filter(identifierParts -> identifierParts.length == 2)
					.map(identifierParts -> identifierParts[1])
					.map(Long::valueOf)
					.collect(Collectors.toList());
			if (searchParams.getWhiteListKeys() != null) {
				manualCurriculumElementKeys.retainAll(searchParams.getWhiteListKeys());
			}
			
			if (!manualCurriculumElementKeys.isEmpty()) {
				SearchParameters manualsSearchParams = createSearchParams(generator, configs, organisations, null, null,
						manualCurriculumElementKeys, excludeBlacklisted);
				List<CurriculumElement> elementsInRange = loadCurriculumElements(generator, manualsSearchParams);
				elements.addAll(elementsInRange);
			}
		}
		
		for (CurriculumElement element : elements) {
			String identifier = getIdentifier(generator, element);
			QualityGeneratorOverride override = overrides.getOverride(identifier);
			Date generatedStart = getDataCollectionStartDate(element, configs);
			strategy.provide(generator, configs, override, generatedStart, element);
		}
	}
	
	private class DataCollectionStrategy implements CurriculumElementProviderStrategy {
		
		private final List<QualityDataCollection> dataCollections = new ArrayList<>();

		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
				QualityGeneratorOverride override, Date generatedStart, CurriculumElement curriculumElement) {
			QualityDataCollection dataCollection = generateDataCollection(generator, configs, override, generatedStart, curriculumElement);
			dataCollections.add(dataCollection);
		}
		
		private List<QualityDataCollection> getDataCollections() {
			return dataCollections;
		}
		
	}

	QualityDataCollection generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverride override, Date generatedStart, CurriculumElement curriculumElement) {
		RepositoryEntry formEntry = generator.getFormEntry();
		Long generatorProviderKey = curriculumElement.getKey();
		Organisation curriculumOrganisation = curriculumElement.getCurriculum().getOrganisation();
		QualityDataCollection dataCollection = qualityService.createDataCollection(
				Collections.singletonList(curriculumOrganisation), formEntry, generator, generatorProviderKey);

		Date dcStart = override != null? override.getStart(): generatedStart;
		dataCollection.setStart(dcStart);
		
		Date deadline = getDataCollectionEnd(configs, dcStart);
		dataCollection.setDeadline(deadline);
		
		String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
		String title = titleCreator.merge(titleTemplate, singletonList(curriculumElement));
		dataCollection.setTitle(title);
		
		dataCollection.setTopicType(QualityDataCollectionTopicType.CURRICULUM_ELEMENT);
		dataCollection.setTopicCurriculumElement(curriculumElement);
		
		dataCollection = qualityService.updateDataCollection(dataCollection);
		dataCollection = qualityService.updateDataCollectionStatus(dataCollection, QualityDataCollectionStatus.READY);
		
		// add participants
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		for (String roleName: roleNames) {
			CurriculumRoles role = CurriculumRoles.valueOf(roleName);
			List<Identity> identities = curriculumService.getMembersIdentity(curriculumElement, role);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, curriculumElement, role).build();
			}
		}
		
		// make reminders
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

	private Date getDataCollectionStartDate(CurriculumElement curriculumElement, QualityGeneratorConfigs configs) {
		String dueDateType = configs.getValue(CONFIG_KEY_DUE_DATE_TYPE);
		String dueDateDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
		Date dcStart = CONFIG_KEY_DUE_DATE_BEGIN.equals(dueDateType)
				? curriculumElement.getBeginDate()
				: curriculumElement.getEndDate();
		dcStart = addDays(dcStart, dueDateDays);
		return dcStart;
	}

	private Date getDataCollectionEnd(QualityGeneratorConfigs configs, Date startDate) {
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		Date deadline = addDays(startDate, duration);
		return deadline;
	}

	private List<CurriculumElement> loadCurriculumElements(QualityGenerator generator, SearchParameters searchParams) {
		if(log.isDebugEnabled()) log.debug("Generator " + generator + " searches with " + searchParams);
		
		List<CurriculumElement> elements = providerDao.loadPending(searchParams);
		
		if(log.isDebugEnabled()) log.debug("Generator " + generator + " found " + elements.size() + " curriculum elements");
		return elements;
	}
	
	private SearchParameters createSearchParams(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<Organisation> organisations, Date fromDate, Date toDate, List<Long> curriculumElementKeys, boolean excludeBlacklisted) {
		SearchParameters searchParams = new SearchParameters();
		searchParams.setGeneratorRef(generator);
		searchParams.setOrganisationRefs(organisations);
		
		String ceTypeKeyString = configs.getValue(CONFIG_KEY_CURRICULUM_ELEMENT_TYPE);
		if (StringHelper.containsNonWhitespace(ceTypeKeyString)) {
			Long ceTypeKey = Long.valueOf(ceTypeKeyString);
			searchParams.setCeTypeKey(ceTypeKey);
		}
		
		String dueDateDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
		if (fromDate != null) {
			Date dueDateFrom = subtractDays(fromDate, dueDateDays);
			searchParams.setFrom(dueDateFrom);
		}
		if (toDate != null) {
			Date dueDateTo = subtractDays(toDate, dueDateDays);
			searchParams.setTo(dueDateTo);
		}
		
		String dueDateType = configs.getValue(CONFIG_KEY_DUE_DATE_TYPE);
		if (CONFIG_KEY_DUE_DATE_BEGIN.equals(dueDateType)) {
			searchParams.setStartDate(true);
		} else if (CONFIG_KEY_DUE_DATE_END.equals(dueDateType)) {
			searchParams.setStartDate(false);
		}
		
		if (curriculumElementKeys != null) {
			searchParams.setWhiteListKeys(curriculumElementKeys);
		} else {
			List<CurriculumElementRef> whiteListRefs = CurriculumElementWhiteListController.getCurriculumElementRefs(configs);
			searchParams.setWhiteListRefs(whiteListRefs);
		}
		if (excludeBlacklisted) {
			List<CurriculumElementRef> blackListRefs = CurriculumElementBlackListController.getCurriculumElementRefs(configs);
			searchParams.setBlackListRefs(blackListRefs);
		}
		
		return searchParams;
	}

	@Override
	public List<QualityPreview> getPreviews(QualityGenerator generator, QualityGeneratorConfigs configs,
			QualityGeneratorOverrides overrides, GeneratorPreviewSearchParams searchParams) {
		List<Long> curriculumElementKeys = null;
		if (searchParams.getCurriculumElementKeys() != null) {
			curriculumElementKeys = new ArrayList<>(searchParams.getCurriculumElementKeys());
			List<CurriculumElementRef> whiteListRefs = CurriculumElementWhiteListController.getCurriculumElementRefs(configs);
			if (whiteListRefs != null && !whiteListRefs.isEmpty()) {
				curriculumElementKeys.retainAll(whiteListRefs.stream().map(CurriculumElementRef::getKey).toList());
			}
			if (curriculumElementKeys.isEmpty()) {
				return List.of();
			}
		}
		
		Set<Long> blackListKeys = CurriculumElementBlackListController.getCurriculumElementRefs(configs).stream()
				.map(CurriculumElementRef::getKey)
				.collect(Collectors.toSet());
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		PreviewStrategy strategy = new PreviewStrategy(roleNames, blackListKeys);
		provide(generator, configs, overrides, strategy, searchParams.getDateRange().getFrom(),
				searchParams.getDateRange().getTo(), curriculumElementKeys, false);
		
		return strategy.getPreviews();
	}
	
	private class PreviewStrategy implements CurriculumElementProviderStrategy {
		
		private final String[] roleNames;
		private final Set<Long> blackListKeys;
		private final Map<Long, List<? extends IdentityRef>> repoKeyToParticipants = new HashMap<>();
		private final List<QualityPreview> previews = new ArrayList<>();
		
		public PreviewStrategy(String[] roleNames, Set<Long> blackListKeys) {
			this.roleNames = roleNames;
			this.blackListKeys = blackListKeys;
		}
		
		@Override
		public void provide(QualityGenerator generator, QualityGeneratorConfigs configs,
				QualityGeneratorOverride override, Date generatedStart, CurriculumElement curriculumElement) {
			QualityPreviewImpl preview = new QualityPreviewImpl();
			preview.setGenerator(generator);
			preview.setGeneratorProviderKey(curriculumElement.getKey());
			preview.setIdentifier(getIdentifier(generator, curriculumElement));
			preview.setFormEntry(generator.getFormEntry());
			
			Organisation curriculumOrganisation = curriculumElement.getCurriculum().getOrganisation();
			preview.setOrganisations(List.of(curriculumOrganisation));
			
			Date dcStart = override != null? override.getStart(): generatedStart;
			preview.setStart(dcStart);
			
			Date deadline = getDataCollectionEnd(configs, dcStart);
			preview.setDeadline(deadline);
			
			String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
			String title = titleCreator.merge(titleTemplate, singletonList(curriculumElement));
			preview.setTitle(title);
			
			preview.setTopicType(QualityDataCollectionTopicType.CURRICULUM_ELEMENT);
			preview.setTopicCurriculumElement(curriculumElement);
			
			List<? extends IdentityRef> participants = repoKeyToParticipants.computeIfAbsent(curriculumElement.getKey(),
						key -> curriculumService.getMemberKeys(List.of(curriculumElement), roleNames).stream().map(IdentityRefImpl::new).toList());
			preview.setParticipants(participants);
			
			if (blackListKeys.contains(curriculumElement.getKey())) {
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
	}

	String getIdentifier(QualityGeneratorRef generator, CurriculumElementRef curriculumElement) {
		return generator.getKey() + "::" + curriculumElement.getKey();
	}

	@Override
	public void addToBlacklist(QualityGeneratorConfigs configs, QualityPreview preview) {
		CurriculumElementBlackListController.addCurriculumElementRef(configs, () -> preview.getGeneratorProviderKey());
	}

	@Override
	public void removeFromBlacklist(QualityGeneratorConfigs configs, QualityPreview preview) {
		CurriculumElementBlackListController.removeCurriculumElementRef(configs, () -> preview.getGeneratorProviderKey());
	}
	
	static interface CurriculumElementProviderStrategy {
		
		void provide(QualityGenerator generator, QualityGeneratorConfigs configs, QualityGeneratorOverride override,
				Date generatedStart, CurriculumElement curriculumElement);
		
	}

}
