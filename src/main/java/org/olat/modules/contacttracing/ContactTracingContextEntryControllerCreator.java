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
package org.olat.modules.contacttracing;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.Tracing;
import org.olat.modules.contacttracing.ui.ContactTracingEntryController;

/**
 * Initial date: 19.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

    private static final Logger log = Tracing.createLoggerFor(ContextEntryControllerCreator.class);

    private final ContactTracingManager contactTracingManager;

    public ContactTracingContextEntryControllerCreator(ContactTracingManager contactTracingManager) {
        this.contactTracingManager = contactTracingManager;
    }

    @Override
    public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
        ContextEntry contextEntry = ces.get(0);
        OLATResourceable olatResourceable = contextEntry.getOLATResourceable();
        Long locationKey = olatResourceable.getResourceableId();

        ContactTracingLocation location = contactTracingManager.getLocation(locationKey);
        if (location == null) {
            DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
        }

        return new ContactTracingEntryController(ureq, wControl, location);
    }

    @Override
    public ContextEntryControllerCreator clone() {
        return new ContactTracingContextEntryControllerCreator(contactTracingManager);
    }
}
