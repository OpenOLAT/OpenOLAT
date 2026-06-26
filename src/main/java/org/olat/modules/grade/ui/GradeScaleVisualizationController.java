/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.grade.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CssCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.component.GradeChart;
import org.olat.modules.assessment.ui.component.GradeChart.GradeCount;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.ui.PerformanceClassBreakpointDataModel.PerformanceClassBreakpointCols;
import org.olat.modules.grade.ui.component.GradeScaleChart;
import org.olat.modules.grade.ui.component.GradeScoreRangeTable;
import org.olat.modules.grade.ui.component.PostionCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jun 26, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class GradeScaleVisualizationController extends FormBasicController implements FlexiTableCssDelegate {

	private FormLayoutContainer numericCont;
	private FormLayoutContainer textCont;
	private GradeScoreRangeTable gradeScoreRangeTable;
	private PerformanceClassBreakpointDataModel dataModel;
	private FlexiTableElement tableEl;
	private GradeScaleChart gradeScaleChart;
	private GradeChart gradeCountChart;

	private final boolean readOnly;
	private GradeSystem gradeSystem;

	@Autowired
	private GradeService gradeService;

	public GradeScaleVisualizationController(UserRequest ureq, WindowControl wControl, Form form, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, form);
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		numericCont = FormLayoutContainer.createCustomFormLayout("editNumeric", getTranslator(),
				velocity_root + "/scale_edit_numeric.html");
		numericCont.setRootForm(mainForm);
		formLayout.add(numericCont);

		gradeScoreRangeTable = new GradeScoreRangeTable("ranges");
		gradeScoreRangeTable.setDomReplacementWrapperRequired(false);
		numericCont.put("ranges", gradeScoreRangeTable);

		textCont = FormLayoutContainer.createCustomFormLayout("editText", getTranslator(),
				velocity_root + "/scale_edit_text.html");
		textCont.setRootForm(mainForm);
		formLayout.add(textCont);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassBreakpointCols.position,
				new CssCellRenderer("o_gr_passed_cell", new PostionCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassBreakpointCols.name,
				new CssCellRenderer("o_gr_passed_cell")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PerformanceClassBreakpointCols.lowerBound));
		dataModel = new PerformanceClassBreakpointDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(),
				textCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCssDelegate(this);
		tableEl.setEnabled(!readOnly);

		gradeScaleChart = new GradeScaleChart("scale.chart");
		gradeScaleChart.setDomReplacementWrapperRequired(false);
		numericCont.put("gradeScaleChart", gradeScaleChart);
		textCont.put("gradeScaleChart", gradeScaleChart);

		gradeCountChart = new GradeChart("chart");
		numericCont.put("gradeChart", gradeCountChart);
		textCont.put("gradeChart", gradeCountChart);

		numericCont.setVisible(false);
		textCont.setVisible(false);
	}

	public void setMode(boolean numeric) {
		numericCont.setVisible(numeric);
		textCont.setVisible(!numeric);
	}

	public void setNumericData(GradeSystem system, List<Breakpoint> breakpoints,
			NavigableSet<GradeScoreRange> gradeScoreRanges, Map<Integer, Long> scoreToCount) {
		this.gradeSystem = system;
		gradeCountChart.setGradeSystem(system);
		gradeScoreRangeTable.setGradeScoreRanges(gradeScoreRanges);
		updateGradeScaleChartUI(breakpoints, gradeScoreRanges);
		updateGradeCountChartUI(gradeScoreRanges, scoreToCount);
	}

	public void setTextData(GradeSystem system, List<Breakpoint> breakpoints,
			NavigableSet<GradeScoreRange> gradeScoreRanges, Map<Integer, Long> scoreToCount) {
		this.gradeSystem = system;
		gradeCountChart.setGradeSystem(system);
		updateGradeScaleChartUI(breakpoints, gradeScoreRanges);
		updateGradeCountChartUI(gradeScoreRanges, scoreToCount);
	}

	public void setReadOnlyTextRows(List<PerformanceClassBreakpointRow> rows) {
		dataModel.setObjects(rows);
		tableEl.reset();
	}

	public FormLayoutContainer getNumericCont() {
		return numericCont;
	}

	public FormLayoutContainer getTextCont() {
		return textCont;
	}

	public PerformanceClassBreakpointDataModel getTableModel() {
		return dataModel;
	}

	public FlexiTableElement getTableEl() {
		return tableEl;
	}

	public void hideCharts() {
		gradeScaleChart.setVisible(false);
		gradeCountChart.setVisible(false);
	}

	public void setNumericContVisible(boolean visible) {
		numericCont.setVisible(visible);
	}

	public void setTextContVisible(boolean visible) {
		textCont.setVisible(visible);
	}

	private void updateGradeScaleChartUI(List<Breakpoint> breakpoints, NavigableSet<GradeScoreRange> gradeScoreRanges) {
		gradeScaleChart.setGradeSystem(gradeSystem);
		gradeScaleChart.setBreakpoints(breakpoints);
		gradeScaleChart.setGradeScoreRanges(gradeScoreRanges);
		gradeScaleChart.setVisible(true);
	}

	@SuppressWarnings("null")
	private void updateGradeCountChartUI(NavigableSet<GradeScoreRange> gradeScoreRanges, Map<Integer, Long> scoreToCount) {
		boolean hasScores = scoreToCount != null && !scoreToCount.isEmpty();
		if (hasScores) {
			List<GradeCount> gradeCounts = new ArrayList<>(gradeScoreRanges.size());
			Iterator<GradeScoreRange> rangeIterator = gradeScoreRanges.iterator();
			while (rangeIterator.hasNext()) {
				GradeScoreRange range = rangeIterator.next();
				gradeCounts.add(new GradeCount(range.getGrade(), Long.valueOf(0)));
			}
			Collections.reverse(gradeCounts);

			for (Map.Entry<Integer, Long> entry : scoreToCount.entrySet()) {
				GradeScoreRange range = gradeService.getGradeScoreRange(gradeScoreRanges,
						Float.valueOf(entry.getKey().floatValue()));
				GradeCount gradeCount = getGradeCount(gradeCounts, range.getGrade());
				if (gradeCount != null) {
					gradeCount.setCount(Long.valueOf(gradeCount.getCount().longValue() + entry.getValue().longValue()));
				}
			}

			gradeCountChart.setGradeCounts(gradeCounts);
		}
		gradeCountChart.setVisible(hasScores);
	}

	private GradeCount getGradeCount(List<GradeCount> gradeCounts, String grade) {
		return gradeCounts.stream().filter(gc -> gc.getGrade().equals(grade)).findFirst().orElse(null);
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
		if (gradeSystem != null && gradeSystem.hasPassed()) {
			PerformanceClassBreakpointRow row = dataModel.getObject(pos);
			if (row.getPerformanceClass() != null) {
				return row.getPerformanceClass().isPassed()
						? "o_gr_passed_row"
						: "o_gr_failed_row";
			}
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		//
	}

}
