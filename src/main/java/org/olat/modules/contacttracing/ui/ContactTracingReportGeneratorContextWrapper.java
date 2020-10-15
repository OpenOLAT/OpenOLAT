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

import java.util.Date;
import java.util.List;

import org.olat.modules.contacttracing.ContactTracingEntry;
import org.olat.modules.contacttracing.ContactTracingLocation;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportGeneratorContextWrapper {

    private String locationSearch;
    private Date startDate;
    private Date endDate;

    private List<ContactTracingLocation> locations;
    private List<ContactTracingEntry> entries;

    public String getLocationSearch() {
        return locationSearch;
    }

    public void setLocationSearch(String locationSearch) {
        this.locationSearch = locationSearch;
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

    public List<ContactTracingLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<ContactTracingLocation> locations) {
        this.locations = locations;
    }

    public List<ContactTracingEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ContactTracingEntry> entries) {
        this.entries = entries;
    }
}
