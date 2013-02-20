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
* <p>
*/ 
package org.olat.core.gui.components.form.flexible.impl.elements.table;


import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Render the table as a long HTML table
 * @author Christian Guretzki
 */
class FlexiTableClassicRenderer implements ComponentRenderer {

	public void render(Renderer renderer, StringOutput target, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		//
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();

		String id = ftC.getFormDispatchId();
		target.append("<div class=\"b_table_wrapper b_floatscrollbox\">")
		  .append("<table id=\"").append(id).append("\">");
		
		FlexiTableDataModel dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnsModel = dataModel.getTableColumnModel();

		int rows = dataModel.getRowCount();
		int cols = columnsModel.getColumnCount();

		// 1. build header links
		target.append("<thead><tr>");

		for (int i = 0; i < cols; i++) {
				FlexiColumnModel fcm = columnsModel.getColumnModel(i);
				String header = translator.translate(fcm.getHeaderKey());

				target.append("<th class=\"");
				// add css class for first and last column to support older browsers
				if (i == 0) target.append(" b_first_child");
				if (i == cols-1) target.append(" b_last_child");
				target.append("\">").append(header).append("</th>");
  	}
		target.append("</tr></thead>");

		// build rows
		target.append("<tbody>");
		// the really selected rowid (from the tabledatamodel)
		
		for (int i = 0; i < rows; i++) {
			// use alternating css class
			String cssClass;
			if (i % 2 == 0) cssClass = "";
			else cssClass = "b_table_odd";
			// add css class for first and last column to support older browsers
			if (i == 0) cssClass += " b_first_child";
			if (i == rows-1) cssClass += " b_last_child";

			target.append("<tr class=\"").append(cssClass).append("\">");
			for (int j = 0; j < cols; j++) {
				FlexiColumnModel fcm = columnsModel.getColumnModel(j);
				int alignment = fcm.getAlignment();
				cssClass = (alignment == FlexiColumnModel.ALIGNMENT_LEFT ? "b_align_normal" : (alignment == FlexiColumnModel.ALIGNMENT_RIGHT ? "b_align_inverse" : "b_align_center"));
				// add css class for first and last column to support older browsers
				if (j == 0) cssClass += " b_first_child";
				if (j == cols-1) cssClass += " b_last_child";				
				target.append("<td class=\"").append(cssClass).append("\">");
				if (j == 0) target.append("<a name=\"table\"></a>"); //add once for accessabillitykey
				
				int columnIndex = fcm.getColumnIndex();
				Object cellValue = columnIndex >= 0 ? dataModel.getValueAt(i, columnIndex) : null;
				if (cellValue instanceof FormItem) {
					FormItem formItem = (FormItem)cellValue;
					formItem.setTranslator(translator);
					formItem.setRootForm(ftE.getRootForm());
					formItem.getComponent().getHTMLRendererSingleton().render(renderer, target, formItem.getComponent(), ubu, translator, renderResult, args);
				} else {
					fcm.getCellRenderer().render(target, cellValue, i, ftC, ubu, translator);
				}
				target.append("</td>");
			}
			target.append("</tr>");
			
		}				
		// end of table table
		target.append("</tbody></table>");
		target.append("</div>");
		
		if (source.isEnabled()) {
			// add set dirty form only if enabled
			target.append(FormJSHelper.getJSStartWithVarDeclaration(id));
			target.append(FormJSHelper.getSetFlexiFormDirty(ftE.getRootForm(), id));
			target.append(FormJSHelper.getJSEnd());
		}
	}

	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		// 
	}

	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		//
	}
}