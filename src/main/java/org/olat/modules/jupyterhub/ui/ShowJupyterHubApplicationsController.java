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
package org.olat.modules.jupyterhub.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.ims.lti13.LTI13Context;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.modules.jupyterhub.manager.JupyterHubDAO;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ShowJupyterHubApplicationsController extends BasicController {

	private final VelocityContainer mainVC;

	@Autowired
	private JupyterManager jupyterManager;

	protected ShowJupyterHubApplicationsController(UserRequest ureq, WindowControl wControl, JupyterHub jupyterHub) {
		super(ureq, wControl);

		List<JupyterHubDAO.JupyterHubApplication> applications = jupyterManager.getJupyterHubApplications(jupyterHub.getKey());

		mainVC = createVelocityContainer("show_applications");
		HashSet<Identity> collectedIdentities = new HashSet<>();
		List<Link> links = applications.stream().map((l) -> applicationToLink(l, collectedIdentities)).toList();
		mainVC.contextPut("links", links);
		mainVC.contextPut("nbParticipants", collectedIdentities.size());
		putInitialPanel(mainVC);
	}

	private Link applicationToLink(JupyterHubDAO.JupyterHubApplication application, Set<Identity> collectedIdentities) {
		LTI13Context ltiContext = application.getLti13Context();
		String linkName = application.getDescription();
		Link link = LinkFactory.createLink(linkName, mainVC, this);
		RepositoryEntry courseEntry = ltiContext.getEntry();
		ICourse course = CourseFactory.loadCourse(ltiContext.getEntry());
		String courseElementName = course.getRunStructure().getNode(ltiContext.getSubIdent()).getShortName();
		String courseName = course.getCourseTitle();
		long participantCount = jupyterManager.getParticipantCount(course, courseEntry, collectedIdentities);
		String linkText = getTranslator().translate("jupyterHub.application.courseElement",
				courseElementName, courseName, Long.toString(participantCount));
		link.setCustomDisplayText(linkText);
		String businessPath = "[RepositoryEntry:"+ ltiContext.getEntry().getKey() + "][CourseNode:" + ltiContext.getSubIdent() + "]";
		link.setUserObject(businessPath);
		return link;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String businessPath = (String) link.getUserObject();
			if (businessPath != null) {
				fireEvent(ureq, new OpenBusinessPathEvent(businessPath));
			}
		}
	}

	public static class OpenBusinessPathEvent extends Event {
		
		private static final long serialVersionUID = 5365640060093627482L;
		
		private final String businessPath;

		public OpenBusinessPathEvent(String businessPath) {
			super("openBusinessPath");
			this.businessPath = businessPath;
		}

		public String getBusinessPath() {
			return businessPath;
		}
	}
}
