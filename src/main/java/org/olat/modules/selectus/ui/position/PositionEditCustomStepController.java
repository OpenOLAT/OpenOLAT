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
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.position.model.EditStepRow;

/**
 * 
 * Initial date: 13 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditCustomStepController extends FormBasicController {
	
	private List<TextElement> labelsEl = new ArrayList<>(3);

	private final EditStepRow row;
	private final List<Locale> positionLanguages = new ArrayList<>();
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	public PositionEditCustomStepController(UserRequest ureq, WindowControl wControl, Position position, EditStepRow row) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.row = row;

		PositionEditHelper.calculatePositionLanguages(position, positionLanguages, positionLanguageToLocale);
		
		initForm(ureq);
	}
	
	public EditStepRow getRow() {
		return row;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String label = row.getCustomName(locale);
			TextElement labelEl = uifactory.addTextElement("attr_name_".concat(lang), "edit.attr.name", 256, label, formLayout);
			labelEl.setMandatory(true);
			labelEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				labelEl.setLabel("edit.attr.name_ml", new String[]{ lang });
				labelEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				labelEl.setElementCssClass("o_sel_attr_name");
			}
			labelsEl.add(labelEl);
		}

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(TextElement labelEl:labelsEl) {
			labelEl.clearError();
			if(!StringHelper.containsNonWhitespace(labelEl.getValue())) {
				labelEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(labelEl.getValue().length() > 35) {
				labelEl.setErrorKey("input.toolong", new String[]{ "35" });
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {	
		for(TextElement labelEl:labelsEl) {
			Locale locale = (Locale)labelEl.getUserObject();
			row.setCustomName(labelEl.getValue(), locale);
		}
		if(row.getTitleEl() != null) {
			row.getTitleEl().setValue(row.getCustomName(getLocale()));
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
