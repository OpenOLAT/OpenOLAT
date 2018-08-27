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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityManager;
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
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.courselectures.manager.CourseLecturesProviderDAO;
import org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo;
import org.olat.modules.quality.generator.provider.courselectures.manager.SearchParameters;
import org.olat.modules.quality.generator.provider.courselectures.ui.CourseLectureProviderConfigController;
import org.olat.modules.quality.generator.ui.AbstractGeneratorEditController;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
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

	private static final OLog log = Tracing.createLoggerFor(CourseLecturesProvider.class);

	public static final String CONFIG_KEY_SURVEY_LECTURE = "survey.lecture";
	public static final String CONFIG_KEY_TOTAL_LECTURES = "total.lecture";
	public static final String CONFIG_KEY_DURATION_DAYS = "duration.days";
	public static final String CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS = "invitation.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER1_AFTER_DC_DAYS = "reminder1.after.dc.start.days";
	public static final String CONFIG_KEY_REMINDER2_AFTER_DC_DAYS = "reminder2.after.dc.start.days";
	public static final String CONFIG_KEY_ROLES = "participants.roles";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String CONFIG_KEY_WHITE_LIST = "white.list";
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
	
	@Override
	public String getType() {
		return "course-lecture";
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
		List<LectureBlockInfo> infos = providerDao.loadLectureBlockInfo(searchParams);
		
		return translator.translate("generate.info", new String[] { String.valueOf(infos.size())});
	}

	@Override
	public boolean hasWhiteListController() {
		//TODO uh 
		return false;
	}

	@Override
	public AbstractGeneratorEditController getWhiteListController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator,
			QualityGeneratorConfigs configs) {
		//TODO uh 
		return null;
	}

	@Override
	public void generate(QualityGenerator generator, QualityGeneratorConfigs configs, Date fromDate, Date toDate) {
		List<Organisation> organisations = generatorService.loadGeneratorOrganisations(generator);
		SearchParameters searchParams = getSeachParameters(generator, configs, organisations, fromDate, toDate);
		List<LectureBlockInfo> infos = providerDao.loadLectureBlockInfo(searchParams);
		for (LectureBlockInfo lectureBlockInfo: infos) {
			generateDataCollection(generator, configs, organisations, lectureBlockInfo);
		}
		
		log.debug(infos.size() + " data collections created by generator " + generator.toString());
	}
	
	private void generateDataCollection(QualityGenerator generator, QualityGeneratorConfigs configs,
			List<Organisation> organisations, LectureBlockInfo lectureBlockInfo) {
		// create data collection	
		RepositoryEntry formEntry = generator.getFormEntry();
		Long generatorProviderKey = lectureBlockInfo.getCourseRepoKey();
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry, generator, generatorProviderKey);

		Date dcStart = lectureBlockInfo.getLectureEndDate();
		dataCollection.setStart(dcStart);
		
		String duration = configs.getValue(CONFIG_KEY_DURATION_DAYS);
		Date deadline = addDays(dcStart, duration);
		dataCollection.setDeadline(deadline);
		
		RepositoryEntry course = repositoryService.loadByKey(lectureBlockInfo.getCourseRepoKey());
		Identity teacher = securityManager.loadIdentityByKey(lectureBlockInfo.getTeacherKey());
		String titleTemplate = configs.getValue(CONFIG_KEY_TITLE);
		String title = titleCreator.merge(titleTemplate, Arrays.asList(course, teacher.getUser()));
		dataCollection.setTitle(title);
		
		dataCollection.setTopicType(QualityDataCollectionTopicType.IDENTIY);
		dataCollection.setTopicIdentity(teacher);
		
		dataCollection.setStatus(QualityDataCollectionStatus.READY);
		qualityService.updateDataCollection(dataCollection);
		
		// add participants
		String[] roleNames = configs.getValue(CONFIG_KEY_ROLES).split(ROLES_DELIMITER);
		for (String roleName: roleNames) {
			GroupRoles role = GroupRoles.valueOf(roleName);
			RepositoryEntry repositoryEntry = repositoryService.loadByKey(lectureBlockInfo.getCourseRepoKey());
			Collection<Identity> identities = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, roleName);
			List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection, identities);
			for (EvaluationFormParticipation participation: participations) {
				qualityService.createContextBuilder(dataCollection, participation, repositoryEntry, role).build();
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

	private SearchParameters getSeachParameters(QualityGenerator generator, QualityGeneratorConfigs configs,
			Collection<? extends OrganisationRef> organisations, Date fromDate, Date toDate) {
		SearchParameters searchParams = new SearchParameters();
		searchParams.setExcludeGeneratorRef(generator);
		searchParams.setOrgansationRefs(organisations);
		searchParams.setFrom(fromDate);
		searchParams.setTo(toDate);
		Integer minExceedingLectures = Integer.parseInt(configs.getValue(CONFIG_KEY_TOTAL_LECTURES));
		searchParams.setMinTotalLectures(minExceedingLectures);
		Integer selectingLecture = Integer.parseInt(configs.getValue(CONFIG_KEY_SURVEY_LECTURE));
		searchParams.setSelectingLecture(selectingLecture);
		return searchParams;
	}

}
