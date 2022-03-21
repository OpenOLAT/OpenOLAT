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
package org.olat.course.core.manager;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.core.CourseElement;
import org.olat.course.core.CourseElementRef;
import org.olat.course.core.CourseElementSearchParams;
import org.olat.course.core.model.CourseElementImpl;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseElementDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CourseElement create(RepositoryEntry entry, CourseNode courseNode, AssessmentConfig assessmentConfig) {
		CourseElementImpl courseElement = new CourseElementImpl();
		courseElement.setCreationDate(new Date());
		courseElement.setLastModified(courseElement.getCreationDate());
		courseElement.setType(courseNode.getType());
		courseElement.setRepositoryEntry(entry);
		courseElement.setSubIdent(courseNode.getIdent());
		updateAttributes(courseElement, courseNode, assessmentConfig);
		
		dbInstance.getCurrentEntityManager().persist(courseElement);
		return courseElement;
	}

	private void updateAttributes(CourseElementImpl courseElement, CourseNode courseNode, AssessmentConfig assessmentConfig) {
		courseElement.setShortTitle(Formatter.truncateOnly(courseNode.getShortTitle(), 32));
		courseElement.setLongTitle(Formatter.truncateOnly(courseNode.getLongTitle(), 1024));
		courseElement.setAssesseable(assessmentConfig.isAssessable());
		courseElement.setScoreMode(assessmentConfig.getScoreMode());
		courseElement.setGrade(assessmentConfig.hasGrade());
		courseElement.setAutoGrade(assessmentConfig.isAutoGrade());
		courseElement.setPassedMode(assessmentConfig.getPassedMode());
		BigDecimal cutValue = Mode.none != assessmentConfig.getPassedMode() && assessmentConfig.getCutValue() != null
				? new BigDecimal(Float.toString(assessmentConfig.getCutValue()))
				: null;
		courseElement.setCutValue(cutValue);
	}
	
	public CourseElement update(CourseElement courseElement, CourseNode courseNode, AssessmentConfig assessmentConfig) {
		if (courseElement instanceof CourseElementImpl) {
			CourseElementImpl impl = (CourseElementImpl)courseElement;
			updateAttributes(impl, courseNode, assessmentConfig);
			return update(impl);
		}
		return courseElement;
	}
	
	public CourseElement update(CourseElement courseElement) {
		if (courseElement instanceof CourseElementImpl) {
			CourseElementImpl impl = (CourseElementImpl)courseElement;
			impl.setLastModified(new Date());
			return dbInstance.getCurrentEntityManager().merge(impl);
		}
		return courseElement;
	}
	
	public List<CourseElement> load(CourseElementSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ce");
		sb.append("  from courseelement ce");
		if (searchParams.getRepositoryEntries() != null && !searchParams.getRepositoryEntries().isEmpty()) {
			sb.and().append("ce.repositoryEntry.key in :repositoryEntryKeys");
		}
		
		TypedQuery<CourseElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CourseElement.class);
		if (searchParams.getRepositoryEntries() != null && !searchParams.getRepositoryEntries().isEmpty()) {
			query.setParameter("repositoryEntryKeys", searchParams.getRepositoryEntries().stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList()));
		}
		
		return query.getResultList();
	}
	
	public void delete(Collection<CourseElementRef> courseElements) {
		String query = "delete from courseelement ce where ce.key in :keys";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("keys", courseElements.stream().map(CourseElementRef::getKey).collect(Collectors.toList()))
				.executeUpdate();
	}

	public void delete(RepositoryEntryRef repositoryEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from courseelement ce");
		sb.and().append("ce.repositoryEntry.key = :repositoryEntryKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.executeUpdate();
	}

	public void delete(RepositoryEntryRef repositoryEntry, Collection<String> subIdents) {
		if (repositoryEntry == null || subIdents == null || subIdents.isEmpty()) return;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from courseelement ce");
		sb.and().append("ce.repositoryEntry.key = :repositoryEntryKey");
		sb.and().append("ce.subIdent in :subIdents");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.setParameter("subIdents", subIdents)
				.executeUpdate();
	}

}
