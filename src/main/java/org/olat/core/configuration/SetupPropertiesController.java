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

package org.olat.core.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.olat.core.configuration.gui.PropertyValueCellRenderer;
import org.olat.core.configuration.gui.RedundantEntryIconCellRenderer;
import org.olat.core.configuration.model.OlatPropertiesTableContentRow;
import org.olat.core.configuration.model.OlatPropertiesTableModel;
import org.olat.core.configuration.model.OlatPropertiesTableModel.OlatPropertiesFilter;
import org.olat.core.configuration.model.OlatPropertiesTableModel.OlatPropertiesTableColumn;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Initial Date: 27.08.2020 <br>
 * 
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com,
 *         http://www.frentix.com
 */
public class SetupPropertiesController extends FormBasicController {
	
	Properties defaultProperties = new Properties();
	Properties overwriteProperties = new Properties();
	Properties systemProperties = new Properties();

	private List<OLATProperty> defaultProps = new ArrayList<>();
	private List<OLATProperty> overwriteProps = new ArrayList<>();
	private List<OLATProperty> systemProps = new ArrayList<>();

	FlexiTableElement defaultPropsTableEl;
	OlatPropertiesTableModel defaultPropsTableModel;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public SetupPropertiesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);

		Resource olatDefaultPropertiesRes = new ClassPathResource("/serviceconfig/olat.properties");
		Resource overwritePropertiesRes = new ClassPathResource("olat.local.properties");

		try {
			defaultProperties.load(olatDefaultPropertiesRes.getInputStream());
			overwriteProperties.load(overwritePropertiesRes.getInputStream());
			systemProperties = System.getProperties();
		} catch (IOException e) {
			logError("Could not load properties files from classpath", e);
		}

		analyzeProperties();
		initForm(ureq);
		loadModel();

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("olat.properties.description");

		// Create columns
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		PropertyValueCellRenderer valueCellRenderer = new PropertyValueCellRenderer();
		RedundantEntryIconCellRenderer redundantEntryIconCellRenderer = new RedundantEntryIconCellRenderer();

		DefaultFlexiColumnModel keyColumn = new DefaultFlexiColumnModel(OlatPropertiesTableColumn.key);
		DefaultFlexiColumnModel defaultValueColumn = new DefaultFlexiColumnModel(OlatPropertiesTableColumn.defaultValue);
		DefaultFlexiColumnModel overwriteValueColumn = new DefaultFlexiColumnModel(OlatPropertiesTableColumn.overwriteValue);
		DefaultFlexiColumnModel systemValueColumn = new DefaultFlexiColumnModel(OlatPropertiesTableColumn.systemProperty);
		DefaultFlexiColumnModel hasRedundantEntryColumn = new DefaultFlexiColumnModel(OlatPropertiesTableColumn.icon);

		defaultValueColumn.setCellRenderer(valueCellRenderer);
		overwriteValueColumn.setCellRenderer(valueCellRenderer);
		systemValueColumn.setCellRenderer(valueCellRenderer);
		hasRedundantEntryColumn.setCellRenderer(redundantEntryIconCellRenderer);
		hasRedundantEntryColumn.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_WARN));

		columnModel.addFlexiColumnModel(keyColumn);
		columnModel.addFlexiColumnModel(defaultValueColumn);
		columnModel.addFlexiColumnModel(overwriteValueColumn);
		columnModel.addFlexiColumnModel(systemValueColumn);
		columnModel.addFlexiColumnModel(hasRedundantEntryColumn);

		defaultPropsTableModel = new OlatPropertiesTableModel(columnModel);
		defaultPropsTableEl = uifactory.addTableElement(getWindowControl(), "defaultProps", defaultPropsTableModel, 50, false, getTranslator(), formLayout);
		defaultPropsTableEl.setExportEnabled(true);
		defaultPropsTableEl.setSearchEnabled(true);
		defaultPropsTableEl.setShowAllRowsEnabled(true);

		initFilters(defaultPropsTableEl);

	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void initFilters(FlexiTableElement tableElement) {
		List<FlexiTableFilter> filters = new ArrayList<>(16);
		filters.add(new FlexiTableFilter(translate("filter.show.all"), OlatPropertiesFilter.showAll.name(), true));
		filters.add(new FlexiTableFilter(translate("filter.show.overwritten.only"), OlatPropertiesFilter.showOverwrittenOnly.name()));
		filters.add(new FlexiTableFilter(translate("filter.show.redundant.only"), OlatPropertiesFilter.showRedundantOnly.name()));
		defaultPropsTableEl.setFilters(null, filters, false);
	}

	private void loadModel() {
		// Load data
		Map<String, OlatPropertiesTableContentRow> propertiesMap = new HashMap<>(defaultProperties.size());
		
		for (OLATProperty defaultProperty : defaultProps) {
			OlatPropertiesTableContentRow row = new OlatPropertiesTableContentRow();
			row.setDefaultProperty(defaultProperty);
			propertiesMap.put(defaultProperty.getKey(), row);
		}
		
		for (OLATProperty overwriteProperty : overwriteProps) {
			if (propertiesMap.containsKey(overwriteProperty.getKey())) {
				propertiesMap.get(overwriteProperty.getKey()).setOverwriteProperty(overwriteProperty);
			} else {
				OlatPropertiesTableContentRow row = new OlatPropertiesTableContentRow();
				row.setOverwriteProperty(overwriteProperty);
				propertiesMap.put(overwriteProperty.getKey(), row);
			}
		}
		
		for (OLATProperty systemProperty : systemProps) {
			if (propertiesMap.containsKey(systemProperty.getKey())) {
				propertiesMap.get(systemProperty.getKey()).setSystemProperty(systemProperty);
			} else {
				OlatPropertiesTableContentRow row = new OlatPropertiesTableContentRow();
				row.setSystemProperty(systemProperty);
				propertiesMap.put(systemProperty.getKey(), row);
			}
		}

		// Set data
		defaultPropsTableModel.setObjects(new ArrayList<>(propertiesMap.values()));
		defaultPropsTableEl.reset(true, true, true);
	}

	private void analyzeProperties() {
		Set<Object> defaultKeySet = defaultProperties.keySet();
		Set<Object> overwriteKeySet = overwriteProperties.keySet();
		Set<Object> systemKeySet = systemProperties.keySet();

		for (Object key : defaultKeySet) {
			String keyValue = (String) key;
			OLATProperty prop = new OLATProperty(keyValue, defaultProperties.getProperty(keyValue));
			if (overwriteProperties.containsKey(keyValue)) {
				prop.setOverwritten(true);
				prop.setOverwriteValue(overwriteProperties.getProperty(keyValue));
			}
			if (defaultProperties.getProperty(keyValue + ".comment") != null) {
				prop.setComment(defaultProperties.getProperty(keyValue + ".comment"));
			}
			if (defaultProperties.getProperty(keyValue + ".values") != null) {
				prop.setAvailableValues(defaultProperties.getProperty(keyValue + ".values"));
			}
			if (!keyValue.endsWith(".comment") && !keyValue.endsWith(".values")) {
				defaultProps.add(prop);
			}
		}

		// insert delimiters between property groups
		groupProperties(defaultProps, "db.");
		groupProperties(defaultProps, "ldap.");
		groupProperties(defaultProps, "instantMessaging.");

		for (Object key : overwriteKeySet) {
			String keyValue = (String) key;

			OLATProperty prop = new OLATProperty(keyValue.trim(), overwriteProperties.getProperty(keyValue).trim());
			overwriteProps.add(prop);
		}
		for (Object key : systemKeySet) {
			String keyValue = (String) key;

			OLATProperty prop = new OLATProperty(keyValue.trim(), systemProperties.getProperty(keyValue).trim());
			systemProps.add(prop);
		}
	}

	private void groupProperties(List<OLATProperty> defProps, String group) {
		int i = 0;
		int firstPos = 0;
		int lastPos = 0;
		boolean found = false;
		OLATProperty delimiter = new OLATProperty("delimiter", "delimiter");

		for (OLATProperty olatProperty : defProps) {
			if (!found && olatProperty.getKey().startsWith(group)) {
				firstPos = i;
				found = true;
			} else if (olatProperty.getKey().startsWith(group)) {
				lastPos = i;
			}
			i++;
		}
		defProps.add(firstPos, delimiter);
		defProps.add(lastPos + 2, delimiter);

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (event instanceof FlexiTableSearchEvent) {
			FlexiTableSearchEvent ftse = (FlexiTableSearchEvent) event;
			String searchString = ftse.getSearch();
			defaultPropsTableModel.filter(searchString, null);
			defaultPropsTableEl.reset(true, true, true);
		}
		super.formInnerEvent(ureq, source, event);
	}
}
