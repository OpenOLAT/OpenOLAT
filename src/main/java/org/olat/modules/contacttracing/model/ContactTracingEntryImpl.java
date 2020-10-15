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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Entity(name = "contactTracingEntry")
@Table(name = "o_contact_tracing_entry")
public class ContactTracingEntryImpl  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
    private Long key;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creationdate", nullable=false, insertable=true, updatable=false)
    private Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_deletion_date", nullable=false, insertable=true, updatable=false)
    private Date deletionDate;

    @ManyToOne(targetEntity = ContactTracingLocationImpl.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="fk_location", nullable=false, insertable=true, updatable=false)
    private ContactTracingLocation location;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_start_date", nullable=false, insertable=true, updatable=false)
    private Date startDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_end_date", nullable=true, insertable=true, updatable=false)
    private Date endDate;
    @Column(name = "l_nick_name", nullable = true, insertable = true, updatable = false)
    private String nickName;
    @Column(name = "l_first_name", nullable = true, insertable = true, updatable = false)
    private String firstName;
    @Column(name = "l_last_name", nullable = true, insertable = true, updatable = false)
    private String lastName;
    @Column(name = "l_street", nullable = true, insertable = true, updatable = false)
    private String street;
    @Column(name = "l_extra_line", nullable = true, insertable = true, updatable = false)
    private String extraLine;
    @Column(name = "l_zip_code", nullable = true, insertable = true, updatable = false)
    private String zipCode;
    @Column(name = "l_city", nullable = true, insertable = true, updatable = false)
    private String city;
    @Column(name = "l_email", nullable = true, insertable = true, updatable = false)
    private String email;
    @Column(name = "l_institutional_email", nullable = true, insertable = true, updatable = false)
    private String institutionalEmail;
    @Column(name = "l_generic_email", nullable = true, insertable = true, updatable = false)
    private String genericEmail;
    @Column(name = "l_private_phone", nullable = true, insertable = true, updatable = false)
    private String privatePhone;
    @Column(name = "l_mobile_phone", nullable = true, insertable = true, updatable = false)
    private String mobilePhone;
    @Column(name = "l_office_phone", nullable = true, insertable = true, updatable = false)
    private String officePhone;


    public Long getKey() {
        return key;
    }

    public Date getCreationDate() {
        return null;
    }

    public boolean equalsByPersistableKey(Persistable persistable) {
        return false;
    }
}
