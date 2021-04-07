/**
	public void setTableEmptyMessage(final String tableEmptyMessage) {
		this.tableEmptyMessage = tableEmptyMessage;
	}* OLAT - Online Learning and Training<br>
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

import org.olat.core.logging.AssertException;

/**
 * Description:<BR>
 * Use the table gui configuration to configure the tables gui options.
 * <P>
 * Initial Date:  Jan 18, 2005
 *
 * @author gnaegi 
 */
public class TableGuiConfiguration {
	private static final TableExporter DEFAULT_TABLE_EXPORTER = new DefaultXlsTableExporter();

	private boolean downloadOffered = true;
	private boolean displayRowCount = true;
	
	private boolean preferencesOffered = false;
	private String preferencesKey;
	
	private boolean displayTableHeader = true;
	private boolean displayTableGrid = false; // default
	private boolean sortingEnabled = true;
	private boolean selectedRowUnselectable = false;
	private boolean multiSelect = false;
	private boolean suppressDirtyFormWarning = false;

	private boolean pageingEnabled = true;
	private int resultsPerPage = 20;

	private String tableEmptyMessage; 	// default value set by table controller (i18n)
	private String tableEmptyHint;		// optional
	private String tableEmtpyIconCss;	// defaults value set by table controller
	private String tableEmptyPrimaryAction; 		// optional
	private String tableEmptyPrimaryActionIconCss;	// optional
	
	private String customCssClass = null; // default is empty
	private boolean showAllLinkEnabled = true;
	private TableExporter tableExporter;
	
	/**
	 * Constructor for a table gui configuration object. The constructor
	 * initializes the configuration with the default values
	 */
	public TableGuiConfiguration() {
		super();
		setDownloadOffered(true);
	}

	/**
	 * Should a download button be offered to the user
	 * @param b 
	 */
	public final void setDownloadOffered(final boolean b) {
		if(b){
			setDownloadOffered(DEFAULT_TABLE_EXPORTER);
		}else{
			removeDownloadOffered();
		}
	}
	
	public void setDownloadOffered(final TableExporter tableExporterParam){
		assert(tableExporterParam != null);
		downloadOffered = true;
		tableExporter = tableExporterParam;
	}
	
	public void removeDownloadOffered(){
		downloadOffered = false;
		tableExporter = null;
	}

	/**
	 * Should the number of rows be displayed?
	 * @param displayRowCount
	 */
	public void setDisplayRowCount(final boolean displayRowCount) {
		this.displayRowCount = displayRowCount;
	}

	/**
	 * @return The key under which the gui preferences are stored
	 */
	public String getPreferencesKey() {
		return preferencesKey;
	}
	/**
	 * @return boolean true: show number of rows
	 */
	public boolean isDisplayRowCount() {
		return displayRowCount;
	}
	/**
	 * @return boolean true: offer download link
	 */
	public boolean isDownloadOffered() {
		return downloadOffered;
	}
	/**
	 * @return boolean true: show the table header 
	 */
	public boolean isDisplayTableHeader() {
		return displayTableHeader;
	}
	/**
	 * @param displayTableHeader true: show the table header 
	 */
	public void setDisplayTableHeader(final boolean displayTableHeader) {
		this.displayTableHeader = displayTableHeader;
	}
	/**
	 * @return boolean true: use pageing
	 */
	public boolean isPageingEnabled() {
		return pageingEnabled;
	}
	/**
	 * @param pageingEnabled true: use pageing
	 */
	public void setPageingEnabled(final boolean pageingEnabled) {
		this.pageingEnabled = pageingEnabled;
	}
	/**
	 * @return int the number of results diplayed per page
	 */
	public int getResultsPerPage() {
		return resultsPerPage;
	}
	/**
	 * @param resultsPerPage the number of results diplayed per page
	 */
	public void setResultsPerPage(final int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}
	/**
	 * @return boolean true: the selected row can't be selected anymore
	 */
	public boolean isSelectedRowUnselectable() {
		return selectedRowUnselectable;
	}
	/**
	 * @param selectedRowUnselectable true: the selected row can't be selected anymore
	 */
	public void setSelectedRowUnselectable(final boolean selectedRowUnselectable) {
		this.selectedRowUnselectable = selectedRowUnselectable;
	}
	/**
	 * @return boolean true: columns can be sorted
	 */
	public boolean isSortingEnabled() {
		return sortingEnabled;
	}
	/**
	 * @param sortingEnabled true: columns can be sorted
	 */
	public void setSortingEnabled(final boolean sortingEnabled) {
		this.sortingEnabled = sortingEnabled;
	}
	/**
	 * @return boolean true: table preferences are enabled
	 */
	public boolean isPreferencesOffered() {
		return preferencesOffered;
	}
	/**
	 * @param preferencesOffered true: table preferences are enabled
	 * @param preferencesKey name of table preferences
	 */
	public void setPreferencesOffered(final boolean preferencesOffered, final String preferencesKey) {
		this.preferencesOffered = preferencesOffered;
		if (preferencesOffered && preferencesKey == null) {
			throw new AssertException("when perferencesOffered=true the preferencesKey must not be null !");
		}
		this.preferencesKey = preferencesKey;
	}
	/**
	 * @return String Messaged displayed then table model is empty
	 */
	public String getTableEmptyMessage() {
		return tableEmptyMessage;
	}
	/**
	 * @return String Hint message displayed then table model is empty
	 */
	public String getTableEmptyHint() {
		return tableEmptyHint;
	}	
	/**
	 * @return String CssClass displayed then table model is empty that represents
	 *         the objects of the table
	 */
	public String getTableEmptyIconCss() {
		return tableEmtpyIconCss;
	}
	
