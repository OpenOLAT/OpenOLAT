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
package org.olat.modules.grading.ui;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewModel;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssessedIdentityVisibility;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.GradingTimeRecordRef;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.modules.grading.ui.GradingAssignmentsTableModel.GAssignmentsCol;
import org.olat.modules.grading.ui.component.GraderMailTemplate;
import org.olat.modules.grading.ui.component.GradingDeadlineStatusCellRenderer;
import org.olat.modules.grading.ui.confirmation.ConfirmReopenAssignmentController;
import org.olat.modules.grading.ui.confirmation.ConfirmUnassignGraderController;
import org.olat.modules.grading.ui.confirmation.ExtendDeadlineController;
import org.olat.modules.grading.ui.event.OpenAssignmentsEvent;
import org.olat.modules.grading.ui.event.OpenEntryAssignmentsEvent;
import org.olat.modules.grading.ui.wizard.AssignGrader1ChooseMemberStep;
import org.olat.modules.grading.ui.wizard.AssignGraderContext;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAssignmentsListController extends FormBasicController implements BreadcrumbPanelAware, Activateable2 {
	
	public static final String USER_PROPS_ID = GradersListController.class.getCanonicalName();
	public static final String ASSESSED_PROPS_ID = GradingAssignmentsListController.class.getCanonicalName() + "_assessed";

	public static final int USER_PROPS_OFFSET = 500;
	public static final int ASSESSED_PROPS_OFFSET = 1500;

	private FormLink reportButton;
	private FormLink assignGraderButton;
	private FormLink changeGraderButton;
	private FormLink sendMailButton;
	private FormLink extendDeadlineButton;
	private FormLink unassignButton;
	
	private BreadcrumbPanel stackPanel;
	private FlexiTableElement tableEl;
	private GradingAssignmentsTableModel tableModel;
	
	private int counter = 0;
	private Identity grader;
	private final boolean isManager;
	private RepositoryEntry testEntry;
	private GradingAssessedIdentityVisibility testEntryAssessedIdentityVisibility;
	
	private final boolean myView;
	private List<UserPropertyHandler> userPropertyHandlers;
	private List<UserPropertyHandler> assessedUserPropertyHandlers;
	private final GradingSecurityCallback secCallback;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ReportCalloutController reportCtrl;
	private ContactFormController contactGraderCtrl;
	private StepsMainRunController changeGraderWizard;
	private StepsMainRunController assignGraderWizard;
	private ExtendDeadlineController extendDeadlineCtrl;
	private final AssignmentsSearchController searchCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableCalloutWindowController reportCalloutCtrl;
	private ConfirmUnassignGraderController confirmUnassignGraderCtrl;
	private ConfirmReopenAssignmentController confirmReopenAssignmentCtrl;
	private CorrectionIdentityAssessmentItemListController correctionCtrl; 

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GradingAssignmentsListController(UserRequest ureq, WindowControl wControl, GradingSecurityCallback secCallback) {
		this(ureq, wControl, null, null, secCallback);
	}
	
	public GradingAssignmentsListController(UserRequest ureq, WindowControl wControl, Identity identity,
			GradingSecurityCallback secCallback) {
		this(ureq, wControl, null, identity, secCallback);
	}
	
	public GradingAssignmentsListController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			GradingSecurityCallback secCallback) {
		this(ureq, wControl, testEntry, null, secCallback);
	}
	
	private GradingAssignmentsListController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry, Identity grader,
			GradingSecurityCallback secCallback) {
		super(ureq, wControl, "assignments_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.grader = grader;
		this.testEntry = testEntry;
		isManager = (testEntry == null && grader == null);
		myView = grader != null && grader.getKey().equals(getIdentity().getKey())
				&& secCallback.canGrade() && !secCallback.canManage();
		if(testEntry != null) {
			testEntryAssessedIdentityVisibility = gradingService.getIdentityVisibility(testEntry);
			if(testEntryAssessedIdentityVisibility == null) {
				testEntryAssessedIdentityVisibility = GradingAssessedIdentityVisibility.anonymous;
			}
		}
		this.secCallback = secCallback;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		assessedUserPropertyHandlers = userManager.getUserPropertyHandlersFor(ASSESSED_PROPS_ID, isAdministrativeUser);
		
		searchCtrl = new AssignmentsSearchController(ureq, getWindowControl(), testEntry, grader, myView, mainForm);
		listenTo(searchCtrl);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("search", searchCtrl.getInitialFormItem());
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("myTitle", Boolean.valueOf(myView));
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(!myView) {
			int colPos = USER_PROPS_OFFSET;
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
	
				String propName = userPropertyHandler.getName();
				boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
	
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
				columnsModel.addFlexiColumnModel(col);
				colPos++;
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GAssignmentsCol.deadline,
				new GradingDeadlineStatusCellRenderer(getTranslator())));
		
		// assessed user props
		if(testEntry == null || testEntryAssessedIdentityVisibility == GradingAssessedIdentityVisibility.nameVisible) {
			int aColPos = ASSESSED_PROPS_OFFSET;
			for (UserPropertyHandler userPropertyHandler : assessedUserPropertyHandlers) {
				if (userPropertyHandler == null) continue;
	
				String propName = userPropertyHandler.getName();
				boolean visible = userManager.isMandatoryUserProperty(ASSESSED_PROPS_ID , userPropertyHandler);
	
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), aColPos, true, propName);
				columnsModel.addFlexiColumnModel(col);
				aColPos++;
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GAssignmentsCol.entry, "open_course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.entryExternalRef, "open_course"));
		if(taxonomyModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GAssignmentsCol.taxonomy));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GAssignmentsCol.courseElement, "open_course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.assessmentDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.correctionMetadataMinutes));
		if(secCallback.canViewRecordedRealMinutes()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.correctionMinutes));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.assignmentDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.doneDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GAssignmentsCol.passed, new PassedCellRenderer()));

		if(secCallback.canGrade()) {
			StaticFlexiCellRenderer gradeRenderer = new StaticFlexiCellRenderer(translate("grade"), "grade", null, "o_icon o_icon_assessment_mode");
			DefaultFlexiColumnModel gradeCol = new DefaultFlexiColumnModel(GAssignmentsCol.grade, new BooleanCellRenderer(gradeRenderer, null));
			gradeCol.setAlwaysVisible(true);
			gradeCol.setExportable(false);
			columnsModel.addFlexiColumnModel(gradeCol);
		}
		if(secCallback.canManage()) {
			DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(GAssignmentsCol.tools);
			toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsCol.setHeaderLabel(translate("table.header.tools"));
			toolsCol.setAlwaysVisible(true);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		tableModel = new GradingAssignmentsTableModel(columnsModel,
				userPropertyHandlers, assessedUserPropertyHandlers, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "assignments", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("table.assignments.empty", null, FlexiTableElement.TABLE_EMPTY_ICON);		
		tableEl.setElementCssClass("o_sel_grading_assignments_list");
		tableEl.setExportEnabled(true);
		String id = "grading-assignments-list-v2-" + (testEntry == null ? "coaching" : testEntry.getKey());
		tableEl.setAndLoadPersistedPreferences(ureq, id);
		
		if(secCallback.canReport()) {
			reportButton = uifactory.addFormLink("report", "assignments.report", null, formLayout, Link.BUTTON);
			reportButton.setIconLeftCSS("o_icon o_icon_report");
		}
		if(secCallback.canManage()) {
			assignGraderButton = uifactory.addFormLink("batchAssignGrader", "tool.assign.grader", null, formLayout, Link.BUTTON);
			assignGraderButton.setIconLeftCSS("o_icon o_icon_edit");
			tableEl.addBatchButton(assignGraderButton);
			changeGraderButton = uifactory.addFormLink("batchChangeGrader", "tool.change.grader", null, formLayout, Link.BUTTON);
			changeGraderButton.setIconLeftCSS("o_icon o_icon_edit");
			tableEl.addBatchButton(changeGraderButton);
			sendMailButton = uifactory.addFormLink("batchSendMail", "tool.send.mail", null, formLayout, Link.BUTTON);
			sendMailButton.setIconLeftCSS("o_icon o_icon_mail");
			tableEl.addBatchButton(sendMailButton);
			extendDeadlineButton = uifactory.addFormLink("batchExtendDeadline", "tool.extend.deadline", null, formLayout, Link.BUTTON);
			extendDeadlineButton.setIconLeftCSS("o_icon o_icon_extend");
			tableEl.addBatchButton(extendDeadlineButton);
			unassignButton = uifactory.addFormLink("batchUnassignGrader", "tool.unassign", null, formLayout, Link.BUTTON);
			unassignButton.setIconLeftCSS("o_icon o_icon_deactivate");
			tableEl.addBatchButton(unassignButton);
			
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		GradingAssignmentSearchParameters searchParams = getSearchParameters();
		List<GradingAssignmentWithInfos> assignments = gradingService.getGradingAssignmentsWithInfos(searchParams);
		List<GradingAssignmentRow> rows = assignments.stream()
				.map(this::forgeRow).
				collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private GradingAssignmentSearchParameters getSearchParameters() {
		GradingAssignmentSearchParameters searchParams = new GradingAssignmentSearchParameters();
		searchParams.setReferenceEntry(testEntry);
		searchParams.setGrader(grader);
		if(searchCtrl != null) {
			searchParams.setAssignmentStatus(searchCtrl.getSearchStatus());
			searchParams.setTaxonomyLevels(searchCtrl.getTaxonomyLevels());
			searchParams.setGradingFromDate(searchCtrl.getGradingFrom());
			searchParams.setGradingToDate(searchCtrl.getGradingTo());
			searchParams.setScoreFrom(searchCtrl.getScoreFrom());
			searchParams.setScoreTo(searchCtrl.getScoreTo());
			searchParams.setPassed(searchCtrl.getPassed());
			searchParams.setEntry(searchCtrl.getEntry());
			
			if(searchParams.getGrader() == null) {
				searchParams.setGrader(searchCtrl.getGrader());
			}
			if(searchParams.getReferenceEntry() == null) {
				searchParams.setReferenceEntry(searchCtrl.getReferenceEntry());
			}
		}
		
		if(isManager) {
			searchParams.setManager(getIdentity());
		}
		
		return searchParams;
	}
	
	private GradingAssignmentRow forgeRow(GradingAssignmentWithInfos assignment) {
		boolean canGrade = secCallback.canGrade() && secCallback.canGrade(assignment.getAssignment());
		GradingAssignmentRow row = new GradingAssignmentRow(assignment, canGrade, isManager);
		
		// tools
		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		row.setToolsLink(toolsLink);
		return row;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof OpenAssignmentsEvent) {
			activate((OpenAssignmentsEvent)state);
		}
	}
	
	protected void activate(OpenAssignmentsEvent openAssignments) {
		if(searchCtrl != null) {
			searchCtrl.setSearchStatus(openAssignments.getSearchStatus());
			searchCtrl.setGrader(openAssignments.getGrader());
			loadModel();
		}
	}
	
	protected void activate(OpenEntryAssignmentsEvent openAssignments) {
		if(searchCtrl != null) {
			searchCtrl.setSearchStatus(openAssignments.getSearchStatus());
			searchCtrl.setReferenceEntry(openAssignments.getReferenceEntry());
			loadModel();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCalloutCtrl == source || reportCalloutCtrl == source || cmc == source) {
			cleanUp();
		} else if(contactGraderCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if(reportCtrl == source) {
			reportCalloutCtrl.deactivate();
			cleanUp();
		} else if(searchCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
			}
		} else if(confirmUnassignGraderCtrl == source
				|| confirmReopenAssignmentCtrl == source
				|| extendDeadlineCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(assignGraderWizard == source || changeGraderWizard == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
			}
		} else if(correctionCtrl == source) {
			if(event instanceof CompleteAssessmentTestSessionEvent) {
				stackPanel.popController(correctionCtrl);
				CompleteAssessmentTestSessionEvent catse = (CompleteAssessmentTestSessionEvent)event;
				doUpdateCourseNode(catse.getTestSessions().get(0), catse.getAssessmentTest(), catse.getStatus(),
						correctionCtrl.getGradingAssignment());
				loadModel();
				cleanUp();
			} else if(event == Event.CANCELLED_EVENT || event == Event.BACK_EVENT) {
				stackPanel.popController(correctionCtrl);
				cleanUp();
			}
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmReopenAssignmentCtrl);
		removeAsListenerAndDispose(confirmUnassignGraderCtrl);
		removeAsListenerAndDispose(extendDeadlineCtrl);
		removeAsListenerAndDispose(contactGraderCtrl);
		removeAsListenerAndDispose(reportCalloutCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(reportCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmReopenAssignmentCtrl = null;
		confirmUnassignGraderCtrl = null;
		extendDeadlineCtrl = null;
		contactGraderCtrl = null;
		reportCalloutCtrl = null;
		toolsCalloutCtrl = null;
		correctionCtrl = null;
		reportCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assignGraderButton == source) {
			doBatchAssignGrader(ureq);
		} else if(changeGraderButton == source) {
			doBatchChangeGrader(ureq);
		} else if(sendMailButton == source) {
			doBatchContact(ureq);
		} else if(extendDeadlineButton == source) {
			doBatchExtendDeadline(ureq);
		} else if(unassignButton == source) {
			doBatchUnassignGrader(ureq);
		} else if(reportButton == source) {
			doOpenReportConfiguration(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, (GradingAssignmentRow)link.getUserObject(), link);
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("grade".equals(se.getCommand())) {
					doGrade(ureq, tableModel.getObject(se.getIndex()), false);
				} else if("open_course".equals(se.getCommand())) {
					doOpenCourse(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doGrade(UserRequest ureq, GradingAssignmentRow row, boolean viewOnly) {
		GradingAssignment assignment = row.getAssignment();
		boolean anonymous = !row.isAssessedIdentityVisible();
		AssessmentEntry assessment = assignment.getAssessmentEntry();
		assessment = gradingService.loadFullAssessmentEntry(assessment);
		
		RepositoryEntry entry = assessment.getRepositoryEntry();
		if(StringHelper.containsNonWhitespace(assessment.getSubIdent())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode node = course.getRunStructure().getNode(assessment.getSubIdent());
			if(node instanceof IQTESTCourseNode) {
				doQTICorrection(ureq, assessment, assignment, (IQTESTCourseNode)node, viewOnly, anonymous);
			}
		}
	}
	
	private void doOpenCourse(UserRequest ureq, GradingAssignmentRow row) {
		String businessPath = "[RepositoryEntry:" + row.getEntry().getKey() + "]";
		if(StringHelper.containsNonWhitespace(row.getSubIdent())) {
			businessPath += "[CourseNode:" + row.getSubIdent() + "]";
		}
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doQTICorrection(UserRequest ureq, AssessmentEntry assessment, GradingAssignment assignment,
			IQTESTCourseNode courseNode, boolean readOnly, boolean anonymous) {
		removeAsListenerAndDispose(correctionCtrl);
		
		Identity assessedIdentity = assessment.getIdentity();
		RepositoryEntry entry = assessment.getRepositoryEntry();
		RepositoryEntry referenceEntry = assessment.getReferenceEntry();
		
		AssessmentTestSession session = qtiService
				.getLastAssessmentTestSessions(entry, courseNode.getIdent(), referenceEntry, assessedIdentity);
		if(session == null) {
			gradingService.deactivateAssignment(assignment);
			showWarning("warning.assignement.deactivated");
			loadModel();
		} else {
			try {
				File unzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(referenceEntry.getOlatResource());
				ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
				ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
				TestSessionState testSessionState = qtiService.loadTestSessionState(session);
				// use mutable maps to allow updates
				Map<Identity,AssessmentTestSession> lastSessions = new HashMap<>();
				lastSessions.put(assessedIdentity, session);
				Map<Identity, TestSessionState> testSessionStates = new HashMap<>();
				testSessionStates.put(assessedIdentity, testSessionState);
				CorrectionOverviewModel model = new CorrectionOverviewModel(entry, courseNode, referenceEntry,
						resolvedAssessmentTest, manifestBuilder, lastSessions, testSessionStates, getTranslator());
				GradingTimeRecordRef record = gradingService.getCurrentTimeRecord(assignment, ureq.getRequestTimestamp());
				
				correctionCtrl = new CorrectionIdentityAssessmentItemListController(ureq, getWindowControl(), stackPanel,
						model, assessedIdentity, assignment, record, readOnly, anonymous);
				listenTo(correctionCtrl);
				stackPanel.pushController(translate("correction"), correctionCtrl);
			} catch (Exception e) {
				logError("", e);
				
				String assessedFullname = userManager.getUserDisplayName(assessedIdentity);
				showError("error.assessment.test.session.identities", new String[] { assessedFullname });
			}
		}
	}
	
	private void doUpdateCourseNode(AssessmentTestSession testSessionsToComplete, AssessmentTest assessmentTest,
			AssessmentEntryStatus status, GradingAssignment assignment) {
		if(testSessionsToComplete == null) return;
		
		assignment = gradingService.getGradingAssignment(assignment);
		AssessmentEntry assessment = assignment.getAssessmentEntry();
		assessment = gradingService.loadFullAssessmentEntry(assessment);

		RepositoryEntry entry = assessment.getRepositoryEntry();
		
		Boolean userVisible = null;
		if(StringHelper.containsNonWhitespace(assessment.getSubIdent())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(assessment.getSubIdent());
			
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
			

			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(testSessionsToComplete.getIdentity(), courseEnv);
			AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			
			BigDecimal finalScore = testSessionsToComplete.getFinalScore();
			Float score = finalScore == null ? null : finalScore.floatValue();
			Boolean passed = scoreEval.getPassed();
			if(testSessionsToComplete.getManualScore() != null && finalScore != null && cutValue != null) {
				boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
			AssessmentEntryStatus finalStatus = status == null ? scoreEval.getAssessmentStatus() : status;
			userVisible = scoreEval.getUserVisible();
			if(finalStatus == AssessmentEntryStatus.done && courseNode instanceof IQTESTCourseNode) {
				userVisible = Boolean.valueOf(((IQTESTCourseNode)courseNode).isScoreVisibleAfterCorrection());
			}
			ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, passed,
					finalStatus, userVisible, scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					scoreEval.getCurrentRunStatus(), testSessionsToComplete.getKey());
			courseAssessmentService.updateScoreEvaluation(courseNode, manualScoreEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);

		}
		
		if(status == AssessmentEntryStatus.done) {
			Long metadataTime = qtiService.getMetadataCorrectionTimeInSeconds(assignment.getReferenceEntry(), testSessionsToComplete);
			gradingService.assignmentDone(assignment, metadataTime, userVisible);
		}
		
		dbInstance.commit();// commit all
	}
	
	private void doOpenReportConfiguration(UserRequest ureq) {
		if(guardModalController(reportCtrl)) return;
		
		reportCtrl = new ReportCalloutController(ureq, getWindowControl(), testEntry, grader);
		listenTo(reportCtrl);
		
		reportCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				reportCtrl.getInitialComponent(), reportButton.getFormDispatchId(), "", true, "");
		listenTo(reportCalloutCtrl);
		reportCalloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, GradingAssignmentRow row, FormLink link) {
		if(guardModalController(toolsCtrl)) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doBatchAssignGrader(UserRequest ureq) {
		List<GradingAssignment> assignments = getSelectedGradingAssignments();
		if(assignments.isEmpty()) {
			showWarning("warning.atleastone.assignment");
		} else {
			doAssignGrader(ureq, assignments);
		}
	}
	
	private List<GradingAssignment> getSelectedGradingAssignments() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<GradingAssignment> assignments = new ArrayList<>(selectedIndex.size());
		for(Integer index:selectedIndex) {
			GradingAssignmentRow row = tableModel.getObject(index.intValue());
			if(row != null) {
				assignments.add(row.getAssignment());
			}
		}
		return assignments;
	}
	
	private void doAssignGrader(UserRequest ureq, GradingAssignmentRow row) {
		List<GradingAssignment> assignments = Collections.singletonList(row.getAssignment());
		doAssignGrader(ureq, assignments);
	}
	
	private void doAssignGrader(UserRequest ureq, final List<GradingAssignment> assignments) {
		RepositoryEntryGradingConfiguration configuration = null;
		if(testEntry != null) {
			configuration = gradingService.getOrCreateConfiguration(testEntry);
		}
		
		final AssignGraderContext assignGrader = new AssignGraderContext(testEntry);
		GraderMailTemplate mailTemplate = GraderMailTemplate.notification(getTranslator(), null, null, testEntry, configuration);
		Step start = new AssignGrader1ChooseMemberStep(ureq, assignGrader, mailTemplate);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			MailerResult result = new MailerResult();
			GraderMailTemplate sendTemplate = assignGrader.isSendEmail() ? mailTemplate : null;
			for(GradingAssignment assignment:assignments) {
				gradingService.assignGrader(assignment, assignGrader.getGrader(), sendTemplate, result);
			}
			if(mailTemplate.getAttachmentsTmpDir() != null) {
				FileUtils.deleteDirsAndFiles(mailTemplate.getAttachmentsTmpDir(), true, true);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		assignGraderWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.grader"), "o_sel_repo_assign_grader_1_wizard");
		listenTo(assignGraderWizard);
		getWindowControl().pushAsModalDialog(assignGraderWizard.getInitialComponent());
	}
	
	private void doBatchChangeGrader(UserRequest ureq) {
		List<GradingAssignment> assignments = getSelectedGradingAssignments();
		if(assignments.isEmpty()) {
			showWarning("warning.atleastone.assignment");
		} else {
			doChangeGrader(ureq, assignments);
		}
	}
	
	private void doChangeGrader(UserRequest ureq, GradingAssignmentRow row) {
		List<GradingAssignment> assignments = Collections.singletonList(row.getAssignment());
		doChangeGrader(ureq, assignments);
	}
	
	private void doChangeGrader(UserRequest ureq, final List<GradingAssignment> assignments) {
		final List<GraderToIdentity> currentGrader = assignments.stream()
				.map(GradingAssignment::getGrader)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		RepositoryEntryGradingConfiguration configuration = null;
		if(testEntry != null) {
			configuration = gradingService.getOrCreateConfiguration(testEntry);
		}
		
		final AssignGraderContext assignGrader = new AssignGraderContext(testEntry, currentGrader);
		GraderMailTemplate mailTemplate = GraderMailTemplate.notification(getTranslator(), null, null, testEntry, configuration);
		Step start = new AssignGrader1ChooseMemberStep(ureq, assignGrader, mailTemplate);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			MailerResult result = new MailerResult();
			GraderMailTemplate sendTemplate = assignGrader.isSendEmail() ? mailTemplate : null;
			for(GradingAssignment assignment:assignments) {
				GradingAssignment unassignedAssignment = gradingService.unassignGrader(assignment);
				gradingService.assignGrader(unassignedAssignment, assignGrader.getGrader(), sendTemplate, result);
			}
			if(mailTemplate.getAttachmentsTmpDir() != null) {
				FileUtils.deleteDirsAndFiles(mailTemplate.getAttachmentsTmpDir(), true, true);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		changeGraderWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("change.grader"), "o_sel_repo_change_grader_1_wizard");
		listenTo(changeGraderWizard);
		getWindowControl().pushAsModalDialog(changeGraderWizard.getInitialComponent());
	}
	
	private void doBatchContact(UserRequest ureq) {
		List<GradingAssignmentRow> assignmentRows = getSelectedGradingAssignmentRows();
		if(assignmentRows.isEmpty()) {
			showWarning("warning.atleastone.assignment");
		} else {	
			ContactMessage msg = new ContactMessage(getIdentity());
			ContactList contact = new ContactList(translate("contact.grader.mail"));
			Set<RepositoryEntry> entries = new HashSet<>();
			Set<RepositoryEntry> referenceEntries = new HashSet<>();
			for(GradingAssignmentRow row:assignmentRows) {
				if(row.getGrader() != null) {
					contact.add(row.getGrader());
				}
				if(row.getEntry() != null) {
					entries.add(row.getEntry());
				}
				if(row.getReferenceEntry() != null) {
					referenceEntries.add(row.getReferenceEntry());
				}
			}
			msg.addEmailTo(contact);
			
			RepositoryEntry entry = entries.size() == 1 ? entries.iterator().next() : null;
			RepositoryEntry referenceEntry = referenceEntries.size() == 1 ? referenceEntries.iterator().next() : null; 
			List<MailTemplate> templates = getTemplates(entry, referenceEntry);
			contactGraderCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, msg, templates);
			contactGraderCtrl.getAndRemoveTitle();
			listenTo(contactGraderCtrl);
			
			String graderName = getGradersNames(assignmentRows);
			String title = translate("contact.grader.title", new String[] { graderName });
			cmc = new CloseableModalController(getWindowControl(), "close", contactGraderCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();	
		}
	}
	
	private void doContact(UserRequest ureq, GradingAssignmentRow row) {
		ContactMessage msg = new ContactMessage(getIdentity());
		ContactList contact = new ContactList(translate("contact.grader.mail"));
		contact.add(row.getGrader());
		msg.addEmailTo(contact);
		
		GradingAssignment assignment = gradingService.getGradingAssignment(row);
		RepositoryEntry referenceEntry = assignment.getReferenceEntry();
		AssessmentEntry assessmentEntry = assignment.getAssessmentEntry();
		RepositoryEntry entry = assessmentEntry.getRepositoryEntry();
		
		List<MailTemplate> templates = getTemplates(entry, referenceEntry);
		contactGraderCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, msg, templates);
		contactGraderCtrl.getAndRemoveTitle();
		listenTo(contactGraderCtrl);
		
		String graderName = userManager.getUserDisplayName(row.getGrader());
		String title = translate("contact.grader.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", contactGraderCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private List<MailTemplate> getTemplates(RepositoryEntry entry, RepositoryEntry referenceEntry) {
		RepositoryEntryGradingConfiguration configuration = null;
		if(referenceEntry != null) {
			configuration = gradingService.getOrCreateConfiguration(referenceEntry);
		}
		
		List<MailTemplate> templates = new ArrayList<>();
		templates.add(GraderMailTemplate.empty(getTranslator(), entry, null, referenceEntry));
		templates.add(GraderMailTemplate.graderTo(getTranslator(), entry, null, referenceEntry));
		templates.add(GraderMailTemplate.notification(getTranslator(), entry, null, referenceEntry, configuration));
		templates.add(GraderMailTemplate.firstReminder(getTranslator(), entry, null, referenceEntry, configuration));
		templates.add(GraderMailTemplate.secondReminder(getTranslator(), entry, null, referenceEntry, configuration));
		return templates;
	}
	
	private void doBatchUnassignGrader(UserRequest ureq) {
		List<GradingAssignmentRow> rows = getSelectedGradingAssignmentRows();
		if(rows.isEmpty()) {
			showWarning("warning.atleastone.assignment");
		} else {
			doUnassign(ureq, rows);
		}
	}
	
	private List<GradingAssignmentRow> getSelectedGradingAssignmentRows() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<GradingAssignmentRow> rows = new ArrayList<>(selectedIndex.size());
		for(Integer index:selectedIndex) {
			GradingAssignmentRow row = tableModel.getObject(index.intValue());
			if(row != null) {
				rows.add(row);
			}
		}
		return rows;
	}
	
	private void doUnassign(UserRequest ureq, GradingAssignmentRow row) {
		doUnassign(ureq, Collections.singletonList(row));
	}
	
	private void doUnassign(UserRequest ureq, List<GradingAssignmentRow> rows) {
		List<GradingAssignment> assignments = rows.stream()
				.filter(GradingAssignmentRow::hasGrader)
				.map(GradingAssignmentRow::getAssignment)
				.collect(Collectors.toList());
		if(assignments.isEmpty()) {
			showWarning("warning.atleastone.assignment.with.grader");
		} else {
			confirmUnassignGraderCtrl = new ConfirmUnassignGraderController(ureq, getWindowControl(), assignments);
			listenTo(confirmUnassignGraderCtrl);
	
			String gradersNames = getGradersNames(rows);
			String title = translate("confirm.unassign.grader.title", new String[] { gradersNames });
			cmc = new CloseableModalController(getWindowControl(), "close", confirmUnassignGraderCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doBatchExtendDeadline(UserRequest ureq) {
		List<GradingAssignmentRow> rows = getSelectedGradingAssignmentRows();
		if(rows.isEmpty()) {
			showWarning("warning.atleastone.assignment");
		} else {
			doExtendDeadline(ureq, rows);
		}
	}
	
	private void doExtendDeadline(UserRequest ureq, GradingAssignmentRow row) {
		doExtendDeadline(ureq, Collections.singletonList(row));
	}
	
	private void doExtendDeadline(UserRequest ureq, List<GradingAssignmentRow> rows) {
		List<GradingAssignment> assignments = rows.stream()
				.map(GradingAssignmentRow::getAssignment)
				.collect(Collectors.toList());
		extendDeadlineCtrl = new ExtendDeadlineController(ureq, getWindowControl(), assignments);
		listenTo(extendDeadlineCtrl);

		String gradersNames = getGradersNames(rows);
		String title = translate("extend.deadline.title", new String[] { gradersNames });
		cmc = new CloseableModalController(getWindowControl(), "close", extendDeadlineCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private String getGradersNames(List<GradingAssignmentRow> rows) {
		StringBuilder sb = new StringBuilder(128);
		for(GradingAssignmentRow row:rows) {
			if(row.getGrader() != null) {
				String graderName = userManager.getUserDisplayName(row.getGrader());
				if(sb.length() > 0) sb.append(", ");
				sb.append(graderName);
			}
		}
		return sb.toString();
	}
	
	private void doReopenGrading(UserRequest ureq, GradingAssignmentRow row) {
		confirmReopenAssignmentCtrl = new ConfirmReopenAssignmentController(ureq, getWindowControl(),
				row.getAssignment());
		listenTo(confirmReopenAssignmentCtrl);

		String graderName = userManager.getUserDisplayName(row.getGrader());
		String title = translate("confirm.reopen.assignment.title", new String[] { graderName });
		cmc = new CloseableModalController(getWindowControl(), "close", confirmReopenAssignmentCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final GradingAssignmentRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, GradingAssignmentRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tools_assignments");
			if(row.hasGrader()) {
				addLink("tool.change.grader", "change_grader", "o_icon o_icon_edit", mainVC);
				addLink("tool.send.mail", "send_mail", "o_icon o_icon_mail", mainVC);
				
				if(row.getAssignmentStatus() == GradingAssignmentStatus.done) {
					addLink("tool.reopen.assignment", "reopen", "o_icon o_icon_reopen", mainVC);
					addLink("tool.view.grading", "view_grading", "o_icon o_icon_assessment_mode", mainVC);
				} else {
					addLink("tool.extend.deadline", "extend_deadline", "o_icon o_icon_extend", mainVC);
				}
			} else {
				addLink("tool.assign.grader", "assign_grader", "o_icon o_icon_edit", mainVC);
			}

			if(row.hasGrader()) {
				addLink("tool.unassign", "unassign", "o_icon o_icon_deactivate", mainVC);
			}
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, name, cmd, mainVC, this);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}
		
		public GradingAssignmentRow getAssignmentRow() {
			return row;
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link) {
				close();
				
				Link link = (Link)source;
				String cmd = link.getCommand();
				switch(cmd) {
					case "assign_grader": doAssignGrader(ureq, getAssignmentRow()); break;
					case "change_grader": doChangeGrader(ureq, getAssignmentRow()); break;
					case "send_mail": doContact(ureq, getAssignmentRow()); break;
					case "unassign": doUnassign(ureq, getAssignmentRow()); break;
					case "extend_deadline": doExtendDeadline(ureq, getAssignmentRow()); break;
					case "view_grading": doGrade(ureq, getAssignmentRow(), true); break;
					case "reopen": doReopenGrading(ureq, getAssignmentRow()); break;
					default: break;// do nothing
				}
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
