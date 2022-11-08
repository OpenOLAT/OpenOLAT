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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.course.db.impl;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.course.db.CourseDBEntry;

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
 * Initial Date:  7 avr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Entity
@Table(name="o_co_db_entry")
public class CourseDBEntryImpl implements Persistable, CourseDBEntry {
	
	private static final long serialVersionUID = -6487632477815812235L;
	
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

	@Column(name="courseid", nullable=true, insertable=true, updatable=true)
	private Long courseKey;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="identity", nullable=true, insertable=true, updatable=false)
	private Identity identity;

	@Column(name="category", nullable=true, insertable=true, updatable=true)
	private String category;
	@Column(name="name", nullable=true, insertable=true, updatable=true)
	private String name;
	@Column(name="floatvalue", nullable=true, insertable=true, updatable=true)
	private Float floatValue;
	@Column(name="longvalue", nullable=true, insertable=true, updatable=true)
	private Long longValue;
	@Column(name="stringvalue", nullable=true, insertable=true, updatable=true)
	private String stringValue;
	@Column(name="textvalue", nullable=true, insertable=true, updatable=true)
	private String textValue;
	
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
	public Long getCourseKey() {
		return courseKey;
	}
	
	public void setCourseKey(Long courseKey) {
		this.courseKey = courseKey;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Object getValue() {
		if(getStringValue() != null) {
			return getStringValue();
		} else if (getLongValue() != null) {
			return getLongValue();
		} else if (getFloatValue() != null) {
			return getFloatValue();
		} else if (getTextValue() != null) {
			return getTextValue();
		}
		return null;
	}

	@Override
	public void setValue(Object value) {
		if(value instanceof Long) {
			setLongValue((Long)value);
		} else if (value instanceof Float) {
			setFloatValue((Float)value);
		} else if (value instanceof String) {
			if (((String) value).length() <= 255) {
				// db field for string value limited to 255
				// reset text value to null for updating of previously long text to short strings
				setStringValue((String)value);				
				setTextValue(null);
			} else {
				// fallback to text for oversized strings
				// reset string value to null for updating of previously short strings to long texts
				setStringValue(null);
				setTextValue((String)value);				
			}
		}
	}

	public Float getFloatValue() {
		return floatValue;
	}
	
	public void setFloatValue(Float floatValue) {
		this.floatValue = floatValue;
	}
	
	public Long getLongValue() {
		return longValue;
	}
	
	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}
	
	public String getStringValue() {
		return stringValue;
	}
	
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public String getTextValue() {
		return textValue;
	}
	
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
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
		if(obj instanceof CourseDBEntryImpl entry) {
			return getKey() != null && getKey().equals(entry.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
