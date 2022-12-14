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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiFilterButton;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiFiltersElementImpl;
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
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFormItem();
		String id = ftC.getFormDispatchId();
		
		renderHeaders(renderer, sb, ftE, ubu, translator, renderResult, args);
		
		if(ftE.getTableDataModel().getRowCount() == 0 && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey())) {
			renderEmptyState(renderer, sb, ubu, translator, renderResult, ftE);
		} else {
			renderTable(renderer, sb, ftC, ubu, translator, renderResult);
		}
		
		//source
		if (source.isEnabled()) {
			FormJSHelper.appendFlexiFormDirty(sb, ftE.getRootForm(), id);
		}
	}
	
	protected final void renderHeaders(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		if(ftE.isFilterTabsEnabled()) {
			renderFilterTabs(renderer, sb, ftE, ubu, translator, renderResult, args);
		}
		if(ftE.isSearchEnabled() && ftE.isSearchLarge()) {
			renderLargeSearch(renderer, sb, ftE, ubu, translator, renderResult);
		}
		if(ftE.isFiltersEnabled()) {
			renderFiltersRow(renderer, sb, ftE, ubu, translator, renderResult, args);
		}
		renderSearchAndOptions(renderer, sb, ftE, ubu, translator, renderResult, args);
		renderBreadcrumbs(sb, ftE);
		renderBulkActions(renderer, sb, ftE.getComponent(), ubu, translator, renderResult, args);
	}
	
	protected void renderTable(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, URLBuilder ubu, Translator translator,
			RenderResult renderResult) {
		FlexiTableElementImpl ftE = ftC.getFormItem();
		String id = ftC.getFormDispatchId();
		
		sb.append("<div class='o_table_wrapper o_table_flexi")
		  .append(" o_table_edit", ftE.isEditMode())
		  .append(" o_table_bulk", hasVisibleBulkActions(ftC));
		String css = ftE.getElementCssClass();
		if (css != null) {
			sb.append(" ").append(css);
		}
		switch(ftE.getRendererType()) {
			case custom: sb.append(" o_rendertype_custom"); break;
			case classic: sb.append(" o_rendertype_classic"); break;
			case external: sb.append(" o_rendertype_user"); break;
		}
		sb.append("'");
		String wrapperSelector = ftE.getWrapperSelector();
		if (wrapperSelector != null) {
			sb.append(" id='").append(wrapperSelector).append("'");
		}
		String scrollableWrapperId = "o_scroll_" + id;
		sb.append("><div class='o_scrollable_wrapper' id=\"").append(scrollableWrapperId).append("\"><div class='o_scrollable'>")
			.append("<table id=\"").append(id).append("\" class=\"table table-condensed table-striped table-hover").append(" table-bordered", ftE.isBordered());
		if(ftE.getCssDelegate() != null) {
			String tableCssClass = ftE.getCssDelegate().getTableCssClass(FlexiTableRendererType.classic);
			if(tableCssClass != null) {
				sb.append(" ").append(tableCssClass);
			}
		}
		sb.append("\">");
		
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
		
		sb.append("</table></div></div>"); // END o_scrollable_wrapper and o_scrollable
		
		renderTreeButtons(sb, ftC, translator);
		if(ftE.getDefaultPageSize() > 0) {
			renderPagesLinks(sb, ftC, translator);
		}
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
		sb.append("</div>"); // END o_table_wrapper
		// Initialize the scrolling overflow indicator code
		sb.append("<script>o_initScrollableOverflowIndicator('").append(scrollableWrapperId).append("');</script>");
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

	/**
	 *  Subclasses can override this method to render extra HTML or form items on the right side of the
	 *  toolbar above the table.
	 */
	protected void renderUserOptions(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu,
									 Translator translator, RenderResult renderResult) {
		//
	}

	private void renderFilterTabs(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		renderFormItem(renderer, sb, ftE.getFilterTabsElement(), ubu, translator, renderResult, args);
	}
	
	private void renderFiltersRow(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		renderFormItem(renderer, sb, ftE.getFiltersElement(), ubu, translator, renderResult, args);
	}
	
	private void renderLargeSearch(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult) {
		
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();

		sb.append("<div class='o_table_large_search o_noprint'>");
		TextElement searchEl = ftE.getSearchElement();
		if(StringHelper.containsNonWhitespace(searchEl.getValue())) {
			searchEl.setFocus(true);
		}
		if(!StringHelper.containsNonWhitespace(searchEl.getPlaceholder())) {
			searchEl.setPlaceholderKey("search.placeholder", null);
		}
		renderFormItem(renderer, sb, searchEl, ubu, translator, renderResult, null);
		
		// reset quick search
		String id = ftE.getSearchElement().getFormDispatchId();
		sb.append("<a href=\"javascript:jQuery('#").append(id).append("').val('');")
		  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
				  new NameValuePair("reset-search", "true")))
		  .append("\" class='btn btn-default o_reset_quick_search' aria-label='")
		  .append(translator.translate("aria.reset.search")).append("'><i class='o_icon o_icon_remove_filters'> </i></a>");
		
		renderFormItem(renderer, sb, ftE.getSearchButton(), ubu, translator, renderResult, null);
		
		// num. of entries
		if(ftE.isNumOfRowsEnabled()) {
			sb.append("<div class='o_table_rowcount'>");
			int rowCount = ftE.getTableDataModel().getRowCount();
			if(rowCount == 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entry"));
			} else if(rowCount > 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entries"));
			}
			sb.append("</div>");
		}
		sb.append("</div>");
	}
	
	private void renderSearchAndOptions(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		Component searchCmp = ftE.getExtendedSearchComponent();
		
		boolean empty = ftE.getTableDataModel().getRowCount() == 0;
		boolean hideSearch = empty && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey()) && !ftE.isFilterEnabled() 
				&& !ftE.isExtendedSearchExpanded() && !StringHelper.containsNonWhitespace(ftE.getQuickSearchString())
				&& !ftE.isShowAlwaysSearchFields();
		
		if(searchCmp != null && ftE.isExtendedSearchExpanded()) {
			renderer.render(searchCmp, sb, args);
		}
		
		boolean isBulk = hasVisibleBulkActions(ftE.getComponent()) && ftE.hasMultiSelectedIndex();
		sb.append("<div class='o_table_toolbar").append(" o_table_batch_show", " o_table_batch_hide",  isBulk).append(" clearfix'>");
		
		sb.append("<div class='o_table_search o_noprint").append(" o_table_search_extended", ftE.getExtendedSearchButton() != null).append("'>");
		// search
		if(!hideSearch && (searchCmp == null || !ftE.isExtendedSearchExpanded())
				&& !ftE.isSearchLarge() && ftE.isSearchEnabled() && ftE.getSearchElement() != null) {
			TextElement searchEl = ftE.getSearchElement();
			if(StringHelper.containsNonWhitespace(searchEl.getPlaceholder())) {
				searchEl.setPlaceholderKey(null, null);
			}
			renderFormItem(renderer, sb, searchEl, ubu, translator, renderResult, null);
			
			// reset quick search
			String id = ftE.getSearchElement().getFormDispatchId();
			sb.append("<a href=\"javascript:jQuery('#").append(id).append("').val('');")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
					  new NameValuePair("reset-search", "true")))
			  .append("\" class='btn btn-default o_reset_quick_search' aria-label='")
			  .append(translator.translate("aria.reset.search")).append("'><i class='o_icon o_icon_remove_filters'> </i></a>");
			
			if(ftE.getExtendedSearchButton() != null) {
				renderFormItem(renderer, sb, ftE.getSearchButton(), ubu, translator, renderResult, null);
				renderFormItem(renderer, sb, ftE.getExtendedSearchButton(), ubu, translator, renderResult, null);
			} else {
				renderFormItem(renderer, sb, ftE.getSearchButton(), ubu, translator, renderResult, null);
			}
		}
		
		// num. of entries
		if(!empty && ftE.isNumOfRowsEnabled()) {
			sb.append(" <span>");
			int rowCount = ftE.getTreeTableDataModel() == null
					? ftE.getTableDataModel().getRowCount() : ftE.getTreeTableDataModel().getTotalNodesCount();
			if(rowCount == 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entry"));
			} else if(rowCount > 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entries"));
			}
			sb.append("</span>");
		}
		sb.append("</div>");

		renderUserOptions(renderer, sb, ftE, ubu, translator, renderResult);

		sb.append("<div class='o_table_tools o_noprint'>");
		
		String filterIndication = null;
		//filter
		if(ftE.isFilterEnabled()) {
			List<FlexiTableFilter> filters = ftE.getDropdownFilters();
			if(filters != null && !filters.isEmpty()) {
				filterIndication = renderFilterDropdown(sb, ftE, filters, translator);
			}
		}
		
		// order by
		if(!empty && ftE.isSortEnabled()) {
			List<FlexiTableSort> sorts = ftE.getSorts();
			if(sorts != null && !sorts.isEmpty()) {
				renderSortDropdown(sb, ftE, sorts, translator);
			}
		}
		
		// view custom / table
		//switch type of tables
		FlexiTableRendererType[] types = ftE.getAvailableRendererTypes();
		if(!empty && types.length > 1) {
			sb.append("<div class='btn-group'>");
			for(FlexiTableRendererType type:types) {
				renderHeaderSwitchType(type, renderer, sb, ftE, ubu, translator, renderResult, null);
			}
			sb.append("</div> ");
		}
		
		// preferences columns
		if(!empty && ftE.getCustomButton() != null && ftE.isCustomizeColumns()
				&& (ftE.getRendererType() == null || ftE.getRendererType() == FlexiTableRendererType.classic)) {
			sb.append("<div class='btn-group'>");
			renderFormItem(renderer, sb, ftE.getCustomButton(), ubu, translator, renderResult, null);
			sb.append("</div> ");
		} else if(ftE.getCustomButton() != null) {
			ftE.getCustomButton().getComponent().setDirty(false);
		}
		
		// download
		if(!empty && ftE.getExportButton() != null && ftE.isExportEnabled()) {
			sb.append("<div class='btn-group'>");
			ftE.getExportButton().setEnabled(!empty);
			renderFormItem(renderer, sb, ftE.getExportButton(), ubu, translator, renderResult, null);
			sb.append("</div> ");
		} else if(ftE.getExportButton() != null) {
			ftE.getExportButton().getComponent().setDirty(false);
		}
		
		// all settings
		if(ftE.getSettingsButton() != null) {
			if(hasSettingsButton(ftE)) {
				sb.append("<div class='btn-group o_table_settings'>");
				renderFormItem(renderer, sb, ftE.getSettingsButton(), ubu, translator, renderResult, null);
				sb.append("</div> ");
			}
			ftE.getSettingsButton().getComponent().setDirty(false);
		}
		
		if(StringHelper.containsNonWhitespace(filterIndication)) {
			sb.append("<div class='o_table_tools_indications'>").append(filterIndication)
				// remove filter
			  .append(" <a href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, true, true, true,
					  new NameValuePair("rm-filter", "true")))
			  .append("\" title=\"").append(translator.translate("remove.filters")).append("\">")
			  .append("<i class='o_icon o_icon_remove o_icon-fw'> </i></a></div>"); 
		}
		
		sb.append("</div>");
		
		sb.append("</div>");
	}
	
	private boolean hasSettingsButton(FlexiTableElementImpl ftE) {
		if(ftE.isCustomizeColumns()
				|| (ftE.getAvailableRendererTypes() != null && ftE.getAvailableRendererTypes().length > 1)) {
			return true;
		}
		
		if(ftE.isSortEnabled()) {
			List<FlexiTableSort> sorts = ftE.getSorts();
			if(sorts != null && !sorts.isEmpty()) {
				return true;
			}
		}
		
		FlexiFiltersElementImpl filtersEl = ftE.getFiltersElement();
		if(filtersEl != null) {
			List<FlexiFilterButton> filtersButtons = filtersEl.getFiltersButtons();
			if(filtersButtons != null && !filtersButtons.isEmpty()) {
				return true;
			}
		}
		
		return false;
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
		
		List<FlexiTableFilter> selectedFilters = ftE.getFilters();

		for(FlexiTableFilter filter:filters) {
			if(FlexiTableFilter.SPACER.equals(filter)) {
				sb.append("<li class='divider'></li>");
			} else {
				boolean isSelected = filter.isSelected() || (filter.isShowAll() && selectedFilters.isEmpty());
				
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
		
		FlexiTableSort selectedSort = null;
		for(FlexiTableSort sort:sorts) {
			if(sort.isSelected()) {
				selectedSort = sort;
			}
		}

		sb.append("<div class='btn-group'>")
		  .append("<button id='table-button-sorters-").append(dispatchId).append("' type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown'")
		  .append(" aria-label='").append(translator.translate("aria.sort")).append("'>");
		
		if(selectedSort != null) {
			if(selectedSort.getSortKey().isAsc()) {
				sb.append("<i class='o_icon o_icon_sort_amount_asc o_icon-lg'> </i> ");
			} else {
				sb.append("<i class='o_icon o_icon_sort_amount_desc o_icon-lg'> </i> ");
			}
			if(StringHelper.containsNonWhitespace(selectedSort.getLabel())) {
				sb.append(selectedSort.getLabel()).append(" ");
			}
		} else {
			sb.append("<i class='o_icon o_icon_sort_menu o_icon-lg'> </i> ");
		}
		sb.append("<b class='caret'></b></button>")
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
				case external: {
					renderFormItem(renderer, sb, ftE.getExternalTypeButton(), ubu, translator, renderResult, args);
					break;
				}
			}
		}
	}
	
	protected void renderBreadcrumbs(StringOutput sb, FlexiTableElementImpl ftE) {
		FlexiTreeTableNode rootCrumb = ftE.getRootCrumb();
		List<FlexiTreeTableNode> crumbs = ftE.getCrumbs();
		if(rootCrumb != null || !crumbs.isEmpty()) {
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
	
	protected void renderTreeButtons(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFormItem();
		if(ftE.getTreeTableDataModel() != null) {
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
	}
	
	protected void renderBulkActions(Renderer renderer, StringOutput sb, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		if(!hasVisibleBulkActions(ftC) && ftC.getFormItem().getSelectionMode() != SelectionMode.multi) return;
		
		FlexiTableElementImpl ftE = ftC.getFormItem();
		List<FormItem> items = ftE.getBatchButtons();
		int numOf = ftE.getNumOfMultiSelectedIndex();
		if(ftE.getTableDataModel() instanceof FlexiTableSelectionDelegate) {
			numOf = ((FlexiTableSelectionDelegate<?>)ftE.getTableDataModel()).getSelectedTreeNodes().size();
		}
		String entryI18n;
		if(numOf <= 1) {
			entryI18n = translator.translate("number.selected.entry", Integer.toString(numOf));
		} else {
			entryI18n = translator.translate("number.selected.entries", Integer.toString(numOf));
		}
		
		String dispatchId = ftE.getFormDispatchId();	
		sb.append("<div id='").append(dispatchId).append("_bab' class='o_button_group o_table_batch_buttons ")
		  .append("o_table_batch_show", "o_table_batch_hide", numOf > 0 || ftE.hasMultiSelectedIndex()).append("'>")
		  .append("<span id='").append(dispatchId).append("_mscount' class='o_table_batch_label'>")
		  .append(entryI18n).append("</span> ");
		for(FormItem item:items) {
			renderFormItem(renderer, sb, item, ubu, translator, renderResult, args);
		}
		sb.append("</div>");
	}
	
	private boolean hasVisibleBulkActions(FlexiTableComponent ftC) {
		FlexiTableElementImpl ftE = ftC.getFormItem();
		List<FormItem> items = ftE.getBatchButtons();
		if(items != null && !items.isEmpty()) {
			return items.stream().anyMatch(FormItem::isVisible);
		}
		return false;
	}
	
	protected abstract void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator);
	
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFormItem();
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

	protected void renderPagesLinks(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFormItem();
		int pageSize = ftE.getPageSize();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		int rows = dataModel.getRowCount();
		
		sb.append("<div class='o_table_pagination'>");

		if (rows > ftE.getDefaultPageSize()) {
			renderSmallPageSize(sb, ftC, translator);
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
		
		sb.append("</div>");
	}
	
	private void renderSmallPageSize(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFormItem();
	
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		
		int pageSize = ftE.getPageSize();
		sb.append("<div class='o_table_rows_infos o_noprint'>")
		  .append(translator.translate("page.size.a.small"))
		  .append(" ")
		  .append("<div class='btn-group dropup'><button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-expanded='false'>")
	      .append("<span>");
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
		FlexiTableElementImpl ftE = ftC.getFormItem();
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
		FlexiTableElementImpl ftE = ftC.getFormItem();
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
		FlexiTableElementImpl ftE = ftC.getFormItem();
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
