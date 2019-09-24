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
package org.olat.course.nodes.en;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR/>
 * Test the enrollment
 * <P/> Initial Date: Jul 28, 2004
 * 
 * @author patrick
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EnrollmentManagerConcurrentTest extends OlatTestCase implements WindowControl {
	//
	private static final Logger log = Tracing.createLoggerFor(EnrollmentManagerConcurrentTest.class);
	/*
	 * ::Test Setup::
	 */
	private static Identity id1;
	// For WaitingGroup tests
	private static Identity wg1, wg2,wg3;
	private static Roles wg1Roles, wg2Roles, wg3Roles;
	
	
		// For WaitingGroup tests
	private static Translator testTranslator = null;
	private static BusinessGroup bgWithWaitingList = null;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EnrollmentManager enrollmentManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private DB dbInstance;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup() throws Exception {
			// Identities
			id1 =  JunitTestHelper.createAndPersistIdentityAsUser("id1");
			DBFactory.getInstance().closeSession();				
			// create business-group with waiting-list
			String bgWithWaitingListName = "Group with WaitingList";
			String bgWithWaitingListDesc = "some short description for Group with WaitingList";
			Boolean enableWaitinglist = new Boolean(true);
			Boolean enableAutoCloseRanks = new Boolean(true);
			RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
			log.info("testAddToWaitingListAndFireEvent: resource=" + resource);
			bgWithWaitingList = businessGroupService.createBusinessGroup(id1, bgWithWaitingListName,
					bgWithWaitingListDesc, -1, -1, enableWaitinglist, enableAutoCloseRanks, resource);
			bgWithWaitingList.setMaxParticipants(new Integer(2));
			log.info("TEST bgWithWaitingList=" + bgWithWaitingList);
			log.info("TEST bgWithWaitingList.getMaxParticipants()=" + bgWithWaitingList.getMaxParticipants() );
			log.info("TEST bgWithWaitingList.getWaitingListEnabled()=" + bgWithWaitingList.getWaitingListEnabled() );
			// create mock objects
			testTranslator = Util.createPackageTranslator(EnrollmentManagerConcurrentTest.class, new Locale("de"));
			// Identities
			wg1 = JunitTestHelper.createAndPersistIdentityAsUser("wg1");
			wg1Roles = securityManager.getRoles(wg1);
			wg2 = JunitTestHelper.createAndPersistIdentityAsUser("wg2");
			wg2Roles = securityManager.getRoles(wg2);
			wg3 = JunitTestHelper.createAndPersistIdentityAsUser("wg3");
			wg3Roles = securityManager.getRoles(wg3);
			DBFactory.getInstance().closeSession();	
			
	}


	// Test for WaitingList
	///////////////////////
	/**
	 * Enroll 3 identities (group with max-size=2 and waiting-list).
	 * Cancel enrollment. Check size after each step.
	 */
	@Test
	public void testEnroll() throws Exception {
		log.info("testEnroll: start...");
		ENCourseNode enNode = new ENCourseNode();

		OLATResource resource = resourceManager.createOLATResourceInstance(CourseModule.class);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry addedEntry = repositoryService.create(null, "Ayanami", "-", "Enrollment test course 1", "A JUnit course",
				resource, RepositoryEntryStatusEnum.trash, defOrganisation);
		CourseEnvironment cenv = CourseFactory.createCourse(addedEntry, "Test", "Test", "learningObjectives").getCourseEnvironment();
		// 1. enroll wg1 user
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(wg1);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
		CoursePropertyManager coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		log.info("enrollmentManager=" + enrollmentManager);
		log.info("bgWithWaitingList=" + bgWithWaitingList);
		assertTrue("bgWithWaitingList is null",bgWithWaitingList != null);
		log.info("userCourseEnv=" + userCourseEnv);
		log.info("userCourseEnv.getCourseEnvironment()=" + userCourseEnv.getCourseEnvironment());
		enrollmentManager.doEnroll(wg1, wg1Roles, bgWithWaitingList, enNode, coursePropertyManager,this /*WindowControl mock*/,testTranslator,
				new ArrayList<Long>()/*enrollableGroupNames*/, new ArrayList<Long>()/*enrollableAreaNames*/, userCourseEnv.getCourseEnvironment().getCourseGroupManager());	
		assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1,bgWithWaitingList));	
		int participantsCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.participant.name());
		assertTrue("Wrong number of participants," + participantsCounter , participantsCounter == 1);
		// 2. enroll wg2 user
		ienv = new IdentityEnvironment();
		ienv.setIdentity(wg2);
		userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
		coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		enrollmentManager.doEnroll(wg2, wg2Roles, bgWithWaitingList, enNode, coursePropertyManager,this /*WindowControl mock*/,testTranslator,
				new ArrayList<Long>()/*enrollableGroupNames*/, new ArrayList<Long>()/*enrollableAreaNames*/, userCourseEnv.getCourseEnvironment().getCourseGroupManager());	
		assertTrue("Enrollment failed, user='wg2'", businessGroupService.isIdentityInBusinessGroup(wg2,bgWithWaitingList));	
		assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1,bgWithWaitingList));	
		participantsCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.participant.name());
		assertTrue("Wrong number of participants," + participantsCounter , participantsCounter == 2);
		// 3. enroll wg3 user => list is full => waiting-list
		ienv = new IdentityEnvironment();
		ienv.setIdentity(wg3);
		userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
		coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		enrollmentManager.doEnroll(wg3, wg3Roles, bgWithWaitingList, enNode, coursePropertyManager,this /*WindowControl mock*/,testTranslator,
				new ArrayList<Long>()/*enrollableGroupNames*/, new ArrayList<Long>()/*enrollableAreaNames*/, userCourseEnv.getCourseEnvironment().getCourseGroupManager());		
		assertFalse("Wrong enrollment, user='wg3' is in PartipiciantGroup, must be on waiting-list", businessGroupService.isIdentityInBusinessGroup(wg3,bgWithWaitingList));	
		assertFalse("Wrong enrollment, user='wg3' is in PartipiciantGroup, must be on waiting-list", businessGroupService.hasRoles(wg3, bgWithWaitingList, GroupRoles.participant.name()));
		assertTrue("Wrong enrollment, user='wg3' must be on waiting-list", businessGroupService.hasRoles(wg3, bgWithWaitingList, GroupRoles.waiting.name()));
		assertTrue("Enrollment failed, user='wg2'", businessGroupService.isIdentityInBusinessGroup(wg2,bgWithWaitingList));	
		assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1,bgWithWaitingList));	
		participantsCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.participant.name());
		assertTrue("Wrong number of participants," + participantsCounter , participantsCounter == 2);
		int waitingListCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.waiting.name());
		assertTrue("Wrong number of waiting-list, must be 1, is " + waitingListCounter , waitingListCounter == 1);
		// cancel enrollment for wg2 => transfer wg3 from waiting-list to participants
		ienv = new IdentityEnvironment();
		ienv.setIdentity(wg2);
		userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
		coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		enrollmentManager.doCancelEnrollment(wg2,bgWithWaitingList, enNode, coursePropertyManager,this /*WindowControl mock*/,testTranslator);		
		assertFalse("Cancel enrollment failed, user='wg2' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg2,bgWithWaitingList));	
		assertTrue("Enrollment failed, user='wg3'", businessGroupService.isIdentityInBusinessGroup(wg3,bgWithWaitingList));	
		assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1,bgWithWaitingList));	
		participantsCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.participant.name());
		assertTrue("Wrong number of participants, must be 2, is " + participantsCounter , participantsCounter == 2);
		waitingListCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.waiting.name());
		assertTrue("Wrong number of waiting-list, must be 0, is " + waitingListCounter , waitingListCounter == 0);
		// cancel enrollment for wg1 
		ienv = new IdentityEnvironment();
		ienv.setIdentity(wg1);
		userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
		coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		enrollmentManager.doCancelEnrollment(wg1,bgWithWaitingList, enNode, coursePropertyManager,this /*WindowControl mock*/,testTranslator);		
		assertFalse("Cancel enrollment failed, user='wg2' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg2,bgWithWaitingList));	
		assertFalse("Cancel enrollment failed, user='wg1' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg1,bgWithWaitingList));	
		assertTrue("Enrollment failed, user='wg3'", businessGroupService.isIdentityInBusinessGroup(wg3,bgWithWaitingList));	
		participantsCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.participant.name());
		assertTrue("Wrong number of participants, must be 1, is " + participantsCounter , participantsCounter == 1);
		waitingListCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.waiting.name());
		assertTrue("Wrong number of waiting-list, must be 0, is " + waitingListCounter , waitingListCounter == 0);
		// cancel enrollment for wg3 
		ienv = new IdentityEnvironment();
		ienv.setIdentity(wg3);
		userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
		coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		enrollmentManager.doCancelEnrollment(wg3,bgWithWaitingList, enNode, coursePropertyManager,this /*WindowControl mock*/,testTranslator);		
		assertFalse("Cancel enrollment failed, user='wg3' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg3,bgWithWaitingList));	
		assertFalse("Cancel enrollment failed, user='wg2' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg2,bgWithWaitingList));	
		assertFalse("Cancel enrollment failed, user='wg1' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg1,bgWithWaitingList));	
		participantsCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.participant.name());
		assertTrue("Wrong number of participants, must be 0, is " + participantsCounter , participantsCounter == 0);
		waitingListCounter = businessGroupService.countMembers(bgWithWaitingList, GroupRoles.waiting.name());
		assertTrue("Wrong number of waiting-list, must be 0, is " + waitingListCounter , waitingListCounter == 0);

		log.info("testEnroll: done...");
	}
	
	@Test
	public void testConcurrentEnrollmentWithWaitingList() {
		int numOfUsers = isOracleConfigured() ? 12 : 30;
		List<Identity> ids = new ArrayList<>(numOfUsers);	
		for(int i=0; i<numOfUsers; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("enroll-a-" + i + "-" + UUID.randomUUID().toString());
			ids.add(id);
		}
		
		ENCourseNode enNode = new ENCourseNode();
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("enroller");
		RepositoryEntry addedEntry = JunitTestHelper.deployBasicCourse(author);
		CourseEnvironment cenv = CourseFactory.createCourse(addedEntry, "Test-Enroll", "Test", "Test enrollment with concurrent users").getCourseEnvironment();
		BusinessGroup group = businessGroupService.createBusinessGroup(id1, "Enrollment", "Enroll", new Integer(1), new Integer(10), true, false, null);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();

		final CountDownLatch doneSignal = new CountDownLatch(ids.size());
		EnrollThread[] threads = new EnrollThread[numOfUsers];
		int t = 0;
		for(Identity id:ids) {
			threads[t++] = new EnrollThread(id, addedEntry, group, enNode, cenv, doneSignal);
		}
		
		for(EnrollThread thread:threads) {
			thread.start();
		}
		
		try {
			boolean interrupt = doneSignal.await(360, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		dbInstance.commitAndCloseSession();

		List<Identity> enrolledIds = businessGroupService.getMembers(group, GroupRoles.participant.name());
		Assert.assertNotNull(enrolledIds);
		Assert.assertEquals(10, enrolledIds.size());
		
		List<Identity> waitingIds = businessGroupService.getMembers(group, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingIds);
		Assert.assertEquals(ids.size() - 10, waitingIds.size());
	}
	
	
	@Test @Ignore
	public void testConcurrentEnrollmentWithWaitingList_big() {
		List<Identity> ids = new ArrayList<>(100);	
		for(int i=0; i<100; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("enroll-a-" + i + "-" + UUID.randomUUID().toString());
			ids.add(id);
		}
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("enroller");
		RepositoryEntry addedEntry = JunitTestHelper.deployBasicCourse(author);
		
		ENCourseNode enNode1 = new ENCourseNode();
		ENCourseNode enNode2 = new ENCourseNode();
		ENCourseNode enNode3 = new ENCourseNode();
		ENCourseNode enNode4 = new ENCourseNode();
		ENCourseNode enNode5 = new ENCourseNode();

		CourseEnvironment cenv = CourseFactory.loadCourse(addedEntry).getCourseEnvironment();
		BusinessGroup group1 = businessGroupService.createBusinessGroup(author, "Enrollment 1", "Enroll 1", new Integer(1), new Integer(8), true, true, addedEntry);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(author, "Enrollment 2", "Enroll 2", new Integer(1), new Integer(10), true, true, addedEntry);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(author, "Enrollment 3", "Enroll 3", new Integer(1), new Integer(4), true, true, addedEntry);
		BusinessGroup group4 = businessGroupService.createBusinessGroup(author, "Enrollment 4", "Enroll 4", new Integer(1), new Integer(10), true, true, addedEntry);
		BusinessGroup group5 = businessGroupService.createBusinessGroup(author, "Enrollment 5", "Enroll 5", new Integer(1), new Integer(9), true, true, addedEntry);
		dbInstance.commitAndCloseSession();

		EnrollThread[] threads = new EnrollThread[100];
		
		final CountDownLatch doneSignal = new CountDownLatch(ids.size());
		int t = 0;
		for(int i=0; i<30; i++) {
			threads[t++] = new EnrollThread(ids.get(i), addedEntry, group1, enNode1, cenv, doneSignal);
		}
		for(int i=30; i<50; i++) {
			threads[t++] = new EnrollThread(ids.get(i), addedEntry, group2, enNode2, cenv, doneSignal);
		}
		for(int i=50; i<70; i++) {
			threads[t++] = new EnrollThread(ids.get(i), addedEntry, group3, enNode3, cenv, doneSignal);
		}
		for(int i=70; i<90; i++) {
			threads[t++] = new EnrollThread(ids.get(i), addedEntry, group4, enNode4, cenv, doneSignal);
		}
		for(int i=90; i<100; i++) {
			threads[t++] = new EnrollThread(ids.get(i), addedEntry, group5, enNode5, cenv, doneSignal);
		}
		
		for(EnrollThread thread:threads) {
			thread.start();
		}

		try {
			boolean interrupt = doneSignal.await(360, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		dbInstance.commitAndCloseSession();

		List<Identity> enrolled_1_Ids = businessGroupService.getMembers(group1, GroupRoles.participant.name());
		Assert.assertEquals(8, enrolled_1_Ids.size());
		List<Identity> enrolled_2_Ids = businessGroupService.getMembers(group2, GroupRoles.participant.name());
		Assert.assertEquals(10, enrolled_2_Ids.size());
		List<Identity> enrolled_3_Ids = businessGroupService.getMembers(group3, GroupRoles.participant.name());
		Assert.assertEquals(4, enrolled_3_Ids.size());
		List<Identity> enrolled_4_Ids = businessGroupService.getMembers(group4, GroupRoles.participant.name());
		Assert.assertEquals(10, enrolled_4_Ids.size());
		List<Identity> enrolled_5_Ids = businessGroupService.getMembers(group5, GroupRoles.participant.name());
		Assert.assertEquals(9, enrolled_5_Ids.size());
	}

	private class EnrollThread extends Thread {
		private final ENCourseNode enNode;
		private final Identity identity;
		private final CourseEnvironment cenv;
		private final BusinessGroup group;
		private final RepositoryEntry courseEntry;
		private final CountDownLatch doneSignal;
		
		public EnrollThread(Identity identity, RepositoryEntry courseEntry, BusinessGroup group,
				ENCourseNode enNode, CourseEnvironment cenv, CountDownLatch doneSignal) {
			this.enNode = enNode;
			this.group = group;
			this.courseEntry = courseEntry;
			this.identity = identity;
			this.cenv = cenv;
			this.doneSignal = doneSignal;
		}

		@Override
		public void run() {
			try {
				UserSession session = new UserSession();
				session.setIdentity(identity);
				session.setSessionInfo(new SessionInfo(identity.getKey(), identity.getName()));
				ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(session);
				
				IdentityEnvironment ienv = new IdentityEnvironment();
				ienv.setIdentity(identity);
				UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
				CoursePropertyManager coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
				CourseGroupManager courseGroupManager = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
				
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(courseEntry.getOlatResource(), OlatResourceableType.course));
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(enNode));
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));

				sleep(Math.round(new Random().nextDouble() * 100l));
				enrollmentManager.doEnroll(identity, Roles.userRoles(), group, enNode, coursePropertyManager, EnrollmentManagerConcurrentTest.this /*WindowControl mock*/, testTranslator,
						new ArrayList<Long>()/*enrollableGroupNames*/, new ArrayList<Long>()/*enrollableAreaNames*/, courseGroupManager);
				DBFactory.getInstance().commit();
			} catch (Exception e) {
				log.error("", e);
			}	finally {
				ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
				DBFactory.getInstance().commitAndCloseSession();
				doneSignal.countDown();
			}
		}
	}

	
	// Implements interface WindowControl
  /////////////////////////////////////
	public void pushToMainArea(Component comp){}
	public void pushAsModalDialog(Component comp){}
	@Override
	public void pushFullScreen(Controller ctrl, String bodyClass) {/* */}
	@Override
	public void pushAsCallout(Component comp, String targetId, CalloutSettings settings){}
	public void pop(){}
	public void setInfo(String string){}
	public void setError(String string){}
	public void setWarning(String string){}
	public DTabs getDTabs(){return null;}
	public WindowControlInfo getWindowControlInfo(){return null;}
	public void makeFlat(){}
	public BusinessControl getBusinessControl() {
		
		BusinessControl control = new BusinessControl() {

			@Override
			public String getAsString() {
				return null;
			}

			@Override
			public List<ContextEntry> getEntries() {
				return Collections.<ContextEntry>emptyList();
			}
			
			@Override
			public List<ContextEntry> getEntriesDownTheControls() {
				return Collections.<ContextEntry>emptyList();
			}

			@Override
			public ContextEntry popLauncherContextEntry() {
				return null;
			}

			@Override
			public ContextEntry getCurrentContextEntry() {
				return null;
			}

			@Override
			public void setCurrentContextEntry(ContextEntry cw) {
				//
			}

			@Override
			public void dropLauncherEntries() {
				//
			}

			@Override
			public boolean hasContextEntry() {
				return false;
			}
			
		};
		
		return control;
		
	}

	public WindowBackOffice getWindowBackOffice() {
		return null;
	};
}