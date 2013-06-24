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
import org.olat.core.util.StringHelper;

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
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnsModel = dataModel.getTableColumnModel();
		
		Form rootForm = ftE.getRootForm();
		String id = ftC.getFormDispatchId();
		int loadedRows = dataModel.getRowCount();

		int selectPos = -1;
		Object selectedObject = ftE.getSelectedObj();
		if(selectedObject != null) {
			for(int i=0; i<dataModel.getRowCount(); i++) {
				if(dataModel.isRowLoaded(i) && selectedObject.equals(dataModel.getObject(i))) {
					selectPos = i;
					break;
				}
			}
		}
		
		String scrollHeight;
		String wrapperSelector = ftE.getWrapperSelector();
		if(StringHelper.containsNonWhitespace(wrapperSelector)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Math.max((jQuery('").append(wrapperSelector).append("').height() - 130),100) + 'px'");
			scrollHeight = sb.toString();
		} else {
			scrollHeight = "'100px'";
		}

		target.append("<script type='text/javascript'>")
		  .append("/* <![CDATA[ */ \n")
		  .append("jQuery(function() {\n")
		  .append(" var scrollHeight = ").append(scrollHeight).append(";\n")
		  .append(" var selectedIndex =").append(selectPos).append(";\n")
      .append("	jQuery('#").append(id).append("').dataTable( {\n")
      .append("		'bScrollInfinite': true,\n")
      .append("		'bScrollCollapse': true,\n")
      .append("		'bFilter': false,\n")
      .append("		'sScrollY': ").append(scrollHeight).append(",\n")
      .append("		'bProcessing': true,\n")
      .append("		'bServerSide': true,\n")
      .append("		'iDisplayLength': ").append(ftE.getPageSize()).append(",\n")
      .append("		'iDeferLoading': ").append(loadedRows).append(",\n")
      .append("		'sAjaxSource': '").append(ftE.getMapperUrl()).append("',\n")
      .append("		'oLanguage': {\n")
      .append("		  'sInfo': '").append(translator.translate("table.sInfo")).append("',\n")
      .append("		  'sEmptyTable': '").append(translator.translate("table.sEmptyTable")).append("'\n")
      .append("    },\n")
      .append("   'asStripeClasses': ['','b_table_odd'],\n")
      .append("		'aoColumns': [\n");
		int count = 0;
		if(ftE.isMultiSelect()) {
			target.append("			{'mData':'multiSelectCol', bSortable: false }\n");
			count++;
		}
		int colDnd = 0;
		for(int i=0; i<columnsModel.getColumnCount(); i++) {
			FlexiColumnModel col = columnsModel.getColumnModel(i);
			if(ftE.isColumnModelVisible(col)) {
				if(count > 0) target.append(",");
				count++;
				target.append("			{'mData':'").append(col.getColumnKey())
			  	.append("', bSortable: ").append(col.isSortable()).append(" }\n");
			}
			if(col.getColumnIndex() == ftE.getColumnLabelForDragAndDrop()) {
				colDnd = count;
			}
		}
    target.append("		],\n")
      .append("		'fnRowCallback': function( nRow, aData, iDisplayIndex, iDisplayIndexFull) {\n")
      .append("     if(selectedIndex == iDisplayIndexFull) {\n")
      .append("       jQuery(nRow).addClass('b_row_selected');\n")
      .append("     }\n")
      .append("			jQuery(nRow).draggable({ \n")
      .append("				containment: '#b_main',\n")
      .append("				zIndex: 10000,\n")
      .append("				cursorAt: {left: 0, top: 0},\n")
      .append("				accept: function(event,ui){ return true; },\n")
      .append("				helper: function(event,ui,zt) {\n")
      .append("				  var helperText = jQuery(this).children(\"td:nth-child(").append(colDnd + 1).append(")\").text();\n")
      .append("				  return jQuery(\"<div class='ui-widget-header b_table_drag'>\" + helperText + \"</div>\").appendTo('body').css('zIndex',5).show();\n")
      .append("				}\n")
      .append("			});\n")
      .append("		},\n")
      .append("	  fnInitComplete: function (oSettings, json) {\n")
      .append("     if(selectedIndex < 0) return;\n")
      .append("     var scrollTo = 0;\n")
      .append("     jQuery('#").append(id).append(" tbody tr').each(function(index, el) { ")
      .append("	    	if(index < selectedIndex) {\n")
      .append("         scrollTo += jQuery(el).outerHeight();\n")
      .append("       }\n")
      .append("	    });\n")
      .append("     jQuery('#").append(id).append("').parent().scrollTop(scrollTo - 40);\n")
      .append("	  }\n")
      .append("	});\n")
      //clic rows
      .append("	jQuery('#").append(id).append(" tbody').click(function(event, ui) {\n")
      .append("   var link = false;\n")
      .append("   var rowId = null;\n")
      .append("   if(event.target.tagName == 'A' || event.target.tagName == 'INPUT') {\n")
      .append("     return;\n")
      .append("   }\n")
      .append("   jQuery(event.target).parents().each(function(index,el) {\n")
      .append("     if(el.tagName == 'A' || el.tagName == 'INPUT') {\n")
      .append("       link = true;\n")
      .append("     } else if (el.tagName == 'TR' && rowId == null) {\n")
      .append("       jQuery('#").append(id).append(" tbody tr').each(function(index, trEl) {\n")
      .append("         jQuery(trEl).removeClass('b_row_selected');\n")
      .append("       });\n")
      .append("       jQuery(el).addClass('b_row_selected');\n")
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
      .append("/* ]]> */\n")
		  .append("</script>\n");
	}
}