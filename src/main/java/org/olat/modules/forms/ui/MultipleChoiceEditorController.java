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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.ChoiceDataModel.ChoiceCols;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceEditorController extends FormBasicController implements PageElementEditorController {

	private static final String CMD_DELETE = "delete";
	
	private FormLink addChoiceEl;
	private FlexiTableElement tableEl;
	private ChoiceDataModel dataModel;
	
	private MultipleChoice multipleChoice;
	private final boolean restrictedEdit;
	
	public MultipleChoiceEditorController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice, boolean restrictedEdit) {
		super(ureq, wControl, "multiple_choice_editor");
		this.multipleChoice = multipleChoice;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);

		setBlockLayoutClass();
	}

	private void setBlockLayoutClass() {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(multipleChoice.getLayoutSettings(), true));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// choices
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.move));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.value));
		if (!restrictedEdit) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.delete, CMD_DELETE,
					new CSSIconFlexiCellRenderer("o_icon o_icon-lg o_icon_delete_item")));
		}
		
		dataModel = new ChoiceDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "choices", dataModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		loadModel();
		
		addChoiceEl = uifactory.addFormLink("choice.add", formLayout, Link.BUTTON);
		addChoiceEl.setElementCssClass("o_sel_add_multiple_choice");
		addChoiceEl.setIconLeftCSS("o_icon o_icon_add");
		addChoiceEl.setVisible(!restrictedEdit);
	}
	
	private void loadModel() {
		List<Choice> choices = multipleChoice.getChoices().asList();
		Map<String, ChoiceRow> rowsMap = dataModel.getObjects().stream()
				.collect(Collectors.toMap(ChoiceRow::getChoiceId, row -> row, (u, v) -> u));
		
		List<ChoiceRow> rows = new ArrayList<>(choices.size());
		for (int i = 0; i < choices.size(); i++) {
			Choice choice = choices.get(i);
			String choiceId = choice.getId();
			ChoiceRow choiceRow = rowsMap.get(choice.getId());
			UpDown upDown;
			if(choiceRow == null) {
				// move
				upDown = UpDownFactory.createUpDown("ud_" + choiceId, LINK_HORIZONTAL,
						flc.getFormItemComponent(), this);
				upDown.setUserObject(choice);
				// value
				TextElement valueEl = uifactory.addTextElement("o_value_" + choiceId, null, 255, null, flc);
				valueEl.setValue(choice.getValue());
				valueEl.addActionListener(FormEvent.ONCHANGE);
				valueEl.setDomReplacementWrapperRequired(false);

				choiceRow = new ChoiceRow(choice, upDown, valueEl);
			} else {
				upDown = choiceRow.getUpDown();
				choiceRow.getValueEl().setFocus(false);
			}
			
			boolean topMost = (i == 0);
			upDown.setTopmost(topMost);
			boolean lowerMost = (i == choices.size() - 1);
			upDown.setLowermost(lowerMost);
			
			rows.add(choiceRow);
		}
		dataModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof TextElement) {
			doSave(ureq);
		} else if (addChoiceEl == source) {
			doAddChoice();
			doSave(ureq);
		}  else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				int index = se.getIndex();
				if (CMD_DELETE.equals(cmd)) {
					doDelete(index);
					doSave(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof MultipleChoiceInspectorController && event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.isElement(multipleChoice) && changePartEvent.getElement() instanceof MultipleChoice multipleChoice) {
				this.multipleChoice = multipleChoice;
				setBlockLayoutClass();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent ude) {
			doMove((Choice)ude.getUserObject(), ude.getDirection());
			doSave(ureq);
		}
		super.event(ureq, source, event);
	}
	
	private void doSave(UserRequest ureq) {
		doSaveChoices();
		fireEvent(ureq, new ChangePartEvent(multipleChoice));
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
		multipleChoice.getChoices().addNotPresent(choice);
		loadModel();
	}

	private void doMove(Choice choice, Direction direction) {
		Integer index = multipleChoice.getChoices().getIndex(choice);
		if (index != null) {
			if (Direction.UP.equals(direction)) {
				doUp(index);
			} else if (Direction.DOWN.equals(direction)) {
				doDown(index);
			}
		}
	}

	private void doUp(int index) {
		if(index > 0) {
			multipleChoice.getChoices().swap(index - 1, index);
		}
		loadModel();
	}

	private void doDown(int index) {
		if(index + 1 < multipleChoice.getChoices().size()) {
			multipleChoice.getChoices().swap(index, index + 1);
		}
		loadModel();
	}

	private void doDelete(int index) {
		ChoiceRow row = dataModel.getObject(index);
		multipleChoice.getChoices().remove(row.getChoice());
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
