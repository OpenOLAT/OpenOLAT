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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderDeliveryOptions;
import org.olat.modules.portfolio.BinderLight;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioElementType;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessedPage;
import org.olat.modules.portfolio.model.AssessmentSectionChange;
import org.olat.modules.portfolio.model.AssessmentSectionImpl;
import org.olat.modules.portfolio.model.AssignmentImpl;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.BinderPageUsage;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.CategoryLight;
import org.olat.modules.portfolio.model.PageImpl;
import org.olat.modules.portfolio.model.SearchSharePagesParameters;
import org.olat.modules.portfolio.model.SectionImpl;
import org.olat.modules.portfolio.model.SectionKeyRef;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyCompetenceDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioServiceImpl.class);
	
	private static XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		configXstream.alias("deliveryOptions", BinderDeliveryOptions.class);
		XStream.setupDefaultSecurity(configXstream);
		Class<?>[] types = new Class[] {
				BinderDeliveryOptions.class
		};
		configXstream.addPermission(new ExplicitTypePermission(types));
	}
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private CommentDAO commentDao;
	@Autowired
	private CategoryDAO categoryDao;
	@Autowired
	private AssignmentDAO assignmentDao;
	@Autowired
	private PageUserInfosDAO pageUserInfosDao;
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
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private PortfolioPageToTaxonomyCompetenceDAO portfolioPageToTaxonomyCompetenceDAO;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private BinderUserInformationsDAO binderUserInformationsDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDAO;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDAO;
	
	@Autowired
	private List<MediaHandler> mediaHandlers;
	
	@Override
	public Binder createNewBinder(String title, String summary, String imagePath, Identity owner) {
		BinderImpl portfolio = binderDao.createAndPersist(title, summary, imagePath, null);
		if(owner != null) {
			groupDao.addMembershipTwoWay(portfolio.getBaseGroup(), owner, PortfolioRoles.owner.name());
		}
		return portfolio;
	}

	@Override
	public OLATResource createBinderTemplateResource() {
		return resourceManager.createOLATResourceInstance(BinderTemplateResource.TYPE_NAME);
	}

	@Override
	public void createAndPersistBinderTemplate(Identity owner, RepositoryEntry entry, Locale locale) {
		BinderImpl binder = binderDao.createAndPersist(entry.getDisplayname(), entry.getDescription(), null, entry);
		if(owner != null) {
			groupDao.addMembershipTwoWay(binder.getBaseGroup(), owner, PortfolioRoles.owner.name());
		}
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
	public Binder copyBinder(Binder transientBinder, RepositoryEntry entry) {
		String imagePath = null;
		if(StringHelper.containsNonWhitespace(transientBinder.getImagePath())) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			File image = new File(bcroot, transientBinder.getImagePath());
			if(image.exists()) {
				imagePath = addPosterImageForBinder(image, image.getName());
			}
		}
		return internalCopyTransientBinder(transientBinder, entry, imagePath, true);
	}

	@Override
	public Binder importBinder(Binder transientBinder, RepositoryEntry templateEntry, File image) {
		String imagePath = null;
		if(StringHelper.containsNonWhitespace(transientBinder.getImagePath())) {
			imagePath = addPosterImageForBinder(image, image.getName());
		}
		return internalCopyTransientBinder(transientBinder, templateEntry, imagePath, false);
	}
	
	private Binder internalCopyTransientBinder(Binder transientBinder, RepositoryEntry entry, String imagePath, boolean copy) {
		Binder binder = binderDao.createAndPersist(transientBinder.getTitle(), transientBinder.getSummary(), imagePath, entry);
		//copy sections
		for(Section transientSection:((BinderImpl)transientBinder).getSections()) {
			SectionImpl section = binderDao.createSection(transientSection.getTitle(), transientSection.getDescription(),
					transientSection.getBeginDate(), transientSection.getEndDate(), binder);
			
			List<Assignment> transientAssignments = ((SectionImpl)transientSection).getAssignments();
			for(Assignment transientAssignment:transientAssignments) {
				if(transientAssignment != null) {
					
					RepositoryEntry formEntry = null;
					if(transientAssignment.getAssignmentType() == AssignmentType.form) {
						formEntry = loadImportedFormEntry(transientAssignment);
						if(formEntry == null) {
							continue;
						}
					}

					File newStorage = portfolioFileStorage.generateAssignmentSubDirectory();
					String storage = portfolioFileStorage.getRelativePath(newStorage);
					assignmentDao.createAssignment(transientAssignment.getTitle(), transientAssignment.getSummary(),
							transientAssignment.getContent(), storage, transientAssignment.getAssignmentType(),
							transientAssignment.isTemplate(), transientAssignment.getAssignmentStatus(), section, null,
							transientAssignment.isOnlyAutoEvaluation(), transientAssignment.isReviewerSeeAutoEvaluation(),
							transientAssignment.isAnonymousExternalEvaluation(), formEntry);
					//copy attachments
					File templateDirectory = portfolioFileStorage.getAssignmentDirectory(transientAssignment);
					if(copy && templateDirectory != null) {
						FileUtils.copyDirContentsToDir(templateDirectory, newStorage, false, "Assignment attachments");
					}
				}
			}
		}
		return binder;
	}
	
	private RepositoryEntry loadImportedFormEntry(Assignment transientAssignment) {
		try {
			RepositoryEntry formEntry = transientAssignment.getFormEntry();
			if(formEntry != null) {
				formEntry = repositoryManager.lookupRepositoryEntryBySoftkey(formEntry.getSoftkey(), false);
			}
			return formEntry;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public boolean detachCourseFromBinders(RepositoryEntry entry) {
		int deletedRows = binderDao.detachBinderFromRepositoryEntry(entry);
		return deletedRows > 0;
	}

	@Override
	public boolean detachRepositoryEntryFromBinders(RepositoryEntry entry, PortfolioCourseNode courseNode) {
		int deletedRows = binderDao.detachBinderFromRepositoryEntry(entry, courseNode);
		return deletedRows > 0;
	}

	@Override
	public boolean deleteBinderTemplate(Binder binder, RepositoryEntry templateEntry) {
		BinderImpl reloadedBinder = (BinderImpl)binderDao.loadByKey(binder.getKey());
		int deletedRows = binderDao.deleteBinderTemplate(reloadedBinder);
		return deletedRows > 0;
	}
	
	@Override
	public boolean deleteBinder(BinderRef binder) {
		int rows = binderDao.deleteBinder(binder);
		return rows > 0;
	}

	@Override
	public BinderDeliveryOptions getDeliveryOptions(OLATResource resource) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(resource);
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		
		BinderDeliveryOptions config;
		if(configXml.exists()) {
			config = (BinderDeliveryOptions)configXstream.fromXML(configXml);
		} else {
			//set default config
			config = BinderDeliveryOptions.defaultOptions();
			setDeliveryOptions(resource, config);
		}
		return config;
	}

	@Override
	public void setDeliveryOptions(OLATResource resource, BinderDeliveryOptions options) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(resource);
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		if(options == null) {
			FileUtils.deleteFile(configXml);
		} else {
			try (OutputStream out = new FileOutputStream(configXml)) {
				configXstream.toXML(options, out);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
	
	@Override
	public Assignment addAssignment(String title, String summary, String content, AssignmentType type, boolean template, Section section, Binder binder,
			boolean onlyAutoEvaluation, boolean reviewerSeeAutoEvaluation, boolean anonymousExternEvaluation, RepositoryEntry formEntry) {
		File newStorage = portfolioFileStorage.generateAssignmentSubDirectory();
		String storage = portfolioFileStorage.getRelativePath(newStorage);

		Binder reloadedBinder = binder == null ? null : binderDao.loadByKey(binder.getKey());
		Section reloadedSection = section == null ? null : binderDao.loadSectionByKey(section.getKey());
		return assignmentDao.createAssignment(title, summary, content, storage, type, template,
				AssignmentStatus.template, reloadedSection, reloadedBinder,
				onlyAutoEvaluation, reviewerSeeAutoEvaluation, anonymousExternEvaluation, formEntry);
	}

	@Override
	public Assignment updateAssignment(Assignment assignment, String title, String summary, String content, AssignmentType type,
			boolean onlyAutoEvaluation, boolean reviewerSeeAutoEvaluation, boolean anonymousExternEvaluation, RepositoryEntry formEntry) {
		if(!StringHelper.containsNonWhitespace(assignment.getStorage())) {
			File newStorage = portfolioFileStorage.generateAssignmentSubDirectory();
			String newRelativeStorage = portfolioFileStorage.getRelativePath(newStorage);
			((AssignmentImpl)assignment).setStorage(newRelativeStorage);
		}
		
		AssignmentImpl impl = (AssignmentImpl)assignment;
		impl.setTitle(title);
		impl.setSummary(summary);
		impl.setContent(content);
		impl.setType(type.name());
		impl.setOnlyAutoEvaluation(onlyAutoEvaluation);
		impl.setReviewerSeeAutoEvaluation(reviewerSeeAutoEvaluation);
		impl.setAnonymousExternalEvaluation(anonymousExternEvaluation);
		impl.setFormEntry(formEntry);
		return assignmentDao.updateAssignment(assignment);
	}
	
	@Override
	public Section moveUpAssignment(Section section, Assignment assignment) {
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		return assignmentDao.moveUpAssignment((SectionImpl)reloadedSection, assignment);
	}

	@Override
	public Section moveDownAssignment(Section section, Assignment assignment) {
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		return assignmentDao.moveDownAssignment((SectionImpl)reloadedSection, assignment);
	}

	@Override
	public void moveAssignment(SectionRef currentSectionRef, Assignment assignment, SectionRef newParentSectionRef) {
		Section currentSection = binderDao.loadSectionByKey(currentSectionRef.getKey());
		Section newParentSection = binderDao.loadSectionByKey(newParentSectionRef.getKey());
		assignmentDao.moveAssignment((SectionImpl)currentSection, assignment, (SectionImpl)newParentSection);
	}

	@Override
	public List<Assignment> getSectionsAssignments(PortfolioElement element, String searchString) {
		if(element.getType() == PortfolioElementType.binder) {
			return assignmentDao.loadAssignments((BinderRef)element, searchString);
		}
		if(element.getType() == PortfolioElementType.section) {
			return assignmentDao.loadAssignments((SectionRef)element, searchString);
		}
		if(element.getType() == PortfolioElementType.page) {
			return assignmentDao.loadAssignments((Page)element, searchString);
		}
		return null;
	}

	@Override
	public List<Assignment> getBindersAssignmentsTemplates(BinderRef binder) {
		return assignmentDao.loadBinderAssignmentsTemplates(binder);
	}

	@Override
	public boolean hasBinderAssignmentTemplate(BinderRef binder) {
		return assignmentDao.hasBinderAssignmentTemplate(binder);
	}

	@Override
	public List<Assignment> searchOwnedAssignments(IdentityRef assignee) {
		return assignmentDao.getOwnedAssignments(assignee);
	}

	@Override
	public boolean isAssignmentInUse(Assignment assignment) {
		return assignmentDao.isAssignmentInUse(assignment);
	}

	@Override
	public boolean deleteAssignment(Assignment assignment) {
		Assignment reloadedAssignment = assignmentDao.loadAssignmentByKey(assignment.getKey());
		Section reloadedSection = reloadedAssignment.getSection();
		Binder reloadedBinder = reloadedAssignment.getBinder();

		if(reloadedSection != null) {
			((SectionImpl)reloadedSection).getAssignments().remove(reloadedAssignment);
			assignmentDao.deleteAssignment(reloadedAssignment);
			binderDao.updateSection(reloadedSection);
		} else if(reloadedBinder != null) {
			Set<Binder> bindersToUpdate = new HashSet<>();
			List<Assignment> synchedBindersAssignments = assignmentDao.loadAssignmentReferences(reloadedAssignment);
			for(Assignment synchedAssignment:synchedBindersAssignments) {
				List<Assignment> instantiatedAssignments = assignmentDao.loadAssignmentReferences(synchedAssignment);
				Set<Section> sectionsToUpdate = new HashSet<>();
				for(Assignment instantiatedAssignment:instantiatedAssignments) {
					if(instantiatedAssignment.getSection() != null) {
						Section assignmentSection = instantiatedAssignment.getSection();
						if(((SectionImpl)assignmentSection).getAssignments().remove(instantiatedAssignment)) {
							sectionsToUpdate.add(assignmentSection);
						}
						assignmentDao.deleteAssignment(instantiatedAssignment);
					}
				}
				for(Section section:sectionsToUpdate) {
					binderDao.updateSection(section);
				}
				
				if(synchedAssignment.getBinder() != null) {
					Binder synchedBinder = synchedAssignment.getBinder();
					if(((BinderImpl)synchedBinder).getAssignments().remove(reloadedAssignment)) {
						bindersToUpdate.add(synchedBinder);
					}
					assignmentDao.deleteAssignment(synchedAssignment);
				}
			}
			
			for(Binder binder:bindersToUpdate) {
				binderDao.updateBinder(binder);
			}
			assignmentDao.deleteAssignment(reloadedAssignment);
			binderDao.updateBinder(reloadedBinder);
		}
		return true;
	}

	@Override
	public Assignment startAssignment(Long assignmentKey, Identity author) {
		Assignment reloadedAssignment = assignmentDao.loadAssignmentByKey(assignmentKey);
		if (reloadedAssignment.getPage() == null) {
			Section section = reloadedAssignment.getSection();
			if (reloadedAssignment.getAssignmentType() == AssignmentType.essay
					|| reloadedAssignment.getAssignmentType() == AssignmentType.document) {
				Page page = appendNewPage(author, reloadedAssignment.getTitle(), reloadedAssignment.getSummary(), null, null, section);
				reloadedAssignment = assignmentDao.startEssayAssignment(reloadedAssignment, page, author);
			} else if (reloadedAssignment.getAssignmentType() == AssignmentType.form) {
				RepositoryEntry formEntry = reloadedAssignment.getFormEntry();
				Page page = appendNewPage(author, reloadedAssignment.getTitle(), reloadedAssignment.getSummary(), null, false, null, section, null);
				reloadedAssignment = assignmentDao.startFormAssignment(reloadedAssignment, page, author);
				// create the session for the assignee
				EvaluationFormSurvey survey = loadOrCreateSurvey(page.getBody(), formEntry);
				loadOrCreateSession(survey, author);
			}
		}
		dbInstance.commit();
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_ASSIGNMENT_STARTED, getClass(),
				LoggingResourceable.wrap(reloadedAssignment.getSection()),
				LoggingResourceable.wrap(reloadedAssignment));
		return reloadedAssignment;
	}
	
	@Override
	public Page startAssignmentFromTemplate(Long assignmentKey, Identity author, String title, String summary, String imagePath, PageImageAlign align,
			SectionRef sectionRef, Boolean onlyAutoEvaluation, Boolean reviewerCanSeeAutoEvaluation) {
		Page page = null;
		Section section = binderDao.loadSectionByKey(sectionRef.getKey());
		Assignment reloadedAssignmentTemplate = assignmentDao.loadAssignmentByKey(assignmentKey);
		Assignment instanciatedAssignment = assignmentDao
				.createAssignment(reloadedAssignmentTemplate, AssignmentStatus.inProgress, section, null,
						false, onlyAutoEvaluation, reviewerCanSeeAutoEvaluation);
		if (instanciatedAssignment.getPage() == null) {
			if (instanciatedAssignment.getAssignmentType() == AssignmentType.essay
					|| instanciatedAssignment.getAssignmentType() == AssignmentType.document) {
				page = appendNewPage(author, title, summary, null, null, section);
				instanciatedAssignment = assignmentDao.startEssayAssignment(instanciatedAssignment, page, author);
			} else if (instanciatedAssignment.getAssignmentType() == AssignmentType.form) {
				RepositoryEntry formEntry = instanciatedAssignment.getFormEntry();
				if(!StringHelper.containsNonWhitespace(title)) {
					title = instanciatedAssignment.getTitle();
				}
				page = appendNewPage(author, title, instanciatedAssignment.getSummary(), null, false, null, section, null);
				instanciatedAssignment = assignmentDao.startFormAssignment(instanciatedAssignment, page, author);
				// create the session for the assignee
				EvaluationFormSurvey survey = loadOrCreateSurvey(page.getBody(), formEntry);
				loadOrCreateSession(survey, author);
			}
		}
		dbInstance.commit();
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_ASSIGNMENT_STARTED, getClass(),
				LoggingResourceable.wrap(instanciatedAssignment.getSection()),
				LoggingResourceable.wrap(instanciatedAssignment));
		return page;
	}

	@Override
	public Assignment getAssignment(PageBody body) {
		return assignmentDao.loadAssignment(body);
	}

	@Override
	public SectionRef appendNewSection(String title, String description, Date begin, Date end, BinderRef binder) {
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		SectionImpl newSection = binderDao.createSection(title, description, begin, end, reloadedBinder);
		return new SectionKeyRef(newSection.getKey());
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
	public Binder moveUpSection(Binder binder, Section section) {
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		return binderDao.moveUpSection((BinderImpl)reloadedBinder, section);
	}

	@Override
	public Binder moveDownSection(Binder binder, Section section) {
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		return binderDao.moveDownSection((BinderImpl)reloadedBinder, section);
	}

	@Override
	public Binder deleteSection(Binder binder, Section section) {
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Binder reloadedBinder = reloadedSection.getBinder();
		return binderDao.deleteSection(reloadedBinder, reloadedSection);
	}

	@Override
	public List<Page> getPages(BinderRef binder, String searchString) {
		return pageDao.getPages(binder, searchString);
	}

	@Override
	public List<Page> getPages(SectionRef section) {
		return pageDao.getPages(section);
	}
	
	@Override
	public List<Page> getPages(PortfolioServiceSearchOptions options) {
		return pageDao.getPages(options);
	}

	@Override
	public List<Binder> getOwnedBinders(IdentityRef owner) {
		return binderDao.getOwnedBinders(owner);
	}

	@Override
	public List<BinderStatistics> searchOwnedBinders(IdentityRef owner) {
		return binderDao.searchOwnedBinders(owner, false);
	}

	@Override
	public int countOwnedBinders(IdentityRef owner) {
		return binderDao.countOwnedBinders(owner, false);
	}

	@Override
	public List<BinderStatistics> searchOwnedDeletedBinders(IdentityRef owner) {
		return binderDao.searchOwnedBinders(owner, true);
	}

	@Override
	public List<BinderStatistics> searchOwnedLastBinders(IdentityRef owner, int maxResults) {
		return binderDao.searchOwnedLastBinders(owner, maxResults);
	}

	@Override
	public List<Binder> searchOwnedBindersFromCourseTemplate(IdentityRef owner) {
		return binderDao.getOwnedBinderFromCourseTemplate(owner);
	}

	@Override
	public List<Binder> searchSharedBindersBy(Identity owner, String searchString) {
		return sharedByMeQueries.searchSharedBinders(owner, searchString);
	}

	@Override
	public List<AssessedBinder> searchSharedBindersWith(Identity coach, String searchString) {
		return sharedWithMeQueries.searchSharedBinders(coach, searchString);
	}
	
	@Override
	public List<AssessedPage> searchSharedPagesWith(Identity coach, SearchSharePagesParameters params) {
		return sharedWithMeQueries.searchSharedPagesEntries(coach, params);
	}

	@Override
	public List<RepositoryEntry> searchCourseWithBinderTemplates(Identity participant) {
		return binderDao.searchCourseTemplates(participant);
	}

	@Override
	public Binder getBinderByKey(Long portfolioKey) {
		return binderDao.loadByKey(portfolioKey);
	}

	@Override
	public void updateBinderUserInformations(Binder binder, Identity user) {
		if(binder == null || user == null) return;
		binderUserInformationsDao.updateBinderUserInformations(binder, user);
	}

	@Override
	public Binder getBinderByResource(OLATResource resource) {
		return binderDao.loadByResource(resource);
	}

	@Override
	public BinderStatistics getBinderStatistics(BinderRef binder) {
		return binderDao.getBinderStatistics(binder);
	}

	@Override
	public RepositoryEntry getRepositoryEntry(Binder binder) {
		OLATResource resource = ((BinderImpl)binder).getOlatResource();
		Long resourceKey = resource.getKey();
		return repositoryService.loadByResourceKey(resourceKey);
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
	public int getTemplateUsage(RepositoryEntryRef templateEntry) {
		return binderDao.getTemplateUsage(templateEntry);
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
		groupDao.addMembershipTwoWay(binder.getBaseGroup(), owner, PortfolioRoles.owner.name());
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
	public boolean isMember(BinderRef binder, IdentityRef identity, String... roles) {
		return binderDao.isMember(binder, identity, roles);
	}

	@Override
	public List<Identity> getMembers(BinderRef binder, String... roles) {
		return binderDao.getMembers(binder, roles);
	}

	@Override
	public List<Identity> getMembers(Page page, String... roles) {
		return pageDao.getMembers(page, roles);
	}

	@Override
	public boolean isBinderVisible(IdentityRef identity, BinderRef binder) {
		return binderDao.isMember(binder, identity,
				PortfolioRoles.owner.name(), PortfolioRoles.coach.name(),
				PortfolioRoles.reviewer.name(), PortfolioRoles.invitee.name());
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
	public List<AccessRights> getAccessRights(Page page) {
		List<AccessRights> rights = binderDao.getBinderAccesRights(page);
		List<AccessRights> sectionRights = binderDao.getSectionAccesRights(page);
		rights.addAll(sectionRights);
		List<AccessRights> pageRights = binderDao.getPageAccesRights(page);
		rights.addAll(pageRights);
		return rights;
	}

	@Override
	public void addAccessRights(PortfolioElement element, Identity identity, PortfolioRoles role) {
		Group baseGroup = element.getBaseGroup();
		if(!groupDao.hasRole(baseGroup, identity, role.name())) {
			groupDao.addMembershipTwoWay(baseGroup, identity, role.name());
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
						groupDao.addMembershipOneWay(group, identity, change.getRole().name());
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
	
	@Override
	public void removeAccessRights(Binder binder, Identity identity, PortfolioRoles... roles) {
		Set<PortfolioRoles> roleSet = new HashSet<>();
		if(roles != null && roles.length > 0) {
			for(PortfolioRoles role:roles) {
				if(role != null) {
					roleSet.add(role);
				}
			}
		}
		
		if(roleSet.isEmpty()) {
			log.warn("Want to remove rights without specifying the roles.");
			return;
		}

		List<AccessRights> rights = getAccessRights(binder, identity);
		for(AccessRights right:rights) {
			PortfolioRoles role = right.getRole();
			if(roleSet.contains(role)) {
				Group baseGroup;
				if(right.getType() == PortfolioElementType.binder) {
					baseGroup = binderDao.loadByKey(right.getBinderKey()).getBaseGroup();
				} else if(right.getType() == PortfolioElementType.section) {
					baseGroup = binderDao.loadSectionByKey(right.getSectionKey()).getBaseGroup();
				} else if(right.getType() == PortfolioElementType.page) {
					baseGroup = pageDao.loadByKey(right.getPageKey()).getBaseGroup();
				} else {
					continue;
				}
				
				if(groupDao.hasRole(baseGroup, identity, right.getRole().name())) {
					groupDao.removeMembership(baseGroup, identity, right.getRole().name());
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
	public List<Category> getCategories(PortfolioElement element) {
		OLATResourceable ores = getOLATResoucreable(element);
		return categoryDao.getCategories(ores);
	}

	@Override
	public List<CategoryToElement> getCategorizedSectionsAndPages(BinderRef binder) {
		return categoryDao.getCategorizedSectionsAndPages(binder);
	}

	@Override
	public List<CategoryToElement> getCategorizedSectionAndPages(SectionRef section) {
		return categoryDao.getCategorizedSectionAndPages(section);
	}

	@Override
	public List<CategoryToElement> getCategorizedOwnedPages(IdentityRef owner) {
		return categoryDao.getCategorizedOwnedPages(owner);
	}

	@Override
	public void updateCategories(PortfolioElement element, List<String> categories) {
		OLATResourceable ores = getOLATResoucreable(element);
		updateCategories(ores, categories);
	}
	
	private OLATResourceable getOLATResoucreable(PortfolioElement element) {
		switch(element.getType()) {
			case binder: return OresHelper.createOLATResourceableInstance(Binder.class, element.getKey());
			case section: return OresHelper.createOLATResourceableInstance(Section.class, element.getKey());
			case page: return OresHelper.createOLATResourceableInstance(Page.class, element.getKey());
			default: return null;
		}
	}

	private void updateCategories(OLATResourceable oresource, List<String> categories) {
		List<Category> existingCategories = categoryDao.getCategories();
		Map<String, Category> existingCategoriesMap = existingCategories.stream().collect(Collectors.toMap(category -> category.getName(), category -> category, (cat1, cat2) -> cat1));
		
		List<Category> currentCategories = categoryDao.getCategories(oresource);
		Map<String,Category> currentCategoryMap = currentCategories.stream().collect(Collectors.toMap(category -> category.getName(), category -> category, (cat1, cat2) -> cat1));
		
		List<String> newCategories = new ArrayList<>(categories);
		for(String newCategory:newCategories) {
			if(!existingCategoriesMap.containsKey(newCategory)) {
				Category category = categoryDao.createAndPersistCategory(newCategory);
				categoryDao.appendRelation(oresource, category);
			} else if (!currentCategoryMap.containsKey(newCategory)) {
				categoryDao.appendRelation(oresource, existingCategoriesMap.get(newCategory));
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
	public Map<Long,Long> getNumberOfComments(BinderRef binder) {
		return commentDao.getNumberOfComments(binder);
	}
	
	@Override
	public Map<Long,Long> getNumberOfComments(SectionRef section) {
		return commentDao.getNumberOfComments(section);
	}
	
	@Override
	public Map<Long,Long> getNumberOfCommentsOnOwnedPage(IdentityRef owner) {
		return commentDao.getNumberOfCommentsOnOwnedPage(owner);
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
			VFSLeaf leaf = VFSManager.olatRootLeaf("/" + imagePath);
			if(leaf.exists()) {
				return leaf;
			}
		}
		return null;
	}

	@Override
	public String addPosterImageForBinder(File file, String filename) {
		File dir = portfolioFileStorage.generateBinderSubDirectory();
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = file.getName();
		}
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
				boolean deleted = file.delete();
				if(!deleted) {
					log.warn("Cannot delete: {}", file);
				}
			}
		}
	}

	@Override
	public int countOwnedPages(IdentityRef owner) {
		return pageDao.countOwnedPages(owner);
	}

	@Override
	public List<Page> searchOwnedPages(IdentityRef owner, String searchString) {
		return pageDao.getOwnedPages(owner, searchString);
	}

	@Override
	public List<Page> searchOwnedLastPages(IdentityRef owner, int maxResults) {
		return pageDao.getLastPages(owner, maxResults);
	}

	@Override
	public List<Page> searchDeletedPages(IdentityRef owner, String searchString) {
		return pageDao.getDeletedPages(owner, searchString);
	}

	@Override
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, PageImageAlign align,
			SectionRef section) {
		return appendNewPage(owner, title, summary, imagePath, true, align, section, null);
	}
	
	@Override
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, PageImageAlign align,
			SectionRef section, Page pageDelegate) {
		return appendNewPage(owner, title, summary, imagePath, true, align, section, pageDelegate);
	}

	private Page appendNewPage(Identity owner, String title, String summary, String imagePath, boolean editable, PageImageAlign align,
			SectionRef section, Page pageDelegate) {
		Section reloadedSection = section == null ? null : binderDao.loadSectionByKey(section.getKey());
		if(reloadedSection != null && reloadedSection.getSectionStatus() == SectionStatus.notStarted) {
			((SectionImpl)reloadedSection).setSectionStatus(SectionStatus.inProgress);
		}
		Page page = pageDao.createAndPersist(title, summary, imagePath, align, editable, reloadedSection, pageDelegate);
		if(pageDelegate != null) {
			for (TaxonomyCompetence competence : portfolioPageToTaxonomyCompetenceDAO.getCompetencesToPortfolioPage(pageDelegate, false)) {
				portfolioPageToTaxonomyCompetenceDAO.createRelation(page, taxonomyCompetenceDAO.createTaxonomyCompetence(competence.getCompetenceType(), competence.getTaxonomyLevel(), competence.getIdentity(), competence.getExpiration()));
			}
			updateCategories(page, getCategories(pageDelegate).stream().map(cat -> cat.getName()).collect(Collectors.toList()));
		}
		
		groupDao.addMembershipTwoWay(page.getBaseGroup(), owner, PortfolioRoles.owner.name());
		return page;
	}

	@Override
	public Page getPageByKey(Long key) {
		return pageDao.loadByKey(key);
	}

	@Override
	public Page getPageByBody(PageBody body) {
		return pageDao.loadByBody(body);
	}

	@Override
	public List<Page> getLastPages(Identity owner, int maxResults) {
		return pageDao.getLastPages(owner, maxResults);
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
			FileUtils.deleteFile(file);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U appendNewPagePart(Page page, U part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		return (U)pageDao.persistPart(body, part);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U appendNewPagePartAt(Page page, U part, int index) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		return (U)pageDao.persistPart(body, part, index);
	}

	@Override
	public void removePagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.removePart(body, part);
	}

	@Override
	public void moveUpPagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.moveUpPart(body, part);
	}

	@Override
	public void moveDownPagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.moveDownPart(body, part);
	}

	@Override
	public void movePagePart(Page page, PagePart partToMove, PagePart sibling, boolean after) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.movePart(body, partToMove, sibling, after);
	}

	@Override
	public Page removePage(Page page) {
		// will take care of the assignments
		return pageDao.removePage(page);
	}	

	@Override
	public void deletePage(Page page) {
		Page reloadedPage = pageDao.loadByKey(page.getKey());
		pageDao.deletePage(reloadedPage);
		pageUserInfosDao.delete(page);
	}

	@Override
	public List<PagePart> getPageParts(Page page) {
		return pageDao.getParts(page.getBody());
	}

	@Override
	public int countSharedPageBody(Page page) {
		return pageDao.getCountSharedPageBody(page);
	}
	
	@Override
	public List<Page> getPagesSharingSameBody(Page page) {
		return pageDao.getPagesBySharedBody(page);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U updatePart(U part) {
		return (U)pageDao.merge(part);
	}

	@Override
	public MediaHandler getMediaHandler(String type) {
		if(mediaHandlers != null) {
			for(MediaHandler handler:mediaHandlers) {
				if(type.equals(handler.getType())) {
					return handler;
				}
			}
		}
		return null;
	}

	@Override
	public List<MediaHandler> getMediaHandlers() {
		return new ArrayList<>(mediaHandlers);
	}

	@Override
	public Media updateMedia(Media media) {
		return mediaDao.update(media);
	}

	@Override
	public void deleteMedia(Media media) {
		mediaDao.deleteMedia(media);
	}

	@Override
	public void updateCategories(Media media, List<String> categories) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Media.class, media.getKey());
		updateCategories(ores, categories);
	}

	@Override
	public List<Category> getCategories(Media media) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Media.class, media.getKey());
		return categoryDao.getCategories(ores);
	}

	@Override
	public List<CategoryLight> getMediaCategories(IdentityRef owner) {
		return categoryDao.getMediaCategories(owner);
	}

	@Override
	public List<MediaLight> searchOwnedMedias(IdentityRef author, String searchString, List<String> tagNames) {
		return mediaDao.searchByAuthor(author, searchString, tagNames);
	}

	@Override
	public Media getMediaByKey(Long key) {
		return mediaDao.loadByKey(key);
	}

	@Override
	public List<BinderPageUsage> getUsedInBinders(MediaLight media) {
		return mediaDao.usedInBinders(media);
	}

	@Override
	public boolean isPageBodyClosed(Page page) {
		List<Page> allPages = pageDao.getPagesBySharedBody(page);
		for(Page p:allPages) {
			if(PageStatus.isClosed(p)) {
				return true;
			}	
		}
		return false;
	}

	@Override
	public Page changePageStatus(Page page, PageStatus status, Identity identity, Role by) {
		PageStatus currentStatus = page.getPageStatus();
		Page reloadedPage = pageDao.loadByKey(page.getKey());
		((PageImpl)reloadedPage).setPageStatus(status);
		if(status == PageStatus.published) {
			Date now = new Date();
			if(reloadedPage.getInitialPublicationDate() == null) {
				((PageImpl)reloadedPage).setInitialPublicationDate(now);
			}
			((PageImpl)reloadedPage).setLastPublicationDate(now);
			Section section = reloadedPage.getSection();
			if(section != null) {
				SectionStatus sectionStatus = section.getSectionStatus();
				if(currentStatus == PageStatus.closed) {
					if(sectionStatus == SectionStatus.closed) {
						((SectionImpl)section).setSectionStatus(SectionStatus.inProgress);
						binderDao.updateSection(section);
					}
				} else if(sectionStatus == null || sectionStatus == SectionStatus.notStarted || sectionStatus == SectionStatus.closed) {
					((SectionImpl)section).setSectionStatus(SectionStatus.inProgress);
					binderDao.updateSection(section);
				}
				List<Identity> owners = getOwners(page, section);
				for (Identity owner: owners) {
					EvaluationFormSurveyRef survey = evaluationFormManager.loadSurvey(getSurveyIdent(page.getBody()));
					EvaluationFormParticipationRef participation = evaluationFormManager.loadParticipationByExecutor(survey, owner);
					EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
					evaluationFormManager.finishSession(session);
				}
			}
		} else if(status == PageStatus.inRevision) {
			Section section = reloadedPage.getSection();
			if(section != null) {
				SectionStatus sectionStatus = section.getSectionStatus();
				if(sectionStatus == null || sectionStatus == SectionStatus.notStarted || sectionStatus == SectionStatus.closed) {
					if(sectionStatus == SectionStatus.closed) {
						((SectionImpl)section).setSectionStatus(SectionStatus.inProgress);
						binderDao.updateSection(section);
					}
				}
				List<Identity> owners = getOwners(page, section);
				for (Identity owner: owners) {
					EvaluationFormSurveyRef survey = evaluationFormManager.loadSurvey(getSurveyIdent(page.getBody()));
					EvaluationFormParticipationRef participation = evaluationFormManager.loadParticipationByExecutor(survey, owner);
					EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
					evaluationFormManager.reopenSession(session);
				}
			}
			pageUserInfosDao.updateStatus(reloadedPage, PageUserStatus.inProcess, PageUserStatus.done);
		} else if(status == PageStatus.closed) {
			//set user informations to done
			pageUserInfosDao.updateStatus(reloadedPage, PageUserStatus.done);
		}
		if(reloadedPage.getSection() != null && reloadedPage.getSection().getBinder() != null) {
			Binder binder = reloadedPage.getSection().getBinder();
			updateAssessmentEntryLastModification(binder, identity, by);
		}
		
		PageStatus sharedStatus = calculateSharedStatus(reloadedPage);
		if(sharedStatus != reloadedPage.getBody().getSyntheticStatusEnum()) {
			pageDao.updateSharedStatus(reloadedPage, sharedStatus);
		}
		return pageDao.updatePage(reloadedPage);
	}
	
	private PageStatus calculateSharedStatus(Page page) {
		List<String> status = pageDao.getSharedPageStatus(page);
		status.add(page.getPageStatus().name());
		
		int max = -1;
		for(String ps:status) {
			if(ps != null && !PageStatus.deleted.name().equals(ps)) {// trash is ignored
				int ord = PageStatus.valueOf(ps).ordinal();
				if(max < ord) {
					max = ord;
				}
			}
		}
		
		return max == -1 ? null : PageStatus.values()[max];
	}
	
	private List<Identity> getOwners(Page page, Section section) {
		Assignment assignment = assignmentDao.loadAssignment(page.getBody());
		if(assignment != null && assignment.getAssignmentType() == AssignmentType.form) {
			return getMembers(section.getBinder(), PortfolioRoles.owner.name());
		}
		return new ArrayList<>();
	}
	
	@Override
	public PageUserInformations getPageUserInfos(Page page, Identity identity, PageUserStatus defaultStatus) {
		PageUserInformations infos = pageUserInfosDao.getPageUserInfos(page, identity);
		if(infos == null) {
			PageStatus status = page.getPageStatus();
			PageUserStatus userStatus = defaultStatus;
			if(status == null || status == PageStatus.draft) {
				userStatus = PageUserStatus.incoming;
			} else if(status == PageStatus.closed || status == PageStatus.deleted) {
				userStatus = PageUserStatus.done;
			}
			infos = pageUserInfosDao.create(userStatus, page, identity);
		}
		return infos;
	}

	@Override
	public List<PageUserInformations> getPageUserInfos(BinderRef binder, IdentityRef identity) {
		return pageUserInfosDao.getPageUserInfos(binder, identity);
	}

	@Override
	public PageUserInformations updatePageUserInfos(PageUserInformations infos) {
		return pageUserInfosDao.update(infos);
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
				PageStatus sharedStatus = calculateSharedStatus(page);
				if(sharedStatus != page.getBody().getSyntheticStatusEnum()) {
					pageDao.updateSharedStatus(page, sharedStatus);
				}
				pageDao.updatePage(page);
				if(newPageStatus == PageStatus.closed) {
					//set user informations to done
					pageUserInfosDao.updateStatus(page, PageUserStatus.done);
				}
			}
		}
		
		((SectionImpl)reloadedSection).setSectionStatus(status);
		reloadedSection = binderDao.updateSection(reloadedSection);
		return reloadedSection;
	}
	
	private void updateAssessmentEntryLastModification(Binder binder, Identity doer, Role by) {
		if(binder.getEntry() == null) return;

		RepositoryEntry entry = binder.getEntry();
		List<Identity> assessedIdentities = getMembers(binder, PortfolioRoles.owner.name());

		//order status from the entry / section
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			for(Identity assessedIdentity:assessedIdentities) {
				UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
				courseAssessmentService.updateLastModifications(courseNode, userCourseEnv, doer, by);
			}
		} else {
			OLATResource resource = ((BinderImpl)binder.getTemplate()).getOlatResource();
			RepositoryEntry referenceEntry = repositoryService.loadByResourceKey(resource.getKey());
			for(Identity assessedIdentity:assessedIdentities) {
				AssessmentEntry assessmentEntry = assessmentService
						.getOrCreateAssessmentEntry(assessedIdentity, null, binder.getEntry(), binder.getSubIdent(), Boolean.TRUE, referenceEntry);
				if(by == Role.coach) {
					assessmentEntry.setLastCoachModified(new Date());
				} else if(by == Role.user) {
					assessmentEntry.setLastUserModified(new Date());
				}
				assessmentService.updateAssessmentEntry(assessmentEntry);
			}
		}
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
		int totalSectionPassed = 0;
		int totalSectionClosed = 0;
		BigDecimal totalScore = new BigDecimal("0.0");
		AssessmentEntryStatus binderStatus = null;

		for(AssessmentSection assessmentSection:assessmentSections) {
			if(assessmentSection.getScore() != null) {
				totalScore = totalScore.add(assessmentSection.getScore());
			}
			if(assessmentSection.getPassed() != null && assessmentSection.getPassed().booleanValue()) {
				allPassed &= true;
				totalSectionPassed++;
			}
			
			Section section = assessmentSection.getSection();
			if(section.getSectionStatus() == SectionStatus.closed) {
				totalSectionClosed++;
			}
		}

		Boolean totalPassed = null;
		if(totalSectionClosed == assessmentSections.size()) {
			totalPassed = Boolean.valueOf(allPassed);
		} else {
			if(assessmentSections.size() == totalSectionPassed) {
				totalPassed = Boolean.TRUE;
			}
			binderStatus = AssessmentEntryStatus.inProgress;
		}

		//order status from the entry / section
		RepositoryEntry entry = binder.getEntry();
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			ScoreEvaluation scoreEval= new ScoreEvaluation(totalScore.floatValue(), totalPassed, binderStatus, true, null, null, null, binder.getKey());
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
			courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, userCourseEnv, coachingIdentity, false,
					Role.coach);
		} else {
			OLATResource resource = ((BinderImpl)binder.getTemplate()).getOlatResource();
			RepositoryEntry referenceEntry = repositoryService.loadByResourceKey(resource.getKey());
			AssessmentEntry assessmentEntry = assessmentService
					.getOrCreateAssessmentEntry(assessedIdentity, null, binder.getEntry(), binder.getSubIdent(), Boolean.TRUE, referenceEntry);
			assessmentEntry.setScore(totalScore);
			assessmentEntry.setPassed(totalPassed);
			assessmentEntry.setAssessmentStatus(binderStatus);
			assessmentService.updateAssessmentEntry(assessmentEntry);
		}
	}

	@Override
	public AssessmentEntryStatus getAssessmentStatus(Identity assessedIdentity, BinderRef binderRef) {
		Binder binder = binderDao.loadByKey(binderRef.getKey());
		RepositoryEntry entry = binder.getEntry();
		
		AssessmentEntryStatus status = null;
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
			AssessmentEvaluation eval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
			status = eval.getAssessmentStatus();
		} else {
			OLATResource resource = ((BinderImpl)binder.getTemplate()).getOlatResource();
			RepositoryEntry referenceEntry = repositoryService.loadByResourceKey(resource.getKey());
			AssessmentEntry assessmentEntry = assessmentService
					.getOrCreateAssessmentEntry(assessedIdentity, null, binder.getEntry(), binder.getSubIdent(), Boolean.TRUE, referenceEntry);
			status = assessmentEntry.getAssessmentStatus();
		}
		return status;
	}

	@Override
	public void setAssessmentStatus(Identity assessedIdentity, BinderRef binderRef, AssessmentEntryStatus status, Identity coachingIdentity) {
		Boolean fullyAssessed = Boolean.FALSE;
		if(status == AssessmentEntryStatus.done) {
			fullyAssessed = Boolean.TRUE;
		}
		Binder binder = binderDao.loadByKey(binderRef.getKey());
		RepositoryEntry entry = binder.getEntry();
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			PortfolioCourseNode pfNode = (PortfolioCourseNode)courseNode;
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
			AssessmentEvaluation eval = courseAssessmentService.getAssessmentEvaluation(pfNode, userCourseEnv);
			
			ScoreEvaluation scoreEval = new ScoreEvaluation(eval.getScore(), eval.getPassed(), status, true,
					null, null, null, binder.getKey());
			courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, userCourseEnv, coachingIdentity, false,
					Role.coach);
		} else {
			OLATResource resource = ((BinderImpl)binder.getTemplate()).getOlatResource();
			RepositoryEntry referenceEntry = repositoryService.loadByResourceKey(resource.getKey());
			AssessmentEntry assessmentEntry = assessmentService
					.getOrCreateAssessmentEntry(assessedIdentity, null, binder.getEntry(), binder.getSubIdent(), Boolean.TRUE, referenceEntry);
			assessmentEntry.setFullyAssessed(fullyAssessed);
			assessmentEntry.setAssessmentStatus(status);
			assessmentService.updateAssessmentEntry(assessmentEntry);
		}
	}
	
	@Override
	public EvaluationFormSurvey loadOrCreateSurvey(PageBody body, RepositoryEntry formEntry) {
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdent(body);
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(surveyIdent);
		if (survey == null) {
			survey = evaluationFormManager.createSurvey(surveyIdent, formEntry);
		}
		return survey;
	}

	private OLATResourceable getOLATResourceableForEvaluationForm(PageBody body) {
		return OresHelper.createOLATResourceableInstance("portfolio-evaluation", body.getKey());
	}

	@Override
	public EvaluationFormSession loadOrCreateSession(EvaluationFormSurvey survey, Identity executor) {
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByExecutor(survey, executor);
		if (participation == null) {
			participation = evaluationFormManager.createParticipation(survey, executor);
		}
		
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	@Override
	public void deleteSurvey(PageBody body) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(getSurveyIdent(body));
		if (survey != null) {
			evaluationFormManager.deleteSurvey(survey);
		}
	}
	
	private EvaluationFormSurveyIdentifier getSurveyIdent(PageBody body) {
		OLATResourceable ores = getOLATResourceableForEvaluationForm(body);
		return EvaluationFormSurveyIdentifier.of(ores);
	}
	
	@Override
	public List<TaxonomyCompetence> getRelatedCompetences(Page page, boolean fetchTaxonomies) {
		return portfolioPageToTaxonomyCompetenceDAO.getCompetencesToPortfolioPage(page, fetchTaxonomies);
	}
	
	@Override
	public void linkCompetence(Page page, TaxonomyCompetence competence) {
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence);
		
	}
	
	@Override
	public void unlinkCompetence(Page page, TaxonomyCompetence competence) {
		portfolioPageToTaxonomyCompetenceDAO.deleteRelation(page, competence);
		
	}
	
	@Override
	public void linkCompetences(Page page, Identity identity, List<TextBoxItem> competences) {
		List<TaxonomyCompetence> relatedCompetences = getRelatedCompetences(page, true);
		List<TaxonomyLevel> relatedCompetenceLevels = relatedCompetences.stream().map(competence -> competence.getTaxonomyLevel()).collect(Collectors.toList());
		
		List<Long> newTaxonomyLevelKeys = competences.stream()
				.map(textBoxItem -> Long.valueOf(textBoxItem.getValue()))
				.collect(Collectors.toList());
		
		List<TaxonomyLevel> newTaxonomyLevels = taxonomyLevelDAO.loadLevelsByKeys(newTaxonomyLevelKeys);
		
		// Remove old competences
		for (TaxonomyCompetence competence : relatedCompetences) {
			if (!newTaxonomyLevels.contains(competence.getTaxonomyLevel())) {
				unlinkCompetence(page, competence);
			}
		}
		
		// Create new competences
		for (TaxonomyLevel newLevel : newTaxonomyLevels) {
			if (!relatedCompetenceLevels.contains(newLevel)) {
				TaxonomyCompetence competence = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, newLevel, identity, null);
				linkCompetence(page, competence);
			}
		}		
	}
	
	@Override
	public LinkedHashMap<TaxonomyLevel, Long> getCompetencesAndUsage(Section section) {
		return portfolioPageToTaxonomyCompetenceDAO.getCompetencesAndUsage(section);
	}
	
	@Override
	public LinkedHashMap<TaxonomyLevel, Long> getCompetencesAndUsage(List<Page> pages) {
		return portfolioPageToTaxonomyCompetenceDAO.getCompetencesAndUsage(pages);
	}
	
	@Override
	public LinkedHashMap<Category, Long> getCategoriesAndUsage(Section section) {
		return categoryDao.getCategoriesAndUsage(section);
	}
	
	@Override
	public LinkedHashMap<Category, Long> getCategoriesAndUsage(List<Page> pages) {
		return categoryDao.getCategoriesAndUsage(pages);
	}
}
