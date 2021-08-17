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

import java.util.List;

import org.olat.core.gui.components.choice.ChoiceModel;

/**
 * 
 * Initial date: 29 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class VisibleFlexiFiltersModel implements ChoiceModel<FlexiFilterButton> {
	
	private final List<FlexiFilterButton> filters;
	
	public VisibleFlexiFiltersModel(List<FlexiFilterButton> filters) {
		this.filters = filters;
	}

	@Override
	public int getRowCount() {
		return filters == null ? 0 : filters.size();
	}

	@Override
	public Boolean isEnabled(int row) {
		FlexiFilterButton filter = getObject(row);
		return filter != null && filter.isEnabled() ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String getLabel(int row) {
		FlexiFilterButton filter = getObject(row);
		return filter == null ? "-" : filter.getFilter().getLabel();
	}

	@Override
	public boolean isDisabled(int row) {
		FlexiFilterButton filter = getObject(row);
		return filter == null || filter.isImplicit();
	}

	@Override
	public FlexiFilterButton getObject(int row) {
		if(filters == null) return null;
		return filters.get(row);
	}
}
