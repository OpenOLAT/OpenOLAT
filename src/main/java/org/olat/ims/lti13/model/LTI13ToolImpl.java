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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolType;

/**
 * 
 * Initial date: 18 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@Entity(name="ltitool")
@Table(name="o_lti_tool")
public class LTI13ToolImpl implements LTI13Tool, Persistable {

	private static final long serialVersionUID = -5258715933334862524L;
	private static final Logger log = Tracing.createLoggerFor(LTI13ToolImpl.class);

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
	
	@Column(name="l_tool_type", nullable=false, insertable=true, updatable=true)
	private String toolType;
	@Column(name="l_tool_name", nullable=false, insertable=true, updatable=true)
	private String toolName;
	@Column(name="l_tool_url", nullable=false, insertable=true, updatable=true)
	private String toolUrl;
    @Column(name="l_client_id", nullable=false, insertable=true, updatable=true)
	private String clientId;

    @Column(name="l_public_key", nullable=true, insertable=true, updatable=true)
	private String publicKey;
    @Column(name="l_public_key_url", nullable=true, insertable=true, updatable=true)
	private String publicKeyUrl;
    @Column(name="l_public_key_type", nullable=true, insertable=true, updatable=true)
	private String publicKeyType;
    
    @Column(name="l_initiate_login_url", nullable=false, insertable=true, updatable=true)
	private String initiateLoginUrl;
	
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

	public String getToolType() {
		return toolType;
	}

	public void setToolType(String toolType) {
		this.toolType = toolType;
	}

	@Override
	public LTI13ToolType getToolTypeEnum() {
		return LTI13ToolType.valueOf(toolType);
	}
	
	public void setToolTypeEnum(LTI13ToolType type) {
		this.toolType = type.name();
	}

	@Override
	public String getToolName() {
		return toolName;
	}

	@Override
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	@Override
	public String getToolUrl() {
		return toolUrl;
	}

	@Override
	public void setToolUrl(String url) {
		this.toolUrl = url;
	}
	
	@Override
	@Transient
	public String getToolDomain() {
		if(StringHelper.containsNonWhitespace(toolUrl)) {
			try {
				URL uri = new URL(toolUrl);
				return uri.getHost();
			} catch (MalformedURLException e) {
				log.error("", e);
				return null;
			}
		}
		return null;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String getPublicKeyUrl() {
		return publicKeyUrl;
	}
	
	public void setPublicKeyUrl(String publicKeyUrl) {
		this.publicKeyUrl = publicKeyUrl;
	}
	
	@Override
	public PublicKeyType getPublicKeyTypeEnum() {
		return publicKeyType == null ? PublicKeyType.KEY : PublicKeyType.valueOf(publicKeyType);
	}
	
	public void setPublicKeyTypeEnum(PublicKeyType publicKeyType) {
		this.publicKeyType = publicKeyType == null ? null : publicKeyType.name();
	}
	
	public String getPublicKeyType() {
		return publicKeyType;
	}
	
	public void setPublicKeyType(String publicKeyType) {
		this.publicKeyType = publicKeyType;
	}

	@Override
	public String getInitiateLoginUrl() {
		return initiateLoginUrl;
	}

	@Override
	public void setInitiateLoginUrl(String initiateLoginUrl) {
		this.initiateLoginUrl = initiateLoginUrl;
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
		if(obj instanceof LTI13ToolImpl) {
			LTI13ToolImpl tool = (LTI13ToolImpl)obj;
			return getKey() != null && getKey().equals(tool.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
