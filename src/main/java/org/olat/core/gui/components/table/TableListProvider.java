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
package org.olat.core.gui.components.table;

import java.util.Set;
import java.util.TreeSet;

import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;

/**
 * 
 * Initial date: 05.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableListProvider implements ListProvider {

	/**
	 * Limit the number of search-suggestions in table-search-popup
	 */
	private static final int MAX_TABLE_SEARCH_RESULT_ENTRIES = 15;
	
	private final Table table;
	
	public TableListProvider(Table table) {
		this.table = table;
	}

	@Override
	public int getMaxEntries() {
		return MAX_TABLE_SEARCH_RESULT_ENTRIES;
	}

	@Override
	public void getResult(String searchValue, ListReceiver receiver) {
		Filter htmlFilter = FilterFactory.getHtmlTagsFilter();
		Set<String> searchEntries = new TreeSet<>();
		int entryCounter = 1;
		// loop over whole data-model
		String lowerSearchValue = searchValue.toLowerCase();
		
		TableDataModel<?> unfilteredModel = table.getUnfilteredTableDataModel();
		
		int rowCount = unfilteredModel.getRowCount();
		int colCount = table.getColumnCountFromAllCDs();
		
		a_a:
		for (int colIndex=0; colIndex < colCount; colIndex++) {
			ColumnDescriptor cd = table.getColumnDescriptorFromAllCDs(colIndex);
			int dataColumn = cd.getDataColumn();
			if (dataColumn >= 0 && table.isColumnDescriptorVisible(cd)) {
				for (int rowIndex=0; rowIndex < rowCount; rowIndex++) {
					Object obj = unfilteredModel.getValueAt(rowIndex, dataColumn);
					// When a CustomCellRenderer exist, use this to render cell-value to String
					if (cd instanceof CustomRenderColumnDescriptor) {
						CustomRenderColumnDescriptor crcd = (CustomRenderColumnDescriptor)cd;
						CustomCellRenderer customCellRenderer = crcd.getCustomCellRenderer();
						if (customCellRenderer instanceof CustomCssCellRenderer) {
							// For css renderers only use the hover
							// text, not the CSS class name and other
							// markup
							CustomCssCellRenderer cssRenderer = (CustomCssCellRenderer) customCellRenderer;
							obj = cssRenderer.getHoverText(obj);									
							if (!StringHelper.containsNonWhitespace((String) obj)) {
								continue;
							}
						} else {
							StringOutput sb = StringOutputPool.allocStringBuilder(250);
							customCellRenderer.render(sb, null, obj, crcd.getLocale(), cd.getAlignment(), null);
							obj = StringOutputPool.freePop(sb);																		
						}
					} 

					if (obj instanceof String) {
						String valueString = (String)obj;

						// Remove any HTML markup from the value
						String filteredValueString = htmlFilter.filter(valueString);
						// Finally compare with search value based on a simple lowercase match
						if (filteredValueString.toLowerCase().contains(lowerSearchValue)
								|| valueString.toLowerCase().contains(lowerSearchValue)) {
							if (searchEntries.add(valueString) ) {
								// Add to receiver list same entries only once
								if (searchEntries.size() == 1) {
									// before first entry, add searchValue. But add only when one search match
									receiver.addEntry(searchValue, searchValue);
								}
								// limit the number of entries
								if (entryCounter++ > MAX_TABLE_SEARCH_RESULT_ENTRIES) {
									receiver.addEntry("...", "...");
									break a_a;
								}
								String val = StringHelper.xssScan(valueString);
								receiver.addEntry(val, val);
							}								
						}
					}
				}
			}
		}
	}	
}