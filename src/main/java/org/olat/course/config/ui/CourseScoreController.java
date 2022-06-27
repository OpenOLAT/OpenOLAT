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
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.config.ui.AssessmentResetController.AssessmentResetEvent;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.RunMainController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentService;
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
	private static final String[] ONE_OPTION = { "xx" };
	private static final String[] UNTRANSLATED = { "" };

	private SingleSelection scoreEl;
	private MultipleSelectionElement gradeApplyEl;
	private MultipleSelectionElement userVisibilityEl;
	private MultipleSelectionElement passedManuallyEl;
	private MultipleSelectionElement passedProgressEl;
	private MultipleSelectionElement passedAllEl;
	private MultipleSelectionElement passedNumberEl;
	private TextElement passedNumberCutEl;
	private MultipleSelectionElement passedPointsEl;
	private TextElement passedPointsCutEl;
	
	private CloseableModalController cmc;
	private AssessmentResetController assessmentResetCtrl;
	
	private final RepositoryEntry courseEntry;
	private final boolean editable;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentService assessmentService;

	public CourseScoreController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.courseEntry = entry;
		this.editable = editable;
		
		initScoreCalculatorSupport(ureq);
		
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

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.removeAll();
		setFormTitle("options.score.title");
		
		SelectionValues scoreKV = new SelectionValues();
		scoreKV.add(SelectionValues.entry(SCORE_VALUE_NONE, translate( "options.score.points.none")));
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_SUM, translate("options.score.points.sum")));
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_AVG, translate("options.score.points.average")));
		scoreEl = uifactory.addDropdownSingleselect("options.score.points", formLayout, scoreKV.keys(), scoreKV.values());
		scoreEl.setEnabled(editable);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		ModuleConfiguration moduleConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		String scoreKey = moduleConfig.has(STCourseNode.CONFIG_SCORE_KEY)? moduleConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY): SCORE_VALUE_NONE;
		scoreEl.select(scoreKey, true);
		
		// Coach rights
		uifactory.addStaticTextElement("options.coach.can", null, formLayout);
		
		gradeApplyEl = uifactory.addCheckboxesHorizontal("options.grade.apply", formLayout, ONE_OPTION, UNTRANSLATED);
		boolean gradeApply = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY);
		gradeApplyEl.select(gradeApplyEl.getKey(0), gradeApply);
		gradeApplyEl.setEnabled(editable);
		
		userVisibilityEl = uifactory.addCheckboxesHorizontal("options.user.visibility", formLayout, ONE_OPTION, UNTRANSLATED);
		userVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		boolean coachUserVisibility = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		userVisibilityEl.select(userVisibilityEl.getKey(0), coachUserVisibility);
		userVisibilityEl.setEnabled(editable);
		
		passedManuallyEl = uifactory.addCheckboxesHorizontal("options.passed.manually", formLayout, ONE_OPTION, UNTRANSLATED);
		boolean passedManually = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_MANUALLY);
		passedManuallyEl.select(passedManuallyEl.getKey(0), passedManually);
		passedManuallyEl.setEnabled(editable);
		
		// Passed evaluation
		StaticTextElement passedIfEl = uifactory.addStaticTextElement("options.passed.if", null, formLayout);
		passedIfEl.setHelpTextKey("options.passed.if.help", null);
		
		passedProgressEl = uifactory.addCheckboxesHorizontal("options.passed.progress", formLayout, ONE_OPTION, UNTRANSLATED);
		boolean passedProgress = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS);
		passedProgressEl.select(passedProgressEl.getKey(0), passedProgress);
		passedProgressEl.setEnabled(editable);
		
		passedAllEl = uifactory.addCheckboxesHorizontal("options.passed.all", formLayout, ONE_OPTION, UNTRANSLATED);
		boolean passedAll = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL);
		passedAllEl.select(passedAllEl.getKey(0), passedAll);
		passedAllEl.setEnabled(editable);
		
		passedNumberEl = uifactory.addCheckboxesHorizontal("options.passed.number", formLayout, ONE_OPTION, UNTRANSLATED);
		passedNumberEl.addActionListener(FormEvent.ONCHANGE);
		boolean passedNumber = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER);
		passedNumberEl.select(passedNumberEl.getKey(0), passedNumber);
		passedNumberEl.setEnabled(editable);
		
		String passedNumberCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_NUMBER_CUT)
				? String.valueOf(moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 1))
				: null;
		passedNumberCutEl = uifactory.addTextElement("options.passed.number.cut", 10, passedNumberCut, formLayout);
		passedNumberCutEl.setCheckVisibleLength(true);
		passedNumberCutEl.setDisplaySize(10);
		passedNumberEl.setEnabled(editable);
		
		passedPointsEl = uifactory.addCheckboxesHorizontal("options.passed.points", formLayout, ONE_OPTION, UNTRANSLATED);
		passedPointsEl.addActionListener(FormEvent.ONCHANGE);
		boolean passedPoints = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS);
		passedPointsEl.select(passedPointsEl.getKey(0), passedPoints);
		passedPointsEl.setEnabled(editable);
		
		String passedPointsCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_POINTS_CUT)
				? String.valueOf(moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_POINTS_CUT, 1))
				: null;
		passedPointsCutEl = uifactory.addTextElement("options.passed.points.cut", 10, passedPointsCut, formLayout);
		passedPointsCutEl.setCheckVisibleLength(true);
		passedPointsCutEl.setDisplaySize(10);
		passedPointsEl.setEnabled(editable);
		
		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
		
		updateUI();
	}

	private void updateUI() {
		boolean userVisibility = userVisibilityEl.isAtLeastSelected(1);
		passedManuallyEl.setVisible(userVisibility);
		
		boolean passedNumber = passedNumberEl.isAtLeastSelected(1);
		passedNumberCutEl.setVisible(passedNumber);
		
		boolean passedPoints = passedPointsEl.isAtLeastSelected(1);
		passedPointsCutEl.setVisible(passedPoints);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == userVisibilityEl) {
			updateUI();
		} else if (source == passedNumberEl) {
			updateUI();
		} else if (source == passedPointsEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == assessmentResetCtrl) {
			if (event instanceof AssessmentResetEvent) {
				AssessmentResetEvent are = (AssessmentResetEvent)event;
				doSettingsConfirmed(ureq, are);
			} else if (event == AssessmentResetController.RESET_SETTING_EVENT) {
				initForm(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
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
			el.setErrorKey(i18nKey, null);
		}
		return allOk;
	}

	private void doConfirmSetting(UserRequest ureq) {
		assessmentResetCtrl = new AssessmentResetController(ureq, getWindowControl(), true, true);
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
		
		boolean gradeApply = gradeApplyEl.isAtLeastSelected(1);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_GRADE_APPLY, gradeApply);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_GRADE_APPLY, gradeApply);
		
		boolean userVisibility = userVisibilityEl.isAtLeastSelected(1);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, userVisibility);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, userVisibility);
		
		boolean passedManually = passedManuallyEl.isVisible() && passedManuallyEl.isAtLeastSelected(1);
		if (passedManually) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_MANUALLY, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_MANUALLY, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
		}
		
		boolean passedProgress = passedProgressEl.isAtLeastSelected(1);
		if (passedProgress) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_PROGRESS);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_PROGRESS);
		}
		
		boolean passedAll = passedAllEl.isAtLeastSelected(1);
		if (passedAll) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_ALL);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_ALL);
		}
		
		boolean passedNumber = passedNumberEl.isAtLeastSelected(1);
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
		
		boolean passedPoints = passedPointsEl.isAtLeastSelected(1);
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

}
