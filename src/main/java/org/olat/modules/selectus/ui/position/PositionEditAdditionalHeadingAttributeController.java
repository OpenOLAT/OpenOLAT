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
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 16 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalHeadingAttributeController extends FormBasicController implements PositionEditAdditionalAttributeController {
	
	private List<TextElement> headingsEl = new ArrayList<>(3);

	private final List<Locale> positionLanguages;
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	private PositionAttributeDefinition attributeDefinition;
	
	@Autowired
	private RecruitingModule recruitingModule;

	public PositionEditAdditionalHeadingAttributeController(UserRequest ureq, WindowControl wControl, Position position, PositionAttributeDefinition attributeDefinition) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.attributeDefinition = attributeDefinition;
		
		if(position == null) {
			positionLanguages = new ArrayList<>();
			positionLanguages.add(recruitingModule.getReportingLocale());
		} else {
			positionLanguages = recruitingModule.getPositionLocales(position);
		}
		for(Locale locale:positionLanguages) {
			positionLanguageToLocale.put(locale.getLanguage(), locale);
		}

		initForm(ureq);
	}
	
	@Override
	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String label = attributeDefinition == null ? "" : attributeDefinition.getLabel(locale);
			TextElement headingEl = uifactory.addTextElement("attr_name_".concat(lang), "edit.heading.name", 256, label, formLayout);
			headingEl.setMandatory(true);
			headingEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				headingEl.setLabel("edit.heading.name_ml", new String[]{ lang });
				headingEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				headingEl.setElementCssClass("o_sel_attr_name");
			}
			headingsEl.add(headingEl);
		}
	
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement labelEl:headingsEl) {
			allOk &= RecruitingHelper.validateTextElement(labelEl, 255, true, new OWASPAntiSamyXSSFilter());
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(TextElement headingEl:headingsEl) {
			Locale locale = (Locale)headingEl.getUserObject();
			attributeDefinition.setLabel(headingEl.getValue(), locale);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
