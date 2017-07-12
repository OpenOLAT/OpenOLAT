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
package org.olat.modules.lecture.manager;

import java.util.Date;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private LectureBlockAuditLogDAO lectureBlockAuditLogDao;
	
	@Test
	public void createAuditLog() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-1-");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-1-");
		lectureBlockAuditLogDao.auditLog(LectureBlockAuditLog.Action.createLectureBlock, "3", "4", "Update absence", null, null,entry, identity, author);
		dbInstance.commitAndCloseSession();
	}
	
	
	@Test
	public void xmlAuditLog() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		String xml = lectureBlockAuditLogDao.toXml(lectureBlock);
		System.out.println(xml);
		
	}

}
