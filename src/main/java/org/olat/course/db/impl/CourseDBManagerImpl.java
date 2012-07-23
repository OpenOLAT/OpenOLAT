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

import java.util.List;

import org.hibernate.FlushMode;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.ICourse;
import org.olat.course.db.CourseDBEntry;
import org.olat.course.db.CourseDBManager;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for CourseDBManagerImpl
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseDBManagerImpl extends CourseDBManager implements GenericEventListener {
	
	private static final String ENABLED = "enabled";
	private boolean enabled;
	
	public CourseDBManagerImpl() {
		//
	}

	@Override
	public void init() {
		//enabled/disabled
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter("enabled", false);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reset(ICourse course, String category) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(CourseDBEntryImpl.class.getName())
			.append(" entry where entry.courseKey=:courseKey");
		if(StringHelper.containsNonWhitespace(category)) {
			sb.append(" and entry.category=:category");
		}
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setLong("courseKey", course.getResourceableId());
		if(StringHelper.containsNonWhitespace(category)) {
			query.setString("category", category);
		}
		query.executeUpdate(FlushMode.AUTO);
	}
	
	@Override
	public CourseDBEntry getValue(ICourse course, Identity identity, String category, String name) {
		CourseDBEntry entry = loadEntry(course.getResourceableId(), identity, category, name);
		return entry;
	}

	@Override
	public CourseDBEntry getValue(Long courseResourceableId, Identity identity, String category, String name) {
		CourseDBEntry entry = loadEntry(courseResourceableId, identity, category, name);
		return entry;
	}

	@Override
	public List<CourseDBEntry> getValues(ICourse course, Identity identity, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("select entry from ").append(CourseDBEntryImpl.class.getName())
			.append(" entry where entry.courseKey=:courseKey");
		if(identity != null) {
			sb.append(" and entry.identity=:identity");
		}
		if(StringHelper.containsNonWhitespace(category)) {
			sb.append(" and entry.category=:category");
		}
		if(StringHelper.containsNonWhitespace(name)) {
			sb.append(" and entry.name=:name");
		}

		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setLong("courseKey", course.getResourceableId());
		if(identity != null) {
			query.setEntity("identity", identity);
		}
		if(StringHelper.containsNonWhitespace(category)) {
			query.setString("category", category);
		}
		if(StringHelper.containsNonWhitespace(name)) {
			query.setString("name", name);
		}
		return query.list();
	}

	@Override
	public CourseDBEntry setValue(ICourse course, Identity identity, String category, String name, Object value) {
		CourseDBEntryImpl entry = loadEntry(course.getResourceableId(), identity, category, name);
		if(entry == null) {
			entry = new CourseDBEntryImpl();
			entry.setCourseKey(course.getResourceableId());
			entry.setIdentity(identity);
			entry.setName(name);
			entry.setCategory(category);
		}
		entry.setValue(value);
		DBFactory.getInstance().saveObject(entry);
		return entry;
	}
	
	private CourseDBEntryImpl loadEntry(Long courseResourceableId, Identity identity, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("select entry from ").append(CourseDBEntryImpl.class.getName())
			.append(" entry where")
			.append(" entry.identity=:identity and entry.courseKey=:courseKey ")
			.append(" and entry.name=:named and entry.category=:category");

		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("named", name);
		query.setString("category", category);
		query.setEntity("identity", identity);
		query.setLong("courseKey", courseResourceableId);
		
		List<CourseDBEntryImpl> entries = query.list();
		if(entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}

	@Override
	public boolean deleteValue(ICourse course, Identity identity, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(CourseDBEntryImpl.class.getName())
			.append(" entry where entry.identity=:identity and entry.courseKey=:courseKey ")
			.append(" and entry.name=:name and entry.category=:category");

		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("name", name);
		query.setString("category", category);
		query.setEntity("identity", identity);
		query.setLong("courseKey", course.getResourceableId());
		
		int rowAffected = query.executeUpdate(FlushMode.AUTO);
		return rowAffected > 0;
	}
}