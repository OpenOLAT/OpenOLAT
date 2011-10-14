/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.course.auditing;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import static org.junit.Assert.*;

/**
 * @author Christian Guretzki
 */
public class UserNodeAuditManagerTest extends OlatTestCase  {
	
	private static Logger log = Logger.getLogger(UserNodeAuditManagerTest.class.getName());
	
	private ICourse course;

	
	@Before
	public void setUp() throws Exception {		
		try {
			log.info("setUp start ------------------------");
			
      //import "Demo course" into the bcroot_junittest
			RepositoryEntry repositoryEntry = JunitTestHelper.deployDemoCourse();
			Long resourceableId = repositoryEntry.getOlatResource().getResourceableId();
			System.out.println("Demo course imported - resourceableId: " + resourceableId);
					
			course = CourseFactory.loadCourse(resourceableId);
			DBFactory.getInstance().closeSession();
						
			log.info("setUp done ------------------------");					
		} catch (RuntimeException e) {
			log.error("Exception in setUp(): " + e);
			e.printStackTrace();
		}
	}

	//@After
	public void tearDown() throws Exception {			
		//remove demo course on file system
		//FIXME: this does not remove all data from the database, see repositoryManger
		CourseFactory.deleteCourse(course);		
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}
	
	/**
	 * 
	 *
	 */
	@Test public void testCreateLimitedLogContent() {
		System.out.println("Start testCreateLimitedLogContent");
		assertNotNull(course);
		UserNodeAuditManagerImpl userNodeAuditManagerImpl = new UserNodeAuditManagerImpl(course);
		StringBuilder logContent = new StringBuilder();
		logContent.append( createTestLogContent(1) );
		String limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertEquals("logContent should not be limited", logContent.toString(), limitedLogContent);
		System.out.println("limitedLogContent:\n" + limitedLogContent);
		System.out.println("limitedLogContent.length=" + limitedLogContent.length());

		logContent.append( createTestLogContent(2) );
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertEquals("logContent should not be limited", logContent.toString(), limitedLogContent);
		System.out.println("limitedLogContent:\n" + limitedLogContent);
		System.out.println("limitedLogContent.length=" + limitedLogContent.length());

		logContent.append( createTestLogContent(3) );
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertEquals("logContent should not be limited", logContent.toString(), limitedLogContent);
		System.out.println("limitedLogContent:\n" + limitedLogContent);
		System.out.println("limitedLogContent.length=" + limitedLogContent.length());

		logContent.append( createTestLogContent(4) );
		System.out.println("logContent.length()=" + logContent.length());
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertTrue("limitedLogContent same size like input, probably not limited", logContent.length() != limitedLogContent.length());
		assertTrue("logContent should not be limited", limitedLogContent.startsWith(UserNodeAuditManagerImpl.LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES));
		assertTrue("Missing Log entry2",limitedLogContent.contains("LogEntry #2"));
		assertTrue("Missing Log entry3",limitedLogContent.contains("LogEntry #3"));
		assertTrue("Missing Log entry4",limitedLogContent.contains("LogEntry #4"));
		System.out.println("limitedLogContent:\n" + limitedLogContent);
		System.out.println("limitedLogContent.length=" + limitedLogContent.length());

		logContent.append( createTestLogContent(5) );
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertTrue("limitedLogContent same size like input, probably not limited", logContent.length() != limitedLogContent.length());
		assertTrue("logContent should not be limited", limitedLogContent.startsWith(UserNodeAuditManagerImpl.LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES));
		assertTrue("Missing Log entry3",limitedLogContent.contains("LogEntry #3"));
		assertTrue("Missing Log entry4",limitedLogContent.contains("LogEntry #4"));
		assertTrue("Missing Log entry5",limitedLogContent.contains("LogEntry #5"));
		System.out.println("limitedLogContent:\n" + limitedLogContent);
		System.out.println("limitedLogContent.length=" + limitedLogContent.length());

	}

	private String createTestLogContent(int entryNumber) {
		StringBuffer sb = new StringBuffer();
		sb.append(UserNodeAuditManagerImpl.LOG_DELIMITER);
		sb.append("Date: xxxxxxx\n");
		sb.append("User: yyyyyyy\n");
		sb.append("LogEntry #");
		sb.append(entryNumber);
		sb.append("\n");
		return sb.toString();
	}
	
	
}
