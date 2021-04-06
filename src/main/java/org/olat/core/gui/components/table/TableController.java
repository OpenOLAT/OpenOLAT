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

package org.olat.core.gui.components.table;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.ajax.autocompletion.AutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.EmptyChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;

/**
 * <!--**************-->
 * <h3>Responsability:</h3>
 * This controller wraps a table component and offers additional features like
 * column selection. Two constructors are supported: regular table and table
 * with a table filter. Use the TableGuiConfiguration object to configure the
 * various rendering options.
 * <p>
 * <!--**************-->
 * <h3>Events fired:</h3>
 * <ul>
 * <li><i>{@link #EVENT_FILTER_SELECTED}</i>:<br>
 * After succesfully activation of the selected filter. </li>
 * <li><i>{@link #EVENT_NOFILTER_SELECTED}</i>:<br>
 * After deactivation of the last filter.</li>
 * <li><i>{@link org.olat.core.gui.components.table.Table Table component events}</i>:<br>
 * Forwards all events from the table component.</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Change table columns:</i><br>
 * Show a modal dialog for choosing visible/invisible table columns.<br>
 * Save table columns settings in the users preferences.</li>
 * <li><i>Download table content:</i><br>
 * Formats table content as CSV.<br>
 * Creates an asynchronously delivered
 * {@link org.olat.core.gui.media.ExcelMediaResource excel media resource}.</li>
 * <li><i>Apply table filter:</i><br>
 * Activates a defined table filter.<br>
 * Deactivates a table filter. </li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Special translators:</h3>
 * Uses a translator provided in the constructor as <i>fallback</i>.
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3>
 * Opens a modal dialog for choosing which columns to hide or show.
 * <p>
 * 
 * @author Felix Jost, Florian Gn√§gi
 */
public class TableController extends BasicController {
	
	private static final String VC_VAR_USE_NO_FILTER_OPTION = "useNoFilterOption";

	private static final String COMPONENT_TABLE_NAME = "table";

	private static final String VC_VAR_SELECTED_FILTER_VALUE = "selectedFilterValue";

	private static final String LINK_NUMBER_OF_ELEMENTS = "link.numberOfElements";

	private static final String VC_VAR_IS_FILTERED = "isFiltered";

	private static final String VC_VAR_HAS_TABLE_SEARCH = "hasTableSearch";

	private static final Logger log = Tracing.createLoggerFor(TableController.class);
	
	private static final String CMD_FILTER = "cmd.filter.";
	private static final String CMD_FILTER_NOFILTER = "cmd.filter.nofilter";

	/** Event is fired when the 'apply no filter' is selected * */
	public static final Event EVENT_NOFILTER_SELECTED = new Event("nofilter.selected");
	/**
	 * Event is fired when a specific filter is selected. Use getActiveFilter to
	 * retrieve the selected filter
	 */
	public static final Event EVENT_FILTER_SELECTED = new Event("filter.selected");
	/**
	 * Fired when the next primary action is klicked in the emtpy state
	 */
	public static final Event EVENT_EMPTY_TABLE_NEXT_PRIMARY_ACTION = new Event("emtpy.table.next.primary.action");
	

	private VelocityContainer contentVc;

	private Table table;

	private Choice colsChoice;
	private TablePrefs prefs;
	private TableGuiConfiguration tableConfig;

	private List<ShortName> filters;
	private String filterTitle;
	private ShortName activeFilter;

	private boolean tablePrefsInitialized = false;
	private CloseableCalloutWindowController cmc;
	private AutoCompleterController tableSearchController;
	private TableSort tableSort;

	private Link resetLink;
	
