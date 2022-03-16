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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CssCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.AssessmentService;
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
import org.olat.modules.grade.ui.PerformanceClassBreakpointDataModel.PerformanceClassBreakpointCols;
import org.olat.modules.grade.ui.component.GradeScaleChart;
import org.olat.modules.grade.ui.component.GradeScoreRangeTable;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleEditController extends FormBasicController implements FlexiTableCssDelegate {

	private FormLayoutContainer messageCont;
	private SingleSelection gradeSystemEl;
	private StaticTextElement resolutionEl;
	private StaticTextElement roundingEl;
	private StaticTextElement cutValueEl;
	private FormLayoutContainer scaleCont;
	private FormLayoutContainer editNumericCont;
	private FormLayoutContainer editTextCont;
	private GradeScoreRangeTable gradeScoreRangeTable;
	private PerformanceClassBreakpointDataModel dataModel;
	private FlexiTableElement tableEl;
	private FormSubmit submitButton;
	
	private final boolean wizard;
	private final RepositoryEntry courseEntry;
	private final String subIdent;
	private final Float minScore;
	private final Float maxScore;
	private GradeScale gradeScale;
	private GradeSystem gradeSystem;
	private final Map<Integer, Breakpoint> positionToBreakpoint;
	
	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;
	
	public GradeScaleEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			String subIdent, Float minScore, Float maxScore, Long defaultGradeSystemKey) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.wizard = false;
		this.courseEntry = courseEntry;
		this.subIdent = subIdent;
		this.minScore = minScore;
		this.maxScore = maxScore;
		
		gradeScale = gradeService.getGradeScale(courseEntry, subIdent);
		if (gradeScale != null) {
			gradeSystem = gradeScale.getGradeSystem();
			positionToBreakpoint = gradeService.getBreakpoints(gradeScale).stream()
					.collect(Collectors.toMap(Breakpoint::getBestToLowest, Function.identity()));
		} else {
			positionToBreakpoint = Collections.emptyMap();
		}
		if (gradeSystem == null && defaultGradeSystemKey != null) {
			loadGradeSystem(defaultGradeSystemKey.toString());
		}
		
		initForm(ureq);
		updateSystemUI();
		updateScaleUI();
		
		if (assessmentService.hasGrades(courseEntry, subIdent)) {
			setReadOnly();
			messageCont.setFormWarning(translate("error.grades.exist"));
		} else {
			messageCont.setFormWarning(translate("message.no.publication"));
		}
	}

	public GradeScaleEditController(UserRequest ureq, WindowControl wControl, Form form, RepositoryEntry courseEntry,
			String subIdent, Float minScore, Float maxScore, Long defaultGradeSystemKey) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, form);
		this.wizard = true;
		this.courseEntry = courseEntry;
		this.subIdent = subIdent;
		this.minScore = minScore;
		this.maxScore = maxScore;
		
		gradeScale = gradeService.getGradeScale(courseEntry, subIdent);
		if (gradeScale != null) {
			gradeSystem = gradeScale.getGradeSystem();
			positionToBreakpoint = gradeService.getBreakpoints(gradeScale).stream()
					.collect(Collectors.toMap(Breakpoint::getBestToLowest, Function.identity()));
		} else {
			positionToBreakpoint = Collections.emptyMap();
		}
		if (gradeSystem == null && defaultGradeSystemKey != null) {
			loadGradeSystem(defaultGradeSystemKey.toString());
		}
		
		initForm(ureq);
		updateSystemUI();
		updateScaleUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		messageCont = FormLayoutContainer.createVerticalFormLayout("message", getTranslator());
		messageCont.setRootForm(mainForm);
		formLayout.add(messageCont);
		if (gradeScale != null && 
				((gradeScale.getMinScore() != null && gradeScale.getMinScore().compareTo(new BigDecimal(minScore)) != 0) 
				|| (gradeScale.getMaxScore() != null && gradeScale.getMaxScore().compareTo(new BigDecimal(maxScore)) != 0))) {
			String translatedText = translate("error.score.min.max", new String[] {
					THREE_DIGITS.format(gradeScale.getMinScore()), THREE_DIGITS.format(gradeScale.getMaxScore()) });
			FormItem minMaxError = uifactory.createSimpleErrorText("score.diff", translatedText);
			messageCont.add(minMaxError);
		}
		
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		SelectionValues gradeSystemSV = getGradeSystemSV();
		gradeSystemEl = uifactory.addDropdownSingleselect("grade.system", generalCont, gradeSystemSV.keys(), gradeSystemSV.values());
		gradeSystemEl.addActionListener(FormEvent.ONCHANGE);
		if (gradeSystem != null && gradeSystemEl.containsKey(gradeSystem.getKey().toString())) {
			gradeSystemEl.select(gradeSystem.getKey().toString(), true);
		} else if (gradeSystemEl.getKeys().length > 0) {
			gradeSystemEl.select(gradeSystemEl.getKey(0), true);
			loadGradeSystem(gradeSystemEl.getSelectedKey());
		}
		
		resolutionEl = uifactory.addStaticTextElement("grade.system.resolution", "", generalCont);
		roundingEl = uifactory.addStaticTextElement("grade.system.rounding", "", generalCont);
		cutValueEl = uifactory.addStaticTextElement("grade.system.cut.value", "", generalCont);
		uifactory.addStaticTextElement("grade.scale.score.min", THREE_DIGITS.format(minScore), generalCont);
		uifactory.addStaticTextElement("grade.scale.score.max", THREE_DIGITS.format(maxScore), generalCont);
		
		scaleCont = FormLayoutContainer.createDefaultFormLayout("scale", getTranslator());
		scaleCont.setFormTitle(translate("grade.scale"));
		scaleCont.setRootForm(mainForm);
		formLayout.add(scaleCont);
		
		// Numeric
		editNumericCont = FormLayoutContainer.createCustomFormLayout("editNumeric", getTranslator(), velocity_root + "/scale_edit_numeric.html");
		editNumericCont.setRootForm(mainForm);
		formLayout.add(editNumericCont);
		
		gradeScoreRangeTable = new GradeScoreRangeTable("ranges");
		gradeScoreRangeTable.setDomReplacementWrapperRequired(false);
		editNumericCont.put("ranges", gradeScoreRangeTable);
		
		// Text
		editTextCont = FormLayoutContainer.createCustomFormLayout("editText", getTranslator(), velocity_root + "/scale_edit_text.html");
		editTextCont.setRootForm(mainForm);
		formLayout.add(editTextCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassBreakpointCols.position, new CssCellRenderer("o_gr_passed_cell")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassBreakpointCols.name, new CssCellRenderer("o_gr_passed_cell")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassBreakpointCols.lowerBound));
		dataModel = new PerformanceClassBreakpointDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), editTextCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCssDelegate(this);
			
		GradeScaleChart gradeScaleChart = new GradeScaleChart("scale.chart");
		gradeScaleChart.setDomReplacementWrapperRequired(false);
		editNumericCont.put("chart", gradeScaleChart);
		editTextCont.put("chart", gradeScaleChart);
		
		if (!wizard) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			buttonsCont.setElementCssClass("o_button_group o_button_group_right o_button_group_bottom");
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			submitButton = uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}

	private SelectionValues getGradeSystemSV() {
		// Load enabled grade systems
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setEnabledOnly(true);
		List<GradeSystem> gradeSystems = gradeService.getGradeSystems(searchParams);
		SelectionValues gradeSystemSV = new SelectionValues();
		gradeSystems.forEach(gs -> gradeSystemSV.add(SelectionValues.entry(
				gs.getKey().toString(),
				GradeUIFactory.translateGradeSystem(getTranslator(), gs))));
		// Get current, but disabled grade system
		if (gradeSystem != null && !gradeSystemSV.containsKey(gradeSystem.getKey().toString())) {
			searchParams = new GradeSystemSearchParams();
			searchParams.setGradeSystem(gradeSystem);
			gradeSystems = gradeService.getGradeSystems(searchParams);
			gradeSystem = !gradeSystems.isEmpty()? gradeSystems.get(0): null;
			if (gradeSystem != null) {
				gradeSystemSV.add(SelectionValues.entry(
						gradeSystem.getKey().toString(),
						translate("grade.system.disabled", GradeUIFactory.translateGradeSystem(getTranslator(), gradeSystem))));
			}
		}
		gradeSystemSV.sort(SelectionValues.VALUE_ASC);
		return gradeSystemSV;
	}
	
	private void setReadOnly() {
		Map<String, FormItem> formItems = flc.getFormComponents();
		for (String formItemName : formItems.keySet()) {
			System.out.println(formItemName);
			FormItem formItem = formItems.get(formItemName);
			if (!"buttons".equals(formItemName)) {
				formItem.setEnabled(false);
			}
		}
		submitButton.setVisible(false);
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		PerformanceClassBreakpointRow row = dataModel.getObject(pos);
		return row.getPerformanceClass().isPassed() ? "o_gr_passed_row" : "o_gr_failed_row" ;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == gradeSystemEl) {
			loadGradeSystem(gradeSystemEl.getSelectedKey());
			updateSystemUI();
			updateScaleUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		gradeSystemEl.clearError();
		if (gradeSystem == null) {
			gradeSystemEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if (tableEl != null) {
			tableEl.clearError();
			boolean lowerBoundsOk = true;
			for (PerformanceClassBreakpointRow row : dataModel.getObjects()) {
				row.getLowerBoundEl().clearError();
				if (!StringHelper.containsNonWhitespace(row.getLowerBoundEl().getValue())) {
					row.getLowerBoundEl().setErrorKey("form.legende.mandatory", null);
					lowerBoundsOk &= false;
				}
			}
			if (lowerBoundsOk) {
				for (PerformanceClassBreakpointRow row : dataModel.getObjects()) {
					lowerBoundsOk &= GradeUIFactory.validateDouble(row.getLowerBoundEl());
				}
				if (lowerBoundsOk) {
					BigDecimal lastValue = null;
					for (PerformanceClassBreakpointRow row : dataModel.getObjects()) {
						BigDecimal value = new BigDecimal(row.getLowerBoundEl().getValue());
						if (lastValue != null && value.compareTo(lastValue) > 0) {
							row.getLowerBoundEl().setErrorKey("error.performance.class.breakpoint.descending", null);
							lowerBoundsOk &= false;
						}
						lastValue = value;
					}
				}
			}
			allOk &= lowerBoundsOk;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (wizard) return;
		
		gradeScale = gradeService.updateOrCreateGradeScale(courseEntry, subIdent, getGradeScale());
		
		if (GradeSystemType.text == gradeSystem.getType()) {
			gradeService.updateOrCreateBreakpoints(gradeScale, getBreakpoints());
		} else {
			gradeService.deleteBreakpoints(gradeScale);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public void updateWrappers(GradeScale gradeScaleWrapper) {
		updateGradeScaleAttributes(gradeScaleWrapper);

	}
	
	public GradeScale getGradeScale() {
		GradeScale gradeScale = new GradeScaleWrapper();
		gradeScale.setGradeSystem(gradeSystem);
		gradeScale.setMinScore(new BigDecimal(minScore));
		gradeScale.setMaxScore(new BigDecimal(maxScore));
		return gradeScale;
	}
	
	public List<Breakpoint> getBreakpoints() {
		if (GradeSystemType.text == gradeSystem.getType()) {
			List<PerformanceClassBreakpointRow> rows = dataModel.getObjects();
			List<Breakpoint> breakpoints = new ArrayList<>(rows.size());
			for (PerformanceClassBreakpointRow row : rows) {
				BreakpointWrapper breakpoint = new BreakpointWrapper();
				BigDecimal value = new BigDecimal(row.getLowerBoundEl().getValue());
				breakpoint.setValue(value);
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
		updateGradeScale.setMinScore(new BigDecimal(minScore));
		updateGradeScale.setMaxScore(new BigDecimal(maxScore));
	}

	private void loadGradeSystem(String selectedKey) {
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setGradeSystem(() -> Long.valueOf(selectedKey));
		List<GradeSystem> gradeSystems = gradeService.getGradeSystems(searchParams);
		gradeSystem = !gradeSystems.isEmpty()? gradeSystems.get(0): null;
	}

	private void updateSystemUI() {
		boolean numeric = gradeSystem != null && GradeSystemType.numeric == gradeSystem.getType();
		if (numeric) {
			String resolutionText = GradeUIFactory.translateResolution(getTranslator(), gradeSystem.getResolution());
			resolutionEl.setValue(resolutionText);
			String roundingText = GradeUIFactory.translateRounding(getTranslator(), gradeSystem.getRounding());
			roundingEl.setValue(roundingText);
			String cutValueText = gradeSystem.getCutValue() != null
					? THREE_DIGITS.format(gradeSystem.getCutValue())
					: translate("grade.system.cut.value.no");
			cutValueEl.setValue(cutValueText);
		}
		resolutionEl.setVisible(numeric);
		roundingEl.setVisible(numeric);
		cutValueEl.setVisible(numeric);
		
		scaleCont.setVisible(gradeSystem != null);
		editNumericCont.setVisible(numeric);
		editTextCont.setVisible(gradeSystem != null && GradeSystemType.text == gradeSystem.getType());
	}

	private void updateScaleUI() {
		if (gradeSystem != null) {
			if (GradeSystemType.numeric == gradeSystem.getType()) {
				updateNumericUI();
			} else {
				updateTextualUI();
			}
		}
	}

	private void updateNumericUI() {
		NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.createGradeScoreRanges(gradeSystem, minScore, maxScore);
		gradeScoreRangeTable.setGradeScoreRanges(gradeScoreRanges);
	}
	
	private void updateTextualUI() {
		updateTextBreakpointModel();
	}

	private void updateTextBreakpointModel() {
		List<PerformanceClass> performanceClasses = gradeService.getPerformanceClasses(gradeSystem);
		Collections.sort(performanceClasses);
		
		Map<Integer, BigDecimal> positionToLowerBound = null;
		if (positionToBreakpoint.isEmpty()) {
			positionToLowerBound = gradeService.getInitialTextLowerBounds(performanceClasses, minScore, maxScore);
		}
		
		List<PerformanceClassBreakpointRow> rows = new ArrayList<>(performanceClasses.size());
		for (int i = 0; i < performanceClasses.size(); i++) {
			PerformanceClass performanceClass = performanceClasses.get(i);
			Breakpoint breakpoint = positionToBreakpoint.get(performanceClass.getBestToLowest());
			PerformanceClassBreakpointRow row = new PerformanceClassBreakpointRow(performanceClass, breakpoint);
			
			String translatedName = GradeUIFactory.translatePerformanceClass(getTranslator(), row.getPerformanceClass());
			row.setTranslatedName(translatedName);
			
			BigDecimal lowerBound = null;
			PerformanceClassBreakpointRow currentRow = dataModel.getObject(i);
			if (performanceClasses.size() == 1) {
				lowerBound = new BigDecimal(minScore);
			}
			if (lowerBound == null && currentRow != null) {
				lowerBound = new BigDecimal(currentRow.getLowerBoundEl().getValue());
			}
			if (lowerBound == null && !positionToBreakpoint.isEmpty() && breakpoint != null) {
				lowerBound = breakpoint.getValue();
			}
			if (lowerBound == null && positionToLowerBound != null && positionToLowerBound.containsKey(performanceClass.getBestToLowest())) {
				lowerBound = positionToLowerBound.get(performanceClass.getBestToLowest());
			}
			if (lowerBound == null) {
				lowerBound = new BigDecimal(minScore);
			}
			String lowerBoundText = THREE_DIGITS.format(lowerBound);
			TextElement lowerBoundEl = uifactory.addTextElement("lb_" + performanceClass.getBestToLowest(), null, 10, lowerBoundText, editNumericCont);
			lowerBoundEl.setDisplaySize(10);
			lowerBoundEl.setUserObject(row);
			if (i == performanceClasses.size()-1) {
				lowerBoundEl.setEnabled(false);
			}
			row.setLowerBoundEl(lowerBoundEl);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset();
	}

}
