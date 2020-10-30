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
import java.util.Objects;

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
@Table(name = "o_ct_location")
public class ContactTracingLocationImpl implements ContactTracingLocation {

    private static final long serialVersionUID = -5722474172355407350L;

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

    @Column(name = "l_reference", nullable = true, unique = false, insertable = true, updatable = true)
    private String reference;
    @Column(name = "l_titel", nullable = true, unique = false, insertable = true, updatable = true)
    private String title;
    @Column(name = "l_building", nullable = true, unique = false, insertable = true, updatable = true)
    private String building;
    @Column(name = "l_room", nullable = true, unique = false, insertable = true, updatable = true)
    private String room;
    @Column(name = "l_sector", nullable = true, unique = false, insertable = true, updatable = true)
    private String sector;
    @Column(name = "l_table", nullable = true, unique = false, insertable = true, updatable = true)
    private String table;
    @Column(name = "l_seat_number", nullable = false, unique = false, insertable = true, updatable = true)
    private boolean seatNumberEnabled;
    @Column(name = "l_qr_id", nullable = false, unique = true, insertable = true, updatable = true)
    private String qrId;
    @Column(name = "l_qr_text", nullable = true, unique = false, insertable = true, updatable = true)
    private String qrText;
    @Column(name = "l_guests", nullable = false, unique = false, insertable = true, updatable = true)
    private boolean accessibleByGuest;
    @Column(name = "l_printed", nullable = false, unique = false, insertable = true, updatable = true)
    private boolean alreadyPrinted;


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
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public boolean isSeatNumberEnabled() {
    	return this.seatNumberEnabled;
    }
    
    @Override
    public void setSeatNumberEnabled(boolean seatNumberEnabled) {
    	this.seatNumberEnabled = seatNumberEnabled;
    }

    @Override
    public String getBuilding() {
        return building;
    }

    @Override
    public void setBuilding(String buildiung) {
        this.building = buildiung;
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
    public String getSector() {
        return sector;
    }

    @Override
    public void setSector(String sector) {
        this.sector = sector;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public void setTable(String table) {
        this.table = table;
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
    public String getQrText() {
        return qrText;
    }

    @Override
    public void setQrText(String qrText) {
        this.qrText = qrText;
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
    public boolean isAlreadyPrinted() {
        return alreadyPrinted;
    }

    @Override
    public void setAlreadyPrinted(boolean alreadyPrinted) {
        this.alreadyPrinted = alreadyPrinted;
    }

    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return equals(persistable);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof ContactTracingLocation) {
            return getKey() != null && getKey().equals(((ContactTracingLocation)object).getKey());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
