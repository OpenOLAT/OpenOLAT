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
package org.olat.core.gui.components.form.flexible.impl.elements.table.tab;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;

/**
 * 
 * Initial date: 10 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterTabPreset extends FlexiFiltersTabImpl implements FlexiFiltersPreset {
	
	private List<String> defaultFilters;
	private List<String> implicitFilters;
	private List<FlexiTableFilterValue> filtersValues;
	
	public FlexiFilterTabPreset(String id, String label,
			List<String> defaultFilters) {
		super(id, label);
		this.defaultFilters = (defaultFilters == null ? null : List.copyOf(defaultFilters));
	}
	
	public static FlexiFilterTabPreset presetWithImplicitFilters(String id, String label,
			List<String> defaultFilters, List<FlexiTableFilterValue> implicitValueFilters) {
		FlexiFilterTabPreset preset = new FlexiFilterTabPreset(id, label, defaultFilters);
		
		List<String> implicitFilters = new ArrayList<>(implicitValueFilters.size());
		for(FlexiTableFilterValue implicitValueFilter:implicitValueFilters) {
			implicitFilters.add(implicitValueFilter.getFilter());
		}
		preset.implicitFilters = implicitFilters;
		preset.filtersValues = implicitValueFilters;
		return preset;
	}

	@Override
	public List<String> getDefaultFilters() {
		return defaultFilters;
	}

	@Override
	public List<String> getImplicitFilters() {
		return implicitFilters;
	}

	@Override
	public List<FlexiTableFilterValue> getDefaultFiltersValues() {
		return filtersValues;
	}
}
