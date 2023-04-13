/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailPackage;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuditLog;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Apr 12, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RepositoryEntryAuditLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryAuditLogDAO repositoryEntryAuditLogDAO;

	@Test
	public void getAuditLogs_byOwner() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2-");

		// make ID to a learning resource owner and thus subscribe id
		IdentitiesAddEvent iae = new IdentitiesAddEvent(identity);
		repositoryManager.addOwners(identity, iae, entry, new MailPackage(false));

		String before = repositoryEntryAuditLogDAO.toXml(entry);

		repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);

		// reload updated repoEntry and assert that status was changed
		RepositoryEntry reloadedEntry = repositoryManager.lookupRepositoryEntry(entry.getKey());
		assertThat(reloadedEntry.getStatus()).isEqualTo(RepositoryEntryStatusEnum.preparation.name());

		String after = repositoryEntryAuditLogDAO.toXml(reloadedEntry);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, identity);
		dbInstance.commitAndCloseSession();

		RepositoryEntryAuditLogSearchParams repositoryEntryAuditLogSearchParams = new RepositoryEntryAuditLogSearchParams();
		repositoryEntryAuditLogSearchParams.setOwner(identity);

		// load the audit log
		List<RepositoryEntryAuditLog> auditLogs = repositoryEntryAuditLogDAO.getAuditLogs(repositoryEntryAuditLogSearchParams);
		Assert.assertEquals(1, auditLogs.size());
	}

	@Test
	public void getAuditLogs_byExcludedAuthor() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity excludedAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-3-");

		String before = repositoryEntryAuditLogDAO.toXml(entry);

		repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);

		// reload updated repoEntry and assert that status was changed
		RepositoryEntry reloadedEntry = repositoryManager.lookupRepositoryEntry(entry.getKey());
		assertThat(reloadedEntry.getStatus()).isEqualTo(RepositoryEntryStatusEnum.preparation.name());

		String after = repositoryEntryAuditLogDAO.toXml(reloadedEntry);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, excludedAuthor);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, author);
		dbInstance.commitAndCloseSession();

		RepositoryEntryAuditLogSearchParams repositoryEntryAuditLogSearchParams = new RepositoryEntryAuditLogSearchParams();
		repositoryEntryAuditLogSearchParams.setExlcudedAuthor(excludedAuthor);

		// load the audit log
		List<RepositoryEntryAuditLog> auditLogs = repositoryEntryAuditLogDAO.getAuditLogs(repositoryEntryAuditLogSearchParams);
		// assert that none of the retrieved auditLogs was authored by the exlucdedAuthor
		Assert.assertTrue(auditLogs.stream().noneMatch(al -> Objects.equals(al.getAuthorKey(), excludedAuthor.getKey())));
	}

	@Test
	public void getAuditLogs_byAfterCreationDate() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2-");

		String before = repositoryEntryAuditLogDAO.toXml(entry);

		repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);

		// reload updated repoEntry and assert that status was changed
		RepositoryEntry reloadedEntry = repositoryManager.lookupRepositoryEntry(entry.getKey());
		assertThat(reloadedEntry.getStatus()).isEqualTo(RepositoryEntryStatusEnum.preparation.name());

		String after = repositoryEntryAuditLogDAO.toXml(reloadedEntry);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, identity);
		dbInstance.commitAndCloseSession();

		RepositoryEntryAuditLogSearchParams repositoryEntryAuditLogSearchParams = new RepositoryEntryAuditLogSearchParams();
		repositoryEntryAuditLogSearchParams.setUntilCreationDate(new Date());

		// load the audit log
		List<RepositoryEntryAuditLog> auditLogs = repositoryEntryAuditLogDAO.getAuditLogs(repositoryEntryAuditLogSearchParams);
		// should be empty because untilCreationDate is set to a date after the logged creationDate
		Assert.assertTrue(auditLogs.isEmpty());

		// Date in past, for retrieving auditLogs
		Calendar calNow = Calendar.getInstance();
		calNow.add(Calendar.SECOND, -1);
		Date date = calNow.getTime();

		repositoryEntryAuditLogSearchParams.setUntilCreationDate(date);
		// load the audit log
		auditLogs = repositoryEntryAuditLogDAO.getAuditLogs(repositoryEntryAuditLogSearchParams);
		// should not be empty because untilCreationDate is set to a date before the logged creationDate
		Assert.assertFalse(auditLogs.isEmpty());
	}

	@Test
	public void getAuditLogs_byAllSearchParams() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity excludedAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-3-");

		// make ID to a learning resource owner and thus subscribe id
		IdentitiesAddEvent iae = new IdentitiesAddEvent(author);
		repositoryManager.addOwners(author, iae, entry, new MailPackage(false));

		String before = repositoryEntryAuditLogDAO.toXml(entry);

		repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);

		// reload updated repoEntry and assert that status was changed
		RepositoryEntry reloadedEntry = repositoryManager.lookupRepositoryEntry(entry.getKey());
		assertThat(reloadedEntry.getStatus()).isEqualTo(RepositoryEntryStatusEnum.preparation.name());

		String after = repositoryEntryAuditLogDAO.toXml(reloadedEntry);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, excludedAuthor);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, author);
		dbInstance.commitAndCloseSession();

		// Date in past, for retrieving auditLogs
		Calendar calNow = Calendar.getInstance();
		calNow.add(Calendar.SECOND, -1);
		Date date = calNow.getTime();

		RepositoryEntryAuditLogSearchParams repositoryEntryAuditLogSearchParams = new RepositoryEntryAuditLogSearchParams();
		repositoryEntryAuditLogSearchParams.setOwner(author);
		repositoryEntryAuditLogSearchParams.setExlcudedAuthor(excludedAuthor);
		repositoryEntryAuditLogSearchParams.setUntilCreationDate(date);

		// load the audit log
		List<RepositoryEntryAuditLog> auditLogs = repositoryEntryAuditLogDAO.getAuditLogs(repositoryEntryAuditLogSearchParams);
		Assert.assertTrue(auditLogs.stream().noneMatch(al -> Objects.equals(al.getAuthorKey(), excludedAuthor.getKey())));
		Assert.assertEquals(1, auditLogs.size());
	}

	@Test
	public void getAuditLogs_byNoSearchParams() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity excludedAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-3-");

		// make ID to a learning resource owner and thus subscribe id
		IdentitiesAddEvent iae = new IdentitiesAddEvent(author);
		repositoryManager.addOwners(author, iae, entry, new MailPackage(false));

		String before = repositoryEntryAuditLogDAO.toXml(entry);

		repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);

		// reload updated repoEntry and assert that status was changed
		RepositoryEntry reloadedEntry = repositoryManager.lookupRepositoryEntry(entry.getKey());
		assertThat(reloadedEntry.getStatus()).isEqualTo(RepositoryEntryStatusEnum.preparation.name());

		String after = repositoryEntryAuditLogDAO.toXml(reloadedEntry);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, excludedAuthor);
		repositoryEntryAuditLogDAO.auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, author);
		dbInstance.commitAndCloseSession();

		RepositoryEntryAuditLogSearchParams repositoryEntryAuditLogSearchParams = new RepositoryEntryAuditLogSearchParams();

		// load the audit log
		List<RepositoryEntryAuditLog> auditLogs = repositoryEntryAuditLogDAO.getAuditLogs(repositoryEntryAuditLogSearchParams);
		Assert.assertTrue(auditLogs.stream().anyMatch(al -> Objects.equals(al.getAuthorKey(), excludedAuthor.getKey())));
		Assert.assertTrue(auditLogs.size() >= 2);
	}
}