/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.importwizard;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.ui.importwizard.CurriculumImportedValue.Level;

/**
 * 
 * Initial date: 11 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ValidationResultController extends FormBasicController {
	
	private final Formatter formatter;
	private final CurriculumImportedValue value;
	
	public ValidationResultController(UserRequest ureq, WindowControl wControl, CurriculumImportedValue value) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.value = value;
		formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_validation_result");
		
		if(value.isWarning() || value.isError()) {
			initFormIssue(formLayout);
		} else if(value.isChanged()) {
			initFormChanges(formLayout);
		}
	}
	
	private void initFormIssue(FormItemContainer formLayout) {
		String i18nHeader = value.getLevel() == Level.ERROR
				? "error.header"
				: "warn.header";
		String label = "<i class='o_icon " + value.getLevel().iconCssClass() + "'> </i> " + translate(i18nHeader);
		String message = StringHelper.escapeHtml(value.getMessage());
		StaticTextElement el = uifactory.addStaticTextElement("issue", null, message, formLayout);
		el.setLabel(label, null, false);
	}
	
	private void initFormChanges(FormItemContainer formLayout) {
		String label = "<i class='o_icon o_icon_changes'> </i> " + translate("value.changed.label");
		String values = translate("value.changed", valueToString(value.getBeforeValue()), valueToString(value.getAfterValue()));
		StaticTextElement el = uifactory.addStaticTextElement("changes", null, values, formLayout);
		el.setLabel(label, null, false);
	}
	
	private String valueToString(Object obj) {
		String val;
		if(obj instanceof String string) {
			val = string;
		} else if(obj instanceof LocalDate localDate) {
			val = formatter.formatDate(localDate);
		} else if(obj instanceof LocalTime localTime) {
			val = formatter.formatTimeShort(localTime);
		} else if(obj instanceof LocalTime localTime) {
			val = formatter.formatTimeShort(localTime);
		} else if(obj instanceof List<?> list) {
			List<String> values = list.stream()
					.map(this::valueToString)
					.toList();
			val = String.join("; ", values);
		} else if(obj != null) {
			val = obj.toString();
		} else {
			val = "";
		}
		return StringHelper.escapeHtml(val);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
