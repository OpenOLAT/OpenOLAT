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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OLATResourceableDeletedEvent;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Initial date: 03.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service(value="projectBrokerGroupDeleteManager")
public class ProjectBrokerGroupDeleteManager implements DeletableGroupData {

	private static final Logger log = Tracing.createLoggerFor(ProjectBrokerGroupDeleteManager.class);

	@Autowired
	private ProjectBrokerManager projectBrokerManager;
	
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		log.debug("deleteAllProjectGroupEntiresFor started.. group={}", group);
		
		List<Project> projectList = projectBrokerManager.getProjectsWith(group);
		for (Project project : projectList) {
			projectBrokerManager.deleteProject(project,false, null, null, null); // no course-env, no course-node
			ProjectBroker projectBroker = project.getProjectBroker();
			OLATResourceableDeletedEvent delEv = new OLATResourceableDeletedEvent(projectBroker);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, projectBroker);
			log.debug("deleteProjectWith: group={}, project={}", group, project);
		}
		return true;
	}
}
