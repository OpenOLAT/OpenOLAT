/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.SafeExamBrowserTemplateSearchParams;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserTemplateDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private SafeExamBrowserTemplateDAO sebTemplateDao;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentInspectionConfigurationDAO inspectionConfigurationDao;

	@Test
	public void createTemplate() {
		SafeExamBrowserTemplate template = sebTemplateDao.createTemplate("Test SEB Template");
		dbInstance.commitAndCloseSession();

		assertThat(template).isNotNull();
		assertThat(template.getKey()).isNotNull();
		assertThat(template.getCreationDate()).isNotNull();
		assertThat(template.getLastModified()).isNotNull();
		assertThat(template.getName()).isEqualTo("Test SEB Template");
		assertThat(template.isActive()).isTrue();
		assertThat(template.isDefault()).isFalse();
	}

	@Test
	public void updateTemplate() {
		SafeExamBrowserTemplate template = sebTemplateDao.createTemplate("Before Update");
		Long key = template.getKey();
		dbInstance.commitAndCloseSession();

		template.setName("After Update");
		template.setActive(false);
		template.setDefault(true);
		template.setSafeExamBrowserConfiguration(new SafeExamBrowserConfiguration());
		template = sebTemplateDao.updateTemplate(template);
		dbInstance.commitAndCloseSession();

		SafeExamBrowserTemplateSearchParams params = new SafeExamBrowserTemplateSearchParams();
		List<SafeExamBrowserTemplate> templates = sebTemplateDao.loadTemplates(params);

		SafeExamBrowserTemplate reloaded = templates.stream()
				.filter(t -> t.getKey().equals(key))
				.findFirst().orElse(null);

		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getName()).isEqualTo("After Update");
		assertThat(reloaded.isActive()).isFalse();
		assertThat(reloaded.isDefault()).isTrue();
		assertThat(reloaded.getSafeExamBrowserConfiguration()).isNotNull();
		assertThat(reloaded.getSafeExamBrowserConfigPList()).isNotNull();
		assertThat(reloaded.getSafeExamBrowserConfigPListKey()).isNotNull();
	}

	@Test
	public void deleteTemplate() {
		SafeExamBrowserTemplate template = sebTemplateDao.createTemplate("To Delete");
		dbInstance.commitAndCloseSession();
		Long key = template.getKey();

		sebTemplateDao.deleteTemplate(template);
		dbInstance.commitAndCloseSession();

		SafeExamBrowserTemplateSearchParams params = new SafeExamBrowserTemplateSearchParams();
		List<SafeExamBrowserTemplate> templates = sebTemplateDao.loadTemplates(params);

		assertThat(templates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.doesNotContain(key);
	}

	@Test
	public void loadTemplates_filterDefault() {
		SafeExamBrowserTemplate defaultTemplate = sebTemplateDao.createTemplate("Default Template");
		defaultTemplate.setDefault(true);
		defaultTemplate = sebTemplateDao.updateTemplate(defaultTemplate);

		SafeExamBrowserTemplate nonDefaultTemplate = sebTemplateDao.createTemplate("Non-Default Template");
		nonDefaultTemplate.setDefault(false);
		nonDefaultTemplate = sebTemplateDao.updateTemplate(nonDefaultTemplate);
		dbInstance.commitAndCloseSession();

		SafeExamBrowserTemplateSearchParams defaultParams = new SafeExamBrowserTemplateSearchParams();
		defaultParams.setDefault(Boolean.TRUE);
		List<SafeExamBrowserTemplate> defaultTemplates = sebTemplateDao.loadTemplates(defaultParams);

		assertThat(defaultTemplates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.contains(defaultTemplate.getKey())
				.doesNotContain(nonDefaultTemplate.getKey());

		SafeExamBrowserTemplateSearchParams nonDefaultParams = new SafeExamBrowserTemplateSearchParams();
		nonDefaultParams.setDefault(Boolean.FALSE);
		List<SafeExamBrowserTemplate> nonDefaultTemplates = sebTemplateDao.loadTemplates(nonDefaultParams);

		assertThat(nonDefaultTemplates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.contains(nonDefaultTemplate.getKey())
				.doesNotContain(defaultTemplate.getKey());
	}

	@Test
	public void loadTemplates_filterKeys() {
		SafeExamBrowserTemplate template1 = sebTemplateDao.createTemplate("Keys Template 1");
		SafeExamBrowserTemplate template2 = sebTemplateDao.createTemplate("Keys Template 2");
		SafeExamBrowserTemplate template3 = sebTemplateDao.createTemplate("Keys Template 3");
		dbInstance.commitAndCloseSession();

		SafeExamBrowserTemplateSearchParams params = new SafeExamBrowserTemplateSearchParams();
		params.setKeys(List.of(template1.getKey(), template2.getKey()));
		List<SafeExamBrowserTemplate> templates = sebTemplateDao.loadTemplates(params);

		assertThat(templates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.contains(template1.getKey(), template2.getKey())
				.doesNotContain(template3.getKey());
	}

	@Test
	public void loadTemplates_filterKey() {
		SafeExamBrowserTemplate template1 = sebTemplateDao.createTemplate("Single Key Template 1");
		SafeExamBrowserTemplate template2 = sebTemplateDao.createTemplate("Single Key Template 2");
		dbInstance.commitAndCloseSession();

		SafeExamBrowserTemplateSearchParams params = new SafeExamBrowserTemplateSearchParams();
		params.setKey(template1.getKey());
		List<SafeExamBrowserTemplate> templates = sebTemplateDao.loadTemplates(params);

		assertThat(templates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.contains(template1.getKey())
				.doesNotContain(template2.getKey());
	}

	@Test
	public void loadTemplates_filterActive() {
		SafeExamBrowserTemplate activeTemplate = sebTemplateDao.createTemplate("Active Template");
		activeTemplate.setActive(true);
		activeTemplate = sebTemplateDao.updateTemplate(activeTemplate);

		SafeExamBrowserTemplate inactiveTemplate = sebTemplateDao.createTemplate("Inactive Template");
		inactiveTemplate.setActive(false);
		inactiveTemplate = sebTemplateDao.updateTemplate(inactiveTemplate);
		dbInstance.commitAndCloseSession();

		SafeExamBrowserTemplateSearchParams activeParams = new SafeExamBrowserTemplateSearchParams();
		activeParams.setActive(Boolean.TRUE);
		List<SafeExamBrowserTemplate> activeTemplates = sebTemplateDao.loadTemplates(activeParams);

		assertThat(activeTemplates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.contains(activeTemplate.getKey())
				.doesNotContain(inactiveTemplate.getKey());

		SafeExamBrowserTemplateSearchParams inactiveParams = new SafeExamBrowserTemplateSearchParams();
		inactiveParams.setActive(Boolean.FALSE);
		List<SafeExamBrowserTemplate> inactiveTemplates = sebTemplateDao.loadTemplates(inactiveParams);

		assertThat(inactiveTemplates)
				.extracting(SafeExamBrowserTemplate::getKey)
				.contains(inactiveTemplate.getKey())
				.doesNotContain(activeTemplate.getKey());
	}

	@Test
	public void getTemplateToUsageCount() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();

		SafeExamBrowserTemplate template1 = sebTemplateDao.createTemplate("Count Template 1");
		SafeExamBrowserTemplate template2 = sebTemplateDao.createTemplate("Count Template 2");
		SafeExamBrowserTemplate template3 = sebTemplateDao.createTemplate("Count Template 3");
		SafeExamBrowserTemplate template4 = sebTemplateDao.createTemplate("Count Template 4");

		createAndPersistModeWithTemplate(entry, template1);
		createAndPersistModeWithTemplate(entry, template1);
		createAndPersistModeWithTemplate(entry, template2);
		createAndPersistInspectionConfigWithTemplate(entry, template1);
		createAndPersistInspectionConfigWithTemplate(entry, template3);
		dbInstance.commitAndCloseSession();

		Map<Long, Long> counts = sebTemplateDao.getTemplateToUsageCount();

		assertThat(counts.get(template1.getKey())).isGreaterThanOrEqualTo(3L);
		assertThat(counts.get(template2.getKey())).isGreaterThanOrEqualTo(1L);
		assertThat(counts.get(template3.getKey())).isGreaterThanOrEqualTo(1L);
		assertThat(counts).doesNotContainKey(template4.getKey());
	}

	private AssessmentInspectionConfiguration createAndPersistInspectionConfigWithTemplate(RepositoryEntry entry, SafeExamBrowserTemplate template) {
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config.setSafeExamBrowserTemplate(template);
		return inspectionConfigurationDao.saveConfiguration(config);
	}

	private AssessmentMode createAndPersistModeWithTemplate(RepositoryEntry entry, SafeExamBrowserTemplate template) {
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Mode with template");
		mode.setTargetAudience(Target.course);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, 2);
		Date begin = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 2);
		Date end = cal.getTime();
		mode.setBegin(begin);
		mode.setEnd(end);

		mode.setSafeExamBrowserTemplate(template);
		return assessmentModeMgr.persist(mode);
	}

}
