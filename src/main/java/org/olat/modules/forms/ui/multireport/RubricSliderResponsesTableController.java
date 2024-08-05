/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.forms.ui.multireport;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.RubricRadarController;
import org.olat.modules.forms.ui.multireport.RubricSliderResponsesTableModel.RubricResponsesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderResponsesTableController extends FormBasicController implements FlexiTableComponentDelegate {
	
	protected static final int ANSWER = 500;
	
	private FormLink tableViewEl;
	private FormLink spiderViewEl;
	
	private FlexiTableElement tableEl;
	private RubricSliderResponsesTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private int count = 0;
	private final Rubric rubric;
	private final SessionFilter filter;
	private final ReportHelper reportHelper;
	
	private RubricRadarController rubricRadarCtrl;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public RubricSliderResponsesTableController(UserRequest ureq, WindowControl wControl,
			Rubric rubric, SessionFilter filter, ReportHelper reportHelper, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "rubrics_table", rootForm);
		setTranslator(Util.createPackageTranslator(EvaluationFormReportController.class, getLocale(), getTranslator()));
		this.rubric = rubric;
		this.filter = filter;
		this.reportHelper = reportHelper;

		rubricRadarCtrl = new RubricRadarController(ureq, getWindowControl(), rubric, filter, reportHelper);
		listenTo(rubricRadarCtrl);

		detailsVC = createVelocityContainer("response_details");

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tableViewEl = uifactory.addFormLink("table.view", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		tableViewEl.setIconLeftCSS("o_icon o_icon-lg o_icon_table");
		tableViewEl.setElementCssClass("active");
		tableViewEl.setTitle(translate("table.view"));
		spiderViewEl = uifactory.addFormLink("spider.view", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		spiderViewEl.setIconLeftCSS("o_icon o_icon-lg o_icon_splider_chart");
		spiderViewEl.setTitle(translate("spider.view"));
		
		initTableForm(formLayout);
		
		formLayout.add("spiderDiagram", rubricRadarCtrl.getInitialFormItem());
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("tableView", Boolean.TRUE);
		}
	}

	protected void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel startColumn = new DefaultFlexiColumnModel(RubricResponsesCols.startLabel);
		startColumn.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.antisamy));
		columnsModel.addFlexiColumnModel(startColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricResponsesCols.evaluation));
		if(rubric.isNoResponseEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricResponsesCols.numOfNoResponses));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricResponsesCols.numOfResponses));
		if(rubric.isSliderCommentsEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricResponsesCols.numOfComments));
		}
		
		tableModel = new RubricSliderResponsesTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "questions", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setMultiDetails(true);
		tableEl.setFooter(true);
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof RubricSliderResponseRow sliderRow) {
			if(sliderRow.getDetailsControllerComponent() != null) {
				components.add(sliderRow.getDetailsControllerComponent());
			}
		}
		return List.of();
	}
	
	private void loadModel() {
		RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, filter);
		
		List<RubricSliderResponseRow> rows = new ArrayList<>(rubric.getSliders().size());
		for(Slider slider: rubric.getSliders()) {
			SliderStatistic sliderStatistic = rubricStatistic.getSliderStatistic(slider);
			rows.add(forgeRow(slider, sliderStatistic));
		}
		
		forgeFooter(rubricStatistic.getTotalStatistic(), rows);
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeFooter(SliderStatistic rubricStatistic, List<RubricSliderResponseRow> rows) {
		String footerLegend = translate("rubric.report.total", "");
		RubricSliderResponseRow footer = new RubricSliderResponseRow(footerLegend);
		
		long numOfComment = 0l;
		long numOfResponses = 0l;
		long numOfNoResponses = 0l;
		for(RubricSliderResponseRow row:rows) {
			numOfComment += toLong(row.getNumOfComments());
			numOfResponses += toLong(row.getNumOfResponses());
			numOfNoResponses += toLong(row.getNumOfNoResponses());
		}
		footer.setNumOfComments(numOfComment);
		footer.setNumOfResponses(numOfResponses);
		footer.setNumOfNoResponses(numOfNoResponses);
		
		BoxPlot totalAssessmentPlot = forgeBoxPlot(rubricStatistic);
		footer.setAssessmentsPlot(totalAssessmentPlot);
		
		tableModel.setFooterRow(footer);
	}
	
	private long toLong(Long val) {
		return val == null ? 0l : val.longValue();
	}
	
	private RubricSliderResponseRow forgeRow(Slider slider, SliderStatistic sliderStatistic) {
		RubricSliderResponseRow row = new RubricSliderResponseRow(slider);
		
		row.setNumOfResponses(sliderStatistic.getNumberOfResponses());
		row.setNumOfNoResponses(sliderStatistic.getNumberOfNoResponses());
		row.setNumOfComments(sliderStatistic.getNumberOfComments());
		BoxPlot assessmentsPlot = forgeBoxPlot(sliderStatistic);
		row.setAssessmentsPlot(assessmentsPlot);
		
		return row;
	}

	private BoxPlot forgeBoxPlot(SliderStatistic sliderStatistic) {
		int numOfSteps = sliderStatistic.getNumberOfSteps();
		Double average = sliderStatistic.getAvg();
		float min = sliderStatistic.getMin();
		float max = sliderStatistic.getMax();
		Double firstQuartile = sliderStatistic.getFirstQuartile();
		Double thirdQuartile = sliderStatistic.getThirdQuartile();
		Double median = sliderStatistic.getMedian();
		
		return new BoxPlot("plot-assessments-" + (count++), numOfSteps, min, max, toFloat(average),
				toFloat(firstQuartile), toFloat(thirdQuartile), toFloat(median), null);
	}
	
	private float toFloat(Double val) {
		return val == null ? 0.0f : val.floatValue();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableViewEl == source) {
			switchView(true);
		} else if(spiderViewEl == source) {
			switchView(false);
		} else if(tableEl == source) {
			if(event instanceof DetailsToggleEvent toggleEvent) {
				RubricSliderResponseRow row = tableModel.getObject(toggleEvent.getRowIndex());
				doOpenDetails(ureq, row);
			}
		}
	}
	
	private void switchView(boolean table) {
		spiderViewEl.setElementCssClass(table ? "" : "active");
		tableViewEl.setElementCssClass(table ? "active" : "");
		flc.contextPut("tableView", Boolean.valueOf(table));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenDetails(UserRequest ureq, RubricSliderResponseRow row) {
		if(row.getDetailsCtrl() != null) {
			removeAsListenerAndDispose(row.getDetailsCtrl());
			row.setDetailsCtrl(null);
		}
		
		Slider slider = row.getSlider();
		RubricSliderNamedResponseListTableController responsesListCtrl = new RubricSliderNamedResponseListTableController(ureq, getWindowControl(),
				rubric, slider, filter, reportHelper, mainForm);
		listenTo(responsesListCtrl);

		flc.add(responsesListCtrl.getInitialFormItem());
		row.setDetailsCtrl(responsesListCtrl);
		detailsVC.put(row.getDetailsControllerName(), row.getDetailsControllerComponent());
	}
}
