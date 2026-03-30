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

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.zoom.ZoomProfile;

/**
 * Tab page controller for the "Zoom LTI Pro configurations" tab in the Zoom
 * administration. Provides a master-detail view: the master lists Zoom LTI Pro
 * profiles; the detail shows the applications of a selected profile.
 *
 * Initial date: 2026-03-26<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ZoomLtiProConfigurationsController extends BasicController implements Activateable2 {

    private final TooledStackedPanel stackPanel;
    private final ZoomProfileListController profileListCtrl;
    private ShowZoomApplicationsController showApplicationsCtrl;

    public ZoomLtiProConfigurationsController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        VelocityContainer mainVC = createVelocityContainer("zoom_lti_pro_configurations");

        stackPanel = new TooledStackedPanel("zoomStack", getTranslator(), this);
        stackPanel.setToolbarEnabled(false);
        mainVC.put("stackPanel", stackPanel);

        profileListCtrl = new ZoomProfileListController(ureq, wControl);
        listenTo(profileListCtrl);
        stackPanel.pushController(translate("zoom.configurations"), profileListCtrl);

        putInitialPanel(mainVC);
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == profileListCtrl) {
            if (event instanceof ZoomProfileListController.ShowApplicationsEvent sae) {
                doShowApplications(ureq, sae.getRow());
            }
        } else if (source == showApplicationsCtrl) {
            if (event instanceof ShowZoomApplicationsController.OpenBusinessPathEvent bpe) {
                stackPanel.popUpToRootController(ureq);
                removeAsListenerAndDispose(showApplicationsCtrl);
                showApplicationsCtrl = null;
                profileListCtrl.loadModel();
                NewControllerFactory.getInstance().launch(bpe.getBusinessPath(), ureq, getWindowControl());
            }
        }
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == stackPanel && event instanceof PopEvent) {
            removeAsListenerAndDispose(showApplicationsCtrl);
            showApplicationsCtrl = null;
        }
    }

    private void doShowApplications(UserRequest ureq, ZoomProfileRow row) {
        removeAsListenerAndDispose(showApplicationsCtrl);

        ZoomProfile zoomProfile = row.getZoomProfile();
        showApplicationsCtrl = new ShowZoomApplicationsController(ureq, getWindowControl(), zoomProfile);
        listenTo(showApplicationsCtrl);

        stackPanel.pushController(zoomProfile.getName(), showApplicationsCtrl);
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        //
    }
}