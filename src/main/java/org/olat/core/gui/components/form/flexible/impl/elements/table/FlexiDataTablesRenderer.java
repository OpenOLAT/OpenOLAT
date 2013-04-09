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
package org.olat.core.gui.components.form.flexible.impl.elements.table;


import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Render the table using the jQuery plugin DataTables
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class FlexiDataTablesRenderer extends AbstractFlexiTableRenderer implements ComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput target, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		super.render(renderer, target, source, ubu, translator, renderResult, args);
		
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnsModel = dataModel.getTableColumnModel();
		
		Form rootForm = ftE.getRootForm();
		String id = ftC.getFormDispatchId();
		int rows = dataModel.getRowCount();
		
		target.append("<script type='text/javascript'>")
		  .append("jQuery(function() {\n")
      .append("	jQuery('#").append(id).append("').dataTable( {\n")
      .append("		'bScrollInfinite': true,\n")
      .append("		'bScrollCollapse': true,\n")
      .append("		'bFilter': false,\n")
      .append("		'sScrollY': '200px',\n")
      .append("		'bProcessing': true,\n")
      .append("		'bServerSide': true,\n")
      .append("		'iDisplayLength': 20,\n")
      .append("		'iDeferLoading': ").append(rows).append(",\n")
      .append("		'sAjaxSource': '").append(ftE.getMapperUrl()).append("',\n")
      .append("		'fnRowCallback': function( nRow, aData, iDisplayIndex ) {\n")
      .append("			jQuery(nRow).draggable({ \n")
      .append("				containment: '#b_main',\n")
      .append("				zIndex: 10000,\n")
      .append("				cursorAt: {left: 0, top: 0},\n")
      .append("				accept: function(event,ui){ return true; },\n")
      .append("				helper: function(event) {\n")
      .append("				  return jQuery(\"<div class='ui-widget-header'>I'm a custom helper</div>\").appendTo('body').css('zIndex',5).show();\n")
      .append("				}\n")
      .append("			});\n")
      .append("		},\n")
      .append("		'aoColumns': [\n");
		if(ftE.isMultiSelect()) {
			target.append("			{'mData':'multiSelectCol', bSortable: false },\n");
		}
		for(int i=0; i<columnsModel.getColumnCount(); i++) {
			FlexiColumnModel col = columnsModel.getColumnModel(i);
			target.append("			{'mData':'").append(col.getColumnKey())
			  .append("', bSortable: ").append(col.isSortable()).append(" },\n");
		}
    target.append("		]\n")
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
      .append("    o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt,'rSelect',rowId);")
      .append("   };")
      .append("	});\n")
      .append("});\n")
		  .append("</script>\n");
	}
}