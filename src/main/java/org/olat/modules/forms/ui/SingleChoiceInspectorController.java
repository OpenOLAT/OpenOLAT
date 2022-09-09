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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.SingleChoice.Presentation;

/**
 * 
 * Initial date: 22 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	
	private TextElement nameEl;
	private SingleSelection presentationEl;
	private SingleSelection obligationEl;
	
	private final SingleChoice singleChoice;
	private boolean restrictedEdit;
	
	public SingleChoiceInspectorController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice, boolean restrictedEdit) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.singleChoice = singleChoice;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.formsinglechoice");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, singleChoice.getName(), formLayout);
		nameEl.addActionListener(FormEvent.ONCHANGE);
		
		// presentation
		presentationEl = uifactory.addRadiosVertical("sc_pres", "single.choice.presentation", formLayout,
				getPresentationKeys(), getPresentationValues());
		if (Arrays.asList(Presentation.values()).contains(singleChoice.getPresentation())) {
			presentationEl.select(singleChoice.getPresentation().name(), true);
		}
		presentationEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_o", "obligation", formLayout,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, singleChoice.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !singleChoice.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCLICK);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (nameEl == source || presentationEl == source || obligationEl == source || source instanceof TextElement) {
			doSave(ureq);
		}
	}

	private void doSave(UserRequest ureq) {
		doSaveSingleChoice();
		fireEvent(ureq, new ChangePartEvent(singleChoice));
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

	@Override
	protected void formOK(UserRequest ureq) {
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
