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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.olat.NewControllerFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingContextEntryControllerCreator;
import org.olat.modules.contacttracing.ContactTracingEntry;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ContactTracingManagerImpl implements ContactTracingManager {

    public static final String CONTACT_TRACING_CONTEXT_KEY = "ContactTracing";

    @Autowired
    private ContactTracingLocationDAO contactTracingLocationDAO;
    @Autowired
    private ContactTracingEntryDAO contactTracingEntryDAO;

    @PostConstruct
    private void init() {
        NewControllerFactory.getInstance().addContextEntryControllerCreator(CONTACT_TRACING_CONTEXT_KEY, new ContactTracingContextEntryControllerCreator(this));
    }

     // Contact tracing locations

    @Override
    public ContactTracingLocation createLocation(String reference, String title, String room, String building, String qrId, String qrText, boolean guestsAllowed) {
        if (qrIdExists(qrId)) {
            return null;
        } else {
            return contactTracingLocationDAO.createAndPersistLocation(reference, title, room, building, qrId, qrText, guestsAllowed);
        }
    }

    @Override
    public ContactTracingLocation updateLocation(ContactTracingLocation location) {
        return contactTracingLocationDAO.updateLocation(location);
    }

    @Override
    public void deleteLocations(List<ContactTracingLocation> locations) {
        contactTracingEntryDAO.deleteEntries(locations);
        contactTracingLocationDAO.deleteLocations(locations);
    }

    @Override
    public ContactTracingLocation getLocation(Long locationKey) {
        return contactTracingLocationDAO.getLocation(locationKey);
    }

    @Override
    public ContactTracingLocation getLocation(String identifier) {
        if(StringHelper.containsNonWhitespace(identifier)) {
            return contactTracingLocationDAO.getLocation(identifier);
        }

        return null;
    }

    @Override
    public List<ContactTracingLocation> getLocation() {
        return contactTracingLocationDAO.getAllLocations();
    }

    @Override
    public List<ContactTracingLocation> getLocation(ContactTracingSearchParams searchParams) {
        return contactTracingLocationDAO.getLocations(searchParams);
    }

    @Override
    public Map<ContactTracingLocation, Long> getLocationsWithRegistrations() {
        Map<ContactTracingLocation, Long> locationRegistrationMap = new HashMap<>();
        ContactTracingSearchParams searchParams = new ContactTracingSearchParams();

        for (ContactTracingLocation location : getLocation()) {
            searchParams.setLocation(location);
            locationRegistrationMap.put(location, contactTracingEntryDAO.getRegistrationsCount(searchParams));
        }

        return locationRegistrationMap;
    }

    @Override
    public Map<ContactTracingLocation, Long> getLocationsWithRegistrations(ContactTracingSearchParams searchParams) {
        Map<ContactTracingLocation, Long> locationRegistrationMap = new HashMap<>();

        for (ContactTracingLocation location : getLocation(searchParams)) {
            searchParams.setLocation(location);
            locationRegistrationMap.put(location, contactTracingEntryDAO.getRegistrationsCount(searchParams));
        }

        return locationRegistrationMap;
    }

    @Override
    public boolean qrIdExists(String qrId) {
        return contactTracingLocationDAO.qrIdExists(qrId);
    }

    @Override
    public long getRegistrationsCount(ContactTracingSearchParams searchParams) {
        return contactTracingEntryDAO.getRegistrationsCount(searchParams);
    }


    // Contact tracing entries

    @Override
    public ContactTracingEntry createEntry(ContactTracingLocation location, Date startDate, Date deletionDate) {
        return contactTracingEntryDAO.createEntry(location, startDate, deletionDate);
    }

    @Override
    public ContactTracingEntry updateEntry(ContactTracingEntry entry) {
        return contactTracingEntryDAO.updateEntry(entry);
    }

    @Override
    public List<ContactTracingEntry> getRegistrations(ContactTracingSearchParams searchParams) {
        return contactTracingEntryDAO.getRegistrations(searchParams);
    }
}
