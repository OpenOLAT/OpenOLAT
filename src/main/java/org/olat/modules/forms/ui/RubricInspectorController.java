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

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.forms.ui.EvaluationFormFormatter.oneDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem.TabIndentation;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private static final String[] ENABLED_KEYS = new String[]{"on"};
	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	private static final String GOOD_RATING_END_KEY = "rubric.good.rating.end";
	private static final String GOOD_RATING_START_KEY = "rubric.good.rating.start";
	private static final String[] GOOD_RATING_KEYS = new String[] {
			GOOD_RATING_END_KEY, GOOD_RATING_START_KEY
	};

	private static AtomicInteger count = new AtomicInteger();
	private final Rubric rubric;
	private final boolean restrictedEdit;
	
	private final String[] nameDisplayKeys = new String[] { NameDisplay.execution.name(), NameDisplay.report.name() };
	private final String[] sliderTypeKeys = new String[] { SliderType.discrete.name(), SliderType.discrete_slider.name(), SliderType.continuous.name() };
	private final String[] sliderStepKeys = new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10" };
	private final String[] showSurveyConfigKey = new String[] { "rubric.survey.configuration.show" };

	private TabbedPaneItem tabbedPane;
	
	private FormLink saveButton;
	private MultipleSelectionElement surveyConfigEl;
	private SingleSelection sliderTypeEl;
	private SingleSelection scaleTypeEl;
	private TextElement nameEl;
	private MultipleSelectionElement nameDisplayEl;
	private SingleSelection stepsEl;
	private MultipleSelectionElement noAnswerEl;
	private FormLayoutContainer insufficientCont;
	private TextElement lowerBoundInsufficientEl;
	private TextElement upperBoundInsufficientEl;
	private FormLayoutContainer neutralCont;
	private TextElement lowerBoundNeutralEl;
	private TextElement upperBoundNeutralEl;
	private FormLayoutContainer sufficientCont;
	private TextElement lowerBoundSufficientEl;
	private TextElement upperBoundSufficientEl;
	private SingleSelection goodRatingEl;
	private SingleSelection obligationEl;

	public RubricInspectorController(UserRequest ureq, WindowControl wControl, Rubric rubric, boolean restrictedEdit) {
		super(ureq, wControl, "rubric_inspector");
		this.rubric = rubric;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
		updateTypeSettings();
		updateUI();
	}

	@Override
	public String getTitle() {
		return translate("inspector.formrubric");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabIndentation.none);
		formLayout.add("tabs", tabbedPane);
		
		initGeneralForm(formLayout, tabbedPane);
		initSurveyConfigForm(formLayout, tabbedPane);
	}
	
	private void initGeneralForm(FormItemContainer formLayout, TabbedPaneItem tPane) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tPane.addTab(translate("rubric.general"), layoutCont);

		// slider type
		String[] sliderTypeValues = new String[] { translate("slider.discrete"), translate("slider.discrete.slider"), translate("slider.continuous") };
		sliderTypeEl = uifactory.addDropdownSingleselect("slider.type." + count.incrementAndGet(), "slider.type", layoutCont, sliderTypeKeys, sliderTypeValues, null);
		sliderTypeEl.addActionListener(FormEvent.ONCHANGE);
		sliderTypeEl.setEnabled(!restrictedEdit);
		boolean typeSelected = false;
		if(rubric != null && rubric.getSliderType() != null) {
			for(String sliderTypeKey:sliderTypeKeys) {
				if(sliderTypeKey.equals(rubric.getSliderType().name())) {
					sliderTypeEl.select(sliderTypeKey, true);
					typeSelected = true;
				}
			}
		}
		if(!typeSelected) {
			sliderTypeEl.select(sliderTypeKeys[0], true);
		}
		
		// slider steps
		stepsEl = uifactory.addDropdownSingleselect("slider.steps." + count.incrementAndGet(), "slider.steps", layoutCont, sliderStepKeys, sliderStepKeys, null);
		stepsEl.addActionListener(FormEvent.ONCHANGE);
		stepsEl.setEnabled(!restrictedEdit);
		boolean stepSelected = false;
		if(rubric != null && rubric.getSteps() > 0) {
			String steps = Integer.toString(rubric.getSteps());
			for(String sliderStepKey:sliderStepKeys) {
				if(sliderStepKey.equals(steps)) {
					stepsEl.select(sliderStepKey, true);
					stepSelected = true;
				}
			}
		}
		if(!stepSelected) {
			stepsEl.select(sliderStepKeys[4], true);
		}
		
		// mandatory
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_" + CodeHelper.getRAMUniqueID(), "obligation", layoutCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, rubric.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !rubric.isMandatory());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		obligationEl.setEnabled(!restrictedEdit);
		
		// no answer
		noAnswerEl = uifactory.addCheckboxesVertical("no.response." + count.incrementAndGet(),
				"rubric.no.response.enabled", layoutCont, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS), 1);
		noAnswerEl.setHelpTextKey("no.response.help", null);
		noAnswerEl.addActionListener(FormEvent.ONCHANGE);
		noAnswerEl.setAjaxOnly(true);
		noAnswerEl.select(ENABLED_KEYS[0], rubric.isNoResponseEnabled());
		noAnswerEl.setEnabled(!restrictedEdit);
	}
	
	private void initSurveyConfigForm(FormItemContainer formLayout, TabbedPaneItem tPane) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("surveyConfig", getTranslator());
		formLayout.add(layoutCont);
		tPane.addTab(translate("rubric.survey.config"), layoutCont);
		
		// survey configs
		surveyConfigEl = uifactory.addCheckboxesHorizontal("rubric.survey.configuration", layoutCont,
				showSurveyConfigKey, translateAll(getTranslator(), showSurveyConfigKey));
		surveyConfigEl.addActionListener(FormEvent.ONCHANGE);
		surveyConfigEl.setAjaxOnly(true);

		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, rubric.getName(), layoutCont);
		nameEl.setHelpTextKey("rubric.name.help", null);
		nameEl.addActionListener(FormEvent.ONCHANGE);
		
		// name display
		String[] nameDisplayValues = new String[] { translate("rubric.name.execution"), translate("rubric.name.report") };
		nameDisplayEl = uifactory.addCheckboxesHorizontal("rubric.name.display", layoutCont, nameDisplayKeys, nameDisplayValues);
		nameDisplayEl.addActionListener(FormEvent.ONCHANGE);
		nameDisplayEl.setAjaxOnly(true);
		nameDisplayEl.setEvaluationOnlyVisible(true);
		for (NameDisplay nameDisplay : rubric.getNameDisplays()) {
			nameDisplayEl.select(nameDisplay.name(), true);
		}
		
		// scale type
		scaleTypeEl = uifactory.addDropdownSingleselect("scale.type." + count.incrementAndGet(), "rubric.scale.type",
				layoutCont, ScaleType.getKeys(), ScaleType.getValues(getTranslator()), null);
		scaleTypeEl.setHelpTextKey("rubric.scale.type.help", null);
		scaleTypeEl.addActionListener(FormEvent.ONCHANGE);
		scaleTypeEl.setEnabled(!restrictedEdit);
		boolean scaleSelected = false;
		if(rubric != null && rubric.getScaleType() != null) {
			for(String scaleTypeKey: ScaleType.getKeys()) {
				if(scaleTypeKey.equals(rubric.getScaleType().getKey())) {
					scaleTypeEl.select(scaleTypeKey, true);
					scaleSelected = true;
				}
			}
		}
		if(!scaleSelected) {
			scaleTypeEl.select(ScaleType.getKeys()[0], true);
		}
		
		// good rating side
		goodRatingEl = uifactory.addDropdownSingleselect("rubric.good.rating" + count.incrementAndGet(), "rubric.good.rating",
				layoutCont, GOOD_RATING_KEYS, translateAll(getTranslator(), GOOD_RATING_KEYS), null);
		goodRatingEl.setHelpTextKey("rubric.good.rating.help", null);
		goodRatingEl.addActionListener(FormEvent.ONCHANGE);
		if (rubric != null) {
			String goodRatingKey = rubric.isStartGoodRating()? GOOD_RATING_START_KEY: GOOD_RATING_END_KEY;
			goodRatingEl.select(goodRatingKey, true);
		}
		
		// insufficient range
		String insufficientPage = velocity_root + "/rubric_range_insufficient.html";
		insufficientCont = FormLayoutContainer.createCustomFormLayout("insufficient",
				getTranslator(), insufficientPage);
		insufficientCont.setRootForm(mainForm);
		layoutCont.add("insufficient", insufficientCont);
		insufficientCont.setLabel("rubric.insufficient", null);
		insufficientCont.setHelpTextKey("rubric.rating.help", new String[] { translate("rubric.insufficient")} );
		String insufficientLowerBound = rubric.getLowerBoundInsufficient() != null
				? String.valueOf(rubric.getLowerBoundInsufficient())
				: null;
		lowerBoundInsufficientEl = uifactory.addTextElement("rubric.lower.bound.insufficient", null, 4,
				insufficientLowerBound, insufficientCont);
		lowerBoundInsufficientEl.setDomReplacementWrapperRequired(false);
		lowerBoundInsufficientEl.addActionListener(FormEvent.ONCHANGE);
		lowerBoundInsufficientEl.setElementCssClass("o_ceditor_narrow");
		lowerBoundInsufficientEl.setDisplaySize(4);
		String insufficientUpperBound = rubric.getUpperBoundInsufficient() != null
				? String.valueOf(rubric.getUpperBoundInsufficient())
				: null;
		upperBoundInsufficientEl = uifactory.addTextElement("rubric.upper.bound.insufficient", null, 4,
				insufficientUpperBound, insufficientCont);
		upperBoundInsufficientEl.setDomReplacementWrapperRequired(false);
		upperBoundInsufficientEl.addActionListener(FormEvent.ONCHANGE);
		upperBoundInsufficientEl.setElementCssClass("o_ceditor_narrow");
		upperBoundInsufficientEl.setDisplaySize(4);

		// neutral range
		String neutralPage = velocity_root + "/rubric_range_neutral.html";
		neutralCont = FormLayoutContainer.createCustomFormLayout("neutral", getTranslator(),
				neutralPage);
		neutralCont.setRootForm(mainForm);
		layoutCont.add("neutral", neutralCont);
		neutralCont.setLabel("rubric.neutral", null);
		neutralCont.setHelpTextKey("rubric.rating.help", new String[] { translate("rubric.neutral")} );
		String neutralLowerBound = rubric.getLowerBoundNeutral() != null ? String.valueOf(rubric.getLowerBoundNeutral())
				: null;
		lowerBoundNeutralEl = uifactory.addTextElement("rubric.lower.bound.neutral", null, 4, neutralLowerBound, neutralCont);
		lowerBoundNeutralEl.setDomReplacementWrapperRequired(false);
		lowerBoundNeutralEl.addActionListener(FormEvent.ONCHANGE);
		lowerBoundNeutralEl.setElementCssClass("o_ceditor_narrow");
		lowerBoundNeutralEl.setDisplaySize(4);
		String neutralUpperBound = rubric.getUpperBoundNeutral() != null ? String.valueOf(rubric.getUpperBoundNeutral())
				: null;
		upperBoundNeutralEl = uifactory.addTextElement("rubric.upper.bound.neutral", null, 4, neutralUpperBound, neutralCont);
		upperBoundNeutralEl.setDomReplacementWrapperRequired(false);
		upperBoundNeutralEl.addActionListener(FormEvent.ONCHANGE);
		upperBoundNeutralEl.setElementCssClass("o_ceditor_narrow");
		upperBoundNeutralEl.setDisplaySize(4);

		// sufficient range
		String sufficientPage = velocity_root + "/rubric_range_sufficient.html";
		sufficientCont = FormLayoutContainer.createCustomFormLayout("sufficient", getTranslator(),
				sufficientPage);
		sufficientCont.setRootForm(mainForm);
		layoutCont.add("sufficient", sufficientCont);
		sufficientCont.setLabel("rubric.sufficient", null);
		sufficientCont.setHelpTextKey("rubric.rating.help", new String[] { translate("rubric.sufficient")} );
		String sufficientLowerBound = rubric.getLowerBoundSufficient() != null
				? String.valueOf(rubric.getLowerBoundSufficient())
				: null;
		lowerBoundSufficientEl = uifactory.addTextElement("rubric.lower.bound.sufficient", null, 4, sufficientLowerBound,
				sufficientCont);
		lowerBoundSufficientEl.setDomReplacementWrapperRequired(false);
		lowerBoundSufficientEl.addActionListener(FormEvent.ONCHANGE);
		lowerBoundSufficientEl.setElementCssClass("o_ceditor_narrow");
		lowerBoundSufficientEl.setDisplaySize(4);
		String sufficientUpperBound = rubric.getUpperBoundSufficient() != null
				? String.valueOf(rubric.getUpperBoundSufficient())
				: null;
		upperBoundSufficientEl = uifactory.addTextElement("rubric.upper.bound.sufficient", null, 4, sufficientUpperBound,
				sufficientCont);
		upperBoundSufficientEl.setDomReplacementWrapperRequired(false);
		upperBoundSufficientEl.addActionListener(FormEvent.ONCHANGE);
		upperBoundSufficientEl.setElementCssClass("o_ceditor_narrow");
		upperBoundSufficientEl.setDisplaySize(4);
		updatePlaceholders();
	}

	private void updateUI() {
		updateSurveyConfigUI();
	}

	private void updateSurveyConfigUI() {
		boolean isSurveyConfig = surveyConfigEl.isAtLeastSelected(1);
		nameEl.setVisible(isSurveyConfig);
		nameDisplayEl.setVisible(isSurveyConfig);
		scaleTypeEl.setVisible(isSurveyConfig);
		insufficientCont.setVisible(isSurveyConfig);
		lowerBoundInsufficientEl.setVisible(isSurveyConfig);
		upperBoundInsufficientEl.setVisible(isSurveyConfig);
		neutralCont.setVisible(isSurveyConfig);
		lowerBoundNeutralEl.setVisible(isSurveyConfig);
		upperBoundNeutralEl.setVisible(isSurveyConfig);
		sufficientCont.setVisible(isSurveyConfig);
		lowerBoundSufficientEl.setVisible(isSurveyConfig);
		upperBoundSufficientEl.setVisible(isSurveyConfig);
		goodRatingEl.setVisible(isSurveyConfig);
	}
	
	private void updateTypeSettings() {
		if(!sliderTypeEl.isOneSelected()) return;
		
		SliderType selectedType = SliderType.valueOf(sliderTypeEl.getSelectedKey());
		if(selectedType == SliderType.discrete || selectedType == SliderType.discrete_slider) {
			stepsEl.setVisible(true);
		} else if(selectedType == SliderType.continuous) {
			stepsEl.setVisible(false);
		}
	}
	
	private void updatePlaceholders() {
		boolean startGoodRating = goodRatingEl.isOneSelected() && GOOD_RATING_START_KEY.equals(goodRatingEl.getSelectedKey())
				? true
				: false;
		String selectedSliderType = sliderTypeEl.getSelectedKey();
		SliderType sliderType =  SliderType.valueOf(selectedSliderType);
		String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
		ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
		
		int steps = sliderType == SliderType.continuous? 100: Integer.parseInt(stepsEl.getSelectedKey());
		double leftValue = scaleType.getStepValue(steps, 1);
		double rightValue = scaleType.getStepValue(steps, steps);
		double highValue = rightValue > leftValue? rightValue: leftValue;
		double lowValue = rightValue < leftValue? rightValue: leftValue;
		int diff = sliderType == SliderType.continuous? 10: 1;
		boolean highIsGood = (startGoodRating && ScaleType.maxToOne.equals(scaleType))
				|| (startGoodRating && !ScaleType.maxToOne.equals(scaleType));
		
		RatingPlaceholder placeholders;
		if (highIsGood) {
			placeholders = new RatingPlaceholder(
					lowValue + 2 * diff,
					highValue,
					lowValue + diff,
					lowValue + 2 * diff,
					lowValue,
					lowValue + diff
					);
		} else {
			placeholders = new RatingPlaceholder(
					lowValue,
					highValue - 2 * diff,
					highValue - 2 * diff,
					highValue - diff,
					highValue - diff,
					highValue
					);
		}
		
		lowerBoundInsufficientEl.setPlaceholderText(oneDecimal(placeholders.getLowerBoundInsufficient()));
		upperBoundInsufficientEl.setPlaceholderText(oneDecimal(placeholders.getUpperBoundInsufficient()));
		lowerBoundNeutralEl.setPlaceholderText(oneDecimal(placeholders.getLowerBoundNeutral()));
		upperBoundNeutralEl.setPlaceholderText(oneDecimal(placeholders.getUpperBoundNeutral()));
		lowerBoundSufficientEl.setPlaceholderText(oneDecimal(placeholders.getLowerBoundSufficient()));
		upperBoundSufficientEl.setPlaceholderText(oneDecimal(placeholders.getUpperBoundSufficient()));
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (surveyConfigEl == source) {
			updateUI();
		} else if(sliderTypeEl == source) {
			updateTypeSettings();
			updatePlaceholders();
			doValidateAndSave(ureq);
		} else if(stepsEl == source) {
			updatePlaceholders();
			doValidateAndSave(ureq);
		} else if (scaleTypeEl == source) {
			updatePlaceholders();
			doValidateAndSave(ureq);
		} else if (goodRatingEl == source) {
			updatePlaceholders();
			doValidateAndSave(ureq);
		} else if(saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}
		} else if(noAnswerEl == source) {
			doValidateAndSave(ureq);
		} else if(source instanceof TextElement) {
			doValidateAndSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		boolean surveyConfigOk = true;
		
		// slider type
		sliderTypeEl.clearError();
		if(!sliderTypeEl.isOneSelected()) {
			sliderTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		// bounds
		double min;
		double max;
		int steps = 100;
		String selectedSliderType = sliderTypeEl.getSelectedKey();
		SliderType sliderType =  SliderType.valueOf(selectedSliderType);
		String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
		ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
		if (sliderType != SliderType.continuous) {
			steps = Integer.parseInt(stepsEl.getSelectedKey());
		}
		min = scaleType.getStepValue(steps, 1);
		max = scaleType.getStepValue(steps, steps);
		if (min > max) {
			double temp = min;
			min = max;
			max = temp;
		}
		lowerBoundInsufficientEl.clearError();
		if (isInvalidDouble(lowerBoundInsufficientEl.getValue(), min, max)
				|| isInvalidDouble(upperBoundInsufficientEl.getValue(), min, max)
				|| isOnlyOnePresent(lowerBoundInsufficientEl, upperBoundInsufficientEl)) {
			lowerBoundInsufficientEl.setErrorKey("error.outside.range",
					new String[] { String.valueOf(min), String.valueOf(max) });
			surveyConfigOk = false;
		}
		lowerBoundNeutralEl.clearError();
		if (isInvalidDouble(lowerBoundNeutralEl.getValue(), min, max)
				|| isInvalidDouble(upperBoundNeutralEl.getValue(), min, max)
				|| isOnlyOnePresent(lowerBoundNeutralEl, upperBoundNeutralEl)) {
			lowerBoundNeutralEl.setErrorKey("error.outside.range",
					new String[] { String.valueOf(min), String.valueOf(max) });
			surveyConfigOk = false;
		} else if (isOverlapping(lowerBoundInsufficientEl, upperBoundInsufficientEl, lowerBoundNeutralEl, upperBoundNeutralEl)) {
			lowerBoundNeutralEl.setErrorKey("error.range.overlapping", null);
			surveyConfigOk = false;
		}
		lowerBoundSufficientEl.clearError();
		if (isInvalidDouble(lowerBoundSufficientEl.getValue(), min, max)
				|| isInvalidDouble(upperBoundSufficientEl.getValue(), min, max)
				|| isOnlyOnePresent(lowerBoundSufficientEl, upperBoundSufficientEl)) {
			lowerBoundSufficientEl.setErrorKey("error.outside.range",
					new String[] { String.valueOf(min), String.valueOf(max) });
			surveyConfigOk = false;
		} else if (isOverlapping(lowerBoundInsufficientEl, upperBoundInsufficientEl, lowerBoundSufficientEl, upperBoundSufficientEl)
				|| isOverlapping(lowerBoundNeutralEl, upperBoundNeutralEl, lowerBoundSufficientEl, upperBoundSufficientEl)) {
			lowerBoundSufficientEl.setErrorKey("error.range.overlapping", null);
			surveyConfigOk = false;
		}
		
		if (!surveyConfigOk) {
			surveyConfigEl.select(surveyConfigEl.getKey(0), true);
			updateSurveyConfigUI();
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean isOnlyOnePresent(TextElement element1, TextElement element2) {
		boolean present1 = StringHelper.containsNonWhitespace(element1.getValue());
		boolean present2 = StringHelper.containsNonWhitespace(element2.getValue());
		if (present1 && !present2) return true;
		if (!present1 && present2) return true;
		return false;
	}

	private boolean isInvalidDouble(String val, double min, double max) {
		boolean inside = true;
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				double value = Double.parseDouble(val);
				if(min > value) {
					inside =  false;
				} else if(max < value) {
					inside =  false;
				}
			} catch (NumberFormatException e) {
				inside =  false;
			}
		}
		return !inside;
	}
	
	private boolean isOverlapping(TextElement range1Lower, TextElement range1Upper, TextElement range2Lower, TextElement range2Upper) {
		if (!StringHelper.containsNonWhitespace(range1Lower.getValue())
				|| !StringHelper.containsNonWhitespace(range1Upper.getValue())
				|| !StringHelper.containsNonWhitespace(range2Lower.getValue())
				|| !StringHelper.containsNonWhitespace(range2Upper.getValue())) {
			return false;
		}
		try {
			double r1Lower = Double.parseDouble(range1Lower.getValue());
			double r1Upper = Double.parseDouble(range1Upper.getValue());
			double r2Lower = Double.parseDouble(range2Lower.getValue());
			double r2Upper = Double.parseDouble(range2Upper.getValue());
			
			double r1Min = r1Lower < r1Upper? r1Lower: r1Upper;
			double r1Max = r1Lower > r1Upper? r1Lower: r1Upper;
			double r2Min = r2Lower < r2Upper? r2Lower: r2Upper;
			double r2Max = r2Lower > r2Upper? r2Lower: r2Upper;
			
			if ((r1Min < r2Min && r1Max > r2Min) || (r2Min < r1Min && r2Max > r1Min)) {
				return true;
			}
		} catch (Exception e) {
			//
		}
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		fireEvent(ureq, new ChangePartEvent(rubric));
	}
	
	private void doValidateAndSave(UserRequest ureq) {
		if(validateFormLogic(ureq)) {
			doSave();
			fireEvent(ureq, new ChangePartEvent(rubric));
		}
	}
	
	private void doSave() {
		String selectedSliderType = sliderTypeEl.getSelectedKey();
		SliderType sliderType =  SliderType.valueOf(selectedSliderType);
		rubric.setSliderType(sliderType);
		if(sliderType == SliderType.continuous) {
			rubric.setStart(1);
			rubric.setEnd(100);
			rubric.setSteps(100);
		} else {
			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			rubric.setStart(1);
			rubric.setEnd(steps);
			rubric.setSteps(steps);
		}
		
		rubric.setName(nameEl.getValue());
		
		List<NameDisplay> nameDisplays = new ArrayList<>(2);
		Collection<String> nameDisplayKeys = nameDisplayEl.getSelectedKeys();
		for (String key : nameDisplayKeys) {
			NameDisplay nameDisplay = NameDisplay.valueOf(key);
			nameDisplays.add(nameDisplay);
		}
		rubric.setNameDisplays(nameDisplays);
		
		String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
		ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
		rubric.setScaleType(scaleType);
		
		boolean noResonse = noAnswerEl.isAtLeastSelected(1);
		rubric.setNoResponseEnabled(noResonse);
		
		String lowerBoundInsufficientValue = lowerBoundInsufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(lowerBoundInsufficientValue)) {
			double lowerBoundInsufficient = Double.parseDouble(lowerBoundInsufficientValue);
			rubric.setLowerBoundInsufficient(lowerBoundInsufficient);
		} else {
			rubric.setLowerBoundInsufficient(null);
		}
		String upperBoundInsufficientValue = upperBoundInsufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(upperBoundInsufficientValue)) {
			double upperBoundInsufficient = Double.parseDouble(upperBoundInsufficientValue);
			rubric.setUpperBoundInsufficient(upperBoundInsufficient);
		} else {
			rubric.setUpperBoundInsufficient(null);
		}
		String lowerBoundNeutralValue = lowerBoundNeutralEl.getValue();
		if (StringHelper.containsNonWhitespace(lowerBoundNeutralValue)) {
			double lowerBoundNeutral = Double.parseDouble(lowerBoundNeutralValue);
			rubric.setLowerBoundNeutral(lowerBoundNeutral);
		} else {
			rubric.setLowerBoundNeutral(null);
		}
		String upperBoundNeutralValue = upperBoundNeutralEl.getValue();
		if (StringHelper.containsNonWhitespace(upperBoundNeutralValue)) {
			double upperBoundNeutral = Double.parseDouble(upperBoundNeutralValue);
			rubric.setUpperBoundNeutral(upperBoundNeutral);
		} else {
			rubric.setUpperBoundNeutral(null);
		}
		String lowerBoundSufficientValue = lowerBoundSufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(lowerBoundSufficientValue)) {
			double lowerBoundSufficient = Double.parseDouble(lowerBoundSufficientValue);
			rubric.setLowerBoundSufficient(lowerBoundSufficient);
		} else {
			rubric.setLowerBoundSufficient(null);
		}
		String upperBoundSufficientValue = upperBoundSufficientEl.getValue();
		if (StringHelper.containsNonWhitespace(upperBoundSufficientValue)) {
			double upperBoundSufficient = Double.parseDouble(upperBoundSufficientValue);
			rubric.setUpperBoundSufficient(upperBoundSufficient);
		} else {
			rubric.setUpperBoundSufficient(null);
		}
		
		boolean startGoodRating = goodRatingEl.isOneSelected() && GOOD_RATING_START_KEY.equals(goodRatingEl.getSelectedKey())
				? true
				: false;
		rubric.setStartGoodRating(startGoodRating);
		
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		rubric.setMandatory(mandatory);
	}
	
	private static final class RatingPlaceholder {
		
		private final double lowerBoundInsufficient;
		private final double upperBoundInsufficient;
		private final double lowerBoundNeutral;
		private final double upperBoundNeutral;
		private final double lowerBoundSufficient;
		private final double upperBoundSufficient;
		
		RatingPlaceholder(double lowerBoundInsufficient, double upperBoundInsufficient, double lowerBoundNeutral,
				double upperBoundNeutral, double lowerBoundSufficient, double upperBoundSufficient) {
			this.lowerBoundInsufficient = lowerBoundInsufficient;
			this.upperBoundInsufficient = upperBoundInsufficient;
			this.lowerBoundNeutral = lowerBoundNeutral;
			this.upperBoundNeutral = upperBoundNeutral;
			this.lowerBoundSufficient = lowerBoundSufficient;
			this.upperBoundSufficient = upperBoundSufficient;
		}

		double getLowerBoundInsufficient() {
			return lowerBoundInsufficient;
		}

		double getUpperBoundInsufficient() {
			return upperBoundInsufficient;
		}

		double getLowerBoundNeutral() {
			return lowerBoundNeutral;
		}

		double getUpperBoundNeutral() {
			return upperBoundNeutral;
		}

		double getLowerBoundSufficient() {
			return lowerBoundSufficient;
		}

		double getUpperBoundSufficient() {
			return upperBoundSufficient;
		}
		
	}
}
