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

import static org.olat.core.commons.persistence.PersistenceHelper.appendFuzzyLike;
import static org.olat.core.commons.persistence.PersistenceHelper.makeFuzzyQueryString;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.handler.EvaluationFormHandler;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AssignmentImpl;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.EvaluationFormPart;
import org.olat.modules.portfolio.model.SectionImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
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
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private EvaluationFormHandler formHandler;
	
	public Assignment createAssignment(String title, String summary, String content,
			String storage, AssignmentType type, boolean template, AssignmentStatus status, Section section, Binder binder,
			boolean onlyAutoEvaluation, boolean reviewerSeeAutoEvaluation, boolean anonymousExternEvaluation, RepositoryEntry formEntry) {
		AssignmentImpl assignment = new AssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setLastModified(assignment.getCreationDate());
		assignment.setTemplate(template);
		assignment.setTitle(title);
		assignment.setSummary(summary);
		assignment.setContent(content);
		assignment.setStorage(storage);
		assignment.setSection(section);
		assignment.setBinder(binder);
		assignment.setType(type.name());
		assignment.setStatus(status.name());
		assignment.setOnlyAutoEvaluation(onlyAutoEvaluation);
		assignment.setReviewerSeeAutoEvaluation(reviewerSeeAutoEvaluation);
		assignment.setAnonymousExternalEvaluation(anonymousExternEvaluation);
		assignment.setFormEntry(formEntry);
		
		if(section != null) {
			((SectionImpl)section).getAssignments().size();
			((SectionImpl)section).getAssignments().add(assignment);
		} else if(binder != null) {
			((BinderImpl)binder).getAssignments().size();
			((BinderImpl)binder).getAssignments().add(assignment);
		}
		dbInstance.getCurrentEntityManager().persist(assignment);
		if(section != null) {
			dbInstance.getCurrentEntityManager().merge(section);
		} else if(binder != null) {
			dbInstance.getCurrentEntityManager().merge(binder);
		}
		return assignment;
	}
	
	public Assignment createAssignment(Assignment templateReference, AssignmentStatus status, Section section, Binder binder,
			boolean template, Boolean onlyAutoEvaluation, Boolean reviewerSeeAutoEvaluation) {
		AssignmentImpl assignment = new AssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setLastModified(assignment.getCreationDate());
		assignment.setTemplate(template);
		assignment.setTitle(templateReference.getTitle());
		assignment.setSummary(templateReference.getSummary());
		assignment.setContent(templateReference.getContent());
		assignment.setSection(section);
		assignment.setBinder(binder);
		assignment.setType(templateReference.getAssignmentType().name());
		assignment.setTemplateReference(templateReference);
		assignment.setStatus(status.name());
		if(onlyAutoEvaluation != null) {
			assignment.setOnlyAutoEvaluation(onlyAutoEvaluation.booleanValue());
			assignment.setReviewerSeeAutoEvaluation(reviewerSeeAutoEvaluation != null && reviewerSeeAutoEvaluation.booleanValue());
		} else {
			assignment.setOnlyAutoEvaluation(templateReference.isOnlyAutoEvaluation());
			assignment.setReviewerSeeAutoEvaluation(templateReference.isReviewerSeeAutoEvaluation());
		}
		assignment.setAnonymousExternalEvaluation(templateReference.isAnonymousExternalEvaluation());
		assignment.setStorage(templateReference.getStorage());
		assignment.setFormEntry(templateReference.getFormEntry());

		if(section != null) {
			((SectionImpl)section).getAssignments().size();
			((SectionImpl)section).getAssignments().add(assignment);
		} else if(binder != null) {
			((BinderImpl)binder).getAssignments().size();
			((BinderImpl)binder).getAssignments().add(assignment);
		}
		dbInstance.getCurrentEntityManager().persist(assignment);
		if(section != null) {
			dbInstance.getCurrentEntityManager().merge(section);
		} else if(binder != null) {
			dbInstance.getCurrentEntityManager().merge(binder);	
		}
		return assignment;
	}
	
	public Assignment updateAssignment(Assignment assignment) {
		((AssignmentImpl)assignment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(assignment);
	}
	
	public Section moveUpAssignment(SectionImpl section, Assignment assignment) {
		section.getAssignments().size();
		int index = section.getAssignments().indexOf(assignment);
		if(index > 0) {
			Assignment reloadedAssigment = section.getAssignments().remove(index);
			section.getAssignments().add(index - 1, reloadedAssigment);
		} else if(index < 0) {
			section.getAssignments().add(0, assignment);
		}
		section = dbInstance.getCurrentEntityManager().merge(section);
		return section;
	}
	
	public Section moveDownAssignment(SectionImpl section, Assignment assignment) {
		section.getAssignments().size();
		int index = section.getAssignments().indexOf(assignment);
		if(index >= 0 && index + 1 < section.getAssignments().size()) {
			Assignment reloadedAssignment = section.getAssignments().remove(index);
			section.getAssignments().add(index + 1, reloadedAssignment);
			section = dbInstance.getCurrentEntityManager().merge(section);
		}
		return section;
	}
	
	public void moveAssignment(SectionImpl currentSection, Assignment assignment, SectionImpl newParentSection) {
		currentSection.getAssignments().size();//load the assignments
		newParentSection.getAssignments().size();
		int index = currentSection.getAssignments().indexOf(assignment);
		if(index >= 0) {
			Assignment reloadedAssignment = currentSection.getAssignments().remove(index);
			((AssignmentImpl)reloadedAssignment).setSection(newParentSection);
			dbInstance.getCurrentEntityManager().merge(currentSection);
			newParentSection.getAssignments().add(reloadedAssignment);
			reloadedAssignment = dbInstance.getCurrentEntityManager().merge(reloadedAssignment);
			
			dbInstance.getCurrentEntityManager().merge(newParentSection);
		}
	}
	
	public Assignment startEssayAssignment(Assignment assigment, Page page, Identity assignee) {
		((AssignmentImpl)assigment).setPage(page);
		((AssignmentImpl)assigment).setAssignee(assignee);
		((AssignmentImpl)assigment).setLastModified(new Date());
		assigment.setAssignmentStatus(AssignmentStatus.inProgress);
		return dbInstance.getCurrentEntityManager().merge(assigment);
	}
	
	public Assignment startFormAssignment(Assignment assignment, Page page, Identity assignee) {
		((AssignmentImpl)assignment).setPage(page);
		((AssignmentImpl)assignment).setAssignee(assignee);
		((AssignmentImpl)assignment).setLastModified(new Date());
		assignment.setAssignmentStatus(AssignmentStatus.inProgress);
		
		RepositoryEntry formEntry = assignment.getFormEntry();
		if(formEntry.getOlatResource().getResourceableTypeName().equals(formHandler.getSupportedType())) {
			File formFile = formHandler.getFormFile(formEntry);
			String formXml = FileUtils.load(formFile, "UTF-8");
			EvaluationFormPart formPart = new EvaluationFormPart();
			formPart.setContent(formXml);
			formPart.setFormEntry(formEntry);
			pageDao.persistPart(page.getBody(), formPart);
		}
		return dbInstance.getCurrentEntityManager().merge(assignment);
	}
	
	public Assignment loadAssignmentByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" left join fetch assignment.section as section")
		  .append(" left join fetch assignment.binder as binder")
		  .append(" left join fetch assignment.page as page")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" left join fetch formEntry.olatResource as resource")
		  .append(" where assignment.key=:assignmentKey");
		
		List<Assignment> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("assignmentKey", key)
				.getResultList();
		return assignments == null || assignments.isEmpty() ? null : assignments.get(0);
	}
	
	public List<Assignment> loadBinderAssignmentsTemplates(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.binder binder")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" where binder.key=:binderKey and assignment.template=true and assignment.page is null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("binderKey", binder.getKey())
				.getResultList();
	}
	
	public boolean hasBinderAssignmentTemplate(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment.key from pfassignment as assignment")
		  .append(" inner join assignment.binder binder")
		  .append(" where binder.key=:binderKey and assignment.template=true and assignment.page.key is null");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("binderKey", binder.getKey())
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}
	
	public List<Assignment> loadAssignments(BinderRef binder, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" left join fetch assignment.page as page")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" where section.binder.key=:binderKey");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "assignment.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "assignment.summary", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "assignment.content", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		sb.append(" order by section.key, assignment.pos");
		
		TypedQuery<Assignment> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("binderKey", binder.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString);
		}
		return query.getResultList();
	}
	
	public List<Assignment> loadAssignments(SectionRef section, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" left join fetch assignment.page as page")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" where section.key=:sectionKey");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "assignment.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "assignment.summary", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "assignment.content", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		sb.append(" order by section.key, assignment.pos");
		
		TypedQuery<Assignment> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("sectionKey", section.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString);
		}
		return query.getResultList();
	}
	
	public List<Assignment> loadAssignmentReferences(Assignment assignment) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select assignment from pfassignment as assignment")
		  .append(" left join fetch assignment.section as section")
		  .append(" left join fetch assignment.page as page")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" where assignment.templateReference.key=:assignmentKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("assignmentKey", assignment.getKey())
				.getResultList();
	}
	
	public List<Assignment> loadAssignments(Page page, String searchString) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" inner join fetch assignment.page as page")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" where page.key=:pageKey");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "assignment.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "assignment.summary", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "assignment.content", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		sb.append(" order by section.key, assignment.pos");
		
		TypedQuery<Assignment> query =  dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("pageKey", page.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString);
		}
		return query.getResultList();
	}
	
	public List<Assignment> getOwnedAssignments(IdentityRef assignee) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assignment from pfpage page")
		  .append(" inner join page.body as body")
		  .append(" inner join page.section as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join pfassignment assignment on (section.key = assignment.section.key)")
		  .append(" inner join fetch assignment.page as assignmentPage")
		  .append(" where (assignment.assignee.key is null or assignment.assignee.key=:assigneeKey)")
		  .append(" and exists (select pageMember from bgroupmember as pageMember")
		  .append("     inner join pageMember.identity as ident on (ident.key=:assigneeKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("  	where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Assignment.class)
			.setParameter("assigneeKey", assignee.getKey())
			.getResultList();
	}
	
	public Assignment loadAssignment(PageBody body) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.page as page")
		  .append(" left join fetch assignment.formEntry as formEntry")
		  .append(" left join fetch formEntry.olatResource as resource")
		  .append(" where page.body.key=:bodyKey");

		List<Assignment> assignments = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Assignment.class)
			.setParameter("bodyKey", body.getKey())
			.getResultList();
		return assignments == null || assignments.isEmpty() ? null : assignments.get(0);
	}
	
	public boolean isAssignmentInUse(Assignment assignment) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select assignment.key from pfassignment as assignment")
		  .append(" where assignment.templateReference.key=:assignmentKey");

		List<Long> counts = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("assignmentKey", assignment.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return counts != null && !counts.isEmpty() && counts.get(0) != null && counts.get(0).longValue() >= 0;
	}
	
	public boolean isFormEntryInUse(RepositoryEntryRef formEntry) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select assignment.key from pfassignment as assignment")
		  .append(" where assignment.formEntry.key=:formEntryKey");

		List<Long> counts = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("formEntryKey", formEntry.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return counts != null && !counts.isEmpty() && counts.get(0) != null && counts.get(0).longValue() >= 0;
	}
	
	public int deleteAssignmentReference(Assignment assignment) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete pfassignment assignment where assignment.templateReference.key=:assignmentKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("assignmentKey", assignment.getKey())
				.executeUpdate();
	}
	
	public int deleteAssignmentBySection(Section section) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete pfassignment assignment where assignment.section.key=:sectionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("sectionKey", section.getKey())
				.executeUpdate();
	}

	public int deleteAssignment(Assignment assignment) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete pfassignment assignment where assignment.templateReference.key=:assignmentKey");
		int deleted = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("assignmentKey", assignment.getKey())
				.executeUpdate();
		dbInstance.getCurrentEntityManager().remove(assignment);
		return deleted + 1;
	}
}
