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

package org.olat.user;

import java.util.Locale;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Desciption:
 * <p>
 * The user field represents an attribute of a known OLAT user.
 * <p>
 * @author Carsten Weisse, Florian Gnägi
 */
public abstract class AbstractUserPropertyHandler implements UserPropertyHandler {
	
	private String name; 
	private String group;
	private boolean deletable = true; // default

	/**
	 * @see org.olat.core.id.UserField#getUserFieldValue(org.olat.core.id.User, java.util.Locale)
	 */
	public String getUserProperty(User user, Locale locale) {
		return getInternalValue(user);
	}

	/**
	 * @see org.olat.core.id.UserField#getUserFieldValueAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	public String getUserPropertyAsHTML(User user, Locale locale) {
		return StringHelper.escapeHtml(getUserProperty(user, locale));
	}

	/**
	 * @see org.olat.core.id.UserField#setUserFieldValue(org.olat.core.id.User, java.lang.String)
	 */
	public void setUserProperty(User user, String value) {
		setInternalValue(user, value);
	}

	/**
	 * @see org.olat.core.id.UserField#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @see org.olat.core.id.UserField#getGroup()
	 */
	public String getGroup() {
		return this.group;
	}

	/**
	 * @see org.olat.core.id.UserField#i18nFormElementLabelKey()
	 */
	public String i18nFormElementLabelKey() {
		return "form.name." + getName();
	}

	/**
	 * @see org.olat.core.id.UserField#i18nFormElementGroupKey()
	 */
	public String i18nFormElementGroupKey() {
		return "form.group." + getGroup();
	}

	/**
	 * @see org.olat.core.id.UserField#i18nColumnDescriptorLabelKey()
	 */
	public String i18nColumnDescriptorLabelKey() {
		return "table.name." + getName();
	}
	
	/**
	 * @see org.olat.core.id.UserField#getColumnDescriptor(int, java.lang.String, java.util.Locale)
	 */
	public ColumnDescriptor getColumnDescriptor(int position, String action, Locale locale) {
		return new DefaultColumnDescriptor(i18nColumnDescriptorLabelKey(), position, action, locale);		
	}

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#isDeletable()
	 */
	public boolean isDeletable() {
		return deletable;
	}

	/**
	 * @return The non-i18-ified raw value from the database
	 */
	protected String getInternalValue(User user) {
		if (user instanceof UserImpl) {
			String value = ((UserImpl)user).getUserProperties().get(name);
			if("_".equals(value) && "oracle".equals(DBFactory.getInstance().getDbVendor())) {
				value = null;
			}
			return value;
		} else if (user instanceof User) {
			return user.getProperty(name, null);
		}
		return null;
	}

	/**
	 * @param value The raw value in a 18n independent form
	 */
	protected void setInternalValue(User user, String value) {
		if (user instanceof UserImpl) {
			// remove fields with null or empty value from o_userfield table (hibernate)
			// sparse data storage
			if (value == null || value.length() == 0) {
				//fxdiff: store each value
				if("oracle".equals(DBFactory.getInstance().getDbVendor())) {
					((UserImpl)user).getUserProperties().put(name, "_");
				} else {
					((UserImpl)user).getUserProperties().put(name, "");
				}
			} else {
				((UserImpl)user).getUserProperties().put(name, value);
			}
		} else if (user instanceof UserImpl) {
			user.setProperty(name, value);
		}
	}

	
	/**
	 * Returns the user field database key, name and value .
	 * @return String internal user field info
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String quickinfo = "AbstractUserPropertyHandler("+this.getClass().getName()+")["+getName()+"]" ;
		return quickinfo + "," + super.toString();
	}

	/**
	 * Spring setter
	 * @param group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Spring setter
	 * @param isDeletable
	 */
	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	/**
	 * Spring setter
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}