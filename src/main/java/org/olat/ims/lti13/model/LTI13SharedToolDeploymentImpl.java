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
package org.olat.ims.lti13.model;

import java.util.Date;
import java.util.List;

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
import javax.persistence.Transient;

import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ltisharedtooldeployment")
@Table(name="o_lti_shared_tool_deployment")
public class LTI13SharedToolDeploymentImpl implements LTI13SharedToolDeployment, Persistable {

	private static final long serialVersionUID = -6482256605058165492L;

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

    @Column(name="l_deployment_id", nullable=false, insertable=true, updatable=false)
	private String deploymentId;
	
	@ManyToOne(targetEntity=LTI13PlatformImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_platform_id", nullable=false, insertable=true, updatable=false)
	private LTI13Platform platform;
	
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_entry_id", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_group_id", nullable=true, insertable=true, updatable=false)
	private BusinessGroup businessGroup;

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
	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	@Override
	public LTI13Platform getPlatform() {
		return platform;
	}

	public void setPlatform(LTI13Platform platform) {
		this.platform = platform;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	@Override
	@Transient
	public String getToolUrl() {
		OLATResourceable ores;
		if(getEntry() != null) {
			ores = getEntry();
		} else if(getBusinessGroup() != null) {
			ores = getBusinessGroup();
		} else {
			return null;
		}
		
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
		return BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
	}

	@Override
	public int hashCode() {
		return key == null ? 265379 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof LTI13SharedToolDeploymentImpl) {
			LTI13SharedToolDeploymentImpl deployment = (LTI13SharedToolDeploymentImpl)obj;
			return getKey() != null && getKey().equals(deployment.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
