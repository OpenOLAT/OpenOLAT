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

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.modules.contacttracing.ContactTracingRegistrationInternalWrapperControllerCreator;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.olat.modules.immunityProof.ImmunityProofModule.ImmunityProofLevel;
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
    public static final String CONTACT_TRACING_SELECTION_KEY = "Selection";
    private static final Logger log = Tracing.createLoggerFor(ContactTracingManager.class);

    @Autowired
    private ContactTracingLocationDAO contactTracingLocationDAO;
    @Autowired
    private ContactTracingRegistrationDAO contactTracingRegistrationDAO;

    @PostConstruct
    private void init() {
        NewControllerFactory.getInstance().addContextEntryControllerCreator(CONTACT_TRACING_CONTEXT_KEY, new ContactTracingRegistrationInternalWrapperControllerCreator(this));
    }

     // Contact tracing locations

    @Override
    public ContactTracingLocation createLocation(String reference, String title, String building, String room, String sector, String table, boolean seatNumberEnabled, String qrId, String qrText, boolean guestsAllowed) {
        if (qrIdExists(qrId)) {
            return null;
        } else {
            return contactTracingLocationDAO.createAndPersistLocation(reference, title, building, room, sector, table, seatNumberEnabled, qrId, qrText, guestsAllowed);
        }
    }
    
    @Override
    public ContactTracingLocation importLocation(ContactTracingLocation location) {
    	if (location == null || location.getQrId() == null) {
            return null;
        } else if (qrIdExists(location.getQrId())) {
        	// If location with this QR ID is already existing, reload and update
        	ContactTracingLocation updateLocation = getLocation(location.getQrId());
        	updateLocation.setReference(location.getReference());
        	updateLocation.setTitle(location.getTitle());
        	updateLocation.setBuilding(location.getBuilding());
        	updateLocation.setRoom(location.getRoom());
        	updateLocation.setSector(location.getSector());
        	updateLocation.setTable(location.getTable());
        	updateLocation.setSeatNumberEnabled(location.isSeatNumberEnabled());
        	updateLocation.setAccessibleByGuests(location.isAccessibleByGuests());
        	updateLocation.setQrText(location.getQrText());
        	
        	return contactTracingLocationDAO.updateLocation(updateLocation);
        } else {
            return contactTracingLocationDAO.createAndPersistLocation(
            		location.getReference(), 
            		location.getTitle(), 
            		location.getBuilding(), 
            		location.getRoom(), 
            		location.getSector(), 
            		location.getTable(), 
            		location.isSeatNumberEnabled(),
            		location.getQrId(), 
            		location.getQrText(), 
            		location.isAccessibleByGuests());
        }
    }

    @Override
    public ContactTracingLocation updateLocation(ContactTracingLocation location) {
        return contactTracingLocationDAO.updateLocation(location);
    }

    @Override
    public void deleteLocations(List<ContactTracingLocation> locations) {
        contactTracingRegistrationDAO.deleteEntries(locations);
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
    public List<ContactTracingLocation> getLocations() {
        return contactTracingLocationDAO.getAllLocations();
    }

    @Override
    public List<ContactTracingLocation> getLocations(ContactTracingSearchParams searchParams) {
        return contactTracingLocationDAO.getLocations(searchParams);
    }

    @Override
    public Map<ContactTracingLocation, Long> getLocationsWithRegistrations() {
        Map<ContactTracingLocation, Long> locationRegistrationMap = new HashMap<>();
        ContactTracingSearchParams searchParams = new ContactTracingSearchParams();

        for (ContactTracingLocation location : getLocations()) {
            searchParams.setLocation(location);
            locationRegistrationMap.put(location, contactTracingRegistrationDAO.getRegistrationsCount(searchParams));
        }

        return locationRegistrationMap;
    }

    @Override
    public Map<ContactTracingLocation, Long> getLocationsWithRegistrations(ContactTracingSearchParams searchParams) {
        Map<ContactTracingLocation, Long> locationRegistrationMap = new HashMap<>();

        for (ContactTracingLocation location : getLocations(searchParams)) {
            searchParams.setLocation(location);
            locationRegistrationMap.put(location, contactTracingRegistrationDAO.getRegistrationsCount(searchParams));
        }

        return locationRegistrationMap;
    }

    @Override
    public boolean qrIdExists(String qrId) {
    	// If empty ID is given, return true
    	if(StringHelper.containsNonWhitespace(qrId)) {
    		return contactTracingLocationDAO.qrIdExists(qrId);
    	}
    	
        return true;
    }

    @Override
    public long getRegistrationsCount(ContactTracingSearchParams searchParams) {
        return contactTracingRegistrationDAO.getRegistrationsCount(searchParams);
    }


    // Contact tracing entries

    @Override
    public ContactTracingRegistration createRegistration(ContactTracingLocation location, Date startDate, Date deletionDate, ImmunityProofLevel immunityProofLevel, Date immunityProofDate) {
        return contactTracingRegistrationDAO.create(location, startDate, deletionDate, immunityProofLevel);
    }

    @Override
    public ContactTracingRegistration persistRegistration(ContactTracingRegistration entry) {
        return contactTracingRegistrationDAO.persist(entry);
    }

    @Override
    public int pruneRegistrations() {
        // Set the border to delete the day after the retention period to 00:00
        // Delete everything which is older than the border
        Date deletionDate = DateUtils.setTime(new Date(), 0, 0, 0);

        int pruneCount = contactTracingRegistrationDAO.pruneEntries(deletionDate);
        log.info("{} contact tracing registrations pruned.", pruneCount);

        return pruneCount;
    }

    @Override
    public List<ContactTracingRegistration> getRegistrations(ContactTracingSearchParams searchParams) {
        return contactTracingRegistrationDAO.getRegistrations(searchParams);
    }

    @Override
    public boolean anyRegistrationsAvailable() {
        return contactTracingRegistrationDAO.anyRegistrationAvailable();
    }
}
