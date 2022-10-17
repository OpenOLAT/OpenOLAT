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
package org.olat.core.commons.services.csp.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="csplog")
@Table(name="o_csp_log")
public class CSPLogImpl implements CSPLog, Persistable {

	private static final long serialVersionUID = -8373081120456945576L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="l_blocked_uri", nullable=true, insertable=true, updatable=false)
	private String blockedUri;
	@Column(name="l_disposition", nullable=true, insertable=true, updatable=false)
	private String disposition;
	@Column(name="l_document_uri", nullable=true, insertable=true, updatable=false)
	private String documentUri;
	@Column(name="l_effective_directive", nullable=true, insertable=true, updatable=false)
	private String effectiveDirective;
	@Column(name="l_original_policy", nullable=true, insertable=true, updatable=false)
	private String originalPolicy;
	@Column(name="l_referrer", nullable=true, insertable=true, updatable=false)
	private String referrer;
	@Column(name="l_script_sample", nullable=true, insertable=true, updatable=false)
	private String scriptSample;
	@Column(name="l_status_code", nullable=true, insertable=true, updatable=false)
	private String statusCode;
	@Column(name="l_violated_directive", nullable=true, insertable=true, updatable=false)
	private String violatedDirective;
	@Column(name="l_source_file", nullable=true, insertable=true, updatable=false)
	private String sourceFile;
	@Column(name="l_line_number", nullable=true, insertable=true, updatable=false)
	private Long lineNumber;
	@Column(name="l_column_number", nullable=true, insertable=true, updatable=false)
	private Long columnNumber;
	
	@Column(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Long identityKey;
	
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
	public String getBlockedUri() {
		return blockedUri;
	}

	public void setBlockedUri(String blockedUri) {
		this.blockedUri = blockedUri;
	}

	@Override
	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	@Override
	public String getDocumentUri() {
		return documentUri;
	}

	public void setDocumentUri(String documentUri) {
		this.documentUri = documentUri;
	}

	@Override
	public String getEffectiveDirective() {
		return effectiveDirective;
	}

	public void setEffectiveDirective(String effectiveDirective) {
		this.effectiveDirective = effectiveDirective;
	}

	@Override
	public String getOriginalPolicy() {
		return originalPolicy;
	}

	public void setOriginalPolicy(String originalPolicy) {
		this.originalPolicy = originalPolicy;
	}

	@Override
	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	@Override
	public String getScriptSample() {
		return scriptSample;
	}

	public void setScriptSample(String scriptSample) {
		this.scriptSample = scriptSample;
	}

	@Override
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public String getViolatedDirective() {
		return violatedDirective;
	}

	public void setViolatedDirective(String violatedDirective) {
		this.violatedDirective = violatedDirective;
	}

	@Override
	public Long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Long lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public Long getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(Long columnNumber) {
		this.columnNumber = columnNumber;
	}

	@Override
	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2679111 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CSPLogImpl) {
			CSPLogImpl log = (CSPLogImpl)obj;
			return getKey() != null && getKey().equals(log.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
