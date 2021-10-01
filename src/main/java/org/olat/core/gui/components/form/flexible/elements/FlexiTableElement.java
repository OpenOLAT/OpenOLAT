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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.util.UserSession;
import org.olat.core.util.prefs.Preferences;

/**
 * 
 */
public interface FlexiTableElement extends FormItem, FormItemCollection {

	public static final String ROM_SELECT_EVENT = "rSelect";
	public static final String TABLE_EMPTY_ICON = "o_icon_empty_objects";

	
	@Override
	public FlexiTableComponent getComponent();
	
	
	public FlexiTableStateEntry getStateEntry();
	
	public void setStateEntry(UserRequest ureq, FlexiTableStateEntry state);
	
	/**
	 * @return the type of renderer used by  this table
	 */
	public FlexiTableRendererType getRendererType();
	
	/**
	 * Set the renderer for this table
	 * @param rendererType
	 */
	public void setRendererType(FlexiTableRendererType rendererType);
	
	/**
	 * Set the renderer available
	 * @param rendererType
	 */
	public void setAvailableRendererTypes(FlexiTableRendererType... rendererType);
	
	/**
	 * Set the row renderer used by the custom renderer type.
	 * @param renderer
	 * @param componentDelegate
	 */
	public void setRowRenderer(VelocityContainer renderer, FlexiTableComponentDelegate componentDelegate);
	
	/**
	 * Enable to show details of multiple rows in the table. If disabled, the behavior is like an accordion.
	 * @param multiDetails
	 */
	public void setMultiDetails(boolean multiDetails);
	
	/**
	 * Set the details renderer used by the classic renderer type.
	 * @param rowRenderer
	 * @param componentDelegate
	 */
	public void setDetailsRenderer(VelocityContainer rowRenderer, FlexiTableComponentDelegate componentDelegate);
	
	/**
	 * @return Return true if the table has border on every cell.
	 */
	public boolean isBordered();
	
	/**
	 * If true, set a border to every cell.
	 * 
	 * @param bordered Set or not border to the cells
	 */
	public void setBordered(boolean bordered);
	
	/**
	 * @return True if the footer is enabled
	 */
	public boolean isFooter();
	
	/**
	 * To enable the footer, the data model need to implement
	 * org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel
	 * 
	 * @param footer Enable/disable the footer
	 */
	public void setFooter(boolean footer);

	/**
	 * @return True if muli selection is enabled
	 */
	public boolean isMultiSelect();
	
	/**
	 * Enable multi-selection. In the case of multi, the rows
	 * of the data model needs to implements and hashCode / equals
	 * method.
	 * 
	 */
	public void setMultiSelect(boolean enable);
	
	/**
	 * 
	 * @return true if the user can customize the columns of the table
	 */
	public boolean isCustomizeColumns();

	/**
	 * Enable customizing of columns
	 * @param customizeColumns
	 */
	public void setCustomizeColumns(boolean customizeColumns);
	
	/**
	 * Set the id of the preferences saved on the database.
	 * 
	 * @param ureq
	 * @param id
	 */
	public void setAndLoadPersistedPreferences(UserRequest ureq, String id);
	
	public void setAndLoadPersistedPreferences(Preferences prefs, String id);
	
	/**
	 * @return The CSS selector used to calculate the height of the table
	 * (datatables variant only)
	 */
	public String getWrapperSelector();
	
	/**
	 * Set a CSS selector for the datatables variant. It can
	 * calculate the height it can use.
	 * @param wrapperSelector
	 */
	public void setWrapperSelector(String wrapperSelector);
	
	public FlexiTableCssDelegate getCssDelegate();

	public void setCssDelegate(FlexiTableCssDelegate rowCssDelegate);
	
	/**
	 * 
	 * @return
	 */
	public int getColumnIndexForDragAndDropLabel();
	
	/**
	 * Show the num of rows, or not
	 * 
	 * @param enable
	 */
	public void setNumOfRowsEnabled(boolean enable);
	
	/**
	 * @return True if the choice in page size "All" is allowed.
	 */
	public boolean isShowAllRowsEnabled();
	
	/**
	 * Enable/disable the "All" choice for the page sizes.
	 * @param showAllRowsEnabled
	 */
	public void setShowAllRowsEnabled(boolean showAllRowsEnabled);

	/**
	 * Setting a value enable the drag and drop on this table. Drag and drop
	 * is only implemented for the classic voew.
	 * 
	 * @param columnLabelForDragAndDrop
	 */
	public void setColumnIndexForDragAndDropLabel(int columnLabelForDragAndDrop);
	
	/**
	 * @return true if the links select all / unselect all are enabled
	 */
	public boolean isSelectAllEnable();
	
