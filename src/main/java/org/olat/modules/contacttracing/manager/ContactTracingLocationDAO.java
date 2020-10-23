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
import java.util.stream.Collectors;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Persistable;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.olat.modules.contacttracing.model.ContactTracingLocationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ContactTracingLocationDAO {
    @Autowired
    private DB dbInstance;

    public ContactTracingLocation createAndPersistLocation(String reference, String title, String building, String room, String sector, String table, String qrId, String qrText, boolean guestsAllowed) {
        ContactTracingLocationImpl contactTracingLocation = new ContactTracingLocationImpl();
        contactTracingLocation.setCreationDate(new Date());
        contactTracingLocation.setLastModified(contactTracingLocation.getCreationDate());
        contactTracingLocation.setReference(reference);
        contactTracingLocation.setTitle(title);
        contactTracingLocation.setBuilding(building);
        contactTracingLocation.setRoom(room);
        contactTracingLocation.setSector(sector);
        contactTracingLocation.setTable(table);
        contactTracingLocation.setQrId(qrId);
        contactTracingLocation.setQrText(qrText);
        contactTracingLocation.setAccessibleByGuests(guestsAllowed);

        dbInstance.getCurrentEntityManager().persist(contactTracingLocation);
        return contactTracingLocation;
    }

    public ContactTracingLocation updateLocation(ContactTracingLocation location) {
        location.setLastModified(new Date());

        return dbInstance.getCurrentEntityManager().merge(location);
    }

    public void deleteLocations(List<ContactTracingLocation> locations) {
        String query = new StringBuilder()
                .append("delete from contactTracingLocation location ")
                .append("where location.key in :deleteLocationKeys")
                .toString();

        List<Long> deleteLocationKeys = locations.stream().map(Persistable::getKey).collect(Collectors.toList());

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("deleteLocationKeys", deleteLocationKeys)
                .executeUpdate();
    }

    public List<ContactTracingLocation> getAllLocations() {
        String query = new StringBuilder()
                .append("from contactTracingLocation")
                .toString();

        return dbInstance.getCurrentEntityManager()
                .createQuery(query, ContactTracingLocation.class)
                .getResultList();
    }

    public ContactTracingLocation getLocation(Long locationKey) {
        String query = new StringBuilder()
                .append("select location from contactTracingLocation as location ")
                .append("where location.key=:locationKey")
                .toString();

        List<ContactTracingLocation> locations = dbInstance.getCurrentEntityManager()
                .createQuery(query, ContactTracingLocation.class)
                .setParameter("locationKey", locationKey)
                .getResultList();

        return locations == null || locations.isEmpty() ? null : locations.get(0);
    }

    public ContactTracingLocation getLocation(String identifier) {
        String query = new StringBuilder()
                .append("select location from contactTracingLocation as location ")
                .append("where location.qrId=:identifier")
                .toString();

        List<ContactTracingLocation> locations = dbInstance.getCurrentEntityManager()
                .createQuery(query, ContactTracingLocation.class)
                .setParameter("identifier", identifier)
                .getResultList();

        return locations == null || locations.isEmpty() ? null : locations.get(0);
    }

    public List<ContactTracingLocation> getLocations(ContactTracingSearchParams searchParams) {
        if (searchParams.isEmpty()) {
            return getAllLocations();
        }

        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append("select distinct location from contactTracingLocation as location ")
             .append("inner join contactTracingRegistration as registration on (location.key = registration.location.key)");

        if (searchParams.getFullTextSearch() != null) {
            queryBuilder.where()
                    .append("(")
                    .append("lower(location.reference) like :searchString or ")
                    .append("lower(location.title) like :searchString or ")
                    .append("lower(location.building) like :searchString or ")
                    .append("lower(location.room) like :searchString or ")
                    .append("lower(location.sector) like :searchString or ")
                    .append("lower(location.table) like :searchString")
                    .append(")");
        }
        if (searchParams.getReference() != null) {
            queryBuilder.where().append("location.reference = :referenceString");
        }
        if (searchParams.getTitle() != null) {
            queryBuilder.where().append("location.title = :titleString");
        }
        if (searchParams.getBuilding() != null) {
            queryBuilder.where().append("location.building = :buildingString");
        }
        if (searchParams.getRoom() != null) {
            queryBuilder.where().append("location.room = :roomString");
        }
        if (searchParams.getSector() != null) {
            queryBuilder.where().append("location.sector = :sectorString");
        }
        if (searchParams.getTable() != null) {
            queryBuilder.where().append("location.table = :tableString");
        }
        if (searchParams.getStartDate() != null) {
            queryBuilder.where().append("registration.startDate >= :start");
        }
        if (searchParams.getEndDate() != null) {
            queryBuilder.where().append("registration.endDate <= :end");
        }

        TypedQuery<ContactTracingLocation> query = dbInstance.getCurrentEntityManager().createQuery(queryBuilder.toString(), ContactTracingLocation.class);

        if (searchParams.getFullTextSearch() != null) {
            query.setParameter("searchString", PersistenceHelper.makeFuzzyQueryString(searchParams.getFullTextSearch()));
        }
        if (searchParams.getReference() != null) {
            query.setParameter("referenceString", PersistenceHelper.makeFuzzyQueryString(searchParams.getReference()));
        }
        if (searchParams.getTitle() != null) {
            query.setParameter("titleString", PersistenceHelper.makeFuzzyQueryString(searchParams.getTitle()));
        }
        if (searchParams.getBuilding() != null) {
            query.setParameter("buildingString", PersistenceHelper.makeFuzzyQueryString(searchParams.getBuilding()));
        }
        if (searchParams.getRoom() != null) {
            query.setParameter("roomString", PersistenceHelper.makeFuzzyQueryString(searchParams.getRoom()));
        }
        if (searchParams.getSector() != null) {
            query.setParameter("sectorString", PersistenceHelper.makeFuzzyQueryString(searchParams.getSector()));
        }
        if (searchParams.getTable() != null) {
            query.setParameter("tableString", PersistenceHelper.makeFuzzyQueryString(searchParams.getTable()));
        }
        if (searchParams.getStartDate() != null) {
            query.setParameter("start", searchParams.getStartDate(), TemporalType.DATE);
        }
        if (searchParams.getEndDate() != null) {
            query.setParameter("end", searchParams.getEndDate(), TemporalType.DATE);
        }

        return query.getResultList();
    }

    public boolean qrIdExists(String qrId) {
        String query = new StringBuilder()
                .append("select location.key from contactTracingLocation as location ")
                .append("where location.qrId=:qrIdToCheck")
                .toString();

        List<Long> locations = dbInstance.getCurrentEntityManager()
                .createQuery(query, Long.class)
                .setParameter("qrIdToCheck", qrId)
                .setMaxResults(1)
                .setFirstResult(0)
                .getResultList();

        return !locations.isEmpty();

    }
}
