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
package org.olat.modules.ceditor.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.ContentAuditLog;
import org.olat.modules.ceditor.ContentAuditLog.Action;
import org.olat.modules.ceditor.Page;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private ContentAuditLogDAO contentAuditLogDao;
	
	@Test
	public void createLog() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("c-audit-log-1");
		ContentAuditLog auditLog = contentAuditLogDao.create(Action.CREATE, null, identity);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(auditLog);
		Assert.assertNotNull(auditLog.getKey());
		Assert.assertNotNull(auditLog.getCreationDate());
		Assert.assertNotNull(auditLog.getLastModified());
		Assert.assertEquals(Action.CREATE, auditLog.getAction());
		Assert.assertEquals(identity, auditLog.getDoer());
	}
	
	@Test
	public void lastChange() {
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, null, null);
		dbInstance.commitAndCloseSession();
		
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("c-audit-log-2");
		ContentAuditLog auditLog = contentAuditLogDao.create(Action.CREATE, page, identity);
		dbInstance.commitAndCloseSession();
		
		ContentAuditLog lastChange = contentAuditLogDao.lastChange(page);
		Assert.assertNotNull(lastChange);
		Assert.assertEquals(auditLog, lastChange);
		Assert.assertEquals(Action.CREATE, auditLog.getAction());
	}

}
