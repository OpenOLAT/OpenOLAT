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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Render hole flexi table.
 * @author Christian Guretzki
 */
class FlexiTableRenderer implements ComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput target, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		//
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel dataModel = ftE.getTableDataModel();
		
		Form rootForm = ftE.getRootForm();
		String id = ftC.getFormDispatchId();

		target.append("<div class=\"b_table_wrapper b_floatscrollbox\">")
		      .append("<table id=\"").append(id).append("\">");
		      
		renderTHead(target, ftE, translator);
		renderTBody(renderer, target, ftC, ubu, translator, renderResult);

		target.append("</table>")
		       .append("</div>");
		
		if (source.isEnabled()) {
			target.append(FormJSHelper.getJSStartWithVarDeclaration(id));
			target.append(FormJSHelper.getSetFlexiFormDirty(ftE.getRootForm(), id));
			target.append(FormJSHelper.getJSEnd());
		}
		
		int rows = dataModel.getRowCount();
		
		target.append("<script>")
		  .append("jQuery(function() {\n")
      .append("	jQuery('#").append(id).append("').dataTable( {\n")
      .append("		'bScrollInfinite': true,\n")
      .append("		'bScrollCollapse': true,\n")
      .append("		'sScrollY': '200px',\n")
      .append("		'bProcessing': true,\n")
      .append("		'bServerSide': true,\n")
      .append("		'iDisplayLength': 20,\n")
      .append("		'iDeferLoading': ").append(rows).append(",\n")
      .append("		'sAjaxSource': '").append(ftE.getMapperUrl()).append("',\n")
      .append("		'fnRowCallback': function( nRow, aData, iDisplayIndex ) {\n")
      .append("			jQuery(nRow).draggable({ ")
      .append("				containment: '#b_main',")
      .append("				accept: function(event,ui){ console.log('Accept'); return true; },\n")
      .append("				helper: function(event) {\n")
      .append("				  return jQuery(\"<div class='ui-widget-header' style='z-index:10000;'>I'm a custom helper</div>\");\n")
      .append("				},\n")
      .append("				start: function(event,ui){ console.log('Start'); },\n")
      .append("				stop: function(event,ui){ console.log('Stop'); }\n")
      .append("			});\n")
      .append("		},\n")
      .append("		'aoColumns': [\n")
      .append("			{'mData':'choice', bSortable: false },\n")
      .append("			{'mData':'key'},\n")
      .append("			{'mData':'subject'},\n")
      .append("			{'mData':'select', bSortable: false },\n")
      .append("			{'mData':'mark', bSortable: false }\n")
      .append("		]\n")
      .append("	});\n")
      //clic rows
      .append("	jQuery('#").append(id).append(" tbody tr').live('click', function(event, ui) {\n")
      .append("   var link = false;\n")
      .append("   var rowId = null;\n")
      .append("   if(event.target.tagName == 'A' || event.target.tagName == 'INPUT') {\n")
      .append("     return;\n")
      .append("   }\n")
      .append("   jQuery(event.target).parents().each(function(index,el) {\n")
      .append("     if(el.tagName == 'A' || el.tagName == 'INPUT') {\n")
      .append("       link = true;\n")
      .append("     } else if (el.tagName == 'TR' && rowId == null) {\n")
      .append("       rowId = jQuery(el).attr('id');\n")
      .append("       return false;\n")
      .append("     }\n")
      .append("   });\n")
      .append("	  if(!link) {\n")
      .append(FormJSHelper.generateXHRFnCallVariables(rootForm, id, 1))
      .append("    o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'select',rowId);")
      .append("   };")
      .append("	});\n")
      .append("});\n")
		  .append("</script>\n");
	}
	
	/**
	 * 
	 * @param target
	 * @param ftE
	 * @param translator
	 */
	private void renderTHead(StringOutput target, FlexiTableElementImpl ftE, Translator translator) {
		FlexiTableDataModel dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnModel = dataModel.getTableColumnModel();
		      
		target.append("<thead><tr>");

		int col = 0;
		if(ftE.isMultiSelect()) {
			target.append("<th class='b_first_child'>").append("choice").append("</th>");
			col++;
		}
		
		int cols = columnModel.getColumnCount();
		for(int i=0; i<cols; i++) {
			FlexiColumnModel fcm = columnModel.getColumnModel(i);
			String header = translator.translate(fcm.getHeaderKey());
				
			target.append("<th class=\"");
			// add css class for first and last column to support older browsers
			if (col == 0) target.append(" b_first_child");
			if (col == cols-1) target.append(" b_last_child");
			target.append("\">").append(header).append("</th>");
			col++;
  	}
		target.append("</tr></thead>");
	}
	
	public void renderTBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnModel = dataModel.getTableColumnModel();

		// build rows
		target.append("<tbody>");
		
		// the really selected rowid (from the tabledatamodel)
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);
		int cols = columnModel.getColumnCount();
		
		String rowIdPrefix = "row_" + id + "-";
		
		for (int i = firstRow; i < lastRow; i++) {
			// use alternating css class
			String cssClass;
			if (i % 2 == 0) cssClass = "";
			else cssClass = "b_table_odd";
			// add css class for first and last column to support older browsers
			if (i == 0) cssClass += " b_first_child";
			if (i == rows-1) cssClass += " b_last_child";

			target.append("<tr id='").append(rowIdPrefix).append(i)
			      .append("' class=\"").append(cssClass).append("\">");
			
			int col = 0;
			if(ftE.isMultiSelect()) {
				target.append("<td class='b_first_child'>")
				      .append("<input type='checkbox' name='ftb_ms' value='").append(rowIdPrefix).append(i).append("'");
				if(ftE.isMultiSelectedIndex(i)) {
					target.append(" checked='checked'");
				}   
				target.append("/></td>");
				col++;
			}
			
			for (int j = 0; j < cols; j++) {
				FlexiColumnModel fcm = ftE.getTableDataModel().getTableColumnModel().getColumnModel(j);
				int alignment = fcm.getAlignment();
				cssClass = (alignment == FlexiColumnModel.ALIGNMENT_LEFT ? "b_align_normal" : (alignment == FlexiColumnModel.ALIGNMENT_RIGHT ? "b_align_inverse" : "b_align_center"));
				// add css class for first and last column to support older browsers
				if (col == 0) cssClass += " b_first_child";
				if (col == cols-1) cssClass += " b_last_child";				
				target.append("<td class=\"").append(cssClass).append("\">");
				if (col == 0) target.append("<a name=\"table\"></a>"); //add once for accessabillitykey
				
				Object cellValue = ftE.getTableDataModel().getValueAt(i, j);
				if (cellValue instanceof FormItem) {
					FormItem formItem = (FormItem)cellValue;
					formItem.setTranslator(translator);
					if(ftE.getRootForm() != formItem.getRootForm()) {
						formItem.setRootForm(ftE.getRootForm());
					}
					formItem.getComponent().getHTMLRendererSingleton().render(renderer, target, formItem.getComponent(),
							ubu, translator, renderResult, null);
					ftE.addFormItem(formItem);
				} else {
					ftE.getTableDataModel().getTableColumnModel().getColumnModel(j).
						getCellRenderer().render(target, cellValue, translator);
				}
				target.append("</td>");
			}
			target.append("</tr>");
			
		}				
		// end of table table
		target.append("</tbody>");
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer,
			StringOutput sb, Component source, RenderingState rstate) {
		//
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb,
			Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		//
	}
}