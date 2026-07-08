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
package org.olat.modules.grade.ui;

import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.model.AssessmentScoreStatistic;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemSearchParams;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.model.BreakpointWrapper;
import org.olat.modules.grade.model.GradeScaleWrapper;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleEditController extends FormBasicController {

	private FormLayoutContainer messageCont;
	private FormLayoutContainer gradeSystemCont;
	private TextElement gradeSystemEl;
	private FormLink gradeSystemEditLink;
	private StaticTextElement resolutionEl;
	private StaticTextElement roundingEl;
	private StaticTextElement cutValueEl;
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private SingleSelection breakpointCountEl;
	private FormLayoutContainer breakpoint1Cont;
	private SingleSelection breakpoint1GradeEl;
	private TextElement breakpoint1ScoreEl;
	private FormLayoutContainer breakpoint2Cont;
	private SingleSelection breakpoint2GradeEl;
	private TextElement breakpoint2ScoreEl;
	private FormLayoutContainer breakpoint3Cont;
	private SingleSelection breakpoint3GradeEl;
	private TextElement breakpoint3ScoreEl;
	private FormLayoutContainer refreshNumericalCont;
	private FormLink refreshNumericalLink;
	private FormLink refreshTextLink;
	private FormLayoutContainer scaleCont;
	private GradeScaleVisualizationController vizCtrl;
	private FormSubmit submitButton;

	private CloseableModalController cmc;
	private GradeSystemSelectionController gradeSystemSelectionCtrl;

	private final boolean wizard;
	private final RepositoryEntry courseEntry;
	private final String subIdent;
	private BigDecimal minScore;
	private BigDecimal maxScore;
	private final boolean minMaxFromScale;
	private final Map<Integer, Long> scoreToCount;
	private final SelectionValues gradeSystemSV;
	private GradeScale gradeScale;
	private GradeSystem gradeSystem;
	private Map<Integer, Breakpoint> positionToBreakpoint;

	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;

	public GradeScaleEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			String subIdent, Float minScore, Float maxScore, boolean minMaxFromScale, boolean courseEditor) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.wizard = false;
		this.courseEntry = courseEntry;
		this.subIdent = subIdent;
		this.minScore = minScore != null ? new BigDecimal(minScore) : null;
		this.maxScore = maxScore != null ? new BigDecimal(maxScore) : null;
		this.minMaxFromScale = minMaxFromScale;
		this.scoreToCount = null;
		this.gradeSystemSV = getGradeSystemSV();
		initGradeScaleAndSystem(courseEntry, subIdent);
		
		initForm(ureq);
		updateSystemUI();
		updateScaleUI(ureq);

		if (assessmentService.hasGrades(courseEntry, subIdent)) {
			setReadOnly();
			if (courseEditor) {
				messageCont.setFormWarning(translate("error.grades.exist.editor"));
			} else {
				messageCont.setFormWarning(translate("error.grades.exist"));
			}
		} else {
			messageCont.setFormWarning(translate("message.no.publication"));
		}
	}

	public GradeScaleEditController(UserRequest ureq, WindowControl wControl, Form form, RepositoryEntry courseEntry,
			String subIdent, Float minScore, Float maxScore, boolean minMaxFromScale,
			List<AssessmentScoreStatistic> scoreStatistics) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, form);
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.wizard = true;
		this.courseEntry = courseEntry;
		this.subIdent = subIdent;
		this.minScore = minScore != null ? new BigDecimal(minScore) : null;
		this.maxScore = maxScore != null ? new BigDecimal(maxScore) : null;
		this.minMaxFromScale = minMaxFromScale;
		this.scoreToCount = scoreStatistics.stream()
				.collect(Collectors.toMap(AssessmentScoreStatistic::getScore, AssessmentScoreStatistic::getCount));
		this.gradeSystemSV = getGradeSystemSV();
		initGradeScaleAndSystem(courseEntry, subIdent);
		
		initForm(ureq);
		updateSystemUI();
		updateScaleUI(ureq);
	}

	private void initGradeScaleAndSystem(RepositoryEntry courseEntry, String subIdent) {
		gradeScale = gradeService.getGradeScale(courseEntry, subIdent);
		if (gradeScale != null) {
			gradeSystem = gradeScale.getGradeSystem();
			if (GradeSystemType.text == gradeSystem.getType()) {
				positionToBreakpoint = gradeService.getBreakpoints(gradeScale).stream()
						.collect(Collectors.toMap(Breakpoint::getBestToLowest, Function.identity()));
			}
		}
		if (gradeSystem == null) {
			loadGradeSystem(gradeSystemSV.keys()[0]);
			markDirty();
		}
		
		if (minMaxFromScale) {
			if (gradeScale != null) {
				minScore = gradeScale.getMinScore();
				maxScore = gradeScale.getMaxScore();
			} else {
				minScore = BigDecimal.ZERO;
				maxScore = new BigDecimal("100");
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		messageCont = FormLayoutContainer.createVerticalFormLayout("message", getTranslator());
		messageCont.setRootForm(mainForm);
		formLayout.add(messageCont);
		if (gradeScale != null && !minMaxFromScale
				&& ((gradeScale.getMinScore() != null && gradeScale.getMinScore().compareTo(minScore) != 0)
						|| (gradeScale.getMaxScore() != null && gradeScale.getMaxScore().compareTo(maxScore) != 0))) {
			String translatedText = translate("error.score.min.max", THREE_DIGITS.format(gradeScale.getMinScore()),
					THREE_DIGITS.format(gradeScale.getMaxScore()));
			uifactory.addErrorText("score.diff", translatedText, messageCont);
		}

		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		generalCont.setElementCssClass("o_sel_grade_scale_general");
		formLayout.add(generalCont);

		gradeSystemCont = FormLayoutContainer.createInputGroupLayout("gradeCont", getTranslator(), null, null);
		gradeSystemCont.setLabel("grade.system", null);
		gradeSystemCont.setRootForm(mainForm);
		generalCont.add(gradeSystemCont);

		gradeSystemEl = uifactory.addTextElement("grade.system", null, 255, "", gradeSystemCont);
		gradeSystemEl.setEnabled(false);
		gradeSystemEl.setDomReplacementWrapperRequired(false);
		gradeSystemEl.setElementCssClass("o_omit_margin");
		gradeSystemEl.setAriaLabel(translate("grade.system"));
		gradeSystemEditLink = uifactory.addFormLink("rightAddOn", "grade.system.selection.button", "grade.system.selection.button", null, gradeSystemCont, Link.BUTTON);
		gradeSystemEditLink.setElementCssClass("input-group-addon o_sel_grade_system_select");

		resolutionEl = uifactory.addStaticTextElement("grade.system.resolution", "", generalCont);
		roundingEl = uifactory.addStaticTextElement("grade.system.rounding", "", generalCont);
		cutValueEl = uifactory.addStaticTextElement("grade.system.cut.value", "", generalCont);
		Float min = minScore != null ? Float.valueOf(minScore.floatValue()) : null;
		Float max = maxScore != null ? Float.valueOf(maxScore.floatValue()) : null;
		String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), min, max);
		if (!minMaxFromScale) {
			uifactory.addStaticTextElement("score.min.max", scoreMinMax, generalCont);
		}
		
		
		// Scale
		scaleCont = FormLayoutContainer.createDefaultFormLayout("scale", getTranslator());
		scaleCont.setFormTitle(translate("grade.scale"));
		scaleCont.setFormContextHelp("manual_user/learningresources/Assessment_translate_points_in_grades/");
		scaleCont.setRootForm(mainForm);
		formLayout.add(scaleCont);

		// Min / Max from scale
		if (minMaxFromScale) {
			String scaleMinScore = gradeScale != null && gradeScale.getMinScore() != null
					? AssessmentHelper.getRoundedScore(gradeScale.getMinScore())
					: "0";
			minScoreEl = uifactory.addTextElement("grade.scale.score.min", 12, scaleMinScore, scaleCont);
			minScoreEl.setMandatory(true);

			String scaleMaxScore = gradeScale != null && gradeScale.getMaxScore() != null
					? AssessmentHelper.getRoundedScore(gradeScale.getMaxScore())
					: "100";
			maxScoreEl = uifactory.addTextElement("grade.scale.score.max", 12, scaleMaxScore, scaleCont);
			maxScoreEl.setMandatory(true);
		}

		// Breakpoints
		List<Breakpoint> breakpoints = gradeScale == null ? Collections.emptyList()
				: gradeService.getBreakpoints(gradeScale).stream().filter(b -> b.getGrade() != null)
						.sorted((b1, b2) -> b2.getScore().compareTo(b1.getScore())).collect(Collectors.toList());

		SelectionValues breakpointSV = new SelectionValues();
		breakpointSV.add(SelectionValues.entry("0", translate("grade.scale.breakpoint.count.0")));
		breakpointSV.add(SelectionValues.entry("1", translate("grade.scale.breakpoint.count.1")));
		breakpointSV.add(SelectionValues.entry("2", translate("grade.scale.breakpoint.count.2")));
		breakpointSV.add(SelectionValues.entry("3", translate("grade.scale.breakpoint.count.3")));
		breakpointCountEl = uifactory.addDropdownSingleselect("grade.scale.breakpoint.count", scaleCont,
				breakpointSV.keys(), breakpointSV.values());
		breakpointCountEl.addActionListener(FormEvent.ONCHANGE);
		if (!breakpoints.isEmpty()) {
			breakpointCountEl.select(String.valueOf(breakpoints.size()), true);
		} else {
			breakpointCountEl.select("0", true);
		}

		SelectionValues breakpointGradeSV = getBreakpointGradeSV();
		breakpoint1Cont = uifactory.addCustomFormLayout("breakpoint1", "grade.scale.breakpoint.1",
				velocity_root + "/breakpoint_item.html", scaleCont);
		breakpoint1Cont.contextPut("counter", "1");
		breakpoint1GradeEl = uifactory.addDropdownSingleselect("grade.scale.breakpoint.grade.1", null, breakpoint1Cont,
				breakpointGradeSV.keys(), breakpointGradeSV.values());
		breakpoint1ScoreEl = uifactory.addTextElement("grade.scale.breakpoint.score.1", null, 10, "", breakpoint1Cont);
		breakpoint1ScoreEl.setDisplaySize(10);
		if (breakpoints.size() > 0) {
			Breakpoint breakpoint = breakpoints.get(0);
			if (breakpoint1GradeEl.containsKey(breakpoint.getGrade())) {
				breakpoint1GradeEl.select(breakpoint.getGrade(), true);
				breakpoint1ScoreEl.setValue(THREE_DIGITS.format(breakpoint.getScore()));
			}
		}
		if (!breakpoint1GradeEl.isOneSelected() && breakpoint1GradeEl.getKeys().length > 0) {
			breakpoint1GradeEl.select(breakpoint1GradeEl.getKey(0), true);
		}

		breakpoint2Cont = uifactory.addCustomFormLayout("breakpoint2", "grade.scale.breakpoint.2",
				velocity_root + "/breakpoint_item.html", scaleCont);
		breakpoint2Cont.contextPut("counter", "2");
		breakpoint2GradeEl = uifactory.addDropdownSingleselect("grade.scale.breakpoint.grade.2", null, breakpoint2Cont,
				breakpointGradeSV.keys(), breakpointGradeSV.values());
		breakpoint2ScoreEl = uifactory.addTextElement("grade.scale.breakpoint.score.2", null, 10, "", breakpoint2Cont);
		breakpoint2ScoreEl.setDisplaySize(10);
		if (breakpoints.size() > 1) {
			Breakpoint breakpoint = breakpoints.get(1);
			if (breakpoint2GradeEl.containsKey(breakpoint.getGrade())) {
				breakpoint2GradeEl.select(breakpoint.getGrade(), true);
				breakpoint2ScoreEl.setValue(THREE_DIGITS.format(breakpoint.getScore()));
			}
		}
		if (!breakpoint2GradeEl.isOneSelected() && breakpoint2GradeEl.getKeys().length > 0) {
			breakpoint2GradeEl.select(breakpoint2GradeEl.getKey(0), true);
		}

		breakpoint3Cont = uifactory.addCustomFormLayout("breakpoint3", "grade.scale.breakpoint.3",
				velocity_root + "/breakpoint_item.html", scaleCont);
		breakpoint3Cont.contextPut("counter", "3");
		breakpoint3GradeEl = uifactory.addDropdownSingleselect("grade.scale.breakpoint.grade.3", null, breakpoint3Cont,
				breakpointGradeSV.keys(), breakpointGradeSV.values());
		breakpoint3ScoreEl = uifactory.addTextElement("grade.scale.breakpoint.score.3", null, 10, "", breakpoint3Cont);
		breakpoint3ScoreEl.setDisplaySize(10);
		if (breakpoints.size() > 2) {
			Breakpoint breakpoint = breakpoints.get(2);
			if (breakpoint3GradeEl.containsKey(breakpoint.getGrade())) {
				breakpoint3GradeEl.select(breakpoint.getGrade(), true);
				breakpoint3ScoreEl.setValue(THREE_DIGITS.format(breakpoint.getScore()));
			}
		}
		if (!breakpoint3GradeEl.isOneSelected() && breakpoint3GradeEl.getKeys().length > 0) {
			breakpoint3GradeEl.select(breakpoint3GradeEl.getKey(0), true);
		}

		refreshNumericalCont = FormLayoutContainer.createButtonLayout("update.num", getTranslator());
		refreshNumericalCont.setRootForm(mainForm);
		scaleCont.add(refreshNumericalCont);

		refreshNumericalLink = uifactory.addFormLink("grade.scale.update.num", "grade.scale.update", null,
				refreshNumericalCont, Link.BUTTON);

		vizCtrl = new GradeScaleVisualizationController(ureq, getWindowControl(), mainForm, false);
		listenTo(vizCtrl);
		formLayout.add("viz", vizCtrl.getInitialFormItem());

		refreshTextLink = uifactory.addFormLink("grade.scale.update.text", "grade.scale.update", null,
				vizCtrl.getTextCont(), Link.BUTTON);

		if (!wizard) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			buttonsCont.setElementCssClass("o_button_group o_button_group_right o_button_group_bottom");
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			submitButton = uifactory.addFormSubmitButton("save", buttonsCont);
			submitButton.setElementCssClass("o_sel_grade_scale_save");
		}
	}

	private SelectionValues getGradeSystemSV() {
		// Load enabled grade systems
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setEnabledOnly(true);
		List<GradeSystem> gradeSystems = gradeService.getGradeSystems(searchParams);
		SelectionValues gradeSystemSV = new SelectionValues();
		gradeSystems.forEach(gs -> gradeSystemSV.add(SelectionValues.entry(gs.getKey().toString(),
				GradeUIFactory.translateGradeSystemName(getTranslator(), gs))));
		// Get current, but disabled grade system
		if (gradeSystem != null && !gradeSystemSV.containsKey(gradeSystem.getKey().toString())) {
			searchParams = new GradeSystemSearchParams();
			searchParams.setGradeSystem(gradeSystem);
			gradeSystems = gradeService.getGradeSystems(searchParams);
			gradeSystem = !gradeSystems.isEmpty() ? gradeSystems.get(0) : null;
			if (gradeSystem != null) {
				gradeSystemSV
						.add(SelectionValues.entry(gradeSystem.getKey().toString(), translate("grade.system.disabled",
								GradeUIFactory.translateGradeSystemName(getTranslator(), gradeSystem))));
			}
		}
		gradeSystemSV.sort(SelectionValues.VALUE_ASC);
		return gradeSystemSV;
	}

	private SelectionValues getBreakpointGradeSV() {
		String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystem);
		SelectionValues gradeSV = new SelectionValues();
		List<String> grades = gradeService.getGrades(gradeSystem, minScore, maxScore);
		if (grades.size() > 2) {
			grades.forEach(grade -> gradeSV.add(
					SelectionValues.entry(grade, translate("grade.scale.breakpoint.grade", grade, gradeSystemLabel))));
		}
		return gradeSV;
	}

	private void setReadOnly() {
		Map<String, FormItem> formItems = flc.getFormComponents();
		for (String formItemName : formItems.keySet()) {
			FormItem formItem = formItems.get(formItemName);
			if (!"buttons".equals(formItemName)) {
				formItem.setEnabled(false);
			}
		}
		refreshNumericalLink.setVisible(false);
		refreshTextLink.setVisible(false);
		submitButton.setVisible(false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == gradeSystemEditLink) {
			doSelectGradeSystem(ureq);
		} else if (source == breakpointCountEl) {
			updateNumericalBreakpointsUI();
		} else if (source == refreshNumericalLink) {
			updateNumericUI(ureq);
		} else if (source == refreshTextLink) {
			updateTextDiagramlUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (gradeSystemSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadGradeSystem(gradeSystemSelectionCtrl.getSelectedKey());
				removeScaleSettings();
				updateSystemUI();
				updateScaleUI(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(gradeSystemSelectionCtrl);
		removeAsListenerAndDispose(cmc);
		gradeSystemSelectionCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		gradeSystemEl.clearError();
		if (gradeSystem == null) {
			gradeSystemEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		allOk &= validateNumericMinMax();
		allOk &= validateNumericBreakpoints();
		allOk &= validateTextBreakpoints();

		return allOk;
	}

	private boolean validateNumericMinMax() {
		boolean allOk = true;

		if (minScoreEl != null && maxScoreEl != null) {
			minScoreEl.clearError();
			maxScoreEl.clearError();
			allOk &= GradeUIFactory.validateBigDecimal(minScoreEl, true, null, null);
			allOk &= GradeUIFactory.validateBigDecimal(maxScoreEl, true, null, null);
			
			if (allOk) {
				if (new BigDecimal(minScoreEl.getValue()).compareTo(new BigDecimal(maxScoreEl.getValue())) > 0) {
					maxScoreEl.setErrorKey("error.min.lower.max");
					allOk &= false;
				}
			}
			
			// Set the fields in validate method because the validateNumericBreakpoints() validates
			// against this values;
			if (allOk) {
				minScore = new BigDecimal(minScoreEl.getValue());
				maxScore = new BigDecimal(maxScoreEl.getValue());
			}
		}

		return allOk;
	}

	private boolean validateNumericBreakpoints() {
		boolean allOk = true;

		breakpoint1GradeEl.clearError();
		breakpoint1ScoreEl.clearError();
		breakpoint2GradeEl.clearError();
		breakpoint2ScoreEl.clearError();
		breakpoint3GradeEl.clearError();
		breakpoint3ScoreEl.clearError();

		allOk &= GradeUIFactory.validateBigDecimal(breakpoint1ScoreEl, true, minScore, maxScore);
		allOk &= GradeUIFactory.validateBigDecimal(breakpoint2ScoreEl, true, minScore, maxScore);
		allOk &= GradeUIFactory.validateBigDecimal(breakpoint3ScoreEl, true, minScore, maxScore);

		if (allOk) {
			if (breakpoint2GradeEl.isVisible()
					&& breakpoint2GradeEl.getSelected() <= breakpoint1GradeEl.getSelected()) {
				breakpoint2GradeEl.setErrorKey("error.breakpoint.desc");
				allOk &= false;
			} else if (breakpoint3GradeEl.isVisible()
					&& breakpoint3GradeEl.getSelected() <= breakpoint2GradeEl.getSelected()) {
				breakpoint3GradeEl.setErrorKey("error.breakpoint.desc");
				allOk &= false;
			} else if (breakpoint2ScoreEl.isVisible() && new BigDecimal(breakpoint2ScoreEl.getValue())
					.compareTo(new BigDecimal(breakpoint1ScoreEl.getValue())) > 0) {
				breakpoint2ScoreEl.setErrorKey("error.breakpoint.desc");
				allOk &= false;
			} else if (breakpoint3ScoreEl.isVisible() && new BigDecimal(breakpoint3ScoreEl.getValue())
					.compareTo(new BigDecimal(breakpoint2ScoreEl.getValue())) > 0) {
				breakpoint3ScoreEl.setErrorKey("error.breakpoint.desc");
				allOk &= false;
			}
		}

		return allOk;
	}

	private boolean validateTextBreakpoints() {
		boolean allOk = true;

		vizCtrl.getTableEl().clearError();
		if (vizCtrl.getTextCont().isVisible()) {
			for (PerformanceClassBreakpointRow row : vizCtrl.getTableModel().getObjects()) {
				row.getLowerBoundEl().clearError();
				if (!StringHelper.containsNonWhitespace(row.getLowerBoundEl().getValue())) {
					row.getLowerBoundEl().setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			}
			if (allOk) {
				for (PerformanceClassBreakpointRow row : vizCtrl.getTableModel().getObjects()) {
					allOk &= GradeUIFactory.validateDouble(row.getLowerBoundEl(), false);
				}
				if (allOk) {
					BigDecimal lastValue = null;
					for (PerformanceClassBreakpointRow row : vizCtrl.getTableModel().getObjects()) {
						if (allOk) {
							BigDecimal value = new BigDecimal(row.getLowerBoundEl().getValue());
							if (allOk && !minMaxFromScale && value.compareTo(maxScore) > 0) {
								row.getLowerBoundEl().setErrorKey("error.breakpoint.higher.max");
								allOk &= false;
							}
							if (allOk && lastValue != null && value.compareTo(lastValue) > 0) {
								row.getLowerBoundEl().setErrorKey("error.performance.class.breakpoint.descending");
								allOk &= false;
							}
							lastValue = value;
						}
					}
					if (allOk && minMaxFromScale) {
						List<PerformanceClassBreakpointRow> rows = vizCtrl.getTableModel().getObjects();
						minScore = new BigDecimal(rows.get(rows.size()-1).getLowerBoundEl().getValue());
						maxScore = new BigDecimal(rows.get(0).getLowerBoundEl().getValue());
					}
				}
			}
		}

		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (wizard)
			return;

		gradeScale = gradeService.updateOrCreateGradeScale(courseEntry, subIdent, getGradeScale());
		gradeService.updateOrCreateBreakpoints(gradeScale, getBreakpoints());

		fireEvent(ureq, Event.DONE_EVENT);
	}

	public void updateWrappers(GradeScale gradeScaleWrapper) {
		updateGradeScaleAttributes(gradeScaleWrapper);
	}

	public GradeScale getGradeScale() {
		GradeScale gradeScale = new GradeScaleWrapper();
		gradeScale.setGradeSystem(gradeSystem);
		gradeScale.setMinScore(minScore);
		gradeScale.setMaxScore(maxScore);
		return gradeScale;
	}

	public List<Breakpoint> getBreakpoints() {
		if (GradeSystemType.numeric == gradeSystem.getType()) {
			int numBreakpoints = breakpointCountEl.isOneSelected()
					? Integer.valueOf(breakpointCountEl.getSelectedValue()).intValue()
					: 0;
			List<Breakpoint> breakpoints = new ArrayList<>(numBreakpoints);
			if (numBreakpoints >= 1) {
				BreakpointWrapper breakpoint = new BreakpointWrapper();
				BigDecimal score = new BigDecimal(breakpoint1ScoreEl.getValue());
				breakpoint.setScore(score);
				String grade = breakpoint1GradeEl.getSelectedKey();
				breakpoint.setGrade(grade);
				breakpoints.add(breakpoint);
			}
			if (numBreakpoints >= 2) {
				BreakpointWrapper breakpoint = new BreakpointWrapper();
				BigDecimal value = new BigDecimal(breakpoint2ScoreEl.getValue());
				breakpoint.setScore(value);
				String grade = breakpoint2GradeEl.getSelectedKey();
				breakpoint.setGrade(grade);
				breakpoints.add(breakpoint);
			}
			if (numBreakpoints >= 3) {
				BreakpointWrapper breakpoint = new BreakpointWrapper();
				BigDecimal value = new BigDecimal(breakpoint3ScoreEl.getValue());
				breakpoint.setScore(value);
				String grade = breakpoint3GradeEl.getSelectedKey();
				breakpoint.setGrade(grade);
				breakpoints.add(breakpoint);
			}
			return breakpoints;
		} else if (GradeSystemType.text == gradeSystem.getType()) {
			List<PerformanceClassBreakpointRow> rows = vizCtrl.getTableModel().getObjects();
			List<Breakpoint> breakpoints = new ArrayList<>(rows.size());
			for (PerformanceClassBreakpointRow row : rows) {
				if (row.getPerformanceClass() == null) {
					// Max score row
					continue;
				}
				BreakpointWrapper breakpoint = new BreakpointWrapper();
				BigDecimal value = new BigDecimal(row.getLowerBoundEl().getValue());
				breakpoint.setScore(value);
				Integer bestToLowest = Integer.valueOf(row.getPerformanceClass().getBestToLowest());
				breakpoint.setBestToLowest(bestToLowest);
				breakpoints.add(breakpoint);
			}
			return breakpoints;
		}
		return Collections.emptyList();
	}

	private void updateGradeScaleAttributes(GradeScale updateGradeScale) {
		updateGradeScale.setGradeSystem(gradeSystem);
		updateGradeScale.setMinScore(minScore);
		updateGradeScale.setMaxScore(maxScore);
	}

	private void loadGradeSystem(String selectedKey) {
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setGradeSystem(() -> Long.valueOf(selectedKey));
		List<GradeSystem> gradeSystems = gradeService.getGradeSystems(searchParams);
		gradeSystem = !gradeSystems.isEmpty() ? gradeSystems.get(0) : null;
	}

	private void doSelectGradeSystem(UserRequest ureq) {
		if (guardModalController(gradeSystemSelectionCtrl))
			return;

		gradeSystemSelectionCtrl = new GradeSystemSelectionController(ureq, getWindowControl(), gradeSystemSV,
				gradeSystem);
		listenTo(gradeSystemSelectionCtrl);

		String title = translate("grade.system.selection.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), gradeSystemSelectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void removeScaleSettings() {
		breakpointCountEl.select(breakpointCountEl.getKey(0), true);

		// text
		positionToBreakpoint = Collections.emptyMap();
	}

	private void updateSystemUI() {
		gradeSystemEl.setValue(GradeUIFactory.translateGradeSystemName(getTranslator(), gradeSystem));
		gradeSystemEl.getComponent().setDirty(false);
		gradeSystemCont.setDirty(true);

		boolean numeric = GradeSystemType.numeric == gradeSystem.getType();
		if (numeric) {
			String resolutionText = GradeUIFactory.translateResolution(getTranslator(), gradeSystem.getResolution());
			resolutionEl.setValue(resolutionText);
			String roundingText = GradeUIFactory.translateRounding(getTranslator(), gradeSystem.getRounding());
			roundingEl.setValue(roundingText);
			String cutValueText = gradeSystem.hasPassed() && gradeSystem.getCutValue() != null
					? THREE_DIGITS.format(gradeSystem.getCutValue())
					: translate("grade.system.cut.value.no");
			cutValueEl.setValue(cutValueText);
		}
		resolutionEl.setVisible(numeric);
		roundingEl.setVisible(numeric);
		cutValueEl.setVisible(numeric);
	}

	private void updateNumericalBreakpointsUI() {
		boolean numeric = GradeSystemType.numeric == gradeSystem.getType();

		if (numeric) {
			SelectionValues gradeSV = getBreakpointGradeSV();
			String breakpoint1Grade = breakpoint1GradeEl.isOneSelected() ? breakpoint1GradeEl.getSelectedKey() : null;
			breakpoint1GradeEl.setKeysAndValues(gradeSV.keys(), gradeSV.values(), null);
			if (StringHelper.containsNonWhitespace(breakpoint1Grade)) {
				if (breakpoint1GradeEl.containsKey(breakpoint1Grade)) {
					breakpoint1GradeEl.select(breakpoint1Grade, true);
				}
			}
			if (!breakpoint1GradeEl.isOneSelected()) {
				breakpoint1GradeEl.select(breakpoint1GradeEl.getKey(0), true);
			}

			String breakpoint2Grade = breakpoint2GradeEl.isOneSelected() ? breakpoint2GradeEl.getSelectedKey() : null;
			breakpoint2GradeEl.setKeysAndValues(gradeSV.keys(), gradeSV.values(), null);
			if (StringHelper.containsNonWhitespace(breakpoint2Grade)) {
				if (breakpoint2GradeEl.containsKey(breakpoint2Grade)) {
					breakpoint2GradeEl.select(breakpoint2Grade, true);
				}
			}
			if (!breakpoint2GradeEl.isOneSelected()) {
				breakpoint2GradeEl.select(breakpoint2GradeEl.getKey(0), true);
			}

			String breakpoint3Grade = breakpoint3GradeEl.isOneSelected() ? breakpoint3GradeEl.getSelectedKey() : null;
			breakpoint3GradeEl.setKeysAndValues(gradeSV.keys(), gradeSV.values(), null);
			if (StringHelper.containsNonWhitespace(breakpoint3Grade)) {
				if (breakpoint3GradeEl.containsKey(breakpoint3Grade)) {
					breakpoint3GradeEl.select(breakpoint3Grade, true);
				}
			}
			if (!breakpoint3GradeEl.isOneSelected()) {
				breakpoint3GradeEl.select(breakpoint3GradeEl.getKey(0), true);
			}
		}
		int numBreakpoints = breakpointCountEl.isOneSelected()
				? Integer.valueOf(breakpointCountEl.getSelectedKey()).intValue()
				: 0;

		breakpointCountEl.setVisible(numeric);
		breakpoint1Cont.setVisible(numeric && numBreakpoints >= 1);
		breakpoint1GradeEl.setVisible(numeric && numBreakpoints >= 1);
		breakpoint1ScoreEl.setVisible(numeric && numBreakpoints >= 1);
		breakpoint2Cont.setVisible(numeric && numBreakpoints >= 2);
		breakpoint2GradeEl.setVisible(numeric && numBreakpoints >= 2);
		breakpoint2ScoreEl.setVisible(numeric && numBreakpoints >= 2);
		breakpoint3Cont.setVisible(numeric && numBreakpoints >= 3);
		breakpoint3GradeEl.setVisible(numeric && numBreakpoints >= 3);
		breakpoint3ScoreEl.setVisible(numeric && numBreakpoints >= 3);
		refreshNumericalCont.setVisible(numeric);
	}

	private void updateScaleUI(UserRequest ureq) {
		updateNumericalBreakpointsUI();

		boolean numeric = gradeSystem != null && GradeSystemType.numeric == gradeSystem.getType();
		if (numeric) {
			updateNumericUI(ureq);
		} else {
			updateTextUI();
		}

		if (minMaxFromScale) {
			minScoreEl.setValue(AssessmentHelper.getRoundedScore(minScore));
			maxScoreEl.setValue(AssessmentHelper.getRoundedScore(maxScore));
			minScoreEl.setVisible(numeric);
			maxScoreEl.setVisible(numeric);
		}
	}

	private void updateNumericUI(UserRequest ureq) {
		boolean validateMinMax = validateNumericMinMax();
		if (!validateMinMax) {
			breakpointCountEl.setVisible(false);
			breakpoint1Cont.setVisible(false);
			breakpoint1GradeEl.setVisible(false);
			breakpoint1ScoreEl.setVisible(false);
			breakpoint2Cont.setVisible(false);
			breakpoint2GradeEl.setVisible(false);
			breakpoint2ScoreEl.setVisible(false);
			breakpoint3Cont.setVisible(false);
			breakpoint3GradeEl.setVisible(false);
			breakpoint3ScoreEl.setVisible(false);
		} else {
			breakpointCountEl.setVisible(true);
			updateNumericalBreakpointsUI();
		}
		
		boolean validateNumericBreakpoints = validateNumericBreakpoints();
		if (validateMinMax && validateNumericBreakpoints) {
			List<Breakpoint> breakpoints = getBreakpoints();
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeSystem, breakpoints,
					minScore, maxScore, getLocale());
			vizCtrl.setNumericData(gradeSystem, breakpoints, gradeScoreRanges, scoreToCount);
			vizCtrl.setMode(true);
		} else {
			validateDeferredFormLogic(ureq);
			vizCtrl.setNumericContVisible(false);
		}
	}

	private void updateTextUI() {
		if (validateNumericMinMax()) {
			vizCtrl.setMode(false);
			updateTextBreakpointModel();
			updateTextDiagramlUI();
		} else {
			vizCtrl.setTextContVisible(false);
		}
	}
	
	private void updateTextBreakpointModel() {
		List<PerformanceClass> performanceClasses = gradeService.getPerformanceClasses(gradeSystem);
		Collections.sort(performanceClasses);

		if (positionToBreakpoint == null) {
			positionToBreakpoint = Collections.emptyMap();
		}
		Map<Integer, BigDecimal> positionToLowerBound = gradeService.getInitialTextLowerBounds(performanceClasses,
				minScore, maxScore);

		List<PerformanceClassBreakpointRow> rows = new ArrayList<>(performanceClasses.size());
		if (minMaxFromScale) {
			PerformanceClassBreakpointRow row = new PerformanceClassBreakpointRow(null, null);
			row.setTranslatedName(translate("maximum"));
			// lb_* TextElements must be registered in numericCont so the FlexiTable can
			// render them as form components; they are displayed in the text layout column.
			TextElement lowerBoundEl = uifactory.addTextElement("lb_max", null, 10, THREE_DIGITS.format(maxScore), vizCtrl.getNumericCont());
			lowerBoundEl.setDisplaySize(10);
			lowerBoundEl.setUserObject(row);
			row.setLowerBoundEl(lowerBoundEl);
			rows.add(row);
		}

		for (int i = 0; i < performanceClasses.size(); i++) {
			PerformanceClass performanceClass = performanceClasses.get(i);
			Breakpoint breakpoint = positionToBreakpoint.get(performanceClass.getBestToLowest());
			PerformanceClassBreakpointRow row = new PerformanceClassBreakpointRow(performanceClass, breakpoint);

			String translatedName = GradeUIFactory.translatePerformanceClass(getTranslator(), performanceClass);
			row.setTranslatedName(translatedName);

			BigDecimal lowerBound = null;
			if (performanceClasses.size() == 1) {
				lowerBound = minScore;
			}
			if (lowerBound == null && !positionToBreakpoint.isEmpty() && breakpoint != null) {
				lowerBound = breakpoint.getScore();
			}
			if (lowerBound == null && positionToLowerBound != null
					&& positionToLowerBound.containsKey(performanceClass.getBestToLowest())) {
				lowerBound = positionToLowerBound.get(performanceClass.getBestToLowest());
			}
			if (lowerBound == null) {
				lowerBound = minScore;
			}
			String lowerBoundText = THREE_DIGITS.format(lowerBound);
			// lb_* TextElements must be registered in numericCont so the FlexiTable can
			// render them as form components; they are displayed in the text layout column.
			TextElement lowerBoundEl = uifactory.addTextElement("lb_" + performanceClass.getBestToLowest(), null, 10,
					lowerBoundText, vizCtrl.getNumericCont());
			lowerBoundEl.setDisplaySize(10);
			lowerBoundEl.setUserObject(row);
			if (i == performanceClasses.size() - 1) {
				lowerBoundEl.setValue(THREE_DIGITS.format(minScore));
				lowerBoundEl.setEnabled(minMaxFromScale);
			}
			row.setLowerBoundEl(lowerBoundEl);

			rows.add(row);
		}

		vizCtrl.getTableModel().setObjects(rows);
		vizCtrl.getTableEl().reset();
	}

	private void updateTextDiagramlUI() {
		if (validateTextBreakpoints()) {
			List<Breakpoint> breakpoints = getBreakpoints();
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeSystem, breakpoints,
					minScore, maxScore, getLocale());
			vizCtrl.setTextData(gradeSystem, breakpoints, gradeScoreRanges, scoreToCount);
		} else {
			vizCtrl.hideCharts();
		}
	}

}
