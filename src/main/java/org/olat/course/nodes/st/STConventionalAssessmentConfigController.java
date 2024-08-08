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
package org.olat.course.nodes.st;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.run.scoring.FailedEvaluationType;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STConventionalAssessmentConfigController extends FormBasicController {
	
	private static final String[] EXAMPLE_PASSED = new String[]{"getPassed(\"69741247660309\")"};
	private static final String[] EXAMPLE_SCORE  = new String[]{"getScore(\"69741247660309\") * 2"};
	private static final String DELETED_NODE_IDENTIFYER = "deletedNode";
	private static final String KEY_SCORE_ENABLED = "score";
	private static final String KEY_GRADE_ENABLED = "grade";
	private static final String KEY_PASSED_ENABLED = "passed";
	
	private MultipleSelectionElement configEl;
	private FormLayoutContainer scoreConfigCont;
	private FormToggle scoreExpertToggleButton;
	private FormLayoutContainer scoreCont;
	private SingleSelection scoreTypeEl;
	private MultipleSelectionElement scoreNodesEl;
	private TextElement scoreExpessionEl;
	private FormLayoutContainer passedConfigCont;
	private FormToggle passedExpertToggleButton;
	private FormLayoutContainer passedCont;
	private SingleSelection passedTypeEl;
	private IntegerElement passedCutValueEl;
	private MultipleSelectionElement passedNodesEl;
	private SingleSelection passedNodesTypeEl;
	private TextElement numberOfNodesToPassEl;
	private TextElement passedExpressionEl;
	private SingleSelection failedTypeEl;
	private FormLayoutContainer gradeConfigCont;
	private FormLayoutContainer gradeCont;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;
	private StaticTextElement gradeMinMaxEl;
	private StaticTextElement gradePassedEl;
	
	private CloseableModalController cmc;
	private GradeScaleEditController gradeScaleCtrl;
	
	private final UserCourseEnvironment euce;
	private final RepositoryEntry courseEntry;
	private final STCourseNode courseNode;
	private final boolean root;
	private ScoreCalculator sc;
	private boolean scoreExpertMode;
	private boolean passedExpertMode;
	private List<CourseNode> assessableNodes;
	private GradeScale gradeScale;
	
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;

	public STConventionalAssessmentConfigController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment euce, STCourseNode courseNode, List<CourseNode> assessableNodes) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(MSEditFormController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		
		this.euce = euce;
		this.courseEntry = euce.getCourseEditorEnv().getCourseGroupManager().getCourseEntry();
		this.courseNode = courseNode;
		this.root = courseNode.getParent() == null;
		this.sc = courseNode.getScoreCalculator();
		this.scoreExpertMode = sc.isScoreExpertMode();
		this.passedExpertMode = sc.isPassedExpertMode();
		this.assessableNodes = assessableNodes;
		
		initForm(ureq);
		updateConfigUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("score.fieldset.title"));
		generalCont.setElementCssClass("o_sel_structure_score");
		generalCont.setFormContextHelp("manual_user/learningresources/Course_Element_Structure/#score");
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		SelectionValues configSV = new SelectionValues();
		configSV.add(SelectionValues.entry(KEY_SCORE_ENABLED, translate("form.configuration.score")));
		if (root && gradeModule.isEnabled()) {
			configSV.add(SelectionValues.entry(KEY_GRADE_ENABLED, translate("node.grade.enabled")));
		}
		configSV.add(SelectionValues.entry(KEY_PASSED_ENABLED, translate("form.configuration.passed")));
		configEl = uifactory.addCheckboxesVertical("form.configuration", generalCont, configSV.keys(), configSV.values(), 1);
		configEl.setElementCssClass("o_sel_score_settings");
		configEl.addActionListener(FormEvent.ONCHANGE);
		if (StringHelper.containsNonWhitespace(sc.getScoreExpression())) {
			configEl.select(KEY_SCORE_ENABLED, true);
		}
		if (StringHelper.containsNonWhitespace(sc.getPassedExpression())) {
			configEl.select(KEY_PASSED_ENABLED, true);
		}
		if (courseNode.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_KEY_GRADE_ENABLED)) {
			configEl.select(KEY_GRADE_ENABLED, true);
		}
		
		
		// SCORE
		scoreConfigCont = FormLayoutContainer.createCustomFormLayout("scoreConfig", getTranslator(), velocity_root + "/assessment_config_part.html");
		scoreConfigCont.setFormTitle(translate("form.configuration.score"));
		scoreConfigCont.setRootForm(mainForm);
		formLayout.add(scoreConfigCont);
		
		scoreExpertToggleButton = uifactory.addToggleButton("expert.mode.show", null, translate("on"), translate("off"), scoreConfigCont);
		
		scoreCont = FormLayoutContainer.createDefaultFormLayout("score", getTranslator());
		scoreCont.setElementCssClass("o_sel_score_config");
		scoreCont.setRootForm(mainForm);
		formLayout.add(scoreCont);
		
		String[] scoreTypeKeys = new String[] {
				ScoreCalculator.SCORE_TYPE_SUM,
				ScoreCalculator.SCORE_TYPE_AVG
		};
		String[] scoreTypeValues = new String[] {
				translate("scform.scoretype.sum"),
				translate("scform.scoretype.avg")
		};
			
		scoreTypeEl = uifactory.addRadiosHorizontal("scform.scoretype", scoreCont, scoreTypeKeys, scoreTypeValues);
		if (sc != null && sc.getScoreType() != null && !sc.getScoreType().equals(ScoreCalculator.SCORE_TYPE_NONE)) {
			scoreTypeEl.select(sc.getScoreType(), true);
		} else {
			scoreTypeEl.select(ScoreCalculator.SCORE_TYPE_SUM, true);
		}
		scoreTypeEl.addActionListener(FormEvent.ONCLICK);
		
		List<String> sumOfScoreNodes = (sc == null ? null : sc.getSumOfScoreNodes());
		scoreNodesEl = initNodeSelectionElement(scoreCont, "scform.scoreNodeIndents", sc, sumOfScoreNodes, assessableNodes);
		
		scoreExpessionEl = uifactory.addTextAreaElement("tscoreexpr", "scorecalc.score", 5000, 6, 45, true, false, sc.getScoreExpression(), scoreCont);
		scoreExpessionEl.setExampleKey("rules.example", EXAMPLE_SCORE);
		
		
		// PASSED
		passedConfigCont = FormLayoutContainer.createCustomFormLayout("passedConfig", getTranslator(), velocity_root + "/assessment_config_part.html");
		passedConfigCont.setFormTitle(translate("form.configuration.passed"));
		passedConfigCont.setRootForm(mainForm);
		formLayout.add(passedConfigCont);
		
		passedExpertToggleButton = uifactory.addToggleButton("expert.mode.show", null, translate("on"), translate("off"), passedConfigCont);

		passedCont = FormLayoutContainer.createDefaultFormLayout("passed", getTranslator());
		passedCont.setElementCssClass("o_sel_passed_config");
		passedCont.setRootForm(mainForm);
		formLayout.add(passedCont);
		
		String[] passedTypeKeys = new String[] {
				ScoreCalculator.PASSED_TYPE_CUTVALUE,
				ScoreCalculator.PASSED_TYPE_INHERIT
		};
		String[] passedTypeValues = new String[] {
				translate("scform.passedtype.cutvalue"),
				translate("scform.passedtype.inherit")
		};
		
		passedTypeEl = uifactory.addRadiosVertical("scform.passedtype", passedCont, passedTypeKeys, passedTypeValues);
		if (sc != null && sc.getPassedType() != null && !sc.getPassedType().equals(ScoreCalculator.PASSED_TYPE_NONE)) {
			passedTypeEl.select(sc.getPassedType(), true);
		} else {
			passedTypeEl.select(ScoreCalculator.PASSED_TYPE_CUTVALUE, true);
		}
		passedTypeEl.addActionListener(FormEvent.ONCLICK);
		
		int cutinitval = 0;
		if (sc != null) cutinitval = sc.getPassedCutValue();
		passedCutValueEl = uifactory.addIntegerElement("scform.passedCutValue", cutinitval, passedCont);
		passedCutValueEl.setDisplaySize(4);
		passedCutValueEl.setVisible(passedTypeEl.isVisible() && passedTypeEl.isSelected(0));
		passedCutValueEl.setMandatory(true);
				
		passedNodesEl = initNodeSelectionElement(
				passedCont, "scform.passedNodeIndents", sc, (sc == null ? null : sc.getPassedNodes()), assessableNodes
		);
		passedNodesEl.setVisible(passedTypeEl.isVisible() && passedTypeEl.isSelected(1));
		
		passedExpressionEl = uifactory.addTextAreaElement("tpassedexpr", "scorecalc.passed", 5000, 6, 45, true, false, sc.getPassedExpression(), passedCont);
		passedExpressionEl.setExampleKey("rules.example", EXAMPLE_PASSED);
		
		SelectionValues passedNodesPK = new SelectionValues();
		passedNodesPK.add(SelectionValues.entry(ScoreCalculator.PASSED_NODES_TYPE_ALL, translate("scform.passedtype.nodes.all")));
		passedNodesPK.add(SelectionValues.entry(ScoreCalculator.PASSED_NODES_TYPE_PARTIAL, translate("scform.passedtype.nodes.partial")));
		passedNodesTypeEl = uifactory.addRadiosVertical("scform.passedtype.nodes", passedCont, passedNodesPK.keys(), passedNodesPK.values());
		if (sc != null && StringHelper.containsNonWhitespace(sc.getPassedNodesType()) && passedNodesPK.containsKey(sc.getPassedNodesType())) {
			passedNodesTypeEl.select(sc.getPassedNodesType(), true);
		} else {
			passedNodesTypeEl.select(ScoreCalculator.PASSED_NODES_TYPE_ALL, true);
		}
		passedNodesTypeEl.addActionListener(FormEvent.ONCLICK);
		
		String numberOfNodesToPass = sc.getNumberOfNodesToPass() > 0 ? Integer.toString(sc.getNumberOfNodesToPass()) : "";
		numberOfNodesToPassEl = uifactory.addTextElement("scform.number.nodes.to.pass", 8, numberOfNodesToPass, passedCont);
		numberOfNodesToPassEl.setElementCssClass("form-inline");
		numberOfNodesToPassEl.setDisplaySize(8);
		numberOfNodesToPassEl.setTextAddOn("scform.number.nodes.to.pass.addon");
		
		String[] failedTypeKeys = new String[]{
				FailedEvaluationType.failedAsNotPassed.name(),
				FailedEvaluationType.failedAsNotPassedAfterEndDate.name()
		};
		String[] failedTypeValues = new String[]{
				translate(FailedEvaluationType.failedAsNotPassed.name()),
				translate(FailedEvaluationType.failedAsNotPassedAfterEndDate.name())
		};
		
		failedTypeEl = uifactory.addDropdownSingleselect("scform.failedtype", passedCont, failedTypeKeys, failedTypeValues, null);
		failedTypeEl.addActionListener(FormEvent.ONCLICK);
		FailedEvaluationType failedTypeValue = sc.getFailedType() == null ? FailedEvaluationType.failedAsNotPassed : sc.getFailedType();
		boolean failedSelected = false;
		for(String failedTypeKey:failedTypeKeys) {
			if(failedTypeKey.equals(failedTypeValue.name())) {
				failedTypeEl.select(failedTypeKey, true);
				failedSelected = true;
			}
		}
		if(!failedSelected) {
			failedTypeEl.select(failedTypeKeys[0], true);
		}
		
		
		// GRADES
		gradeConfigCont = FormLayoutContainer.createCustomFormLayout("gradeConfig", getTranslator(), velocity_root + "/assessment_config_part.html");
		gradeConfigCont.setFormTitle(translate("node.grade.enabled"));
		gradeConfigCont.setRootForm(mainForm);
		formLayout.add(gradeConfigCont);
		
		gradeCont = FormLayoutContainer.createDefaultFormLayout("grade", getTranslator());
		gradeCont.setRootForm(mainForm);
		formLayout.add(gradeCont);
		
		SelectionValues autoSV = new SelectionValues();
		autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
		gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", gradeCont, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
		gradeAutoEl.select(gradeAutoEl.getKey(0), true);
		
		gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
		gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", gradeCont);
		
		gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
		gradeScaleButtonsCont.setRootForm(mainForm);
		gradeCont.add(gradeScaleButtonsCont);
		gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
		gradeScaleEditLink.setElementCssClass("o_sel_grade_edit_scale");
		
		gradeMinMaxEl = uifactory.addStaticTextElement("score.min.max", "score.min.max", "", gradeCont);
		gradePassedEl = uifactory.addStaticTextElement("node.grade.passed", "form.passed", "", gradeCont);
		
		FormLayoutContainer buttonWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonWrapper", getTranslator());
		buttonWrapperCont.setElementCssClass("o_sel_score_buttons");
		buttonWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonWrapperCont);
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonWrapperCont.add(buttonCont);
		uifactory.addFormSubmitButton("submit", buttonCont);
	}
	
	/**
	 * Initializes the node selection form elements first check if the form has a
	 * selection on a node that has been deleted in since the last edition of this
	 * form. if so, set remember this to later add a dummy placeholder for the
	 * deleted node. We do not just ignore this since the form would look ok then
	 * to the user, the generated rule visible in the expert mode however would
	 * still be invalid. user must explicitly uncheck this deleted node reference.
	 * 
	 * @param elemId name of the generated form element
	 * @param scoreCalculator
	 * @param selectedNodeList List of course nodes that are preselected
	 * @param allNodesList List of all assessable course nodes
	 * @return StaticMultipleSelectionElement The configured form element
	 */
	private MultipleSelectionElement initNodeSelectionElement(FormItemContainer cont, String elemId, ScoreCalculator scoreCalculator,
			List<String> selectedNodeList, List<CourseNode> allNodesList) {
		
		boolean addDeletedNodeIdent = false;
		if (scoreCalculator != null && selectedNodeList != null) {
			for (Iterator<String> iter = selectedNodeList.iterator(); iter.hasNext();) {
				String nodeIdent = iter.next();
				boolean found = false;
				for (Iterator<CourseNode> nodeIter = allNodesList.iterator(); nodeIter.hasNext();) {
					CourseNode node = nodeIter.next();
					if (node.getIdent().equals(nodeIdent)) {
						found = true;
					}
				}
				if (!found) addDeletedNodeIdent = true;
			}
		}

		String[] nodeKeys = new String[allNodesList.size() + (addDeletedNodeIdent ? 1 : 0)];
		String[] nodeValues = new String[allNodesList.size() + (addDeletedNodeIdent ? 1 : 0)];
		for (int i = 0; i < allNodesList.size(); i++) {
			CourseNode courseNode = allNodesList.get(i);
			nodeKeys[i] = courseNode.getIdent();
			nodeValues[i] = courseNode.getShortName() + " (Id:" + courseNode.getIdent() + ")";
		}
		// add a deleted dummy node at last position
		if (addDeletedNodeIdent) {
			nodeKeys[allNodesList.size()] = DELETED_NODE_IDENTIFYER;
			nodeValues[allNodesList.size()] = translate("scform.deletedNode");
		}
		
		MultipleSelectionElement mse = uifactory.addCheckboxesVertical(elemId, cont, nodeKeys, nodeValues, 2);
		// preselect nodes from configuration
		if (scoreCalculator != null && selectedNodeList != null) {
			for (Iterator<String> iter = selectedNodeList.iterator(); iter.hasNext();) {
				String nodeIdent = iter.next();
				boolean found = false;
				for (Iterator<CourseNode> nodeIter = allNodesList.iterator(); nodeIter.hasNext();) {
					CourseNode node = nodeIter.next();
					if (node.getIdent().equals(nodeIdent)) {
						found = true;
					}
				}
				if (found) {
					mse.select(nodeIdent, true);
				} else {
					mse.select(DELETED_NODE_IDENTIFYER, true);
				}
			}
		}
		return mse;
	}
	
	private void updateConfigUI() {
		boolean scoreEnabled = configEl.isKeySelected(KEY_SCORE_ENABLED);
		
		if (configEl.getKeys().contains(KEY_GRADE_ENABLED)) {
			if (scoreEnabled) {
				configEl.setEnabled(KEY_GRADE_ENABLED, !configEl.isKeySelected(KEY_PASSED_ENABLED));
			} else {
				configEl.setEnabled(KEY_GRADE_ENABLED, false);
				configEl.select(KEY_GRADE_ENABLED, false);
			}
		}
		
		configEl.setEnabled(KEY_PASSED_ENABLED, !configEl.isKeySelected(KEY_GRADE_ENABLED));
		
		updateScoreUI();
		updatePassedUI();
		updateGradeUI();
	}
	
	private void updateScoreUI() {
		boolean scoreEnabled = configEl.isKeySelected(KEY_SCORE_ENABLED);
		
		scoreConfigCont.setVisible(scoreEnabled);
		if(scoreExpertMode) {
			scoreExpertToggleButton.toggleOn();	
		} else {
			scoreExpertToggleButton.toggleOff();
		}
		scoreExpertToggleButton.setVisible(scoreEnabled);

		scoreCont.setVisible(scoreEnabled);

		scoreTypeEl.setVisible(scoreEnabled && !scoreExpertMode);
		scoreNodesEl.setVisible(scoreEnabled && !scoreExpertMode);
		if (!scoreNodesEl.isVisible()) {
			scoreNodesEl.clearError();
		}
		
		scoreExpessionEl.setVisible(scoreEnabled && scoreExpertMode);
	}
	
	private void updatePassedUI() {
		boolean passedEnabled = configEl.isKeySelected(KEY_PASSED_ENABLED);
		
		passedConfigCont.setVisible(passedEnabled);
		if(passedExpertMode) {
			passedExpertToggleButton.toggleOn();
		} else {
			passedExpertToggleButton.toggleOff();
			
		}
		passedExpertToggleButton.setVisible(passedEnabled);
		passedCont.setVisible(passedEnabled);
		
		boolean passedCut = passedTypeEl.isOneSelected() && passedTypeEl.isKeySelected(ScoreCalculator.PASSED_TYPE_CUTVALUE);
		passedTypeEl.setVisible(passedEnabled && !passedExpertMode);
		passedCutValueEl.setVisible(passedEnabled && !passedExpertMode && passedCut);
		passedNodesEl.setVisible(passedEnabled && !passedExpertMode && !passedCut);
		if (!passedNodesEl.isVisible()) {
			passedNodesEl.clearError();
		}
		
		passedNodesTypeEl.setVisible(passedEnabled && !passedExpertMode && !passedCut);
		boolean partialNodes = passedNodesTypeEl.isVisible() && passedNodesTypeEl.isOneSelected()
				&& ScoreCalculator.PASSED_NODES_TYPE_PARTIAL.equals(passedNodesTypeEl.getSelectedKey());
		numberOfNodesToPassEl.setVisible(partialNodes);
		
		passedExpressionEl.setVisible(passedEnabled && passedExpertMode);
	}
	
	private void updateGradeUI() {
		boolean gradeEnabled = configEl.isKeySelected(KEY_GRADE_ENABLED);
		
		gradeConfigCont.setVisible(gradeEnabled);
		gradeCont.setVisible(gradeEnabled);
		
		gradeAutoEl.setVisible(gradeEnabled);
		String gradeScaleText = gradeScale == null
				? translate("node.grade.scale.not.available")
				: GradeUIFactory.translateGradeSystemName(getTranslator(), gradeScale.getGradeSystem());
		gradeScaleEl.setValue(gradeScaleText);
		gradeScaleEl.setVisible(gradeEnabled);
		gradeScaleButtonsCont.setVisible(gradeEnabled);
		gradeScaleEditLink.setVisible(gradeEnabled);
		
		String scoreMinMax = gradeScale != null
				? AssessmentHelper.getMinMax(getTranslator(), gradeScale.getMinScore().floatValue(), gradeScale.getMaxScore().floatValue())
				: null;
		gradeMinMaxEl.setValue(scoreMinMax);
		gradeMinMaxEl.setVisible(gradeEnabled && scoreMinMax != null);
		
		GradeScoreRange minRange = gradeService.getMinPassedGradeScoreRange(gradeScale, getLocale());
		gradePassedEl.setValue(GradeUIFactory.translateMinPassed(getTranslator(), minRange));
		gradePassedEl.setVisible(gradeEnabled && minRange != null);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
				updateGradeUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(gradeScaleCtrl);
		removeAsListenerAndDispose(cmc);
		gradeScaleCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc != gradeScaleEditLink) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == configEl) {
			updateConfigUI();
		} else if (source == scoreExpertToggleButton) {
			scoreExpertMode = scoreExpertToggleButton.isOn();
			updateScoreUI();
		} else if (source == passedExpertToggleButton) {
			passedExpertMode = passedExpertToggleButton.isOn();
			updatePassedUI();
		} else if (source == passedTypeEl || source == passedNodesTypeEl) {
			updatePassedUI();
		} else if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		scoreNodesEl.clearError();
		if (scoreNodesEl.isVisible()) {
			if (scoreNodesEl.getSelectedKeys().isEmpty()) {
				scoreNodesEl.setErrorKey("scform.scoreNodeIndents.error");
				allOk &= false;
			} else if (scoreNodesEl.getSelectedKeys().contains(DELETED_NODE_IDENTIFYER)) {
				scoreNodesEl.setErrorKey("scform.deletedNode.error");
				allOk &= false;
			}
		}
		
		scoreExpessionEl.clearError();
		scoreExpessionEl.setExampleKey("rules.example", EXAMPLE_SCORE);
		if (scoreExpessionEl.isVisible()) {
			String scoreExp = scoreExpessionEl.getValue().trim();
			if (StringHelper.containsNonWhitespace(scoreExp)) {
				
				CourseEditorEnv cev = euce.getCourseEditorEnv();
				ConditionExpression ce = new ConditionExpression("score",scoreExp);
				ConditionErrorMessage[] cerrmsgs = cev.validateConditionExpression(ce);
				
				if (cerrmsgs != null && cerrmsgs.length>0) {
					setError(ureq, scoreExpessionEl, cerrmsgs);
					allOk &= false;
				}
			}
		}
		
		passedNodesEl.clearError();
		if (passedNodesEl.isVisible()) {
			if (passedTypeEl.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_INHERIT)) {
				if (passedNodesEl.getSelectedKeys().isEmpty()) {
					passedNodesEl.setErrorKey("scform.passedNodeIndents.error");
					allOk &= false;
				} else if (passedNodesEl.getSelectedKeys().contains(DELETED_NODE_IDENTIFYER)) {
					passedNodesEl.setErrorKey("scform.deletedNode.error");
					allOk &= false;
				}
			} else if (passedTypeEl.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_CUTVALUE)) {
				if (!configEl.isKeySelected(KEY_SCORE_ENABLED)) {
					passedTypeEl.setErrorKey("scform.passedType.error");
					allOk &= false;
				}
			}
		}
		
		passedExpressionEl.clearError();
		passedExpressionEl.setExampleKey("rules.example", EXAMPLE_PASSED);
		if (passedExpressionEl.isVisible()) {
			String passedExp = passedExpressionEl.getValue().trim();
			if (StringHelper.containsNonWhitespace(passedExp)) {
				
				CourseEditorEnv cev = euce.getCourseEditorEnv();
				ConditionExpression ce = new ConditionExpression("passed",passedExp);
				ConditionErrorMessage[] cerrmsgs = cev.validateConditionExpression(ce);
				
				if (cerrmsgs != null && cerrmsgs.length>0) {
					setError(ureq, passedExpressionEl,  cerrmsgs);
					allOk &= false;
				}
			}
		}
		
		numberOfNodesToPassEl.clearError();
		if(numberOfNodesToPassEl.isVisible()) {
			int numOfNodes = passedNodesEl.getSelectedKeys().size();
			if(StringHelper.containsNonWhitespace(numberOfNodesToPassEl.getValue())) {
				try {
					int numOfNodesToPass = Integer.parseInt(numberOfNodesToPassEl.getValue());
					if(numOfNodesToPass <= 0) {
						numberOfNodesToPassEl.setErrorKey("form.error.positive.integer");
						allOk &= false;
					} else if(numOfNodesToPass > numOfNodes) {
						numberOfNodesToPassEl.setErrorKey("scform.number.nodes.to.pass.error");
						allOk &= false;
					}
				} catch(Exception e) {
					numberOfNodesToPassEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} else {
				numberOfNodesToPassEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}
	
	private void setError(UserRequest ureq, FormItem fi, ConditionErrorMessage[] cem) {
		Translator conditionTranslator = Util.createPackageTranslator(Condition.class, ureq.getLocale());
		
		//Set the generic error message...
		fi.setErrorKey("rules.error", conditionTranslator.translate(cem[0].getErrorKey(), cem[0].getErrorKeyParams()));
		if (cem[0].getSolutionMsgKey() != null && !"".equals(cem[0].getSolutionMsgKey())) {
			// ...and a hint or example to clarify the error message
			fi.setExampleKey("rules.error", new String[] {
					conditionTranslator.translate(cem[0].getSolutionMsgKey(), cem[0].getErrorKeyParams()) });
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		updateScoreCalculator();
		courseNode.setScoreCalculator(sc);
		
		boolean gradeEnabled = configEl.isKeySelected(KEY_GRADE_ENABLED);
		if (gradeEnabled) {
			courseNode.getModuleConfiguration().setBooleanEntry(STCourseNode.CONFIG_KEY_GRADE_ENABLED, true);
		} else {
			courseNode.getModuleConfiguration().remove(STCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeService.deleteGradeScale(courseEntry, courseNode.getIdent());
			gradeScale = null;
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void updateScoreCalculator() {
		boolean scoreEnabled = configEl.isKeySelected(KEY_SCORE_ENABLED);
		boolean passedEnabled = configEl.isKeySelected(KEY_PASSED_ENABLED);
		if (!scoreEnabled && !passedEnabled) {
			sc = null;
			return;
		}
		
		if (sc == null) {
			sc = new ScoreCalculator();
		}

		// 1) score configuration
		if (scoreEnabled) {
			if (scoreExpertMode) {
				sc.setScoreExpertMode(true);
				String scoreExp = scoreExpessionEl.getValue().trim();
				scoreExp = StringHelper.containsNonWhitespace(scoreExp)? scoreExp: null;
				sc.setScoreExpression(scoreExp);
				// Reset easy mode
				sc.setScoreType(ScoreCalculator.SCORE_TYPE_NONE);
				sc.setSumOfScoreNodes(null);
			} else {
				sc.setScoreExpertMode(false);
				String scoreTypeSelection = scoreTypeEl.isOneSelected()
						? scoreTypeEl.getSelectedKey()
						: ScoreCalculator.SCORE_TYPE_SUM;
				sc.setScoreType(scoreTypeSelection);
				sc.setSumOfScoreNodes(new ArrayList<>(scoreNodesEl.getSelectedKeys()));
				sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
			}
		} else {
			sc.setScoreExpertMode(false);
			sc.setScoreType(ScoreCalculator.SCORE_TYPE_NONE);
			sc.setSumOfScoreNodes(null);
			sc.setScoreExpression(null);
		}
		
		// 2) passed configuration
		if (passedEnabled) {
			if (passedExpertMode) {
				sc.setPassedExpertMode(true);
				String passedExp = passedExpressionEl.getValue().trim();
				passedExp = StringHelper.containsNonWhitespace(passedExp)? passedExp: null;
				sc.setPassedExpression(passedExp);
				// Reset easy mode
				sc.setPassedType(ScoreCalculator.PASSED_TYPE_NONE);
				sc.setPassedCutValue(0);
				sc.setPassedNodes(null);
				sc.setPassedNodesType(null);
				sc.setNumberOfNodesToPass(-1);
			} else {
				sc.setPassedExpertMode(false);
				if (passedTypeEl.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_CUTVALUE)) {
					sc.setPassedType(ScoreCalculator.PASSED_TYPE_CUTVALUE);
					sc.setPassedCutValue(passedCutValueEl.getIntValue());
					sc.setPassedNodesType(null);
					sc.setNumberOfNodesToPass(-1);
				} else if (passedTypeEl.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_INHERIT)) {
					sc.setPassedType(ScoreCalculator.PASSED_TYPE_INHERIT);
					sc.setPassedNodes(new ArrayList<>(passedNodesEl.getSelectedKeys()));
					
					String passedNodeType = passedNodesTypeEl.getSelectedKey();
					sc.setPassedNodesType(passedNodeType);
					if(ScoreCalculator.PASSED_NODES_TYPE_PARTIAL.equals(passedNodeType) && StringHelper.isLong(numberOfNodesToPassEl.getValue())) {
						sc.setNumberOfNodesToPass(Integer.parseInt(numberOfNodesToPassEl.getValue()));
					} else {
						sc.setNumberOfNodesToPass(-1);
					}
				}
				String passedExp = sc.getPassedExpressionFromEasyModeConfiguration();
				sc.setPassedExpression(passedExp);
			}
			sc.setFailedType(FailedEvaluationType.valueOf(failedTypeEl.getSelectedKey()));
		} else {
			sc.setPassedExpertMode(false);
			sc.setPassedType(ScoreCalculator.PASSED_TYPE_NONE);
			sc.setPassedExpression(null);
			sc.setNumberOfNodesToPass(-1);
		}
		
		if (sc.getScoreExpression() == null && sc.getPassedExpression() == null) {
			sc = null;
		}
	}
	
	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), courseEntry, courseNode.getIdent(),
				null, null, true, true);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
