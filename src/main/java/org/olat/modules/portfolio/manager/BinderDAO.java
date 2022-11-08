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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.invitation.manager.InvitationDAO;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssignmentImpl;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.PageImpl;
import org.olat.modules.portfolio.model.SectionImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.TypedQuery;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BinderDAO {
	
	private static final Logger log = Tracing.createLoggerFor(BinderDAO.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private AssignmentDAO assignmentDao;
	@Autowired
	private PageUserInfosDAO pageUserInfosDao;
	@Autowired
	private AssessmentSectionDAO assessmentSectionDao;
	@Autowired
	private BinderUserInformationsDAO userInformationsDAO;
	
	public BinderImpl createAndPersist(String title, String summary, String imagePath, RepositoryEntry entry) {
		BinderImpl binder = new BinderImpl();
		binder.setCreationDate(new Date());
		binder.setLastModified(binder.getCreationDate());
		binder.setTitle(title);
		binder.setSummary(summary);
		binder.setImagePath(imagePath);
		binder.setStatus(BinderStatus.open.name());
		binder.setBaseGroup(groupDao.createGroup());
		if(entry != null) {
			binder.setOlatResource(entry.getOlatResource());
		}
		binder.setAssignments(new ArrayList<>());
		binder.setSections(new ArrayList<>());
		dbInstance.getCurrentEntityManager().persist(binder);
		return binder;
	}
	
	public BinderImpl createCopy(BinderImpl template, RepositoryEntry entry, String subIdent) {
		BinderImpl binder = new BinderImpl();
		binder.setCreationDate(new Date());
		binder.setLastModified(binder.getCreationDate());
		binder.setTitle(template.getTitle());
		binder.setSummary(template.getSummary());
		binder.setImagePath(template.getImagePath());
		binder.setStatus(BinderStatus.open.name());
		binder.setBaseGroup(groupDao.createGroup());
		binder.setTemplate(template);
		binder.setCopyDate(binder.getCreationDate());
		if(entry != null) {
			binder.setEntry(entry);
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			binder.setSubIdent(subIdent);
		}
		binder.setAssignments(new ArrayList<>());
		binder.setSections(new ArrayList<>());
		dbInstance.getCurrentEntityManager().persist(binder);
		binder.getSections().size();
		
		for(Section templateSection:template.getSections()) {
			Section section = createInternalSection(binder, templateSection);
			binder.getSections().add(section);
			dbInstance.getCurrentEntityManager().persist(section);
		}
		
		binder = dbInstance.getCurrentEntityManager().merge(binder);
		return binder;
	}
	
	public Binder syncWithTemplate(BinderImpl template, BinderImpl binder, AtomicBoolean changes) {
		binder.setImagePath(template.getImagePath());
		binder.setSummary(template.getSummary());
		
		List<Section> templateSections = template.getSections();
		Map<Assignment,Section> assignmentTemplateToSectionTemplatesMap = new HashMap<>();
		for(Section templateSection:templateSections) {
			for(Assignment assignment:templateSection.getAssignments()) {
				assignmentTemplateToSectionTemplatesMap.put(assignment, templateSection);
			}
		}

		List<Section> currentSections = binder.getSections();
		List<Section> leadingSections = new ArrayList<>(binder.getSections());
		Map<Section,Section> templateToSectionsMap = new HashMap<>();
		for(Section currentSection:currentSections) {
			Section templateRef = currentSection.getTemplateReference();
			if(templateRef != null) {
				templateToSectionsMap.put(templateRef, currentSection);
			}
		}
		
		currentSections.clear();
		for(int i=0; i<templateSections.size(); i++) {
			SectionImpl templateSection = (SectionImpl)templateSections.get(i);
			SectionImpl currentSection = (SectionImpl)templateToSectionsMap.get(templateSection);
			
			if(currentSection != null) {
				leadingSections.remove(currentSection);
				
				currentSections.add(currentSection);
				syncSectionMetadata(templateSection, currentSection);
				templateToSectionsMap.put(templateSection, currentSection);
			} else {
				SectionImpl section = createInternalSection(binder, templateSection);
				currentSections.add(section);
				dbInstance.getCurrentEntityManager().persist(section);
				templateToSectionsMap.put(templateSection, section);
			}
		}
		currentSections.addAll(leadingSections);
		
		//sync moving assignments
		for(int i=0; i<templateSections.size(); i++) {
			SectionImpl templateSection = (SectionImpl)templateSections.get(i);
			SectionImpl currentSection = (SectionImpl)templateToSectionsMap.get(templateSection);
			syncMovingAssignments(templateSection, currentSection, templateToSectionsMap);
		}
		
		//sync assignments
		for(int i=0; i<templateSections.size(); i++) {
			SectionImpl templateSection = (SectionImpl)templateSections.get(i);
			SectionImpl currentSection = (SectionImpl)templateToSectionsMap.get(templateSection);
			syncAssignments(templateSection, currentSection);
		}
		
		//sync templates
		syncAssignments(template, binder);

		//update all sections
		for(int i=0; i<templateSections.size(); i++) {
			SectionImpl templateSection = (SectionImpl)templateSections.get(i);
			SectionImpl currentSection = (SectionImpl)templateToSectionsMap.get(templateSection);
			currentSection = dbInstance.getCurrentEntityManager().merge(currentSection);
		}

		binder = dbInstance.getCurrentEntityManager().merge(binder);
		changes.set(true);
		return binder;
	}
	
	private void syncSectionMetadata(Section templateSection, Section currentSection) {
		currentSection.setTitle(templateSection.getTitle());
		currentSection.setDescription(templateSection.getDescription());
		if(!currentSection.isOverrideBeginEndDates()) {
			currentSection.setBeginDate(templateSection.getBeginDate());
			currentSection.setEndDate(templateSection.getEndDate());
		}
	}
	
	private void syncMovingAssignments(SectionImpl templateSection, SectionImpl currentSection, Map<Section,Section> templateToSectionsMap) {
		List<Assignment> templateAssignments = new ArrayList<>(templateSection.getAssignments());
		for(Iterator<Assignment> currentAssignmentIt=currentSection.getAssignments().iterator(); currentAssignmentIt.hasNext(); ) {
			if(currentAssignmentIt.next() == null) {
				currentAssignmentIt.remove();
			}
		}

		List<Assignment> currentAssignments = new ArrayList<>(currentSection.getAssignments());
		for(int i=0; i<currentAssignments.size(); i++) {
			Assignment currentAssignment = currentAssignments.get(i);
			if(currentAssignment == null) {
				currentSection.getAssignments().remove(i);
			} else {
				Assignment refAssignment = currentAssignment.getTemplateReference();
				if(refAssignment != null && refAssignment.getSection() != null
						&& !templateAssignments.contains(refAssignment)
						&& !refAssignment.getSection().equals(templateSection)
						&& templateToSectionsMap.containsKey(refAssignment.getSection())) {
						//really moved
					templateAssignments.remove(refAssignment);
					SectionImpl newSection = (SectionImpl)templateToSectionsMap.get(refAssignment.getSection());
					syncMovedAssignment(currentSection, newSection, currentAssignment);
				}
			}
		}
	}
	
	private void syncMovedAssignment(SectionImpl currentSection, SectionImpl newSection, Assignment assignment) {
		currentSection.getAssignments().size();
		newSection.getAssignments().size();

		currentSection.getAssignments().remove(assignment);
		((AssignmentImpl)assignment).setSection(newSection);
		assignment = dbInstance.getCurrentEntityManager().merge(assignment);
		newSection.getAssignments().add(assignment);

		Page page = assignment.getPage();
		if(page != null) {
			currentSection.getPages().remove(page);
			newSection.getPages().add(page);
			((PageImpl)page).setSection(newSection);
			dbInstance.getCurrentEntityManager().merge(page);
		}
	}
	
	private void syncAssignments(SectionImpl templateSection, SectionImpl currentSection) {
		List<Assignment> templateAssignments = new ArrayList<>(templateSection.getAssignments());
		List<Assignment> currentAssignments = new ArrayList<>(currentSection.getAssignments());
		syncAssignmentsList(templateAssignments, currentAssignments, currentSection);
		for(Assignment templateAssignment:templateAssignments) {
			if(templateAssignment != null) {
				assignmentDao.createAssignment(templateAssignment, AssignmentStatus.notStarted, currentSection, null, templateAssignment.isTemplate(), null, null);
			}
		}
	}
	
	private void syncAssignments(BinderImpl templateBinder, BinderImpl currentBinder) {
		List<Assignment> templateAssignments = new ArrayList<>(templateBinder.getAssignments());
		List<Assignment> currentAssignments = new ArrayList<>(currentBinder.getAssignments());
		syncAssignmentsList(templateAssignments, currentAssignments, currentBinder);
		for(Assignment templateAssignment:templateAssignments) {
			if(templateAssignment != null) {
				assignmentDao.createAssignment(templateAssignment, AssignmentStatus.notStarted, null, currentBinder, templateAssignment.isTemplate(), null, null);
			}
		}
	}
	
	/**
	 * Help method which sync / update but not create the assignments.
	 * 
	 * @param templateAssignments A modifiable list of assignments from the template
	 * @param currentAssignments A modifiable list of assignments from the current object
	 * @param currentOwner The owner of the assignments, binder or section
	 */
	private void syncAssignmentsList(List<Assignment> templateAssignments, List<Assignment> currentAssignments, PortfolioElement currentOwner) {
		for(Assignment currentAssignment:currentAssignments) {
			if(currentAssignment == null) {
				log.error("Missing assignment: " + currentOwner.getKey());
				continue;
			}
			
			Assignment refAssignment = currentAssignment.getTemplateReference();
			if(refAssignment == null) {
				if(currentAssignment.getAssignmentStatus() != AssignmentStatus.deleted) {
					currentAssignment.setAssignmentStatus(AssignmentStatus.deleted);
					currentAssignment = dbInstance.getCurrentEntityManager().merge(currentAssignment);
				}
			} else if(!templateAssignments.contains(refAssignment)) {
				//this case is normally not possible
				//if it happens, don't do anything, let the data safe
			} else {
				templateAssignments.remove(refAssignment);

				AssignmentImpl currentImpl = (AssignmentImpl)currentAssignment;
				currentAssignment = syncAssignment(refAssignment, currentImpl);
			}
		}
	}
	
	private AssignmentImpl syncAssignment(Assignment refAssignment, AssignmentImpl currentAssignment) {
		if(StringHelper.isSame(currentAssignment.getTitle(), refAssignment.getTitle())
				&& StringHelper.isSame(currentAssignment.getSummary(), refAssignment.getSummary())
				&& StringHelper.isSame(currentAssignment.getContent(), refAssignment.getContent())
				&& StringHelper.isSame(currentAssignment.getStorage(), refAssignment.getStorage())
				&& StringHelper.isSame(currentAssignment.getType(), refAssignment.getAssignmentType().name())
				&& StringHelper.isSame(currentAssignment.isOnlyAutoEvaluation(), refAssignment.isOnlyAutoEvaluation())
				&& StringHelper.isSame(currentAssignment.isReviewerSeeAutoEvaluation(), refAssignment.isReviewerSeeAutoEvaluation())
				&& StringHelper.isSame(currentAssignment.isAnonymousExternalEvaluation(), refAssignment.isAnonymousExternalEvaluation())
				&& StringHelper.isSame(currentAssignment.getFormEntry(), refAssignment.getFormEntry())) {
			return currentAssignment;
		}
		
		currentAssignment.setTitle(refAssignment.getTitle());
		currentAssignment.setSummary(refAssignment.getSummary());
		currentAssignment.setContent(refAssignment.getContent());
		currentAssignment.setStorage(refAssignment.getStorage());
		currentAssignment.setType(refAssignment.getAssignmentType().name());
		currentAssignment.setOnlyAutoEvaluation(refAssignment.isOnlyAutoEvaluation());
		currentAssignment.setReviewerSeeAutoEvaluation(refAssignment.isReviewerSeeAutoEvaluation());
		currentAssignment.setAnonymousExternalEvaluation(refAssignment.isAnonymousExternalEvaluation());
		
		RepositoryEntry formEntry = refAssignment.getFormEntry();
		if(formEntry != null) {
			RepositoryEntry refFormEntry = dbInstance.getCurrentEntityManager()
					.getReference(RepositoryEntry.class, formEntry.getKey());
			currentAssignment.setFormEntry(refFormEntry);
		}
		return dbInstance.getCurrentEntityManager().merge(currentAssignment);
	}
	
	private SectionImpl createInternalSection(Binder binder, Section templateSection) {
		SectionImpl section = new SectionImpl();
		section.setCreationDate(new Date());
		section.setLastModified(section.getCreationDate());
		section.setBaseGroup(groupDao.createGroup());
		section.setTitle(templateSection.getTitle());
		section.setDescription(templateSection.getDescription());
		section.setBeginDate(templateSection.getBeginDate());
		section.setEndDate(templateSection.getEndDate());
		section.setOverrideBeginEndDates(false);
		section.setStatus(SectionStatus.notStarted.name());
		section.setBinder(binder);
		section.setTemplateReference(templateSection);
		section.setAssignments(new ArrayList<>());
		section.setPages(new ArrayList<>());
		return section;
	}
	
	public Binder updateBinder(Binder binder) {
		((BinderImpl)binder).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(binder);
	}
	
	/**
	 * @param owner The owner
	 * @return All the binder where the specified identity as the role owner
	 */
	public List<Binder> getAllBindersAsOwner(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" left join fetch binder.olatResource as resource")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", PortfolioRoles.owner.name())
			.getResultList();
	}
	
	/**
	 * 
	 * @param owner The owner
	 * @return The binder where the specified identity has the role 'owner' and the binder is still open.
	 */
	public List<Binder> getOwnedBinders(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role")
		  .append(" and (binder.status is null or binder.status='").append(BinderStatus.open.name()).append("')");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", PortfolioRoles.owner.name())
			.getResultList();
	}
	
	public List<Binder> getOwnedBinderFromCourseTemplate(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" inner join fetch binder.entry as entry")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role")
		  .append(" and binder.subIdent is not null and entry.key is not null");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", PortfolioRoles.owner.name())
			.getResultList();
	}
	
	public void detachBinderTemplate() {
		//unlink entry
		//unlink template
	}
	
	public int deleteBinder(BinderRef binderRef) {
		int rows = userInformationsDAO.deleteBinderUserInfos(binderRef);
		
		BinderImpl binder = (BinderImpl)loadByKey(binderRef.getKey());
		List<Section> sections = new ArrayList<>(binder.getSections());
		for(Section section:sections) {
			List<Page> pages = new ArrayList<>(section.getPages());
			section.getPages().clear();
			section = dbInstance.getCurrentEntityManager().merge(section);
			
			for(Page page:pages) {
				if(page != null) {
					rows += pageDao.deletePage(page);
					rows += pageUserInfosDao.delete(page);
				}
			}
			
			rows += assessmentSectionDao.deleteAssessmentSections(section);
			
			Group baseGroup = section.getBaseGroup();
			rows += groupDao.removeMemberships(baseGroup);
			
			dbInstance.getCurrentEntityManager().remove(section);
			dbInstance.getCurrentEntityManager().remove(baseGroup);
			rows += 2;
		}
		
		binder.getSections().clear();
		
		Group baseGroup = binder.getBaseGroup();
		invitationDao.deleteInvitation(baseGroup);
		rows += groupDao.removeMemberships(baseGroup);
		dbInstance.getCurrentEntityManager().remove(binder);
		dbInstance.getCurrentEntityManager().remove(baseGroup);
		return rows + 2;
	}
	
	public int deleteBinderTemplate(BinderImpl binder) {
		int rows = userInformationsDAO.deleteBinderUserInfos(binder);
		
		List<Section> sections = new ArrayList<>(binder.getSections());
		for(Section section:sections) {
			binder = (BinderImpl)deleteSection(binder, section);
		}
		
		dbInstance.getCurrentEntityManager().flush();
		
		//remove reference via template
		String sb = "update pfbinder binder set binder.template=null where binder.template.key=:binderKey";
		rows += dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("binderKey", binder.getKey())
			.executeUpdate();

		String binderQ = "delete from pfbinder binder where binder.key=:binderKey";
		rows += dbInstance.getCurrentEntityManager()
				.createQuery(binderQ)
				.setParameter("binderKey", binder.getKey())
				.executeUpdate();
		return rows;
	}
	
	public int detachBinderFromRepositoryEntry(RepositoryEntry entry) {
		//remove reference to the course and the course node
		String sb = "update pfbinder binder set binder.entry=null,binder.subIdent=null where binder.entry.key=:entryKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("entryKey", entry.getKey())
			.executeUpdate();
	}
	
	public int detachBinderFromRepositoryEntry(RepositoryEntry entry, PortfolioCourseNode node) {
		//remove reference to the course and the course node
		String sb = "update pfbinder binder set binder.entry=null,binder.subIdent=null where binder.entry.key=:entryKey and binder.subIdent=:nodeIdent";
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("entryKey", entry.getKey())
			.setParameter("nodeIdent", node.getIdent())
			.executeUpdate();
	}
	
	/**
	 * The same type of query is user for the categories
	 * @param owner
	 * @return
	 */
	public BinderStatistics getBinderStatistics(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, binder.title, binder.imagePath, binder.lastModified, binder.status,")
		  .append(" binderEntry.displayname,")
		  .append(" (select count(section.key) from pfsection as section")
		  .append("   where section.binder.key=binder.key")
		  .append(" ) as numOfSections,")
		  .append(" (select count(page.key) from pfpage as page, pfsection as pageSection")
		  .append("   where pageSection.binder.key=binder.key and page.section.key=pageSection.key")
		  .append(" ) as numOfPages,")
		  .append(" (select count(comment.key) from usercomment as comment, pfpage as page, pfsection as pageSection")
		  .append("   where pageSection.binder.key=binder.key and page.section.key=pageSection.key and comment.resId=page.key and comment.resName='Page'")
		  .append(" ) as numOfComments")
		  .append(" from pfbinder as binder")
		  .append(" left join binder.entry binderEntry")
		  .append(" where binder.key=:binderKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("binderKey", binder.getKey())
			.getResultList();
		
		if(objects.size() >= 1) {
			int pos = 0;
			Object[] object = objects.get(0);
			Long key = (Long)object[pos++];
			String title = (String)object[pos++];
			String imagePath = (String)object[pos++];
			Date lastModified = (Date)object[pos++];
			String status = (String)object[pos++];
			String repoEntryName = (String)object[pos++];

			int numOfSections = ((Number)object[pos++]).intValue();
			int numOfPages = ((Number)object[pos++]).intValue();
			int numOfComments = ((Number)object[pos++]).intValue();
			
			return new BinderStatistics(key, title, imagePath, lastModified, numOfSections, numOfPages, status, repoEntryName, numOfComments);
		}
		return null;
	}
	
	/**
	 * The same type of query is user for the categories
	 * @param owner
	 * @return
	 */
	public int countOwnedBinders(IdentityRef owner, boolean deleted) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("select count(distinct binder.key)")
		  .append(" from pfbinder as binder")
		  .append(" left join binder.baseGroup as baseGroup")
		  .append(" left join baseGroup.members as membership")
		  .append(" where binder.olatResource is null and membership.identity.key=:identityKey and membership.role=:role");
		if(deleted) {
			sb.append(" and binder.status='").append(BinderStatus.deleted.name()).append("'");
		} else {
			sb.append(" and (binder.status is null or binder.status='").append(BinderStatus.open.name()).append("')");
		}
		
		List<Long> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", PortfolioRoles.owner.name())
			.getResultList();
		return objects != null && !objects.isEmpty() && objects.get(0) != null ? objects.get(0).intValue() : 0;
	}
	
	/**
	 * The same type of query is user for the categories
	 * @param owner
	 * @return
	 */
	public List<BinderStatistics> searchOwnedBinders(IdentityRef owner, boolean deleted) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("select binder.key, binder.title, binder.imagePath, binder.lastModified, binder.status,")
		  .append(" binderEntry.displayname,")
		  .append(" (select count(section.key) from pfsection as section")
		  .append("   where section.binder.key=binder.key")
		  .append(" ) as numOfSections,")
		  .append(" (select count(page.key) from pfpage as page, pfsection as pageSection")
		  .append("   where pageSection.binder.key=binder.key and page.section.key=pageSection.key")
		  .append(" ) as numOfPages,")
		  .append(" (select count(comment.key) from usercomment as comment, pfpage as page, pfsection as pageSection")
		  .append("   where pageSection.binder.key=binder.key and page.section.key=pageSection.key and comment.resId=page.key and comment.resName='Page'")
		  .append(" ) as numOfComments")
		  .append(" from pfbinder as binder")
		  .append(" left join binder.baseGroup as baseGroup")
		  .append(" left join baseGroup.members as membership")
		  .append(" left join binder.entry binderEntry")
		  .append(" where binder.olatResource is null and membership.identity.key=:identityKey and membership.role=:role");
		if(deleted) {
			sb.append(" and binder.status='").append(BinderStatus.deleted.name()).append("'");
		} else {
			sb.append(" and (binder.status is null or binder.status='").append(BinderStatus.open.name()).append("')");
		}
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", PortfolioRoles.owner.name())
			.getResultList();
		List<BinderStatistics> rows = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			int pos = 0;
			Long key = (Long)object[pos++];
			String title = (String)object[pos++];
			String imagePath = (String)object[pos++];
			Date lastModified = (Date)object[pos++];
			String status = (String)object[pos++];
			String repoEntryName = (String)object[pos++];

			int numOfSections = ((Number)object[pos++]).intValue();
			int numOfPages = ((Number)object[pos++]).intValue();
			int numOfComments = ((Number)object[pos++]).intValue();
			
			rows.add(new BinderStatistics(key, title, imagePath, lastModified, numOfSections, numOfPages, status, repoEntryName, numOfComments));
		}
		return rows;
	}
	
	public List<BinderStatistics> searchOwnedLastBinders(IdentityRef owner, int maxResults) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("select binder.key, binder.title, binder.imagePath, binder.lastModified, binder.status,")
		  .append(" binderEntry.displayname,")
		  .append(" (select count(section.key) from pfsection as section")
		  .append("   where section.binder.key=binder.key")
		  .append(" ) as numOfSections,")
		  .append(" (select count(page.key) from pfpage as page, pfsection as pageSection")
		  .append("   where pageSection.binder.key=binder.key and page.section.key=pageSection.key")
		  .append(" ) as numOfPages,")
		  .append(" (select count(comment.key) from usercomment as comment, pfpage as page, pfsection as pageSection")
		  .append("   where pageSection.binder.key=binder.key and page.section.key=pageSection.key and comment.resId=page.key and comment.resName='Page'")
		  .append(" ) as numOfComments")
		  .append(" from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join pfbinderuserinfos as uinfos on (uinfos.binder.key=binder.key and uinfos.identity.key=membership.identity.key)")
		  .append(" left join binder.entry binderEntry")
		  .append(" where binder.olatResource is null and membership.identity.key=:identityKey and membership.role=:role")
		  .append(" and (binder.status is null or binder.status='").append(BinderStatus.open.name()).append("')")
		  .append(" order by uinfos.recentLaunch desc nulls last");

		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", PortfolioRoles.owner.name())
			.setFirstResult(0)
			.setMaxResults(maxResults)
			.getResultList();
		List<BinderStatistics> rows = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			int pos = 0;
			Long key = (Long)object[pos++];
			String title = (String)object[pos++];
			String imagePath = (String)object[pos++];
			Date lastModified = (Date)object[pos++];
			String status = (String)object[pos++];
			String repoEntryName = (String)object[pos++];

			int numOfSections = ((Number)object[pos++]).intValue();
			int numOfPages = ((Number)object[pos++]).intValue();
			int numOfComments = ((Number)object[pos++]).intValue();
			
			rows.add(new BinderStatistics(key, title, imagePath, lastModified, numOfSections, numOfPages, status, repoEntryName, numOfComments));
		}
		return rows;
	}
	
	public Binder loadByKey(Long key) {
		List<Binder> binders = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadBinderByKey", Binder.class)
			.setParameter("portfolioKey", key)
			.getResultList();
		return binders == null || binders.isEmpty() ? null : binders.get(0);
	}
	
	public Binder loadByResource(OLATResource resource) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" inner join fetch binder.olatResource as olatResource")
		  .append(" where olatResource.key=:resourceKey");
		
		List<Binder> binders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("resourceKey", resource.getKey())
			.getResultList();
		return binders == null || binders.isEmpty() ? null : binders.get(0);
	}
	
	public Binder loadByGroup(Group group) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" left join fetch binder.olatResource as olatResource")
		  .append(" where baseGroup.key=:groupKey");
		
		List<Binder> binders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("groupKey", group.getKey())
			.getResultList();
		return binders == null || binders.isEmpty() ? null : binders.get(0);
	}
	
	public Binder loadBySection(SectionRef section) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select binder from pfsection as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" where section.key=:sectionKey");
		
		List<Binder> binders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("sectionKey", section.getKey())
			.getResultList();
		return binders == null || binders.isEmpty() ? null : binders.get(0);
	}
	
	public Binder deleteSection(Binder binder, Section section) {
		List<Page> pages = new ArrayList<>(section.getPages());
		//delete pages
		for(Page page:pages) {
			if(page != null) {
				pageDao.deletePage(page);
				pageUserInfosDao.delete(page);
				section.getPages().remove(page);
			}
		}
		
		List<Assignment> assignments = assignmentDao.loadAssignments(section, null);
		for(Assignment assignment:assignments) {
			assignmentDao.deleteAssignmentReference(assignment);
		}
		assignmentDao.deleteAssignmentBySection(section);
		assessmentSectionDao.deleteAssessmentSections(section);

		//remove reference via template
		String sb = "update pfsection section set section.templateReference=null where section.templateReference.key=:sectionKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("sectionKey", section.getKey())
			.executeUpdate();

		((BinderImpl)binder).getSections().remove(section);
		dbInstance.getCurrentEntityManager().remove(section);
		binder = dbInstance.getCurrentEntityManager().merge(binder);
		if(binder.getOlatResource() != null) {
			binder.getOlatResource().getResourceableTypeName();
		}
		return binder;
	}
	
	public boolean isTemplateInUse(BinderRef template, RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select binder.key from pfbinder as binder")
		  .append(" where binder.template.key=:templateKey");
		if(entry != null) {
			sb.append(" and binder.entry.key=:entryKey");
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and binder.subIdent=:subIdent");
		}
		
		TypedQuery<Long> binderKeyQuery = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("templateKey", template.getKey());
		if(entry != null) {
			binderKeyQuery.setParameter("entryKey", entry.getKey());
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			binderKeyQuery.setParameter("subIdent", subIdent);
		}
		
		List<Long> binderKeys = binderKeyQuery.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return binderKeys != null && !binderKeys.isEmpty() && binderKeys.get(0) != null && binderKeys.get(0).longValue() >= 0;
	}
	
	public int getTemplateUsage(RepositoryEntryRef templateEntry) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select count(binder.key) from pfbinder as binder")
		  .append(" inner join binder.template as template")
		  .append(" inner join template.olatResource as res")
		  .append(" inner join repositoryentry as v on (res.key=v.olatResource.key)")
		  .append(" where v.key=:entryKey");
		
		List<Long> counter = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("entryKey", templateEntry.getKey())
			.getResultList();
		return counter == null || counter.isEmpty() || counter.get(0) == null ? 0 : counter.get(0).intValue();
	}
	
	public Binder getBinder(Identity owner, BinderRef template, RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership on (membership.identity.key=:identityKey and membership.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append(" where binder.template.key=:templateKey");
		if(entry != null) {
			sb.append(" and binder.entry.key=:entryKey");
		} else {
			sb.append(" and binder.entry.key is null");
		}
		
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and binder.subIdent=:subIdent");
		} else {
			sb.append(" and binder.subIdent is null");
		}
		
		TypedQuery<Binder> binderKeyQuery = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("templateKey", template.getKey())
			.setParameter("identityKey", owner.getKey());
		if(entry != null) {
			binderKeyQuery.setParameter("entryKey", entry.getKey());
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			binderKeyQuery.setParameter("subIdent", subIdent);
		}
		
		List<Binder> binders = binderKeyQuery.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return binders != null && !binders.isEmpty() && binders.get(0) != null ? binders.get(0) : null;
	}
	
	public List<Binder> getBinders(Identity owner, RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership on (membership.identity.key=:identityKey and membership.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append(" where  binder.entry.key=:entryKey and ");
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append("binder.subIdent=:subIdent");
		} else {
			sb.append("binder.subIdent is null");
		}

		TypedQuery<Binder> binders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("entryKey", entry.getKey());
		if(StringHelper.containsNonWhitespace(subIdent)) {
			binders.setParameter("subIdent", subIdent);
		}
		return binders.getResultList();
	}
	
	public Group getGroup(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select baseGroup from pfbinder as binder ")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" where binder.key=:binderKey");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Group.class)
				.setParameter("binderKey", binder.getKey())
				.getSingleResult();
	}
	
	public Group getGroup(SectionRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select baseGroup from pfsection as section ")
		  .append(" inner join section.baseGroup as baseGroup")
		  .append(" where section.key=:sectionKey");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Group.class)
				.setParameter("sectionKey", binder.getKey())
				.getSingleResult();
	}
	
	public boolean isMember(BinderRef binder, IdentityRef member, String... roles)  {
		if(binder == null || roles == null || roles.length == 0 || roles[0] == null) {
			return false;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select count(membership.key) from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" where binder.key=:binderKey and membership.identity.key=:identityKey and membership.role in (:roles)");
		
		List<String> roleList = new ArrayList<>(roles.length);
		for(String role:roles) {
			if(StringHelper.containsNonWhitespace(role)) {
				roleList.add(role);
			}
		}
		
		List<Number> counts = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
			.setParameter("binderKey", binder.getKey())
			.setParameter("identityKey", member.getKey())
			.setParameter("roles", roleList)
			.getResultList();
		return counts == null || counts.isEmpty() || counts.get(0) == null ? false : counts.get(0).longValue() > 0;
	}
	
	public List<Identity> getMembers(BinderRef binder, String... roles)  {
		if(binder == null || roles == null || roles.length == 0 || roles[0] == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select membership.identity from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where binder.key=:binderKey and membership.role in (:roles)");
		
		List<String> roleList = new ArrayList<>(roles.length);
		for(String role:roles) {
			if(StringHelper.containsNonWhitespace(role)) {
				roleList.add(role);
			}
		}
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Identity.class)
			.setParameter("binderKey", binder.getKey())
			.setParameter("roles", roleList)
			.getResultList();
	}
	
	public List<AccessRights> getBinderAccesRights(BinderRef binder, IdentityRef identity)  {
		if(binder == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select membership.role, ident, invitation from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership");
		if(identity != null) {
			sb.append(" on (membership.identity.key =:identityKey)");
		}
		sb.append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" left join binvitation as invitation on (invitation.baseGroup.key=baseGroup.key and identUser.email=invitation.mail)")
		  .append(" where binder.key=:binderKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("binderKey", binder.getKey());
		if(identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		
		List<Object[]> objects = query.getResultList();
		List<AccessRights> rightList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			String role = (String)object[0];
			Identity member = (Identity)object[1];
			Invitation invitation = (Invitation)object[2];
			
			AccessRights rights = new AccessRights();
			rights.setRole(PortfolioRoles.valueOf(role));
			rights.setBinderKey(binder.getKey());
			rights.setIdentity(member);
			rights.setInvitation(invitation);
			rightList.add(rights);
		}
		return rightList;
	}
	
	public List<AccessRights> getSectionAccesRights(BinderRef binder, IdentityRef identity)  {
		if(binder == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select section.key, membership.role, ident, invitation from pfbinder as binder")
		  .append(" inner join binder.sections as section")
		  .append(" inner join section.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership");
		if(identity != null) {
			sb.append(" on (membership.identity.key =:identityKey)");
		}
		sb.append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" left join binvitation as invitation on (invitation.baseGroup.key=binder.baseGroup.key and identUser.email=invitation.mail)")
		  .append(" where binder.key=:binderKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("binderKey", binder.getKey());
		if(identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		
		List<Object[]> objects = query.getResultList();
		List<AccessRights> rightList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long sectionKey = (Long)object[0];
			String role = (String)object[1];
			Identity member = (Identity)object[2];
			Invitation invitation = (Invitation)object[3];
			
			AccessRights rights = new AccessRights();
			rights.setRole(PortfolioRoles.valueOf(role));
			rights.setBinderKey(binder.getKey());
			rights.setSectionKey(sectionKey);
			rights.setIdentity(member);
			rights.setInvitation(invitation);
			rightList.add(rights);
		}
		return rightList;
	}
	
	public List<AccessRights> getPageAccesRights(BinderRef binder, IdentityRef identity)  {
		if(binder == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select section.key, page.key, membership.role, ident, invitation from pfbinder as binder")
		  .append(" inner join binder.sections as section")
		  .append(" inner join section.pages as page")
		  .append(" inner join page.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership");
		if(identity != null) {
			sb.append(" on (membership.identity.key=:identityKey)");
		}
		sb.append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" left join binvitation as invitation on (invitation.baseGroup.key=binder.baseGroup.key and identUser.email=invitation.mail)")
		  .append(" where binder.key=:binderKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("binderKey", binder.getKey());
		if(identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		
		List<Object[]> objects = query.getResultList();
		List<AccessRights> rightList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long sectionKey = (Long)object[0];
			Long pageKey = (Long)object[1];
			String role = (String)object[2];
			Identity member = (Identity)object[3];
			Invitation invitation = (Invitation)object[4];
			
			AccessRights rights = new AccessRights();
			rights.setRole(PortfolioRoles.valueOf(role));
			rights.setBinderKey(binder.getKey());
			rights.setSectionKey(sectionKey);
			rights.setPageKey(pageKey);
			rights.setIdentity(member);
			rights.setInvitation(invitation);
			rightList.add(rights);
		}
		return rightList;
	}
	
	
	public List<AccessRights> getBinderAccesRights(Page page)  {
		if(page == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, section.key, page.key, membership.role, ident, invitation from pfpage as page")
		  .append(" inner join page.section as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" left join binvitation as invitation on (invitation.baseGroup.key=binder.baseGroup.key and identUser.email=invitation.mail)")
		  .append(" where page.key=:pageKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("pageKey", page.getKey());

		
		List<Object[]> objects = query.getResultList();
		List<AccessRights> rightList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long binderKey = (Long)object[0];
			Long sectionKey = (Long)object[1];
			Long pageKey = (Long)object[2];
			String role = (String)object[3];
			Identity member = (Identity)object[4];
			Invitation invitation = (Invitation)object[5];
			
			AccessRights rights = new AccessRights();
			rights.setRole(PortfolioRoles.valueOf(role));
			rights.setBinderKey(binderKey);
			rights.setSectionKey(sectionKey);
			rights.setPageKey(pageKey);
			rights.setIdentity(member);
			rights.setInvitation(invitation);
			rightList.add(rights);
		}
		return rightList;
	}
	
	public List<AccessRights> getSectionAccesRights(Page page)  {
		if(page == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, section.key, page.key, membership.role, ident, invitation from pfpage as page")
		  .append(" inner join page.section as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join section.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" left join binvitation as invitation on (invitation.baseGroup.key=binder.baseGroup.key and identUser.email=invitation.mail)")
		  .append(" where page.key=:pageKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("pageKey", page.getKey());

		
		List<Object[]> objects = query.getResultList();
		List<AccessRights> rightList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long binderKey = (Long)object[0];
			Long sectionKey = (Long)object[1];
			Long pageKey = (Long)object[2];
			String role = (String)object[3];
			Identity member = (Identity)object[4];
			Invitation invitation = (Invitation)object[5];
			
			AccessRights rights = new AccessRights();
			rights.setRole(PortfolioRoles.valueOf(role));
			rights.setBinderKey(binderKey);
			rights.setSectionKey(sectionKey);
			rights.setPageKey(pageKey);
			rights.setIdentity(member);
			rights.setInvitation(invitation);
			rightList.add(rights);
		}
		return rightList;
	}
	
	public List<AccessRights> getPageAccesRights(Page page)  {
		if(page == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, section.key, page.key, membership.role, ident, invitation from pfpage as page")
		  .append(" inner join page.section as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join page.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" left join binvitation as invitation on (invitation.baseGroup.key=binder.baseGroup.key and identUser.email=invitation.mail)")
		  .append(" where page.key=:pageKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("pageKey", page.getKey());

		
		List<Object[]> objects = query.getResultList();
		List<AccessRights> rightList = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long binderKey = (Long)object[0];
			Long sectionKey = (Long)object[1];
			Long pageKey = (Long)object[2];
			String role = (String)object[3];
			Identity member = (Identity)object[4];
			Invitation invitation = (Invitation)object[5];
			
			AccessRights rights = new AccessRights();
			rights.setRole(PortfolioRoles.valueOf(role));
			rights.setBinderKey(binderKey);
			rights.setSectionKey(sectionKey);
			rights.setPageKey(pageKey);
			rights.setIdentity(member);
			rights.setInvitation(invitation);
			rightList.add(rights);
		}
		return rightList;
	}
	
	/**
	 * Add a section to a binder. The binder must be a fresh reload one
	 * with the possibility to lazy load the sections.
	 * 
	 * @param title
	 * @param description
	 * @param begin
	 * @param end
	 * @param binder
	 */
	public SectionImpl createSection(String title, String description, Date begin, Date end, Binder binder) {
		SectionImpl section = new SectionImpl();
		section.setCreationDate(new Date());
		section.setLastModified(section.getCreationDate());
		
		section.setBaseGroup(groupDao.createGroup());
		section.setTitle(title);
		section.setDescription(description);
		section.setBeginDate(begin);
		section.setEndDate(end);
		section.setOverrideBeginEndDates(false);
		section.setStatus(SectionStatus.notStarted.name());
		section.setAssignments(new ArrayList<>());
		section.setPages(new ArrayList<>());
		//force load of the list
		((BinderImpl)binder).getSections().size();
		section.setBinder(binder);
		((BinderImpl)binder).getSections().add(section);
		dbInstance.getCurrentEntityManager().persist(section);
		dbInstance.getCurrentEntityManager().merge(binder);
		return section;
	}
	
	public Section updateSection(Section section) {
		((SectionImpl)section).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(section);
	}
	
	public Section loadSectionByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select section from pfsection as section")
		  .append(" inner join fetch section.baseGroup as baseGroup")
		  .append(" where section.key=:sectionKey");
		
		List<Section> sections = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Section.class)
			.setParameter("sectionKey", key)
			.getResultList();
		return sections == null || sections.isEmpty() ? null : sections.get(0);
	}
	
	public List<Section> getSections(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select section from pfsection as section")
		  .append(" inner join fetch section.baseGroup as baseGroup")
		  .append(" where section.binder.key=:binderKey")
		  .append(" order by section.pos");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Section.class)
			.setParameter("binderKey", binder.getKey())
			.getResultList();
	}
	
	public Binder moveUpSection(BinderImpl binder, Section section) {
		binder.getSections().size();
		int index = binder.getSections().indexOf(section);
		if(index > 0) {
			Section reloadedPart = binder.getSections().remove(index);
			binder.getSections().add(index - 1, reloadedPart);
		} else if(index < 0) {
			binder.getSections().add(0, section);
		}
		return dbInstance.getCurrentEntityManager().merge(binder);
	}

	public Binder moveDownSection(BinderImpl binder, Section section) {
		binder.getSections().size();
		int index = binder.getSections().indexOf(section);
		if(index >= 0 && index + 1 < binder.getSections().size()) {
			Section reloadedSection = binder.getSections().remove(index);
			binder.getSections().add(index + 1, reloadedSection);
			binder = dbInstance.getCurrentEntityManager().merge(binder);
		}
		return binder;
	}
	
	/**
	 * Return only the course where the user is participant and 
	 * have a reference to a binder template.
	 * 
	 * @param participant
	 * @return
	 */
	public List<RepositoryEntry> searchCourseTemplates(IdentityRef participant) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
		  .append(" inner join fetch v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on (membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where v.status ").in(RepositoryEntryStatusEnum.published)
		  .append(" and exists (select ref.key from references as ref ")
		  .append("   inner join ref.target as targetOres")
		  .append("   where ref.source.key=ores.key and targetOres.resName='BinderTemplate'")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", participant.getKey())
				.getResultList();
	}
}
