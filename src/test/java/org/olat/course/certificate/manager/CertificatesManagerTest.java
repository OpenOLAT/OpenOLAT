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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
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
	private RepositoryService repositoryService;
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
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-0");
		URL templateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(templateUrl);
		File templateFile = new File(templateUrl.toURI());
		
		String certificateName = UUID.randomUUID() + ".pdf";
		CertificateTemplate template = certificatesManager.addTemplate(certificateName, templateFile, null, null, true, identity);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(template);
		Assert.assertNotNull(template.getKey());
		Assert.assertNotNull(template.getCreationDate());
		Assert.assertNotNull(template.getLastModified());
		Assert.assertEquals(certificateName, template.getName());
		Assert.assertTrue(template.isPublicTemplate());
	}
	
	@Test
	public void createCertificate() throws URISyntaxException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		Assert.assertNotNull(certificate);
		Assert.assertNotNull(certificate.getKey());
		Assert.assertNotNull(certificate.getUuid());
		Assert.assertEquals(entry.getOlatResource().getKey(), certificate.getArchivedResourceKey());
		//check if the pdf exists / flush cache, reload the entry with the updated path
		dbInstance.commitAndCloseSession();

		waitCertificate(certificate.getKey());

		Certificate reloadCertificate = certificatesManager.getCertificateById(certificate.getKey());
		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(reloadCertificate);
		Assert.assertNotNull(certificateFile);
		Assert.assertTrue(certificateFile.exists());
	}
	
	@Test
	public void loadCertificate() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		//full
		Certificate reloadedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadedCertificate);
		Assert.assertEquals(certificate, reloadedCertificate);
		Assert.assertNotNull(reloadedCertificate.getUuid());
		Assert.assertEquals(certificate.getUuid(), reloadedCertificate.getUuid());
		Assert.assertEquals(entry.getDisplayname(), reloadedCertificate.getCourseTitle());
		Assert.assertEquals(identity, reloadedCertificate.getIdentity());
		
		//light
		CertificateLight reloadedLight = certificatesManager.getCertificateLightById(certificate.getKey());
		Assert.assertNotNull(reloadedLight);
		Assert.assertEquals(certificate.getKey(), reloadedLight.getKey());
		Assert.assertEquals(entry.getDisplayname(), reloadedLight.getCourseTitle());
		Assert.assertEquals(identity.getKey(), reloadedLight.getIdentityKey());
		Assert.assertEquals(entry.getOlatResource().getKey(), reloadedLight.getOlatResourceKey());
		
		//uuid
		Certificate reloadedUuid = certificatesManager.getCertificateByUuid(certificate.getUuid());
		Assert.assertNotNull(reloadedUuid);
		Assert.assertEquals(certificate, reloadedUuid);
		Assert.assertEquals(entry.getDisplayname(), reloadedUuid.getCourseTitle());
		Assert.assertEquals(identity, reloadedUuid.getIdentity());
		
		//boolean
		boolean has = certificatesManager.hasCertificate(identity, entry.getOlatResource().getKey());
		Assert.assertTrue(has);
	}
	
	@Test
	public void loadLastCertificate() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		//per resource
		Certificate reloadedCertificate = certificatesManager.getLastCertificate(identity, entry.getOlatResource().getKey());
		Assert.assertNotNull(reloadedCertificate);
		Assert.assertEquals(certificate, reloadedCertificate);
		
		//all
		List<CertificateLight> allCertificates = certificatesManager.getLastCertificates(identity);
		Assert.assertNotNull(allCertificates);
		Assert.assertEquals(1, allCertificates.size());
		CertificateLight allCertificate = allCertificates.get(0);
		Assert.assertEquals(certificate.getKey(), allCertificate.getKey());
		Assert.assertEquals(entry.getDisplayname(), allCertificate.getCourseTitle());
		Assert.assertEquals(identity.getKey(), allCertificate.getIdentityKey());
		Assert.assertEquals(entry.getOlatResource().getKey(), allCertificate.getOlatResourceKey());
	}
	
	@Test
	public void certificateNotifications_courseCoach() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-2");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-3");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-4");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(owner);
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos1 = new CertificateInfos(participant1, null, null, null, null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate1 = certificatesManager.generateCertificate(certificateInfos1, entry, null, config);
		Assert.assertNotNull(certificate1);
		CertificateInfos certificateInfos2 = new CertificateInfos(participant2, null, null, null, null);
		Certificate certificate2 = certificatesManager.generateCertificate(certificateInfos2, entry, null, config);
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
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(owner);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "certified-group", "Group with certification", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);
	    businessGroupRelationDao.addRole(coach, group, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make a certificate
		CertificateInfos certificateInfos1 = new CertificateInfos(participant1, null, null, null, null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate1 = certificatesManager.generateCertificate(certificateInfos1, entry, null, config);
		Assert.assertNotNull(certificate1);
		CertificateInfos certificateInfos2 = new CertificateInfos(participant2, null, null, null, null);
		Certificate certificate2 = certificatesManager.generateCertificate(certificateInfos2, entry, null, config);
		Assert.assertNotNull(certificate2);
		dbInstance.commitAndCloseSession();
		
		waitCertificate(certificate1.getKey());
		waitCertificate(certificate2.getKey());

		dbInstance.commitAndCloseSession();
		sleep(2000);

		Calendar lastestNews = Calendar.getInstance();
		lastestNews.add(Calendar.DATE, -1);
		
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
	public void uploadCertificate() throws URISyntaxException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.YEAR, 2012);
		Date creationDate = cal.getTime();
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		
		Certificate certificate = certificatesManager.uploadCertificate(identity, creationDate, null, null, entry.getOlatResource(), certificateFile);
		Assert.assertNotNull(certificate);
		Assert.assertNotNull(certificate.getKey());
		Assert.assertNotNull(certificate.getUuid());
		Assert.assertEquals(entry.getDisplayname(), certificate.getCourseTitle());
		Assert.assertEquals(identity, certificate.getIdentity());
		
		dbInstance.commitAndCloseSession();
		
		//double check
		Certificate reloadedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadedCertificate);
		Assert.assertNotNull(reloadedCertificate.getUuid());
		Assert.assertEquals(entry.getDisplayname(), reloadedCertificate.getCourseTitle());
		Assert.assertEquals(identity, reloadedCertificate.getIdentity());
		Assert.assertEquals(entry.getOlatResource().getKey(), reloadedCertificate.getArchivedResourceKey());
		Assert.assertEquals(creationDate, reloadedCertificate.getCreationDate());
		
		//the file
		VFSLeaf savedCertificateFile = certificatesManager.getCertificateLeaf(reloadedCertificate);
		Assert.assertNotNull(savedCertificateFile);
		Assert.assertTrue(savedCertificateFile.exists());
		
		//load last
		Certificate lastCertificate = certificatesManager.getLastCertificate(identity, entry.getOlatResource().getKey());
		Assert.assertNotNull(lastCertificate);
		Assert.assertEquals(certificate, lastCertificate);
	}
	
	@Test
	public void uploadStandalone() throws URISyntaxException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1");
		dbInstance.commitAndCloseSession();
		
		String courseTitle = "Unkown course";
		Long resourceKey = 4l;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.YEAR, 2012);
		Date creationDate = cal.getTime();
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		
		Certificate certificate = certificatesManager
				.uploadStandaloneCertificate(identity, creationDate, null, null, courseTitle, resourceKey, certificateFile);
		Assert.assertNotNull(certificate);
		Assert.assertNotNull(certificate.getKey());
		Assert.assertNotNull(certificate.getUuid());
		Assert.assertEquals(courseTitle, certificate.getCourseTitle());
		Assert.assertEquals(identity, certificate.getIdentity());
		
		dbInstance.commitAndCloseSession();
		
		//load by id
		Certificate reloadedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadedCertificate);
		Assert.assertNotNull(reloadedCertificate.getUuid());
		Assert.assertEquals(courseTitle, reloadedCertificate.getCourseTitle());
		Assert.assertEquals(identity, reloadedCertificate.getIdentity());
		Assert.assertEquals(resourceKey, reloadedCertificate.getArchivedResourceKey());
		Assert.assertEquals(creationDate, reloadedCertificate.getCreationDate());
		
		//load last
		Certificate lastCertificate = certificatesManager.getLastCertificate(identity, resourceKey);
		Assert.assertNotNull(lastCertificate);
		Assert.assertEquals(certificate.getKey(), lastCertificate.getKey());
		Assert.assertEquals(reloadedCertificate, lastCertificate);
	}
	
	/**
	 * Create a course, add a certificate to it and delete the course.
	 * The certificate stays.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void deleteCourse()  throws URISyntaxException  {
		//create a course with a certificate
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-del-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		Long resourceKey = entry.getOlatResource().getKey();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		File certificateFile = new File(certificateUrl.toURI());
		Certificate certificate = certificatesManager.uploadCertificate(identity, cal.getTime(), null, null, entry.getOlatResource(), certificateFile);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		//delete the course
		Roles roles = Roles.administratorRoles();
		repositoryService.deletePermanently(entry, identity, roles, Locale.ENGLISH);
		dbInstance.commitAndCloseSession();
		
		//retrieve the certificate
		Certificate reloadedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadedCertificate);
		Assert.assertEquals(certificate, reloadedCertificate);
		Assert.assertNotNull(reloadedCertificate.getArchivedResourceKey());
		Assert.assertEquals(resourceKey, reloadedCertificate.getArchivedResourceKey());
	}
	
	
	/**
	 * Create 2 courses, add a certificate to them and delete the first course.
	 * Check that a certificate loose the relation to the deleted course but not
	 * the other. The two certificates stay.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void deleteCourse_paranoiaCheck()  throws URISyntaxException  {
		//create a course with a certificate
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-del-3");
		RepositoryEntry entryToDelete = JunitTestHelper.deployBasicCourse(identity);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		Long resourceKeyToDelete = entryToDelete.getOlatResource().getKey();
		Long resourceKey = entry.getOlatResource().getKey();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		File certificateFile = new File(certificateUrl.toURI());
		//certificate linked to the course which will be deleted
		Certificate certificateDeletedCourse = certificatesManager.uploadCertificate(identity, cal.getTime(), null, null, entryToDelete.getOlatResource(), certificateFile);
		Assert.assertNotNull(certificateDeletedCourse);
		//certificate of the staying course
		Certificate certificate = certificatesManager.uploadCertificate(identity, cal.getTime(), null, null, entry.getOlatResource(), certificateFile);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		//delete the course
		Roles roles = Roles.administratorRoles();
		repositoryService.deletePermanently(entryToDelete, identity, roles, Locale.ENGLISH);
		dbInstance.commitAndCloseSession();
		
		//retrieve the certificate of the deleted course
		Certificate reloadedCertificateDeletedCourse = certificatesManager.getCertificateById(certificateDeletedCourse.getKey());
		Assert.assertNotNull(reloadedCertificateDeletedCourse);
		Assert.assertEquals(certificateDeletedCourse, reloadedCertificateDeletedCourse);
		Assert.assertNotNull(reloadedCertificateDeletedCourse.getArchivedResourceKey());
		Assert.assertNull(((CertificateImpl)reloadedCertificateDeletedCourse).getOlatResource());
		Assert.assertEquals(resourceKeyToDelete, reloadedCertificateDeletedCourse.getArchivedResourceKey());
		
		//retrieve the certificate of the staying course
		Certificate reloadedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadedCertificate);
		Assert.assertEquals(certificate, reloadedCertificate);
		Assert.assertNotNull(reloadedCertificate.getArchivedResourceKey());
		Assert.assertEquals(resourceKey, reloadedCertificate.getArchivedResourceKey());
		Assert.assertEquals(entry.getOlatResource(), ((CertificateImpl)reloadedCertificate).getOlatResource());
	}
	
	
	private void waitCertificate(Long certificateKey) {
		//wait until the certificate is created
		waitForCondition(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				Certificate reloadedCertificate = certificatesManager.getCertificateById(certificateKey);
				return CertificateStatus.ok.equals(reloadedCertificate.getStatus());
			}
		}, 30000);
	}
}
