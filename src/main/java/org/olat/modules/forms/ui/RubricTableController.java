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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.RubricDataModel.RubricReportCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricTableController extends FormBasicController {
	
	private final Rubric rubric;
	private final SessionFilter filter;
	
	private RubricDataModel dataModel;
	private FlexiTableElement tableEl;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public RubricTableController(UserRequest ureq, WindowControl wControl, Rubric rubric, SessionFilter filter) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.rubric = rubric;
		this.filter = filter;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<LegendEntry> legendLabels = new ArrayList<>();
		List<LegendEntry> legendSigns = new ArrayList<>();
		int columnIndex = 0;
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel startLabelModel = new DefaultFlexiColumnModel(RubricReportCols.startLabel.i18nHeaderKey(), columnIndex++, false, null);
		startLabelModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.antisamy));
		columnsModel.addFlexiColumnModel(startLabelModel);
		
		if (!rubric.getSliderType().equals(SliderType.continuous)) {
			ScaleType scaleType = rubric.getScaleType();
			for (int step = 1; step <= rubric.getSteps(); step++) {
				double stepValue = scaleType.getStepValue(rubric.getSteps(), step);
				String header = EvaluationFormFormatter.formatZeroOrOneDecimals(stepValue);
				String label = rubric.getStepLabels() != null && ! rubric.getStepLabels().isEmpty()
						? rubric.getStepLabels().get(step -1).getLabel()
						: null;
				if (StringHelper.containsNonWhitespace(label)) {
					legendLabels.add(new LegendEntry(header, label));
				}
				DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(header, columnIndex++);
				columnModel.setHeaderLabel(header);
				columnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
				columnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
				columnsModel.addFlexiColumnModel(columnModel);
			}
		}

		if (hasEndLabel()) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(RubricReportCols.endLabel.i18nHeaderKey(), columnIndex++, false, null);
			columnModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.antisamy));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		if (rubric.isNoResponseEnabled()) {
			DefaultFlexiColumnModel noResponsesColumn = new DefaultFlexiColumnModel(RubricReportCols.numberOfNoResponses.i18nHeaderKey(), columnIndex++, false, null);
			noResponsesColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			noResponsesColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnsModel.addFlexiColumnModel(noResponsesColumn);
			legendSigns.add(new LegendEntry(translate("rubric.report.number.no.responses.abrev"), translate("rubric.report.number.no.responses.title")));
		}
		
		DefaultFlexiColumnModel responsesColumn = new DefaultFlexiColumnModel(RubricReportCols.numberOfResponses.i18nHeaderKey(), columnIndex++, false, null);
		responsesColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		responsesColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(responsesColumn);
		legendSigns.add(new LegendEntry(translate("rubric.report.number.responses.abrev"), translate("rubric.report.number.responses.title")));
		
		if (hasWeight()) {
			DefaultFlexiColumnModel medianColumn = new DefaultFlexiColumnModel(RubricReportCols.weight.i18nHeaderKey(), columnIndex++, false, null);
			medianColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			medianColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnsModel.addFlexiColumnModel(medianColumn);
			legendSigns.add(new LegendEntry(translate("rubric.report.weight.abrev"), translate("rubric.report.weight.title")));
		}
		
		DefaultFlexiColumnModel medianColumn = new DefaultFlexiColumnModel(RubricReportCols.median.i18nHeaderKey(), columnIndex++, false, null);
		medianColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		medianColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(medianColumn);
		legendSigns.add(new LegendEntry(translate("rubric.report.median.abrev"), translate("rubric.report.median.title")));
		
		DefaultFlexiColumnModel varianceColumn = new DefaultFlexiColumnModel(RubricReportCols.variance.i18nHeaderKey(), columnIndex++, false, null);
		varianceColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		varianceColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(varianceColumn);
		legendSigns.add(new LegendEntry(translate("rubric.report.variance.abrev"), translate("rubric.report.variance.title")));
		
		DefaultFlexiColumnModel sdtDevColumn = new DefaultFlexiColumnModel(RubricReportCols.stdDev.i18nHeaderKey(), columnIndex++, false, null);
		sdtDevColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		sdtDevColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(sdtDevColumn);
		legendSigns.add(new LegendEntry(translate("rubric.report.sdtdev.abrev"), translate("rubric.report.sdtdev.title")));
		
		RubricAvgRenderer avgRenderer = new RubricAvgRenderer(rubric);
		DefaultFlexiColumnModel avgColumn = new DefaultFlexiColumnModel(RubricReportCols.avg.i18nHeaderKey(), columnIndex++, false, null);
		avgColumn.setCellRenderer(avgRenderer);
		avgColumn.setFooterCellRenderer(avgRenderer);
		avgColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		avgColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(avgColumn);
		legendSigns.add(new LegendEntry(translate("rubric.report.avg.abrev"), translate("rubric.report.avg.title")));

		String name = rubric.getNameDisplays().contains(NameDisplay.report)? rubric.getName(): "";
		String footerHeader = translate("rubric.report.total", new String[] {name});
		dataModel = new RubricDataModel(columnsModel, footerHeader);
		tableEl = uifactory.addTableElement(getWindowControl(), "ru_" + CodeHelper.getRAMUniqueID(), dataModel, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_rubric_table");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setFooter(true);
		
		String legendPage = velocity_root + "/rubric_table_legend.html";
		if (!legendLabels.isEmpty()) {
			FormLayoutContainer legendLablesLayout = FormLayoutContainer.createCustomFormLayout("legendLables", getTranslator(), legendPage);
			flc.add("legendLables", legendLablesLayout);
			legendLablesLayout.setElementCssClass("o_rubric_table_legend");
			legendLablesLayout.contextPut("legendEntries", legendLabels);
		}
		
		FormLayoutContainer legendSignsLayout = FormLayoutContainer.createCustomFormLayout("legendSigns", getTranslator(), legendPage);
		flc.add("legendSigns", legendSignsLayout);
		legendSignsLayout.setElementCssClass("o_rubric_table_legend o_last");
		legendSignsLayout.contextPut("legendEntries", legendSigns);
	}

	private void loadModel() {
		RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, filter);
		List<RubricRow> rows = new ArrayList<>();
		boolean hasWeight = hasWeight();
		for (Slider slider: rubric.getSliders()) {
			SliderStatistic sliderStatistic = rubricStatistic.getSliderStatistic(slider);
			RubricRow rubricRow = new RubricRow(rubric, slider, sliderStatistic, hasWeight);
			rows.add(rubricRow);
		}
		RubricRow totalRow = new RubricRow(rubric, null, rubricStatistic.getTotalStatistic(), hasWeight);
		dataModel.setObjects(rows, totalRow);
		tableEl.reset();	
	}
	
	private boolean hasEndLabel() {
		for (Slider slider: rubric.getSliders()) {
			String endLabel = slider.getEndLabel();
			if (StringHelper.containsNonWhitespace(endLabel)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasWeight() {
		for (Slider slider: rubric.getSliders()) {
			if (slider.getWeight().intValue() != 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class LegendEntry {
		
		private final String abrev;
		private final String full;
		
		public LegendEntry(String abrev, String full) {
			this.abrev = abrev;
			this.full = full;
		}

		public String getAbrev() {
			return abrev;
		}

		public String getFull() {
			return full;
		}
		
	}

}
