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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.reset.ResetDataContext;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.reset.ResetDataFinishStepCallback;
import org.olat.course.assessment.ui.reset.ResetWizardContext;
import org.olat.course.assessment.ui.reset.ResetWizardContext.ResetDataStep;
import org.olat.course.editor.overview.OverviewListController.OverviewListOptions;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.RunMainController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseScoreController extends FormBasicController {
	
	public static final String SCORE_VALUE_NONE = "options.score.points.none";
	public static final String PASSED_BY_PASSED_ENABLED = "passed-by-passed-enabled";
	
	private static final OverviewListOptions PROGRESS_OPTIONS = new OverviewListOptions(true, false, false, false, false, AssessmentObligation.mandatory, false, false);
	private static final OverviewListOptions PASSED_OPTIONS = new OverviewListOptions(true, false, false, true, true, null, true, false);
	private static final OverviewListOptions SCORE_OPTIONS = new OverviewListOptions(true, false, false, true, true, null, false, true);

	private FormLayoutContainer settingsCont;
	private SingleSelection scoreEl;
	private FormToggle scoreEnableEl;
	private FormToggle passedEnableEl;
	private MultipleSelectionElement passedByProgressEl;
	private MultipleSelectionElement passedByPassedEnableEl;
	private SingleSelection passedByPassedEl;
	private MultipleSelectionElement passedByScoreEl;
	
	private InfoPanelItem passedEnableInfosEl;
	private StaticTextElement passedLabelEl;
	private TextElement passedNumberCutEl;
	private TextElement passedPointsCutEl;
	private FormLink passedByProgressButton;
	private FormLink passedPointsCutOverviewButton;
	private FormLink passedNumberCutOverviewButton;
	private FormLayoutContainer passedByProgressElCont;
	private FormLayoutContainer passedNumberCutOverviewCont;
	private FormLayoutContainer passedPointsCutOverviewCont;
	
	private CloseableModalController overviewCmc;
	private CourseOverviewController overviewCtrl;
	private StepsMainRunController resetDataWizardCtrl;
	
	private final RepositoryEntry courseEntry;
	private final boolean editable;
	private final boolean mandatoryNodesAvailable;
	private CourseElementsInfos infos;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
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
		setFormInfo("options.score.description");
		setFormContextHelp("manual_user/learningresources/Course_Settings/#assessment-settings-for-learning-path-courses");
		
		settingsCont = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add(settingsCont);
		
		// Scoring
		ICourse course = CourseFactory.loadCourse(courseEntry);
		ModuleConfiguration moduleConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		String scoreKey = moduleConfig.has(STCourseNode.CONFIG_SCORE_KEY)? moduleConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY): SCORE_VALUE_NONE;
		scoreEnableEl = uifactory.addToggleButton("options.score.enable", "options.score.enable", translate("on"), translate("off"), settingsCont);
		scoreEnableEl.addActionListener(FormEvent.ONCHANGE);
		scoreEnableEl.setEnabled(editable);
		if(SCORE_VALUE_NONE.equals(scoreKey)) {
			scoreEnableEl.toggleOff();
		} else {
			scoreEnableEl.toggleOn();
		}
		
		SelectionValues scoreKV = new SelectionValues();
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_SUM, translate("options.score.points.sum"),
				translate("options.score.points.sum.descr"), null, null, true));
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_SUM_WEIGHTED, translate("options.score.points.sum.weighted"),
				translate("options.score.points.sum.weighted.descr"), null, null, true));
		scoreKV.add(SelectionValues.entry(STCourseNode.CONFIG_SCORE_VALUE_AVG, translate("options.score.points.average"),
				translate("options.score.points.average.descr"), null, null, true));
		scoreEl = uifactory.addCardSingleSelectHorizontal("options.score.points", "options.score.points", settingsCont, scoreKV);
		scoreEl.addActionListener(FormEvent.ONCHANGE);
		scoreEl.setEnabled(editable);
		if(scoreKV.containsKey(scoreKey)) {
			scoreEl.select(scoreKey, true);
		}
		
		uifactory.addSpacerElement("passed.spacer", settingsCont, false);
		
		// Passed evaluation
		passedEnableEl = uifactory.addToggleButton("options.passed.enable", "options.passed.enable", translate("on"), translate("off"), settingsCont);
		passedEnableEl.addActionListener(FormEvent.ONCHANGE);
		passedEnableEl.setEnabled(editable);
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS)) {
			passedEnableEl.toggleOn();
		} else {
			passedEnableEl.toggleOff();
		}
		
		passedEnableInfosEl = uifactory.addInfoPanel("passed.info", null, settingsCont);
		passedEnableInfosEl.setInformations(translate("options.passed.infos"));
		
		passedLabelEl = uifactory.addStaticTextElement("options.passed", "options.passed", "", settingsCont);
		
		SelectionValues passedByProgressSV = new SelectionValues();
		passedByProgressSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_PROGRESS, translate("options.passed.progress")));
		passedByProgressEl = uifactory.addCheckboxesVertical("options.passed.by.progress", settingsCont, passedByProgressSV.keys(), passedByProgressSV.values(), 1);
		passedByProgressEl.addActionListener(FormEvent.ONCHANGE);
		passedByProgressEl.setEnabled(editable);
		passedByProgressEl.setVisible(passedEnableEl.isOn());
		passedByProgressEl.select(STCourseNode.CONFIG_PASSED_PROGRESS, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS));
		
		infos = loadElementsInfos(course);

		String page = velocity_root + "/with_show_elements.html";
		passedByProgressElCont = uifactory.addCustomFormLayout("options.passed.by.progress.cont", null, page, settingsCont);
		passedByProgressButton = uifactory.addFormLink("options.passed.by.progress.rightAddOn", "options.passed.by.progress.overview", null, passedByProgressElCont, Link.LINK);
		passedByProgressButton.setIconLeftCSS("o_icon o_icon_preview");
		passedByProgressElCont.setElementCssClass("o_block_move_up");
		passedByProgressElCont.contextPut("linkId", "options.passed.by.progress.rightAddOn");
		passedByProgressElCont.contextPut("msg", translate("options.passed.by.progress.explain", Integer.toString(infos.getNumOfMandatoryElements())));
		
		SelectionValues passedByPassedEnableSV = new SelectionValues();
		passedByPassedEnableSV.add(SelectionValues.entry(PASSED_BY_PASSED_ENABLED, translate("options.passed.by.passed.elements")));
		passedByPassedEnableEl = uifactory.addCheckboxesVertical("options.passed.by.passed", settingsCont, passedByPassedEnableSV.keys(), passedByPassedEnableSV.values(), 1);
		passedByPassedEnableEl.addActionListener(FormEvent.ONCHANGE);
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)) {
			passedByPassedEnableEl.select(PASSED_BY_PASSED_ENABLED, true);	
		}
		
		SelectionValues passedByPassedSV = new SelectionValues();
		passedByPassedSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_ALL, translate("options.passed.all")));
		passedByPassedSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_NUMBER, translate("options.passed.number")));
		passedByPassedEl = uifactory.addDropdownSingleselect("options.passed.by.passed.rule", settingsCont, passedByPassedSV.keys(), passedByPassedSV.values());
		passedByPassedEl.addActionListener(FormEvent.ONCHANGE);
		passedByPassedEl.setEnabled(editable);
		passedByPassedEl.setVisible(passedEnableEl.isOn() && passedByPassedEnableEl.isAtLeastSelected(1));
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)) {
			passedByPassedEl.select(STCourseNode.CONFIG_PASSED_ALL, true);
		} else if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)) {
			passedByPassedEl.select(STCourseNode.CONFIG_PASSED_NUMBER, true);	
		}
		
		String passedNumberCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_NUMBER_CUT)
				? String.valueOf(moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 1))
				: null;
		passedNumberCutEl = uifactory.addTextElement("options.passed.number.cut", 10, passedNumberCut, settingsCont);
		passedNumberCutEl.setElementCssClass("form-inline");
		passedNumberCutEl.setCheckVisibleLength(true);
		passedNumberCutEl.setDisplaySize(10);
		passedNumberCutEl.setEnabled(editable);
		passedNumberCutEl.setTextAddOn("options.passed.to.pass");
		
		passedNumberCutOverviewCont = uifactory.addCustomFormLayout("number.rightAddOn.cont", null, page, settingsCont);
		passedNumberCutOverviewButton = uifactory.addFormLink("number.rightAddOn", "options.passed.number.cut.overview", null, passedNumberCutOverviewCont, Link.LINK);
		passedNumberCutOverviewButton.setIconLeftCSS("o_icon o_icon_preview");
		passedNumberCutOverviewCont.setElementCssClass("o_block_move_up");
		passedNumberCutOverviewCont.contextPut("linkId", "number.rightAddOn");
		String[] numberCutOverviewArgs = new String[] {
			Integer.toString(infos.getNumOfMandatoryPassedElements() + infos.getNumOfOptionalPassedElements()),
			Integer.toString(infos.getNumOfMandatoryPassedElements()),
			Integer.toString(infos.getNumOfOptionalPassedElements())
		};
		passedNumberCutOverviewCont.contextPut("msg", translate("options.passed.number.cut.explain", numberCutOverviewArgs));
		
		SelectionValues passedByScoreSV = new SelectionValues();
		passedByScoreSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_POINTS, translate("options.passed.by.points")));
		passedByScoreEl = uifactory.addCheckboxesVertical("options.passed.by.score", settingsCont, passedByScoreSV.keys(), passedByScoreSV.values(), 1);
		passedByScoreEl.addActionListener(FormEvent.ONCHANGE);
		passedByScoreEl.setEnabled(editable);
		passedByScoreEl.setVisible(passedEnableEl.isOn());
		passedByScoreEl.select(STCourseNode.CONFIG_PASSED_POINTS, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS));
		
		// Score cut value
		String passedPointsCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_POINTS_CUT)
				? String.valueOf(moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_POINTS_CUT, 1))
				: null;
		passedPointsCutEl = uifactory.addTextElement("options.passed.points.cut", "options.passed.points.cut", 10, passedPointsCut, settingsCont);
		passedPointsCutEl.setCheckVisibleLength(true);
		passedPointsCutEl.setElementCssClass("form-inline");
		passedPointsCutEl.setDisplaySize(10);
		passedPointsCutEl.setEnabled(editable);
		passedPointsCutEl.setTextAddOn("options.passed.to.pass");
		
		passedPointsCutOverviewCont = uifactory.addCustomFormLayout("points.rightAddOn.cont", null, page, settingsCont);
		passedPointsCutOverviewButton = uifactory.addFormLink("points.rightAddOn", "options.passed.points.cut.overview", null, passedPointsCutOverviewCont, Link.LINK);
		passedPointsCutOverviewButton.setIconLeftCSS("o_icon o_icon_preview");
		passedPointsCutOverviewCont.setElementCssClass("o_block_move_up");
		passedPointsCutOverviewCont.contextPut("linkId", "points.rightAddOn");
		
		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			settingsCont.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
		
		updateUI();
	}
	
	/**
	 * Set a default value if the feature is enabled
	 */
	private void updatePassedUI() {
		boolean passedEnabled = passedEnableEl.isOn();
		passedByProgressEl.setVisible(passedEnabled);
		passedByPassedEl.setVisible(passedEnabled);
		passedByPassedEnableEl.setVisible(passedEnabled);
		passedByScoreEl.setVisible(passedEnabled);
		
		boolean passedByPassedEnabled = passedByPassedEnableEl.isAtLeastSelected(1);				
		passedByPassedEl.setVisible(passedEnabled && passedByPassedEnabled);
			
		if(passedEnabled &&  passedByProgressEl.getSelectedKeys().isEmpty() 
				&& (!passedByPassedEl.isVisible() || !passedByPassedEl.isOneSelected())
				&& passedByScoreEl.getSelectedKeys().isEmpty() ) {
			passedByProgressEl.select(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		}
	}

	private void updateUI() {
		scoreEl.setVisible(scoreEnableEl.isOn());
		boolean weighted = scoreEnableEl.isOn() && scoreEl.isOneSelected()
				&& STCourseNode.CONFIG_SCORE_VALUE_SUM_WEIGHTED.equals(scoreEl.getSelectedKey());
		String cutI18n = weighted ? "options.passed.points.cut.weighted" : "options.passed.points.cut";
		passedPointsCutEl.setLabel(cutI18n, null);
		
		boolean passedEnabled = passedEnableEl.isOn();
		passedByProgressElCont.setVisible(passedEnabled);
		passedEnableInfosEl.setVisible(passedEnabled);
		passedLabelEl.setVisible(passedEnabled);
		
		passedByPassedEnableEl.setVisible(passedEnabled);
		boolean passedByPassedEnabled = passedByPassedEnableEl.isAtLeastSelected(1);
		passedByPassedEl.setVisible(passedEnabled && passedByPassedEnabled);
		
		boolean passedNumber = passedEnabled && passedByPassedEnabled
				&& passedByPassedEl.isOneSelected() && STCourseNode.CONFIG_PASSED_NUMBER.equals(passedByPassedEl.getSelectedKey());
		passedNumberCutEl.setVisible(passedNumber);
		passedNumberCutOverviewCont.setVisible(passedEnabled);
		
		String cutExplain = weighted
				? translate("options.passed.points.cut.explain.weighted", AssessmentHelper.getRoundedScore(infos.getWeightedMaxScore()))
				: translate("options.passed.points.cut.explain", AssessmentHelper.getRoundedScore(infos.getMaxScore()));
		passedPointsCutOverviewCont.contextPut("msg", cutExplain);
		
		boolean passedPoints = passedEnabled && passedByScoreEl.isKeySelected(STCourseNode.CONFIG_PASSED_POINTS);
		passedPointsCutEl.setVisible(passedPoints);
		passedPointsCutOverviewCont.setVisible(passedEnabled);
		
		settingsCont.setElementCssClass(null);
		if (!mandatoryNodesAvailable && passedByProgressEl.isKeySelected(STCourseNode.CONFIG_PASSED_PROGRESS)) {
			setFormTranslatedWarning(translate("error.passed.progress.only.optional"));
			settingsCont.setElementCssClass("o_block_top");
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == passedEnableEl) {
			updatePassedUI();
			updateUI();
		} else if (source == scoreEnableEl || source == scoreEl
				|| source == passedByProgressEl || source == passedByPassedEnableEl
				|| source == passedByPassedEl || source == passedByScoreEl) {
			updateUI();
		} else if(passedByProgressButton == source) {
			doOpenOverviewCourseElements(ureq, PROGRESS_OPTIONS);
		} else if(passedNumberCutOverviewButton == source) {
			doOpenOverviewCourseElements(ureq, PASSED_OPTIONS);
		} else if(passedPointsCutOverviewButton == source) {
			doOpenOverviewCourseElements(ureq, SCORE_OPTIONS);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(passedNumberCutOverviewButton != fiSrc && passedPointsCutOverviewButton != fiSrc && passedByProgressButton != fiSrc) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resetDataWizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					fireEvent(ureq, Event.CHANGED_EVENT);
				}
				cleanUp();
			}
		} else if(source == overviewCtrl) {
			overviewCmc.deactivate();
			cleanUp();
		} else if(source == overviewCmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(resetDataWizardCtrl);
		removeControllerListener(overviewCtrl);
		removeAsListenerAndDispose(overviewCmc);
		resetDataWizardCtrl = null;
		overviewCmc = null;
		overviewCtrl = null;
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
		AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		IdentityEnvironment identityEnv = new IdentityEnvironment(getIdentity(), ureq.getUserSession().getRoles());
		CourseEnvironment courseEnvironment = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
		UserCourseEnvironmentImpl coachCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnvironment);
		
		RepositoryEntry courseEntry =  courseEnvironment.getCourseGroupManager().getCourseEntry();
		String rootNodeIdent = courseEnvironment.getRunStructure().getRootNode().getIdent();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, rootNodeIdent, null, secCallback);
		long numAssessedIdentities = assessmentToolManager.countAssessedIdentities(getIdentity(), params);
		
		if (numAssessedIdentities <= 0) {
			doSave();
			return;
		}
		
		ResetDataContext dataContext = new ResetDataContext(courseEntry);
		dataContext.setResetParticipants(ResetParticipants.all);
		dataContext.setResetCourse(ResetCourse.elements);
		dataContext.setResetEmptyNodes(true);
		dataContext.setCourseNodes(List.of());
		
		ResetWizardContext wizardContext = new ResetWizardContext(getIdentity(), dataContext, coachCourseEnv, secCallback, false, true, false, false);
		wizardContext.setCurrent(ResetDataStep.courseElements);
		
		resetDataWizardCtrl = new StepsMainRunController(ureq, getWindowControl(),
				new ResetDataCancelStep(ureq, wizardContext),
				getResetCallback(dataContext, secCallback),
				getCancelCallback(),
				translate("assessment.reset.title"),
				"");
		listenTo(resetDataWizardCtrl);
		getWindowControl().pushAsModalDialog(resetDataWizardCtrl.getInitialComponent());
	}
	
	private StepRunnerCallback getResetCallback(ResetDataContext dataContext, AssessmentToolSecurityCallback secCallback) {
		return (uureq, wControl, runContext) -> {
			Object applyValue = runContext.get(ResetDataCancelStep.KEY_APPLY);
			if (applyValue instanceof Boolean apply && apply) {
				boolean saved = doSave();
				if (saved) {
					ResetDataFinishStepCallback finishCallback = new ResetDataFinishStepCallback(dataContext, secCallback);
					finishCallback.execute(uureq, wControl, runContext);
					return StepsMainRunController.DONE_MODIFIED;
				}
			} else {
				// Discard changes
				initForm(uureq);
			}
			
			return StepsMainRunController.DONE_UNCHANGED;
		};
	}
	
	private StepRunnerCallback getCancelCallback() {
		return (uureq, wControl, runContext) -> {
				// Mark dirty so that unsaved changes are not lost.
				markDirty();
				return StepsMainRunController.DONE_UNCHANGED;
			};
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doConfirmSetting(ureq);
	}
	
	private void doOpenOverviewCourseElements(UserRequest ureq, OverviewListOptions listOptions) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		overviewCtrl = new CourseOverviewController(ureq, getWindowControl(), course, listOptions);
		listenTo(overviewCtrl);
		
		overviewCmc = new CloseableModalController(getWindowControl(), translate("close"),
				overviewCtrl.getInitialComponent(), true, translate("overview.cours.elements"), true);
		listenTo(overviewCmc);
		overviewCmc.activate();
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

		if (scoreEnableEl.isOn()) {
			String selectedScoreKey = scoreEl.getSelectedKey();
			runConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, selectedScoreKey);
			editorConfig.setStringValue(STCourseNode.CONFIG_SCORE_KEY, selectedScoreKey);
		} else {
			runConfig.remove(STCourseNode.CONFIG_SCORE_KEY);
			editorConfig.remove(STCourseNode.CONFIG_SCORE_KEY);
		}
		
		boolean passedProgress = passedEnableEl.isOn() && passedByProgressEl.isKeySelected(STCourseNode.CONFIG_PASSED_PROGRESS);
		if (passedProgress) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_PROGRESS, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_PROGRESS);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_PROGRESS);
		}
		
		boolean passedAll = passedEnableEl.isOn() && passedByPassedEnableEl.isAtLeastSelected(1)
				&& passedByPassedEl.isOneSelected() && STCourseNode.CONFIG_PASSED_ALL.equals(passedByPassedEl.getSelectedKey());
		if (passedAll) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_ALL, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_ALL);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_ALL);
		}
		
		boolean passedNumber = passedEnableEl.isOn() && passedByPassedEnableEl.isAtLeastSelected(1)
				&& passedByPassedEl.isOneSelected() && STCourseNode.CONFIG_PASSED_NUMBER.equals(passedByPassedEl.getSelectedKey());
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
		
		boolean passedPoints = passedEnableEl.isOn() && passedByScoreEl.isKeySelected(STCourseNode.CONFIG_PASSED_POINTS);
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
		
		boolean passedEnabled = runConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS)
				|| runConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)
				|| runConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)
				|| runConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS);
		if (!passedEnabled) {
			runConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
		}
		
		CourseFactory.saveCourse(courseEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		return true;
	}
	
	private CourseElementsInfos loadElementsInfos(ICourse course) {
		CourseElementsInfos visitor = new CourseElementsInfos();
		new TreeVisitor(node -> {
			if(node instanceof CourseNode courseNode) {
				CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
				LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(course), courseNode);
				visitor.visit(courseNode, learningPathConfigs, assessmentConfig);
			}
		}, course.getRunStructure().getRootNode(), true)
			.visitAll();
		return visitor;
	}
	
	private static class CourseElementsInfos {
		private int numOfMandatoryElements = 0;
		private int numOfMandatoryPassedElements = 0;
		private int numOfOptionalPassedElements = 0;
		
		private double maxScore = 0.0d;
		private double weightedMaxScore = 0.0d;
		
		public int getNumOfMandatoryElements() {
			return numOfMandatoryElements;
		}
		
		public int getNumOfMandatoryPassedElements() {
			return numOfMandatoryPassedElements;
		}
		
		public int getNumOfOptionalPassedElements() {
			return numOfOptionalPassedElements;
		}
		
		public double getMaxScore() {
			return maxScore;
		}

		public double getWeightedMaxScore() {
			return weightedMaxScore;
		}
		
		public void visit(CourseNode courseNode, LearningPathConfigs learningPathConfigs, AssessmentConfig assessmentConfig) {
			AssessmentObligation obligation = learningPathConfigs.getObligation();
			if (AssessmentObligation.mandatory.equals(obligation)) {
				numOfMandatoryElements++;
			}
			
			if(assessmentConfig.isAssessable() && !assessmentConfig.ignoreInCourseAssessment()) {
				if(Mode.none != assessmentConfig.getPassedMode() && courseNode.getParent() != null) {
					if (AssessmentObligation.mandatory.equals(obligation)) {
						numOfMandatoryPassedElements++;
					} else if (AssessmentObligation.optional.equals(obligation)) {
						numOfOptionalPassedElements++;
					}
				}
				
				if (Mode.none != assessmentConfig.getScoreMode() && !(courseNode instanceof STCourseNode)) {
					maxScore += assessmentConfig.getMaxScore() != null ? assessmentConfig.getMaxScore().doubleValue(): 0.0d;
					weightedMaxScore  += assessmentConfig.getWeightedMaxScore() != null ? assessmentConfig.getWeightedMaxScore().doubleValue(): 0.0d;
				}
			}
		}
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
