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
*/

package org.olat.course;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Initial Date: 02.09.2005 <br>
 * 
 * @author Mike Stock
 * @author guido
 * @author Florian Gnägi
 */
public class CourseModule extends AbstractOLATModule {

	private static boolean courseChatEnabled;
	private static boolean displayParticipantsCount;
	// Repository types
	public static String ORES_TYPE_COURSE = OresHelper.calculateTypeName(CourseModule.class);
	private static OLATResourceable ORESOURCEABLE_TYPE_COURSE = OresHelper.lookupType(CourseModule.class);
	public static final String ORES_COURSE_ASSESSMENT = OresHelper.calculateTypeName(AssessmentManager.class);
	private static String helpCourseSoftkey;
	private static CoordinatorManager coordinatorManager;
	private Map<String, RepositoryEntry> deployedCourses;
	private boolean deployCoursesEnabled;
	private PropertyManager propertyManager;
	private CourseFactory courseFactory;
	private Map<String, String> logVisibilities;
	private List<DeployableCourseExport> deployableCourseExports;
	private RepositoryService repositoryService;
	private OLATResourceManager olatResourceManager;
	


	/**
	 * [used by spring]
	 */
	private CourseModule(CoordinatorManager coordinatorManager, PropertyManager propertyManager, CourseFactory courseFactory, RepositoryService repositoryService, OLATResourceManager olatResourceManager) {
		CourseModule.coordinatorManager = coordinatorManager;
		this.propertyManager = propertyManager;
		this.courseFactory = courseFactory;
		this.repositoryService = repositoryService;
		this.olatResourceManager = olatResourceManager;
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, FrameworkStartupEventChannel.getStartupEventChannel());
	}

	/**
	 * Courses are deployed after the startup has completed.
	 * 
	 */
	@Override
	public void event(org.olat.core.gui.control.Event event) {
		//do not deploy courses/help course if in JUnit Mode.
		if (Settings.isJUnitTest()) return;
		
		if (event instanceof FrameworkStartedEvent && ((FrameworkStartedEvent) event).isEventOnThisNode()) {
			// Deploy demo courses
			logInfo("Received FrameworkStartedEvent and is on same node, will start deploying demo courses...");
			deployCoursesFromCourseExportFiles();
		}
		//also in startup event processing intermediateCommit
		DBFactory.getInstance(false).intermediateCommit();
	}

	
	/**
	 * [used by spring]
	 */
	public void setCourseExportFiles(List<DeployableCourseExport> deployableCourseExports) {
		this.deployableCourseExports = deployableCourseExports;
	}
	
	/**
	 * [used by spring]
	 */
	public void setLogVisibilityForCourseAuthor(Map<String, String> logVisibilities) {
		this.logVisibilities = logVisibilities;
	}

	@Override
	protected void initDefaultProperties() {
		courseChatEnabled = getBooleanConfigParameter("enableCourseChat", true);
		deployCoursesEnabled = getBooleanConfigParameter("deployCourseExportsEnabled", true);
		displayParticipantsCount = getBooleanConfigParameter("displayParticipantsCount", true);
		helpCourseSoftkey = getStringConfigParameter("helpCourseSoftKey", "", true);
	}

	/**
	 * @see org.olat.core.configuration.OLATModule#init(com.anthonyeden.lib.config.Configuration)
	 */
	@Override
	public void init() {
		// skip all the expensive course demo setup and deployment when we are in junit mode.
		if (Settings.isJUnitTest()) return;
		
		logInfo("Initializing the OpenOLAT course system");		
		
		// Cleanup, otherwise this subjects will have problems in normal OLAT
		// operation
		DBFactory.getInstance(false).intermediateCommit();		
	}

	private void deployCoursesFromCourseExportFiles( ) {
		logInfo("Deploying course exports.");
		for (DeployableCourseExport export: deployableCourseExports) {
			if (0 < export.getAccess() && export.getAccess() < 5) {
				if (deployCoursesEnabled) {
					try {
						deployCourse(export, export.getAccess());
					} catch (Exception e) {
						logWarn("Skipping deployment of course::" + export.getIdentifier(), e);
					}
					DBFactory.getInstance().intermediateCommit();
					continue;
				}
			} else {
				logInfo("Skipping deployment of course::" + export.getIdentifier() + " ; access attribute must be 1,2,3 or 4 but values is::"+ export.getAccess());
			}
			logInfo("Skipping deployment of course::" + export.getIdentifier());
		}
		if (!deployCoursesEnabled) {
			logInfo("Skipping deployment of demo course exports. To deploy course exports, please enable in the configuration file. Help course will always be deployed!");
		}
	}
	
	private RepositoryEntry deployCourse(DeployableCourseExport export, int access) {
		// let's see if we previously deployed demo courses...
		
		RepositoryEntry re = getDeployedCourses().get(export.getIdentifier());
		if (re != null) {
			logInfo("Course '" + export.getIdentifier() + "' has been previousely deployed. Skipping.");
			return re;
		}
		
		File file = export.getDeployableCourseZipFile();
		if (file != null && file.exists()) {
			logInfo("deploying Course: " + file.getName());
	  	if (!file.exists()) {
				//do not throw exception as users may upload bad file
				logWarn("Cannot deploy course from file: " + file.getAbsolutePath(),null);
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
					logInfo("This course was already deployed and has old property values. course: "+prop.getStringValue());
					for (DeployableCourseExport export: deployableCourseExports) {
						if (export.getIdentifier().equals(prop.getStringValue())) {
							logInfo("found this old course in the deployable courses list");
							if (export.isRedeploy()){
								// found in deployableCourses again and it should be redeployed, therefore delete:
								logInfo("marked as to be redeployed, therefore delete first!");
								deleteCourseAndProperty(prop, re);
								re = null; //do not add to deployed courses
							} else {
								logInfo("no redeploy! just update its version.");
								markAsDeployed(export, re);
							}
						}
					}
				} else {
					//check if latest version if course is installed
					for (DeployableCourseExport export: deployableCourseExports) {
						if (export.getIdentifier().equals(prop.getStringValue()) && export.getVersion() > prop.getFloatValue() && export.isRedeploy()) {
							//we have a newer version - delete the old course
							logInfo("There is a new version for this course available. Deleting it and redeploy course: "+prop.getStringValue());
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
			logWarn("Could not delete course and property of demo course with name: "+prop.getStringValue(), e);
		}
	}


	
	/**
	 * @return true if the course author can see/download/modify the admin log
	 */
	public boolean isAdminLogVisibleForMigrationOnly() {
		return logVisibilities.get("AdminLog").equals("VISIBLE");
	}

	/**
	 * @return true if the course author can see/download/modify the user log
	 */
	public boolean isUserLogVisibleForMigrationOnly() {
		return logVisibilities.get("UserLog").equals("VISIBLE");
	}

	/**
	 * @return true if the course author can see/download/modify the statistic log
	 */
	public boolean isStatisticLogVisibleForMigrationOnly() {
		return logVisibilities.get("StatisticLog").equals("VISIBLE");
	}


	/**
	 * 
	 * @return The filename of the zipped help course
	 */
	public static String getHelpCourseSoftKey() {
		return helpCourseSoftkey;
	}
	
	/**
	 * @return type name
	 */
	public static String getCourseTypeName() {
		return ORES_TYPE_COURSE;
	}

	/**
	 * @param ce
	 * @param cn
	 * @return the generated SubscriptionContext
	 */
	public static SubscriptionContext createSubscriptionContext(CourseEnvironment ce, CourseNode cn) {
		SubscriptionContext sc = new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent());
		return sc;
	}

	/**
	 * @param ce
	 * @param cn
	 * @return a subscriptioncontext with no translations for the user, but only
	 *         to be able to cleanup/obtain
	 */
	public static SubscriptionContext createTechnicalSubscriptionContext(CourseEnvironment ce, CourseNode cn) {
		SubscriptionContext sc = new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent());
		return sc;
	}

	/**
	 * Creates subscription context which points to an element e.g. that is a sub
	 * element of a node (subsubId). E.g. inside the course node dialog elements
	 * where a course node can have several forums.
	 * 
	 * @param ce
	 * @param cn
	 * @param subsubId
	 * @return
	 */
	public static SubscriptionContext createSubscriptionContext(CourseEnvironment ce, CourseNode cn, String subsubId) {
		SubscriptionContext sc = new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent() + ":" + subsubId);
		return sc;
	}

	/**
	 * whether course chat is enabled or not - depends on Instant Messaging enabled! you should check first for
	 * IM Enabled @see {@link org.olat.instantMessaging.InstantMessagingModule}
	 * @return
	 */
	public static boolean isCourseChatEnabled(){
		return courseChatEnabled;
	}
	
	public static void registerForCourseType(GenericEventListener gel, Identity identity) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(gel, identity, ORESOURCEABLE_TYPE_COURSE);
	}

	public static void deregisterForCourseType(GenericEventListener gel) {
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(gel, ORESOURCEABLE_TYPE_COURSE);
	}

	/**
	 * max number of course nodes
	 * @return
	 */
	public static int getCourseNodeLimit() {
		return 499;
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	public static boolean displayParticipantsCount() {
		return CourseModule.displayParticipantsCount;
	}
}
