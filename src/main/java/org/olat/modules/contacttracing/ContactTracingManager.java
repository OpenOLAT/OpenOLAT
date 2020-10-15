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
    public ContactTracingLocation createLocation(String reference, String title, String room , String building, String qrId, boolean guestsAllowed);

    /**
     * Save changes done to an existing location
     *
     * @param location Location with pending changes
     * @return Location with persisted changes
     */
    public ContactTracingLocation updateLocation(ContactTracingLocation location);

    /**
     * Get all contact tracing locations
     *
     * @return A list with all contact tracing locations
     */
    public List<ContactTracingLocation> getLocations();

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
     * Count the registrations for the given location
     *
     * @param location Given location
     * @return Count of registrations
     */
    public long getRegistrations(ContactTracingLocation location);
}
