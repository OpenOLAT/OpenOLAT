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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.course.auditing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Christian Guretzki
 */
public class UserNodeAuditManagerTest extends OlatTestCase  {

	private static final Logger log = Tracing.createLoggerFor(UserNodeAuditManagerTest.class);
	
	@Autowired
	private DB dbInstance;
	
	@Test
	public void appendToUserNodeLog() {
		//import a course
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("Auth-");
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Logger-");
		RepositoryEntry repositoryEntry = JunitTestHelper.deployDemoCourse(author);
		Long resourceableId = repositoryEntry.getOlatResource().getResourceableId();
		ICourse course = CourseFactory.loadCourse(resourceableId);
		dbInstance.commitAndCloseSession();
		
		CourseNode courseNode = course.getRunStructure().getRootNode();
		UserNodeAuditManager userNodeAuditManager = course.getCourseEnvironment().getAuditManager();
		userNodeAuditManager.appendToUserNodeLog(courseNode, author, assessedIdentity, "Hello", Role.coach);
		dbInstance.commitAndCloseSession();

		userNodeAuditManager.appendToUserNodeLog(courseNode, assessedIdentity, assessedIdentity, " world", Role.user);
		dbInstance.commitAndCloseSession();
		
		String logText = userNodeAuditManager.getUserNodeLog(courseNode, assessedIdentity);
		Assert.assertTrue(logText.contains("Hello"));
		Assert.assertTrue(logText.contains("world"));
	}
	
	@Test
	public void testCreateLimitedLogContent() {
		//import a course
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("Auth-");
		RepositoryEntry repositoryEntry = JunitTestHelper.deployDemoCourse(author);
		Long resourceableId = repositoryEntry.getOlatResource().getResourceableId();
		log.info("Demo course imported - resourceableId: " + resourceableId);	
		ICourse course = CourseFactory.loadCourse(resourceableId);
		dbInstance.commitAndCloseSession();

		
		log.info("Start testCreateLimitedLogContent");
		assertNotNull(course);
		UserNodeAuditManagerImpl userNodeAuditManagerImpl = new UserNodeAuditManagerImpl(course);
		StringBuilder logContent = new StringBuilder();
		logContent.append( createTestLogContent(1) );
		String limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertEquals("logContent should not be limited", logContent.toString(), limitedLogContent);
		log.info("limitedLogContent: {}", limitedLogContent);
		log.info("limitedLogContent.length= {}", limitedLogContent.length());

		logContent.append( createTestLogContent(2) );
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertEquals("logContent should not be limited", logContent.toString(), limitedLogContent);
		log.info("limitedLogContent: {}", limitedLogContent);
		log.info("limitedLogContent.length={}", limitedLogContent.length());

		logContent.append( createTestLogContent(3) );
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertEquals("logContent should not be limited", logContent.toString(), limitedLogContent);
		log.info("limitedLogContent: {}", limitedLogContent);
		log.info("limitedLogContent.length={}", limitedLogContent.length());

		logContent.append( createTestLogContent(4) );
		log.info("logContent.length()=" + logContent.length());
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertTrue("limitedLogContent same size like input, probably not limited", logContent.length() != limitedLogContent.length());
		assertTrue("logContent should not be limited", limitedLogContent.startsWith(UserNodeAuditManagerImpl.LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES));
		assertTrue("Missing Log entry2",limitedLogContent.contains("LogEntry #2"));
		assertTrue("Missing Log entry3",limitedLogContent.contains("LogEntry #3"));
		assertTrue("Missing Log entry4",limitedLogContent.contains("LogEntry #4"));
		log.info("limitedLogContent: {}", limitedLogContent);
		log.info("limitedLogContent.length={}", limitedLogContent.length());

		logContent.append( createTestLogContent(5) );
		limitedLogContent = userNodeAuditManagerImpl.createLimitedLogContent(logContent.toString(), 400);
		assertTrue("limitedLogContent same size like input, probably not limited", logContent.length() != limitedLogContent.length());
		assertTrue("logContent should not be limited", limitedLogContent.startsWith(UserNodeAuditManagerImpl.LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES));
		assertTrue("Missing Log entry3",limitedLogContent.contains("LogEntry #3"));
		assertTrue("Missing Log entry4",limitedLogContent.contains("LogEntry #4"));
		assertTrue("Missing Log entry5",limitedLogContent.contains("LogEntry #5"));
		log.info("limitedLogContent: {}", limitedLogContent);
		log.info("limitedLogContent.length={}", limitedLogContent.length());
	}

	private String createTestLogContent(int entryNumber) {
		StringBuilder sb = new StringBuilder();
		sb.append(UserNodeAuditManagerImpl.LOG_DELIMITER)
		  .append("Date: xxxxxxx\n")
		  .append("User: yyyyyyy\n")
		  .append("LogEntry #")
		  .append(entryNumber)
		  .append("\n");
		return sb.toString();
	}
}