/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.basesecurity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
@Entity(name="authentication")
@Table(name="o_bs_authentication")
public class AuthenticationImpl implements Authentication {

	private static final long serialVersionUID = 7969409958077836798L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="provider", nullable=false, insertable=true, updatable=false)
	private String provider;
	@Column(name="issuer", nullable=false, insertable=true, updatable=false)
	private String issuer;
	@Column(name="authusername", nullable=false, insertable=true, updatable=true)
	private String authusername;
	@Column(name="credential", nullable=true, insertable=true, updatable=true)
	private String credential;
	@Column(name="salt", nullable=true, insertable=true, updatable=true)
	private String salt;
	@Column(name="hashalgorithm", nullable=true, insertable=true, updatable=true)
	private String algorithm;
	
	@Column(name="w_user_handle", nullable=false, insertable=true, updatable=true)
	private byte[] userHandle;
	@Column(name="w_credential_id", nullable=false, insertable=true, updatable=true)
	private byte[] credentialId;
	@Column(name="w_aaguid", nullable=false, insertable=true, updatable=true)
	private byte[] aaGuid;
	@Column(name="w_cose_key", nullable=false, insertable=true, updatable=true)
	private byte[] coseKey;

	@Column(name="w_counter", nullable=false, insertable=true, updatable=true)
	private long counter;

	@Column(name="w_attestation_object", nullable=false, insertable=true, updatable=true)
	private String attestationObject;
	@Column(name="w_client_extensions", nullable=false, insertable=true, updatable=true)
	private String clientExtensions;
	@Column(name="w_authenticator_extensions", nullable=false, insertable=true, updatable=true)
	private String authenticatorExtensions;
	@Column(name="w_transports", nullable=false, insertable=true, updatable=true)
	private String transports;
	
	@Column(name="externalid", nullable=true, insertable=true, updatable=true)
	private String externalId;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="identity_fk", nullable=false, insertable=true, updatable=false)
	private Identity identity;

	public AuthenticationImpl() {
	//  
	}
	
	AuthenticationImpl(Identity identity, String provider, String issuer, String externalId,
			String authusername, String credentials) {
		this(identity, provider, issuer, externalId, authusername, credentials, null, null);
	}

	public AuthenticationImpl(Identity identity, String provider, String issuer, String externalId,
			String authusername, String credential, String salt, String algorithm) {
		
		if (provider.length() > 8) {
			// this implementation allows only 8 characters, as defined in hibernate file
			throw new AssertException("Authentication provider '" + provider + "' to long, only 8 characters supported!");
		}
		this.identity = identity;
		this.provider = provider;
		this.issuer = issuer;
		this.externalId = externalId;
		this.authusername = authusername;
		this.credential = credential;
		this.salt = salt;
		this.algorithm = algorithm;
	}
	
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
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * @return
	 */
	@Override
	public String getAuthusername() {
		return authusername;
	}

	/**
	 * @return
	 */
	@Override
	public String getProvider() {
		return provider;
	}

	/**
	 * for hibernate only (can never be changed, but set only)
	 * 
	 * @param string
	 */
	@Override
	public void setAuthusername(String string) {
		authusername = string;
	}

	/**
	 * for hibernate only (can never be changed, but set only)
	 * 
	 * @param string
	 */
	@Override
	public void setProvider(String string) {
		provider = string;
	}
	
	@Override
	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * for hibernate only
	 * 
	 * @return
	 */
	@Override
	public String getCredential() {
		return credential;
	}

	/**
	 * @param string
	 */
	@Override
	public void setCredential(String string) {
		credential = string;
	}

	@Override
	public String getSalt() {
		return salt;
	}

	@Override
	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public String getAlgorithm() {
		return algorithm;
	}

	@Override
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public byte[] getUserHandle() {
		return userHandle;
	}

	public void setUserHandle(byte[] userHandle) {
		this.userHandle = userHandle;
	}

	public byte[] getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(byte[] credentialId) {
		this.credentialId = credentialId;
	}

	public byte[] getAaGuid() {
		return aaGuid;
	}

	public void setAaGuid(byte[] aaGuid) {
		this.aaGuid = aaGuid;
	}

	public byte[] getCoseKey() {
		return coseKey;
	}

	public void setCoseKey(byte[] coseKey) {
		this.coseKey = coseKey;
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	/**
	 * Serialized AttestationObject in CBOR format and stringufied
	 * in Base64 url form.
	 * 
	 * @return
	 */
	public String getAttestationObject() {
		return attestationObject;
	}

	public void setAttestationObject(String attestationObject) {
		this.attestationObject = attestationObject;
	}

	public String getClientExtensions() {
		return clientExtensions;
	}

	public void setClientExtensions(String clientExtensions) {
		this.clientExtensions = clientExtensions;
	}

	public String getAuthenticatorExtensions() {
		return authenticatorExtensions;
	}

	public void setAuthenticatorExtensions(String authenticatorExtensions) {
		this.authenticatorExtensions = authenticatorExtensions;
	}

	public String getTransports() {
		return transports;
	}

	public void setTransports(String transports) {
		this.transports = transports;
	}
	
	public List<String> getTransportsList() {
		List<String> transportsList = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(getTransports())) {
			String[] transportsArr = getTransports().split("[,]");
			
			for(String transport:transportsArr) {
				if(StringHelper.containsNonWhitespace(transport)) {
					transportsList.add(transport);
				}
			}
		}
		if(transportsList.isEmpty()) {
			transportsList.add("internal");
			transportsList.add("hybrid");
			transportsList.add("nfc");
			transportsList.add("usb");
			transportsList.add("bte");
		}
		return transportsList;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public String toString() {
		return "auth: provider:" + provider + " ,authusername:" + authusername + ", hashpwd:" + credential + " ," + super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 20818 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Authentication auth) {
			return getKey() != null && getKey().equals(auth.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}