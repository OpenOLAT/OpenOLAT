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
* Initial code contributed and copyrighted by<br>
* frentix GmbH, http://www.frentix.com
* <p>
*/
package org.olat.test;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * Helper methods to create identities that can be used in junit tests. Start
 * the test case with -Djunit.maildomain=mydomain.com to create identities with
 * mail accounts that go to your domain, otherwhise mytrashmail.com will be used
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class JunitTestHelper {
	
	private static final OLog log = Tracing.createLoggerFor(JunitTestHelper.class);
	
	public static final String PWD = "A6B7C8";

	private static final Random randomResId = new Random();
	static String maildomain = System.getProperty("junit.maildomain");
	static {
		if (maildomain == null) {
			maildomain = "mytrashmail.com";
		}
	}
	
	public static Roles getAdminRoles() {
		return new Roles(true, true, true, true, false, false, false);
	}
	
	public static Roles getUserRoles() {
		return new Roles(false, false, false, false, false, false, false);
	}
	
	public static final OLATResource createRandomResource() {
		String resName = UUID.randomUUID().toString().replace("-", "");
		long resId = randomResId.nextInt(Integer.MAX_VALUE - 10) + 1;
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, new Long(resId));
		OLATResource resource = OLATResourceManager.getInstance().createOLATResourceInstance(ores);
		OLATResourceManager.getInstance().saveOLATResource(resource);
		return resource;
	}
	
	public static final Identity createAndPersistIdentityAsRndUser(String prefixLogin) {
		if(StringHelper.containsNonWhitespace(prefixLogin)) {
			if(!prefixLogin.endsWith("-")) {
				prefixLogin += "-";
			}
		} else {
			prefixLogin = "junit-";
		}
		String login = prefixLogin + UUID.randomUUID().toString();
		return createAndPersistIdentityAsUser(login);
	}

	/**
	 * Create an identity with user permissions
	 * @param login
	 * @return
	 */
	public static final Identity createAndPersistIdentityAsUser(String login) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		Identity identity = securityManager.findIdentityByName(login);
		if (identity != null) return identity;
		SecurityGroup group = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		if (group == null) group = securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_OLATUSERS);
		User user = UserManager.getInstance().createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(login, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, PWD);
		securityManager.addIdentityToSecurityGroup(identity, group);
		return identity;
	}

	/**
	 * Create an identity with author permissions
	 * @param login
	 * @return
	 */
	public static final Identity createAndPersistIdentityAsAuthor(String login) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		Identity identity = securityManager.findIdentityByName(login);
		if (identity != null) return identity;
		SecurityGroup group = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
		if (group == null) group = securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_AUTHORS);
		User user = UserManager.getInstance().createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(login, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, PWD);
		securityManager.addIdentityToSecurityGroup(identity, group);
		return identity;
	}
	
	/**
	 * Create an identity with admin permissions
	 * @param login
	 * @return
	 */
	public static final Identity createAndPersistIdentityAsAdmin(String login) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		Identity identity = securityManager.findIdentityByName(login);
		if (identity != null) return identity;
		SecurityGroup group = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
		if (group == null) group = securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_ADMIN);
		User user = UserManager.getInstance().createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(login, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, PWD);
		securityManager.addIdentityToSecurityGroup(identity, group);
		return identity;
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry() {
		return createAndPersistRepositoryEntry(false); 
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry(boolean membersOnly) {
		return createAndPersistRepositoryEntry("Florian Gnägi", membersOnly);
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry(String initialAuthor, boolean membersOnly) {
		OLATResourceManager resourceManager = OLATResourceManager.getInstance();
		String resourceName = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resourceName, CodeHelper.getForeverUniqueID());
		OLATResource r =  resourceManager.createOLATResourceInstance(ores);
		resourceManager.saveOLATResource(r);
		return createAndPersistRepositoryEntry(initialAuthor, r, membersOnly);
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry(String initialAuthor, OLATResource r, boolean membersOnly) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		RepositoryEntry re = repositoryService.create(initialAuthor, "Lernen mit OLAT", r.getResourceableTypeName(), null, r);
		if(membersOnly) {
			re.setAccess(RepositoryEntry.ACC_OWNERS);
			re.setMembersOnly(true);
		} else {
			re.setAccess(RepositoryEntry.ACC_USERS);
		}
		repositoryService.update(re);
		return re;
	}
	
	/**
	 * Deploys/imports the "Demo Course".
	 * @return the created RepositoryEntry
	 */
	public static RepositoryEntry deployDemoCourse(Identity initialAuthor) {		
		String displayname = "Demo-Kurs-7.1";
		String description = "";

		RepositoryEntry re = null;
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Demo-Kurs-7.1.zip");
			File courseFile = new File(courseUrl.toURI());
			
			RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
					.getRepositoryHandler(CourseModule.getCourseTypeName());
			re = courseHandler.importResource(initialAuthor, null, displayname, description, true, Locale.ENGLISH, courseFile, null);
			
			ICourse course = CourseFactory.loadCourse(re);
			CourseFactory.publishCourse(course, RepositoryEntry.ACC_USERS, false,  initialAuthor, Locale.ENGLISH);
		} catch (Exception e) {
			log.error("", e);
		}
		return re;
	}
	
	/**
	 * Deploy a course with only a single page.
	 * @param initialAuthor
	 * @return
	 */
	public static RepositoryEntry deployBasicCourse(Identity initialAuthor) {		
		String displayname = "Basic course (" + CodeHelper.getForeverUniqueID() + ")";
		String description = "A course with only a single page";

		RepositoryEntry re = null;
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Basic_course.zip");
			File courseFile = new File(courseUrl.toURI());
			
			RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
					.getRepositoryHandler(CourseModule.getCourseTypeName());
			re = courseHandler.importResource(initialAuthor, null, displayname, description, true, Locale.ENGLISH, courseFile, null);
			
			ICourse course = CourseFactory.loadCourse(re);
			CourseFactory.publishCourse(course, RepositoryEntry.ACC_USERS, false,  initialAuthor, Locale.ENGLISH);
		} catch (Exception e) {
			log.error("", e);
		}
		return re;
	}
	
	/**
	 * Deploy a course with only a single page.
	 * @param initialAuthor
	 * @return
	 */
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname, File courseFile) {		
		String description = "A course";

		RepositoryEntry re = null;
		try {
			RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
					.getRepositoryHandler(CourseModule.getCourseTypeName());
			re = courseHandler.importResource(initialAuthor, null, displayname, description, true, Locale.ENGLISH, courseFile, null);
			
			ICourse course = CourseFactory.loadCourse(re);
			CourseFactory.publishCourse(course, RepositoryEntry.ACC_USERS, false,  initialAuthor, Locale.ENGLISH);
		} catch (Exception e) {
			log.error("", e);
		}
		return re;
	}
}
