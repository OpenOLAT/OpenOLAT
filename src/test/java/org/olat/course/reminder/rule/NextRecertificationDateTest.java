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
package org.olat.course.reminder.rule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.manager.CertificatesManagerTest;
import org.olat.course.certificate.manager.RepositoryEntryCertificateConfigurationDAO;
import org.olat.course.nodes.gta.rule.AssignTaskRuleSPI;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class NextRecertificationDateTest extends OlatTestCase {
	

	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private NextRecertificationDateSPI nextRecertificationSPI;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryEntryCertificateConfigurationDAO repositoryEntryCertificateConfigurationDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "",
							null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void nextRecertification() throws URISyntaxException {
		// Create a course
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		
		// Enable recertification
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.getConfiguration(entry);
		config.setAutomaticCertificationEnabled(true);
		config.setRecertificationEnabled(true);
		config.setRecertificationLeadTimeInDays(30);
		repositoryEntryCertificateConfigurationDao.updateConfiguration(config);
		
		// Add participant
		repositoryEntryRelationDao.addRole(identity, entry, GroupRoles.participant.name());
		
		// Create a certificate
		Date nextRecertificationDate = DateUtils.addDays(new Date(), 8);
		URL templateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(templateUrl);
		File certificateFile = new File(templateUrl.toURI());
		
		Certificate certificate = certificatesManager
				.uploadCertificate(identity, new Date(), null, null, entry.getOlatResource(), nextRecertificationDate, certificateFile);
		dbInstance.commit();
		Assert.assertNotNull(certificate);

		{// Between 10 and 5 days before recertification
			ReminderRuleImpl reminder = getNextRecertificationDateRule(-10, LaunchUnit.day, 5);
			List<Identity> identitiesToRemind = nextRecertificationSPI.evaluate(entry, reminder);
			Assertions.assertThat(identitiesToRemind)
				.hasSize(1)
				.containsExactly(identity);
		}
		
		{// 10 day before recertification
			ReminderRuleImpl reminder = getNextRecertificationDateRule(-10, LaunchUnit.day, 1);
			List<Identity> identitiesToRemind = nextRecertificationSPI.evaluate(entry, reminder);
			Assertions.assertThat(identitiesToRemind)
				.isEmpty();
		}
		
		{// 10 day before recertification without tolerance
			ReminderRuleImpl reminder = getNextRecertificationDateRule(-10, LaunchUnit.day, null);
			List<Identity> identitiesToRemind = nextRecertificationSPI.evaluate(entry, reminder);
			Assertions.assertThat(identitiesToRemind)
				.hasSize(1)
				.containsExactly(identity);
		}

	}
	
	private ReminderRuleImpl getNextRecertificationDateRule(int amount, LaunchUnit unit, Integer tolerance) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(AssignTaskRuleSPI.class.getSimpleName());
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());
		if(tolerance != null) {
			rule.setTolerance(tolerance.toString());
			rule.setToleranceUnit(LaunchUnit.day.name());
		}
		return rule;
	}

}
