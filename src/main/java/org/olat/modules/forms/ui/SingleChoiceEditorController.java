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

import java.util.Arrays;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.SingleChoice.Presentation;
import org.olat.modules.forms.ui.ChoiceDataModel.ChoiceCols;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;

/**
 * 
 * Initial date: 10.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceEditorController extends FormBasicController implements PageElementEditorController {

	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private SingleSelection presentationEl;
	private FormLink addChoiceEl;
	private FlexiTableElement tableEl;
	private ChoiceDataModel dataModel;

	private SingleChoiceController singleChoiceCtrl;
	private CloseableModalController cmc;
	private ChoiceController choiceValueCtrl;
	
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
		
		// presentation
		presentationEl = uifactory.addDropdownSingleselect("sc_pres_" + postfix, "single.choice.presentation", settingsCont, getPresentationKeys(),
				getPresentationValues(), null);
		if (Arrays.asList(Presentation.values()).contains(singleChoice.getPresentation())) {
			presentationEl.select(singleChoice.getPresentation().name(), true);
		}
		presentationEl.addActionListener(FormEvent.ONCHANGE);
		
		// choices
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.up, CMD_UP,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_UP, "o_icon o_icon-lg o_icon_move_up"),
						null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.down, CMD_DOWN,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_DOWN, "o_icon o_icon-lg o_icon_move_down"),
						null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.value));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.edit, CMD_EDIT,
				new StaticFlexiCellRenderer("", CMD_EDIT, "o_icon o_icon-lg o_icon_edit")));
		if (!restrictedEdit) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.delete, CMD_DELETE,
					new StaticFlexiCellRenderer("", CMD_DELETE, "o_icon o_icon-lg o_icon_delete_item")));
		}
		
		dataModel = new ChoiceDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "choices", dataModel, getTranslator(), settingsCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setLabel("choice.values", null);
		loadModel();
		
		if (!restrictedEdit) {
			addChoiceEl = uifactory.addFormLink("choice.add", flc, Link.BUTTON);
			addChoiceEl.setIconLeftCSS("o_icon o_icon_add");
		}
	}
	
	private void loadModel() {
		dataModel.setObjects(singleChoice.getChoices().asList());
		tableEl.reset();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (presentationEl == source) {
			doSetPresentation();
			singleChoiceCtrl.updateForm();
		} else if (addChoiceEl == source) {
			doAddChoice(ureq);
		}  else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				int index = se.getIndex();
				if (CMD_UP.equals(cmd)) {
					doUp(index);	
				} else if (CMD_DOWN.equals(cmd)) {
					doDown(index);
				} else if (CMD_EDIT.equals(cmd)) {
					doEditChoice(ureq, index);
				} else if (CMD_DELETE.equals(cmd)) {
					doDelete(index);
				}
			}
			singleChoiceCtrl.updateForm();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (choiceValueCtrl == source) {
			if (event == Event.DONE_EVENT) {
				singleChoice.getChoices().addNotPresent(choiceValueCtrl.getChoice());
				loadModel();
				singleChoiceCtrl.updateForm();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(choiceValueCtrl);
		removeAsListenerAndDispose(cmc);
		choiceValueCtrl = null;
		cmc = null;
	}

	private void doSetPresentation() {
		Presentation presentation = null;
		if (presentationEl.isOneSelected()) {
			String selectedKey = presentationEl.getSelectedKey();
			presentation = Presentation.valueOf(selectedKey);
		}
		singleChoice.setPresentation(presentation);
	}

	private void doAddChoice(UserRequest ureq) {
		Choice choice = new Choice();
		choice.setId(UUID.randomUUID().toString());
		String title = translate("choice.add");
		doEditChoice(ureq, choice, title);
	}

	private void doEditChoice(UserRequest ureq, int index) {
		Choice choice = dataModel.getObject(index);
		String title = translate("choice.edit.value");
		doEditChoice(ureq, choice, title);
	}
	
	private void doEditChoice(UserRequest ureq, Choice choice, String title) {
		choiceValueCtrl = new ChoiceController(ureq, getWindowControl(), choice);
		listenTo(choiceValueCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", choiceValueCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
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
		Choice choice = dataModel.getObject(index);
		singleChoice.getChoices().remove(choice);
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
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
