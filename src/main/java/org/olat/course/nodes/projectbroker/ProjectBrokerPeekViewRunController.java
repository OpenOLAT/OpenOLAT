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
package org.olat.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The projectbroker peekview controller displays the selected and coached projects for certain user.
 * 
 * @author Christian Guretzki
 */
public class ProjectBrokerPeekViewRunController extends BasicController implements Controller {


	private static final int MAX_NBR_PROJECTS = 3;
	
	private final String courseNodeIdent;
	@Autowired
	private ProjectBrokerManager projectBrokerManager;
	
	/**
	 * Constructor
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public ProjectBrokerPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode) {		
		// Use fallback translator from forum
		super(ureq, wControl);
		courseNodeIdent = courseNode.getIdent();
		
		CoursePropertyManager cpm = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		Long projectBrokerId = projectBrokerManager.getProjectBrokerId(cpm, courseNode);
		getLogger().debug("projectBrokerId=" +projectBrokerId);
		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		List<Project> myProjects = null;
		if (projectBrokerId != null) {
			myProjects = projectBrokerManager.getProjectsOf(ureq.getIdentity(), projectBrokerId);
		} else {
			// when projectBrokerId is null, created empty project list (course-preview)
			myProjects = new ArrayList<>();
		}
		// check nbr of projects and limit it
		if (myProjects.size() > MAX_NBR_PROJECTS) {
			peekviewVC.contextPut("peekviewMoreProjects", "true");
			myProjects = myProjects.subList(0, MAX_NBR_PROJECTS);
		}
		peekviewVC.contextPut("myProjects", myProjects);
		for (Iterator<Project> iterator = myProjects.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			// Add link to show all items (go to node)
			Link nodeLink = LinkFactory.createLink("nodeLink_" + project.getKey(), peekviewVC, this);
			nodeLink.setCustomDisplayText(project.getTitle());
			nodeLink.setCustomEnabledLinkCSS("o_gotoNode");
			nodeLink.setUserObject(Long.toString(project.getKey().longValue()));				
		}

		List<Project> myCoachedProjects = null;
		if (projectBrokerId != null) {
			myCoachedProjects = projectBrokerManager.getCoachedProjectsOf(ureq.getIdentity(), projectBrokerId);
		} else {
			// when projectBrokerId is null, created empty project list (course-preview)
			myCoachedProjects = new ArrayList<>();
		}
		// check nbr of projects and limit it
		if (myCoachedProjects.size() > MAX_NBR_PROJECTS) {
			peekviewVC.contextPut("peekviewMoreCoachedProjects", "true");
			myCoachedProjects = myCoachedProjects.subList(0, MAX_NBR_PROJECTS);
		}
		peekviewVC.contextPut("myCoachedProjects", myCoachedProjects);
		for (Iterator<Project> iterator = myCoachedProjects.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			// Add link to show all items (go to node)
			Link nodeLink = LinkFactory.createLink("coachedNodeLink_" + project.getKey(), peekviewVC, this);
			nodeLink.setCustomDisplayText(project.getTitle());
			nodeLink.setCustomEnabledLinkCSS("o_gotoNode");
			nodeLink.setUserObject(Long.toString(project.getKey().longValue()));				
		}	

		this.putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link projectLink = (Link) source;
			String projectId = (String) projectLink.getUserObject();
			if (projectId == null) {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, courseNodeIdent));								
			} else {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, courseNodeIdent + "/" + projectId));				
			}
		}
	}

}
