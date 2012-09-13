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

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Encoder;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.DeployableCourseExport;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

	private static final Random randomResId = new Random();
	static String maildomain = System.getProperty("junit.maildomain");
	static {
		if (maildomain == null) {
			maildomain = "mytrashmail.com";
		}
	}
	
	public static final OLATResource createRandomResource() {
		String resName = UUID.randomUUID().toString().replace("-", "");
		long resId = randomResId.nextInt(Integer.MAX_VALUE - 10) + 1;
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, new Long(resId));
		OLATResource resource = OLATResourceManager.getInstance().createOLATResourceInstance(ores);
		OLATResourceManager.getInstance().saveOLATResource(resource);
		return resource;
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
		identity = securityManager.createAndPersistIdentityAndUser(login, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login,
				Encoder.encrypt("A6B7C8"));
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
		identity = securityManager.createAndPersistIdentityAndUser(login, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login,
				Encoder.encrypt("A6B7C8"));
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
		identity = securityManager.createAndPersistIdentityAndUser(login, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), login,
				Encoder.encrypt("A6B7C8"));
		securityManager.addIdentityToSecurityGroup(identity, group);
		return identity;
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry() {
		return createAndPersistRepositoryEntry(false); 
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry(boolean membersOnly) {
		OLATResourceManager resourceManager = OLATResourceManager.getInstance();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), CodeHelper.getForeverUniqueID());
		OLATResource r =  resourceManager.createOLATResourceInstance(ores);
		resourceManager.saveOLATResource(r);
		return createAndPersistRepositoryEntry(r, membersOnly);
	}
	
	public static final RepositoryEntry createAndPersistRepositoryEntry(OLATResource r, boolean membersOnly) {
		RepositoryManager repositoryManager = RepositoryManager.getInstance();
		RepositoryEntry re = repositoryManager.createRepositoryEntryInstance("Florian Gnägi");
		re.setOlatResource(r);
		if(membersOnly) {
			re.setAccess(RepositoryEntry.ACC_OWNERS);
			re.setMembersOnly(true);
		} else {
			re.setAccess(RepositoryEntry.ACC_USERS);
		}
		re.setResourcename("Lernen mit OLAT");
		re.setDisplayname(r.getResourceableTypeName());
		repositoryManager.createOwnerSecurityGroup(re);
		repositoryManager.createTutorSecurityGroup(re);
		repositoryManager.createParticipantSecurityGroup(re);
		repositoryManager.saveRepositoryEntry(re);
		return re;
	}
	
	/**
	 * Deploys/imports the "Demo Course".
	 * @return the created RepositoryEntry
	 */
	public static RepositoryEntry deployDemoCourse() {
		
		RepositoryEntry re = null;
		PropertyManager propertyManager = PropertyManager.getInstance();
		List<Property> l = propertyManager.findProperties(null, null, null, "_o3_", "deployedCourses");
		if (l.size() > 0) {
			re = RepositoryManager.getInstance().lookupRepositoryEntry(l.get(0).getLongValue());
			if (re != null) {
				//try to load it
				try {
					CourseFactory.loadCourse(re.getOlatResource());
					return re;
				} catch(Exception ex) {
					propertyManager.deleteProperties(null, null, null, "_o3_", "deployedCourses");
					RepositoryManager.getInstance().deleteRepositoryEntry(re);
				}
			}
		}
		
		createAndPersistIdentityAsAdmin("administrator");
		
		DeployableCourseExport export = (DeployableCourseExport)new ClassPathXmlApplicationContext("/org/olat/test/_spring/demoCourseExport.xml").getBean("demoCourse");
		if (!export.getDeployableCourseZipFile().exists()) {
			//do not throw exception as users may upload bad file
			System.out.println("Cannot deploy course from file: " + export.getIdentifier());
			return null;
		}
		re = CourseFactory.deployCourseFromZIP(export.getDeployableCourseZipFile(), 4);	
		if (re != null) {
			Property prop = propertyManager.createPropertyInstance(null, null, null, "_o3_", "deployedCourses", export.getVersion(), re.getKey(), export.getIdentifier(), null);
			propertyManager.saveProperty(prop);
		}
		return re;
	}

}
