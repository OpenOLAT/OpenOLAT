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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
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
import org.olat.fileresource.types.ImageFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	
	public static final RepositoryEntry createRandomRepositoryEntry(Identity author) {
		OLATResource resource = OLATResourceManager.getInstance()
				.createOLATResourceInstance(new ImageFileResource());
		Organisation defOrganisation = CoreSpringFactory.getImpl(OrganisationService.class)
				.getDefaultOrganisation();
		return CoreSpringFactory.getImpl(RepositoryService.class)
				.create(author, "", "-", "Image - " + resource.getResourceableId(), "", resource, 1, defOrganisation);
	}
	
	public static final Identity createAndPersistIdentityAsRndUser(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		return createAndPersistIdentityAsUser(login);
	}
	
	private static final String getRandomizedLoginName(String prefixLogin) {
		if(StringHelper.containsNonWhitespace(prefixLogin)) {
			if(!prefixLogin.endsWith("-")) {
				prefixLogin += "-";
			}
		} else {
			prefixLogin = "junit-";
		}
		return prefixLogin + UUID.randomUUID();
	}

	/**
	 * Create an identity with user permissions
	 * @param login
	 * @return
	 */
	public static final Identity createAndPersistIdentityAsUser(String login) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.findIdentityByName(login);
		if (identity != null) {
			return identity;
		}
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		User user = userManager.createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(login, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, PWD);
		addToDefaultOrganisation(identity, OrganisationRoles.user);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}
	
	/**
	 * Create a new identity with author permission.
	 * 
	 * @param prefixLogin The prefix of the user name.
	 * @return The new unique identity
	 */
	public static final Identity createAndPersistIdentityAsRndAuthor(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		return createAndPersistIdentityAsAuthor(login);
	}

	/**
	 * Create an identity with author permissions
	 * @param login
	 * @return
	 */
	public static final Identity createAndPersistIdentityAsAuthor(String login) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.findIdentityByName(login);
		if (identity != null) {
			return identity;
		}

		User user = CoreSpringFactory.getImpl(UserManager.class)
				.createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(login, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, PWD);
		addToDefaultOrganisation(identity, OrganisationRoles.author);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}
	
	/**
	 * Create an identity with admin permissions
	 * @param login
	 * @return
	 */
	public static final Identity createAndPersistIdentityAsAdmin(String login) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.findIdentityByName(login);
		if (identity != null) {
			return identity;
		}

		User user = CoreSpringFactory.getImpl(UserManager.class)
				.createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(login, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, PWD);
		addToDefaultOrganisation(identity, OrganisationRoles.administrator);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}
	
	private static void addToDefaultOrganisation(Identity identity, OrganisationRoles role) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		organisationService.addMember(identity, role);
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
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, initialAuthor, "Lernen mit OLAT", r.getResourceableTypeName(), null, r, 0, defOrganisation);
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
		RepositoryEntry re = null;
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Demo-Kurs-7.1.zip");
			re = deployCourse(initialAuthor, "Demo-Kurs-7.1", RepositoryEntry.ACC_USERS, courseUrl);
		} catch (Exception e) {
			log.error("", e);
		}
		return re;
	}
	
	/**
	 * Deploy a course with only a single page. Title is randomized.
	 * 
	 * @param initialAuthor The author
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployBasicCourse(Identity initialAuthor) {
		String displayname = "Basic course (" + CodeHelper.getForeverUniqueID() + ")";
		return deployBasicCourse(initialAuthor, displayname, RepositoryEntry.ACC_USERS);
	}
	
	/**
	 * Deploy a course with only a single page. Title is randomized.
	 * 
	 * @param initialAuthor The author
	 * @param access The access
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployBasicCourse(Identity initialAuthor, int access) {
		String displayname = "Basic course (" + CodeHelper.getForeverUniqueID() + ")";
		return deployBasicCourse(initialAuthor, displayname, access);
	}

	/**
	 * Deploy a course with only a single page.
	 * 
	 * @param initialAuthor The author
	 * @param displayname The title of the course
	 * @param access The access
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployBasicCourse(Identity initialAuthor, String displayname, int access) {
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Basic_course.zip");
			return deployCourse(initialAuthor, displayname, access, courseUrl);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static RepositoryEntry deployEmptyCourse(Identity initialAuthor, String displayname, int access) {
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Empty_course.zip");
			return deployCourse(initialAuthor, displayname, access, courseUrl);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * The course will be accessible to all registrated users.
	 * 
	 * @param initialAuthor The author
	 * @param displayname the name of the course
	 * @param courseUrl The file to import
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname, URL courseUrl) {
		return deployCourse(initialAuthor, displayname, RepositoryEntry.ACC_USERS, courseUrl);
	}
	
	/**
	 * 
	 * @param initialAuthor The author
	 * @param displayname The name of the course
	 * @param access The access
	 * @param courseUrl The file to import
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname, int access, URL courseUrl) {
		try {
			File courseFile = new File(courseUrl.toURI());
			return deployCourse(initialAuthor, displayname, courseFile, access);
		} catch (URISyntaxException e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * The course will be accessible to all registrated users.
	 * 
	 * @param initialAuthor The author (not mandatory)
	 * @param displayname The name of the course
	 * @param courseFile The file to import
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname, File courseFile) {	
		return deployCourse(initialAuthor, displayname, courseFile, RepositoryEntry.ACC_USERS) ;
	}
	
	/**
	 * 
	 * @param initialAuthor The author (not mandatory)
	 * @param displayname The name of the course
	 * @param courseFile The file to import
	 * @param access The access
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname, File courseFile, int access) {		
		try {
			RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
					.getRepositoryHandler(CourseModule.getCourseTypeName());
			OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			RepositoryEntry re = courseHandler.importResource(initialAuthor, null, displayname, "A course", true, defOrganisation, Locale.ENGLISH, courseFile, null);
			
			ICourse course = CourseFactory.loadCourse(re);
			CourseFactory.publishCourse(course, access, false,  initialAuthor, Locale.ENGLISH);
			return  CoreSpringFactory.getImpl(RepositoryManager.class).lookupRepositoryEntry(re.getKey());
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
