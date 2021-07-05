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

import static org.olat.core.gui.components.updown.UpDown.Layout.LINK_HORIZONTAL;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.SingleChoice.Presentation;
import org.olat.modules.forms.ui.ChoiceDataModel.ChoiceCols;

/**
 * 
 * Initial date: 10.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceEditorController extends FormBasicController implements PageElementEditorController {

	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	private static final String CMD_DELETE = "delete";
	
	private TextElement nameEl;
	private SingleSelection presentationEl;
	private SingleSelection obligationEl;
	private FormLink addChoiceEl;
	private FlexiTableElement tableEl;
	private ChoiceDataModel dataModel;

	private SingleChoiceController singleChoiceCtrl;
	
	private final SingleChoice singleChoice;
	private boolean editMode = false;
	private boolean restrictedEdit;
	
	public SingleChoiceEditorController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice, boolean restrictedEdit) {
		super(ureq, wControl, "single_choice_editor");
		this.singleChoice = singleChoice;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
		setEditMode(editMode);
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
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		singleChoiceCtrl = new SingleChoiceController(ureq, getWindowControl(), singleChoice);
		formLayout.add("preview", singleChoiceCtrl.getInitialFormItem());

		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("sc_settings_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, singleChoice.getName(), settingsCont);
		nameEl.addActionListener(FormEvent.ONCHANGE);
		
		// presentation
		presentationEl = uifactory.addRadiosHorizontal("sc_pres_" + postfix, "single.choice.presentation", settingsCont,
				getPresentationKeys(), getPresentationValues());
		if (Arrays.asList(Presentation.values()).contains(singleChoice.getPresentation())) {
			presentationEl.select(singleChoice.getPresentation().name(), true);
		}
		presentationEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosHorizontal("obli_" + CodeHelper.getRAMUniqueID(), "obligation", settingsCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, singleChoice.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !singleChoice.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCLICK);
		
		// choices
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.move));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.value));
		if (!restrictedEdit) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.delete, CMD_DELETE,
					new CSSIconFlexiCellRenderer("o_icon o_icon-lg o_icon_delete_item")));
		}
		
		dataModel = new ChoiceDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "choices", dataModel, getTranslator(), settingsCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setLabel("choice.values", null);
		loadModel();
		
		addChoiceEl = uifactory.addFormLink("choice.add", settingsCont, Link.BUTTON);
		addChoiceEl.setElementCssClass("o_sel_add_single_choice");
		addChoiceEl.setIconLeftCSS("o_icon o_icon_add");
		addChoiceEl.setVisible(!restrictedEdit);
	}
	
	private void loadModel() {
		List<Choice> choices = singleChoice.getChoices().asList();
		List<ChoiceRow> rows = new ArrayList<>(choices.size());
		for (int i = 0; i < choices.size(); i++) {
			Choice choice = choices.get(i);
			
			// move
			UpDown upDown = UpDownFactory.createUpDown("ud_" + CodeHelper.getRAMUniqueID(), LINK_HORIZONTAL,
					flc.getFormItemComponent(), this);
			upDown.setUserObject(choice);
			if (i == 0) {
				upDown.setTopmost(true);
			}
			if (i == choices.size() - 1) {
				upDown.setLowermost(true);
			}
			
			// value
			TextElement valueEl = uifactory.addTextElement("o_value_" + CodeHelper.getRAMUniqueID(), null, 255, null, flc);
			valueEl.setValue(choice.getValue());
			valueEl.addActionListener(FormEvent.ONCHANGE);
			
			ChoiceRow choiceRow = new ChoiceRow(choice, upDown, valueEl);
			rows.add(choiceRow);
		}
		dataModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (nameEl == source || presentationEl == source || obligationEl == source || source instanceof TextElement) {
			doSave();
		} else if (addChoiceEl == source) {
			doAddChoice();
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				int index = se.getIndex();
				if (CMD_DELETE.equals(cmd)) {
					doDelete(index);
				}
			}
			singleChoiceCtrl.updateForm();
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent) {
			UpDownEvent ude = (UpDownEvent) event;
			doMove((Choice)ude.getUserObject(), ude.getDirection());
			singleChoiceCtrl.updateForm();
		} 
		super.event(ureq, source, event);
	}
	
	private void doSave() {
		doSaveSingleChoice();
		doSaveChoices();
		singleChoiceCtrl.updateForm();
	}

	private void doSaveSingleChoice() {
		singleChoice.setName(nameEl.getValue());
		
		Presentation presentation = null;
		if (presentationEl.isOneSelected()) {
			String selectedKey = presentationEl.getSelectedKey();
			presentation = Presentation.valueOf(selectedKey);
		}
		singleChoice.setPresentation(presentation);
		
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		singleChoice.setMandatory(mandatory);
	}
	
	private void doSaveChoices() {
		for (ChoiceRow choiceRow : dataModel.getObjects()) {
			TextElement valueEl = choiceRow.getValueEl();
			String value = valueEl.getValue();
			choiceRow.getChoice().setValue(value);
		}
	}

	private void doAddChoice() {
		Choice choice = new Choice();
		choice.setId(UUID.randomUUID().toString());
		choice.setValue(translate("choice.example"));
		singleChoice.getChoices().addNotPresent(choice);
		loadModel();
		singleChoiceCtrl.updateForm();
	}

	private void doMove(Choice choice, Direction direction) {
		Integer index = singleChoice.getChoices().getIndex(choice);
		if (index != null) {
			if (Direction.UP.equals(direction)) {
				doUp(index);
			} else if (Direction.DOWN.equals(direction)) {
				doDown(index);
			}
		}
	}

	private void doUp(int index) {
		singleChoice.getChoices().swap(index - 1, index);
		loadModel();
	}

	private void doDown(int index) {
		singleChoice.getChoices().swap(index, index + 1);
		loadModel();
	}

	private void doDelete(int index) {
		ChoiceRow row = dataModel.getObject(index);
		singleChoice.getChoices().remove(row.getChoice());
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public String[] getPresentationKeys() {
		return Arrays.stream(Presentation.values())
				.map(Presentation::name)
				.toArray(String[]::new);
	}
	
	public String[] getPresentationValues() {
		return Arrays.stream(Presentation.values())
				.map(type -> "single.choice.presentation." + type.name().toLowerCase())
				.map(i18n -> getTranslator().translate(i18n))
				.toArray(String[]::new);
	}

}
