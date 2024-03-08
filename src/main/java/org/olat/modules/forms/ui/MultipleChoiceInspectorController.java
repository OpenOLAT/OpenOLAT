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

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.MultipleChoice.Presentation;
import org.olat.modules.forms.ui.ChoiceDataModel.ChoiceCols;

import org.springframework.beans.factory.annotation.Autowired;

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

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private TextElement nameEl;
	private SingleSelection presentationEl;
	private MultipleSelectionElement withOthersEl;
	private SingleSelection obligationEl;
	
	private final MultipleChoice multipleChoice;
	private final boolean restrictedEdit;

	@Autowired
	private ColorService colorService;

	public MultipleChoiceInspectorController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice, boolean restrictedEdit) {
		super(ureq, wControl, "multiple_choice_inspector");
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
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);

		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, multipleChoice.getName(), layoutCont);
		nameEl.addActionListener(FormEvent.ONCHANGE);

		// presentation
		SelectionValues presentationKV = new SelectionValues();
		Arrays.stream(Presentation.values()).forEach(presentation -> presentationKV.add(entry(
				presentation.name(),
				translate("single.choice.presentation." + presentation.name().toLowerCase()))));
		presentationEl = uifactory.addRadiosVertical("mc_pres", "single.choice.presentation",
				layoutCont, presentationKV.keys(), presentationKV.values());
		if (Arrays.asList(Presentation.values()).contains(multipleChoice.getPresentation())) {
			presentationEl.select(multipleChoice.getPresentation().name(), true);
		}
		presentationEl.addActionListener(FormEvent.ONCHANGE);

		// withOthers
		withOthersEl = uifactory.addCheckboxesVertical("mc_others", "multiple.choice.with.others",
				layoutCont, WITH_OTHER_KEYS, new String[] { translate(WITH_OTHER_KEY) }, null, null, 1);
		withOthersEl.select(WITH_OTHER_KEY, multipleChoice.isWithOthers());
		withOthersEl.addActionListener(FormEvent.ONCHANGE);
		withOthersEl.setEnabled(!restrictedEdit);

		// Mandatory
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_" + CodeHelper.getRAMUniqueID(), "obligation", layoutCont,
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

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		Translator translator = Util.createPackageTranslator(PageElementTarget.class, getLocale());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, translator, uifactory, getLayoutSettings(), velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings() {
		if (multipleChoice.getLayoutSettings() != null) {
			return multipleChoice.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (multipleChoice.getAlertBoxSettings() != null) {
			return multipleChoice.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (nameEl == source || presentationEl == source || source == withOthersEl || source == obligationEl
				|| source instanceof TextElement) {
			doSave(ureq);
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
	}

	private void doSave(UserRequest ureq) {
		doSaveMultipleChoice();
		fireEvent(ureq, new ChangePartEvent(multipleChoice));
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

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		multipleChoice.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(multipleChoice));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		multipleChoice.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(multipleChoice));

		getInitialComponent().setDirty(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
