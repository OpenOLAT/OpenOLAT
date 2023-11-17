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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.olat.core.id.Persistable;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.model.LTI13ToolImpl;
import org.olat.modules.jupyterhub.JupyterHub;

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

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name ="jupyterhub")
@Table(name = "o_jup_hub")
public class JupyterHubImpl implements Persistable, JupyterHub {
	@Serial
	private static final long serialVersionUID = -3619131168535906345L;

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

	@Column(name = "j_name", nullable = false, insertable = true, updatable = true)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "j_status", nullable = false, insertable = true, updatable = true)
	private JupyterHubStatus status;

	@Column(name = "j_ram", nullable = false, insertable = true, updatable = true)
	private String ram;

	@Column(name = "j_cpu", nullable = false, insertable = true, updatable = true)
	private long cpu;

	@Column(name = "j_image_checking_service_url", nullable = true, insertable = true, updatable = true)
	private String imageCheckingServiceUrl;

	@Column(name = "j_info_text", nullable = true, insertable = true, updatable = true)
	private String infoText;

	@Column(name = "j_lti_key", nullable = true, insertable = true, updatable = true)
	private String ltiKey;

	@Column(name = "j_access_token", nullable = true, insertable = true, updatable = true)
	private String accessToken;

	@Enumerated(EnumType.STRING)
	@Column(name = "j_agreement_setting", nullable = false, insertable = true, updatable = true)
	private AgreementSetting agreementSetting;

	@OneToOne(targetEntity = LTI13ToolImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_lti_tool_id", nullable = false, insertable = true, updatable = false)
	private LTI13Tool ltiTool;

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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public JupyterHubStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(JupyterHubStatus status) {
		this.status = status;
	}

	@Override
	public String getRam() {
		return ram;
	}

	@Override
	public void setRam(String ram) {
		this.ram = ram;
	}

	@Override
	public BigDecimal getCpu() {
		return BigDecimal.valueOf(cpu, 2);
	}

	@Override
	public void setCpu(BigDecimal cpu) {
		if (cpu.scale() != 2) {
			cpu = cpu.setScale(2, RoundingMode.HALF_UP);
		}
		this.cpu = cpu.unscaledValue().longValue();
	}

	@Override
	public String getImageCheckingServiceUrl() {
		return imageCheckingServiceUrl;
	}

	@Override
	public void setImageCheckingServiceUrl(String imageCheckingServiceUrl) {
		this.imageCheckingServiceUrl = imageCheckingServiceUrl;
	}

	@Override
	public String getInfoText() {
		return infoText;
	}

	@Override
	public void setInfoText(String infoText) {
		this.infoText = infoText;
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
	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public AgreementSetting getAgreementSetting() {
		return agreementSetting;
	}

	@Override
	public void setAgreementSetting(AgreementSetting agreementSetting) {
		this.agreementSetting = agreementSetting;
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
	public String getJupyterHubUrl() {
		return ltiTool.getToolUrl();
	}

	@Override
	public void setJupyterHubUrl(String jupyterHubUrl) {
		ltiTool.setToolUrl(jupyterHubUrl);
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 3468812 : getKey().hashCode();
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
		if (obj instanceof JupyterHubImpl jupyterHub) {
			return getKey() != null && getKey().equals(jupyterHub.getKey());
		}
		return false;
	}
}