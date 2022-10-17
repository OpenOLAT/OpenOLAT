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

import jakarta.persistence.FlushModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.ICourse;
import org.olat.course.db.CourseDBEntry;
import org.olat.course.db.CourseDBManager;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Manage the key value pairs for the course DB.
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service("courseDBManager")
public class CourseDBManagerImpl extends AbstractSpringModule implements CourseDBManager, GenericEventListener {
	
	private static final String ENABLED = "enabled";
	
	@Value("${course.db.enabled:true}")
	private boolean enabled;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Autowired
	public CourseDBManagerImpl(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
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
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public Long getCourseId(Long key) {
		OLATResource resource = repositoryManager.lookupRepositoryEntryResource(key);
		if(resource == null) {
			return key;
		}
		return resource.getResourceableId();
	}
	
	public List<String> getUsedCategories(ICourse course) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct entry.category from ").append(CourseDBEntryImpl.class.getName()).append(" as entry")
		  .append(" where entry.courseKey=:courseKey ");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), String.class)
				.setParameter("courseKey", course.getResourceableId())
				.getResultList();
	}

	@Override
	public void reset(ICourse course, String category) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(CourseDBEntryImpl.class.getName())
			.append(" entry where entry.courseKey=:courseKey");
		if(StringHelper.containsNonWhitespace(category)) {
			sb.append(" and entry.category=:category");
		}
		
		Query query = dbInstance.getCurrentEntityManager().createQuery(sb.toString());
		query.setParameter("courseKey", course.getResourceableId());
		if(StringHelper.containsNonWhitespace(category)) {
			query.setParameter("category", category);
		}
		query.setFlushMode(FlushModeType.AUTO)
			.executeUpdate();
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

		TypedQuery<CourseDBEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CourseDBEntry.class)
				.setParameter("courseKey", course.getResourceableId());
		if(identity != null) {
			query.setParameter("identity", identity);
		}
		if(StringHelper.containsNonWhitespace(category)) {
			query.setParameter("category", category);
		}
		if(StringHelper.containsNonWhitespace(name)) {
			query.setParameter("name", name);
		}
		return query.getResultList();
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
		if(entry.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(entry);
		} else {
			entry = dbInstance.getCurrentEntityManager().merge(entry);
		}
		return entry;
	}
	
	private CourseDBEntryImpl loadEntry(Long courseResourceableId, Identity identity, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("select entry from ").append(CourseDBEntryImpl.class.getName())
			.append(" entry where")
			.append(" entry.identity=:identity and entry.courseKey=:courseKey ")
			.append(" and entry.name=:named and entry.category=:category");

		List<CourseDBEntryImpl> entries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CourseDBEntryImpl.class)
				.setParameter("named", name)
				.setParameter("category", category)
				.setParameter("identity", identity)
				.setParameter("courseKey", courseResourceableId)
				.getResultList();
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

		int rowAffected = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("name", name)
				.setParameter("category", category)
				.setParameter("identity", identity)
				.setParameter("courseKey", course.getResourceableId())
				.setFlushMode(FlushModeType.AUTO)
				.executeUpdate();
		return rowAffected > 0;
	}
}