	/**
	 * Enable the select all /unselect all links
	 * @param enable
	 */
	public void setSelectAllEnable(boolean enable);
	
	/**
	 * Return all selected rows
	 * @return
	 */
	public Set<Integer> getMultiSelectedIndex();
	
	/**
	 * Set a list of selected index (don't sort after this point)
	 * @param set
	 */
	public void setMultiSelectedIndex(Set<Integer> set);
	
	/**
	 * 
	 * @param index
	 * @return true if the row is selected
	 */
	public boolean isMultiSelectedIndex(int index);
	
	/**
	 * Select all rows of all pages.
	 */
	public void selectAll();
	
	/**
	 * Select all rows visible on the current page.
	 */
	public void selectPage();
	
	/**
	 * Remove all multi selected index.
	 */
	public void deselectAll();
	
	/**
	 * Is a search field enabled
	 * @return
	 */
	public boolean isSearchEnabled();
	
	/**
	 * Enable the search field
	 * @param enable
	 */
	public void setSearchEnabled(boolean enable);
	
	/**
	 * Enable the search with a suggestions provider.
	 * 
	 * @param autoCompleteProvider
	 */
	public void setSearchEnabled(ListProvider autoCompleteProvider, UserSession usess);
	
	/**
	 * Is the filer enabled?
	 * @return
	 */
	public boolean isFilterEnabled();
	
	public List<FlexiTableFilter> getSelectedFilters();
	
	/**
	 * @return The selected key by the filter, or null if no item is selected
	 */
	public String getSelectedFilterKey();
	
	/**
	 * Preset the selected filter, but don't trigger sort/filter operation.
	 * @param key
	 */
	public void setSelectedFilterKey(String key);
	
	public void setSelectedFilterKeys(Collection<String> keys);
	
	/**
	 * Make sure that the option multi-selection is enabled if you specify several filters.
	 * 
	 * @param filters A list of filters
	 */
	public void setSelectedFilters(List<FlexiTableFilter> filters);
	
	/**
	 * @return The selected value by the filter, or null if no item is selected
	 */
	public String getSelectedFilterValue();
	
	/**
	 * Set the values for the filter and it will enable it.
	 * @param keys
	 * @param values
	 * @param multiSelection Allow to select more than one filter
	 */
	public void setFilters(String label, List<FlexiTableFilter> filters, boolean multiSelection);
	
	/**
	 * 
	 * @param label
	 * @param sorts
	 */
	public void setSortSettings(FlexiTableSortOptions options);
	
	/**
	 * Return the current sorting if any.
	 * @return
	 */
	public SortKey[] getOrderBy();
	
	/**
	 * Enable export
	 * @return True if export is enabled
	 */
	public boolean isExportEnabled();
	
	public void setExportEnabled(boolean enabled);
	
	/**
	 *
	 * @return True if the table is in editing mode
	 */
	public boolean isEditMode();

	/**
	 * Set a visual change but do not change anything on the model
	 * @param editMode
	 */
	public void setEditMode(boolean editMode);
	
	public boolean isColumnModelVisible(FlexiColumnModel col);
	
	public void setColumnModelVisible(FlexiColumnModel col, boolean visible);
	
	/**
	 * 
	 * @param callout
	 */
	public void setExtendedSearch(ExtendedFlexiTableSearchController controller);
	
	
	public boolean isExtendedSearchExpanded();
	
	/**
	 * Open the extended search
	 */
	public void expandExtendedSearch(UserRequest ureq);
	
	/**
	 * Close the extended search callout if open
	 */
	public void collapseExtendedSearch();
	
	public boolean isFiltersEnabled();
	
	public List<FlexiTableExtendedFilter> getExtendedFilters();
	
	/**
	 * 
	 * @param enable true to enable the filter buttons are enable
	 * @param filters The list of filters
	 * @param customPresets If the user can save custom filters presets (if tab enabled)
	 * @param alwaysExpanded The filters are always expanded
	 */
	public void setFilters(boolean enable, List<FlexiTableExtendedFilter> filters, boolean customPresets, boolean alwaysExpanded);
	
	public void setFiltersValues(String quickSearch, List<FlexiTableFilterValue> values);
	
	public void expandFilters(boolean expand);
	
	public boolean isFiltersExpanded();
	
	public boolean isFilterTabsEnabled();
	
	public void setFilterTabs(boolean enable, List<? extends FlexiFiltersTab> tabs);
	
	public FlexiFiltersTab getFilterTabById(String id);
	
	public FlexiFiltersTab getSelectedFilterTab();
	
	public void setSelectedFilterTab(UserRequest ureq, FlexiFiltersTab tab);
	
