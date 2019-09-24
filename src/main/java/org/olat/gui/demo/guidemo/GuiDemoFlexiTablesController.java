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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * @author guretzki
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GuiDemoFlexiTablesController extends FormBasicController {
	
	private FlexiTableDataModel<Row> tableDataModel;
	
	public GuiDemoFlexiTablesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "flexitable");
		initForm(ureq);		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header1", 0));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header2", 1));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header3", 2));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header4", 3));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header5", 4));
		// column 6 : Image depending on True/False value / center alignment
		FlexiColumnModel exampleCustomColumnModel = new DefaultFlexiColumnModel("guidemo.table.header6", 5);
		exampleCustomColumnModel.setCellRenderer(new ExampleCustomFlexiCellRenderer() );
		exampleCustomColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
		tableColumnModel.addFlexiColumnModel(exampleCustomColumnModel);
    // column 7 : Link
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("guidemo.table.header7", 6));

		tableDataModel = new FlexiTableDataModelImpl<>(new SampleFlexiTableModel(formLayout), tableColumnModel);
		uifactory.addTableElement(getWindowControl(), "gui-demo", tableDataModel, getTranslator(), formLayout);
		uifactory.addFormSubmitButton("ok", formLayout);
	}
	
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		//do some validation here
		return isInputValid && super.validateFormLogic(ureq);			
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, org.olat.core.gui.components.form.flexible.FormItem source, FormEvent event) {
		System.out.println("TEST formInnerEvent");
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Object obj = tableDataModel.getValueAt(0, 2);
		if(obj instanceof MultipleSelectionElement) {
			MultipleSelectionElement selection = (MultipleSelectionElement)obj;
			System.out.println(selection.getSelectedKeys());
			
		}
		fireEvent(ureq, Event.DONE_EVENT);  
	}
	
	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);      
	}	
	
	/**
	 * Example legacy (non-flexi-table) table-model.
	 * @author guretzki
	 */
	private class SampleFlexiTableModel extends BaseTableDataModelWithoutFilter<Row> {
		private final int COLUMN_COUNT = 7;
		private final List<Row> entries;

		public SampleFlexiTableModel(FormItemContainer formContainer) {
			int iEntries = 50;
			entries = new ArrayList<>(iEntries);
			for (int i=0; i < iEntries; i++) {
				entries.add(new Row(i, formContainer));
			}
		}

		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		public int getRowCount() {
			return entries.size();
		}

		public Object getValueAt(int row, int col) {
			Row entry = entries.get(row);
			switch(col) {
				case 0: return entry.getCol1();
				case 1: return entry.getCol2();
				case 2: return entry.getSelection3();
				case 3: return entry.getCol4();
				case 4: return entry.getCol5();
				case 5: return entry.getCol6();
				case 6: return entry.getCol7();
				default: return entry;
			}
		}
	}
	
	private class Row {
		private final String col1;
		private final String col2;
		private final MultipleSelectionElement selection3;
		private final String col4;
		private final Date col5;
		private final Boolean col6;
		private final FormLink col7;
		
		public Row(int i, FormItemContainer formContainer) {
			// column 1 : string
			col1 = "Flexi Lorem" + i;
			// column 2 : string
			col2 = "Ipsum" + i;
			// column 3 : checkbox
			
			selection3 = new MultipleSelectionElementImpl("checkbox", Layout.vertical);
			String[] keys = new String[] { "ison", "isOff" };
			String[] values = new String[] { "on", "off" };
			selection3.setKeysAndValues(keys, values);
			selection3.select("ison", true);
			
			formContainer.add(UUID.randomUUID().toString(), selection3);
			
			// column 4 : Integer
			col4 = Integer.toString(i);
			// column 5 : Date
			col5 = new Date();
			// column 6 : Boolean value => custom rendered with two images
			col6 = ((i % 2 == 0) ? Boolean.TRUE : Boolean.FALSE);
			// column 7: Action Link
			FormLinkImpl link = new FormLinkImpl("choose");
			link.setUserObject(Integer.valueOf(i));
			link.addActionListener(FormEvent.ONCLICK);
			col7 = link;
		}

		public String getCol1() {
			return col1;
		}

		public String getCol2() {
			return col2;
		}

		public MultipleSelectionElement getSelection3() {
			return selection3;
		}

		public String getCol4() {
			return col4;
		}

		public Date getCol5() {
			return col5;
		}

		public Boolean getCol6() {
			return col6;
		}

		public FormLink getCol7() {
			return col7;
		}	
	}

	/**
	 * Example for custom cell renderer. Add one of two images depending on a 
	 * boolean value.
	 * @author guretzki
	 */
	private class ExampleCustomFlexiCellRenderer implements FlexiCellRenderer {
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (cellValue instanceof Boolean) {
				if ( ((Boolean)cellValue).booleanValue() ) {
					target.append("<img src=\"");
					Renderer.renderStaticURI(target, "images/openolat/openolat_logo_16.png");
					target.append("\" alt=\"An image within a table...\" />");				
				} else {
					target.append("<img src=\"");
					Renderer.renderStaticURI(target, "images/openolat/bug.png");
					target.append("\" alt=\"An image within a table...\" />");				
				}
			}
		}
	}
}


