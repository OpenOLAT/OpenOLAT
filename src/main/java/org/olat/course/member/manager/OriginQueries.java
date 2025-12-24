/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.member.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.member.model.OriginCoursePlannerRow;
import org.olat.course.member.model.OriginCourseRow;
import org.olat.course.member.model.OriginGroupRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2025-12-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OriginQueries {
	
	@Autowired
	private DB dbInstance;
	
	public List<OriginCourseRow> getCourseOrigins(Long repositoryEntryKey, Long identityKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select gm.role, gm.creationDate");
		sb.append(" from repositoryentry r");
		sb.append(" inner join r.groups r2g");
		sb.append(" inner join r2g.group g");
		sb.append(" inner join g.members gm");
		sb.append(" inner join gm.identity as i");
		sb.and().append(" r.key = :repositoryEntryKey");
		sb.and().append(" i.key = :identityKey");
		sb.and().append(" r2g.defaultGroup = true");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("repositoryEntryKey", repositoryEntryKey)
				.setParameter("identityKey", identityKey)
				.getResultStream().map(this::mapToOriginCourseRow).toList();
	}

	private OriginCourseRow mapToOriginCourseRow(Object[] objects) {
		String role = (String) objects[0];
		Date creationDate = (Date) objects[1];
		return new OriginCourseRow(role, creationDate);
	}
	
	public List<OriginGroupRow> getGroupOrigins(Long repositoryEntryKey, Long identityKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select gm.role, bg.key, bg.name, bg.managedFlagsString, gm.creationDate");
		sb.append(" from repositoryentry r");
		sb.append(" inner join r.groups r2g");
		sb.append(" inner join r2g.group g");
		sb.append(" inner join businessgroup bg on bg.baseGroup.key = g.key");
		sb.append(" inner join g.members gm");
		sb.append(" inner join gm.identity as i");
		sb.and().append(" r.key = :repositoryEntryKey");
		sb.and().append(" i.key = :identityKey");
		sb.and().append(" r2g.defaultGroup = false");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("repositoryEntryKey", repositoryEntryKey)
				.setParameter("identityKey", identityKey)
				.getResultStream().map(this::mapToOriginGroupRow).toList();
	}
	
	private OriginGroupRow mapToOriginGroupRow(Object[] objects) {
		String role = (String) objects[0];
		Long groupKey = (Long) objects[1];
		String groupName = (String) objects[2];
		String groupManagedFlagsString = (String) objects[3];
		Date creationDate = (Date) objects[4];
		return new OriginGroupRow(role, groupKey, groupName, groupManagedFlagsString, creationDate);
	}

	public List<OriginCoursePlannerRow> getCoursePlannerOrigins(Long repositoryEntryKey, Long identityKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select gm.role, ce.key, ce.displayName, ce.identifier, c.key, c.displayName, gm.creationDate");
		sb.append(" from repositoryentry r");
		sb.append(" inner join r.groups r2g");
		sb.append(" inner join r2g.group g");
		sb.append(" inner join g.members gm");
		sb.append(" inner join gm.identity as i");
		sb.append(" inner join curriculumelement ce on ce.group.key = g.key");
		sb.append(" inner join ce.curriculum c");
		sb.and().append(" r.key = :repositoryEntryKey");
		sb.and().append(" i.key = :identityKey");
		sb.and().append(" r2g.defaultGroup = false");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("repositoryEntryKey", repositoryEntryKey)
				.setParameter("identityKey", identityKey)
				.getResultStream().map(this::mapToOriginCoursePlannerRow).toList();
	}

	private OriginCoursePlannerRow mapToOriginCoursePlannerRow(Object[] objects) {
		String role = (String) objects[0];
		Long elementKey = (Long) objects[1];
		String elementName = (String) objects[2];
		String identifier = (String) objects[3];
		Long curriculumKey = (Long) objects[4];
		String curriculumName = (String) objects[5];
		Date creationDate = (Date) objects[6];
		return new OriginCoursePlannerRow(role, elementKey, elementName, identifier, curriculumKey, curriculumName, creationDate);
	}
}
