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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.choice.ChoiceModel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;

/**
 * Description: <br>
 * a table. 1.) set column descriptors 2.) generate a tabledatamodel with valid
 * datas 3.) do table.setTableDataModel(tdm); this inits and sorts the table
 * 
 * @author Felix Jost
 */
public class Table extends AbstractComponent {
	
	private static final int NO_ROW_SELECTED = -1;
	private static final int DEFAULT_RESULTS_PER_PAGE = 20;
	private static final int INITIAL_COLUMNSIZE = 5;
	private static final Logger log = Tracing.createLoggerFor(Table.class);
	private static final ComponentRenderer RENDERER = new TableRenderer();
	
	/**
	 * TableMultiSelectEvent command identifier.
	 */
	public static final String COMMAND_MULTISELECT = "ms";
	
	// The following two commands will be submitted traditional style via URLBuilder URIS.
	/**
	 * Table row selection.
	 */
	public static final String COMMANDLINK_ROWACTION_CLICKED = "r";
	/**
	 * Comment for <code>COMMAND_ROWACTION_CLICKED_ROWID</code>
	 */
	public static final String COMMANDLINK_ROWACTION_ID = "p";
	
	// The following commands will be submitted via hidden form parameters.
	// The commands are internal to the table and affect functionality such as sorting and pageing.
	
	// The two ids formCmd and formParam are the hidden fields used by the form to submit the relevant actions.
	protected static final String FORM_CMD = "cmd";
	protected static final String FORM_PARAM = "param";
	
	/**
	 * Comment for <code>COMMAND_SORTBYCOLUMN</code>
	 */
	protected static final String COMMAND_SORTBYCOLUMN = "cid";
	/**
	 * Comment for <code>COMMAND_PAGEACTION</code>
	 */
	protected static final String COMMAND_PAGEACTION = "pg";
	/**
	 * Comment for <code>COMMAND_PAGEACTION_SHOWALL</code>
	 */
	protected static final String COMMAND_PAGEACTION_SHOWALL = "a";
	/**
	 * Comment for <code>COMMAND_PAGEACTION_FORWARD</code>
	 */
	protected static final String COMMAND_PAGEACTION_FORWARD = "f";
	/**
	 * Comment for <code>COMMAND_PAGEACTION_BACKWARD</code>
	 */
	protected static final String COMMAND_PAGEACTION_BACKWARD = "b";
	
	protected static final String COMMAND_SHOW_PAGES = "s_p";


	// order of left-to-right presentation of Columns (visible columndescriptors):
	// list of columndescriptors
	private List<ColumnDescriptor> columnOrder; // default visibility to improve speed, not private

	// all column descriptors whether visible or not
	private List<ColumnDescriptor> allCDs; // default visibility to improve speed, not private
	private List<ColumnDescriptor> defaultVisibleCDs;

	private TableDataModel tableDataModel;
	// DO NOT REFERENCE filteredTableDataModel directly, use always getFilteredTableDataModel() because lazy init!
	private TableDataModel filteredTableDataModel;
	private List<Integer> sorter;

	private int sortColumn = 0;
	private boolean sortAscending = true;

	// config
	private boolean multiSelect = false;
	private boolean multiSelectDisabled = false;
	private boolean selectedRowUnselectable = false;
	private boolean sortingEnabled = true;
	private boolean displayTableHeader = true;
	private boolean displayTableGrid = false; // default
	private boolean pageingEnabled = true;
	private Integer currentPageId;
	private int resultsPerPage;
	private boolean isShowAllSelected;
	private boolean suppressDirtyFormWarning;

	private List<TableMultiSelect> multiSelectActions = new ArrayList<>();
	private BitSet multiSelectSelectedRows = new BitSet();
	private BitSet multiSelectReadonlyRows = new BitSet();

	int selectedRowId;

	private boolean enableShowAllLinkValue = true;
	private String tableSearchString;

