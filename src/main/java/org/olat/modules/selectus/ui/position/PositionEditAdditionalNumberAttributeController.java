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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;

/**
 * 
 * Initial date: 4 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalNumberAttributeController extends FormBasicController implements PositionEditAdditionalAttributeController {

	private static final String[] mandatoryKeys = new String[] { "mandatory" };
	
	private MultipleSelectionElement mandatoryEl;
	private List<TextElement> labelsEl = new ArrayList<>(3);
	private List<TextElement> placeholdersEl = new ArrayList<>(3);
	
	private final List<Locale> positionLanguages = new ArrayList<>();
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	private final Position position;
	private PositionAttributeDefinition attributeDefinition;

	private final PositionApplicationAttributeTabEnum tab;
	private final ApplicationAttributesDelegate attributesDelegate;
	
	public PositionEditAdditionalNumberAttributeController(UserRequest ureq, WindowControl wControl,
			Position position, PositionAttributeDefinition attributeDefinition, PositionApplicationAttributeTabEnum tab) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.tab = tab;
		this.position = position;
		this.attributeDefinition = attributeDefinition;
		PositionEditHelper.calculatePositionLanguages(position, positionLanguages, positionLanguageToLocale);
		attributesDelegate = new ApplicationAttributesDelegate(attributeDefinition.getTabEnum());
		
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
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String placeholder = attributeDefinition == null ? "" : attributeDefinition.getPlaceholder(locale);
			TextElement placeholderEl = uifactory.addTextElement("placeholder_name_".concat(lang), "edit.attr.placeholder", 256, placeholder, formLayout);
			placeholderEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				placeholderEl.setLabel("edit.attr.placeholder_ml", new String[]{ lang });
				placeholderEl.setElementCssClass("o_sel_attr_placeholder_" + lang);
			} else {
				placeholderEl.setElementCssClass("o_sel_attr_placeholder");
			}
			placeholdersEl.add(placeholderEl);
		}

		mandatoryEl = uifactory.addCheckboxesHorizontal("edit.attr.mandatory", formLayout, mandatoryKeys, mandatoryKeys);
		mandatoryEl.setVisible(tab != PositionApplicationAttributeTabEnum.global);
		if(attributeDefinition != null && attributeDefinition.isMandatory()) {
			mandatoryEl.select(mandatoryKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement labelEl:labelsEl) {
			allOk &= RecruitingHelper.validateTextElement(labelEl, 255, true, new OWASPAntiSamyXSSFilter());
			allOk &= attributesDelegate.validateFormLabel(labelEl, attributeDefinition, position);
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
			attributeDefinition.setLabel(labelEl.getValue(), locale);
		}
		for(TextElement placeholderEl:placeholdersEl) {
			Locale locale = (Locale)placeholderEl.getUserObject();
			attributeDefinition.setPlaceholder(placeholderEl.getValue(), locale);
		}
		attributeDefinition.setMandatory(mandatoryEl.isAtLeastSelected(1));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
