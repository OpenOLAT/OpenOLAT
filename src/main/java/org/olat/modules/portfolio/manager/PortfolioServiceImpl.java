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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.modules.bc.vfs.OlatRootFileImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderLight;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessmentSectionChange;
import org.olat.modules.portfolio.model.AssessmentSectionImpl;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.BinderRow;
import org.olat.modules.portfolio.model.PageImpl;
import org.olat.modules.portfolio.model.SectionImpl;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {
	
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private CategoryDAO categoryDao;
	@Autowired
	private SharedByMeQueries sharedByMeQueries;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private SharedWithMeQueries sharedWithMeQueries;
	@Autowired
	private PortfolioFileStorage portfolioFileStorage;
	@Autowired
	private AssessmentSectionDAO assessmentSectionDao;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private List<MediaHandler> mediaHandlers;
	
	@Override
	public Binder createNewBinder(String title, String summary, String imagePath, Identity owner) {
		BinderImpl portfolio = binderDao.createAndPersist(title, summary, imagePath, null);
		groupDao.addMembership(portfolio.getBaseGroup(), owner, GroupRoles.owner.name());
		return portfolio;
	}

	@Override
	public OLATResource createBinderTemplateResource() {
		OLATResource resource = resourceManager.createOLATResourceInstance(BinderTemplateResource.TYPE_NAME);
		return resource;
	}

	@Override
	public void createAndPersistBinderTemplate(Identity owner, RepositoryEntry entry, Locale locale) {
		BinderImpl binder = binderDao.createAndPersist(entry.getDisplayname(), entry.getDescription(), null, entry);
		groupDao.addMembership(binder.getBaseGroup(), owner, GroupRoles.owner.name());
		//add section
		Translator pt = Util.createPackageTranslator(PortfolioHomeController.class, locale);
		String sectionTitle = pt.translate("new.section.title");
		String sectionDescription = pt.translate("new.section.desc");
		binderDao.createSection(sectionTitle, sectionDescription, null, null, binder);
	}

	@Override
	public Binder updateBinder(Binder binder) {
		return binderDao.updateBinder(binder);
	}

	@Override
	public void appendNewSection(String title, String description, Date begin, Date end, BinderRef binder) {
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		binderDao.createSection(title, description, begin, end, reloadedBinder);
	}

	@Override
	public Section updateSection(Section section) {
		return binderDao.updateSection(section);
	}

	@Override
	public List<Section> getSections(BinderRef binder) {
		return binderDao.getSections(binder);
	}

	@Override
	public Section getSection(SectionRef section) {
		return binderDao.loadSectionByKey(section.getKey());
	}

	@Override
	public List<Page> getPages(BinderRef binder) {
		return pageDao.getPages(binder);
	}

	@Override
	public List<Page> getPages(SectionRef section) {
		return pageDao.getPages(section);
	}

	@Override
	public List<Binder> getOwnedBinders(IdentityRef owner) {
		return binderDao.getOwnedBinders(owner);
	}

	@Override
	public List<BinderRow> searchOwnedBinders(IdentityRef owner) {
		return binderDao.searchOwnedBinders(owner);
	}
	
	@Override
	public List<Binder> searchSharedBindersBy(Identity owner) {
		return sharedByMeQueries.searchSharedBinders(owner);
	}

	@Override
	public List<AssessedBinder> searchSharedBindersWith(Identity coach) {
		return sharedWithMeQueries.searchSharedBinders(coach);
	}
	
	@Override
	public Binder getBinderByKey(Long portfolioKey) {
		return binderDao.loadByKey(portfolioKey);
	}

	@Override
	public Binder getBinderByResource(OLATResource resource) {
		return binderDao.loadByResource(resource);
	}

	@Override
	public Binder getBinderBySection(SectionRef section) {
		return binderDao.loadBySection(section);
	}
	
	@Override
	public boolean isTemplateInUse(Binder binder, RepositoryEntry courseEntry, String subIdent) {
		return binderDao.isTemplateInUse(binder, courseEntry, subIdent);
	}

	@Override
	public Binder getBinder(Identity owner, BinderRef templateBinder, RepositoryEntryRef courseEntry, String subIdent) {
		return binderDao.getBinder(owner, templateBinder, courseEntry, subIdent);
	}

	@Override
	public List<Binder> getBinders(Identity owner, RepositoryEntryRef courseEntry, String subIdent) {
		return binderDao.getBinders(owner, courseEntry, subIdent);
	}

	@Override
	public Binder assignBinder(Identity owner, BinderRef templateBinder, RepositoryEntry entry, String subIdent, Date deadline) {
		BinderImpl reloadedTemplate = (BinderImpl)binderDao.loadByKey(templateBinder.getKey());
		BinderImpl binder = binderDao.createCopy(reloadedTemplate, entry, subIdent);
		groupDao.addMembership(binder.getBaseGroup(), owner, GroupRoles.owner.name());
		return binder;
	}

	@Override
	public SynchedBinder loadAndSyncBinder(BinderRef binder) {
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		AtomicBoolean changes = new AtomicBoolean(false);
		if(reloadedBinder.getTemplate() != null) {
			reloadedBinder = binderDao
					.syncWithTemplate((BinderImpl)reloadedBinder.getTemplate(), (BinderImpl)reloadedBinder, changes);
		}
		return new SynchedBinder(reloadedBinder, changes.get());
	}

	@Override
	public List<Identity> getMembers(BinderRef binder, String... roles) {
		return binderDao.getMembers(binder, roles);
	}

	@Override
	public boolean isBinderVisible(Identity identity, Binder binder) {
		return true;
	}

	@Override
	public List<AccessRights> getAccessRights(Binder binder) {
		List<AccessRights> rights = binderDao.getBinderAccesRights(binder, null);
		List<AccessRights> sectionRights = binderDao.getSectionAccesRights(binder, null);
		rights.addAll(sectionRights);
		List<AccessRights> pageRights = binderDao.getPageAccesRights(binder, null);
		rights.addAll(pageRights);
		return rights;
	}
	
	@Override
	public List<AccessRights> getAccessRights(Binder binder, Identity identity) {
		List<AccessRights> rights = binderDao.getBinderAccesRights(binder, identity);
		List<AccessRights> sectionRights = binderDao.getSectionAccesRights(binder, identity);
		rights.addAll(sectionRights);
		List<AccessRights> pageRights = binderDao.getPageAccesRights(binder, identity);
		rights.addAll(pageRights);
		return rights;
	}

	@Override
	public void addAccessRights(PortfolioElement element, Identity identity, PortfolioRoles role) {
		Group baseGroup = element.getBaseGroup();
		if(!groupDao.hasRole(baseGroup, identity, role.name())) {
			groupDao.addMembership(baseGroup, identity, role.name());
		}
	}
	
	@Override
	public void changeAccessRights(List<Identity> identities, List<AccessRightChange> changes) {
		for(Identity identity:identities) {
			for(AccessRightChange change:changes) {
				Group baseGroup = change.getElement().getBaseGroup();
				if(change.isAdd()) {
					if(!groupDao.hasRole(baseGroup, identity, change.getRole().name())) {
						Group group = getGroup(change.getElement());
						groupDao.addMembership(group, identity, change.getRole().name());
					}
				} else {
					if(groupDao.hasRole(baseGroup, identity, change.getRole().name())) {
						Group group = getGroup(change.getElement());
						groupDao.removeMembership(group, identity, change.getRole().name());
					}
				}
			}
		}
	}
	
	private Group getGroup(PortfolioElement element) {
		if(element instanceof Page) {
			return pageDao.getGroup((Page)element);
		}
		if(element instanceof SectionRef) {
			return binderDao.getGroup((SectionRef)element);
		}
		if(element instanceof BinderRef) {
			return binderDao.getGroup((BinderRef)element);
		}
		return null;
	}

	@Override
	public List<Category> getCategories(Binder binder) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Binder.class, binder.getKey());
		return categoryDao.getCategories(ores);
	}

	@Override
	public void updateCategories(Binder binder, List<String> categories) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Binder.class, binder.getKey());
		updateCategories(ores, categories);
	}

	private void updateCategories(OLATResourceable oresource, List<String> categories) {
		List<Category> currentCategories = categoryDao.getCategories(oresource);
		Map<String,Category> currentCategoryMap = new HashMap<>();
		for(Category category:currentCategories) {
			currentCategoryMap.put(category.getName(), category);
		}
		
		List<String> newCategories = new ArrayList<>(categories);
		for(String newCategory:newCategories) {
			if(!currentCategoryMap.containsKey(newCategory)) {
				Category category = categoryDao.createAndPersistCategory(newCategory);
				categoryDao.appendRelation(oresource, category);
			}
		}
		
		for(Category currentCategory:currentCategories) {
			String name = currentCategory.getName();
			if(!newCategories.contains(name)) {
				categoryDao.removeRelation(oresource, currentCategory);
			}
		}
	}

	@Override
	public File getPosterImageFile(BinderLight binder) {
		String imagePath = binder.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			return new File(bcroot, imagePath);
		}
		return null;
	}
	
	@Override
	public VFSLeaf getPosterImageLeaf(BinderLight binder) {
		String imagePath = binder.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			OlatRootFileImpl leaf = new OlatRootFileImpl("/" + imagePath, null);
			if(leaf.exists()) {
				return leaf;
			}
		}
		return null;
	}

	@Override
	public String addPosterImageForBinder(File file, String filename) {
		File dir = portfolioFileStorage.generateBinderSubDirectory();
		File destinationFile = new File(dir, filename);
		String renamedFile = FileUtils.rename(destinationFile);
		if(renamedFile != null) {
			destinationFile = new File(dir, renamedFile);
		}
		FileUtils.copyFileToFile(file, destinationFile, false);
		return portfolioFileStorage.getRelativePath(destinationFile);
	}

	@Override
	public void removePosterImage(Binder binder) {
		String imagePath = binder.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			File file = new File(bcroot, imagePath);
			if(file.exists()) {
				file.delete();
			}
		}
	}

	@Override
	public List<Page> searchOwnedPages(IdentityRef owner) {
		List<Page> pages = pageDao.getOwnedPages(owner);
		return pages;
	}

	@Override
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, SectionRef section) {
		Section reloadedSection = section == null ? null : binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist(title, summary, null, reloadedSection, null);
		groupDao.addMembership(page.getBaseGroup(), owner, PortfolioRoles.owner.name());
		return page;
	}

	@Override
	public Page getPageByKey(Long key) {
		return pageDao.loadByKey(key);
	}

	@Override
	public Page updatePage(Page page, SectionRef newParentSection) {
		Page updatedPage;
		if(newParentSection == null) {
			updatedPage = pageDao.updatePage(page);
		} else {
			Section currentSection = null;
			if(page.getSection() != null) {
				currentSection = binderDao.loadSectionByKey(page.getSection().getKey());
				currentSection.getPages().remove(page);
			}
			
			Section newParent = binderDao.loadSectionByKey(newParentSection.getKey());
			((PageImpl)page).setSection(newParent);
			newParent.getPages().add(page);
			updatedPage = pageDao.updatePage(page);
			if(currentSection != null) {
				binderDao.updateSection(currentSection);
			}
			binderDao.updateSection(newParent);
		}
		return updatedPage;
	}

	@Override
	public File getPosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			return new File(bcroot, imagePath);
		}
		return null;
	}

	@Override
	public String addPosterImageForPage(File file, String filename) {
		File dir = portfolioFileStorage.generatePageSubDirectory();
		File destinationFile = new File(dir, filename);
		String renamedFile = FileUtils.rename(destinationFile);
		if(renamedFile != null) {
			destinationFile = new File(dir, renamedFile);
		}
		FileUtils.copyFileToFile(file, destinationFile, false);
		return portfolioFileStorage.getRelativePath(destinationFile);
	}

	@Override
	public void removePosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			File file = new File(bcroot, imagePath);
			if(file.exists()) {
				file.delete();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U appendNewPagePart(Page page, U part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		return (U)pageDao.persistPart(body, part);
	}

	@Override
	public List<PagePart> getPageParts(Page page) {
		return pageDao.getParts(page.getBody());
	}

	@Override
	public PagePart updatePart(PagePart part) {
		return pageDao.merge(part);
	}

	@Override
	public MediaHandler getMediaHandler(String type) {
		if(this.mediaHandlers != null) {
			for(MediaHandler handler:mediaHandlers) {
				if(type.equals(handler.getType())) {
					return handler;
				}
			}
		}
		return null;
	}

	@Override
	public void updateCategories(Media media, List<String> categories) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Media.class, media.getKey());
		updateCategories(ores, categories);
	}

	@Override
	public List<MediaLight> searchOwnedMedias(IdentityRef author) {
		return mediaDao.loadByAuthor(author);
	}

	@Override
	public Media getMediaByKey(Long key) {
		return mediaDao.loadByKey(key);
	}

	@Override
	public Page changePageStatus(Page page, PageStatus status) {
		Page reloadedPage = pageDao.loadByKey(page.getKey());
		((PageImpl)reloadedPage).setPageStatus(status);
		return pageDao.updatePage(reloadedPage);
	}

	@Override
	public Section changeSectionStatus(Section section, SectionStatus status, Identity coach) {
		PageStatus newPageStatus;
		if(status == SectionStatus.closed) {
			newPageStatus = PageStatus.closed;
		} else {
			newPageStatus = PageStatus.inRevision;
		}

		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		List<Page> pages = reloadedSection.getPages();
		for(Page page:pages) {
			if(page != null) {
				((PageImpl)page).setPageStatus(newPageStatus);
				pageDao.updatePage(page);
			}
		}
		
		((SectionImpl)reloadedSection).setSectionStatus(status);
		reloadedSection = binderDao.updateSection(reloadedSection);
		return reloadedSection;
	}

	@Override
	public List<AssessmentSection> getAssessmentSections(BinderRef binder, Identity coach) {
		return assessmentSectionDao.loadAssessmentSections(binder);
	}

	@Override
	public void updateAssessmentSections(BinderRef binderRef, List<AssessmentSectionChange> changes, Identity coachingIdentity) {
		Binder binder = binderDao.loadByKey(binderRef.getKey());
		Map<Identity,List<AssessmentSectionChange>> assessedIdentitiesToChangesMap = new HashMap<>();
		for(AssessmentSectionChange change:changes) {
			List<AssessmentSectionChange> identityChanges;
			if(assessedIdentitiesToChangesMap.containsKey(change.getIdentity())) {
				identityChanges = assessedIdentitiesToChangesMap.get(change.getIdentity());
			} else {
				identityChanges = new ArrayList<>();
				assessedIdentitiesToChangesMap.put(change.getIdentity(), identityChanges);
			}
			identityChanges.add(change);
		}

		for(Map.Entry<Identity,List<AssessmentSectionChange>> changesEntry:assessedIdentitiesToChangesMap.entrySet()) {
			Identity assessedIdentity = changesEntry.getKey();
			List<AssessmentSection> currentAssessmentSections = assessmentSectionDao.loadAssessmentSections(binder, assessedIdentity);
			Set<AssessmentSection> updatedAssessmentSections = new HashSet<>(currentAssessmentSections);
			
			List<AssessmentSectionChange> identityChanges = changesEntry.getValue();
			//check update or create

			for(AssessmentSectionChange change:identityChanges) {
				AssessmentSection assessmentSection = change.getAssessmentSection();
				for(AssessmentSection currentAssessmentSection:currentAssessmentSections) {
					if(assessmentSection != null && assessmentSection.equals(currentAssessmentSection)) {
						assessmentSection = currentAssessmentSection;
					} else if(change.getSection().equals(currentAssessmentSection.getSection())) {
						assessmentSection = currentAssessmentSection;
					}
				}
				
				if(assessmentSection == null) {
					assessmentSection = assessmentSectionDao
							.createAssessmentSection(change.getScore(), change.getPassed(), change.getSection(), assessedIdentity);
				} else {
					((AssessmentSectionImpl)assessmentSection).setScore(change.getScore());
					((AssessmentSectionImpl)assessmentSection).setPassed(change.getPassed());
					assessmentSection = assessmentSectionDao.update(assessmentSection);
				}
				updatedAssessmentSections.add(assessmentSection);
			}

			updateAssessmentEntry(assessedIdentity, binder, updatedAssessmentSections, coachingIdentity);
		}
	}
	
	private void updateAssessmentEntry(Identity assessedIdentity, Binder binder, Set<AssessmentSection> assessmentSections, Identity coachingIdentity) {
		
		boolean allPassed = true;
		int totalSectionDone = 0;
		BigDecimal totalScore = new BigDecimal("0.0");
		AssessmentEntryStatus binderStatus = null;

		for(AssessmentSection assessmentSection:assessmentSections) {
			if(assessmentSection.getScore() != null) {
				totalScore = totalScore.add(assessmentSection.getScore());
			}
			if(assessmentSection.getPassed() != null) {
				totalSectionDone++;
				allPassed &= assessmentSection.getPassed().booleanValue();
			}
		}
		
		Boolean totalPassed = null;
		if(totalSectionDone == assessmentSections.size()) {
			totalPassed = new Boolean(allPassed);
			binderStatus = AssessmentEntryStatus.done;
		} else {
			binderStatus = AssessmentEntryStatus.inProgress;
		}
		//order status from the entry / section

		RepositoryEntry entry = binder.getEntry();
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			if(courseNode instanceof PortfolioCourseNode) {
				PortfolioCourseNode pfNode = (PortfolioCourseNode)courseNode;
				ScoreEvaluation scoreEval= new ScoreEvaluation(totalScore.floatValue(), totalPassed, binderStatus, true, binder.getKey());
				UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
				pfNode.updateUserScoreEvaluation(scoreEval, userCourseEnv, coachingIdentity, false);
			}
		} else {
			OLATResource resource = ((BinderImpl)binder.getTemplate()).getOlatResource();
			RepositoryEntry referenceEntry = repositoryService.loadByResourceKey(resource.getKey());
			AssessmentEntry assessmentEntry = assessmentService
					.getOrCreateAssessmentEntry(assessedIdentity, binder.getEntry(), binder.getSubIdent(), referenceEntry);
			assessmentEntry.setScore(totalScore);
			assessmentEntry.setPassed(totalPassed);
			assessmentEntry.setAssessmentStatus(binderStatus);
			assessmentService.updateAssessmentEntry(assessmentEntry);
		}
	}
}
