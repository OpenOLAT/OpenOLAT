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

package org.olat.test.guidemo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElment;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CustomFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

public class GuiDemoFlexiTablesController extends FormBasicController {
	
	VelocityContainer vcMain;
	
	public GuiDemoFlexiTablesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		initForm(ureq);		
	}

	public void event(UserRequest ureq, Component source, Event event) {
	}

	protected void doDispose() {
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header1"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header2"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header3"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header4"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header5"));
		// column 6 : Image depending on True/False value / center alignment
		FlexiColumnModel exampleCustomColumnModel = new DefaultFlexiColumnModel("guidemo.table.header6");
		exampleCustomColumnModel.setCellRenderer(new ExampleCustomFlexiCellRenderer() );
		exampleCustomColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
		tableColumnModel.addFlexiColumnModel(exampleCustomColumnModel);
    // column 7 : Link
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header7"));

		FlexiTableDataModel tableDataModel = FlexiTableDataModelFactory.createFlexiTableDataModel(new SampleFlexiTableModel(this), tableColumnModel);
		FlexiTableElment fte = FormUIFactory.getInstance().addTableElement("gui-demo", tableDataModel, formLayout);
	}	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		// TODO: ADD VALIDATION CHECK
		return isInputValid;			
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, org.olat.core.gui.components.form.flexible.FormItem source, FormEvent event) {
		System.out.println("TEST formInnerEvent");
	}
	
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);  
	}
	
	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);      
	}	
	
}

/**
 * Example legacy (non-flexi-table) table-model.
 * @author guretzki
 */
class SampleFlexiTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
	
	private int COLUMN_COUNT = 7;
	private List entries;
	
	public SampleFlexiTableModel(Controller controller) {
		int iEntries = 50;
		this.entries = new ArrayList(iEntries);
		for (int i=0; i < iEntries; i++) {
			List row = new ArrayList(COLUMN_COUNT);
			// column 1 : checkbox
			row.add("Flexi Lorem" + i);
			// column 2 : checkbox
			row.add("Ipsum" + i);
			// column 3 : checkbox
			MultipleSelectionElement checkbox = new MultipleSelectionElementImpl("checkbox", MultipleSelectionElementImpl.createVerticalLayout("checkbox",1)) {
				{
					keys = new String[] { "ison", "isOff" };
					values = new String[] { "on", "off" };
					select("ison", true);
				}
			};
			row.add(checkbox);
			// column 4 : Integer
			row.add(Integer.toString(i));
			// column 5 : Date
			row.add(new Date());
			// column 6 : Boolean value => custom rendered with two images
			row.add((i % 2 == 0) ? Boolean.TRUE : Boolean.FALSE);
			// column 7: Action Link
			FormLinkImpl link = new FormLinkImpl("choose");
			link.setUserObject(Integer.valueOf(i));
			link.addActionListener(controller, FormEvent.ONCLICK);
			row.add(link);
			
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
		List entry = (List)entries.get(row);
		return entry.get(col);
	}

}

/**
 * Example for custom cell renderer. Add one of two images depending on a 
 * boolean value.
 * @author guretzki
 */
class ExampleCustomFlexiCellRenderer extends CustomFlexiCellRenderer {

	public void render(StringOutput target, Object cellValue, Translator translator) {
		if (cellValue instanceof Boolean) {
			if ( ((Boolean)cellValue).booleanValue() ) {
				target.append("<img src=\"");
				Renderer.renderStaticURI(target, "images/olat/olatlogo16x16.png");
				target.append("\" alt=\"An image within a table...\" />");				
			} else {
				target.append("<img src=\"");
				Renderer.renderStaticURI(target, "images/olat/bug.png");
				target.append("\" alt=\"An image within a table...\" />");				
			}
		}
	}
	
}
