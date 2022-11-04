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

package org.olat.resource;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

/**
 * A <b>OLATResourceImpl</b> is 
 * 
 * @author Andreas
 *
 */
@Entity
@Table(name="o_olatresource")
public class OLATResourceImpl implements Persistable, OLATResource {

	private static final long serialVersionUID = 4797534778467150679L;

	/** for mysql, need always to provide a type and a key to allow a composite index, so 0 is
	 * a reserved key meaning "no key"
	 */
	public static final Long NULLVALUE = Long.valueOf(0l);
	
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
	@Column(name="resource_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="resname", nullable=false, insertable=true, updatable=false)
	private String resName;
	@Column(name="resid", nullable=false, insertable=true, updatable=false)
	private Long resId;

	/**
	* Constructor needed for Hibernate.
	*/
	protected OLATResourceImpl() {
		// singleton
	}

	OLATResourceImpl(Long id, String typeName) {
		if (id == null) id = NULLVALUE;
		resId = id;
		resName = typeName;
	}

	OLATResourceImpl(OLATResourceable resourceable) {
		Long id = resourceable.getResourceableId();
		if (id == null) id = NULLVALUE;
		resId = id;
		resName = resourceable.getResourceableTypeName();
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
	public String getResourceableTypeName() {
		return getResName();
	}

	@Override
	public Long getResourceableId() {
		Long val = getResId();
		if (val == null) throw new AssertException("hibernate should never set id to null, but to zero instead");
		return val.equals(NULLVALUE) ? null : val;
	}
	
	/**
	 * for hibernate only
	 * @return Long
	 */
	public Long getResId() {
		return resId;
	}
	
	/**
	 * for hibernate only
	 * @param id
	 */
	public void setResId(Long id) {
		resId = id;
	}

	/**
	 * for hibernate only
	 * @return String
	 */
	public String getResName() {
		return resName;
	}

	/**
	 * for hibernate only
	 * @param typeName
	 */
	public void setResName(String typeName) {
		resName = typeName;
	}

	@Override
	public String toString() {
		StringBuilder desc =
			new StringBuilder()
				.append(" OLATResource(")
				.append(this.getKey())
				.append(")[")
				.append(this.getResourceableTypeName())
				.append("(")
				.append(this.getResourceableId())
				.append(")")
				.append("], ");
		return desc.toString() + super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 9734598 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OLATResource resource) {
			return getKey() != null && getKey().equals(resource.getKey());	
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}