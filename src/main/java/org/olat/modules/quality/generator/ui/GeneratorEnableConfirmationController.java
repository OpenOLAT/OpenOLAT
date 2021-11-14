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
package org.olat.modules.quality.generator.ui;

import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorEnableConfirmationController extends FormBasicController {
	
	private DateChooser fromEl;
	private StaticTextElement enableInfoEl;

	private final QualityGenerator generator;
	
	@Autowired
	private QualityGeneratorService generatorService;

	public GeneratorEnableConfirmationController(UserRequest ureq, WindowControl wControl, QualityGenerator generator) {
		super(ureq, wControl);
		// Load to have the most recent last run date.
		this.generator = generatorService.loadGenerator(generator);
		initForm(ureq);
	}

	Date getFromDate() {
		return fromEl.getDate();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Date fromDate = generator.getLastRun() != null? generator.getLastRun(): new Date();
		fromEl = uifactory.addDateChooser("generator.from.date", fromDate, formLayout);
		fromEl.setDateChooserTimeEnabled(true);
		fromEl.setMandatory(true);
		fromEl.addActionListener(FormEvent.ONCHANGE);
		
		enableInfoEl = uifactory.addStaticTextElement("generator.enable.info", "", formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("generator.enable.confirm.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}

	private void updateUI() {
		if (fromEl.getDate() != null) {
			String enableInfo = generatorService.getGeneratorEnableInfo(generator, fromEl.getDate(), new Date(), getLocale());
			enableInfoEl.setValue(enableInfo);
		} else {
			enableInfoEl.setValue("");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (fromEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= validateIsMandatory(fromEl);
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
