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
package org.olat.modules.contacttracing.manager;

import java.util.List;

import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ContactTracingManagerImpl implements ContactTracingManager {
    @Autowired
    private ContactTracingLocationDAO contactTracingLocationDAO;
    @Autowired
    private ContactTracingEntryDAO contactTracingEntryDAO;

    @Override
    public ContactTracingLocation createLocation(String reference, String title, String room, String building, String qrId, boolean guestsAllowed) {
        if (qrIdExists(qrId)) {
            return null;
        } else {
            return contactTracingLocationDAO.createAndPersistLocation(reference, title, room, building, qrId, guestsAllowed);
        }
    }

    @Override
    public ContactTracingLocation updateLocation(ContactTracingLocation location) {
        return contactTracingLocationDAO.updateLocation(location);
    }

    @Override
    public void deleteLocations(List<ContactTracingLocation> locations) {
        contactTracingLocationDAO.deleteLocations(locations);
    }

    @Override
    public List<ContactTracingLocation> getLocations() {
        return contactTracingLocationDAO.getAllLocations();
    }

    @Override
    public boolean qrIdExists(String qrId) {
        return contactTracingLocationDAO.qrIdExists(qrId);
    }

    @Override
    public long getRegistrations(ContactTracingLocation location) {
        return contactTracingEntryDAO.getRegistrations(location);
    }


}
