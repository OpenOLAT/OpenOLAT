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

package org.olat.course.nodes.ms;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides a FlexiForm for the assesment settings dialog, including custom
 * rules for when to show which dialog element.
 * 
 * @author twuersch
 * 
 */
public class MSEditFormController extends FormBasicController {

	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final ModuleConfiguration modConfig;
	private final boolean ignoreInCourseAssessmentAvailable;
	
	private CloseableModalController cmc;
	private GradeScaleEditController gradeScaleCtrl;

	/** whether score will be awarded or not. */
	private MultipleSelectionElement scoreGranted;

	/** Dropdown for choosing whether pass/fail will be displayed or not. */
	private MultipleSelectionElement displayPassed;

	/**
	 * whether pass and fail will be decided automatically
	 * or manually.
	 */
	private SingleSelection displayType;

	/** whether results will be commented individually. */
	private MultipleSelectionElement commentFlag;
	/** whether results will be commented individually. */
	private MultipleSelectionElement individualAssessmentDocsFlag;

	/** Text input element for the minimum score. */
	private TextElement minVal;

	/** Text input element for the maximum score. */
	private TextElement maxVal;
	
	/** Grade */
	private SpacerElement gradeSpacer;
	private MultipleSelectionElement gradeEnabledEl;
	private SingleSelection gradeAutoEl;
	private StaticTextElement gradeScaleEl;
	private FormLayoutContainer gradeScaleButtonsCont;
	private FormLink gradeScaleEditLink;

	/** Text input element for the passing score. */
	private TextElement cutVal;
	
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private SpacerElement ignoreInCourseAssessmentSpacer;

	/** Rich text input element for a notice to all users. */
	private RichTextElement infotextUser;

	/** Rich text input element for a notice to all tutors. */
	private RichTextElement infotextCoach;

	/** The keys for true / false dropdowns. */
	private String[] trueFalseKeys;

	/** The keys for manual/automatic scoring dropdown. */
	private String[] passedTypeValues;

	private  static final String scoreRex = "^[0-9]+(\\.[0-9]+)?$";

