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

package org.olat.course.nodes.ta;


import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseModule;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * Description:<br>
 * Notification handler for course node task. Subscribers get informed about
 * new uploaded file in the solution folder.
 * <P>
 * Initial Date: 23.06.2010 <br />
 * 
 * @author christian guretzki
 */
public class SolutionFileUploadNotificationHandler extends AbstractTaskNotificationHandler implements NotificationsHandler {
	private static final String CSS_CLASS_SOLUTION_ICON = "o_solution_icon"; 
	private static final Logger log = Tracing.createLoggerFor(SolutionFileUploadNotificationHandler.class);
	
	public SolutionFileUploadNotificationHandler() {
		//empty block
	}

	protected static SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, CourseNode node) {
	  return CourseModule.createSubscriptionContext(courseEnv, node, "Solutionbox");
	}

	protected String getCssClassIcon() {
		return CSS_CLASS_SOLUTION_ICON;
	}
	
	protected String getNotificationHeaderKey() {
		return "solution.notifications.header";
	}
	
	protected String getNotificationEntryKey() {
		return "solution.notifications.entry";
	}
	
	protected Logger getLogger() {
		return log;
	}
	
	@Override
	public String getType() {
		return "SolutionController";
	}

}
