/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

public class GuiDemoTablesController extends BasicController {
	
	VelocityContainer vcMain;
	TableController table;
	TableDataModel<List<Object>> model;
	
	public GuiDemoTablesController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);
	
		vcMain = this.createVelocityContainer("guidemo-table");
		TableGuiConfiguration tgc = new TableGuiConfiguration();
		tgc.setPreferencesOffered(true, "TableGuiDemoPrefs");
		table = new TableController(tgc, ureq, getWindowControl(), getTranslator());
		listenTo(table);
		table.setMultiSelect(true);
		table.addMultiSelectAction("guidemo.table.submit", "submitAction");
		table.addMultiSelectAction("guidemo.table.submit2", "submitAction2");
		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header1", 0, null, ureq.getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header2", 1, null, ureq.getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header3", 2, null, ureq.getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header4", 3, null, ureq.getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header5", 4, null, ureq.getLocale()));
		table.addColumnDescriptor(new CustomRenderColumnDescriptor("guidemo.table.header6", 5, null, ureq.getLocale(), CustomRenderColumnDescriptor.ALIGNMENT_CENTER, new ImageCellRenderer()));
		table.addColumnDescriptor(new StaticColumnDescriptor("action.select", "guidemo.table.header7", "Select"));
		model = new SampleTableModel();
		table.setTableDataModel(model);
		vcMain.put("table", table.getInitialComponent());
		
		//add source view control
    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), vcMain);
    vcMain.put("sourceview", sourceview.getInitialComponent());
		
		this.putInitialPanel(vcMain);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

class ImageCellRenderer implements CustomCellRenderer {

	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		sb.append("<img src=\"");
		Renderer.renderStaticURI(sb, "images/openolat/openolat_logo_16.png");
		sb.append("\" alt=\"An image within a table...\" />");
	}
	
}

class SampleTableModel extends BaseTableDataModelWithoutFilter<List<Object>> {
	
	private int COLUMN_COUNT = 7;
	private List<List<Object>> entries;
	
	public SampleTableModel() {
		int iEntries = 50;
		this.entries = new ArrayList<>(iEntries);
		for (int i=0; i < iEntries; i++) {
			List<Object> row = new ArrayList<>(5);
			row.add("Lorem" + i);
			row.add("Ipsum" + i);
			row.add("Dolor" + i);
			row.add("Sit" + i);
			row.add(Integer.toString(i));
			row.add("");
			entries.add(row);
		}
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	public int getRowCount() {
		return entries.size();
	}

	public Object getValueAt(int row, int col) {
		List<Object> entry = entries.get(row);
		return entry.get(col);
	}

}