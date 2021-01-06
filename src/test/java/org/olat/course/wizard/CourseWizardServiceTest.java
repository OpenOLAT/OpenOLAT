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
package org.olat.course.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesManagerTest;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseWizardServiceTest extends OlatTestCase {
	
	private Identity author;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	
	@Autowired
	private CourseWizardService sut;
	
	@Before
	public void setUp() {
		author = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
	}
	
	@Test
	public void shouldSetRepositoryEntryStatus() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryStatusEnum review = RepositoryEntryStatusEnum.review;
		sut.updateEntryStatus(author, entry, review);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reloadedEntry = repositoryService.loadByKey(entry.getKey());
		assertThat(reloadedEntry.getEntryStatus()).isEqualTo(review);
	}
	
	@Test
	public void shouldPublishCourseElements() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		IQTESTCourseNodeDefaults defaults = new IQTESTCourseNodeDefaults();
		defaults.setReferencedEntry(testEntry);
		String shortTitle = random();
		defaults.setShortTitle(shortTitle);
		ICourse course = sut.startCourseEditSession(entry);
		sut.createIQTESTCourseNode(course, defaults);
		sut.finishCourseEditSession(course);
		dbInstance.commitAndCloseSession();
		
		sut.publishCourse(author, course);
		
		INode lastNode = TreeHelper.getLastNode(course.getRunStructure().getRootNode());
		CourseNode courseNode = (CourseNode)lastNode;
		assertThat(courseNode.getShortTitle()).isEqualTo(shortTitle);
	}
	
	@Test
	public void shouldSetCertificateConfigs() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		CertificateDefaults defaults = new CertificateDefaults();
		defaults.setAutomaticCertificationEnabled(true);
		defaults.setManualCertificationEnabled(false);
		String certificateCustom1 = random();
		defaults.setCertificateCustom1(certificateCustom1);
		String certificateCustom2 = random();
		defaults.setCertificateCustom2(certificateCustom2);
		String certificateCustom3 = random();
		defaults.setCertificateCustom3(certificateCustom3);
		CertificateTemplate template = createTemplate();
		defaults.setTemplate(template);
		
		ICourse course = sut.startCourseEditSession(entry);
		sut.setCertificateConfigs(course, defaults);
		sut.finishCourseEditSession(course);
		
		SoftAssertions softly = new SoftAssertions();
		CourseConfig courseConfig = course.getCourseConfig();
		assertThat(courseConfig.isAutomaticCertificationEnabled()).isTrue();
		assertThat(courseConfig.isManualCertificationEnabled()).isFalse();
		assertThat(courseConfig.getCertificateCustom1()).isEqualTo(certificateCustom1);
		assertThat(courseConfig.getCertificateCustom2()).isEqualTo(certificateCustom2);
		assertThat(courseConfig.getCertificateCustom3()).isEqualTo(certificateCustom3);
		assertThat(courseConfig.getCertificateTemplate()).isEqualTo(template.getKey());
		softly.assertAll();
	}
	
	public CertificateTemplate createTemplate() {
		CertificateTemplate template = null;
		try {
			URL templateUrl = CertificatesManagerTest.class.getResource("template.pdf");
			File templateFile = new File(templateUrl.toURI());
			String certificateName = random() + ".pdf";
			template = certificatesManager.addTemplate(certificateName, templateFile, null, null, true, author);
			dbInstance.commitAndCloseSession();
		} catch (URISyntaxException e) {
			//
		}
		return template;
	}
	
	@Test
	public void shouldCreateIQTESTCourseNode() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		IQTESTCourseNodeDefaults defaults = new IQTESTCourseNodeDefaults();

		ICourse course = sut.startCourseEditSession(entry);
		sut.createIQTESTCourseNode(course, defaults);
		sut.finishCourseEditSession(course);

		INode lastNode = TreeHelper.getLastNode(course.getEditorTreeModel().getRootNode());
		CourseNode lastCourseNode = ((CourseEditorTreeNode)lastNode).getCourseNode();
		assertThat(lastCourseNode.getType()).isEqualTo(IQTESTCourseNode.TYPE);
	}
	
	@Test
	public void shouldSetIQTESTCourseNodeConfigs() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		IQTESTCourseNodeDefaults defaults = new IQTESTCourseNodeDefaults();
		defaults.setReferencedEntry(testEntry);
		String shortTitle = random();
		defaults.setShortTitle(shortTitle);
		String longTitle = random();
		defaults.setLongTitle(longTitle);
		String objectives = random();
		defaults.setObjectives(objectives);
		ModuleConfiguration defaultModuleConfig = new ModuleConfiguration();
		String modulConfigValue = random();
		defaultModuleConfig.setStringValue("configKey", modulConfigValue);
		defaults.setModuleConfig(defaultModuleConfig);

		ICourse course = sut.startCourseEditSession(entry);
		sut.createIQTESTCourseNode(course, defaults);
		sut.finishCourseEditSession(course);

		INode lastNode = TreeHelper.getLastNode(course.getEditorTreeModel().getRootNode());
		CourseNode courseNode = ((CourseEditorTreeNode)lastNode).getCourseNode();
		ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(courseNode.getShortTitle()).isEqualTo(shortTitle);
		softly.assertThat(courseNode.getLongTitle()).isEqualTo(longTitle);
		softly.assertThat(courseNode.getLearningObjectives()).isEqualTo(objectives);
		softly.assertThat(moduleConfig.getStringValue(IQEditController.CONFIG_KEY_TYPE_QTI)).isEqualTo(IQEditController.CONFIG_VALUE_QTI21);
		softly.assertThat(IQEditController.getIQReference(moduleConfig, true)).isEqualTo(testEntry);
		softly.assertThat(moduleConfig.getStringValue("configKey")).isEqualTo(modulConfigValue);
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateAssessmentModeForTestIfDateDependent() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		IQTESTCourseNodeDefaults defaults = new IQTESTCourseNodeDefaults();
		String shortTitle = random();
		defaults.setShortTitle(shortTitle);
		ModuleConfiguration defaultModuleConfig = new ModuleConfiguration();
		defaultModuleConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, true);
		Date start = new GregorianCalendar(2020, 12, 10, 10, 0, 0).getTime();
		defaultModuleConfig.setDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE, start);
		Date end = new GregorianCalendar(2020, 12, 10, 11, 13, 15).getTime();
		defaultModuleConfig.setDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE, end);
		defaults.setModuleConfig(defaultModuleConfig);
		
		ICourse course = sut.startCourseEditSession(entry);
		sut.createIQTESTCourseNode(course, defaults);
		sut.finishCourseEditSession(course);
		
		List<AssessmentMode> assessmentModes = assessmentModeManager.getAssessmentModeFor(entry);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(assessmentModes).hasSize(1);
		if (!assessmentModes.isEmpty()) {
			AssessmentMode assessmentMode = assessmentModes.get(0);
			softly.assertThat(assessmentMode.getName()).isEqualTo(shortTitle);
			softly.assertThat(assessmentMode.getBegin()).isCloseTo(start, 2000);
			softly.assertThat(assessmentMode.getEnd()).isCloseTo(end, 2000);
		}
		softly.assertAll();
	}
	
	@Test
	public void shouldNotCreateAssessmentModeForTestIfNotDateDependent() {
		RepositoryEntry entry = JunitTestHelper.deployEmptyCourse(author, random(), RepositoryEntryStatusEnum.published, false, false);
		dbInstance.commitAndCloseSession();
		IQTESTCourseNodeDefaults defaults = new IQTESTCourseNodeDefaults();
		ModuleConfiguration defaultModuleConfig = new ModuleConfiguration();
		defaultModuleConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, false);
		defaults.setModuleConfig(defaultModuleConfig);
		
		ICourse course = sut.startCourseEditSession(entry);
		sut.createIQTESTCourseNode(course, defaults);
		sut.finishCourseEditSession(course);
		
		List<AssessmentMode> assessmentModes = assessmentModeManager.getAssessmentModeFor(entry);
		assertThat(assessmentModes).isEmpty();
	}

}
