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


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.gui.ConfigurationTreeCellRenderer;
import org.olat.core.configuration.gui.PropertyValueCellRenderer;
import org.olat.core.configuration.model.ConfigurationPropertiesContentRow;
import org.olat.core.configuration.model.ConfigurationPropertiesTableModel;
import org.olat.core.configuration.model.ConfigurationPropertiesTableModel.ConfigurationPropertiesCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;

/**
 * Initial Date:  27.08.2020 <br>
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com, http://www.frentix.com
 */

public class ConfigurationPropertiesController extends FormBasicController {
    
	private static final Logger log = Tracing.createLoggerFor(ConfigurationPropertiesController.class);
	
	private FlexiTableElement tableEl;
	private ConfigurationPropertiesTableModel tableModel;
	

    public ConfigurationPropertiesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("olat.local.properties.description");
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		ConfigurationTreeCellRenderer treeNodeRenderer = new ConfigurationTreeCellRenderer();
		PropertyValueCellRenderer valueCellRenderer = new PropertyValueCellRenderer();
		
		DefaultFlexiColumnModel keyColumn = new DefaultFlexiColumnModel(ConfigurationPropertiesCols.key);
		keyColumn.setCellRenderer(treeNodeRenderer);
		
		DefaultFlexiColumnModel valueColumn = new DefaultFlexiColumnModel(ConfigurationPropertiesCols.value);
		valueColumn.setCellRenderer(valueCellRenderer);
		
		columnsModel.addFlexiColumnModel(keyColumn);
		columnsModel.addFlexiColumnModel(valueColumn);
		
		tableModel = new ConfigurationPropertiesTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setExportEnabled(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel() {
		List<ConfigurationPropertiesContentRow> rows = new ArrayList<>();
		
		
		getConfigurationData().entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey()))
		.forEach(entry -> {
			ConfigurationPropertiesContentRow parent = new ConfigurationPropertiesContentRow(entry.getKey());
			parent.setHasChildren(entry.getValue().entrySet().size() > 0);
			rows.add(parent);
			List<ConfigurationPropertiesContentRow> children = new ArrayList<>();
			
			entry.getValue().entrySet().forEach(configuration -> {
				OLATProperty property = new OLATProperty((String)configuration.getKey(), (String)configuration.getValue());
				children.add(new ConfigurationPropertiesContentRow(property, parent));
			});
			
			Collator collator = Collator.getInstance(getLocale());
			collator.setStrength(Collator.IDENTICAL);
			
			Collections.sort(children, Comparator.comparing(child -> child.getKey(), collator));
			rows.addAll(children);
		});
		
		tableModel.setObjects(rows);
		tableEl.reset(true,true,true);
	}
	
	private HashMap<String, Properties> getConfigurationData() {
		HashMap<String, Properties> properties = new HashMap<>();
		String userDataDirectory = WebappHelper.getUserDataRoot();
		File folder = Paths.get(userDataDirectory, "system", "configuration").toFile();
		List<File>listOfFiles = new ArrayList<>(Arrays.asList(folder.listFiles()));

		for (File file : listOfFiles) {
			String extension = "";

			int i = file.getName().lastIndexOf('.');
			if (i > 0) {
			    extension = file.getName().substring(i+1);
			}
			if (file.isFile() && extension.equals("properties")) {
				Properties fileProperties = getPropertyFile(file.getName());
				if(fileProperties != null){
					properties.put(file.getName(), fileProperties);
				}
			}
		}
		
		return properties;
	}

	private Properties getPropertyFile(String filename) {
		String userDataDirectory = WebappHelper.getUserDataRoot();
		File configurationPropertiesFile = Paths.get(userDataDirectory, "system", "configuration", filename).toFile();
		if (configurationPropertiesFile.exists()) {
			InputStream is = null;
			OutputStream fileStream = null;
			try {
				is = new FileInputStream(configurationPropertiesFile);
				Properties configuredProperties = new Properties();
				configuredProperties.load(is);
				is.close();	
				return configuredProperties;				
			} catch (Exception e) {
				log.error("Error when reading / writing user properties config file from path::" + configurationPropertiesFile.getAbsolutePath(), e);
				return null;
			} finally {
				try {
					if (is != null ) is.close();
					if (fileStream != null ) fileStream.close();
				} catch (Exception e) {
					log.error("Could not close stream after storing config to file::" + configurationPropertiesFile.getAbsolutePath(), e);
				}
			}
		}
		
		return null;
	}

    
}