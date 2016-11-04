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
package org.olat.admin.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseFactory;
import org.olat.course.DeployableCourseExport;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.DefaultUser;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 
 * Initial date: 14.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SetupModule implements GenericEventListener {
	
	private static final OLog log = Tracing.createLoggerFor(SetupModule.class);

	@Value("${deploy.course.exports}")
	private boolean deployCoursesEnabled;
	@Autowired @Qualifier("deployedCourseList")
	private ArrayList<DeployableCourseExport> deployableCourseExports;

	@Value("${user.generateTestUsers}")
	private boolean hasTestUsers;
	@Value("${default.auth.provider.identifier}")
	private String authenticationProviderConstant;

	@Autowired @Qualifier("defaultUsers")
	private ArrayList<DefaultUser> defaultUsers;
	@Autowired @Qualifier("testUsers")
	private ArrayList<DefaultUser> testUsers;
	
	private Map<String, RepositoryEntry> deployedCourses;
	
	@Autowired
	private CourseFactory courseFactory;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager olatResourceManager;
	@Autowired
	private BaseSecurity securityManager;
	

	@Autowired
	public SetupModule(CoordinatorManager coordinatorManager) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, FrameworkStartupEventChannel.getStartupEventChannel());
	}

	/**
	 * Courses are deployed after the startup has completed.
	 * 
	 */
	@Override
	public void event(org.olat.core.gui.control.Event event) {
		if (!Settings.isJUnitTest() && deployCoursesEnabled
				&& event instanceof FrameworkStartedEvent
				&& ((FrameworkStartedEvent) event).isEventOnThisNode()) {
			// Deploy demo courses
			log.info("Received FrameworkStartedEvent and is on same node, will start deploying demo courses...");
			deployCoursesFromCourseExportFiles();
			//also in startup event processing intermediateCommit
			DBFactory.getInstance().intermediateCommit();
		}
		
		createDefaultUsers();
		DBFactory.getInstance().intermediateCommit();
	}
	
	private void createDefaultUsers() {
		// read user editable fields configuration
		if (defaultUsers != null) {
			for (DefaultUser user:defaultUsers) {
				createUser(user);
			}
		}
		if (hasTestUsers) {
			// read user editable fields configuration
			if (testUsers != null) {
				for (DefaultUser user :testUsers) {
					createUser(user);
				}
			}
		}
		// Cleanup, otherwhise this subjects will have problems in normal OLAT
		// operation
		DBFactory.getInstance().commitAndCloseSession();
	}

	private void deployCoursesFromCourseExportFiles( ) {
		log.info("Deploying course exports.");
		for (DeployableCourseExport export: deployableCourseExports) {
			if (0 < export.getAccess() && export.getAccess() < 5) {
				if (deployCoursesEnabled) {
					try {
						deployCourse(export, export.getAccess());
					} catch (Exception e) {
						log.warn("Skipping deployment of course::" + export.getIdentifier(), e);
					}
					DBFactory.getInstance().intermediateCommit();
					continue;
				}
			} else {
				log.info("Skipping deployment of course::" + export.getIdentifier() + " ; access attribute must be 1,2,3 or 4 but values is::"+ export.getAccess());
			}
			log.info("Skipping deployment of course::" + export.getIdentifier());
		}
		if (!deployCoursesEnabled) {
			log.info("Skipping deployment of demo course exports. To deploy course exports, please enable in the configuration file. Help course will always be deployed!");
		}
	}
	
	private RepositoryEntry deployCourse(DeployableCourseExport export, int access) {
		// let's see if we previously deployed demo courses...
		
		RepositoryEntry re = getDeployedCourses().get(export.getIdentifier());
		if (re != null) {
			log.info("Course '" + export.getIdentifier() + "' has been previousely deployed. Skipping.");
			return re;
		}
		
		File file = export.getDeployableCourseZipFile();
		if (file != null && file.exists()) {
			log.info("deploying Course: " + file.getName());
	  	if (!file.exists()) {
				//do not throw exception as users may upload bad file
				log.warn("Cannot deploy course from file: " + file.getAbsolutePath(),null);
				return null;
			}
			re = CourseFactory.deployCourseFromZIP(file, null, access);
			if (re != null) markAsDeployed(export, re);
			return re;
		}
		return null;
	}

	/**
	 * Mark a course as deployed. Remember the key of the repository entry it was
	 * deployed.
	 * 
	 * @param courseExportPath
	 * @param re
	 */
	private void markAsDeployed(DeployableCourseExport export, RepositoryEntry re) {
		List<Property> props = propertyManager.findProperties(null, null, null, "_o3_", "deployedCourses");
		Property prop = null;
		for (Property property : props) {
			if (property.getLongValue() == re.getKey()){
				prop = property;
			}
		}
		if (prop == null) {
			prop = propertyManager.createPropertyInstance(null, null, null, "_o3_", "deployedCourses", export.getVersion(), re.getKey(), export.getIdentifier(), null);
		}
		prop.setFloatValue(export.getVersion());
		prop.setStringValue(export.getIdentifier());
		propertyManager.saveProperty(prop);
		deployedCourses.put(export.getIdentifier(), re);
	}

	/**
	 * Get the Map of deployed courses. Map contains repo entries by path keys.
	 * 
	 * @return
	 */
	private Map<String, RepositoryEntry> getDeployedCourses() {
		if (deployedCourses != null) return deployedCourses;
		List<?> props = propertyManager.findProperties(null, null, null, "_o3_", "deployedCourses");
		deployedCourses = new HashMap<String, RepositoryEntry>(props.size());
		for (Iterator<?> iter = props.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			Long repoKey = prop.getLongValue();
			RepositoryEntry re = null;
			re = RepositoryManager.getInstance().lookupRepositoryEntry(repoKey);
			if (re != null) {
				//props with floatValue null are old entries - delete them.
				if (prop.getFloatValue() == null) {
					//those are courses deployed with the old mechanism, check, if they exist and what should be done with them:
					//fxdiff: no delete! 
					log.info("This course was already deployed and has old property values. course: "+prop.getStringValue());
					for (DeployableCourseExport export: deployableCourseExports) {
						if (export.getIdentifier().equals(prop.getStringValue())) {
							log.info("found this old course in the deployable courses list");
							if (export.isRedeploy()){
								// found in deployableCourses again and it should be redeployed, therefore delete:
								log.info("marked as to be redeployed, therefore delete first!");
								deleteCourseAndProperty(prop, re);
								re = null; //do not add to deployed courses
							} else {
								log.info("no redeploy! just update its version.");
								markAsDeployed(export, re);
							}
						}
					}
				} else {
					//check if latest version if course is installed
					for (DeployableCourseExport export: deployableCourseExports) {
						if (export.getIdentifier().equals(prop.getStringValue()) && export.getVersion() > prop.getFloatValue() && export.isRedeploy()) {
							//we have a newer version - delete the old course
							log.info("There is a new version for this course available. Deleting it and redeploy course: "+prop.getStringValue());
							deleteCourseAndProperty(prop, re);
							re = null; //do not add to deployed courses
							break;
						}
					}
				}
			}
			if (re != null) deployedCourses.put(prop.getStringValue(), re);
		}
		return deployedCourses;
	}

	private void deleteCourseAndProperty(Property prop, RepositoryEntry re) {
		try {
			propertyManager.deleteProperty(prop);
			repositoryService.deleteRepositoryEntryAndBaseGroups(re);
			CourseFactory.deleteCourse(re, re.getOlatResource());
			OLATResource ores = olatResourceManager.findResourceable(re.getOlatResource());
			olatResourceManager.deleteOLATResource(ores);
		} catch (Exception e) {
			log.warn("Could not delete course and property of demo course with name: "+prop.getStringValue(), e);
		}
	}

	/**
	 * Method to create a user with the given configuration
	 * 
	 * @return Identity or null
	 */
	protected Identity createUser(DefaultUser user) {
		Identity identity;
		identity = securityManager.findIdentityByName(user.getUserName());
		if (identity == null) {
			// Create new user and subject
			UserImpl newUser = new UserImpl();
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setEmail(user.getEmail());
			
			newUser.getPreferences().setLanguage(user.getLanguage());
			newUser.getPreferences().setInformSessionTimeout(true);

			if (!StringUtils.hasText(authenticationProviderConstant)){
				throw new OLATRuntimeException(this.getClass(), "Auth token not set! Please fix! " + authenticationProviderConstant, null);
			}

			// Now finally create that user thing on the database with all
			// credentials, person etc. in one transation context!
			identity = securityManager.createAndPersistIdentityAndUser(user.getUserName(), null, newUser, authenticationProviderConstant,
					user.getUserName(), user.getPassword());
			if (identity == null) {
				throw new OLATRuntimeException(this.getClass(), "Error, could not create  user and subject with name " + user.getUserName(), null);
			} else {
				
				if (user.isGuest()) {
					SecurityGroup anonymousGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
					securityManager.addIdentityToSecurityGroup(identity, anonymousGroup);
					log .info("Created anonymous user " + user.getUserName());
				} else {
					SecurityGroup olatuserGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
					
					if (user.isAdmin()) {
						SecurityGroup adminGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
						securityManager.addIdentityToSecurityGroup(identity, adminGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created admin user " + user.getUserName());
					}  else if (user.isAuthor()) {
						SecurityGroup authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
						securityManager.addIdentityToSecurityGroup(identity, authorGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log.info("Created author user " + user.getUserName());
					} else if (user.isUserManager()) {
						SecurityGroup usermanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
						securityManager.addIdentityToSecurityGroup(identity, usermanagerGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created userManager user " + user.getUserName());
					} else if (user.isGroupManager()) {
						SecurityGroup groupmanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
						securityManager.addIdentityToSecurityGroup(identity, groupmanagerGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created groupManager user " + user.getUserName());
					} else {
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created user " + user.getUserName());
					}
				}
			}
		}
		return identity;
	}
}

