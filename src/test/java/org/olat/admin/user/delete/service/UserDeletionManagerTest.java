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

package org.olat.admin.user.delete.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description: <br>
 * 
 * @author Christian Guretzki
 */
public class UserDeletionManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserDeletionManager userDeletionManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseDisclaimerManager courseDisclaimerManager;
	
	@Test
	public void deleteIdentity() {
		String username = "id-to-del-" + UUID.randomUUID();
		String email = username + "@frentix.com";
		User user = userManager.createUser("first" + username, "last" + username, email);
		user.setProperty(UserConstants.COUNTRY, "");
		user.setProperty(UserConstants.CITY, "Basel");
		user.setProperty(UserConstants.INSTITUTIONALNAME, "Del-23");
		user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "Del-24");
		Identity identity = securityManager.createAndPersistIdentityAndUser(username, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username, "secret");
		dbInstance.commitAndCloseSession();
		// add some stuff
		
		//a group
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "Group", "Group", -1, -1, false, false, null);
		Assert.assertNotNull(group);
		dbInstance.commit();
		//a course
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(identity);
		dbInstance.commitAndCloseSession();
		//a course disclaimer
		OLATResourceable courseOres = course.getOlatResource();
		CourseConfig courseConfig = CourseFactory.openCourseEditSession(courseOres.getResourceableId()).getCourseEnvironment().getCourseConfig();
		courseConfig.setDisclaimerEnabled(1, true);
		courseConfig.setDisclaimerEnabled(2, true);
		CourseFactory.setCourseConfig(courseOres.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(courseOres.getResourceableId());
		CourseFactory.closeCourseEditSession(courseOres.getResourceableId(), true);
		//a consent to the disclaimer
		courseDisclaimerManager.acceptDisclaimer(course, identity, true, true);
		
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(course, identity));		
		Assert.assertEquals(username, course.getInitialAuthor());
		Assert.assertTrue(repositoryService.hasRoleExpanded(identity, GroupRoles.owner.name()));
		assertThat(courseDisclaimerManager.getConsents(course)).hasSize(1);
		
		//delete the identity
		userDeletionManager.deleteIdentity(identity, null);
		dbInstance.commit();

		//check
		Identity deletedIdentity = securityManager.loadIdentityByKey(identity.getKey());
		Assert.assertNotNull(deletedIdentity);
		
		//check membership of group
		boolean isMember = businessGroupService.isIdentityInBusinessGroup(deletedIdentity, group);
		Assert.assertFalse(isMember);
		RepositoryEntry reloadedCourse = repositoryService.loadByKey(course.getKey());
		Assert.assertFalse(reloadedCourse.getInitialAuthor().equals(username));
		boolean isOwner = repositoryService.hasRoleExpanded(identity, GroupRoles.owner.name());
		Assert.assertFalse(isOwner);
		
		//check deleted consents
		assertThat(courseDisclaimerManager.getConsents(course)).hasSize(0);
		
		User deletedUser = deletedIdentity.getUser();
		// process keep first name last name from user with some "administrative"
		Assert.assertEquals("first" + username, deletedUser.getProperty(UserConstants.FIRSTNAME, null));
		Assert.assertEquals("last" + username, deletedUser.getProperty(UserConstants.LASTNAME, null));
		// but not the other properties
		String institutionalName = deletedUser.getProperty(UserConstants.INSTITUTIONALNAME, null);
		Assert.assertFalse(StringHelper.containsNonWhitespace(institutionalName));
		String institutionalId = deletedUser.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		Assert.assertFalse(StringHelper.containsNonWhitespace(institutionalId));
		String deletedEmail = deletedUser.getProperty(UserConstants.EMAIL, null);
		Assert.assertFalse(StringHelper.containsNonWhitespace(deletedEmail));
	}
	

	/**
	 * The test checked that all of the user properties are wiped out.
	 * 
	 */
	@Test
	public void deleteIdentity_noRoles() {
		Identity groupCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("del-6");
		
		String username = "id-to-del-2-" + UUID.randomUUID();
		String email = username + "@frentix.com";
		User user = userManager.createUser("first" + username, "last" + username, email);
		user.setProperty(UserConstants.COUNTRY, "");
		user.setProperty(UserConstants.CITY, "Basel");
		user.setProperty(UserConstants.INSTITUTIONALNAME, "Del-23");
		user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "Del-24");
		Identity identity = securityManager.createAndPersistIdentityAndUser(username, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), username, "secret");
		dbInstance.commitAndCloseSession();

		//a group
		Roles coachRolesId = securityManager.getRoles(groupCoach);
		BusinessGroup group = businessGroupService.createBusinessGroup(groupCoach, "Group", "Group", -1, -1, false, false, null);
		dbInstance.commit();
		businessGroupService.addParticipants(groupCoach, coachRolesId, Collections.singletonList(identity), group, null);
		dbInstance.commit();
		
		//delete the identity
		userDeletionManager.deleteIdentity(identity, groupCoach);
		dbInstance.commit();
		
		IdentityImpl deletedIdentity = (IdentityImpl)securityManager.loadIdentityByKey(identity.getKey());
		Assert.assertNotNull(deletedIdentity);
		Assert.assertNotNull(deletedIdentity.getDeletedDate());
		Assert.assertEquals(groupCoach.getUser().getLastName() + ", " + groupCoach.getUser().getFirstName(), deletedIdentity.getDeletedBy());

		User deletedUser = deletedIdentity.getUser();
		Assert.assertFalse(StringHelper.containsNonWhitespace(deletedUser.getProperty(UserConstants.FIRSTNAME, null)));
		Assert.assertFalse(StringHelper.containsNonWhitespace(deletedUser.getProperty(UserConstants.LASTNAME, null)));
		Assert.assertFalse(StringHelper.containsNonWhitespace(deletedUser.getProperty(UserConstants.INSTITUTIONALNAME, null)));
		Assert.assertFalse(StringHelper.containsNonWhitespace(deletedUser.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null)));
		Assert.assertFalse(StringHelper.containsNonWhitespace(deletedUser.getProperty(UserConstants.EMAIL, null)));
	}

	@Test
	public void setIdentityAsActiv() throws InterruptedException {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity");
		
		final int maxLoop = 2000; // => 2000 x 11ms => 22sec => finished in 120sec
		// Let two thread call UserDeletionManager.setIdentityAsActiv

		CountDownLatch latch = new CountDownLatch(4);
		ActivThread[] threads = new ActivThread[4];
		for(int i=0; i<threads.length;i++) {
			threads[i] = new ActivThread(ident, maxLoop, latch);
		}

		for(int i=0; i<threads.length;i++) {
			threads[i].start();
		}

		latch.await(120, TimeUnit.SECONDS);

		List<Exception> exceptionsHolder = new ArrayList<>();
		for(int i=0; i<threads.length;i++) {
			exceptionsHolder.addAll(threads[i].exceptionHolder);
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionsHolder) {
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionsHolder.size(), exceptionsHolder.isEmpty());				
	}
	
	private static class ActivThread extends Thread {
		
		private final int maxLoop;
		private final Identity identity;
		private final CountDownLatch countDown;
		private final List<Exception> exceptionHolder = new ArrayList<>();
		private final UserDeletionManager userDeletionManager;
		
		public ActivThread(Identity identity, int maxLoop, CountDownLatch countDown) {
			this.identity = identity;
			this.maxLoop = maxLoop;
			this.countDown = countDown;
			userDeletionManager = CoreSpringFactory.getImpl(UserDeletionManager.class);
		}
		
		@Override
		public void run() {
			try {
				sleep(10);
				for (int i=0; i<maxLoop; i++) {
					try {
						userDeletionManager.setIdentityAsActiv(identity);
					} catch (Exception e) {
						exceptionHolder.add(e);
					} finally {
						try {
							DBFactory.getInstance().closeSession();
						} catch (Exception e) {
							// ignore
						}
					}
				}
			} catch (Exception e) {
				exceptionHolder.add(e);
			} finally {
				countDown.countDown();
			}
		}
	}
}