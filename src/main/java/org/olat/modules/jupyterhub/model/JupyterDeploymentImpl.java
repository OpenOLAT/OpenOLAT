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
package org.olat.modules.jupyterhub.model;

import java.io.Serial;
import java.util.Date;

import org.olat.core.id.Persistable;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterHub;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name = "jupyterdeployment")
@Table(name = "o_jup_deployment")
public class JupyterDeploymentImpl implements Persistable, JupyterDeployment {

	@Serial
	private static final long serialVersionUID = -2768800440094333009L;

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

	@Column(name = "j_description", nullable = false, insertable = true, updatable = true)
	private String description;

	@Column(name = "j_image", nullable = false, insertable = true, updatable = true)
	private String image;

	@Column(name = "j_suppress_data_transmission_agreement", nullable = true, insertable = true, updatable = true)
	private Boolean suppressDataTransmissionAgreement;

	@ManyToOne(targetEntity = JupyterHubImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_hub", nullable = false, insertable = true, updatable = true)
	private JupyterHub jupyterHub;

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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

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
	public String getImage() {
		return image;
	}

	@Override
	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public Boolean getSuppressDataTransmissionAgreement() {
		return suppressDataTransmissionAgreement;
	}

	@Override
	public void setSuppressDataTransmissionAgreement(Boolean suppressDataTransmissionAgreement) {
		this.suppressDataTransmissionAgreement = suppressDataTransmissionAgreement;
	}

	@Override
	public JupyterHub getJupyterHub() {
		return jupyterHub;
	}

	@Override
	public void setJupyterHub(JupyterHub jupyterHub) {
		this.jupyterHub = jupyterHub;
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
		if (obj instanceof JupyterDeployment jupyterDeployment) {
			return getKey() != null && getKey().equals(jupyterDeployment.getKey());
		}
		return false;
	}
}
