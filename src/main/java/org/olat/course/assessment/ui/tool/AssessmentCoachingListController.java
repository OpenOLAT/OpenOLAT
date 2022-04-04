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
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CoachingAssessmentEntry;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentCoachingTableModel.AssessmentCoachingsCol;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.ui.AssessedIdentityController;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.UserDisplayNameCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentCoachingListController extends FormBasicController {
	
	private static final String CMD_OPEN_COURSE = "open.course";
	private static final String CMD_OPEN_COURSE_NODE = "open.course.node";
	private static final String CMD_ASSESS = "assess";
	private static final String CMD_DETAILS = "details";
	private static final String CMD_APPLY_GRADE = "apply";

	private TooledStackedPanel stackPanel;
	private List<UserPropertyHandler> userPropertyHandlers;
	private FlexiTableElement tableEl;
	private AssessmentCoachingTableModel dataModel;
	private FormLink bulkEmailButton;
	private FormLink bulkVisibleButton;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private DialogBoxController applyGradeCtrl;
	private AssessedIdentityController currentIdentityCtrl;
	
	private final String translatedFormTitle;
	
	@Autowired
	protected DB dbInstance;
	@Autowired
	protected AssessmentToolManager assessmentToolManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;

	public AssessmentCoachingListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, String translatedFormTitle) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.stackPanel = stackPanel;
		this.translatedFormTitle = translatedFormTitle;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentCoachingTableModel.USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		initMultiSelectionTools();
		reload();
	}

	protected abstract boolean isShowLastUserModified();
	protected abstract boolean isShowStatusDoneInfo();
	protected abstract boolean canEditUserVisibility();
	protected abstract boolean canAssess();
	protected abstract boolean canViewDetails();
	protected abstract boolean canApplyGrade();
	protected abstract List<CoachingAssessmentEntry> loadModel();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTranslatedTitle(translatedFormTitle);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colPos = AssessmentCoachingTableModel.USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(AssessmentCoachingTableModel.USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AssessmentCoachingsCol.courseKey, CMD_OPEN_COURSE));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentCoachingsCol.course, CMD_OPEN_COURSE));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AssessmentCoachingsCol.courseExternalId, CMD_OPEN_COURSE));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AssessmentCoachingsCol.courseExternalRef, CMD_OPEN_COURSE));
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentCoachingsCol.courseNode, CMD_OPEN_COURSE_NODE, intendedNodeRenderer));
		if (isShowLastUserModified()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentCoachingsCol.lastUserModified));
		}
		if (isShowStatusDoneInfo()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentCoachingsCol.statusDoneBy, UserDisplayNameCellRenderer.get()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssessmentCoachingsCol.statusDoneAt));
		}
		if (canAssess()) {
			DefaultFlexiColumnModel assessColumn = new DefaultFlexiColumnModel("cmd.assess", -1, CMD_ASSESS,
					new StaticFlexiCellRenderer(translate("cmd.assess"), CMD_ASSESS, "", "", null));
			assessColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(assessColumn);
		}
		if (canViewDetails()) {
			DefaultFlexiColumnModel detailsColumn = new DefaultFlexiColumnModel("cmd.details", -1, CMD_DETAILS,
					new StaticFlexiCellRenderer(translate("cmd.details"), CMD_DETAILS, "", "", null));
			detailsColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(detailsColumn);
		}
		if (canApplyGrade()) {
			DefaultFlexiColumnModel detailsColumn = new DefaultFlexiColumnModel("cmd.grade.apply", -1, CMD_APPLY_GRADE,
					new StaticFlexiCellRenderer(translate("cmd.grade.apply"), CMD_APPLY_GRADE, "", "", null));
			detailsColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(detailsColumn);
		}
		
		dataModel = new AssessmentCoachingTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "assessment.coaching");
	}
	
	private void initMultiSelectionTools() {
		FormLayoutContainer emptyCont = FormLayoutContainer.createBareBoneFormLayout("empty", getTranslator());
		emptyCont.setRootForm(mainForm);
		
		bulkEmailButton = uifactory.addFormLink("bulk.email", emptyCont, Link.BUTTON);
		bulkEmailButton.setElementCssClass("o_sel_assessment_bulk_email");
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		tableEl.addBatchButton(bulkEmailButton);
		
		if (canEditUserVisibility()) {
			bulkVisibleButton = uifactory.addFormLink("bulk.visible", emptyCont, Link.BUTTON);
			bulkVisibleButton.setElementCssClass("o_sel_assessment_bulk_visible");
			bulkVisibleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_results_visible");
			tableEl.addBatchButton(bulkVisibleButton);
		}
	}

	public void reload() {
		List<AssessmentCoachingRow> rows = loadModel().stream()
				.map(entry -> new AssessmentCoachingRow(entry, userPropertyHandlers, getLocale()))
				.collect(Collectors.toList());
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	protected String getQuickSearchString() {
		return tableEl.getQuickSearchString();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(currentIdentityCtrl == source) {
			if(event instanceof AssessmentFormEvent) {
				AssessmentFormEvent aee = (AssessmentFormEvent)event;
				reload();
				if(aee.isClose()) {
					stackPanel.popController(currentIdentityCtrl);
				}
			} else if(event == Event.CHANGED_EVENT) {
				reload();
			} else if(event == Event.CANCELLED_EVENT) {
				reload();
				stackPanel.popController(currentIdentityCtrl);
			}
		} else if (source == contactCtrl) {
			if(cmc != null) {
				cmc.deactivate();
			}
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (applyGradeCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				AssessmentCoachingRow row = (AssessmentCoachingRow)applyGradeCtrl.getUserObject();
				doApplyGrade(ureq, row);
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(applyGradeCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		applyGradeCtrl = null;
		contactCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (bulkEmailButton == source) {
			doEmail(ureq);
		} else if (bulkVisibleButton == source) {
			doSetUserVisibility(true);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if (CMD_ASSESS.equals(se.getCommand())) {
					doAssess(ureq, dataModel.getObject(se.getIndex()));
				} else if (CMD_DETAILS.equals(se.getCommand())) {
					// The assessment controller opens the release view automatically.
					doAssess(ureq, dataModel.getObject(se.getIndex()));
				} else if (CMD_APPLY_GRADE.equals(se.getCommand())) {
					doConfirmApplyGrade(ureq, dataModel.getObject(se.getIndex()));
				} else if (CMD_OPEN_COURSE.equals(se.getCommand())) {
					doOpenCourse(ureq, dataModel.getObject(se.getIndex()));
				} else if (CMD_OPEN_COURSE_NODE.equals(se.getCommand())) {
					doOpenCourseNode(ureq, dataModel.getObject(se.getIndex()));
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				reload();
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}
	
	private void doAssess(UserRequest ureq, AssessmentCoachingRow row) {
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(row.getRepositoryEntryKey());
		if (repositoryEntry == null) return;
		
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		if (course == null) return;
		
		CourseNode courseNode = course.getRunStructure().getNode(row.getSubIdent());
		if (courseNode == null) return;
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		if (assessedIdentity == null) return;
		
		if (!isAssessedIdentityLocked(ureq, repositoryEntry, courseNode, assessedIdentity)) {
			doAssess(ureq, repositoryEntry, course, courseNode, assessedIdentity);
		}
	}
	
	private Controller doAssess(UserRequest ureq, RepositoryEntry courseEntry, ICourse course, CourseNode courseNode, Identity assessedIdentity) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		UserCourseEnvironment coachCourseEnv = createUserCourseEnv(course, getIdentity());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Identity", assessedIdentity.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		if(courseNode.getParent() == null) {
			currentIdentityCtrl = new AssessmentIdentityCourseController(ureq, bwControl, stackPanel, courseEntry,
					coachCourseEnv, assessedIdentity, true);
		} else {
			currentIdentityCtrl = new AssessmentIdentityCourseNodeController(ureq, getWindowControl(), stackPanel,
					courseEntry, courseNode, coachCourseEnv, assessedIdentity, true, true);
		}
		listenTo(currentIdentityCtrl);
		
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		stackPanel.pushController(fullName, currentIdentityCtrl);
		return currentIdentityCtrl;
	}
	
	private boolean isAssessedIdentityLocked(UserRequest ureq, RepositoryEntry courseEntry, CourseNode courseNode, Identity assessedIdentity) {
		if(courseNode.getParent() == null) return false;

		ICourse course = CourseFactory.loadCourse(courseEntry);
		String locksubkey = AssessmentIdentityCourseNodeController.lockKey(courseNode, assessedIdentity);
		if(CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(course, locksubkey)) {
			LockEntry lock = CoordinatorManager.getInstance().getCoordinator().getLocker().getLockEntry(course, locksubkey);
			if(lock != null && lock.getOwner() != null && !lock.getOwner().equals(getIdentity())) {
				String msg = DialogBoxUIFactory.getLockedMessage(ureq, lock, "assessmentLock", getTranslator());
				getWindowControl().setWarning(msg);
				return true;
			}
		}
		
		return false;
	}
	
	private void doConfirmApplyGrade(UserRequest ureq, AssessmentCoachingRow row) {
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(row.getRepositoryEntryKey());
		if (repositoryEntry == null) return;
		
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		if (course == null) return;
		
		CourseNode courseNode = course.getRunStructure().getNode(row.getSubIdent());
		if (courseNode == null) return;
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		if (assessedIdentity == null) return;
		
		if (!isAssessedIdentityLocked(ureq, repositoryEntry, courseNode, assessedIdentity)) {
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(assessedIdentity, course.getCourseEnvironment());
			AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			
			if (scoreEval != null && scoreEval.getScore() != null) {
				GradeScale gradeScale = gradeService.getGradeScale(repositoryEntry, courseNode.getIdent());
				NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
				GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
				String grade = gradeScoreRange.getGrade();
				Boolean passed = Mode.none != courseAssessmentService.getAssessmentConfig(courseNode).getPassedMode()
						? Boolean.valueOf(gradeScoreRange.isPassed())
						: null;
				
				String text = null;
				if (passed != null) {
					if (passed.booleanValue()) {
						text = translate("grade.apply.text.passed", grade);
					} else {
						text = translate("grade.apply.text.failed", grade);
					}
				} else {
					text = translate("grade.apply.text", grade);
				}
				String title = translate("grade.apply");
				applyGradeCtrl = activateYesNoDialog(ureq, title, text, applyGradeCtrl);
				applyGradeCtrl.setUserObject(row);
			}
		}
	}
	
	private void doApplyGrade(UserRequest ureq, AssessmentCoachingRow row) {
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(row.getRepositoryEntryKey());
		if (repositoryEntry == null) return;
		
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		if (course == null) return;
		
		CourseNode courseNode = course.getRunStructure().getNode(row.getSubIdent());
		if (courseNode == null) return;
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		if (assessedIdentity == null) return;
		
		if (!isAssessedIdentityLocked(ureq, repositoryEntry, courseNode, assessedIdentity)) {
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(assessedIdentity, course.getCourseEnvironment());
			AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			if (scoreEval != null && scoreEval.getScore() != null) {
				GradeScale gradeScale = gradeService.getGradeScale(repositoryEntry, courseNode.getIdent());
				NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
				GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
				String grade = gradeScoreRange.getGrade();
				String performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
				Boolean passed = Mode.none != courseAssessmentService.getAssessmentConfig(courseNode).getPassedMode()
						? Boolean.valueOf(gradeScoreRange.isPassed())
						: null;
				
				ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), grade,
						performanceClassIdent, passed, scoreEval.getAssessmentStatus(), scoreEval.getUserVisible(),
						scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
						scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
				courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv,
						getIdentity(), false, Role.coach);
				reload();
			}
		}
	}
	
	private void doEmail(UserRequest ureq) {
		if(guardModalController(contactCtrl)) return;
		
		Set<Long> identityKeys = tableEl.getMultiSelectedIndex().stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(AssessmentCoachingRow::getIdentityKey)
				.collect(Collectors.toSet());
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		
		if (identities.isEmpty()) {
			showWarning("error.msg.send.no.rcps");
		} else {
			ContactMessage contactMessage = new ContactMessage(getIdentity());
			ContactList contactList = new ContactList("participants");
			contactList.addAllIdentites(identities);
			contactMessage.addEmailTo(contactList);
			
			removeAsListenerAndDispose(contactCtrl);
			contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
			listenTo(contactCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					contactCtrl.getInitialComponent(), true, translate("bulk.email"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doSetUserVisibility(boolean visible) {
		Boolean visibility = Boolean.valueOf(visible);
		
		List<AssessmentCoachingRow> selectedRows = tableEl.getMultiSelectedIndex().stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		Set<Long> repositoryEntryKeys = selectedRows.stream()
				.map(AssessmentCoachingRow::getRepositoryEntryKey)
				.collect(Collectors.toSet());
		Map<Long, RepositoryEntry> repoEntryKeyToReproEntry = repositoryManager.lookupRepositoryEntries(repositoryEntryKeys).stream()
				.collect(Collectors.toMap(RepositoryEntry::getKey, Function.identity()));
		
		for (AssessmentCoachingRow row : selectedRows) {
			RepositoryEntry repositoryEntry = repoEntryKeyToReproEntry.get(row.getRepositoryEntryKey());
			doSetUserVisibility(repositoryEntry, row.getSubIdent(), row.getIdentityKey(), visibility);
		}
		
		reload();
	}
	
	private void doSetUserVisibility(RepositoryEntry repositoryEntry, String subIdent, Long identityKey, Boolean userVisibility) {
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		if (course == null) return;
		
		CourseNode courseNode = course.getRunStructure().getNode(subIdent);
		if (courseNode == null) return;
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		UserCourseEnvironment assessedUserCourseEnv = createUserCourseEnv(course, assessedIdentity);
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();
		
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), scoreEval.getAssessmentStatus(),
				userVisibility, scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv, getIdentity(),
				false, Role.coach);
		dbInstance.commitAndCloseSession();
	}

	private UserCourseEnvironment createUserCourseEnv(ICourse course, Identity identity) {
		Roles roles = securityManager.getRoles(identity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(identity, roles);
		return new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
	}

	private void doOpenCourse(UserRequest ureq, AssessmentCoachingRow row) {
		String businessPath = "[RepositoryEntry:" + row.getRepositoryEntryKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doOpenCourseNode(UserRequest ureq, AssessmentCoachingRow row) {
		String businessPath = "[RepositoryEntry:" + row.getRepositoryEntryKey() + "]" + "[CourseNode:" + row.getSubIdent() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
