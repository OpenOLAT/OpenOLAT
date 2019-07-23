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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.logging.AssertException;
import org.olat.group.BusinessGroup;

/**
 * Initial Date:  Mar 10, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class Property extends PersistentObject implements ModifiedInfo {

	private static final long serialVersionUID = -7029205250635324093L;

	/** max length of a category */
	public static final int CATEGORY_MAX_LENGHT = 33;
    
	private Identity identity;
	private BusinessGroup grp;
	private String resourceTypeName;
	private Long resourceTypeId;
	private String category;
	private String name;
	private Float floatValue;
	private Long 	longValue;
	private String stringValue;
	private String textValue;
	private Date lastModified;

	private static final int RESOURCETYPENAME_MAXLENGTH = 50;

	/**
	 * 
	 */
	Property() { 
	    // notthing to do 
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

	/**
	 * @param long1
	 */
	public void setResourceTypeId(Long long1) {
		resourceTypeId = long1;
	}

	/**
	 * @param string
	 */
	public void setResourceTypeName(String string) {
		if (string != null && string.length() > RESOURCETYPENAME_MAXLENGTH)
			throw new AssertException("resourcetypename of o_property too long");
		resourceTypeName = string;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#setLastModified(java.util.Date)
	 */
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
		if(obj instanceof Property) {
			Property prop = (Property)obj;
			return getKey() != null && getKey().equals(prop.getKey());
		}
		return false;
	}
}
