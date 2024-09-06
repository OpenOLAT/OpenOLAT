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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
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
	
	private static final Logger log = Tracing.createLoggerFor(JunitTestHelper.class);
	
	public static final String PWD = "A6B7C8";

	private static final Random randomResId = new Random();
	static String maildomain = System.getProperty("junit.maildomain");
	static {
		if (maildomain == null) {
			maildomain = "mytrashmail.com";
		}
	}
	
	private static Identity defaultAuthor;
	private static Organisation defaultOrganisation;
	
	public static final Identity getDefaultAuthor() {
		if(defaultAuthor == null) {
			defaultAuthor = createAndPersistIdentityAsRndAuthor("the-author");
		}
		return defaultAuthor;
	}
	
	public static final Organisation getDefaultOrganisation() {
		if(defaultOrganisation == null) {
			defaultOrganisation = CoreSpringFactory.getImpl(OrganisationService.class).getDefaultOrganisation();
		}
		return defaultOrganisation;
	}

	public static final String random() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * This method returns the first bloc of a UUID. It's
	 * not really random. But it's only 7 characters long.
	 * 
	 * @return The first bloc of a UUID
	 */
	public static final String miniRandom() {
		String r = UUID.randomUUID().toString();
		return r.substring(0, r.indexOf('-'));
	}
	
	/**
	 * Make a copy of the file in tmp, especially useful if for import
	 * method which use rename instead of copying the file.
	 * 
	 * @param file
	 * @return
	 */
	public static File tmpCopy(File file) {
		File itemFile = new File(WebappHelper.getTmpDir(), file.getName());
		try {
			Files.copy(file.toPath(), itemFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("", e);
		}
		return itemFile;
	}
	

	public static File tmpCopy(URL url) {
		try {
			return tmpCopy(new File(url.toURI()));
		} catch (URISyntaxException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static final OLATResource createRandomResource() {
		String resName = UUID.randomUUID().toString().replace("-", "");
		long resId = randomResId.nextInt(Integer.MAX_VALUE - 10) + 1;
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, Long.valueOf(resId));
		OLATResource resource = OLATResourceManager.getInstance().createOLATResourceInstance(ores);
		OLATResourceManager.getInstance().saveOLATResource(resource);
		return resource;
	}
	
	public static final RepositoryEntry createRandomRepositoryEntry(Identity author) {
		Organisation defOrganisation = getDefaultOrganisation();
		return createRandomRepositoryEntry(author, defOrganisation);
	}
	
	public static final RepositoryEntry createRandomRepositoryEntry(Identity author, Organisation organisation) {
		OLATResource resource = OLATResourceManager.getInstance()
				.createOLATResourceInstance(new ImageFileResource());
		return CoreSpringFactory.getImpl(RepositoryService.class).create(author, "", "-", "Image - " + resource.getResourceableId(), "",
				resource, RepositoryEntryStatusEnum.preparation, RepositoryEntryRuntimeType.embedded, organisation);
	}
	
	public static final Identity createAndPersistIdentityAsRndUser(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		Organisation defOrganisation = getDefaultOrganisation();
		return createAndPersistIdentityAsUser(login, defOrganisation, null);
	}
	
	public static final Identity createAndPersistIdentityAsRndUser(String prefixLogin, String password) {
		String login = getRandomizedLoginName(prefixLogin);
		Organisation defOrganisation = getDefaultOrganisation();
		return createAndPersistIdentityAsUser(login, defOrganisation, password);
	}
	
	public static final Identity createAndPersistIdentityAsRndUser(String prefixLogin, Organisation organisation, String password) {
		String login = getRandomizedLoginName(prefixLogin);
		return createAndPersistIdentityAsUser(login, organisation, password);
	}

	public static final IdentityWithLogin createAndPersistRndUser(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		String password = PWD;
		Organisation defOrganisation = getDefaultOrganisation();
		Identity identity = createAndPersistIdentityAsUser(login, defOrganisation, password);
		return new IdentityWithLogin(identity, login, password);
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

	public static final Identity createAndPersistIdentityAsUser(String login) {
		return createAndPersistIdentityAsUser(login, PWD);
	}
	
	public static final Identity createAndPersistIdentityAsUser(String login, String pwd) {
		Identity identity = findIdentityByLogin(login);
		if (identity != null) {
			return identity;
		}
		Organisation defaultOrganisation = getDefaultOrganisation();
		return createAndPersistIdentityAsUser(login, defaultOrganisation, pwd);
	}

	/**
	 * Create an identity with user permissions
	 * @param login
	 * @return
	 */
	private static final Identity createAndPersistIdentityAsUser(String login, Organisation organisation, String pwd) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		User user = userManager.createUser("first" + login, "last" + login, login + "@" + maildomain);
		
		Identity identity;
		if(StringHelper.containsNonWhitespace(pwd)) {
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null, login, pwd, organisation, null);
		} else {
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
					null, null, null, null, null, organisation, null);
		}
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}

	public static IdentityWithLogin createAndPersistWebDAVAuthentications(IdentityWithLogin identityWithLogin) {
		createAndPersistWebDAVAuthentications(identityWithLogin.getIdentity(), identityWithLogin.getLogin(),  identityWithLogin.getPassword());
		return identityWithLogin;
	}
	
	public static void createAndPersistWebDAVAuthentications(Identity identity, String login, String pwd) {
		WebDAVAuthManager webdavAuthManager = CoreSpringFactory.getImpl(WebDAVAuthManager.class);
		webdavAuthManager.upgradePassword(identity, login, pwd);
	}
	
	/**
	 * Return the identity with the specified login or
	 * user name save as credentials for the OLAT authentication
	 * provider.
	 * 
	 * @param login The login
	 * @return An identity
	 */
	public static final Identity findIdentityByLogin(String login) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Authentication auth = securityManager.findAuthenticationByAuthusername(login,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);
		if (auth != null) {
			return auth.getIdentity();
		}
		return null;
	}
	
	public static final IdentityWithLogin createAndPersistRndAuthor(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		String password = PWD;
		Identity identity = createAndPersistIdentityAsAuthor(login, getDefaultOrganisation(), password);
		return new IdentityWithLogin(identity, login, password);
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
	
	public static final Identity createAndPersistIdentityAsRndAuthor(String prefixLogin, Organisation organisation, String pwd) {
		String login = getRandomizedLoginName(prefixLogin);
		return createAndPersistIdentityAsAuthor(login, organisation, pwd);
	}

	public static final Identity createAndPersistIdentityAsAuthor(String login) {
		return createAndPersistIdentityAsAuthor(login, getDefaultOrganisation(), PWD);
	}

	/**
	 * Create an identity with author permissions
	 * @param login
	 * @return
	 */
	private static final Identity createAndPersistIdentityAsAuthor(String login, Organisation organisation, String password) {
		Identity identity = findIdentityByLogin(login);
		if (identity != null) {
			return identity;
		}
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		User user = CoreSpringFactory.getImpl(UserManager.class)
				.createUser("first" + login, "last" + login, login + "@" + maildomain);
		if(StringHelper.containsNonWhitespace(password)) {
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null, login, password, organisation, null);
		} else {
			identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
				null, null, null, null, null, organisation, null);
		}
		addToDefaultOrganisation(identity, OrganisationRoles.author);
		addToDefaultOrganisation(identity, OrganisationRoles.user);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}
	
	public static final IdentityWithLogin createAndPersistRndAdmin(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		String password = PWD;
		Identity identity = createAndPersistIdentityAsAdmin(login, password);
		return new IdentityWithLogin(identity, login, password);
	}
	
	public static final IdentityWithLogin createAndPersistRndAdmin(String prefixLogin, Organisation organisation) {
		String login = getRandomizedLoginName(prefixLogin);
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		User user = CoreSpringFactory.getImpl(UserManager.class)
				.createUser("first" + login, "last" + login, login + "@" + maildomain);
		Identity identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null, login, PWD, organisation, null);
		CoreSpringFactory.getImpl(OrganisationService.class).addMember(organisation, identity, OrganisationRoles.user);
		CoreSpringFactory.getImpl(OrganisationService.class).addMember(organisation, identity, OrganisationRoles.administrator);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return new IdentityWithLogin(identity, login, PWD);
	}
	
	public static final Identity createAndPersistIdentityAsRndAdmin(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		return createAndPersistIdentityAsAdmin(login, PWD);
	}
	
	/**
	 * Create an identity with administrator permissions
	 * @param login The login
	 * @param password The password
	 * @return
	 */
	private static final Identity createAndPersistIdentityAsAdmin(String login, String password) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = findIdentityByLogin(login);
		if (identity != null) {
			return identity;
		}

		User user = CoreSpringFactory.getImpl(UserManager.class)
				.createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(null, login, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null, login, password, null);
		addToDefaultOrganisation(identity, OrganisationRoles.administrator);
		addToDefaultOrganisation(identity, OrganisationRoles.user);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}
	
	public static final Identity createAndPersistIdentityAsRndLearnResourceManager(String prefixLogin) {
		String login = getRandomizedLoginName(prefixLogin);
		return createAndPersistIdentityAsLearnResourceManager(login);
	}
	
	/**
	 * Create an identity with admin permissions
	 * @param login
	 * @return
	 */
	private static final Identity createAndPersistIdentityAsLearnResourceManager(String login) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = findIdentityByLogin(login);
		if (identity != null) {
			return identity;
		}

		User user = CoreSpringFactory.getImpl(UserManager.class)
				.createUser("first" + login, "last" + login, login + "@" + maildomain);
		identity = securityManager.createAndPersistIdentityAndUser(null, login, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null, login, PWD, null);
		addToDefaultOrganisation(identity, OrganisationRoles.learnresourcemanager);
		addToDefaultOrganisation(identity, OrganisationRoles.user);
		CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
		return identity;
	}
	
	public static void addToDefaultOrganisation(Identity identity, OrganisationRoles role) {
		CoreSpringFactory.getImpl(OrganisationService.class).addMember(identity, role);
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
		Organisation defOrganisation = getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, initialAuthor, "Lernen mit OLAT", r.getResourceableTypeName(), null,
				r, RepositoryEntryStatusEnum.published, RepositoryEntryRuntimeType.embedded, defOrganisation);
		if(!membersOnly) {
			re.setPublicVisible(true);
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
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Demo-Kurs-16.0.zip");
			re = deployCourse(initialAuthor, "Demo-Kurs-7.1", RepositoryEntryStatusEnum.published, courseUrl);
		} catch (Exception e) {
			log.error("", e);
		}
		return re;
	}
	
	/**
	 * Deploys/imports the "Demo Course".
	 * @return the created RepositoryEntry
	 */
	public static RepositoryEntry deployCopyWizardCourse(Identity initialAuthor) {		
		RepositoryEntry re = null;
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/CopyCourseWizardTestCourse.zip");
			re = deployCourse(initialAuthor, "CPW-Test", RepositoryEntryStatusEnum.published, courseUrl);
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
		return deployBasicCourse(initialAuthor, displayname, RepositoryEntryStatusEnum.published);
	}
	
	/**
	 * Deploy a course with only a single page. Title is randomized.
	 * 
	 * @param initialAuthor The author
	 * @param access The access
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployBasicCourse(Identity initialAuthor, RepositoryEntryStatusEnum status) {
		String displayname = "Basic course (" + CodeHelper.getForeverUniqueID() + ")";
		return deployBasicCourse(initialAuthor, displayname, status);
	}

	/**
	 * Deploy a course with only a single page.
	 * 
	 * @param initialAuthor The author
	 * @param displayname The title of the course
	 * @param access The access
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployBasicCourse(Identity initialAuthor, String displayname, RepositoryEntryStatusEnum status) {
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Basic_course.zip");
			return deployCourse(initialAuthor, displayname, status, courseUrl);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static RepositoryEntry deployEmptyCourse(Identity initialAuthor, String displayname,
			RepositoryEntryStatusEnum status) {
		try {
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/Empty_course.zip");
			return deployCourse(initialAuthor, displayname, status, courseUrl);
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
		return deployCourse(initialAuthor, displayname, RepositoryEntryStatusEnum.published, courseUrl);
	}
	
	/**
	 * 
	 * @param initialAuthor The author
	 * @param displayname The name of the course
	 * @param courseUrl The file to import
	 * @param access The access
	 * @return The repository entry of the course
	 */
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname,
			RepositoryEntryStatusEnum status, URL courseUrl) {
		try {
			File courseFile = new File(courseUrl.toURI());
			Organisation defOrganisation = getDefaultOrganisation();
			return deployCourse(initialAuthor, displayname, courseFile, status, defOrganisation);
		} catch (URISyntaxException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static RepositoryEntry deployCourse(Identity initialAuthor, String displayname,
			RepositoryEntryStatusEnum status, URL courseUrl, Organisation organisation) {
		try {
			File courseFile = new File(courseUrl.toURI());
			return deployCourse(initialAuthor, displayname, courseFile, status, organisation);
		} catch (URISyntaxException e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * 
	 * @param initialAuthor The author (not mandatory)
	 * @param displayname The name of the course
	 * @param courseFile The file to import
	 * @param access The access
	 * @return The repository entry of the course
	 */
	private static RepositoryEntry deployCourse(Identity initialAuthor, String displayname,
			File courseFile, RepositoryEntryStatusEnum status, Organisation organisation) {
		try {
			RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
					.getRepositoryHandler(CourseModule.getCourseTypeName());
			RepositoryEntry re = courseHandler.importResource(initialAuthor, null, displayname, "A course",
					RepositoryEntryImportExportLinkEnum.WITH_REFERENCE, organisation, Locale.ENGLISH, courseFile, null);
			ICourse course = CourseFactory.loadCourse(re);
			CourseFactory.publishCourse(course, status, initialAuthor, Locale.ENGLISH);
			return CoreSpringFactory.getImpl(RepositoryManager.class).lookupRepositoryEntry(re.getKey());
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static class IdentityWithLogin implements IdentityRef {
		
		private final Identity identity;
		private final String login;
		private final String password;
		
		public IdentityWithLogin(Identity identity, String login, String password) {
			this.identity = identity;
			this.login = login;
			this.password = password;
		}
		
		@Override
		public Long getKey() {
			return identity.getKey();
		}
		
		public Identity getIdentity() {
			return identity;
		}
		
		public User getUser() {
			return identity.getUser();
		}
		
		public String getLogin() {
			return login;
		}
		
		public String getPassword() {
			return password;
		}	
	}
}
