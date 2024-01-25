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
package org.olat.repository.manager;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAuditLogDAOXStreamTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryAuditLogDAOXStreamTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryServiceImpl repositoryService;
	
	@Autowired
	private RepositoryEntryAuditLogDAO repositoryEntryAuditLogDao;
	
	@Test
	public void readXmlAuditLogV1724() {
		String xml = readXml("repositoryentry_auditlog_1724.xml");
		RepositoryEntry entry = repositoryEntryAuditLogDao.repositoryEntryFromXml(xml);
		Assert.assertNotNull(entry);
		Assert.assertEquals("XML Course", entry.getDisplayname());
		Assert.assertEquals(RepositoryEntryStatusEnum.published, entry.getEntryStatus());
	}
	
	@Test
	public void readXmlAuditLogV1725() {
		String xml = readXml("repositoryentry_auditlog_1725.xml");
		RepositoryEntry entry = repositoryEntryAuditLogDao.repositoryEntryFromXml(xml);
		Assert.assertNotNull(entry);
		Assert.assertEquals("XML Course", entry.getDisplayname());
		Assert.assertEquals(RepositoryEntryStatusEnum.coachpublished, entry.getEntryStatus());
	}
	
	@Test
	public void writeReadAuditLog() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("xml");
		
		String displayName = "XML Entry";
		String description = "XML Entry";
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "xml-entry", displayName, description, null,
				RepositoryEntryStatusEnum.published, RepositoryEntryRuntimeType.embedded, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry realodedRe = repositoryService.loadBy(re);
		String xml = repositoryEntryAuditLogDao.toXml(realodedRe);
		RepositoryEntry xmlRe = repositoryEntryAuditLogDao.repositoryEntryFromXml(xml);
		
		Assert.assertNotNull(xmlRe);
		Assert.assertEquals("XML Entry", xmlRe.getDisplayname());
		Assert.assertEquals(RepositoryEntryStatusEnum.published, xmlRe.getEntryStatus());
	}
	
	private String readXml(String filename) {
		try(InputStream inStream = RepositoryEntryAuditLogDAOXStreamTest.class.getResourceAsStream(filename)) {
			return IOUtils.toString(inStream, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Cannot read xml", e);
			return null;
		}
	}
}
