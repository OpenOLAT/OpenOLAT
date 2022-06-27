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


import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Render the table as a long HTML table
 * @author Christian Guretzki
 */
class FlexiTableClassicRenderer extends AbstractFlexiTableRenderer {
	
	@Override
	protected void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		FlexiTableColumnModel columnModel = dataModel.getTableColumnModel();
		      
		target.append("<thead><tr>");

		// 1) Special case: table has selection boxes
		if(ftE.getSelectionMode() != SelectionMode.disabled) {
			target.append("<th class='o_multiselect o_table_checkall o_col_sticky_left'>");
			// 1a) Select all feature enabled
			if(ftE.isSelectAllEnable()) {
				renderSelectAll(target, ftE, translator);
			} else if(ftE.getSelectionMode() == SelectionMode.multi) {
				// 1b) Select all feature disabled
				target.append("<div title=\"").append(translator.translate("form.checksinge")).append("\">")
					.append("<i class='o_icon o_icon-lg o_icon_check_disabled text-muted' aria-hidden='true'> </i></div>");
			} else {
				// single selection, no icons
				target.append(" ");
			}
			target.append("</th>");			
			
		}
		
		// 2) Collapsible details
		if (ftE.hasDetailsRenderer()) {
			target.append("<th>");
			target.append("<div title=\"").append(translator.translate("form.details")).append("\">");
			target.append("<i class='o_icon o_icon-lg o_icon_details_expand' aria-hidden='true'> </i>");
			target.append("</div>");
			target.append("</th>");
		}
		
		// 3) Regular columns
		int cols = columnModel.getColumnCount();
		for(int i=0; i<cols; i++) {
			FlexiColumnModel fcm = columnModel.getColumnModel(i);
			if(ftE.isColumnModelVisible(fcm)) {
				renderHeader(target, ftC, fcm, translator);
			}
		}
		
