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
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.position.model.PositionAdditionalAttributeRow;

/**
 * 
 * Initial date: 12 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalAttributeLabelController extends FormBasicController {
	
	private List<TextElement> labelsEl = new ArrayList<>(3);

	private final Locale[] positionLanguages;
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	private final PositionAttributeDefinitionTypeEnum type;
	private PositionAdditionalAttributeRow attributeDefinition;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditAdditionalAttributeLabelController(UserRequest ureq, WindowControl wControl, PositionAdditionalAttributeRow attributeDefinition) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		this.attributeDefinition = attributeDefinition;
		type = attributeDefinition.getType();
		positionLanguages = recruitingModule.getPositionLocales();
		for(int i=positionLanguages.length; i-->0; ) {
			positionLanguageToLocale.put(positionLanguages[i].getLanguage(), positionLanguages[i]);
		}
		initForm(ureq);
	}
	
	public PositionAdditionalAttributeRow getAttributeDefinition() {
		return attributeDefinition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String elI18nKey = "edit.attr.name";
		String elMlI18nKey = "edit.attr.name_ml";
		if(type == PositionAttributeDefinitionTypeEnum.heading) {
			elI18nKey = "edit.heading.name";
			elMlI18nKey = "edit.heading.name_ml";
		}
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String label = attributeDefinition.getAttributeDefinition().getLabel(locale);
			TextElement labelEl = uifactory.addTextElement("attr_name_".concat(lang), elI18nKey, 256, label, formLayout);
			labelEl.setMandatory(true);
			labelEl.setUserObject(locale);
			if(positionLanguages.length > 1) {
				labelEl.setLabel(elMlI18nKey, new String[]{ lang });
				labelEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				labelEl.setElementCssClass("o_sel_attr_name");
			}
			labelsEl.add(labelEl);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement labelEl:labelsEl) {
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
		for(TextElement labelEl:labelsEl) {
			Locale locale = (Locale)labelEl.getUserObject();
			attributeDefinition.getAttributeDefinition().setLabel(labelEl.getValue(), locale);
			if(locale.getLanguage().equals(getLocale().getLanguage())) {
				attributeDefinition.getLabelEl().setValue(labelEl.getValue());
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
