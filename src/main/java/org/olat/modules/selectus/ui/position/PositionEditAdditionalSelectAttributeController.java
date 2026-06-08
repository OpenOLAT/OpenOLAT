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
import java.util.Scanner;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Display;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Option;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Order;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;

/**
 * 
 * Initial date: 16 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalSelectAttributeController extends FormBasicController implements PositionEditAdditionalAttributeController {
	
	private static final String TYPE_SINGLE = "single";
	private static final String TYPE_MULTILPLE = "multiple";
	private static final String[] ON_KEYS = new String[] { "on" };
	
	private SingleSelection typeEl;
	private SingleSelection orderEl;
	private SingleSelection displayEl;
	private TextAreaElement optionsEl;
	private MultipleSelectionElement otherEl;
	private MultipleSelectionElement mandatoryEl;
	private List<TextElement> labelsEl = new ArrayList<>(3);
	private List<TextElement> placeholdersEl = new ArrayList<>(3);

	private final List<Locale> positionLanguages;
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	private final Position position;
	private SelectConfiguration configuration;
	private final PositionApplicationAttributeTabEnum tab;
	private PositionAttributeDefinition attributeDefinition;
	private final ApplicationAttributesDelegate attributesDelegate;
	
	@Autowired
	private RecruitingModule recruitingModule;

	public PositionEditAdditionalSelectAttributeController(UserRequest ureq, WindowControl wControl,
			Position position, PositionAttributeDefinition attributeDefinition, PositionApplicationAttributeTabEnum tab,
			SelectConfiguration defaultConfiguration) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.tab = tab;
		this.position = position;
		this.attributeDefinition = attributeDefinition;
		configuration = attributeDefinition.getConfiguration(SelectConfiguration.class);
		if(configuration == null) {
			configuration = defaultConfiguration;
		}
		if(configuration == null) {
			configuration = SelectConfiguration.defaultSingle();
		}
		
		if(position == null) {
			positionLanguages = new ArrayList<>();
			positionLanguages.add(recruitingModule.getReportingLocale());
		} else {
			positionLanguages = recruitingModule.getPositionLocales(position);
		}
		for(Locale locale:positionLanguages) {
			positionLanguageToLocale.put(locale.getLanguage(), locale);
		}
		
		attributesDelegate = new ApplicationAttributesDelegate(attributeDefinition.getTabEnum());

		initForm(ureq);
		updateUI();
	}
	
	@Override
	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(TYPE_SINGLE, translate("select.type.single")));
		typeValues.add(SelectionValues.entry(TYPE_MULTILPLE, translate("select.type.multiple")));
		typeEl = uifactory.addRadiosHorizontal("select.type", "select.type", formLayout, typeValues.keys(), typeValues.values());
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = configuration.isMultiple() ? TYPE_MULTILPLE : TYPE_SINGLE;
		typeEl.select(type, true);
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String label = attributeDefinition == null ? "" : attributeDefinition.getLabel(locale);
			TextElement headingEl = uifactory.addTextElement("attr_name_".concat(lang), "edit.attr.name", 256, label, formLayout);
			headingEl.setMandatory(true);
			headingEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				headingEl.setLabel("edit.attr.name_ml", new String[]{ lang });
				headingEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				headingEl.setElementCssClass("o_sel_attr_name");
			}
			labelsEl.add(headingEl);
		}
		
		String options = getOptionsForTextArea();
		optionsEl = uifactory.addTextAreaElement("select.options", "select.options", 255000, 8, 60, false, true, false, options, formLayout);
		if(positionLanguages.size() > 1) {
			optionsEl.setExampleKey("custom.attribute.options.help", getPositionLangages());
		}
		
		SelectionValues displayValues = new SelectionValues();
		displayValues.add(SelectionValues.entry(Display.dropdown.name(), translate("select.display.dropdown")));
		displayValues.add(SelectionValues.entry(Display.list.name(), translate("select.display.list")));
		displayEl = uifactory.addRadiosHorizontal("select.display", "select.display", formLayout, displayValues.keys(), displayValues.values());
		displayEl.setHelpTextKey("select.display.hint", null);
		displayEl.addActionListener(FormEvent.ONCHANGE);
		String display = configuration.getDisplay().name();
		displayEl.select(display, true);
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			Translator translator = Util.createPackageTranslator(PositionController.class, locale);
			String placeholder = attributeDefinition == null || attributeDefinition.getKey() == null
					? translator.translate("please.choose") : attributeDefinition.getPlaceholder(locale);
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
		
		SelectionValues orderValues = new SelectionValues();
		orderValues.add(SelectionValues.entry(Order.alphabetically.name(), translate("select.order.alphabetically")));
		orderValues.add(SelectionValues.entry(Order.asEntered.name(), translate("select.order.asEntered")));
		orderEl = uifactory.addRadiosHorizontal("select.order", "select.order", formLayout, orderValues.keys(), orderValues.values());
		String order = configuration.getOrder().name();
		orderEl.select(order, true);
		
		String[] otherValues = new String[] { translate("select.other.on") };
		otherEl = uifactory.addCheckboxesHorizontal("select.other", formLayout, ON_KEYS, otherValues);
		if(configuration.isOther()) {
			otherEl.select(ON_KEYS[0], true);
		}
		
		String[] mandatoryValues = new String[] { translate("mandatory.on") };
		mandatoryEl = uifactory.addCheckboxesHorizontal("edit.attr.mandatory", formLayout, ON_KEYS, mandatoryValues);
		mandatoryEl.setVisible(tab != PositionApplicationAttributeTabEnum.global);
		if(attributeDefinition != null && attributeDefinition.isMandatory()) {
			mandatoryEl.select(ON_KEYS[0], true);
		}
	
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private String[] getPositionLangages() {
		StringBuilder args = new StringBuilder();
		for(int i=0; i<positionLanguages.size(); i++) {
			if(i > 0) {
				args.append(" tab ");
			}
			args.append(positionLanguages.get(i).getDisplayLanguage(getLocale()));
		}
		return new String[] { args.toString() };
	}
	
	private String getOptionsForTextArea() {
		StringBuilder sb = new StringBuilder(8192);
		List<Option> options = configuration.getOptions();
		if(options != null && !options.isEmpty()) {
			for(Option option:options) {
				boolean addTab = false;
				for(Locale locale:positionLanguages) {
					if(addTab) {
						sb.append(",");
					} else {
						addTab = true;
					}
					String val = option.getValue(locale);
					if(val != null) {
						sb.append(val);
					}
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement labelEl:labelsEl) {
			allOk &= RecruitingHelper.validateTextElement(labelEl, 255, true, new OWASPAntiSamyXSSFilter());
			allOk &= attributesDelegate.validateFormLabel(labelEl, attributeDefinition, position);
		}
		allOk &= RecruitingHelper.validateSingleSelection(displayEl);
		allOk &= RecruitingHelper.validateSingleSelection(orderEl);
		allOk &= RecruitingHelper.validateSingleSelection(typeEl);
		return allOk;
	}
	
	private void updateUI() {
		boolean dropdown = displayEl.isOneSelected()
					&& Display.dropdown.name().equals(displayEl.getSelectedKey());
		for(TextElement placeholderEl:placeholdersEl) {
			placeholderEl.setVisible(dropdown);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(typeEl == source || displayEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(TextElement headingEl:labelsEl) {
			Locale locale = (Locale)headingEl.getUserObject();
			attributeDefinition.setLabel(headingEl.getValue(), locale);
		}
		for(TextElement placeholderEl:placeholdersEl) {
			Locale locale = (Locale)placeholderEl.getUserObject();
			attributeDefinition.setPlaceholder(placeholderEl.getValue(), locale);
		}
		attributeDefinition.setMandatory(mandatoryEl.isAtLeastSelected(1));
		
		configuration.setDisplay(Display.valueOf(displayEl.getSelectedKey()));
		configuration.setOrder(Order.valueOf(orderEl.getSelectedKey()));
		configuration.setMultiple(TYPE_MULTILPLE.equals(typeEl.getSelectedKey()));
		configuration.setOther(otherEl.isAtLeastSelected(1));
		List<Option> options = parseOptions();
		configuration.setOptions(options);
		
		attributeDefinition.setConfiguration(configuration);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private List<Option> parseOptions() {
		String text = optionsEl.getValue();
        List<Option> options = new ArrayList<>();
		try(Scanner scanner = new Scanner(text)) {
	        while (scanner.hasNextLine()) {
	        	String line = scanner.nextLine();
	        	if(StringHelper.containsNonWhitespace(line)) {
	        		Option opt = parseOption(line);
	        		options.add(opt);
	        	}
	        }
		} catch(Exception e) {
			logError("", e);
		}
		return options;
	}
	
	private Option parseOption(String line) {
		Option opt = new Option();
    	String[] values = line.split("[\t,]");
    	for(int i=0; i<positionLanguages.size(); i++) {
	    	if(values.length > i) {
	    		opt.setValue(values[i], positionLanguages.get(i));
	    	}
    	}
    	return opt;
	}
}
