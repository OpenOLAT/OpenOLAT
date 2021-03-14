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
import org.olat.ims.lti13.LTI13SharedTool;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@Entity(name="ltisharedtool")
@Table(name="o_lti_shared_tool")
public class LTI13SharedToolImpl implements LTI13SharedTool, Persistable {

	private static final long serialVersionUID = -5258715933334862524L;

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
	
	@Column(name="l_issuer", nullable=false, insertable=true, updatable=true)
	private String issuer;
    @Column(name="l_client_id", nullable=false, insertable=true, updatable=true)
	private String clientId;

    @Column(name="l_key_id", nullable=true, insertable=true, updatable=true)
	private String keyId;
    @Column(name="l_public_key", nullable=true, insertable=true, updatable=true)
	private String publicKey;
    @Column(name="l_private_key", nullable=true, insertable=true, updatable=true)
	private String privateKey;
    
    @Column(name="l_authorization_uri", nullable=false, insertable=true, updatable=true)
	private String authorizationUri;
    @Column(name="l_token_uri", nullable=false, insertable=true, updatable=true)
	private String tokenUri;
    @Column(name="l_jwk_set_uri", nullable=false, insertable=true, updatable=true)
	private String jwkSetUri;
    
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
	public String getIssuer() {
		return issuer;
	}

	@Override
	public void setIssuer(String issuer) {
		this.issuer = issuer;
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
	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	@Override
	public String getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	@Override
	public String getAuthorizationUri() {
		return authorizationUri;
	}

	@Override
	public void setAuthorizationUri(String authorizationUri) {
		this.authorizationUri = authorizationUri;
	}

	@Override
	public String getTokenUri() {
		return tokenUri;
	}

	@Override
	public void setTokenUri(String tokenUri) {
		this.tokenUri = tokenUri;
	}

	@Override
	public String getJwkSetUri() {
		return jwkSetUri;
	}

	@Override
	public void setJwkSetUri(String jwkSetUri) {
		this.jwkSetUri = jwkSetUri;
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
	public int hashCode() {
		return key == null ? 265379 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof LTI13SharedToolImpl) {
			LTI13SharedToolImpl tool = (LTI13SharedToolImpl)obj;
			return getKey() != null && getKey().equals(tool.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