	private final String title;
	private final String helpUrl;
	private GradeScale gradeScale;
	
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	
	public MSEditFormController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, NodeAccessType nodeAccessType) {
		this(ureq, wControl, courseEntry, courseNode, nodeAccessType, null, null);
	}
	
	public MSEditFormController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, NodeAccessType nodeAccessType, String title, String helpUrl) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.modConfig = courseNode.getModuleConfiguration();
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		this.title = title;
		this.helpUrl = helpUrl;
		trueFalseKeys = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		passedTypeValues = new String[] { translate("form.passedtype.cutval"), translate("form.passedtype.manual") };
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (StringHelper.containsNonWhitespace(title)) {
			setFormTitle("form.title", new String[] {title});
		}
		if (StringHelper.containsNonWhitespace(helpUrl)) {
			setFormContextHelp(helpUrl);
		}
		formLayout.setElementCssClass("o_sel_course_ms_form");

		// Create the "score granted" field...
		scoreGranted = uifactory.addCheckboxesHorizontal("form.score", formLayout, new String[]{"xx"}, new String[]{null});
		scoreGranted.addActionListener(FormEvent.ONCLICK);
		Boolean sf = (Boolean) modConfig.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		scoreGranted.select("xx", sf == null ? false : sf.booleanValue());
		scoreGranted.setElementCssClass("o_sel_course_ms_score");

		// ...minimum value...
		Float min = (Float) modConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		if (min == null) {
			min = Float.valueOf(0);
		}
		minVal = uifactory.addTextElement("form.min", "form.min", 8, min.toString(), formLayout);
		minVal.setDisplaySize(5);
		minVal.setRegexMatchCheck(scoreRex, "form.error.wrongFloat");
		minVal.setElementCssClass("o_sel_course_ms_min_val");
		
		Float max = (Float) modConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if (max == null) {
			max = Float.valueOf(0);
		}
		// ...and maximum value input.
		maxVal = uifactory.addTextElement("form.max", "form.max", 8, max.toString(), formLayout);
		maxVal.setDisplaySize(5);
		maxVal.setRegexMatchCheck(scoreRex, "form.error.wrongFloat");
		maxVal.setElementCssClass("o_sel_course_ms_max_val");
		
		if (gradeModule.isEnabled()) {
			gradeSpacer = uifactory.addSpacerElement("spacer0", formLayout, false);
			
			gradeEnabledEl = uifactory.addCheckboxesHorizontal("node.grade.enabled", formLayout, new String[]{"xx"}, new String[]{null});
			gradeEnabledEl.addActionListener(FormEvent.ONCLICK);
			boolean gradeEnabled = modConfig.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			gradeEnabledEl.select("xx", gradeEnabled);
			
			SelectionValues autoSV = new SelectionValues();
			autoSV.add(new SelectionValue(Boolean.FALSE.toString(), translate("node.grade.auto.manually"), translate("node.grade.auto.manually.desc"), null, null, true));
			autoSV.add(new SelectionValue(Boolean.TRUE.toString(), translate("node.grade.auto.auto"), translate("node.grade.auto.auto.desc"), null, null, true));
			gradeAutoEl = uifactory.addCardSingleSelectHorizontal("node.grade.auto", formLayout, autoSV.keys(), autoSV.values(), autoSV.descriptions(), autoSV.icons());
			gradeAutoEl.select(Boolean.valueOf(modConfig.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO)).toString(), true);
			
			gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			gradeScaleEl = uifactory.addStaticTextElement("node.grade.scale.not", "grade.scale", "", formLayout);
			
			gradeScaleButtonsCont = FormLayoutContainer.createButtonLayout("gradeButtons", getTranslator());
			gradeScaleButtonsCont.setRootForm(mainForm);
			formLayout.add(gradeScaleButtonsCont);
			gradeScaleEditLink = uifactory.addFormLink("grade.scale.edit", gradeScaleButtonsCont, "btn btn-default");
		}
		
		uifactory.addSpacerElement("spacer1", formLayout, false);

		// Create the "display passed / failed"
		displayPassed = uifactory.addCheckboxesHorizontal("form.passed", formLayout, new String[]{"xx"}, new String[]{null});
		displayPassed.addActionListener(FormEvent.ONCLICK);
		Boolean pf = (Boolean) modConfig.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if (pf == null) pf = Boolean.TRUE;
		displayPassed.select("xx", pf);

		// ...the automatic / manual dropdown (note that TRUE means automatic and
		// FALSE means manually)...
		Float cut = (Float) modConfig.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		displayType = uifactory.addRadiosVertical("form.passed.type", formLayout, trueFalseKeys, passedTypeValues);
		displayType.addActionListener(FormEvent.ONCLICK);
		displayType.setElementCssClass("o_sel_course_ms_display_type");

		displayType.select(trueFalseKeys[1], true);
		if (cut != null) {
			displayType.select(trueFalseKeys[0], true);
		}

		// ...and the passing grade input field.
		if (cut == null) cut = Float.valueOf(0);
		cutVal = uifactory.addTextElement("form.cut", "form.cut", 8, cut.toString(), formLayout);
		cutVal.setDisplaySize(5);
		cutVal.setRegexMatchCheck(scoreRex, "form.error.wrongFloat");
		cutVal.setElementCssClass("o_sel_course_ms_cut_val");

		uifactory.addSpacerElement("spacer2", formLayout, false);
		
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", formLayout,
				new String[] { "xx" }, new String[] { null });
		boolean ignoreInCourseAssessment = modConfig.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		ignoreInCourseAssessmentEl.select(ignoreInCourseAssessmentEl.getKey(0), ignoreInCourseAssessment);
		
		ignoreInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer3", formLayout, false);
		
		// Create the "individual comment" dropdown.
		commentFlag = uifactory.addCheckboxesHorizontal("form.comment", formLayout, new String[]{"xx"}, new String[]{null});
		Boolean cf = (Boolean) modConfig.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		if (cf == null) cf = Boolean.TRUE;
		commentFlag.select("xx", cf.booleanValue());
		
		individualAssessmentDocsFlag = uifactory.addCheckboxesHorizontal("form.individual.assessment.docs", formLayout, new String[]{"xx"}, new String[]{null});
		boolean docsCf = modConfig.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
		if(docsCf) {
			individualAssessmentDocsFlag.select("xx", true);
		}

		uifactory.addSpacerElement("spacer4", formLayout, false);

		// Create the rich text fields.
		String infoUser = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		if (infoUser == null) infoUser = "";
		infotextUser = uifactory.addRichTextElementForStringDataMinimalistic("infotextUser", "form.infotext.user", infoUser, 10, -1,
				formLayout, getWindowControl());

		String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		if (infoCoach == null) infoCoach = "";
		infotextCoach = uifactory.addRichTextElementForStringDataMinimalistic("infotextCoach", "form.infotext.coach", infoCoach, 10, -1,
				formLayout, getWindowControl());

		

		// Create submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		update(ureq);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (gradeScaleCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
				update(ureq);
				flc.setDirty(true);
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
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == gradeScaleEditLink) {
			doEditGradeScale(ureq);
		} else {
			update(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void update(UserRequest ureq) {
		minVal.setVisible(scoreGranted.isSelected(0));
		maxVal.setVisible(scoreGranted.isSelected(0));
		minVal.setMandatory(minVal.isVisible());
		maxVal.setMandatory(maxVal.isVisible());
		
		if (gradeEnabledEl != null) {
			gradeSpacer.setVisible(scoreGranted.isSelected(0));
			gradeEnabledEl.setVisible(scoreGranted.isSelected(0));
			gradeAutoEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
			String gradeScaleText = gradeScale == null
					? translate("node.grade.scale.not.available")
					: translate("node.grade.scale.available");
			gradeScaleEl.setValue(gradeScaleText);
			gradeScaleEl.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
			gradeScaleButtonsCont.setVisible(gradeEnabledEl.isVisible() && gradeEnabledEl.isAtLeastSelected(1));
		}
		
		boolean gradeDisable = gradeEnabledEl == null || !gradeEnabledEl.isVisible() || !gradeEnabledEl.isAtLeastSelected(1);
		displayType.setVisible(displayPassed.isSelected(0) && gradeDisable);
		cutVal.setVisible(displayType.isVisible() && displayType.isSelected(0));
		cutVal.setMandatory(cutVal.isVisible());
		
		boolean ignoreInScoreVisible = ignoreInCourseAssessmentAvailable
				&& (scoreGranted.isSelected(0) || displayPassed.isSelected(0));
		ignoreInCourseAssessmentEl.setVisible(ignoreInScoreVisible);
		ignoreInCourseAssessmentSpacer.setVisible(ignoreInScoreVisible);
		
		validateFormLogic(ureq);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		
		// coach info text
		if (infotextCoach.getValue().length() > 4000) {
			infotextCoach.setErrorKey("input.toolong", new String[] {"4000"});
			return false;
		} else {
			infotextCoach.clearError();
		}
		// user info text
		if (infotextUser.getValue().length() > 4000) {
			infotextUser.setErrorKey("input.toolong", new String[] {"4000"});
			return false;
		} else {
			infotextUser.clearError();
		}
		// score flag
		if (scoreGranted.isSelected(0)) {
			if (!minVal.getValue().matches(scoreRex)) {
				minVal.setErrorKey("form.error.wrongFloat", null);
				return false;
			} else {
				minVal.clearError();
			}
			if (!maxVal.getValue().matches(scoreRex)) {
				maxVal.setErrorKey("form.error.wrongFloat", null);
				return false;
			} else if (Float.parseFloat(minVal.getValue()) > Float.parseFloat(maxVal.getValue())) {
				maxVal.setErrorKey("form.error.minGreaterThanMax", null);
				return false;
			} else {
				maxVal.clearError();
			}
		}
		// display flag		
		displayType.clearError();
		if (displayPassed.isSelected(0) && displayType.isSelected(0)) {
			if (Boolean.valueOf(displayType.getSelectedKey()).booleanValue() && !scoreGranted.isSelected(0)) {
				displayType.setErrorKey("form.error.cutButNoScore", null);
				return false;
			}
			if (!cutVal.getValue().matches(scoreRex)) {
				cutVal.setErrorKey("form.error.wrongFloat", null);
				return false;
			} else if (Float.parseFloat(cutVal.getValue()) < Float.parseFloat(minVal.getValue())
					|| Float.parseFloat(cutVal.getValue()) > Float.parseFloat(maxVal.getValue())) {
				cutVal.setErrorKey("form.error.cutOutOfRange", null);
				return false;
			} else {
				cutVal.clearError();
			}
		}
		return true;
	}

	/**
	 * Sets this form to be write-protected.
	 * 
	 * @param displayOnly
	 */
	public void setDisplayOnly(boolean displayOnly) {
		Map<String, FormItem> formItems = flc.getFormComponents();
		for (String formItemName : formItems.keySet()) {
			formItems.get(formItemName).setEnabled(!displayOnly);
		}
		if (gradeScaleButtonsCont != null) {
			gradeScaleButtonsCont.setVisible(!displayOnly);
		}
	}

	public void updateModuleConfiguration(ModuleConfiguration moduleConfiguration) {
		// mandatory score flag
		Boolean sf = Boolean.valueOf(scoreGranted.isSelected(0));
		moduleConfiguration.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, sf);
		if (sf.booleanValue()) {
			// do min/max value
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, Float.valueOf(minVal.getValue()));
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, Float.valueOf(maxVal.getValue()));
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_SCORE_MIN);
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		}
		
		// Grade
		if (gradeEnabledEl != null) {
			moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_ENABLED, gradeEnabledEl.isAtLeastSelected(1));
			moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_GRADE_AUTO, Boolean.valueOf(gradeAutoEl.getSelectedKey()).booleanValue());
		} else {
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
		}
		
		// mandatory passed flag
		Boolean pf = Boolean.valueOf(displayPassed.isSelected(0));
		moduleConfiguration.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, pf);
		if (pf.booleanValue()) {
			// do cut value
			Boolean cf = Boolean.valueOf(displayType.getSelectedKey());
			if (cf.booleanValue() && cutVal.isVisible()) {
				moduleConfiguration.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, Float.valueOf(cutVal.getValue()));
			} else {
				// remove old config
				moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			}
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		}
		
		boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isVisible() && ignoreInCourseAssessmentEl.isAtLeastSelected(1);
		moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);

		// mandatory comment flag
		moduleConfiguration.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.valueOf(commentFlag.isSelected(0)));
		// individual assessment docs
		boolean withAssessmentDocs = individualAssessmentDocsFlag.isVisible() && individualAssessmentDocsFlag.isSelected(0);
		moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, withAssessmentDocs);

		// set info text only if something is in there
		String iu = infotextUser.getValue();
		if (StringHelper.containsNonWhitespace(iu)) {
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, iu);
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		}

		String ic = infotextCoach.getValue();
		if (StringHelper.containsNonWhitespace(ic)) {
			moduleConfiguration.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, ic);
		} else {
			// remove old config
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		}
	}

	/**
	 * @param config the module configuration
	 * @return true if valid, false otherwhise
	 */
	public static boolean isConfigValid(ModuleConfiguration config) {
		boolean isValid = true;
		Object confElement;

		// score flag is mandatory
		confElement = config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		if (confElement instanceof Boolean) {
			Boolean hasScore = (Boolean) confElement;
			if (hasScore.booleanValue()) {
				// score min and max are mandatory if score flag is set to true
				confElement = config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
				isValid = confElement instanceof Float;
				confElement = config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
				isValid = confElement instanceof Float;
			}
		} else return false;

		// passed flag is mandatory
		confElement = config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if (confElement instanceof Boolean) {
			Boolean hasPassed = (Boolean) confElement;
			if (hasPassed.booleanValue()) {
				// cut value is optional if passed flag set to true, but type must match
				confElement = config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
				if (!((confElement == null) || (confElement instanceof Float))) return false;
			}
		} else return false;

		// comment flag is mandatory
		confElement = config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
		isValid = confElement instanceof Boolean;

		// infotext is optional
		confElement = config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		if (!((confElement == null) || (confElement instanceof String))) return false;

		confElement = config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		if (!((confElement == null) || (confElement instanceof String))) return false;

		return isValid;
	}

	private void doEditGradeScale(UserRequest ureq) {
		if (guardModalController(gradeScaleCtrl)) return;
		
		Float minScore = modConfig.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		Float maxScore = modConfig.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		if ((minScore == null || minScore.intValue() == 0) && (maxScore == null || maxScore.intValue() == 0)) {
			showWarning("error.score.min.max.not.set");
			return;
		}
		
		String gradeSystemKey = modConfig.getStringValue(MSCourseNode.CONFIG_KEY_GRADE_SYSTEM);
		Long defautGradesystemKey = StringHelper.isLong(gradeSystemKey)? Long.valueOf(gradeSystemKey): null;

		gradeScaleCtrl = new GradeScaleEditController(ureq, getWindowControl(), courseEntry, courseNode.getIdent(),
				minScore, maxScore, defautGradesystemKey);
		listenTo(gradeScaleCtrl);
		
		String title = translate("grade.scale.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", gradeScaleCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
