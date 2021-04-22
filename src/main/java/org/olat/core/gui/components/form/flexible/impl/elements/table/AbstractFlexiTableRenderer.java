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

import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
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
		
		renderHeaderButtons(renderer, sb, ftE, ubu, translator, renderResult, args);
		renderBreadcrumbs(sb, ftE);
		
		if(ftE.getTableDataModel().getRowCount() == 0 && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey())) {
			renderEmptyState(renderer, sb, ubu, translator, renderResult, ftE);			
			
		} else {
			sb.append("<div class='o_table_wrapper o_table_flexi")
			  .append(" o_table_edit", ftE.isEditMode());
			String css = ftE.getElementCssClass();
			if (css != null) {
				sb.append(" ").append(css);
			}
			switch(ftE.getRendererType()) {
				case custom: sb.append(" o_rendertype_custom"); break;
				case classic: sb.append(" o_rendertype_classic"); break;
			}
			sb.append("'");
			String wrapperSelector = ftE.getWrapperSelector();
			if (wrapperSelector != null) {
				sb.append(" id='").append(wrapperSelector).append("'");
			}
			sb.append("><table id=\"").append(id).append("\" class=\"table table-condensed table-striped table-hover")
			  .append(" table-bordered", ftE.isBordered()).append("\">");
			
			//render headers
			renderHeaders(sb, ftC, translator);
			//render footers
			if(hasFooter(ftE)) {
				sb.append("<tfoot>");
				renderFooter(renderer, sb, ftC, ubu, translator, renderResult);
				sb.append("</tfoot>");
			}
			//render body
			sb.append("<tbody>");
			renderBody(renderer, sb, ftC, ubu, translator, renderResult);
			sb.append("</tbody>");
			
			sb.append("</table>");
			renderFooterButtons(sb, ftC, translator);
			renderFooterGroupedButtons(renderer, sb, ftC, ubu, translator, renderResult, args);
			//draggable
			if(ftE.getColumnIndexForDragAndDropLabel() > 0) {
				sb.append("<script>")
				  .append("jQuery(function() {\n")
				  .append(" jQuery('.o_table_flexi table tr').draggable({\n")
		          .append("  containment: '#o_main',\n")
		          .append("  zIndex: 10000,\n")
		          .append("  cursorAt: {left: 0, top: 0},\n")
		          .append("  accept: function(event,ui){ return true; },\n")
		          .append("  helper: function(event,ui,zt) {\n")
		          .append("    var helperText = jQuery(this).children('.o_dnd_label').text();\n")
		          .append("    return jQuery(\"<div class='ui-widget-header o_table_drag'>\" + helperText + \"</div>\").appendTo('body').css('zIndex',5).show();\n")
		          .append("  }\n")
		          .append("});\n")
		          .append("});\n")
				  .append("</script>\n");
			}
			
			sb.append("</div>");
		}
		
		//source
		if (source.isEnabled()) {
			FormJSHelper.appendFlexiFormDirty(sb, ftE.getRootForm(), id);
		}
	}

	protected void renderEmptyState(Renderer renderer, StringOutput sb, URLBuilder ubu, Translator translator,
		RenderResult renderResult, FlexiTableElementImpl ftE) {
		String emptyMessageKey = ftE.getEmtpyTableMessageKey();
		String emptyMessageIconCss = ftE.getEmtpyTableIconCss();
		String emptyMessageHintKey = ftE.getEmptyTableHintKey();
		sb.append("<div class=\"o_empty_state\"");
		
		String wrapperSelector = ftE.getWrapperSelector();
		if (wrapperSelector != null) {
			sb.append(" id=\"").append(wrapperSelector).append("\"");
		}
		sb.append("><div class=\"o_empty_visual\"><i class='o_icon o_icon_empty_indicator'></i><i class='o_icon ").append(emptyMessageIconCss).append("'></i></div>")
			.append("<h3 class=\"o_empty_msg\">").append(translator.translate(emptyMessageKey)).append("</h3>");			
		if (emptyMessageHintKey != null) {
			sb.append("<div class=\"o_empty_hint\">").append(translator.translate(emptyMessageHintKey)).append("</div>");
		}			
		if (ftE.getEmptyTablePrimaryActionButton() != null) {
			sb.append("<div class=\"o_empty_action\">");			
			renderFormItem(renderer, sb, ftE.getEmptyTablePrimaryActionButton(), ubu, translator, renderResult, null);
			sb.append("</div> ");				
		}
		sb.append("</div>");
	}
	
	protected boolean hasFooter(FlexiTableElementImpl ftE) {
		if(ftE.isFooter()) {
			return true;
		}
		
		FlexiTableColumnModel columns = ftE.getTableDataModel().getTableColumnModel();
		for(int i=columns.getColumnCount(); i-->0; ) {
			if(columns.getColumnModel(i).isSelectAll()) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void renderHeaderButtons(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		Component searchCmp = ftE.getExtendedSearchComponent();
		
		if(searchCmp == null && !ftE.isExtendedSearchExpanded() && !ftE.isNumOfRowsEnabled()
				&& !ftE.isFilterEnabled() && !ftE.isSortEnabled() && ! ftE.isExportEnabled()
				&& !ftE.isCustomizeColumns() && ftE.getAvailableRendererTypes().length  <= 1) {
			if(ftE.getCustomButton() != null) {
				ftE.getCustomButton().getComponent().setDirty(false);
			}
			return;
		}
		
		boolean empty = ftE.getTableDataModel().getRowCount() == 0;
		boolean hideSearch = empty && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey()) && !ftE.isFilterEnabled() 
				&& !ftE.isExtendedSearchExpanded() && !StringHelper.containsNonWhitespace(ftE.getQuickSearchString())
				&& !ftE.isShowAlwaysSearchFields();
		
		if(searchCmp != null && ftE.isExtendedSearchExpanded()) {
			renderer.render(searchCmp, sb, args);
		}
		
		sb.append("<div class='row clearfix o_table_toolbar'>")
		  .append("<div class='col-sm-6 col-xs-12 o_table_toolbar_left'>");
		if(!hideSearch && (searchCmp == null || !ftE.isExtendedSearchExpanded())) {
			renderHeaderSearch(renderer, sb, ftE, ubu, translator, renderResult, args);
		}
		sb.append("</div>");

		sb.append("<div class='col-sm-2 col-xs-4 o_table_row_count'>");
		if(!empty && ftE.isNumOfRowsEnabled()) {
			int rowCount = ftE.getTableDataModel().getRowCount();
			if(rowCount == 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entry"));
			} else if(rowCount > 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entries"));
			}
		}
		sb.append("</div><div class='col-sm-4 col-xs-8'><div class='pull-right'><div class='o_table_tools o_noprint'>");
		
		String filterIndication = null;
		//filter
		if(ftE.isFilterEnabled()) {
			List<FlexiTableFilter> filters = ftE.getFilters();
			if(filters != null && !filters.isEmpty()) {
				filterIndication = renderFilterDropdown(sb, ftE, filters, translator);
			}
		}
		
		//sort
		if(!empty && ftE.isSortEnabled()) {
			List<FlexiTableSort> sorts = ftE.getSorts();
			if(sorts != null && !sorts.isEmpty()) {
				renderSortDropdown(sb, ftE, sorts, translator);
			}
		}
		
		if(!empty && ftE.getExportButton() != null && ftE.isExportEnabled()) {
			sb.append("<div class='btn-group'>");
			ftE.getExportButton().setEnabled(!empty);
			renderFormItem(renderer, sb, ftE.getExportButton(), ubu, translator, renderResult, null);
			sb.append("</div> ");
		}
		if(!empty && ftE.getCustomButton() != null && ftE.isCustomizeColumns()
				&& (ftE.getRendererType() == null || ftE.getRendererType() == FlexiTableRendererType.classic)) {
			sb.append("<div class='btn-group'>");
			renderFormItem(renderer, sb, ftE.getCustomButton(), ubu, translator, renderResult, null);
			sb.append("</div> ");
		}
		
		//switch type of tables
		FlexiTableRendererType[] types = ftE.getAvailableRendererTypes();
		if(!empty && types.length > 1) {
			sb.append("<div class='btn-group'>");
			for(FlexiTableRendererType type:types) {
				renderHeaderSwitchType(type, renderer, sb, ftE, ubu, translator, renderResult, null);
			}
			sb.append("</div> ");
		}
		sb.append("</div>");
		if(StringHelper.containsNonWhitespace(filterIndication)) {
			Form theForm = ftE.getRootForm();
			String dispatchId = ftE.getFormDispatchId();
			
			sb.append("<div class='o_table_tools_indications'>").append(filterIndication)
				// remove filter
			  .append(" <a href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
					  new NameValuePair("rm-filter", "true")))
			  .append("\" title=\"").append(translator.translate("remove.filters")).append("\">")
			  .append("<i class='o_icon o_icon_remove o_icon-fw'> </i></a></div>"); 
		}
		sb.append("</div>");
		
		
		sb.append("</div></div>");
	}
	
	protected void renderHeaderSearch(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		if(ftE.isSearchEnabled() || ftE.getExtendedFilterButton() != null) {
			Form theForm = ftE.getRootForm();
			String dispatchId = ftE.getFormDispatchId();
			boolean searchInput = ftE.getSearchElement() != null;
			
			sb.append("<div class='o_table_search input-group o_noprint'>");
			if(searchInput) {
				renderFormItem(renderer, sb, ftE.getSearchElement(), ubu, translator, renderResult, null);
				sb.append("<div class='input-group-btn'>");
				// reset quick search
				String id = ftE.getSearchElement().getFormDispatchId();
				sb.append("<a href=\"javascript:jQuery('#").append(id).append("').val('');")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
						  new NameValuePair("reset-search", "true")))
				  .append("\" class='btn o_reset_quick_search'><i class='o_icon o_icon_remove_filters' aria-label='")
				  .append(translator.translate("aria.reset.search")).append("'> </i></a>");
							
				renderFormItem(renderer, sb, ftE.getSearchButton(), ubu, translator, renderResult, null);
				if(ftE.getExtendedSearchButton() != null) {
					renderFormItem(renderer, sb, ftE.getExtendedSearchButton(), ubu, translator, renderResult, null);
				}
			}
			
			StringBuilder filterIndication = new StringBuilder();
			if(ftE.getExtendedFilterButton() != null) {
				ftE.getSelectedExtendedFilters().forEach(filter -> {
					if(filterIndication.length() > 0) filterIndication.append(", ");
					filterIndication.append(filter.getLabel());
				});
				
				renderFormItem(renderer, sb, ftE.getExtendedFilterButton(), ubu, translator, renderResult, args);
			}
			sb.append("</div>", searchInput);// close the div input-group-btn
			sb.append("</div>");
			if(filterIndication.length() > 0) {
				sb.append("<div class='o_table_tools_indications").append("_filter_only", !searchInput).append("'>").append(filterIndication)
				// remove filter
				  .append("<a href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
						  new NameValuePair("rm-extended-filter", "true")))
				  .append("\" title=\"").append(translator.translate("remove.filters")).append("\">")
				  .append("<i class='o_icon o_icon_remove o_icon-fw'> </i></a></div>");
			}
		} else if(ftE.getExtendedSearchButton() != null) {
			renderFormItem(renderer, sb, ftE.getExtendedSearchButton(), ubu, translator, renderResult, args);
		}
	}
	
	protected String renderFilterDropdown(StringOutput sb, FlexiTableElementImpl ftE, List<FlexiTableFilter> filters, Translator translator) {
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		StringBuilder selected = new StringBuilder(256);
		
		sb.append("<div class='btn-group'>")
		  .append("<button id='table-button-filters-").append(dispatchId).append("' type='button' aria-label='")
		  .append(translator.translate("aria.filters")).append("' class='btn btn-default dropdown-toggle' data-toggle='dropdown'>")
		  .append("<i class='o_icon o_icon_filter o_icon-lg'> </i> <b class='caret'> </b></button>")
		  .append("<div id='table-filters-").append(dispatchId).append("' class='hide'><ul class='o_dropdown list-unstyled' role='menu'>");
		
		List<FlexiTableFilter> selectedFilters = ftE.getSelectedFilters();
		List<FlexiTableFilter> selectedExtendedFilters = ftE.getSelectedExtendedFilters();
		
		
		for(FlexiTableFilter filter:filters) {
			if(FlexiTableFilter.SPACER.equals(filter)) {
				sb.append("<li class='divider'></li>");
			} else {
				boolean isSelected = filter.isSelected() || (filter.isShowAll() && selectedFilters.isEmpty() && selectedExtendedFilters.isEmpty());
				
				sb.append("<li><a href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
						  new NameValuePair("filter", filter.getFilter())))
				  .append("\">").append("<i class='o_icon o_icon_check o_icon-fw'> </i> ", isSelected);
				if(filter.getIconLeftCSS() != null) {
					sb.append("<i class='o_icon ").append(filter.getIconLeftCSS()).append("'> </i> ");
				}
				if(filter.getIconRenderer() != null) {
					filter.getIconRenderer().render(sb, filter, ftE.getComponent(), ftE.getTranslator());
				}
				sb.append(filter.getLabel()).append("</a></li>");
				if(filter.isSelected() && !filter.isShowAll()) {
					if(selected.length() > 0) selected.append(", ");
					selected.append(filter.getLabel());
				}
			}
		}
		sb.append("</ul></div></div>\n")
		  .append("<script>\n")
		  .append("jQuery(function() { o_popover('table-button-filters-").append(dispatchId).append("','table-filters-").append(dispatchId).append("'); });\n")
		  .append("</script>");
		return selected.toString();
	}
	
	protected void renderSortDropdown(StringOutput sb, FlexiTableElementImpl ftE, List<FlexiTableSort> sorts, Translator translator) {
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		
		sb.append("<div class='btn-group'>")
		  .append("<button id='table-button-sorters-").append(dispatchId).append("' type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown'")
		  .append(" aria-label='").append(translator.translate("aria.sort")).append("'>")
		  .append("<i class='o_icon o_icon_sort_menu o_icon-lg'> </i> <b class='caret'></b></button>")
		  .append("<div id='table-sorters-").append(dispatchId).append("' class='hide'><ul class='o_dropdown list-unstyled' role='menu'>");
		
		for(FlexiTableSort sort:sorts) {
			if(FlexiTableSort.SPACER.equals(sort)) {
				sb.append("<li class='divider'></li>");
			} else {
				sb.append("<li><a href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
						  new NameValuePair("sort", sort.getSortKey().getKey()),
						  new NameValuePair("asc",  sort.getSortKey().isAsc() ? "desc" : "asc")))
				  .append("\">");
				if(sort.isSelected()) {
					if(sort.getSortKey().isAsc()) {
						sb.append("<i class='o_icon o_icon_sort_desc o_icon-fw'> </i> ");
					} else {
						sb.append("<i class='o_icon o_icon_sort_asc o_icon-fw'> </i> ");
					}
				}
				sb.append(sort.getLabel()).append("</a></li>");
			}
		}
		sb.append("</ul></div></div>\n")
		  .append("<script>\n")
		  .append("jQuery(function() { o_popover('table-button-sorters-").append(dispatchId).append("','table-sorters-").append(dispatchId).append("'); });\n")
		  .append("</script>");
	}
	
	protected void renderHeaderSwitchType(FlexiTableRendererType type, Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if(type != null) {
			switch(type) {
				case custom: {
					renderFormItem(renderer, sb, ftE.getCustomTypeButton(), ubu, translator, renderResult, args);
					break;
				}
				case classic: {
					renderFormItem(renderer, sb, ftE.getClassicTypeButton(), ubu, translator, renderResult, args);
					break;
				}
			}
		}
	}
	
	protected void renderBreadcrumbs(StringOutput sb, FlexiTableElementImpl ftE) {
		FlexiTreeTableNode rootCrumb = ftE.getRootCrumb();
		List<FlexiTreeTableNode> crumbs = ftE.getCrumbs();
		if(rootCrumb != null || crumbs.size() > 0) {
			sb.append("<div class='o_breadcrumb o_table_flexi_breadcrumb'><ol class='breadcrumb'>");
			if(rootCrumb != null) {
				renderBreadcrumbs(sb, ftE, rootCrumb, "tt-root-crumb");
			}
			int index = 0;
			for(FlexiTreeTableNode crumb:crumbs) {
				renderBreadcrumbs(sb, ftE, crumb, Integer.toString(index++));
			}
			sb.append("</ol></div>");
		}
	}
	
	protected void renderBreadcrumbs(StringOutput sb, FlexiTableElementImpl ftE, FlexiTreeTableNode crumb, String index) {
		String dispatchId = ftE.getFormDispatchId();
		sb.append("<li><a id='").append(dispatchId).append("_").append(index).append("_bc' href=\"javascript:;\" onclick=\"")
		  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
				  new NameValuePair("tt-crumb", index)))
		  .append("\">").append(crumb.getCrump()).append("</a></li>");
	}
	
	protected void renderFormItem(Renderer renderer, StringOutput sb, FormItem item, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if(item != null && item.isVisible()) {
			Component cmp = item.getComponent();
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
			cmp.setDirty(false);
		}
	}
	
	protected void renderFooterButtons(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		if(ftE.isSelectAllEnable() || ftE.getTreeTableDataModel() != null) {
			String dispatchId = ftE.getFormDispatchId();

			if(ftE.getTreeTableDataModel() != null && ftE.getTreeTableDataModel().hasOpenCloseAll()) {
				sb.append("<div class='o_table_footer'><div class='o_table_expandall input-sm'>");
			
				sb.append("<a id='")
				  .append(dispatchId).append("_toa' href=\"javascript:;\" onclick=\"")
				  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
						  new NameValuePair("tt-openclose", "openall")))
				  .append("\"><i class='o_icon o_icon-lg o_icon_close_tree'> </i> <span>").append(translator.translate("form.openall"))
				  .append("</span></a>");

				sb.append("<a id='")
				  .append(dispatchId).append("_tca' href=\"javascript:;\" onclick=\"")
				  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, true, true, true,
						  new NameValuePair("tt-openclose", "closeall")))
				  .append("\"><i class='o_icon o_icon-lg o_icon_open_tree'> </i> <span>").append(translator.translate("form.closeall"))
				  .append("</span></a>");
				
				sb.append("</div></div>");
			}
		}
		
		if(ftE.getDefaultPageSize() > 0) {
			renderPagesLinks(sb, ftC, translator);
		}
	}
	
	protected void renderFooterGroupedButtons(Renderer renderer, StringOutput sb, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		List<FormItem> items = ftC.getFlexiTableElement().getBatchButtons();
		if(items != null) {
			boolean atLeastOneVisible = items.stream().anyMatch(FormItem::isVisible);
			if(atLeastOneVisible) {
				sb.append("<div class='o_button_group'>");
				for(FormItem item:items) {
					renderFormItem(renderer, sb, item, ubu, translator, renderResult, args);
				}
				sb.append("</div>");
			}
		}
	}
	
	protected abstract void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator);
	
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
				renderRow(renderer, target, ftC, rowIdPrefix, i, ubu, translator, renderResult);
			}
		}				
		// end of table table
	}
	
	protected abstract void renderRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult);

	protected abstract void renderFooter(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult);

	private void renderPagesLinks(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		int pageSize = ftE.getPageSize();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		int rows = dataModel.getRowCount();

		if (rows > ftE.getDefaultPageSize()) {
			renderPageSize(sb, ftC, translator);
		}

		if(pageSize > 0 && rows > pageSize) {
			sb.append("<ul class='pagination'>");
			int page = ftE.getPage();
			int maxPage = (int)Math.ceil(((double) rows / (double) pageSize));
			renderPageBackLink(sb, ftC, page);
			renderPageNumberLinks(sb, ftC, page, maxPage);
			renderPageNextLink(sb, ftC, page, maxPage);
			sb.append("</ul>");
		}
	}
	
	private void renderPageSize(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		
		int pageSize = ftE.getPageSize();
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);
		
		sb.append("<div class='o_table_rows_infos o_noprint'>");
		sb.append(translator.translate("page.size.a", new String[] {
				Integer.toString(firstRow + 1),//for humans
				Integer.toString(lastRow),
				Integer.toString(rows)
		  }))
		  .append(" ");
		
		sb.append("<div class='btn-group dropup'><button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-expanded='false'>")
	      .append(" <span>");
		if(pageSize < 0) {
			sb.append(translator.translate("show.all"));
		} else {
			sb.append(Integer.toString(pageSize));
		}
		
		sb.append("</span> <span class='caret'></span></button>")
	      .append("<ul class='dropdown-menu' role='menu'>");
		
		int[] sizes = new int[]{ 20, 50, 100, 250 };
		int defaultPageSize = ftE.getDefaultPageSize();
		if (Arrays.binarySearch(sizes, defaultPageSize) < 0) {
			sizes = new int[]{ 20, 50, 100, 250, defaultPageSize };
			Arrays.sort(sizes);
		}
		for(int size:sizes) {
			sb.append("<li><a href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
					  new NameValuePair("pagesize", Integer.toString(size))))
			  .append("\">").append(Integer.toString(size)).append("</a></li>");
		}
		
		if(ftE.isShowAllRowsEnabled()) {
			sb.append("<li><a href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
					  new NameValuePair("pagesize", "all")))
			  .append("\">").append(translator.translate("show.all")).append("</a></li>");
		}
		  
		sb.append("</ul></div>")
		  .append(" ").append(translator.translate("page.size.b"))
		  .append("</div> ");
	}
	
	private void renderPageBackLink(StringOutput sb, FlexiTableComponent ftC, int page) {
		boolean disabled = (page <= 0);
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<li").append(" class='disabled'", disabled).append("><a href=\"");
		if(disabled) {
			sb.append("#");
		} else {
			sb.append("javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, true, true, true,
					  new NameValuePair("page", Integer.toString(page - 1))));
		}
		sb.append("\">").append("&laquo;").append("</a></li>");
	}
	
	private void renderPageNextLink(StringOutput sb, FlexiTableComponent ftC, int page, int maxPage) {
		boolean disabled = (page >= maxPage);
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<li ").append(" class='disabled'", disabled).append("><a href=\"");
		if(disabled) {
			sb.append("#");
		} else {
			sb.append("javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, true, true, true,
					  new NameValuePair("page", Integer.toString(page + 1)))); 
		}
		sb.append("\">").append("&raquo;").append("</a></li>");
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
			for (int i = 0; i <= maxPage; i++) {
				// adapt stepsize if needed
				stepSize = adaptStepIfNeeded(page, maxStepSize, stepSize, i);
	
				isStep = ((i % stepSize) == 0);
				if (isStep) {
					stepCnt++;
					isStep = isStep && (stepCnt % useEveryStep == 0);
				}
				isNear = (i > (page - nearleft) && i < (page + nearright));
				if (i == 0 || i == maxPage || isStep || isNear) {
					appendPagenNumberLink(sb, ftC, page, i);
				}
			}
		}
	}
	
	private void appendPagenNumberLink(StringOutput sb, FlexiTableComponent ftC, int page, int i) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<li").append(" class='active'", (page == i)).append("><a href=\"javascript:")
		  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, true, true, true,
				  new NameValuePair("page", Integer.toString(i))))
		  .append(";\">").append(i+1).append("</a></li>");
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
