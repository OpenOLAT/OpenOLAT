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

import java.util.*;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Persistable;
import org.olat.modules.contacttracing.ContactTracingLocation;
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

    public ContactTracingLocation createAndPersistLocation(String reference, String title, String room, String building, String qrId, boolean guestsAllowed) {
        ContactTracingLocationImpl contactTracingLocation = new ContactTracingLocationImpl();
        contactTracingLocation.setCreationDate(new Date());
        contactTracingLocation.setLastModified(contactTracingLocation.getCreationDate());
        contactTracingLocation.setReference(reference);
        contactTracingLocation.setTitle(title);
        contactTracingLocation.setRoom(room);
        contactTracingLocation.setBuildiung(building);
        contactTracingLocation.setQrId(qrId);
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

    public boolean qrIdExists(String qrId) {
        String query = new StringBuilder()
                .append("select count(location) from contactTracingLocation location ")
                .append("where qrId=:qrIdToCheck")
                .toString();

        Long quantity = dbInstance.getCurrentEntityManager()
                .createQuery(query, Long.class)
                .setParameter("qrIdToCheck", qrId)
                .getSingleResult();

        return quantity > 0;
    }
}
