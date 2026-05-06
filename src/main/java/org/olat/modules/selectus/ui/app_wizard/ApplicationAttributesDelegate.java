/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter.NumericalRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionComparator;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Display;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Option;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Order;
import org.olat.modules.selectus.model.attributes.SeparatorConfiguration;
import org.olat.modules.selectus.model.attributes.StaticTextConfiguration;
import org.olat.modules.selectus.model.attributes.StaticTextConfiguration.TextDisplay;
import org.olat.modules.selectus.model.attributes.TextConfiguration;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.LongTextRenderer;
import org.olat.modules.selectus.ui.components.PercentageCellRenderer;
import org.olat.modules.selectus.ui.components.SelectAdditionalAttributeCellRenderer;
import org.olat.modules.selectus.ui.document.ApplicationXMLV2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * Initial date: 10 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationAttributesDelegate {
	
	private static final Logger log = Tracing.createLoggerFor(ApplicationAttributesDelegate.class);
	
	private static int count = 0;
	public static final int COLS_OFFSET = 50000;
	public static final String CUSTOM = "custom_";
	public static final String SELECT_OTHER = "xxx-other-xxx";
	public static final String SELECT_CHOOSE = "xxx-choose-xxx";
	public static final String SELECT_NOTHING = "xxx-nothing-xxx";
	public static final String FILTER_OTHERS = "others";
	
	public static final int TEXT_MAX_LENGTH_ORACLE = 4000;
	public static final int TEXT_MAX_LENGTH = 32000;
	
	private final PositionApplicationAttributeTabEnum tab;
	private final RecruitingService recruitingService;
	private final FormUIFactory uifactory = FormUIFactory.getInstance();
	
	public ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum tab) {
		this.tab = tab;
		recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
	}
	
	public PositionApplicationAttributeTabEnum tab() {
		return tab;
	}
	
	public List<ApplicationAttributeWithDefinition> getTabAttributesDefinitions(Application application) {
		Position position = application.getPosition();
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions().stream()
				.filter(def -> tab.equals(def.getTabEnum()))
				.collect(Collectors.toList());
		Set<ApplicationAttribute> attributes = application.getAttributes();
		Map<PositionAttributeDefinition,ApplicationAttribute> attributesMap = attributes
				.stream().collect(Collectors.toMap(ApplicationAttribute::getDefinition, attr -> attr));
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = new ArrayList<>();
		for(PositionAttributeDefinition definition:definitions) {
			ApplicationAttribute value = attributesMap.get(definition);
			valueWithDefinitions.add(new ApplicationAttributeWithDefinition(definition, value));
		}
		return valueWithDefinitions;
	}
	
	public List<ApplicationAttributeWithDefinition> getTabAttributesDefinitions(Position position) {
		List<PositionAttributeDefinition> definitions = recruitingService.getGlobalAttributeDefinition();
		
		if(definitions.size() > 1) {
			Collections.sort(definitions, new PositionAttributeDefinitionComparator());
		}
		
		Set<ApplicationAttribute> attributes = position.getAttributes();
		Map<PositionAttributeDefinition,ApplicationAttribute> attributesMap = attributes
				.stream().collect(Collectors.toMap(ApplicationAttribute::getDefinition, attr -> attr));
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = new ArrayList<>();
		for(PositionAttributeDefinition definition:definitions) {
			ApplicationAttribute value = attributesMap.get(definition);
			valueWithDefinitions.add(new ApplicationAttributeWithDefinition(definition, value));
		}
		return valueWithDefinitions;
	}
	
	public boolean hasSomeAttributes(Application application) {
		Position position = application.getPosition();
		PositionAttributeDefinition firstDefinition = position.getAttributesDefinitions().stream()
				.filter(def -> tab.equals(def.getTabEnum()))
				.findFirst().orElse(null);
		return firstDefinition != null;
	}
	
	public boolean hasSomeGlobalAttributes() {
		List<PositionAttributeDefinition> definitions = recruitingService.getGlobalAttributeDefinition();
		return !definitions.isEmpty();
	}
	
	public boolean hasSomeValue(Application application) {
		Set<ApplicationAttribute> attributes = application.getAttributes();
		if(attributes.isEmpty()) {
			return false;
		}
		
		Position position = application.getPosition();
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions().stream()
				.filter(def -> tab.equals(def.getTabEnum()))
				.collect(Collectors.toList());
		Map<PositionAttributeDefinition,ApplicationAttribute> attributesMap = attributes
				.stream().collect(Collectors.toMap(ApplicationAttribute::getDefinition, attr -> attr));
		for(PositionAttributeDefinition definition:definitions) {
			ApplicationAttribute value = attributesMap.get(definition);
			if(value != null && StringHelper.containsNonWhitespace(value.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Generate the flexi elements for the forms.
	 * 
	 * @param formLayout The form layout
	 * @param customAttributesEl The list of additional attributes
	 * @param application The application
	 * @param admin true if administrator
	 * @param editable true if editable
	 * @param locale The locale
	 * @return true if there are some attributes
	 */
	public boolean initAdditionalAttributes(FormItemContainer formLayout, List<FormItem> customAttributesEl,
			Application application, boolean admin, boolean editable, Locale locale) {
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = getTabAttributesDefinitions(application);
		return initAdditionalAttributes(formLayout, customAttributesEl, valueWithDefinitions, admin, editable, locale);
	}
	
	public boolean initGlobalAdditionalAttributes(FormItemContainer formLayout, List<FormItem> customAttributesEl,
			Position position, boolean admin, boolean editable, Locale locale) {
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = getTabAttributesDefinitions(position);
		return initAdditionalAttributes(formLayout, customAttributesEl, valueWithDefinitions, admin, editable, locale);
	}
	
	private boolean initAdditionalAttributes(FormItemContainer formLayout, List<FormItem> customAttributesEl,
			List<ApplicationAttributeWithDefinition> valueWithDefinitions, boolean admin, boolean editable, Locale locale) {
		for(ApplicationAttributeWithDefinition valueWithDefinition:valueWithDefinitions) {
			PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
			switch(definition.getTypeEnum()) {
				case question:
					initAdditionalSingleOrMultiLineTextAttribute(formLayout, valueWithDefinition, admin, editable, locale);
					break;
				case number:
					initAdditionalNumberAttribute(formLayout, valueWithDefinition, admin, editable, locale);
					break;	
				case percentage:
					initAdditionalPercentageAttribute(formLayout, valueWithDefinition, admin, editable, locale);
					break;		
				case date:
					initAdditionalDateAttribute(formLayout, valueWithDefinition, admin, editable, locale);
					break;	
				case heading:
					initAdditionalHeadingAttribute(formLayout, valueWithDefinition, locale);
					break;
				case separator:
					initAdditionalSeparatorAttribute(formLayout, valueWithDefinition);
					break;
				case select:
					initAdditionalSelectAttribute(formLayout, valueWithDefinition, admin, editable, locale);
					break;
				case text:
					initAdditionalTextAttribute(formLayout, valueWithDefinition, locale);
					break;
				default:
					continue;
			}
			customAttributesEl.add(valueWithDefinition.getPrimaryItem());
			if(valueWithDefinition.getSecondaryItem() != null) {
				customAttributesEl.add(valueWithDefinition.getSecondaryItem());
			}
		}
		return !valueWithDefinitions.isEmpty();
	}
	
	private FormItem initAdditionalSeparatorAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		SeparatorConfiguration config = definition.getConfiguration(SeparatorConfiguration.class);
		boolean onlySpaceAndNoLine = config != null && !config.isWithLine();
		SpacerElement element = uifactory.addSpacerElement(elementId(definition), formLayout, onlySpaceAndNoLine);
		element.setLabel("", null, false);
		valueWithDefinition.setPrimaryItem(element);
		return element;
	}
	
	private FormItem initAdditionalTextAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			Locale locale) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		StaticTextConfiguration config = definition.getConfiguration(StaticTextConfiguration.class);
		TextDisplay display = config == null ? null : config.getDisplay();
		String text = config == null ? "" : StringHelper.xssScan(config.getText(locale));
		if(StringHelper.containsNonWhitespace(text)) {
			text = Formatter.escWithBR(text).toString();
		}
		if(StringHelper.containsNonWhitespace(display.cssClass())) {
			text = "<p class='" + display.cssClass() + "'>" + text + "</p>";
		}
		StaticTextElement element = uifactory.addStaticTextElement(elementId(definition), null, text, formLayout);
		element.setLabel(null, null, false);
		element.setDomWrapperElement(DomWrapperElement.div);
		valueWithDefinition.setPrimaryItem(element);
		return element;
	}
	
	private FormItem initAdditionalHeadingAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			Locale locale) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		StaticTextElement element = uifactory.addStaticTextElement(elementId(definition), "custom.attribute", "", formLayout);
		String label = definition.getLabel(locale, true);
		element.setElementCssClass("o_static_heading");
		element.setLabel(label, null, false);
		valueWithDefinition.setPrimaryItem(element);
		return element;
	}
	
	private TextElement initAdditionalSingleOrMultiLineTextAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			boolean admin, boolean editable, Locale locale) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		TextConfiguration config = definition.getConfiguration(TextConfiguration.class);
		boolean multi = false;
		int maxLength = 255;
		if(config != null) {
			multi = config.isMultiLine();
			maxLength = config.getMaxLength();
		}
		return initAdditionalTextAttribute(formLayout, valueWithDefinition, multi, maxLength, admin, editable, locale);
	}
	
	private TextElement initAdditionalNumberAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			boolean admin, boolean editable, Locale locale) {
		return initAdditionalTextAttribute(formLayout, valueWithDefinition, false, 16, admin, editable, locale);
	}
	
	private TextElement initAdditionalPercentageAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			boolean admin, boolean editable, Locale locale) {
		TextElement percentageEl = initAdditionalTextAttribute(formLayout, valueWithDefinition, false, 16, admin, editable, locale);
		percentageEl.setElementCssClass("form-inline");
		percentageEl.setTextAddOn("custom.attribute.percentage.addon");
		return percentageEl;
	}
	
	private TextElement initAdditionalTextAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			boolean multi, int maxLength, boolean admin, boolean editable, Locale locale) {
		
		ApplicationAttribute value = valueWithDefinition.getValue();
		String val = null;
		if(value != null) {
			val = value.getValue();
		}
		
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		TextElement element;
		if(multi) {
			int rows = (maxLength / 90) + 2;
			if(rows < 3) {
				rows = 3;
			} else if(rows > 8) {
				rows = 8;
			}
			element = uifactory.addTextAreaElement(elementId(definition), "custom.attribute", maxLength, rows, 60, false, false, false, val, formLayout);
			element.setExampleKey("edit.multi.max.length.hint", new String[] { Integer.toString(maxLength) });
		} else {
			element = uifactory.addTextElement(elementId(definition), "custom.attribute", 255, val, formLayout);
		}
		element.setMandatory(definition.isMandatory() && !admin);
		String placeholder = definition.getPlaceholder(locale, true);
		element.setPlaceholderText(placeholder);
		element.setUserObject(valueWithDefinition);
		String label = definition.getLabel(locale, true);
		element.setLabel(label, null, false);
		element.setEnabled(editable);
		valueWithDefinition.setPrimaryItem(element);
		return element;
	}
	
	private FormItem initAdditionalSelectAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			boolean admin, boolean editable, Locale locale) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		ApplicationAttribute value = valueWithDefinition.getValue();
		String val = null;
		if(value != null) {
			val = value.getValue();
		}
		
		FormItem selectElement;
		SelectConfiguration configuration = definition.getConfiguration(SelectConfiguration.class);
		if(configuration.isMultiple()) {
			selectElement = initAdditionalMultiSelectAttribute(formLayout, valueWithDefinition, configuration, val, admin, editable, locale);
		} else {
			selectElement = initAdditionalSingleSelectAttribute(formLayout, valueWithDefinition, configuration, val, admin, editable, locale);
		}
		return selectElement;
	}
	
	private FormItem initAdditionalMultiSelectAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			SelectConfiguration configuration, String val, boolean admin, boolean editable, Locale locale) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		List<Option> options = configuration.getOptions();
		if(configuration.getOrder() == Order.alphabetically) {
			Collections.sort(options, new OptionComparator(locale));
		}
		
		SelectionValues keyValues = new SelectionValues();
		List<String> values = getValues(val);
		List<String> selectedOptions = new ArrayList<>();
		for(Option option:options) {
			String opt = option.getValue(locale, true);
			keyValues.add(SelectionValues.entry(opt, opt));
			if(option.isInList(values)) {
				selectedOptions.add(opt);
			}
		}
		
		List<String> unkownValues = new ArrayList<>();
		if(configuration.isOther()) {
			for(String value:values) {
				if(SELECT_OTHER.equals(value) || SELECT_CHOOSE.equals(value)) {
					continue;
				}
				
				boolean known = false;
				for(Option option:options) {
					if(option.isOption(value)) {
						known = true;
					}
				}
				if(!known) {
					unkownValues.add(value);
				}
			}
			
			Translator translator = Util.createPackageTranslator(ApplicationAttributesDelegate.class, locale);
			keyValues.add(SelectionValues.entry(SELECT_OTHER, translator.translate("other")));
		}
		
		MultipleSelectionElement element;
		if(configuration.getDisplay() == Display.dropdown) {
			element = uifactory.addCheckboxesDropdown(elementId(definition), "custom.attribute", formLayout,
					keyValues.keys(), keyValues.values());
			String placeholder = definition.getPlaceholder(locale, true);
			if(!StringHelper.containsNonWhitespace(placeholder)) {
				placeholder = formLayout.getTranslator().translate("custom.attribute.please.choose");
			}
			element.setNonSelectedText(placeholder);
		} else {
			element = uifactory.addCheckboxesVertical(elementId(definition), "custom.attribute", formLayout,
					keyValues.keys(), keyValues.values(), 1);
		}
		element.addActionListener(FormEvent.ONCHANGE);
		for(String selectedOption:selectedOptions) {
			if(keyValues.containsKey(selectedOption)) {
				element.select(selectedOption, true);
			} 
		}
		if(configuration.isOther() && !unkownValues.isEmpty()) {
			element.select(SELECT_OTHER, true);
		} else if(element.getSelectedKeys().isEmpty()
				&& configuration.getDisplay() == Display.dropdown
				&& keyValues.containsKey(SELECT_CHOOSE)) {
			element.select(SELECT_CHOOSE, true);
		}
		
		element.setMandatory(definition.isMandatory() && !admin);
		element.setUserObject(valueWithDefinition);
		String label = definition.getLabel(locale, true);
		element.setLabel(label, null, false);
		element.setEnabled(editable);
		valueWithDefinition.setPrimaryItem(element);
		
		String unkownValue = String.join(" ", unkownValues);
		initAdditionalSelectOtherAttribute(formLayout, valueWithDefinition, configuration,
				unkownValue, admin, unkownValues.isEmpty());
		return element;
	}
	
	private DateChooser initAdditionalDateAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			boolean admin, boolean editable, Locale locale) {
		
		ApplicationAttribute value = valueWithDefinition.getValue();
		Date val = null;
		if(value != null && StringHelper.containsNonWhitespace(value.getValue())) {
			try {
				val = Formatter.parseDatetime(value.getValue());
			} catch (ParseException e) {
				log.warn("", e);
			}
		}
		
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		TextElement element = uifactory.addDateChooser(elementId(definition), "custom.attribute", val, formLayout);
		
		element.setMandatory(definition.isMandatory() && !admin);
		String placeholder = definition.getPlaceholder(locale, true);
		element.setPlaceholderText(placeholder);
		element.setUserObject(valueWithDefinition);
		String label = definition.getLabel(locale, true);
		element.setLabel(label, null, false);
		element.setEnabled(editable);
		valueWithDefinition.setPrimaryItem(element);
		
		return null;
	}
	
	private String elementId(PositionAttributeDefinition definition) {
		if(definition.getKey() != null) {
			return CUSTOM + definition.getKey();
		}
		return CUSTOM + (++count);
	}
	
	public static final List<String> getValues(String val) {
		List<String> values = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(val)) {
			String[] arr = val.split("[|]");
			for(String str:arr) {
				if(StringHelper.containsNonWhitespace(str)) {
					values.add(str);
				}
			}
		}
		return values;
	}
	
	public static final String[] getValuesArray(String val) {
		List<String> values = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(val)) {
			String[] arr = val.split("[|]");
			for(String str:arr) {
				if(StringHelper.containsNonWhitespace(str)) {
					values.add(str);
				}
			}
		}
		return values.toArray(new String[values.size()]);
	}
	
	public static final Object getLocalizedValuesWithOthers(PositionAttributeDefinitionConfiguration configuration, String val, Locale locale) {
		if(configuration == null) return val;
		
		if(configuration.type() == PositionAttributeDefinitionTypeEnum.select) {
			return getLocalizedValuesWithOthers((SelectConfiguration)configuration.configuration(), val, locale);
		}
		if(configuration.type() == PositionAttributeDefinitionTypeEnum.number
				|| configuration.type() == PositionAttributeDefinitionTypeEnum.percentage) {
			if(StringHelper.containsNonWhitespace(val) && StringHelper.isLong(val)) {
				return Long.valueOf(val);
			}
		}
		if(configuration.type() == PositionAttributeDefinitionTypeEnum.date) {
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					return Formatter.parseDatetime(val);
				} catch (ParseException e) {
					log.debug("Cannot parse XML dat: {}", val, e);
				}
			}
			return null;
		}

		return val;
	}

	public static final String[] getLocalizedValuesWithOthers(SelectConfiguration configuration, String val, Locale locale) {
		String[] valuesArray = getValuesArray(val);
		List<String> list = new ArrayList<>();
		List<Option> options = configuration.getOptions();
		boolean other = false;
		for(String value:valuesArray) {
			String localizedVal = null;
			for(Option option:options) {
				if(option.isOption(value)) {
					localizedVal = option.getValue(locale, true);
				}
			}
			
			if(localizedVal == null) {
				list.add(value);
				other = true;
			} else {
				list.add(localizedVal);
			}
		}
		
		if(other) {
			list.add(FILTER_OTHERS);
		}

		return list.toArray(new String[list.size()]);
	}
	
	public static final String getLocalizedValues(SelectConfiguration configuration, String val, Locale locale) {
		List<String> values = getValues(val);
		List<String> localizedValues = new ArrayList<>(values.size());
		for(String value:values) {
			String localizedValue = getSingleLocalizedValue(configuration, value, locale);
			if(!ApplicationAttributesDelegate.SELECT_CHOOSE.equals(localizedValue)
					&& !ApplicationAttributesDelegate.SELECT_OTHER.equals(localizedValue)
					&& !ApplicationAttributesDelegate.FILTER_OTHERS.equals(localizedValue)) {
				localizedValues.add(localizedValue);
			}
		}
		return String.join(", ", localizedValues);
	}
	
	public static final String getSingleLocalizedValue(SelectConfiguration configuration, String val, Locale locale) {
		List<Option> options = configuration.getOptions();
		for(Option option:options) {
			if(option.isOption(val)) {
				return option.getValue(locale, true);
			}
		}
		return val;
	}
	
	private FormItem initAdditionalSingleSelectAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			SelectConfiguration configuration, String val, boolean admin, boolean editable, Locale locale) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		List<Option> options = configuration.getOptions();
		if(configuration.getOrder() == Order.alphabetically) {
			Collections.sort(options, new OptionComparator(locale));
		}

		SelectionValues keyValues = new SelectionValues();
		if(configuration.getDisplay() == Display.dropdown) {
			String placeholder = definition.getPlaceholder(locale, true);
			if(!StringHelper.containsNonWhitespace(placeholder)) {
				placeholder = formLayout.getTranslator().translate("custom.attribute.please.choose");
			}
			keyValues.add(SelectionValues.entry(SELECT_CHOOSE, placeholder));
		}

		for(Option option:options) {
			String opt = option.getValue(locale, true);
			keyValues.add(SelectionValues.entry(opt, opt));
		}
		
		if(configuration.isOther()) {
			String otherLabel = formLayout.getTranslator().translate("custom.attribute.other");
			keyValues.add(SelectionValues.entry(SELECT_OTHER, otherLabel));
		}
		if(!definition.isMandatory() && configuration.getDisplay() == Display.list) {
			String otherLabel = formLayout.getTranslator().translate("custom.attribute.no.answer");
			keyValues.add(SelectionValues.entry(SELECT_NOTHING, otherLabel));
		}
		
		SingleSelection element;
		if(configuration.getDisplay() == Display.dropdown) {
			element = uifactory.addDropdownSingleselect(elementId(definition), "custom.attribute", formLayout,
					keyValues.keys(), keyValues.values(), null);
		} else {
			element = uifactory.addRadiosHorizontal(elementId(definition), "custom.attribute", formLayout,
					keyValues.keys(), keyValues.values());
		}
		element.addActionListener(FormEvent.ONCHANGE);
		
		boolean knownValue = keyValues.containsKey(val);
		if(knownValue) {
			element.select(val, true);
		} else if(configuration.isOther() && StringHelper.containsNonWhitespace(val)) {
			element.select(SELECT_OTHER, true);
		} else if(configuration.getDisplay() == Display.dropdown && !keyValues.isEmpty()) {
			element.select(keyValues.keys()[0], true);
		}
		
		element.setMandatory(definition.isMandatory() && !admin);
		element.setAllowNoSelection(true);
		element.setUserObject(valueWithDefinition);
		String label = definition.getLabel(locale, true);
		element.setLabel(label, null, false);
		element.setEnabled(editable);
		valueWithDefinition.setPrimaryItem(element);
		
		initAdditionalSelectOtherAttribute(formLayout, valueWithDefinition, configuration, val, admin, knownValue);
		
		return element;
	}
	
	private void initAdditionalSelectOtherAttribute(FormItemContainer formLayout, ApplicationAttributeWithDefinition valueWithDefinition,
			SelectConfiguration configuration, String val,  boolean admin, boolean knownValue) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		if(configuration.isOther()) {
			boolean isOther = !knownValue && StringHelper.containsNonWhitespace(val);
			String secondaryVal = isOther ? val : null;
			if(secondaryVal != null && secondaryVal.contains("|")) {
				secondaryVal = secondaryVal.replace("|", ", ");
			}
			TextElement inputElement = uifactory.addTextElement("custom_secondary_" + definition.getKey(), "custom.attribute.other.label", 255, secondaryVal, formLayout);
			inputElement.setVisible(isOther);
			inputElement.setMandatory(definition.isMandatory() && !admin);
			valueWithDefinition.setSecondaryItem(inputElement);
		}
	}
	
	public boolean validateFormLogic(List<FormItem> elements, boolean admin) {
		boolean allOk = true;
		
		for(FormItem element:elements) {
			if(!(element.getUserObject() instanceof ApplicationAttributeWithDefinition)) continue;

			ApplicationAttributeWithDefinition valueWithDefinition = (ApplicationAttributeWithDefinition)element.getUserObject();
			PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
			
			element.clearError();
			if(element instanceof TextElement) {
				TextElement textElement = (TextElement)element;
				if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question) {
					int maxLength = getMaxLength(definition);
					allOk &= RecruitingHelper.validateTextElement(textElement, maxLength, definition.isMandatory() && !admin, new OWASPAntiSamyXSSFilter());
				} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number
						|| definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage) {
					allOk &= RecruitingHelper.validateIntegerElement(textElement, definition.isMandatory() && !admin);
				}
			} else if(element instanceof SingleSelection) {
				SingleSelection selectElement = (SingleSelection)element;
				allOk &= validateFormLogic(selectElement, valueWithDefinition, admin);
			} else if(element instanceof MultipleSelectionElement) {
				MultipleSelectionElement selectElement = (MultipleSelectionElement)element;
				allOk &= validateFormLogic(selectElement, valueWithDefinition, admin);
			} else if(element instanceof DateChooser) {
				DateChooser dateElement = (DateChooser)element;
				allOk &= validateFormLogic(dateElement, valueWithDefinition, admin);
			}
			
			
		}
		
		return allOk;
	}
	
	private int getMaxLength(PositionAttributeDefinition definition) {
		int maxLength = 255;
		if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question) {
			TextConfiguration configuration = definition.getConfiguration(TextConfiguration.class);
			if(configuration != null) {
				maxLength = configuration.getMaxLength();
			}
		}
		return maxLength;
	}
	
	private boolean validateFormLogic(MultipleSelectionElement selectElement, ApplicationAttributeWithDefinition valueWithDefinition, boolean admin) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		
		boolean allOk = true;
		
		if(valueWithDefinition.getSecondaryItem() != null) {
			valueWithDefinition.getSecondaryItem().clearError();
		}

		List<String> selectVals = new ArrayList<>(selectElement.getSelectedKeys());
		selectVals.remove(SELECT_CHOOSE);
		if(selectVals.isEmpty()) {
			if(definition.isMandatory() && !admin) {
				selectElement.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}	
		} else if(selectVals.size() == 1 && selectVals.contains(SELECT_OTHER)
				&& valueWithDefinition.getSecondaryItem() instanceof TextElement) {
			TextElement textElement = ((TextElement)valueWithDefinition.getSecondaryItem());
			allOk &= RecruitingHelper.validateTextElement(textElement, 255, definition.isMandatory() && !admin, new OWASPAntiSamyXSSFilter());
		} 
		
		return allOk;
	}
	
	private boolean validateFormLogic(DateChooser selectElement, ApplicationAttributeWithDefinition valueWithDefinition, boolean admin) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		
		boolean allOk = true;
		if(selectElement.getDate() != null && definition.isMandatory() && !admin) {
			selectElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}	
		return allOk;
	}
	
	private boolean validateFormLogic(SingleSelection selectElement, ApplicationAttributeWithDefinition valueWithDefinition, boolean admin) {
		PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
		
		boolean allOk = true;
		
		if(valueWithDefinition.getSecondaryItem() != null) {
			valueWithDefinition.getSecondaryItem().clearError();
		}
	
		if(selectElement.isOneSelected()) {
			String selectVal = selectElement.getSelectedKey();
			if(SELECT_CHOOSE.equals(selectVal)) {
				if(definition.isMandatory() && !admin) {
					selectElement.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			} else if(SELECT_OTHER.equals(selectVal) && valueWithDefinition.getSecondaryItem() instanceof TextElement) {
				TextElement textElement = ((TextElement)valueWithDefinition.getSecondaryItem());
				allOk &= RecruitingHelper.validateTextElement(textElement, 255, definition.isMandatory() && !admin, new OWASPAntiSamyXSSFilter());
			}
		} else if(definition.isMandatory() && !admin) {
			selectElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	public void commitChanges(List<FormItem> elements, Application application) {
		commitChanges(elements, null, application);
	}

	public void commitChanges(List<FormItem> elements, Position position) {
		commitChanges(elements, position, null);
	}
	
	private void commitChanges(List<FormItem> elements, Position position, Application application) {
		Set<ApplicationAttribute> attributes;
		if(application != null) {
			attributes = application.getAttributes();
		} else {
			attributes = position.getAttributes();
		}

		Map<PositionAttributeDefinition,ApplicationAttribute> attributesMap = attributes
				.stream().collect(Collectors.toMap(ApplicationAttribute::getDefinition, attr -> attr));
		for(FormItem element:elements) {
			if(!(element.getUserObject() instanceof ApplicationAttributeWithDefinition)) continue;
			
			ApplicationAttributeWithDefinition valueWithDefinition = (ApplicationAttributeWithDefinition)element.getUserObject();
			PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
			ApplicationAttribute appAttribute = attributesMap.get(definition);

			if(element instanceof DateChooser) {
				DateChooser dateElement = (DateChooser)element;
				String xmlDate = null;
				if(dateElement.getDate() != null) {
					xmlDate = Formatter.formatDatetime(dateElement.getDate());
				}
				commitChange(xmlDate, position, application, appAttribute, definition);
			} else if(element instanceof TextElement) {
				TextElement textElement = (TextElement)element;
				commitChange(textElement.getValue(), position, application, appAttribute, definition);
			} else if(element instanceof SingleSelection) {
				String val;
				SingleSelection selectElement = (SingleSelection)element;
				if(selectElement.isOneSelected()) {
					String selectVal = selectElement.getSelectedKey();
					if(SELECT_CHOOSE.equals(selectVal) || SELECT_NOTHING.equals(selectVal)) {
						val = null;
					} else if(SELECT_OTHER.equals(selectVal) && valueWithDefinition.getSecondaryItem() instanceof TextElement) {
						val = ((TextElement)valueWithDefinition.getSecondaryItem()).getValue();
					} else {
						val = selectVal;
					}
				} else {
					val = null;
				}
				commitChange(val, position, application, appAttribute, definition);
			} else if(element instanceof MultipleSelectionElement) {
				MultipleSelectionElement selectElement = (MultipleSelectionElement)element;
				List<String> selectedKeys = new ArrayList<>(selectElement.getSelectedKeys());
				selectedKeys.remove(SELECT_CHOOSE);
				if(selectedKeys.contains(SELECT_OTHER) && valueWithDefinition.getSecondaryItem() instanceof TextElement) {
					String otherVal = ((TextElement)valueWithDefinition.getSecondaryItem()).getValue();
					if(StringHelper.containsNonWhitespace(otherVal)) {
						selectedKeys.add(otherVal);
					}
				}
				selectedKeys.remove(SELECT_OTHER);
				String val = String.join("|", selectedKeys);
				commitChange(val, position, application, appAttribute, definition);
			}
		}
	}
	
	private void commitChange(String val, Position position, Application application, ApplicationAttribute appAttribute, PositionAttributeDefinition definition) {
		if(appAttribute == null) {
			appAttribute = recruitingService.createAttribute(position, application, definition, val);
			
			if(application != null) {
				application.getAttributes().add(appAttribute);
			} else if(position != null) {
				position.getAttributes().add(appAttribute);
			}
		} else {
			appAttribute.setValue(val);
		}
	}
	
	public void formInnerEvent(FormItem source) {
		if(source instanceof SingleSelection && source.getUserObject() instanceof ApplicationAttributeWithDefinition) {
			SingleSelection selectEl = (SingleSelection)source;
			ApplicationAttributeWithDefinition valueWithDefinition = (ApplicationAttributeWithDefinition)selectEl.getUserObject();
			if(valueWithDefinition.getDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
				if(selectEl.isOneSelected()
						&& SELECT_OTHER.equals(selectEl.getSelectedKey())
						&& valueWithDefinition.getSecondaryItem() != null) {
					valueWithDefinition.getSecondaryItem().setVisible(true);
				} else if(valueWithDefinition.getSecondaryItem() != null) {
					valueWithDefinition.getSecondaryItem().setVisible(false);
				}
			}
		} else if(source instanceof MultipleSelectionElement && source.getUserObject() instanceof ApplicationAttributeWithDefinition) {
			MultipleSelectionElement selectEl = (MultipleSelectionElement)source;
			ApplicationAttributeWithDefinition valueWithDefinition = (ApplicationAttributeWithDefinition)selectEl.getUserObject();
			if(valueWithDefinition.getDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
				if(selectEl.getSelectedKeys().contains(SELECT_OTHER)
						&& valueWithDefinition.getSecondaryItem() != null) {
					valueWithDefinition.getSecondaryItem().setVisible(true);
				} else if(valueWithDefinition.getSecondaryItem() != null) {
					valueWithDefinition.getSecondaryItem().setVisible(false);
				}
			}
		}
	}
	
	public static List<ApplicationAttributeWithDefinition> removeEmptyHeadingSections(List<ApplicationAttributeWithDefinition> valueWithDefinitions) {
		List<Integer> headingsIndex = new ArrayList<>();
		for(int i=0; i<valueWithDefinitions.size(); i++) {
			PositionAttributeDefinition definition = valueWithDefinitions.get(i).getDefinition();
			if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.heading) {
				headingsIndex.add(Integer.valueOf(i));
			}
		}
		
		if(headingsIndex.isEmpty()) {
			return valueWithDefinitions;
		}
		
		List<ApplicationAttributeWithDefinition> layout = new ArrayList<>(valueWithDefinitions);
		
		for(int i=headingsIndex.size(); i-->0; ) {
			int startIndex = headingsIndex.get(i);
			int stopIndex = layout.size();
			if(i + 1 < headingsIndex.size()) {
				stopIndex = headingsIndex.get(i + 1);
			}
			
			boolean hasValue = false;
			for(int j=startIndex + 1; j<stopIndex; j++) {
				PositionAttributeDefinition definition = valueWithDefinitions.get(j).getDefinition();
				if(definition.getTypeEnum().valueType()) {
					hasValue = true;
				}	
			}
			
			if(!hasValue) {
				for(int k=stopIndex; k-->startIndex; ) {
					layout.remove(k);
				}
				layout = new ArrayList<>(layout);
			}
		}
		return layout;
	}

	public Details initAdditionalAttributesDetails(FormItemContainer formLayout, FormLayoutContainer currentContainer, Form mainForm,
			Application application, String section, RecruitingPositionSecurityCallback secCallback, Locale locale) {
		List<FormLayoutContainer> containers = new ArrayList<>();
		
		boolean hasValue = false;
		boolean lineSeparator = false;
		StaticTextElement lastElement = null;
		
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = getApplicationAttributeWithDefinitionWithValues(application, section, secCallback);
		if(secCallback != null) {
			valueWithDefinitions = removeEmptyHeadingSections(valueWithDefinitions);
		}
		
		for(ApplicationAttributeWithDefinition valueWithDefinition:valueWithDefinitions) {
			PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
			ApplicationAttribute value = valueWithDefinition.getValue();
			if(value != null && StringHelper.containsNonWhitespace(value.getValue())) {
				String content = getContent(definition, value, locale, true);
				StaticTextElement element = uifactory.addStaticTextElement("add_details_" + definition.getKey(), "custom.attribute",
						content, currentContainer);
				element.setLabel(definition.getLabel(locale, true), null, false);
				if(lineSeparator) {
					element.setElementCssClass("o_line_separator");
				}
				lastElement = element;
				lineSeparator = false;
				currentContainer.setVisible(true);
				hasValue = true;
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.heading) {
				String title = definition.getLabel(locale, true);
				String containerId = tab.name() + "_" + CodeHelper.getRAMUniqueID();
				currentContainer = FormLayoutContainer.createTableCondensedLayout(containerId, formLayout.getTranslator());
				currentContainer.setRootForm(mainForm);
				formLayout.add(currentContainer);
				currentContainer.setFormTitle(title);
				containers.add(currentContainer);
				lastElement = null;
				lineSeparator = false;
				currentContainer.setVisible(true);
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.separator) {
				SeparatorConfiguration separatorConfiguration = definition.getConfiguration(SeparatorConfiguration.class);
				lineSeparator = false;
				if(separatorConfiguration.isWithLine()) {
					lineSeparator = true;
				} else if(lastElement != null) {
					lastElement.setElementCssClass("o_separator");
				}
				lastElement = null;
			} else {
				lastElement = null;
				lineSeparator = false;
			}
		}
		formLayout.contextPut("containers" + tab.name(), containers);
		return new Details(hasValue, containers);
	}
	
	public void appendAdditionalAttributesDetails(Document doc, Element parentEl, Application application, String section,
			RecruitingPositionSecurityCallback secCallback, Translator translator) {
		parentEl.setAttribute("tab", tab.name());
		
		Element sectionEl = (Element)parentEl.appendChild(doc.createElement("customAttributesGroup"));
		
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = getApplicationAttributeWithDefinitionWithValues(application, section, secCallback);
		if(secCallback != null) {
			valueWithDefinitions = removeEmptyHeadingSections(valueWithDefinitions);
		}
		
		for(ApplicationAttributeWithDefinition valueWithDefinition:valueWithDefinitions) {
			PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
			ApplicationAttribute value = valueWithDefinition.getValue();
			if(value != null && StringHelper.containsNonWhitespace(value.getValue())) {
				String content = getContent(definition, value, translator.getLocale(), false);
				Element element = addDOMTextElement(doc, sectionEl, content);
				element.setAttribute("label", definition.getLabel(translator.getLocale(), true));
				sectionEl.setAttribute("visible", "true");
				parentEl.setAttribute("visible", "true");
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.heading) {
				String title = definition.getLabel(translator.getLocale(), true);
				sectionEl = (Element)parentEl.appendChild(doc.createElement("customAttributesGroup"));
				sectionEl.setAttribute("title", title);
				parentEl.setAttribute("visible", "true");
			}
		}
	}
	
	private Element addDOMTextElement(Document doc, Element parentEl,  String value) {
		Element attributeEl = (Element)parentEl.appendChild(doc.createElement("customAttribute"));
		if(StringHelper.containsNonWhitespace(value)) {
			value = ApplicationXMLV2.replaceLineBreaks(value);
		}
		attributeEl.appendChild(doc.createTextNode(value));
		if(StringHelper.containsNonWhitespace(value)) {
			parentEl.setAttribute("visible", "true");
		}
		return attributeEl;
	}
	
	public static class Details {
		
		private final boolean hasValue;
		private final List<FormLayoutContainer> containers;
		
		public Details(boolean hasValue, List<FormLayoutContainer> containers) {
			this.hasValue = hasValue;
			this.containers = containers;
		}
		
		public boolean hasValue() {
			return hasValue;
		}
		
		public List<FormLayoutContainer> containers() {
			return containers;
		}
	}
	
	private List<ApplicationAttributeWithDefinition> getApplicationAttributeWithDefinitionWithValues(Application application, String section,
			RecruitingPositionSecurityCallback secCallback) {
		List<ApplicationAttributeWithDefinition> layout = new ArrayList<>();
		List<ApplicationAttributeWithDefinition> valueWithDefinitions = getTabAttributesDefinitions(application);
		for(ApplicationAttributeWithDefinition valueWithDefinition:valueWithDefinitions) {
			PositionAttributeDefinition definition = valueWithDefinition.getDefinition();
			PositionAttributeDefinitionTypeEnum attributeType = definition.getTypeEnum();
			if(attributeType == null) {
				// ignore
			} else if(attributeType.valueType()) {
				ApplicationAttribute value = valueWithDefinition.getValue();
				if(value != null && StringHelper.containsNonWhitespace(value.getValue())
						&& (secCallback == null || secCallback.canViewField(section, RecruitingModule.APP_CUSTOM_FIELD_PREFIX + definition.getKey()))) {
					layout.add(valueWithDefinition);
				}
			} else {
				layout.add(valueWithDefinition);
			}
		}
		return layout;
	}
	
	private String getContent(PositionAttributeDefinition definition, ApplicationAttribute value, Locale locale, boolean html) {
		try {
			String content;
			if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.question) {
				content = html ? StringHelper.escapeHtml(value.getValue()) : value.getValue();
				content = LongTextRenderer.escapeReturns(content);
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
				SelectConfiguration configuration = definition.getConfiguration(SelectConfiguration.class);
				content = value.getValue();
				content = getLocalizedValues(configuration, content, locale);
				content = html ? StringHelper.escapeHtml(content) : content;
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.number) {
				content = value.getValue();
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.percentage) {
				content = value.getValue() + (html ? "&nbsp;" : " ") + "%";
			} else if(definition.getTypeEnum() == PositionAttributeDefinitionTypeEnum.date) {
				Date date = Formatter.parseDatetime(value.getValue());
				content = DateCellRenderer.format(date);
			} else {
				content = "";
			}
			return content;
		} catch (Exception e) {
			log.error("", e);
			return "";
		}
	}
	
	public void initColumnsModel(FlexiTableColumnModel columnsModel, Position position, String action, Locale locale, List<FlexiTableExtendedFilter> filters) {
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();
		for(int i=0; i<definitions.size(); i++) {
			PositionAttributeDefinition definition = definitions.get(i);
			PositionAttributeDefinitionTypeEnum type = definition.getTypeEnum();
			if(definition.getTabEnum() == tab && (type == PositionAttributeDefinitionTypeEnum.question || type == PositionAttributeDefinitionTypeEnum.select
					|| type == PositionAttributeDefinitionTypeEnum.number || type == PositionAttributeDefinitionTypeEnum.percentage
					|| type == PositionAttributeDefinitionTypeEnum.date)) {
				String label = definition.getLabel(locale, true);
				DefaultFlexiColumnModel column = new DefaultFlexiColumnModel(false, "custom.attribute." + i, COLS_OFFSET + i, action, true, "custom-attr-" + i);
				
				if(type == PositionAttributeDefinitionTypeEnum.question) {
					TextConfiguration configuration = definition.getConfiguration(TextConfiguration.class);
					if(configuration == null || configuration.getMaxLength() > 64) {
						column.setCellRenderer(new LongTextRenderer());
					}
				} else if(type == PositionAttributeDefinitionTypeEnum.select) {
					SelectConfiguration configuration = definition.getConfiguration(SelectConfiguration.class);
					column.setCellRenderer(new SelectAdditionalAttributeCellRenderer(configuration, locale));
				} else if(type == PositionAttributeDefinitionTypeEnum.date) {
					column.setCellRenderer(new DateCellRenderer());
				} else if(type == PositionAttributeDefinitionTypeEnum.percentage) {
					column.setCellRenderer(new PercentageCellRenderer());
				}

				column.setHeaderLabel(label);
				columnsModel.addFlexiColumnModel(column);
				
				if(filters != null) {
					initFilter(label, "filter." + (COLS_OFFSET + i), definition,  filters, locale);
				}
			}
		}
	}
	
	public void initFilter(String label, String filter, PositionAttributeDefinition definition,
			List<FlexiTableExtendedFilter> filters, Locale locale) {
		PositionAttributeDefinitionTypeEnum type = definition.getTypeEnum();
		if(type == PositionAttributeDefinitionTypeEnum.number
				|| type == PositionAttributeDefinitionTypeEnum.percentage) {
			Translator translator = Util.createPackageTranslator(ApplicationAttributesDelegate.class, locale);
			filters.add(new FlexiTableNumericalRangeFilter(label, filter, false,
					translator.translate("from"), translator.translate("to")));
		} else if(type == PositionAttributeDefinitionTypeEnum.text
				|| type == PositionAttributeDefinitionTypeEnum.question) {
			filters.add(new FlexiTableTextFilter(label, filter, false));
		} else if(type == PositionAttributeDefinitionTypeEnum.date) {
			filters.add(new FlexiTableDateRangeFilter(label, filter, false, false, locale));
		} else if(type == PositionAttributeDefinitionTypeEnum.select) {
			SelectConfiguration configuration = definition.getConfiguration(SelectConfiguration.class);
			SelectionValues pk = filterOptions(configuration, locale);
			if(configuration.isMultiple()) {
				filters.add(new FlexiTableMultiSelectionFilter(label, filter, pk, false));
			} else {
				filters.add(new FlexiTableSingleSelectionFilter(label, filter, pk, false));
			}
		}
	}
	
	private SelectionValues filterOptions(SelectConfiguration configuration, Locale locale) {
		List<Option> options = configuration.getOptions();
		SelectionValues keyValues = new SelectionValues();
		for(Option option:options) {
			String opt = option.getValue(locale, true);
			keyValues.add(SelectionValues.entry(opt, opt));
		}
		
		if(configuration.isOther()) {
			Translator translator = Util.createPackageTranslator(ApplicationAttributesDelegate.class, locale);
			keyValues.add(SelectionValues.entry(SELECT_OTHER, translator.translate("other")));
		}
		return keyValues;
	}
	
	public static FieldFilter getFilterValue(FlexiTableFilter filter, int column) {
		if(filter instanceof FlexiTableMultiSelectionFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return new FieldFilter(column, Set.copyOf(filterValues), null, null, null);
			}
			return null;
		}
		if(filter instanceof FlexiTableSingleSelectionFilter selectionFilter) {
			String filterValue = selectionFilter.getValue();
			if(StringHelper.containsNonWhitespace(filterValue)) {
				return new FieldFilter(column, Set.of(filterValue), null, null, null);
			}
			return null;
		}
		if(filter instanceof FlexiTableDateRangeFilter dateFilter) {
			DateRange range = dateFilter.getDateRange();
			if(range != null && (range.getStart() != null || range.getEnd() != null)) {
				return new FieldFilter(column, null, range, null, null);
			}
			return null;
		}
		if(filter instanceof FlexiTableNumericalRangeFilter numericalFilter) {
			NumericalRange range = numericalFilter.getNumericalRange();
			if(range != null && (range.getStart() != null || range.getEnd() != null)) {
				return new FieldFilter(column, null, null, range, null);
			}
			return null;
		}
		if(filter instanceof FlexiTableTextFilter textFilter) {
			String text = textFilter.getValue();
			if(StringHelper.containsNonWhitespace(text)) {
				return new FieldFilter(column, null, null, null, text.toLowerCase());
			}
			return null;
		}
		return null;
	}
	
	public record FieldFilter(int column, Set<String> set, DateRange range, NumericalRange numericalRange, String text) {
		//
	}
	
	public boolean validateFormLabel(TextElement labelEl, PositionAttributeDefinition attributeDefinition, Position position) {
		String val = labelEl.getValue();
		
		
		List<PositionAttributeDefinition> attrs;
		if(position != null) {
			attrs = position.getAttributesDefinitions();
		} else {
			attrs = recruitingService.getGlobalAttributeDefinition();
		}
		
		for(PositionAttributeDefinition attr:attrs) {
			if(attr == null || attr.equals(attributeDefinition)) {
				continue;
			}
			if(attr.useLabel(val)) {
				labelEl.setErrorKey("error.label.in.use");
				return false;
			}
		}
		return true;
	}
	
	private static class OptionComparator implements Comparator<Option> {
		
		private final Locale locale;
		private final Collator collator;
		
		public OptionComparator(Locale locale) {
			this.locale = locale;
			collator = Collator.getInstance(locale);
		}

		@Override
		public int compare(Option o1, Option o2) {
			if(o1 == null && o2 == null) {
				return 0;
			} else if(o1 == null) {
				return -1;
			} else if(o2 == null) {
				return 1;
			}
			
			String val1 = o1.getValue(locale);
			String val2 = o2.getValue(locale);
			
			if(val1 == null && val2 == null) {
				return 0;
			} else if(val1 == null) {
				return -1;
			} else if(val2 == null) {
				return 1;
			}
			return collator.compare(val1, val2);
		}
	}
}
