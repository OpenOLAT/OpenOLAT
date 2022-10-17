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

import org.olat.core.id.Persistable;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomProfile;

import jakarta.persistence.*;
import java.util.Date;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
@Entity(name = "zoomconfig")
@Table(name = "o_zoom_config")
public class ZoomConfigImpl implements Persistable, ZoomConfig {

    private static final long serialVersionUID = 8572187883083481593L;

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

    @Column(name = "z_description", nullable = false, insertable = true, updatable = true)
    private String description;

    @ManyToOne(targetEntity = ZoomProfileImpl.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_profile", nullable = false, insertable = true, updatable = true)
    private ZoomProfile profile;

    @OneToOne(targetEntity = LTI13ToolDeploymentImpl.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_lti_tool_deployment_id", nullable = false, insertable = true, updatable = false)
    private LTI13ToolDeployment ltiToolDeployment;

    @Override
    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
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
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public ZoomProfile getProfile() {
        return profile;
    }

    @Override
    public void setProfile(ZoomProfile profile) {
        this.profile = profile;
    }

    @Override
    public LTI13ToolDeployment getLtiToolDeployment() {
        return ltiToolDeployment;
    }

    @Override
    public void setLtiToolDeployment(LTI13ToolDeployment ltiToolDeployment) {
        this.ltiToolDeployment = ltiToolDeployment;
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
        if (obj instanceof ZoomConfigImpl) {
            ZoomConfigImpl context = (ZoomConfigImpl) obj;
            return getKey() != null && getKey().equals(context.getKey());
        }
        return false;
    }
}
