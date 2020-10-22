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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public interface ContactTracingLocation extends Persistable, CreateInfo, ModifiedInfo {

    /**
     * @return Reference to location of QR code
     */
    public String getReference();

    /**
     * Set the reference of this location
     * @param reference
     */
    public void setReference(String reference);

    /**
     * @return Title of location
     */
    public String getTitle();

    /**
     * Set the title of this location
     * @param title
     */
    public void setTitle(String title);

    /**
     * @return Building of location
     */
    public String getBuilding();

    /**
     * Set the building of this location
     * @param buildiung
     */
    public void setBuilding(String buildiung);

    /**
     * @return Room of location
     */
    public String getRoom();

    /**
     * Set the room of this location
     * @param room
     */
    public void setRoom(String room);

    /**
     * Used to divide a room in sectors
     * @return Sector
     */
    public String getSector();

    /**
     * Set sector of a location
     * @param sector
     */
    public void setSector(String sector);

    /**
     * Used to divide sectors in tables
     * @return Table
     */
    public String getTable();

    /**
     * Set table of a location
     * @param table
     */
    public void setTable(String table);

    /**
     * Used as a part of the URL
     * @return Unique QR-ID
     */
    public String getQrId();

    /**
     * Get the text, which is displayed below the QR code
     * @return Displayed text
     */
    public String getQrText();

    /**
     * Set the text, which should be displayed below a QR Code
     * @param qrText Text, which should be displayed
     */
    public void setQrText(String qrText);

    /**
     * Set the qrId of this location
     * @param qrId Must be unique
     */
    public void setQrId(String qrId);

    /**
     * @return Whether guests can register at this location
     */
    public boolean isAccessibleByGuests();

    /**
     * Set whether this location is accessible by guests
     * @param accessibleByGuests
     */
    public void setAccessibleByGuests(boolean accessibleByGuests);

    /**
     * Get whether this location has already been printed
     * Used to warn the user making changes to the URL of a location which is already printed
     * @return Already printed
     */
    public boolean isAlreadyPrinted();

    /**
     * Set whether this location has already been printed
     * @param alreadyPrinted
     */
    public void setAlreadyPrinted(boolean alreadyPrinted);
}
