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
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti.ui.LTIDisplayContentController;
import org.olat.ims.lti13.ui.LTI13DisplayController;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomRunController extends BasicController implements Activateable2 {

    private VelocityContainer container;
    private Link startButton;

    private StackedPanel mainPanel;

    ZoomConfig zoomConfig;

    @Autowired
    ZoomManager zoomManager;

    private LTIDisplayContentController ltiCtrl;

    public ZoomRunController(UserRequest ureq, WindowControl wControl,
                             ZoomManager.ApplicationType zoomApplicationType,
                             RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
                             boolean admin, boolean coach, boolean participant, String clientId) {
        super(ureq, wControl);

        zoomConfig = zoomManager.getConfig(entry, subIdent, businessGroup);
        if (zoomConfig == null && clientId != null) {
            zoomManager.initializeConfig(entry, subIdent, businessGroup, zoomApplicationType, clientId, getIdentity().getUser());
            zoomConfig = zoomManager.getConfig(entry, subIdent, businessGroup);
        }

        if (zoomConfig == null) {
            String title = translate("zoom.config.missing.error.title");
            String text;
            switch (zoomApplicationType) {
                case courseElement:
                    text = translate("zoom.config.missing.error.courseElement");
                    break;
                case courseTool:
                    text  = translate("zoom.config.missing.error.courseTool");
                    break;
                case groupTool:
                default:
                    text = translate("zoom.config.missing.error.groupTool");
                    break;
            }
            Controller ctrl = MessageUIFactory.createErrorMessage(ureq, wControl, title, text);
            mainPanel = new SimpleStackedPanel("zoomErrorContainer");
            putInitialPanel(mainPanel);
            mainPanel.setContent(ctrl.getInitialComponent());
        } else if (zoomConfig.getProfile().getStatus() == ZoomProfile.ZoomProfileStatus.inactive) {
            container = createVelocityContainer("run");
            container.contextPut("isConfigInactive", true);
            container.contextPut("zoomProfileName", zoomConfig.getProfile().getName());
            mainPanel = putInitialPanel(container);
        } else {
            ltiCtrl = new LTI13DisplayController(ureq, wControl, zoomConfig.getLtiToolDeployment(), admin, coach, participant);
            ltiCtrl.openLtiContent(ureq);
            putInitialPanel(ltiCtrl.getInitialComponent());
        }
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
//        if (source == startButton) {
//            openZoom(ureq);
//        }
    }

    private void openZoom(UserRequest ureq) {
        ltiCtrl.openLtiContent(ureq);
        mainPanel.setContent(ltiCtrl.getInitialComponent());
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
    }
}