	private Link preferenceLink;
	private Link downloadLink;
	private Link tableEmptyNextActionLink;

	
	/**
	 * Constructor for the table controller using the table filter.
	 * 
	 * @param tableConfig The table GUI configuration determines the tables
	 *          behavior, may be <code>null</code> to use default table configuration.
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param filters A list of filter objects ({@link ShortName})
	 * @param activeFilter The initially activated filter object
	 * @param filterTitle The translated title of the filter
	 * @param noFilterOption The translated key for the no-filter filter or
	 *          <code>null</code> if not used
	 * @param enableTableSearch Enable the auto completter for search within the table
	 * @param tableTrans The translator that is used to translate the table
	 */
	public TableController(final TableGuiConfiguration tableConfig, final UserRequest ureq, final WindowControl wControl, final List<ShortName> filters, final ShortName activeFilter,
			final String filterTitle, final String noFilterOption, final boolean enableTableSearch, final Translator tableTrans) {
		// init using regular constructor
		this(tableConfig, ureq, wControl, tableTrans);

		// push filter to velocity page
		this.filterTitle = filterTitle;
		setFilters(filters, activeFilter);
		
		if (noFilterOption != null) {
			contentVc.contextPut("noFilterOption", noFilterOption);
			contentVc.contextPut(VC_VAR_USE_NO_FILTER_OPTION, Boolean.TRUE);
		} else {
			contentVc.contextPut(VC_VAR_USE_NO_FILTER_OPTION, Boolean.FALSE);
		}
		
		if (enableTableSearch) {
			tableSearchController = createTableSearchController(ureq, wControl);
			contentVc.put("tableSearch", tableSearchController.getInitialComponent());
			contentVc.contextPut(VC_VAR_HAS_TABLE_SEARCH, Boolean.TRUE);
		} else {
			contentVc.contextPut(VC_VAR_HAS_TABLE_SEARCH, Boolean.FALSE);
		}
	}

	/**
	 * Constructor for the table controller
	 * 
	 * @param tableConfig The table gui configuration determines the tables
	 *          behaviour, may be <code>null</code> to use default table config.
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param tableTrans The translator that is used to translate the table
	 */
	public TableController(final TableGuiConfiguration tableConfigP, final UserRequest ureq, final WindowControl wControl, final Translator tableTrans) {
		super(ureq, wControl);
		if (tableConfigP == null){
			tableConfig = new TableGuiConfiguration();
		}else{
			tableConfig = tableConfigP;
		}
		
		if (tableTrans != null) {
			setTranslator(Util.createPackageTranslator(TableController.class, ureq.getLocale(), tableTrans));
		}
		
		table = new Table(COMPONENT_TABLE_NAME, getTranslator());
		table.addListener(this);

		// propagate table specific configuration to table,
		// rest of configuration is handled by this controller
		table.setDisplayTableHeader(tableConfig.isDisplayTableHeader());
		table.setSelectedRowUnselectable(tableConfig.isSelectedRowUnselectable());
		table.setSortingEnabled(tableConfig.isSortingEnabled());
		table.setPageingEnabled(tableConfig.isPageingEnabled());
		table.setResultsPerPage(tableConfig.getResultsPerPage());
		table.setMultiSelect(tableConfig.isMultiSelect());
		table.setEnableShowAllLink(tableConfig.isShowAllLinkEnabled());
		table.setDisplayTableGrid(tableConfig.isDisplayTableGrid());
		table.setSuppressDirtyFormWarning(tableConfig.isSuppressDirtyFormWarning());


		// table is embedded in a velocity page that renders the surrounding layout
		contentVc = createVelocityContainer("tablelayout");
		contentVc.put(COMPONENT_TABLE_NAME, table);

		// fetch prefs (which were loaded at login time
		String preferencesKey = tableConfig.getPreferencesKey();
		if (tableConfig.isPreferencesOffered() && preferencesKey != null) {
			prefs = (TablePrefs) ureq.getUserSession().getGuiPreferences().get(TableController.class, preferencesKey);
		}

		// empty table message
		String tableEmptyMessage = tableConfig.getTableEmptyMessage();
		if (tableEmptyMessage == null) {
			tableEmptyMessage = translate("default.tableEmptyMessage");
		}
		contentVc.contextPut("tableEmptyMessage", tableEmptyMessage);
		contentVc.contextPut("tableEmptyHint", tableConfig.getTableEmptyHint());
		contentVc.contextPut("tableEmptyIconCss", tableConfig.getTableEmptyIconCss());
		
		// table empty next primary action link
		if (tableConfig.getTableEmptyPrimaryAction() != null) {
			tableEmptyNextActionLink = LinkFactory.createCustomLink("tableEmptyNextActionLink",
					"tableEmptyNextActionLink", tableConfig.getTableEmptyPrimaryAction(), Link.BUTTON + Link.NONTRANSLATED, contentVc, this);
			tableEmptyNextActionLink.setPrimary(true);
			if (tableConfig.getTableEmptyPrimaryActionIconCss() != null) {
				tableEmptyNextActionLink.setIconLeftCSS("o_icon o_icon-fw " + tableConfig.getTableEmptyPrimaryActionIconCss());
			}
		}		

		contentVc.contextPut("tableConfig", tableConfig);
		contentVc.contextPut(VC_VAR_HAS_TABLE_SEARCH, Boolean.FALSE);
		
		//sorters
		contentVc.contextPut("hasSorters", new Boolean(tableConfig.isSortingEnabled()));
		tableSort = new TableSort("tableSort", table);
		contentVc.put("tableSort", tableSort);
		
		

		//preference + download links
		preferenceLink = LinkFactory.createCustomLink("prefLink", "cmd.changecols", "", Link.BUTTON | Link.NONTRANSLATED, contentVc, this);
		preferenceLink.setIconLeftCSS("o_icon o_icon_customize");
		preferenceLink.setTooltip(translate("command.changecols"));
		
		downloadLink = LinkFactory.createCustomLink("downloadLink", "cmd.download", "", Link.BUTTON | Link.NONTRANSLATED, contentVc, this);
		downloadLink.setTooltip(translate("table.export.title"));
		downloadLink.setIconLeftCSS("o_icon o_icon_download");
		
		putInitialPanel(contentVc);
	}