		target.append("</tr></thead>");
	}
	
	private void renderSelectAll(StringOutput target, FlexiTableElementImpl ftE, Translator translator) {
		String dispatchId = ftE.getFormDispatchId();			
		String formName = ftE.getRootForm().getFormName();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		
		// Concept: there are three states: 
		// - all selected, 
		// - mixed (partly selected)
		// - all deselected
		// The icons to trigger the actions are always in DOM. Only currently 
		// correct action is visible. When selecting somethign the backend 
		// will be notified immediately. The backend then sends a JS command
		// to update the visibility of the select-all buttons to prevent loading
		// of the entire page on each selection. 
		int numOfRows = dataModel.getRowCount();
		int numOfChecked = ftE.getMultiSelectedIndex().size();
		// Everything is checked - uncheck all
		target.append("<a id='").append(dispatchId).append("_dsa' href=\"javascript:o_table_toggleCheck('")
			.append(formName).append("', false);")
			.append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
					new NameValuePair("select", "uncheckall")))
			.append("\" title=\"").append(translator.translate("form.uncheckall")).append("\"")
			.append(" style='display:none'", (numOfChecked < numOfRows || numOfChecked == 0))
			.append("><i class='o_icon o_icon-lg o_icon_check_on' aria-hidden='true'> </i></a>");

		// Some are checked (mixed) - uncheck all
		target.append("<a id='").append(dispatchId).append("_dsm' href=\"javascript:o_table_toggleCheck('")
			.append(formName).append("', false);")
			.append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
					new NameValuePair("select", "uncheckall")))
			.append("\" title=\"").append(translator.translate("form.uncheckall")).append("\"")
			.append(" style='display:none'", (numOfChecked == numOfRows || numOfChecked == 0))
			.append("><i class='o_icon o_icon-lg o_icon_check_mixed' aria-hidden='true'> </i></a>");

		// Nothing is checked - check all
		if (ftE.getPageSize() == -1 || numOfRows <= ftE.getPageSize()) {
			// Nothing is checked - check all
			target.append("<a id='").append(dispatchId).append("_sa' href=\"javascript:o_table_toggleCheck('")
				.append(formName).append("', true);")
				.append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
						new NameValuePair("select", "checkall")))
				.append("\" title=\"")
				.append(translator.translate("form.checkall.numbered",
						new String[] { Integer.toString(numOfRows) })).append("\"")
				.append(" style='display:none'", numOfChecked > 0)
				.append("><i class='o_icon o_icon-lg o_icon_check_off' aria-hidden='true'> </i></a>");
								
		} else {					
			// Show menu to opt for all or just current page check
			target.append("<div id='").append(dispatchId).append("_sm' style='position: relative; ")
				.append("display:none;", numOfChecked > 0)
				.append("'><a class='dropdown-toggle' data-toggle='dropdown' href='#' ")
				.append(" title=\"").append(translator.translate("form.checkall")).append("\"")
				.append("><i class='o_icon o_icon-lg o_icon_check_off' aria-hidden='true'> </i></a>")					
				.append("<ul class='dropdown-menu dropdown-menu-left'>")
				// page
				.append("<li><a id='").append(dispatchId).append("_sp' href=\"javascript:o_table_toggleCheck('")
				.append(formName).append("', true);")
				.append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
						new NameValuePair("select", "checkpage")))
				.append("\"><i class='o_icon o_icon-lg o_icon-fw o_icon_check_mixed' aria-hidden='true'> </i> <span>")
				.append(translator.translate("form.checkpage"))
				.append("</span></a></li>")
				// all 
				.append("<li><a id='").append(dispatchId).append("_sa' href=\"javascript:o_table_toggleCheck('")
				.append(formName).append("', true);")
				.append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
						new NameValuePair("select", "checkall")))
				.append("\"><i class='o_icon o_icon-lg o_icon-fw o_icon_check_on' aria-hidden='true'> </i> <span>")
				.append(translator.translate("form.checkall.numbered",
						new String[] { Integer.toString(numOfRows) }))
				.append("</span></a></li>")
				.append("</ul></div>");
		}
		
	}
	
	private void renderHeader(StringOutput sb, FlexiTableComponent ftC, FlexiColumnModel fcm, Translator translator) {
		String header = getHeader(fcm, translator);
		sb.append("<th scope='col'");
		if(StringHelper.containsNonWhitespace(fcm.getHeaderTooltip())) {
			String title =  fcm.getHeaderTooltip();
			sb.append(" title=\"").appendHtmlEscaped(title).append("\"");
		} 
		if (fcm.getSortKey() != null || fcm.getHeaderAlignment() != null || (fcm.getColumnCssClass() != null)) {
			sb.append(" class='");
			// append sort key to make column width set via css
			if (fcm.getSortKey() != null) {
				sb.append(" o_col_").append(fcm.getSortKey());	
			}
			if (fcm.getHeaderAlignment() != null) {
				String alignmentCssClass = getAlignmentCssClass(fcm.getHeaderAlignment());
				sb.append(" ").append(alignmentCssClass);
			}
			if (fcm.getColumnCssClass() != null) {
				String colCssClass = fcm.getColumnCssClass();
				sb.append(" ").append(colCssClass);
			}
			sb.append("'");
		}
		sb.append(">");
		// sort is not defined
		if (!fcm.isSortable() || fcm.getSortKey() == null) {
			sb.append(header);	
		} else {
			FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
			
			Boolean asc = null;
			String sortKey = fcm.getSortKey();
			SortKey[] orderBy = ftE.getOrderBy();
			if(orderBy != null && orderBy.length > 0) {
				for(int i=orderBy.length; i-->0; ) {
					if(sortKey.equals(orderBy[i].getKey())) {
						asc = Boolean.valueOf(orderBy[i].isAsc());
					}
				}
			}

			Form theForm = ftE.getRootForm();
			if(asc == null) {
				sb.append("<a class='o_orderby' href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, true, true, true,
						  new NameValuePair("sort", sortKey), new NameValuePair("asc", "asc")))
				  .append("\">");
			} else if(asc.booleanValue()) {
				sb.append("<a class='o_orderby o_orderby_asc' href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, true, true, true,
						  new NameValuePair("sort", sortKey), new NameValuePair("asc", "desc")))
				  .append("\">");
			} else {
				sb.append("<a class='o_orderby o_orderby_desc' href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, true, true, true,
						  new NameValuePair("sort", sortKey), new NameValuePair("asc", "asc")))
				  .append("\">");
			}
			sb.append(header).append("</a>");
		}
		sb.append("</th>");
	}
	
	private String getHeader(FlexiColumnModel fcm, Translator translator) {
		String header;
		if(StringHelper.containsNonWhitespace(fcm.getIconHeader())) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("<i class=\"").append(fcm.getIconHeader()).append("\"");
			
			String title = null;
			if(StringHelper.containsNonWhitespace(fcm.getHeaderLabel())) {
				title = fcm.getHeaderLabel();
			} else {
				title = translator.translate(fcm.getHeaderKey());
			}
			if(StringHelper.containsNonWhitespace(title)) {
				sb.append(" title=\"").append(title).append("\"");
			}
			
			sb.append("> </i>");
			header = sb.toString();
		} else if(StringHelper.containsNonWhitespace(fcm.getHeaderLabel())) {
			header = fcm.getHeaderLabel();
		} else {
			header = translator.translate(fcm.getHeaderKey());
		}
		return header;
	}
	
	@Override
	protected void renderRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableColumnModel columnsModel = ftE.getTableDataModel().getTableColumnModel();
		int numOfCols = columnsModel.getColumnCount();
		Form theForm = ftE.getRootForm();
		
		// use alternating css class
		int numOfColumns = 0;
		target.append("<tr id='").append(rowIdPrefix).append(row).append("' class='");
		if(ftE.getCssDelegate() != null) {
			String cssClass = ftE.getCssDelegate().getRowCssClass(FlexiTableRendererType.classic, row);
			if(StringHelper.containsNonWhitespace(cssClass)) {
				target.append(cssClass);
			}
		}
		if(ftE.isMultiSelectedIndex(row)) {
			target.append(" o_row_selected");				
		}
		target.append("'");

		SelectionMode selectionMode = ftE.getSelectionMode();
		if(selectionMode == SelectionMode.multi && ftE.isRowSelectionEnabled()) {
			target.append(" onclick=\"o_ffTableToggleRowListener('").append(rowIdPrefix).append(row).append("','o_row_selected');")
		          .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, false, false, false,
		    		  new NameValuePair("chkbox", Integer.toString(row))))
			     .append(";\"");	
		}
		target.append(">");
			
		if(selectionMode != null && selectionMode != SelectionMode.disabled) {
			final String selectionType;
			if(selectionMode == SelectionMode.single) {
				selectionType = "radio";
			} else {
				selectionType = "checkbox";
			}
			target.append("<td class='").append("o_singleselect", "o_multiselect", selectionMode == SelectionMode.single).append(" o_col_sticky_left'>")
			      .append("<input type='").append(selectionType).append("' name='tb_ms' value='").append(rowIdPrefix).append(row).append("'")
			      .append(" onclick=\"o_ffTableToggleRowCheck('").append(rowIdPrefix).append(row).append("','o_row_selected');")
			      .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, false, false, false,
			    		  new NameValuePair("chkbox", Integer.toString(row))))
				  .append(";\"");	 
			if(ftE.isMultiSelectedIndex(row)) {
				target.append(" checked='checked'");
			}
			boolean selectable = ftE.getTableDataModel().isSelectable(row);
			if(!selectable) {
				target.append(" disabled='disabled'");
			}
			target.append("/></td>");
		}
		
		if(ftE.hasDetailsRenderer()) {
			
			target.append("<td>");

			Object rowObject = ftE.getTableDataModel().getObject(row);
			if (ftE.getComponentDelegate() != null && ftE.getComponentDelegate().isDetailsRow(row, rowObject)) {
				String collapseIcon = ftE.isDetailsExpended(row)? "o_icon_details_collaps": "o_icon_details_expand";
				target.append("<div title=\"").append(translator.translate("form.details")).append("\">");
				target.append("<a href='javascript:;' onclick=\"");
				target.append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, false, false, false,
						new NameValuePair("tt-details", Integer.toString(row))));
				target.append(";");
				target.append(" return false;\">");
				target.append("<i class='o_icon o_icon-lg ").append(collapseIcon).append("'> </i>");
				target.append("</a>");
			}
			
			target.append("</td>");
		}
				
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel fcm = columnsModel.getColumnModel(j);
			if(ftE.isColumnModelVisible(fcm)) {
				renderCell(renderer, target, ftC, fcm, row, ubu, translator, renderResult);
				numOfColumns++;
			}
		}
		target.append("</tr>");
		if(ftE.isDetailsExpended(row)) {
			target.append("<tr id='").append(rowIdPrefix).append(row)
			  .append("_details' class='o_table_row_details'>");
			
			VelocityContainer container = ftE.getDetailsRenderer();
			Object rowObject = ftE.getTableDataModel().getObject(row);
			container.contextPut("row", rowObject);

			FlexiTableComponentDelegate cmpDelegate = ftE.getComponentDelegate();
			if(cmpDelegate != null) {
				Iterable<Component> cmps = cmpDelegate.getComponents(row, rowObject);
				if(cmps != null) {
					for(Component cmp:cmps) {
						container.put(cmp.getComponentName(), cmp);
					}
				}
			}
			
			if(ftE.getSelectionMode() != SelectionMode.disabled) {
				target.append("<td> </td>");
			}
			target.append("<td colspan='").append(numOfColumns + 1).append("'>");

			container.getHTMLRendererSingleton().render(renderer, target, container, ubu, translator, renderResult, null);
			container.contextRemove("row");
			container.setDirty(false);
			
			target.append("</td></tr>");
		}
	}

	private void renderCell(Renderer renderer, StringOutput target, FlexiTableComponent ftC, FlexiColumnModel fcm,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();

		int alignment = fcm.getAlignment();
		String cssClass = getAlignmentCssClass(alignment);

		target.append("<td class=\"").append(cssClass).append(" ")
		  .append("o_dnd_label", ftE.getColumnIndexForDragAndDropLabel() == fcm.getColumnIndex());
		if (fcm.getColumnCssClass() != null) {
			String colCss = fcm.getColumnCssClass();
			target.append(" ").append(colCss);
		}
		target.append("\">");
		
		int columnIndex = fcm.getColumnIndex();
		Object cellValue = columnIndex >= 0 ? 
				dataModel.getValueAt(row, columnIndex) : null;
		if (cellValue instanceof FormItem) {
			FormItem formItem = (FormItem)cellValue;
			renderFormItem(renderer, target, ubu, translator, renderResult, ftE, formItem);
		} else if(cellValue instanceof Component) {
			Component cmp = (Component)cellValue;
			cmp.setTranslator(translator);
			if(cmp.isVisible()) {
				cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubu, translator, renderResult, null);
				cmp.setDirty(false);
			}
		} else if (cellValue instanceof FormItemCollection) {
			FormItemCollection collection = (FormItemCollection)cellValue;
			for (FormItem formItem : collection.getFormItems()) {
				renderFormItem(renderer, target, ubu, translator, renderResult, ftE, formItem);
			}
		} else {
			fcm.getCellRenderer().render(renderer, target, cellValue, row, ftC, ubu, translator);
		}
		target.append("</td>");
	}

	private void renderFormItem(Renderer renderer, StringOutput target, URLBuilder ubu, Translator translator,
			RenderResult renderResult, FlexiTableElementImpl ftE, FormItem formItem) {
		formItem.setTranslator(translator);
		if(ftE.getRootForm() != formItem.getRootForm()) {
			formItem.setRootForm(ftE.getRootForm());
		}
		ftE.addFormItem(formItem);
		if(formItem.isVisible()) {
			Component cmp = formItem.getComponent();
			cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubu, translator, renderResult, null);
			cmp.setDirty(false);
		} else if(formItem.getComponent() != null) {
			formItem.getComponent().setDirty(false);
		}
		if(formItem.hasError()) {
			Component errorCmp = formItem.getErrorC();
			errorCmp.getHTMLRendererSingleton().render(renderer, target, formItem.getErrorC(),
					ubu, translator, renderResult, null);
			errorCmp.setDirty(false);
		} 
	}

	@Override
	protected void renderFooter(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		
		boolean hasSelectAll = false;
		boolean selection = ftE.getSelectionMode() != SelectionMode.disabled;
		FlexiTableColumnModel columnsModel = ftE.getTableDataModel().getTableColumnModel();
		int numOfCols = columnsModel.getColumnCount();
		for(int i=numOfCols; i-->0; ) {
			if(columnsModel.getColumnModel(i).isSelectAll()) {
				hasSelectAll = true;
			}
		}
		
		if(hasSelectAll) {
			String dispatchId = ftE.getFormDispatchId();
			target.append("<tr id='all_").append(ftC.getFormDispatchId()).append("' class=''>");		
			if(selection) {
				target.append("<td> </td>");
			}
			for (int j = 0; j<numOfCols; j++) {
				FlexiColumnModel fcm = columnsModel.getColumnModel(j);
				if(fcm.isSelectAll()) {
					target.append("<td><a id='")
					      .append(dispatchId).append("_csa' href=\"javascript:;\" onclick=\"")
					      .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, false,
							  new NameValuePair("cc-selectall", fcm.getColumnIndex())))
					      .append("\"><i class='o_icon o_icon_check_on'> </i> <span>").append(translator.translate("form.select.all"))
					      .append("</span></a><br><a id='")
					      .append(dispatchId).append("_cdsa' href=\"javascript:;\" onclick=\"")
					      .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, false,
							  new NameValuePair("cc-deselectall", fcm.getColumnIndex())))
					      .append("\"><i class='o_icon o_icon_check_off'> </i> <span>").append(translator.translate("form.uncheckall"))
					      .append("</span></a></td>");
				} else {
					target.append("<td> </td>");
				}
			}
			target.append("</tr>");
		}

		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		if(dataModel instanceof FlexiTableFooterModel) {
			FlexiTableFooterModel footerDataModel = (FlexiTableFooterModel)dataModel;
			target.append("<tr id='footer_").append(ftC.getFormDispatchId()).append("' class='o_table_footer'>");		
			if(selection) {
				target.append("<td> </td>");
			}
			
			boolean footerHeader = false;
					
			for (int j = 0; j<numOfCols; j++) {
				FlexiColumnModel fcm = columnsModel.getColumnModel(j);
				if(ftE.isColumnModelVisible(fcm)) {
					int alignment = fcm.getAlignment();
					int columnIndex = fcm.getColumnIndex();
					Object cellValue = columnIndex >= 0 ? footerDataModel.getFooterValueAt(columnIndex) : null;
					if(cellValue == null && !footerHeader) {
						String header = footerDataModel.getFooterHeader();
						target.append("<th scope='row'>");
						if(header != null) {
							target.append(header);
						}
						target.append("</th>");
						footerHeader = true;
					} else {
						String cssClass = getAlignmentCssClass(alignment);
						target.append("<td class=\"").append(cssClass).append("\">");
						fcm.getFooterCellRenderer().render(renderer, target, cellValue, 0, ftC, ubu, translator);
						target.append("</td>");
					}
				}
			}
			target.append("</tr>");
		}
	}

	private String getAlignmentCssClass(int alignment) {
		switch (alignment) {
		case FlexiColumnModel.ALIGNMENT_LEFT:
			return "text-left";
		case FlexiColumnModel.ALIGNMENT_RIGHT:
			return "text-right";
		case FlexiColumnModel.ALIGNMENT_CENTER:
			return "text-center";
		case FlexiColumnModel.ALIGNMENT_ICON:
			return "o_table_header_fw";
		default:
			return "";
		}
	}
}