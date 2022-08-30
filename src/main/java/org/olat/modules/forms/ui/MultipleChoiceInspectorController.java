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

import java.util.Arrays;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.MultipleChoice.Presentation;
import org.olat.modules.forms.ui.ChoiceDataModel.ChoiceCols;

/**
 * 
 * Initial date: 22 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	private static final String WITH_OTHER_KEY = "multiple.choice.with.others.enabled";
	private static final String[] WITH_OTHER_KEYS = new String[] {WITH_OTHER_KEY};
	private static final String CMD_DELETE = "delete";
	
	private TextElement nameEl;
	private SingleSelection presentationEl;
	private MultipleSelectionElement withOthersEl;
	private SingleSelection obligationEl;
	
	private final MultipleChoice multipleChoice;
	private final boolean restrictedEdit;
	
	public MultipleChoiceInspectorController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice, boolean restrictedEdit) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.multipleChoice = multipleChoice;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.formmultiplechoice");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, multipleChoice.getName(), formLayout);
		nameEl.addActionListener(FormEvent.ONCHANGE);
		
		// presentation
		SelectionValues presentationKV = new SelectionValues();
		Arrays.stream(Presentation.values()).forEach(presentation -> presentationKV.add(entry(
				presentation.name(),
				translate("single.choice.presentation." + presentation.name().toLowerCase()))));
		presentationEl = uifactory.addRadiosVertical("mc_pres", "single.choice.presentation",
				formLayout, presentationKV.keys(), presentationKV.values());
		if (Arrays.asList(Presentation.values()).contains(multipleChoice.getPresentation())) {
			presentationEl.select(multipleChoice.getPresentation().name(), true);
		}
		presentationEl.addActionListener(FormEvent.ONCHANGE);
		
		// withOthers
		withOthersEl = uifactory.addCheckboxesVertical("mc_others", "multiple.choice.with.others",
				formLayout, WITH_OTHER_KEYS, new String[] { translate(WITH_OTHER_KEY) }, null, null, 1);
		withOthersEl.select(WITH_OTHER_KEY, multipleChoice.isWithOthers());
		withOthersEl.addActionListener(FormEvent.ONCHANGE);
		withOthersEl.setEnabled(!restrictedEdit);
		
		// Mandatory
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_" + CodeHelper.getRAMUniqueID(), "obligation", formLayout,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, multipleChoice.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !multipleChoice.isMandatory());
		obligationEl.addActionListener(FormEvent.ONCLICK);
		obligationEl.setEnabled(!restrictedEdit);
		
		// choices
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.move));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.value));
		if (!restrictedEdit) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceCols.delete, CMD_DELETE,
					new CSSIconFlexiCellRenderer("o_icon o_icon-lg o_icon_delete_item")));
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (nameEl == source || presentationEl == source || source == withOthersEl || source == obligationEl
				|| source instanceof TextElement) {
			doSave();
		}
	}
	
	private void doSave() {
		doSaveMultipleChoice();
	}
	
	private void doSaveMultipleChoice() {
		multipleChoice.setName(nameEl.getValue());
		
		Presentation presentation = null;
		if (presentationEl.isOneSelected()) {
			String selectedKey = presentationEl.getSelectedKey();
			presentation = Presentation.valueOf(selectedKey);
		}
		multipleChoice.setPresentation(presentation);
		
		boolean withOthers = withOthersEl.getSelectedKeys().contains(WITH_OTHER_KEY);
		multipleChoice.setWithOthers(withOthers);
		
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		multipleChoice.setMandatory(mandatory);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
