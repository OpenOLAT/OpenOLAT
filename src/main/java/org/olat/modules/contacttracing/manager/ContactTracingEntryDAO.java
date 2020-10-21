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
import java.util.List;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.contacttracing.ContactTracingEntry;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.olat.modules.contacttracing.model.ContactTracingEntryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ContactTracingEntryDAO {

    @Autowired
    private DB dbInstance;

    public ContactTracingEntry createEntry(ContactTracingLocation location, Date startDate, Date deletionDate) {
        ContactTracingEntryImpl entry = new ContactTracingEntryImpl();

        entry.setCreationDate(new Date());
        entry.setStartDate(startDate);
        entry.setDeletionDate(deletionDate);
        entry.setLocation(location);

        dbInstance.getCurrentEntityManager().persist(entry);
        return entry;
    }

    public ContactTracingEntry updateEntry(ContactTracingEntry entry) {
        return dbInstance.getCurrentEntityManager().merge(entry);
    }

    public void deleteEntries(List<ContactTracingLocation> locations) {
        String query = new StringBuilder()
                .append("delete from contactTracingEntry as entry ")
                .append("where entry.location in (:locationList)")
                .toString();

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("locationList", locations)
                .executeUpdate();
    }

    public long getRegistrationsCount(ContactTracingSearchParams searchParams) {
        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append("select count(entry) from contactTracingEntry entry");
        if (searchParams.getLocation() != null) {
            queryBuilder.where().append("entry.location=:locationToCheck");
        }
        if (searchParams.getStartDate() != null) {
            queryBuilder.where().append("entry.startDate >= :start");
        }
        if (searchParams.getEndDate() != null) {
            queryBuilder.where().append("entry.endDate <= :end");
        }

        TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
                .createQuery(queryBuilder.toString(), Long.class);
        if (searchParams.getLocation() != null) {
            query.setParameter("locationToCheck", searchParams.getLocation());
        }
        if (searchParams.getStartDate() != null) {
            query.setParameter("start", searchParams.getStartDate());
        }
        if (searchParams.getEndDate() != null) {
            query.setParameter("end", searchParams.getEndDate());
        }
        return query.getSingleResult();
    }

    public List<ContactTracingEntry> getRegistrations(ContactTracingSearchParams searchParams) {
        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append("select entry from contactTracingEntry entry");
        if (searchParams.getLocation() != null) {
            queryBuilder.where().append("entry.location=:locationToCheck");
        }
        if (searchParams.getStartDate() != null) {
            queryBuilder.where().append("entry.startDate >= :start");
        }
        if (searchParams.getEndDate() != null) {
            queryBuilder.where().append("entry.endDate <= :end");
        }

        TypedQuery<ContactTracingEntry> query = dbInstance.getCurrentEntityManager()
                .createQuery(queryBuilder.toString(), ContactTracingEntry.class);
        if (searchParams.getLocation() != null) {
            query.setParameter("locationToCheck", searchParams.getLocation());
        }
        if (searchParams.getStartDate() != null) {
            query.setParameter("start", searchParams.getStartDate());
        }
        if (searchParams.getEndDate() != null) {
            query.setParameter("end", searchParams.getEndDate());
        }
        return query.getResultList();
    }
}
