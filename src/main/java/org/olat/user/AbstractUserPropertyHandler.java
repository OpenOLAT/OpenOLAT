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

import java.lang.reflect.Field;
import java.util.Locale;

import jakarta.persistence.Column;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
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
	private static final Logger log = Tracing.createLoggerFor(AbstractUserPropertyHandler.class);
	
	private String name; 
	private String group;
	private String databaseColumnName;

	/**
	 * @see org.olat.core.id.UserField#getUserFieldValue(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserProperty(User user, Locale locale) {
		return getInternalValue(user);
	}

	/**
	 * @see org.olat.core.id.UserField#getUserFieldValueAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		return StringHelper.escapeHtml(getUserProperty(user, locale));
	}

	/**
	 * @see org.olat.core.id.UserField#setUserFieldValue(org.olat.core.id.User, java.lang.String)
	 */
	@Override
	public void setUserProperty(User user, String value) {
		setInternalValue(user, value);
	}

	/**
	 * @see org.olat.core.id.UserField#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getDatabaseColumnName() {
		return databaseColumnName;
	}

	/**
	 * @see org.olat.core.id.UserField#getGroup()
	 */
	@Override
	public String getGroup() {
		return group;
	}

	/**
	 * @see org.olat.core.id.UserField#i18nFormElementLabelKey()
	 */
	@Override
	public String i18nFormElementLabelKey() {
		return "form.name." + getName();
	}

	/**
	 * @see org.olat.core.id.UserField#i18nFormElementGroupKey()
	 */
	@Override
	public String i18nFormElementGroupKey() {
		return "form.group." + getGroup();
	}

	/**
	 * @see org.olat.core.id.UserField#i18nColumnDescriptorLabelKey()
	 */
	@Override
	public String i18nColumnDescriptorLabelKey() {
		return "table.name." + getName();
	}
	
	/**
	 * @see org.olat.core.id.UserField#getColumnDescriptor(int, java.lang.String, java.util.Locale)
	 */
	@Override
	public ColumnDescriptor getColumnDescriptor(int position, String action, Locale locale) {
		return new DefaultColumnDescriptor(i18nColumnDescriptorLabelKey(), position, action, locale);		
	}

	/**
	 * @return The non-i18-ified raw value from the database
	 */
	protected String getInternalValue(User user) {
		if (user instanceof UserImpl) {
			String value = ((UserImpl)user).getProperty(name);
			if("_".equals(value) && "oracle".equals(DBFactory.getInstance().getDbVendor())) {
				value = null;
			}
			return value;
		} else if (user != null) {
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
				((UserImpl)user).setUserProperty(name, null);
			} else {
				((UserImpl)user).setUserProperty(name, value);
			}
		} else {
			log.warn("Set read-only value: {}", name);
		}
	}

	
	/**
	 * Returns the user field database key, name and value .
	 * @return String internal user field info
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String quickinfo = "AbstractUserPropertyHandler("+this.getClass().getName()+")["+getName()+"]" ;
		return quickinfo + "," + super.toString();
	}

	/**
	 * Spring setter
	 * @param group
	 */
	@Override
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Spring setter
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
		setInternalGetterSetter(name);
	}
	
	protected void setInternalGetterSetter(String name) {
		try {
			Field getter = UserImpl.class.getDeclaredField(name);
			getter.setAccessible(true);
			if(getter.isAnnotationPresent(Column.class)) {
				Column col = getter.getAnnotation(Column.class);
				databaseColumnName = col.name();
			}
		} catch (NoSuchFieldException | SecurityException e) {
			log.error("", e);
		}
	}
}