	/**
	 * @param tableEmptyMessage Messaged displayed then table model is empty or null
	 *                          for default message
	 * @param tableEmptyHint    An additional hint displayed below the message to
	 *                          indicate why the table is empty and what the user
	 *                          can do about it
	 * @param tableEmtpyIconCss An icon that represents the objects in the table
	 */
	public void setTableEmptyMessage(final String tableEmptyMessage, final String tableEmptyHint, final String tableEmtpyIconCss) {
		this.tableEmptyMessage = tableEmptyMessage;
		this.tableEmptyHint = tableEmptyHint;
		this.tableEmtpyIconCss = tableEmtpyIconCss;
	}
	
	/**
	 * @return String Button label of the next primary action in case the table is
	 *         empty
	 */
	public String getTableEmptyPrimaryAction() {
		return tableEmptyPrimaryAction;
	}
	
	/**
	 * @return String CssClass of the button of next primary action in case the
	 *         table is empty
	 */
	public String getTableEmptyPrimaryActionIconCss() {
		return tableEmptyPrimaryActionIconCss;
	}
	
	/**
	 * Show a button below the empty state message with the next primary action for
	 * the user. Default is to not have such an action.
	 * 
	 * @param tableEmptyPrimaryAction
	 * @param tableEmptyPrimaryActionIconCss
	 */
	public void setTableEmptyNextPrimaryAction(final String tableEmptyPrimaryAction, final String tableEmptyPrimaryActionIconCss) {
		this.tableEmptyPrimaryAction = tableEmptyPrimaryAction;
		this.tableEmptyPrimaryActionIconCss = tableEmptyPrimaryActionIconCss;
	}
	/**
	 * @return String the additional CSS class used when rendering the table
	 */
	public String getCustomCssClass() {
		return customCssClass;
	}
	/**
	 * @param cssPrefix the additional CSS class used when rendering the table
	 */
	public void setCustomCssClass(final String customCssClass) {
		this.customCssClass = customCssClass;
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}

	public void setMultiSelect(final boolean multiselect) {
		this.multiSelect = multiselect;
	}

	public void setShowAllLinkEnabled(final boolean showAllLinkEnabled) {
		this.showAllLinkEnabled  = showAllLinkEnabled;
	}

	public boolean isShowAllLinkEnabled() {
		return showAllLinkEnabled;
	}

	public TableExporter getDownloadOffered() {
		assert((tableExporter != null) && downloadOffered);
		return tableExporter;
	}

	/**
	 * Option to display a table grid
	 * @param enabled true: show the table grid; false: don't show table grid (default)
	 */
	public void setDisplayTableGrid(boolean enabled) {
		this.displayTableGrid = enabled;		
	}
	/**
	 * @return true: show the table grid; false: don't show table grid (default)
	 */
	public boolean isDisplayTableGrid() {
		return this.displayTableGrid;
	}

	public boolean isSuppressDirtyFormWarning() {
		return suppressDirtyFormWarning;
	}

	public void setSuppressDirtyFormWarning(boolean suppressDirtyFormWarning) {
		this.suppressDirtyFormWarning = suppressDirtyFormWarning;
	}

}
