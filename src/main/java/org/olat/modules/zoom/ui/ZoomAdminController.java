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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.ims.lti13.LTI13Module;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomAdminController extends BasicController implements Activateable2 {

    private final VelocityContainer mainVC;
    private final SegmentViewComponent segmentView;
    private final Link configurationLink;

    @Autowired
    private LTI13Module lti13Module;

    private ZoomConfigurationController configController;

    public ZoomAdminController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        mainVC = createVelocityContainer("zoom_admin");
        segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
        configurationLink = LinkFactory.createLink("zoom.configuration", mainVC, this);

        if (lti13Module.isEnabled()) {
            doOpenConfiguration(ureq);
            mainVC.contextPut("isLtiAvailable", Boolean.TRUE);
        } else {
            mainVC.contextPut("isLtiAvailable", Boolean.FALSE);
        }

        putInitialPanel(mainVC);
    }

    private void doOpenConfiguration(UserRequest ureq) {
        if (configController == null) {
            WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Configuration", 0l), null);
            configController = new ZoomConfigurationController(ureq, bwControl);
            listenTo(configController);
        } else {
            addToHistory(ureq, configController);
        }
        mainVC.put("segmentCmp", configController.getInitialComponent());
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == segmentView && event instanceof SegmentViewEvent) {
            SegmentViewEvent sve = (SegmentViewEvent)event;
            String segmentCName = sve.getComponentName();
            Component clickedLink = mainVC.getComponent(segmentCName);
            if (clickedLink == configurationLink) {
                doOpenConfiguration(ureq);
            }
        }
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        if (entries == null || entries.isEmpty()) return;

        String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
        if ("Configuration".equalsIgnoreCase(type)) {
            doOpenConfiguration(ureq);
            segmentView.select(configurationLink);
        }
    }
}
