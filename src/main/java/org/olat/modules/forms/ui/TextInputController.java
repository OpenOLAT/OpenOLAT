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

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputController extends FormBasicController implements EvaluationFormResponseController {
	
	private TextElement singleRowEl;
	private TextAreaElement multiRowEl;
	private DateChooser dateEl;
	
	private final TextInput textInput;
	private final boolean editor;
	private boolean singleRow;
	private boolean isDate;
	private EvaluationFormResponse response;
	private boolean validationEnabled = true;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public TextInputController(UserRequest ureq, WindowControl wControl, TextInput textInput, boolean editor) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.textInput = textInput;
		this.editor = editor;
		initForm(ureq);
	}
	
	public TextInputController(UserRequest ureq, WindowControl wControl, TextInput textInput, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.textInput = textInput;
		this.editor = false;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		singleRowEl = uifactory.addTextElement("textinput_" + CodeHelper.getRAMUniqueID(), null, 1000, null, formLayout);
		
		multiRowEl = uifactory.addTextAreaElement("textinput_" + CodeHelper.getRAMUniqueID(), null, 56000, -1, 72, false, true, "", formLayout);
		
		dateEl = uifactory.addDateChooser("textinput_" + CodeHelper.getRAMUniqueID(), null, null, formLayout);
		dateEl.setButtonsEnabled(!editor);
		
		update();
	}
	
	public void update() {
		singleRow = textInput.isNumeric() || textInput.isSingleRow();
		isDate = textInput.isDate();
		
		int rows = 12;
		if(textInput.getRows() > 0) {
			rows = textInput.getRows();
		}
		multiRowEl.setRows(rows);
		
		singleRowEl.setVisible(singleRow);
		multiRowEl.setVisible(!singleRow && !isDate);
		dateEl.setVisible(isDate);
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
		singleRowEl.clearError();
		multiRowEl.clearError();
		dateEl.clearError();
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (textInput.isMandatory()) {
			if (singleRowEl.isVisible() && !StringHelper.containsNonWhitespace(singleRowEl.getValue())) {
				singleRowEl.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			} else if (multiRowEl.isVisible() && !StringHelper.containsNonWhitespace(multiRowEl.getValue())) {
				multiRowEl.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}if (dateEl.isVisible() && dateEl.getDate() == null) {
				dateEl.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}
		}
		
		if (textInput.isNumeric()) {
			String val = singleRowEl.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				if (dbInstance.isMySQL() && val.length() > 55) {
					singleRowEl.setErrorKey("error.number.too.large", null);
					allOk = false;
				} else {
					try {
						double value = Double.parseDouble(val);
						if (textInput.getNumericMin() != null && textInput.getNumericMax() != null) {
							if (textInput.getNumericMin().doubleValue() > value || textInput.getNumericMax().doubleValue() < value) {
								singleRowEl.setErrorKey("error.number.between", new String[] {
										textInput.getNumericMin().toString(), textInput.getNumericMax().toString() });
								allOk = false;
							}
						} else if (textInput.getNumericMin() != null) {
							if (textInput.getNumericMin().doubleValue() > value) {
								singleRowEl.setErrorKey("error.number.min", new String[] { textInput.getNumericMin().toString()});
								allOk = false;
							}
						} else if (textInput.getNumericMax() != null) {
							if (textInput.getNumericMax().doubleValue() < value) {
								singleRowEl.setErrorKey("error.number.max", new String[] { textInput.getNumericMax().toString() });
								allOk = false;
							}
						}
					} catch (NumberFormatException e) {
						singleRowEl.setErrorKey("error.no.number", null);
						allOk = false;
					}
				}
			}
		}
		
		
		
		return allOk;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		singleRowEl.setEnabled(!readOnly);
		int rows = readOnly? -1: textInput.getRows();
		multiRowEl.setRows(rows);
		multiRowEl.setEnabled(!readOnly);
		dateEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return true;
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, textInput.getId());
		if (response != null) {
			if (singleRow) {
				singleRowEl.setValue(response.getStringuifiedResponse());
			} else if (isDate) {
				Date date = evaluationFormManager.getDate(response);
				dateEl.setDate(date);
			} else {
				multiRowEl.setValue(response.getStringuifiedResponse());
			}
		}
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		if (textInput.isNumeric()) {
			saveNumericResponse(session);
		} else if (isDate) {
			saveDateResponse(session);
		} else {
			saveTextResponse(session);
		}
	}

	private void saveNumericResponse(EvaluationFormSession session) {
		if (StringHelper.containsNonWhitespace(singleRowEl.getValue())) {
			BigDecimal value = new BigDecimal(singleRowEl.getValue());
			if (response == null) {
				response = evaluationFormManager.createNumericalResponse(textInput.getId(), session, value);
			} else {
				response = evaluationFormManager.updateNumericalResponse(response, value);
			}
		} else {
			deleteResponse(session);
		}
	}
	
	private void saveDateResponse(EvaluationFormSession session) {
		Date date = dateEl.getDate();
		if (date != null) {
			date = DateUtils.setTime(date, 0, 0, 0);
			if (response == null) {
				response = evaluationFormManager.createDateResponse(textInput.getId(), session, date);
			} else {
				response = evaluationFormManager.updateDateResponse(response, date);
			}
		} else {
			deleteResponse(session);
		}
	}
	
	private void saveTextResponse(EvaluationFormSession session) {
		String value = singleRow? singleRowEl.getValue(): multiRowEl.getValue();
		if (StringHelper.containsNonWhitespace(value)) {
			if (response == null) {
				response = evaluationFormManager.createStringResponse(textInput.getId(), session, value);
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
