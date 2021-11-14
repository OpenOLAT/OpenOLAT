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
package org.olat.admin.sysinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 19.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JavaEnvironmmentPropertiesController extends BasicController {
	private static final int lineCut = 100;
	
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	
	public JavaEnvironmmentPropertiesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("jvm_envprops");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setPageingEnabled(false);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.name.i18nKey(), Cols.name.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.value.i18nKey(), Cols.value.ordinal(), null, getLocale()));
		listenTo(tableCtr);
		loadModel();
		mainVC.put("javaenv", tableCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private void loadModel() {
		Properties p = System.getProperties();
		List<NameValuePair> pairs = new ArrayList<>(p.size());
		for(Map.Entry<Object,Object> entry : p.entrySet()) {
			String name = entry.getKey().toString();
			String value = entry.getValue().toString();
			pairs.add(new NameValuePair(name, value));
		}
		tableCtr.setTableDataModel(new PropertiesDataModel(pairs));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private enum Cols {
		name("java.envprops.name"),
		value("java.envprops.value");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private static class NameValuePair {
		private final String name;
		private final String value;
		
		public NameValuePair(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}

	private static class PropertiesDataModel extends DefaultTableDataModel<NameValuePair> {
		public PropertiesDataModel(List<NameValuePair> threads) {
			super(threads);
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public Object getValueAt(int row, int col) {
			NameValuePair view = getObject(row);
			switch(Cols.values()[col]) {
				case name: return view.getName();
				case value: {
					String value = view.getValue();
					if(value.length() < lineCut) {
						return value;
					}
					StringBuilder props = new StringBuilder(value.length() + 50);
					while (value.length() > lineCut) {
						value = (props.length() == 0 ? "" : "<br />") + value.substring(lineCut);
						props.append(value.substring(0,	value.length() > lineCut ? lineCut : value.length()));
					}
					return props.toString();
				}
				default: return "ERROR";
			}
		}
	}
	

}
