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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.handler.RubricHandler;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.RubricAvgRenderer;
import org.olat.modules.forms.ui.multireport.RubricSliderNamedResponseListTableModel.RubricUserResponseCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class RubricSliderNamedResponseListTableController extends FormBasicController {

	private static final String NO_RESPONSE_KEY = "enabled";
	protected static final int STEPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private RubricSliderNamedResponseListTableModel tableModel;
	
	private static int count = 0;
	private final Rubric rubric;
	private final Slider slider;
	private final SessionFilter filter;
	
	private final int steps;
	private final int start;
	private final int end;
	private SelectionValues radioSteps;
	private final ReportHelper reportHelper;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public RubricSliderNamedResponseListTableController(UserRequest ureq, WindowControl wControl,
			Rubric rubric, Slider slider, SessionFilter filter, ReportHelper reportHelper, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "rubric_response_slider_list", mainForm);
		setTranslator(Util.createPackageTranslator(EvaluationFormReportController.class, getLocale(), getTranslator()));
		this.rubric = rubric;
		this.slider = slider;
		this.filter = filter;
		this.reportHelper = reportHelper;
		
		steps = rubric.getSteps();
		start = rubric.getStart();
		end = rubric.getEnd();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricUserResponseCols.user));
		
		if(rubric.getSliderType() == SliderType.discrete) {
			radioSteps = initRadioColumns();
			List<SelectionValue> stepValues = radioSteps.keyValues();
			for(int i=0; i<stepValues.size(); i++) {
				SelectionValue val = stepValues.get(i);
				DefaultFlexiColumnModel stepColumn = new DefaultFlexiColumnModel(val.getKey(), STEPS_OFFSET + i);
				if(StringHelper.containsNonWhitespace(val.getValue())) {
					stepColumn.setHeaderLabel(val.getValue());
				} else {
					stepColumn.setHeaderLabel(val.getKey());
				}
				stepColumn.setCellRenderer(new DiscreteCellRenderer(i));
				columnsModel.addFlexiColumnModel(stepColumn);
			}
		} else if(rubric.getSliderType() == SliderType.continuous
				|| rubric.getSliderType() == SliderType.discrete_slider
				|| rubric.getSliderType() == SliderType.discrete_star) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricUserResponseCols.slider));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricUserResponseCols.noResponse));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricUserResponseCols.comment));
		
		tableModel = new RubricSliderNamedResponseListTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "all-responses", tableModel, 500, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setElementCssClass("o_rubric_slider_answers_list");
		
		tableEl.addActionListener(FormEvent.ONBLUR);
	}
	
	private SelectionValues initRadioColumns() {
		SelectionValues stepsSV = new SelectionValues();
		
		double[] theSteps = new double[steps];
		double step = (end - start + 1) / (double)steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			String value = null;
			String description = null;
			String cssClass = null;
			
			if (rubric.isSliderStepLabelsEnabled()) {
				if (rubric.getStepLabels() != null && rubric.getStepLabels().size() > i) {
					String label = rubric.getStepLabels().get(i).getLabel();
					if (StringHelper.containsNonWhitespace(label)) {
						value = label;
					}
				}
				if (slider.getStepLabels() != null && slider.getStepLabels().size() > i) {
					String label = slider.getStepLabels().get(i).getLabel();
					if (StringHelper.containsNonWhitespace(label)) {
						description = label;
					}
				}
				RubricRating rating = evaluationFormManager.getRubricRating(rubric, Double.valueOf(i + 1));
				cssClass = RubricAvgRenderer.getRatingCssClass(rating);
			} else {
				value = "";
			}
			
			stepsSV.add(SelectionValues.entry(Double.toString(theSteps[i]), value, description, null, cssClass, true));
		}

		return stepsSV;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel() {
		String sliderId = slider.getId();
		String commentId = RubricHandler.getSliderCommentId(slider);
		
		List<EvaluationFormResponse> responses = evaluationFormManager.getResponses(List.of(sliderId, commentId), true, filter, Limit.all());
		List<RubricSliderNamedResponseRow> rows = new ArrayList<>(responses.size());
		
		for(EvaluationFormResponse response:responses) {
			if(response.getResponseIdentifier().equals(sliderId)) {
				RubricSliderNamedResponseRow row = forgeRow(response);
				rows.add(row);
				
				Optional<EvaluationFormResponse> comment = getComment(commentId, response.getSession(), responses);
				if(comment.isPresent()) {
					row.setComment(comment.get().getStringuifiedResponse());
				}
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private Optional<EvaluationFormResponse> getComment(String commentId, EvaluationFormSession session, List<EvaluationFormResponse> responses) {
		return responses.stream()
				.filter(response -> session.equals(response.getSession()))
				.filter(response -> commentId.equals(response.getResponseIdentifier()))
				.findFirst();
	}
	
	private RubricSliderNamedResponseRow forgeRow(EvaluationFormResponse response) {
		EvaluationFormSession session = response.getSession();
		String user = reportHelper.getLegend(session).getName();
		RubricSliderNamedResponseRow row = new RubricSliderNamedResponseRow(user, response.isNoResponse());
		if(rubric.isNoResponseEnabled()) {
			createNoResponseEl(row);
		}
		
		FormItem ratingItem = null;
		if(rubric.getSliderType() == SliderType.continuous) {
			ratingItem = forgeContinuousSlider(response.getNumericalResponse());
		} else if(rubric.getSliderType() == SliderType.discrete_slider) {
			ratingItem = forgeDiscreteSlider(response.getNumericalResponse());
		} else if(rubric.getSliderType() == SliderType.discrete) {
			ratingItem = forgeDiscreteRadioButtons(response.getNumericalResponse());
		} else if(rubric.getSliderType() == SliderType.discrete_star) {
			ratingItem = forgeDiscreteStar(response.getNumericalResponse());
		}
		
		if(ratingItem != null) {
			ratingItem.setEnabled(false);
			row.setRatingComponent(ratingItem.getComponent());
		}
		return row;
	}
	
	private SliderElement forgeDiscreteSlider(BigDecimal numericalResponse) {
		SliderElement sliderEl = forgeContinuousSlider(numericalResponse);
		sliderEl.setStep(1);
		return sliderEl;
	}
	
	private SliderElement forgeContinuousSlider(BigDecimal numericalResponse) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + (++count), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(rubric.getStart());
		sliderEl.setMaxValue(rubric.getEnd());
		if(numericalResponse != null) {
			sliderEl.setValue(numericalResponse.doubleValue());
		}
		return sliderEl;
	}
	
	private RatingFormItem forgeDiscreteStar(BigDecimal numericalResponse) {
		RatingFormItem sliderEl = uifactory.addRatingItem("slider_" + (++count), null, 0, rubric.getSteps(), true, flc);
		sliderEl.setCssClass("o_slider_star");
		sliderEl.setExplanation(null);
		
		sliderEl.setTranslateRatingLabels(false);
		List<StepLabel> stepLabels = rubric.getStepLabels();
		for (int i=0; i < sliderEl.getMaxRating(); i++) {
			if (stepLabels.size() > i) {
				StepLabel label = stepLabels.get(i);
				if (!StringHelper.containsNonWhitespace(label.getLabel())) {
					sliderEl.setLevelLabel(i, label.getLabel());
				}
			}
		}
		if(numericalResponse != null) {
			sliderEl.setCurrentRating(numericalResponse.floatValue());
		}
		
		return sliderEl;
	}
	
	private SingleSelection forgeDiscreteRadioButtons(BigDecimal numericalResponse) {
		SingleSelection radioEl = null;
		if (rubric.isSliderStepLabelsEnabled()) {
			radioEl = uifactory.addCardSingleSelectHorizontal("slider_" + (++count), null, flc, radioSteps);
		} else {
			radioEl = uifactory.addRadiosVertical("slider_" + (++count), null, flc, radioSteps.keys(), radioSteps.values());
		}
		radioEl.setDomReplacementWrapperRequired(false);
		radioEl.setAllowNoSelection(true);
		for(int i=radioSteps.keys().length; i-->0; ) {
			radioEl.setEnabled(i, false);
		}
		
		if(numericalResponse != null) {
			double[] theSteps = new double[steps];
			String[] theKeys = new String[steps];
			
			double step = (end - start + 1) / (double) steps;
			for(int i=0; i<steps; i++) {
				theSteps[i] = start + (i * step);
				theKeys[i] = Double.toString(theSteps[i]);
			}
			
			double val = numericalResponse.doubleValue();
			double error = step / 10.0d;
			for (int i = 0; i < theSteps.length; i++) {
				double theStep = theSteps[i];
				double margin = Math.abs(theStep - val);
				if (margin < error) {
					radioEl.select(theKeys[i], true);
				}
			}
		}
		
		return radioEl;
	}

	private void createNoResponseEl(RubricSliderNamedResponseRow row) {
		SingleSelection noResponseEl = row.getNoResponseComponent();
		if (noResponseEl == null) {
			SelectionValues noResponses = new SelectionValues();
			noResponses.add(SelectionValues.entry(NO_RESPONSE_KEY, ""));
			noResponseEl = uifactory.addRadiosVertical("no_response_" + (++count), null, flc, noResponses.keys(), noResponses.values());
			noResponseEl.setAllowNoSelection(true);
			noResponseEl.setEscapeHtml(false);
			noResponseEl.setEnabled(false);
			noResponseEl.setDomReplacementWrapperRequired(false);
			row.setNoResponseComponent(noResponseEl);
			if(row.isNoResponse()) {
				noResponseEl.select(NO_RESPONSE_KEY, true);
			}
		}
	}
}
