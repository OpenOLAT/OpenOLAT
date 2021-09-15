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
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.olat.modules.contacttracing.model.ContactTracingRegistrationImpl;
import org.olat.modules.immunityProof.ImmunityProofModule.ImmunityProofLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ContactTracingRegistrationDAO {

    @Autowired
    private DB dbInstance;

    public ContactTracingRegistration create(ContactTracingLocation location, Date startDate, Date deletionDate, ImmunityProofLevel immunityProofLevel, Date immunityProofSafeDate) {
        ContactTracingRegistrationImpl entry = new ContactTracingRegistrationImpl();

        entry.setCreationDate(new Date());
        entry.setStartDate(startDate);
        entry.setDeletionDate(deletionDate);
        entry.setLocation(location);
        entry.setImmunityProofLevel(immunityProofLevel);
        entry.setImmunityProofDate(immunityProofSafeDate);

        return entry;
    }

    public ContactTracingRegistration persist(ContactTracingRegistration entry) {
        dbInstance.getCurrentEntityManager().persist(entry);

        return entry;
    }
    
    public ContactTracingRegistration getByKey(Long key) {
    	QueryBuilder queryBuilder = new QueryBuilder();
    	queryBuilder.append("select entry from contactTracingRegistration entry");
    	queryBuilder.and().append(" entry.key = :key");
    	
        List<ContactTracingRegistration> registrations = dbInstance.getCurrentEntityManager()
                .createQuery(queryBuilder.toString(), ContactTracingRegistration.class)
                .setParameter("key", key)
                .getResultList();

        return registrations == null || registrations.isEmpty() ? null : registrations.get(0);
    }

    public void deleteEntries(List<ContactTracingLocation> locations) {
        String query = new StringBuilder()
                .append("delete from contactTracingRegistration as entry ")
                .append("where entry.location in (:locationList)")
                .toString();

        dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("locationList", locations)
                .executeUpdate();
    }

    public int pruneEntries(Date deletionDate) {
        String query = new StringBuilder()
                .append("delete from contactTracingRegistration as entry ")
                .append("where entry.deletionDate < :deletionDate")
                .toString();

        return dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("deletionDate", deletionDate)
                .executeUpdate();
    }

    public long getRegistrationsCount(ContactTracingSearchParams searchParams) {
        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append("select count(entry) from contactTracingRegistration entry");
        appendWhere(queryBuilder, searchParams);

        TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
                .createQuery(queryBuilder.toString(), Long.class);
        appendParams(query, searchParams);
        return query.getSingleResult();
    }
      
    public long getRegistrationsWithProofCount(ContactTracingSearchParams searchParams, ImmunityProofLevel level) {
        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append("select count(entry) from contactTracingRegistration entry")
        			.where().append("(immunityProofLevel = :level");
        
        if (level.equals(ImmunityProofLevel.none)) {
        	queryBuilder.append(" or immunityProofLevel is null  or immunityProofLevel = ''");
        }
        queryBuilder.append(") ");
        
        appendWhere(queryBuilder, searchParams);

        TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
                .createQuery(queryBuilder.toString(), Long.class)
                .setParameter("level", level);
        
        appendParams(query, searchParams);
        return query.getSingleResult();
    }


    public List<ContactTracingRegistration> getRegistrations(ContactTracingSearchParams searchParams) {
        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append("select entry from contactTracingRegistration entry");
        appendWhere(queryBuilder, searchParams);

        TypedQuery<ContactTracingRegistration> query = dbInstance.getCurrentEntityManager()
                .createQuery(queryBuilder.toString(), ContactTracingRegistration.class);
        appendParams(query, searchParams);
        return query.getResultList();
    }
	
	private void appendWhere(QueryBuilder queryBuilder, ContactTracingSearchParams searchParams) {
		if (searchParams.getLocation() != null) {
            queryBuilder.where().append("entry.location=:locationToCheck");
        }
        if (searchParams.getStartDate() != null) {
            queryBuilder.where().append("entry.startDate >= :start");
        }
        if (searchParams.getEndDate() != null) {
            queryBuilder.where().append("entry.endDate <= :end");
        }
	}
	
	private void appendParams(TypedQuery<?> query, ContactTracingSearchParams searchParams) {
		if (searchParams.getLocation() != null) {
            query.setParameter("locationToCheck", searchParams.getLocation());
        }
        if (searchParams.getStartDate() != null) {
            query.setParameter("start", searchParams.getStartDate());
        }
        if (searchParams.getEndDate() != null) {
            query.setParameter("end", searchParams.getEndDate());
        }
	}

    public boolean anyRegistrationAvailable() {
        String query = "select registration.key from contactTracingRegistration as registration";

        List<Long> registrations = dbInstance.getCurrentEntityManager()
                .createQuery(query, Long.class)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();

        return !registrations.isEmpty();
    }
}