	public TableController(final TableGuiConfiguration tableConfig, final UserRequest ureq, final WindowControl wControl, final Translator tableTrans,
			final boolean enableTableSearch ) {
		this(tableConfig, ureq, wControl, tableTrans);
		if (enableTableSearch) {
			tableSearchController = createTableSearchController(ureq, wControl);
			contentVc.put("tableSearch", tableSearchController.getInitialComponent());
			contentVc.contextPut(VC_VAR_HAS_TABLE_SEARCH, Boolean.TRUE);
		} else {
			contentVc.contextPut(VC_VAR_HAS_TABLE_SEARCH, Boolean.FALSE);
		}
	}

	private AutoCompleterController createTableSearchController(final UserRequest ureq, final WindowControl wControl) {
		ListProvider genericProvider = new TableListProvider(table);
		removeAsListenerAndDispose(tableSearchController);
		tableSearchController = new AutoCompleterController(ureq, wControl, genericProvider, null, false, 60, 3, translate("table.filter.label"));
		tableSearchController.setEmptyAsReset(true);
		listenTo(tableSearchController);
		return tableSearchController;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == table) {
			String cmd = event.getCommand();
			if(cmd.equalsIgnoreCase(Table.COMMAND_SORTBYCOLUMN)) {
				tableSort.setDirty(true);
			} else if (!cmd.equalsIgnoreCase(Table.COMMAND_SHOW_PAGES)
					&& !cmd.equalsIgnoreCase(Table.COMMAND_PAGEACTION_SHOWALL)) {
				// forward to table controller listener
				fireEvent(ureq, event);
			}
		} else if (source == contentVc) {
			handleCommandsOfTableVcContainer(ureq, event); 
		} else if(source == preferenceLink && tableConfig.getPreferencesKey() != null){
			colsChoice = getColumnListAndTheirVisibility();
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableCalloutWindowController(ureq, getWindowControl(), colsChoice, preferenceLink , translate("title.changecols"), true, "");
			listenTo(cmc);
			cmc.activate();
		} else if(source == downloadLink && tableConfig.isDownloadOffered() ){
			TableExporter tableExporter = tableConfig.getDownloadOffered();
			MediaResource mr = tableExporter.export(table);
			ureq.getDispatchResult().setResultingMediaResource(mr);
		} else if (source == colsChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				//sideeffect on table and prefs
				applyAndcheckChangedColumnsChoice(ureq, colsChoice.getSelectedRows());
			} else if (event == Choice.EVNT_FORM_RESETED) {
				//sideeffect on table and prefs
				List<Integer> visibleCols = table.getDefaultVisibleColumnsToResetColumnsChoice();
				applyAndcheckChangedColumnsChoice(ureq, visibleCols);
			} else { // cancelled
				cmc.deactivate();
			}
		} else if (source == resetLink) {
			table.setSearchString(null);
			modelChanged();
		} else if (source == tableEmptyNextActionLink) {
			fireEvent(ureq, EVENT_EMPTY_TABLE_NEXT_PRIMARY_ACTION);
		}
	}

	private void handleCommandsOfTableVcContainer(final UserRequest ureq,	final Event event) {
		// links of this vc container coming in
		String cmd = event.getCommand();
		if (cmd.equals(CMD_FILTER_NOFILTER)) {
			// update new filter value
			setActiveFilter(null);
			fireEvent(ureq, EVENT_NOFILTER_SELECTED);
		} else if (cmd.indexOf(CMD_FILTER) == 0) {
			String areafilter = cmd.substring(CMD_FILTER.length());
			int filterPosition = Integer.parseInt(areafilter);
			// security check
			if (filters.size() < (filterPosition + 1)){
				throw new AssertException("Filter size was ::" + filters.size() + " but requested filter was ::" + filterPosition);
			}
			// update new filter value
			setActiveFilter(filters.get(filterPosition));
			fireEvent(ureq, EVENT_FILTER_SELECTED);
		}
	}

	private void applyAndcheckChangedColumnsChoice(final UserRequest ureq, List<Integer> selRows) {
		if (selRows.size() == 0) {
			showError("error.selectatleastonecolumn");
		} else {
			// check that there is at least one data column (because of sorting
			// (technical) and information (usability))
			if (table.isSortableColumnIn(selRows)) {
				// ok
				table.updateConfiguredRows(selRows);
				// update user preferences, use the given preferences key
				if (prefs == null){
					prefs = new TablePrefs();
				}
				prefs.setActiveColumnsRef(selRows);
				ureq.getUserSession().getGuiPreferences().putAndSave(TableController.class, tableConfig.getPreferencesKey(), prefs);
				// pop configuration dialog
				cmc.deactivate();
			} else {
				showError("error.atleastonedatacolumn");
			}
		}
	}

	private Choice getColumnListAndTheirVisibility() {
		Choice choice = new Choice("colchoice", getTranslator());
		choice.setModel(table.createChoiceModel());
		choice.addListener(this);
		choice.setEscapeHtml(false);
		choice.setCancelKey("cancel");
		choice.setResetKey("reset");
		choice.setSubmitKey("save");
		choice.setElementCssClass("o_table_config");
		return choice;
	}

	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		log.debug("dispatchEvent event=" + event + "  source=" + source);
		if (event instanceof EntriesChosenEvent) {
			EntriesChosenEvent ece = (EntriesChosenEvent)event;				
			List<String> filterList = ece.getEntries();
			if (!filterList.isEmpty()) {
				table.setSearchString(filterList.get(0));
				modelChanged(false);
			}	else {
			  // reset filter search filter in modelChanged
				modelChanged();
			}
		}  else if(event instanceof EmptyChosenEvent) {
			modelChanged(true);
		}
	}
	
	public int getRowCount() {
		return table.getRowCount();
	}
	
	public int getSortedRow(int originalRow) {
		return table.getSortedRow(originalRow);
	}
	
	/**
	 * Return the object at the visible index (sorted or not, searched or not)
	 * @param index
	 * @return
	 */
	public Object getSortedObjectAt(int sortedRow) {
		int row = table.getSortedRow(sortedRow);
		return getTableDataModel().getObject(row);
	}
	
	/**
	 * Return the visible index of the object (sorted or not, searched or not)
	 * @param obj
	 * @return
	 */
	public int getIndexOfSortedObject(Object obj) {
		int index = -1;
		for(int i=getTableDataModel().getRowCount(); i-->0; ) {
			if(obj.equals(getTableDataModel().getObject(i))) {
				index = i;
				break;
			}
		}

		for(int i=0; i<getRowCount(); i++) {
			int currentPos = getSortedRow(i);
			if(currentPos == index) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @return The currently active filter object or <code>null</code> if no
	 *         filter is applied
	 */
	public ShortName getActiveFilter() {
		return activeFilter;
	}

	/**
	 * @param activeFilter The currently applied filter or <code>null</code> if
	 *          no filter is applied
	 */
	public void setActiveFilter(final ShortName activeFilter) {
		this.activeFilter = activeFilter;
		if (activeFilter == null) {
			contentVc.contextPut(VC_VAR_SELECTED_FILTER_VALUE, CMD_FILTER_NOFILTER);
		} else {
			contentVc.contextPut(VC_VAR_SELECTED_FILTER_VALUE, activeFilter);
		}
	}

	/**
	 * Sets the list of filters and the currently active filter
	 * 
	 * @param filters List of TableFilter
	 * @param activeFilter active TableFilter
	 */
	public void setFilters(final List<ShortName> filters, final ShortName activeFilter) {
		this.filters = filters;
		contentVc.contextPut("hasFilters", filters == null ? Boolean.FALSE : Boolean.TRUE);
		contentVc.contextPut("filters", filters);
		contentVc.contextPut("filterTitle", filterTitle == null ? "" : filterTitle);
		setActiveFilter(activeFilter);
	}

	public void modelChanged() {
		modelChanged(true);
	}

	/**
	 * Notifies the controller about a changed table data model. This will check
	 * if the table data model has any values and show a message instead of the
	 * table when the model has no rows.
	 */
	public void modelChanged(final boolean resetSearchString) {
		if (resetSearchString) {
			table.setSearchString(null);
		}
		table.modelChanged();
		TableDataModel<?> tableModel = table.getTableDataModel();
		if (tableModel != null) {
			contentVc.contextPut("tableEmpty", tableModel.getRowCount() == 0 ? Boolean.TRUE : Boolean.FALSE);
			contentVc.contextPut("numberOfElements", String.valueOf(table.getUnfilteredRowCount()));
			contentVc.contextPut("rowCounts", String.valueOf(table.getRowCount()));
			if (table.isTableFiltered()) {
				contentVc.contextPut("numberFilteredElements", String.valueOf(table.getRowCount()));
				contentVc.contextPut(VC_VAR_IS_FILTERED, Boolean.TRUE); 
				contentVc.contextPut("filter", table.getSearchString());
				resetLink = LinkFactory.createCustomLink(LINK_NUMBER_OF_ELEMENTS, LINK_NUMBER_OF_ELEMENTS, String.valueOf(table.getUnfilteredRowCount()), Link.NONTRANSLATED, contentVc, this);
			} else {
				contentVc.contextPut(VC_VAR_IS_FILTERED, Boolean.FALSE); 
			}
		}
		// else do nothing. The table might have no table data model during
		// constructing time of
		// this controller.
	}

	/**
	 * Sets the tableDataModel. IMPORTANT: Once a tableDataModel is set, it is
	 * assumed to remain constant in its data & row & colcount. Otherwise a
	 * modelChanged has to be called
	 * 
	 * @param tableDataModel The tableDataModel to set
	 */
	public void setTableDataModel(final TableDataModel tableDataModel) {
		table.setTableDataModel(tableDataModel);
		if (!tablePrefsInitialized) { // first time
			if (prefs != null) {
				try {
					List<Integer> acolRefs = prefs.getActiveColumnsRef();
					table.updateConfiguredRows(acolRefs);
				} catch(IndexOutOfBoundsException ex) {
					// GUI prefs match not to table data model => reset prefs
					prefs = null;
				}
			}
			tablePrefsInitialized = true;
		}
		modelChanged();
	}

	/**
	 * Add a table column descriptor
	 * 
	 * @param visible true: is visible; false: is not visible
	 * @param cd column descriptor
	 */
	public void addColumnDescriptor(final boolean visible, final ColumnDescriptor cd) {
		table.addColumnDescriptor(cd, -1, visible);
	}

	/**
	 * Add a visible table column descriptor
	 * 
	 * @param cd column descriptor
	 */
	public void addColumnDescriptor(final ColumnDescriptor cd) {
		table.addColumnDescriptor(cd, -1, true);
	}
	
	/**
	 * Get the table column descriptor.
	 * @param row
	 * @return ColumnDescriptor
	 */
	public ColumnDescriptor getColumnDescriptor(final int row) {
		return table.getColumnDescriptor(row);
	}

	/**
	 * Get the current table data model from the table
	 * 
	 * @return TableDataModel
	 */
	public TableDataModel getTableDataModel() {
		return table.getTableDataModel();
	}
	
	/**
	 * Sorts the selected table row indexes according with the table Comparator,
	 * and then retrieves the rows from the input defaultTableDataModel.
	 * It is assumed that the defaultTableDataModel IS THE MODEL for the table.
	 * @param objectMarkers
	 * @return the List with the sorted selected objects in this table.
	 */
	public List getSelectedSortedObjects(final BitSet objectMarkers, final DefaultTableDataModel defaultTableDataModel) {		
		List results = new ArrayList();
		List<Integer> sortedIndexes = new ArrayList<>();
		if(objectMarkers.isEmpty()) {
			sortedIndexes.clear();
		}
		for (int i = objectMarkers.nextSetBit(0); i >= 0; i = objectMarkers.nextSetBit(i + 1)) {
			sortedIndexes.add(i);
		}
		Collections.sort(sortedIndexes, table.getComparator());
		Iterator<Integer> indexesIterator = sortedIndexes.iterator();
		while (indexesIterator.hasNext()) {
			results.add(defaultTableDataModel.getObject(indexesIterator.next()));
		}
		return results;
	}
	
	public List getObjects(final BitSet objectMarkers) {
		List results = new ArrayList();
		for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
			results.add(getTableDataModel().getObject(i));
		}
		return results;
	}

	/**
	 * Sets the selectedRowId to a specific row id. Make sure that this is valid,
	 * the table does not check for out of bound exception.
	 * 
	 * @param selectedRowId The selectedRowId to set
	 */
	public void setSelectedRowId(final int selectedRowId) {
		table.setSelectedRowId(selectedRowId);
	}
	
	/**
	 * Set the page viewed if pageing is enabled
	 * @param pageNr
	 */
	public void setPage(Integer pageNr) {
		table.updatePageing(pageNr);
	}
	
	/**
	 * Return the number of items per page if pageing is enable
	 * @return
	 */
	public int getPageSize() {
		return table.getResultsPerPage();
	}

	/**
	 * Sets the sortColumn to a specific colun id. Check if the column can be accessed 
	 * and if it is sortable.
	 * 
	 * @param sortColumn The sortColumn to set
	 * @param isSortAscending true: sorting is ascending
	 */
	public void setSortColumn(final int sortColumn, final boolean isSortAscending) {
		if ((table.getColumnCount() > sortColumn)
				&& table.getColumnDescriptor(sortColumn).isSortingAllowed()) {
			table.setSortColumn(sortColumn, isSortAscending);
			table.resort();
			tableSort.setDirty(true);
		}
	}
	
	public void setSortColumn(final ColumnDescriptor sortColumn, final boolean isSortAscending) {
		for(int i=table.getColumnCount(); i-->0; ) {
			if(sortColumn == table.getColumnDescriptor(i)) {
				table.setSortColumn(i, isSortAscending);
				table.resort();
				tableSort.setDirty(true);
			}
		}
	}

	/**
	 * Sets whether user is able to select multiple rows via checkboxes.
	 * 
	 * @param isMultiSelect
	 */
	public void setMultiSelect(final boolean isMultiSelect) {
		table.setMultiSelect(isMultiSelect);
	}
	
	/**
	 * Make the multi select as disabled box and remove the select all / deselect all
	 * @param disable
	 */
	public void setMultiSelectAsDisabled(boolean disabled) {
		table.setMultiSelectAsDisabled(disabled);
	}
	
	public void setMultiSelectSelectedAt(final int row, final boolean selected) {
		table.setMultiSelectSelectedAt(row, selected);
	}
	
	public void setMultiSelectReadonlyAt(final int row, final boolean readonly) {
		table.setMultiSelectReadonlyAt(row, readonly);
	}
	
	/**
	 * Add a multiselect action.
	 * 
	 * @param actionKeyi18n The i18n key to translate
	 * @param actionIdentifier
	 */
	public void addMultiSelectAction(final String actionKeyi18n, final String actionIdentifier) {
		table.addMultiSelectAction(null, actionKeyi18n, actionIdentifier);
	}
	
	/**
	 * Add a multiselect action with an already translated label
	 * @param label The label
	 * @param actionIdentifier
	 */
	public void addLabeledMultiSelectAction(final String label, final String actionIdentifier) {
		table.addMultiSelectAction(label, null, actionIdentifier);
	}
	
	public int getTableSortCol() {
		return table.getSortColumn();
	}
	public boolean getTableSortAsc() {
		return table.getSortAscending();
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
