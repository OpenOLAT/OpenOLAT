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

		if(ftE.isMultiSelect()) {
			// render as checkbox icon to minimize used space for header
			String choice = translator.translate("table.header.choice");
			target.append("<th class='o_multiselect'><i class='o_icon o_icon_checkbox_checked o_icon-lg' title=\"").append(choice).append("\"> </i></th>");
		}
		
		int cols = columnModel.getColumnCount();
		for(int i=0; i<cols; i++) {
			FlexiColumnModel fcm = columnModel.getColumnModel(i);
			if(ftE.isColumnModelVisible(fcm)) {
				renderHeader(target, ftC, fcm, translator);
			}
		}
		
		target.append("</tr></thead>");
	}
	
	private void renderHeader(StringOutput sb, FlexiTableComponent ftC, FlexiColumnModel fcm, Translator translator) {
		String header = getHeader(fcm, translator);
		sb.append("<th scope='col'");
		if (fcm.getSortKey() != null || fcm.getHeaderAlignment() != null) {
			sb.append(" class='");
			// append sort key to make column width set via css
			if (fcm.getSortKey() != null) {
				sb.append(" o_col_").append(fcm.getSortKey());	
			}
			if (fcm.getHeaderAlignment() != null) {
				String alignmentCssClass = getAlignmentCssClass(fcm.getHeaderAlignment());
				sb.append(" ").append(alignmentCssClass);
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
		target.append("<tr id='").append(rowIdPrefix).append(row).append("'");
		if(ftE.getCssDelegate() != null) {
			String cssClass = ftE.getCssDelegate().getRowCssClass(FlexiTableRendererType.classic, row);
			if(StringHelper.containsNonWhitespace(cssClass)) {
				target.append(" class='").append(cssClass).append("'");
			}
		}
		target.append(">");
				
		if(ftE.isMultiSelect()) {
			target.append("<td class='o_multiselect'>")
			      .append("<input type='checkbox' name='tb_ms' value='").append(rowIdPrefix).append(row).append("'")
			      .append(" onclick=\"javascript:")
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
			
			if(ftE.isMultiSelect()) {
				target.append("<td></td>");
			}
			target.append("<td colspan='").append(numOfColumns).append("'>");

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
		  .append("o_dnd_label", ftE.getColumnIndexForDragAndDropLabel() == fcm.getColumnIndex())
		  .append("\">");
		
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
			if(ftE.isMultiSelect()) {
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
		}

		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		if(dataModel instanceof FlexiTableFooterModel) {
			FlexiTableFooterModel footerDataModel = (FlexiTableFooterModel)dataModel;
			target.append("<tr id='footer_").append(ftC.getFormDispatchId()).append("' class='o_table_footer'>");		
			if(ftE.isMultiSelect()) {
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