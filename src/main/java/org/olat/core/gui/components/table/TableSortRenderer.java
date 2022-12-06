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
package org.olat.core.gui.components.table;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 10.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class TableSortRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		TableSort sorter = (TableSort)source;
		Table table = sorter.getTable();
		String id = sorter.getDispatchID();
		String formName = "tb_ms_" + table.hashCode();
		
		sb.append("<div id='o_c").append(id).append("' class='btn-group'>")
		  .append("<button id='table-button-sorters-").append(id).append("' type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown'>")
		  .append("<i class='o_icon o_icon-lg o_icon_sort_menu'> </i>")
		  .append("<b class='caret'></b>")
		  .append("</button>")
		  .append("<div id='table-sorters-").append(id).append("' class='hide'><ul class='o_dropdown list-unstyled' role='menu'>");

		int cols = table.getColumnCount();
		boolean asc = table.isSortAscending();
		ColumnDescriptor sortedCD = table.getCurrentlySortedColumnDescriptor();
		
		for (int i = 0; i < cols; i++) {
			ColumnDescriptor cd = table.getColumnDescriptor(i);
			// header either a link or not
			if (cd.isSortingAllowed()) {
				sb.append("<li><a  href=\"javascript:;\" onclick=\"o_XHRSubmit('")
	              .append(formName).append("','").append(Table.FORM_CMD).append("','").append(Table.COMMAND_SORTBYCOLUMN)
	              .append("','").append(Table.FORM_PARAM).append("','").append(i).append("'); return false;\"")
				  .append(" title=\"")
				  .appendHtmlEscaped(translator.translate("row.sort")).append("\">");
				
				if(sortedCD == cd) {
					if(asc) {
						sb.append("<i class='o_icon o_icon_sort_asc'></i> ");
					} else {
						sb.append("<i class='o_icon o_icon_sort_desc'></i> ");
					}
				}
				
				String header;
				if (cd.translateHeaderKey()) {
					header = translator.translate(cd.getHeaderKey());
				} else {
					header = cd.getHeaderKey();
				}
				sb.append(header)
				  .append("</a></li>");
			}
		}

		sb.append("</ul></div></div>")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */\n")
		  .append("jQuery(function() { o_popover('table-button-sorters-").append(id).append("','table-sorters-").append(id).append("'); });\n")
		  .append("/* ]]> */\n")
		  .append("</script>");
	}
}
