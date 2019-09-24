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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.course.db.CourseDBEntry;

/**
 * Initial Date:  7 avr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseDBEntryImpl extends PersistentObject implements CourseDBEntry {
	
	private static final long serialVersionUID = -6487632477815812235L;
	
	private Long courseKey;
	private Identity identity;
	
	private String category;
	private String name;
	private Float floatValue;
	private Long 	longValue;
	private String stringValue;
	private String textValue;
	private Date lastModified;
	
	public Long getCourseKey() {
		return courseKey;
	}
	
	public void setCourseKey(Long courseKey) {
		this.courseKey = courseKey;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
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
	
	
	

}
