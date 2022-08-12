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
package org.olat.modules.zoom.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.manager.ZoomProfileDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Initial date: 2022-08-11<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ShowZoomApplicationsController extends BasicController {

    private final VelocityContainer mainVC;

    @Autowired
    private ZoomManager zoomManager;

    public ShowZoomApplicationsController(UserRequest ureq, WindowControl wControl, ZoomProfile zoomProfile) {
        super(ureq, wControl);

        List<ZoomProfileDAO.ZoomProfileApplication> applications = zoomManager.getProfileApplications(zoomProfile.getKey());

        mainVC = createVelocityContainer("show_applications");
        List<Link> links = applications.stream().map(this::applicationToLink).collect(Collectors.toList());
        mainVC.contextPut("links", links);

        putInitialPanel(mainVC);
    }

    private Link applicationToLink(ZoomProfileDAO.ZoomProfileApplication zoomProfileApplication) {
        LTI13ToolDeployment toolDeployment = zoomProfileApplication.getLti13ToolDeployment();
        String linkName = zoomProfileApplication.getDescription();
        Link link = LinkFactory.createLink(linkName, mainVC, this);
        StringBuilder businessPath = new StringBuilder(128);

        switch (zoomProfileApplication.getApplicationType()) {
            case courseElement:
                ICourse course = CourseFactory.loadCourse(toolDeployment.getEntry());
                String courseElementName = course.getRunStructure().getNode(toolDeployment.getSubIdent()).getShortName();
                String courseName = course.getCourseTitle();
                String linkText = getTranslator().translate("zoom.profile.application.courseElement", courseElementName, courseName);
                link.setCustomDisplayText(linkText);
                businessPath.append("[RepositoryEntry:").append(toolDeployment.getEntry().getKey())
                        .append("][CourseNode:").append(toolDeployment.getSubIdent()).append("]");
                break;
            case courseTool:
                course = CourseFactory.loadCourse(toolDeployment.getEntry());
                linkText = getTranslator().translate("zoom.profile.application.courseTool", course.getCourseTitle());
                link.setCustomDisplayText(linkText);
                businessPath.append("[RepositoryEntry:").append(toolDeployment.getEntry().getKey()).append("][zoom:0]");
                break;
            case groupTool:
                linkText = getTranslator().translate("zoom.profile.application.groupTool", toolDeployment.getBusinessGroup().getName());
                link.setCustomDisplayText(linkText);
                businessPath.append("[BusinessGroup:").append(toolDeployment.getBusinessGroup().getKey()).append("][toolzoom:0]");
                break;
        }

        link.setUserObject(businessPath.toString());

        return link;
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source instanceof Link) {
            Link link = (Link) source;
            String businessPath = (String) link.getUserObject();
            if (businessPath != null) {
                fireEvent(ureq, new OpenBusinessPathEvent(businessPath));
            }
        }
    }

    public static class OpenBusinessPathEvent extends Event {
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
