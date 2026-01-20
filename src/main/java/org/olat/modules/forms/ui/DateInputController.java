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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.DateInputHandler;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.DateInput;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 17, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DateInputController extends FormBasicController implements EvaluationFormResponseController {
	
	private DateChooser dateEl;
	private FormLink nowButton;
	
	private DateInput dateInput;
	private final boolean editor;
	private EvaluationFormResponse response;
	private boolean validationEnabled = true;
	private boolean readOnly = false;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public DateInputController(UserRequest ureq, WindowControl wControl, DateInput dateInput, boolean editor) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.dateInput = dateInput;
		this.editor = editor;
		initForm(ureq);
		setBlockLayoutClass(dateInput.getLayoutSettings());
	}

	private void setBlockLayoutClass(BlockLayoutSettings layoutSettings) {
		setFormStyle("o_form_element " + BlockLayoutClassFactory.buildClass(layoutSettings, true));
	}

	public DateInputController(UserRequest ureq, WindowControl wControl, DateInput dateInput, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.dateInput = dateInput;
		this.editor = false;
		initForm(ureq);
		setBlockLayoutClass(dateInput.getLayoutSettings());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer dateLayout = FormLayoutContainer.createVerticalFormLayout("dateinput_" + CodeHelper.getRAMUniqueID(), getTranslator());
		dateLayout.setElementCssClass("o_inline_cont");
		dateLayout.setRootForm(mainForm);
		formLayout.add(dateLayout);
		
		dateEl = uifactory.addDateChooser("dateinput_" + CodeHelper.getRAMUniqueID(), null, null, dateLayout);
		dateEl.setElementCssClass("o_no_padding");
		dateEl.setButtonsEnabled(!editor);
		
		nowButton = uifactory.addFormLink("dateinput_" + CodeHelper.getRAMUniqueID(), null, null, dateLayout, Link.BUTTON + Link.NONTRANSLATED);
		nowButton.setGhost(true);
		nowButton.addActionListener(FormEvent.ONCHANGE);
		
		updateUI();
	}
	
	private void updateUI() {
		dateEl.setDateChooserTimeEnabled(dateInput.isTime());
		dateEl.setEnabled(!readOnly);
		
		if (!readOnly && StringHelper.containsNonWhitespace(dateInput.getNowButtonLabel())) {
			nowButton.setVisible(true);
			nowButton.setI18nKey(StringHelper.escapeHtml(dateInput.getNowButtonLabel()));
		} else {
			nowButton.setVisible(false);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ChangePartEvent cpe && cpe.getElement() instanceof DateInput dateInput) {
			updateUI();
			setBlockLayoutClass(dateInput.getLayoutSettings());
			flc.setDirty(true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == nowButton) {
			dateEl.setDate(new Date());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	public void setValidationEnabled(boolean enabled) {
		this.validationEnabled = enabled;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		dateEl.clearError();
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (dateInput.isMandatory()) {
			if (dateEl.getDate() == null) {
				dateEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}
		
		return allOk;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		updateUI();
	}

	@Override
	public boolean hasResponse() {
		return true;
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, dateInput.getId());
		if (response != null) {
			Date date = DateInputHandler.fromResponseValue(response.getStringuifiedResponse());
			dateEl.setDate(date);
		}
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		saveDateResponse(session);
	}
	
	private void saveDateResponse(EvaluationFormSession session) {
		Date date = dateEl.getDate();
		if (date != null) {
			String value = DateInputHandler.toResponseValue(dateInput, date);
			if (response == null) {
				response = evaluationFormManager.createStringResponse(dateInput.getId(), session, value);
			} else {
				response = evaluationFormManager.updateStringResponse(response, value);
			}
		} else {
			deleteResponse(session);
		}
	}
	
	@Override
	public void deleteResponse(EvaluationFormSession session) {
		if (response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

	@Override
	public Progress getProgress() {
		int current = response != null && StringHelper.containsNonWhitespace(response.getStringuifiedResponse())? 1: 0;
		return Progress.of(current, 1);
	}
}
