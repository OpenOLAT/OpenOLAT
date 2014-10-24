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
package org.olat.course.certificate.manager;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createTemplate() throws URISyntaxException {
		URL templateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(templateUrl);
		File templateFile = new File(templateUrl.toURI());
		
		String name = UUID.randomUUID() + ".pdf";
		CertificateTemplate template = certificatesManager.addTemplate(name, templateFile, true);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(template);
		Assert.assertNotNull(template.getKey());
		Assert.assertNotNull(template.getCreationDate());
		Assert.assertNotNull(template.getLastModified());
		Assert.assertEquals(name, template.getName());
		Assert.assertTrue(template.isPublicTemplate());
	}
	
	@Test
	public void createCertificate() throws URISyntaxException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1");
		RepositoryEntry entry = JunitTestHelper.deployDemoCourse(identity);
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null);
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null);
		Assert.assertNotNull(certificate);
		Assert.assertNotNull(certificate.getKey());
		Assert.assertNotNull(certificate.getUuid());
		Assert.assertNotNull(certificate.getName());
		Assert.assertEquals(entry.getOlatResource().getKey(), certificate.getArchivedResourceKey());
		
		//check if the pdf exists
		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
		Assert.assertNotNull(certificateFile);
		Assert.assertTrue(certificateFile.exists());
	}
	
	@Test
	public void certificateNotifications_courseCoach() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-2");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-3");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-4");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-4");
		RepositoryEntry entry = JunitTestHelper.deployDemoCourse(owner);
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos1 = new CertificateInfos(participant1, null, null);
		Certificate certificate1 = certificatesManager.generateCertificate(certificateInfos1, entry, null);
		Assert.assertNotNull(certificate1);
		CertificateInfos certificateInfos2 = new CertificateInfos(participant2, null, null);
		Certificate certificate2 = certificatesManager.generateCertificate(certificateInfos2, entry, null);
		Assert.assertNotNull(certificate2);
		dbInstance.commitAndCloseSession();
		
		Calendar lastestNews = Calendar.getInstance();
		lastestNews.add(Calendar.HOUR_OF_DAY, -1);
	
		//check the notifications of the author ( entry admin )
		List<Certificate> authorNotifications = certificatesManager.getCertificatesForNotifications(owner, entry, lastestNews.getTime());
		Assert.assertNotNull(authorNotifications);
		Assert.assertEquals(2, authorNotifications.size());
		
		//check the notifications of the coach
		List<Certificate> coachNotifications = certificatesManager.getCertificatesForNotifications(coach, entry, lastestNews.getTime());
		Assert.assertNotNull(coachNotifications);
		Assert.assertEquals(2, coachNotifications.size());
		
		//check the notifications of the participant
		List<Certificate> participantNotifications = certificatesManager.getCertificatesForNotifications(participant1, entry, lastestNews.getTime());
		Assert.assertNotNull(participantNotifications);
		Assert.assertEquals(1, participantNotifications.size());
		Assert.assertTrue(participantNotifications.contains(certificate1));
	}
	
	@Test
	public void certificateNotifications_courseCoachByGroups() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-5");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-6");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-7");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-8");
		RepositoryEntry entry = JunitTestHelper.deployDemoCourse(owner);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "certified-group", "Group with certification", null, null, false, false, entry);
	    businessGroupRelationDao.addRole(coach, group, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make a certificate
		CertificateInfos certificateInfos1 = new CertificateInfos(participant1, null, null);
		Certificate certificate1 = certificatesManager.generateCertificate(certificateInfos1, entry, null);
		Assert.assertNotNull(certificate1);
		CertificateInfos certificateInfos2 = new CertificateInfos(participant2, null, null);
		Certificate certificate2 = certificatesManager.generateCertificate(certificateInfos2, entry, null);
		Assert.assertNotNull(certificate2);
		dbInstance.commitAndCloseSession();
		
		
		Calendar lastestNews = Calendar.getInstance();
		lastestNews.add(Calendar.HOUR_OF_DAY, -1);
		
		//check the notifications of the coach
		List<Certificate> coachNotifications = certificatesManager.getCertificatesForNotifications(coach, entry, lastestNews.getTime());
		Assert.assertNotNull(coachNotifications);
		Assert.assertEquals(2, coachNotifications.size());
		
		//check the notifications of the participant
		List<Certificate> participantNotifications = certificatesManager.getCertificatesForNotifications(participant1, entry, lastestNews.getTime());
		Assert.assertNotNull(participantNotifications);
		Assert.assertEquals(1, participantNotifications.size());
		Assert.assertTrue(participantNotifications.contains(certificate1));
	}
}
