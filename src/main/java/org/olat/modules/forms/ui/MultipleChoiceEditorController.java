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

import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
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
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.ChoiceDataModel.ChoiceCols;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceEditorController extends FormBasicController implements PageElementEditorController {

	private static final String WITH_OTHER_KEY = "multiple.choice.with.others.enabled";
	private static final String[] WITH_OTHER_KEYS = new String[] {WITH_OTHER_KEY};
	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private MultipleSelectionElement withOthersEl;
	private FormLink addChoiceEl;
	private FlexiTableElement tableEl;
	private ChoiceDataModel dataModel;

	private MultipleChoiceController multipleChoiceCtrl;
	private CloseableModalController cmc;
	private ChoiceController choiceValueCtrl;
	
	private final MultipleChoice multipleChoice;
	private boolean editMode = false;
	private final boolean restrictedEdit;
	
	public MultipleChoiceEditorController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice, boolean restrictedEdit) {
		super(ureq, wControl, "multiple_choice_editor");
		this.multipleChoice = multipleChoice;
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
		multipleChoiceCtrl = new MultipleChoiceController(ureq, getWindowControl(), multipleChoice);
		formLayout.add("preview", multipleChoiceCtrl.getInitialFormItem());

		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("sc_settings_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		// withOthers
		withOthersEl = uifactory.addCheckboxesVertical("mc_others_" + postfix, "multiple.choice.with.others",
				settingsCont, WITH_OTHER_KEYS, new String[] { translate(WITH_OTHER_KEY) }, null, null, 1);
		withOthersEl.select(WITH_OTHER_KEY, multipleChoice.isWithOthers());
		withOthersEl.addActionListener(FormEvent.ONCHANGE);
		withOthersEl.setEnabled(!restrictedEdit);
		
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
		
		addChoiceEl = uifactory.addFormLink("choice.add", flc, Link.BUTTON);
		addChoiceEl.setIconLeftCSS("o_icon o_icon_add");
		addChoiceEl.setVisible(!restrictedEdit);
	}
	
	private void loadModel() {
		dataModel.setObjects(multipleChoice.getChoices().asList());
		tableEl.reset();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (withOthersEl == source) {
			doSetWithOthers();
			multipleChoiceCtrl.updateForm();
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
			multipleChoiceCtrl.updateForm();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (choiceValueCtrl == source) {
			if (event == Event.DONE_EVENT) {
				multipleChoice.getChoices().addNotPresent(choiceValueCtrl.getChoice());
				loadModel();
				multipleChoiceCtrl.updateForm();
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

	private void doSetWithOthers() {
		boolean withOthers = withOthersEl.getSelectedKeys().contains(WITH_OTHER_KEY);
		multipleChoice.setWithOthers(withOthers);
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
		multipleChoice.getChoices().swap(index - 1, index);
		loadModel();
	}

	private void doDown(int index) {
		multipleChoice.getChoices().swap(index, index + 1);
		loadModel();
	}

	private void doDelete(int index) {
		Choice choice = dataModel.getObject(index);
		multipleChoice.getChoices().remove(choice);
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
	}

	@Override
	protected void doDispose() {
		//
	}

}
