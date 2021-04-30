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
package org.olat.modules.forms.rules.ui;

import java.util.Arrays;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ui.PageEditorUIFactory;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Action;
import org.olat.modules.forms.model.xml.Container;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.VisibilityAction;

/**
 * 
 * Initial date: 8 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VisibilityActionFragement implements ActionEditorFragment {

	private FormLayoutContainer ruleCont;
	private SingleSelection elementEl;
	
	private final FormUIFactory uifactory;
	private VisibilityAction action;
	private final Form form;

	public VisibilityActionFragement(FormUIFactory uifactory, VisibilityAction action, Form form) {
		this.uifactory = uifactory;
		this.action = action;
		this.form = form;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Translator translator = formLayout.getTranslator();
		
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/visibility.html";
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		ruleCont = FormLayoutContainer
				.createCustomFormLayout("visibility." + id, translator, page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		ruleCont.getFormItemComponent().contextPut("id", id);
	
		KeyValues conditionKV = new KeyValues();
		for (AbstractElement element : form.getElements()) {
			if (element instanceof Container) {
				Container container = (Container)element;
				String value = StringHelper.containsNonWhitespace(container.getContainerSettings().getName())
						? container.getContainerSettings().getName()
						: PageEditorUIFactory.formatUntitled(translator, container.getId());
				value = Formatter.truncate(value, 40);
				conditionKV.add(KeyValues.entry(container.getId(), value));
			}
		}
		elementEl = uifactory.addDropdownSingleselect("element." + id, null, ruleCont, conditionKV.keys(),
				conditionKV.values(), null);
		elementEl.setDomReplacementWrapperRequired(false);
		elementEl.addActionListener(FormEvent.ONCHANGE);
		if (action != null) {
			String elementId = action.getElementId();
			if (Arrays.asList(elementEl.getKeys()).contains(elementId)) {
				elementEl.select(elementId, true);
			} else {
				elementEl.enableNoneSelection(translator.translate("element.deleted"));
				ruleCont.setErrorKey("error.element.not.available", null);
			}
		} else if (elementEl.getKeys().length > 0) {
			elementEl.select(elementEl.getKeys()[0], true);
		}
		
		if (elementEl.getKeys().length == 0) {
			ruleCont = FormLayoutContainer.createBareBoneFormLayout("choice." + id, translator);
			ruleCont.setRootForm(formLayout.getRootForm());
			formLayout.add(ruleCont);
			ruleCont.getFormItemComponent().contextPut("id", id);
			uifactory.addStaticTextElement("element." + id, null,
					translator.translate("no.action.available"), ruleCont);
		}
		
		return ruleCont;
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == elementEl) {
			elementEl.disableNoneSelection();
			ruleCont.clearError();
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		ruleCont.clearError();
		if (!elementEl.isOneSelected()) {
			ruleCont.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public Action getAction() {
		if (action == null) {
			action = new VisibilityAction();
			action.setId(UUID.randomUUID().toString());
		}
		
		action.setElementId(elementEl.getSelectedKey());
		
		return action;
	}

}
