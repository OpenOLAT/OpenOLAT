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
package org.olat.modules.taxonomy.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;

/**
 * 
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="taxonomycompetenceauditlog")
@Table(name="o_tax_competence_audit_log")
public class TaxonomyCompetenceAuditLogImpl implements TaxonomyCompetenceAuditLog, Persistable {

	private static final long serialVersionUID = -1009831288341614553L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="t_action", nullable=true, insertable=true, updatable=false)
	private String action;	
	@Column(name="t_val_before", nullable=true, insertable=true, updatable=false)
	private String before;	
	@Column(name="t_val_after", nullable=true, insertable=true, updatable=false)
	private String after;
	@Column(name="t_message", nullable=true, insertable=true, updatable=false)
	private String message;
	
	@Column(name="fk_taxonomy", nullable=true, insertable=true, updatable=false)
	private Long taxonomyKey;
	@Column(name="fk_taxonomy_competence", nullable=true, insertable=true, updatable=false)
	private Long taxonomyCompetenceKey;

	@Column(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Long identityKey;
	@Column(name="fk_author", nullable=true, insertable=true, updatable=false)
	private Long authorKey;

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	@Override
	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Long getTaxonomyKey() {
		return taxonomyKey;
	}

	public void setTaxonomyKey(Long taxonomyKey) {
		this.taxonomyKey = taxonomyKey;
	}

	@Override
	public Long getTaxonomyCompetenceKey() {
		return taxonomyCompetenceKey;
	}

	public void setTaxonomyCompetenceKey(Long taxonomyCompetenceKey) {
		this.taxonomyCompetenceKey = taxonomyCompetenceKey;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public Long getAuthorKey() {
		return authorKey;
	}

	public void setAuthorKey(Long authorKey) {
		this.authorKey = authorKey;
	}

	@Override
	public int hashCode() {
		return key == null ? 236520 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TaxonomyCompetenceAuditLogImpl) {
			TaxonomyCompetenceAuditLogImpl auditLog = (TaxonomyCompetenceAuditLogImpl)obj;
			return key != null && key.equals(auditLog.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}