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
package org.olat.upgrade.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

/**
 *  This is the old taxonomy level for question pool.
 *  
 * Initial date: 20.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="upgradetaxonomylevel")
@Table(name="o_qp_taxonomy_level")
public class UpgradeTaxonomyLevel implements Persistable  {

	private static final long serialVersionUID = -1150399691749897973L;

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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="q_field", nullable=false, insertable=true, updatable=true)
	private String field;
	
	@Column(name="q_mat_path_ids", nullable=false, insertable=true, updatable=true)
	private String materializedPathKeys;
	@Column(name="q_mat_path_names", nullable=false, insertable=true, updatable=true)
	private String materializedPathNames;
	
	@ManyToOne(targetEntity=UpgradeTaxonomyLevel.class)
	@JoinColumn(name="fk_parent_field", nullable=true, insertable=true, updatable=true)
	private UpgradeTaxonomyLevel parentField;
	
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

	public void setLastModified(Date date) {
		this.lastModified = date;
	}
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public UpgradeTaxonomyLevel getParentField() {
		return parentField;
	}

	public void setParentField(UpgradeTaxonomyLevel parentField) {
		this.parentField = parentField;
	}

	public String getMaterializedPathKeys() {
		return materializedPathKeys;
	}

	public void setMaterializedPathKeys(String materializedPathKeys) {
		this.materializedPathKeys = materializedPathKeys;
	}

	public String getMaterializedPathNames() {
		return materializedPathNames;
	}

	public void setMaterializedPathNames(String materializedPathNames) {
		this.materializedPathNames = materializedPathNames;
	}

	@Override
	public int hashCode() {
		return key == null ? 97489 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UpgradeTaxonomyLevel) {
			UpgradeTaxonomyLevel f = (UpgradeTaxonomyLevel)obj;
			return key != null && key.equals(f.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("studyField[key=").append(this.key)
			.append("]").append(super.toString());
		return sb.toString();
	}
}
