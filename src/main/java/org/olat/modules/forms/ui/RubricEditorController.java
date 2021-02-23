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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.forms.ui.EvaluationFormFormatter.oneDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String[] ENABLED_KEYS = new String[]{"on"};
	private static final String GOOD_RATING_END_KEY = "rubric.good.rating.end";
	private static final String GOOD_RATING_START_KEY = "rubric.good.rating.start";
	private final String[] GOOD_RATING_KEYS = new String[] {
			GOOD_RATING_END_KEY, GOOD_RATING_START_KEY
	};

	private static AtomicInteger count = new AtomicInteger();
	private final Rubric rubric;
	private boolean editMode = false;
	private final boolean restrictedEdit;
	private final boolean restrictedEditWeight;
	private RubricController rubricCtrl;
	
	private final String[] nameDisplayKeys = new String[] { NameDisplay.execution.name(), NameDisplay.report.name() };
	private final String[] sliderTypeKeys = new String[] { SliderType.discrete.name(), SliderType.discrete_slider.name(), SliderType.continuous.name() };
	private final String[] sliderStepKeys = new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10" };
	private final String[] showSurveyConfigKey = new String[] { "rubric.survey.configuration.show" };
	
	private List<StepLabelColumn> stepLabels = new ArrayList<>();
	private List<SliderRow> sliders = new ArrayList<>();
	
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
	private FormLink addSliderButton;
	private Boolean showEnd;
	private FormLink showEndButton;
	private FormLink hideEndButton;
	private FormLayoutContainer settingsLayout;

	public RubricEditorController(UserRequest ureq, WindowControl wControl, Rubric rubric, boolean restrictedEdit,
			boolean restrictedEditWeight) {
		super(ureq, wControl, "rubric_editor");
		this.rubric = rubric;
		this.restrictedEdit = restrictedEdit;
		this.restrictedEditWeight = restrictedEditWeight;
		this.showEnd = initShowEnd();

		initForm(ureq);
		setEditMode(editMode);
	}

	private Boolean initShowEnd() {
		for (Slider slider : rubric.getSliders()) {
			if (StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		rubricCtrl = new RubricController(ureq, getWindowControl(), rubric, mainForm);
		listenTo(rubricCtrl);
		formLayout.add("rubric", rubricCtrl.getInitialFormItem());
	}

	private void initEditForm(FormItemContainer formLayout) {
		settingsLayout = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsLayout.setRootForm(mainForm);
		formLayout.add("settings", settingsLayout);
		
		// slider type
		String[] sliderTypeValues = new String[] { translate("slider.discrete"), translate("slider.discrete.slider"), translate("slider.continuous") };
		sliderTypeEl = uifactory.addDropdownSingleselect("slider.type." + count.incrementAndGet(), "slider.type", settingsLayout, sliderTypeKeys, sliderTypeValues, null);
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
		stepsEl = uifactory.addDropdownSingleselect("slider.steps." + count.incrementAndGet(), "slider.steps", settingsLayout, sliderStepKeys, sliderStepKeys, null);
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
		
		// survey configs
		uifactory.addSpacerElement("rubric.survey.configuration.upper", settingsLayout, false);
		surveyConfigEl = uifactory.addCheckboxesHorizontal("rubric.survey.configuration", settingsLayout,
				showSurveyConfigKey, translateAll(getTranslator(), showSurveyConfigKey));
		surveyConfigEl.addActionListener(FormEvent.ONCHANGE);

		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, rubric.getName(), settingsLayout);
		nameEl.setHelpTextKey("rubric.name.help", null);
		
		// name display
		String[] nameDisplayValues = new String[] { translate("rubric.name.execution"), translate("rubric.name.report") };
		nameDisplayEl = uifactory.addCheckboxesHorizontal("rubric.name.display", settingsLayout, nameDisplayKeys, nameDisplayValues);
		nameDisplayEl.addActionListener(FormEvent.ONCHANGE);
		nameDisplayEl.setEvaluationOnlyVisible(true);
		for (NameDisplay nameDisplay : rubric.getNameDisplays()) {
			nameDisplayEl.select(nameDisplay.name(), true);
		}
		
		// scale type
		scaleTypeEl = uifactory.addDropdownSingleselect("scale.type." + count.incrementAndGet(), "rubric.scale.type",
				settingsLayout, ScaleType.getKeys(), ScaleType.getValues(getTranslator()), null);
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
				settingsLayout, GOOD_RATING_KEYS, translateAll(getTranslator(), GOOD_RATING_KEYS), null);
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
		settingsLayout.add("insufficient", insufficientCont);
		insufficientCont.setLabel("rubric.insufficient", null);
		insufficientCont.setHelpTextKey("rubric.rating.help", new String[] { translate("rubric.insufficient")} );
		String insufficientLowerBound = rubric.getLowerBoundInsufficient() != null
				? String.valueOf(rubric.getLowerBoundInsufficient())
				: null;
		lowerBoundInsufficientEl = uifactory.addTextElement("rubric.lower.bound.insufficient", null, 4,
				insufficientLowerBound, insufficientCont);
		lowerBoundInsufficientEl.setDomReplacementWrapperRequired(false);
		lowerBoundInsufficientEl.setDisplaySize(4);
		String insufficientUpperBound = rubric.getUpperBoundInsufficient() != null
				? String.valueOf(rubric.getUpperBoundInsufficient())
				: null;
		upperBoundInsufficientEl = uifactory.addTextElement("rubric.upper.bound.insufficient", null, 4,
				insufficientUpperBound, insufficientCont);
		upperBoundInsufficientEl.setDomReplacementWrapperRequired(false);
		upperBoundInsufficientEl.setDisplaySize(4);

		// neutral range
		String neutralPage = velocity_root + "/rubric_range_neutral.html";
		neutralCont = FormLayoutContainer.createCustomFormLayout("neutral", getTranslator(),
				neutralPage);
		neutralCont.setRootForm(mainForm);
		settingsLayout.add("neutral", neutralCont);
		neutralCont.setLabel("rubric.neutral", null);
		neutralCont.setHelpTextKey("rubric.rating.help", new String[] { translate("rubric.neutral")} );
		String neutralLowerBound = rubric.getLowerBoundNeutral() != null ? String.valueOf(rubric.getLowerBoundNeutral())
				: null;
		lowerBoundNeutralEl = uifactory.addTextElement("rubric.lower.bound.neutral", null, 4, neutralLowerBound, neutralCont);
		lowerBoundNeutralEl.setDomReplacementWrapperRequired(false);
		lowerBoundNeutralEl.setDisplaySize(4);
		String neutralUpperBound = rubric.getUpperBoundNeutral() != null ? String.valueOf(rubric.getUpperBoundNeutral())
				: null;
		upperBoundNeutralEl = uifactory.addTextElement("rubric.upper.bound.neutral", null, 4, neutralUpperBound, neutralCont);
		upperBoundNeutralEl.setDomReplacementWrapperRequired(false);
		upperBoundNeutralEl.setDisplaySize(4);

		// sufficient range
		String sufficientPage = velocity_root + "/rubric_range_sufficient.html";
		sufficientCont = FormLayoutContainer.createCustomFormLayout("sufficient", getTranslator(),
				sufficientPage);
		sufficientCont.setRootForm(mainForm);
		settingsLayout.add("sufficient", sufficientCont);
		sufficientCont.setLabel("rubric.sufficient", null);
		sufficientCont.setHelpTextKey("rubric.rating.help", new String[] { translate("rubric.sufficient")} );
		String sufficientLowerBound = rubric.getLowerBoundSufficient() != null
				? String.valueOf(rubric.getLowerBoundSufficient())
				: null;
		lowerBoundSufficientEl = uifactory.addTextElement("rubric.lower.bound.sufficient", null, 4, sufficientLowerBound,
				sufficientCont);
		lowerBoundSufficientEl.setDomReplacementWrapperRequired(false);
		lowerBoundSufficientEl.setDisplaySize(4);
		String sufficientUpperBound = rubric.getUpperBoundSufficient() != null
				? String.valueOf(rubric.getUpperBoundSufficient())
				: null;
		upperBoundSufficientEl = uifactory.addTextElement("rubric.upper.bound.sufficient", null, 4, sufficientUpperBound,
				sufficientCont);
		upperBoundSufficientEl.setDomReplacementWrapperRequired(false);
		upperBoundSufficientEl.setDisplaySize(4);
		updatePlaceholders();
		
		// no answer
		noAnswerEl = uifactory.addCheckboxesVertical("no.response." + count.incrementAndGet(),
				"rubric.no.response.enabled", settingsLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS), 1);
		noAnswerEl.setHelpTextKey("no.response.help", null);
		noAnswerEl.setEnabled(!restrictedEdit);
		
		uifactory.addSpacerElement("rubric.survey.configuration.upper", settingsLayout, false);

		updateTypeSettings();
		updateSteps();
		
		sliders.clear();
		for(Slider slider:rubric.getSliders()) {
			SliderRow row = forgeSliderRow(slider);
			sliders.add(row);
		}
		setUpDownVisibility();
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("sliders", sliders);
		}
		
		long postfix = CodeHelper.getRAMUniqueID();
		saveButton = uifactory.addFormLink("save_" + postfix, "save", null, formLayout, Link.BUTTON);
		if(!restrictedEdit) {
			addSliderButton = uifactory.addFormLink("add.slider." + postfix, "add.slider", null, formLayout, Link.BUTTON);
			addSliderButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");

			showEndButton = uifactory.addFormLink("show.end", "show.end", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			showEndButton.setIconLeftCSS("o_icon o_icon_eva_end_show");

			hideEndButton = uifactory.addFormLink("hide.end", "hide.end", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			hideEndButton.setIconLeftCSS("o_icon o_icon_eva_end_hide");
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("postfix", Long.toString(postfix));
		}
		
		doShowHideEnd();
		updateUI();
	}

	private void updateUI() {
		updateSurveyConfigUI();
	}

	private void updateSurveyConfigUI() {
		boolean isSurveyConfig = surveyConfigEl.isAtLeastSelected(1);
		nameEl.setVisible(isSurveyConfig);
		nameDisplayEl.setVisible(isSurveyConfig);
		scaleTypeEl.setVisible(isSurveyConfig);
		noAnswerEl.setVisible(isSurveyConfig);
		noAnswerEl.select(noAnswerEl.getKey(0), rubric.isNoResponseEnabled());
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
	
	private void doShowEnd() {
		showEnd = Boolean.TRUE;
		doShowHideEnd();
	}
	
	private void doHideEnd() {
		showEnd = Boolean.FALSE;
		doShowHideEnd();
	}

	private void doShowHideEnd() {
		flc.contextPut("showEnd", showEnd);
		flc.setDirty(true);
	}

	private void updateSteps() {
		List<StepLabelColumn> stepLabelColumns = new ArrayList<>();
		if (stepsEl.isVisible() && stepsEl.isOneSelected()
				&& (sliderTypeEl.isSelected(0) || sliderTypeEl.isSelected(1))) {
			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			for(int i=0; i<steps; i++) {
				Integer step = Integer.valueOf(i);
				StepLabelColumn col = stepLabels.size() > step? stepLabels.get(step): null;
				if(col == null) {
					String label = "";
					if(rubric.getStepLabels() != null && i<rubric.getStepLabels().size()) {
						label = rubric.getStepLabels().get(i).getLabel();
					}
					
					TextElement textEl = uifactory.addTextElement("steplabel_" + count.incrementAndGet(), "steplabel_" + count.incrementAndGet(), null, 256, label, flc);
					textEl.setDomReplacementWrapperRequired(false);
					textEl.setDisplaySize(4);
					col = new StepLabelColumn(i, textEl);
				}
				if (scaleTypeEl.isOneSelected()) {
					String selectedScaleTypeKey = scaleTypeEl.getSelectedKey();
					ScaleType scaleType = ScaleType.getEnum(selectedScaleTypeKey);
					double stepValue = scaleType.getStepValue(steps, i + 1);
					col.setExampleText(String.valueOf(stepValue));
				} else {
					col.removeExampleText();
				}
				stepLabelColumns.add(col);
			}
			
			int stepInPercent = Math.round(90.0f / steps);//90 is empirically choose to not make a second line
			flc.contextPut("stepInPercent", stepInPercent);
		}
		stepLabels = stepLabelColumns;
		flc.contextPut("stepLabels", stepLabelColumns);
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
	
	private void updateSliders() {
		for (SliderRow row: sliders) {
			row.setSliderEl(createSliderEl());
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

	private SliderRow forgeSliderRow(Slider slider) {
		String startLabel = slider.getStartLabel();
		RichTextElement startLabelEl = uifactory.addRichTextElementForStringDataMinimalistic(
				"start.label." + count.incrementAndGet(), null, startLabel, 4, -1, flc, getWindowControl());
		startLabelEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);

		String endLabel = slider.getEndLabel();
		RichTextElement endLabelEl = uifactory.addRichTextElementForStringDataMinimalistic(
				"end.label." + count.incrementAndGet(), null, endLabel, 4, -1, flc, getWindowControl());
		endLabelEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		
		// weight
		String weight = slider.getWeight() != null? slider.getWeight().toString(): "";
		TextElement weightEl = uifactory.addTextElement("weight" + count.incrementAndGet(), null, 4, weight, flc);
		weightEl.setElementCssClass("o_slider_weight");
		weightEl.setExampleKey("slider.weight", null);
		weightEl.setEnabled(!restrictedEditWeight);
		
		SliderRow row = new SliderRow(slider, startLabelEl, endLabelEl, weightEl, createSliderEl());
		
		FormLink upButton = uifactory.addFormLink("up." + count.incrementAndGet(), "up", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
		upButton.setDomReplacementWrapperRequired(false);
		upButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		upButton.setUserObject(row);
		row.setUpButton(upButton);
		
		FormLink downButton = uifactory.addFormLink("down." + count.incrementAndGet(), "down", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
		downButton.setDomReplacementWrapperRequired(false);
		downButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		downButton.setUserObject(row);
		row.setDownButton(downButton);
		
		if(!restrictedEdit) {
			FormLink deleteButton = uifactory.addFormLink("del." + count.incrementAndGet(), "delete_slider", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
			deleteButton.setDomReplacementWrapperRequired(false);
			deleteButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
			deleteButton.setUserObject(row);
			row.setDeleteButton(deleteButton);
			flc.contextPut("deleteButtons", Boolean.TRUE);
		}
		
		return row;
	}

	private FormItem createSliderEl() {
		SliderType selectedType = SliderType.valueOf(sliderTypeEl.getSelectedKey());
		if (selectedType == SliderType.discrete) {
			return createRadioEl();
		} else if (selectedType == SliderType.discrete_slider) {
			return createDescreteSliderEl();
		}
		return createContinousSliderEl();
	}

	private FormItem createRadioEl() {
		int start = 1;
		int steps = Integer.parseInt(stepsEl.getSelectedKey());
		int end = 1 + steps;
		
		double[] theSteps = new double[steps];
		String[] theKeys = new String[steps];
		String[] theValues = new String[steps];
		
		double step = (end - start + 1) / (double)steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			theKeys[i] = Double.toString(theSteps[i]);
			theValues[i] = "";
		}

		SingleSelection radioEl = uifactory.addRadiosVertical("slider_" + CodeHelper.getRAMUniqueID(), null, flc, theKeys, theValues);
		radioEl.setAllowNoSelection(true);
		radioEl.setDomReplacementWrapperRequired(false);
		int widthInPercent = Math.round(100.0f / steps) - 1;
		radioEl.setWidthInPercent(widthInPercent, true);
		return radioEl;
	}

	private FormItem createDescreteSliderEl() {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(1);
		sliderEl.setMaxValue( Integer.parseInt(stepsEl.getSelectedKey()));
		sliderEl.setStep(1);
		return sliderEl;
	}

	private FormItem createContinousSliderEl() {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(1);
		sliderEl.setMaxValue(100);
		return sliderEl;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		if (editMode) {
			initEditForm(flc);
		}
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (surveyConfigEl == source) {
			updateUI();
		} else if (addSliderButton == source) {
			doAddSlider();
		} else if (showEndButton == source) {
			doShowEnd();
		} else if (hideEndButton == source) {
			doHideEnd();
		} else if(sliderTypeEl == source) {
			updateTypeSettings();
			updateSteps();
			updateSliders();
			updatePlaceholders();
		} else if(stepsEl == source) {
			updateSteps();
			updateSliders();
			updatePlaceholders();
		} else if (scaleTypeEl == source) {
			updateSteps();
			updateSliders();
			updatePlaceholders();
		} else if (goodRatingEl == source) {
			updatePlaceholders();
		} else if(saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if ("up".equals(button.getCmd())) {
				doMoveUp((SliderRow)button.getUserObject());
			} else if ("down".equals(button.getCmd())) {
				doMoveDown((SliderRow)button.getUserObject());
			} else if ("delete_slider".equals(button.getCmd())) {
				doRemoveSlider((SliderRow)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doMoveUp(SliderRow slideRow) {
		int index = sliders.indexOf(slideRow);
		if (index > -1) {
			swapSliders(index - 1, index);
			setUpDownVisibility();
			flc.setDirty(true);
		}
	}

	private void doMoveDown(SliderRow slideRow) {
		int index = sliders.indexOf(slideRow);
		if (index > -1) {
			swapSliders(index, index + 1);
			setUpDownVisibility();
			flc.setDirty(true);
		}
	}

	private void swapSliders(int i, int j) {
		if(i >= 0 && j >= 0 && i < sliders.size() && j < sliders.size()) {
			SliderRow tempSlider = sliders.get(i);
			sliders.set(i, sliders.get(j));
			sliders.set(j, tempSlider);
		}
	}

	private void doRemoveSlider(SliderRow row) {
		updateSteps();
		sliders.remove(row);
		setUpDownVisibility();
		flc.setDirty(true);
	}
	
	private void doAddSlider() {
		Slider slider = new Slider();
		slider.setId(UUID.randomUUID().toString());
		SliderRow row = forgeSliderRow(slider);
		sliders.add(row);
		setUpDownVisibility();
		flc.setDirty(true);
	}
	
	private void setUpDownVisibility() {
		for (int i = 0; i < sliders.size(); i++) {
			SliderRow sliderRow = sliders.get(i);
			sliderRow.getUpButton().setEnabled(true);
			sliderRow.getDownButton().setEnabled(true);
			if (i == 0) {
				sliderRow.getUpButton().setEnabled(false);
			}
			if (i == sliders.size() -1) {
				sliderRow.getDownButton().setEnabled(false);
			}
		}
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
		double min = -101;
		double max = 101;
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
		
		for(SliderRow row:sliders) {
			TextElement weightEl = row.getWeightEl();
			weightEl.clearError();
			if (isInvalidInteger(weightEl.getValue(), 0, 1000)) {
				weightEl.setErrorKey("error.wrong.int", null);
				allOk = false;
			}
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
	
	private boolean isValidInteger(String val, int min, int max) {
		return !isInvalidDouble(val, min, max);
	}
	
	private boolean isInvalidInteger(String val, int min, int max) {
		boolean inside = true;
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				double value = Integer.parseInt(val);
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
		commitFields();
		commitStepLabels();

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
		
		rubricCtrl.updateForm();
		
		fireEvent(ureq, new ChangePartEvent(rubric));
		fireEvent(ureq, new ClosePartEvent(rubric));
	}
	
	private void commitStepLabels() {
		if(!sliderTypeEl.isOneSelected()) return;
		
		SliderType selectedType = SliderType.valueOf(sliderTypeEl.getSelectedKey());
		if(selectedType == SliderType.discrete || selectedType == SliderType.discrete_slider) {
			if(rubric.getStepLabels() == null) {
				rubric.setStepLabels(new ArrayList<>());
			}

			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			for(int i=0; i<stepLabels.size() && i<steps; i++) {
				StepLabelColumn stepLabel = stepLabels.get(i);
				if(i < rubric.getStepLabels().size()) {
					rubric.getStepLabels().get(i).setLabel(stepLabel.getStepLabelEl().getValue());
				} else {
					StepLabel label = new StepLabel();
					label.setId(UUID.randomUUID().toString());
					label.setLabel(stepLabel.getStepLabelEl().getValue());
					rubric.getStepLabels().add(label);
				}
			}
			
			if(rubric.getStepLabels().size() > steps) {
				List<StepLabel> labels = new ArrayList<>(rubric.getStepLabels().subList(0, steps));
				rubric.setStepLabels(labels);
			}
		} else {
			rubric.getStepLabels().clear();
		}
	}
	
	private void commitFields() {
		List<Slider> editedSliders = new ArrayList<>(sliders.size());
		for(SliderRow row:sliders) {
			String start = row.getStartLabelEl().getValue();
			if(StringHelper.containsNonWhitespace(start)) {
				row.getSlider().setStartLabel(start);
			} else {
				row.getSlider().setStartLabel(null);
			}

			String end = row.getEndLabelEl().getValue();
			if(StringHelper.containsNonWhitespace(end) && showEnd) {
				row.getSlider().setEndLabel(end);
			} else {
				row.getSlider().setEndLabel(null);
				row.endLabelEl.setValue(null);
			}
			
			Integer weight = getIntOrNull(row.getWeightEl());
			row.getSlider().setWeight(weight);
			if (row.getSlider().getStartLabel() != null || row.getSlider().getEndLabel() != null) {
				editedSliders.add(row.getSlider());
			}
		}
		rubric.setSliders(editedSliders);
	}
	
	private Integer getIntOrNull(TextElement weightEl) {
		return StringHelper.containsNonWhitespace(weightEl.getValue())
				&& isValidInteger(weightEl.getValue(), 0, 10000000)
				? Integer.parseInt(weightEl.getValue())
				: null;
	}
	
	public class StepLabelColumn {
		
		private final int step;
		private final TextElement stepLabelEl;
		
		public StepLabelColumn(int step, TextElement stepLabelEl) {
			this.step = step;
			this.stepLabelEl = stepLabelEl;
		}

		public int getStep() {
			return step;
		}

		public TextElement getStepLabelEl() {
			return stepLabelEl;
		}
		
		public void setExampleText(String example) {
			stepLabelEl.setExampleKey("rubric.scale.example.value", new String[] {example});
		}

		public void removeExampleText() {
			stepLabelEl.setExampleKey(null, null);
		}
	}
	
	public class SliderRow {
		
		private final TextElement startLabelEl;
		private final TextElement endLabelEl;
		private final TextElement weightEl;
		private FormLink upButton;
		private FormLink downButton;
		private FormLink deleteButton;
		private FormItem sliderEl;
		
		private final Slider slider;
		
		public SliderRow(Slider slider, TextElement startLabelEl, TextElement endLabelEl, TextElement weightEl, FormItem sliderEl) {
			this.slider = slider;
			this.startLabelEl = startLabelEl;
			this.endLabelEl = endLabelEl;
			this.weightEl  = weightEl;
			this.sliderEl = sliderEl;
		}

		public Slider getSlider() {
			return slider;
		}
		
		public TextElement getStartLabelEl() {
			return startLabelEl;
		}

		public TextElement getEndLabelEl() {
			return endLabelEl;
		}
		
		public TextElement getWeightEl() {
			return weightEl;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
		
		public FormLink getUpButton() {
			return upButton;
		}

		public void setUpButton(FormLink upButton) {
			this.upButton = upButton;
		}

		public FormLink getDownButton() {
			return downButton;
		}

		public void setDownButton(FormLink downButton) {
			this.downButton = downButton;
		}

		public FormItem getSliderEl() {
			return sliderEl;
		}

		public void setSliderEl(FormItem sliderEl) {
			this.sliderEl = sliderEl;
		}
		
		public String getSliderCss() {
			if (sliderEl instanceof SingleSelection) {
				return "o_slider_descrete_radio";
			}
			if (sliderEl instanceof SliderElement) {
				SliderElement sliderElement = (SliderElement) sliderEl;
				if (sliderElement.getStep() == 0) {
					return "o_slider_continous";
				}
				return "o_slider_descrete";
			}
			return "";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((slider == null) ? 0 : slider.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SliderRow other = (SliderRow) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (slider == null) {
				if (other.slider != null)
					return false;
			} else if (!slider.equals(other.slider))
				return false;
			return true;
		}

		private RubricEditorController getOuterType() {
			return RubricEditorController.this;
		}
		
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
