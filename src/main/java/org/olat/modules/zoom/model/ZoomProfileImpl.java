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
package org.olat.modules.zoom.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.model.LTI13ToolImpl;
import org.olat.modules.zoom.ZoomProfile;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
@Entity(name = "zoomprofile")
@Table(name = "o_zoom_profile")
public class ZoomProfileImpl implements Persistable, ZoomProfile {

    private static final long serialVersionUID = 576730271492255402L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
    private Long key;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
    private Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
    private Date lastModified;

    @Column(name = "z_name", nullable = false, insertable = true, updatable = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "z_status", nullable = false, insertable = true, updatable = true)
    private ZoomProfileStatus status;

    @Column(name = "z_lti_key", nullable = false, insertable = true, updatable = true)
    private String ltiKey;

    @Column(name = "z_mail_domains", nullable = true, insertable = true, updatable = true)
    private String mailDomains;

    @Column(name = "z_students_can_host", nullable = false, insertable = true, updatable = true)
    private boolean studentsCanHost;

    @Column(name = "z_token", nullable = false, insertable = true, updatable = true)
    private String token;

    @OneToOne(targetEntity = LTI13ToolImpl.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_lti_tool_id", nullable = false, insertable = true, updatable = false)
    private LTI13Tool ltiTool;

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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ZoomProfileStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ZoomProfileStatus status) {
        this.status = status;
    }

    @Override
    public String getLtiKey() {
        return ltiKey;
    }

    @Override
    public void setLtiKey(String ltiKey) {
        this.ltiKey = ltiKey;
    }

    @Override
    public String getMailDomains() {
        return mailDomains;
    }

    @Override
    public void setMailDomains(String mailDomains) {
        this.mailDomains = mailDomains;
    }

    @Override
    public boolean isStudentsCanHost() {
        return studentsCanHost;
    }

    @Override
    public void setStudentsCanHost(boolean studentsCanHost) {
        this.studentsCanHost = studentsCanHost;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
	public LTI13Tool getLtiTool() {
        return ltiTool;
    }

    @Override
	public void setLtiTool(LTI13Tool ltiTool) {
        this.ltiTool = ltiTool;
    }

    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return equals(persistable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ZoomProfileImpl) {
            ZoomProfileImpl profile = (ZoomProfileImpl) obj;
            return getKey() != null && getKey().equals(profile.getKey());
        }
        return false;
    }
}
