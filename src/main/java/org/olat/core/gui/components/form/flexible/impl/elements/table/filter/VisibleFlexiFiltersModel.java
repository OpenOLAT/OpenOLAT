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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.choice.ChoiceModel;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;

/**
 * 
 * Initial date: 29 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class VisibleFlexiFiltersModel implements ChoiceModel<FlexiTableFilter> {
	
	private final List<FlexiTableFilter> filters;
	private final Set<FlexiTableFilter> enabledFilters;
	
	public VisibleFlexiFiltersModel(List<FlexiTableFilter> filters, List<FlexiTableFilter> enabledFilters) {
		this.filters = filters;
		this.enabledFilters = new HashSet<>(enabledFilters);
	}

	@Override
	public int getRowCount() {
		return filters == null ? 0 : filters.size();
	}

	@Override
	public Boolean isEnabled(int row) {
		FlexiTableFilter filter = getObject(row);
		return filter != null && enabledFilters.contains(filter) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String getLabel(int row) {
		FlexiTableFilter filter = getObject(row);
		return filter == null ? "-" : filter.getLabel();
	}

	@Override
	public boolean isDisabled(int row) {
		FlexiTableFilter filter = getObject(row);
		return filter == null || filter.isAlwaysVisible();
	}

	@Override
	public FlexiTableFilter getObject(int row) {
		if(filters == null) return null;
		return filters.get(row);
	}
}
