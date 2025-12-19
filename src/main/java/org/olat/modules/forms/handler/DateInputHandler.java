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
package org.olat.modules.forms.handler;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.forms.model.xml.DateInput;
import org.olat.modules.forms.ui.DateInputController;
import org.olat.modules.forms.ui.DateInputInspectorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: Dec 17, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DateInputHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler, CloneElementHandler {
	
	private final boolean restrictedEdit;
	
	public DateInputHandler(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	public String getType() {
		return "formdateinput";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_calendar";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.questionType;
	}

	@Override
	public int getSortOrder() {
		return 41;
	}
	
	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints hints) {
		if (element instanceof DateInput dateInput) {
			Controller ctrl = new DateInputController(ureq, wControl, dateInput, false);
			return new PageRunControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof DateInput dateInput) {
			return new DateInputInspectorController(ureq, wControl, dateInput, restrictedEdit);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		DateInput dateInput = new DateInput();
		dateInput.setId(UUID.randomUUID().toString());
		dateInput.setMandatory(false);
		dateInput.setDate(true);
		dateInput.setTime(false);
		return dateInput;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof DateInput dateInput) {
			DateInput clone = new DateInput();
			clone.setId(UUID.randomUUID().toString());
			clone.setMandatory(dateInput.isMandatory());
			clone.setDate(dateInput.isDate());
			clone.setTime(dateInput.isTime());
			clone.setNowButtonLabel(dateInput.getNowButtonLabel());
			clone.setLayoutSettings(BlockLayoutSettings.clone(dateInput.getLayoutSettings()));
			clone.setAlertBoxSettings(AlertBoxSettings.clone(dateInput.getAlertBoxSettings()));
			return clone;
		}
		return null;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof DateInput dateInput) {
			EvaluationFormResponseController ctrl = new DateInputController(ureq, wControl, dateInput, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}
	
	public static String toResponseValue(DateInput dateInput, Date date) {
		if (date == null) {
			return null;
		}
		
		Date responseDate = date;
		if (!dateInput.isTime()) {
			responseDate = DateUtils.setTime(responseDate, 0, 0, 0);
		}
		
		return Formatter.formatDatetime(responseDate);
	}
	
	public static Date fromResponseValue(String value) {
		if (!StringHelper.containsNonWhitespace(value)) {
			return null;
		}
		
		Date date = null;
		try {
			date = Formatter.parseDatetime(value);
		} catch (ParseException e) {
			//
		}
		
		return date;
	}

}
