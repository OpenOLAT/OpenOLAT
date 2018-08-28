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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
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
	private final List<? extends EvaluationFormSessionRef> sessions;
	
	private RubricDataModel dataModel;
	private FlexiTableElement tableEl;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public RubricTableController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			List<? extends EvaluationFormSessionRef> sessions) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.rubric = rubric;
		this.sessions = sessions;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int columnIndex = 0;
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricReportCols.startLabel.i18nHeaderKey(), columnIndex++, false, null));
		
		if (!rubric.getSliderType().equals(SliderType.continuous)) {
			ScaleType scaleType = rubric.getScaleType();
			for (int step = 1; step <= rubric.getSteps(); step++) {
				String label = rubric.getStepLabels().get(step -1).getLabel();
				double stepValue = scaleType.getStepValue(rubric.getSteps(), step);
				StringBuilder headerSb = new StringBuilder();
				if (StringHelper.containsNonWhitespace(label)) {
					headerSb.append(label).append("<br>");
				}
				headerSb.append("(").append(EvaluationFormFormatter.formatZeroOrOneDecimals(stepValue)).append(")");
				String header = headerSb.toString();
				DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(header, columnIndex++);
				columnModel.setHeaderLabel(header);
				columnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
				columnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
				columnsModel.addFlexiColumnModel(columnModel);
			}
		}

		if (hasEndLabel()) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel(RubricReportCols.endLabel.i18nHeaderKey(), columnIndex++, false, null);
			columnsModel.addFlexiColumnModel(columnModel);
		}
		if (rubric.isNoResponseEnabled()) {
			DefaultFlexiColumnModel noResponsesColumn = new DefaultFlexiColumnModel(RubricReportCols.numberOfNoResponses.i18nHeaderKey(), columnIndex++, false, null);
			noResponsesColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			noResponsesColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnsModel.addFlexiColumnModel(noResponsesColumn);
		}
		
		DefaultFlexiColumnModel responsesColumn = new DefaultFlexiColumnModel(RubricReportCols.numberOfResponses.i18nHeaderKey(), columnIndex++, false, null);
		responsesColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		responsesColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(responsesColumn);
		
		DefaultFlexiColumnModel medianColumn = new DefaultFlexiColumnModel(RubricReportCols.median.i18nHeaderKey(), columnIndex++, false, null);
		medianColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		medianColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(medianColumn);
		
		DefaultFlexiColumnModel varianceColumn = new DefaultFlexiColumnModel(RubricReportCols.variance.i18nHeaderKey(), columnIndex++, false, null);
		varianceColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		varianceColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(varianceColumn);
		
		DefaultFlexiColumnModel sdtDevColumn = new DefaultFlexiColumnModel(RubricReportCols.stdDev.i18nHeaderKey(), columnIndex++, false, null);
		sdtDevColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		sdtDevColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(sdtDevColumn);
		
		RubricAvgRenderer avgRenderer = new RubricAvgRenderer(rubric);
		DefaultFlexiColumnModel avgColumn = new DefaultFlexiColumnModel(RubricReportCols.avg.i18nHeaderKey(), columnIndex++, false, null);
		avgColumn.setCellRenderer(avgRenderer);
		avgColumn.setFooterCellRenderer(avgRenderer);
		avgColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		avgColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(avgColumn);

		String footerHeader = translate("rubric.report.total", new String[] {rubric.getName()});
		dataModel = new RubricDataModel(columnsModel, footerHeader);
		tableEl = uifactory.addTableElement(getWindowControl(), "ru_" + CodeHelper.getRAMUniqueID(), dataModel, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setFooter(true);
	}
	
	private void loadModel() {
		RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, sessions);
		List<RubricRow> rows = new ArrayList<>();
		for (Slider slider: rubric.getSliders()) {
			SliderStatistic sliderStatistic = rubricStatistic.getSliderStatistic(slider);
			RubricRow rubricRow = new RubricRow(rubric, slider, sliderStatistic);
			rows.add(rubricRow);
		}
		RubricRow totalRow = new RubricRow(rubric, null, rubricStatistic.getTotalStatistic());
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

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
