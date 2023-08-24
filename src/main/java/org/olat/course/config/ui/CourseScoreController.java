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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.config.ui.AssessmentResetController.AssessmentResetEvent;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.RunMainController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseScoreController extends FormBasicController {
	
	private static final String SCORE_VALUE_NONE = "options.score.points.none";

	private FormLayoutContainer settingsCont;
	private SingleSelection scoreEl;
	private MultipleSelectionElement coachesCanEl;
	private MultipleSelectionElement passedEl;
	private TextElement passedNumberCutEl;
	private TextElement passedPointsCutEl;
	
	private CloseableModalController cmc;
	private AssessmentResetController assessmentResetCtrl;
	
	private final RepositoryEntry courseEntry;
	private final boolean editable;
	private final boolean mandatoryNodesAvailable;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private LearningPathService learningPathService;

	public CourseScoreController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean editable) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.courseEntry = entry;
		this.editable = editable;
		
		initScoreCalculatorSupport(ureq);
		mandatoryNodesAvailable = isMandataoryNodesAvailable();
		
		initForm(ureq);
	}

	/**
	 * This is a security procedure to ensure the right value in CONFIG_SCORE_CALCULATOR_SUPPORTED.
	 * In releases between OO 15.pre.0 and 15.pre.7 this value was not set.
	 * If a user has access to this controller we have to set the value.
	 * 
	 * @param ureq 
	 */
	private void initScoreCalculatorSupport(UserRequest ureq) {
		OLATResourceable courseOres = courseEntry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		ModuleConfiguration runConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		
		boolean scoreCalculatorSupported = runConfig.getBooleanSafe(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, true);
		if (scoreCalculatorSupported) {
			CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
			ModuleConfiguration editorConfig = courseEditorTreeNode.getCourseNode().getModuleConfiguration();
			
			runConfig.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, false);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, false);
			
			CourseFactory.saveCourse(courseEntry.getOlatResource().getResourceableId());
		}
		
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if (scoreCalculatorSupported) {
			courseAssessmentService.evaluateAll(course, true);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	private boolean isMandataoryNodesAvailable() {
		CourseNode rootNode = CourseFactory.loadCourse(courseEntry).getRunStructure().getRootNode();
		MandatoryVisitor visitor = new MandatoryVisitor();
		TreeVisitor tv = new TreeVisitor(visitor, rootNode, true);
		tv.visitAll();
		return visitor.isMandatory();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.removeAll();
		setFormTitle("options.score.title");
		setFormContextHelp("manual_user/course_create/Course_Settings/#tab-assessment-for-learning-path-courses");
		
		settingsCont = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add(settingsCont);
		
		SelectionValues scoreKV = new SelectionValues();
		scoreKV.add(SelectionValues.entry(SCORE_VALUE_NONE, translate( "options.score.points.none")));
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_SUM, translate("options.score.points.sum")));
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_AVG, translate("options.score.points.average")));
		scoreEl = uifactory.addDropdownSingleselect("options.score.points", settingsCont, scoreKV.keys(), scoreKV.values());
		scoreEl.setEnabled(editable);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		ModuleConfiguration moduleConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		String scoreKey = moduleConfig.has(STCourseNode.CONFIG_SCORE_KEY)? moduleConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY): SCORE_VALUE_NONE;
		scoreEl.select(scoreKey, true);
		
		// Coach rights
		SelectionValues coachesCanSV = new SelectionValues();
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_COACH_GRADE_APPLY, translate("options.grade.apply")));
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, translate("options.user.visibility")));
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_MANUALLY, translate("options.passed.manually")));
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_COACH_RESET_DATA, translate("options.reset.data")));
		coachesCanEl = uifactory.addCheckboxesVertical("options.coaches.can", settingsCont, coachesCanSV.keys(), coachesCanSV.values(), 1);
		coachesCanEl.addActionListener(FormEvent.ONCHANGE);
		coachesCanEl.setEnabled(editable);
		coachesCanEl.select(STCourseNode.CONFIG_COACH_GRADE_APPLY, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY));
		coachesCanEl.select(STCourseNode.CONFIG_COACH_USER_VISIBILITY, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY));
		coachesCanEl.select(STCourseNode.CONFIG_PASSED_MANUALLY, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_MANUALLY));
		coachesCanEl.select(STCourseNode.CONFIG_COACH_RESET_DATA, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_RESET_DATA));
		
		// Passed evaluation
		SelectionValues passedSV = new SelectionValues();
		passedSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_PROGRESS, translate("options.passed.progress")));
		passedSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_ALL, translate("options.passed.all")));
		passedSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_NUMBER, translate("options.passed.number")));
		passedSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_POINTS, translate("options.passed.points")));
		passedEl = uifactory.addCheckboxesVertical("options.passed", settingsCont, passedSV.keys(), passedSV.values(), 1);
		passedEl.setHelpTextKey("options.passed.help", null);
		passedEl.addActionListener(FormEvent.ONCHANGE);
		passedEl.setEnabled(editable);
		passedEl.select(STCourseNode.CONFIG_PASSED_PROGRESS, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS));
		passedEl.select(STCourseNode.CONFIG_PASSED_ALL, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL));
		passedEl.select(STCourseNode.CONFIG_PASSED_NUMBER, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER));
		passedEl.select(STCourseNode.CONFIG_PASSED_POINTS, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS));
		
		String passedNumberCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_NUMBER_CUT)
				? String.valueOf(moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 1))
				: null;
		passedNumberCutEl = uifactory.addTextElement("options.passed.number.cut", 10, passedNumberCut, settingsCont);
		passedNumberCutEl.setCheckVisibleLength(true);
		passedNumberCutEl.setDisplaySize(10);
		passedNumberCutEl.setEnabled(editable);
		
		String passedPointsCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_POINTS_CUT)
				? String.valueOf(moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_POINTS_CUT, 1))
				: null;
		passedPointsCutEl = uifactory.addTextElement("options.passed.points.cut", 10, passedPointsCut, settingsCont);
		passedPointsCutEl.setCheckVisibleLength(true);
		passedPointsCutEl.setDisplaySize(10);
		passedPointsCutEl.setEnabled(editable);
		
		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			settingsCont.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
		
		updateUI();
	}

	private void updateUI() {
		boolean userVisibility = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		if (userVisibility) {
			coachesCanEl.setEnabled(STCourseNode.CONFIG_PASSED_MANUALLY, true);
		} else {
			coachesCanEl.setEnabled(STCourseNode.CONFIG_PASSED_MANUALLY, false);
			coachesCanEl.select(STCourseNode.CONFIG_PASSED_MANUALLY, false);
		}
		
		boolean passedNumber = passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_NUMBER);
		passedNumberCutEl.setVisible(passedNumber);
		
		boolean passedPoints = passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_POINTS);
		passedPointsCutEl.setVisible(passedPoints);
		
		setFormWarning(null);
		settingsCont.setElementCssClass(null);
		if (!mandatoryNodesAvailable && passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_PROGRESS)) {
			setFormTranslatedWarning(translate("error.passed.progress.only.optional"));
			settingsCont.setElementCssClass("o_block_top");
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == coachesCanEl || source == passedEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == assessmentResetCtrl) {
			if (event instanceof AssessmentResetEvent are) {
				doSettingsConfirmed(ureq, are);
			} else if (event == AssessmentResetController.RESET_SETTING_EVENT) {
				initForm(ureq);
			} else {
				markDirty();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
			markDirty();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(assessmentResetCtrl);
		removeAsListenerAndDispose(cmc);
		assessmentResetCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateInteger(passedNumberCutEl, 1, Integer.MAX_VALUE, true, "error.positiv.int");
		allOk &= validateInteger(passedPointsCutEl, 1, Integer.MAX_VALUE, true, "error.positiv.int");
		
		return allOk;
	}

	private boolean validateInteger(TextElement el, int min, int max, boolean mandatory, String i18nKey) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						allOk = false;
					} else if(max < value) {
						allOk = false;
					}
				} catch (NumberFormatException e) {
					allOk = false;
				}
			} else if (mandatory) {
				allOk = false;
			}
		}
		if (!allOk) {
			el.setErrorKey(i18nKey);
		}
		return allOk;
	}

	private void doConfirmSetting(UserRequest ureq) {
		String rootNodeIdent = CourseFactory.loadCourse(courseEntry).getRunStructure().getRootNode().getIdent();
		boolean warningOptionalOnly = !mandatoryNodesAvailable && passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_PROGRESS);
		assessmentResetCtrl = new AssessmentResetController(ureq, getWindowControl(), courseEntry, rootNodeIdent, true,
				true, warningOptionalOnly);
		listenTo(assessmentResetCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				assessmentResetCtrl.getInitialComponent(), true, translate("assessment.reset.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doConfirmSetting(ureq);
	}
	
	private void doSettingsConfirmed(UserRequest ureq, AssessmentResetEvent are) {
		boolean saved = doSave();
		
		if (saved) {
			if (are.isResetOverriden()) {
				assessmentService.resetAllOverridenRootPassed(courseEntry);
			}
			if (are.isResetPassed()) {
				assessmentService.resetAllRootPassed(courseEntry);
			}
			if (are.isRecalculateAll()) {
				ICourse course = CourseFactory.loadCourse(courseEntry);
				courseAssessmentService.evaluateAll(course, true);
			}
			
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	private boolean doSave() {
		OLATResourceable courseOres = courseEntry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return false;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		ModuleConfiguration runConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
		ModuleConfiguration editorConfig = courseEditorTreeNode.getCourseNode().getModuleConfiguration();
		
		runConfig.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, false);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, false);
		
		String selectedScoreKey = scoreEl.getSelectedKey();
		if (SCORE_VALUE_NONE.equals(selectedScoreKey)) {
			runConfig.remove(STCourseNode.CONFIG_SCORE_KEY);
			editorConfig.remove(STCourseNode.CONFIG_SCORE_KEY);
		} else {
			runConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, selectedScoreKey);
			editorConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, selectedScoreKey);
		}
		
		boolean gradeApply = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_GRADE_APPLY);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_GRADE_APPLY, gradeApply);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_GRADE_APPLY, gradeApply);
		
		boolean userVisibility = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, userVisibility);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, userVisibility);
		
		boolean passedManually = coachesCanEl.isKeySelected(STCourseNode.CONFIG_PASSED_MANUALLY);
		if (passedManually) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_MANUALLY, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_MANUALLY, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
		}

		boolean resetData = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_RESET_DATA);
		if (resetData) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_RESET_DATA, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_RESET_DATA, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_COACH_RESET_DATA);
			editorConfig.remove(STCourseNode.CONFIG_COACH_RESET_DATA);
		}
		
		boolean passedProgress = passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_PROGRESS);
		if (passedProgress) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_PROGRESS);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_PROGRESS);
		}
		
		boolean passedAll = passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_ALL);
		if (passedAll) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_ALL);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_ALL);
		}
		
		boolean passedNumber = passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_NUMBER);
		if (passedNumber) {
			int numberCut = Integer.parseInt(passedNumberCutEl.getValue());
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_NUMBER, true);
			runConfig.setIntValue(STCourseNode.CONFIG_PASSED_NUMBER_CUT, numberCut);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_NUMBER, true);
			editorConfig.setIntValue(STCourseNode.CONFIG_PASSED_NUMBER_CUT, numberCut);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_NUMBER);
			runConfig.remove(STCourseNode.CONFIG_PASSED_NUMBER_CUT);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_NUMBER);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_NUMBER_CUT);
		}
		
		boolean passedPoints = passedEl.isKeySelected(STCourseNode.CONFIG_PASSED_POINTS);
		if (passedPoints) {
			int pointsCut = Integer.parseInt(passedPointsCutEl.getValue());
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_POINTS, true);
			runConfig.setIntValue(STCourseNode.CONFIG_PASSED_POINTS_CUT, pointsCut);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_POINTS, true);
			editorConfig.setIntValue(STCourseNode.CONFIG_PASSED_POINTS_CUT, pointsCut);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_POINTS);
			runConfig.remove(STCourseNode.CONFIG_PASSED_POINTS_CUT);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_POINTS);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_POINTS_CUT);
		}
		
		CourseFactory.saveCourse(courseEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		return true;
	}
	
	public class MandatoryVisitor implements Visitor {
		
		boolean mandatory = false;
		
		@Override
		public void visit(INode node) {
			if (node instanceof CourseNode courseNode) {
				if (!mandatory) {
					AssessmentObligation obligation = learningPathService.getConfigs(courseNode).getObligation();
					if (AssessmentObligation.mandatory == obligation) {
						mandatory = true;
					}
					
				}
			}
		}

		public boolean isMandatory() {
			return mandatory;
		}

	}

}
