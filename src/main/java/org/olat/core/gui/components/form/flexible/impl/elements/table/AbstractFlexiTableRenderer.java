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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 01.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractFlexiTableRenderer implements ComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();

		if(ftE.isSearch()) {
			renderSearchFields(renderer, sb, ftE, ubu, translator, renderResult, args);
		}
		
		String id = ftC.getFormDispatchId();
		sb.append("<div class=\"b_table_wrapper b_floatscrollbox\">")
		  .append("<table id=\"").append(id).append("\">");
		
		//render headers
		renderHeaders(sb, ftE, translator);
		//render body
		renderBody(renderer, sb, ftC, ubu, translator, renderResult);
		
		sb.append("</table></div>");
		
		//source
		if (source.isEnabled()) {
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(id));
			sb.append(FormJSHelper.getSetFlexiFormDirty(ftE.getRootForm(), id));
			sb.append(FormJSHelper.getJSEnd());
		}
	}
	
	protected void renderSearchFields(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TextElement searchEl = ftE.getSearchElement();
		if(searchEl != null) {
			Component searchC = searchEl.getComponent();
			searchC.getHTMLRendererSingleton().render(renderer, sb, searchC, ubu, translator, renderResult, args);
		}
		
		FormSubmit searchButton = ftE.getSearchButton();
		if(searchButton != null) {
			Component searchB = searchButton.getComponent();
			searchB.getHTMLRendererSingleton().render(renderer, sb, searchB, ubu, translator, renderResult, args);
		}
	}
	
	protected void renderHeaders(StringOutput target, FlexiTableElementImpl ftE, Translator translator) {
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
			renderHeader(target, fcm, col, cols, translator);
			col++;
  	}
		
		target.append("</tr></thead>");
	}
	
	protected void renderHeader(StringOutput target, FlexiColumnModel fcm, int colPos, int numOfCols, Translator translator) {
		String header = translator.translate(fcm.getHeaderKey());	
		target.append("<th class=\"");
		// add css class for first and last column to support older browsers
		if (colPos == 0) target.append(" b_first_child");
		if (colPos == numOfCols-1) target.append(" b_last_child");
		target.append("\">").append(header).append("</th>");
	}
	
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel dataModel = ftE.getTableDataModel();

		// build rows
		target.append("<tbody>");
		
		// the really selected rowid (from the tabledatamodel)
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);

		String rowIdPrefix = "row_" + id + "-";
		for (int i = firstRow; i < lastRow; i++) {
			renderRow(renderer, target, ftC, rowIdPrefix,	i, rows, ubu, translator, renderResult);
		}				
		// end of table table
		target.append("</tbody>");
	}
	
	protected void renderRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix,
			int row, int rows, URLBuilder ubu, Translator translator, RenderResult renderResult) {

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableColumnModel columnsModel = ftE.getTableDataModel().getTableColumnModel();
		int numOfCols = columnsModel.getColumnCount();
		
		// use alternating css class
		String cssClass;
		if (row % 2 == 0) cssClass = "";
		else cssClass = "b_table_odd";
		// add css class for first and last column to support older browsers
		if (row == 0) cssClass += " b_first_child";
		if (row == rows-1) cssClass += " b_last_child";

		target.append("<tr id='").append(rowIdPrefix).append(row)
				  .append("' class=\"").append(cssClass).append("\">");
				
		int col = 0;
		if(ftE.isMultiSelect()) {
			target.append("<td class='b_first_child'>")
			      .append("<input type='checkbox' name='ftb_ms' value='").append(rowIdPrefix).append(row).append("'");
			if(ftE.isMultiSelectedIndex(row)) {
				target.append(" checked='checked'");
			}   
			target.append("/></td>");
			col++;
		}
				
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel fcm = columnsModel.getColumnModel(j);
			renderCell(renderer, target, ftC, fcm, row, col, numOfCols, ubu, translator, renderResult); 
			col++;
		}
		target.append("</tr>");
	}

	protected void renderCell(Renderer renderer, StringOutput target, FlexiTableComponent ftC, FlexiColumnModel fcm,
			int row, int col, int numOfCols, URLBuilder ubu, Translator translator, RenderResult renderResult) {

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel dataModel = ftE.getTableDataModel();

		int alignment = fcm.getAlignment();
		String cssClass = (alignment == FlexiColumnModel.ALIGNMENT_LEFT ? "b_align_normal" : (alignment == FlexiColumnModel.ALIGNMENT_RIGHT ? "b_align_inverse" : "b_align_center"));
		// add css class for first and last column to support older browsers
		if (col == 0) cssClass += " b_first_child";
		if (col == numOfCols-1) cssClass += " b_last_child";				
		target.append("<td class=\"").append(cssClass).append("\">");
		if (col == 0) target.append("<a name=\"table\"></a>"); //add once for accessabillitykey

		int columnIndex = fcm.getColumnIndex();
		Object cellValue = columnIndex >= 0 ? 
				dataModel.getValueAt(row, columnIndex) : null;
		if (cellValue instanceof FormItem) {
			FormItem formItem = (FormItem)cellValue;
			formItem.setTranslator(translator);
			if(ftE.getRootForm() != formItem.getRootForm()) {
				formItem.setRootForm(ftE.getRootForm());
			}
			ftE.addFormItem(formItem);
			formItem.getComponent().getHTMLRendererSingleton().render(renderer, target, formItem.getComponent(),
					ubu, translator, renderResult, null);
		} else {
			fcm.getCellRenderer().render(target, cellValue, row, ftC, ubu, translator);
		}
		target.append("</td>");
	}


	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderingState rstate) {
		//
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source,
			RenderingState rstate) {
		//
	}
	
	



}