	/**
	 * Constructor for a table. The table is preconfigured with the following
	 * options: downloadOffered=true, sortingEnabled=true,
	 * columnMovingOffered=true, displayTableHeader=true, displayRowCount=true
	 * 
	 * @param name
	 */
	protected Table(String name, Translator translator) {
		this(null, name, translator);
	}
		
	protected Table(String id, String name, Translator translator) {
		super(id, name, translator);
		columnOrder = new ArrayList<>(INITIAL_COLUMNSIZE);
		allCDs = new ArrayList<>(INITIAL_COLUMNSIZE);
		defaultVisibleCDs = new ArrayList<>(INITIAL_COLUMNSIZE);
		sorter = new ArrayList<>(DEFAULT_RESULTS_PER_PAGE);
		selectedRowId = NO_ROW_SELECTED;
		currentPageId = Integer.valueOf(1);
		resultsPerPage = DEFAULT_RESULTS_PER_PAGE;
	}

	/**
	 * @param column
	 * @return Column descriptor of given column
	 */
	protected ColumnDescriptor getColumnDescriptor(final int column) {
		return columnOrder.get(column);
	}

	protected ColumnDescriptor getColumnDescriptorFromAllCDs(final int column) {
		return allCDs.get(column);
	}
	
	protected int getColumnCountFromAllCDs() {
		return allCDs.size();
	}

	/**
	 * @return Column descriptor of currently sorted column
	 */
	protected ColumnDescriptor getCurrentlySortedColumnDescriptor() {
		if(columnOrder.size() >= 0 && sortColumn < columnOrder.size()) {
			return getColumnDescriptor(sortColumn);
		}
		return null;
	}

