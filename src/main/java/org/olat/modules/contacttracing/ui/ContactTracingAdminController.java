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
package org.olat.modules.contacttracing.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingAdminController extends BasicController implements Activateable2 {

    private final VelocityContainer mainVC;
    private final SegmentViewComponent segments;

    private final Link configurationLink;
    private final Link locationListLink;
    private final Link reportLink;

    private ContactTracingConfigurationController configurationController;
    private ContactTracingLocationListController locationListController;
    private ContactTracingReportController reportController;

    @Autowired
    private ContactTracingModule contactTracingModule;

    public ContactTracingAdminController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        mainVC = createVelocityContainer("contact_tracing_admin");
        configurationLink = LinkFactory.createLink("contact.tracing.configuration", mainVC, this);
        locationListLink= LinkFactory.createLink("contact.tracing.locations", mainVC, this);
        reportLink = LinkFactory.createLink("contact.tracing.report", mainVC, this);

        segments = SegmentViewFactory.createSegmentView("segments", mainVC, this);
        segments.setDontShowSingleSegment(true);
        segments.addSegment(configurationLink, true);

        initSegments();
        putInitialPanel(mainVC);
        openConfiguration(ureq);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if(source == segments) {
            if (event instanceof SegmentViewEvent) {
                SegmentViewEvent sve = (SegmentViewEvent)event;
                String segmentCName = sve.getComponentName();
                Component clickedLink = mainVC.getComponent(segmentCName);
                if (clickedLink == configurationLink) {
                    openConfiguration(ureq);
                } else if (clickedLink == locationListLink){
                    openLocations(ureq);
                } else if (clickedLink == reportLink) {
                    openReport(ureq);
                }
            }
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == configurationController) {
            if (event == Event.CHANGED_EVENT) {
                initSegments();
            }
        }
    }

    @Override
    protected void doDispose() {

    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {

    }

    private void initSegments() {
        if (contactTracingModule.isEnabled()) {
            segments.addSegment(locationListLink, false);
            segments.addSegment(reportLink, false);
        } else {
            segments.removeSegment(locationListLink);
            segments.removeSegment(reportLink);
        }
    }

    private void openConfiguration(UserRequest ureq) {
        if (configurationController == null) {
            OLATResourceable ores = OresHelper.createOLATResourceableInstance("Configuration", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
            configurationController = new ContactTracingConfigurationController(ureq, bwControl);
            listenTo(configurationController);
        } else {
            addToHistory(ureq, configurationController);
        }

        mainVC.put("segmentCmp", configurationController.getInitialComponent());
    }

    private void openLocations(UserRequest ureq) {
        if (locationListController == null) {
            OLATResourceable ores = OresHelper.createOLATResourceableInstance("Locations", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
            locationListController = new ContactTracingLocationListController(ureq, bwControl);
            listenTo(locationListController);
        } else {
            addToHistory(ureq, locationListController);
        }

        mainVC.put("segmentCmp", locationListController.getInitialComponent());
    }

    private void openReport(UserRequest ureq) {
        if (reportController == null) {
            OLATResourceable ores = OresHelper.createOLATResourceableInstance("Report", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
            reportController = new ContactTracingReportController(ureq, bwControl);
            listenTo(reportController);
        } else {
            addToHistory(ureq, reportController);
        }

        mainVC.put("segmentCmp", reportController.getInitialComponent());
    }
}
