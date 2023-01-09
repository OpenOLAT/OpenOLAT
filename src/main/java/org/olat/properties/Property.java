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

package org.olat.properties;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;

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

/**
 * Initial Date:  Mar 10, 2004
 *
 * @author Mike Stock
 */
@Entity(name="property")
@Table(name="o_property")
public class Property implements Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = -7029205250635324093L;

	/** max length of a category */
	public static final int CATEGORY_MAX_LENGHT = 33;
	private static final int RESOURCETYPENAME_MAXLENGTH = 50;

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

	@Column(name="resourcetypename", nullable=false, insertable=true, updatable=true)
	private String resourceTypeName;
	@Column(name="resourcetypeid", nullable=false, insertable=true, updatable=true)
	private Long resourceTypeId;
	@Column(name="category", nullable=false, insertable=true, updatable=true)
	private String category;
	@Column(name="name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="floatvalue", nullable=false, insertable=true, updatable=true)
	private Float floatValue;
	@Column(name="longvalue", nullable=false, insertable=true, updatable=true)
	private Long longValue;
	@Column(name="stringvalue", nullable=false, insertable=true, updatable=true)
	private String stringValue;
	@Column(name="textvalue", nullable=false, insertable=true, updatable=true)
	private String textValue;

	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="identity", nullable=true, insertable=true, updatable=true)
	private Identity identity;
	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="grp", nullable=true, insertable=true, updatable=true)
	private BusinessGroup grp;

	Property() { 
	    // nothing to do 
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

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return string value
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * @return identity
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @return text value
	 */
	public String getTextValue() {
		return textValue;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string maximal 255 chars long; for longer strings use text value
	 */
	public void setStringValue(String string) {
		stringValue = string;
	}

	/**
	 * @param identity
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * @param string for longer strings (saved as TEXT or BLOB in your database)
	 */
	public void setTextValue(String string) {
		textValue = string;
	}

	/**
	 * @return category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return Long
	 */
	public Long getLongValue() {
		return longValue;
	}
	/**
	 * @param longValue
	 */
	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}
	/**
	 * @return float value
	 */
	public Float getFloatValue() {
		return floatValue;
	}

	/**
	 * @return group
	 */
	public BusinessGroup getGrp() {
		return grp;
	}
	
	/**
	 *
	 * @param string, maximal length 33 characters
	 */
	public void setCategory(String string) {
		if (string != null && string.length() > CATEGORY_MAX_LENGHT) throw new RuntimeException("Property.category too long. Max is " + CATEGORY_MAX_LENGHT);
		category = string;
	}

	/**
	 * @param f
	 */
	public void setFloatValue(Float f) {
		floatValue = f;
	}

	/**
	 * @param group
	 */
	public void setGrp(BusinessGroup group) {
		this.grp = group;
	}

	/**
	 * @return resource type ID
	 */
	public Long getResourceTypeId() {
		return resourceTypeId;
	}

	/**
	 * @return resource type name
	 */
	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeId(Long long1) {
		resourceTypeId = long1;
	}

	public void setResourceTypeName(String string) {
		if (string != null && string.length() > RESOURCETYPENAME_MAXLENGTH)
			throw new AssertException("resourcetypename of o_property too long");
		resourceTypeName = string;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 82526 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof Property prop) {
			return getKey() != null && getKey().equals(prop.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