	/**
	 * 
	 */
	protected void modelChanged() {
		// we got a new TableDataModel, so we need to prepare the sorting
		int rows = getRowCount();
		selectedRowId = NO_ROW_SELECTED; // no selection anymore
		sorter = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			sorter.add(Integer.valueOf(i));
		}
		// notify all ColumnDescriptors so that they get a chance to presort/cache
		// sorting before
		// the comparator calls start
		int cdcnt = getColumnCount();
		for (int i = 0; i < cdcnt; i++) {
			ColumnDescriptor cd = getColumnDescriptor(i);
			cd.modelChanged();
		}
		// now sort
		resort();
		updatePageing(getCurrentPageId());
		// do not reset pageing to first page when in multiselect - else cannot keep the page selection when item selected (see OLAT-1340)
		if (pageingEnabled && !isMultiSelect()) {
			currentPageId = Integer.valueOf(1);
		}
		// Reset multi selected rows
		// Best would be to remove the unnecessary bits, but how can we know if 
		// a row has been added or deleted? Most save action is to clear the selections
		// and start fresh. But we will loose the selections this way.
		// Any better ideas?
		multiSelectSelectedRows = new BitSet();
	}

	/**
	 * serves a purpose: it maps from the rowid in the gui (first row = 0, second =
	 * 1 and so on) to the corresponding row in the tabledatamodel
	 * getSortedRow(guirow) is used by the columnDescriptors: public String
	 * getRenderValue(int row).. to determine the row in the model they have to
	 * return and the tablerenderer
	 * 
	 * @param originalRow
	 * @return integer representing the row id after sorting
	 */
	public int getSortedRow(final int originalRow) {
		Integer i = sorter.get(originalRow);
		return i.intValue();
	}
	
	public int getOriginalIndex(final int sortedRow) {
		int count = 0;
		for(Integer row:sorter) {
			if(row.intValue() == sortedRow) {
				return count;
			}
			count++;
		}
		return -1;
	}

	/**
	 * @return integer representing the number of columns in the table
	 */
	protected int getColumnCount() {
		return columnOrder.size();
	}

	/**
	 * @return integer representing the number of rows in the table
	 */
	protected int getRowCount() {
		if (isTableFiltered()) {
			return getFilteredTableDataModel().getRowCount();
		} else {
			return tableDataModel.getRowCount();
		}
	}

	/**
	 * @param position The position of the column descriptor. Set to -1 to add this CD at the end.
	 * @param visible
	 * @param cd
	 */
	protected void addColumnDescriptor(final ColumnDescriptor cd, final int position, final boolean visible) {
		cd.setTable(this);
		if (position != NO_ROW_SELECTED){
			allCDs.add(position, cd);
		} else {
			allCDs.add(cd);
		}
		if (visible) {
			if (position != NO_ROW_SELECTED){
				columnOrder.add(position, cd);
			} else {
				columnOrder.add(cd);
			}
			defaultVisibleCDs.add(cd);
		}
	}

	/**
	 * @param cd
	 */
	protected void addColumnDescriptor(final ColumnDescriptor cd) {
		addColumnDescriptor(cd, -1, true);
	}

	/**
	 * Remove a column descriptor.
	 * 
	 * @param position
	 */
	protected void removeColumnDescriptor(final int position) {
		allCDs.remove(position);
		columnOrder.remove(position);
		if (sortColumn >= allCDs.size()){
			sortColumn = 0;
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(final UserRequest ureq) {
		String formCmd = ureq.getParameter(FORM_CMD);
		String formParam = ureq.getParameter(FORM_PARAM);
		String rowAction = ureq.getParameter(COMMANDLINK_ROWACTION_CLICKED);
		String multiAction = ureq.getParameter("multi_action_identifier");

		if (formCmd != null && formCmd.length() > 0) {
			// this is an internal command submitted by a form-submit()
			// first update the multiselect state
			updateMultiSelectState(ureq);
			// then fetch the internal command to be processed
			if (formCmd.equals(COMMAND_SORTBYCOLUMN)) {
				doSort(ureq, formParam);
			} else if (formCmd.equals(COMMAND_PAGEACTION)) {
				doAction(ureq, formParam);
			}
		} else if (rowAction != null) {
			// this is a row action clicked by the user. no form is submitted, so we don't evaluate any columns.

			String actionId = ureq.getParameter(COMMANDLINK_ROWACTION_ID);
			// sanity check
			int rowid = Integer.parseInt(rowAction);
			int actualrows = getTableDataModel().getRowCount();
			if (rowid < 0 || rowid >= actualrows){
				setDirty(true);
				return;
			}
			
			selectedRowId = rowid;
			//setDirty(true); commented as timestamp was consumed in AJAX mode: see OLAT-2007
			// create and add replay event
			fireEvent(ureq, new TableEvent(COMMANDLINK_ROWACTION_CLICKED, selectedRowId, actionId));
		} else if(StringHelper.containsNonWhitespace(multiAction)) {
			// check for multiselect actions
			for (TableMultiSelect action: multiSelectActions) {
				String actionIdentifier = action.getAction();
				if (multiAction.equals(actionIdentifier)) {
					// get the multiselect command
					// update multiselect state
					updateMultiSelectState(ureq);
					
					setDirty(true);
					fireEvent(ureq, new TableMultiSelectEvent(COMMAND_MULTISELECT, actionIdentifier, getMultiSelectSelectedRows()));
					break;
				}
			}
		}
	}
	
	private void doSort(UserRequest ureq, String value) {
		// if sorting command, resort
		int oldSortColumn = sortColumn;
		sortColumn = Integer.parseInt(value);
		if (oldSortColumn == sortColumn) { // click the same column again, change
			// sort order
			sortAscending = !sortAscending;
		} else { // new column, always sort ascending first
			sortAscending = true;
		}

		setDirty(true);
		resort();
		fireEvent(ureq, new TableEvent(COMMAND_SORTBYCOLUMN, -1, COMMAND_SORTBYCOLUMN));
	}
	
	private void doAction(UserRequest ureq, String value) {
		if (value.equals(COMMAND_PAGEACTION_SHOWALL)) {
			//updatePageing(null);	(see OLAT-1340)			
			setShowAllSelected(true);
			fireEvent(ureq, new Event(COMMAND_PAGEACTION_SHOWALL));
			setDirty(true);
		} else if (value.equals(COMMAND_PAGEACTION_FORWARD)) {
			if (currentPageId != null) {
				updatePageing(Integer.valueOf(currentPageId.intValue() + 1));
				setDirty(true);
			}
		} else if (value.equals(COMMAND_PAGEACTION_BACKWARD)) {
			if (currentPageId != null) {
				updatePageing(Integer.valueOf(currentPageId.intValue() - 1));
				setDirty(true);
			}				
		} else if (value.equals(COMMAND_SHOW_PAGES)) {
			setShowAllSelected(false);
			fireEvent(ureq, new Event(COMMAND_SHOW_PAGES));
			if (currentPageId != null) {
				updatePageing(Integer.valueOf(currentPageId.intValue()));					
			}else {
				updatePageing(Integer.valueOf(1));
			}
			setDirty(true);				
		} else {
			updatePageing(Integer.valueOf(value));
			setDirty(true);
		}
	}

	/**
	 * Updates the state of multi selects in the table.
	 * The state is saved in a BitSet (each bit representing a
	 * column within the tablemodel.
	 *
	 */
	private void updateMultiSelectState(final UserRequest ureq) {
		String[] sRowIds = ureq.getHttpReq().getParameterValues(TableRenderer.TABLE_MULTISELECT_GROUP);
		if (sRowIds == null) {
			multiSelectSelectedRows = new BitSet(); //if all deselected create new multiSelectSelectedRows
			return;
		}
		List<Integer> rowIds = new ArrayList<>();
		for (int i = 0; i < sRowIds.length; i++) {
			String sRowId = sRowIds[i];
			try {
				rowIds.add(Integer.valueOf(sRowId));
			} catch (NumberFormatException nfe) {
				throw new OLATRuntimeException("Invalid rowID submitted as table multiselect parameter", nfe);
			}
		}
		
		int rows = getRowCount();
		int startRowId = 0;
		int endRowId = rows;
		// initalize pageing
		if (isPageingEnabled() && currentPageId != null && !isShowAllSelected()) {
			startRowId = ((currentPageId.intValue() - 1) * resultsPerPage);
			endRowId = startRowId + resultsPerPage;
			if (endRowId > rows){
				endRowId = rows;
			}
		} else {
			startRowId = 0;
			endRowId = rows;
		}

		// walk through all the rows and see if the row is selected this time.
		// we need this because user might have unchecked a row.
		for (int i = startRowId; i < endRowId; i++) {
			Integer sortedRow = Integer.valueOf(getSortedRow(i));
			if (rowIds.contains(sortedRow)) {
				rowIds.remove(sortedRow);
				multiSelectSelectedRows.set(sortedRow.intValue());
			} else {
				multiSelectSelectedRows.clear(sortedRow.intValue());
			}
		}
	}
	

	/**
	 * Sets the tableDataModel. IMPORTANT: Once a tableDataModel is set, it is
	 * assumed to remain constant in its data & row & colcount. Otherwise a
	 * modelChanged has to be called
	 * 
	 * @param tableDataModel The tableDataModel to set
	 */
	protected void setTableDataModel(final TableDataModel tableDataModel) {
		this.tableDataModel = tableDataModel;
		this.filteredTableDataModel = null; // lazy init
		// modelChanged(); now called from the controller
	}

	/**
	 * @return TableDataModel
	 */
	public TableDataModel getTableDataModel() {
		if (isTableFiltered()) {
			return getFilteredTableDataModel();
		} else {
			return tableDataModel;
		}
	}

	/**
	 * @return filtered TableDataModel
	 */
	public TableDataModel getUnfilteredTableDataModel() {
		return tableDataModel;
	}

	/**
	 * @return filtered TableDataModel
	 */
	public TableDataModel getFilteredTableDataModel() {
		if (filteredTableDataModel == null) {
			this.filteredTableDataModel = (TableDataModel)tableDataModel.createCopyWithEmptyList();
		}
		return filteredTableDataModel;
	}

	protected void resort() {
		if (isSortingEnabled() && sortColumn < getColumnCount()) {
			ColumnDescriptor currentSortingCd = getColumnDescriptor(sortColumn); // we sort after this
			if(currentSortingCd != null && currentSortingCd.isSortingAllowed()) {
				// column descriptor
				// notify all nonactive ColumnDescriptors about their state
				int cdcnt = getColumnCount();
				for (int i = 0; i < cdcnt; i++) {
					ColumnDescriptor cd = getColumnDescriptor(i);
					if (cd != currentSortingCd){
						cd.otherColumnDescriptorSorted();
					}
				}
				currentSortingCd.sortingAboutToStart();
				try {
					Collections.sort(sorter, new TableComparator(currentSortingCd, sortAscending));
				} catch (IllegalArgumentException e) {
					log.error("Sort column: {} in columns: {}", sortColumn, columnOrder, e);
				}
			} else {
				log.error("Sort column not found:" + sortColumn + " in columns: " + columnOrder);
			}
		}
	}
	
	public TableComparator getComparator() {
		ColumnDescriptor currentSortingCd = getColumnDescriptor(sortColumn);
		return new TableComparator(currentSortingCd, sortAscending);
	}

	/**
	 * * used by renderer only
	 * 
	 * @return boolean
	 */
	public boolean isSortAscending() {
		return sortAscending;
	}

	/**
	 * Sets the sortColumn.
	 * 
	 * @param sortColumn The sortColumn to set
	 * @param isSortAscending true: sorting ascending order
	 */
	protected void setSortColumn(final int sortColumn, final boolean isSortAscending) {
		this.sortColumn = sortColumn;
		this.sortAscending = isSortAscending;
	}

	/**
	 * @return boolean
	 */
	protected boolean isSelectedRowUnselectable() {
		return selectedRowUnselectable;
	}

	/**
	 * Sets the selectedRowUnselectable.
	 * 
	 * @param selectedRowUnselectable The selectedRowUnselectable to set
	 */
	protected void setSelectedRowUnselectable(final boolean selectedRowUnselectable) {
		this.selectedRowUnselectable = selectedRowUnselectable;
	}

	/**
	 * @return int
	 */
	protected int getSelectedRowId() {
		return selectedRowId;
	}

	/**
	 * Sets the selectedRowId.
	 * 
	 * @param selectedRowId The selectedRowId to set
	 */
	protected void setSelectedRowId(final int selectedRowId) {
		this.selectedRowId = selectedRowId;
	}

	/**
	 * @return true when data can be and should be sorted, false for data that is
	 *         not sortable
	 */
	public boolean isSortingEnabled() {
		return sortingEnabled;
	}

	/**
	 * Set table configuration: should table be sortable and be sorted when adding
	 * a new table data model?
	 * 
	 * @param sortingEnabled true: allow table sorting, false: never sort table,
	 *          not even when adding a new table data model.
	 */
	protected void setSortingEnabled(final boolean sortingEnabled) {
		this.sortingEnabled = sortingEnabled;
	}

	/**
	 * @return true: table will render header, false: table has no headers
	 */
	protected boolean isDisplayTableHeader() {
		return displayTableHeader;
	}

	/**
	 * Set the table header configuration
	 * 
	 * @param displayTableHeader
	 */
	protected void setDisplayTableHeader(final boolean displayTableHeader) {
		this.displayTableHeader = displayTableHeader;
	}

	/**
	 * Option to display a table grid
	 * @param enabled true: show the table grid; false: don't show table grid (default)
	 */
	protected void setDisplayTableGrid(boolean enabled) {
		this.displayTableGrid = enabled;		
	}
	/**
	 * @return true: show the table grid; false: don't show table grid (default)
	 */
	protected boolean isDisplayTableGrid() {
		return this.displayTableGrid;
	}

	/**
	 * @param cd
	 * @return true if the columndescriptor is visible
	 */
	protected boolean isColumnDescriptorVisible(final ColumnDescriptor cd) {
		return columnOrder.contains(cd);
	}

	/**
	 * @return a tabledatamodel for a choice
	 */
	protected ChoiceModel createChoiceModel() {
		return new ChoiceTableDataModel(isMultiSelect(), allCDs, columnOrder, getTranslator());
	}
	
	/**
	 * Only use this for reset as it reorder the columns, remove the multi-select column
	 * and so on...
	 * 
	 * @return The list of index (without multi select) and reorder to begin with 0
	 */
	protected List<Integer> getDefaultVisibleColumnsToResetColumnsChoice() {
		List<Integer> indexList = new ArrayList<>();
		for(ColumnDescriptor defaultVisibleCD:defaultVisibleCDs) {
			if(defaultVisibleCD instanceof MultiSelectColumnDescriptor) continue;
			
			int index = allCDs.indexOf(defaultVisibleCD);
			if(isMultiSelect()) {
				if(index > 0) {
					indexList.add(index - 1);
				}
			} else {
				indexList.add(index);
			}
		}
		return indexList;
	}

	/**
	 * @param selRows
	 */
	protected void updateConfiguredRows(final List<Integer> selRows) {
		setDirty(true);
		columnOrder.clear();
		for (Iterator<Integer> itSel = selRows.iterator(); itSel.hasNext();) {
			int pos = itSel.next().intValue();
			// if multiselect, skip the first cd (which is the multiselect CD)
			if (isMultiSelect()){
				pos += 1;
			}
			columnOrder.add(allCDs.get(pos));
		}
		// make sure sorting is smooth in all cases
		if (isMultiSelect()) {
			// if multiselect, add the multiselect CD at the beginning
			MultiSelectColumnDescriptor mscd = new MultiSelectColumnDescriptor();
			mscd.setTable(this);
			columnOrder.add(0, mscd);
			setSortColumn(1, isSortAscending());
		} else {
			setSortColumn(0, isSortAscending());
		}
		resort();
	}

	/**
	 * @param selRows
	 * @return true if there is at least one sortable row in the list of all
	 *         columndescriptors (both visible and invisible)
	 */
	public boolean isSortableColumnIn(final List<Integer> selRows) {
		for (Iterator<Integer> itSelRows = selRows.iterator(); itSelRows.hasNext();) {
			Integer posI = itSelRows.next();
			ColumnDescriptor cd = allCDs.get(posI.intValue());
			if (cd.isSortingAllowed()){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param newPageId new id used as active page
	 */
	protected void updatePageing(final Integer newPageId) {
		if (newPageId == null) {
			this.currentPageId = null;
		} else {
			if (newPageId.intValue() < 1) {
				this.currentPageId = Integer.valueOf(1);
			} else {
				this.currentPageId = newPageId;
				if(tableDataModel!=null) {
					int maxPageNumber = (tableDataModel.getRowCount()/getResultsPerPage());					
					if(tableDataModel.getRowCount()%getResultsPerPage() > 0) {
						maxPageNumber++;	
					}
					while(currentPageId>maxPageNumber && currentPageId>1)	{
						currentPageId--; 
					}
				}
			}
		}
	}

	/**
	 * @param enabledFlag
	 */
	protected void setPageingEnabled(final boolean enabledFlag) {
		this.pageingEnabled = enabledFlag;
	}

	/**
	 * @return boolean
	 */
	protected boolean isPageingEnabled() {
		return pageingEnabled;
	}

	/**
	 * @return Integer current page position
	 */
	protected Integer getCurrentPageId() {				
		return currentPageId;
	}

	/**
	 * @return int number of results per page
	 */
	protected int getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * @param resultsPerPage number of results per page
	 */
	protected void setResultsPerPage(final int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	protected boolean isMultiSelect() {
		return multiSelect;
	}

	protected void setMultiSelect(final boolean multiSelect) {
		if (!this.multiSelect && multiSelect) {
			// state change: from non-multiselect to multiselect: add extra checkbox column
			// add a CD to render the checkboxes
			addColumnDescriptor(new MultiSelectColumnDescriptor(), 0, true);
			// adjust sort column
			setSortColumn(sortColumn + 1, isSortAscending());
		} else if (this.multiSelect && !multiSelect) {
			// state change: from multiselect to non-multiselect: remove extra checkbox column
			// add a CD to render the checkboxes
			removeColumnDescriptor(0);
			// adjust sort column
			setSortColumn(sortColumn - 1, isSortAscending());
		}
		// only update after state change checks (see above) are through
		this.multiSelect = multiSelect;
	}
	
	public boolean isMultiSelectAsDisabled() {
		return multiSelectDisabled;
	}
	
	public void setMultiSelectAsDisabled(boolean disabled) {
		multiSelectDisabled = disabled;
	}

	protected void addMultiSelectAction(String label, String actionKeyi18n, String actionIdentifier) {
		multiSelectActions.add(new TableMultiSelect(label, actionKeyi18n, actionIdentifier));
	}

	protected List<TableMultiSelect> getMultiSelectActions() {
		return multiSelectActions;
	}

	protected BitSet getMultiSelectSelectedRows() {
		return multiSelectSelectedRows;
	}

	public boolean isShowAllSelected() {
		return isShowAllSelected;
	}

	public void setShowAllSelected(final boolean isShowAllSelected) {
		this.isShowAllSelected = isShowAllSelected;
	}
	
	public void setEnableShowAllLink(final boolean enableShowAllLinkValue) {
		this.enableShowAllLinkValue  = enableShowAllLinkValue;
	}
	
	public boolean isShowAllLinkEnabled() {
		return enableShowAllLinkValue;
	}
	
	protected void setMultiSelectSelectedAt(final int row, final boolean selected) {
		multiSelectSelectedRows.set(row, selected);
	}
	
	protected void setMultiSelectReadonlyAt(final int row, final boolean readonly) {
		multiSelectReadonlyRows.set(row, readonly);
	}
	
	protected BitSet getMultiSelectReadonlyRows() {
		return multiSelectReadonlyRows;
	}
    
	/**
	 * @return true: don't check for dirt forms; false: check for dirty forms
	 *         (default)
	 */
	public boolean isSuppressDirtyFormWarning() {
		return suppressDirtyFormWarning;
	}
	
	/**
	 * When pressing an action of the table, the dirty form check is or is not
	 * check based on the specified setting.
	 * 
	 * @param suppressDirtyFormWarning true: don't check for dirt forms; false:
	 *          check for dirty forms (default)
	 */
	public void setSuppressDirtyFormWarning(boolean suppressDirtyFormWarning) {
		this.suppressDirtyFormWarning = suppressDirtyFormWarning;
	}


	
	protected int getSortColumn() {
		return sortColumn;
	}
	protected boolean getSortAscending() {
		return sortAscending;
	}

	public int getUnfilteredRowCount() {
		return tableDataModel.getRowCount();
	}

	public void setSearchString(final String tableSearchString) {
		this.tableSearchString = tableSearchString;
		if (isTableFiltered()) {
			buildFilteredTableDataModel(tableSearchString);
		}
	}

	private void buildFilteredTableDataModel(final String searchString) {
		List<Object> filteredElementList = new ArrayList<>();
		log.debug("buildFilteredTableDataModel: tableDataModel.getRowCount()={}", tableDataModel.getRowCount());
		if (tableDataModel.getRowCount() > 0) {
			log.debug("buildFilteredTableDataModel: tableDataModel.getObject(0)={}", tableDataModel.getObject(0));
		}
		for (int row = 0; row < tableDataModel.getRowCount(); row++) {
			if (matchRowWithSearchString(row, searchString)) {
				filteredElementList.add(tableDataModel.getObject(row));
			}
		}
		log.debug("buildFilteredTableDataModel: unfiltered-row-count={} filtered-row-count={}", tableDataModel.getRowCount(), filteredElementList.size());
		getFilteredTableDataModel().setObjects(filteredElementList);
	}

	/**
	 * Check if the row-value matches with the search-query.
	 * @param row
	 * @param tableSearchString2
	 * @return
	 */
	private boolean matchRowWithSearchString(final int row, final String tableSearchString2) {
		log.debug("matchRowWithFilter: row={} tableFilterString={}", row, tableSearchString2);
		if ( !isTableFiltered() ) {
			return true;
		}
		
		final String loweredSearchString = tableSearchString2.toLowerCase();
		
		// loop over all columns
		final TableDataModel unfilteredModel = getUnfilteredTableDataModel();
		
		Filter htmlFilter = FilterFactory.getHtmlTagsFilter();
		for (int colIndex = getColumnCountFromAllCDs(); colIndex-->0; ) {
			ColumnDescriptor cd = getColumnDescriptorFromAllCDs(colIndex);
			int dataColumn = cd.getDataColumn();
			if (dataColumn >= 0 && isColumnDescriptorVisible(cd)) {
				Object value = unfilteredModel.getValueAt(row, dataColumn);
				// When a CustomCellRenderer exist, use this to render cell-value to String
				if (cd instanceof CustomRenderColumnDescriptor) {
					CustomRenderColumnDescriptor cdrd = (CustomRenderColumnDescriptor)cd;
					CustomCellRenderer customCellRenderer = cdrd.getCustomCellRenderer();
					if (customCellRenderer instanceof CustomCssCellRenderer) {
						// For css renderers only use the hover
						// text, not the CSS class name and other
						// HTLM markup
						CustomCssCellRenderer cssRenderer = (CustomCssCellRenderer) customCellRenderer;
						value = cssRenderer.getHoverText(value);									
						if (!StringHelper.containsNonWhitespace((String) value)) {
							continue;
						}
					} else {
						StringOutput sb = StringOutputPool.allocStringBuilder(250);
						customCellRenderer.render(sb, null, value, cdrd.getLocale(), cd.getAlignment(), null);
						value = StringOutputPool.freePop(sb);
					}
				}
				
				if (value instanceof String) {
					String valueAsString = (String)value;
					// Remove any HTML markup from the value
					String valueAsHtml = htmlFilter.filter(valueAsString);
					// Finally compare with search value based on a simple lowercase match
					if (valueAsString.toLowerCase().contains(loweredSearchString)
							|| valueAsHtml.toLowerCase().contains(loweredSearchString)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getSearchString( ) {
		return tableSearchString;
	}

	public boolean isTableFiltered() {
		return tableSearchString != null;
	}

}

class ChoiceTableDataModel implements ChoiceModel {
	
	private boolean isMultiSelect;
	private List<ColumnDescriptor> allCDs;
	private List<ColumnDescriptor> columnOrder;
	private Translator translator;

	protected ChoiceTableDataModel(final boolean isMultiSelect, final List<ColumnDescriptor> allCDs, final List<ColumnDescriptor> columnOrder, final Translator translator) {
		this.isMultiSelect = isMultiSelect;
		this.allCDs = allCDs;
		this.columnOrder = columnOrder;
		this.translator = translator;
	}

	@Override
	public int getRowCount() {
		// if this is a multiselect table, we do not want the checkboxes of
		// the multiselect to be disabled. therefore we simply exclude the entire
		// checkbox row (which is at the very beginning of the CD array).
		if (isMultiSelect){
			return allCDs.size() - 1;
		} else {
			return allCDs.size();
		}
	}

	@Override
	public Boolean isEnabled(int row) {
		ColumnDescriptor cd = getObject(row);
		return columnOrder.contains(cd) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String getLabel(int row) {
		ColumnDescriptor cd = getObject(row);
		return cd.translateHeaderKey() ? translator.translate(cd.getHeaderKey()) : cd.getHeaderKey();
	}

	@Override
	public boolean isDisabled(int row) {
		return false;
	}
	
	public ColumnDescriptor getObject(int row) {
		return allCDs.get(isMultiSelect? (row + 1): row);
	}
}