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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.OLog;
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
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.curriculumelement.manager.CurriculumElementProviderDAO;
import org.olat.modules.quality.generator.provider.curriculumelement.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.curriculumelement.ui.CurriculumElementProviderConfigController;
import org.olat.modules.quality.generator.ui.AbstractGeneratorEditController;
import org.olat.modules.quality.generator.ui.CurriculumElementWhiteListController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
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

	private static final OLog log = Tracing.createLoggerFor(CurriculumElementProvider.class);
	
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
	public static final String CONFIG_KEY_WHITE_LIST = "white.list";
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
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		return new CurriculumElementProviderConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getEnableInfo(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate,
			Date toDate, Locale locale) {
		Translator translator = Util.createPackageTranslator(CurriculumElementProviderConfigController.class, locale);
		
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		String ceTypeKeyString = configs.getValue(CONFIG_KEY_CURRICULUM_ELEMENT_TYPE);
		Long ceTypeKey = Long.valueOf(ceTypeKeyString);
		SearchParameters searchParams = new SearchParameters(generator, organisations, ceTypeKey, fromDate, toDate);
		List<CurriculumElementRef> curriculumElementRefs = CurriculumElementWhiteListController.getCurriculumElementRefs(configs);
		searchParams.setCurriculumElementRefs(curriculumElementRefs);
		
		Long count = 0l;
		String dueDateType = configs.getValue(CONFIG_KEY_DUE_DATE_TYPE);
		if (CONFIG_KEY_DUE_DATE_BEGIN.equals(dueDateType)) {
			searchParams.setStartDate(true);
			count = providerDao.loadPendingCount(searchParams);
		} else if (CONFIG_KEY_DUE_DATE_END.equals(dueDateType)) {
			searchParams.setStartDate(false);
			count = providerDao.loadPendingCount(searchParams);
		}
		
		return translator.translate("generate.info", new String[] { String.valueOf(count)});
	}
	
	@Override
	public boolean hasWhiteListController() {
		return true;
	}

	@Override
	public AbstractGeneratorEditController getWhiteListController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		return new CurriculumElementWhiteListController(ureq, wControl, secCallback, stackPanel, generator, configs);
	}

	@Override
	public void generate(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		int numCreated = 0;
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		String ceTypeKeyString = configs.getValue(CONFIG_KEY_CURRICULUM_ELEMENT_TYPE);
		Long ceTypeKey = Long.valueOf(ceTypeKeyString);
		SearchParameters searchParams = new SearchParameters(generator, organisations, ceTypeKey, fromDate, toDate);
		List<CurriculumElementRef> curriculumElementRefs = CurriculumElementWhiteListController.getCurriculumElementRefs(configs);
		searchParams.setCurriculumElementRefs(curriculumElementRefs);
		
		String dueDateType = configs.getValue(CONFIG_KEY_DUE_DATE_TYPE);
		String dueDateDays = configs.getValue(CONFIG_KEY_DUE_DATE_DAYS);
		if (CONFIG_KEY_DUE_DATE_BEGIN.equals(dueDateType)) {
			searchParams.setStartDate(true);
			List<CurriculumElement> curriculumElementsFormStart = providerDao.loadPending(searchParams);
			for (CurriculumElement curriculumElement: curriculumElementsFormStart) {
				Date dcStart = addDays(curriculumElement.getBeginDate(), dueDateDays);
				generateDataCollection(generator, configs, organisations, curriculumElement, dcStart);
			}
			numCreated += curriculumElementsFormStart.size();
		} else if (CONFIG_KEY_DUE_DATE_END.equals(dueDateType)) {
			searchParams.setStartDate(false);
			List<CurriculumElement> curriculumElementsFormEnd = providerDao.loadPending(searchParams);
			for (CurriculumElement curriculumElement: curriculumElementsFormEnd) {
				Date dcStart = addDays(curriculumElement.getEndDate(), dueDateDays);
				generateDataCollection(generator, configs, organisations, curriculumElement, dcStart);
			}
			numCreated += curriculumElementsFormEnd.size();
		}
		
		log.debug(numCreated + " data collections created by generator " + generator.toString());
	}

	private void generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<Organisation> organisations, CurriculumElement curriculumElement, Date dcStart) {
		// create data collection	
		RepositoryEntry formEntry = generator.getFormEntry();
		Long generatorProviderKey = curriculumElement.getKey();
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry, generator, generatorProviderKey);

		dataCollection.setStart(dcStart);
		
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		Date deadline = addDays(dcStart, duration);
		dataCollection.setDeadline(deadline);
		
		String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
		String title = titleCreator.merge(titleTemplate, singletonList(curriculumElement));
		dataCollection.setTitle(title);
		
		dataCollection.setTopicType(QualityDataCollectionTopicType.CURRICULUM_ELEMENT);
		dataCollection.setTopicCurriculumElement(curriculumElement);
		
		dataCollection.setStatus(QualityDataCollectionStatus.READY);
		qualityService.updateDataCollection(dataCollection);
		
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
	}

	private Date addDays(Date date, String daysToAdd) {
		int days = Integer.parseInt(daysToAdd);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

}
