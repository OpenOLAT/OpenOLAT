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
package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CorruptedCourseException;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.assessment.portfolio.EfficiencyStatementMediaHandler;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificatesModule;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.Cols;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.wizard.CollectArtefactController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementListController extends FormBasicController implements BreadcrumbPanelAware, GenericEventListener, Activateable2 {
	
	private static final String CMD_SHOW = "cmd.show";
	private static final String CMD_LAUNCH_COURSE = "cmd.launch.course";
	private static final String CMD_DELETE = "cmd.delete";
	private static final String CMD_MEDIA = "cmd.MEDIA";
	private static final String CMD_INDIVIDUAL_COURSES = "cmd.individual.courses";
	private static final String CMD_ALL_EVIDENCE = "cmd.all.evidence";
	private static final String CMD_CURRICULUM = "cmd.filter.curriculum.";

	private FlexiTableElement tableEl;
	private BreadcrumbPanel stackPanel;
	private FormLink coachingToolButton;
	private FormLink uploadCertificateButton;
	private CertificateAndEfficiencyStatementListModel tableModel;
	private CertificateAndEfficiencyStatementRenderer treeRenderer;
	
	private FormLink freeFloatingCoursesLink;
	private FormLink allEvidenceLink;
	private String currentFilter;
	private Curriculum currentCurriculum;
	
	private boolean showCurriculumFilterButtons;
	private List<FormLink> curriculumFilterButtons;

	private CloseableModalController cmc;
	private CollectArtefactController collectorCtrl;
	private DialogBoxController confirmDeleteCtr;
	private UploadExternalCertificateController uploadCertificateController;
	
	private final boolean canModify;
	private final boolean linkToCoachingTool;
	private final boolean canLaunchCourse;
	private boolean canUploadExternalCertificate;
	private final Identity assessedIdentity;
	
	@Autowired
	private EfficiencyStatementManager esm;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private EfficiencyStatementMediaHandler mediaHandler;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CertificatesModule certificatesModule;
	@Autowired
	private BaseSecurityManager baseSecurityManager;
	@Autowired
	private CurriculumService curriculumService;
	
	
	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getUserSession().getIdentity(), false, true, true);
	}
	
	
	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean linkToCoachingTool, boolean canModify, boolean canLaunchCourse) {
		super(ureq, wControl, "cert_statement_list");
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.canModify = canModify;
		this.assessedIdentity = assessedIdentity;
		this.linkToCoachingTool = linkToCoachingTool;
		this.canLaunchCourse = canLaunchCourse;
		
		// Upload certificates
		Roles userRoles = ureq.getUserSession().getRoles();
		if (getIdentity().equals(assessedIdentity)) {
			canUploadExternalCertificate = certificatesModule.canUserUploadExternalCertificates();
		} else if (userRoles.isUserManager()) {
			canUploadExternalCertificate = certificatesModule.canUserManagerUploadExternalCertificates();
		} else if (userRoles.isLineManager()) {
			canUploadExternalCertificate = baseSecurityManager.getRoles(assessedIdentity).hasRole(userRoles.getOrganisations(), OrganisationRoles.user);			
		} else if (userRoles.isAdministrator() || userRoles.isSystemAdmin()) {
			canUploadExternalCertificate = true;
		}

		// Show heading
		flc.contextPut("showHeading", true);
		
		initForm(ureq);
		activateFilter(CMD_ALL_EVIDENCE);
		loadModel();
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean linkToCoachingTool, boolean canModify, boolean canLaunchCourse, boolean showHeading) {
		this(ureq, wControl, assessedIdentity, linkToCoachingTool, canModify, canLaunchCourse);

		// Set visibility of heading
		flc.contextPut("showHeading", showHeading);
	}
	
	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean withFieldSet) {
		this(ureq, wControl, assessedIdentity, false, false, true);
		
		// Show different header in user management
		flc.contextPut("withFieldSet", withFieldSet);
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(getIdentity().getKey().equals(ce.getOwnerKey())) {
				updateStatement(ce.getResourceKey(), ce.getCertificateKey());
			}
		}
	}
	
	private void updateStatement(Long resourceKey, Long certificateKey) {
		List<CertificateAndEfficiencyStatement> statements = tableModel.getObjects();
		for(int i=statements.size(); i-->0; ) {
			CertificateAndEfficiencyStatement statement = statements.get(i);
			if(resourceKey.equals(statement.getResourceKey())) {
				CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
				statement.setCertificate(certificate);
				break;
			}
		}
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(linkToCoachingTool) {
			flc.contextPut("linkToCoachingTool", true);
			coachingToolButton = uifactory.addFormLink("coaching.tool", formLayout, Link.BUTTON);
			coachingToolButton.setIconLeftCSS("o_icon o_icon_coach");
		}
		
		if (canUploadExternalCertificate) {
			flc.contextPut("uploadCertificate", true);
			uploadCertificateButton = uifactory.addFormLink("upload.certificate", formLayout, Link.BUTTON);
			uploadCertificateButton.setIconLeftCSS("o_icon o_icon_import");
		}
		
		// Check whether to show the curricula filters or not
		List<Curriculum> userCurricula = curriculumService.getMyCurriculums(assessedIdentity);
		if (!userCurricula.isEmpty()) {		
			curriculumFilterButtons = new ArrayList<>();
			
			// The different curricula
			for(Curriculum curriculum : userCurricula) {
				FormLink curriculumLink = uifactory.addFormLink(CMD_CURRICULUM + curriculum.getKey().toString(), curriculum.getDisplayName(), null, formLayout, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED);
				curriculumLink.setElementCssClass("o_curriculum_filter_button");
				curriculumLink.setUserObject(curriculum);
				
				curriculumFilterButtons.add(curriculumLink);
			}
			
			// Free floating courses
			freeFloatingCoursesLink = uifactory.addFormLink(CMD_INDIVIDUAL_COURSES, "filter.free.floating.courses", null, formLayout, Link.LINK_CUSTOM_CSS);
			freeFloatingCoursesLink.setElementCssClass("o_curriculum_filter_button");
			
			// All courses and certificates
			allEvidenceLink = uifactory.addFormLink(CMD_ALL_EVIDENCE, "filter.all.evidence", null, formLayout, Link.LINK_CUSTOM_CSS);
			allEvidenceLink.setElementCssClass("o_curriculum_filter_button");
			
			
			curriculumFilterButtons.add(freeFloatingCoursesLink);
			curriculumFilterButtons.add(allEvidenceLink);
			
			if (!curriculumFilterButtons.isEmpty()) {
				showCurriculumFilterButtons = true;
			}
			
			this.flc.contextPut("showCurriculumFilterButtons", showCurriculumFilterButtons);
			this.flc.contextPut("curriculumFilterButtons", curriculumFilterButtons.stream().map(FormLink::getName).collect(Collectors.toList()));
		}
		
		treeRenderer = new CertificateAndEfficiencyStatementRenderer(CMD_SHOW);
		treeRenderer.setFlatBySearchAndFilter(true);
		treeRenderer.setFlatBySort(true);
		
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName, treeRenderer));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.curriculumElIdent));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.completion, new LearningProgressCompletionCellRenderer()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passed, new PassedCellRenderer(getLocale())));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.score));		
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastModified));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUserUpdate));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.certificate, new DownloadCertificateCellRenderer(assessedIdentity, getLocale())));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recertification, new DateFlexiCellRenderer(getLocale())));
	
		if (canLaunchCourse) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.launchcourse",
					translate("table.header.launchcourse"), CMD_LAUNCH_COURSE));
		}
	
		if(canModify) {
			DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel(Cols.deleteEfficiencyStatement.i18nHeaderKey(), Cols.deleteEfficiencyStatement.ordinal(), CMD_DELETE,
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.action.delete"), CMD_DELETE), null));
			tableColumnModel.addFlexiColumnModel(deleteColumn);
		}
		
		//artefact
		if(assessedIdentity.equals(getIdentity())) {
			if(portfolioV2Module.isEnabled()) {
				DefaultFlexiColumnModel portfolioColumn = new DefaultFlexiColumnModel( Cols.artefact, CMD_MEDIA,
						new BooleanCellRenderer(new StaticFlexiCellRenderer(CMD_MEDIA, new AsArtefactCellRenderer()), null));
				tableColumnModel.addFlexiColumnModel(portfolioColumn);
			}
		}
		
		tableModel = new CertificateAndEfficiencyStatementListModel(tableColumnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "certificates", tableModel, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_certificates_table");
		tableEl.setEmptyTableSettings("table.statements.empty", null, "o_icon_certificate");
	}
	
	private void loadModel() {
		if (StringHelper.containsNonWhitespace(currentFilter)) {
			if (currentFilter == CMD_INDIVIDUAL_COURSES) {
				loadModelFlat(true);
			} else if (currentFilter == CMD_ALL_EVIDENCE) {
				loadModelFlat(false);
			} else if (currentFilter.startsWith(CMD_CURRICULUM) && currentCurriculum != null) {
				loadModel(currentCurriculum);
			} else {
				loadModelFlat(false);
			}
		} else {
			loadModelFlat(false);
		}
	}
	
	private void loadModel(Curriculum curriculum) {
		// Activate tree
		treeRenderer.setFlat(false);
		
		// Load curricula
		List<Curriculum> userCurricula = curriculumService.getMyCurriculums(assessedIdentity);
		
		// Check if curriculum is still there
		if (!userCurricula.contains(curriculum)) {
			showWarning("user.curriculum.not.there");
			return;
		}
		
		// Load efficiency statements
		List<UserEfficiencyStatementLight> efficiencyStatementsList = esm.findEfficiencyStatementsLight(assessedIdentity);
		
		// Only one statement per repository entry!
		Map<Long, UserEfficiencyStatementLight> olatResourceKeyToStatement = new HashMap<>();
		
		// Olat Resource key to Certificates
		Map<Long, CertificateLight> olatResourceKeyToCertificate = certificatesManager.getLastCertificates(assessedIdentity).stream()
				.filter(certificate -> !(certificate.getOlatResourceKey().equals(0l) || certificate.getOlatResourceKey().equals(-1l)))
				.collect(Collectors.toMap(CertificateLight::getOlatResourceKey, Function.identity()));
		
		List<Long> courseEntryKeys = efficiencyStatementsList.stream()
				.map(UserEfficiencyStatementLight::getCourseRepoKey)
				.filter(key -> key != null)
				.collect(Collectors.toList());
		
		Map<Long, AssessmentEntryScoring> courseEntryKeysToScoring = assessmentService
				.loadRootAssessmentEntriesByAssessedIdentity(assessedIdentity, courseEntryKeys).stream()
				.filter(ae -> ae.getCompletion() != null)
				.collect(Collectors.toMap(
						AssessmentEntryScoring::getRepositoryEntryKey,
						Function.identity()
					));
		
		efficiencyStatementsList.forEach(statement -> {
			if (!olatResourceKeyToStatement.containsKey(statement.getArchivedResourceKey())) {
				olatResourceKeyToStatement.put(statement.getArchivedResourceKey(), statement);
			} 			
		});
		
		List<CertificateAndEfficiencyStatement> tableRows = new ArrayList<>();
		
		
		Roles userRoles = baseSecurityManager.getRoles(assessedIdentity);
		List<CurriculumElementRepositoryEntryViews> curriculumElements = curriculumService.getCurriculumElements(assessedIdentity, userRoles, Collections.singletonList(curriculum));
		Map<CurriculumElement, CertificateAndEfficiencyStatement> curriculumElToRows = new HashMap<>();
		Map<CurriculumElement, Map<TaxonomyLevel, CertificateAndEfficiencyStatement>> taxonomyLevelToCurriculumElements = new HashMap<>();

		// Create row for every curriculum element
		for(CurriculumElementRepositoryEntryViews element : curriculumElements) {
			List<TaxonomyLevel> taxonomyLevels = curriculumService.getTaxonomy(element.getCurriculumElement());
			CurriculumElement parent = element.getCurriculumElement().getParent();
			
			// Create row directly, if no taxonomy levels are assigned
			if (taxonomyLevels.isEmpty()) {
				CertificateAndEfficiencyStatement curriculumElementRow = forgeRow(element.getCurriculumElement());
				curriculumElementRow.setParent(curriculumElToRows.get(parent));
				curriculumElementRow.setIsCurriculumElement(true);
				
				tableRows.add(curriculumElementRow);
				curriculumElToRows.put(element.getCurriculumElement(), curriculumElementRow);
				
				forgeStatementRows(element, curriculumElementRow, olatResourceKeyToStatement, olatResourceKeyToCertificate, tableRows);
			} else {	
				// Create first taxonomy level rows, if existent
				Map<TaxonomyLevel, CertificateAndEfficiencyStatement> taxonomyLevelToRows = taxonomyLevelToCurriculumElements.get(parent);
				
				if (taxonomyLevelToRows == null) {
					taxonomyLevelToRows = new HashMap<>();
					taxonomyLevelToCurriculumElements.put(parent, taxonomyLevelToRows);
				}
				
				for(TaxonomyLevel taxonomyLevel : taxonomyLevels) {
					CertificateAndEfficiencyStatement taxonomyRow = taxonomyLevelToRows.get(taxonomyLevel);
					
					if (taxonomyRow == null) {
						taxonomyRow = new CertificateAndEfficiencyStatement();
						taxonomyRow.setDisplayName(taxonomyLevel.getDisplayName());
						taxonomyRow.setTaxonomy(true);
						taxonomyRow.setParentElement(parent);
						taxonomyRow.setParent(curriculumElToRows.get(parent));
					}
					
					tableRows.add(taxonomyRow);
					taxonomyLevelToRows.put(taxonomyLevel, taxonomyRow);					
					
					// Create actual curriculum element row
					CertificateAndEfficiencyStatement curriculumElementRow = forgeRow(element.getCurriculumElement());
					curriculumElementRow.setParent(taxonomyRow);
					curriculumElementRow.setIsCurriculumElement(true);
					
					tableRows.add(curriculumElementRow);
					curriculumElToRows.put(element.getCurriculumElement(), curriculumElementRow);
					
					forgeStatementRows(element, curriculumElementRow, olatResourceKeyToStatement, olatResourceKeyToCertificate, tableRows);				
				}
			}
		}		
		
		// Parent line
		for(CertificateAndEfficiencyStatement row : tableRows) {
			if(row.getParent() == null) {
				if (row.getParentElement() != null) {
					row.setParent(curriculumElToRows.get(row.getParentElement()));
				} else if (row.getCurriculumElement() != null && row.getCurriculumElement().getParent() != null) {
					row.setParent(curriculumElToRows.get(row.getCurriculumElement().getParent()));
				}
			}
		}
		
		// Remove all rows, which are not statements or do not contain any children which are statements
		tableRows.removeIf(row -> !(row.hasStatementChildren() || row.isStatement()));
		
		// Add scores to elements and parents
		for (CertificateAndEfficiencyStatement row : tableRows) {
			if (row.getCourseRepoKey() == null) {
				continue;
			}
			
			AssessmentEntryScoring scoring = courseEntryKeysToScoring.get(row.getCourseRepoKey());
			if (scoring != null) {
				row.addToScore(scoring.getMaxScore() != null ? scoring.getMaxScore().floatValue() : null, 
						scoring.getScore() != null ? scoring.getScore().floatValue() : null, scoring.getKey());
				row.setCompletion(scoring.getCompletion());
			}
		}
		
		tableRows.sort(new CertificateAndEfficiencyStatementComparator(getLocale()));
		
		tableModel.setObjects(tableRows);
		tableModel.openAll();
		tableEl.setSortEnabled(true);
		tableEl.reset();
	}
	
	private void forgeStatementRows(CurriculumElementRepositoryEntryViews element, 
			CertificateAndEfficiencyStatement parentRow, 
			Map<Long, UserEfficiencyStatementLight> olatResourceKeyToStatement, 
			Map<Long, CertificateLight> olatResourceKeyToCertificate,
			List<CertificateAndEfficiencyStatement> tableRows) {
		Set<UserEfficiencyStatementLight> efficiencyStatements = new HashSet<>();
		
		for (RepositoryEntryMyView repoEl : element.getEntries()) {
			UserEfficiencyStatementLight efficiencyStatement = olatResourceKeyToStatement.get(repoEl.getOlatResource().getKey());
			
			if (efficiencyStatement != null) {
				efficiencyStatements.add(efficiencyStatement);
			}
		}
		
		
		// If more than one course entry, create entry rows
		// Otherwise put information on curriculum element row
		if (efficiencyStatements.size() > 1) {
			for (UserEfficiencyStatementLight efficiencyStatement : efficiencyStatements) {
				CertificateAndEfficiencyStatement statementRow = new CertificateAndEfficiencyStatement();
				statementRow.setParent(parentRow);				
				statementRow.setDisplayName(efficiencyStatement.getTitle());
				statementRow.setPassed(efficiencyStatement.getPassed());
				statementRow.setEfficiencyStatementKey(efficiencyStatement.getKey());
				statementRow.setResourceKey(efficiencyStatement.getArchivedResourceKey());
				statementRow.setLastModified(efficiencyStatement.getLastModified());
				statementRow.setLastUserModified(efficiencyStatement.getLastUserModified());
				statementRow.setCourseRepoKey(efficiencyStatement.getCourseRepoKey());
				statementRow.setCertificate(olatResourceKeyToCertificate.get(efficiencyStatement.getArchivedResourceKey()));
				
				statementRow.setStatement(true);
				
				tableRows.add(statementRow);
			}
		} else if (efficiencyStatements.size() == 1) {
			for (UserEfficiencyStatementLight efficiencyStatement : efficiencyStatements) {
				parentRow.setPassed(efficiencyStatement.getPassed());
				parentRow.setEfficiencyStatementKey(efficiencyStatement.getKey());
				parentRow.setResourceKey(efficiencyStatement.getArchivedResourceKey());
				parentRow.setLastModified(efficiencyStatement.getLastModified());
				parentRow.setLastUserModified(efficiencyStatement.getLastUserModified());
				parentRow.setCourseRepoKey(efficiencyStatement.getCourseRepoKey());
				parentRow.setCertificate(olatResourceKeyToCertificate.get(efficiencyStatement.getArchivedResourceKey()));
				
				parentRow.setStatement(true);
			}
		}
	}
	
	private CertificateAndEfficiencyStatement forgeRow(CurriculumElement curriculumElement) {
		CertificateAndEfficiencyStatement statement = new CertificateAndEfficiencyStatement();
		statement.setDisplayName(curriculumElement.getDisplayName());
		statement.setCurriculumElement(curriculumElement);
		statement.setCurriculum(curriculumElement.getCurriculum());
		
		return statement;
	}
	
	private void loadModelFlat(boolean onlyFreeFloatingCourses) {
		// Deactivate tree
		treeRenderer.setFlat(true);
		
		Map<Long, CertificateAndEfficiencyStatement> resourceKeyToStatments = new HashMap<>();
		List<CertificateAndEfficiencyStatement> statments = new ArrayList<>();
		List<UserEfficiencyStatementLight> efficiencyStatementsList = esm.findEfficiencyStatementsLight(assessedIdentity);
		
		List<Long> courseEntryKeys = efficiencyStatementsList.stream()
				.map(UserEfficiencyStatementLight::getCourseRepoKey)
				.filter(key -> key != null)
				.collect(Collectors.toList());
		Map<Long, Double> courseEntryKeysToCompletion = assessmentService
				.loadRootAssessmentEntriesByAssessedIdentity(assessedIdentity, courseEntryKeys).stream()
				.filter(ae -> ae.getCompletion() != null)
				.collect(Collectors.toMap(
						AssessmentEntryScoring::getRepositoryEntryKey,
						AssessmentEntryScoring::getCompletion
					));
		
		for(UserEfficiencyStatementLight efficiencyStatement:efficiencyStatementsList) {
			CertificateAndEfficiencyStatement wrapper = new CertificateAndEfficiencyStatement();
			wrapper.setDisplayName(efficiencyStatement.getTitle());
			wrapper.setPassed(efficiencyStatement.getPassed());
			wrapper.setScore(efficiencyStatement.getScore());
			wrapper.setEfficiencyStatementKey(efficiencyStatement.getKey());
			wrapper.setResourceKey(efficiencyStatement.getArchivedResourceKey());
			wrapper.setLastModified(efficiencyStatement.getLastModified());
			wrapper.setLastUserModified(efficiencyStatement.getLastUserModified());
			Double completion = courseEntryKeysToCompletion.get(efficiencyStatement.getCourseRepoKey());
			wrapper.setCompletion(completion);
			wrapper.setStatement(true);
			statments.add(wrapper);
			
			resourceKeyToStatments.put(efficiencyStatement.getArchivedResourceKey(), wrapper);
		}
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(assessedIdentity);
		for(CertificateLight certificate:certificates) {
			Long resourceKey = certificate.getKey();
			CertificateAndEfficiencyStatement wrapper = resourceKeyToStatments.get(resourceKey);
			if(wrapper == null) {
				wrapper = new CertificateAndEfficiencyStatement();
				wrapper.setDisplayName(certificate.getCourseTitle());
				resourceKeyToStatments.put(resourceKey, wrapper);
				statments.add(wrapper);
			} else {
				if(!StringHelper.containsNonWhitespace(wrapper.getDisplayName())) {
					wrapper.setDisplayName(certificate.getCourseTitle());
				}
				wrapper.setResourceKey(resourceKey);
			}
			if(resourceKey != null && wrapper.getResourceKey() == null) {
				wrapper.setResourceKey(resourceKey);
			}
			wrapper.setCertificate(certificate);
			wrapper.setStatement(true);
		}
		
		for(CertificateAndEfficiencyStatement statment:statments) {
			if(!StringHelper.containsNonWhitespace(statment.getDisplayName()) && statment.getResourceKey() != null) {
				String displayName = repositoryManager.lookupDisplayNameByResourceKey(statment.getResourceKey());
				statment.setDisplayName(displayName);
			}
		}
		
		if (onlyFreeFloatingCourses) {
			// Remove all courses attached to a curriculum
			List<Curriculum> userCurricula = curriculumService.getMyCurriculums(assessedIdentity);
			List<CurriculumElementMembership> curriculumMemberships = new ArrayList<>();
			Set<RepositoryEntry> coursesWithCurriculum = new HashSet<>();
			
			for (Curriculum curriculum : userCurricula) {
				curriculumMemberships.addAll(curriculumService.getCurriculumElementMemberships(curriculum, assessedIdentity));
			}
			
			for (CurriculumElementMembership membership : curriculumMemberships) {
				coursesWithCurriculum.addAll(curriculumService.getRepositoryEntries(new CurriculumElementRefImpl(membership.getCurriculumElementKey())));
			}
			
			List<Long> coursesWithCurriculumKeys = coursesWithCurriculum.stream().map(RepositoryEntry::getOlatResource).map(OLATResource::getKey).collect(Collectors.toList());
			
			statments.removeIf(statement -> coursesWithCurriculumKeys.contains(statement.getResourceKey()));
		}
		
		tableModel.setObjects(statments);
		tableModel.openAll();
		tableEl.setSortEnabled(true);
		tableEl.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				CertificateAndEfficiencyStatement statement = tableModel.getObject(te.getIndex());
				if(CMD_LAUNCH_COURSE.equals(cmd)) {
					doLaunchCourse(ureq, statement.getResourceKey());
				} else if(CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, statement);
				} else if(CMD_SHOW.equals(cmd)) {
					doShowStatement(ureq, statement);
				} else if(CMD_MEDIA.equals(cmd)) {
					doCollectMedia(ureq, statement.getDisplayName(), statement.getEfficiencyStatementKey());
				}
			}
		} else if(coachingToolButton == source) {
			doLaunchCoachingTool(ureq);
		} else if(uploadCertificateButton == source) {
			showUploadCertificateController(ureq);
		} else if(source instanceof FormLink) {
			FormLink sourceLink = (FormLink) source;
			
			if (sourceLink.getCmd().equals(CMD_INDIVIDUAL_COURSES)) {
				activateFilter(CMD_INDIVIDUAL_COURSES);
			} else if (sourceLink.getCmd().equals(CMD_ALL_EVIDENCE)) {
				activateFilter(CMD_ALL_EVIDENCE);
			} else if (sourceLink.getCmd().startsWith(CMD_CURRICULUM)) {
				Curriculum curriculum = (Curriculum) source.getUserObject();
				activateFilter(CMD_CURRICULUM + curriculum.getKey().toString());
				currentCurriculum = curriculum;
			}
			
			loadModel();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (source == confirmDeleteCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				CertificateAndEfficiencyStatement statement = (CertificateAndEfficiencyStatement)confirmDeleteCtr.getUserObject();
				doDelete(statement);
			}
		} else if(collectorCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(uploadCertificateController == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
				tableEl.reset();
			}
			
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(uploadCertificateController);
		removeAsListenerAndDispose(collectorCtrl);
		removeAsListenerAndDispose(cmc);
		uploadCertificateController = null;
		collectorCtrl = null;
		cmc = null;
	}
	
	private FormLink getFilterButton(String name) {
		if(curriculumFilterButtons == null) return null;
		return curriculumFilterButtons.stream()
				.filter(button -> button.getName().equals(name))
				.findFirst().orElse(null);
	}
	
	private void activateFilter(String name) {
		if(curriculumFilterButtons != null) {
			curriculumFilterButtons.stream()
				.forEach(button -> button.setElementCssClass("o_curriculum_filter_button"));
		}
		
		FormLink filterButton = getFilterButton(name);
		if(filterButton != null) {
			filterButton.setElementCssClass("o_curriculum_filter_button active");
		}
		
		currentFilter = name;
	}
	
	private void doShowStatement(UserRequest ureq, CertificateAndEfficiencyStatement statement) {
		RepositoryEntry entry = repositoryService.loadByResourceKey(statement.getResourceKey());
		EfficiencyStatement efficiencyStatment = esm.getUserEfficiencyStatementByKey(statement.getEfficiencyStatementKey());
		CertificateAndEfficiencyStatementController efficiencyCtrl = new CertificateAndEfficiencyStatementController(getWindowControl(), ureq,
				assessedIdentity, null, statement.getResourceKey(), entry, efficiencyStatment, false);
		listenTo(efficiencyCtrl);
		stackPanel.pushController(statement.getDisplayName(), efficiencyCtrl);
	}

	private void doConfirmDelete(UserRequest ureq, CertificateAndEfficiencyStatement statement) {
		RepositoryEntry re = repositoryService.loadByResourceKey(statement.getResourceKey());
		if(re == null) {
			String text = translate("efficiencyStatements.delete.confirm", statement.getDisplayName());
			confirmDeleteCtr = activateYesNoDialog(ureq, null, text, confirmDeleteCtr);
			confirmDeleteCtr.setUserObject(statement);
		} else {
			showWarning("efficiencyStatements.cannot.delete");
		}
	}
	
	private void doDelete(CertificateAndEfficiencyStatement statement) {
		if (statement == null) {
			return;
		}
		
		UserEfficiencyStatementLight efficiencyStatement = esm.getUserEfficiencyStatementLightByKey(statement.getEfficiencyStatementKey());
		if(efficiencyStatement != null) {
			esm.deleteEfficiencyStatement(efficiencyStatement);
		} else {
			// Delete standalone certificate
			Certificate certificate = certificatesManager.getCertificateByUuid(statement.getCertificate().getUuid());
			
			if (certificate != null) {
				certificatesManager.deleteStandalonCertificate(certificate);
			}
		}
		
		loadModel();
		tableEl.reset();
		showInfo("info.efficiencyStatement.deleted");
	}
	
	private void doLaunchCoachingTool(UserRequest ureq) {
		String businessPath = "[CoachSite:0][Search:0][Identity:" + assessedIdentity.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doLaunchCourse(UserRequest ureq, Long resourceKey) {
		RepositoryEntry entry = repositoryService.loadByResourceKey(resourceKey);
		if(entry == null) {
			showWarning("efficiencyStatements.course.noexists");
		} else if (!repositoryManager.isAllowedToLaunch(getIdentity(), ureq.getUserSession().getRoles(), entry)) {
			showWarning("efficiencyStatements.course.noaccess");
		} else {
			try {
				String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				logError("Course corrupted: " + entry.getKey() + " (" + entry.getResourceableId() + ")", e);
				showError("cif.error.corrupted");
			}
		}
	}

	private void doCollectMedia(UserRequest ureq, String title, Long efficiencyStatementKey) {
		if(guardModalController(collectorCtrl)) return;
		
		EfficiencyStatement fullStatement = esm.getUserEfficiencyStatementByKey(efficiencyStatementKey);
		collectorCtrl = new CollectArtefactController(ureq, getWindowControl(), fullStatement, mediaHandler, null);
		listenTo(collectorCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), null, collectorCtrl.getInitialComponent(), true, title, true);
		cmc.addControllerListener(this);
		cmc.activate();
	}
	
	private void showUploadCertificateController(UserRequest ureq) {
		if(guardModalController(uploadCertificateController)) return;
		
		uploadCertificateController = new UploadExternalCertificateController(ureq, getWindowControl(), assessedIdentity);
		listenTo(uploadCertificateController);
		
		cmc = new CloseableModalController(getWindowControl(), null, uploadCertificateController.getInitialComponent(), true, translate("upload.certificate"), true);
		cmc.addControllerListener(this);
		cmc.activate();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	public class AsArtefactCellRenderer implements FlexiCellRenderer {
		
		@Override
		public void render(Renderer renderer, StringOutput sb, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			sb.append("<i class='o_icon o_icon-lg o_icon_eportfolio_add'> </i> <span title=\"")
				.append(translate("table.add.as.artefact"))
				.append("\"> </span>");
		}
	}
}
