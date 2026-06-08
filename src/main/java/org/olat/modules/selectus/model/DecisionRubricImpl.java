/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model;

import java.util.Date;

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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rdecisionrubric")
@Table(name="o_selectus_decision_rubric")
public class DecisionRubricImpl implements DecisionRubric, Persistable {

	private static final long serialVersionUID = -3810274642995889964L;

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
	@Column(name="rubric_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="d_string_value", nullable=true, insertable=true, updatable=true)
	private String stringValue;
	@Column(name="d_integer_value", nullable=true, insertable=true, updatable=true)
	private Integer integerValue;
	
	@ManyToOne(targetEntity=DecisionRubricDefinitionImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_definition_id", nullable=false, insertable=true, updatable=false)
	private DecisionRubricDefinition definition;
	
	@ManyToOne(targetEntity=ApplicationLightImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_application_id", nullable=false, insertable=true, updatable=false)
	private ApplicationLight application;
	
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
	public String getStringValue() {
		return stringValue;
	}

	@Override
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public Integer getIntegerValue() {
		return integerValue;
	}

	@Override
	public void setIntegerValue(Integer integerValue) {
		this.integerValue = integerValue;
	}

	@Override
	public DecisionRubricDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(DecisionRubricDefinition definition) {
		this.definition = definition;
	}

	@Override
	public ApplicationLight getApplication() {
		return application;
	}

	public void setApplication(ApplicationLight application) {
		this.application = application;
	}

	@Override
	public int hashCode() {
		return key == null ? 365800 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DecisionRubricImpl) {
			DecisionRubricImpl rubrik = (DecisionRubricImpl)obj;
			return key != null && key.equals(rubrik.key);
		}
		return super.equals(obj);
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("decisionRubric[key=").append(getKey() == null ? "" : getKey());
		if(integerValue != null) {
			sb.append(";integerValue=").append(integerValue);
			
		}
		if(StringHelper.containsNonWhitespace(stringValue)) {
			sb.append(";stringValue=").append(stringValue).append(";");
		}
		if(integerValue == null && !StringHelper.containsNonWhitespace(stringValue)) {
			sb.append(";noValue=");
		}
		if(definition != null) {
			sb.append(";definition=").append(definition.getRubric());
		}
		sb.append("]");
		return sb.toString();
	}
}
