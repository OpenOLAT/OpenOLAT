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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;
import org.olat.modules.portfolio.ui.editor.event.ClosePartEvent;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricEditorController extends FormBasicController implements PageElementEditorController {
	
	private static AtomicInteger count = new AtomicInteger();
	private final Rubric rubric;
	private boolean editMode = false;
	private final boolean restrictedEdit;
	private RubricController rubricCtrl;
	
	private final String[] sliderTypeKeys = new String[] { SliderType.discrete.name(), SliderType.discrete_slider.name(), SliderType.continuous.name() };
	private final String[] sliderStepKeys = new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10" };
	
	private List<StepLabelColumn> stepLabels;
	private List<SliderRow> sliders = new ArrayList<>();
	private Map<Integer,StepLabelColumn> stepToColumns = new HashMap<>();
	
	private FormLink saveButton;
	private SingleSelection typeEl;
	private SingleSelection stepsEl;
	private FormLink addSliderButton;
	private FormLayoutContainer settingsLayout;
	
	public RubricEditorController(UserRequest ureq, WindowControl wControl, Rubric rubric, boolean restrictedEdit) {
		super(ureq, wControl, "rubric_editor");
		this.rubric = rubric;
		this.restrictedEdit = restrictedEdit;

		initForm(ureq);
		setEditMode(editMode);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		rubricCtrl = new RubricController(ureq, getWindowControl(), rubric, mainForm);
		listenTo(rubricCtrl);
		formLayout.add("rubric", rubricCtrl.getInitialFormItem());
		
		settingsLayout = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsLayout.setRootForm(mainForm);
		formLayout.add("settings", settingsLayout);

		String[] sliderTypeValues = new String[] { translate("slider.discrete"), translate("slider.discrete.slider"), translate("slider.continuous") };
		typeEl = uifactory.addDropdownSingleselect("slider.type." + count.incrementAndGet(), "slider.type", settingsLayout, sliderTypeKeys, sliderTypeValues, null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		typeEl.setEnabled(!restrictedEdit);
		boolean typeSelected = false;
		if(rubric != null && rubric.getSliderType() != null) {
			for(String sliderTypeKey:sliderTypeKeys) {
				if(sliderTypeKey.equals(rubric.getSliderType().name())) {
					typeEl.select(sliderTypeKey, true);
					typeSelected = true;
				}
			}
		}
		if(!typeSelected) {
			typeEl.select(sliderTypeKeys[0], true);
		}
		
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
		updateTypeSettings();
		updateSteps();
		
		for(Slider slider:rubric.getSliders()) {
			SliderRow row = forgeSliderRow(slider);
			sliders.add(row);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("sliders", sliders);
		}
		
		long postfix = CodeHelper.getRAMUniqueID();
		saveButton = uifactory.addFormLink("save_" + postfix, "save", null, formLayout, Link.BUTTON);
		if(!restrictedEdit) {
			addSliderButton = uifactory.addFormLink("add.slider." + postfix, "add.slider", null, formLayout, Link.BUTTON);
			addSliderButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("postfix", Long.toString(postfix));
		}
	}
	
	private void updateSteps() {
		List<StepLabelColumn> stepLabelColumns = new ArrayList<>();
		if(stepsEl.isVisible() && stepsEl.isOneSelected()
				&& (typeEl.isSelected(0) || typeEl.isSelected(1))) {
			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			for(int i=0; i<steps; i++) {
				Integer step = new Integer(i);
				StepLabelColumn col = stepToColumns.get(step);
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
				
				stepLabelColumns.add(col);
			}
			
			int stepInPercent = Math.round(90.0f / steps);//90 is empirically choose to not make a second line
			flc.contextPut("stepInPercent", stepInPercent);
		}
		stepLabels = stepLabelColumns;
		flc.contextPut("stepLabels", stepLabelColumns);
	}
	
	private void updateTypeSettings() {
		if(!typeEl.isOneSelected()) return;
		
		SliderType selectedType = SliderType.valueOf(typeEl.getSelectedKey());
		if(selectedType == SliderType.discrete || selectedType == SliderType.discrete_slider) {
			stepsEl.setVisible(true);
		} else if(selectedType == SliderType.continuous) {
			stepsEl.setVisible(false);
		}
	}
	
	private SliderRow forgeSliderRow(Slider slider) {
		String startLabel = slider.getStartLabel();
		TextElement startLabelEl = uifactory.addTextElement("start.label." + count.incrementAndGet(), "start.label", 256, startLabel, flc);
		startLabelEl.setDomReplacementWrapperRequired(false);
		String endLabel = slider.getEndLabel();
		TextElement endLabelEl = uifactory.addTextElement("end.label." + count.incrementAndGet(), "end.label", 256, endLabel, flc);
		endLabelEl.setDomReplacementWrapperRequired(false);

		SliderRow row = new SliderRow(slider, startLabelEl, endLabelEl);
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
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSliderButton == source) {
			doAddSlider();
		} else if(typeEl == source) {
			updateTypeSettings();
			updateSteps();
		} else if(stepsEl == source) {
			updateSteps();
		} else if(saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if("delete_slider".equals(button.getCmd())) {
				doRemoveSlider((SliderRow)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRemoveSlider(SliderRow row) {
		updateSteps();
		sliders.remove(row);
		rubric.getSliders().remove(row.getSlider());
		flc.setDirty(true);
	}
	
	private void doAddSlider() {
		Slider slider = new Slider();
		slider.setId(UUID.randomUUID().toString());
		rubric.getSliders().add(slider);
		SliderRow row = forgeSliderRow(slider);
		sliders.add(row);
		flc.setDirty(true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitFields();
		commitStepLabels();

		String selectedType = typeEl.getSelectedKey();
		SliderType type =  SliderType.valueOf(selectedType);
		rubric.setSliderType(type);
		if(type == SliderType.continuous) {
			rubric.setStart(1);
			rubric.setEnd(100);
			rubric.setSteps(100);
		} else {
			int steps = Integer.parseInt(stepsEl.getSelectedKey());
			rubric.setStart(1);
			rubric.setEnd(steps);
			rubric.setSteps(steps);
		}
		
		for(Iterator<Slider> sliderIt=rubric.getSliders().iterator(); sliderIt.hasNext(); ) {
			Slider slider = sliderIt.next();
			if(!StringHelper.containsNonWhitespace(slider.getStartLabel()) && !StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				sliderIt.remove();
			}
		}
		
		rubricCtrl.updateForm();
		
		fireEvent(ureq, new ChangePartEvent(rubric));
		fireEvent(ureq, new ClosePartEvent(rubric));
	}
	
	private void commitStepLabels() {
		if(!typeEl.isOneSelected()) return;
		
		SliderType selectedType = SliderType.valueOf(typeEl.getSelectedKey());
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
		for(SliderRow row:sliders) {
			String start = row.getStartLabelEl().getValue();
			String end = row.getEndLabelEl().getValue();
			
			if(StringHelper.containsNonWhitespace(start)) {
				row.getSlider().setStartLabel(start);
			} else {
				row.getSlider().setStartLabel(null);
			}
			if(StringHelper.containsNonWhitespace(end)) {
				row.getSlider().setEndLabel(end);
			} else {
				row.getSlider().setEndLabel(null);
			}
		}
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
	}
	
	public class SliderRow {
		
		private final TextElement startLabelEl;
		private final TextElement endLabelEl;
		private FormLink deleteButton;
		
		private final Slider slider;
		
		public SliderRow(Slider slider, TextElement startLabelEl, TextElement endLabelEl) {
			this.slider = slider;
			this.startLabelEl = startLabelEl;
			this.endLabelEl = endLabelEl;
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
		
		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
	}
}
