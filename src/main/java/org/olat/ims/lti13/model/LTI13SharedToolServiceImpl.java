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
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13SharedToolService;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ltisharedtoolserivce")
@Table(name="o_lti_shared_tool_service")
public class LTI13SharedToolServiceImpl implements LTI13SharedToolService, Persistable {

	private static final long serialVersionUID = 7807428168086180073L;

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
	
	@Column(name="l_context_id", nullable=true, insertable=true, updatable=true)
	private String contextId;
	@Column(name="l_service_type", nullable=false, insertable=true, updatable=false)
	private String type;
	@Column(name="l_service_endpoint", nullable=false, insertable=true, updatable=true)
	private String endpointUrl;
	
	@ManyToOne(targetEntity=LTI13SharedToolDeploymentImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_deployment_id", nullable=false, insertable=true, updatable=false)
	private LTI13SharedToolDeployment deployment;
	
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
	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public ServiceType getTypeEnum() {
		return type == null ? null : ServiceType.valueOf(type);
	}

	@Override
	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpoint(String url) {
		this.endpointUrl = url;
	}

	@Override
	public LTI13SharedToolDeployment getDeployment() {
		return deployment;
	}

	public void setDeployment(LTI13SharedToolDeployment deployment) {
		this.deployment = deployment;
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
		if(obj instanceof LTI13SharedToolServiceImpl) {
			LTI13SharedToolServiceImpl tool = (LTI13SharedToolServiceImpl)obj;
			return getKey() != null && getKey().equals(tool.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
