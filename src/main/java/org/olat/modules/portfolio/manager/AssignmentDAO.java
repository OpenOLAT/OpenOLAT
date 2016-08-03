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
package org.olat.modules.portfolio.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AssignmentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssignmentDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Assignment createAssignment(String title, String summary, String content,
			String storage, AssignmentType type, AssignmentStatus status, Section section) {
		AssignmentImpl assignment = new AssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setLastModified(assignment.getCreationDate());
		assignment.setTitle(title);
		assignment.setSummary(summary);
		assignment.setContent(content);
		assignment.setStorage(storage);
		assignment.setSection(section);
		assignment.setType(type.name());
		assignment.setStatus(status.name());
		
		dbInstance.getCurrentEntityManager().persist(assignment);
		return assignment;
	}
	
	public Assignment createAssignment(Assignment templateReference, AssignmentStatus status, Section section) {
		AssignmentImpl assignment = new AssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setLastModified(assignment.getCreationDate());
		assignment.setTitle(templateReference.getTitle());
		assignment.setSummary(templateReference.getSummary());
		assignment.setContent(templateReference.getContent());
		assignment.setSection(section);
		assignment.setType(templateReference.getAssignmentType().name());
		assignment.setTemplateReference(templateReference);
		assignment.setStatus(status.name());
		
		dbInstance.getCurrentEntityManager().persist(assignment);
		return assignment;
	}
	
	public Assignment updateAssignment(Assignment assignment) {
		((AssignmentImpl)assignment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(assignment);
	}
	
	public Assignment startEssayAssignment(Assignment assigment, Page page, Identity assignee) {
		((AssignmentImpl)assigment).setPage(page);
		((AssignmentImpl)assigment).setAssignee(assignee);
		((AssignmentImpl)assigment).setLastModified(new Date());
		assigment.setAssignmentStatus(AssignmentStatus.inProgress);
		return dbInstance.getCurrentEntityManager().merge(assigment);
	}
	
	public Assignment loadAssignmentByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" left join fetch assignment.page as page")
		  .append(" where assignment.key=:assignmentKey");
		
		List<Assignment> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("assignmentKey", key)
				.getResultList();
		return assignments == null || assignments.isEmpty() ? null : assignments.get(0);
	}
	
	public List<Assignment> loadAssignments(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" left join fetch assignment.page as page")
		  .append(" where section.binder.key=:binderKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("binderKey", binder.getKey())
				.getResultList();
	}
	
	public List<Assignment> loadAssignments(SectionRef section) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" left join fetch assignment.page as page")
		  .append(" where section.key=:sectionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("sectionKey", section.getKey())
				.getResultList();
	}
	
	public List<Assignment> loadAssignments(Page page) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" inner join fetch assignment.page as page")
		  .append(" where page.key=:pageKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("pageKey", page.getKey())
				.getResultList();
	}
	
	public List<Assignment> getOwnedAssignments(IdentityRef assignee) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfpage as page")
		  .append(" inner join page.body as body")
		  .append(" inner join page.section as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join pfassignment assignment on (section.key = assignment.section.key)")
		  .append(" where assignment.assignee.key is null or assignment.assignee.key=:assigneeKey")
		  .append(" and exists (select pageMember from bgroupmember as pageMember")
		  .append("     inner join pageMember.identity as ident on (ident.key=:assigneeKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("  	where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Assignment.class)
			.setParameter("assigneeKey", assignee.getKey())
			.getResultList();
	}
}
