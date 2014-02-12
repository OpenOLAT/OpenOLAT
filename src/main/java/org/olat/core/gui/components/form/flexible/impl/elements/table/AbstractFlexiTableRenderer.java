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
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 01.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractFlexiTableRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();

		String id = ftC.getFormDispatchId();
		sb.append("<div class=\"b_table_wrapper b_flexitable_wrapper b_floatscrollbox\">");
		renderHeaderButtons(renderer, sb, ftE, ubu, translator, renderResult, args);
		sb.append("<table id=\"").append(id).append("\">");
		
		//render headers
		renderHeaders(sb, ftC, translator);
		//render body
		sb.append("<tbody>");
		renderBody(renderer, sb, ftC, ubu, translator, renderResult);
		sb.append("</tbody></table>");
		
		renderFooterButtons(renderer, sb, ftC, ubu, translator, renderResult, args);
		sb.append("</div>");
		
		//source
		if (source.isEnabled()) {
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(id));
			sb.append(FormJSHelper.getSetFlexiFormDirty(ftE.getRootForm(), id));
			sb.append(FormJSHelper.getJSEnd());
		}
	}
	
	protected void renderHeaderButtons(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if(ftE.isFilterEnabled()) {
			sb.append("<div class='b_table_filter'>");
			SingleSelection filterEl = ftE.getFilterSelection();
			String text = filterEl.getLabelText();
			if(text != null && text.length() < 128) {
				sb.append("<label>").append(text).append("</label>");
			}
			renderFormItem(renderer, sb, filterEl, ubu, translator, renderResult, args);
			sb.append("</div>");
		}
		if(ftE.isSearchEnabled()) {
			renderFormItem(renderer, sb, ftE.getSearchElement(), ubu, translator, renderResult, args);
			renderFormItem(renderer, sb, ftE.getSearchButton(), ubu, translator, renderResult, args);
		}
		if(ftE.getExtendedSearchButton() != null) {
			renderFormItem(renderer, sb, ftE.getExtendedSearchButton(), ubu, translator, renderResult, args);
		}
		
		if(ftE.getExportButton() != null && ftE.isExportEnabled()) {
			renderFormItem(renderer, sb, ftE.getExportButton(), ubu, translator, renderResult, args);
		}
		if(ftE.getCustomButton() != null && ftE.isCustomizeColumns()) {
			renderFormItem(renderer, sb, ftE.getCustomButton(), ubu, translator, renderResult, args);
		}
	}
	
	protected void renderFormItem(Renderer renderer, StringOutput sb, FormItem item, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if(item != null) {
			Component cmp = item.getComponent();
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
		}
	}
	
	protected void renderFooterButtons(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		if(ftE.isSelectAllEnable()) {
			String formName = ftE.getRootForm().getFormName();
			String dispatchId = ftE.getFormDispatchId();

			sb.append("<div class='b_table_footer'>");

			sb.append("<input type=\"checkbox\" checked=\"checked\" disabled=\"disabled\" />&nbsp;<a id=\"")
			  .append(dispatchId).append("\" href=\"javascript:b_table_toggleCheck('").append(formName).append("', true);")
			  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, new NameValuePair("select", "checkall")))
			  .append("\"><span>").append(translator.translate("form.checkall")).append("</span></a>");

			sb.append("&nbsp;<input type=\"checkbox\" disabled=\"disabled\" />&nbsp;<a id=\"")
			  .append(dispatchId).append("\" href=\"javascript:b_table_toggleCheck('").append(formName).append("', false);")
			  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, new NameValuePair("select", "uncheckall")))
			  .append("\"><span>").append(translator.translate("form.uncheckall")).append("</span></a>");
			
			sb.append("</div>");
		}
		
		if(ftE.getRendererType() != FlexiTableRendererType.dataTables && ftE.getPageSize() > 0) {
			renderPagesLinks(sb, ftC);
		}
	}
	
	protected void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnModel = dataModel.getTableColumnModel();
		      
		target.append("<thead><tr>");

		int col = 0;
		if(ftE.isMultiSelect()) {
			String choice = translator.translate("table.header.choice");
			target.append("<th class='b_first_child'>").append(choice).append("</th>");
			col++;
		}
		
		int cols = columnModel.getColumnCount();
		for(int i=0; i<cols; i++) {
			FlexiColumnModel fcm = columnModel.getColumnModel(i);
			if(ftE.isColumnModelVisible(fcm)) {
				renderHeader(target, ftC, fcm, col++, cols, translator);
			}
		}
		
		target.append("</tr></thead>");
	}
	
	protected void renderHeader(StringOutput target, FlexiTableComponent ftC, FlexiColumnModel fcm, int colPos, int numOfCols, Translator translator) {
		String header;
		if(StringHelper.containsNonWhitespace(fcm.getHeaderLabel())) {
			header = fcm.getHeaderLabel();
		} else {
			header = translator.translate(fcm.getHeaderKey());
		}
		target.append("<th class=\"");
		// add css class for first and last column to support older browsers
		if (colPos == 0) target.append(" b_first_child");
		if (colPos == numOfCols-1) target.append(" b_last_child");
		target.append("\">").append(header);
		renderHeaderSort(target, ftC, fcm, colPos, translator);
		target.append("</th>");
	}
	
	protected abstract void renderHeaderSort(StringOutput target, FlexiTableComponent ftC, FlexiColumnModel fcm, int colPos, Translator translator);
	
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		
		// the really selected rowid (from the tabledatamodel)
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);

		String rowIdPrefix = "row_" + id + "-";
		for (int i = firstRow; i < lastRow; i++) {
			if(dataModel.isRowLoaded(i)) {
				renderRow(renderer, target, ftC, rowIdPrefix,	i, rows, ubu, translator, renderResult);
			}
		}				
		// end of table table
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
			      .append("<input type='checkbox' name='tb_ms' value='").append(rowIdPrefix).append(row).append("'");
			if(ftE.isAllSelectedIndex() || ftE.isMultiSelectedIndex(row)) {
				target.append(" checked='checked'");
			}   
			target.append("/></td>");
			col++;
		}
				
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel fcm = columnsModel.getColumnModel(j);
			if(ftE.isColumnModelVisible(fcm)) {
				renderCell(renderer, target, ftC, fcm, row, col++, numOfCols, ubu, translator, renderResult);
			}
		}
		target.append("</tr>");
	}

	protected void renderCell(Renderer renderer, StringOutput target, FlexiTableComponent ftC, FlexiColumnModel fcm,
			int row, int col, int numOfCols, URLBuilder ubu, Translator translator, RenderResult renderResult) {

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();

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
	

	private void renderPagesLinks(StringOutput sb, FlexiTableComponent ftC) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		int pageSize = ftE.getPageSize();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		int rows = dataModel.getRowCount();
		
		if(pageSize > 0 && rows > pageSize) {
			sb.append("<div class='b_table_page'>");

			int page = ftE.getPage();
			int maxPage = (int)Math.ceil(((double) rows / (double) pageSize));
	
			renderPageBackLink(sb, ftC, page);
			renderPageNumberLinks(sb, ftC, page, maxPage);
			renderPageNextLink(sb, ftC, page, maxPage);

			sb.append("</div>");
		}
	}
	
	private void renderPageBackLink(StringOutput sb, FlexiTableComponent ftC, int page) {
		if (page <= 0) return;

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<a class='b_table_backward' href=\"javascript:")
		  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("page", Integer.toString(page - 1))))
		  .append("\">").append("&nbsp;").append("</a>");
	}
	
	private void renderPageNextLink(StringOutput sb, FlexiTableComponent ftC, int page, int maxPage) {
		if (page == maxPage) return;

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<a class='b_table_forward' href=\"javascript:")
		  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("page", Integer.toString(page + 1))))
		  .append("\">").append("&nbsp;").append("</a>");
	}
	
	private void renderPageNumberLinks(StringOutput sb, FlexiTableComponent ftC, int page, int maxPage) {
		if (maxPage < 12) {
			for (int i=0; i<maxPage; i++) {
				appendPagenNumberLink(sb, ftC, page, i);
			}
		} else {
			int powerOf10 = String.valueOf(maxPage).length() - 1;
			int maxStepSize = (int) Math.pow(10, powerOf10);
			int stepSize = (int) Math.pow(10, String.valueOf(page).length() - 1);
			boolean isStep = false;
			int useEveryStep = 3;
			int stepCnt = 0;
			boolean isNear = false;
			int nearleft = 5;
			int nearright = 5;
			if (page < nearleft) {
				nearleft = page;
				nearright += (nearright - nearleft);
			} else if (page > (maxPage - nearright)) {
				nearright = maxPage - page;
				nearleft += (nearleft - nearright);
			}
			for (int i = 1; i <= maxPage; i++) {
				// adapt stepsize if needed
				stepSize = adaptStepIfNeeded(page, maxStepSize, stepSize, i);
	
				isStep = ((i % stepSize) == 0);
				if (isStep) {
					stepCnt++;
					isStep = isStep && (stepCnt % useEveryStep == 0);
				}
				isNear = (i > (page - nearleft) && i < (page + nearright));
				if (i == 1 || i == maxPage || isStep || isNear) {
					appendPagenNumberLink(sb, ftC, page, i);
				}
			}
		}
	}
	
	private void appendPagenNumberLink(StringOutput sb, FlexiTableComponent ftC, int page, int i) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		String cssClass = (page == i) ? "b_table_page b_table_page_active" : "b_table_page";
		sb.append("<a class='").append(cssClass).append("' href=\"javascript:")
		  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("page", Integer.toString(i))))
		  .append("\">").append(i+1).append("</a>");
	}

	private int adaptStepIfNeeded(final int page, final int maxStepSize, final int stepSize, final int i) {
		int newStepSize = stepSize;
		if (i < page && stepSize > 1 && ((page - i) / stepSize == 0)) {
			newStepSize = stepSize / 10;
		} else if (i > page && stepSize < maxStepSize && ((i - page) / stepSize == 9)) {
			newStepSize = stepSize * 10;
		}
		return newStepSize;
	}
}
