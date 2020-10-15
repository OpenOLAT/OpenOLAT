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
package org.olat.modules.contacttracing.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.contacttracing.ContactTracingLocation;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Entity(name = "contactTracingLocation")
@Table(name = "o_contact_tracing_location")
public class ContactTracingLocationImpl implements ContactTracingLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
    private Long key;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creationdate", nullable=false, insertable=true, updatable=false)
    private Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
    private Date lastModified;

    @Column(name = "l_reference", nullable = false, unique = false, insertable = true, updatable = true)
    private String reference;
    @Column(name = "l_titel", nullable = false, unique = false, insertable = true, updatable = true)
    private String titel;
    @Column(name = "l_room", nullable = false, unique = false, insertable = true, updatable = true)
    private String room;
    @Column(name = "l_building", nullable = false, unique = false, insertable = true, updatable = true)
    private String building;
    @Column(name = "l_qr_id", nullable = false, unique = true, insertable = true, updatable = true)
    private String qrId;
    @Column(name = "l_guests", nullable = false, unique = false, insertable = true, updatable = true)
    private boolean accessibleByGuest;


    @Override
    public Long getKey() {
        return key;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String getTitle() {
        return titel;
    }

    @Override
    public void setTitle(String title) {
        this.titel = title;
    }

    @Override
    public String getRoom() {
        return room;
    }

    @Override
    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public String getBuilding() {
        return building;
    }

    @Override
    public void setBuildiung(String buildiung) {
        this.building = buildiung;
    }

    @Override
    public String getQrId() {
        return qrId;
    }

    @Override
    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    @Override
    public boolean isAccessibleByGuests() {
        return accessibleByGuest;
    }

    @Override
    public void setAccessibleByGuests(boolean accessibleByGuests) {
        this.accessibleByGuest = accessibleByGuests;
    }

    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return key.equals(persistable.getKey());
    }
}
