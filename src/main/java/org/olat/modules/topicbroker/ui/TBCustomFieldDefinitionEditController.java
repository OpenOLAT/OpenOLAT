/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.ui;

import java.util.Arrays;
import java.util.Objects;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDefinitionEditController extends FormBasicController {
	
	private StaticTextElement identifierEl;
	private TextElement nameEl;
	private SingleSelection typeEl;
	private FormToggle addToggleButton;
	
	private final TBBroker broker;
	private TBCustomFieldDefinition definition;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBCustomFieldDefinitionEditController(UserRequest ureq, WindowControl wControl, TBBroker broker, TBCustomFieldDefinition definition) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.broker = broker;
		this.definition = definition;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (definition != null) {
			identifierEl = uifactory.addStaticTextElement("custom.field.def.identifier", definition.getIdentifier(), formLayout);
		}
		
		String title = definition != null? definition.getName(): null;
		nameEl = uifactory.addTextElement("custom.field.def.name", 100, title, formLayout);
		nameEl.setMandatory(true);
		
		SelectionValues typeSV = new SelectionValues();
		typeSV.add(SelectionValues.entry(TBCustomFieldType.text.name(), TBUIFactory.getTranslatedType(getTranslator(), TBCustomFieldType.text)));
		typeSV.add(SelectionValues.entry(TBCustomFieldType.file.name(), TBUIFactory.getTranslatedType(getTranslator(), TBCustomFieldType.file)));
		typeEl = uifactory.addRadiosHorizontal("custom.field.def.type", formLayout, typeSV.keys(), typeSV.values());
		typeEl.setEnabled(definition == null);
		if (definition != null && definition.getType() != null && Arrays.asList(typeEl.getKeys()).contains(definition.getType().name())) {
			typeEl.select(definition.getType().name(), true);
		} else {
			typeEl.select(TBCustomFieldType.text.name(), true);
		}
		
		addToggleButton = uifactory.addToggleButton("custom.field.def.in.table", "custom.field.def.in.table", translate("on"), translate("off"), formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if ((definition == null || !Objects.equals(definition.getName(), nameEl.getValue()))
				&& !topicBrokerService.isCustomFieldDefinitionNameAvailable(broker, nameEl.getValue())) {
			nameEl.setErrorKey("error.name.not.unique");
			allOk &= false;
		}
		
		return allOk;
	}
	
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String identifier = null;
		if (definition == null) {
			definition = topicBrokerService.createCustomFieldDefinition(getIdentity(), broker);
			identifier = definition.getIdentifier();
		}
		
		if (identifierEl != null) {
			identifier = identifierEl.getValue();
		}
		definition = topicBrokerService.updateCustomFieldDefinition(getIdentity(), definition, identifier,
				nameEl.getValue(), TBCustomFieldType.valueOf(typeEl.getSelectedKey()), addToggleButton.isOn());
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
