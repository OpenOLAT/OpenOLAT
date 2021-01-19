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
package org.olat.modules.quality.analysis.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.ui.PresentationEvent.Action;
import org.olat.modules.quality.ui.QualityUIFactory;

/**
 * 
 * Initial date: 02.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class PresentationController extends FormBasicController {

	private TextElement nameEl;
	private MultipleSelectionElement replaceEl;
	private FormSubmit createButton;
	private FormSubmit replaceButton;
	
	private final AnalysisPresentation presentation;
	
	PresentationController(UserRequest ureq, WindowControl wControl, AnalysisPresentation presentation) {
		super(ureq, wControl);
		this.presentation = presentation;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		nameEl = uifactory.addTextElement("presentation.name", 100, presentation.getName(), formLayout);
		nameEl.setMandatory(true);
		
		String[] replaceValues = new String[] { translate("presentation.replace.selection") };
		replaceEl = uifactory.addCheckboxesHorizontal("confirm.delete", "", formLayout, replaceValues, replaceValues);
		replaceEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		createButton = uifactory.addFormSubmitButton("presentation.create.button", buttonLayout);
		replaceButton = uifactory.addFormSubmitButton("presentation.replace.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}

	private void updateUI() {
		boolean existing = presentation.getKey() != null;
		replaceEl.setVisible(existing);
		
		boolean replace = replaceEl.isVisible() && replaceEl.isAtLeastSelected(1);
		boolean create = !replace;
		replaceButton.setVisible(replace);
		createButton.setVisible(create);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == replaceEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= QualityUIFactory.validateIsMandatory(nameEl);
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = nameEl.getValue();
		presentation.setName(name);
		
		Action action = presentation.getKey() == null || (replaceEl.isVisible() && replaceEl.isAtLeastSelected(1))
				? Action.SAVE
				: Action.CLONE;
		fireEvent(ureq, new PresentationEvent(action, presentation));
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
