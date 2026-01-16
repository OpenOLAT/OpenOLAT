/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.generic.dashboard;

import java.io.Serializable;
import java.util.Set;

/**
 * 
 * Initial date: Jan 15, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TableWidgetConfigPrefs implements Serializable {
	
	private static final long serialVersionUID = 6498267090046033407L;
	
	public enum FilterType {relevant, custom}
	
	private Set<String> keyFigureKeys;
	private FilterType filterType;
	private Set<String> filterFigureKeys;
	private int numRows;
	
	public Set<String> getKeyFigureKeys() {
		return keyFigureKeys;
	}

	public void setKeyFigureKeys(Set<String> keyFigureKeys) {
		this.keyFigureKeys = keyFigureKeys;
	}
	
	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	public Set<String> getFilterFigureKeys() {
		return filterFigureKeys;
	}

	public void setFilterFigureKeys(Set<String> filterFigureKeys) {
		this.filterFigureKeys = filterFigureKeys;
	}

	public int getNumRows() {
		return numRows;
	}
	
	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}
	
}
