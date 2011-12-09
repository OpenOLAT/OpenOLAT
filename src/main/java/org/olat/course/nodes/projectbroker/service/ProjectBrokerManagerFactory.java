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

package org.olat.course.nodes.projectbroker.service;



/**
 * Common factory method for all project-broker managers.
 * @author guretzki
 */

//TODO inject via spring
public class ProjectBrokerManagerFactory  {
	
	private static ProjectBrokerManager projectBrokerManagerInstance = new ProjectBrokerManagerImpl();
	private static ProjectGroupManager  projectGroupManagerInstance  = new ProjectGroupManagerImpl();
	private static ProjectBrokerMailer  projectBrokerMailerInstance  = new ProjectBrokerMailerImpl();

	/**
	 * Return instance of general project-broker manager.
	 * @return
	 */
	public static ProjectBrokerManager getProjectBrokerManager() {
		return projectBrokerManagerInstance;
	}

	/**
	 * Returns manager which can be used to manage project-broker groups.
	 * @return
	 */
	public static ProjectGroupManager getProjectGroupManager() {
		return projectGroupManagerInstance;
	}

	/**
	 * Returns manager which can be used for sending project-broker mails.
	 * @return
	 */
	public static ProjectBrokerMailer getProjectBrokerEmailer() {
		return projectBrokerMailerInstance;
	}

}
