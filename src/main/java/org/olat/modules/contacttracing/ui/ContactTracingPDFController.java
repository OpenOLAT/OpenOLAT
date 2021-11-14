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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingDispatcher;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 19.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingPDFController extends BasicController {

    @Autowired
    private ContactTracingModule contactTracingModule;

    public ContactTracingPDFController(UserRequest ureq, WindowControl wControl, List<ContactTracingLocation> locations) {
        super(ureq, wControl);

        VelocityContainer qrContainer = createVelocityContainer("contact_tracing_export_pdf_template");
        Map<ContactTracingLocation, String> locationUrlMap = new HashMap<>();

        for (ContactTracingLocation location : locations) {
            locationUrlMap.put(location, ContactTracingDispatcher.getRegistrationUrl(location.getQrId()));
        }

        qrContainer.contextPut("generalInstructions", StringHelper.xssScan(contactTracingModule.getQrCodeInstructions()));
        qrContainer.contextPut("locationList", locations);
        qrContainer.contextPut("locationUrlMap", locationUrlMap);

        putInitialPanel(qrContainer);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {

    }
}
