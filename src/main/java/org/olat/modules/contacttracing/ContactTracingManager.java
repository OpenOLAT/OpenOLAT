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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.modules.contacttracing.model.ContactTracingLocationInfo;
import org.olat.modules.immunityProof.ImmunityProofModule.ImmunityProofLevel;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public interface ContactTracingManager {

    /**
     * Create and persist a new contact tracing location
     *
     * @param reference Reference of the location
     * @param title Title of the location
     * @param room Room of the location
     * @param building Building of the location
     * @param qrId QR ID of the location - must be unique
     * @return A persisted contact tracing location
     */
    public ContactTracingLocation createLocation(String reference, String title, String building, String room , String sector, String table, boolean seatNumberEnabled, String qrId, String qrText, boolean guestsAllowed);
    
    /**
     * Saves a contact tracing location
     * 
     * @param location
     * @return Persisted contact tracing location
     */
    public ContactTracingLocation importLocation(ContactTracingLocation location);

    /**
     * Save changes done to an existing location
     *
     * @param location Location with pending changes
     * @return Location with persisted changes
     */
    public ContactTracingLocation updateLocation(ContactTracingLocation location);

    /**
     * Get a contact tracing location by key
     *
     * @param locationKey Location key
     * @return Contact tracing location
     */
    public ContactTracingLocation getLocation(Long locationKey);

    /**
     * Get all contact tracing locations
     *
     * @return A list with all contact tracing locations
     */
    public List<ContactTracingLocation> getLocations();

    /**
     * Get a specific location by its unique identifier
     *
     * @param identifier Unique QR ID
     * @return Contact tracing location
     */
    public ContactTracingLocation getLocation(String identifier);

    /**
     * Get a list of locations which fit the provided search parameters
     * @param searchParams ContactTracingSearchParams
     * @return List of contact tracing locations
     */
    public List<ContactTracingLocation> getLocations(ContactTracingSearchParams searchParams);

    /**
     * Get a list of locations which fit the provided search parameters
     * @param searchParams ContactTracingSearchParams
     * @return List of contact tracing locations
     */
    public Map<ContactTracingLocation, ContactTracingLocationInfo> getLocationsWithRegistrations(ContactTracingSearchParams searchParams);

    /**
     * Get all contact tracing locations and their registrations count
     *
     * @return A map with all contact tracing locations and their registrations
     */
    public Map<ContactTracingLocation, ContactTracingLocationInfo> getLocationsWithRegistrations();

    /**
     * Delete the given locations
     *
     * @param locations Locations to delete
     */
    public void deleteLocations(List<ContactTracingLocation> locations);

    /**
     * Checks whether a given qrId already exists
     *
     * @param qrId QR ID to check
     * @return True: QR ID exists, False: QR ID does'nt exist
     */
    public boolean qrIdExists(String qrId);

    /**
     * Creates a new non-persisted empty registration
     * Details must be applied manually and then persisted
     *
     * @param location 				Contact tracing location must be provided to make sure it isn't empty
     * @param deletionDate 			Deletion date must be provided to make sure it isn´t empty
     * @param immunityProofLevel	level of covid certifcate 
     * @param immunityProofDate TODO
     * @return Empty 				contact tracing registration
     */
    public ContactTracingRegistration createRegistration(ContactTracingLocation location, Date startDate, Date deletionDate, ImmunityProofLevel immunityProofLevel, Date immunityProofDate);

    /**
     * Persist a given contact tracing registration
     *
     * @param registration Contact tracing registration
     * @return Update contact tracing registration
     */
    public ContactTracingRegistration persistRegistration(ContactTracingRegistration registration);

    /**
     * Count the registrations for the given parameters
     *
     * @param searchParams Given searchParams
     * @return Count of registrations
     */
    public long getRegistrationsCount(ContactTracingSearchParams searchParams);

    /**
     * Get the registrations matching given parameters
     *
     * @param searchParams Search parameters
     * @return List of ContactTracingRegistration
     */
    public List<ContactTracingRegistration> getRegistrations(ContactTracingSearchParams searchParams);

    /**
     * Deletes all entries whose retention period has expired
     *
     * @return Count of deleted entries
     */
    public int pruneRegistrations();

    /**
     * Used to determine whether any registration is available
     *
     * @return Boolean
     */
    public boolean anyRegistrationsAvailable();
}
