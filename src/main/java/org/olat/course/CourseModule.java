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

import java.util.HashMap;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 02.09.2005 <br>
 * 
 * @author Mike Stock
 * @author guido
 * @author Florian Gnägi
 */
@Service
public class CourseModule extends AbstractSpringModule {

	@Value("${course.display.participants.count}")
	private boolean displayParticipantsCount;
	@Value("${help.course.softkey}")
	private String helpCourseSoftkey;
	@Autowired @Qualifier("logVisibilityForCourseAuthor")
	private HashMap<String, String> logVisibilities;
	
	// Repository types
	public static String ORES_TYPE_COURSE = OresHelper.calculateTypeName(CourseModule.class);
	public static OLATResourceable ORESOURCEABLE_TYPE_COURSE = OresHelper.lookupType(CourseModule.class);
	public static final String ORES_COURSE_ASSESSMENT = OresHelper.calculateTypeName(AssessmentManager.class);
	
	private static CoordinatorManager coordinatorManager;

	@Autowired
	public CourseModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		CourseModule.coordinatorManager = coordinatorManager;
	}
	
	@Override
	protected void initFromChangedProperties() {
		//
	}

	@Override
	public void init() {
		//
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
	public String getHelpCourseSoftKey() {
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

	public boolean displayParticipantsCount() {
		return displayParticipantsCount;
	}
}