	/**
	 * Add the current state of the table, especially tabs / presets in the history.
	 * 
	 * @param ureq The user request
	 */
	public void addToHistory(UserRequest ureq);
	
	/**
	 * Is the details view visible for this particular row?
	 */
	public boolean isDetailsExpended(int row);
	
	/**
	 * 
	 */
	public void expandDetails(int row);
	
	public void collapseDetails(int row);
	
	public void collapseAllDetails();
	
	/**
	 * @return The root bread crumb or null
	 */
	public FlexiTreeTableNode getRootCrumb();

	/**
	 * Set a root bread crumb which is not part of
	 * the tree table model.
	 * 
	 * @param rootCrumb A bread crumb
	 */
	public void setRootCrumb(FlexiTreeTableNode rootCrumb);
	
	/**
	 * Return the page size
	 * @return
	 */
	public int getPageSize();
	
	public void setPageSize(int pageSize);
	/**
	 * Return the default page size which cannot be changed
	 * by users.
	 * 
	 * @return
	 */
	public int getDefaultPageSize();
	
	public int getPage();
	
	public void setPage(int page);
	
	/**
	 * Utility method to handle next / previous on a data source.
	 * 
	 * @param index The index of the object in the table
	 */
	public void preloadPageOfObjectIndex(int index);
	
	/**
	 *Return the value of the quick search field if it is
	 * visible and enabled.
	 * @return
	 */
	public String getQuickSearchString();
	
	public void quickSearch(UserRequest ureq, String search);
	
	/**
	 * Sort with the specified parameter. A null sort key
	 * will remove the order by.
	 * 
	 * @param sortKey
	 * @param asc
	 */
	public void sort(String sortKey, boolean asc);
	
	/**
	 * Order by the specified setting.
	 * @param sortKey The sort key cannot be null.
	 */
	public void sort(SortKey sortKey);
	
	@Override
	public void reset();
	
	/**
	 * Fine grained reset method for the flexi table.
	 * 
	 * @param page Set the current page of pageing to the firs
	 * @param internal Set the row count and other internal variable to 0
	 * @param reloadData Reload the data
	 */
	public void reset(boolean page, boolean internal, boolean reloadData);
	
	/**
	 * It will reload all the data without filter. Use it with cautious as
	 * at some place, there are minimal restrictions to the search string.
	 * 
	 * @param ureq
	 */
	public void resetSearch(UserRequest ureq);
	
	public void reloadData();

	/**
	 * Set the message displayed when the table is empty and the table header
	 * and table options such as search, sort etc are hidden. If null (default)
	 * the empty table is shown.
	 * 
	 * @param i18key
	 */
	public void setEmptyTableMessageKey(String i18key);

	/**
	 * Configure the empty table screen with custom message, hint text and
	 * background icon and define the next primary user action.
	 * 
	 * @param emtpyMessagei18key        the i18n key used as the main message. If
	 *                                  set to null, the empty screen is disabled
	 *                                  alltogether
	 * @param emptyTableHintKey         the i18n key for an optional hint message to
	 *                                  tell the user what the empty table means and
	 *                                  what to do about it
	 * @param emtpyTableIconCss         the CSS icon class that shows the icon for
	 *                                  the objects listed in the table
	 */
	public void setEmptyTableSettings(String emtpyMessagei18key, String emptyTableHintKey, String emtpyTableIconCss);

	/**
	 * Configure the empty table screen with custom message, hint text and
	 * background icon and define the next primary user action.
	 * 
	 * @param emtpyMessagei18key        the i18n key used as the main message. If
	 *                                  set to null, the empty screen is disabled
	 *                                  alltogether
	 * @param emptyTableHintKey         the i18n key for an optional hint message to
	 *                                  tell the user what the empty table means and
	 *                                  what to do about it
	 * @param emtpyTableIconCss         the CSS icon class that shows the icon for
	 *                                  the objects listed in the table
	 * @param emptyPrimaryActionKey     the i18n key for the button presented as the
	 *                                  primary user action below the empty screen
	 *                                  (optional)
	 * @param emptyPrimaryActionIconCSS the CSS icon class added to the button
	 *                                  (optional)
	 * @param showAlwaysSearchFields    true (default): show the search field,
	 *                                  false: don't show it on empty tables
	 */
	public void setEmptyTableSettings(String emtpyMessagei18key, String emptyTableHintKey, String emtpyTableIconCss, String emptyPrimaryActionKey, String emptyPrimaryActionIconCSS, boolean showAlwaysSearchFields);
	
	/**
	 * Add a button or an other component in the "button grouped"
	 * panel under the table.
	 * 
	 * @param item An acceptable form item
	 */
	public void addBatchButton(FormItem item);
}