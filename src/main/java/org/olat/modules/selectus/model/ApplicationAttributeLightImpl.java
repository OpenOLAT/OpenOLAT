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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 3 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="appattributelight")
@Table(name="o_selectus_app_attribute")
public class ApplicationAttributeLightImpl implements ApplicationAttributeLight {

	private static final long serialVersionUID = -1613399152840139167L;

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

	@Column(name="attrvalue", nullable=true, insertable=false, updatable=false)
	private String value;
	
	@Column(name="fk_definition_id", nullable=false, insertable=false, updatable=false)
	private Long definitionKey;
	@Column(name="fk_application_id", nullable=false, insertable=false, updatable=false)
	private Long applicationKey;
	@Column(name="fk_position_id", nullable=false, insertable=false, updatable=false)
	private Long positionKey;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public Long getDefinitionKey() {
		return definitionKey;
	}

	public void setDefinitionKey(Long definitionKey) {
		this.definitionKey = definitionKey;
	}
	
	@Override
	public Long getApplicationKey() {
		return applicationKey;
	}

	public void setApplicationKey(Long applicationKey) {
		this.applicationKey = applicationKey;
	}

	@Override
	public Long getPositionKey() {
		return positionKey;
	}

	public void setPositionKey(Long positionKey) {
		this.positionKey = positionKey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -755612 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ApplicationAttributeLightImpl) {
			ApplicationAttributeLightImpl attr = (ApplicationAttributeLightImpl)obj;
			return getKey() != null && getKey().equals(attr.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
