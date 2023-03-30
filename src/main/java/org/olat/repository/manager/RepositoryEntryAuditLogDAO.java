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

import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryAuditLog;
import org.olat.repository.model.RepositoryEntryAuditLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: Mär 15, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class RepositoryEntryAuditLogDAO {

	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryAuditLogDAO.class);

	@Autowired
	private DB dbInstance;

	private static final XStream repositoryEntryXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(repositoryEntryXStream);
		repositoryEntryXStream.ignoreUnknownElements();

		repositoryEntryXStream.alias("repositoryEntry", RepositoryEntry.class);
	}

	public void auditLog(RepositoryEntryAuditLog.Action action, String before, String after,
						 RepositoryEntryRef entry, IdentityRef author) {
		RepositoryEntryAuditLogImpl auditLog = new RepositoryEntryAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action.name());
		auditLog.setBefore(before);
		auditLog.setAfter(after);

		if (entry != null) {
			auditLog.setEntryKey(entry.getKey());
		}
		if (author != null) {
			auditLog.setAuthorKey(author.getKey());
		}

		dbInstance.getCurrentEntityManager().persist(auditLog);
	}

	public List<RepositoryEntryAuditLog> getAuditLogs(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select log from repositoryentryauditlog log where log.authorKey!=:authorKey order by creationDate asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryAuditLog.class)
				.setParameter("authorKey", identity.getKey())
				.getResultList();
	}

	public String toXml(RepositoryEntry repositoryEntry) {
		if (repositoryEntry == null) return null;
		return repositoryEntryXStream.toXML(repositoryEntry);
	}

	public RepositoryEntry repositoryEntryFromXml(String xml) {
		if (StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = repositoryEntryXStream.fromXML(xml);
				if (obj instanceof RepositoryEntry repositoryEntry) {
					return repositoryEntry;
				}
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
		return null;
	}
}
