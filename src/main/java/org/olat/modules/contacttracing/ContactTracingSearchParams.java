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

import org.olat.core.util.StringHelper;

/**
 * Initial date: 19.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingSearchParams {

    private ContactTracingLocation location;

    private String fullTextSearch;
    private String reference;
    private String title;
    private String building;
    private String room;
    private String sector;
    private String table;
    private Date startDate;
    private Date endDate;

    public ContactTracingLocation getLocation() {
        return location;
    }

    public void setLocation(ContactTracingLocation location) {
        this.location = location;
    }

    public String getFullTextSearch() {
        return fullTextSearch;
    }

    public void setFullTextSearch(String fullTextSearch) {
        this.fullTextSearch =fullTextSearch;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Checks whether any parameter is set
     * @return Boolean
     */
    public boolean isEmpty() {
        return  location == null &&
                startDate == null &&
                endDate == null &&
                !StringHelper.containsNonWhitespace(fullTextSearch) &&
                !StringHelper.containsNonWhitespace(reference) &&
                !StringHelper.containsNonWhitespace(title) &&
                !StringHelper.containsNonWhitespace(building) &&
                !StringHelper.containsNonWhitespace(room) &&
                !StringHelper.containsNonWhitespace(sector) &&
                !StringHelper.containsNonWhitespace(table);
    }
}
