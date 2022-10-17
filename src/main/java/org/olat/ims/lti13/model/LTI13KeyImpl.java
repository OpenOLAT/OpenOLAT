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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.ims.lti13.LTI13Key;

/**
 * 
 * Initial date: 9 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ltikey")
@Table(name="o_lti_key")
public class LTI13KeyImpl implements LTI13Key, Persistable {

	private static final long serialVersionUID = -8572944283462080996L;

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
	
    @Column(name="l_key_id", nullable=true, insertable=true, updatable=true)
	private String keyId;
    @Column(name="l_public_key", nullable=true, insertable=true, updatable=true)
	private String publicKeyText;
    @Column(name="l_private_key", nullable=true, insertable=true, updatable=true)
	private String privateKeyText;
    @Column(name="l_algorithm", nullable=true, insertable=true, updatable=true)
	private String algorithm;
    @Column(name="l_issuer", nullable=true, insertable=true, updatable=true)
	private String issuer;
	
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
	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	
	@Transient
	public void setKeyPair(String algorithm, KeyPair keyPair) {
		setAlgorithm(algorithm);
		setPublicKey(keyPair.getPublic());
		setPrivateKey(keyPair.getPrivate());
	}

	@Override
	@Transient
	public PublicKey getPublicKey() {
		return CryptoUtil.string2PublicKey(publicKeyText);
	}
	
	public void setPublicKey(PublicKey publicKey) {
		if(publicKey == null) {
			setPublicKeyText(null);
		} else {
			setPublicKeyText(CryptoUtil.getPublicEncoded(publicKey));
		}
	}

	public String getPublicKeyText() {
		return publicKeyText;
	}

	public void setPublicKeyText(String publicKeyText) {
		this.publicKeyText = publicKeyText;
	}

	@Override
	public PrivateKey getPrivateKey() {
		return StringHelper.containsNonWhitespace(privateKeyText)
				? CryptoUtil.string2PrivateKey(privateKeyText) : null;
	}
	
	public void setPrivateKey(PrivateKey privateKey) {
		if(privateKey == null) {
			setPrivateKeyText(null);
		} else {
			setPrivateKeyText(CryptoUtil.getPrivateEncoded(privateKey));
		}
	}

	public String getPrivateKeyText() {
		return privateKeyText;
	}

	public void setPrivateKeyText(String privateKeyText) {
		this.privateKeyText = privateKeyText;
	}

	@Override
	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
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
		if(obj instanceof LTI13KeyImpl) {
			LTI13KeyImpl k = (LTI13KeyImpl)obj;
			return getKey() != null && getKey().equals(k.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
