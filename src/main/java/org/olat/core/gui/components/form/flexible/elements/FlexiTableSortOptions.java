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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;

/**
 * 
 * Initial date: 27.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableSortOptions {
	
	private SortKey defaultOrderBy;
	private boolean fromColumnModel;
	private boolean openAllBySort = false;
	private List<FlexiTableSort> sorts;
	
	public FlexiTableSortOptions() {
		sorts = Collections.emptyList();
		fromColumnModel = false;
	}
	
	public FlexiTableSortOptions(boolean fromColumnModel) {
		this.sorts = Collections.emptyList();
		this.fromColumnModel = fromColumnModel;
	}
	
	public FlexiTableSortOptions(boolean fromColumnModel, SortKey defaultOrderBy) {
		this(fromColumnModel);
		this.defaultOrderBy = defaultOrderBy;
	}
	
	public FlexiTableSortOptions(List<FlexiTableSort> sorts) {
		this.sorts = sorts;
		fromColumnModel = false;
	}
	
	public boolean isFromColumnModel() {
		return fromColumnModel;
	}
	
	/**
	 * Reopen a tree model before sorting.
	 * 
	 * @return true if reopen a tree model before sorting
	 */
	public boolean isOpenAllBySort() {
		return openAllBySort;
	}
	
	/**
	 * @param openAllBySort true if you want to open all the model before sorting it
	 */
	public void setOpenAllBySort(boolean openAllBySort) {
		this.openAllBySort = openAllBySort;
	}
	
	/**
	 * @return true if a default order is set
	 */
	public boolean hasDefaultOrderBy() {
		return defaultOrderBy != null;
	}

	public SortKey getDefaultOrderBy() {
		return defaultOrderBy;
	}

	public void setDefaultOrderBy(SortKey defaultOrderBy) {
		this.defaultOrderBy = defaultOrderBy;
	}

	public void setFromColumnModel(boolean fromColumnModel) {
		this.fromColumnModel = fromColumnModel;
	}
	
	public List<FlexiTableSort> getSorts() {
		return sorts;
	}
	
	public void setSorts(List<FlexiTableSort> sorts) {
		this.sorts = sorts;
	}
}